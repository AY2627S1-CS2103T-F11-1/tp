package seedu.address.model.tag;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a Tag in the address book.
 * Guarantees: immutable; name is valid as declared in {@link #isValidTagName(String)}
 */
public class Tag {

    public static final String MESSAGE_CONSTRAINTS =
            "Tag names should only contain alphanumeric characters, spaces, and punctuation (- / & '), "
            + "must start and end with an alphanumeric character, and cannot exceed 50 characters";

    /*
     * The tag name must start and end with an alphanumeric character.
     * Internal spaces and allowed punctuation (- / & ') are accepted.
     */
    public static final String VALIDATION_REGEX = "^[\\p{Alnum}]([\\p{Alnum} /&'-]*[\\p{Alnum}])?$";
    public static final int MAX_LENGTH = 50;

    public final String tagName;

    /**
     * Constructs a {@code Tag}.
     *
     * @param tagName A valid tag name.
     */
    public Tag(String tagName) {
        requireNonNull(tagName);
        checkArgument(isValidTagName(tagName), MESSAGE_CONSTRAINTS);
        this.tagName = formatTagName(tagName);
    }

    /**
     * Returns true if a given string is a valid tag name.
     */
    public static boolean isValidTagName(String test) {
        return test.length() <= MAX_LENGTH && test.matches(VALIDATION_REGEX);
    }

    /**
     * Formats a tag name by collapsing consecutive whitespace.
     */
    private static String formatTagName(String tagName) {
        return tagName.trim().replaceAll("\\s+", " ");
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Tag otherTag)) {
            return false;
        }

        return tagName.equalsIgnoreCase(otherTag.tagName);
    }

    @Override
    public int hashCode() {
        return tagName.toLowerCase().hashCode();
    }

    /**
     * Formats state as text for viewing.
     */
    @Override
    public String toString() {
        return '[' + tagName + ']';
    }

}
