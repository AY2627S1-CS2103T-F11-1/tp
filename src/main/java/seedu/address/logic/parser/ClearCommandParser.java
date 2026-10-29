package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import seedu.address.logic.commands.ClearCommand;

/**
 * Parses input arguments (if any) and creates a new ClearCommand object.
 */
public class ClearCommandParser implements Parser<ClearCommand> {

    /**
     * Parses arguments into a clear command, recording whether extra arguments are present.
     *
     * @param args arguments following the command word
     * @return the clear command
     */
    @Override
    public ClearCommand parse(String args) {
        requireNonNull(args);
        boolean hasExtraArgs = !args.isBlank();

        return new ClearCommand(hasExtraArgs);
    }
}
