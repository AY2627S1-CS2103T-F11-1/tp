package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_FOLLOW_UP_DATE;
import static seedu.address.logic.commands.CommandTestUtil.VALID_FOLLOW_UP_DESCRIPTION;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.HOON;
import static seedu.address.testutil.TypicalPersons.IDA;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class JsonAddressBookStorageTest {
    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonAddressBookStorageTest");

    @TempDir
    public Path testFolder;

    @Test
    public void readAddressBook_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> readAddressBook(null));
    }

    private java.util.Optional<ReadOnlyAddressBook> readAddressBook(String filePath) throws Exception {
        return new JsonAddressBookStorage(Paths.get(filePath)).readAddressBook(addToTestDataPathIfNotNull(filePath));
    }

    private Path addToTestDataPathIfNotNull(String prefsFileInTestDataFolder) {
        return prefsFileInTestDataFolder != null
                ? TEST_DATA_FOLDER.resolve(prefsFileInTestDataFolder)
                : null;
    }

    @Test
    public void read_missingFile_emptyResult() throws Exception {
        assertFalse(readAddressBook("NonExistentFile.json").isPresent());
    }

    @Test
    public void read_notJsonFormat_exceptionThrown() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("notJsonFormatAddressBook.json"));
    }

    @Test
    public void readAddressBook_invalidPersonAddressBook_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("invalidPersonAddressBook.json"));
    }

    @Test
    public void readAddressBook_invalidAndValidPersonAddressBook_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("invalidAndValidPersonAddressBook.json"));
    }

    @Test
    public void readAddressBook_blankUnicodeAddress_throwsDataLoadingException() throws Exception {
        Path filePath = testFolder.resolve("blankAddress.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);
        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(ALICE);
        String json = JsonUtil.toJsonString(new JsonSerializableAddressBook(addressBook));
        String[] addresses = {"", " ", "\u200B", "\u00A0", "\u2060", "\u2003", "\u200B\u00A0\u2060\u2003"};

        for (String address : addresses) {
            Files.writeString(filePath, json.replace(ALICE.getAddress().value, address));
            assertThrows(DataLoadingException.class, storage::readAddressBook);
        }
    }

    @Test
    public void readAddressBook_nullPersonAddressBook_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("nullPersonAddressBook.json"));
    }

    @Test
    public void readAndSaveAddressBook_allInOrder_success() throws Exception {
        Path filePath = testFolder.resolve("TempAddressBook.json");
        AddressBook original = getTypicalAddressBook();
        JsonAddressBookStorage jsonAddressBookStorage = new JsonAddressBookStorage(filePath);

        // Save in new file and read back
        jsonAddressBookStorage.saveAddressBook(original, filePath);
        ReadOnlyAddressBook readBack = jsonAddressBookStorage.readAddressBook(filePath).get();
        assertEquals(original, new AddressBook(readBack));

        // Modify data, overwrite existing file, and read back
        original.addPerson(HOON);
        original.removePerson(ALICE);
        jsonAddressBookStorage.saveAddressBook(original, filePath);
        readBack = jsonAddressBookStorage.readAddressBook(filePath).get();
        assertEquals(original, new AddressBook(readBack));

        // Save and read without specifying file path
        original.addPerson(IDA);
        jsonAddressBookStorage.saveAddressBook(original); // file path not specified
        readBack = jsonAddressBookStorage.readAddressBook().get(); // file path not specified
        assertEquals(original, new AddressBook(readBack));
    }

    @Test
    public void readAndSaveAddressBook_followUpChanges_preservesValues() throws Exception {
        Path filePath = testFolder.resolve("followUps.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);
        AddressBook addressBook = getTypicalAddressBook();

        Person original = addressBook.getPersonList().get(0);
        Person withFollowUp = new PersonBuilder(original)
                .withFollowUp(VALID_FOLLOW_UP_DATE, VALID_FOLLOW_UP_DESCRIPTION)
                .build();

        addressBook.setPerson(original, withFollowUp);
        storage.saveAddressBook(addressBook);

        assertEquals(addressBook, new AddressBook(storage.readAddressBook().orElseThrow()));

        Person replacement = new PersonBuilder(withFollowUp)
                .withFollowUp("9999-12-31", "Confirm revised quotation")
                .build();
        addressBook.setPerson(withFollowUp, replacement);
        storage.saveAddressBook(addressBook);

        assertEquals(addressBook, new AddressBook(storage.readAddressBook().orElseThrow()));

        Person withoutFollowUp = new PersonBuilder(replacement).withoutFollowUp().build();
        addressBook.setPerson(replacement, withoutFollowUp);
        storage.saveAddressBook(addressBook);

        assertEquals(addressBook, new AddressBook(storage.readAddressBook().orElseThrow()));
        assertTrue(storage.readAddressBook().orElseThrow().getPersonList().get(0).getFollowUp().isEmpty());
    }

    @Test
    public void readAddressBook_invalidFollowUp_throwsDataLoadingException() throws Exception {
        Path filePath = testFolder.resolve("invalidFollowUp.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);
        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(new PersonBuilder()
                .withFollowUp(VALID_FOLLOW_UP_DATE, VALID_FOLLOW_UP_DESCRIPTION)
                .build());

        String json = JsonUtil.toJsonString(new JsonSerializableAddressBook(addressBook));
        String invalidJson = json.replace(VALID_FOLLOW_UP_DATE, "2026-02-30");
        Files.writeString(filePath, invalidJson);

        assertThrows(DataLoadingException.class, storage::readAddressBook);
    }

    @Test
    public void readAddressBook_blankUnicodeFollowUpDescription_throwsDataLoadingException() throws Exception {
        Path filePath = testFolder.resolve("blankFollowUpDescription.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);
        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(new PersonBuilder()
                .withFollowUp(VALID_FOLLOW_UP_DATE, VALID_FOLLOW_UP_DESCRIPTION)
                .build());
        String json = JsonUtil.toJsonString(new JsonSerializableAddressBook(addressBook));
        String[] descriptions = {
            "", "   ", "\u2003", "\u200B", "\u00A0", "\u2060",
            "\u200B\u00A0\u2060", " \u200B \u00A0\u2003\u2060 "
        };

        for (String description : descriptions) {
            Files.writeString(filePath, json.replace(VALID_FOLLOW_UP_DESCRIPTION, description));
            assertThrows(DataLoadingException.class, storage::readAddressBook);
        }
    }

    @Test
    public void saveAddressBook_nullAddressBook_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveAddressBook(null, "SomeFile.json"));
    }

    /**
     * Saves {@code addressBook} at the specified {@code filePath}.
     */
    private void saveAddressBook(ReadOnlyAddressBook addressBook, String filePath) {
        try {
            new JsonAddressBookStorage(Paths.get(filePath))
                    .saveAddressBook(addressBook, addToTestDataPathIfNotNull(filePath));
        } catch (IOException ioe) {
            throw new AssertionError("There should not be an error writing to the file.", ioe);
        }
    }

    @Test
    public void saveAddressBook_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveAddressBook(new AddressBook(), null));
    }

    @Test
    public void backupAddressBookFile_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new JsonAddressBookStorage(Paths.get("somePath"))
                .backupAddressBookFile(null));
    }

    @Test
    public void backupAddressBookFile_missingFile_throwsFileNotFoundException() {
        Path missingPath = testFolder.resolve("NonExistent.json");
        assertThrows(FileNotFoundException.class, () -> new JsonAddressBookStorage(missingPath)
                .backupAddressBookFile(missingPath));
    }

    @Test
    public void backupAddressBookFile_validFile_createsBackupSuccessfully() throws Exception {
        Path filePath = testFolder.resolve("CorruptedAddressBook.json");
        String fileContent = "{ invalid json content }";
        Files.writeString(filePath, fileContent);

        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);
        Path backupPath = storage.backupAddressBookFile();

        assertTrue(Files.exists(backupPath));
        assertTrue(backupPath.getFileName().toString().matches(
                "CorruptedAddressBook\\.json\\.backup\\.\\d{8}_\\d{6}(_\\d+)?"));
        assertEquals(fileContent, Files.readString(backupPath));
    }

    @Test
    public void backupAddressBookFile_collision_resolvesWithCounter() throws Exception {
        Path filePath = testFolder.resolve("CorruptedAddressBookCollision.json");
        Files.writeString(filePath, "data");

        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);
        Path backup1 = storage.backupAddressBookFile();
        Path backup2 = storage.backupAddressBookFile();

        assertTrue(Files.exists(backup1));
        assertTrue(Files.exists(backup2));
        assertNotEquals(backup1, backup2);
    }

    @Test
    public void backupAddressBookFile_pathWithoutParent_createsBackupSuccessfully() throws Exception {
        Path relativePath = Path.of("temp_relative_corrupted.json");
        Path backup1 = null;
        Path backup2 = null;
        try {
            Files.writeString(relativePath, "corrupted content");
            JsonAddressBookStorage storage = new JsonAddressBookStorage(relativePath);
            backup1 = storage.backupAddressBookFile();
            backup2 = storage.backupAddressBookFile();

            assertTrue(Files.exists(backup1));
            assertTrue(Files.exists(backup2));
            assertNotEquals(backup1, backup2);
            assertEquals("corrupted content", Files.readString(backup1));
        } finally {
            Files.deleteIfExists(relativePath);
            if (backup1 != null) {
                Files.deleteIfExists(backup1);
            }
            if (backup2 != null) {
                Files.deleteIfExists(backup2);
            }
        }
    }
}
