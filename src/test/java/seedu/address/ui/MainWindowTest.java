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
import java.util.stream.IntStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import seedu.address.commons.core.GuiSettings;
import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.SelectCommand;
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
        model.setGuiSettings(new GuiSettings(450, 400, 0, 0));
        runOnFxThread(() -> initializeMainWindow(model));
        awaitLayout();
        runOnFxThread(() -> {
            assertEquals(800, stage.getWidth(), 1);
            assertEquals(600, stage.getHeight(), 1);
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

    private ListCell<?> contactCell(Person person) {
        return (ListCell<?>) contactList().lookupAll(".list-cell").stream()
                .filter(node -> node instanceof ListCell<?> cell && cell.getItem() == person && cell.isVisible())
                .findFirst().orElseThrow();
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
            ListCell<?> cell = contactCell(ALICE);
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
    public void selectCommand_updatesDetailsAndRetainsInputFocus_invalidIndexesKeepSelection() throws Exception {
        runOnFxThread(this::initializeContactWindow);
        awaitLayout();
        runOnFxThread(() -> {
            assertSelectionCleared();
            TextField command = (TextField) stage.getScene().lookup("#commandTextField");
            command.requestFocus();
            submitCommand("select 2");
            assertSelected(BENSON);
            assertEquals(1, contactList().getSelectionModel().getSelectedIndex());
            assertSame(command, stage.getScene().getFocusOwner());
            TextArea feedback = (TextArea) stage.getScene().lookup("#resultDisplay");
            assertEquals(String.format(SelectCommand.MESSAGE_SELECT_CONTACT_SUCCESS, 2, BENSON.getName().fullName),
                    feedback.getText());

            submitCommand("select 0", false);
            assertSelected(BENSON);
            assertSame(command, stage.getScene().getFocusOwner());
            submitCommand("select 3", false);
            assertSelected(BENSON);
            assertSame(command, stage.getScene().getFocusOwner());
        });
    }

    @Test
    public void selectCommand_filteredSortedView_usesDisplayedNumbersAndAllowsRepeatedSelection() throws Exception {
        runOnFxThread(() -> {
            ModelManager model = new ModelManager();
            model.addPerson(CARL);
            model.addPerson(ALICE);
            model.addPerson(BENSON);
            initializeMainWindow(model);
            TextField command = (TextField) stage.getScene().lookup("#commandTextField");
            command.requestFocus();
            submitCommand("sort");
            submitCommand("find Alice Carl");
            List<Person> viewBefore = List.copyOf(model.getFilteredPersonList());
            submitCommand("select 2");
            assertSelected(CARL);
            assertEquals(1, contactList().getSelectionModel().getSelectedIndex());
            submitCommand("select 2");
            assertSelected(CARL);
            assertEquals(viewBefore, model.getFilteredPersonList());
            assertEquals(List.of(CARL, ALICE, BENSON), model.getAddressBook().getPersonList());
            assertSame(command, stage.getScene().getFocusOwner());
        });
    }

    @Test
    public void selectCommand_sameNames_selectsExactContactAfterSorting() throws Exception {
        Person other = new PersonBuilder(ALICE).withPhone("81000001").withEmail("other@example.com")
                .withAddress("Different supplier address").withRole("Employee").build();
        runOnFxThread(() -> {
            ModelManager model = new ModelManager();
            model.addPerson(ALICE);
            model.addPerson(other);
            initializeMainWindow(model);
            submitCommand("select 1");
            assertSelected(ALICE);
            submitCommand("select 2");
            assertSelected(other);
            submitCommand("sort r/ n/");
            assertSelected(other);
            submitCommand("select 2");
            assertSelected(ALICE);
            submitCommand("select 1");
            assertSelected(other);
        });
    }

    @Test
    public void selectCommand_invalidArguments_preserveDetailsFocusAndAllowRecovery() throws Exception {
        runOnFxThread(() -> {
            initializeContactWindow();
            TextField command = (TextField) stage.getScene().lookup("#commandTextField");
            command.requestFocus();
            submitCommand("select 2");
            for (String input : List.of("select", "select 0", "select -1", "select +1", "select text",
                    "select 1.5", "select 2147483648", "select 1 2", "select 1 n/Alice", "select 3")) {
                submitCommand(input, false);
                assertSelected(BENSON);
                assertSame(command, stage.getScene().getFocusOwner());
                assertEquals(input, command.getText());
            }
            submitCommand("select 01");
            assertSelected(ALICE);
            assertEquals("", command.getText());
            submitCommand("list");
            assertSelected(ALICE);
        });
    }

    @Test
    public void selectCommand_emptyResults_preserveEmptyStateUntilAVisibleContactIsSelected() throws Exception {
        runOnFxThread(() -> {
            initializeContactWindow();
            submitCommand("select 2");
            submitCommand("find Nonexistent");
            assertSelectionCleared();
            submitCommand("select 1", false);
            assertSelectionCleared();
            submitCommand("list");
            assertSelectionCleared();
            submitCommand("select 2");
            assertSelected(BENSON);
        });
    }

    @Test
    public void selectCommand_editAndDelete_doNotReuseEarlierSelectionRequests() throws Exception {
        runOnFxThread(() -> {
            ModelManager model = initializeContactWindow();
            submitCommand("select 1");
            submitCommand("edit 1 n/Zed Supplier p/81000000 e/zed@example.com");
            Person edited = model.getFilteredPersonList().get(0);
            assertSelected(edited);
            submitCommand("sort");
            assertSelected(edited);
            submitCommand("select 1");
            assertSelected(BENSON);
            submitCommand("delete 1");
            assertSelectionCleared();
            submitCommand("select 1");
            assertSelected(edited);
        });
    }

    @Test
    public void selectCommand_savingFailure_updatesTargetWithoutLeakingRequestsToLaterCommands() throws Exception {
        runOnFxThread(() -> {
            ModelManager model = new ModelManager();
            model.addPerson(ALICE);
            model.addPerson(BENSON);
            initializeFailingStorageWindow(model);
            TextField command = (TextField) stage.getScene().lookup("#commandTextField");
            command.requestFocus();
            submitCommand("select 2", false);
            assertSelected(BENSON);
            TextArea feedback = (TextArea) stage.getScene().lookup("#resultDisplay");
            assertTrue(feedback.getText().contains("Simulated saving failure"));
            contactList().getSelectionModel().select(0);
            assertSelected(ALICE);
            submitCommand("select 0", false);
            assertSelected(ALICE);
            submitCommand("list", false);
            assertSelected(ALICE);
            submitCommand("select 2", false);
            assertSelected(BENSON);
            assertSame(command, stage.getScene().getFocusOwner());
            assertEquals(List.of(ALICE, BENSON), model.getAddressBook().getPersonList());
            assertFalse(model.canUndoAddressBook());
            assertFalse(model.canRedoAddressBook());
        });
    }

    @Test
    public void selectCommand_offscreenAndRepeated_revealsContactWithoutResettingDetailsScroll() throws Exception {
        Person target = new PersonBuilder().withName("Target Supplier").withPhone("81000999")
                .withEmail("target@example.com").withTags("X".repeat(50), "switches", "lube")
                .withAddress("Unit 08 Artisan Keyboard Business Park, ".repeat(16))
                .withFollowUp("2026-11-01", "Confirm switch shipment ".repeat(8).strip()).build();
        runOnFxThread(() -> {
            ModelManager model = new ModelManager();
            for (int i = 0; i < 40; i++) {
                model.addPerson(new PersonBuilder().withName("Supplier " + i).withPhone("81000" + (100 + i))
                        .withEmail("supplier" + i + "@example.com").build());
            }
            model.addPerson(target);
            initializeMainWindow(model);
        });
        awaitLayout();
        runOnFxThread(() -> {
            TextField command = (TextField) stage.getScene().lookup("#commandTextField");
            command.requestFocus();
            submitCommand("select 41");
        });
        awaitLayout();
        runOnFxThread(() -> {
            assertSelected(target);
            assertContactRowVisible(target);
            ScrollPane details = (ScrollPane) stage.getScene().lookup("#detailsScrollPane");
            details.setVvalue(1);
            contactList().scrollTo(0);
        });
        awaitLayout();
        runOnFxThread(() -> {
            assertSelected(target);
            submitCommand("select 41");
        });
        awaitLayout();
        runOnFxThread(() -> {
            assertSelected(target);
            assertContactRowVisible(target);
            ScrollPane details = (ScrollPane) stage.getScene().lookup("#detailsScrollPane");
            assertEquals(1, details.getVvalue(), 0.001);
            assertSame(stage.getScene().lookup("#commandTextField"), stage.getScene().getFocusOwner());

            // A freshly appended row has not yet been measured by the list's virtual layout.
            submitCommand("add n/Appended Supplier p/81000998 e/appended@example.com a/Another supplier address "
                    + "r/Supplier t/" + "Y".repeat(50) + " t/lube t/switches");
            assertSelected(target);
            submitCommand("select 42");
        });
        awaitLayout();
        runOnFxThread(() -> {
            Person appended = (Person) contactList().getItems().get(41);
            assertEquals("Appended Supplier", appended.getName().fullName);
            assertSelected(appended);
            assertContactRowVisible(appended);
        });
    }

    private void assertContactRowVisible(Person person) {
        Bounds row = contactCell(person).localToScene(contactCell(person).getLayoutBounds());
        Bounds list = contactList().localToScene(contactList().getLayoutBounds());
        assertTrue(row.getMinY() >= list.getMinY() - 1, "The selected row should not be clipped at the top");
        assertTrue(row.getMaxY() <= list.getMaxY() + 1, "The selected row should not be clipped at the bottom");
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
    public void cards_summaryKeepsFullDetailsAndSelectionStylingWhenCommandBoxFocused() throws Exception {
        Person person = new PersonBuilder(ALICE).withTags("switches", "lube")
                .withFollowUp("2026-11-01", "Confirm switch quotation").build();
        runOnFxThread(() -> {
            ModelManager model = new ModelManager();
            model.addPerson(person);
            model.addPerson(BENSON);
            initializeMainWindow(model);
        });
        awaitLayout();
        runOnFxThread(() -> {
            contactList().getSelectionModel().select(0);
            stage.getScene().getRoot().applyCss();
            assertSelected(person);

            ListCell<?> cell = contactCell(person);
            Region card = (Region) cell.getGraphic();
            assertEquals("1. ", ((Label) card.lookup("#id")).getText());
            assertEquals(person.getRole().value, ((Label) card.lookup("#role")).getText());
            assertEquals(person.getPhone().value, ((Label) card.lookup("#phone")).getText());
            FlowPane tags = (FlowPane) card.lookup("#tags");
            assertEquals(List.of("lube", "switches"), tags.getChildren().stream()
                    .map(node -> ((Label) node).getText()).toList());
            assertNull(card.lookup("#email"));
            assertNull(card.lookup("#address"));
            assertNull(card.lookup("#followUpSection"));

            ScrollPane details = (ScrollPane) stage.getScene().lookup("#detailsScrollPane");
            assertTrue(details.getContent().lookup("#followUpSection").isVisible());
            Label description = (Label) details.getContent().lookup("#followUpDescription");
            assertEquals("Confirm switch quotation", description.getText());

            var selectedBackground = cell.getBackground();
            assertFalse(card.getBorder().isEmpty());
            assertEquals(1, card.getBorder().getStrokes().get(0).getWidths().getTop());
            TextField command = (TextField) stage.getScene().lookup("#commandTextField");
            command.requestFocus();
            stage.getScene().getRoot().applyCss();
            assertSame(command, stage.getScene().getFocusOwner());
            assertTrue(cell.isSelected());
            assertEquals(selectedBackground, cell.getBackground());
            assertEquals(1, card.getBorder().getStrokes().get(0).getWidths().getTop());
            assertSelected(person);
        });
    }

    @Test
    public void cards_compactHeightAndEmptyTags_removeUnusedSpace() throws Exception {
        Person withTags = new PersonBuilder(ALICE).withTags("lube", "switches")
                .withFollowUp("2026-11-01", "Confirm switch quotation").build();
        Person withoutTags = new PersonBuilder(BENSON).withTags().build();
        runOnFxThread(() -> {
            ModelManager model = new ModelManager();
            model.addPerson(withTags);
            model.addPerson(withoutTags);
            initializeMainWindow(model);
        });
        awaitLayout();
        runOnFxThread(() -> {
            ListCell<?> tagged = contactCell(withTags);
            ListCell<?> untagged = contactCell(withoutTags);
            assertTrue(tagged.getHeight() < 105, "Summary cards should shrink below the old 105px minimum");
            assertTrue(untagged.getHeight() < tagged.getHeight(), "Empty tags should leave no layout gap");
            FlowPane tags = (FlowPane) untagged.getGraphic().lookup("#tags");
            assertFalse(tags.isVisible());
            assertFalse(tags.isManaged());
        });
    }

    @Test
    public void cards_longNameAndManyTags_fitListWidthAndKeepNameTooltip() throws Exception {
        String[] tags = IntStream.range(0, 36).mapToObj(i -> "Component " + i).toArray(String[]::new);
        tags[0] = "X".repeat(50);
        Person person = new PersonBuilder(ALICE).withName("Mechanical Keyboard Supplier ".repeat(10).strip())
                .withTags(tags).build();
        runOnFxThread(() -> {
            ModelManager model = new ModelManager();
            model.addPerson(person);
            initializeMainWindow(model);
        });
        awaitLayout();
        runOnFxThread(() -> {
            Region card = (Region) contactCell(person).getGraphic();
            Bounds bounds = card.localToScene(card.getLayoutBounds());
            for (Node node : card.lookupAll(".label")) {
                Bounds labelBounds = node.localToScene(node.getLayoutBounds());
                assertTrue(labelBounds.getMinX() >= bounds.getMinX());
                assertTrue(labelBounds.getMaxX() <= bounds.getMaxX() + 1, "Card text should fit the list width");
            }
            Label name = (Label) card.lookup("#name");
            assertEquals(person.getName().fullName, name.getTooltip().getText());
            FlowPane flow = (FlowPane) card.lookup("#tags");
            assertEquals(36, flow.getChildren().size());
            Label longTag = (Label) flow.getChildren().stream()
                    .filter(node -> ((Label) node).getText().equals("X".repeat(50))).findFirst().orElseThrow();
            assertTrue(longTag.getHeight() > longTag.getFont().getSize() * 1.5);
            for (Node node : contactList().lookupAll(".scroll-bar")) {
                ScrollBar bar = (ScrollBar) node;
                if (bar.getOrientation() == Orientation.HORIZONTAL) {
                    assertFalse(bar.isVisible(), "Summary cards should not widen the contact list");
                }
            }
            assertContactPanesFit();
        });
    }

    @Test
    public void keyboard_escapeReturnsToCommandInput_preservesDraftSelectionAndContact() throws Exception {
        runOnFxThread(this::initializeContactWindow);
        awaitLayout();
        runOnFxThread(() -> {
            TextField command = (TextField) stage.getScene().lookup("#commandTextField");
            command.requestFocus();
            command.setText("edit 1 n/Draft Supplier");
            command.selectRange(2, 6);
            contactList().getSelectionModel().select(0);
            contactList().requestFocus();
            contactList().fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.DOWN,
                    false, false, false, false));
            assertSame(contactList(), stage.getScene().getFocusOwner());
            assertSelected(BENSON);
            contactList().fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.ESCAPE,
                    false, false, false, false));
            assertSame(command, stage.getScene().getFocusOwner());
            assertEquals("edit 1 n/Draft Supplier", command.getText());
            assertEquals(2, command.getAnchor());
            assertEquals(6, command.getCaretPosition());
            assertSelected(BENSON);

            for (String id : List.of("detailsScrollPane", "resultDisplay")) {
                Node source = stage.getScene().lookup("#" + id);
                source.requestFocus();
                source.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.ESCAPE,
                        false, false, false, false));
                assertSame(command, stage.getScene().getFocusOwner());
                assertEquals("edit 1 n/Draft Supplier", command.getText());
                assertSelected(BENSON);
            }
        });
        awaitLayout();
        runOnFxThread(() -> {
            TextField command = (TextField) stage.getScene().lookup("#commandTextField");
            assertSame(command, stage.getScene().getFocusOwner());
            assertEquals(2, command.getAnchor());
            assertEquals(6, command.getCaretPosition());
        });
    }

    private Person initializeLongContentWindow() {
        String[] tags = IntStream.range(0, 36).mapToObj(i -> "Component " + i).toArray(String[]::new);
        tags[0] = "X".repeat(50);
        Person person = new PersonBuilder(ALICE).withName("Mechanical Keyboard Supplier ".repeat(10).strip())
                .withRole("Keyboard component supplier ".repeat(8).strip())
                .withEmail("supplier".repeat(18) + "@keyboard.example.com")
                .withAddress("Unit 08 Artisan Keyboard Business Park, ".repeat(16)).withTags(tags)
                .withFollowUp("2026-11-01", "Confirm switch order and delivery date ".repeat(5).strip()).build();
        ModelManager model = new ModelManager();
        model.addPerson(person);
        for (int i = 0; i < 40; i++) {
            model.addPerson(new PersonBuilder().withName("Supplier " + i).withPhone("81000" + (100 + i))
                    .withEmail("supplier" + i + "@example.com").build());
        }
        initializeMainWindow(model);
        contactList().getSelectionModel().select(0);
        return person;
    }

    private ScrollBar scrollBar(Node control, Orientation orientation) {
        return (ScrollBar) control.lookupAll(".scroll-bar").stream()
                .filter(node -> node instanceof ScrollBar bar && bar.getOrientation() == orientation)
                .findFirst().orElseThrow();
    }

    private void scrollDown(Node target) {
        Bounds bounds = target.localToScene(target.getLayoutBounds());
        double x = bounds.getCenterX();
        double y = bounds.getCenterY();
        target.fireEvent(new ScrollEvent(ScrollEvent.SCROLL, x, y, x, y,
                false, false, false, false, false, false, 0, -180, 0, -180,
                ScrollEvent.HorizontalTextScrollUnits.NONE, 0,
                ScrollEvent.VerticalTextScrollUnits.LINES, -6, 0, new PickResult(target, x, y)));
    }

    @Test
    public void scrolling_listAndDetails_scrollIndependentlyAtMinimumSize() throws Exception {
        double[] positions = new double[2];
        runOnFxThread(this::initializeLongContentWindow);
        awaitLayout();
        runOnFxThread(() -> {
            ScrollPane details = (ScrollPane) stage.getScene().lookup("#detailsScrollPane");
            assertTrue(scrollBar(details, Orientation.VERTICAL).isVisible());
            assertTrue(scrollBar(contactList(), Orientation.VERTICAL).isVisible());
            positions[0] = scrollBar(contactList(), Orientation.VERTICAL).getValue();
            scrollDown(details.lookup(".viewport"));
        });
        awaitLayout();
        runOnFxThread(() -> {
            ScrollPane details = (ScrollPane) stage.getScene().lookup("#detailsScrollPane");
            assertTrue(details.getVvalue() > 0, "Mouse-wheel input should scroll details");
            assertEquals(positions[0], scrollBar(contactList(), Orientation.VERTICAL).getValue(), 0.001);
            positions[1] = details.getVvalue();
            scrollDown(contactList().lookup(".virtual-flow"));
        });
        awaitLayout();
        runOnFxThread(() -> {
            ScrollPane details = (ScrollPane) stage.getScene().lookup("#detailsScrollPane");
            assertTrue(scrollBar(contactList(), Orientation.VERTICAL).getValue() > positions[0],
                    "Mouse-wheel input should scroll the contact list");
            assertEquals(positions[1], details.getVvalue(), 0.001);
            assertSame(contactList().getItems().get(0), mainWindow.getPersonListPanel().selectedPersonProperty().get());
            details.setVvalue(1);
        });
        awaitLayout();
        runOnFxThread(() -> {
            ScrollPane details = (ScrollPane) stage.getScene().lookup("#detailsScrollPane");
            Node viewport = details.lookup(".viewport");
            Node lastField = details.getContent().lookup("#followUpDescription");
            Bounds viewportBounds = viewport.localToScene(viewport.getLayoutBounds());
            Bounds lastBounds = lastField.localToScene(lastField.getLayoutBounds());
            assertTrue(lastBounds.getMinY() >= viewportBounds.getMinY() - 1);
            assertTrue(lastBounds.getMaxY() <= viewportBounds.getMaxY() + 1,
                    "The bottom of long details should remain accessible");
        });
    }

    @Test
    public void layout_longContentAtDividerLimits_fitsAfterResizing() throws Exception {
        runOnFxThread(this::initializeLongContentWindow);
        awaitLayout();
        for (double width : new double[] {1100, 800}) {
            for (double position : new double[] {0, 1}) {
                runOnFxThread(() -> {
                    stage.setWidth(width);
                    SplitPane split = (SplitPane) stage.getScene().lookup("#contactSplitPane");
                    split.setDividerPositions(position);
                });
                awaitLayout();
                runOnFxThread(() -> {
                    assertContactPanesFit();
                    ScrollPane details = (ScrollPane) stage.getScene().lookup("#detailsScrollPane");
                    assertFalse(scrollBar(contactList(), Orientation.HORIZONTAL).isVisible());
                    assertFalse(scrollBar(details, Orientation.HORIZONTAL).isVisible());
                    Bounds contentBounds = details.getContent().localToScene(details.getContent().getLayoutBounds());
                    for (Node node : details.getContent().lookupAll(".label")) {
                        Bounds bounds = node.localToScene(node.getLayoutBounds());
                        assertTrue(bounds.getMinX() >= contentBounds.getMinX() - 1);
                        assertTrue(bounds.getMaxX() <= contentBounds.getMaxX() + 1);
                    }
                });
            }
        }
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
