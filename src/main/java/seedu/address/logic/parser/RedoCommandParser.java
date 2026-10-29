package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.logic.commands.RedoCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new RedoCommand object.
 */
public class RedoCommandParser implements Parser<RedoCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the RedoCommand
     * and returns a RedoCommand object for execution.
     *
     * @param args arguments following the command word
     * @return the parsed redo command
     * @throws ParseException if the user input does not conform to the expected format
     */
    @Override
    public RedoCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String trimmedArgs = args.trim();
        if (!trimmedArgs.isEmpty()) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, RedoCommand.COMMAND_USAGE));
        }
        return new RedoCommand();
    }
}
