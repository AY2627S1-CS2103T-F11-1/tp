package seedu.address.ui;

import java.util.Comparator;

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
    private FlowPane tags;

    /**
     * Creates a {@code PersonCard} with the given {@code Person} and index to display.
     */
    public PersonCard(Person person, int displayedIndex) {
        super(FXML);
        this.person = person;
        id.setText(displayedIndex + ". ");
        name.setText(person.getName().fullName);
        Tooltip nameTooltip = new Tooltip(person.getName().fullName);
        nameTooltip.setWrapText(true);
        nameTooltip.setMaxWidth(400);
        name.setTooltip(nameTooltip);
        role.setText(person.getRole().value);
        role.getStyleClass().add("role-badge");
        int colorIndex = Math.abs(person.getRole().value.toLowerCase().hashCode()) % NUM_ROLE_COLORS;
        role.getStyleClass().add("role-color-" + colorIndex);
        phone.setText(person.getPhone().value);
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
}
