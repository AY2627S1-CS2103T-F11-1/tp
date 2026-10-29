package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

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

        assertEquals(HelpCommand.MESSAGE_SHOWING_HELP, result.getFeedbackToUser());
        assertTrue(result.isShowHelp());
        assertFalse(result.isExit());
    }

    @Test
    public void execute_helpAdd_returnsAddUsageInResultFeedback() throws Exception {
        CommandResult result = executeHelpInput("help add");

        assertEquals(AddCommand.COMMAND_USAGE, result.getFeedbackToUser());
        assertFalse(result.isShowHelp());
        assertFalse(result.isExit());
    }

    @Test
    public void execute_helpEdit_returnsEditUsageInResultFeedback() throws Exception {
        CommandResult result = executeHelpInput("help edit");

        assertEquals(EditCommand.COMMAND_USAGE, result.getFeedbackToUser());
        assertFalse(result.isShowHelp());
        assertFalse(result.isExit());
    }

    @Test
    public void execute_helpDelete_returnsDeleteUsageInResultFeedback() throws Exception {
        CommandResult result = executeHelpInput("help delete");

        assertEquals(DeleteCommand.COMMAND_USAGE, result.getFeedbackToUser());
        assertFalse(result.isShowHelp());
        assertFalse(result.isExit());
    }

    @Test
    public void execute_helpFind_returnsFindUsageInResultFeedback() throws Exception {
        CommandResult result = executeHelpInput("help find");

        assertEquals(FindCommand.COMMAND_USAGE, result.getFeedbackToUser());
        assertFalse(result.isShowHelp());
        assertFalse(result.isExit());
    }

    @Test
    public void execute_helpSelect_returnsUsageWithoutSelectingOrChangingData() throws Exception {
        Model expected = new ModelManager();
        CommandResult result = executeHelpInput("help select");
        assertEquals(SelectCommand.COMMAND_USAGE, result.getFeedbackToUser());
        assertFalse(result.isShowHelp());
        assertFalse(result.isExit());
        assertTrue(result.getSelectedPerson().isEmpty());
        assertEquals(expected, model);
    }

    @Test
    public void execute_helpEveryCommand_returnsUsageWithoutExecutingTarget() throws Exception {
        Map<String, String> usages = Map.ofEntries(
                Map.entry("add", AddCommand.COMMAND_USAGE),
                Map.entry("edit", EditCommand.COMMAND_USAGE),
                Map.entry("delete", DeleteCommand.COMMAND_USAGE),
                Map.entry("find", FindCommand.COMMAND_USAGE),
                Map.entry("followup", FollowUpCommand.COMMAND_USAGE),
                Map.entry("followups", FollowUpsCommand.COMMAND_USAGE),
                Map.entry("sort", SortCommand.COMMAND_USAGE),
                Map.entry("select", SelectCommand.COMMAND_USAGE),
                Map.entry("list", ListCommand.COMMAND_USAGE),
                Map.entry("clear", ClearCommand.COMMAND_USAGE),
                Map.entry("undo", UndoCommand.COMMAND_USAGE),
                Map.entry("redo", RedoCommand.COMMAND_USAGE),
                Map.entry("help", HelpCommand.COMMAND_USAGE),
                Map.entry("exit", ExitCommand.COMMAND_USAGE));

        for (Map.Entry<String, String> usage : usages.entrySet()) {
            CommandResult result = executeHelpInput("help " + usage.getKey());
            assertEquals(usage.getValue(), result.getFeedbackToUser());
            assertFalse(result.isShowHelp());
            assertFalse(result.isExit());
            assertTrue(result.getSelectedPerson().isEmpty());
        }
    }

    @Test
    public void execute_helpCommandsWithoutParameters_returnsDescriptionOnly() throws Exception {
        Map<String, String> descriptions = Map.of(
                "list", ListCommand.COMMAND_DESCRIPTION,
                "clear", ClearCommand.COMMAND_DESCRIPTION,
                "undo", UndoCommand.COMMAND_DESCRIPTION,
                "redo", RedoCommand.COMMAND_DESCRIPTION,
                "exit", ExitCommand.COMMAND_DESCRIPTION);

        for (Map.Entry<String, String> description : descriptions.entrySet()) {
            String feedback = executeHelpInput("help " + description.getKey()).getFeedbackToUser();
            assertEquals(description.getKey() + ": " + description.getValue(), feedback);
            assertFalse(feedback.contains("Parameters:"));
            assertFalse(feedback.contains("Example:"));
        }
    }

    @Test
    public void execute_helpUnsupportedTarget_returnsGuidanceInResultFeedback() throws Exception {
        CommandResult result = executeHelpInput("help invalid");

        assertEquals("No command-specific help is available for 'invalid'.\n"
                + "Enter `help` to open the full help window, or use `help COMMAND` for command-specific help.\n"
                + "Supported commands: add, clear, delete, edit, exit, find, followup, followups, help, list, redo, "
                + "select, sort, undo.", result.getFeedbackToUser());
        assertFalse(result.isShowHelp());
        assertFalse(result.isExit());
    }

    @Test
    public void parse_helpInvalidInput_throwsParseException() {
        AddressBookParser parser = new AddressBookParser();
        ParseException exception = assertThrows(ParseException.class, () -> parser.parseCommand("help invalid input"));

        assertEquals("Specify only one command after help.", exception.getMessage());
    }

    private CommandResult executeHelpInput(String input) throws Exception {
        Command command = new AddressBookParser().parseCommand(input);
        return command.execute(model);
    }
}
