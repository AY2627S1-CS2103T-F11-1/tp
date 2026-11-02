package seedu.address.storage;

import static java.util.Objects.requireNonNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.ReadOnlyUserPrefs;
import seedu.address.model.UserPrefs;

/**
 * A class to access UserPrefs stored on the hard disk as a JSON file
 */
public class JsonUserPrefsStorage {

    private static final Logger logger = LogsCenter.getLogger(JsonUserPrefsStorage.class);

    private Path filePath;

    public JsonUserPrefsStorage(Path filePath) {
        this.filePath = filePath;
    }

    public Path getUserPrefsFilePath() {
        return filePath;
    }

    /**
     * Returns UserPrefs data from storage.
     * Returns {@code Optional.empty()} if storage file is not found.
     *
     * @throws DataLoadingException if the loading of data from preference file failed.
     */
    public Optional<UserPrefs> readUserPrefs() throws DataLoadingException {
        return readUserPrefs(filePath);
    }

    /**
     * Similar to {@link #readUserPrefs()}
     * @param prefsFilePath location of the data. Cannot be null.
     * @throws DataLoadingException if the file format is not as expected.
     */
    public Optional<UserPrefs> readUserPrefs(Path prefsFilePath) throws DataLoadingException {
        return JsonUtil.readJsonFile(prefsFilePath, UserPrefs.class);
    }

    /**
     * Saves the given {@link seedu.address.model.ReadOnlyUserPrefs} to the storage.
     * @param userPrefs cannot be null.
     * @throws IOException if there was any problem writing to the file.
     */
    public void saveUserPrefs(ReadOnlyUserPrefs userPrefs) throws IOException {
        JsonUtil.saveJsonFile(userPrefs, filePath);
    }

    /**
     * Creates a timestamped backup of the user prefs file at {@link #getUserPrefsFilePath()}.
     *
     * @return the {@code Path} of the created backup file.
     * @throws IOException if an error occurs while creating the backup.
     */
    public Path backupUserPrefsFile() throws IOException {
        return backupUserPrefsFile(filePath);
    }

    /**
     * Creates a timestamped backup of the user prefs file at the specified {@code filePath}.
     *
     * @param filePath location of the preferences file to back up. Cannot be null.
     * @return the {@code Path} of the created backup file.
     * @throws IOException if an error occurs while creating the backup.
     */
    public Path backupUserPrefsFile(Path filePath) throws IOException {
        requireNonNull(filePath);

        if (!Files.exists(filePath)) {
            throw new FileNotFoundException("Cannot backup non-existent file: " + filePath);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String timestamp = LocalDateTime.now().format(formatter);
        String baseBackupName = filePath.getFileName().toString() + ".backup." + timestamp;
        Path parent = filePath.getParent();
        Path backupPath = parent == null ? Path.of(baseBackupName) : parent.resolve(baseBackupName);

        int counter = 1;
        while (Files.exists(backupPath)) {
            String collisionName = baseBackupName + "_" + counter;
            backupPath = parent == null ? Path.of(collisionName) : parent.resolve(collisionName);
            counter++;
        }

        Files.copy(filePath, backupPath);
        logger.info("Created backup of user preferences file from " + filePath + " to " + backupPath);
        return backupPath;
    }

}
