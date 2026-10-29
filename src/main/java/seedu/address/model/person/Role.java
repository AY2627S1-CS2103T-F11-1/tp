package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a Person's role in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidRole(String)}
 */
public class Role {

    public static final String MESSAGE_CONSTRAINTS =
            "Roles should only contain alphanumeric characters, spaces, and punctuation (- / & '), "
            + "and must start and end with an alphanumeric character.";

    /*
     * The role must start and end with an alphanumeric character.
     * Internal spaces and allowed punctuation (- / & ') are accepted.
     */
    public static final String VALIDATION_REGEX = "^[\\p{Alnum}]([\\p{Alnum} /&'-]*[\\p{Alnum}])?$";

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
        return test.matches(VALIDATION_REGEX);
    }

    /**
     * Formats a role by collapsing consecutive whitespace.
     */
    private static String formatRole(String role) {
        return role.trim().replaceAll("\\s+", " ");
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
        return value.toLowerCase().hashCode();
    }
}
