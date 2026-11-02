package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class NameTest {

    @Test
    public void equals_unicodeCaseVariants_remainsCaseSensitive() {
        // EP: Unicode searching may ignore case, but name identity and stored spelling remain case-sensitive.
        Name name = new Name("Jos\u00E9 \u03C3");
        assertEquals(name, new Name("Jos\u00E9 \u03C3"));
        assertEquals(name.hashCode(), new Name("Jos\u00E9 \u03C3").hashCode());
        assertFalse(name.equals(new Name("JOS\u00C9 \u03C2")));
        assertEquals("Jos\u00E9 \u03C3", name.fullName);
    }

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Name(null));
    }

    @Test
    public void constructor_invalidName_throwsIllegalArgumentException() {
        String invalidName = "";
        assertThrows(IllegalArgumentException.class, () -> new Name(invalidName));
    }

    @Test
    public void constructor_collapsesSpacesAndPreservesCasing() {
        assertEquals("Anne-Marie O'Connor", new Name("Anne-Marie   O'Connor").fullName);
        assertEquals("ACME & Sons", new Name("ACME   &   Sons").fullName);
        assertEquals("David s/o Me", new Name("David  s/o   Me").fullName);
        assertEquals("anne marie", new Name("anne   marie").fullName);
        assertEquals("ANNE MARIE", new Name("ANNE   MARIE").fullName);
        assertEquals("Anne-Marie", new Name("Anne-Marie").fullName);
    }

    @Test
    public void isValidName() {
        // null name
        assertThrows(NullPointerException.class, () -> Name.isValidName(null));

        // invalid name
        assertFalse(Name.isValidName("")); // empty string
        assertFalse(Name.isValidName(" ")); // spaces only
        assertFalse(Name.isValidName("David\tTan")); // internal tab
        assertFalse(Name.isValidName("David\nTan")); // internal newline

        // valid name
        // EP: punctuation in any position and surrounding spaces are accepted.
        assertTrue(Name.isValidName("^"));
        assertTrue(Name.isValidName("peter*"));
        assertTrue(Name.isValidName(" David"));
        assertTrue(Name.isValidName("David "));
        assertTrue(Name.isValidName("-David"));
        assertTrue(Name.isValidName("David-"));
        assertTrue(Name.isValidName("/David"));
        assertTrue(Name.isValidName("David/"));
        assertTrue(Name.isValidName("&David"));
        assertTrue(Name.isValidName("David&"));
        assertTrue(Name.isValidName("'David"));
        assertTrue(Name.isValidName("David'"));
        assertTrue(Name.isValidName("- / & '"));
        assertTrue(Name.isValidName("peter jack")); // alphabets only
        assertTrue(Name.isValidName("12345")); // numbers only
        assertTrue(Name.isValidName("peter the 2nd")); // alphanumeric characters
        assertTrue(Name.isValidName("Capital Tan")); // with capital letters
        assertTrue(Name.isValidName("David Roger Jackson Ray Jr 2nd")); // long names
        assertTrue(Name.isValidName("Anne-Marie")); // hyphen
        assertTrue(Name.isValidName("O'Connor")); // apostrophe
        assertTrue(Name.isValidName("ACME & Sons")); // ampersand
        assertTrue(Name.isValidName("David s/o Me")); // slash
        assertTrue(Name.isValidName("David   Tan")); // multiple internal spaces
        assertTrue(Name.isValidName("D")); // single character
    }

    @Test
    public void equals() {
        Name name = new Name("Valid Name");

        // same values -> returns true
        assertTrue(name.equals(new Name("Valid Name")));

        // same value with different spacing -> returns true after normalisation
        Name extraSpaces = new Name("Valid   Name");
        assertEquals(name, extraSpaces);
        assertEquals(name.hashCode(), extraSpaces.hashCode());

        // name equality remains case-sensitive
        assertFalse(name.equals(new Name("valid name")));

        // same object -> returns true
        assertTrue(name.equals(name));

        // null -> returns false
        assertFalse(name.equals(null));

        // different types -> returns false
        assertFalse(name.equals(5.0f));

        // different values -> returns false
        assertFalse(name.equals(new Name("Other Valid Name")));
    }

    @Test
    public void constructor_expandedText_normalizesSpacingAndPreservesContent() {
        // EP: Unicode, punctuation, backslashes and surrounding Unicode whitespace.
        String[] texts = {"John A. Tan", "Jos\u00E9 Tan", "\u738B\u5C0F\u660E", "Supplier (PCB)",
            "62.5g", "PCB v1.6", "Switches (5-pin)", "C:\\Parts", "!", "a\u00A0b", "a\u2003b"};
        for (String text : texts) {
            assertTrue(Name.isValidName("\u2003 " + text + " \u2003"));
            assertEquals(text, new Name("\u2003 " + text + " \u2003").fullName);
        }
        assertEquals("PCB v1.6", new Name(" PCB   v1.6 ").fullName);
    }

    @Test
    public void constructor_blankInvisibleOrInternalProhibitedCharacters_throwsIllegalArgumentException() {
        // EP: no meaningful text, internal ISO controls, and Unicode line/paragraph separators.
        String[] texts = {"", " ", "\u00A0", "\u200B\u2060", " \u200B\u00A0\u2003 ",
            "a\tb", "a\nb", "a\rb", "a\u0000b", "a\u0085b", "a\u2028b", "a\u2029b"};
        for (String text : texts) {
            assertFalse(Name.isValidName(text));
            assertThrows(IllegalArgumentException.class, Name.MESSAGE_CONSTRAINTS, () -> new Name(text));
        }
    }

    @Test
    public void constructor_longText_hasNoMaximumLength() {
        // EP: text longer than the tag and follow-up limits remains valid.
        String text = "a".repeat(1000);
        assertEquals(text, new Name(text).fullName);
    }
}
