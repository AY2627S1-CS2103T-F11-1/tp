package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ROLE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

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
 * Contains integration tests (interaction with the Model) and unit tests for EditCommand.
 */
public class EditCommandTest {

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_allFieldsSpecifiedUnfilteredList_success() {
        Person editedPerson = new PersonBuilder().withTags("solderer").build();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder(editedPerson)
                .withTagsToDelete("friends").build();
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON, descriptor);

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(model.getFilteredPersonList().get(0), editedPerson);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
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

    /**
     * Asserts that a tag edit updates the selected contact and preserves all its other fields.
     */
    private void assertTagEditSuccess(Index index, EditPersonDescriptor descriptor, String... expectedTags) {
        Person original = model.getFilteredPersonList().get(index.getZeroBased());
        Person editedPerson = new PersonBuilder(original).withTags(expectedTags).build();
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(original, editedPerson);
        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));

        assertCommandSuccess(new EditCommand(index, descriptor), model, expectedMessage, expectedModel);
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

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_noFieldSpecifiedUnfilteredList_success() {
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON, new EditPersonDescriptor());
        Person editedPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());

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
