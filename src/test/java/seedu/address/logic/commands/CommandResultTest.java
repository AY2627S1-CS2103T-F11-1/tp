package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class CommandResultTest {
    @Test
    public void constructor_existingSignatures_haveNoSelectionRequest() {
        for (CommandResult result : List.of(new CommandResult("feedback"),
                new CommandResult("feedback", false, false), new CommandResult("help", true, false),
                new CommandResult("exit", false, true))) {
            assertTrue(result.getSelectedPerson().isEmpty());
        }
    }

    @Test
    public void constructor_selectionRequest_retainsExactContactAndDefaultsOtherActions() {
        CommandResult result = new CommandResult("Selection requested", ALICE);
        assertEquals("Selection requested", result.getFeedbackToUser());
        assertSame(ALICE, result.getSelectedPerson().orElseThrow());
        assertFalse(result.isShowHelp());
        assertFalse(result.isExit());
        assertThrows(NullPointerException.class, () -> new CommandResult("feedback", null));
        assertThrows(NullPointerException.class, () -> new CommandResult(null, ALICE));
    }

    @Test
    public void equalsAndHashCode_includeSelectionRequest() {
        CommandResult result = new CommandResult("feedback", ALICE);
        CommandResult equal = new CommandResult("feedback", new PersonBuilder(ALICE).build());
        assertEquals(result, equal);
        assertEquals(result.hashCode(), equal.hashCode());
        assertNotEquals(result, new CommandResult("feedback"));
        assertNotEquals(result, new CommandResult("feedback", BENSON));
        assertNotEquals(result, new CommandResult("different", ALICE));
    }

    @Test
    public void equals() {
        CommandResult commandResult = new CommandResult("feedback");

        // same values -> returns true
        assertTrue(commandResult.equals(new CommandResult("feedback")));
        assertTrue(commandResult.equals(new CommandResult("feedback", false, false)));

        // same object -> returns true
        assertTrue(commandResult.equals(commandResult));

        // null -> returns false
        assertFalse(commandResult.equals(null));

        // different types -> returns false
        assertFalse(commandResult.equals(0.5f));

        // different feedbackToUser value -> returns false
        assertFalse(commandResult.equals(new CommandResult("different")));

        // different showHelp value -> returns false
        assertFalse(commandResult.equals(new CommandResult("feedback", true, false)));

        // different exit value -> returns false
        assertFalse(commandResult.equals(new CommandResult("feedback", false, true)));
    }

    @Test
    public void hashcode() {
        CommandResult commandResult = new CommandResult("feedback");

        // same values -> returns same hashcode
        assertEquals(commandResult.hashCode(), new CommandResult("feedback").hashCode());

        // different feedbackToUser value -> returns different hashcode
        assertNotEquals(commandResult.hashCode(), new CommandResult("different").hashCode());

        // different showHelp value -> returns different hashcode
        assertNotEquals(commandResult.hashCode(), new CommandResult("feedback", true, false).hashCode());

        // different exit value -> returns different hashcode
        assertNotEquals(commandResult.hashCode(), new CommandResult("feedback", false, true).hashCode());
    }

    @Test
    public void toStringMethod() {
        CommandResult commandResult = new CommandResult("feedback");
        String expected = CommandResult.class.getCanonicalName() + "{feedbackToUser="
                + commandResult.getFeedbackToUser() + ", showHelp=" + commandResult.isShowHelp()
                + ", exit=" + commandResult.isExit() + ", selectedPerson=" + commandResult.getSelectedPerson() + "}";
        assertEquals(expected, commandResult.toString());
    }

    @Test
    public void toStringMethod_includesRequestedContact() {
        CommandResult result = new CommandResult("feedback", ALICE);
        String expected = CommandResult.class.getCanonicalName()
                + "{feedbackToUser=feedback, showHelp=false, exit=false, "
                + "selectedPerson=" + result.getSelectedPerson() + "}";
        assertEquals(expected, result.toString());
    }
}
