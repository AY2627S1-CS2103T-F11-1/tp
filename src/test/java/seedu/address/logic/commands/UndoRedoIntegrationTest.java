package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.CARL;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.PersonBuilder;
import seedu.address.testutil.PersonUtil;

public class UndoRedoIntegrationTest {

    @TempDir
    public Path temporaryFolder;

    private Model model;
    private Logic logic;
    private JsonAddressBookStorage addressBookStorage;

    @BeforeEach
    public void setUp() throws Exception {
        model = new ModelManager(
                new AddressBookBuilder().withPerson(ALICE).build(),
                new UserPrefs());

        addressBookStorage =
                new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        JsonUserPrefsStorage userPrefsStorage =
                new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"));

        logic = new LogicManager(model,
                new StorageManager(addressBookStorage, userPrefsStorage));

        addressBookStorage.saveAddressBook(model.getAddressBook());
    }

    @Test
    public void execute_multipleUndoAndRedo_restoresEachState() throws Exception {
        assertState(false, false, ALICE);

        logic.execute(PersonUtil.getAddCommand(BENSON));
        assertState(true, false, ALICE, BENSON);

        logic.execute("delete 1");
        assertState(true, false, BENSON);

        assertEquals(UndoCommand.MESSAGE_SUCCESS,
                logic.execute("undo").getFeedbackToUser());
        assertState(true, true, ALICE, BENSON);

        assertEquals(UndoCommand.MESSAGE_SUCCESS,
                logic.execute("undo").getFeedbackToUser());
        assertState(false, true, ALICE);

        assertEquals(RedoCommand.MESSAGE_SUCCESS,
                logic.execute("redo").getFeedbackToUser());
        assertState(true, true, ALICE, BENSON);

        assertEquals(RedoCommand.MESSAGE_SUCCESS,
                logic.execute("redo").getFeedbackToUser());
        assertState(true, false, BENSON);
    }

    @Test
    public void execute_editUndoRedo_restoresAllFields() throws Exception {
        Person editedAlice = new PersonBuilder(ALICE)
                .withName("Alice Updated")
                .withPhone("91234567")
                .withEmail("updated@example.com")
                .withAddress("456 Updated Street")
                .withRole("Customer")
                .withTags("friends", "updated")
                .build();

        logic.execute("edit 1 n/Alice Updated p/91234567 e/updated@example.com "
                + "a/456 Updated Street r/Customer t/updated");
        assertState(true, false, editedAlice);

        logic.execute("undo");
        assertState(false, true, ALICE);

        logic.execute("redo");
        assertState(true, false, editedAlice);
    }

    @Test
    public void execute_clearUndoRedo_restoresContactsInOrder() throws Exception {
        logic.execute(PersonUtil.getAddCommand(BENSON));
        assertState(true, false, ALICE, BENSON);

        logic.execute("clear");
        assertState(true, false);

        logic.execute("undo");
        assertState(true, true, ALICE, BENSON);

        logic.execute("redo");
        assertState(true, false);
    }

    @Test
    public void execute_newModificationAfterUndo_discardsPreviousRedoBranch() throws Exception {
        logic.execute(PersonUtil.getAddCommand(BENSON));
        assertState(true, false, ALICE, BENSON);

        logic.execute(PersonUtil.getAddCommand(CARL));
        assertState(true, false, ALICE, BENSON, CARL);

        logic.execute("undo");
        assertState(true, true, ALICE, BENSON);

        logic.execute("delete 1");
        assertState(true, false, BENSON);

        CommandException exception = assertThrows(
                CommandException.class, () -> logic.execute("redo"));
        assertEquals(RedoCommand.MESSAGE_FAILURE, exception.getMessage());
        assertState(true, false, BENSON);

        // Navigation now follows the replacement branch.
        logic.execute("undo");
        assertState(true, true, ALICE, BENSON);

        logic.execute("redo");
        assertState(true, false, BENSON);
    }

    @Test
    public void execute_undoFilteredDeletion_restoresContactAndShowsAllPersons() throws Exception {
        logic.execute(PersonUtil.getAddCommand(BENSON));
        assertState(true, false, ALICE, BENSON);

        logic.execute("find Alice");
        assertEquals(List.of(ALICE), model.getFilteredPersonList());
        assertContacts(ALICE, BENSON);

        logic.execute("delete 1");
        assertEquals(List.of(), model.getFilteredPersonList());
        assertContacts(BENSON);
        assertHistory(true, false);

        logic.execute("undo");
        assertState(true, true, ALICE, BENSON);
    }

