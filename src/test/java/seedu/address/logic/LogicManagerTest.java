package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import static seedu.address.testutil.TypicalPersons.BOB;
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
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.RedoCommand;
import seedu.address.logic.commands.SortCommand;
import seedu.address.logic.commands.UndoCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Address;
import seedu.address.model.person.FollowUp;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;
import seedu.address.testutil.PersonUtil;

public class LogicManagerTest {
    private static final IOException DUMMY_IO_EXCEPTION = new IOException("dummy IO exception");
    private static final IOException DUMMY_AD_EXCEPTION = new AccessDeniedException("dummy access denied exception");

    @TempDir
    public Path temporaryFolder;

    private Model model = new ModelManager();
    private Logic logic;

    @BeforeEach
    public void setUp() {
        JsonAddressBookStorage addressBookStorage =
                new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"));
        StorageManager storage = new StorageManager(addressBookStorage, userPrefsStorage);
        logic = new LogicManager(model, storage);
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
    public void execute_findRepeatedSpaces_matchesNormalisedFields() throws Exception {
        logic.execute("add n/Spacing   Tester p/91234567 e/spacingtester@example.com a/Test Street"
                + " r/Regional   Supplier t/Tactile   Switch");
        Person expectedPerson = model.getAddressBook().getPersonList().getFirst();
        model.addPerson(AMY);

        for (String command : List.of("find n/Spacing Tester", "find n/Spacing   Tester",
                "find r/Regional Supplier", "find r/Regional   Supplier",
                "find t/Tactile Switch", "find t/Tactile   Switch",
                "find n/cing   test r/gional   supp t/ctile   swi")) {
            logic.execute(command);
            assertEquals(List.of(expectedPerson), logic.getFilteredPersonList(), command);
        }
    }

    @Test
    public void execute_findRepeatedSpaces_preservesOrAndLogic() throws Exception {
        Person alice = new PersonBuilder(ALICE).withName("Alice Tan")
                .withRole("Regional Supplier").withTags("Good Friend").build();
        Person amy = new PersonBuilder(AMY).withName("Amy Bee")
                .withRole("Local Distributor").withTags("Trusted Partner").build();
        Person wrongName = new PersonBuilder(BENSON).withName("Benson Meier")
                .withRole("Regional Supplier").withTags("Good Friend").build();
        Person wrongRole = new PersonBuilder(CARL).withName("Alice Tan")
                .withRole("Employee").withTags("Good Friend").build();
        Person wrongTag = new PersonBuilder(BOB).withName("Amy Bee")
                .withRole("Local Distributor").withTags("Family").build();
        List.of(alice, amy, wrongName, wrongRole, wrongTag).forEach(model::addPerson);

        logic.execute("find n/alice   tan n/amy   bee r/regional   supp r/local   distrib"
                + " t/good   fri t/trusted   part");

        assertEquals(List.of(alice, amy), logic.getFilteredPersonList());
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
    public void execute_addBlankUnicodeAddress_preservesContactsStorageAndHistory() throws Exception {
        logic.execute(PersonUtil.getAddCommand(AMY));
        logic.execute(PersonUtil.getAddCommand(BENSON));
        logic.execute(UndoCommand.COMMAND_WORD);
        Path savedFile = temporaryFolder.resolve("addressBook.json");
        String savedContents = Files.readString(savedFile);
        String[] addresses = {
            "", " ", "   ", "\u200B", "\u00A0", "\u2060", "\u2003",
            "\u200B\u00A0\u2060\u2003", " \u200B \u00A0\t\u2060 \u2003 "
        };

        for (String address : addresses) {
            String command = "add n/Blank Address p/123 e/blank@example.com a/" + address + " r/Customer";
            assertThrows(ParseException.class, Address.MESSAGE_CONSTRAINTS, () -> logic.execute(command));
            assertEquals(List.of(AMY), model.getAddressBook().getPersonList());
            assertEquals(List.of(AMY), logic.getFilteredPersonList());
            assertTrue(model.canUndoAddressBook());
            assertTrue(model.canRedoAddressBook());
            assertEquals(savedContents, Files.readString(savedFile));
        }

        logic.execute(UndoCommand.COMMAND_WORD);
        assertTrue(model.getAddressBook().getPersonList().isEmpty());
        assertFalse(model.canUndoAddressBook());

        logic.execute(RedoCommand.COMMAND_WORD);
        assertEquals(List.of(AMY), model.getAddressBook().getPersonList());
        logic.execute(RedoCommand.COMMAND_WORD);
        assertEquals(List.of(AMY, BENSON), model.getAddressBook().getPersonList());
        assertFalse(model.canRedoAddressBook());
    }

    @Test
    public void execute_addVisibleAddressWithUnicodeCharacters_savesAndReloads() throws Exception {
        String address = "\u200B\u00A0123\u2060 Main\u2003St";
        Person person = new PersonBuilder().withAddress(address).build();

        logic.execute(PersonUtil.getAddCommand(person));

        assertEquals(List.of(person), model.getAddressBook().getPersonList());
        JsonAddressBookStorage storage =
                new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        assertEquals(List.of(person), storage.readAddressBook().orElseThrow().getPersonList());
    }

    @Test
    public void execute_followUpBlankUnicodeDescription_preservesContactsStorageAndHistory() throws Exception {
        logic.execute(PersonUtil.getAddCommand(AMY));
        logic.execute(PersonUtil.getAddCommand(BENSON));
        logic.execute("followup 1 by/2026-11-01 abt/Confirm quotation");
        logic.execute("followup 1 by/2026-11-02 abt/Call customer");
        logic.execute(UndoCommand.COMMAND_WORD);

        Person withOriginalFollowUp = new PersonBuilder(AMY)
                .withFollowUp("2026-11-01", "Confirm quotation").build();
        Path savedFile = temporaryFolder.resolve("addressBook.json");
        String savedContents = Files.readString(savedFile);
        String[] descriptions = {
            "", "   ", "\t\n", "\u2003", "\u200B", "\u00A0", "\u2060",
            "\u200B\u00A0\u2060", " \u200B \u00A0\u2003\u2060 "
        };

        for (String description : descriptions) {
            String command = "followup 1 by/2026-11-03 abt/" + description;
            assertThrows(ParseException.class, FollowUp.MESSAGE_CONSTRAINTS, () -> logic.execute(command));
            assertEquals(List.of(withOriginalFollowUp, BENSON), model.getAddressBook().getPersonList());
            assertEquals(List.of(withOriginalFollowUp, BENSON), logic.getFilteredPersonList());
            assertTrue(model.canUndoAddressBook());
            assertTrue(model.canRedoAddressBook());
            assertEquals(savedContents, Files.readString(savedFile));
        }

        logic.execute(UndoCommand.COMMAND_WORD);
        assertEquals(List.of(AMY, BENSON), model.getAddressBook().getPersonList());
        logic.execute(RedoCommand.COMMAND_WORD);
        assertEquals(List.of(withOriginalFollowUp, BENSON), model.getAddressBook().getPersonList());
        logic.execute(RedoCommand.COMMAND_WORD);
        Person withReplacementFollowUp = new PersonBuilder(AMY)
                .withFollowUp("2026-11-02", "Call customer").build();
        assertEquals(List.of(withReplacementFollowUp, BENSON), model.getAddressBook().getPersonList());
        assertFalse(model.canRedoAddressBook());
    }

    @Test
    public void execute_followUpVisibleDescriptionWithInvisibleCharacters_savesAndReloads() throws Exception {
        String description = "\u200B\u00A0Confirm\u2060 quotation\u2003now\u200B";
        logic.execute(PersonUtil.getAddCommand(AMY));

        logic.execute("followup 1 by/2026-11-01 abt/" + description);

        Person expected = new PersonBuilder(AMY).withFollowUp("2026-11-01", description).build();
        assertEquals(List.of(expected), model.getAddressBook().getPersonList());
        JsonAddressBookStorage storage =
                new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        assertEquals(List.of(expected), storage.readAddressBook().orElseThrow().getPersonList());
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
