package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class RoleTest {

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
        assertFalse(Role.isValidRole("Supplier ")); // trailing whitespace
        assertFalse(Role.isValidRole(" Supplier")); // leading whitespace
        assertFalse(Role.isValidRole(" Supplier ")); // leading and trailing whitespace
        assertFalse(Role.isValidRole("-Supplier")); // starts with hyphen
        assertFalse(Role.isValidRole("Supplier-")); // ends with hyphen
        assertFalse(Role.isValidRole("-Partner")); // starts with punctuation
        assertFalse(Role.isValidRole("Supplier/")); // ends with punctuation
        assertFalse(Role.isValidRole("Supplier!")); // disallowed punctuation
        assertFalse(Role.isValidRole("@Supplier")); // disallowed symbol
        assertFalse(Role.isValidRole("Sales*Manager")); // disallowed symbol

        // valid roles
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
}
