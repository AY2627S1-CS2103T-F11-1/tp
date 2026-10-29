package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class PersonCardTest {
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-28T16:30:00Z"),
            ZoneId.of("Asia/Singapore"));

    @BeforeAll
    public static void initializeJavaFx() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        Runnable initialize = () -> {
            Platform.setImplicitExit(false);
            started.countDown();
        };

        try {
            Platform.startup(initialize);
        } catch (IllegalStateException e) {
            Platform.runLater(initialize);
        }

        assertTrue(started.await(10, TimeUnit.SECONDS), "JavaFX did not initialize");
    }

    private static void runOnFxThread(Runnable action) throws Exception {
        FutureTask<Void> task = new FutureTask<>(action, null);
        Platform.runLater(task);
        task.get(10, TimeUnit.SECONDS);
    }

    @Test
    public void displaysCompactSummaryAndLeavesFullDetailsToPanel() throws Exception {
        runOnFxThread(() -> {
            PersonCard card = new PersonCard(ALICE, 1);

            Label idLabel = (Label) card.getRoot().lookup("#id");
            Label nameLabel = (Label) card.getRoot().lookup("#name");
            Label roleLabel = (Label) card.getRoot().lookup("#role");
            Label phoneLabel = (Label) card.getRoot().lookup("#phone");
            FlowPane tagsPane = (FlowPane) card.getRoot().lookup("#tags");

            assertEquals("1. ", idLabel.getText());
            assertEquals(ALICE.getName().fullName, nameLabel.getText());
            assertEquals(ALICE.getRole().value, roleLabel.getText());
            assertEquals(ALICE.getPhone().value, phoneLabel.getText());
            assertNull(card.getRoot().lookup("#address"));
            Label emailLabel = (Label) card.getRoot().lookup("#email");
            assertEquals(ALICE.getEmail().value, emailLabel.getText());
            assertEquals(ALICE.getEmail().value, emailLabel.getTooltip().getText());
            assertTrue(emailLabel.getTooltip().isWrapText());
            for (String id : List.of("id", "name", "role", "followUpBadge", "phone", "email", "tags")) {
                assertEquals(1, card.getRoot().lookupAll("#" + id).size(), "Each summary field should appear once");
            }

            PersonDetailsPanel details = new PersonDetailsPanel(CLOCK);
            details.showPerson(ALICE);
            ScrollPane scroll = (ScrollPane) details.getRoot().lookup("#detailsScrollPane");
            assertEquals(ALICE.getEmail().value, ((Label) scroll.getContent().lookup("#email")).getText());
            assertEquals(ALICE.getAddress().value, ((Label) scroll.getContent().lookup("#address")).getText());

            assertTrue(roleLabel.getStyleClass().contains("role-badge"));
            int expectedColorIndex = Math.abs(ALICE.getRole().value.toLowerCase().hashCode())
                    % PersonCard.NUM_ROLE_COLORS;
            assertTrue(roleLabel.getStyleClass().contains("role-color-" + expectedColorIndex));

            List<String> displayedTags = tagsPane.getChildren().stream()
                    .map(node -> ((Label) node).getText())
                    .collect(Collectors.toList());
            List<String> expectedTags = ALICE.getTags().stream()
                    .map(tag -> tag.tagName)
                    .sorted()
                    .collect(Collectors.toList());
            assertEquals(expectedTags, displayedTags);
        });
    }

    @Test
    public void displaysMultipleTagsInAlphabeticalOrder() throws Exception {
        runOnFxThread(() -> {
            Person personWithMultipleTags = new PersonBuilder()
                    .withTags("zoology", "astronomy", "biology")
                    .build();
            PersonCard card = new PersonCard(personWithMultipleTags, 1);
            FlowPane tagsPane = (FlowPane) card.getRoot().lookup("#tags");

            List<String> displayedTags = tagsPane.getChildren().stream()
                    .map(node -> ((Label) node).getText())
                    .collect(Collectors.toList());
            assertEquals(List.of("astronomy", "biology", "zoology"), displayedTags);
        });
    }

    @Test
    public void roleBadge_dynamicColorHash_assignsConsistentColorClass() throws Exception {
        runOnFxThread(() -> {
            String[] roles = {"Supplier", "Customer", "Manufacturer", "Partner", "Logistics"};
            for (String roleName : roles) {
                Person person = new PersonBuilder().withRole(roleName).build();
                PersonCard card = new PersonCard(person, 1);
                Label roleLabel = (Label) card.getRoot().lookup("#role");

                int expectedColorIndex = Math.abs(roleName.toLowerCase().hashCode())
                        % PersonCard.NUM_ROLE_COLORS;
                assertTrue(roleLabel.getStyleClass().contains("role-color-" + expectedColorIndex),
                        "Expected role-color-" + expectedColorIndex + " for role: " + roleName);
            }
        });
    }

    @Test
    public void roleBadge_caseInsensitiveRoleHash_producesSameColorClass() throws Exception {
        runOnFxThread(() -> {
            Person upperPerson = new PersonBuilder().withRole("SUPPLIER").build();
            Person lowerPerson = new PersonBuilder().withRole("supplier").build();
            Person mixedPerson = new PersonBuilder().withRole("SuPpLiEr").build();

            PersonCard upperCard = new PersonCard(upperPerson, 1);
            PersonCard lowerCard = new PersonCard(lowerPerson, 2);
            PersonCard mixedCard = new PersonCard(mixedPerson, 3);

            Label upperLabel = (Label) upperCard.getRoot().lookup("#role");
            Label lowerLabel = (Label) lowerCard.getRoot().lookup("#role");
            Label mixedLabel = (Label) mixedCard.getRoot().lookup("#role");

            int expectedColorIndex = Math.abs("supplier".hashCode()) % PersonCard.NUM_ROLE_COLORS;
            String expectedClass = "role-color-" + expectedColorIndex;

            assertTrue(upperLabel.getStyleClass().contains(expectedClass));
            assertTrue(lowerLabel.getStyleClass().contains(expectedClass));
            assertTrue(mixedLabel.getStyleClass().contains(expectedClass));
        });
    }

    @Test
    public void followUpContent_appearsInDetailsPanelInsteadOfSummary() throws Exception {
        runOnFxThread(() -> {
            String dueDate = "2026-12-31";
            String description = "Confirm shipment delivery";
            Person personWithFollowUp = new PersonBuilder(ALICE)
                    .withFollowUp(dueDate, description)
                    .build();

            PersonCard card = new PersonCard(personWithFollowUp, 1);

            assertNull(card.getRoot().lookup("#followUpSection"));
            assertNull(card.getRoot().lookup("#followUpDueDate"));
            assertNull(card.getRoot().lookup("#followUpDescription"));
            PersonDetailsPanel details = new PersonDetailsPanel(CLOCK);
            details.showPerson(personWithFollowUp);
            ScrollPane scroll = (ScrollPane) details.getRoot().lookup("#detailsScrollPane");
            VBox followUpSection = (VBox) scroll.getContent().lookup("#followUpSection");
            Label followUpDueDate = (Label) scroll.getContent().lookup("#followUpDueDate");
            Label followUpDescription = (Label) scroll.getContent().lookup("#followUpDescription");

            assertTrue(followUpSection.isVisible());
            assertTrue(followUpSection.isManaged());
            assertEquals("Due: " + dueDate, followUpDueDate.getText());
            assertEquals(description, followUpDescription.getText());
        });
    }

    @Test
    public void emptyTagsAndAbsentFollowUp_leaveNoSummarySections() throws Exception {
        runOnFxThread(() -> {
            Person personWithoutFollowUp = new PersonBuilder(ALICE).withTags().build();
            PersonCard card = new PersonCard(personWithoutFollowUp, 1);

            assertNull(card.getRoot().lookup("#followUpSection"));
            FlowPane tags = (FlowPane) card.getRoot().lookup("#tags");
            assertFalse(tags.isVisible());
            assertFalse(tags.isManaged());
        });
    }
}
