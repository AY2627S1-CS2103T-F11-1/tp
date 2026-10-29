package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.abort;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import seedu.address.model.person.Person;
import seedu.address.testutil.MutableClock;
import seedu.address.testutil.PersonBuilder;

public class PersonDetailsPanelTest {
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-28T16:30:00Z"),
            ZoneId.of("Asia/Singapore"));
    private static final PseudoClass OVERDUE = PseudoClass.getPseudoClass("overdue");

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
        } catch (UnsupportedOperationException e) {
            if (!"Unable to open DISPLAY".equals(e.getMessage())) {
                throw e;
            }
            abort("Skipping UI tests: no graphical display is available");
        }
        assertTrue(started.await(10, TimeUnit.SECONDS), "JavaFX did not initialize");
    }

    private static void runOnFxThread(Runnable action) throws Exception {
        FutureTask<Void> task = new FutureTask<>(action, null);
        Platform.runLater(task);
        task.get(10, TimeUnit.SECONDS);
    }

    private static Label label(PersonDetailsPanel panel, String id) {
        return (Label) node(panel, id);
    }

    private static Node node(PersonDetailsPanel panel, String id) {
        Node found = panel.getRoot().lookup("#" + id);
        // ScrollPane content joins the scene graph only after its skin is created.
        return found != null ? found : scrollPane(panel).getContent().lookup("#" + id);
    }

    private static ScrollPane scrollPane(PersonDetailsPanel panel) {
        return (ScrollPane) panel.getRoot().lookup("#detailsScrollPane");
    }

    private static void assertSectionVisible(PersonDetailsPanel panel, String id, boolean visible) {
        Node section = node(panel, id);
        assertEquals(visible, section.isVisible(), id + " visibility");
        assertEquals(visible, section.isManaged(), id + " layout participation");
    }

    /**
     * Attaches the panel to a narrow scene with the application's theme, then resolves layout.
     */
    private static StackPane createScene(PersonDetailsPanel panel) {
        StackPane root = new StackPane(panel.getRoot());
        Scene scene = new Scene(root, 360, 320);
        scene.getStylesheets().add(PersonDetailsPanelTest.class.getResource("/view/DarkTheme.css").toExternalForm());
        scene.getStylesheets().add(PersonDetailsPanelTest.class.getResource("/view/Extensions.css").toExternalForm());
        layout(root);
        return root;
    }

    private static void layout(StackPane root) {
        root.applyCss();
        root.resize(360, 320);
        // Resolve the viewport, its width bindings, and the resulting wrapped content heights.
        for (int i = 0; i < 3; i++) {
            root.layout();
        }
    }

    private static Person longContact() {
        String[] tags = IntStream.range(0, 36).mapToObj(i -> "Component " + i).toArray(String[]::new);
        tags[0] = "X".repeat(50);
        return new PersonBuilder().withName("Artisan Keyboard Supplier ".repeat(12).strip())
                .withAddress("Unit 08 Artisan Keyboard Business Park, ".repeat(16))
                .withEmail("supplier".repeat(18) + "@keyboard.example.com")
                .withTags(tags).build();
    }

    @Test
    public void constructor_displaysEmptyState() throws Exception {
        runOnFxThread(() -> {
            PersonDetailsPanel panel = new PersonDetailsPanel(CLOCK);
            assertEquals("Select a contact to view details.", label(panel, "emptyState").getText());
            assertSectionVisible(panel, "emptyState", true);
            assertSectionVisible(panel, "detailsScrollPane", false);
            assertSectionVisible(panel, "tagsSection", false);
            assertSectionVisible(panel, "followUpSection", false);
        });
    }

    @Test
    public void showPerson_displaysAllFieldsAndSortedTags() throws Exception {
        runOnFxThread(() -> {
            Person person = new PersonBuilder().withTags("switches", "keycaps", "lube")
                    .withFollowUp("2026-10-30", "Confirm quotation").build();
            PersonDetailsPanel panel = new PersonDetailsPanel(CLOCK);
            panel.showPerson(person);

            assertEquals(person.getName().fullName, label(panel, "name").getText());
            assertEquals("Role: " + person.getRole().value, label(panel, "role").getText());
            assertEquals(person.getPhone().value, label(panel, "phone").getText());
            assertEquals(person.getEmail().value, label(panel, "email").getText());
            assertEquals(person.getAddress().value, label(panel, "address").getText());
            FlowPane tags = (FlowPane) node(panel, "detailTags");
            assertEquals(List.of("keycaps", "lube", "switches"), tags.getChildren().stream()
                    .map(node -> ((Label) node).getText()).toList());
            assertEquals("Due: 2026-10-30", label(panel, "followUpDueDate").getText());
            assertEquals("Confirm quotation", label(panel, "followUpDescription").getText());
            assertSectionVisible(panel, "emptyState", false);
            assertSectionVisible(panel, "detailsScrollPane", true);
            assertSectionVisible(panel, "tagsSection", true);
            assertSectionVisible(panel, "followUpSection", true);
        });
    }

    @Test
    public void showPerson_overdueTodayAndFuture_updatesDateAndStyling() throws Exception {
        runOnFxThread(() -> {
            PersonDetailsPanel panel = new PersonDetailsPanel(CLOCK);
            for (String date : List.of("2026-10-28", "2026-10-29", "2026-10-30")) {
                panel.showPerson(new PersonBuilder().withFollowUp(date, "Discuss switches").build());
                boolean overdue = date.equals("2026-10-28");
                assertEquals((overdue ? "Overdue - due: " : "Due: ") + date,
                        label(panel, "followUpDueDate").getText());
                assertEquals(overdue, label(panel, "followUpDueDate").getPseudoClassStates().contains(OVERDUE));
            }
        });
    }

    @Test
    public void showPerson_withoutOptionalSections_removesPreviousContentAndGaps() throws Exception {
        runOnFxThread(() -> {
            PersonDetailsPanel panel = new PersonDetailsPanel(CLOCK);
            panel.showPerson(new PersonBuilder().withTags("switches")
                    .withFollowUp("2026-10-28", "Confirm delivery").build());
            Person next = new PersonBuilder().withName("Another Contact").build();
            panel.showPerson(next);

            assertEquals("Another Contact", label(panel, "name").getText());
            assertTrue(((FlowPane) node(panel, "detailTags")).getChildren().isEmpty());
            assertEquals("", label(panel, "followUpDueDate").getText());
            assertEquals("", label(panel, "followUpDescription").getText());
            assertFalse(label(panel, "followUpDueDate").getPseudoClassStates().contains(OVERDUE));
            assertSectionVisible(panel, "tagsSection", false);
            assertSectionVisible(panel, "followUpSection", false);
            assertSectionVisible(panel, "detailsScrollPane", true);
        });
    }

    @Test
    public void clear_removesAllFieldsAndRestoresEmptyState() throws Exception {
        runOnFxThread(() -> {
            PersonDetailsPanel panel = new PersonDetailsPanel(CLOCK);
            panel.showPerson(new PersonBuilder().withTags("lube")
                    .withFollowUp("2026-10-28", "Check stock").build());
            panel.clear();
            panel.clear();

            for (String id : List.of("name", "role", "phone", "email", "address",
                    "followUpDueDate", "followUpDescription")) {
                assertEquals("", label(panel, id).getText());
            }
            assertTrue(((FlowPane) node(panel, "detailTags")).getChildren().isEmpty());
            assertFalse(label(panel, "followUpDueDate").getPseudoClassStates().contains(OVERDUE));
            assertSectionVisible(panel, "emptyState", true);
            assertSectionVisible(panel, "detailsScrollPane", false);
            assertSectionVisible(panel, "tagsSection", false);
            assertSectionVisible(panel, "followUpSection", false);
        });
    }

    @Test
    public void dateRefresh_afterClearingOrSelectingNoFollowUp_doesNotRestoreStaleContent() throws Exception {
        runOnFxThread(() -> {
            MutableClock clock = new MutableClock(CLOCK.instant(), CLOCK.getZone());
            UiDateTracker date = new UiDateTracker(clock);
            PersonDetailsPanel panel = new PersonDetailsPanel(date);
            panel.showPerson(new PersonBuilder().withFollowUp("2026-10-29", "Confirm delivery").build());
            clock.setInstant(Instant.parse("2026-10-29T16:30:00Z"));
            date.refresh();
            assertEquals("Overdue - due: 2026-10-29", label(panel, "followUpDueDate").getText());
            panel.clear();
            clock.setInstant(Instant.parse("2026-10-30T16:30:00Z"));
            date.refresh();
            assertEquals("", label(panel, "followUpDueDate").getText());
            assertFalse(label(panel, "followUpDueDate").getPseudoClassStates().contains(OVERDUE));
            assertSectionVisible(panel, "emptyState", true);
            assertSectionVisible(panel, "followUpSection", false);

            panel.showPerson(new PersonBuilder().build());
            clock.setInstant(Instant.parse("2026-10-31T16:30:00Z"));
            date.refresh();
            assertEquals("", label(panel, "followUpDueDate").getText());
            assertSectionVisible(panel, "followUpSection", false);
            assertSectionVisible(panel, "detailsScrollPane", true);
        });
    }

    @Test
    public void showPerson_longContent_wrapsWithoutHorizontalOverflow() throws Exception {
        runOnFxThread(() -> {
            PersonDetailsPanel panel = new PersonDetailsPanel(CLOCK);
            panel.showPerson(longContact());
            StackPane root = createScene(panel);
            ScrollPane scroll = scrollPane(panel);
            VBox content = (VBox) scroll.getContent();
            assertTrue(scroll.getViewportBounds().getWidth() > 0);
            assertTrue(content.getHeight() > scroll.getViewportBounds().getHeight());
            assertEquals(ScrollPane.ScrollBarPolicy.AS_NEEDED, scroll.getHbarPolicy());
            for (Node node : root.lookupAll(".scroll-bar")) {
                ScrollBar bar = (ScrollBar) node;
                if (bar.getOrientation() == Orientation.HORIZONTAL) {
                    assertFalse(bar.isVisible(), "Content should fit without hiding the horizontal scrollbar");
                } else {
                    assertTrue(bar.isVisible(), "Long details should scroll vertically");
                }
            }

            Bounds contentBounds = content.localToScene(content.getLayoutBounds());
            for (Node node : content.lookupAll(".label")) {
                Label label = (Label) node;
                Bounds bounds = label.localToScene(label.getLayoutBounds());
                assertTrue(bounds.getMinX() >= contentBounds.getMinX());
                assertTrue(bounds.getMaxX() <= contentBounds.getMaxX() + 1, label.getText());
                assertTrue(label.getHeight() >= label.prefHeight(label.getWidth()) - 1,
                        "Wrapped text should have its full preferred height");
            }
            for (String id : List.of("name", "email", "address")) {
                Label wrapped = label(panel, id);
                assertTrue(wrapped.getHeight() > wrapped.getFont().getSize() * 1.5, id + " should use multiple lines");
            }
            FlowPane tags = (FlowPane) node(panel, "detailTags");
            assertEquals(36, tags.getChildren().size());
            Label longTag = (Label) tags.getChildren().stream()
                    .filter(node -> ((Label) node).getText().equals("X".repeat(50))).findFirst().orElseThrow();
            assertTrue(longTag.getHeight() > longTag.getFont().getSize() * 1.5);
            assertTrue(tags.getHeight() > longTag.getHeight(), "Tags should flow onto multiple rows");
            assertSectionVisible(panel, "followUpSection", false);
        });
    }

    @Test
    public void showPerson_afterScrolling_resetsPositionAndRejectsNullWithoutClearing() throws Exception {
        runOnFxThread(() -> {
            PersonDetailsPanel panel = new PersonDetailsPanel(CLOCK);
            panel.showPerson(longContact());
            StackPane root = createScene(panel);
            scrollPane(panel).setVvalue(1);
            layout(root);
            assertEquals(1, scrollPane(panel).getVvalue());

            Person next = new PersonBuilder().withTags("fresh").build();
            panel.showPerson(next);
            layout(root);
            assertEquals(0, scrollPane(panel).getVvalue());
            assertThrows(NullPointerException.class, () -> panel.showPerson(null));
            assertEquals(next.getName().fullName, label(panel, "name").getText());
            assertSectionVisible(panel, "detailsScrollPane", true);
        });
    }
}