    @Test
    public void execute_redoWithActiveFilter_restoresDataAndShowsAllPersons() throws Exception {
        logic.execute(PersonUtil.getAddCommand(BENSON));
        assertState(true, false, ALICE, BENSON);

        logic.execute("undo");
        assertState(false, true, ALICE);

        logic.execute("find Alice");
        assertEquals(List.of(ALICE), model.getFilteredPersonList());

        logic.execute("redo");
        assertState(true, false, ALICE, BENSON);
    }

    @Test
    public void execute_undoOnStartup_preservesStateAndFilter() throws Exception {
        logic.execute("find Benson");
        assertEquals(List.of(), model.getFilteredPersonList());
        String savedJson = Files.readString(addressBookStorage.getAddressBookFilePath());

        CommandException exception = assertThrows(
                CommandException.class, () -> logic.execute("undo"));

        assertEquals(UndoCommand.MESSAGE_FAILURE, exception.getMessage());
        assertContacts(ALICE);
        assertHistory(false, false);
        assertEquals(List.of(), model.getFilteredPersonList());
        assertEquals(savedJson, Files.readString(addressBookStorage.getAddressBookFilePath()));
    }

    @Test
    public void execute_undoAfterHistoryExhausted_preservesRedoHistory() throws Exception {
        logic.execute(PersonUtil.getAddCommand(BENSON));
        logic.execute("undo");
        assertState(false, true, ALICE);

        CommandException exception = assertThrows(
                CommandException.class, () -> logic.execute("undo"));

        assertEquals(UndoCommand.MESSAGE_FAILURE, exception.getMessage());
        assertState(false, true, ALICE);

        logic.execute("redo");
        assertState(true, false, ALICE, BENSON);
    }

    @Test
    public void execute_redoOnStartup_preservesStateAndFilter() throws Exception {
        logic.execute("find Benson");
        assertEquals(List.of(), model.getFilteredPersonList());
        String savedJson = Files.readString(addressBookStorage.getAddressBookFilePath());

        CommandException exception = assertThrows(
                CommandException.class, () -> logic.execute("redo"));

        assertEquals(RedoCommand.MESSAGE_FAILURE, exception.getMessage());
        assertContacts(ALICE);
        assertHistory(false, false);
        assertEquals(List.of(), model.getFilteredPersonList());
        assertEquals(savedJson, Files.readString(addressBookStorage.getAddressBookFilePath()));
    }

    @Test
    public void execute_redoAfterHistoryExhausted_preservesUndoHistory() throws Exception {
        logic.execute(PersonUtil.getAddCommand(BENSON));
        logic.execute("undo");
        logic.execute("redo");
        assertState(true, false, ALICE, BENSON);

        CommandException exception = assertThrows(
                CommandException.class, () -> logic.execute("redo"));

        assertEquals(RedoCommand.MESSAGE_FAILURE, exception.getMessage());
        assertState(true, false, ALICE, BENSON);

        logic.execute("undo");
        assertState(false, true, ALICE);
    }

    @Test
    public void execute_duplicateAddAfterUndo_preservesRedoHistory() throws Exception {
        logic.execute(PersonUtil.getAddCommand(BENSON));
        logic.execute("undo");
        assertState(false, true, ALICE);
        String savedJson = Files.readString(addressBookStorage.getAddressBookFilePath());

        CommandException exception =
                assertThrows(CommandException.class, () -> logic.execute(PersonUtil.getAddCommand(ALICE)));

        assertEquals(Messages.MESSAGE_DUPLICATE_PHONE_AND_EMAIL, exception.getMessage());
        assertState(false, true, ALICE);
        assertEquals(savedJson, Files.readString(addressBookStorage.getAddressBookFilePath()));

        logic.execute("redo");
        assertState(true, false, ALICE, BENSON);
    }

    @Test
    public void execute_invalidDeleteAfterUndo_preservesRedoHistory() throws Exception {
        logic.execute(PersonUtil.getAddCommand(BENSON));
        logic.execute("undo");
        assertState(false, true, ALICE);
        String savedJson = Files.readString(addressBookStorage.getAddressBookFilePath());

        assertThrows(CommandException.class, () -> logic.execute("delete 2"));

        assertState(false, true, ALICE);
        assertEquals(savedJson, Files.readString(addressBookStorage.getAddressBookFilePath()));

        logic.execute("redo");
        assertState(true, false, ALICE, BENSON);
    }

