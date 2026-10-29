package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.abort;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import seedu.address.testutil.MutableClock;

public class UiDateTrackerTest {
    private Stage stage;

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

    @AfterEach
    public void cleanUp() throws Exception {
        runOnFxThread(() -> {
            if (stage != null) {
                stage.hide();
            }
        });
    }

    @Test
    public void refresh_usesLocalMidnightAndNotifiesOnlyWhenDateChanges() throws Exception {
        runOnFxThread(() -> {
            MutableClock clock = new MutableClock(Instant.parse("2026-10-29T15:59:59Z"),
                    ZoneId.of("Asia/Singapore"));
            UiDateTracker date = new UiDateTracker(clock);
            AtomicInteger changes = new AtomicInteger();
            date.dateProperty().addListener((observable, previous, today) -> changes.incrementAndGet());
            assertEquals(LocalDate.of(2026, 10, 29), date.getDate());
            date.refresh();
            assertEquals(0, changes.get());
            clock.setInstant(Instant.parse("2026-10-29T16:00:00Z"));
            date.refresh();
            assertEquals(LocalDate.of(2026, 10, 30), date.getDate());
            assertEquals(1, changes.get());
            clock.setInstant(Instant.parse("2026-10-29T16:01:00Z"));
            date.refresh();
            assertEquals(1, changes.get());
            // A manual clock correction can move the calendar backwards as well.
            clock.setInstant(Instant.parse("2026-10-29T15:00:00Z"));
            date.refresh();
            assertEquals(LocalDate.of(2026, 10, 29), date.getDate());
            assertEquals(2, changes.get());
        });
    }

    @Test
    public void tracking_periodicRefreshStopsWhenHiddenAndCatchesUpOnShow() throws Exception {
        MutableClock clock = new MutableClock(Instant.parse("2026-10-29T15:59:59Z"), ZoneId.of("Asia/Singapore"));
        UiDateTracker[] date = new UiDateTracker[1];
        CountDownLatch advanced = new CountDownLatch(1);
        CountDownLatch changedWhileHidden = new CountDownLatch(1);
        runOnFxThread(() -> {
            date[0] = new UiDateTracker(clock, Duration.millis(30));
            stage = new Stage();
            stage.setScene(new Scene(new StackPane(), 100, 100));
            date[0].track(stage);
            stage.show();
            date[0].dateProperty().addListener((observable, previous, today) -> {
                if (today.equals(LocalDate.of(2026, 10, 30))) {
                    advanced.countDown();
                } else {
                    changedWhileHidden.countDown();
                }
            });
            clock.setInstant(Instant.parse("2026-10-29T16:00:00Z"));
        });
        assertTrue(advanced.await(3, TimeUnit.SECONDS), "The timer should notice the next local day");
        runOnFxThread(() -> {
            stage.hide();
            clock.setInstant(Instant.parse("2026-10-30T16:00:00Z"));
        });
        assertFalse(changedWhileHidden.await(200, TimeUnit.MILLISECONDS), "A hidden window should stop its timer");
        runOnFxThread(() -> {
            assertEquals(LocalDate.of(2026, 10, 30), date[0].getDate());
            stage.show();
            assertEquals(LocalDate.of(2026, 10, 31), date[0].getDate());
        });
    }

    @Test
    public void tracking_alreadyShownWindow_refreshesImmediately() throws Exception {
        runOnFxThread(() -> {
            MutableClock clock = new MutableClock(Instant.parse("2026-10-29T15:59:59Z"),
                    ZoneId.of("Asia/Singapore"));
            UiDateTracker date = new UiDateTracker(clock);
            stage = new Stage();
            stage.setScene(new Scene(new StackPane(), 100, 100));
            stage.show();
            clock.setInstant(Instant.parse("2026-10-29T16:00:00Z"));
            date.track(stage);
            assertEquals(LocalDate.of(2026, 10, 30), date.getDate());
        });
    }

    @Test
    public void focusReturning_refreshesWithoutWaitingForTimer() throws Exception {
        runOnFxThread(() -> {
            MutableClock clock = new MutableClock(Instant.parse("2026-10-29T15:59:59Z"),
                    ZoneId.of("Asia/Singapore"));
            UiDateTracker date = new UiDateTracker(clock);
            SimpleBooleanProperty focused = new SimpleBooleanProperty(true);
            stage = new Stage();
            date.track(stage, focused);
            focused.set(false);
            clock.setInstant(Instant.parse("2026-10-29T16:00:00Z"));
            assertEquals(LocalDate.of(2026, 10, 29), date.getDate());
            // The hidden stage has no running timer; only this focus transition can refresh the date.
            focused.set(true);
            assertEquals(LocalDate.of(2026, 10, 30), date.getDate());
        });
    }
}
