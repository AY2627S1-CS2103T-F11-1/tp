package seedu.address;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ReadOnlyUserPrefs;
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
        assertTrue(warningMessage.contains("is corrupted ("));
        assertTrue(warningMessage.contains("). A backup was created at "));
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
        assertTrue(warningMessage.contains("is corrupted ("));
        assertTrue(warningMessage.contains("), and creating a backup failed: Simulated disk write error."));
        assertTrue(warningMessage.endsWith("Starting with an empty contact book."));
        assertEquals(new AddressBook(), new AddressBook(model.getAddressBook()));
    }

    @Test
    public void initPrefs_corruptedPrefsFile_createsBackupAndSetsWarning() throws Exception {
        Path corruptedPrefsPath = testFolder.resolve("corrupted_prefs.json");
        Files.writeString(corruptedPrefsPath, "invalid json content");
        JsonUserPrefsStorage prefsStorage = new JsonUserPrefsStorage(corruptedPrefsPath);

        UserPrefs resultPrefs = mainApp.initPrefs(prefsStorage);

        assertEquals(new UserPrefs(), resultPrefs);
        String warningMessage = mainApp.getInitialStatusMessage();
        assertTrue(warningMessage != null && !warningMessage.isBlank());
        assertTrue(warningMessage.startsWith("Warning: Preference file "));
        assertTrue(warningMessage.contains("is corrupted"));
        assertTrue(warningMessage.contains("A backup was created at "));
        assertTrue(warningMessage.endsWith("Using default preferences."));

        boolean backupFound = false;
        try (var stream = Files.list(testFolder)) {
            backupFound = stream.anyMatch(path ->
                    path.getFileName().toString().startsWith("corrupted_prefs.json.backup."));
        }
        assertTrue(backupFound);
    }

    @Test
    public void initPrefs_corruptedPrefsFileBackupThrowsIoException_setsFallbackWarning() throws Exception {
        Path corruptedPrefsPath = testFolder.resolve("corrupted_prefs_io_error.json");
        Files.writeString(corruptedPrefsPath, "invalid json content");

        JsonUserPrefsStorage faultyPrefsStorage = new JsonUserPrefsStorage(corruptedPrefsPath) {
            @Override
            public Path backupUserPrefsFile(Path filePath) throws IOException {
                throw new IOException("Disk failure during prefs backup");
            }
        };

        UserPrefs resultPrefs = mainApp.initPrefs(faultyPrefsStorage);

        assertEquals(new UserPrefs(), resultPrefs);
        String warningMessage = mainApp.getInitialStatusMessage();
        assertTrue(warningMessage != null && !warningMessage.isBlank());
        assertTrue(warningMessage.startsWith("Warning: Preference file "));
        assertTrue(warningMessage.contains("is corrupted"));
        assertTrue(warningMessage.contains("creating a backup failed: Disk failure during prefs backup"));
        assertTrue(warningMessage.endsWith("Using default preferences."));
    }

    @Test
    public void init_bothPrefsAndDataCorrupted_setsCombinedWarning() throws Exception {
        Path corruptedPrefsPath = testFolder.resolve("dual_corrupted_prefs.json");
        Path corruptedDataPath = testFolder.resolve("dual_corrupted_data.json");
        Files.writeString(corruptedPrefsPath, "invalid prefs content");
        Files.writeString(corruptedDataPath, "invalid data content");

        JsonUserPrefsStorage prefsStorage = new JsonUserPrefsStorage(corruptedPrefsPath);
        JsonAddressBookStorage dataStorage = new JsonAddressBookStorage(corruptedDataPath);
        StorageManager storageManager = new StorageManager(dataStorage, prefsStorage);

        UserPrefs initializedPrefs = mainApp.initPrefs(prefsStorage);
        mainApp.initModelManager(storageManager, initializedPrefs);

        String warningMessage = mainApp.getInitialStatusMessage();
        assertTrue(warningMessage != null && !warningMessage.isBlank());
        assertTrue(warningMessage.contains("Warning: Preference file "));
        assertTrue(warningMessage.contains("Warning: Data file "));
        assertTrue(warningMessage.contains("\n"));
    }

    @Test
    public void initPrefs_missingFile_returnsDefaultsAndSavesNewFile() {
        Path missingPrefsPath = testFolder.resolve("missing_prefs.json");
        JsonUserPrefsStorage prefsStorage = new JsonUserPrefsStorage(missingPrefsPath);

        UserPrefs initializedPrefs = mainApp.initPrefs(prefsStorage);

        assertEquals(new UserPrefs(), initializedPrefs);
        assertNull(mainApp.getInitialStatusMessage());
        assertTrue(Files.exists(missingPrefsPath));
    }

    @Test
    public void initPrefs_validFile_loadsCustomPrefsAndNoWarning() throws Exception {
        Path validPrefsPath = testFolder.resolve("valid_prefs.json");
        JsonUserPrefsStorage prefsStorage = new JsonUserPrefsStorage(validPrefsPath);
        UserPrefs customPrefs = new UserPrefs();
        customPrefs.setGuiSettings(new GuiSettings(1024, 768, 50, 50));
        prefsStorage.saveUserPrefs(customPrefs);

        UserPrefs initializedPrefs = mainApp.initPrefs(prefsStorage);

        assertEquals(customPrefs, initializedPrefs);
        assertNull(mainApp.getInitialStatusMessage());
    }

    @Test
    public void initPrefs_corruptedFileWithBlankCause_usesFallbackWarningText() throws Exception {
        Path corruptedPrefsPath = testFolder.resolve("blank_cause_prefs.json");
        Files.writeString(corruptedPrefsPath, "invalid");

        JsonUserPrefsStorage blankCauseStorage = new JsonUserPrefsStorage(corruptedPrefsPath) {
            @Override
            public Optional<UserPrefs> readUserPrefs() throws DataLoadingException {
                throw new DataLoadingException(new Exception("   "));
            }
        };

        UserPrefs resultPrefs = mainApp.initPrefs(blankCauseStorage);

        assertEquals(new UserPrefs(), resultPrefs);
        String warningMessage = mainApp.getInitialStatusMessage();
        assertTrue(warningMessage != null && !warningMessage.isBlank());
        assertTrue(warningMessage.contains("(unrecognized format)"));
    }

    @Test
    public void initPrefs_nonExistentFileThrowsDataLoadingException_doesNotAttemptBackup() {
        Path nonExistentPath = testFolder.resolve("non_existent_prefs.json");
        JsonUserPrefsStorage nonExistentFaultyStorage = new JsonUserPrefsStorage(nonExistentPath) {
            @Override
            public Optional<UserPrefs> readUserPrefs() throws DataLoadingException {
                throw new DataLoadingException(new Exception("Simulated read failure"));
            }
        };

        UserPrefs resultPrefs = mainApp.initPrefs(nonExistentFaultyStorage);

        assertEquals(new UserPrefs(), resultPrefs);
        assertNull(mainApp.getInitialStatusMessage());
    }

    @Test
    public void initPrefs_saveUserPrefsThrowsIoException_returnsPrefs() {
        Path validPrefsPath = testFolder.resolve("save_ioe_prefs.json");
        JsonUserPrefsStorage faultySaveStorage = new JsonUserPrefsStorage(validPrefsPath) {
            @Override
            public void saveUserPrefs(ReadOnlyUserPrefs prefs) throws IOException {
                throw new IOException("Simulated disk error");
            }
        };

        UserPrefs resultPrefs = mainApp.initPrefs(faultySaveStorage);

        assertEquals(new UserPrefs(), resultPrefs);
    }

    @Test
    public void version_isCorrect() {
        assertEquals("V1.6", MainApp.VERSION);
    }

    @Test
    public void init_executesSuccessfully() throws Exception {
        Path prefsFile = Path.of("preferences.json");
        boolean prefsExisted = Files.exists(prefsFile);
        byte[] originalPrefs = prefsExisted ? Files.readAllBytes(prefsFile) : null;
        try {
            mainApp.init();
        } finally {
            if (prefsExisted) {
                Files.write(prefsFile, originalPrefs);
            } else {
                Files.deleteIfExists(prefsFile);
            }
        }
    }

    @Test
    public void start_logsVersionAndDelegatesToUi() {
        boolean[] uiStarted = new boolean[1];
        mainApp.ui = new seedu.address.ui.Ui() {
            @Override
            public void start(javafx.stage.Stage primaryStage) {
                uiStarted[0] = true;
            }
        };

        mainApp.start(null);

        assertTrue(uiStarted[0]);
    }

    @Test
    public void stop_savesUserPrefs() throws Exception {
        Path prefsPath = testFolder.resolve("stop_prefs.json");
        Path addressBookPath = testFolder.resolve("stop_addressbook.json");
        StorageManager storageManager = new StorageManager(
                new JsonAddressBookStorage(addressBookPath),
                new JsonUserPrefsStorage(prefsPath));
        UserPrefs prefs = new UserPrefs();
        mainApp.storage = storageManager;
        mainApp.model = new seedu.address.model.ModelManager(new AddressBook(), prefs);

        mainApp.stop();

        assertTrue(Files.exists(prefsPath));
    }
}
