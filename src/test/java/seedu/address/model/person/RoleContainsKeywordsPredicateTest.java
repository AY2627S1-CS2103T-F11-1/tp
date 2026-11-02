package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class RoleContainsKeywordsPredicateTest {
    @Test
    public void test_unicodeCaseVariants_matchSubstrings() {
        // EP: accents, sigma variants, Turkish I, long S, Kelvin sign and supplementary case pairs.
        String[][] pairs = {{"Jos\u00E9", "JOS\u00C9"}, {"\u03C3", "\u03C2"},
            {"\u039F\u03A3", "\u03BF\u03C2"}, {"I", "\u0131"}, {"i", "\u0130"},
            {"S", "\u017F"}, {"K", "\u212A"}, {"\uD801\uDC00", "\uD801\uDC28"}};
        for (String[] pair : pairs) {
            Person first = new PersonBuilder().withRole("Prefix " + pair[0] + " Suffix").build();
            Person second = new PersonBuilder().withRole("Prefix " + pair[1] + " Suffix").build();
            assertTrue(new RoleContainsKeywordsPredicate(List.of(pair[1])).test(first));
            assertTrue(new RoleContainsKeywordsPredicate(List.of(pair[0])).test(second));
        }
        // EP: case-insensitive matching does not remove accents or expand one letter into multiple letters.
        Person accented = new PersonBuilder().withRole("Jos\u00E9").build();
        assertFalse(new RoleContainsKeywordsPredicate(List.of("Jose")).test(accented));
        Person sharpS = new PersonBuilder().withRole("\u00DF").build();
        assertFalse(new RoleContainsKeywordsPredicate(List.of("SS")).test(sharpS));
        Person chinese = new PersonBuilder().withRole("\u738B\u5C0F\u660E").build();
        assertTrue(new RoleContainsKeywordsPredicate(List.of("\u5C0F")).test(chinese));
        assertFalse(new RoleContainsKeywordsPredicate(List.of("\u674E")).test(chinese));
    }

    @Test
    public void test_turkishDefaultLocale_matchesUnicodeCaseVariants() {
        // EP: changing the default locale must not change case-insensitive substring results.
        Locale originalLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            Person person = new PersonBuilder().withRole("\u0130STANBUL \u03C2").build();
            assertTrue(new RoleContainsKeywordsPredicate(List.of("istan")).test(person));
            assertTrue(new RoleContainsKeywordsPredicate(List.of("\u03C3")).test(person));
        } finally {
            Locale.setDefault(originalLocale);
        }
    }

    @Test
    public void equals() {
        List<String> firstPredicateKeywordList = List.of("first");
        List<String> secondPredicateKeywordList = List.of("first", "second");

        RoleContainsKeywordsPredicate firstPredicate = new RoleContainsKeywordsPredicate(firstPredicateKeywordList);
        RoleContainsKeywordsPredicate secondPredicate = new RoleContainsKeywordsPredicate(secondPredicateKeywordList);

        // same object -> returns true
        assertTrue(firstPredicate.equals(firstPredicate));

        // same values -> returns true
        RoleContainsKeywordsPredicate firstPredicateCopy = new RoleContainsKeywordsPredicate(firstPredicateKeywordList);
        assertTrue(firstPredicate.equals(firstPredicateCopy));

        // different types -> returns false
        assertFalse(firstPredicate.equals(1));

        // null -> returns false
        assertFalse(firstPredicate.equals(null));

        // different keywords -> returns false
        assertFalse(firstPredicate.equals(secondPredicate));
    }

    @Test
    public void constructor_nullKeywords_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new RoleContainsKeywordsPredicate(null));
    }

    @Test
    public void test_nullPerson_throwsAssertionError() {
        RoleContainsKeywordsPredicate predicate =
                new RoleContainsKeywordsPredicate(List.of("Supplier"));

        assertThrows(AssertionError.class, () -> predicate.test(null));
    }

    @Test
    public void constructor_blankKeywords_storesOnlyNonblankKeywords() {
        RoleContainsKeywordsPredicate predicate =
                new RoleContainsKeywordsPredicate(List.of("", "Supplier", "", "Employee"));
        RoleContainsKeywordsPredicate expectedPredicate =
                new RoleContainsKeywordsPredicate(List.of("Supplier", "Employee"));
        assertEquals(expectedPredicate, predicate);

        String expectedString =
                RoleContainsKeywordsPredicate.class.getCanonicalName() + "{keywords=[Supplier, Employee]}";
        assertEquals(expectedString, predicate.toString());
    }

    @Test
    public void test_roleContainsKeywords_returnsTrue() {
        // One keyword
        RoleContainsKeywordsPredicate predicate = new RoleContainsKeywordsPredicate(List.of("Supplier"));
        assertTrue(predicate.test(new PersonBuilder().withRole("Supplier ABC").build()));

        // Multiple keywords
        predicate = new RoleContainsKeywordsPredicate(List.of("Supplier", "ABC"));
        assertTrue(predicate.test(new PersonBuilder().withRole("Supplier ABC").build()));

        // Only one matching keyword
        predicate = new RoleContainsKeywordsPredicate(List.of("Supplier", "Employee"));
        assertTrue(predicate.test(new PersonBuilder().withRole("Supplier ABC").build()));

        // Mixed-case keywords
        predicate = new RoleContainsKeywordsPredicate(List.of("sUpPLier", "aBc"));
        assertTrue(predicate.test(new PersonBuilder().withRole("Supplier ABC").build()));
    }

    @Test
    public void test_roleDoesNotContainKeywords_returnsFalse() {
        // Zero keywords
        RoleContainsKeywordsPredicate predicate = new RoleContainsKeywordsPredicate(List.of());
        assertFalse(predicate.test(new PersonBuilder().withRole("Supplier").build()));

        // Non-matching keyword
        predicate = new RoleContainsKeywordsPredicate(List.of("Supplier"));
        assertFalse(predicate.test(new PersonBuilder().withRole("Employee").build()));

        // Keywords match phone, email and address, but do not match role
        predicate = new RoleContainsKeywordsPredicate(List.of("Alice,", "12345", "alice@email.com", "Main", "Street"));
        assertFalse(predicate.test(new PersonBuilder().withName("Alice").withPhone("12345")
                .withEmail("alice@email.com").withAddress("Main Street").withRole("Supplier").build()));
    }

    @Test
    public void test_blankKeywords_returnsFalse() {
        Person person = new PersonBuilder().withRole("Supplier").build();

        assertFalse(new RoleContainsKeywordsPredicate(List.of("")).test(person));
        assertFalse(new RoleContainsKeywordsPredicate(List.of(" ", "\t", "\n")).test(person));
    }

    @Test
    public void test_mixedBlankAndNonBlankKeywords_ignoresBlanks() {
        Person person = new PersonBuilder().withRole("Supplier").build();

        assertTrue(new RoleContainsKeywordsPredicate(List.of("", " ", "Supplier")).test(person));
        assertFalse(new RoleContainsKeywordsPredicate(List.of("", "\t", "xyz")).test(person));
    }

    @Test
    public void test_roleContainsSubstring_returnsTrue() {
        Person person = new PersonBuilder().withRole("Supplier").build();

        // Prefix of a role
        assertTrue(new RoleContainsKeywordsPredicate(List.of("sup")).test(person));

        // Middle of a role
        assertTrue(new RoleContainsKeywordsPredicate(List.of("pli")).test(person));

        // Suffix of a role
        assertTrue(new RoleContainsKeywordsPredicate(List.of("er")).test(person));

        // Mixed-case substring
        assertTrue(new RoleContainsKeywordsPredicate(List.of("pPl")).test(person));

        // Single-character substring
        assertTrue(new RoleContainsKeywordsPredicate(List.of("p")).test(person));
    }

    @Test
    public void test_substringMatchesAnyKeyword_returnsTrue() {
        RoleContainsKeywordsPredicate firstPredicate = new RoleContainsKeywordsPredicate(List.of("Supp", "Distrib"));
        RoleContainsKeywordsPredicate secondPredicate =
                new RoleContainsKeywordsPredicate(List.of("12345", "Employee", "Friend", "Bo"));

        // Both keywords match
        assertTrue(firstPredicate.test(new PersonBuilder().withRole("Supplier Distributor").build()));

        // Only first keyword matches
        assertTrue(firstPredicate.test(new PersonBuilder().withRole("Supplier ABC").build()));

        // Only second keyword matches
        assertTrue(firstPredicate.test(new PersonBuilder().withRole("Distributor ABC").build()));

        // Neither keyword matches
        assertFalse(firstPredicate.test(new PersonBuilder().withRole("Employee").build()));

        // Only one matching keyword
        assertTrue(secondPredicate.test(new PersonBuilder().withRole("Boss").build()));
    }

    @Test
    public void toStringMethod() {
        List<String> keywords = List.of("keyword1", "keyword2");
        RoleContainsKeywordsPredicate predicate = new RoleContainsKeywordsPredicate(keywords);

        String expected = RoleContainsKeywordsPredicate.class.getCanonicalName() + "{keywords=" + keywords + "}";
        assertEquals(expected, predicate.toString());
    }
}
