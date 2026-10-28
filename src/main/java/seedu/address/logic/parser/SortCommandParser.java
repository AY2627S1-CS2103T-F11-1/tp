package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ROLE;

import java.util.ArrayList;
import java.util.List;

import seedu.address.logic.commands.SortCommand;
import seedu.address.logic.commands.SortCommand.SortField;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses sort fields in priority order and creates a SortCommand.
 */
public class SortCommandParser implements Parser<SortCommand> {
    /**
     * Parses whitespace-separated sort fields, defaulting to name when no fields are supplied.
     *
     * @param args arguments following the command word
     * @return the command with fields in the supplied priority order
     * @throws ParseException if an argument is unsupported or a field is repeated
     */
    @Override
    public SortCommand parse(String args) throws ParseException {
        requireNonNull(args);

        String trimmedArgs = args.trim();

        if (trimmedArgs.isEmpty()) {
            return new SortCommand(List.of(SortField.NAME));
        }

        List<String> tokens = List.of(trimmedArgs.split("\\s+"));

        if (tokens.size() == 1 && tokens.get(0).equals("clear")) {
            return SortCommand.clearSort();
        }

        if (tokens.contains("clear")) {
            throw new ParseException(SortCommand.MESSAGE_CLEAR_WITH_FIELDS);
        }

        List<SortField> sortFields = new ArrayList<>();

        for (String token : tokens) {
            SortField field = parseField(token);

            if (sortFields.contains(field)) {
                throw new ParseException(
                        String.format(SortCommand.MESSAGE_DUPLICATE_FIELD, token));
            }

            sortFields.add(field);
        }

        return new SortCommand(sortFields);
    }

    /**
     * Converts a standalone field prefix into a sort field.
     *
     * @throws ParseException if the token contains a value or is unsupported
     */
    private SortField parseField(String token) throws ParseException {
        if (token.equals(PREFIX_NAME.getPrefix())) {
            return SortField.NAME;
        }

        if (token.equals(PREFIX_ROLE.getPrefix())) {
            return SortField.ROLE;
        }

        // Exact prefixes were handled above; any remaining suffix is an unexpected value
        for (Prefix prefix : List.of(PREFIX_NAME, PREFIX_ROLE)) {
            if (token.startsWith(prefix.getPrefix())) {
                throw new ParseException(String.format(SortCommand.MESSAGE_UNEXPECTED_VALUE, prefix));
            }
        }

        throw new ParseException(String.format(SortCommand.MESSAGE_UNEXPECTED_TOKEN, token));
    }
}
