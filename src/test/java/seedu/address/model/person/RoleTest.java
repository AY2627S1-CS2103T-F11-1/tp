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

        // valid roles
        assertTrue(Role.isValidRole("Supplier"));
        assertTrue(Role.isValidRole("Sales Manager")); // internal spaces allowed
        assertTrue(Role.isValidRole("Logistics-Partner")); // punctuation allowed
        assertTrue(Role.isValidRole("S")); // one character
    }

    @Test
    public void equals() {
        Role role = new Role("Supplier");

        // same values -> returns true
        assertTrue(role.equals(new Role("Supplier")));

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

        // different values -> returns different hashcode
        assertNotEquals(role.hashCode(), new Role("Employee").hashCode());
    }

    @Test
    public void toStringMethod() {
        Role role = new Role("Supplier");
        assertEquals("Supplier", role.toString());
    }
}
