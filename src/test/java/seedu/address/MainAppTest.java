package seedu.address;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.UserPrefs;
import seedu.address.model.util.SampleDataUtil;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

public class MainAppTest {

    @TempDir
    public Path testFolder;

    private MainApp mainApp;
    private UserPrefs userPrefs;

    @BeforeEach
    public void setUp() {
        mainApp = new MainApp();
        userPrefs = new UserPrefs();
    }

    @Test
    public void initModelManager_missingFile_loadsSampleDataAndNoBackup() {
        Path missingPath = testFolder.resolve("missing.json");
        Path prefsPath = testFolder.resolve("prefs.json");
        StorageManager storageManager = new StorageManager(
                new JsonAddressBookStorage(missingPath),
                new JsonUserPrefsStorage(prefsPath));

        Model model = mainApp.initModelManager(storageManager, userPrefs);

        assertNull(mainApp.getInitialStatusMessage());
        assertEquals(SampleDataUtil.getSampleAddressBook(), new AddressBook(model.getAddressBook()));
    }

    @Test
    public void initModelManager_validFile_loadsDataAndNoBackup() throws Exception {
        Path validPath = testFolder.resolve("valid.json");
        Path prefsPath = testFolder.resolve("prefs.json");
        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(validPath);
        AddressBook originalAddressBook = new AddressBook();
        addressBookStorage.saveAddressBook(originalAddressBook);

        StorageManager storageManager = new StorageManager(
                addressBookStorage,
                new JsonUserPrefsStorage(prefsPath));

        Model model = mainApp.initModelManager(storageManager, userPrefs);

        assertNull(mainApp.getInitialStatusMessage());
        assertEquals(originalAddressBook, new AddressBook(model.getAddressBook()));
    }

    @Test
    public void initModelManager_corruptedFile_createsBackupAndSetsWarning() throws Exception {
        Path corruptedPath = testFolder.resolve("corrupted.json");
        Path prefsPath = testFolder.resolve("prefs.json");
        String corruptedContent = "{ invalid json content: 123";
        Files.writeString(corruptedPath, corruptedContent);

        StorageManager storageManager = new StorageManager(
                new JsonAddressBookStorage(corruptedPath),
                new JsonUserPrefsStorage(prefsPath));

        Model model = mainApp.initModelManager(storageManager, userPrefs);

        String warningMessage = mainApp.getInitialStatusMessage();
        assertTrue(warningMessage != null && !warningMessage.isBlank());
        assertTrue(warningMessage.startsWith("Warning: Data file "));
        assertTrue(warningMessage.contains("is corrupted. A backup was created at "));
        assertTrue(warningMessage.endsWith("Starting with an empty contact book."));

        // Verify model starts with an empty address book
        assertEquals(new AddressBook(), new AddressBook(model.getAddressBook()));

        // Verify backup file exists in the folder
        boolean backupFound = false;
        try (var stream = Files.list(testFolder)) {
            for (Path file : stream.toList()) {
                if (file.getFileName().toString().startsWith("corrupted.json.backup.")) {
                    backupFound = true;
                    assertEquals(corruptedContent, Files.readString(file));
                }
            }
        }
        assertTrue(backupFound);
    }

    @Test
    public void initModelManager_corruptedFileBackupThrowsIoException_setsFallbackWarningAndStartsEmpty()
            throws Exception {
        Path corruptedPath = testFolder.resolve("corrupted_io_exception.json");
        Path prefsPath = testFolder.resolve("prefs.json");
        Files.writeString(corruptedPath, "corrupted content");

        JsonAddressBookStorage faultyStorage = new JsonAddressBookStorage(corruptedPath) {
            @Override
            public Path backupAddressBookFile(Path filePath) throws IOException {
                throw new IOException("Simulated disk write error");
            }
        };
        StorageManager storageManager = new StorageManager(faultyStorage, new JsonUserPrefsStorage(prefsPath));

        Model model = mainApp.initModelManager(storageManager, userPrefs);

        String warningMessage = mainApp.getInitialStatusMessage();
        assertTrue(warningMessage != null && !warningMessage.isBlank());
        assertTrue(warningMessage.startsWith("Warning: Data file "));
        assertTrue(warningMessage.contains("is corrupted, and creating a backup failed: Simulated disk write error."));
        assertTrue(warningMessage.endsWith("Starting with an empty contact book."));
        assertEquals(new AddressBook(), new AddressBook(model.getAddressBook()));
    }
}
