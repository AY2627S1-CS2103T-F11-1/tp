package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import seedu.address.logic.commands.SelectCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses exactly one positive displayed-contact index into a select command.
 */
public class SelectCommandParser implements Parser<SelectCommand> {
    /**
     * Parses the arguments for {@code select INDEX}, allowing surrounding whitespace.
     *
     * @throws ParseException if the index is missing, malformed, or accompanied by extra arguments.
     */
    @Override
    public SelectCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String trimmedArgs = args.trim();
        if (trimmedArgs.isEmpty()) {
            throw new ParseException(SelectCommand.MESSAGE_INDEX_REQUIRED);
        }
        if (trimmedArgs.split("\\s+").length != 1) {
            throw new ParseException(SelectCommand.MESSAGE_EXTRA_ARGUMENTS);
        }
        return new SelectCommand(ParserUtil.parseIndex(trimmedArgs));
    }
}
