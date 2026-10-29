package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.abort;
import static seedu.address.logic.commands.ExitCommand.MESSAGE_EXIT_ACKNOWLEDGEMENT;
import static seedu.address.testutil.TypicalPersons.ALICE;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import seedu.address.commons.core.GuiSettings;
import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.ListCommand;
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
        initializeMainWindow(new ModelManager());
    }

    /**
     * Creates and shows the test window with the given model on the JavaFX application thread.
     */
    private void initializeMainWindow(ModelManager model) {
        Path addressBookPath = testFolder.resolve("addressbook.json");
        StorageManager storage = new StorageManager(
                new JsonAddressBookStorage(addressBookPath),
                new JsonUserPrefsStorage(testFolder.resolve("preferences.json"))
        );
        Logic logic = new LogicManager(model, storage);

        stage = new Stage();
        MainWindow mainWindow = new MainWindow(stage, logic, addressBookPath);
        mainWindow.fillInnerParts();
        mainWindow.show();
    }

    /**
     * Waits for window-size notifications and layout to reach the scene graph.
     */
    private static void awaitLayout() throws Exception {
        CountDownLatch laidOut = new CountDownLatch(1);
        runOnFxThread(() -> new AnimationTimer() {
            private int frames;

            @Override
            public void handle(long now) {
                if (++frames == 2) {
                    stop();
                    Platform.runLater(laidOut::countDown);
                }
            }
        }.start());
        assertTrue(laidOut.await(10, TimeUnit.SECONDS), "Window layout did not complete");
    }

    /**
     * Checks that both panes fit inside the split area and retain their minimum widths.
     * Must be called on the JavaFX application thread after layout.
     */
    private void assertContactPanesFit() {
        SplitPane splitPane = (SplitPane) stage.getScene().lookup("#contactSplitPane");
        Region list = (Region) splitPane.getItems().get(0);
        Region details = (Region) splitPane.getItems().get(1);
        Bounds splitBounds = splitPane.localToScene(splitPane.getBoundsInLocal());
        Bounds listBounds = list.localToScene(list.getLayoutBounds());
        Bounds detailsBounds = details.localToScene(details.getLayoutBounds());

        assertTrue(list.getWidth() >= list.getMinWidth(), "Contact pane is narrower than its minimum");
        assertTrue(details.getWidth() >= details.getMinWidth(), "Details pane is narrower than its minimum");
        assertTrue(splitBounds.getMinX() >= 0);
        assertTrue(splitBounds.getMaxX() <= stage.getScene().getWidth());
        assertTrue(listBounds.getMinX() >= splitBounds.getMinX());
        assertTrue(listBounds.getMaxX() <= detailsBounds.getMinX());
        assertTrue(detailsBounds.getMaxX() <= splitBounds.getMaxX());

        Node command = stage.getScene().lookup("#commandBoxPlaceholder");
        Node feedback = stage.getScene().lookup("#resultDisplayPlaceholder");
        Node status = stage.getScene().lookup("#statusbarPlaceholder");
        assertTrue(command.localToScene(command.getLayoutBounds()).getMaxY()
                <= feedback.localToScene(feedback.getLayoutBounds()).getMinY());
        assertTrue(feedback.localToScene(feedback.getLayoutBounds()).getMaxY() <= splitBounds.getMinY());
        assertTrue(splitBounds.getMaxY() <= status.localToScene(status.getLayoutBounds()).getMinY());
    }

    @Test
    public void layout_defaultWindow_showsTwoPanesAtMinimumWidth() throws Exception {
        runOnFxThread(this::initializeMainWindow);
        awaitLayout();

        runOnFxThread(() -> {
            assertEquals(800, stage.getWidth(), 1);
            SplitPane splitPane = (SplitPane) stage.getScene().lookup("#contactSplitPane");
            assertEquals(0.45, splitPane.getDividerPositions()[0], 0.02);
            StackPane details = (StackPane) stage.getScene().lookup("#personDetailsPanelPlaceholder");
            assertTrue(details.isVisible());
            assertTrue(details.getChildren().isEmpty());
            assertContactPanesFit();
        });
    }

    @Test
    public void layout_savedWindowWidth_clampsSmallWidthsAndPreservesLargerWidths() throws Exception {
        ModelManager model = new ModelManager();
        model.setGuiSettings(new GuiSettings(450, 600, 0, 0));
        runOnFxThread(() -> initializeMainWindow(model));
        awaitLayout();
        runOnFxThread(() -> {
            assertEquals(800, stage.getWidth(), 1);
            assertContactPanesFit();
            stage.hide();
        });

        model.setGuiSettings(new GuiSettings(1100, 720, 0, 0));
        runOnFxThread(() -> initializeMainWindow(model));
        awaitLayout();
        runOnFxThread(() -> {
            assertEquals(1100, stage.getWidth(), 1);
            assertEquals(720, stage.getHeight(), 1);
            assertContactPanesFit();
        });
    }

    @Test
    public void layout_resizeAndMoveDivider_keepsBothPanesWithinBounds() throws Exception {
        runOnFxThread(() -> {
            initializeMainWindow();
            stage.setWidth(1100);
            stage.setHeight(780);
        });
        awaitLayout();

        runOnFxThread(() -> {
            SplitPane splitPane = (SplitPane) stage.getScene().lookup("#contactSplitPane");
            Node divider = splitPane.lookup(".split-pane-divider");
            Bounds bounds = divider.localToScene(divider.getBoundsInLocal());
            double x = bounds.getCenterX();
            double y = bounds.getCenterY();
            double initialPosition = splitPane.getDividerPositions()[0];
            divider.fireEvent(new MouseEvent(MouseEvent.MOUSE_PRESSED, x, y, x, y, MouseButton.PRIMARY, 1,
                    false, false, false, false, true, false, false, false, false, false,
                    new PickResult(divider, x, y)));
            divider.fireEvent(new MouseEvent(MouseEvent.MOUSE_DRAGGED, x + 100, y, x + 100, y, MouseButton.PRIMARY, 1,
                    false, false, false, false, true, false, false, false, false, false,
                    new PickResult(divider, x + 100, y)));
            divider.fireEvent(new MouseEvent(MouseEvent.MOUSE_RELEASED, x + 100, y, x + 100, y, MouseButton.PRIMARY, 1,
                    false, false, false, false, false, false, false, false, false, false,
                    new PickResult(divider, x + 100, y)));
            assertTrue(splitPane.getDividerPositions()[0] > initialPosition + 0.05,
                    "Dragging the divider should increase the width of the contact pane");
            splitPane.setDividerPositions(0.6);
        });
        awaitLayout();
        runOnFxThread(() -> {
            SplitPane splitPane = (SplitPane) stage.getScene().lookup("#contactSplitPane");
            assertEquals(0.6, splitPane.getDividerPositions()[0], 0.02);
            assertContactPanesFit();
        });

        for (double position : new double[] {0, 1}) {
            runOnFxThread(() -> {
                stage.setWidth(stage.getMinWidth());
                stage.setHeight(stage.getMinHeight());
                SplitPane splitPane = (SplitPane) stage.getScene().lookup("#contactSplitPane");
                splitPane.setDividerPositions(position);
            });
            awaitLayout();
            runOnFxThread(this::assertContactPanesFit);
        }
    }

    @Test
    public void executeCommand_list_preservesContactListAndFeedback() throws Exception {
        runOnFxThread(() -> {
            ModelManager model = new ModelManager();
            model.addPerson(ALICE);
            model.updateFilteredPersonList(_ -> false);
            initializeMainWindow(model);

            ListView<?> list = (ListView<?>) stage.getScene().lookup("#personListView");
            assertTrue(list.getItems().isEmpty());
            TextField command = (TextField) stage.getScene().lookup("#commandTextField");
            command.setText("list");
            command.fireEvent(new ActionEvent(command, command));

            assertEquals(List.of(ALICE), list.getItems());
            TextArea feedback = (TextArea) stage.getScene().lookup("#resultDisplay");
            assertEquals(ListCommand.MESSAGE_SUCCESS, feedback.getText());
        });
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
