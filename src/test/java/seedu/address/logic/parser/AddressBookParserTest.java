package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.logic.commands.CommandTestUtil.VALID_FOLLOW_UP_DATE;
import static seedu.address.logic.commands.CommandTestUtil.VALID_FOLLOW_UP_DESCRIPTION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_FOLLOW_UP_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_FOLLOW_UP_DESCRIPTION;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.ClearFollowUpCommand;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.FollowUpCommand;
import seedu.address.logic.commands.FollowUpsCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.RedoCommand;
import seedu.address.logic.commands.SelectCommand;
import seedu.address.logic.commands.SetFollowUpCommand;
import seedu.address.logic.commands.SortCommand;
import seedu.address.logic.commands.SortCommand.SortField;
import seedu.address.logic.commands.UndoCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.FollowUp;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.Person;
import seedu.address.testutil.EditPersonDescriptorBuilder;
import seedu.address.testutil.PersonBuilder;
import seedu.address.testutil.PersonUtil;

public class AddressBookParserTest {

    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void parseCommand_add() throws Exception {
        Person person = new PersonBuilder().build();
        AddCommand command = (AddCommand) parser.parseCommand(PersonUtil.getAddCommand(person));
        assertEquals(new AddCommand(person), command);
    }

    @Test
    public void parseCommand_clear() throws Exception {
        assertTrue(parser.parseCommand(ClearCommand.COMMAND_WORD) instanceof ClearCommand);
        assertTrue(parser.parseCommand(ClearCommand.COMMAND_WORD + " 3") instanceof ClearCommand);
    }

    @Test
    public void parseCommand_delete() throws Exception {
        DeleteCommand command = (DeleteCommand) parser.parseCommand(
                DeleteCommand.COMMAND_WORD + " " + INDEX_FIRST_PERSON.getOneBased());
        assertEquals(new DeleteCommand(INDEX_FIRST_PERSON), command);
    }

    @Test
    public void parseCommand_select_dispatchesToSelectParser() throws Exception {
        assertEquals(new SelectCommand(INDEX_FIRST_PERSON), parser.parseCommand(" select \t1 "));
    }

    @Test
    public void parseCommand_selectInvalidArguments_throwsSelectParseErrors() {
        assertThrows(ParseException.class, SelectCommand.MESSAGE_INDEX_REQUIRED, () -> parser.parseCommand("select"));
        assertThrows(ParseException.class, SelectCommand.MESSAGE_EXTRA_ARGUMENTS, () ->
                parser.parseCommand("select 1 2"));
    }

    @Test
    public void parseCommand_edit() throws Exception {
        Person person = new PersonBuilder().withTags("solderer").build();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder(person).withTagsToDelete("trainee").build();
        EditCommand command = (EditCommand) parser.parseCommand(EditCommand.COMMAND_WORD + " "
                + INDEX_FIRST_PERSON.getOneBased() + " " + PersonUtil.getEditPersonDescriptorDetails(descriptor));
        assertEquals(new EditCommand(INDEX_FIRST_PERSON, descriptor), command);
    }

    @Test
    public void parseCommand_exit() throws Exception {
        assertTrue(parser.parseCommand(ExitCommand.COMMAND_WORD) instanceof ExitCommand);
        assertTrue(parser.parseCommand(ExitCommand.COMMAND_WORD + " 3") instanceof ExitCommand);
    }

    @Test
    public void parseCommand_find() throws Exception {
        List<String> keywords = List.of("foo", "bar", "baz");
        FindCommand command = (FindCommand) parser.parseCommand(
                FindCommand.COMMAND_WORD + " " + keywords.stream().collect(Collectors.joining(" ")));
        assertEquals(new FindCommand(new NameContainsKeywordsPredicate(keywords)), command);
    }

    @Test
    public void parseCommand_help() throws Exception {
        assertTrue(parser.parseCommand(HelpCommand.COMMAND_WORD) instanceof HelpCommand);
        assertTrue(parser.parseCommand(HelpCommand.COMMAND_WORD + " " + AddCommand.COMMAND_WORD)
                instanceof HelpCommand);
    }

    @Test
    public void parseCommand_listWithoutArguments_returnsListCommand() throws Exception {
        assertTrue(parser.parseCommand(ListCommand.COMMAND_WORD) instanceof ListCommand);
    }

    @Test
    public void parseCommand_listWithArguments_returnsListCommand() throws Exception {
        assertTrue(parser.parseCommand(ListCommand.COMMAND_WORD + " 3") instanceof ListCommand);
    }

    @Test
    public void parseCommand_listWithWhitespaceOnlyArguments_returnsListCommand() throws Exception {
        assertTrue(parser.parseCommand(ListCommand.COMMAND_WORD + " \t  ") instanceof ListCommand);
    }

    @Test
    public void parseCommand_undo() throws Exception {
        assertTrue(parser.parseCommand(UndoCommand.COMMAND_WORD) instanceof UndoCommand);
        assertTrue(parser.parseCommand(UndoCommand.COMMAND_WORD + " \t  ") instanceof UndoCommand);
    }

    @Test
    public void parseCommand_undoWithArgs_throwsParseException() {
        assertThrows(ParseException.class, () -> parser.parseCommand(UndoCommand.COMMAND_WORD + " 123"));
    }

