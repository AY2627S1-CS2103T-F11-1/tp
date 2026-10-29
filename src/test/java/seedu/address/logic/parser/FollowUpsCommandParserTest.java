package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.commands.CommandTestUtil.VALID_FOLLOW_UP_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_FOLLOW_UP_DATE;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.FollowUpsCommand;

public class FollowUpsCommandParserTest {

    private static final String DATE_ARGUMENT =
            " " + PREFIX_FOLLOW_UP_DATE + VALID_FOLLOW_UP_DATE;
    private static final String INVALID_FORMAT_MESSAGE =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, FollowUpsCommand.COMMAND_USAGE);

    private final FollowUpsCommandParser parser = new FollowUpsCommandParser();

    @Test
    public void parse_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }

    @Test
    public void parse_emptyArguments_returnsAllQuery() {
        assertParseSuccess(parser, "", FollowUpsCommand.all());
        assertParseSuccess(parser, " \t\n ", FollowUpsCommand.all());
    }

    @Test
    public void parse_overdue_returnsOverdueQuery() {
        assertParseSuccess(parser, "overdue", FollowUpsCommand.overdue());
        assertParseSuccess(parser, " \toverdue \n", FollowUpsCommand.overdue());
    }

    @Test
    public void parse_dateCutoff_returnsDueByQuery() {
        FollowUpsCommand expected =
                FollowUpsCommand.dueBy(LocalDate.parse(VALID_FOLLOW_UP_DATE));

        assertParseSuccess(parser, DATE_ARGUMENT, expected);
        assertParseSuccess(parser,
                " \t " + PREFIX_FOLLOW_UP_DATE + " " + VALID_FOLLOW_UP_DATE + " \t",
                expected);
    }

    @Test
    public void parse_supportedDates_success() {
        String[] dates = {
            "0000-01-01", "2000-02-29", "2001-01-01", "9999-12-31"
        };

        for (String date : dates) {
            assertParseSuccess(parser, " " + PREFIX_FOLLOW_UP_DATE + date,
                    FollowUpsCommand.dueBy(LocalDate.parse(date)));
        }
    }

    @Test
    public void parse_invalidDate_failure() {
        String[] dates = {
            "", " ", "not-a-date", "2026-1-01",
            "+02026-11-01", "10000-01-01",
            "2026-02-30", "2026-11-01 extra"
        };

        for (String date : dates) {
            assertParseFailure(parser, " " + PREFIX_FOLLOW_UP_DATE + date,
                    ParserUtil.MESSAGE_INVALID_FOLLOW_UP_DATE);
        }
    }

    @Test
    public void parse_duplicateDatePrefix_failure() {
        assertParseFailure(parser, DATE_ARGUMENT + DATE_ARGUMENT,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_FOLLOW_UP_DATE));
    }

    @Test
    public void parse_unknownPrefix_failure() {
        assertParseFailure(parser, " abt/example",
                Messages.getErrorMessageForUnrecognizedPrefixes(new Prefix("abt/")));
        assertParseFailure(parser, DATE_ARGUMENT + " date/example",
                Messages.getErrorMessageForUnrecognizedPrefixes(new Prefix("date/")));
    }

    @Test
    public void parse_unsupportedArguments_failure() {
        String[] inputs = {
            "late", "OVERDUE", "1", "unknown",
            "overdue extra", "overdue overdue",
            "overdue" + DATE_ARGUMENT,
            "1" + DATE_ARGUMENT
        };

        for (String input : inputs) {
            assertParseFailure(parser, input, INVALID_FORMAT_MESSAGE);
        }
    }

    @Test
    public void parse_keywordAfterDate_failure() {
        assertParseFailure(parser, DATE_ARGUMENT + " overdue",
                ParserUtil.MESSAGE_INVALID_FOLLOW_UP_DATE);
    }
}
