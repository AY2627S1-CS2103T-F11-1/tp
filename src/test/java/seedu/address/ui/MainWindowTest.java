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

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import javax.imageio.ImageIO;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollToEvent;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
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
import seedu.address.testutil.MutableClock;
import seedu.address.testutil.PersonBuilder;

public class MainWindowTest {
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-28T16:30:00Z"),
            ZoneId.of("Asia/Singapore"));

    @TempDir
    public Path testFolder;

    private Stage stage;
    private MainWindow mainWindow;
    private StringBuilder scrollTrace;

    // Temporary Windows diagnostics: capture before @AfterEach hides the failed window.
    @RegisterExtension
    private final AfterTestExecutionCallback captureScrollDiagnostics = context -> {
        if (scrollTrace != null) {
            try {
                runOnFxThread(() -> saveScrollDiagnostics(context.getExecutionException().isPresent()));
            } catch (Exception e) {
                System.err.println("Unable to capture temporary UI diagnostics: " + e);
            }
        }
    };

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

    private void saveScrollDiagnostics(boolean failed) {
        try {
            Path directory = Path.of("build", "ui-diagnostics", "select-new-contact");
            Files.createDirectories(directory);
            mainWindow.getPersonListPanel().recordScrollDiagnostics(failed ? "test-failed" : "test-passed");
            scrollTrace.append("window=").append(stage.getWidth()).append('x').append(stage.getHeight())
                    .append(" scale=").append(stage.getOutputScaleX()).append('x').append(stage.getOutputScaleY())
                    .append('\n');
            Files.writeString(directory.resolve("scroll-trace.txt"), scrollTrace.toString());
            if (failed) {
                WritableImage snapshot = stage.getScene().snapshot(null);
                int width = (int) snapshot.getWidth();
                int height = (int) snapshot.getHeight();
                int[] pixels = new int[width * height];
                snapshot.getPixelReader().getPixels(0, 0, width, height, PixelFormat.getIntArgbInstance(),
                        pixels, 0, width);
                BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                image.setRGB(0, 0, width, height, pixels, 0, width);
                ImageIO.write(image, "png", directory.resolve("failure.png").toFile());
            }
            System.out.println("Temporary UI diagnostics: " + directory.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("Unable to write temporary UI diagnostics: " + e);
        }
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
        initializeMainWindow(model, addressBookStorage, new UiDateTracker(CLOCK));
    }

    /**
     * Creates and shows a window with an injectable date tracker for date-transition tests.
     */
    private void initializeMainWindow(ModelManager model, JsonAddressBookStorage addressBookStorage,
            UiDateTracker uiDate) {
        createMainWindow(model, addressBookStorage, uiDate);
        mainWindow.show();
    }

    /**
     * Creates a window without showing it, allowing requested sizes to be checked before the OS adjusts them.
     */
    private void createMainWindow(ModelManager model, JsonAddressBookStorage addressBookStorage) {
        createMainWindow(model, addressBookStorage, new UiDateTracker(CLOCK));
    }

    /**
     * Creates an unshown window with the given date tracker, retaining deterministic date checks.
     */
    private void createMainWindow(ModelManager model, JsonAddressBookStorage addressBookStorage, UiDateTracker uiDate) {
        Path addressBookPath = addressBookStorage.getAddressBookFilePath();
        StorageManager storage = new StorageManager(
                addressBookStorage,
                new JsonUserPrefsStorage(testFolder.resolve("preferences.json"))
        );
        Logic logic = new LogicManager(model, storage);

        stage = new Stage();
        mainWindow = new MainWindow(stage, logic, addressBookPath, null, uiDate);
        mainWindow.fillInnerParts();
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
        assertTrue(stage.getWidth() >= stage.getMinWidth() - 1,
                "Window width " + stage.getWidth() + " is below minimum " + stage.getMinWidth());
        assertTrue(stage.getHeight() >= stage.getMinHeight() - 1,
                "Window height " + stage.getHeight() + " is below minimum " + stage.getMinHeight());
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
        runOnFxThread(() -> {
            createMainWindow(new ModelManager(), new JsonAddressBookStorage(testFolder.resolve("addressbook.json")));
            assertEquals(800, stage.getWidth(), 1);
            mainWindow.show();
        });
        awaitLayout();

        runOnFxThread(() -> {
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
    public void layout_savedWindowSize_requestsClampedOrPreservedDimensionsBeforeShowing() throws Exception {
        runOnFxThread(() -> {
            ModelManager model = new ModelManager();
            model.setGuiSettings(new GuiSettings(450, 400, 0, 0));
            createMainWindow(model, new JsonAddressBookStorage(testFolder.resolve("addressbook.json")));
            assertFalse(stage.isShowing());
            assertEquals(800, stage.getWidth(), 0.001);
            assertEquals(600, stage.getHeight(), 0.001);

            model.setGuiSettings(new GuiSettings(1100, 720, 0, 0));
            createMainWindow(model, new JsonAddressBookStorage(testFolder.resolve("addressbook.json")));
            assertFalse(stage.isShowing());
            assertEquals(1100, stage.getWidth(), 0.001);
            assertEquals(720, stage.getHeight(), 0.001);
        });
    }

    @Test
    public void layout_savedWindowSize_keepsPanesUsableAfterShowing() throws Exception {
        for (GuiSettings settings : List.of(new GuiSettings(450, 400, 0, 0), new GuiSettings(1100, 720, 0, 0))) {
            runOnFxThread(() -> {
                ModelManager model = new ModelManager();
                model.setGuiSettings(settings);
                initializeMainWindow(model);
            });
            awaitLayout();
            runOnFxThread(() -> {
                // Native window managers may constrain the requested size to the runner's desktop.
                assertContactPanesFit();
                stage.hide();
            });
        }
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
        ListCell<?> cell = findContactCell(person);
        if (cell == null) {
            throw new AssertionError(contactRowDiagnostics(person));
        }
        return cell;
    }

    private ListCell<?> findContactCell(Person person) {
        VirtualFlow<?> flow = (VirtualFlow<?>) contactList().lookup(".virtual-flow");
        int index = contactList().getItems().indexOf(person);
        if (flow != null && index >= 0 && flow.getVisibleCell(index) instanceof ListCell<?> cell
                && cell.getItem() == person && cell.isVisible()) {
            return cell;
        }
        return null;
    }

    private Bounds contactViewportBounds() {
        Node viewport = contactList().lookup(".clipped-container");
        return viewport.localToScene(viewport.getLayoutBounds());
    }

    /**
     * Waits for the requested row to fit in the list for two consecutive frames, without issuing another scroll.
     */
    private void awaitContactRowVisible(int index, String operation) throws Exception {
        CompletableFuture<String> result = new CompletableFuture<>();
        AnimationTimer[] timer = new AnimationTimer[1];
        runOnFxThread(() -> {
            Person person = (Person) contactList().getItems().get(index);
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
            timer[0] = new AnimationTimer() {
                private int visibleFrames;

                @Override
                public void handle(long now) {
                    ListCell<?> cell = findContactCell(person);
                    Bounds listBounds = contactViewportBounds();
                    Bounds rowBounds = cell == null ? null : cell.localToScene(cell.getLayoutBounds());
                    boolean visible = rowBounds != null && cell.getHeight() > 0
                            && rowBounds.getMinY() >= listBounds.getMinY() - 1
                            && rowBounds.getMaxY() <= listBounds.getMaxY() + 1;
                    visibleFrames = visible ? visibleFrames + 1 : 0;
                    if (visibleFrames >= 2) {
                        stop();
                        result.complete(null);
                    } else if (System.nanoTime() >= deadline) {
                        stop();
                        String diagnostics = contactRowDiagnostics(person);
                        if (scrollTrace != null) {
                            mainWindow.getPersonListPanel().recordScrollDiagnostics("visibility-wait-timeout");
                            String trace = scrollTrace.toString();
                            diagnostics += "\nRecent scroll diagnostics:\n"
                                    + trace.substring(Math.max(0, trace.length() - 8000));
                        }
                        result.complete("Timed out " + operation + ": " + diagnostics);
                    }
                }
            };
            timer[0].start();
        });
        try {
            String failure = result.get(12, TimeUnit.SECONDS);
            assertNull(failure, failure);
        } finally {
            runOnFxThread(() -> timer[0].stop());
        }
    }

    private String contactRowDiagnostics(Person person) {
        List<String> rows = contactList().lookupAll(".list-cell").stream()
                .filter(node -> node instanceof ListCell<?> cell && cell.isVisible() && !cell.isEmpty())
                .map(node -> {
                    ListCell<?> cell = (ListCell<?>) node;
                    Bounds bounds = cell.localToScene(cell.getLayoutBounds());
                    return cell.getIndex() + " at y=" + bounds.getMinY() + ".." + bounds.getMaxY();
                }).toList();
        return "Expected visible contact '" + person.getName().fullName + "', target index="
                + contactList().getItems().indexOf(person) + ", selected index="
                + contactList().getSelectionModel().getSelectedIndex() + ", window="
                + stage.getWidth() + "x" + stage.getHeight() + ", list bounds="
                + contactList().localToScene(contactList().getLayoutBounds()) + ", visible rows=" + rows;
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
    public void helpSelect_displaysUsageAndKeepsSelectedContactAndInputFocus() throws Exception {
        runOnFxThread(() -> {
            initializeContactWindow();
            TextField command = (TextField) stage.getScene().lookup("#commandTextField");
            command.requestFocus();
            submitCommand("select 2");
            submitCommand("help select");
            assertSelected(BENSON);
            TextArea feedback = (TextArea) stage.getScene().lookup("#resultDisplay");
            assertEquals(SelectCommand.COMMAND_USAGE, feedback.getText());
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
        scrollTrace = new StringBuilder("Temporary select/new-row diagnostics\n")
                .append("os=").append(System.getProperty("os.name")).append(' ')
                .append(System.getProperty("os.version")).append(' ').append(System.getProperty("os.arch"))
                .append("\njava=").append(System.getProperty("java.version"))
                .append("\njavafx=").append(System.getProperty("javafx.runtime.version")).append('\n');
        boolean[] newRowLaidOut = {false};
        int[] earlyScrollRequests = {0};
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
            mainWindow.getPersonListPanel().setScrollDiagnostics(message -> scrollTrace.append(message).append('\n'));
            contactList().addEventFilter(ScrollToEvent.scrollToTopIndex(), event ->
                    mainWindow.getPersonListPanel().recordScrollDiagnostics("scroll-event target="
                            + event.getScrollTarget() + " consumed=" + event.isConsumed()));
        });
        awaitLayout();
        runOnFxThread(() -> {
            TextField command = (TextField) stage.getScene().lookup("#commandTextField");
            command.requestFocus();
            submitCommand("select 41");
        });
        awaitContactRowVisible(40, "after selecting the off-screen contact");
        runOnFxThread(() -> {
            assertSelected(target);
            assertContactRowVisible(target);
            ScrollPane details = (ScrollPane) stage.getScene().lookup("#detailsScrollPane");
            details.setVvalue(1);
            contactList().scrollTo(0);
        });
        awaitContactRowVisible(0, "after scrolling away from the selected contact");
        runOnFxThread(() -> {
            assertSelected(target);
            submitCommand("select 41");
        });
        awaitContactRowVisible(40, "after repeating selection of the off-screen contact");
        runOnFxThread(() -> {
            assertSelected(target);
            assertContactRowVisible(target);
            ScrollPane details = (ScrollPane) stage.getScene().lookup("#detailsScrollPane");
            assertEquals(1, details.getVvalue(), 0.001);
            assertSame(stage.getScene().lookup("#commandTextField"), stage.getScene().getFocusOwner());

            // A freshly appended row has not yet been measured by the list's virtual layout.
            // Simulate the native list dropping scroll requests made before that layout completes.
            stage.getScene().addPostLayoutPulseListener(new Runnable() {
                @Override
                public void run() {
                    newRowLaidOut[0] = true;
                    mainWindow.getPersonListPanel().recordScrollDiagnostics("test-first-post-layout-marker");
                    stage.getScene().removePostLayoutPulseListener(this);
                }
            });
            contactList().addEventFilter(ScrollToEvent.scrollToTopIndex(), event -> {
                if (event.getScrollTarget() == 41 && !newRowLaidOut[0]) {
                    earlyScrollRequests[0]++;
                    mainWindow.getPersonListPanel().recordScrollDiagnostics("test-drop-early-scroll target=41");
                    event.consume();
                }
            });
            submitCommand("add n/Appended Supplier p/81000998 e/appended@example.com a/Another supplier address "
                    + "r/Supplier t/" + "Y".repeat(50) + " t/lube t/switches");
            assertSelected(target);
            submitCommand("select 42");
        });
        awaitContactRowVisible(41, "after adding and immediately selecting a contact");
        runOnFxThread(() -> {
            Person appended = (Person) contactList().getItems().get(41);
            assertEquals("Appended Supplier", appended.getName().fullName);
            assertTrue(earlyScrollRequests[0] > 0, "The regression should discard an early scroll request");
            assertSelected(appended);
            assertContactRowVisible(appended);
        });
    }

    @Test
    public void deferredScrolling_droppedRequests_revealsContactAndStopsRetrying() throws Exception {
        int[] droppedRequests = {0};
        runOnFxThread(() -> {
            ModelManager model = new ModelManager();
            for (int i = 0; i < 40; i++) {
                model.addPerson(new PersonBuilder().withName("Supplier " + i).withPhone("81000" + (100 + i))
                        .withEmail("supplier" + i + "@example.com").build());
            }
            initializeMainWindow(model);
            contactList().addEventFilter(ScrollToEvent.scrollToTopIndex(), event -> {
                if (event.getScrollTarget() == 39) {
                    droppedRequests[0]++;
                    event.consume();
                }
            });
        });
        awaitContactRowVisible(0, "before checking dropped native scroll requests");
        runOnFxThread(() -> submitCommand("select 40"));
        awaitContactRowVisible(39, "after the native index-scroll requests were dropped");
        runOnFxThread(() -> {
            assertTrue(droppedRequests[0] > 0);
            assertSelected((Person) contactList().getItems().get(39));
            assertContactRowVisible((Person) contactList().getItems().get(39));
            contactList().scrollTo(0);
        });
        awaitContactRowVisible(0, "after scrolling away from the completed selection request");
        awaitLayout();
        runOnFxThread(() -> assertContactRowVisible((Person) contactList().getItems().getFirst()));
    }

    @Test
    public void deferredScrolling_transientVisibility_checksAgainBeforeStopping() throws Exception {
        boolean[] shifted = {false};
        runOnFxThread(() -> {
            ModelManager model = new ModelManager();
            for (int i = 0; i < 40; i++) {
                model.addPerson(new PersonBuilder().withName("Supplier " + i).withPhone("81000" + (100 + i))
                        .withEmail("supplier" + i + "@example.com").build());
            }
            initializeMainWindow(model);
        });
        awaitContactRowVisible(0, "before checking transient row visibility");
        runOnFxThread(() -> {
            submitCommand("select 40");
            // Run after the request's first visibility check, simulating a subsequent native layout correction.
            stage.getScene().addPostLayoutPulseListener(new Runnable() {
                @Override
                public void run() {
                    shifted[0] = true;
                    contactList().scrollTo(0);
                    stage.getScene().removePostLayoutPulseListener(this);
                }
            });
        });
        awaitContactRowVisible(39, "after a native layout correction moved the selected row away");
        runOnFxThread(() -> {
            assertTrue(shifted[0]);
            assertSelected((Person) contactList().getItems().get(39));
            assertContactRowVisible((Person) contactList().getItems().get(39));
        });
    }

    @Test
    public void deferredScrolling_laterCommandsSelectionAndManualScrolling_cancelOldRequests() throws Exception {
        Person[] last = new Person[1];
        int[] scrollRequests = {0};
        int[] requestsBeforeLayout = {0};
        runOnFxThread(() -> {
            ModelManager model = new ModelManager();
            model.addPerson(ALICE);
            for (int i = 1; i < 40; i++) {
                last[0] = new PersonBuilder().withName("Supplier " + i).withPhone("81000" + (100 + i))
                        .withEmail("supplier" + i + "@example.com").build();
                model.addPerson(last[0]);
            }
            initializeMainWindow(model);
            contactList().addEventFilter(ScrollToEvent.scrollToTopIndex(), event -> {
                if (event.getScrollTarget() == 39) {
                    scrollRequests[0]++;
                    event.consume();
                }
            });
        });
        awaitContactRowVisible(0, "before checking cancellation of delayed scrolling");
        for (String action : List.of("command", "selection", "scroll")) {
            runOnFxThread(() -> {
                submitCommand("select 40");
                assertSelected(last[0]);
                if (action.equals("command")) {
                    submitCommand("select 1");
                } else if (action.equals("selection")) {
                    contactList().getSelectionModel().select(0);
                } else {
                    scrollDown(contactList().lookup(".virtual-flow"));
                }
                requestsBeforeLayout[0] = scrollRequests[0];
            });
            awaitLayout();
            awaitLayout();
            runOnFxThread(() -> {
                assertEquals(requestsBeforeLayout[0], scrollRequests[0],
                        "An old delayed scroll should not run after a later " + action);
                if (action.equals("scroll")) {
                    assertSelected(last[0]);
                    assertTrue(scrollBar(contactList(), Orientation.VERTICAL).getValue() > 0,
                            "Manual scrolling should be preserved");
                } else {
                    assertSelected(ALICE);
                    assertContactRowVisible(ALICE);
                }
            });
        }
    }

    private void assertContactRowVisible(Person person) {
        Bounds row = contactCell(person).localToScene(contactCell(person).getLayoutBounds());
        Bounds list = contactViewportBounds();
        assertTrue(row.getMinY() >= list.getMinY() - 1,
                "The selected row should not be clipped at the top: " + contactRowDiagnostics(person));
        assertTrue(row.getMaxY() <= list.getMaxY() + 1,
                "The selected row should not be clipped at the bottom: " + contactRowDiagnostics(person));
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
            assertFollowUpBadge(person);
            Label email = (Label) card.lookup("#email");
            assertEquals(person.getEmail().value, email.getText());
            assertEquals(person.getEmail().value, email.getTooltip().getText());
            FlowPane tags = (FlowPane) card.lookup("#tags");
            assertEquals(List.of("lube", "switches"), tags.getChildren().stream()
                    .map(node -> ((Label) node).getText()).toList());
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
        Person withTags = new PersonBuilder(ALICE).withTags("lube", "switches").build();
        Person withoutTags = new PersonBuilder(withTags).withName("Another Contact").withPhone("81000901")
                .withEmail("another@example.com").withTags().build();
        runOnFxThread(() -> {
            ModelManager model = new ModelManager();
            model.addPerson(withTags);
            model.addPerson(withoutTags);
            initializeMainWindow(model);
        });
        awaitLayout();
        for (int fontSize : new int[] {13, 19}) {
            runOnFxThread(() -> {
                for (Person person : List.of(withTags, withoutTags)) {
                    for (Node label : contactCell(person).getGraphic().lookupAll(".cell_small_label")) {
                        label.setStyle("-fx-font-size: " + fontSize + "px;");
                    }
                }
            });
            awaitLayout();
            runOnFxThread(() -> {
                ListCell<?> tagged = contactCell(withTags);
                ListCell<?> untagged = contactCell(withoutTags);
                FlowPane shownTags = (FlowPane) tagged.getGraphic().lookup("#tags");
                VBox content = (VBox) shownTags.getParent();
                assertTrue(shownTags.isVisible());
                assertTrue(shownTags.isManaged());
                assertTrue(shownTags.getHeight() > 0);
                assertEquals(shownTags.getHeight() + content.getSpacing(), tagged.getHeight() - untagged.getHeight(), 1,
                        "Only the tag row and its spacing should add height, independent of font metrics");
                FlowPane tags = (FlowPane) untagged.getGraphic().lookup("#tags");
                assertFalse(tags.isVisible());
                assertFalse(tags.isManaged());
                Label email = (Label) untagged.getGraphic().lookup("#email");
                Region card = (Region) untagged.getGraphic();
                VBox untaggedContent = (VBox) email.getParent();
                double bottomGap = card.localToScene(card.getLayoutBounds()).getMaxY()
                        - email.localToScene(email.getLayoutBounds()).getMaxY();
                assertEquals(untaggedContent.getPadding().getBottom() + card.getInsets().getBottom(), bottomGap, 1,
                        "No blank tag row or fixed minimum height should remain below the email");
                assertFollowUpBadge(withoutTags);
            });
        }
    }

    private void assertFollowUpBadge(Person person) {
        assertFollowUpBadge(person, LocalDate.now(CLOCK));
    }

    private void assertFollowUpBadge(Person person, LocalDate today) {
        Region card = (Region) contactCell(person).getGraphic();
        Label badge = (Label) card.lookup("#followUpBadge");
        boolean hasFollowUp = person.getFollowUp().isPresent();
        assertEquals(hasFollowUp, badge.isVisible());
        assertEquals(hasFollowUp, badge.isManaged());
        boolean overdue = person.getFollowUp().map(followUp -> followUp.getDueDate().isBefore(today)).orElse(false);
        boolean dueToday = person.getFollowUp().map(followUp -> followUp.getDueDate().equals(today)).orElse(false);
        assertEquals(overdue ? "Overdue" : dueToday ? "Due today" : "Follow-up", badge.getText());
        assertEquals(overdue, badge.getPseudoClassStates().contains(PseudoClass.getPseudoClass("overdue")));
        assertEquals(dueToday, badge.getPseudoClassStates().contains(PseudoClass.getPseudoClass("due-today")));
        person.getFollowUp().ifPresent(followUp -> {
            List<String> labels = card.lookupAll(".label").stream()
                    .map(node -> ((Label) node).getText()).toList();
            assertFalse(labels.contains(followUp.getDueDate().toString()), "Cards should show only the indicator");
            assertFalse(labels.contains(followUp.getDescription()), "The description belongs in the details panel");
        });
    }

    private void assertBadgeTextFits(Label badge) {
        Text rendered = (Text) badge.lookup(".text");
        String dimensions = "font=" + badge.getFont() + ", width=" + badge.getWidth()
                + ", height=" + badge.getHeight() + ", insets=" + badge.getInsets()
                + ", labelPadding=" + badge.getLabelPadding();
        assertEquals(badge.getText(), rendered.getText(), "The full badge text should be visible: " + dimensions);
        double availableWidth = badge.getWidth() - badge.getInsets().getLeft() - badge.getInsets().getRight()
                - badge.getLabelPadding().getLeft() - badge.getLabelPadding().getRight();
        double availableHeight = badge.getHeight() - badge.getInsets().getTop() - badge.getInsets().getBottom()
                - badge.getLabelPadding().getTop() - badge.getLabelPadding().getBottom();
        assertTrue(rendered.getLayoutBounds().getWidth() <= availableWidth + 1, dimensions);
        assertTrue(rendered.getLayoutBounds().getHeight() <= availableHeight + 1, dimensions);
    }

    @Test
    public void cards_followUpCommandsAndUndoRedo_refreshBadgeWithoutLeavingSpace() throws Exception {
        double[] originalHeight = new double[1];
        runOnFxThread(() -> {
            initializeContactWindow().commitAddressBook();
            TextField command = (TextField) stage.getScene().lookup("#commandTextField");
            command.requestFocus();
        });
        awaitLayout();
        runOnFxThread(() -> {
            assertFollowUpBadge(ALICE);
            originalHeight[0] = contactCell(ALICE).getHeight();
        });

        String[] commands = {
            "followup 1 by/2026-10-28 abt/Confirm switches",
            "followup 1 by/2026-10-29 abt/Confirm delivery",
            "edit 1 r/Employee",
            "undo",
            "redo",
            "followup 1 clear",
            "undo",
            "redo"
        };
        boolean[] hasFollowUp = {true, true, true, true, true, false, true, false};
        for (int i = 0; i < commands.length; i++) {
            String command = commands[i];
            boolean expected = hasFollowUp[i];
            runOnFxThread(() -> submitCommand(command));
            awaitLayout();
            runOnFxThread(() -> {
                Person person = (Person) contactList().getItems().get(0);
                assertEquals(expected, person.getFollowUp().isPresent());
                assertFollowUpBadge(person);
                assertFollowUpBadge((Person) contactList().getItems().get(1));
                assertSame(stage.getScene().lookup("#commandTextField"), stage.getScene().getFocusOwner());
                if (!expected) {
                    assertEquals(originalHeight[0], contactCell(person).getHeight(), 1,
                            "Clearing the follow-up should restore the card's original height");
                }
            });
        }
    }

    @Test
    public void cards_followUpBadgeAndLongRole_fitAtDividerLimits() throws Exception {
        Person person = new PersonBuilder(ALICE).withRole("Keyboard component supplier ".repeat(8).strip())
                .withTags().withFollowUp("2026-10-29", "Confirm switch shipment").build();
        runOnFxThread(() -> {
            ModelManager model = new ModelManager();
            model.addPerson(person);
            initializeMainWindow(model);
        });
        for (double width : new double[] {800, 1100, 800}) {
            for (double position : new double[] {0, 1}) {
                runOnFxThread(() -> {
                    stage.setWidth(width);
                    SplitPane split = (SplitPane) stage.getScene().lookup("#contactSplitPane");
                    split.setDividerPositions(position);
                });
                awaitLayout();
                runOnFxThread(() -> {
                    assertFollowUpBadge(person);
                    Region card = (Region) contactCell(person).getGraphic();
                    Label role = (Label) card.lookup("#role");
                    Label badge = (Label) card.lookup("#followUpBadge");
                    assertBadgeTextFits(badge);
                    assertEquals(person.getRole().value, ((Text) role.lookup(".text")).getText(),
                            "The role should wrap without losing text");
                    Bounds roleBounds = role.localToScene(role.getLayoutBounds());
                    Bounds badgeBounds = badge.localToScene(badge.getLayoutBounds());
                    Bounds cardBounds = card.localToScene(card.getLayoutBounds());
                    Bounds phoneBounds = card.lookup("#phone").localToScene(card.lookup("#phone").getLayoutBounds());
                    assertTrue(roleBounds.getMinX() >= cardBounds.getMinX());
                    assertTrue(roleBounds.getMaxX() <= badgeBounds.getMinX());
                    assertTrue(badgeBounds.getMaxX() <= cardBounds.getMaxX() + 1);
                    assertTrue(phoneBounds.getMinY() >= Math.max(roleBounds.getMaxY(), badgeBounds.getMaxY()));
                    assertFalse(scrollBar(contactList(), Orientation.HORIZONTAL).isVisible());
                    assertContactPanesFit();
                });
            }
        }
    }

    @Test
    public void dateChanges_refreshUrgencyAndDetailsWithoutResettingSelectionScrollOrDraft() throws Exception {
        MutableClock clock = new MutableClock(CLOCK.instant(), CLOCK.getZone());
        UiDateTracker[] date = new UiDateTracker[1];
        Region[] originalCard = new Region[1];
        double[] positions = new double[4];
        Person target = new PersonBuilder(ALICE).withAddress("Artisan Keyboard Business Park, ".repeat(20))
                .withFollowUp("2026-10-30", "Confirm switch shipment").build();
        runOnFxThread(() -> {
            ModelManager model = new ModelManager();
            for (int i = 0; i < 40; i++) {
                model.addPerson(new PersonBuilder().withName("Supplier " + i).withPhone("81000" + (100 + i))
                        .withEmail("supplier" + i + "@example.com").build());
            }
            model.addPerson(target);
            date[0] = new UiDateTracker(clock);
            initializeMainWindow(model, new JsonAddressBookStorage(testFolder.resolve("addressbook.json")), date[0]);
            submitCommand("select 41");
        });
        awaitLayout();
        runOnFxThread(() -> {
            assertFollowUpBadge(target);
            originalCard[0] = (Region) contactCell(target).getGraphic();
            positions[0] = scrollBar(contactList(), Orientation.VERTICAL).getValue();
            assertTrue(positions[0] > 0);
            ScrollPane details = (ScrollPane) stage.getScene().lookup("#detailsScrollPane");
            details.setVvalue(0.65);
            positions[1] = details.getVvalue();
            positions[2] = contactCell(target).getHeight();
            positions[3] = ((Region) originalCard[0].lookup("#followUpBadge")).getWidth();
            TextField command = (TextField) stage.getScene().lookup("#commandTextField");
            command.requestFocus();
            command.setText("edit 41 n/Draft Supplier");
            command.selectRange(2, 6);
        });
        String[] instants = {"2026-10-29T16:30:00Z", "2026-10-30T16:30:00Z", "2026-10-28T16:30:00Z"};
        String[] badges = {"Due today", "Overdue", "Follow-up"};
        for (int i = 0; i < instants.length; i++) {
            Instant instant = Instant.parse(instants[i]);
            String expectedBadge = badges[i];
            runOnFxThread(() -> {
                clock.setInstant(instant);
                date[0].refresh();
            });
            awaitLayout();
            runOnFxThread(() -> {
                assertSelected(target);
                assertFollowUpBadge(target, LocalDate.now(clock));
                assertSame(originalCard[0], contactCell(target).getGraphic(), "Date refresh should retain the card");
                Label badge = (Label) originalCard[0].lookup("#followUpBadge");
                assertEquals(expectedBadge, badge.getText());
                assertBadgeTextFits(badge);
                assertEquals(positions[3], badge.getWidth(), 0.1, "All urgency states should use the same width");
                assertEquals(positions[2], contactCell(target).getHeight(), 0.1);
                assertEquals(positions[0], scrollBar(contactList(), Orientation.VERTICAL).getValue(), 0.001);
                ScrollPane details = (ScrollPane) stage.getScene().lookup("#detailsScrollPane");
                assertEquals(positions[1], details.getVvalue(), 0.001);
                Label dueDate = (Label) details.getContent().lookup("#followUpDueDate");
                boolean overdue = expectedBadge.equals("Overdue");
                assertEquals((overdue ? "Overdue - due: " : "Due: ") + "2026-10-30", dueDate.getText());
                assertEquals(overdue, dueDate.getPseudoClassStates().contains(PseudoClass.getPseudoClass("overdue")));
                TextField command = (TextField) stage.getScene().lookup("#commandTextField");
                assertSame(command, stage.getScene().getFocusOwner());
                assertEquals("edit 41 n/Draft Supplier", command.getText());
                assertEquals(2, command.getAnchor());
                assertEquals(6, command.getCaretPosition());
            });
        }
    }

    @Test
    public void cards_badgesFitDifferentFontsAndPadding_withoutResizingBetweenStates() throws Exception {
        MutableClock clock = new MutableClock(CLOCK.instant(), CLOCK.getZone());
        UiDateTracker[] date = new UiDateTracker[1];
        double[] dimensions = new double[2];
        Person person = new PersonBuilder(ALICE).withRole("Keyboard component supplier ".repeat(8).strip())
                .withTags().withFollowUp("2026-10-30", "Confirm shipment").build();
        runOnFxThread(() -> {
            ModelManager model = new ModelManager();
            model.addPerson(person);
            date[0] = new UiDateTracker(clock);
            initializeMainWindow(model, new JsonAddressBookStorage(testFolder.resolve("addressbook.json")), date[0]);
        });
        awaitLayout();
        for (String family : List.of("System", "Monospaced", "Serif")) {
            for (int fontSize : new int[] {11, 17}) {
                runOnFxThread(() -> {
                    Label badge = (Label) contactCell(person).getGraphic().lookup("#followUpBadge");
                    badge.setStyle("-fx-font-family: '" + family + "'; -fx-font-size: " + fontSize
                            + "px; -fx-padding: 4 14;");
                    clock.setInstant(CLOCK.instant());
                    date[0].refresh();
                });
                awaitLayout();
                runOnFxThread(() -> {
                    Label badge = (Label) contactCell(person).getGraphic().lookup("#followUpBadge");
                    dimensions[0] = badge.getWidth();
                    dimensions[1] = contactCell(person).getHeight();
                });
                for (String instant : List.of("2026-10-28T16:30:00Z", "2026-10-29T16:30:00Z", "2026-10-30T16:30:00Z")) {
                    runOnFxThread(() -> {
                        clock.setInstant(Instant.parse(instant));
                        date[0].refresh();
                    });
                    awaitLayout();
                    runOnFxThread(() -> {
                        Label badge = (Label) contactCell(person).getGraphic().lookup("#followUpBadge");
                        assertFollowUpBadge(person, LocalDate.now(clock));
                        assertBadgeTextFits(badge);
                        assertEquals(dimensions[0], badge.getWidth(), 0.1, "Badge width should stay stable");
                        assertEquals(dimensions[1], contactCell(person).getHeight(), 0.1,
                                "Card height should stay stable");
                        assertFalse(scrollBar(contactList(), Orientation.HORIZONTAL).isVisible());
                        assertContactPanesFit();
                    });
                }
            }
        }
    }

    @Test
    public void cards_longEmail_staysOnOneLineWithTooltipAtDividerLimits() throws Exception {
        Person person = new PersonBuilder(ALICE).withEmail("supplier".repeat(30) + "@keyboard.example.com")
                .withTags().build();
        runOnFxThread(() -> {
            ModelManager model = new ModelManager();
            model.addPerson(person);
            initializeMainWindow(model);
        });
        for (double width : new double[] {800, 1100, 800}) {
            for (double position : new double[] {0, 1}) {
                runOnFxThread(() -> {
                    stage.setWidth(width);
                    SplitPane split = (SplitPane) stage.getScene().lookup("#contactSplitPane");
                    split.setDividerPositions(position);
                });
                awaitLayout();
                runOnFxThread(() -> {
                    Region card = (Region) contactCell(person).getGraphic();
                    Label phone = (Label) card.lookup("#phone");
                    Label email = (Label) card.lookup("#email");
                    assertEquals(person.getEmail().value, email.getText());
                    assertEquals(person.getEmail().value, email.getTooltip().getText());
                    assertTrue(email.getTooltip().isWrapText());
                    assertFalse(email.isWrapText());
                    Text displayed = (Text) email.lookup(".text");
                    assertTrue(displayed.getText().endsWith(email.getEllipsisString()),
                            "Long emails should visibly end in an ellipsis");
                    assertTrue(email.getHeight() < email.getFont().getSize() * 2,
                            "An email should occupy only one line");
                    Bounds emailBounds = email.localToScene(email.getLayoutBounds());
                    Bounds cardBounds = card.localToScene(card.getLayoutBounds());
                    assertTrue(emailBounds.getMinY() >= phone.localToScene(phone.getLayoutBounds()).getMaxY());
                    assertTrue(emailBounds.getMinX() >= cardBounds.getMinX());
                    assertTrue(emailBounds.getMaxX() <= cardBounds.getMaxX() + 1);
                    assertFalse(scrollBar(contactList(), Orientation.HORIZONTAL).isVisible());
                    assertContactPanesFit();
                });
            }
        }
    }

    @Test
    public void cards_emailEditsAndUndoRedo_updateSummaryAndDetails() throws Exception {
        runOnFxThread(() -> {
            initializeContactWindow().commitAddressBook();
            submitCommand("select 1");
            submitCommand("edit 1 e/newalice@example.com");
        });
        awaitLayout();
        runOnFxThread(() -> {
            Person edited = (Person) contactList().getItems().get(0);
            assertSelected(edited);
            assertEquals("newalice@example.com", ((Label) contactCell(edited).getGraphic().lookup("#email")).getText());
            submitCommand("undo");
        });
        awaitLayout();
        runOnFxThread(() -> {
            assertEquals(ALICE.getEmail().value, ((Label) contactCell(ALICE).getGraphic().lookup("#email")).getText());
            submitCommand("redo");
        });
        awaitLayout();
        runOnFxThread(() -> {
            Person edited = (Person) contactList().getItems().get(0);
            assertEquals("newalice@example.com", ((Label) contactCell(edited).getGraphic().lookup("#email")).getText());
            submitCommand("select 1");
            assertSelected(edited);
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
