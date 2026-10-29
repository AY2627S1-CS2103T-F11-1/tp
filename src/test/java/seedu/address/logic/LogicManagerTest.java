package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.logic.commands.CommandTestUtil.ADDRESS_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.ROLE_DESC_AMY;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.AMY;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.CARL;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.RedoCommand;
import seedu.address.logic.commands.SortCommand;
import seedu.address.logic.commands.UndoCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

public class LogicManagerTest {
    private static final IOException DUMMY_IO_EXCEPTION = new IOException("dummy IO exception");
    private static final IOException DUMMY_AD_EXCEPTION = new AccessDeniedException("dummy access denied exception");

    @TempDir
    public Path temporaryFolder;

    private Model model = new ModelManager();
    private Logic logic;
    private StorageManager storage;

    @BeforeEach
    public void setUp() {
        JsonAddressBookStorage addressBookStorage =
                new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"));
        storage = new StorageManager(addressBookStorage, userPrefsStorage);
        logic = new LogicManager(model, storage);
    }

    /**
     * Injects a request-producing test command without registering a production select command.
     */
    private void useSelectionRequest(Person requestedPerson) {
        AddressBookParser parser = new AddressBookParser() {
            @Override
            public Command parseCommand(String commandText) throws ParseException {
                if (commandText.equals("test-selection")) {
                    return new Command() {
                        @Override
                        public CommandResult execute(Model commandModel) {
                            return new CommandResult("Selection requested", requestedPerson);
                        }
                    };
                }
                return super.parseCommand(commandText);
            }
        };
        logic = new LogicManager(model, storage, parser);
    }

    @Test
    public void execute_selectionRequest_forwardsExactContactIncludingFromEmptySelection() throws Exception {
        model.addPerson(ALICE);
        model.addPerson(BENSON);
        useSelectionRequest(BENSON);
        CommandResult result = logic.execute("test-selection");
        assertSame(BENSON, result.getSelectedPerson().orElseThrow());
        assertSame(BENSON, logic.getSelectionChange().applyTo(null));
        assertSame(BENSON, logic.getSelectionChange().applyTo(ALICE));
    }

    @Test
    public void execute_select_preservesStoredContactsAndFilteredSortedView() throws Exception {
        model.addPerson(CARL);
        model.addPerson(ALICE);
        model.addPerson(BENSON);
        logic.execute("sort");
        logic.execute("find Alice Carl");
        AddressBook storedBefore = new AddressBook(model.getAddressBook());
        List<Person> displayedBefore = List.copyOf(model.getFilteredPersonList());
        String jsonBefore = Files.readString(storage.getAddressBookFilePath());

        CommandResult result = logic.execute("select 2");
        assertSame(CARL, result.getSelectedPerson().orElseThrow());
        assertSame(CARL, logic.getSelectionChange().applyTo(null));
        assertEquals(storedBefore, model.getAddressBook());
        assertEquals(displayedBefore, model.getFilteredPersonList());
        assertEquals(jsonBefore, Files.readString(storage.getAddressBookFilePath()));
        assertFalse(model.canUndoAddressBook());
        assertFalse(model.canRedoAddressBook());
    }

    @Test
    public void execute_selectBetweenUndoAndRedo_preservesHistoryAndAddsNoUndoStep() throws Exception {
        model.addPerson(ALICE);
        model.addPerson(BENSON);
        model.commitAddressBook();
        logic.execute("edit 2 n/Updated Supplier");
        Person edited = model.getFilteredPersonList().get(1);
        logic.execute("undo");
        assertTrue(model.canRedoAddressBook());

        logic.execute("select 2");
        logic.execute("select 1");
        assertTrue(model.canRedoAddressBook());
        logic.execute("redo");
        assertSame(edited, model.getFilteredPersonList().get(1));
        assertNull(logic.getSelectionChange().applyTo(ALICE));

        logic.execute("select 2");
        assertSame(edited, logic.getSelectionChange().applyTo(null));
        logic.execute("undo");
        assertSame(BENSON, model.getFilteredPersonList().get(1));
        assertNull(logic.getSelectionChange().applyTo(edited));
    }

