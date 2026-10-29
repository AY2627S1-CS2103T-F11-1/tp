package seedu.address.logic.commands;

import java.util.Map;
import java.util.stream.Collectors;

import seedu.address.model.Model;

/**
 * Shows full or command-specific usage instructions.
 */
public class HelpCommand extends Command {

    public static final String COMMAND_WORD = "help";

    public static final String COMMAND_DESCRIPTION = "Opens the full help window or shows usage for a specific "
            + "command.";
    public static final String COMMAND_PARAMETERS = "[COMMAND]";
    public static final String COMMAND_EXAMPLE = COMMAND_WORD + " add";
    public static final String COMMAND_USAGE = COMMAND_WORD + ": " + COMMAND_DESCRIPTION + "\nParameters: "
            + COMMAND_PARAMETERS + "\nExample: " + COMMAND_EXAMPLE;

    public static final String MESSAGE_SHOWING_HELP = "Opened help window.";
    private static final String MESSAGE_NO_COMMAND_HELP = "No command-specific help is available for '%s'.\n"
            + "Enter `help` to open the full help window, or use `help COMMAND` for command-specific help.\n"
            + "Supported commands: %s.";

    private static final Map<String, String> COMMAND_USAGE_BY_NAME = Map.ofEntries(
            Map.entry(AddCommand.COMMAND_WORD, AddCommand.COMMAND_USAGE),
            Map.entry(EditCommand.COMMAND_WORD, EditCommand.COMMAND_USAGE),
            Map.entry(DeleteCommand.COMMAND_WORD, DeleteCommand.COMMAND_USAGE),
            Map.entry(FindCommand.COMMAND_WORD, FindCommand.COMMAND_USAGE),
            Map.entry(FollowUpCommand.COMMAND_WORD, FollowUpCommand.COMMAND_USAGE),
            Map.entry(FollowUpsCommand.COMMAND_WORD, FollowUpsCommand.COMMAND_USAGE),
            Map.entry(SortCommand.COMMAND_WORD, SortCommand.COMMAND_USAGE),
            Map.entry(SelectCommand.COMMAND_WORD, SelectCommand.COMMAND_USAGE),
            Map.entry(ListCommand.COMMAND_WORD, ListCommand.COMMAND_USAGE),
            Map.entry(ClearCommand.COMMAND_WORD, ClearCommand.COMMAND_USAGE),
            Map.entry(UndoCommand.COMMAND_WORD, UndoCommand.COMMAND_USAGE),
            Map.entry(RedoCommand.COMMAND_WORD, RedoCommand.COMMAND_USAGE),
            Map.entry(HelpCommand.COMMAND_WORD, HelpCommand.COMMAND_USAGE),
            Map.entry(ExitCommand.COMMAND_WORD, ExitCommand.COMMAND_USAGE));

    private final String commandName;

    /** Creates a command that opens the full help window. */
    public HelpCommand() {
        this(null);
    }

    /** Creates a command that shows help for the given command in the result display. */
    public HelpCommand(String commandName) {
        this.commandName = commandName;
    }

    @Override
    public CommandResult execute(Model model) {
        if (commandName == null) {
            return new CommandResult(MESSAGE_SHOWING_HELP, true, false);
        }

        String commandUsage = COMMAND_USAGE_BY_NAME.get(commandName);
        if (commandUsage != null) {
            return new CommandResult(commandUsage);
        }

        String supportedCommands = COMMAND_USAGE_BY_NAME.keySet().stream()
                .sorted()
                .collect(Collectors.joining(", "));
        return new CommandResult(String.format(MESSAGE_NO_COMMAND_HELP, commandName, supportedCommands));
    }
}
