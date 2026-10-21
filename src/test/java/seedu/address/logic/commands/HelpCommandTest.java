package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;

public class HelpCommandTest {
    private Model model = new ModelManager();

    @Test
    public void execute_help_opensFullHelpWindow() throws Exception {
        CommandResult result = executeHelpInput("help");

        assertEquals(HelpCommand.SHOWING_HELP_MESSAGE, result.getFeedbackToUser());
        assertTrue(result.isShowHelp());
        assertFalse(result.isExit());
    }

    @Test
    public void execute_helpAdd_returnsAddUsageInResultFeedback() throws Exception {
        CommandResult result = executeHelpInput("help add");

        assertEquals(AddCommand.MESSAGE_USAGE, result.getFeedbackToUser());
        assertFalse(result.isShowHelp());
        assertFalse(result.isExit());
    }

    @Test
    public void execute_helpFind_returnsFindUsageInResultFeedback() throws Exception {
        CommandResult result = executeHelpInput("help find");

        assertEquals(FindCommand.MESSAGE_USAGE, result.getFeedbackToUser());
        assertFalse(result.isShowHelp());
        assertFalse(result.isExit());
    }

    @Test
    public void execute_helpUnsupportedTarget_returnsGuidanceInResultFeedback() throws Exception {
        CommandResult result = executeHelpInput("help invalid");

        assertEquals("No command-specific help is available for 'invalid'.\n"
                + "Enter `help` to open the full help window, or use `help COMMAND` for command-specific help.\n"
                + "Supported commands: add, edit, delete, find.", result.getFeedbackToUser());
        assertFalse(result.isShowHelp());
        assertFalse(result.isExit());
    }

    @Test
    public void parse_helpInvalidInput_throwsParseException() {
        AddressBookParser parser = new AddressBookParser();
        ParseException exception = assertThrows(ParseException.class, () -> parser.parseCommand("help invalid input"));

        assertEquals(String.format(MESSAGE_INVALID_COMMAND_FORMAT, HelpCommand.MESSAGE_USAGE), exception.getMessage());
    }

    private CommandResult executeHelpInput(String input) throws Exception {
        Command command = new AddressBookParser().parseCommand(input);
        return command.execute(model);
    }
}
