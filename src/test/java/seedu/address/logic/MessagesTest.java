package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ROLE;

import org.junit.jupiter.api.Test;

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
}
