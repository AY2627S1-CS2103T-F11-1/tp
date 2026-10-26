package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_FOLLOW_UP_DATE;
import static seedu.address.logic.commands.CommandTestUtil.VALID_FOLLOW_UP_DESCRIPTION;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ROLE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.Locale;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.testutil.EditPersonDescriptorBuilder;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for EditCommand.
 */
public class EditCommandTest {

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
    // Keep expected history across successive edits, including edits that leave the contact unchanged.
    private Model expectedTagEditModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_allFieldsSpecifiedUnfilteredList_success() {
        Person editedPerson = new PersonBuilder().withTags("solderer").build();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder(editedPerson)
                .withTagsToDelete("friends").build();
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON, descriptor);

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(model.getFilteredPersonList().get(0), editedPerson);
        expectedModel.commitAddressBook();

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_editContactFields_preservesFollowUp() throws Exception {
        Person original = new PersonBuilder(model.getFilteredPersonList().get(0))
                .withFollowUp(VALID_FOLLOW_UP_DATE, VALID_FOLLOW_UP_DESCRIPTION)
                .build();
        AddressBook initialState = new AddressBook(model.getAddressBook());
        initialState.setPerson(initialState.getPersonList().get(0), original);
        model.setAddressBook(initialState);
        model.commitAddressBook();

        Person edited = new PersonBuilder(original)
                .withName("Updated Contact")
                .withPhone("91234567")
                .withEmail("updated@example.com")
                .withAddress("Updated address")
                .withRole("Employee")
                .build();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder(edited).build();

        Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        expectedModel.setAddressBook(initialState);
        expectedModel.commitAddressBook();

        expectedModel.setPerson(original, edited);
        expectedModel.commitAddressBook();

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(edited));
        assertCommandSuccess(new EditCommand(INDEX_FIRST_PERSON, descriptor), model, expectedMessage, expectedModel);

        model.undoAddressBook();
        assertEquals(initialState, model.getAddressBook());

