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
                assertEquals(address.strip(), new Address(address).value);
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

    @Test
    public void constructor_trimsEdgesAndPreservesInternalSpacing() {
        // EP: Unicode/punctuation/backslashes, surrounding whitespace and repeated internal spaces.
        String address = "\u738B Street  1, C:\\Parts / #02-03";
        assertEquals(address, new Address("\u2003 " + address + " \u2003").value);
        assertEquals("a\u00A0b\u2003c", new Address("a\u00A0b\u2003c").value);
        // EP: no maximum address length.
        assertEquals("a".repeat(1000), new Address("a".repeat(1000)).value);
    }

    @Test
    public void constructor_internalProhibitedCharacters_throwsIllegalArgumentException() {
        // EP: internal ISO controls and Unicode line/paragraph separators.
        String[] texts = {"a\tb", "a\nb", "a\rb", "a\u0000b", "a\u0085b", "a\u2028b", "a\u2029b"};
        for (String text : texts) {
            assertFalse(Address.isValidAddress(text));
            assertThrows(IllegalArgumentException.class, Address.MESSAGE_CONSTRAINTS, () -> new Address(text));
        }
    }
}
