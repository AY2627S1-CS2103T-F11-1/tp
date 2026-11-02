package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;

public class EmailTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Email(null));
    }

    @Test
    public void constructor_invalidEmail_throwsIllegalArgumentException() {
        String invalidEmail = "";
        assertThrows(IllegalArgumentException.class, () -> new Email(invalidEmail));
    }

    @Test
    public void isValidEmail() {
        // null email
        assertThrows(NullPointerException.class, () -> Email.isValidEmail(null));

        // blank email
        assertFalse(Email.isValidEmail("")); // empty string
        assertFalse(Email.isValidEmail(" ")); // spaces only

        // missing parts
        assertFalse(Email.isValidEmail("@example.com")); // missing local part
        assertFalse(Email.isValidEmail("peterjackexample.com")); // missing '@' symbol
        assertFalse(Email.isValidEmail("peterjack@")); // missing domain name

        // invalid parts
        assertFalse(Email.isValidEmail("peterjack@-")); // invalid domain name
        assertFalse(Email.isValidEmail("peterjack@exam_ple.com")); // underscore in domain name
        assertFalse(Email.isValidEmail("peter jack@example.com")); // spaces in local part
        assertFalse(Email.isValidEmail("peterjack@exam ple.com")); // spaces in domain name
        assertFalse(Email.isValidEmail(" peterjack@example.com")); // leading space
        assertFalse(Email.isValidEmail("peterjack@example.com ")); // trailing space
        assertFalse(Email.isValidEmail("peterjack@@example.com")); // double '@' symbol
        assertFalse(Email.isValidEmail("peter@jack@example.com")); // '@' symbol in local part
        assertFalse(Email.isValidEmail("peterjack@example@com")); // '@' symbol in domain name
        assertFalse(Email.isValidEmail("peterjack@.example.com")); // domain name starts with a period
        assertFalse(Email.isValidEmail("peterjack@example.com.")); // domain name ends with a period
        assertFalse(Email.isValidEmail("peterjack@-example.com")); // domain name starts with a hyphen
        assertFalse(Email.isValidEmail("peterjack@example.com-")); // domain name ends with a hyphen

        // valid email
        // EP: WHATWG syntax allows local punctuation at the edges, repeated dots and one-character domain labels.
        assertTrue(Email.isValidEmail("-peterjack@example.com"));
        assertTrue(Email.isValidEmail("peterjack-@example.com"));
        assertTrue(Email.isValidEmail("peter..jack@example.com"));
        assertTrue(Email.isValidEmail("peterjack@example.c"));
        assertTrue(Email.isValidEmail("PeterJack_1190@example.com")); // underscore in local part
        assertTrue(Email.isValidEmail("PeterJack.1190@example.com")); // period in local part
        assertTrue(Email.isValidEmail("PeterJack+1190@example.com")); // '+' symbol in local part
        assertTrue(Email.isValidEmail("PeterJack-1190@example.com")); // hyphen in local part
        assertTrue(Email.isValidEmail("a@bc")); // minimal
        assertTrue(Email.isValidEmail("test@localhost")); // alphabets only
        assertTrue(Email.isValidEmail("123@145")); // numeric local part and domain name
        assertTrue(Email.isValidEmail("a1+be.d@example1.com")); // mixture of alphanumeric and special characters
        assertTrue(Email.isValidEmail("peter_jack@very-very-very-long-example.com")); // long domain name
        assertTrue(Email.isValidEmail("if.you.dream.it_you.can.do.it@example.com")); // long local part
        assertTrue(Email.isValidEmail("e1234567@u.nus.edu")); // more than one period in domain
    }

    @Test
    public void constructor_supportedSyntax_preservesSpelling() {
        // EP: reported examples, subdomains, punycode and the WHATWG local-part punctuation set.
        String[] emails = {"o'connor@example.com", "sales@keyboard--shop.com", "O'Connor@Keyboard--Shop.COM",
            "a+b_c-d.e@example.com", "a!#$%&'*+/=?^_`{|}~-z@example.com", "a@shop.example.com",
            "sales@xn--bcher-kva.example", "a@localhost", ".a.@example.com", "a..b@example.com"};
        for (String email : emails) {
            assertTrue(Email.isValidEmail(email));
            assertEquals(email, new Email(email).value);
        }
        // EP: every allowed special character can occur alone or at either end of a local part.
        for (char character : ".!#$%&'*+/=?^_`{|}~-".toCharArray()) {
            assertTrue(Email.isValidEmail(character + "@example.com"));
            assertTrue(Email.isValidEmail(character + "name" + character + "@example.com"));
        }
    }

    @Test
    public void constructor_unsupportedSyntax_throwsConstraintError() {
        // EP: blank, invisible, missing/multiple @, unsupported local/domain syntax and non-ASCII text.
        String[] emails = {"", " ", "\u200B", "@example.com", "user@", "userexample.com", "a@@example.com",
            "a b@example.com", "a\\b@example.com", "a,b@example.com", "a(b)@example.com",
            "\"a b\"@example.com", "a@[127.0.0.1]", "a@[IPv6:::1]", "Jos\u00E9@example.com",
            "a@b\u00FCcher.example", "a@.example.com", "a@example..com", "a@example.com.",
            "a@-example.com", "a@example-.com", "a@sub.-example.com", "a@sub.example-.com", "a@exam_ple.com",
            "a@exam ple.com", "a\tb@example.com", "a\nb@example.com", "a\u0085b@example.com",
            "a\u2028b@example.com", "a\u2029b@example.com", "a@example.com\n", "a@example.com\u2028"};
        for (String email : emails) {
            assertFalse(Email.isValidEmail(email), email);
            assertThrows(IllegalArgumentException.class, Email.MESSAGE_CONSTRAINTS, () -> new Email(email));
        }
    }

    @Test
    public void isValidEmail_domainLabelLength_enforcesWhatwgBoundaries() {
        // BVA: domain labels contain 1 to 63 characters, checked at every position in the domain.
        for (int length : new int[] {1, 2, 62, 63}) {
            String label = "a".repeat(length);
            assertTrue(Email.isValidEmail("a@" + label));
            assertTrue(Email.isValidEmail("a@" + label + ".com"));
            assertTrue(Email.isValidEmail("a@example." + label + ".com"));
            assertTrue(Email.isValidEmail("a@example." + label));
        }
        for (int length : new int[] {0, 64}) {
            String label = "a".repeat(length);
            assertFalse(Email.isValidEmail("a@" + label));
            assertFalse(Email.isValidEmail("a@" + label + ".com"));
            assertFalse(Email.isValidEmail("a@example." + label + ".com"));
            assertFalse(Email.isValidEmail("a@example." + label));
        }
        // BVA: a maximum-length label may contain consecutive hyphens internally.
        assertTrue(Email.isValidEmail("a@a" + "-".repeat(61) + "b.com"));
    }

    @Test
    public void equalsAndHashCode_caseVariants_workUnderTurkishLocale() {
        // EP: punctuation and ASCII I case variants must agree in equality and hashing across locales.
        Email email = new Email("O'CONNOR@KEYBOARD--SHOP.COM");
        Email lowerCase = new Email("o'connor@keyboard--shop.com");
        Email asciiI = new Email("INFO@EXAMPLE.COM");
        int originalHash = asciiI.hashCode();
        Locale originalLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            assertEquals(email, lowerCase);
            assertEquals(email.hashCode(), lowerCase.hashCode());
            assertEquals(originalHash, asciiI.hashCode());
            assertEquals(asciiI.hashCode(), new Email("info@example.com").hashCode());
            HashSet<Email> emails = new HashSet<>(List.of(email, asciiI));
            assertFalse(emails.add(lowerCase));
            assertTrue(emails.remove(new Email("info@example.com")));
            assertEquals(1, emails.size());
        } finally {
            Locale.setDefault(originalLocale);
        }
    }

    @Test
    public void equals() {
        Email email = new Email("valid@email");

        // same values -> returns true
        assertTrue(email.equals(new Email("valid@email")));

        // same values different case -> returns true
        assertTrue(email.equals(new Email("VALID@EMAIL")));
        assertTrue(email.equals(new Email("Valid@Email")));

        // same object -> returns true
        assertTrue(email.equals(email));

        // null -> returns false
        assertFalse(email.equals(null));

        // different types -> returns false
        assertFalse(email.equals(5.0f));

        // different values -> returns false
        assertFalse(email.equals(new Email("other.valid@email")));
    }

    @Test
    public void hashCodeMethod() {
        Email email = new Email("valid@email");

        // same value -> returns same hashcode
        assertEquals(email.hashCode(), new Email("valid@email").hashCode());

        // same value different case -> returns same hashcode
        assertEquals(email.hashCode(), new Email("VALID@EMAIL").hashCode());
        assertEquals(email.hashCode(), new Email("Valid@Email").hashCode());
    }
}
