package seedu.address.model.tag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;

public class TagTest {

    @Test
    public void equalsAndHashCode_unicodeVariants_deduplicateAndRemoveFromHashSets() {
        // EP: case-equivalent Unicode spellings must deduplicate and remain removable through either spelling.
        String[][] pairs = {{"\u03C3", "\u03C2"}, {"I", "\u0131"}, {"i", "\u0130"},
            {"\uD801\uDC00", "\uD801\uDC28"}};
        for (String[] pair : pairs) {
            Tag first = new Tag(pair[0]);
            Tag second = new Tag(pair[1]);
            assertEquals(first, second);
            assertEquals(first.hashCode(), second.hashCode());
            HashSet<Tag> tags = new HashSet<>(List.of(first));
            assertTrue(tags.contains(second));
            assertFalse(tags.add(second));
            assertTrue(tags.remove(second));
            assertTrue(tags.isEmpty());
            assertEquals(pair[0], first.tagName); // preserve the first stored spelling
        }
    }

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
        assertFalse(Tag.isValidTagName("a".repeat(51))); // exceeds 50 characters

        // valid tag names
        // EP: punctuation in any position and surrounding spaces are accepted.
        assertTrue(Tag.isValidTagName(" switches"));
        assertTrue(Tag.isValidTagName("switches "));
        assertTrue(Tag.isValidTagName("-tag"));
        assertTrue(Tag.isValidTagName("/tag"));
        assertTrue(Tag.isValidTagName("&tag"));
        assertTrue(Tag.isValidTagName("'tag"));
        assertTrue(Tag.isValidTagName("tag-"));
        assertTrue(Tag.isValidTagName("tag/"));
        assertTrue(Tag.isValidTagName("tag&"));
        assertTrue(Tag.isValidTagName("tag'"));
        assertTrue(Tag.isValidTagName("_tag"));
        assertTrue(Tag.isValidTagName("cherry_mx"));
        assertTrue(Tag.isValidTagName("%60"));
        assertTrue(Tag.isValidTagName("60%"));
        assertTrue(Tag.isValidTagName("#friend"));
        assertTrue(Tag.isValidTagName("hubby*"));
        assertTrue(Tag.isValidTagName("tag@domain"));
        assertTrue(Tag.isValidTagName("tag!test"));
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


    @Test
    public void constructor_expandedText_normalizesSpacingAndPreservesContent() {
        // EP: Unicode, punctuation, backslashes and surrounding Unicode whitespace.
        String[] texts = {"John A. Tan", "Jos\u00E9 Tan", "\u738B\u5C0F\u660E", "Supplier (PCB)",
            "62.5g", "PCB v1.6", "Switches (5-pin)", "C:\\Parts", "!", "a\u00A0b", "a\u2003b"};
        for (String text : texts) {
            assertTrue(Tag.isValidTagName("\u2003 " + text + " \u2003"));
            assertEquals(text, new Tag("\u2003 " + text + " \u2003").tagName);
        }
        assertEquals("PCB v1.6", new Tag(" PCB   v1.6 ").tagName);
    }

    @Test
    public void constructor_blankInvisibleOrInternalProhibitedCharacters_throwsIllegalArgumentException() {
        // EP: no meaningful text, internal ISO controls, and Unicode line/paragraph separators.
        String[] texts = {"", " ", "\u00A0", "\u200B\u2060", " \u200B\u00A0\u2003 ",
            "a\tb", "a\nb", "a\rb", "a\u0000b", "a\u0085b", "a\u2028b", "a\u2029b"};
        for (String text : texts) {
            assertFalse(Tag.isValidTagName(text));
            assertThrows(IllegalArgumentException.class, Tag.MESSAGE_CONSTRAINTS, () -> new Tag(text));
        }
    }

    @Test
    public void isValidTagName_lengthCheckedAfterNormalization() {
        // BVA: 49, 50 and 51 Java UTF-16 code units after trimming/collapsing spaces.
        for (int length : new int[] {49, 50}) {
            String normalized = "a".repeat(length - 2) + " b";
            String raw = " " + "a".repeat(length - 2) + "    b ";
            assertTrue(Tag.isValidTagName(raw));
            assertEquals(normalized, new Tag(raw).tagName);
        }
        String tooLong = " " + "a".repeat(49) + "    b ";
        assertFalse(Tag.isValidTagName(tooLong));
        assertThrows(IllegalArgumentException.class, Tag.MESSAGE_CONSTRAINTS, () -> new Tag(tooLong));
        // BVA: supplementary characters count as two String.length() units each.
        assertTrue(Tag.isValidTagName("\uD83D\uDE00".repeat(25)));
        assertFalse(Tag.isValidTagName("\uD83D\uDE00".repeat(25) + "a"));
    }
}
