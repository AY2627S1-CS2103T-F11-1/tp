package seedu.address.logic.commands;

import java.util.Map;

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
    public static final String COMMAND_USAGE = COMMAND_WORD + " " + COMMAND_PARAMETERS + ": "
            + COMMAND_DESCRIPTION + "\nExample: " + COMMAND_EXAMPLE;

    public static final String SHOWING_HELP_MESSAGE = "Opened help window.";
    private static final String MESSAGE_NO_COMMAND_HELP = "No command-specific help is available for '%s'.\n"
            + "Enter `help` to open the full help window, or use `help COMMAND` for command-specific help.\n"
            + "Supported commands: add, edit, delete, find.";

    private static final Map<String, String> COMMAND_USAGE_BY_NAME = Map.of(
            AddCommand.COMMAND_WORD, AddCommand.COMMAND_USAGE,
            EditCommand.COMMAND_WORD, EditCommand.COMMAND_USAGE,
            DeleteCommand.COMMAND_WORD, DeleteCommand.COMMAND_USAGE,
            FindCommand.COMMAND_WORD, FindCommand.COMMAND_USAGE);

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
            return new CommandResult(SHOWING_HELP_MESSAGE, true, false);
        }

        String commandUsage = COMMAND_USAGE_BY_NAME.get(commandName);
        if (commandUsage != null) {
            return new CommandResult(commandUsage);
        }

        return new CommandResult(String.format(MESSAGE_NO_COMMAND_HELP, commandName));
    }
}
