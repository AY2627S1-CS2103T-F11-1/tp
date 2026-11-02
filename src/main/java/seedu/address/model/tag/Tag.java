package seedu.address.model.tag;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import seedu.address.commons.util.StringUtil;

/**
 * Represents a Tag in the address book.
 * Guarantees: immutable; name is valid as declared in {@link #isValidTagName(String)}
 */
public class Tag {

    public static final String MESSAGE_CONSTRAINTS =
            "Tag names must contain text other than whitespace or invisible formatting characters, "
            + "must not contain internal control characters or Unicode line/paragraph separators, "
            + "and cannot exceed 50 characters (after trimming and collapsing ordinary spaces).";

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
        return StringUtil.isValidText(test.strip()) && formatTagName(test).length() <= MAX_LENGTH;
    }

    /**
     * Formats a tag name by trimming surrounding whitespace and collapsing consecutive ordinary spaces.
     */
    private static String formatTagName(String tagName) {
        return StringUtil.normalizeSpaces(tagName);
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
        return StringUtil.normalizeCase(tagName).hashCode();
    }

    /**
     * Formats state as text for viewing.
     */
    @Override
    public String toString() {
        return '[' + tagName + ']';
    }

}
