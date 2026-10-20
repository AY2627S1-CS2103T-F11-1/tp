package seedu.address.logic.parser;

import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.FindCommand;
import seedu.address.model.person.NameContainsKeywordsPredicate;

public class FindCommandParserTest {

    private FindCommandParser parser = new FindCommandParser();

    @Test
    public void parse_legacyValidArgs_returnsFindCommand() {
        // no leading and trailing whitespaces
        FindCommand expectedFindCommand =
                new FindCommand(new NameContainsKeywordsPredicate(List.of("Alice", "Bob")));
        assertParseSuccess(parser, "Alice Bob", expectedFindCommand);

        // multiple whitespaces between keywords
        assertParseSuccess(parser, " \n Alice \n \t Bob  \t", expectedFindCommand);
    }

    @Test
    public void parse_singlePrefixedName_returnsFindCommand() {
        FindCommand expectedFindCommand =
                new FindCommand(new NameContainsKeywordsPredicate(List.of("Alice")));

        assertParseSuccess(parser, " n/Alice", expectedFindCommand);
    }

    @Test
    public void parse_multiWordPrefixedName_returnsFindCommand() {
        FindCommand expectedFindCommand =
                new FindCommand(new NameContainsKeywordsPredicate(List.of("John Doe")));

        assertParseSuccess(parser, " n/John Doe", expectedFindCommand);
    }

    @Test
    public void parse_multiplePrefixedName_returnsFindCommand() {
        FindCommand expectedFindCommand =
                new FindCommand(new NameContainsKeywordsPredicate(List.of("Alice", "Bob")));

        assertParseSuccess(parser, " n/Alice n/Bob", expectedFindCommand);
    }

    @Test
    public void parse_repeatedMultiWordPrefixedName_returnsFindCommand() {
        FindCommand expectedFindCommand =
                new FindCommand(new NameContainsKeywordsPredicate(List.of("Alice Tan", "Bob Lee")));

        assertParseSuccess(parser, " n/Alice Tan n/Bob Lee", expectedFindCommand);
    }

    @Test
    public void parse_prefixedNameWithWhitespace_returnsFindCommand() {
        FindCommand expectedFindCommand =
                new FindCommand(new NameContainsKeywordsPredicate(List.of("Alice", "Bob")));

        assertParseSuccess(parser, " \n n/Alice \n \t n/Bob  \t", expectedFindCommand);
    }

    @Test
    public void parse_blankArgs_throwsParseException() {
        assertParseFailure(parser, "", FindCommand.MESSAGE_MISSING_CRITERIA);
    }

    @Test
    public void parse_whitespaceOnlyArgs_throwsParseException() {
        assertParseFailure(parser, "     ", FindCommand.MESSAGE_MISSING_CRITERIA);
    }

    @Test
    public void parse_emptyNameValue_throwsParseException() {
        assertParseFailure(parser, " n/", FindCommand.MESSAGE_EMPTY_NAME_VALUE);

        assertParseFailure(parser, " n/   n/", FindCommand.MESSAGE_EMPTY_NAME_VALUE);

        assertParseFailure(parser, " n/Alice   n/", FindCommand.MESSAGE_EMPTY_NAME_VALUE);

    }

    @Test
    public void parse_mixedSearchMode_throwsParseException() {
        assertParseFailure(parser, "Alice n/Tan", FindCommand.MESSAGE_MIXED_SEARCH_MODE);

        assertParseFailure(parser, "Alice  Bob n/Tan", FindCommand.MESSAGE_MIXED_SEARCH_MODE);
    }

    @Test
    public void parse_unrecognisedPrefix_throwsParseException() {
        assertParseFailure(parser, " p/91234567",
                Messages.getErrorMessageForUnrecognizedPrefixes(PREFIX_PHONE));
    }
}
