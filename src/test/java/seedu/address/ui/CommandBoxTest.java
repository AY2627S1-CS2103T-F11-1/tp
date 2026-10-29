package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.ui.CommandBox.ERROR_STYLE_CLASS;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.TextField;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;

public class CommandBoxTest {

    private static final String VALID_COMMAND = "list";
    private static final String INVALID_COMMAND = "invalid command";

    private final List<String> executedCommands = new ArrayList<>();
    private CommandBox commandBox;
    private TextField commandTextField;

    @BeforeAll
    public static void initializeJavaFx() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        Runnable initialize = () -> {
            Platform.setImplicitExit(false);
            started.countDown();
        };

        try {
            Platform.startup(initialize);
        } catch (IllegalStateException e) {
            Platform.runLater(initialize);
        }

        assertTrue(started.await(10, TimeUnit.SECONDS), "JavaFX did not initialize");
    }

    @BeforeEach
    public void setUp() throws Exception {
        executedCommands.clear();
        runOnFxThread(() -> {
            commandBox = new CommandBox(commandText -> {
                executedCommands.add(commandText);
                if (INVALID_COMMAND.equals(commandText)) {
                    throw new ParseException("Parse error");
                }
                return new CommandResult("Command success");
            });
            commandTextField = (TextField) commandBox.getRoot().lookup("#commandTextField");
        });
    }

    private static void runOnFxThread(Runnable action) throws Exception {
        FutureTask<Void> task = new FutureTask<>(action, null);
        Platform.runLater(task);
        task.get(10, TimeUnit.SECONDS);
    }

    @Test
    public void commandBox_startingWithDefaultStyle() throws Exception {
        runOnFxThread(() -> {
            assertFalse(commandTextField.getStyleClass().contains(ERROR_STYLE_CLASS));
            assertFalse(commandBox.getRoot().getStyleClass().contains(ERROR_STYLE_CLASS));
        });
    }

    @Test
    public void handleCommandEntered_validCommand_clearsInputAndMaintainsDefaultStyle() throws Exception {
        runOnFxThread(() -> {
            commandTextField.setText(VALID_COMMAND);
            commandTextField.fireEvent(new ActionEvent(commandTextField, commandTextField));

            assertEquals(List.of(VALID_COMMAND), executedCommands);
            assertEquals("", commandTextField.getText());
            assertFalse(commandTextField.getStyleClass().contains(ERROR_STYLE_CLASS));
            assertFalse(commandBox.getRoot().getStyleClass().contains(ERROR_STYLE_CLASS));
        });
    }

    @Test
    public void handleCommandEntered_invalidCommand_indicatesFailureOnFieldAndContainer() throws Exception {
        runOnFxThread(() -> {
            commandTextField.setText(INVALID_COMMAND);
            commandTextField.fireEvent(new ActionEvent(commandTextField, commandTextField));

            assertEquals(List.of(INVALID_COMMAND), executedCommands);
            assertEquals(INVALID_COMMAND, commandTextField.getText());
            assertTrue(commandTextField.getStyleClass().contains(ERROR_STYLE_CLASS));
            assertTrue(commandBox.getRoot().getStyleClass().contains(ERROR_STYLE_CLASS));
        });
    }

    @Test
    public void handleCommandEntered_emptyInput_doesNotExecute() throws Exception {
        runOnFxThread(() -> {
            commandTextField.setText("");
            commandTextField.fireEvent(new ActionEvent(commandTextField, commandTextField));

            assertTrue(executedCommands.isEmpty());
            assertFalse(commandTextField.getStyleClass().contains(ERROR_STYLE_CLASS));
            assertFalse(commandBox.getRoot().getStyleClass().contains(ERROR_STYLE_CLASS));
        });
    }

    @Test
    public void textPropertyListener_afterError_resetsToDefaultStyle() throws Exception {
        runOnFxThread(() -> {
            commandTextField.setText(INVALID_COMMAND);
            commandTextField.fireEvent(new ActionEvent(commandTextField, commandTextField));
            assertTrue(commandBox.getRoot().getStyleClass().contains(ERROR_STYLE_CLASS));

            // Typing new input triggers setStyleToDefault()
            commandTextField.setText("new input");
            assertFalse(commandTextField.getStyleClass().contains(ERROR_STYLE_CLASS));
            assertFalse(commandBox.getRoot().getStyleClass().contains(ERROR_STYLE_CLASS));
        });
    }

    @Test
    public void handleCommandEntered_consecutiveFailures_maintainsErrorStyleWithoutDuplicates() throws Exception {
        runOnFxThread(() -> {
            commandTextField.setText(INVALID_COMMAND);
            commandTextField.fireEvent(new ActionEvent(commandTextField, commandTextField));
            assertTrue(commandBox.getRoot().getStyleClass().contains(ERROR_STYLE_CLASS));

            // Fire error again without changing text
            commandTextField.fireEvent(new ActionEvent(commandTextField, commandTextField));
            long countRoot = commandBox.getRoot().getStyleClass().stream().filter(ERROR_STYLE_CLASS::equals).count();
            long countField = commandTextField.getStyleClass().stream().filter(ERROR_STYLE_CLASS::equals).count();
            assertEquals(1, countRoot);
            assertEquals(1, countField);
        });
    }

    @Test
    public void handleCommandEntered_commandException_indicatesFailure() throws Exception {
        runOnFxThread(() -> {
            CommandBox boxWithCommandException = new CommandBox(commandText -> {
                throw new CommandException("Execution failure");
            });
            TextField field = (TextField) boxWithCommandException.getRoot().lookup("#commandTextField");
            field.setText("delete 999");
            field.fireEvent(new ActionEvent(field, field));

            assertTrue(field.getStyleClass().contains(ERROR_STYLE_CLASS));
            assertTrue(boxWithCommandException.getRoot().getStyleClass().contains(ERROR_STYLE_CLASS));
        });
    }

    @Test
    public void handleCommandEntered_rootAlreadyContainsError_doesNotAddDuplicateError() throws Exception {
        runOnFxThread(() -> {
            commandTextField.setText(INVALID_COMMAND);
            commandBox.getRoot().getStyleClass().add(ERROR_STYLE_CLASS);
            commandTextField.fireEvent(new ActionEvent(commandTextField, commandTextField));

            long countRoot = commandBox.getRoot().getStyleClass().stream().filter(ERROR_STYLE_CLASS::equals).count();
            assertEquals(1, countRoot);
        });
    }
}
