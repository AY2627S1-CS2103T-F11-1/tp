package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class AddressTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Address(null));
    }

    @Test
    public void constructor_invalidAddress_throwsIllegalArgumentException() {
        String invalidAddress = "";
        assertThrows(IllegalArgumentException.class, () -> new Address(invalidAddress));
    }

    @Test
    public void isValidAddress() {
        // null address
        assertThrows(NullPointerException.class, () -> Address.isValidAddress(null));

        // invalid addresses
        assertFalse(Address.isValidAddress("")); // empty string
        assertFalse(Address.isValidAddress(" ")); // spaces only

        // valid addresses
        assertTrue(Address.isValidAddress("Blk 456, Den Road, #01-355"));
        assertTrue(Address.isValidAddress("-")); // one character
        assertTrue(Address.isValidAddress("Leng Inc; 1234 Market St; San Francisco CA 2349879; USA")); // long address
    }

    @Test
    public void isValidAddress_onlyWhitespaceOrFormattingCharacters_returnsFalse() {
        String[] blankAddresses = {
            "", " ", "   ", "\t", "\u200B", "\u00A0", "\u2060", "\u2003",
            "\u200B\u00A0\u2060\u2003", " \u200B \u00A0\t\u2060 \u2003 "
        };

        for (String address : blankAddresses) {
            assertFalse(Address.isValidAddress(address));
            assertThrows(IllegalArgumentException.class, Address.MESSAGE_CONSTRAINTS, () -> new Address(address));
        }
    }

    @Test
    public void isValidAddress_visibleTextWithWhitespaceOrFormattingCharacters_returnsTrue() {
        String[] invisibleCharacters = {"\u200B", "\u00A0", "\u2060", "\u2003", "\u200B\u00A0\u2060\u2003"};

        for (String characters : invisibleCharacters) {
            String[] addresses = {
                characters + "123 Main St", "123" + characters + " Main St", "123 Main St" + characters
            };
            for (String address : addresses) {
                assertTrue(Address.isValidAddress(address));
                assertEquals(address, new Address(address).value);
            }
        }
    }

    @Test
    public void equals() {
        Address address = new Address("Valid Address");

        // same values -> returns true
        assertTrue(address.equals(new Address("Valid Address")));

        // same object -> returns true
        assertTrue(address.equals(address));

        // null -> returns false
        assertFalse(address.equals(null));

        // different types -> returns false
        assertFalse(address.equals(5.0f));

        // different values -> returns false
        assertFalse(address.equals(new Address("Other Valid Address")));
    }
}
