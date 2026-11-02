package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import seedu.address.commons.util.StringUtil;

/**
 * Represents a Person's role in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidRole(String)}
 */
public class Role {

    public static final String MESSAGE_CONSTRAINTS =
            "Roles must contain text other than whitespace or invisible formatting characters, "
            + "and must not contain internal control characters or Unicode line/paragraph separators.";

    public final String value;

    /**
     * Constructs a {@code Role}.
     *
     * @param role A valid role.
     */
    public Role(String role) {
        requireNonNull(role);
        checkArgument(isValidRole(role), MESSAGE_CONSTRAINTS);
        value = formatRole(role);
    }

    /**
     * Returns true if a given string is a valid role.
     */
    public static boolean isValidRole(String test) {
        return StringUtil.isValidText(test.strip());
    }

    /**
     * Formats a role by trimming surrounding whitespace and collapsing consecutive ordinary spaces.
     */
    private static String formatRole(String role) {
        return StringUtil.normalizeSpaces(role);
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Role otherRole)) {
            return false;
        }

        return value.equalsIgnoreCase(otherRole.value);
    }

    @Override
    public int hashCode() {
        return StringUtil.normalizeCase(value).hashCode();
    }
}