    @Test
    public void execute_invalidEditAfterUndo_preservesRedoHistory() throws Exception {
        logic.execute(PersonUtil.getAddCommand(BENSON));
        logic.execute("undo");
        assertState(false, true, ALICE);
        String savedJson = Files.readString(addressBookStorage.getAddressBookFilePath());

        assertThrows(CommandException.class, () -> logic.execute("edit 2 n/Updated Name"));

        assertState(false, true, ALICE);
        assertEquals(savedJson, Files.readString(addressBookStorage.getAddressBookFilePath()));

        logic.execute("redo");
        assertState(true, false, ALICE, BENSON);
    }

    @Test
    public void execute_find_preservesHistory() throws Exception {
        logic.execute(PersonUtil.getAddCommand(BENSON));
        logic.execute(PersonUtil.getAddCommand(CARL));
        logic.execute("undo");
        assertState(true, true, ALICE, BENSON);

        logic.execute("find Alice");
        assertContacts(ALICE, BENSON);
        assertHistory(true, true);

        logic.execute("redo");
        assertState(true, false, ALICE, BENSON, CARL);

        logic.execute("undo");
        assertState(true, true, ALICE, BENSON);

        logic.execute("undo");
        assertState(false, true, ALICE);
    }

    @Test
    public void execute_list_preservesHistory() throws Exception {
        logic.execute(PersonUtil.getAddCommand(BENSON));
        logic.execute(PersonUtil.getAddCommand(CARL));
        logic.execute("undo");
        assertState(true, true, ALICE, BENSON);

        logic.execute("list");
        assertContacts(ALICE, BENSON);
        assertHistory(true, true);

        logic.execute("redo");
        assertState(true, false, ALICE, BENSON, CARL);

        logic.execute("undo");
        assertState(true, true, ALICE, BENSON);

        logic.execute("undo");
        assertState(false, true, ALICE);
    }

    @Test
    public void execute_help_preservesHistory() throws Exception {
        logic.execute(PersonUtil.getAddCommand(BENSON));
        logic.execute(PersonUtil.getAddCommand(CARL));
        logic.execute("undo");
        assertState(true, true, ALICE, BENSON);

        logic.execute("help");
        assertContacts(ALICE, BENSON);
        assertHistory(true, true);

        logic.execute("redo");
        assertState(true, false, ALICE, BENSON, CARL);

        logic.execute("undo");
        assertState(true, true, ALICE, BENSON);

        logic.execute("undo");
        assertState(false, true, ALICE);
    }

    @Test
    public void execute_undoWithArguments_preservesStateAndFilter() throws Exception {
        logic.execute(PersonUtil.getAddCommand(BENSON));
        logic.execute(PersonUtil.getAddCommand(CARL));
        logic.execute("undo");
        logic.execute("find Alice");
        String savedJson = Files.readString(addressBookStorage.getAddressBookFilePath());

        ParseException exception = assertThrows(
                ParseException.class, () -> logic.execute("undo 1"));

        assertEquals(String.format(MESSAGE_INVALID_COMMAND_FORMAT, UndoCommand.COMMAND_USAGE),
                exception.getMessage());
        assertContacts(ALICE, BENSON);
        assertHistory(true, true);
        assertEquals(List.of(ALICE), model.getFilteredPersonList());
        assertEquals(savedJson, Files.readString(addressBookStorage.getAddressBookFilePath()));

        logic.execute("redo");
        assertState(true, false, ALICE, BENSON, CARL);
    }

    @Test
    public void execute_redoWithArguments_preservesStateAndFilter() throws Exception {
        logic.execute(PersonUtil.getAddCommand(BENSON));
        logic.execute(PersonUtil.getAddCommand(CARL));
        logic.execute("undo");
        logic.execute("find Alice");
        String savedJson = Files.readString(addressBookStorage.getAddressBookFilePath());

        ParseException exception = assertThrows(
                ParseException.class, () -> logic.execute("redo 1"));

        assertEquals(String.format(MESSAGE_INVALID_COMMAND_FORMAT, RedoCommand.COMMAND_USAGE),
                exception.getMessage());
        assertContacts(ALICE, BENSON);
        assertHistory(true, true);
        assertEquals(List.of(ALICE), model.getFilteredPersonList());
        assertEquals(savedJson, Files.readString(addressBookStorage.getAddressBookFilePath()));

        logic.execute("redo");
        assertState(true, false, ALICE, BENSON, CARL);
    }