        model.redoAddressBook();
        assertEquals(edited, model.getAddressBook().getPersonList().get(0));
    }

    @Test
    public void execute_editContactWithoutFollowUp_keepsFollowUpAbsent() throws Exception {
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName("Updated Contact").build();
        new EditCommand(INDEX_FIRST_PERSON, descriptor).execute(model);
        assertEquals("Updated Contact", model.getAddressBook().getPersonList().get(0).getName().fullName);
        assertTrue(model.getAddressBook().getPersonList().get(0).getFollowUp().isEmpty());
    }

    @Test
    public void execute_addTags_preservesExistingTags() {
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                .withTagsToAdd("solderer", "supplier").build();
        assertTagEditSuccess(INDEX_SECOND_PERSON, descriptor, "owesMoney", "friends", "solderer", "supplier");
    }

    @Test
    public void execute_addExistingTagDifferentCase_successWithoutDuplicate() {
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withTagsToAdd("FRIENDS").build();
        assertTagEditSuccess(INDEX_FIRST_PERSON, descriptor, "friends");
    }

    @Test
    public void execute_addTagToUntaggedPerson_success() {
        Index index = Index.fromOneBased(3);
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withTagsToAdd("solderer").build();
        assertTagEditSuccess(index, descriptor, "solderer");
    }

    @Test
    public void execute_deleteTag_preservesOtherTags() {
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withTagsToDelete("owesMoney").build();
        assertTagEditSuccess(INDEX_SECOND_PERSON, descriptor, "friends");
    }

    @Test
    public void execute_deleteAllNamedTags_success() {
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                .withTagsToDelete("owesMoney", "friends").build();
        assertTagEditSuccess(INDEX_SECOND_PERSON, descriptor);
    }

    @Test
    public void execute_deleteTagDifferentCase_success() {
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withTagsToDelete("FRIENDS").build();
        assertTagEditSuccess(INDEX_FIRST_PERSON, descriptor);
    }

    @Test
    public void execute_addAndDeleteTags_success() {
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                .withTagsToAdd("solderer").withTagsToDelete("owesMoney").build();
        assertTagEditSuccess(INDEX_SECOND_PERSON, descriptor, "friends", "solderer");
    }

    @Test
    public void execute_tagEditFilteredList_success() {
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                .withTagsToAdd("solderer").withTagsToDelete("owesMoney").build();
        assertTagEditSuccess(INDEX_FIRST_PERSON, descriptor, "friends", "solderer");
    }

    @Test
    public void execute_editTagsFilteredList_preservesFollowUp() {
        Person original = new PersonBuilder(model.getFilteredPersonList().get(1))
                .withTags("trainee", "builder")
                .withFollowUp(VALID_FOLLOW_UP_DATE, VALID_FOLLOW_UP_DESCRIPTION)
                .build();
        AddressBook initialState = new AddressBook(model.getAddressBook());
        initialState.setPerson(initialState.getPersonList().get(1), original);
        model.setAddressBook(initialState);
        model.commitAddressBook();

        Person edited = new PersonBuilder(original).withTags("builder", "solderer").build();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                .withTagsToAdd("solderer")
                .withTagsToDelete("trainee")
                .build();

        Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        expectedModel.setAddressBook(initialState);
        expectedModel.commitAddressBook();

        expectedModel.setPerson(original, edited);
        expectedModel.commitAddressBook();

        showPersonAtIndex(model, INDEX_SECOND_PERSON);

        String expectedMessage =
                String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(edited));
        assertCommandSuccess(new EditCommand(INDEX_FIRST_PERSON, descriptor),
                model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_deleteMissingTag_failure() {
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withTagsToDelete("trainee").build();
        assertCommandFailure(new EditCommand(INDEX_FIRST_PERSON, descriptor), model,
                String.format(EditCommand.MESSAGE_TAG_NOT_FOUND, "trainee"));
    }

    @Test
    public void execute_deleteTagFromUntaggedPerson_failure() {
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withTagsToDelete("trainee").build();
        assertCommandFailure(new EditCommand(Index.fromOneBased(3), descriptor), model,
                String.format(EditCommand.MESSAGE_TAG_NOT_FOUND, "trainee"));
    }

    @Test
    public void execute_missingDeletionWithOtherEdits_failureLeavesAllDetailsUnchanged() {
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName("Alex")
                .withRole(VALID_ROLE_BOB).withTagsToAdd("solderer")
                .withTagsToDelete("owesMoney", "trainee").build();

        // The failure helper checks that both the address book and displayed list are unchanged.
        assertCommandFailure(new EditCommand(INDEX_FIRST_PERSON, descriptor), model,
                String.format(EditCommand.MESSAGE_TAG_NOT_FOUND, "trainee"));
    }

    @Test
    public void execute_multipleMissingDeletions_reportsFirstAlphabetically() {
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                .withTagsToDelete("trainee", "solderer").build();
        assertCommandFailure(new EditCommand(INDEX_FIRST_PERSON, descriptor), model,
                String.format(EditCommand.MESSAGE_TAG_NOT_FOUND, "solderer"));
    }

    @Test
    public void parseAndExecute_addTag_preservesExistingTags() throws Exception {
        Person original = model.getFilteredPersonList().get(0);
        Person expectedPerson = new PersonBuilder(original).withTags("friends", "solderer").build();
        assertParsedEditSuccess("edit 1 t/solderer", expectedPerson);
    }

    @Test
    public void parseAndExecute_deleteTag_preservesOtherTags() throws Exception {
        Person original = model.getFilteredPersonList().get(0);
        Person taggedPerson = new PersonBuilder(original).withTags("trainee", "supplier").build();
        model.setPerson(original, taggedPerson);
        Person expectedPerson = new PersonBuilder(taggedPerson).withTags("supplier").build();
        assertParsedEditSuccess("edit 1 dt/trainee", expectedPerson);
    }

    @Test
    public void parseAndExecute_addAndDeleteTags_preservesOtherTags() throws Exception {
        Person original = model.getFilteredPersonList().get(0);
        Person taggedPerson = new PersonBuilder(original).withTags("trainee", "supplier").build();
        model.setPerson(original, taggedPerson);
        Person expectedPerson = new PersonBuilder(taggedPerson).withTags("supplier", "solderer").build();
        assertParsedEditSuccess("edit 1 t/solderer dt/trainee", expectedPerson);
    }

    @Test
    public void parseAndExecute_missingDeletion_rejectsEntireEdit() throws Exception {
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        Command command = new AddressBookParser().parseCommand(
                "edit 1 n/Alex p/12345678 e/alex@example.com a/New Address r/Employee"
                + " t/solderer dt/owesMoney dt/trainee");
        assertCommandFailure(command, model, String.format(EditCommand.MESSAGE_TAG_NOT_FOUND, "trainee"));
    }

    @Test
    public void parseAndExecute_duplicatePhone_rejectsTagChanges() throws Exception {
        String duplicatePhone = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased()).getPhone().value;
        Command command = new AddressBookParser().parseCommand(
                "edit 1 p/" + duplicatePhone + " t/solderer dt/friends");
        assertCommandFailure(command, model, Messages.MESSAGE_DUPLICATE_PHONE);
    }

    @Test
    public void parseAndExecute_repeatedTagsAndCaseDifferences_success() throws Exception {
        Person original = model.getFilteredPersonList().get(0);
        Person expectedPerson = new PersonBuilder(original).withTags("solderer").build();
        assertParsedEditSuccess("edit 1 t/solderer t/SOLDERER dt/friends dt/FRIENDS", expectedPerson);
    }

    @Test
    public void parseAndExecute_caseInsensitiveTagOperationsUnderTurkishLocale_success() throws Exception {
        Locale originalLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            Person original = model.getFilteredPersonList().get(0);
            assertParsedEditSuccess("edit 1 t/FRIENDS", original);
            Person expectedPerson = new PersonBuilder(original).withTags().build();
            assertParsedEditSuccess("edit 1 dt/FRIENDS", expectedPerson);
        } finally {
            Locale.setDefault(originalLocale);
        }
    }

    @Test
    public void parseCommand_emptyOrConflictingTags_rejectsWithoutChangingModel() {
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        AddressBookParser parser = new AddressBookParser();
        assertThrows(ParseException.class, EditCommand.MESSAGE_EMPTY_TAG, () -> parser.parseCommand(
                "edit 1 n/Alex t/"));
        assertEquals(expectedModel, model);
        assertThrows(ParseException.class, EditCommand.MESSAGE_EMPTY_TAG, () -> parser.parseCommand(
                "edit 1 n/Alex dt/"));
        assertEquals(expectedModel, model);
        String conflictMessage = String.format(EditCommand.MESSAGE_TAG_CONFLICT, "trainee");
        assertThrows(ParseException.class, conflictMessage, () -> parser.parseCommand(
                "edit 1 n/Alex t/trainee dt/TRAINEE"));
        assertEquals(expectedModel, model);
    }

    /**
     * Parses and executes an edit of the first displayed contact, checking the feedback and full model.
     */
    private void assertParsedEditSuccess(String input, Person expectedPerson) throws ParseException {
        Command command = new AddressBookParser().parseCommand(input);
        expectedTagEditModel.setAddressBook(model.getAddressBook());
        expectedTagEditModel.setPerson(model.getFilteredPersonList().get(0), expectedPerson);
        expectedTagEditModel.commitAddressBook();
        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS,
                Messages.format(expectedPerson));
        assertCommandSuccess(command, model, expectedMessage, expectedTagEditModel);
    }

    /**
     * Asserts that a tag edit updates the selected contact and preserves all its other fields.
     */
    private void assertTagEditSuccess(Index index, EditPersonDescriptor descriptor, String... expectedTags) {
        Person original = model.getFilteredPersonList().get(index.getZeroBased());
        Person editedPerson = new PersonBuilder(original).withTags(expectedTags).build();
        expectedTagEditModel.setAddressBook(model.getAddressBook());
        expectedTagEditModel.setPerson(original, editedPerson);
        expectedTagEditModel.commitAddressBook();
        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));

        assertCommandSuccess(new EditCommand(index, descriptor), model, expectedMessage, expectedTagEditModel);
    }

    @Test
    public void execute_someFieldsSpecifiedUnfilteredList_success() {
        Index indexLastPerson = Index.fromOneBased(model.getFilteredPersonList().size());
        Person lastPerson = model.getFilteredPersonList().get(indexLastPerson.getZeroBased());

        PersonBuilder personInList = new PersonBuilder(lastPerson);
        Person editedPerson = personInList.withName(VALID_NAME_BOB).withPhone(VALID_PHONE_BOB)
                .withTags(VALID_TAG_HUSBAND).build();
        assertEquals(lastPerson.getRole(), editedPerson.getRole());

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB)
                .withPhone(VALID_PHONE_BOB).withTagsToAdd(VALID_TAG_HUSBAND).build();
        EditCommand editCommand = new EditCommand(indexLastPerson, descriptor);

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(lastPerson, editedPerson);
        expectedModel.commitAddressBook();

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_roleSpecifiedUnfilteredList_success() {
        Person personToEdit = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person editedPerson = new PersonBuilder(personToEdit).withRole(VALID_ROLE_BOB).build();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withRole(VALID_ROLE_BOB).build();
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON, descriptor);

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(personToEdit, editedPerson);
        expectedModel.commitAddressBook();

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_roleAndOtherFieldSpecified_success() {
        Person personToEdit = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person editedPerson = new PersonBuilder(personToEdit).withRole(VALID_ROLE_BOB)
                .withName(VALID_NAME_BOB).build();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withRole(VALID_ROLE_BOB)
                .withName(VALID_NAME_BOB).build();
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON, descriptor);

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(personToEdit, editedPerson);
        expectedModel.commitAddressBook();

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_noFieldSpecifiedUnfilteredList_success() {
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON, new EditPersonDescriptor());
        Person editedPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.commitAddressBook();

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_filteredList_success() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        Person personInFilteredList = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person editedPerson = new PersonBuilder(personInFilteredList).withName(VALID_NAME_BOB).build();
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB).build());

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(model.getFilteredPersonList().get(0), editedPerson);
        expectedModel.commitAddressBook();

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_duplicatePersonUnfilteredList_failure() {
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder(firstPerson).build();
        EditCommand editCommand = new EditCommand(INDEX_SECOND_PERSON, descriptor);

        assertCommandFailure(editCommand, model, Messages.MESSAGE_DUPLICATE_PHONE_AND_EMAIL);
    }

    @Test
    public void execute_duplicatePhoneOnlyUnfilteredList_failure() {
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                .withPhone(firstPerson.getPhone().value).build();
        EditCommand editCommand = new EditCommand(INDEX_SECOND_PERSON, descriptor);

        assertCommandFailure(editCommand, model, Messages.MESSAGE_DUPLICATE_PHONE);
    }

    @Test
    public void execute_duplicateEmailOnlyUnfilteredList_failure() {
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                .withEmail(firstPerson.getEmail().value).build();
        EditCommand editCommand = new EditCommand(INDEX_SECOND_PERSON, descriptor);

        assertCommandFailure(editCommand, model, Messages.MESSAGE_DUPLICATE_EMAIL);
    }

    @Test
    public void execute_duplicateEmailDifferentCaseUnfilteredList_failure() {
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                .withEmail(firstPerson.getEmail().value.toUpperCase()).build();
        EditCommand editCommand = new EditCommand(INDEX_SECOND_PERSON, descriptor);

        assertCommandFailure(editCommand, model, Messages.MESSAGE_DUPLICATE_EMAIL);
    }

    @Test
    public void execute_sameNameDifferentPhoneAndEmailUnfilteredList_success() {
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person secondPerson = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                .withName(firstPerson.getName().fullName).build();
        EditCommand editCommand = new EditCommand(INDEX_SECOND_PERSON, descriptor);

        Person editedSecondPerson = new PersonBuilder(secondPerson).withName(firstPerson.getName().fullName).build();
        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS,
                Messages.format(editedSecondPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(secondPerson, editedSecondPerson);
        expectedModel.commitAddressBook();

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_duplicatePersonFilteredList_failure() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        // edit person in filtered list into a duplicate in address book
        Person personInList = model.getAddressBook().getPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder(personInList).build());

        assertCommandFailure(editCommand, model, Messages.MESSAGE_DUPLICATE_PHONE_AND_EMAIL);
    }

    @Test
    public void execute_invalidPersonIndexUnfilteredList_failure() {
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB).build();
        EditCommand editCommand = new EditCommand(outOfBoundIndex, descriptor);

        assertCommandFailure(editCommand, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    /**
     * Edit filtered list where index is larger than size of filtered list,
     * but smaller than size of address book
     */
    @Test
    public void execute_invalidPersonIndexFilteredList_failure() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        Index outOfBoundIndex = INDEX_SECOND_PERSON;
        // ensures that outOfBoundIndex is still in bounds of address book list
        assertTrue(outOfBoundIndex.getZeroBased() < model.getAddressBook().getPersonList().size());

        EditCommand editCommand = new EditCommand(outOfBoundIndex,
                new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB).build());

        assertCommandFailure(editCommand, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_invalidIndexAfterUndo_preservesRedoHistory() {
        Person newPerson = new PersonBuilder().build();
        model.addPerson(newPerson);
        model.commitAddressBook();
        model.undoAddressBook();

        Index invalidIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        EditPersonDescriptor descriptor =
                new EditPersonDescriptorBuilder().withName("Updated Name").build();

        assertCommandFailure(new EditCommand(invalidIndex, descriptor), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);

        assertTrue(model.canRedoAddressBook());
        model.redoAddressBook();
        assertTrue(model.hasPerson(newPerson));
    }

    @Test
    public void execute_duplicatePersonAfterUndo_preservesRedoHistory() {
        Person newPerson = new PersonBuilder().build();
        model.addPerson(newPerson);
        model.commitAddressBook();
        model.undoAddressBook();

        Person firstPerson = model.getFilteredPersonList()
                .get(INDEX_FIRST_PERSON.getZeroBased());
        EditPersonDescriptor descriptor =
                new EditPersonDescriptorBuilder(firstPerson).build();

        assertCommandFailure(new EditCommand(INDEX_SECOND_PERSON, descriptor), model,
                Messages.MESSAGE_DUPLICATE_PHONE_AND_EMAIL);

        assertTrue(model.canRedoAddressBook());
        model.redoAddressBook();
        assertTrue(model.hasPerson(newPerson));
    }

    @Test
    public void equals() {
        final EditCommand standardCommand = new EditCommand(INDEX_FIRST_PERSON, DESC_AMY);

        // same values -> returns true
        EditPersonDescriptor copyDescriptor = new EditPersonDescriptor(DESC_AMY);
        EditCommand commandWithSameValues = new EditCommand(INDEX_FIRST_PERSON, copyDescriptor);
        assertTrue(standardCommand.equals(commandWithSameValues));

        // same object -> returns true
        assertTrue(standardCommand.equals(standardCommand));

        // null -> returns false
        assertFalse(standardCommand.equals(null));

        // different types -> returns false
        assertFalse(standardCommand.equals(new ClearCommand()));

        // different index -> returns false
        assertFalse(standardCommand.equals(new EditCommand(INDEX_SECOND_PERSON, DESC_AMY)));

        // different descriptor -> returns false
        assertFalse(standardCommand.equals(new EditCommand(INDEX_FIRST_PERSON, DESC_BOB)));
    }

    @Test
    public void toStringMethod() {
        Index index = Index.fromOneBased(1);
        EditPersonDescriptor editPersonDescriptor = new EditPersonDescriptor();
        EditCommand editCommand = new EditCommand(index, editPersonDescriptor);
        String expected = EditCommand.class.getCanonicalName() + "{index=" + index + ", editPersonDescriptor="
                + editPersonDescriptor + "}";
        assertEquals(expected, editCommand.toString());
    }

}
