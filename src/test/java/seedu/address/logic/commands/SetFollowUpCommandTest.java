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

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.FollowUp;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class SetFollowUpCommandTest {

    private static final FollowUp FOLLOW_UP = new FollowUp(
            LocalDate.parse(VALID_FOLLOW_UP_DATE), VALID_FOLLOW_UP_DESCRIPTION);

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
    private final Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new SetFollowUpCommand(null, FOLLOW_UP));
        assertThrows(NullPointerException.class, () -> new SetFollowUpCommand(INDEX_FIRST_PERSON, null));
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        SetFollowUpCommand command = new SetFollowUpCommand(INDEX_FIRST_PERSON, FOLLOW_UP);

        assertThrows(NullPointerException.class, () -> command.execute(null));
    }

    @Test
    public void execute_contactWithoutFollowUp_success() {
        assertSetSuccess(INDEX_FIRST_PERSON, FOLLOW_UP);
    }

    @Test
    public void execute_contactWithFollowUp_replacesBothFields() {
        seedFollowUp();

        FollowUp replacement =
                new FollowUp(FOLLOW_UP.getDueDate().plusDays(1), "Confirm revised quotation");

        assertSetSuccess(INDEX_FIRST_PERSON, replacement);
    }

    @Test
    public void execute_sameFollowUp_commitsAnotherState() {
        assertSetSuccess(INDEX_FIRST_PERSON, FOLLOW_UP);
        assertSetSuccess(INDEX_FIRST_PERSON, FOLLOW_UP);

        // Undoing the second set retains the first set.
        model.undoAddressBook();
        expectedModel.undoAddressBook();
        assertEquals(expectedModel, model);
        assertEquals(FOLLOW_UP,
                model.getAddressBook().getPersonList().get(0).getFollowUp().orElseThrow());

        // Undoing the first set restores the original contact.
        model.undoAddressBook();
        expectedModel.undoAddressBook();
        assertEquals(expectedModel, model);
        assertTrue(model.getAddressBook().getPersonList().get(0).getFollowUp().isEmpty());
    }

    @Test
    public void execute_filteredList_preservesFilterAndUsesDisplayedIndex() {
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        showPersonAtIndex(expectedModel, INDEX_SECOND_PERSON);

        assertSetSuccess(INDEX_FIRST_PERSON, FOLLOW_UP);

        assertEquals(1, model.getFilteredPersonList().size());
        assertEquals(model.getAddressBook().getPersonList().get(1),
                model.getFilteredPersonList().get(0));
    }

    @Test
    public void execute_emptyList_failure() {
        Model emptyModel = new ModelManager();

        assertCommandFailure(new SetFollowUpCommand(INDEX_FIRST_PERSON, FOLLOW_UP),
                emptyModel, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_invalidIndexUnfilteredList_failure() {
        Index invalidIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);

        assertCommandFailure(new SetFollowUpCommand(invalidIndex, FOLLOW_UP),
                model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_invalidIndexFilteredList_failure() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        assertCommandFailure(new SetFollowUpCommand(INDEX_SECOND_PERSON, FOLLOW_UP),
                model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_undoRedo_restoresFollowUp() {
        AddressBook initialState = new AddressBook(model.getAddressBook());
        assertSetSuccess(INDEX_FIRST_PERSON, FOLLOW_UP);

        model.undoAddressBook();
        expectedModel.undoAddressBook();
        assertEquals(expectedModel, model);
        assertEquals(initialState, model.getAddressBook());
        assertFalse(model.canUndoAddressBook());

        model.redoAddressBook();
        expectedModel.redoAddressBook();
        assertEquals(expectedModel, model);
        assertEquals(FOLLOW_UP,
                model.getAddressBook().getPersonList().get(0).getFollowUp().orElseThrow());
    }

    @Test
    public void execute_afterUndo_discardsRedoHistory() {
        assertSetSuccess(INDEX_FIRST_PERSON, FOLLOW_UP);
        model.undoAddressBook();
        expectedModel.undoAddressBook();

        assertTrue(model.canRedoAddressBook());
        assertSetSuccess(INDEX_SECOND_PERSON, FOLLOW_UP);

        assertFalse(model.canRedoAddressBook());
    }

    @Test
    public void execute_invalidIndexAfterUndo_preservesRedoHistory() {
        assertSetSuccess(INDEX_FIRST_PERSON, FOLLOW_UP);
        model.undoAddressBook();

        Index invalidIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        assertCommandFailure(new SetFollowUpCommand(invalidIndex, FOLLOW_UP),
                model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);

        model.redoAddressBook();
        assertEquals(expectedModel, model);
    }

    @Test
    public void equals() {
        SetFollowUpCommand command = new SetFollowUpCommand(INDEX_FIRST_PERSON, FOLLOW_UP);
        FollowUp equalFollowUp = new FollowUp(
                FOLLOW_UP.getDueDate(), FOLLOW_UP.getDescription());

        // same object -> returns true
        assertTrue(command.equals(command));

        // same values -> returns true
        assertTrue(command.equals(new SetFollowUpCommand(INDEX_FIRST_PERSON, equalFollowUp)));

        // null -> returns false
        assertFalse(command.equals(null));

        // different types -> returns false
        assertFalse(command.equals(new ClearFollowUpCommand(INDEX_FIRST_PERSON)));

        // different values -> returns false
        assertFalse(command.equals(new SetFollowUpCommand(INDEX_SECOND_PERSON, FOLLOW_UP)));
        assertFalse(command.equals(new SetFollowUpCommand(INDEX_FIRST_PERSON,
                new FollowUp(FOLLOW_UP.getDueDate().plusDays(1), FOLLOW_UP.getDescription()))));
        assertFalse(command.equals(new SetFollowUpCommand(INDEX_FIRST_PERSON,
                new FollowUp(FOLLOW_UP.getDueDate(), "Different description"))));
    }

    @Test
    public void toStringMethod() {
        SetFollowUpCommand command = new SetFollowUpCommand(INDEX_FIRST_PERSON, FOLLOW_UP);
        String expected = SetFollowUpCommand.class.getCanonicalName()
                + "{index=" + INDEX_FIRST_PERSON + ", followUp=" + FOLLOW_UP + "}";

        assertEquals(expected, command.toString());
    }

    /**
     * Adds an existing follow-up to both models and records identical setup histories.
     */
    private void seedFollowUp() {
        Person original = model.getFilteredPersonList().get(0);
        Person updated = new PersonBuilder(original)
                .withFollowUp(VALID_FOLLOW_UP_DATE, VALID_FOLLOW_UP_DESCRIPTION)
                .build();

        model.setPerson(original, updated);
        model.commitAddressBook();
        expectedModel.setPerson(original, updated);
        expectedModel.commitAddressBook();
    }

    /**
     * Verifies the result, complete contact data, filter, and history after setting a follow-up.
     */
    private void assertSetSuccess(Index index, FollowUp followUp) {
        Person original = expectedModel.getFilteredPersonList().get(index.getZeroBased());
        Person updated = new PersonBuilder(original)
                .withFollowUp(followUp.getDueDate().toString(), followUp.getDescription())
                .build();

        expectedModel.setPerson(original, updated);
        expectedModel.commitAddressBook();

        String expectedMessage = String.format(SetFollowUpCommand.MESSAGE_SUCCESS,
                original.getName(), followUp.getDueDate(), followUp.getDescription());

        assertCommandSuccess(new SetFollowUpCommand(index, followUp),
                model, expectedMessage, expectedModel);
    }
}
