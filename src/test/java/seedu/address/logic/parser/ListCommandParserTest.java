package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ListCommand;

public class ListCommandParserTest {

    private final ListCommandParser parser = new ListCommandParser();

    @Test
    public void parse_emptyArgs_returnsListCommandWithoutWarning() {
        assertParseSuccess(parser, "", new ListCommand(false));
    }

    @Test
    public void parse_whitespaceArgs_returnsListCommandWithoutWarning() {
        assertParseSuccess(parser, " \t\n  ", new ListCommand(false));
    }

    @Test
    public void parse_oneAdditionalArgument_returnsListCommandWithWarning() {
        assertParseSuccess(parser, " 3", new ListCommand(true));
    }

    @Test
    public void parse_multipleAdditionalArguments_returnsListCommandWithWarning() {
        assertParseSuccess(parser, " abc xyz", new ListCommand(true));
    }
}
