package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ROLE;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.Prefix;

public class MessagesTest {

    @Test
    public void getErrorMessageForDuplicatePrefixes() {
        // single duplicate prefix
        String expectedMessage = Messages.MESSAGE_DUPLICATE_FIELDS + "n/";
        assertEquals(expectedMessage, Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME));
    }

    @Test
    public void getErrorMessageForMissingPrefixes() {
        // single missing prefix
        String expectedSingle = Messages.MESSAGE_MISSING_PARAMETERS + "r/";
        assertEquals(expectedSingle, Messages.getErrorMessageForMissingPrefixes(PREFIX_ROLE));

        // multiple missing prefixes in deterministic order
        String expectedMultiple = Messages.MESSAGE_MISSING_PARAMETERS + "n/ p/ e/ a/ r/";
        assertEquals(expectedMultiple, Messages.getErrorMessageForMissingPrefixes(
                PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_ADDRESS, PREFIX_ROLE));
    }

    @Test
    public void getErrorMessageForDuplicatePrefixes_emptyPrefixes_throwsAssertionError() {
        assertThrows(AssertionError.class, () -> Messages.getErrorMessageForDuplicatePrefixes());
    }

    @Test
    public void getErrorMessageForMissingPrefixes_emptyPrefixes_throwsAssertionError() {
        assertThrows(AssertionError.class, () -> Messages.getErrorMessageForMissingPrefixes());
    }

    @Test
    public void getErrorMessageForUnrecognizedPrefixes() {
        // single unrecognized prefix
        String expectedSingle = Messages.MESSAGE_UNRECOGNIZED_PREFIXES + "x/";
        assertEquals(expectedSingle, Messages.getErrorMessageForUnrecognizedPrefixes(new Prefix("x/")));

        // multiple unrecognized prefixes
        String expectedMultiple = Messages.MESSAGE_UNRECOGNIZED_PREFIXES + "x/ y/";
        assertEquals(expectedMultiple, Messages.getErrorMessageForUnrecognizedPrefixes(
                new Prefix("x/"), new Prefix("y/")));

        // duplicate unrecognized prefixes de-duplicated
        String expectedDeduplicated = Messages.MESSAGE_UNRECOGNIZED_PREFIXES + "x/";
        assertEquals(expectedDeduplicated, Messages.getErrorMessageForUnrecognizedPrefixes(
                new Prefix("x/"), new Prefix("x/")));
    }

    @Test
    public void getErrorMessageForUnrecognizedPrefixes_emptyPrefixes_throwsAssertionError() {
        assertThrows(AssertionError.class, () -> Messages.getErrorMessageForUnrecognizedPrefixes());
    }
}
