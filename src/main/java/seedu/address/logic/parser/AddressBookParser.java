package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.FollowUpCommand;
import seedu.address.logic.commands.FollowUpsCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.RedoCommand;
import seedu.address.logic.commands.SortCommand;
import seedu.address.logic.commands.UndoCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses user input.
 */
public class AddressBookParser {

    static final String MESSAGE_COMMAND_REQUIRED = "Enter a command.";
    static final String MESSAGE_MULTIPLE_HELP_COMMANDS = "Specify only one command after help.";

    /**
     * Used for initial separation of command word and args.
     */
    private static final Pattern BASIC_COMMAND_FORMAT = Pattern.compile("(?<commandWord>\\S+)(?<arguments>.*)");
    private static final Logger logger = LogsCenter.getLogger(AddressBookParser.class);

    /**
     * Parses user input into command for execution.
     *
     * @param userInput full user input string
     * @return the command based on the user input
     * @throws ParseException if the user input does not conform to the expected format
     */
    public Command parseCommand(String userInput) throws ParseException {
        final Matcher matcher = BASIC_COMMAND_FORMAT.matcher(userInput.trim());
        if (!matcher.matches()) {
            throw new ParseException(MESSAGE_COMMAND_REQUIRED);
        }

        final String commandWord = matcher.group("commandWord");
        final String arguments = matcher.group("arguments");

        // Note to developers: Change LOG_LEVEL in LogsCenter to enable lower level (i.e., FINE, FINER and lower)
        // log messages such as the one below.
        // Lower level log messages are used sparingly to minimize noise in the code.
        logger.fine("Command word: " + commandWord + "; Arguments: " + arguments);

        return switch (commandWord) {
            case AddCommand.COMMAND_WORD -> new AddCommandParser().parse(arguments);
            case EditCommand.COMMAND_WORD -> new EditCommandParser().parse(arguments);
            case DeleteCommand.COMMAND_WORD -> new DeleteCommandParser().parse(arguments);
            case ClearCommand.COMMAND_WORD -> new ClearCommandParser().parse(arguments);
            case FindCommand.COMMAND_WORD -> new FindCommandParser().parse(arguments);
            case ListCommand.COMMAND_WORD -> new ListCommandParser().parse(arguments);
            case ExitCommand.COMMAND_WORD -> new ExitCommandParser().parse(arguments);
            case HelpCommand.COMMAND_WORD -> parseHelpCommand(arguments);
            case UndoCommand.COMMAND_WORD -> new UndoCommandParser().parse(arguments);
            case RedoCommand.COMMAND_WORD -> new RedoCommandParser().parse(arguments);
            case FollowUpCommand.COMMAND_WORD -> new FollowUpCommandParser().parse(arguments);
            case FollowUpsCommand.COMMAND_WORD -> new FollowUpsCommandParser().parse(arguments);
            case SortCommand.COMMAND_WORD -> new SortCommandParser().parse(arguments);

            default -> {
                logger.finer("This user input caused a ParseException: " + userInput);
                throw new ParseException(MESSAGE_UNKNOWN_COMMAND);
            }
        };
    }

    /** Parses the optional command name for the help command. */
    private Command parseHelpCommand(String arguments) throws ParseException {
        String commandName = arguments.trim();
        if (commandName.isEmpty()) {
            return new HelpCommand();
        }
        if (commandName.matches("\\S+")) {
            return new HelpCommand(commandName);
        }
        throw new ParseException(MESSAGE_MULTIPLE_HELP_COMMANDS);
    }

}
