package seedu.address.logic.commands;

import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.ExitCommand.MESSAGE_EXIT_ACKNOWLEDGEMENT;
import static seedu.address.logic.commands.ExitCommand.MESSAGE_EXIT_DISCLAIMER;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.ExitCommandParser;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;

public class ExitCommandTest {
    private Model model = new ModelManager();
    private Model expectedModel = new ModelManager();

    @Test
    public void execute_noAdditionalArgs_success() {
        CommandResult expectedCommandResult = new CommandResult(MESSAGE_EXIT_ACKNOWLEDGEMENT, false, true);
        assertCommandSuccess(new ExitCommand(false), model, expectedCommandResult, expectedModel);
    }

    @Test
    public void execute_whitespaceArgs_success() {
        ExitCommand command = new ExitCommandParser().parse(" \t ");
        CommandResult expectedCommandResult = new CommandResult(MESSAGE_EXIT_ACKNOWLEDGEMENT, false, true);
        assertCommandSuccess(command, model, expectedCommandResult, expectedModel);
    }

    @Test
    public void execute_additionalArgs_success() {
        ExitCommand command = new ExitCommandParser().parse(" -f");
        CommandResult expectedCommandResult = new CommandResult(MESSAGE_EXIT_DISCLAIMER, false, true);
        assertCommandSuccess(command, model, expectedCommandResult, expectedModel);
    }
}
