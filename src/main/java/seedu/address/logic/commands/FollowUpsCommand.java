package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;
import static seedu.address.model.person.FollowUp.DATE_MESSAGE_CONSTRAINTS;
import static seedu.address.model.person.FollowUp.isValidDueDate;

import java.time.Clock;
import java.time.LocalDate;
import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Lists contacts with follow-ups, optionally filtered by their due dates.
 */
public class FollowUpsCommand extends Command {

    public static final String COMMAND_WORD = "followups";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Lists contacts with follow-ups across entire KeyBossWarriorsPro.\n"
            + "Parameters: [overdue | by/YYYY-MM-DD]\n"
            + "Without arguments, lists all contacts with follow-ups.\n"
            + "overdue lists follow-ups due before today.\n"
            + "by/ lists follow-ups due on or before the supplied date. Past dates are accepted.\n"
            + "Examples:\n"
            + COMMAND_WORD + "\n"
            + COMMAND_WORD + " overdue\n"
            + COMMAND_WORD + " by/2026-11-01";

    public static final String MESSAGE_SUCCESS = "%1$d follow-up(s) found.";
    public static final String MESSAGE_NO_FOLLOW_UPS = "No follow-ups found.";

    private enum Mode {
        ALL, OVERDUE, BY_DATE
    }

    private final Mode mode;

    // Only present for BY_DATE queries
    private final LocalDate cutoffDate;
    private final Clock clock;

    /**
     * Creates a FollowUpsCommand with its mode, optional cutoff and clock.
     * Public factory methods ensure that each mode receives valid arguments.
     */
    private FollowUpsCommand(Mode mode, LocalDate cutoffDate, Clock clock) {
        requireAllNonNull(mode, clock);

        this.mode = mode;
        this.cutoffDate = cutoffDate;
        this.clock = clock;
    }

    /**
     * Creates and returns a FollowUpsCommand that lists all contacts with follow-ups
     */
    public static FollowUpsCommand all() {
        return new FollowUpsCommand(Mode.ALL, null, Clock.systemDefaultZone());
    }

    /**
     * Creates and returns a FollowUpsCommand that lists follow-ups due before the local date
     * at execution time
     */
    public static FollowUpsCommand overdue() {
        return overdue(Clock.systemDefaultZone());
    }

    /**
     * Creates and returns a FollowUpsCommand that lists follow-ups due before the supplied clock.
     *
     * @throws NullPointerException if the clock is null
     */
    public static FollowUpsCommand overdue(Clock clock) {
        return new FollowUpsCommand(Mode.OVERDUE, null, clock);
    }

    /**
     * Creates and returns a FollowsUpCommand that lists follow-ups due on or before the given date.
     *
     * @throws NullPointerException if cutoffDate is null
     * @throws IllegalArgumentException if the date has an unsupported year.
     */
    public static FollowUpsCommand dueBy(LocalDate cutoffDate) {
        requireNonNull(cutoffDate);
        if (!isValidDueDate(cutoffDate)) {
            throw new IllegalArgumentException(DATE_MESSAGE_CONSTRAINTS);
        }
        return new FollowUpsCommand(Mode.BY_DATE, cutoffDate, Clock.systemDefaultZone());
    }

    /**
     * Replaces the current filter with this follow-up query.
     * Contact data and undo/redo history remain unchanged.
     */
    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);

        model.updateFilteredPersonList(createPredicate());

        int count = model.getFilteredPersonList().size();
        return new CommandResult(count == 0 ? MESSAGE_NO_FOLLOW_UPS : String.format(MESSAGE_SUCCESS, count));
    }

    /**
     * Creates a contact predicate, capturing today's date once for overdue queries.
     */
    private Predicate<Person> createPredicate() {
        return switch (mode) {
            case ALL -> person -> person.getFollowUp().isPresent();
            case OVERDUE -> {
                LocalDate today = LocalDate.now(clock);
                yield person -> person.getFollowUp()
                        .map(followUp -> followUp.getDueDate().isBefore(today))
                        .orElse(false);
            }
            case BY_DATE -> person -> person.getFollowUp()
                    .map(followUp -> !followUp.getDueDate().isAfter(cutoffDate))
                    .orElse(false);
        };
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof FollowUpsCommand otherCommand)) {
            return false;
        }

        if (mode != otherCommand.mode) {
            return false;
        }

        return switch (mode) {
            case ALL -> true;
            case OVERDUE -> clock.equals(otherCommand.clock);
            case BY_DATE -> cutoffDate.equals(otherCommand.cutoffDate);
        };
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("mode", mode)
                .add("cutoffDate", cutoffDate)
                .toString();
    }

}
