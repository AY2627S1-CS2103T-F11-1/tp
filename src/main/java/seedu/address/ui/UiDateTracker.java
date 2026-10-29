package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.time.Clock;
import java.time.LocalDate;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableBooleanValue;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Shares today's local date across a window, refreshing while shown and when focus returns.
 * All methods must be called on the JavaFX application thread.
 */
final class UiDateTracker {
    private final Clock clock;
    private final ReadOnlyObjectWrapper<LocalDate> date = new ReadOnlyObjectWrapper<>();
    private final Timeline refreshTimer;

    UiDateTracker(Clock clock) {
        this(clock, Duration.minutes(1));
    }

    UiDateTracker(Clock clock, Duration refreshInterval) {
        this.clock = requireNonNull(clock);
        refresh();
        refreshTimer = new Timeline(new KeyFrame(refreshInterval, event -> refresh()));
        refreshTimer.setCycleCount(Animation.INDEFINITE);
    }

    ReadOnlyObjectProperty<LocalDate> dateProperty() {
        return date.getReadOnlyProperty();
    }

    LocalDate getDate() {
        return date.get();
    }

    /** Updates the date without notifying observers when the calendar day is unchanged. */
    void refresh() {
        LocalDate today = LocalDate.now(clock);
        if (!today.equals(date.get())) {
            date.set(today);
        }
    }

    /** Starts and stops the single refresh timer with the window's visibility. */
    void track(Stage stage) {
        track(stage, stage.focusedProperty());
    }

    /** Accepts a focus source so focus transitions can be tested without the operating system's window manager. */
    void track(Stage stage, ObservableBooleanValue focusedProperty) {
        stage.showingProperty().addListener((observable, wasShowing, showing) -> {
            if (showing) {
                refresh();
                refreshTimer.play();
            } else {
                refreshTimer.stop();
            }
        });
        focusedProperty.addListener((observable, wasFocused, focused) -> {
            if (focused) {
                refresh();
            }
        });
        if (stage.isShowing()) {
            refresh();
            refreshTimer.play();
        }
    }
}
