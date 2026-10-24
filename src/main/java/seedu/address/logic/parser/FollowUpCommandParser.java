package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_FOLLOW_UP_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_FOLLOW_UP_DESCRIPTION;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.ClearFollowUpCommand;
import seedu.address.logic.commands.FollowUpCommand;
import seedu.address.logic.commands.SetFollowUpCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.FollowUp;

/**
 * Parses input arguments and creates a new FollowUpCommand object
 */
public class FollowUpCommandParser implements Parser<FollowUpCommand> {

    public static final String MESSAGE_INVALID_DATE =
            "Follow-up due dates must be valid calendar dates in YYYY-MM-DD format.";

    private static final String CLEAR_KEYWORD = "clear";

    // Requires four digits for year and two digits each for month and day.
    // Calendar validity is checked separately by LocalDate.parse().
    private static final String DATE_FORMAT_REGEX = "[0-9]{4}-[0-9]{2}-[0-9]{2}";

    /**
     * Parses the given {@code String} of arguments in the context of the FollowUpCommand
     * and returns a FollowUpCommand object for execution.
     * @throws ParseException if the arguments or follow-up values are invalid
     */
    @Override
    public FollowUpCommand parse(String args) throws ParseException {
        requireNonNull(args);

        ArgumentTokenizer.verifyNoUnrecognizedPrefixes(args,
                PREFIX_FOLLOW_UP_DATE, PREFIX_FOLLOW_UP_DESCRIPTION);
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(args,
                PREFIX_FOLLOW_UP_DATE, PREFIX_FOLLOW_UP_DESCRIPTION);

        String[] preamble = arguments.getPreamble().split("\\s+");
        boolean hasDate = arguments.getValue(PREFIX_FOLLOW_UP_DATE).isPresent();
        boolean hasDescription = arguments.getValue(PREFIX_FOLLOW_UP_DESCRIPTION).isPresent();

        if (preamble.length == 2 && CLEAR_KEYWORD.equals(preamble[1]) && !hasDate && !hasDescription) {
            return new ClearFollowUpCommand(ParserUtil.parseIndex(preamble[0]));
        }

        if (arguments.getPreamble().isBlank() || preamble.length != 1) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, FollowUpCommand.MESSAGE_USAGE));
        }

        Index index = ParserUtil.parseIndex(preamble[0]);

        arguments.verifyNoMissingPrefixesFor(PREFIX_FOLLOW_UP_DATE, PREFIX_FOLLOW_UP_DESCRIPTION);
        arguments.verifyNoDuplicatePrefixesFor(PREFIX_FOLLOW_UP_DATE, PREFIX_FOLLOW_UP_DESCRIPTION);

        LocalDate dueDate = parseDueDate(arguments.getValue(PREFIX_FOLLOW_UP_DATE).orElseThrow());
        String description = arguments.getValue(PREFIX_FOLLOW_UP_DESCRIPTION).orElseThrow();

        if (!FollowUp.isValidDescription(description)) {
            throw new ParseException(FollowUp.MESSAGE_CONSTRAINTS);
        }

        return new SetFollowUpCommand(index, new FollowUp(dueDate, description));
    }

    private static LocalDate parseDueDate(String value) throws ParseException {
        if (!value.matches(DATE_FORMAT_REGEX)) {
            throw new ParseException(MESSAGE_INVALID_DATE);
        }

        try {
            return LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            throw new ParseException(MESSAGE_INVALID_DATE);
        }
    }

}
