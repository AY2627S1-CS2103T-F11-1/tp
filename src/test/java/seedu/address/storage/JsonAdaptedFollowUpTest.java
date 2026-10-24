package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.commands.CommandTestUtil.VALID_FOLLOW_UP_DATE;
import static seedu.address.logic.commands.CommandTestUtil.VALID_FOLLOW_UP_DESCRIPTION;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.person.FollowUp;

public class JsonAdaptedFollowUpTest {

    @Test
    public void toModelType_validDetails_returnsFollowUp() throws Exception {
        JsonAdaptedFollowUp adapter =
                new JsonAdaptedFollowUp(VALID_FOLLOW_UP_DATE, VALID_FOLLOW_UP_DESCRIPTION);
        FollowUp expected =
                new FollowUp(LocalDate.parse(VALID_FOLLOW_UP_DATE), VALID_FOLLOW_UP_DESCRIPTION);

        assertEquals(expected, adapter.toModelType());
    }

    @Test
    public void toModelType_nullDate_throwsIllegalValueException() {
        JsonAdaptedFollowUp adapter =
                new JsonAdaptedFollowUp(null, VALID_FOLLOW_UP_DESCRIPTION);
        String expectedMessage = String.format(
                JsonAdaptedFollowUp.MISSING_FIELD_MESSAGE_FORMAT, "dueDate");

        assertThrows(IllegalValueException.class, expectedMessage, adapter::toModelType);
    }

    @Test
    public void toModelType_supportedDates_returnsFollowUp() throws Exception {
        String[] dates = new String[] {"0000-01-01", "2005-02-05", VALID_FOLLOW_UP_DATE, "9999-12-31"};

        for (String date : dates) {
            JsonAdaptedFollowUp adapter = new JsonAdaptedFollowUp(date, VALID_FOLLOW_UP_DESCRIPTION);
            assertEquals(LocalDate.parse(date), adapter.toModelType().getDueDate());
        }
    }

    @Test
    public void toModelType_invalidDate_throwsIllegalValueException() {
        String[] dates = {
            "", " ", "2026-1-01", "2026-01-1",
            "26-01-01", "10000-01-01", "-0001-01-01",
            "2026/01/01", "2026-01-01T00:00:00",
            " 2026-01-01", "2026-01-01 ",
            "2026-00-01", "2026-13-01", "2026-01-00",
            "2026-04-31", "2026-02-29", "1900-02-29"
        };

        for (String date : dates) {
            JsonAdaptedFollowUp adapter = new JsonAdaptedFollowUp(date, VALID_FOLLOW_UP_DESCRIPTION);
            assertThrows(IllegalValueException.class,
                    JsonAdaptedFollowUp.INVALID_DATE_MESSAGE, adapter::toModelType);
        }
    }

    @Test
    public void toModelType_nullDescription_throwsIllegalValueException() {
        JsonAdaptedFollowUp adapter = new JsonAdaptedFollowUp(VALID_FOLLOW_UP_DATE, null);
        String expectedMessage = String.format(JsonAdaptedFollowUp.MISSING_FIELD_MESSAGE_FORMAT, "description");

        assertThrows(IllegalValueException.class, expectedMessage, adapter::toModelType);
    }

    @Test
    public void toModelType_invalidDescription_throwsIllegalValueException() {
        String[] descriptions = {
            "", "   ",
            "a".repeat(FollowUp.MAX_DESCRIPTION_LENGTH + 1),
            "Before\nAfter",
            "Before\tAfter",
            "Before\u2028After",
            "Before\u2029After"
        };

        for (String description : descriptions) {
            JsonAdaptedFollowUp adapter = new JsonAdaptedFollowUp(VALID_FOLLOW_UP_DATE, description);

            assertThrows(IllegalValueException.class, FollowUp.MESSAGE_CONSTRAINTS, adapter::toModelType);
        }
    }

    @Test
    public void toModelType_paddedDescription_returnsTrimmedDescription() throws Exception {
        JsonAdaptedFollowUp adapter = new JsonAdaptedFollowUp(
                VALID_FOLLOW_UP_DATE, "  " + VALID_FOLLOW_UP_DESCRIPTION + "  ");

        assertEquals(VALID_FOLLOW_UP_DESCRIPTION, adapter.toModelType().getDescription());
    }

    @Test
    public void jsonRoundTrip_validFollowUp_preservesValues() throws Exception {
        FollowUp original = new FollowUp(
                LocalDate.parse(VALID_FOLLOW_UP_DATE),
                "Confirm \"PCB\" quotation — 报价 🙂");

        String json = JsonUtil.toJsonString(new JsonAdaptedFollowUp(original));
        JsonAdaptedFollowUp restored =
                JsonUtil.fromJsonString(json, JsonAdaptedFollowUp.class);

        assertEquals(original, restored.toModelType());
    }
}
