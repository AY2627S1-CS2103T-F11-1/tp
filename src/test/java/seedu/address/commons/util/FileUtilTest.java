package seedu.address.commons.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class FileUtilTest {

    @TempDir
    public Path testFolder;

    @Test
    public void writeToFile_nullFile_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> FileUtil.writeToFile(null, "content"));
    }

    @Test
    public void writeToFile_nullContent_throwsNullPointerException() {
        Path file = testFolder.resolve("test.txt");
        assertThrows(NullPointerException.class, () -> FileUtil.writeToFile(file, null));
    }

    @Test
    public void writeToFile_newFile_success() throws IOException {
        Path file = testFolder.resolve("newFile.txt");
        String content = "Hello World!";

        FileUtil.writeToFile(file, content);

        assertTrue(Files.exists(file));
        assertEquals(content, FileUtil.readFromFile(file));
        assertNoTempFilesRemain(testFolder);
    }

    @Test
    public void writeToFile_nestedDirectory_createsDirectoriesAndWrites() throws IOException {
        Path file = testFolder.resolve("nested").resolve("dir").resolve("file.txt");
        String content = "Nested content";

        FileUtil.writeToFile(file, content);

        assertTrue(Files.exists(file));
        assertEquals(content, FileUtil.readFromFile(file));
        assertNoTempFilesRemain(file.getParent());
    }

    @Test
    public void writeToFile_existingFile_overwritesAtomically() throws IOException {
        Path file = testFolder.resolve("existingFile.txt");
        FileUtil.writeToFile(file, "initial content");

        String updatedContent = "updated content";
        FileUtil.writeToFile(file, updatedContent);

        assertTrue(Files.exists(file));
        assertEquals(updatedContent, FileUtil.readFromFile(file));
        assertNoTempFilesRemain(testFolder);
    }

    @Test
    public void writeToFile_pathWithoutParent_success() throws IOException {
        Path relativePath = Path.of("temp_relative_write_test.txt");
        try {
            String content = "Relative file content";
            FileUtil.writeToFile(relativePath, content);

            assertTrue(Files.exists(relativePath));
            assertEquals(content, FileUtil.readFromFile(relativePath));
        } finally {
            Files.deleteIfExists(relativePath);
        }
    }

    @Test
    public void createIfMissing_missingFile_createsFileAndParentDirs() throws IOException {
        Path file = testFolder.resolve("sub").resolve("missing.txt");
        assertFalse(Files.exists(file));

        FileUtil.createIfMissing(file);

        assertTrue(Files.exists(file));
    }

    @Test
    public void createIfMissing_existingFile_doesNotModify() throws IOException {
        Path file = testFolder.resolve("existing.txt");
        FileUtil.writeToFile(file, "data");

        FileUtil.createIfMissing(file);

        assertEquals("data", FileUtil.readFromFile(file));
    }

    @Test
    public void readFromFile_validFile_returnsContent() throws IOException {
        Path file = testFolder.resolve("readTest.txt");
        String expectedContent = "Testing readFromFile\nwith newline.";
        Files.writeString(file, expectedContent);

        String actualContent = FileUtil.readFromFile(file);

        assertEquals(expectedContent, actualContent);
    }

    @Test
    public void writeToFile_nullFileMover_throwsNullPointerException() {
        Path file = testFolder.resolve("test.txt");
        assertThrows(NullPointerException.class, () -> FileUtil.writeToFile(file, "content", null));
    }

    @Test
    public void writeToFile_atomicMoveNotSupported_fallsBackToReplaceExisting() throws IOException {
        Path file = testFolder.resolve("fallback.txt");
        String content = "Fallback content";
        AtomicBoolean fallbackCalled = new AtomicBoolean(false);

        FileUtil.FileMover mover = (source, target, options) -> {
            boolean hasAtomicMove = Arrays.asList(options).contains(StandardCopyOption.ATOMIC_MOVE);
            if (hasAtomicMove) {
                throw new AtomicMoveNotSupportedException(
                        source.toString(), target.toString(), "Atomic move unsupported");
            }
            fallbackCalled.set(true);
            Files.move(source, target, options);
        };

        FileUtil.writeToFile(file, content, mover);

        assertTrue(fallbackCalled.get());
        assertTrue(Files.exists(file));
        assertEquals(content, FileUtil.readFromFile(file));
        assertNoTempFilesRemain(testFolder);
    }

    @Test
    public void writeToFile_moveThrowsIoException_cleansUpTempFileAndThrowsException() {
        Path file = testFolder.resolve("ioError.txt");
        String content = "Error content";

        FileUtil.FileMover mover = (source, target, options) -> {
            throw new IOException("Simulated disk error during move");
        };

        assertThrows(IOException.class, () -> FileUtil.writeToFile(file, content, mover));
        assertFalse(Files.exists(file));
        try {
            assertNoTempFilesRemain(testFolder);
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    @Test
    public void constructor_instantiatesWithoutError() {
        assertNotNull(new FileUtil());
    }

    private void assertNoTempFilesRemain(Path directory) throws IOException {
        try (var stream = Files.list(directory)) {
            List<Path> tempFiles = stream
                    .filter(path -> path.getFileName().toString().endsWith(".tmp"))
                    .toList();
            assertTrue(tempFiles.isEmpty(), "Unexpected temporary files found: " + tempFiles);
        }
    }
}
