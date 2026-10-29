package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;

public class ClearCommandTest {

    @Test
    public void execute_emptyAddressBook_success() {
        Model model = new ModelManager();
        Model expectedModel = new ModelManager();
        expectedModel.commitAddressBook();

        assertCommandSuccess(new ClearCommand(), model, ClearCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void execute_nonEmptyAddressBook_success() {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        expectedModel.setAddressBook(new AddressBook());
        expectedModel.commitAddressBook();

        assertCommandSuccess(new ClearCommand(), model, ClearCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void execute_clearWithExtraArguments_showsWarning() {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        expectedModel.setAddressBook(new AddressBook());
        expectedModel.commitAddressBook();

        String expectedMessage = ClearCommand.MESSAGE_SUCCESS
                + "\n"
                + ClearCommand.MESSAGE_IGNORED_ARGUMENTS;

        assertCommandSuccess(new ClearCommand(true), model, expectedMessage, expectedModel);
    }

    @Test
    public void equals() {
        ClearCommand clearFirst = new ClearCommand();
        ClearCommand clearSecond = new ClearCommand(false);
        ClearCommand clearWithArgs = new ClearCommand(true);

        // same values -> returns true
        assertTrue(clearFirst.equals(clearSecond));
        assertTrue(clearFirst.equals(clearFirst));

        // null -> returns false
        assertFalse(clearFirst.equals(null));

        // different types -> returns false
        assertFalse(clearFirst.equals(1));

        // different hasExtraArgs -> returns false
        assertFalse(clearFirst.equals(clearWithArgs));
    }

    @Test
    public void toStringMethod() {
        ClearCommand clearCommand = new ClearCommand(true);
        String expected = ClearCommand.class.getCanonicalName() + "{hasExtraArgs=true}";
        assertEquals(expected, clearCommand.toString());
    }

}

