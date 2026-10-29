package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ClearCommand;

public class ClearCommandParserTest {

    private final ClearCommandParser parser = new ClearCommandParser();

    @Test
    public void parse_nullArgs_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }

    @Test
    public void parse_emptyArgs_returnsClearCommandWithoutExtraArgs() {
        assertParseSuccess(parser, "", new ClearCommand(false));
    }

    @Test
    public void parse_whitespaceArgs_returnsClearCommandWithoutExtraArgs() {
        assertParseSuccess(parser, "   \t \r \n ", new ClearCommand(false));
    }

    @Test
    public void parse_extraArgs_returnsClearCommandWithExtraArgs() {
        assertParseSuccess(parser, " 1", new ClearCommand(true));
        assertParseSuccess(parser, " all", new ClearCommand(true));
        assertParseSuccess(parser, " followups", new ClearCommand(true));
        assertParseSuccess(parser, " 123 abc", new ClearCommand(true));
    }
}
