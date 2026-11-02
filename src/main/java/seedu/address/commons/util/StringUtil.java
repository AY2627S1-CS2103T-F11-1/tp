package seedu.address.commons.util;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;

/**
 * Helper functions for handling strings.
 */
public class StringUtil {

    /**
     * Returns whether text contains meaningful content and no control characters or Unicode line/paragraph separators.
     * Whitespace, Unicode spaces and formatting characters alone do not count as meaningful content.
     */
    public static boolean isValidText(String text) {
        requireNonNull(text);
        return text.codePoints().anyMatch(codePoint -> !Character.isWhitespace(codePoint)
                && !Character.isSpaceChar(codePoint)
                && Character.getType(codePoint) != Character.FORMAT)
                && text.codePoints().noneMatch(StringUtil::isDisallowedTextCharacter);
    }

    /**
     * Returns whether a code point is an ISO control character or a Unicode line/paragraph separator.
     */
    private static boolean isDisallowedTextCharacter(int codePoint) {
        return Character.isISOControl(codePoint)
                || Character.getType(codePoint) == Character.LINE_SEPARATOR
                || Character.getType(codePoint) == Character.PARAGRAPH_SEPARATOR;
    }

    /**
     * Trims surrounding whitespace and collapses repeated ordinary spaces, preserving other internal characters.
     */
    public static String normalizeSpaces(String text) {
        requireNonNull(text);
        return text.strip().replaceAll(" {2,}", " ");
    }

    /**
     * Returns a locale-independent comparison form consistent with {@link String#equalsIgnoreCase(String)}.
     * Maps Unicode code points individually, including supplementary characters, without changing stored text.
     */
    public static String normalizeCase(String text) {
        requireNonNull(text);
        return text.codePoints().map(codePoint -> Character.toLowerCase(Character.toUpperCase(codePoint)))
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
    }

    /**
     * Returns true if the {@code sentence} contains the {@code word}.
     *   Ignores case, but a full word match is required.
     *   <br>examples:<pre>
     *       containsWordIgnoreCase("ABc def", "abc") == true
     *       containsWordIgnoreCase("ABc def", "DEF") == true
     *       containsWordIgnoreCase("ABc def", "AB") == false //not a full word match
     *       </pre>
     * @param sentence cannot be null
     * @param word cannot be null, cannot be empty, must be a single word
     */
    public static boolean containsWordIgnoreCase(String sentence, String word) {
        requireNonNull(sentence);
        requireNonNull(word);

        String preppedWord = word.trim();
        checkArgument(!preppedWord.isEmpty(), "Word parameter cannot be empty");
        checkArgument(preppedWord.split("\\s+").length == 1, "Word parameter should be a single word");

        String preppedSentence = sentence;
        String[] wordsInPreppedSentence = preppedSentence.split("\\s+");

        return Arrays.stream(wordsInPreppedSentence)
                .anyMatch(preppedWord::equalsIgnoreCase);
    }

    /**
     * Returns a detailed message of {@code t}, including the stack trace.
     */
    public static String getDetails(Throwable t) {
        requireNonNull(t);
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return t.getMessage() + "\n" + sw.toString();
    }

    /**
     * Returns true if {@code s} represents a non-zero unsigned integer
     * e.g. 1, 2, 3, ..., {@code Integer.MAX_VALUE} <br>
     * Will return false for any other non-null string input
     * e.g. empty string, "-1", "0", "+1", and " 2 " (untrimmed), "3 0" (contains whitespace), "1 a" (contains letters)
     * @throws NullPointerException if {@code s} is null.
     */
    public static boolean isNonZeroUnsignedInteger(String s) {
        requireNonNull(s);

        try {
            int value = Integer.parseInt(s);
            return value > 0 && !s.startsWith("+"); // "+1" is successfully parsed by Integer#parseInt(String)
        } catch (NumberFormatException nfe) {
            return false;
        }
    }
}
