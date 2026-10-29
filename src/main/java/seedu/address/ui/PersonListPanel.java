package seedu.address.ui;

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
    private boolean commandExecuting;
    private ScrollRequest pendingScroll;

    @FXML
    private ListView<Person> personListView;

    /**
     * Creates a {@code PersonListPanel} with the given {@code ObservableList}.
     */
    public PersonListPanel(ObservableList<Person> personList) {
        super(FXML);
        personListView.setItems(personList);
        personListView.setCellFactory(listView -> new PersonListViewCell());
        personListView.getSelectionModel().selectedItemProperty().addListener((observable, previous, selected) -> {
            if (!commandExecuting) {
                cancelPendingScroll();
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
        personListView.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> cancelPendingScroll());
        personListView.addEventFilter(KeyEvent.KEY_PRESSED, event -> cancelPendingScroll());
        personListView.addEventFilter(ScrollEvent.SCROLL, event -> cancelPendingScroll());
        personListView.sceneProperty().addListener((observable, previous, scene) -> cancelPendingScroll());
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
                personListView.scrollTo(index);
            }
        } finally {
            commandExecuting = false;
        }
        selectedPerson.set(index == -1 ? null : personListView.getItems().get(index));
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

    private void cancelPendingScroll() {
        if (pendingScroll != null) {
            pendingScroll.cancel();
        }
    }

    private void scrollAfterLayout(Person person) {
        cancelPendingScroll();
        Scene scene = personListView.getScene();
        if (scene == null || scene.getWindow() == null || !scene.getWindow().isShowing()) {
            return;
        }
        pendingScroll = new ScrollRequest(person, scene);
        scene.addPostLayoutPulseListener(pendingScroll);
        scene.getWindow().addEventHandler(WindowEvent.WINDOW_HIDDEN, pendingScroll.onHidden);
        Platform.requestNextPulse();
    }

    private VirtualFlow<?> virtualFlow() {
        Node node = personListView.lookup(".virtual-flow");
        return node instanceof VirtualFlow<?> flow ? flow : null;
    }

    private boolean isFullyVisible(Person person) {
        VirtualFlow<?> flow = virtualFlow();
        int index = indexOf(person);
        if (flow == null || index == -1) {
            return false;
        }
        // Measurement and recycled cells found by CSS lookup are not displayed rows.
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

    /** Completes scrolling once native row counts and variable-height layout have settled. */
    private class ScrollRequest implements Runnable {
        private final Person person;
        private final Scene scene;
        private final Window window;
        private final EventHandler<WindowEvent> onHidden = event -> cancel();
        private int attempts;
        private int visibleLayouts;

        ScrollRequest(Person person, Scene scene) {
            this.person = person;
            this.scene = scene;
            window = scene.getWindow();
        }

        @Override
        public void run() {
            if (pendingScroll != this || scene != personListView.getScene() || !window.isShowing()
                    || selectedPerson.get() != person
                    || personListView.getSelectionModel().getSelectedItem() != person) {
                cancel();
                return;
            }
            if (isFullyVisible(person)) {
                if (++visibleLayouts >= REQUIRED_VISIBLE_LAYOUTS) {
                    cancel();
                } else {
                    Platform.requestNextPulse();
                }
                return;
            }
            visibleLayouts = 0;
            int index = indexOf(person);
            if (index == -1 || attempts >= MAX_SCROLL_ATTEMPTS) {
                cancel();
                return;
            }
            VirtualFlow<?> flow = virtualFlow();
            if (flow == null || flow.getCellCount() != personListView.getItems().size()) {
                personListView.scrollTo(index);
            } else if (index == 0 || index == personListView.getItems().size() - 1) {
                flow.setPosition(index == 0 ? 0 : 1);
                flow.requestLayout();
            } else {
                // Do not combine this geometry correction with an index-scroll event in the same pulse.
                flow.scrollTo(index);
            }
            personListView.requestLayout();
            attempts++;
            Platform.requestNextPulse();
        }

        private void cancel() {
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
        PersonListViewCell() {
            // Let the viewport determine cell width rather than the summary's preferred width.
            setPrefWidth(0);
        }

        @Override
        protected void updateItem(Person person, boolean empty) {
            super.updateItem(person, empty);

            if (empty || person == null) {
                setGraphic(null);
                setText(null);
            } else {
                Region card = new PersonCard(person, getIndex() + 1).getRoot();
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
