package seedu.address.ui;

import java.util.logging.Logger;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.person.Person;

/**
 * Panel containing the list of persons.
 */
public class PersonListPanel extends UiPart<Region> {
    private static final String FXML = "PersonListPanel.fxml";
    private final Logger logger = LogsCenter.getLogger(PersonListPanel.class);
    private final ReadOnlyObjectWrapper<Person> selectedPerson = new ReadOnlyObjectWrapper<>(this, "selectedPerson");
    private boolean commandExecuting;

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
                selectedPerson.set(selected);
            }
        });
        // A list change can replace the selected contact or automatically select a neighbour.
        personList.addListener((ListChangeListener<Person>) change -> {
            if (!commandExecuting) {
                personListView.getSelectionModel().clearSelection();
            }
        });
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
        commandExecuting = true;
    }

    /**
     * Restores a contact by object identity if it remains visible, or clears selection otherwise.
     * The index is resolved from the current list after all replacements, filtering, and sorting.
     */
    void endCommand(Person personToSelect) {
        int index = -1;
        for (int i = 0; i < personListView.getItems().size(); i++) {
            if (personListView.getItems().get(i) == personToSelect) {
                index = i;
                break;
            }
        }
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
