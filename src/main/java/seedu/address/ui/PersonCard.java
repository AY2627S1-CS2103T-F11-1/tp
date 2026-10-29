package seedu.address.ui;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import seedu.address.model.person.Person;

/**
 * A compact summary of a {@code Person}; complete contact information is shown in the details panel.
 */
public class PersonCard extends UiPart<Region> {

    private static final String FXML = "PersonListCard.fxml";
    private static final PseudoClass DUE_TODAY = PseudoClass.getPseudoClass("due-today");
    private static final PseudoClass OVERDUE = PseudoClass.getPseudoClass("overdue");

    /**
     * Note: Certain keywords such as "location" and "resources" are reserved keywords in JavaFX.
     * As a consequence, UI elements' variable names cannot be set to such keywords
     * or an exception will be thrown by JavaFX during runtime.
     *
     * @see <a href="https://github.com/se-edu/addressbook-level4/issues/336">The issue on AddressBook level 4</a>
     */

    public final Person person;

    // Keep the weak listener alive only while this card is in use by a list cell.
    private final ChangeListener<LocalDate> dateListener;

    @FXML
    private HBox cardPane;
    @FXML
    private Label name;
    @FXML
    private Label id;
    @FXML
    private Label role;
    @FXML
    private Label followUpBadge;
    @FXML
    private Label phone;
    @FXML
    private Label email;
    @FXML
    private FlowPane tags;

    /**
     * Creates a {@code PersonCard} with the given {@code Person} and index to display.
     */
    public PersonCard(Person person, int displayedIndex) {
        this(person, displayedIndex, new UiDateTracker(Clock.systemDefaultZone()));
    }

    PersonCard(Person person, int displayedIndex, UiDateTracker uiDate) {
        super(FXML);
        this.person = person;
        id.setText(displayedIndex + ". ");
        name.setText(person.getName().fullName);
        Tooltip nameTooltip = new Tooltip(person.getName().fullName);
        nameTooltip.setWrapText(true);
        nameTooltip.setMaxWidth(400);
        name.setTooltip(nameTooltip);
        role.setText(person.getRole().value);
        boolean hasFollowUp = person.getFollowUp().isPresent();
        followUpBadge.setVisible(hasFollowUp);
        followUpBadge.setManaged(hasFollowUp);
        dateListener = (observable, previous, today) -> updateFollowUpBadge(today);
        uiDate.dateProperty().addListener(new WeakChangeListener<>(dateListener));
        updateFollowUpBadge(uiDate.getDate());
        phone.setText(person.getPhone().value);
        email.setText(person.getEmail().value);
        Tooltip emailTooltip = new Tooltip(person.getEmail().value);
        emailTooltip.setWrapText(true);
        emailTooltip.setMaxWidth(400);
        email.setTooltip(emailTooltip);
        person.getTags().stream()
                .sorted(Comparator.comparing(tag -> tag.tagName))
                .forEach(tag -> {
                    Label label = new Label(tag.tagName);
                    label.setWrapText(true);
                    label.setMinWidth(0);
                    label.setMinHeight(Region.USE_PREF_SIZE);
                    tags.getChildren().add(label);
                });
        tags.setVisible(!person.getTags().isEmpty());
        tags.setManaged(!person.getTags().isEmpty());
    }

    private void updateFollowUpBadge(LocalDate today) {
        person.getFollowUp().ifPresent(followUp -> {
            LocalDate dueDate = followUp.getDueDate();
            boolean overdue = dueDate.isBefore(today);
            boolean dueToday = dueDate.equals(today);
            followUpBadge.setText(overdue ? "Overdue" : dueToday ? "Due today" : "Follow-up");
            followUpBadge.pseudoClassStateChanged(DUE_TODAY, dueToday);
            followUpBadge.pseudoClassStateChanged(OVERDUE, overdue);
            followUpBadge.setAccessibleText(followUpBadge.getText()
                    + ". Select this contact to view follow-up details.");
        });
    }
}
