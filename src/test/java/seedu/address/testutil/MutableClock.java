package seedu.address.testutil;

import static java.util.Objects.requireNonNull;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A controllable clock for testing date changes without waiting for real time to pass.
 */
public class MutableClock extends Clock {
    private final AtomicReference<Instant> instant;
    private final ZoneId zone;

    public MutableClock(Instant instant, ZoneId zone) {
        this(new AtomicReference<>(requireNonNull(instant)), zone);
    }

    private MutableClock(AtomicReference<Instant> instant, ZoneId zone) {
        this.instant = instant;
        this.zone = requireNonNull(zone);
    }

    public void setInstant(Instant instant) {
        this.instant.set(requireNonNull(instant));
    }

    @Override
    public ZoneId getZone() {
        return zone;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return new MutableClock(instant, zone);
    }

    @Override
    public Instant instant() {
        return instant.get();
    }
}
