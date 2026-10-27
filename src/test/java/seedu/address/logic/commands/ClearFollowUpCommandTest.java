package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_FOLLOW_UP_DATE;
import static seedu.address.logic.commands.CommandTestUtil.VALID_FOLLOW_UP_DESCRIPTION;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.Assert.assertThrows;
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

public class ClearFollowUpCommandTest {

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
    private final Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void constructor_nullIndex_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ClearFollowUpCommand(null));
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        ClearFollowUpCommand command = new ClearFollowUpCommand(INDEX_FIRST_PERSON);

        assertThrows(NullPointerException.class, () -> command.execute(null));
    }

    @Test
    public void execute_contactWithFollowUp_success() {
        seedFollowUp(INDEX_FIRST_PERSON);

        assertClearSuccess(INDEX_FIRST_PERSON);
    }

    @Test
    public void execute_contactWithoutFollowUp_failure() {
        Person original = model.getFilteredPersonList().get(0);

        assertCommandFailure(new ClearFollowUpCommand(INDEX_FIRST_PERSON),
                model, String.format(ClearFollowUpCommand.MESSAGE_NO_FOLLOW_UP,
                        original.getName()));
    }

    @Test
    public void execute_filteredList_preservesFilterAndUsesDisplayedIndex() {
        seedFollowUp(INDEX_SECOND_PERSON);
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        showPersonAtIndex(expectedModel, INDEX_SECOND_PERSON);

        assertClearSuccess(INDEX_FIRST_PERSON);

        assertEquals(1, model.getFilteredPersonList().size());
        assertEquals(model.getAddressBook().getPersonList().get(1),
                model.getFilteredPersonList().get(0));
    }

    @Test
    public void execute_followUpOnlyFilter_removesContactFromView() {
        seedFollowUp(INDEX_FIRST_PERSON);
        model.updateFilteredPersonList(person -> person.getFollowUp().isPresent());
        expectedModel.updateFilteredPersonList(person -> person.getFollowUp().isPresent());

        assertClearSuccess(INDEX_FIRST_PERSON);

        assertTrue(model.getFilteredPersonList().isEmpty());
        assertFalse(model.getAddressBook().getPersonList().isEmpty());
    }

    @Test
    public void execute_emptyList_failure() {
        Model emptyModel = new ModelManager();

        assertCommandFailure(new ClearFollowUpCommand(INDEX_FIRST_PERSON),
                emptyModel, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_invalidIndexUnfilteredList_failure() {
        Index invalidIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);

        assertCommandFailure(new ClearFollowUpCommand(invalidIndex),
                model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_invalidIndexFilteredList_failure() {
        seedFollowUp(INDEX_FIRST_PERSON);
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        assertCommandFailure(new ClearFollowUpCommand(INDEX_SECOND_PERSON),
                model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_undoRedo_restoresAndRemovesFollowUp() {
        seedFollowUp(INDEX_FIRST_PERSON);
        AddressBook initialState = new AddressBook(model.getAddressBook());

        assertClearSuccess(INDEX_FIRST_PERSON);

        model.undoAddressBook();
        expectedModel.undoAddressBook();
        assertEquals(expectedModel, model);
        assertEquals(initialState, model.getAddressBook());

        model.redoAddressBook();
        expectedModel.redoAddressBook();
        assertEquals(expectedModel, model);
        assertTrue(model.getAddressBook().getPersonList().get(0).getFollowUp().isEmpty());
    }

    @Test
    public void execute_noFollowUpAfterUndo_preservesRedoHistory() {
        seedFollowUp(INDEX_FIRST_PERSON);
        model.undoAddressBook();

        Person original = model.getFilteredPersonList().get(0);
        assertCommandFailure(new ClearFollowUpCommand(INDEX_FIRST_PERSON),
                model, String.format(ClearFollowUpCommand.MESSAGE_NO_FOLLOW_UP,
                        original.getName()));

        model.redoAddressBook();
        assertEquals(expectedModel, model);
    }

    @Test
    public void execute_afterUndo_discardsRedoHistory() {
        seedFollowUp(INDEX_FIRST_PERSON);
        assertClearSuccess(INDEX_FIRST_PERSON);

        model.undoAddressBook();
        expectedModel.undoAddressBook();
        assertTrue(model.canRedoAddressBook());

        assertClearSuccess(INDEX_FIRST_PERSON);

        assertFalse(model.canRedoAddressBook());
    }

    @Test
    public void equals() {
        ClearFollowUpCommand command = new ClearFollowUpCommand(INDEX_FIRST_PERSON);

        // same object -> returns true
        assertTrue(command.equals(command));

        // same values -> returns true
        assertTrue(command.equals(new ClearFollowUpCommand(INDEX_FIRST_PERSON)));

        // null -> returns false
        assertFalse(command.equals(null));

        // different tpyes -> returns false
        assertFalse(command.equals(new UndoCommand()));

        // different values -> returns false
        assertFalse(command.equals(new ClearFollowUpCommand(INDEX_SECOND_PERSON)));
    }

    @Test
    public void toStringMethod() {
        ClearFollowUpCommand command = new ClearFollowUpCommand(INDEX_FIRST_PERSON);
        String expected = ClearFollowUpCommand.class.getCanonicalName()
                + "{index=" + INDEX_FIRST_PERSON + "}";

        assertEquals(expected, command.toString());
    }

    /**
     * Adds a follow-up to both models and records identical setup histories.
     */
    private void seedFollowUp(Index index) {
        Person original = model.getFilteredPersonList().get(index.getZeroBased());
        Person updated = new PersonBuilder(original)
                .withFollowUp(VALID_FOLLOW_UP_DATE, VALID_FOLLOW_UP_DESCRIPTION)
                .build();

        model.setPerson(original, updated);
        model.commitAddressBook();
        expectedModel.setPerson(original, updated);
        expectedModel.commitAddressBook();
    }

    /**
     * Verifies the result, complete contact data, filter, and history after clearing a follow-up.
     */
    private void assertClearSuccess(Index index) {
        Person original = expectedModel.getFilteredPersonList().get(index.getZeroBased());
        Person updated = new PersonBuilder(original).withoutFollowUp().build();

        expectedModel.setPerson(original, updated);
        expectedModel.commitAddressBook();

        String expectedMessage =
                String.format(ClearFollowUpCommand.MESSAGE_SUCCESS, original.getName());

        assertCommandSuccess(new ClearFollowUpCommand(index),
                model, expectedMessage, expectedModel);
    }
}