    @Test
    public void parseCommand_redo() throws Exception {
        assertTrue(parser.parseCommand(RedoCommand.COMMAND_WORD) instanceof RedoCommand);
        assertTrue(parser.parseCommand(RedoCommand.COMMAND_WORD + " \t  ") instanceof RedoCommand);
    }

    @Test
    public void parseCommand_redoWithArgs_throwsParseException() {
        assertThrows(ParseException.class, () -> parser.parseCommand(RedoCommand.COMMAND_WORD + " 123"));
    }

    @Test
    public void parseCommand_sort() throws Exception {
        assertEquals(new SortCommand(List.of(SortField.NAME)), parser.parseCommand("sort"));
        assertEquals(new SortCommand(List.of(SortField.ROLE, SortField.NAME)),
                parser.parseCommand("sort r/ n/"));
    }

    @Test
    public void parseCommand_unrecognisedInput_throwsParseException() {
        assertThrows(ParseException.class, AddressBookParser.MESSAGE_COMMAND_REQUIRED, () -> parser.parseCommand(""));
    }

    @Test
    public void parseCommand_helpWithMultipleTargets_throwsParseException() {
        String expectedMessage = AddressBookParser.MESSAGE_MULTIPLE_HELP_COMMANDS;
        assertThrows(ParseException.class, expectedMessage, () -> parser.parseCommand("help add edit"));
    }

    @Test
    public void parseCommand_unknownCommand_throwsParseException() {
        assertThrows(ParseException.class, MESSAGE_UNKNOWN_COMMAND, () -> parser.parseCommand("unknownCommand"));
    }

    @Test
    public void parseCommand_followUpSet() throws Exception {
        FollowUp followUp = new FollowUp(
                LocalDate.parse(VALID_FOLLOW_UP_DATE), VALID_FOLLOW_UP_DESCRIPTION);

        SetFollowUpCommand command = (SetFollowUpCommand) parser.parseCommand(
                FollowUpCommand.COMMAND_WORD + " " + INDEX_FIRST_PERSON.getOneBased()
                        + " " + PREFIX_FOLLOW_UP_DATE + VALID_FOLLOW_UP_DATE
                        + " " + PREFIX_FOLLOW_UP_DESCRIPTION + VALID_FOLLOW_UP_DESCRIPTION);

        assertEquals(new SetFollowUpCommand(INDEX_FIRST_PERSON, followUp), command);
    }

    @Test
    public void parseCommand_followUpClear() throws Exception {
        ClearFollowUpCommand command = (ClearFollowUpCommand) parser.parseCommand(
                FollowUpCommand.COMMAND_WORD + " " + INDEX_FIRST_PERSON.getOneBased() + " clear");

        assertEquals(new ClearFollowUpCommand(INDEX_FIRST_PERSON), command);
    }

    @Test
    public void parseCommand_followUpWithoutArguments_throwsParseException() {
        assertThrows(ParseException.class,
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, FollowUpCommand.COMMAND_USAGE), () ->
                        parser.parseCommand(FollowUpCommand.COMMAND_WORD));
    }

    @Test
    public void parseCommand_followUpMixedOperations_throwsParseException() {
        assertThrows(ParseException.class,
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, FollowUpCommand.COMMAND_USAGE), () ->
                        parser.parseCommand(
                        FollowUpCommand.COMMAND_WORD + " " + INDEX_FIRST_PERSON.getOneBased()
                                + " clear " + PREFIX_FOLLOW_UP_DATE + VALID_FOLLOW_UP_DATE
                                + " " + PREFIX_FOLLOW_UP_DESCRIPTION + VALID_FOLLOW_UP_DESCRIPTION));
    }

    @Test
    public void parseCommand_followUpsAll() throws Exception {
        assertEquals(FollowUpsCommand.all(),
                parser.parseCommand(FollowUpsCommand.COMMAND_WORD));
        assertEquals(FollowUpsCommand.all(),
                parser.parseCommand(FollowUpsCommand.COMMAND_WORD + " \t "));
    }

    @Test
    public void parseCommand_followUpsOverdue() throws Exception {
        assertEquals(FollowUpsCommand.overdue(),
                parser.parseCommand(FollowUpsCommand.COMMAND_WORD + " overdue"));
    }

    @Test
    public void parseCommand_followUpsDueBy() throws Exception {
        assertEquals(FollowUpsCommand.dueBy(LocalDate.parse(VALID_FOLLOW_UP_DATE)),
                parser.parseCommand(FollowUpsCommand.COMMAND_WORD
                        + " " + PREFIX_FOLLOW_UP_DATE + VALID_FOLLOW_UP_DATE));
    }

    @Test
    public void parseCommand_followUpsInvalidDate_throwsParseException() {
        assertThrows(ParseException.class, ParserUtil.MESSAGE_INVALID_FOLLOW_UP_DATE, () ->
                parser.parseCommand(FollowUpsCommand.COMMAND_WORD + " by/2026-02-30"));
    }

    @Test
    public void parseCommand_followUpsUnsupportedArguments_throwsParseException() {
        String expectedMessage =
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, FollowUpsCommand.COMMAND_USAGE);

        assertThrows(ParseException.class, expectedMessage, () ->
                parser.parseCommand(FollowUpsCommand.COMMAND_WORD + " late"));
        assertThrows(ParseException.class, expectedMessage, () ->
                parser.parseCommand(FollowUpsCommand.COMMAND_WORD + " overdue by/2026-11-01"));
    }
}
