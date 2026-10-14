package seedu.address.logic.parser;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new DeleteCommand object
 */
public class DeleteCommandParser implements Parser<DeleteCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the DeleteCommand
     * and returns a DeleteCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public DeleteCommand parse(String args) throws ParseException {
        String trimmedArgs = args.trim();

        if (trimmedArgs.isEmpty()) {
            throw new ParseException(DeleteCommand.MESSAGE_INDEX_REQUIRED);
        }

        String[] arguments = trimmedArgs.split("\\s+");

        if (arguments.length > 1) {
            throw new ParseException(DeleteCommand.MESSAGE_EXTRA_ARGUMENTS);
        }

        Index index = ParserUtil.parseIndex(args);
        return new DeleteCommand(index);
    }
}
