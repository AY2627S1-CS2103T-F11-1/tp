package seedu.address.logic.commands;

import seedu.address.model.Model;

/**
 * Shows full or command-specific usage instructions.
 */
public class HelpCommand extends Command {

    public static final String COMMAND_WORD = "help";

    public static final String MESSAGE_USAGE = COMMAND_WORD + " [COMMAND]: Opens the full help window or shows "
            + "usage for a specific command.\nExample: " + COMMAND_WORD + " add";

    public static final String SHOWING_HELP_MESSAGE = "Opened help window.";

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
        if (AddCommand.COMMAND_WORD.equals(commandName)) {
            return new CommandResult(AddCommand.MESSAGE_USAGE);
        }
        return new CommandResult(SHOWING_HELP_MESSAGE, true, false);
    }
}
