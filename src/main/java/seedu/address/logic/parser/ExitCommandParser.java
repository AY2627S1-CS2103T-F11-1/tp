package seedu.address.logic.parser;

import seedu.address.logic.commands.ExitCommand;

/**
 * Parses input arguments (if any) and creates a new ExitCommand object
 */
public class ExitCommandParser implements Parser<ExitCommand> {

    /**
     * Parses arguments into an exit command, recording whether extra text is present.
     *
     * @param args the arguments following the command word
     * @return the exit command
     */
    public ExitCommand parse(String args) {
        boolean hasExtraArgs = !args.isBlank();

        return new ExitCommand(hasExtraArgs);
    }

}
