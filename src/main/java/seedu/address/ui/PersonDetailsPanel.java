package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.time.Clock;
import java.util.Comparator;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.model.person.FollowUp;
import seedu.address.model.person.Person;

/**
 * A scrollable view of one contact's details, or a prompt when no contact is displayed.
 */
public class PersonDetailsPanel extends UiPart<Region> {
    private static final String FXML = "PersonDetailsPanel.fxml";
    private static final PseudoClass OVERDUE = PseudoClass.getPseudoClass("overdue");

    private final UiDateTracker uiDate;
    private final DoubleBinding contentWidth;
    private FollowUp displayedFollowUp;

    @FXML
    private Label emptyState;
    @FXML
    private ScrollPane detailsScrollPane;
    @FXML
    private VBox detailsContent;
    @FXML
    private Label name;
    @FXML
    private Label role;
    @FXML
    private VBox tagsSection;
    @FXML
    private FlowPane detailTags;
    @FXML
    private Label phone;
    @FXML
    private Label email;
    @FXML
    private Label address;
    @FXML
    private VBox followUpSection;
    @FXML
    private Label followUpDueDate;
    @FXML
    private Label followUpDescription;

    /**
     * Creates an empty panel using the system's local date for overdue follow-ups.
     */
    public PersonDetailsPanel() {
        this(Clock.systemDefaultZone());
    }

    /**
     * Creates an empty panel with a clock for deterministic overdue checks.
     */
    PersonDetailsPanel(Clock clock) {
        this(new UiDateTracker(clock));
    }

    PersonDetailsPanel(UiDateTracker uiDate) {
        super(FXML);
        this.uiDate = requireNonNull(uiDate);
        uiDate.dateProperty().addListener((observable, previous, today) -> updateFollowUpDate());
        contentWidth = Bindings.createDoubleBinding(() -> Math.max(0,
                detailsScrollPane.getViewportBounds().getWidth()
                        - detailsContent.getInsets().getLeft() - detailsContent.getInsets().getRight()),
                detailsScrollPane.viewportBoundsProperty(), detailsContent.insetsProperty());
        for (Label label : new Label[] {name, role, phone, email, address, followUpDueDate, followUpDescription}) {
            configureWrapping(label);
            label.prefWidthProperty().bind(contentWidth);
            label.maxWidthProperty().bind(contentWidth);
        }
        emptyState.maxWidthProperty().bind(getRoot().widthProperty());
        clear();
    }

    /**
     * Displays the given contact, replacing all previous content and resetting the scroll position.
     *
     * @throws NullPointerException if {@code person} is null.
     */
    public void showPerson(Person person) {
        requireNonNull(person);
        uiDate.refresh();
        clear();
        name.setText(person.getName().fullName);
        role.setText("Role: " + person.getRole().value);
        phone.setText(person.getPhone().value);
        email.setText(person.getEmail().value);
        address.setText(person.getAddress().value);

        person.getTags().stream().sorted(Comparator.comparing(tag -> tag.tagName)).forEach(tag -> {
            Label label = new Label(tag.tagName);
            configureWrapping(label);
            label.maxWidthProperty().bind(Bindings.min(contentWidth, 250));
            detailTags.getChildren().add(label);
        });
        setSectionVisible(tagsSection, !person.getTags().isEmpty());
        person.getFollowUp().ifPresent(this::showFollowUp);
        setSectionVisible(emptyState, false);
        setSectionVisible(detailsScrollPane, true);
    }

    /**
     * Removes all contact content and displays the selection prompt.
     */
    public void clear() {
        displayedFollowUp = null;
        for (Label label : new Label[] {name, role, phone, email, address, followUpDueDate, followUpDescription}) {
            label.setText("");
        }
        detailTags.getChildren().clear();
        followUpDueDate.pseudoClassStateChanged(OVERDUE, false);
        setSectionVisible(tagsSection, false);
        setSectionVisible(followUpSection, false);
        detailsScrollPane.setVvalue(0);
        detailsScrollPane.setHvalue(0);
        setSectionVisible(detailsScrollPane, false);
        setSectionVisible(emptyState, true);
    }

    private void showFollowUp(FollowUp followUp) {
        displayedFollowUp = followUp;
        updateFollowUpDate();
        followUpDescription.setText(followUp.getDescription());
        setSectionVisible(followUpSection, true);
    }

    private void updateFollowUpDate() {
        if (displayedFollowUp == null) {
            return;
        }
        boolean overdue = displayedFollowUp.getDueDate().isBefore(uiDate.getDate());
        followUpDueDate.setText((overdue ? "Overdue - due: " : "Due: ") + displayedFollowUp.getDueDate());
        followUpDueDate.pseudoClassStateChanged(OVERDUE, overdue);
    }

    private static void configureWrapping(Label label) {
        label.setWrapText(true);
        label.setMinWidth(0);
        label.setMinHeight(Region.USE_PREF_SIZE);
    }

    private static void setSectionVisible(Node section, boolean visible) {
        section.setVisible(visible);
        section.setManaged(visible);
    }
}
