package seedu.address.model.tag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.Locale;

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
    public void constructor_collapsesWhitespaceAndPreservesCasing() {
        assertEquals("switches", new Tag("switches").tagName);
        assertEquals("Switches", new Tag("Switches").tagName);
        assertEquals("CHERRY-MX", new Tag("CHERRY-MX").tagName);
        assertEquals("FR4-Plate", new Tag("FR4-Plate").tagName);
        assertEquals("R&D Specialist", new Tag("R&D Specialist").tagName);
        assertEquals("Maker's Keycap", new Tag("Maker's Keycap").tagName);
        assertEquals("Tactile Switch", new Tag("Tactile   Switch").tagName);
        assertEquals("lubed/filmed", new Tag("lubed/filmed").tagName);
    }

    @Test
    public void isValidTagName() {
        // null tag name
        assertThrows(NullPointerException.class, () -> Tag.isValidTagName(null));

        // invalid tag names
        assertFalse(Tag.isValidTagName("")); // empty string
        assertFalse(Tag.isValidTagName(" ")); // spaces only
        assertFalse(Tag.isValidTagName(" switches")); // leading spaces
        assertFalse(Tag.isValidTagName("switches ")); // trailing spaces
        assertFalse(Tag.isValidTagName("-tag")); // starts with hyphen
        assertFalse(Tag.isValidTagName("/tag")); // starts with slash
        assertFalse(Tag.isValidTagName("&tag")); // starts with ampersand
        assertFalse(Tag.isValidTagName("'tag")); // starts with apostrophe
        assertFalse(Tag.isValidTagName("tag-")); // ends with hyphen
        assertFalse(Tag.isValidTagName("tag/")); // ends with slash
        assertFalse(Tag.isValidTagName("tag&")); // ends with ampersand
        assertFalse(Tag.isValidTagName("tag'")); // ends with apostrophe
        assertFalse(Tag.isValidTagName("_tag")); // underscore disallowed
        assertFalse(Tag.isValidTagName("cherry_mx")); // underscore disallowed
        assertFalse(Tag.isValidTagName("%60")); // percent disallowed
        assertFalse(Tag.isValidTagName("60%")); // percent disallowed
        assertFalse(Tag.isValidTagName("#friend")); // disallowed symbol '#'
        assertFalse(Tag.isValidTagName("hubby*")); // disallowed symbol '*'
        assertFalse(Tag.isValidTagName("tag@domain")); // disallowed symbol '@'
        assertFalse(Tag.isValidTagName("tag!test")); // disallowed symbol '!'
        assertFalse(Tag.isValidTagName("a".repeat(51))); // exceeds 50 characters

        // valid tag names
        assertTrue(Tag.isValidTagName("switches")); // alphabets only
        assertTrue(Tag.isValidTagName("friend"));
        assertTrue(Tag.isValidTagName("colleague"));
        assertTrue(Tag.isValidTagName("s")); // single character
        assertTrue(Tag.isValidTagName("123")); // numbers only
        assertTrue(Tag.isValidTagName("cherry mx")); // internal space allowed
        assertTrue(Tag.isValidTagName("Tactile   Switch")); // multiple internal spaces allowed
        assertTrue(Tag.isValidTagName("FR4-plate")); // hyphen allowed
        assertTrue(Tag.isValidTagName("lubed/filmed")); // slash allowed
        assertTrue(Tag.isValidTagName("R&D")); // ampersand allowed
        assertTrue(Tag.isValidTagName("Maker's Keycap")); // apostrophe allowed
        assertTrue(Tag.isValidTagName("lubed/filmed-switch")); // combined symbols
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

        // same values with different internal whitespace -> returns true
        assertTrue(new Tag("cherry   mx").equals(new Tag("cherry mx")));

        // same acronym different case -> returns true
        assertTrue(new Tag("FR4-Plate").equals(new Tag("fr4-plate")));

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

        // same acronym different case -> returns same hashcode
        assertEquals(new Tag("FR4-Plate").hashCode(), new Tag("fr4-plate").hashCode());

        // different values -> returns different hashcode
        assertNotEquals(tag.hashCode(), new Tag("keycaps").hashCode());
    }

    @Test
    public void hashCode_caseInsensitiveUnderTurkishLocale() {
        Locale originalLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            assertEquals(new Tag("trainee").hashCode(), new Tag("TRAINEE").hashCode());
        } finally {
            Locale.setDefault(originalLocale);
        }
    }

    @Test
    public void toStringMethod() {
        Tag tag = new Tag("Switches");
        assertEquals("[Switches]", tag.toString());
    }

}
