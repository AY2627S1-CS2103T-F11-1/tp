package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class NameTest {

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
        assertFalse(Name.isValidName("^")); // only non-alphanumeric characters
        assertFalse(Name.isValidName("peter*")); // contains unsupported punctuation
        assertFalse(Name.isValidName(" David")); // leading space
        assertFalse(Name.isValidName("David ")); // trailing space
        assertFalse(Name.isValidName("David\tTan")); // internal tab
        assertFalse(Name.isValidName("David\nTan")); // internal newline
        assertFalse(Name.isValidName("-David"));
        assertFalse(Name.isValidName("David-"));
        assertFalse(Name.isValidName("/David"));
        assertFalse(Name.isValidName("David/"));
        assertFalse(Name.isValidName("&David"));
        assertFalse(Name.isValidName("David&"));
        assertFalse(Name.isValidName("'David"));
        assertFalse(Name.isValidName("David'"));
        assertFalse(Name.isValidName("- / & '")); // punctuation only

        // valid name
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
}
