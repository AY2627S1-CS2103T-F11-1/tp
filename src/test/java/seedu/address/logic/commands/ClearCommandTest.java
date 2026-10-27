package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;

public class ClearCommandTest {

    @Test
    public void execute_emptyAddressBook_showsAlreadyEmpty() {
        Model model = new ModelManager();
        Model expectedModel = new ModelManager();

        assertCommandSuccess(new ClearCommand(), model, ClearCommand.MESSAGE_EMPTY_ADDRESS_BOOK, expectedModel);
        assertFalse(model.canUndoAddressBook());
    }

    @Test
    public void execute_emptyAddressBook_preservesUndoRedoHistory() {
        Model model = new ModelManager();
        model.addPerson(ALICE);
        model.commitAddressBook();

        // Undo to revert back to empty book while redo is available
        model.undoAddressBook();
        assertTrue(model.getAddressBook().getPersonList().isEmpty());
        assertTrue(model.canRedoAddressBook());

        // Clear empty address book
        CommandResult result = new ClearCommand().execute(model);
        assertEquals(ClearCommand.MESSAGE_EMPTY_ADDRESS_BOOK, result.getFeedbackToUser());

        // Redo history must be preserved
        assertTrue(model.canRedoAddressBook());
        model.redoAddressBook();
        assertTrue(model.hasPerson(ALICE));
    }

    @Test
    public void execute_nonEmptyAddressBook_success() {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        expectedModel.setAddressBook(new AddressBook());
        expectedModel.commitAddressBook();

        assertCommandSuccess(new ClearCommand(), model, ClearCommand.MESSAGE_SUCCESS, expectedModel);
    }

}
