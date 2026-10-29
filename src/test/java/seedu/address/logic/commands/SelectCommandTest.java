package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.CARL;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.Comparator;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;

public class SelectCommandTest {
    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void constructor_nullIndex_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new SelectCommand(null));
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new SelectCommand(INDEX_FIRST_PERSON).execute(null));
    }

    @Test
    public void execute_validIndex_returnsExactContactWithoutChangingModel() throws Exception {
        Person target = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        CommandResult result = new SelectCommand(INDEX_SECOND_PERSON).execute(model);

        assertSame(target, result.getSelectedPerson().orElseThrow());
        assertEquals(String.format(SelectCommand.MESSAGE_SELECT_CONTACT_SUCCESS, 2, target.getName().fullName),
                result.getFeedbackToUser());
        assertFalse(result.isShowHelp());
        assertFalse(result.isExit());
        assertEquals(expectedModel, model);
        assertFalse(model.canUndoAddressBook());
        assertFalse(model.canRedoAddressBook());
    }

    @Test
    public void execute_filteredSortedList_usesDisplayedIndexAndPreservesView() throws Exception {
        Comparator<Person> order = Comparator.comparing(person -> person.getName().fullName, Comparator.reverseOrder());
        Predicate<Person> filter = person -> person == ALICE || person == CARL;
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        model.updateSortedPersonList(order);
        model.updateFilteredPersonList(filter);
        expectedModel.updateSortedPersonList(order);
        expectedModel.updateFilteredPersonList(filter);

        CommandResult result = new SelectCommand(INDEX_FIRST_PERSON).execute(model);
        assertSame(CARL, result.getSelectedPerson().orElseThrow());
        assertEquals(expectedModel, model);
    }

    @Test
    public void execute_emptyList_throwsCommandException() {
        assertCommandFailure(new SelectCommand(INDEX_FIRST_PERSON), new ModelManager(),
                SelectCommand.MESSAGE_NO_CONTACTS_TO_SELECT);
    }

    @Test
    public void execute_emptyFilteredList_throwsCommandException() {
        model.updateFilteredPersonList(person -> false);
        assertCommandFailure(new SelectCommand(INDEX_FIRST_PERSON), model, SelectCommand.MESSAGE_NO_CONTACTS_TO_SELECT);
    }

    @Test
    public void execute_outOfRangeIndex_throwsCommandException() {
        int size = model.getFilteredPersonList().size();
        assertCommandFailure(new SelectCommand(Index.fromOneBased(size + 1)), model,
                String.format(SelectCommand.MESSAGE_INDEX_OUT_OF_RANGE, size + 1, size));
    }

    @Test
    public void execute_singleDisplayedContact_rejectsIndexValidOnlyInUnderlyingBook() {
        model.updateFilteredPersonList(person -> person == ALICE);
        assertCommandFailure(new SelectCommand(INDEX_SECOND_PERSON), model,
                String.format(SelectCommand.MESSAGE_SINGLE_INDEX_OUT_OF_RANGE, 2));
    }

    @Test
    public void equalsAndHashCode_compareDisplayedIndexesByValue() {
        SelectCommand first = new SelectCommand(INDEX_FIRST_PERSON);
        SelectCommand equal = new SelectCommand(Index.fromOneBased(1));
        assertEquals(first, first);
        assertEquals(first, equal);
        assertEquals(first.hashCode(), equal.hashCode());
        assertNotEquals(first, new SelectCommand(INDEX_SECOND_PERSON));
        assertFalse(first.equals(null));
        assertFalse(first.equals(1));
    }

    @Test
    public void toStringMethod_includesTargetIndex() {
        SelectCommand command = new SelectCommand(INDEX_FIRST_PERSON);
        String expected = SelectCommand.class.getCanonicalName() + "{targetIndex=" + INDEX_FIRST_PERSON + "}";
        assertEquals(expected, command.toString());
    }
}
