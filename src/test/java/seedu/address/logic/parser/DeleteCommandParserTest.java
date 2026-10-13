package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.logic.parser.ParserUtil.MESSAGE_INVALID_INDEX;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.DeleteCommand;

/**
 * Tests the parsing of arguments for {@code DeleteCommand}.
 */
public class DeleteCommandParserTest {

    private DeleteCommandParser parser = new DeleteCommandParser();

    @Test
    public void parse_validArgs_returnsDeleteCommand() {
        assertParseSuccess(parser, "1", new DeleteCommand(INDEX_FIRST_PERSON));
    }

    @Test
    public void parse_invalidArgs_throwsParseException() {
        assertParseFailure(parser, "a", MESSAGE_INVALID_INDEX);
    }

    @Test
    public void parse_blankArgs_throwsParseException() {
        assertParseFailure(parser, "", DeleteCommand.MESSAGE_INDEX_REQUIRED);
    }

    @Test
    public void parse_whitespaceOnlyArgs_throwsParseException() {
        assertParseFailure(parser, "   ", DeleteCommand.MESSAGE_INDEX_REQUIRED);
    }

    @Test
    public void parse_extraArgs_throwsParseException() {
        assertParseFailure(parser, "1 2", DeleteCommand.MESSAGE_EXTRA_ARGUMENTS);
    }
}
