package seedu.address.commons.util;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Writes and reads files
 */
public class FileUtil {

    private static final String CHARSET = "UTF-8";

    /**
     * Functional interface for file move operations to facilitate testing.
     */
    @FunctionalInterface
    interface FileMover {
        void move(Path source, Path target, CopyOption... options) throws IOException;
    }

    /**
     * Creates a file if it does not exist along with its missing parent directories.
     * @throws IOException if the file or directory cannot be created.
     */
    public static void createIfMissing(Path file) throws IOException {
        if (Files.exists(file)) {
            return;
        }

        createParentDirsOfFile(file);

        Files.createFile(file);
    }

    /**
     * Creates parent directories of file if it has a parent directory
     */
    private static void createParentDirsOfFile(Path file) throws IOException {
        Path parentDir = file.getParent();

        if (parentDir != null) {
            Files.createDirectories(parentDir);
        }
    }

    /**
     * Assumes file exists
     */
    public static String readFromFile(Path file) throws IOException {
        return new String(Files.readAllBytes(file), CHARSET);
    }

    /**
     * Writes given string to a file atomically.
     * Creates the file and any missing parent directories if they do not exist yet.
     *
     * @param file the target file to write to. Cannot be null.
     * @param content the string content to write. Cannot be null.
     * @throws IOException if an error occurs during writing or moving the file.
     */
    public static void writeToFile(Path file, String content) throws IOException {
        writeToFile(file, content, Files::move);
    }

    /**
     * Writes given string to a file atomically using the specified {@code FileMover}.
     */
    static void writeToFile(Path file, String content, FileMover fileMover) throws IOException {
        requireNonNull(file);
        requireNonNull(content);
        requireNonNull(fileMover);

        createParentDirsOfFile(file);

        Path parentDir = file.getParent();
        Path dir = parentDir == null ? Path.of(".") : parentDir;
        String prefix = "tmp_" + file.getFileName().toString() + ".";
        Path tempFile = Files.createTempFile(dir, prefix, ".tmp");

        try {
            Files.write(tempFile, content.getBytes(CHARSET));
            try {
                fileMover.move(tempFile, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                fileMover.move(tempFile, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

}
