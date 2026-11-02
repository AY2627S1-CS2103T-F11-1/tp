package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import seedu.address.commons.util.StringUtil;

/**
 * Represents a Person's email in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidEmail(String)}
 */
public class Email {

    public static final String MESSAGE_CONSTRAINTS = "Emails must use WHATWG HTML email syntax: local-part@domain.\n"
            + "Local part: one or more ASCII letters, digits or .!#$%&'*+/=?^_`{|}~-; dots may appear anywhere.\n"
            + "Domain labels must contain 1-63 ASCII letters/digits/hyphens, start and end with a letter/digit, "
            + "and be separated by dots. Single-label domains are allowed.\n"
            + "Spaces, quoted local parts, address literals and non-ASCII characters are not supported.";
    // Email validation regex taken from WHATWG HTML
    // Link: https://html.spec.whatwg.org/multipage/input.html#valid-e-mail-address
    public static final String VALIDATION_REGEX = "[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@"
            + "[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?"
            + "(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*";

    public final String value;

    /**
     * Constructs an {@code Email}.
     *
     * @param email A valid email address.
     */
    public Email(String email) {
        requireNonNull(email);
        checkArgument(isValidEmail(email), MESSAGE_CONSTRAINTS);
        value = email;
    }

    /**
     * Returns true if a given string is a valid email.
     */
    public static boolean isValidEmail(String test) {
        return test.matches(VALIDATION_REGEX);
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
        if (!(other instanceof Email otherEmail)) {
            return false;
        }

        return value.equalsIgnoreCase(otherEmail.value);
    }

    @Override
    public int hashCode() {
        return StringUtil.normalizeCase(value).hashCode();
    }

}
