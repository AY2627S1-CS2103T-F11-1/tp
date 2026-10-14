package seedu.address.logic.commands;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.Model;

/**
 * Terminates the program.
 */
public class ExitCommand extends Command {

    public static final String COMMAND_WORD = "exit";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Exits the program.";

    public static final String MESSAGE_EXIT_ACKNOWLEDGEMENT = "Exiting Address Book as requested ...";
    public static final String MESSAGE_EXIT_DISCLAIMER =
            "exit takes no parameters. Extra text is ignored. Exiting Address Book ...";

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

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof ExitCommand otherExitCommand)) {
            return false;
        }

        return hasExtraArgs == otherExitCommand.hasExtraArgs;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("hasExtraArgs", hasExtraArgs)
                .toString();
    }

}
