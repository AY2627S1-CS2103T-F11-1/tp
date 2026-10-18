package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class NameContainsKeywordsPredicateTest {

    @Test
    public void equals() {
        List<String> firstPredicateKeywordList = List.of("first");
        List<String> secondPredicateKeywordList = List.of("first", "second");

        NameContainsKeywordsPredicate firstPredicate = new NameContainsKeywordsPredicate(firstPredicateKeywordList);
        NameContainsKeywordsPredicate secondPredicate = new NameContainsKeywordsPredicate(secondPredicateKeywordList);

        // same object -> returns true
        assertTrue(firstPredicate.equals(firstPredicate));

        // same values -> returns true
        NameContainsKeywordsPredicate firstPredicateCopy = new NameContainsKeywordsPredicate(firstPredicateKeywordList);
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
        assertThrows(NullPointerException.class,
                () -> new NameContainsKeywordsPredicate(null));
    }

    @Test
    public void constructor_blankKeywords_storesOnlyNonblankKeywords() {
        NameContainsKeywordsPredicate predicate = new NameContainsKeywordsPredicate(List.of("", "Alice", "", "Bob"));
        NameContainsKeywordsPredicate expectedPredicate = new NameContainsKeywordsPredicate(List.of("Alice", "Bob"));
        assertEquals(expectedPredicate, predicate);

        String expectedString = NameContainsKeywordsPredicate.class.getCanonicalName() + "{keywords=[Alice, Bob]}";
        assertEquals(expectedString, predicate.toString());
    }

    @Test
    public void test_nameContainsKeywords_returnsTrue() {
        // One keyword
        NameContainsKeywordsPredicate predicate = new NameContainsKeywordsPredicate(List.of("Alice"));
        assertTrue(predicate.test(new PersonBuilder().withName("Alice Bob").build()));

        // Multiple keywords
        predicate = new NameContainsKeywordsPredicate(List.of("Alice", "Bob"));
        assertTrue(predicate.test(new PersonBuilder().withName("Alice Bob").build()));

        // Only one matching keyword
        predicate = new NameContainsKeywordsPredicate(List.of("Bob", "Carol"));
        assertTrue(predicate.test(new PersonBuilder().withName("Alice Carol").build()));

        // Mixed-case keywords
        predicate = new NameContainsKeywordsPredicate(List.of("aLIce", "bOB"));
        assertTrue(predicate.test(new PersonBuilder().withName("Alice Bob").build()));
    }

    @Test
    public void test_nameDoesNotContainKeywords_returnsFalse() {
        // Zero keywords
        NameContainsKeywordsPredicate predicate = new NameContainsKeywordsPredicate(List.of());
        assertFalse(predicate.test(new PersonBuilder().withName("Alice").build()));

        // Non-matching keyword
        predicate = new NameContainsKeywordsPredicate(List.of("Carol"));
        assertFalse(predicate.test(new PersonBuilder().withName("Alice Bob").build()));

        // Keywords match phone, email and address, but do not match name
        predicate = new NameContainsKeywordsPredicate(List.of("12345", "alice@email.com", "Main", "Street"));
        assertFalse(predicate.test(new PersonBuilder().withName("Alice").withPhone("12345")
                .withEmail("alice@email.com").withAddress("Main Street").build()));
    }

    @Test
    public void test_blankKeywords_returnsFalse() {
        Person person = new PersonBuilder().withName("Alice").build();

        assertFalse(new NameContainsKeywordsPredicate(List.of("")).test(person));
        assertFalse(new NameContainsKeywordsPredicate(List.of(" ", "\t", "\n")).test(person));
    }

    @Test
    public void test_mixedBlankAndNonblankKeywords_ignoresBlanks() {
        Person person = new PersonBuilder().withName("Alice").build();

        assertTrue(new NameContainsKeywordsPredicate(List.of("", " ", "Alice")).test(person));
        assertFalse(new NameContainsKeywordsPredicate(List.of("", "\t", "xyz")).test(person));
    }

    @Test
    public void test_nameContainsSubstring_returnsTrue() {
        Person person = new PersonBuilder().withName("Bella Poarch").build();

        // Prefix of a name
        assertTrue(new NameContainsKeywordsPredicate(List.of("bel")).test(person));
        assertTrue(new NameContainsKeywordsPredicate(List.of("po")).test(person));

        // Middle of a name
        assertTrue(new NameContainsKeywordsPredicate(List.of("ell")).test(person));
        assertTrue(new NameContainsKeywordsPredicate(List.of("oar")).test(person));

        // Suffix of a name
        assertTrue(new NameContainsKeywordsPredicate(List.of("la")).test(person));
        assertTrue(new NameContainsKeywordsPredicate(List.of("ch")).test(person));

        // Mixed-case substring
        assertTrue(new NameContainsKeywordsPredicate(List.of("BeL")).test(person));

        // Single-character substring
        assertTrue(new NameContainsKeywordsPredicate(List.of("p")).test(person));
    }

    @Test
    public void test_substringsMatchesAnyKeyword_returnsTrue() {
        NameContainsKeywordsPredicate firstPredicate = new NameContainsKeywordsPredicate(List.of("Bella", "p"));
        NameContainsKeywordsPredicate secondPredicate =
                new NameContainsKeywordsPredicate(List.of("12345", "alice@email.com", "Main", "p", "Street"));

        // Both keywords match
        assertTrue(firstPredicate.test(new PersonBuilder().withName("Bella Poarch").build()));

        // Only first keyword matches
        assertTrue(firstPredicate.test(new PersonBuilder().withName("Bella Tan").build()));

        // Only second keyword matches
        assertTrue(firstPredicate.test(new PersonBuilder().withName("Joseph Tan").build()));

        // Neither keyword matches
        assertFalse(firstPredicate.test(new PersonBuilder().withName("Alice Tan").build()));

        // Only one matching keyword
        assertTrue(secondPredicate.test(new PersonBuilder().withName("Joseph Tan").build()));
    }

    @Test
    public void toStringMethod() {
        List<String> keywords = List.of("keyword1", "keyword2");
        NameContainsKeywordsPredicate predicate = new NameContainsKeywordsPredicate(keywords);

        String expected = NameContainsKeywordsPredicate.class.getCanonicalName() + "{keywords=" + keywords + "}";
        assertEquals(expected, predicate.toString());
    }
}
