package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;

public class RoleTest {

    @Test
    public void equalsAndHashCode_unicodeVariants_workInHashSets() {
        // EP: Greek sigma, Turkish I and supplementary case pairs must satisfy the equality/hash contract.
        String[][] pairs = {{"\u03C3", "\u03C2"}, {"I", "\u0131"}, {"i", "\u0130"},
            {"\uD801\uDC00", "\uD801\uDC28"}};
        for (String[] pair : pairs) {
            Role first = new Role(pair[0]);
            Role second = new Role(pair[1]);
            assertEquals(first, second);
            assertEquals(first.hashCode(), second.hashCode());
            HashSet<Role> roles = new HashSet<>(List.of(first));
            assertTrue(roles.contains(second));
            assertFalse(roles.add(second));
            assertTrue(roles.remove(second));
            assertTrue(roles.isEmpty());
            assertEquals(pair[0], first.value); // comparison must not alter display casing
        }
    }

    @Test
    public void hashCode_turkishDefaultLocale_doesNotChangeResult() {
        Role role = new Role("SUPPLIER");
        int originalHash = role.hashCode();
        Locale originalLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            assertEquals(originalHash, role.hashCode());
            assertEquals(new Role("supplier").hashCode(), role.hashCode());
        } finally {
            Locale.setDefault(originalLocale);
        }
    }

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Role(null));
    }

    @Test
    public void constructor_invalidRole_throwsIllegalArgumentException() {
        String invalidRole = "";
        assertThrows(IllegalArgumentException.class, () -> new Role(invalidRole));
    }

    @Test
    public void isValidRole() {
        // null role
        assertThrows(NullPointerException.class, () -> Role.isValidRole(null));

        // invalid roles
        assertFalse(Role.isValidRole("")); // empty string
        assertFalse(Role.isValidRole(" ")); // spaces only

        // valid roles
        // EP: punctuation in any position and surrounding spaces are accepted.
        assertTrue(Role.isValidRole("Supplier "));
        assertTrue(Role.isValidRole(" Supplier"));
        assertTrue(Role.isValidRole(" Supplier "));
        assertTrue(Role.isValidRole("-Supplier"));
        assertTrue(Role.isValidRole("Supplier-"));
        assertTrue(Role.isValidRole("-Partner"));
        assertTrue(Role.isValidRole("Supplier/"));
        assertTrue(Role.isValidRole("Supplier!"));
        assertTrue(Role.isValidRole("@Supplier"));
        assertTrue(Role.isValidRole("Sales*Manager"));
        assertTrue(Role.isValidRole("Supplier"));
        assertTrue(Role.isValidRole("Sales Manager")); // internal spaces allowed
        assertTrue(Role.isValidRole("Logistics-Partner")); // hyphen allowed
        assertTrue(Role.isValidRole("R&D Specialist")); // ampersand allowed
        assertTrue(Role.isValidRole("Logistics/Shipping")); // slash allowed
        assertTrue(Role.isValidRole("Maker's Assistant")); // apostrophe allowed
        assertTrue(Role.isValidRole("Sales   Manager")); // multiple internal spaces allowed
        assertTrue(Role.isValidRole("S")); // one character
    }

    @Test
    public void constructor_collapsesWhitespaceAndPreservesCasing() {
        assertEquals("supplier", new Role("supplier").value);
        assertEquals("SUPPLIER", new Role("SUPPLIER").value);
        assertEquals("PCB Supplier", new Role("PCB Supplier").value);
        assertEquals("RGB Designer", new Role("RGB Designer").value);
        assertEquals("sales manager", new Role("sales manager").value);
        assertEquals("Sales Manager", new Role("Sales   Manager").value);
        assertEquals("logistics-partner", new Role("logistics-partner").value);
        assertEquals("Part-Time Staff", new Role("Part-Time Staff").value);
        assertEquals("R&D Specialist", new Role("R&D Specialist").value);
        assertEquals("Maker's Assistant", new Role("Maker's Assistant").value);
        assertEquals("logistics/shipping", new Role("logistics/shipping").value);
    }

    @Test
    public void equals() {
        Role role = new Role("Supplier");

        // same values -> returns true
        assertTrue(role.equals(new Role("Supplier")));

        // same values different case -> returns true
        assertTrue(role.equals(new Role("supplier")));
        assertTrue(role.equals(new Role("SUPPLIER")));

        // acronyms with different case and whitespace -> returns true
        assertTrue(new Role("PCB Supplier").equals(new Role("pcb supplier")));
        assertTrue(new Role("PCB   Supplier").equals(new Role("pcb supplier")));

        // same object -> returns true
        assertTrue(role.equals(role));

        // null -> returns false
        assertFalse(role.equals(null));

        // different types -> returns false
        assertFalse(role.equals(5.0f));

        // different values -> returns false
        assertFalse(role.equals(new Role("Employee")));
    }

    @Test
    public void hashcode() {
        Role role = new Role("Supplier");

        // same values -> returns same hashcode
        assertEquals(role.hashCode(), new Role("Supplier").hashCode());

        // same values different case -> returns same hashcode
        assertEquals(role.hashCode(), new Role("supplier").hashCode());
        assertEquals(role.hashCode(), new Role("SUPPLIER").hashCode());

        // acronyms with different case -> returns same hashcode
        assertEquals(new Role("PCB Supplier").hashCode(), new Role("pcb supplier").hashCode());

        // different values -> returns different hashcode
        assertNotEquals(role.hashCode(), new Role("Employee").hashCode());
    }

    @Test
    public void toStringMethod() {
        Role role = new Role("Supplier");
        assertEquals("Supplier", role.toString());
    }

    @Test
    public void constructor_expandedText_normalizesSpacingAndPreservesContent() {
        // EP: Unicode, punctuation, backslashes and surrounding Unicode whitespace.
        String[] texts = {"John A. Tan", "Jos\u00E9 Tan", "\u738B\u5C0F\u660E", "Supplier (PCB)",
            "62.5g", "PCB v1.6", "Switches (5-pin)", "C:\\Parts", "!", "a\u00A0b", "a\u2003b"};
        for (String text : texts) {
            assertTrue(Role.isValidRole("\u2003 " + text + " \u2003"));
            assertEquals(text, new Role("\u2003 " + text + " \u2003").value);
        }
        assertEquals("PCB v1.6", new Role(" PCB   v1.6 ").value);
    }

    @Test
    public void constructor_blankInvisibleOrInternalProhibitedCharacters_throwsIllegalArgumentException() {
        // EP: no meaningful text, internal ISO controls, and Unicode line/paragraph separators.
        String[] texts = {"", " ", "\u00A0", "\u200B\u2060", " \u200B\u00A0\u2003 ",
            "a\tb", "a\nb", "a\rb", "a\u0000b", "a\u0085b", "a\u2028b", "a\u2029b"};
        for (String text : texts) {
            assertFalse(Role.isValidRole(text));
            assertThrows(IllegalArgumentException.class, Role.MESSAGE_CONSTRAINTS, () -> new Role(text));
        }
    }

    @Test
    public void constructor_longText_hasNoMaximumLength() {
        // EP: text longer than the tag and follow-up limits remains valid.
        String text = "a".repeat(1000);
        assertEquals(text, new Role(text).value);
    }
}
