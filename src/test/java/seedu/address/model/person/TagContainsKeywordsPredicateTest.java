package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class TagContainsKeywordsPredicateTest {
    @Test
    public void test_unicodeCaseVariants_matchSubstrings() {
        // EP: accents, sigma variants, Turkish I, long S, Kelvin sign and supplementary case pairs.
        String[][] pairs = {{"Jos\u00E9", "JOS\u00C9"}, {"\u03C3", "\u03C2"},
            {"\u039F\u03A3", "\u03BF\u03C2"}, {"I", "\u0131"}, {"i", "\u0130"},
            {"S", "\u017F"}, {"K", "\u212A"}, {"\uD801\uDC00", "\uD801\uDC28"}};
        for (String[] pair : pairs) {
            Person first = new PersonBuilder().withTags("Prefix " + pair[0] + " Suffix").build();
            Person second = new PersonBuilder().withTags("Prefix " + pair[1] + " Suffix").build();
            assertTrue(new TagContainsKeywordsPredicate(List.of(pair[1])).test(first));
            assertTrue(new TagContainsKeywordsPredicate(List.of(pair[0])).test(second));
        }
        // EP: case-insensitive matching does not remove accents or expand one letter into multiple letters.
        Person accented = new PersonBuilder().withTags("Jos\u00E9").build();
        assertFalse(new TagContainsKeywordsPredicate(List.of("Jose")).test(accented));
        Person sharpS = new PersonBuilder().withTags("\u00DF").build();
        assertFalse(new TagContainsKeywordsPredicate(List.of("SS")).test(sharpS));
        Person chinese = new PersonBuilder().withTags("\u738B\u5C0F\u660E").build();
        assertTrue(new TagContainsKeywordsPredicate(List.of("\u5C0F")).test(chinese));
        assertFalse(new TagContainsKeywordsPredicate(List.of("\u674E")).test(chinese));
    }

    @Test
    public void test_turkishDefaultLocale_matchesUnicodeCaseVariants() {
        // EP: changing the default locale must not change case-insensitive substring results.
        Locale originalLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            Person person = new PersonBuilder().withTags("\u0130STANBUL \u03C2").build();
            assertTrue(new TagContainsKeywordsPredicate(List.of("istan")).test(person));
            assertTrue(new TagContainsKeywordsPredicate(List.of("\u03C3")).test(person));
        } finally {
            Locale.setDefault(originalLocale);
        }
    }

    @Test
    public void equals() {
        List<String> firstPredicateKeywordList = List.of("first");
        List<String> secondPredicateKeywordList = List.of("first", "second");

        TagContainsKeywordsPredicate firstPredicate = new TagContainsKeywordsPredicate(firstPredicateKeywordList);
        TagContainsKeywordsPredicate secondPredicate = new TagContainsKeywordsPredicate(secondPredicateKeywordList);

        // same object -> returns true
        assertTrue(firstPredicate.equals(firstPredicate));

        // same values -> returns true
        TagContainsKeywordsPredicate firstPredicateCopy = new TagContainsKeywordsPredicate(firstPredicateKeywordList);
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
        assertThrows(NullPointerException.class, () -> new TagContainsKeywordsPredicate(null));
    }

    @Test
    public void test_nullPerson_throwsAssertionError() {
        TagContainsKeywordsPredicate predicate =
                new TagContainsKeywordsPredicate(List.of("friend"));

        assertThrows(AssertionError.class, () -> predicate.test(null));
    }

    @Test
    public void constructor_blankKeywords_storesOnlyNonblankKeywords() {
        TagContainsKeywordsPredicate predicate =
                new TagContainsKeywordsPredicate(List.of("", "RGB", "", "PCB"));
        TagContainsKeywordsPredicate expectedPredicate =
                new TagContainsKeywordsPredicate(List.of("RGB", "PCB"));
        assertEquals(expectedPredicate, predicate);

        String expectedString =
                TagContainsKeywordsPredicate.class.getCanonicalName() + "{keywords=[RGB, PCB]}";
        assertEquals(expectedString, predicate.toString());
    }

    @Test
    public void test_tagsContainKeywords_returnsTrue() {
        // One keyword
        TagContainsKeywordsPredicate predicate = new TagContainsKeywordsPredicate(List.of("friend"));
        assertTrue(predicate.test(new PersonBuilder().withTags("friend").build()));

        // Multiple keywords
        predicate = new TagContainsKeywordsPredicate(List.of("friend", "colleague"));
        assertTrue(predicate.test(new PersonBuilder().withTags("friend").build()));

        // Multiple tags on person - matches first tag
        predicate = new TagContainsKeywordsPredicate(List.of("friend"));
        assertTrue(predicate.test(new PersonBuilder().withTags("friend", "colleague").build()));

        // Multiple tags on person - matches second tag
        predicate = new TagContainsKeywordsPredicate(List.of("colleague"));
        assertTrue(predicate.test(new PersonBuilder().withTags("friend", "colleague").build()));

        // Mixed-case keywords
        predicate = new TagContainsKeywordsPredicate(List.of("fRiEnD"));
        assertTrue(predicate.test(new PersonBuilder().withTags("friend").build()));
    }

    @Test
    public void test_tagsDoNotContainKeywords_returnsFalse() {
        // Zero keywords
        TagContainsKeywordsPredicate predicate = new TagContainsKeywordsPredicate(List.of());
        assertFalse(predicate.test(new PersonBuilder().withTags("friend").build()));

        // Non-matching keyword
        predicate = new TagContainsKeywordsPredicate(List.of("family"));
        assertFalse(predicate.test(new PersonBuilder().withTags("friend").build()));

        // Person with no tags
        predicate = new TagContainsKeywordsPredicate(List.of("friend"));
        assertFalse(predicate.test(new PersonBuilder().withTags().build()));

        // Keywords match name, phone, email, address, and role, but do not match tags
        predicate = new TagContainsKeywordsPredicate(
                List.of("Alice", "12345", "alice@email.com", "Main", "Street", "Supplier"));

        assertFalse(predicate.test(new PersonBuilder().withName("Alice").withPhone("12345")
                .withEmail("alice@email.com").withAddress("Main Street").withRole("Supplier")
                .withTags("friend").build()));
    }

    @Test
    public void test_blankKeywords_returnsFalse() {
        Person person = new PersonBuilder().withTags("friend").build();

        assertFalse(new TagContainsKeywordsPredicate(List.of("")).test(person));
        assertFalse(new TagContainsKeywordsPredicate(List.of(" ", "\t", "\n")).test(person));
    }

    @Test
    public void test_mixedBlankAndNonBlankKeywords_ignoresBlanks() {
        Person person = new PersonBuilder().withTags("friend").build();

        assertTrue(new TagContainsKeywordsPredicate(List.of("", " ", "friend")).test(person));
        assertFalse(new TagContainsKeywordsPredicate(List.of("", "\t", "colleague")).test(person));
    }

    @Test
    public void test_tagContainsSubstring_returnsTrue() {
        Person person = new PersonBuilder().withTags("colleague").build();

        // Prefix of a tag
        assertTrue(new TagContainsKeywordsPredicate(List.of("col")).test(person));

        // Middle of a tag
        assertTrue(new TagContainsKeywordsPredicate(List.of("llea")).test(person));

        // Suffix of a tag
        assertTrue(new TagContainsKeywordsPredicate(List.of("gue")).test(person));

        // Mixed-case substring
        assertTrue(new TagContainsKeywordsPredicate(List.of("CoLl")).test(person));

        // Single-character substring
        assertTrue(new TagContainsKeywordsPredicate(List.of("c")).test(person));
    }

    @Test
    public void test_substringMatchesAnyKeyword_returnsTrue() {
        TagContainsKeywordsPredicate firstPredicate = new TagContainsKeywordsPredicate(List.of("frie", "colle"));
        TagContainsKeywordsPredicate secondPredicate =
                new TagContainsKeywordsPredicate(List.of("12345", "supplier", "owes", "bo"));

        // Matches first keyword
        assertTrue(firstPredicate.test(new PersonBuilder().withTags("friends").build()));

        // Matches second keyword
        assertTrue(firstPredicate.test(new PersonBuilder().withTags("colleagues").build()));

        // Neither keyword matches
        assertFalse(firstPredicate.test(new PersonBuilder().withTags("family").build()));

        // Matches one of the keywords across multiple tags
        assertTrue(secondPredicate.test(new PersonBuilder().withTags("owesMoney", "neighbour").build()));
    }

    @Test
    public void toStringMethod() {
        List<String> keywords = List.of("keyword1", "keyword2");
        TagContainsKeywordsPredicate predicate = new TagContainsKeywordsPredicate(keywords);

        String expected = TagContainsKeywordsPredicate.class.getCanonicalName() + "{keywords=" + keywords + "}";

        assertEquals(expected, predicate.toString());
    }
}
