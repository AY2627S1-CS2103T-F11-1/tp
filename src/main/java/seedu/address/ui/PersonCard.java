package seedu.address.ui;

import java.util.Comparator;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.model.person.FollowUp;
import seedu.address.model.person.Person;

/**
 * A UI component that displays information of a {@code Person}.
 */
public class PersonCard extends UiPart<Region> {

    public static final int NUM_ROLE_COLORS = 8;
    private static final String FXML = "PersonListCard.fxml";

    /**
     * Note: Certain keywords such as "location" and "resources" are reserved keywords in JavaFX.
     * As a consequence, UI elements' variable names cannot be set to such keywords
     * or an exception will be thrown by JavaFX during runtime.
     *
     * @see <a href="https://github.com/se-edu/addressbook-level4/issues/336">The issue on AddressBook level 4</a>
     */

    public final Person person;

    @FXML
    private HBox cardPane;
    @FXML
    private Label name;
    @FXML
    private Label id;
    @FXML
    private Label role;
    @FXML
    private Label phone;
    @FXML
    private Label address;
    @FXML
    private Label email;
    @FXML
    private FlowPane tags;
    @FXML
    private VBox followUpSection;
    @FXML
    private Label followUpDueDate;
    @FXML
    private Label followUpDescription;

    /**
     * Creates a {@code PersonCard} with the given {@code Person} and index to display.
     */
    public PersonCard(Person person, int displayedIndex) {
        super(FXML);
        this.person = person;
        id.setText(displayedIndex + ". ");
        name.setText(person.getName().fullName);
        role.setText(person.getRole().value);
        role.getStyleClass().add("role-badge");
        int colorIndex = Math.abs(person.getRole().value.toLowerCase().hashCode()) % NUM_ROLE_COLORS;
        role.getStyleClass().add("role-color-" + colorIndex);
        phone.setText(person.getPhone().value);
        address.setText(person.getAddress().value);
        email.setText(person.getEmail().value);
        person.getTags().stream()
                .sorted(Comparator.comparing(tag -> tag.tagName))
                .forEach(tag -> tags.getChildren().add(new Label(tag.tagName)));
        initializeFollowUp();
    }

    /**
     * Displays the contact's follow-up when present.
     * Otherwise, hides the section and excludes it from layout calculations.
     */
    private void initializeFollowUp() {
        boolean hasFollowUp = person.getFollowUp().isPresent();

        followUpSection.setVisible(hasFollowUp);
        followUpSection.setManaged(hasFollowUp);

        if (hasFollowUp) {
            FollowUp followUp = person.getFollowUp().orElseThrow();
            followUpDueDate.setText("Follow-up due: " + followUp.getDueDate());
            followUpDescription.setText(followUp.getDescription());
        }
    }
}
