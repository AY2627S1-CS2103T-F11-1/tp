package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.logic.parser.ParserUtil.MESSAGE_INVALID_INDEX;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.SelectCommand;

public class SelectCommandParserTest {
    private final SelectCommandParser parser = new SelectCommandParser();

    @Test
    public void parse_validIndexAndWhitespace_returnsSelectCommand() {
        for (String args : List.of("1", " 1 ", "\t1\t", "01")) {
            assertParseSuccess(parser, args, new SelectCommand(INDEX_FIRST_PERSON));
        }
    }

    @Test
    public void parse_largestValidInteger_returnsSelectCommand() {
        assertParseSuccess(parser, "2147483647", new SelectCommand(Index.fromOneBased(Integer.MAX_VALUE)));
    }

    @Test
    public void parse_blankIndex_throwsParseException() {
        for (String args : List.of("", " ", "\t\n ")) {
            assertParseFailure(parser, args, SelectCommand.MESSAGE_INDEX_REQUIRED);
        }
    }

    @Test
    public void parse_invalidOrOverflowingIndex_throwsParseException() {
        for (String args : List.of("0", "-1", "+1", "a", "1.0", "2147483648", "999999999999999999999")) {
            assertParseFailure(parser, args, MESSAGE_INVALID_INDEX);
        }
    }

    @Test
    public void parse_extraArguments_throwsParseException() {
        for (String args : List.of("1 2", "1 name", "1 n/Alice", "1\t2")) {
            assertParseFailure(parser, args, SelectCommand.MESSAGE_EXTRA_ARGUMENTS);
        }
    }

    @Test
    public void parse_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }
}