    @Test
    public void execute_realSelectSavingFailures_retainTargetAndResetBeforeInvalidAttempt() {
        model.addPerson(ALICE);
        model.addPerson(BENSON);
        AddressBook storedBefore = new AddressBook(model.getAddressBook());
        for (IOException failure : List.of(DUMMY_IO_EXCEPTION, DUMMY_AD_EXCEPTION)) {
            Path failingPath = temporaryFolder.resolve("real-selection-failure.json");
            JsonAddressBookStorage failingStorage = new JsonAddressBookStorage(failingPath) {
                @Override
                public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                    assertSame(BENSON, logic.getSelectionChange().applyTo(ALICE));
                    throw failure;
                }
            };
            StorageManager failingManager = new StorageManager(failingStorage,
                    new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json")));
            logic = new LogicManager(model, failingManager);
            CommandException exception = Assertions.assertThrows(
                    CommandException.class, () -> logic.execute("select 2"));
            assertSame(failure, exception.getCause());
            assertSame(BENSON, logic.getSelectionChange().applyTo(null));
            assertEquals(storedBefore, model.getAddressBook());
            assertFalse(model.canUndoAddressBook());
            assertFalse(model.canRedoAddressBook());

            assertThrows(ParseException.class, () -> logic.execute("select 0"));
            assertSame(ALICE, logic.getSelectionChange().applyTo(ALICE));
            assertNull(logic.getSelectionChange().applyTo(null));
        }
    }

    @Test
    public void execute_afterSelectionRequest_clearsRequestForSuccessAndFailures() throws Exception {
        model.addPerson(ALICE);
        model.addPerson(BENSON);
        useSelectionRequest(BENSON);
        logic.execute("test-selection");
        logic.execute("list");
        assertSame(ALICE, logic.getSelectionChange().applyTo(ALICE));
        assertNull(logic.getSelectionChange().applyTo(null));

        logic.execute("test-selection");
        assertThrows(ParseException.class, () -> logic.execute("unknowncommand"));
        assertSame(ALICE, logic.getSelectionChange().applyTo(ALICE));
        assertNull(logic.getSelectionChange().applyTo(null));

        logic.execute("test-selection");
        assertThrows(CommandException.class, () -> logic.execute("delete 9"));
        assertSame(ALICE, logic.getSelectionChange().applyTo(ALICE));
        assertNull(logic.getSelectionChange().applyTo(null));
    }

    @Test
    public void execute_savingFailure_retainsRequestBeforeAndAfterSaving() {
        model.addPerson(ALICE);
        model.addPerson(BENSON);
        for (IOException failure : List.of(DUMMY_IO_EXCEPTION, DUMMY_AD_EXCEPTION)) {
            Path failingPath = temporaryFolder.resolve("failure.json");
            JsonAddressBookStorage failingStorage = new JsonAddressBookStorage(failingPath) {
                @Override
                public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                    assertSame(BENSON, logic.getSelectionChange().applyTo(ALICE));
                    throw failure;
                }
            };
            storage = new StorageManager(failingStorage,
                    new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json")));
            useSelectionRequest(BENSON);
            CommandException exception = Assertions.assertThrows(
                    CommandException.class, () -> logic.execute("test-selection"));
            assertSame(failure, exception.getCause());
            assertSame(BENSON, logic.getSelectionChange().applyTo(null));
            assertSame(BENSON, logic.getSelectionChange().applyTo(ALICE));
        }
    }

