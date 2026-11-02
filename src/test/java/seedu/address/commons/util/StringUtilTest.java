package seedu.address.commons.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.FileNotFoundException;
import java.util.Locale;

import org.junit.jupiter.api.Test;

public class StringUtilTest {

    @Test
    public void normalizeCase_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> StringUtil.normalizeCase(null));
    }

    @Test
    public void normalizeCase_unicodePairs_agreesWithEqualsIgnoreCase() {
        // EP: ASCII, accents, Greek sigma, Turkish I, long S, Kelvin sign and supplementary Deseret letters.
        String[][] pairs = {{"Supplier", "SUPPLIER"}, {"Jos\u00E9", "JOS\u00C9"},
            {"\u03A3", "\u03C2"}, {"\u03C3", "\u03C2"}, {"\u039F\u03A3", "\u03BF\u03C2"},
            {"I", "\u0131"}, {"i", "\u0130"}, {"S", "\u017F"}, {"K", "\u212A"},
            {"\uD801\uDC00", "\uD801\uDC28"}};
        for (String[] pair : pairs) {
            assertTrue(pair[0].equalsIgnoreCase(pair[1]));
            assertEquals(StringUtil.normalizeCase(pair[0]), StringUtil.normalizeCase(pair[1]));
        }
    }

    @Test
    public void normalizeCase_preservesNonCaseDistinctions() {
        // BVA: empty text and one uncased character; EP: spaces, punctuation, backslashes and emoji.
        for (String text : new String[] {"", "\u738B", "\u738B  (PCB) \\ / \uD83D\uDE00"}) {
            assertEquals(text.replace("PCB", "pcb"), StringUtil.normalizeCase(text));
        }
        // EP: no accent removal, canonical composition, or multi-character case expansion.
        assertFalse(StringUtil.normalizeCase("Jos\u00E9").equals(StringUtil.normalizeCase("Jose")));
        assertFalse(StringUtil.normalizeCase("\u00E9").equals(StringUtil.normalizeCase("e\u0301")));
        assertFalse(StringUtil.normalizeCase("\u00DF").equals(StringUtil.normalizeCase("SS")));
    }

    @Test
    public void normalizeCase_turkishDefaultLocale_doesNotChangeResult() {
        // EP: locale-sensitive I variants must follow equalsIgnoreCase regardless of the default locale.
        Locale originalLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            assertEquals("iiii", StringUtil.normalizeCase("I\u0130i\u0131"));
        } finally {
            Locale.setDefault(originalLocale);
        }
    }

    @Test
    public void isValidText_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> StringUtil.isValidText(null));
    }

    @Test
    public void isValidText_blankOrInvisibleOnly_returnsFalse() {
        // EP: empty (BVA: zero characters), whitespace, Unicode spaces and formatting-only text.
        String[] invalidTexts = {"", " ", "\t\n", "\u2003", "\u00A0", "\u200B", "\u2060",
            " \u200B\u00A0\u2003\u2060 "};
        for (String text : invalidTexts) {
            assertFalse(StringUtil.isValidText(text));
        }
    }

    @Test
    public void isValidText_prohibitedCharacters_returnsFalse() {
        // EP: all ISO controls, including C1 controls, and Unicode line/paragraph separators.
        for (int codePoint = 0; codePoint <= 0x9F; codePoint++) {
            if (Character.isISOControl(codePoint)) {
                assertFalse(StringUtil.isValidText("a" + (char) codePoint + "b"));
            }
        }
        assertFalse(StringUtil.isValidText("a\u2028b"));
        assertFalse(StringUtil.isValidText("a\u2029b"));
        // EP: validation itself does not trim prohibited characters at the edges.
        assertFalse(StringUtil.isValidText("\tText"));
        assertFalse(StringUtil.isValidText("Text\n"));
    }

    @Test
    public void isValidText_meaningfulContent_returnsTrue() {
        // EP: Unicode, punctuation, literal backslashes, and formatting/spaces alongside visible content.
        // BVA: one meaningful character is sufficient; this helper imposes no maximum length.
        String[] validTexts = {"!", "John A. Tan", "Jos\u00E9 Tan", "\u738B\u5C0F\u660E", "Supplier (PCB)",
            "62.5g", "PCB v1.6", "Switches (5-pin)", "C:\\Parts", "a/b", "\uD83D\uDE00",
            " \u200BText\u00A0\u2060 ", "a".repeat(201)};
        for (String text : validTexts) {
            assertTrue(StringUtil.isValidText(text));
        }
    }

    @Test
    public void normalizeSpaces_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> StringUtil.normalizeSpaces(null));
    }

    @Test
    public void normalizeSpaces_trimsAndCollapsesOrdinarySpaces() {
        // EP: empty/blank, surrounding Unicode whitespace, and repeated ordinary spaces.
        assertEquals("", StringUtil.normalizeSpaces(""));
        assertEquals("", StringUtil.normalizeSpaces("   "));
        assertEquals("John A. Tan", StringUtil.normalizeSpaces("\u2003 John   A.  Tan \u2003"));
        // BVA: a single ordinary space remains a single space.
        assertEquals("PCB v1.6", StringUtil.normalizeSpaces("PCB v1.6"));
    }

    @Test
    public void normalizeSpaces_preservesOtherInternalCharacters() {
        // EP: casing, Unicode, punctuation and literal backslashes remain unchanged.
        String text = "Jos\u00E9 (PCB) \\ Parts / \u738B";
        assertEquals(text, StringUtil.normalizeSpaces(text));
        // EP: internal non-ordinary spaces and prohibited characters are not silently collapsed.
        String internalCharacters = "A\tB\nC\u00A0D\u2003E\u200BF\u2028G\u2029H";
        assertEquals(internalCharacters, StringUtil.normalizeSpaces(internalCharacters));
    }

    //---------------- Tests for isNonZeroUnsignedInteger --------------------------------------

    @Test
    public void isNonZeroUnsignedInteger() {

        // EP: empty strings
        assertFalse(StringUtil.isNonZeroUnsignedInteger("")); // Boundary value
        assertFalse(StringUtil.isNonZeroUnsignedInteger("  "));

        // EP: not a number
        assertFalse(StringUtil.isNonZeroUnsignedInteger("a"));
        assertFalse(StringUtil.isNonZeroUnsignedInteger("aaa"));

        // EP: zero
        assertFalse(StringUtil.isNonZeroUnsignedInteger("0"));

        // EP: zero as prefix
        assertTrue(StringUtil.isNonZeroUnsignedInteger("01"));

        // EP: signed numbers
        assertFalse(StringUtil.isNonZeroUnsignedInteger("-1"));
        assertFalse(StringUtil.isNonZeroUnsignedInteger("+1"));

        // EP: numbers with white space
        assertFalse(StringUtil.isNonZeroUnsignedInteger(" 10 ")); // Leading/trailing spaces
        assertFalse(StringUtil.isNonZeroUnsignedInteger("1 0")); // Spaces in the middle

        // EP: number larger than Integer.MAX_VALUE
        assertFalse(StringUtil.isNonZeroUnsignedInteger(Long.toString(Integer.MAX_VALUE + 1)));

        // EP: valid numbers, should return true
        assertTrue(StringUtil.isNonZeroUnsignedInteger("1")); // Boundary value
        assertTrue(StringUtil.isNonZeroUnsignedInteger("10"));
    }


    //---------------- Tests for containsWordIgnoreCase --------------------------------------

    /*
     * Invalid equivalence partitions for word: null, empty, multiple words
     * Invalid equivalence partitions for sentence: null
     * The four test cases below test one invalid input at a time.
     */

    @Test
    public void containsWordIgnoreCase_nullWord_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> StringUtil.containsWordIgnoreCase("typical sentence", null));
    }

    @Test
    public void containsWordIgnoreCase_emptyWord_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, "Word parameter cannot be empty", ()
            -> StringUtil.containsWordIgnoreCase("typical sentence", "  "));
    }

    @Test
    public void containsWordIgnoreCase_multipleWords_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, "Word parameter should be a single word", ()
            -> StringUtil.containsWordIgnoreCase("typical sentence", "aaa BBB"));
    }

    @Test
    public void containsWordIgnoreCase_nullSentence_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> StringUtil.containsWordIgnoreCase(null, "abc"));
    }

    /*
     * Valid equivalence partitions for word:
     *   - any word
     *   - word containing symbols/numbers
     *   - word with leading/trailing spaces
     *
     * Valid equivalence partitions for sentence:
     *   - empty string
     *   - one word
     *   - multiple words
     *   - sentence with extra spaces
     *
     * Possible scenarios returning true:
     *   - matches first word in sentence
     *   - last word in sentence
     *   - middle word in sentence
     *   - matches multiple words
     *
     * Possible scenarios returning false:
     *   - query word matches part of a sentence word
     *   - sentence word matches part of the query word
     *
     * The test method below tries to verify all above with a reasonably low number of test cases.
     */

    @Test
    public void containsWordIgnoreCase_validInputs_correctResult() {

        // Empty sentence
        assertFalse(StringUtil.containsWordIgnoreCase("", "abc")); // Boundary case
        assertFalse(StringUtil.containsWordIgnoreCase("    ", "123"));

        // Matches a partial word only
        assertFalse(StringUtil.containsWordIgnoreCase("aaa bbb ccc", "bb")); // Sentence word bigger than query word
        assertFalse(StringUtil.containsWordIgnoreCase("aaa bbb ccc", "bbbb")); // Query word bigger than sentence word

        // Matches word in the sentence, different upper/lower case letters
        assertTrue(StringUtil.containsWordIgnoreCase("aaa bBb ccc", "Bbb")); // First word (boundary case)
        assertTrue(StringUtil.containsWordIgnoreCase("aaa bBb ccc@1", "CCc@1")); // Last word (boundary case)
        assertTrue(StringUtil.containsWordIgnoreCase("  AAA   bBb   ccc  ", "aaa")); // Sentence has extra spaces
        assertTrue(StringUtil.containsWordIgnoreCase("Aaa", "aaa")); // Only one word in sentence (boundary case)
        assertTrue(StringUtil.containsWordIgnoreCase("aaa bbb ccc", "  ccc  ")); // Leading/trailing spaces

        // Matches multiple words in sentence
        assertTrue(StringUtil.containsWordIgnoreCase("AAA bBb ccc  bbb", "bbB"));
    }

    //---------------- Tests for getDetails --------------------------------------

    /*
     * Equivalence Partitions: null, valid throwable object
     */

    @Test
    public void getDetails_exceptionGiven() {
        assertTrue(StringUtil.getDetails(new FileNotFoundException("file not found"))
            .contains("java.io.FileNotFoundException: file not found"));
    }

    @Test
    public void getDetails_nullGiven_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> StringUtil.getDetails(null));
    }

}
