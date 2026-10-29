package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.abort;
import static seedu.address.logic.commands.ExitCommand.MESSAGE_EXIT_ACKNOWLEDGEMENT;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.CARL;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;
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
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

public class MainWindowTest {

    @TempDir
    public Path testFolder;

    private Stage stage;
    private MainWindow mainWindow;

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
        initializeMainWindow(model, new JsonAddressBookStorage(testFolder.resolve("addressbook.json")));
    }

    /**
     * Creates a window with injectable address book storage for saving-failure tests.
     */
    private void initializeMainWindow(ModelManager model, JsonAddressBookStorage addressBookStorage) {
        Path addressBookPath = addressBookStorage.getAddressBookFilePath();
        StorageManager storage = new StorageManager(
                addressBookStorage,
                new JsonUserPrefsStorage(testFolder.resolve("preferences.json"))
        );
        Logic logic = new LogicManager(model, storage);

        stage = new Stage();
        mainWindow = new MainWindow(stage, logic, addressBookPath);
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
            assertEquals(1, details.getChildren().size());
            Label emptyState = (Label) details.lookup("#emptyState");
            assertEquals("Select a contact to view details.", emptyState.getText());
            assertTrue(emptyState.isVisible());
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

    /**
     * Creates a window with two distinct contacts and returns its model for list-change tests.
     */
    private ModelManager initializeContactWindow() {
        ModelManager model = new ModelManager();
        model.addPerson(ALICE);
        model.addPerson(BENSON);
        initializeMainWindow(model);
        return model;
    }

    private ListView<?> contactList() {
        return (ListView<?>) stage.getScene().lookup("#personListView");
    }

    private void assertDetailsName(String expected) {
        ScrollPane scroll = (ScrollPane) stage.getScene().lookup("#detailsScrollPane");
        Label name = (Label) scroll.getContent().lookup("#name");
        assertEquals(expected, name.getText());
        assertTrue(scroll.isVisible());
        assertFalse(stage.getScene().lookup("#emptyState").isVisible());
    }

    private void assertSelectionCleared() {
        assertNull(mainWindow.getPersonListPanel().selectedPersonProperty().get());
        assertNull(contactList().getSelectionModel().getSelectedItem());
        assertTrue(stage.getScene().lookup("#emptyState").isVisible());
        ScrollPane scroll = (ScrollPane) stage.getScene().lookup("#detailsScrollPane");
        assertFalse(scroll.isVisible());
        assertEquals("", ((Label) scroll.getContent().lookup("#name")).getText());
    }

    @Test
    public void selection_mouseAndArrowKeys_updateDetailsWithoutMovingFocus() throws Exception {
        runOnFxThread(this::initializeContactWindow);
        awaitLayout();
        runOnFxThread(() -> {
            ListView<?> list = contactList();
            ListCell<?> cell = (ListCell<?>) list.lookupAll(".list-cell").stream()
                    .filter(node -> node instanceof ListCell<?> item && item.getItem() == ALICE)
                    .findFirst().orElseThrow();
            Bounds bounds = cell.localToScene(cell.getLayoutBounds());
            double x = bounds.getCenterX();
            double y = bounds.getCenterY();
            cell.fireEvent(new MouseEvent(MouseEvent.MOUSE_PRESSED, x, y, x, y, MouseButton.PRIMARY, 1,
                    false, false, false, false, true, false, false, false, false, false,
                    new PickResult(cell, x, y)));
            cell.fireEvent(new MouseEvent(MouseEvent.MOUSE_RELEASED, x, y, x, y, MouseButton.PRIMARY, 1,
                    false, false, false, false, false, false, false, false, false, false,
                    new PickResult(cell, x, y)));
            assertSame(ALICE, mainWindow.getPersonListPanel().selectedPersonProperty().get());
            assertDetailsName(ALICE.getName().fullName);

            list.requestFocus();
            list.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.DOWN, false, false, false, false));
            assertSame(BENSON, mainWindow.getPersonListPanel().selectedPersonProperty().get());
            assertDetailsName(BENSON.getName().fullName);
            assertSame(list, stage.getScene().getFocusOwner());

            list.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.UP, false, false, false, false));
            assertSame(ALICE, mainWindow.getPersonListPanel().selectedPersonProperty().get());
            assertDetailsName(ALICE.getName().fullName);
        });
    }

    @Test
    public void selection_startupAndNullSelection_showEmptyState() throws Exception {
        runOnFxThread(() -> {
            initializeContactWindow();
            assertSelectionCleared();
            contactList().getSelectionModel().select(0);
            assertDetailsName(ALICE.getName().fullName);
            contactList().getSelectionModel().clearSelection();
            assertSelectionCleared();
        });
    }

    @Test
    public void selection_deleteFilterAndClear_removeStaleDetails() throws Exception {
        runOnFxThread(() -> {
            ModelManager model = initializeContactWindow();
            contactList().getSelectionModel().select(0);
            model.deletePerson(ALICE);
            assertSelectionCleared();

            contactList().getSelectionModel().select(0);
            assertDetailsName(BENSON.getName().fullName);
            model.updateFilteredPersonList(_ -> false);
            assertSelectionCleared();

            model.updateFilteredPersonList(_ -> true);
            contactList().getSelectionModel().select(0);
            assertDetailsName(BENSON.getName().fullName);
            model.setAddressBook(new AddressBook());
            assertSelectionCleared();
        });
    }

    @Test
    public void selection_directReplacementAndSorting_clearPotentiallyStaleDetails() throws Exception {
        runOnFxThread(() -> {
            ModelManager model = initializeContactWindow();
            model.updateSortedPersonList(Comparator.comparing(person -> person.getName().fullName));
            contactList().getSelectionModel().select(0);
            model.setPerson(ALICE, new PersonBuilder(ALICE).withName("Zed Supplier")
                    .withPhone("81000000").withEmail("zed@example.com").build());
            assertSelectionCleared();

            contactList().getSelectionModel().select(0);
            assertDetailsName(BENSON.getName().fullName);
            model.setPerson(BENSON, new PersonBuilder(BENSON).build());
            assertSelectionCleared();

            contactList().getSelectionModel().select(0);
            assertDetailsName(BENSON.getName().fullName);
            model.updateSortedPersonList(Comparator.comparing(person -> person.getName().fullName,
                    Comparator.reverseOrder()));
            assertSelectionCleared();
            contactList().getSelectionModel().select(0);
            assertDetailsName("Zed Supplier");
        });
    }

    private void submitCommand(String text) {
        submitCommand(text, true);
    }

    private void submitCommand(String text, boolean expectSuccess) {
        TextField command = (TextField) stage.getScene().lookup("#commandTextField");
        command.setText(text);
        command.fireEvent(new ActionEvent(command, command));
        TextArea feedback = (TextArea) stage.getScene().lookup("#resultDisplay");
        assertEquals(!expectSuccess, command.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS),
                feedback.getText());
    }

    private void assertSelected(Person expected) {
        assertSame(expected, mainWindow.getPersonListPanel().selectedPersonProperty().get());
        assertSame(expected, contactList().getSelectionModel().getSelectedItem());
        assertDetailsName(expected.getName().fullName);
        ScrollPane scroll = (ScrollPane) stage.getScene().lookup("#detailsScrollPane");
        assertEquals(expected.getPhone().value, ((Label) scroll.getContent().lookup("#phone")).getText());
        assertEquals(expected.getEmail().value, ((Label) scroll.getContent().lookup("#email")).getText());
        assertEquals(expected.getAddress().value, ((Label) scroll.getContent().lookup("#address")).getText());
    }

    private void initializeFailingStorageWindow(ModelManager model) {
        initializeMainWindow(model, new JsonAddressBookStorage(testFolder.resolve("addressbook.json")) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw new IOException("Simulated saving failure");
            }
        });
    }

    @Test
    public void commandSelection_findListAndSort_preserveVisibleContactOrClear() throws Exception {
        runOnFxThread(() -> {
            ModelManager model = initializeContactWindow();
            contactList().getSelectionModel().select(1);
            submitCommand("find Benson");
            assertSelected(BENSON);
            assertEquals(0, contactList().getSelectionModel().getSelectedIndex());

            submitCommand("list");
            assertSelected(BENSON);
            assertEquals(1, contactList().getSelectionModel().getSelectedIndex());
            submitCommand("sort r/ n/");
            assertSelected(BENSON);

            submitCommand("find Alice");
            assertSelectionCleared();
            submitCommand("list");
            assertSelectionCleared();
            assertEquals(2, model.getFilteredPersonList().size());
        });
    }

    @Test
    public void commandSelection_deleteSelected_doesNotSelectNeighbour() throws Exception {
        runOnFxThread(() -> {
            initializeContactWindow();
            contactList().getSelectionModel().select(0);
            submitCommand("delete 1");
            assertSelectionCleared();
            assertEquals(List.of(BENSON), contactList().getItems());

            contactList().getSelectionModel().select(0);
            submitCommand("delete 1");
            assertSelectionCleared();
            assertTrue(contactList().getItems().isEmpty());
        });
    }

    @Test
    public void commandSelection_deleteOtherAndClear_preserveThenClear() throws Exception {
        runOnFxThread(() -> {
            initializeContactWindow();
            contactList().getSelectionModel().select(1);
            submitCommand("delete 1");
            assertSelected(BENSON);
            assertEquals(0, contactList().getSelectionModel().getSelectedIndex());
            submitCommand("clear");
            assertSelectionCleared();
        });
    }

    @Test
    public void commandSelection_filteredSortedEdit_tracksReplacementAcrossIdentityAndIndexChanges() throws Exception {
        runOnFxThread(() -> {
            ModelManager model = initializeContactWindow();
            model.addPerson(CARL);
            submitCommand("sort");
            submitCommand("find Alice");
            contactList().getSelectionModel().select(0);
            submitCommand("edit 1 n/Zed Supplier p/81000000 e/zed@example.com");

            Person edited = model.getFilteredPersonList().get(2);
            assertEquals("Zed Supplier", edited.getName().fullName);
            assertEquals("81000000", edited.getPhone().value);
            assertEquals("zed@example.com", edited.getEmail().value);
            assertSelected(edited);
            assertEquals(2, contactList().getSelectionModel().getSelectedIndex());
        });
    }

    @Test
    public void commandSelection_editOtherAndEqualReplacement_keepCorrectObject() throws Exception {
        runOnFxThread(() -> {
            ModelManager model = initializeContactWindow();
            contactList().getSelectionModel().select(0);
            submitCommand("edit 2 n/A Supplier");
            assertSelected(ALICE);
            submitCommand("sort");
            assertSelected(ALICE);
            assertEquals(1, contactList().getSelectionModel().getSelectedIndex());

            submitCommand("edit 2 n/Alice Pauline");
            Person replaced = model.getFilteredPersonList().get(1);
            assertEquals(ALICE, replaced);
            assertNotSame(ALICE, replaced);
            assertSelected(replaced);
        });
    }

    @Test
    public void commandSelection_followUpChanges_refreshDetailsAndRespectFilter() throws Exception {
        runOnFxThread(() -> {
            ModelManager model = initializeContactWindow();
            contactList().getSelectionModel().select(0);
            submitCommand("followup 1 by/2026-11-01 abt/Confirm switches");
            assertSelected(model.getFilteredPersonList().get(0));
            ScrollPane scroll = (ScrollPane) stage.getScene().lookup("#detailsScrollPane");
            assertEquals("Confirm switches", ((Label) scroll.getContent().lookup("#followUpDescription")).getText());

            submitCommand("followup 1 clear");
            assertSelected(model.getFilteredPersonList().get(0));
            assertFalse(scroll.getContent().lookup("#followUpSection").isVisible());

            submitCommand("followup 1 by/2026-11-01 abt/Call supplier");
            submitCommand("followups by/2026-11-01");
            assertSelected(model.getFilteredPersonList().get(0));
            submitCommand("followup 1 by/2026-11-02 abt/Call tomorrow");
            assertSelectionCleared();
            assertTrue(model.getFilteredPersonList().isEmpty());
        });
    }

    @Test
    public void commandSelection_clearFollowUpWhileFiltered_clearsSelection() throws Exception {
        runOnFxThread(() -> {
            initializeContactWindow();
            submitCommand("followup 1 by/2026-11-01 abt/Check stock");
            assertSelectionCleared();
            submitCommand("followups");
            contactList().getSelectionModel().select(0);
            submitCommand("followup 1 clear");
            assertSelectionCleared();
            submitCommand("list");
            assertSelectionCleared();
        });
    }

    @Test
    public void commandSelection_undoAndRedo_clearEvenForUnchangedSnapshots() throws Exception {
        runOnFxThread(() -> {
            ModelManager model = initializeContactWindow();
            model.commitAddressBook();
            model.commitAddressBook();
            contactList().getSelectionModel().select(0);
            submitCommand("undo");
            assertSelectionCleared();
            contactList().getSelectionModel().select(0);
            submitCommand("redo");
            assertSelectionCleared();

            contactList().getSelectionModel().select(0);
            submitCommand("edit 2 r/Employee");
            assertSelected(ALICE);
            submitCommand("undo");
            assertSelectionCleared();
            contactList().getSelectionModel().select(0);
            submitCommand("redo");
            assertSelectionCleared();
        });
    }

    @Test
    public void commandSelection_failedCommands_preserveSelectionAndAllowFurtherNavigation() throws Exception {
        runOnFxThread(() -> {
            initializeContactWindow();
            contactList().getSelectionModel().select(0);
            for (String command : List.of("unknowncommand", "edit", "edit 9 n/Invalid Index",
                    "edit 1 p/" + BENSON.getPhone().value, "undo", "redo")) {
                submitCommand(command, false);
                assertSelected(ALICE);
            }
            contactList().getSelectionModel().select(1);
            assertSelected(BENSON);
        });
    }

    @Test
    public void commandSelection_savingFailure_reconcilesEditedFilteredAndDeletedContacts() throws Exception {
        runOnFxThread(() -> {
            ModelManager model = new ModelManager();
            model.addPerson(ALICE);
            model.addPerson(BENSON);
            initializeFailingStorageWindow(model);
            contactList().getSelectionModel().select(0);
            submitCommand("edit 1 n/Zed Supplier p/81000000 e/zed@example.com", false);
            assertSelected(model.getFilteredPersonList().get(0));
            assertDetailsName("Zed Supplier");
            submitCommand("find Benson", false);
            assertSelectionCleared();
            contactList().getSelectionModel().select(0);
            assertSelected(BENSON);
            submitCommand("delete 1", false);
            assertSelectionCleared();
            assertTrue(model.getFilteredPersonList().isEmpty());
        });
    }

    @Test
    public void commandSelection_savingFailureDuringUndoRedo_clearsSelection() throws Exception {
        runOnFxThread(() -> {
            ModelManager model = new ModelManager();
            model.addPerson(ALICE);
            model.commitAddressBook();
            model.commitAddressBook();
            initializeFailingStorageWindow(model);
            contactList().getSelectionModel().select(0);
            submitCommand("undo", false);
            assertSelectionCleared();
            contactList().getSelectionModel().select(0);
            submitCommand("redo", false);
            assertSelectionCleared();
        });
    }

    @Test
    public void commandSelection_sameNamesAndSorting_keepSelectedContact() throws Exception {
        runOnFxThread(() -> {
            ModelManager model = initializeContactWindow();
            Person sameName = new PersonBuilder(ALICE).withPhone("81000001").withEmail("other@example.com").build();
            model.addPerson(sameName);
            contactList().getSelectionModel().select(2);
            submitCommand("sort");
            assertSelected(sameName);
            submitCommand("find Alice");
            assertSelected(sameName);
            submitCommand("edit 1 n/Zed Supplier");
            assertSelected(sameName);
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
