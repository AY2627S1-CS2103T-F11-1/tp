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
    public static final String MESSAGE_USAGE = COMMAND_WORD + " " + COMMAND_PARAMETERS + ": "
            + COMMAND_DESCRIPTION + "\nExample: " + COMMAND_EXAMPLE;

    public static final String SHOWING_HELP_MESSAGE = "Opened help window.";
    private static final String MESSAGE_NO_COMMAND_HELP = "No command-specific help is available for '%s'.";

    private static final Map<String, String> COMMAND_USAGE = Map.of(
            AddCommand.COMMAND_WORD, formatCommandUsage(AddCommand.COMMAND_WORD, AddCommand.COMMAND_DESCRIPTION,
                    AddCommand.COMMAND_PARAMETERS, AddCommand.COMMAND_EXAMPLE),
            EditCommand.COMMAND_WORD, formatCommandUsage(EditCommand.COMMAND_WORD, EditCommand.COMMAND_DESCRIPTION,
                    EditCommand.COMMAND_PARAMETERS, EditCommand.COMMAND_EXAMPLE),
            DeleteCommand.COMMAND_WORD, formatCommandUsage(DeleteCommand.COMMAND_WORD,
                    DeleteCommand.COMMAND_DESCRIPTION, DeleteCommand.COMMAND_PARAMETERS,
                    DeleteCommand.COMMAND_EXAMPLE),
            FindCommand.COMMAND_WORD, formatCommandUsage(FindCommand.COMMAND_WORD, FindCommand.COMMAND_DESCRIPTION,
                    FindCommand.COMMAND_PARAMETERS, FindCommand.COMMAND_EXAMPLE));

    private final String commandName;

    /** Creates a command that opens the full help window. */
    public HelpCommand() {
        this(null);
    }

    /** Creates a command that shows help for the given command in the result display. */
    public HelpCommand(String commandName) {
        this.commandName = commandName;
    }

    private static String formatCommandUsage(String commandWord, String description, String parameters,
            String example) {
        return commandWord + ": " + description + "\nParameters: " + parameters + "\nExample: " + example;
    }

    @Override
    public CommandResult execute(Model model) {
        if (commandName == null) {
            return new CommandResult(SHOWING_HELP_MESSAGE, true, false);
        }

        String commandUsage = COMMAND_USAGE.get(commandName);
        if (commandUsage != null) {
            return new CommandResult(commandUsage);
        }

        return new CommandResult(String.format(MESSAGE_NO_COMMAND_HELP, commandName));
    }
}
