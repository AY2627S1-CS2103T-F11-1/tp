package seedu.address.model.tag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class TagTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Tag(null));
    }

    @Test
    public void constructor_invalidTagName_throwsIllegalArgumentException() {
        String invalidTagName = "";
        assertThrows(IllegalArgumentException.class, () -> new Tag(invalidTagName));
    }

    @Test
    public void constructor_normalizesToLowercase() {
        assertEquals("switches", new Tag("Switches").tagName);
        assertEquals("cherry_mx", new Tag("CHERRY_MX").tagName);
        assertEquals("fr4-plate", new Tag("FR4-Plate").tagName);
        assertEquals("60%", new Tag("60%").tagName);
    }

    @Test
    public void isValidTagName() {
        // null tag name
        assertThrows(NullPointerException.class, () -> Tag.isValidTagName(null));

        // invalid tag names
        assertFalse(Tag.isValidTagName("")); // empty string
        assertFalse(Tag.isValidTagName(" ")); // spaces only
        assertFalse(Tag.isValidTagName("cherry mx")); // internal spaces
        assertFalse(Tag.isValidTagName(" switches")); // leading spaces
        assertFalse(Tag.isValidTagName("switches ")); // trailing spaces
        assertFalse(Tag.isValidTagName("-tag")); // starts with hyphen
        assertFalse(Tag.isValidTagName("_tag")); // starts with underscore
        assertFalse(Tag.isValidTagName("/tag")); // starts with slash
        assertFalse(Tag.isValidTagName("%60")); // starts with percent
        assertFalse(Tag.isValidTagName("tag-")); // ends with hyphen
        assertFalse(Tag.isValidTagName("tag_")); // ends with underscore
        assertFalse(Tag.isValidTagName("tag/")); // ends with slash
        assertFalse(Tag.isValidTagName("#friend")); // disallowed symbol '#'
        assertFalse(Tag.isValidTagName("hubby*")); // disallowed symbol '*'
        assertFalse(Tag.isValidTagName("tag@domain")); // disallowed symbol '@'
        assertFalse(Tag.isValidTagName("tag!test")); // disallowed symbol '!'
        assertFalse(Tag.isValidTagName("a".repeat(51))); // exceeds 50 characters

        // valid tag names
        assertTrue(Tag.isValidTagName("switches")); // alphabets only
        assertTrue(Tag.isValidTagName("s")); // single character
        assertTrue(Tag.isValidTagName("123")); // numbers only
        assertTrue(Tag.isValidTagName("cherry_mx")); // underscore allowed
        assertTrue(Tag.isValidTagName("FR4-plate")); // hyphen allowed
        assertTrue(Tag.isValidTagName("lubed/filmed")); // slash allowed
        assertTrue(Tag.isValidTagName("60%")); // percent sign allowed
        assertTrue(Tag.isValidTagName("75%")); // percent sign allowed
        assertTrue(Tag.isValidTagName("mil-max_7305")); // combined symbols
        assertTrue(Tag.isValidTagName("a".repeat(50))); // exactly 50 characters
    }

    @Test
    public void equals() {
        Tag tag = new Tag("switches");

        // same values -> returns true
        assertTrue(tag.equals(new Tag("switches")));

        // same values different case -> returns true
        assertTrue(tag.equals(new Tag("Switches")));
        assertTrue(tag.equals(new Tag("SWITCHES")));

        // same object -> returns true
        assertTrue(tag.equals(tag));

        // null -> returns false
        assertFalse(tag.equals(null));

        // different types -> returns false
        assertFalse(tag.equals(5.0f));

        // different values -> returns false
        assertFalse(tag.equals(new Tag("keycaps")));
    }

    @Test
    public void hashcode() {
        Tag tag = new Tag("switches");

        // same values -> returns same hashcode
        assertEquals(tag.hashCode(), new Tag("switches").hashCode());

        // same values different case -> returns same hashcode
        assertEquals(tag.hashCode(), new Tag("Switches").hashCode());
        assertEquals(tag.hashCode(), new Tag("SWITCHES").hashCode());

        // different values -> returns different hashcode
        assertNotEquals(tag.hashCode(), new Tag("keycaps").hashCode());
    }

    @Test
    public void toStringMethod() {
        Tag tag = new Tag("Switches");
        assertEquals("[switches]", tag.toString());
    }

}