    @Test
    public void restart_savedContactsPersistButHistoryIsCleared() throws Exception {
        logic.execute(PersonUtil.getAddCommand(BENSON));
        logic.execute(PersonUtil.getAddCommand(CARL));
        logic.execute("undo");
        assertState(true, true, ALICE, BENSON);

        model = new ModelManager(
                addressBookStorage.readAddressBook().orElseThrow(), new UserPrefs());
        logic = new LogicManager(model, new StorageManager(
                addressBookStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"))));

        assertState(false, false, ALICE, BENSON);

        CommandException undoFailure = assertThrows(
                CommandException.class, () -> logic.execute("undo"));
        assertEquals(UndoCommand.MESSAGE_FAILURE, undoFailure.getMessage());

        CommandException redoFailure = assertThrows(
                CommandException.class, () -> logic.execute("redo"));
        assertEquals(RedoCommand.MESSAGE_FAILURE, redoFailure.getMessage());

        assertState(false, false, ALICE, BENSON);
    }

    @Test
    public void execute_saveFailsAfterUndo_retainsRestoredStateInMemory() throws Exception {
        logic.execute(PersonUtil.getAddCommand(BENSON));
        assertState(true, false, ALICE, BENSON);

        IOException saveFailure = new IOException("Test save failure");
        useFailingStorage(saveFailure);

        CommandException exception = assertThrows(
                CommandException.class, () -> logic.execute("undo"));

        assertEquals(String.format(LogicManager.FILE_OPS_ERROR_FORMAT, saveFailure.getMessage()),
                exception.getMessage());
        assertEquals(saveFailure, exception.getCause());
        assertEquals(List.of(ALICE), model.getAddressBook().getPersonList());
        assertEquals(List.of(ALICE), model.getFilteredPersonList());
        assertHistory(false, true);

        // The failed save leaves the previously persisted state intact.
        assertEquals(List.of(ALICE, BENSON),
                addressBookStorage.readAddressBook().orElseThrow().getPersonList());
    }

    @Test
    public void execute_saveFailsAfterRedo_retainsReappliedStateInMemory() throws Exception {
        logic.execute(PersonUtil.getAddCommand(BENSON));
        logic.execute("undo");
        assertState(false, true, ALICE);

        IOException saveFailure = new IOException("Test save failure");
        useFailingStorage(saveFailure);

        CommandException exception = assertThrows(
                CommandException.class, () -> logic.execute("redo"));

        assertEquals(String.format(LogicManager.FILE_OPS_ERROR_FORMAT, saveFailure.getMessage()),
                exception.getMessage());
        assertEquals(saveFailure, exception.getCause());
        assertEquals(List.of(ALICE, BENSON), model.getAddressBook().getPersonList());
        assertEquals(List.of(ALICE, BENSON), model.getFilteredPersonList());
        assertHistory(true, false);

        assertEquals(List.of(ALICE),
                addressBookStorage.readAddressBook().orElseThrow().getPersonList());
    }

    /**
     * Checks current and saved contacts, the unfiltered view, and history availability.
     */
    private void assertState(boolean canUndo, boolean canRedo, Person... expectedPersons)
            throws Exception {
        assertContacts(expectedPersons);
        assertEquals(List.of(expectedPersons), model.getFilteredPersonList());
        assertHistory(canUndo, canRedo);
    }

    /**
     * Checks the complete ordered contact lists in memory and on disk.
     */
    private void assertContacts(Person... expectedPersons) throws Exception {
        List<Person> expected = List.of(expectedPersons);
        assertEquals(expected, model.getAddressBook().getPersonList());
        assertEquals(expected,
                addressBookStorage.readAddressBook().orElseThrow().getPersonList());
    }

    /**
     * Checks whether undo and redo are available.
     */
    private void assertHistory(boolean canUndo, boolean canRedo) {
        assertEquals(canUndo, model.canUndoAddressBook());
        assertEquals(canRedo, model.canRedoAddressBook());
    }

    /**
     * Replaces the logic's storage with one that fails before writing contact data.
     */
    private void useFailingStorage(IOException failure) {
        JsonAddressBookStorage failingStorage =
                new JsonAddressBookStorage(addressBookStorage.getAddressBookFilePath()) {
                    @Override
                    public void saveAddressBook(seedu.address.model.ReadOnlyAddressBook addressBook)
                            throws IOException {
                        throw failure;
                    }
                };

        logic = new LogicManager(model, new StorageManager(
                failingStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"))));
    }
}
