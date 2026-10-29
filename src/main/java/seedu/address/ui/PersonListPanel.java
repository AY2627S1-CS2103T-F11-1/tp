package seedu.address.ui;

import java.time.Clock;
import java.util.function.Consumer;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Region;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.person.Person;

/**
 * Panel containing the list of persons.
 */
public class PersonListPanel extends UiPart<Region> {
    private static final String FXML = "PersonListPanel.fxml";
    private static final int MAX_SCROLL_ATTEMPTS = 3;
    private static final int REQUIRED_VISIBLE_LAYOUTS = 2;
    private final Logger logger = LogsCenter.getLogger(PersonListPanel.class);
    private final ReadOnlyObjectWrapper<Person> selectedPerson = new ReadOnlyObjectWrapper<>(this, "selectedPerson");
    private final UiDateTracker uiDate;
    private boolean commandExecuting;
    private ScrollRequest pendingScroll;

    @FXML
    private ListView<Person> personListView;

    /**
     * Creates a {@code PersonListPanel} with the given {@code ObservableList}.
     */
    public PersonListPanel(ObservableList<Person> personList) {
        this(personList, new UiDateTracker(Clock.systemDefaultZone()));
    }

    PersonListPanel(ObservableList<Person> personList, UiDateTracker uiDate) {
        super(FXML);
        this.uiDate = uiDate;
        personListView.setItems(personList);
        personListView.setCellFactory(listView -> new PersonListViewCell());
        personListView.getSelectionModel().selectedItemProperty().addListener((observable, previous, selected) -> {
            recordScrollDiagnostics("native-selection previous=" + identity(previous) + " next=" + identity(selected));
            if (!commandExecuting) {
                cancelPendingScroll("native-selection-change");
                selectedPerson.set(selected);
            }
        });
        // A list change can replace the selected contact or automatically select a neighbour.
        personList.addListener((ListChangeListener<Person>) change -> {
            if (!commandExecuting) {
                cancelPendingScroll();
                personListView.getSelectionModel().clearSelection();
            }
        });
        personListView.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> cancelPendingScroll("mouse-pressed"));
        personListView.addEventFilter(KeyEvent.KEY_PRESSED, event -> cancelPendingScroll("key-pressed"));
        personListView.addEventFilter(ScrollEvent.SCROLL, event -> cancelPendingScroll("wheel-scroll"));
        personListView.sceneProperty().addListener((observable, previous, scene) ->
                cancelPendingScroll("scene-change"));
    }

    /**
     * Returns the read-only selected-contact property. Its value is null when no contact is selected.
     */
    public ReadOnlyObjectProperty<Person> selectedPersonProperty() {
        return selectedPerson.getReadOnlyProperty();
    }

    /**
     * Prevents automatic ListView selection changes from becoming contact selections during a command.
     */
    void beginCommand() {
        cancelPendingScroll();
        commandExecuting = true;
    }

    /**
     * Restores a contact by object identity if it remains visible, or clears selection otherwise.
     * The index is resolved from the current list after all replacements, filtering, and sorting.
     */
    void endCommand(Person personToSelect) {
        int index = indexOf(personToSelect);
        try {
            // Force the native selection model to discard an equal but replaced Person as well.
            personListView.getSelectionModel().clearSelection();
            if (index != -1) {
                personListView.getSelectionModel().select(index);
                // Process newly added rows and selected-card styling before resolving the scroll position.
                personListView.applyCss();
                personListView.layout();
                recordScrollDiagnostics("immediate-scroll-before target=" + index);
                personListView.scrollTo(index);
                recordScrollDiagnostics("immediate-scroll-after target=" + index);
            }
        } finally {
            commandExecuting = false;
        }
        selectedPerson.set(index == -1 ? null : personListView.getItems().get(index));
        recordScrollDiagnostics("selection-published");
        if (index != -1) {
            scrollAfterLayout(personToSelect);
        }
    }

    private int indexOf(Person person) {
        for (int i = 0; i < personListView.getItems().size(); i++) {
            if (personListView.getItems().get(i) == person) {
                return i;
            }
        }
        return -1;
    }

    private void cancelPendingScroll(String reason) {
        if (pendingScroll != null) {
            pendingScroll.cancel(reason);
        }
    }

    private void scrollAfterLayout(Person person) {
        cancelPendingScroll("replacement-request");
        Scene scene = personListView.getScene();
        if (scene == null || scene.getWindow() == null || !scene.getWindow().isShowing()) {
            recordScrollDiagnostics("schedule-skipped-window-not-showing");
            return;
        }
        pendingScroll = new ScrollRequest(person, scene);
        scene.addPostLayoutPulseListener(pendingScroll);
        scene.getWindow().addEventHandler(WindowEvent.WINDOW_HIDDEN, pendingScroll.onHidden);
        recordScrollDiagnostics("request-scheduled id=" + pendingScroll.id);
        Platform.requestNextPulse();
    }

    /** Connects temporary test diagnostics without enabling tracing for normal app use. */
    void setScrollDiagnostics(Consumer<String> diagnostics) {
        scrollDiagnostics = diagnostics;
        recordScrollDiagnostics("diagnostics-enabled");
    }

    /** Records read-only native list state; remove this instrumentation after the Windows investigation. */
    void recordScrollDiagnostics(String event) {
        if (scrollDiagnostics == null) {
            return;
        }
        Node nativeList = personListView.lookup(".virtual-flow");
        String nativeState = nativeList instanceof VirtualFlow<?> flow
                ? "nativeCount=" + flow.getCellCount() + " position=" + flow.getPosition()
                        + " nativeNeedsLayout=" + flow.isNeedsLayout()
                : "native-list-not-created";
        String rows = personListView.lookupAll(".list-cell").stream()
                .filter(node -> node instanceof ListCell<?> cell && cell.isVisible() && !cell.isEmpty())
                .map(node -> {
                    ListCell<?> cell = (ListCell<?>) node;
                    Bounds bounds = cell.localToScene(cell.getLayoutBounds());
                    return cell.getIndex() + ":" + identity((Person) cell.getItem())
                            + "@" + bounds.getMinY() + ".." + bounds.getMaxY();
                }).toList().toString();
        scrollDiagnostics.accept(System.nanoTime() + " " + event + " items=" + personListView.getItems().size()
                + " " + nativeState + " selectedIndex=" + personListView.getSelectionModel().getSelectedIndex()
                + " nativeSelected=" + identity(personListView.getSelectionModel().getSelectedItem())
                + " published=" + identity(selectedPerson.get()) + " commandExecuting=" + commandExecuting
                + " pending=" + (pendingScroll == null ? "none" : pendingScroll.id + "/" + pendingScroll.attempts
                        + "/visible=" + pendingScroll.visibleLayouts)
                + " listNeedsLayout=" + personListView.isNeedsLayout() + " listBounds="
                + personListView.localToScene(personListView.getLayoutBounds()) + " rows=" + rows);
    }

    private static String identity(Person person) {
        return person == null ? "null" : person.getName().fullName + "@"
                + Integer.toHexString(System.identityHashCode(person));
    }

    private boolean isFullyVisible(Person person) {
        VirtualFlow<?> flow = virtualFlow();
        int index = indexOf(person);
        if (flow == null || index == -1) {
            return false;
        }
        // CSS lookup can also find measurement/recycled cells; only inspect the row in the live viewport.
        if (!(flow.getVisibleCell(index) instanceof ListCell<?> cell)
                || cell.getItem() != person || !cell.isVisible() || cell.getHeight() <= 0) {
            return false;
        }
        Node viewport = flow.lookup(".clipped-container");
        if (viewport == null) {
            return false;
        }
        Bounds viewportBounds = viewport.localToScene(viewport.getLayoutBounds());
        Bounds bounds = cell.localToScene(cell.getLayoutBounds());
        return bounds.getMinY() >= viewportBounds.getMinY() - 1
                && bounds.getMaxY() <= viewportBounds.getMaxY() + 1;
    }

    private VirtualFlow<?> virtualFlow() {
        Node node = personListView.lookup(".virtual-flow");
        return node instanceof VirtualFlow<?> flow ? flow : null;
    }

    /** Completes scrolling after the native list has processed item-count and variable-height layout changes. */
    private class ScrollRequest implements Runnable {
        private final long id = ++scrollRequestId;
        private final Person person;
        private final Scene scene;
        private final Window window;
        private final EventHandler<WindowEvent> onHidden = event -> cancel("window-hidden");
        private int attempts;
        private int visibleLayouts;

        ScrollRequest(Person person, Scene scene) {
            this.person = person;
            this.scene = scene;
            window = scene.getWindow();
        }

        @Override
        public void run() {
            recordScrollDiagnostics("post-layout-enter id=" + id + " target=" + identity(person));
            String stopReason = null;
            if (pendingScroll != this) {
                stopReason = "superseded";
            } else if (scene != personListView.getScene()) {
                stopReason = "different-scene";
            } else if (!window.isShowing()) {
                stopReason = "window-not-showing";
            } else if (selectedPerson.get() != person) {
                stopReason = "published-selection-changed";
            } else if (personListView.getSelectionModel().getSelectedItem() != person) {
                stopReason = "native-selection-changed";
            }
            if (stopReason != null) {
                cancel(stopReason);
                return;
            }
            if (isFullyVisible(person)) {
                visibleLayouts++;
                if (visibleLayouts >= REQUIRED_VISIBLE_LAYOUTS) {
                    cancel("stably-visible");
                } else {
                    Platform.requestNextPulse();
                }
                return;
            }
            visibleLayouts = 0;
            if (attempts >= MAX_SCROLL_ATTEMPTS) {
                cancel("attempt-limit");
                return;
            }
            int index = indexOf(person);
            if (index == -1) {
                cancel("contact-missing");
                return;
            }
            recordScrollDiagnostics("retry-before id=" + id + " targetIndex=" + index);
            VirtualFlow<?> flow = virtualFlow();
            if (flow == null || flow.getCellCount() != personListView.getItems().size()) {
                // Let the skin update its item count before correcting the measured viewport on a later pulse.
                personListView.scrollTo(index);
            } else if (index == 0 || index == personListView.getItems().size() - 1) {
                // An endpoint does not depend on cached estimates of the preceding variable-height rows.
                flow.setPosition(index == 0 ? 0 : 1);
                flow.requestLayout();
            } else {
                // Use one scrolling operation per pulse. An index-scroll event first resets the estimated
                // position; combining it with this geometry correction can undo the correction on layout.
                flow.scrollTo(index);
            }
            personListView.requestLayout();
            attempts++;
            recordScrollDiagnostics("retry-after id=" + id + " targetIndex=" + index);
            Platform.requestNextPulse();
        }

        private void cancel(String reason) {
            recordScrollDiagnostics("request-end id=" + id + " reason=" + reason);
            scene.removePostLayoutPulseListener(this);
            window.removeEventHandler(WindowEvent.WINDOW_HIDDEN, onHidden);
            if (pendingScroll == this) {
                pendingScroll = null;
            }
        }
    }

    /**
     * Custom {@code ListCell} that displays the graphics of a {@code Person} using a {@code PersonCard}.
     */
    class PersonListViewCell extends ListCell<Person> {
        // Retain the controller and its weak date listener for as long as the card is displayed.
        private PersonCard personCard;

        PersonListViewCell() {
            // Let the viewport determine cell width rather than the summary's preferred width.
            setPrefWidth(0);
        }

        @Override
        protected void updateItem(Person person, boolean empty) {
            super.updateItem(person, empty);

            if (empty || person == null) {
                personCard = null;
                setGraphic(null);
                setText(null);
            } else {
                personCard = new PersonCard(person, getIndex() + 1, uiDate);
                Region card = personCard.getRoot();
                DoubleBinding availableWidth = Bindings.createDoubleBinding(() -> Math.max(0,
                        getWidth() - getInsets().getLeft() - getInsets().getRight()),
                        widthProperty(), insetsProperty());
                card.prefWidthProperty().bind(availableWidth);
                card.maxWidthProperty().bind(availableWidth);
                setGraphic(card);
            }
        }
    }

}