    @Test
    public void execute_savingFailureAfterEditOrDelete_restoresOriginalSelectionAndHistory() {
        for (IOException failure : List.of(DUMMY_IO_EXCEPTION, DUMMY_AD_EXCEPTION)) {
            for (String command : List.of("edit 1 n/Zed Supplier", "delete 1")) {
                model = new ModelManager();
                model.addPerson(ALICE);
                model.addPerson(BENSON);
                model.commitAddressBook();
                AddressBook original = new AddressBook(model.getAddressBook());
                int pointerBefore = model.getAddressBookStatePointer();
                model.updateFilteredPersonList(person -> person == ALICE);
                JsonAddressBookStorage failingStorage = new JsonAddressBookStorage(
                        temporaryFolder.resolve("rollback-selection.json")) {
                    @Override
                    public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                        throw failure;
                    }
                };
                storage = new StorageManager(failingStorage,
                        new JsonUserPrefsStorage(temporaryFolder.resolve("rollback-preferences.json")));
                logic = new LogicManager(model, storage);

                CommandException exception = Assertions.assertThrows(
                        CommandException.class, () -> logic.execute(command));

                assertSame(failure, exception.getCause());
                assertEquals(original, model.getAddressBook(), command);
                assertEquals(List.of(ALICE, BENSON), model.getFilteredPersonList());
                assertEquals(pointerBefore, model.getAddressBookStatePointer());
                assertSame(ALICE, logic.getSelectionChange().applyTo(ALICE));
                assertSame(BENSON, logic.getSelectionChange().applyTo(BENSON));
                assertNull(logic.getSelectionChange().applyTo(null));
                assertFalse(model.canRedoAddressBook());
            }
        }
    }

    @Test
    public void execute_invalidCommandFormat_throwsParseException() {
        String invalidCommand = "uicfhmowqewca";
        assertParseException(invalidCommand, MESSAGE_UNKNOWN_COMMAND);
    }

    @Test
    public void execute_filteredSortedEdit_reportsReplacementByObjectIdentity() throws Exception {
        model.addPerson(ALICE);
        model.addPerson(BENSON);
        model.addPerson(CARL);
        logic.execute("sort");
        logic.execute("find Alice");
        logic.execute("edit 1 n/Zed Supplier p/81000000 e/zed@example.com");

        Person replacement = model.getFilteredPersonList().get(2);
        assertSame(replacement, logic.getSelectionChange().applyTo(ALICE));
        assertSame(BENSON, logic.getSelectionChange().applyTo(BENSON));
        Person equalButDistinct = new PersonBuilder(ALICE).build();
        assertSame(equalButDistinct, logic.getSelectionChange().applyTo(equalButDistinct));
    }

    @Test
    public void execute_failureAfterEdit_resetsReplacementInformation() throws Exception {
        model.addPerson(ALICE);
        logic.execute("edit 1 n/Updated Supplier");
        Person replacement = model.getFilteredPersonList().get(0);
        assertSame(replacement, logic.getSelectionChange().applyTo(ALICE));

        assertThrows(ParseException.class, () -> logic.execute("unknowncommand"));
        assertSame(ALICE, logic.getSelectionChange().applyTo(ALICE));
        logic.execute("edit 1 n/Another Supplier");
        assertThrows(CommandException.class, () -> logic.execute("edit 9 n/Invalid Index"));
        assertSame(replacement, logic.getSelectionChange().applyTo(replacement));
    }

    @Test
    public void execute_commandExecutionError_throwsCommandException() {
        String deleteCommand = "delete 9";
        assertCommandException(deleteCommand, DeleteCommand.MESSAGE_NO_CONTACTS_TO_DELETE);
    }

    @Test
    public void execute_validListOnEmptyAddressBook_showsNoContactsMessage() throws Exception {
        assertCommandSuccess(ListCommand.COMMAND_WORD, ListCommand.MESSAGE_NO_CONTACTS, model);
    }

    @Test
    public void execute_listWithAdditionalArgument_showsWarning() throws Exception {
        model.addPerson(AMY);

        Model expectedModel = new ModelManager();
        expectedModel.addPerson(AMY);

        String expectedMessage = ListCommand.MESSAGE_SUCCESS + "\n" + ListCommand.MESSAGE_IGNORED_ARGUMENTS;

        assertCommandSuccess("list 3", expectedMessage, expectedModel);
    }

    @Test
    public void execute_listWithMultipleAdditionalArguments_showsWarning() throws Exception {
        model.addPerson(AMY);

        Model expectedModel = new ModelManager();
        expectedModel.addPerson(AMY);

        String expectedMessage = ListCommand.MESSAGE_SUCCESS + "\n" + ListCommand.MESSAGE_IGNORED_ARGUMENTS;

        assertCommandSuccess("list abc xyz", expectedMessage, expectedModel);
    }

    @Test
    public void execute_listWithWhitespaceOnlyArguments_showsNoWarning() throws Exception {
        model.addPerson(AMY);

        Model expectedModel = new ModelManager();
        expectedModel.addPerson(AMY);

        assertCommandSuccess("list \t  ", ListCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void execute_findThenList_showsAllContacts() throws Exception {
        model.addPerson(AMY);
        model.addPerson(BENSON);

        logic.execute("find Amy");

        assertEquals(1, logic.getFilteredPersonList().size());

        Model expectedModel = new ModelManager();
        expectedModel.addPerson(AMY);
        expectedModel.addPerson(BENSON);

        assertCommandSuccess("list", ListCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void execute_undoAfterMutation_success() throws Exception {
        model.addPerson(AMY);
        model.commitAddressBook();
        String deleteCommand = DeleteCommand.COMMAND_WORD + " 1";

        logic.execute(deleteCommand);
        assertFalse(model.hasPerson(AMY));

        logic.execute(UndoCommand.COMMAND_WORD);
        assertTrue(model.hasPerson(AMY));
    }

    @Test
    public void execute_redoAfterUndo_success() throws Exception {
        model.addPerson(AMY);
        model.commitAddressBook();
        String deleteCommand = DeleteCommand.COMMAND_WORD + " 1";

        logic.execute(deleteCommand);
        assertFalse(model.hasPerson(AMY));

        logic.execute(UndoCommand.COMMAND_WORD);
        assertTrue(model.hasPerson(AMY));

        logic.execute(RedoCommand.COMMAND_WORD);
        assertFalse(model.hasPerson(AMY));
    }

    @Test
    public void execute_storageThrowsIoException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(DUMMY_IO_EXCEPTION, String.format(
                LogicManager.FILE_OPS_ERROR_FORMAT, DUMMY_IO_EXCEPTION.getMessage()));
    }

    @Test
    public void execute_storageThrowsAdException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(DUMMY_AD_EXCEPTION, String.format(
                LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT, DUMMY_AD_EXCEPTION.getMessage()));
    }

    @Test
    public void execute_storageThrowsIoExceptionOnReadOnlyCommand_preservesModelAndHistory() throws Exception {
        model.addPerson(AMY);
        model.commitAddressBook();

        Path prefPath = temporaryFolder.resolve("ExceptionUserPrefsList.json");
        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(prefPath) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw DUMMY_IO_EXCEPTION;
            }
        };
        JsonUserPrefsStorage userPrefsStorage =
                new JsonUserPrefsStorage(temporaryFolder.resolve("ExceptionUserPrefsList.json"));
        StorageManager storage = new StorageManager(addressBookStorage, userPrefsStorage);
        logic = new LogicManager(model, storage);

        assertThrows(CommandException.class, () -> logic.execute(ListCommand.COMMAND_WORD));
        assertTrue(model.hasPerson(AMY));
        assertTrue(model.canUndoAddressBook());
        assertFalse(model.canRedoAddressBook());
    }

    @Test
    public void execute_storageAtomicWriteFails_throwsCommandExceptionAndCleansUpTempFiles() throws Exception {
        Path invalidFilePath = temporaryFolder.resolve("invalidStorageDir");
        Files.createDirectory(invalidFilePath);

        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(invalidFilePath);
        JsonUserPrefsStorage userPrefsStorage =
                new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"));
        StorageManager storage = new StorageManager(addressBookStorage, userPrefsStorage);
        logic = new LogicManager(model, storage);

        String addCommand = AddCommand.COMMAND_WORD + NAME_DESC_AMY + PHONE_DESC_AMY
                + EMAIL_DESC_AMY + ADDRESS_DESC_AMY + ROLE_DESC_AMY;
        Person expectedPerson = new PersonBuilder(AMY).withTags().build();
        ModelManager expectedModel = new ModelManager();

        CommandException exception = Assertions.assertThrows(CommandException.class, () -> logic.execute(addCommand));
        String fileOpsErrorPrefix = String.format(LogicManager.FILE_OPS_ERROR_FORMAT, "").trim();
        String fileOpsPermissionErrorSubstring = "due to insufficient permissions to write to the file or the folder";
        boolean isExpectedMessage = exception.getMessage().contains(fileOpsErrorPrefix)
                || exception.getMessage().contains(fileOpsPermissionErrorSubstring);
        assertTrue(isExpectedMessage, "Expected file operation error message but got: " + exception.getMessage());
        assertTrue(exception.getCause() instanceof IOException);
        assertEquals(expectedModel, model);
        assertFalse(model.hasPerson(expectedPerson));
        assertFalse(model.canUndoAddressBook());
        assertFalse(model.canRedoAddressBook());
        assertTrue(Files.isDirectory(invalidFilePath));
        assertNoTempFilesRemain(temporaryFolder);
    }

    @Test
    public void execute_sortThenDelete_usesDisplayedIndex() throws Exception {
        model.addPerson(CARL);
        model.addPerson(ALICE);
        model.addPerson(BENSON);

        logic.execute("sort n/");
        assertEquals(List.of(ALICE, BENSON, CARL), logic.getFilteredPersonList());

        logic.execute("delete 1");

        // ALICE occupies displayed index 1, although CARL was inserted first
        assertFalse(model.hasPerson(ALICE));
        assertEquals(List.of(BENSON, CARL), logic.getFilteredPersonList());
        assertEquals(List.of(CARL, BENSON), model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_sortThenEdit_usesDisplayedIndexAndRepositionsContact() throws Exception {
        model.addPerson(CARL);
        model.addPerson(ALICE);
        model.addPerson(BENSON);

        logic.execute("sort n/");
        assertEquals(List.of(ALICE, BENSON, CARL), logic.getFilteredPersonList());

        Person renamedAlice = new PersonBuilder(ALICE).withName("Zoe").build();

        logic.execute("edit 1 n/Zoe");

        // The edited contact keeps ALICE's other details and moves to the end
        assertEquals(List.of(BENSON, CARL, renamedAlice), logic.getFilteredPersonList());
        assertEquals(List.of(CARL, renamedAlice, BENSON), model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_sortThenEditRole_repositionsContactByRole() throws Exception {
        model.addPerson(BENSON);
        model.addPerson(ALICE);
        model.addPerson(AMY);

        logic.execute("sort r/ n/");
        assertEquals(List.of(AMY, ALICE, BENSON), logic.getFilteredPersonList());

        Person accountantBenson = new PersonBuilder(BENSON)
                .withRole("Accountant")
                .build();

        logic.execute("edit 3 r/Accountant");

        assertEquals(List.of(accountantBenson, AMY, ALICE),
                logic.getFilteredPersonList());
        assertEquals(List.of(accountantBenson, ALICE, AMY),
                model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_findThenSort_showsAllContactsSortedByName() throws Exception {
        model.addPerson(CARL);
        model.addPerson(ALICE);
        model.addPerson(BENSON);

        logic.execute("find n/Alice");
        assertEquals(List.of(ALICE), logic.getFilteredPersonList());

        Model expectedModel = new ModelManager();
        expectedModel.addPerson(CARL);
        expectedModel.addPerson(ALICE);
        expectedModel.addPerson(BENSON);
        expectedModel.updateSortedPersonList(Comparator.comparing(person -> person.getName().fullName));

        // Bare sort must default to name sorting and reveal all contacts
        assertCommandSuccess("sort", SortCommand.MESSAGE_SUCCESS, expectedModel);
        assertEquals(List.of(ALICE, BENSON, CARL), logic.getFilteredPersonList());

        // Saving after sorting must preserve the underlying contact order.
        JsonAddressBookStorage savedStorage =
                new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));

        ReadOnlyAddressBook savedAddressBook =
                savedStorage.readAddressBook().orElseThrow();

        assertEquals(List.of(CARL, ALICE, BENSON),
                savedAddressBook.getPersonList());
    }

    @Test
    public void execute_sortThenFindThenList_preservesSortOrder() throws Exception {
        model.addPerson(BENSON);
        model.addPerson(ALICE);
        model.addPerson(AMY);

        // AMY is a Distributor; ALICE and BENSON are Suppliers
        logic.execute("sort r/ n/");
        assertEquals(List.of(AMY, ALICE, BENSON), logic.getFilteredPersonList());

        logic.execute("find r/Supplier");
        assertEquals(List.of(ALICE, BENSON), logic.getFilteredPersonList());

        logic.execute("list");
        assertEquals(List.of(AMY, ALICE, BENSON), logic.getFilteredPersonList());

        assertEquals(List.of(BENSON, ALICE, AMY), model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_sortThenDeleteUndoRedo_preservesSortOrder() throws Exception {
        model.addPerson(CARL);
        model.addPerson(ALICE);
        model.addPerson(BENSON);

        // Record the initial populated state so undo can restore it
        model.commitAddressBook();

        logic.execute("sort n/");
        assertEquals(List.of(ALICE, BENSON, CARL), logic.getFilteredPersonList());

        logic.execute("delete 1");
        assertEquals(List.of(BENSON, CARL), logic.getFilteredPersonList());

        logic.execute("undo");
        assertEquals(List.of(ALICE, BENSON, CARL), logic.getFilteredPersonList());
        assertEquals(List.of(CARL, ALICE, BENSON), model.getAddressBook().getPersonList());

        logic.execute("redo");
        assertEquals(List.of(BENSON, CARL), logic.getFilteredPersonList());
        assertEquals(List.of(CARL, BENSON), model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_sortBetweenUndoAndRedo_preservesRedoHistory() throws Exception {
        model.addPerson(CARL);
        model.addPerson(ALICE);
        model.addPerson(BENSON);
        model.commitAddressBook();

        // Before sorting, index 1 refers to CARL
        logic.execute("delete 1");
        assertFalse(model.hasPerson(CARL));

        logic.execute("undo");
        assertEquals(List.of(CARL, ALICE, BENSON), logic.getFilteredPersonList());
        assertTrue(model.canRedoAddressBook());

        logic.execute("sort n/");
        assertEquals(List.of(ALICE, BENSON, CARL), logic.getFilteredPersonList());
        assertTrue(model.canRedoAddressBook());

        // Redo must repeat the original deletion, regardless of the new indices
        logic.execute("redo");
        assertEquals(List.of(ALICE, BENSON), logic.getFilteredPersonList());
        assertFalse(model.hasPerson(CARL));
        assertFalse(model.canRedoAddressBook());
    }

    @Test
    public void execute_invalidSort_preservesState() throws Exception {
        model.addPerson(BENSON);
        model.addPerson(ALICE);
        model.addPerson(AMY);

        logic.execute("sort r/ n/");
        logic.execute("find r/Supplier");
        assertEquals(List.of(ALICE, BENSON), logic.getFilteredPersonList());

        Model expectedModel = new ModelManager();
        expectedModel.addPerson(BENSON);
        expectedModel.addPerson(ALICE);
        expectedModel.addPerson(AMY);

        // Define the expected order independently of the command's comparator
        List<Person> expectedOrder = List.of(AMY, ALICE, BENSON);
        expectedModel.updateSortedPersonList(Comparator.comparingInt(expectedOrder::indexOf));
        expectedModel.updateFilteredPersonList(person -> !person.equals(AMY));

        assertCommandFailure("sort n/ n/", ParseException.class,
                String.format(SortCommand.MESSAGE_DUPLICATE_FIELD, "n/"), expectedModel);

        // The failed command must preserve the active filter
        assertEquals(List.of(ALICE, BENSON), logic.getFilteredPersonList());

        // Revealing all contacts must retain the previous role-first ordering
        logic.execute("list");
        assertEquals(expectedOrder, logic.getFilteredPersonList());
    }

    @Test
    public void execute_sortWithEqualKeys_preservesContactsAndDeletesDisplayedIdentity() throws Exception {
        Person otherAlice = new PersonBuilder(BENSON)
                .withName(ALICE.getName().fullName)
                .withRole(ALICE.getRole().value)
                .build();

        model.addPerson(CARL);
        model.addPerson(ALICE);
        model.addPerson(otherAlice);

        logic.execute("sort n/ r/");

        assertEquals(3, logic.getFilteredPersonList().size());
        assertTrue(logic.getFilteredPersonList().contains(ALICE));
        assertTrue(logic.getFilteredPersonList().contains(otherAlice));
        assertEquals(CARL, logic.getFilteredPersonList().get(2));

        Person firstDisplayed = logic.getFilteredPersonList().get(0);
        assertTrue(firstDisplayed.equals(ALICE) || firstDisplayed.equals(otherAlice));

        Person remainingAlice = firstDisplayed.equals(ALICE) ? otherAlice : ALICE;

        logic.execute("delete 1");

        assertFalse(model.hasPerson(firstDisplayed));
        assertTrue(model.hasPerson(remainingAlice));
        assertEquals(List.of(remainingAlice, CARL), logic.getFilteredPersonList());
    }

    @Test
    public void getFilteredPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> logic.getFilteredPersonList().remove(0));
    }

    /**
     * Executes the command and confirms that
     * - no exceptions are thrown <br>
     * - the feedback message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandSuccess(String inputCommand, String expectedMessage,
            Model expectedModel) throws CommandException, ParseException {
        CommandResult result = logic.execute(inputCommand);
        assertEquals(expectedMessage, result.getFeedbackToUser());
        assertEquals(expectedModel, model);
    }

    /**
     * Executes the command, confirms that a ParseException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertParseException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, ParseException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that a CommandException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, CommandException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that the exception is thrown and that the result message is correct,
     * assuming that the model's state remains the initial empty model.
     * For tests that modify {@code model} before execution, use
     * {@link #assertCommandFailure(String, Class, String, Model)} instead.
     *
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage) {
        Model expectedModel = new ModelManager();
        assertCommandFailure(inputCommand, expectedException, expectedMessage, expectedModel);
    }

    /**
     * Executes the command and confirms that
     * - the {@code expectedException} is thrown <br>
     * - the resulting error message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandSuccess(String, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage, Model expectedModel) {
        assertThrows(expectedException, expectedMessage, () -> logic.execute(inputCommand));
        assertEquals(expectedModel, model);
    }

    /**
     * Tests the Logic component's handling of an {@code IOException} thrown by the Storage component.
     *
     * @param e the exception to be thrown by the Storage component
     * @param expectedMessage the message expected inside exception thrown by the Logic component
     */
    private void assertCommandFailureForExceptionFromStorage(IOException e, String expectedMessage) {
        Path prefPath = temporaryFolder.resolve("ExceptionUserPrefs.json");

        // Inject LogicManager with a JsonAddressBookStorage that throws the IOException e when saving
        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(prefPath) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw e;
            }
        };

        JsonUserPrefsStorage userPrefsStorage =
                new JsonUserPrefsStorage(temporaryFolder.resolve("ExceptionUserPrefs.json"));
        StorageManager storage = new StorageManager(addressBookStorage, userPrefsStorage);

        logic = new LogicManager(model, storage);

        // Triggers the saveAddressBook method by executing an add command
        String addCommand = AddCommand.COMMAND_WORD + NAME_DESC_AMY + PHONE_DESC_AMY
                + EMAIL_DESC_AMY + ADDRESS_DESC_AMY + ROLE_DESC_AMY;
        Person expectedPerson = new PersonBuilder(AMY).withTags().build();
        ModelManager expectedModel = new ModelManager();
        assertCommandFailure(addCommand, CommandException.class, expectedMessage, expectedModel);
        assertFalse(model.hasPerson(expectedPerson));
        assertFalse(model.canUndoAddressBook());
        assertFalse(model.canRedoAddressBook());
    }

    /**
     * Asserts that no temporary {@code .tmp} files remain in the given directory.
     */
    private void assertNoTempFilesRemain(Path directory) throws IOException {
        try (var stream = Files.list(directory)) {
            List<Path> tempFiles = stream
                    .filter(path -> path.getFileName().toString().endsWith(".tmp"))
                    .toList();
            assertTrue(tempFiles.isEmpty(), "Unexpected temporary files found: " + tempFiles);
        }
    }
}
