package seedu.address.logic.parser;

import seedu.address.logic.commands.ListCommand;

/**
 * Parses input arguments (if any) and creates a new ListCommand object.
 */
public class ListCommandParser implements Parser<ListCommand> {
    /**
     * Parses arguments into a list command, recording whether extra arguments are present.
     *
     * @param args arguments following the command word
     * @return the list command
     */
    @Override
    public ListCommand parse(String args) {
        boolean hasExtraArgs = !args.isBlank();

        return new ListCommand(hasExtraArgs);
    }
}
