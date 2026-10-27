package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_FOLLOW_UP_DATE;
import static seedu.address.logic.commands.CommandTestUtil.VALID_FOLLOW_UP_DESCRIPTION;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for
 * {@code DeleteCommand}.
 */
public class DeleteCommandTest {

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_emptyList_throwsCommandException() {
        Model emptyModel = new ModelManager();
        DeleteCommand deleteCommand = new DeleteCommand(INDEX_FIRST_PERSON);

        assertCommandFailure(deleteCommand, emptyModel, DeleteCommand.MESSAGE_NO_CONTACTS_TO_DELETE);
    }

    @Test
    public void execute_validIndexUnfilteredList_success() {
        Person personToDelete = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        DeleteCommand deleteCommand = new DeleteCommand(INDEX_FIRST_PERSON);

        String expectedMessage = String.format(DeleteCommand.MESSAGE_DELETE_CONTACT_SUCCESS,
                Messages.format(personToDelete));

        ModelManager expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.deletePerson(personToDelete);
        expectedModel.commitAddressBook();

        assertCommandSuccess(deleteCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_deleteContactWithFollowUp_undoRestoresAndRedoRemoves() {
        Person original = new PersonBuilder(model.getFilteredPersonList().get(0))
                .withFollowUp(VALID_FOLLOW_UP_DATE, VALID_FOLLOW_UP_DESCRIPTION)
                .build();

        AddressBook initialState = new AddressBook(model.getAddressBook());
        initialState.setPerson(initialState.getPersonList().get(0), original);
        model.setAddressBook(initialState);
        model.commitAddressBook();

        Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        expectedModel.setAddressBook(initialState);
        expectedModel.commitAddressBook();
        expectedModel.deletePerson(original);
        expectedModel.commitAddressBook();

        String expectedMessage =
                String.format(DeleteCommand.MESSAGE_DELETE_CONTACT_SUCCESS, Messages.format(original));
        assertCommandSuccess(new DeleteCommand(INDEX_FIRST_PERSON), model, expectedMessage, expectedModel);

        model.undoAddressBook();
        assertEquals(initialState, model.getAddressBook());

        model.redoAddressBook();
        assertEquals(expectedModel.getAddressBook(), model.getAddressBook());
    }

    @Test
    public void execute_invalidIndexMultipleContacts_throwsCommandException() {
        assertTrue(model.getFilteredPersonList().size() > 1);

        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        DeleteCommand deleteCommand = new DeleteCommand(outOfBoundIndex);

        String expectedMessage = String.format(
                DeleteCommand.MESSAGE_INDEX_OUT_OF_RANGE,
                outOfBoundIndex.getOneBased(),
                model.getFilteredPersonList().size());

        assertCommandFailure(deleteCommand, model, expectedMessage);
    }

    @Test
    public void execute_validIndexFilteredList_success() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        Person personToDelete = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        DeleteCommand deleteCommand = new DeleteCommand(INDEX_FIRST_PERSON);

        String expectedMessage = String.format(DeleteCommand.MESSAGE_DELETE_CONTACT_SUCCESS,
                Messages.format(personToDelete));

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.deletePerson(personToDelete);
        expectedModel.commitAddressBook();
        showNoPerson(expectedModel);

        assertCommandSuccess(deleteCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_invalidIndexSingleContact_throwsCommandException() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        assertEquals(1, model.getFilteredPersonList().size());

        Index outOfBoundIndex = INDEX_SECOND_PERSON;
        // ensures that outOfBoundIndex is still in bounds of address book list
        assertTrue(outOfBoundIndex.getZeroBased() < model.getAddressBook().getPersonList().size());

        DeleteCommand deleteCommand = new DeleteCommand(outOfBoundIndex);

        String expectedMessage = String.format(
                DeleteCommand.MESSAGE_SINGLE_INDEX_OUT_OF_RANGE,
                outOfBoundIndex.getOneBased());

        assertCommandFailure(deleteCommand, model, expectedMessage);
    }

    @Test
    public void execute_emptyFilteredList_throwsCommandException() {
        model.updateFilteredPersonList(person -> false);

        DeleteCommand deleteCommand = new DeleteCommand(INDEX_FIRST_PERSON);

        assertCommandFailure(deleteCommand, model, DeleteCommand.MESSAGE_NO_CONTACTS_TO_DELETE);
    }

    @Test
    public void execute_invalidIndexAfterUndo_preservesRedoHistory() {
        Person newPerson = new PersonBuilder().build();
        model.addPerson(newPerson);
        model.commitAddressBook();
        model.undoAddressBook();

        Index invalidIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        String expectedMessage = String.format(DeleteCommand.MESSAGE_INDEX_OUT_OF_RANGE, invalidIndex.getOneBased(),
                model.getFilteredPersonList().size());

        assertCommandFailure(new DeleteCommand(invalidIndex), model, expectedMessage);

        assertTrue(model.canRedoAddressBook());
        model.redoAddressBook();
        assertTrue(model.hasPerson(newPerson));
    }

    @Test
    public void equals() {
        DeleteCommand deleteFirstCommand = new DeleteCommand(INDEX_FIRST_PERSON);
        DeleteCommand deleteSecondCommand = new DeleteCommand(INDEX_SECOND_PERSON);

        // same object -> returns true
        assertTrue(deleteFirstCommand.equals(deleteFirstCommand));

        // same values -> returns true
        DeleteCommand deleteFirstCommandCopy = new DeleteCommand(INDEX_FIRST_PERSON);
        assertTrue(deleteFirstCommand.equals(deleteFirstCommandCopy));

        // different types -> returns false
        assertFalse(deleteFirstCommand.equals(1));

        // null -> returns false
        assertFalse(deleteFirstCommand.equals(null));

        // different person -> returns false
        assertFalse(deleteFirstCommand.equals(deleteSecondCommand));
    }

    @Test
    public void toStringMethod() {
        Index targetIndex = Index.fromOneBased(1);
        DeleteCommand deleteCommand = new DeleteCommand(targetIndex);
        String expected = DeleteCommand.class.getCanonicalName() + "{targetIndex=" + targetIndex + "}";
        assertEquals(expected, deleteCommand.toString());
    }

    /**
     * Updates {@code model}'s filtered list to show no one.
     */
    private void showNoPerson(Model model) {
        model.updateFilteredPersonList(p -> false);

        assertTrue(model.getFilteredPersonList().isEmpty());
    }
}
