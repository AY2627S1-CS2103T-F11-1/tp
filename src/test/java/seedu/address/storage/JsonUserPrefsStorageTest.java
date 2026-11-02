package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.model.UserPrefs;

public class JsonUserPrefsStorageTest {

    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonUserPrefsStorageTest");

    @TempDir
    public Path testFolder;

    @Test
    public void getUserPrefsFilePath() {
        Path filePath = Paths.get("preferences.json");
        JsonUserPrefsStorage storage = new JsonUserPrefsStorage(filePath);
        assertEquals(filePath, storage.getUserPrefsFilePath());
    }

    @Test
    public void readUserPrefs_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> readUserPrefs(null));
    }

    private Optional<UserPrefs> readUserPrefs(String userPrefsFileInTestDataFolder) throws DataLoadingException {
        Path prefsFilePath = addToTestDataPathIfNotNull(userPrefsFileInTestDataFolder);
        return new JsonUserPrefsStorage(prefsFilePath).readUserPrefs(prefsFilePath);
    }

    @Test
    public void readUserPrefs_missingFile_emptyResult() throws DataLoadingException {
        assertFalse(readUserPrefs("NonExistentFile.json").isPresent());
    }

    @Test
    public void readUserPrefs_notJsonFormat_exceptionThrown() {
        assertThrows(DataLoadingException.class, () -> readUserPrefs("NotJsonFormatUserPrefs.json"));
    }

    @Test
    public void readUserPrefs_nullFile_throwsDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readUserPrefs("NullUserPrefs.json"));
    }

    @Test
    public void readUserPrefs_nullGuiSettings_returnsDefaults() throws Exception {
        UserPrefs actual = readUserPrefs("NullGuiSettingsUserPrefs.json").get();
        assertEquals(new GuiSettings(), actual.getGuiSettings());
        assertEquals(new UserPrefs(), actual);

        Path tempFile = testFolder.resolve("reSavedPrefs.json");
        JsonUserPrefsStorage storage = new JsonUserPrefsStorage(tempFile);
        storage.saveUserPrefs(actual);
        UserPrefs reloaded = storage.readUserPrefs().get();
        assertEquals(new UserPrefs(), reloaded);
    }

    private Path addToTestDataPathIfNotNull(String userPrefsFileInTestDataFolder) {
        return userPrefsFileInTestDataFolder != null
                ? TEST_DATA_FOLDER.resolve(userPrefsFileInTestDataFolder)
                : null;
    }

    @Test
    public void readUserPrefs_fileInOrder_successfullyRead() throws DataLoadingException {
        UserPrefs expected = getTypicalUserPrefs();
        UserPrefs actual = readUserPrefs("TypicalUserPref.json").get();
        assertEquals(expected, actual);
    }

    @Test
    public void readUserPrefs_valuesMissingFromFile_defaultValuesUsed() throws DataLoadingException {
        UserPrefs actual = readUserPrefs("EmptyUserPrefs.json").get();
        assertEquals(new UserPrefs(), actual);
    }

    @Test
    public void readUserPrefs_extraValuesInFile_extraValuesIgnored() throws DataLoadingException {
        UserPrefs expected = getTypicalUserPrefs();
        UserPrefs actual = readUserPrefs("ExtraValuesUserPref.json").get();

        assertEquals(expected, actual);
    }

    private UserPrefs getTypicalUserPrefs() {
        UserPrefs userPrefs = new UserPrefs();
        userPrefs.setGuiSettings(new GuiSettings(1000, 500, 300, 100));
        return userPrefs;
    }

    @Test
    public void savePrefs_nullPrefs_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveUserPrefs(null, "SomeFile.json"));
    }

    @Test
    public void saveUserPrefs_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveUserPrefs(new UserPrefs(), null));
    }

    /**
     * Saves {@code userPrefs} at the specified {@code prefsFileInTestDataFolder} filepath.
     */
    private void saveUserPrefs(UserPrefs userPrefs, String prefsFileInTestDataFolder) {
        try {
            new JsonUserPrefsStorage(addToTestDataPathIfNotNull(prefsFileInTestDataFolder))
                    .saveUserPrefs(userPrefs);
        } catch (IOException ioe) {
            throw new AssertionError("There should not be an error writing to the file", ioe);
        }
    }

    @Test
    public void saveUserPrefs_allInOrder_success() throws DataLoadingException, IOException {

        UserPrefs original = new UserPrefs();
        original.setGuiSettings(new GuiSettings(1200, 200, 0, 2));

        Path pefsFilePath = testFolder.resolve("TempPrefs.json");
        JsonUserPrefsStorage jsonUserPrefsStorage = new JsonUserPrefsStorage(pefsFilePath);

        //Try writing when the file doesn't exist
        jsonUserPrefsStorage.saveUserPrefs(original);
        UserPrefs readBack = jsonUserPrefsStorage.readUserPrefs().get();
        assertEquals(original, readBack);

        //Try saving when the file exists
        original.setGuiSettings(new GuiSettings(5, 5, 5, 5));
        jsonUserPrefsStorage.saveUserPrefs(original);
        readBack = jsonUserPrefsStorage.readUserPrefs().get();
        assertEquals(original, readBack);
    }

    @Test
    public void backupUserPrefsFile_nullFilePath_throwsNullPointerException() {
        JsonUserPrefsStorage storage = new JsonUserPrefsStorage(testFolder.resolve("prefs.json"));
        assertThrows(NullPointerException.class, () -> storage.backupUserPrefsFile(null));
    }

    @Test
    public void backupUserPrefsFile_nonExistentFile_throwsFileNotFoundException() {
        Path nonExistent = testFolder.resolve("non_existent_prefs.json");
        JsonUserPrefsStorage storage = new JsonUserPrefsStorage(nonExistent);
        assertThrows(FileNotFoundException.class, storage::backupUserPrefsFile);
    }

    @Test
    public void backupUserPrefsFile_existingFile_backupCreatedSuccessfully() throws Exception {
        Path original = testFolder.resolve("original_prefs.json");
        Files.writeString(original, "{\"guiSettings\": null}");
        JsonUserPrefsStorage storage = new JsonUserPrefsStorage(original);

        Path backupPath = storage.backupUserPrefsFile();

        assertTrue(Files.exists(backupPath));
        assertTrue(backupPath.getFileName().toString().startsWith("original_prefs.json.backup."));
        assertEquals("{\"guiSettings\": null}", Files.readString(backupPath));
    }

    @Test
    public void backupUserPrefsFile_filenameCollision_appendsCounter() throws Exception {
        Path original = testFolder.resolve("collision_prefs.json");
        Files.writeString(original, "test content");
        JsonUserPrefsStorage storage = new JsonUserPrefsStorage(original);

        Path backup1 = storage.backupUserPrefsFile();
        Path backup2 = storage.backupUserPrefsFile();

        assertTrue(Files.exists(backup1));
        assertTrue(Files.exists(backup2));
        assertFalse(backup1.equals(backup2));
        assertTrue(backup2.getFileName().toString().contains("_1"));
    }

}
