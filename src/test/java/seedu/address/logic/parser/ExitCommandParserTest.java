package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ExitCommand;

public class ExitCommandParserTest {

    private ExitCommandParser parser = new ExitCommandParser();

    @Test
    public void parse_emptyArgs_returnsNormalExitCommand() {
        assertParseSuccess(parser, "", new ExitCommand(false));
    }

    @Test
    public void parse_whitespaceArgs_returnsNormalExitCommand() {
        assertParseSuccess(parser, " \t\n  ", new ExitCommand(false));
    }

    @Test
    public void parse_additionalArgs_returnsExitCommandWithDisclaimer() {
        ExitCommand expectedCommand = new ExitCommand(true);

        assertParseSuccess(parser, " -1", expectedCommand);
        assertParseSuccess(parser, " so", expectedCommand);
        assertParseSuccess(parser, " \t-f  ", expectedCommand);
    }
}
