package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_FOLLOW_UP_DATE;

import java.time.LocalDate;

import seedu.address.logic.commands.FollowUpsCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new FollowUpsCommand object
 */
public class FollowUpsCommandParser implements Parser<FollowUpsCommand> {

    public static final String MESSAGE_INVALID_DATE =
            "Follow-up date cutoffs must be valud calendar dates in YYYY-MM-DD format.";

    private static final String OVERDUE_KEYWORD = "overdue";

    /**
     * Parses an empty query, an overdue keyword, or a date cutoff.
     *
     * @throws ParseException if the arguments or date are invalid
     */
    @Override
    public FollowUpsCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String trimmedArgs = args.trim();

        if (trimmedArgs.isEmpty()) {
            return FollowUpsCommand.all();
        }

        if (OVERDUE_KEYWORD.equals(trimmedArgs)) {
            return FollowUpsCommand.overdue();
        }

        ArgumentTokenizer.verifyNoUnrecognizedPrefixes(args, PREFIX_FOLLOW_UP_DATE);
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(args, PREFIX_FOLLOW_UP_DATE);

        if (!arguments.getPreamble().isBlank()) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, FollowUpsCommand.COMMAND_USAGE));
        }

        arguments.verifyNoMissingPrefixesFor(PREFIX_FOLLOW_UP_DATE);
        arguments.verifyNoDuplicatePrefixesFor(PREFIX_FOLLOW_UP_DATE);

        LocalDate cutoffDate = ParserUtil.parseFollowUpDate(
                arguments.getValue(PREFIX_FOLLOW_UP_DATE).orElseThrow());

        return FollowUpsCommand.dueBy(cutoffDate);
    }
}
