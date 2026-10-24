package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.time.LocalDate;
import java.util.Objects;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Represents an immutable follow-up belonging to a contact.
 * Both the due date and description are mandatory. Past dates are accepted.
 */
public class FollowUp {

    public static final int MAX_DESCRIPTION_LENGTH = 200;

    public static final String MESSAGE_CONSTRAINTS =
            "Follow-up descriptions must not be blank and must not exceed "
                    + MAX_DESCRIPTION_LENGTH + " characters after trimming.";

    private static final int UNICODE_LINE_SEPARATOR = 0x2028;
    private static final int UNICODE_PARAGRAPH_SEPARATOR = 0x2029;

    private final LocalDate dueDate;
    private final String description;

    /**
     * Constructs a {@code FollowUp}.
     *
     * @param dueDate A valid date.
     * @param description A valid string describing the follow-up.
     * @throws NullPointerException if either argument is null.
     * @throws IllegalArgumentException if the description is invalid.
     */
    public FollowUp(LocalDate dueDate, String description) {
        requireNonNull(dueDate);
        requireNonNull(description);

        String trimmedDescription = description.strip();
        checkArgument(isValidDescription(trimmedDescription), MESSAGE_CONSTRAINTS);

        this.dueDate = dueDate;
        this.description = trimmedDescription;
    }

    /**
     * Returns whether the description is valid after trimming.
     */
    public static boolean isValidDescription(String description) {
        requireNonNull(description);
        String trimmedDescription = description.strip();

        return !trimmedDescription.isBlank()
                && trimmedDescription.length() <= MAX_DESCRIPTION_LENGTH
                && trimmedDescription.codePoints().noneMatch(FollowUp::isDisallowedDescriptionCharacter);
    }

    /**
     * Returns whether a Unicode character is prohibited in a description.
     * ISO control characters include tabs, carriage returns and newlines.
     * Unicode also defines separate line and paragraph separators, which
     * are not ISO control characters and must be checked explicitly.
     *
     * @param codePoint the numeric Unicode value of a character.
     */
    private static boolean isDisallowedDescriptionCharacter(int codePoint) {
        return Character.isISOControl(codePoint)
                || codePoint == UNICODE_LINE_SEPARATOR
                || codePoint == UNICODE_PARAGRAPH_SEPARATOR;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof FollowUp otherFollowUp)) {
            return false;
        }

        return dueDate.equals(otherFollowUp.dueDate) && description.equals(otherFollowUp.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dueDate, description);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("dueDate", dueDate)
                .add("description", description)
                .toString();
    }
}
