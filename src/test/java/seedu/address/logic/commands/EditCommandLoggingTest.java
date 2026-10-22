package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ROLE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.testutil.EditPersonDescriptorBuilder;
import seedu.address.testutil.PersonBuilder;

/**
 * Checks edit logging with detailed logging enabled without changing the application's logging configuration.
 */
public class EditCommandLoggingTest {

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
    private Person original = model.getFilteredPersonList().get(0);

    @Test
    public void execute_nameOnly_logsNameSupplied() {
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB).build();
        Person editedPerson = new PersonBuilder(original).withName(VALID_NAME_BOB).build();
        assertSuccessLog(descriptor, editedPerson,
                "name supplied=true, phone supplied=false, email supplied=false, address supplied=false, "
                + "role supplied=false, requested tag additions=0, requested tag deletions=0");
    }

    @Test
    public void execute_phoneOnly_logsPhoneSupplied() {
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withPhone(VALID_PHONE_BOB).build();
        Person editedPerson = new PersonBuilder(original).withPhone(VALID_PHONE_BOB).build();
        assertSuccessLog(descriptor, editedPerson,
                "name supplied=false, phone supplied=true, email supplied=false, address supplied=false, "
                + "role supplied=false, requested tag additions=0, requested tag deletions=0");
    }

    @Test
    public void execute_emailOnly_logsEmailSupplied() {
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withEmail(VALID_EMAIL_BOB).build();
        Person editedPerson = new PersonBuilder(original).withEmail(VALID_EMAIL_BOB).build();
        assertSuccessLog(descriptor, editedPerson,
                "name supplied=false, phone supplied=false, email supplied=true, address supplied=false, "
                + "role supplied=false, requested tag additions=0, requested tag deletions=0");
    }

    @Test
    public void execute_addressOnly_logsAddressSupplied() {
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withAddress("New Address").build();
        Person editedPerson = new PersonBuilder(original).withAddress("New Address").build();
        assertSuccessLog(descriptor, editedPerson,
                "name supplied=false, phone supplied=false, email supplied=false, address supplied=true, "
                + "role supplied=false, requested tag additions=0, requested tag deletions=0");
    }

    @Test
    public void execute_roleOnly_logsRoleSupplied() {
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withRole(VALID_ROLE_BOB).build();
        Person editedPerson = new PersonBuilder(original).withRole(VALID_ROLE_BOB).build();
        assertSuccessLog(descriptor, editedPerson,
                "name supplied=false, phone supplied=false, email supplied=false, address supplied=false, "
                + "role supplied=true, requested tag additions=0, requested tag deletions=0");
    }

    @Test
    public void execute_tagsOnly_logsCountsAndNoOtherFieldsSupplied() {
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                .withTagsToAdd("solderer", "supplier").withTagsToDelete("friends").build();
        Person editedPerson = new PersonBuilder(original).withTags("solderer", "supplier").build();
        assertSuccessLog(descriptor, editedPerson,
                "name supplied=false, phone supplied=false, email supplied=false, address supplied=false, "
                + "role supplied=false, requested tag additions=2, requested tag deletions=1");
    }

    @Test
    public void execute_allFields_logsAllFieldsSupplied() {
        Person editedPerson = new PersonBuilder().withTags("solderer").build();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder(editedPerson)
                .withTagsToDelete("friends").build();
        assertSuccessLog(descriptor, editedPerson,
                "name supplied=true, phone supplied=true, email supplied=true, address supplied=true, "
                + "role supplied=true, requested tag additions=1, requested tag deletions=1");
    }

    @Test
    public void execute_unchangedName_logsSuppliedRatherThanChanged() {
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                .withName(original.getName().fullName).build();
        assertSuccessLog(descriptor, original,
                "name supplied=true, phone supplied=false, email supplied=false, address supplied=false, "
                + "role supplied=false, requested tag additions=0, requested tag deletions=0");
    }

    @Test
    public void execute_existingTag_logsRequestedAddition() {
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withTagsToAdd("FRIENDS").build();
        assertSuccessLog(descriptor, original,
                "name supplied=false, phone supplied=false, email supplied=false, address supplied=false, "
                + "role supplied=false, requested tag additions=1, requested tag deletions=0");
    }

    @Test
    public void execute_missingTag_logsWarningWithoutSuccess() {
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName("Alex")
                .withTagsToAdd("solderer").withTagsToDelete("trainee").build();
        assertFailureLog(new EditCommand(INDEX_FIRST_PERSON, descriptor),
                String.format(EditCommand.MESSAGE_TAG_NOT_FOUND, "trainee"),
                "EditCommand rejected: A requested tag deletion is absent from the selected contact");
    }

    @Test
    public void execute_invalidIndex_logsWarningWithoutSuccess() {
        Index invalidIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName("Alex").build();
        assertFailureLog(new EditCommand(invalidIndex, descriptor), Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX,
                "EditCommand rejected: Displayed contact index is out of bounds");
    }

    @Test
    public void execute_duplicatePhone_logsWarningWithoutSuccess() {
        String duplicatePhone = model.getFilteredPersonList().get(1).getPhone().value;
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withPhone(duplicatePhone).build();
        assertFailureLog(new EditCommand(INDEX_FIRST_PERSON, descriptor), Messages.MESSAGE_DUPLICATE_PHONE,
                "EditCommand rejected: Duplicate phone exists in address book");
    }

    @Test
    public void execute_duplicateEmail_logsWarningWithoutSuccess() {
        String duplicateEmail = model.getFilteredPersonList().get(1).getEmail().value;
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withEmail(duplicateEmail).build();
        assertFailureLog(new EditCommand(INDEX_FIRST_PERSON, descriptor), Messages.MESSAGE_DUPLICATE_EMAIL,
                "EditCommand rejected: Duplicate email exists in address book");
    }

    @Test
    public void execute_duplicatePhoneAndEmail_logsWarningWithoutSuccess() {
        Person otherPerson = model.getFilteredPersonList().get(1);
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                .withPhone(otherPerson.getPhone().value).withEmail(otherPerson.getEmail().value).build();
        assertFailureLog(new EditCommand(INDEX_FIRST_PERSON, descriptor), Messages.MESSAGE_DUPLICATE_PHONE_AND_EMAIL,
                "EditCommand rejected: Duplicate phone and email exist in address book");
    }

    /**
     * Checks successful execution, including history, and the detailed log describing the supplied fields.
     */
    private void assertSuccessLog(EditPersonDescriptor descriptor, Person editedPerson, String expectedDetails) {
        EditCommand command = new EditCommand(INDEX_FIRST_PERSON, descriptor);
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(original, editedPerson);
        expectedModel.commitAddressBook();
        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));

        List<LogRecord> records = captureLogs(() ->
                assertCommandSuccess(command, model, expectedMessage, expectedModel));
        assertEquals(List.of("Executing EditCommand for displayed contact index: 1"), messagesAt(records, Level.FINE));
        assertEquals(List.of("Person edited in model: displayed contact index=1, " + expectedDetails),
                messagesAt(records, Level.FINER));
        assertEquals(List.of(), messagesAt(records, Level.WARNING));
    }

    /**
     * Checks that a rejected edit preserves the model and emits a warning without a success entry.
     */
    private void assertFailureLog(EditCommand command, String expectedError, String expectedWarning) {
        List<LogRecord> records = captureLogs(() -> assertCommandFailure(command, model, expectedError));
        assertEquals(List.of(expectedWarning), messagesAt(records, Level.WARNING));
        assertFalse(records.stream().anyMatch(record -> record.getLevel().equals(Level.FINER)));
    }

    private List<String> messagesAt(List<LogRecord> records, Level level) {
        return records.stream().filter(record -> record.getLevel().equals(level)).map(LogRecord::getMessage).toList();
    }

    /**
     * Captures this command's logs and restores its previous settings even if the test fails.
     * Commands are constructed before this method so their static logger initialization has completed.
     */
    private List<LogRecord> captureLogs(Runnable action) {
        Logger logger = Logger.getLogger("ab3.EditCommand");
        Level originalLevel = logger.getLevel();
        boolean originalUseParentHandlers = logger.getUseParentHandlers();
        List<LogRecord> records = new ArrayList<>();
        Handler handler = new Handler() {
            @Override
            public void publish(LogRecord record) {
                records.add(record);
            }

            @Override
            public void flush() {}

            @Override
            public void close() {}
        };
        handler.setLevel(Level.ALL);
        logger.addHandler(handler);
        logger.setLevel(Level.FINER);
        logger.setUseParentHandlers(false);
        try {
            action.run();
            return records;
        } finally {
            logger.removeHandler(handler);
            logger.setLevel(originalLevel);
            logger.setUseParentHandlers(originalUseParentHandlers);
            handler.close();
        }
    }
}
