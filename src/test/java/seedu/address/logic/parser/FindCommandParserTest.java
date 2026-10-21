package seedu.address.logic.parser;

import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.FindCommand;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.RoleContainsKeywordsPredicate;
import seedu.address.model.person.TagContainsKeywordsPredicate;

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
    public void parse_singlePrefixedValue_returnsFindCommand() {
        FindCommand expectedFindCommand =
                new FindCommand(new NameContainsKeywordsPredicate(List.of("Alice")));

        assertParseSuccess(parser, " n/Alice", expectedFindCommand);

        expectedFindCommand =
                new FindCommand(new RoleContainsKeywordsPredicate(List.of("Supplier")));

        assertParseSuccess(parser, " r/Supplier", expectedFindCommand);

        expectedFindCommand =
                new FindCommand(new TagContainsKeywordsPredicate(List.of("Friend")));

        assertParseSuccess(parser, " t/Friend", expectedFindCommand);
    }

    @Test
    public void parse_multiWordPrefixedValue_returnsFindCommand() {
        FindCommand expectedFindCommand =
                new FindCommand(new NameContainsKeywordsPredicate(List.of("John Doe")));

        assertParseSuccess(parser, " n/John Doe", expectedFindCommand);

        expectedFindCommand =
                new FindCommand(new RoleContainsKeywordsPredicate(List.of("Supplier ABC")));

        assertParseSuccess(parser, " r/Supplier ABC", expectedFindCommand);

        expectedFindCommand =
                new FindCommand(new TagContainsKeywordsPredicate(List.of("Good Friend")));

        assertParseSuccess(parser, " t/Good Friend", expectedFindCommand);
    }

    @Test
    public void parse_multiplePrefixedValues_returnsFindCommand() {
        FindCommand expectedFindCommand =
                new FindCommand(new NameContainsKeywordsPredicate(List.of("Alice", "Bob")));

        assertParseSuccess(parser, " n/Alice n/Bob", expectedFindCommand);

        expectedFindCommand =
                new FindCommand(new RoleContainsKeywordsPredicate(List.of("Supplier", "Distributor")));

        assertParseSuccess(parser, " r/Supplier r/Distributor", expectedFindCommand);

        expectedFindCommand =
                new FindCommand(new TagContainsKeywordsPredicate(List.of("Good", "Outstanding")));

        assertParseSuccess(parser, " t/Good t/Outstanding", expectedFindCommand);
    }

    @Test
    public void parse_multipleMultiWordPrefixedValues_returnsFindCommand() {
        FindCommand expectedFindCommand =
                new FindCommand(new NameContainsKeywordsPredicate(List.of("Alice Tan", "Bob Lee")));

        assertParseSuccess(parser, " n/Alice Tan n/Bob Lee", expectedFindCommand);

        expectedFindCommand =
                new FindCommand(new RoleContainsKeywordsPredicate(List.of("Supplier ABC", "Distributor XYZ")));

        assertParseSuccess(parser, " r/Supplier ABC r/Distributor XYZ", expectedFindCommand);

        expectedFindCommand =
                new FindCommand(new TagContainsKeywordsPredicate(List.of("Good Friend", "Outstanding Colleague")));

        assertParseSuccess(parser, " t/Good Friend t/Outstanding Colleague", expectedFindCommand);
    }

    @Test
    public void parse_prefixedValuesWithWhitespace_returnsFindCommand() {
        FindCommand expectedFindCommand =
                new FindCommand(new NameContainsKeywordsPredicate(List.of("Alice", "Bob")));

        assertParseSuccess(parser, " \n n/   Alice \n \t n/Bob  \t", expectedFindCommand);

        expectedFindCommand =
                new FindCommand(new RoleContainsKeywordsPredicate(List.of("Supplier", "Distributor")));

        assertParseSuccess(parser, " \n r/   Supplier \n \t r/Distributor  \t", expectedFindCommand);

        expectedFindCommand =
                new FindCommand(new TagContainsKeywordsPredicate(List.of("Good", "Outstanding")));

        assertParseSuccess(parser, " \n t/   Good \n \t t/Outstanding  \t", expectedFindCommand);
    }

    @Test
    public void parse_differentPrefixes_returnsFindCommand() {
        FindCommand expectedFindCommand = new FindCommand(
                new NameContainsKeywordsPredicate(List.of("John")),
                new RoleContainsKeywordsPredicate(List.of("Distributor")),
                new TagContainsKeywordsPredicate(List.of("Friend")));

        assertParseSuccess(parser, " n/John r/Distributor t/Friend", expectedFindCommand);
    }

    @Test
    public void parse_multipleDifferentPrefixes_returnsFindCommand() {
        FindCommand expectedFindCommand = new FindCommand(
                new NameContainsKeywordsPredicate(List.of("Alice", "John")),
                new RoleContainsKeywordsPredicate(List.of("Distributor", "Employee")),
                new TagContainsKeywordsPredicate(List.of("Friend", "Family")));

        assertParseSuccess(
                parser,
                " n/Alice n/John r/Distributor r/Employee t/Friend t/Family",
                expectedFindCommand);
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
    public void parse_emptyPrefixedValue_throwsParseException() {
        assertParseFailure(parser, " n/", FindCommand.MESSAGE_EMPTY_PREFIX_VALUE);

        assertParseFailure(parser, " r/", FindCommand.MESSAGE_EMPTY_PREFIX_VALUE);

        assertParseFailure(parser, " t/", FindCommand.MESSAGE_EMPTY_PREFIX_VALUE);

        assertParseFailure(parser, " n/   n/", FindCommand.MESSAGE_EMPTY_PREFIX_VALUE);

        assertParseFailure(parser, " r/   r/", FindCommand.MESSAGE_EMPTY_PREFIX_VALUE);

        assertParseFailure(parser, " n/   r/", FindCommand.MESSAGE_EMPTY_PREFIX_VALUE);

        assertParseFailure(parser, " t/   r/  n/", FindCommand.MESSAGE_EMPTY_PREFIX_VALUE);

        assertParseFailure(parser, " n/   r/ t/  ", FindCommand.MESSAGE_EMPTY_PREFIX_VALUE);

        assertParseFailure(parser, " r/Employee   r/ t/", FindCommand.MESSAGE_EMPTY_PREFIX_VALUE);

        assertParseFailure(parser, " n/Alice   n/ t/", FindCommand.MESSAGE_EMPTY_PREFIX_VALUE);
    }

    @Test
    public void parse_legacyAndPrefixedValues_throwsParseException() {
        assertParseFailure(parser, "Alice n/Tan t/Good", FindCommand.MESSAGE_MIXED_SEARCH_MODE);

        assertParseFailure(parser, "Alice r/Employee", FindCommand.MESSAGE_MIXED_SEARCH_MODE);

        assertParseFailure(parser, "Alice  Bob n/Tan", FindCommand.MESSAGE_MIXED_SEARCH_MODE);
    }

    @Test
    public void parse_unrecognisedPrefix_throwsParseException() {
        assertParseFailure(parser, " p/91234567",
                Messages.getErrorMessageForUnrecognizedPrefixes(PREFIX_PHONE));
    }
}
