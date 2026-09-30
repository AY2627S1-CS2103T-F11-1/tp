package seedu.address.logic.commands;

import seedu.address.model.Model;

/**
 * Changes the remark of an existing person in the address book.
 */
public class RemarkCommand extends Command {

    public static final String COMMAND_WORD = "remark";

    public static final String PLACEHOLDER_SUCCESS_STRING = "Hello from remark";

    @Override
    public CommandResult execute(Model model) {
        return new CommandResult(PLACEHOLDER_SUCCESS_STRING);
    }
}
