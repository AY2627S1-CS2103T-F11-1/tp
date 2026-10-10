package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.abort;
import static seedu.address.logic.commands.ExitCommand.MESSAGE_EXIT_ACKNOWLEDGEMENT;

import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.model.ModelManager;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

public class MainWindowTest {

    @TempDir
    public Path testFolder;

    private Stage stage;

    @BeforeAll
    public static void initializeJavaFx() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        Runnable initialize = () -> {
            // Keep JavaFX running when the test window closes.
            Platform.setImplicitExit(false);
            started.countDown();
        };

        try {
            Platform.startup(initialize);
        } catch (IllegalStateException e) {
            // Another UI test may already have started JavaFX
            Platform.runLater(initialize);
        } catch (UnsupportedOperationException e) {
            if (!"Unable to open DISPLAY".equals(e.getMessage())) {
                throw e;
            }
            abort("Skipping UI tests: no graphical display is available");
        }

        assertTrue(started.await(10, TimeUnit.SECONDS),
                "JavaFX did not initialize");
    }

    @AfterEach
    public void cleanUp() throws Exception {
        runOnFxThread(() -> {
            if (stage != null) {
                stage.hide();
            }
        });
    }

    private static void runOnFxThread(Runnable action) throws Exception {
        FutureTask<Void> task = new FutureTask<>(action, null);
        Platform.runLater(task);
        task.get(10, TimeUnit.SECONDS);
    }

    /**
     * Creates and shows the test window.
     * Must be called on the JavaFX application thread.
     */
    private void initializeMainWindow() {
        Path addressBookPath = testFolder.resolve("addressbook.json");
        StorageManager storage = new StorageManager(
                new JsonAddressBookStorage(addressBookPath),
                new JsonUserPrefsStorage(testFolder.resolve("preferences.json"))
        );
        Logic logic = new LogicManager(new ModelManager(), storage);

        stage = new Stage();
        MainWindow mainWindow = new MainWindow(stage, logic, addressBookPath);
        mainWindow.fillInnerParts();
        mainWindow.show();
    }

    @Test
    public void executeCommand_exit_displaysMessageDisablesInputAndCloses()
            throws Exception {
        CountDownLatch hidden = new CountDownLatch(1);

        runOnFxThread(() -> {
            initializeMainWindow();
            stage.setOnHidden(_ -> hidden.countDown());

            TextField commandField = (TextField) stage.getScene().lookup("#commandTextField");
            TextArea resultDisplay = (TextArea) stage.getScene().lookup("#resultDisplay");

            commandField.setText("exit");
            commandField.fireEvent(new ActionEvent(commandField, commandField));

            assertEquals(MESSAGE_EXIT_ACKNOWLEDGEMENT, resultDisplay.getText());
            assertTrue(commandField.isDisabled(), "Command input should be disabled during exit");
            assertTrue(stage.isShowing(), "Window should remain open after submitting exit");
        });

        assertTrue(hidden.await(5, TimeUnit.SECONDS),
                "Window did not close after the exit delay");

        runOnFxThread(() -> assertFalse(stage.isShowing()));
    }
}
