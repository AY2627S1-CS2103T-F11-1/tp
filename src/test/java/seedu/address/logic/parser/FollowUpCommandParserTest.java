package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.commands.CommandTestUtil.VALID_FOLLOW_UP_DATE;
import static seedu.address.logic.commands.CommandTestUtil.VALID_FOLLOW_UP_DESCRIPTION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_FOLLOW_UP_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_FOLLOW_UP_DESCRIPTION;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.ClearFollowUpCommand;
import seedu.address.logic.commands.FollowUpCommand;
import seedu.address.logic.commands.SetFollowUpCommand;
import seedu.address.model.person.FollowUp;

public class FollowUpCommandParserTest {

    private static final String DATE_ARGUMENT =
            " " + PREFIX_FOLLOW_UP_DATE + VALID_FOLLOW_UP_DATE;
    private static final String DESCRIPTION_ARGUMENT =
            " " + PREFIX_FOLLOW_UP_DESCRIPTION + VALID_FOLLOW_UP_DESCRIPTION;
    private static final String INVALID_FORMAT_MESSAGE =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, FollowUpCommand.COMMAND_USAGE);

    private final FollowUpCommandParser parser = new FollowUpCommandParser();

    @Test
    public void parse_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }

    @Test
    public void parse_setFollowUp_success() {
        FollowUp expected = new FollowUp(
                LocalDate.parse(VALID_FOLLOW_UP_DATE), VALID_FOLLOW_UP_DESCRIPTION);

        assertParseSuccess(parser, "1" + DATE_ARGUMENT + DESCRIPTION_ARGUMENT,
                new SetFollowUpCommand(INDEX_FIRST_PERSON, expected));
    }

    @Test
    public void parse_reversedPrefixOrder_success() {
        FollowUp expected = new FollowUp(
                LocalDate.parse(VALID_FOLLOW_UP_DATE), VALID_FOLLOW_UP_DESCRIPTION);

        assertParseSuccess(parser, "1" + DESCRIPTION_ARGUMENT + DATE_ARGUMENT,
                new SetFollowUpCommand(INDEX_FIRST_PERSON, expected));
    }

    @Test
    public void parse_surroundingWhitespace_success() {
        FollowUp expected = new FollowUp(
                LocalDate.parse(VALID_FOLLOW_UP_DATE), VALID_FOLLOW_UP_DESCRIPTION);

        assertParseSuccess(parser,
                " \t1 \t" + DATE_ARGUMENT + DESCRIPTION_ARGUMENT + " \t",
                new SetFollowUpCommand(INDEX_FIRST_PERSON, expected));
    }

    @Test
    public void parse_supportedDates_success() {
        String[] dates = {"0000-01-01", "2000-02-29", VALID_FOLLOW_UP_DATE, "9999-12-31"};

        for (String date : dates) {
            FollowUp expected =
                    new FollowUp(LocalDate.parse(date), VALID_FOLLOW_UP_DESCRIPTION);

            assertParseSuccess(parser,
                    "1 " + PREFIX_FOLLOW_UP_DATE + date + DESCRIPTION_ARGUMENT,
                    new SetFollowUpCommand(INDEX_FIRST_PERSON, expected));
        }
    }

    @Test
    public void parse_clear_success() {
        assertParseSuccess(parser, "1 clear",
                new ClearFollowUpCommand(INDEX_FIRST_PERSON));
        assertParseSuccess(parser, " \t1 \tclear \t",
                new ClearFollowUpCommand(INDEX_FIRST_PERSON));
    }

    @Test
    public void parse_missingIndex_failure() {
        String[] inputs = {"", " ", DATE_ARGUMENT + DESCRIPTION_ARGUMENT};

        for (String input : inputs) {
            assertParseFailure(parser, input, INVALID_FORMAT_MESSAGE);
        }
    }

    @Test
    public void parse_invalidIndex_failure() {
        String[] indexes = {"0", "-1", "+1", "abc", "1.5", "2147483648"};

        for (String index : indexes) {
            assertParseFailure(parser, index + DATE_ARGUMENT + DESCRIPTION_ARGUMENT,
                    ParserUtil.MESSAGE_INVALID_INDEX);
            assertParseFailure(parser, index + " clear",
                    ParserUtil.MESSAGE_INVALID_INDEX);
        }
    }

    @Test
    public void parse_missingPrefixes_failure() {
        assertParseFailure(parser, "1",
                Messages.getErrorMessageForMissingPrefixes(
                        PREFIX_FOLLOW_UP_DATE, PREFIX_FOLLOW_UP_DESCRIPTION));
        assertParseFailure(parser, "1" + DATE_ARGUMENT,
                Messages.getErrorMessageForMissingPrefixes(PREFIX_FOLLOW_UP_DESCRIPTION));
        assertParseFailure(parser, "1" + DESCRIPTION_ARGUMENT,
                Messages.getErrorMessageForMissingPrefixes(PREFIX_FOLLOW_UP_DATE));
    }

    @Test
    public void parse_blankDate_failure() {
        assertParseFailure(parser,
                "1 " + PREFIX_FOLLOW_UP_DATE + DESCRIPTION_ARGUMENT,
                ParserUtil.MESSAGE_INVALID_FOLLOW_UP_DATE);
    }

    @Test
    public void parse_invalidDate_failure() {
        String[] dates = {
            "2026-1-01", "2026-01-1", "26-01-01",
            "10000-01-01", "-0001-01-01", "2026/01/01",
            "2026-01-01T00:00:00", "2026-00-01",
            "2026-13-01", "2026-01-00", "2026-04-31",
            "2026-02-29", "1900-02-29"
        };

        for (String date : dates) {
            assertParseFailure(parser,
                    "1 " + PREFIX_FOLLOW_UP_DATE + date + DESCRIPTION_ARGUMENT,
                    ParserUtil.MESSAGE_INVALID_FOLLOW_UP_DATE);
        }
    }

    @Test
    public void parse_blankDescription_failure() {
        assertParseFailure(parser,
                "1" + DATE_ARGUMENT + " " + PREFIX_FOLLOW_UP_DESCRIPTION,
                FollowUp.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser,
                "1" + DATE_ARGUMENT + " " + PREFIX_FOLLOW_UP_DESCRIPTION + "   ",
                FollowUp.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_invalidDescription_failure() {
        String[] descriptions = {
            "a".repeat(FollowUp.MAX_DESCRIPTION_LENGTH + 1),
            "Before\nAfter",
            "Before\tAfter",
            "Before\u2028After",
            "Before\u2029After"
        };

        for (String description : descriptions) {
            assertParseFailure(parser,
                    "1" + DATE_ARGUMENT + " " + PREFIX_FOLLOW_UP_DESCRIPTION + description,
                    FollowUp.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_maximumLengthDescription_success() {
        String description = "a".repeat(FollowUp.MAX_DESCRIPTION_LENGTH);
        FollowUp expected =
                new FollowUp(LocalDate.parse(VALID_FOLLOW_UP_DATE), description);

        assertParseSuccess(parser,
                "1" + DATE_ARGUMENT + " " + PREFIX_FOLLOW_UP_DESCRIPTION + description,
                new SetFollowUpCommand(INDEX_FIRST_PERSON, expected));
    }

    @Test
    public void parse_clearAsDescription_success() {
        FollowUp expected =
                new FollowUp(LocalDate.parse(VALID_FOLLOW_UP_DATE), "clear");

        assertParseSuccess(parser,
                "1" + DATE_ARGUMENT + " " + PREFIX_FOLLOW_UP_DESCRIPTION + "clear",
                new SetFollowUpCommand(INDEX_FIRST_PERSON, expected));
    }

    @Test
    public void parse_duplicateDatePrefix_failure() {
        assertParseFailure(parser,
                "1" + DATE_ARGUMENT + DATE_ARGUMENT + DESCRIPTION_ARGUMENT,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_FOLLOW_UP_DATE));
    }

    @Test
    public void parse_duplicateDescriptionPrefix_failure() {
        assertParseFailure(parser,
                "1" + DATE_ARGUMENT + DESCRIPTION_ARGUMENT + DESCRIPTION_ARGUMENT,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_FOLLOW_UP_DESCRIPTION));
    }

    @Test
    public void parse_unknownPrefix_failure() {
        Prefix unknownPrefix = new Prefix("date/");

        assertParseFailure(parser,
                "1" + DATE_ARGUMENT + DESCRIPTION_ARGUMENT + " date/example",
                Messages.getErrorMessageForUnrecognizedPrefixes(unknownPrefix));
    }

    @Test
    public void parse_clearCombinedWithPrefixes_failure() {
        String[] inputs = {
            "1 clear" + DATE_ARGUMENT,
            "1 clear" + DESCRIPTION_ARGUMENT,
            "1 clear" + DATE_ARGUMENT + DESCRIPTION_ARGUMENT
        };

        for (String input : inputs) {
            assertParseFailure(parser, input, INVALID_FORMAT_MESSAGE);
        }
    }

    @Test
    public void parse_extraPreambleArguments_failure() {
        String[] inputs = {
            "1 extra" + DATE_ARGUMENT + DESCRIPTION_ARGUMENT,
            "1 2" + DATE_ARGUMENT + DESCRIPTION_ARGUMENT,
            "1 clear extra",
            "clear 1",
            "1 CLEAR"
        };

        for (String input : inputs) {
            assertParseFailure(parser, input, INVALID_FORMAT_MESSAGE);
        }
    }
}
