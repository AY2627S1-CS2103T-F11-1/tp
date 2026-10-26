package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

public class FollowUpTest {

    private static final LocalDate VALID_DATE = LocalDate.of(2026, 11, 1);
    private static final String VALID_DESCRIPTION = "Confirm PCB quotation";

    private static final int CODEPOINT_LIMIT = 0x9F;
    private static final String UNICODE_LINE_SEPARATOR_STRING = "\u2028";
    private static final String UNICODE_PARAGRAPH_SEPARATOR_STRING = "\u2029";

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new FollowUp(null, VALID_DESCRIPTION));
        assertThrows(NullPointerException.class, () -> new FollowUp(VALID_DATE, null));
    }

    @Test
    public void constructor_supportedDates_acceptsPastAndYearBoundaries() {
        LocalDate[] dates = {
            LocalDate.of(0, 1, 1),
            LocalDate.of(2000, 2, 29),
            VALID_DATE,
            LocalDate.of(9999, 12, 31)
        };

        for (LocalDate date : dates) {
            assertTrue(FollowUp.isValidDueDate(date));
            assertEquals(date, new FollowUp(date, VALID_DESCRIPTION).getDueDate());
        }
    }

    @Test
    public void constructor_unsupportedYears_throwsIllegalArgumentException() {
        int[] invalidYears = new int[] {-1, 10000};

        for (int year : invalidYears) {
            LocalDate date = LocalDate.of(year, 1, 1);
            assertFalse(FollowUp.isValidDueDate(date));
            assertThrows(IllegalArgumentException.class, () -> new FollowUp(date, VALID_DESCRIPTION));
        }
    }

    @Test
    public void constructor_validDescription_preservesContentAndTrimsEdges() {
        FollowUp followUp = new FollowUp(VALID_DATE, "   \tConfirm  PCB quotation — 报价 \uD83D\uDE00\n ");

        assertEquals(VALID_DATE, followUp.getDueDate());
        assertEquals("Confirm  PCB quotation — 报价 \uD83D\uDE00", followUp.getDescription());
    }

    @Test
    public void constructor_descriptionLengthBoundary_validatesAfterTrimming() {
        String maxLength = "a".repeat(FollowUp.MAX_DESCRIPTION_LENGTH);

        assertEquals(maxLength, new FollowUp(VALID_DATE, " " + maxLength + " ").getDescription());
        assertThrows(IllegalArgumentException.class, () -> new FollowUp(VALID_DATE, maxLength + "a"));
    }

    @Test
    public void constructor_blankDescriptions_throwsIllegalArgumentException() {
        String[] inputs = new String[] {"", " ", "\t\n", "\u2003"};
        for (String description : inputs) {
            assertThrows(IllegalArgumentException.class, () -> new FollowUp(VALID_DATE, description));
        }
    }

    @Test
    public void constructor_internalControlCharacters_throwsIllegalArgumentException() {
        // Exhaustively cover the ISO control-character ranges.
        for (int codePoint = 0; codePoint <= CODEPOINT_LIMIT; codePoint++) {
            if (Character.isISOControl(codePoint)) {
                String description = "Before" + Character.toString(codePoint) + "After";
                assertFalse(FollowUp.isValidDescription(description));
                assertThrows(IllegalArgumentException.class, () -> new FollowUp(VALID_DATE, description));
            }
        }
    }

    @Test
    public void constructor_internalUnicodeSeparators_throwsIllegalArgumentException() {
        String[] separators = new String[] {UNICODE_LINE_SEPARATOR_STRING, UNICODE_PARAGRAPH_SEPARATOR_STRING};

        for (String separator : separators) {
            String description = "Before" + separator + "After";
            assertFalse(FollowUp.isValidDescription(description));
            assertThrows(IllegalArgumentException.class, () -> new FollowUp(VALID_DATE, description));
        }
    }

    @Test
    public void isValidDescription_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> FollowUp.isValidDescription(null));
    }

    @Test
    public void isValidDescription_validInputs_returnsTrue() {
        assertTrue(FollowUp.isValidDescription("x"));
        assertTrue(FollowUp.isValidDescription("Confirm quotation: $200 / 100 PCBs"));
        assertTrue(FollowUp.isValidDescription("确认报价 — 🙂"));
        assertTrue(FollowUp.isValidDescription("  Confirm  quotation  "));
        assertTrue(FollowUp.isValidDescription("a".repeat(FollowUp.MAX_DESCRIPTION_LENGTH)));
    }

    @Test
    public void isValidDescription_invalidInputs_returnsFalse() {
        assertFalse(FollowUp.isValidDescription(""));
        assertFalse(FollowUp.isValidDescription("   "));
        assertFalse(FollowUp.isValidDescription("a".repeat(FollowUp.MAX_DESCRIPTION_LENGTH + 1)));
        assertFalse(FollowUp.isValidDescription("Test" + UNICODE_PARAGRAPH_SEPARATOR_STRING + "String"));
    }

    @Test
    public void isValidDueDate_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> FollowUp.isValidDueDate(null));
    }

    @Test
    public void isValidDueDate_validInputs_returnsTrue() {
        LocalDate[] dates = {
            LocalDate.of(0, 1, 1),
            LocalDate.of(2000, 2, 29),
            VALID_DATE,
            LocalDate.of(9999, 12, 31)
        };

        for (LocalDate date : dates) {
            assertTrue(FollowUp.isValidDueDate(date));
        }
    }

    @Test
    public void isValidDueDate_invalidInputs_returnsFalse() {
        int[] invalidYears = new int[] {-1, 10000};

        for (int year : invalidYears) {
            LocalDate date = LocalDate.of(year, 1, 1);
            assertFalse(FollowUp.isValidDueDate(date));
        }
    }

    @Test
    public void equals() {
        FollowUp followUp = new FollowUp(VALID_DATE, VALID_DESCRIPTION);
        FollowUp equalCopy = new FollowUp(VALID_DATE, " " + VALID_DESCRIPTION + " ");

        // same object -> returns true
        assertEquals(followUp, followUp);

        // different object, same due date and description -> returns true
        assertEquals(followUp, equalCopy);
        assertEquals(equalCopy, followUp);
        assertEquals(followUp.hashCode(), equalCopy.hashCode());

        // different due date -> returns false
        assertNotEquals(followUp, new FollowUp(VALID_DATE.plusDays(1), VALID_DESCRIPTION));

        // different casing in description -> returns false
        assertNotEquals(followUp, new FollowUp(VALID_DATE, "confirm PCB quotation"));

        // different whitespace in description -> returns false
        assertNotEquals(followUp, new FollowUp(VALID_DATE, "Confirm  PCB quotation"));

        // null -> returns false
        assertNotEquals(followUp, null);

        // different types -> returns false
        assertNotEquals(followUp, VALID_DESCRIPTION);
    }

    @Test
    public void toStringMethod() {
        FollowUp followUp = new FollowUp(VALID_DATE, VALID_DESCRIPTION);
        String expected = FollowUp.class.getCanonicalName() + "{dueDate=" + followUp.getDueDate()
                + ", description=" + followUp.getDescription() + "}";
        assertEquals(expected, followUp.toString());
    }

}
