package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.RedoCommand;

public class RedoCommandParserTest {

    private final RedoCommandParser parser = new RedoCommandParser();

    @Test
    public void parse_nullArgs_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }

    @Test
    public void parse_emptyArgs_returnsRedoCommand() {
        assertParseSuccess(parser, "", new RedoCommand());
    }

    @Test
    public void parse_whitespaceArgs_returnsRedoCommand() {
        assertParseSuccess(parser, "   \t \r \n ", new RedoCommand());
    }

    @Test
    public void parse_invalidArgs_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, RedoCommand.MESSAGE_USAGE);

        assertParseFailure(parser, " 123", expectedMessage);
        assertParseFailure(parser, " all", expectedMessage);
        assertParseFailure(parser, " extra arguments", expectedMessage);
    }
}
