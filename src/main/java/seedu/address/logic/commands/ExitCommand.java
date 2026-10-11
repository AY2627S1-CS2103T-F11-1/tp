package seedu.address.logic.commands;

import seedu.address.model.Model;

/**
 * Terminates the program.
 */
public class ExitCommand extends Command {

    public static final String COMMAND_WORD = "exit";

    public static final String MESSAGE_EXIT_ACKNOWLEDGEMENT = "Exiting Address Book as requested ...";
    public static final String MESSAGE_EXIT_DISCLAIMER
            = "exit takes no parameters. Extra text is ignored. Exiting Address Book ...";

    private final boolean hasExtraArgs;

    public ExitCommand(boolean hasExtraArgs) {
        this.hasExtraArgs = hasExtraArgs;
    }

    @Override
    public CommandResult execute(Model model) {
        if (hasExtraArgs) {
            return new CommandResult(MESSAGE_EXIT_DISCLAIMER, false, true);
        } else {
            return new CommandResult(MESSAGE_EXIT_ACKNOWLEDGEMENT, false, true);
        }
    }

}
