package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.SortCommand;
import seedu.address.logic.commands.SortCommand.SortField;

public class SortCommandParserTest {
    private SortCommandParser parser = new SortCommandParser();

    @Test
    public void parse_validFields_returnsSortCommand() {
        SortCommand expectedSortCommand = new SortCommand(List.of(SortField.NAME));
        assertParseSuccess(parser, " n/", expectedSortCommand);

        expectedSortCommand = new SortCommand(List.of(SortField.ROLE));
        assertParseSuccess(parser, " r/", expectedSortCommand);

        expectedSortCommand = new SortCommand(List.of(SortField.NAME, SortField.ROLE));
        assertParseSuccess(parser, " n/ r/", expectedSortCommand);

        expectedSortCommand = new SortCommand(List.of(SortField.ROLE, SortField.NAME));
        assertParseSuccess(parser, " r/ n/", expectedSortCommand);
    }

    @Test
    public void parse_validFieldsWithWhitespace_returnsSortCommand() {
        SortCommand expectedSortCommand = new SortCommand(List.of(SortField.NAME));
        assertParseSuccess(parser, "   n/    ", expectedSortCommand);

        expectedSortCommand = new SortCommand(List.of(SortField.ROLE, SortField.NAME));
        assertParseSuccess(parser, "   r/ \t n/   ", expectedSortCommand);
    }

    @Test
    public void parse_blankArgs_defaultsToName() {
        SortCommand expectedSortCommand = new SortCommand(List.of(SortField.NAME));
        assertParseSuccess(parser, "", expectedSortCommand);

        assertParseSuccess(parser, " ", expectedSortCommand);

        assertParseSuccess(parser, " \t   ", expectedSortCommand);
    }

    @Test
    public void parse_clear_returnsClearSortCommand() {
        assertParseSuccess(parser, "clear", SortCommand.clearSort());
    }

    @Test
    public void parse_clearWithWhitespace_returnsClearSortCommand() {
        assertParseSuccess(parser, "   clear \t", SortCommand.clearSort());
    }

    @Test
    public void parse_clearWithFields_throwsParseException() {
        assertParseFailure(parser, "clear n/", SortCommand.MESSAGE_CLEAR_WITH_FIELDS);
        assertParseFailure(parser, "r/ clear", SortCommand.MESSAGE_CLEAR_WITH_FIELDS);
    }

    @Test
    public void parse_nullArgs_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }

    @Test
    public void parse_duplicateField_throwsParseException() {
        assertParseFailure(parser, "n/ n/", String.format(SortCommand.MESSAGE_DUPLICATE_FIELD, "n/"));

        assertParseFailure(parser, "r/ r/", String.format(SortCommand.MESSAGE_DUPLICATE_FIELD, "r/"));

        assertParseFailure(parser, "n/ r/ n/", String.format(SortCommand.MESSAGE_DUPLICATE_FIELD, "n/"));
    }

    @Test
    public void parse_validFieldsWithUnexpectedValue_throwsParseException() {
        assertParseFailure(parser, "n/Alice", String.format(SortCommand.MESSAGE_UNEXPECTED_VALUE, "n/"));

        assertParseFailure(parser, "r/Supplier", String.format(SortCommand.MESSAGE_UNEXPECTED_VALUE, "r/"));

        assertParseFailure(parser, "n/r/", String.format(SortCommand.MESSAGE_UNEXPECTED_VALUE, "n/"));
    }

    @Test
    public void parse_unexpectedToken_throwsParseException() {
        assertParseFailure(parser, "p/", String.format(SortCommand.MESSAGE_UNEXPECTED_TOKEN, "p/"));

        assertParseFailure(parser, "hello", String.format(SortCommand.MESSAGE_UNEXPECTED_TOKEN, "hello"));

        assertParseFailure(parser, "hello n/", String.format(SortCommand.MESSAGE_UNEXPECTED_TOKEN, "hello"));

        assertParseFailure(parser, "n/ Alice", String.format(SortCommand.MESSAGE_UNEXPECTED_TOKEN, "Alice"));

        assertParseFailure(parser, "r/ n/ extra", String.format(SortCommand.MESSAGE_UNEXPECTED_TOKEN, "extra"));
    }
}
