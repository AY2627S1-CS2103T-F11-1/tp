package seedu.address.storage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.FollowUp;

/**
 * Jackson-friendly version of {@link FollowUp}.
 */
class JsonAdaptedFollowUp {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Follow-up's %s field is missing!";
    public static final String INVALID_DATE_MESSAGE =
            "Follow-up due dates must be valid calendar dates in YYYY-MM-DD format.";

    // Requires exactly 4 year digits and 2 digits each for month and day.
    // Calendar validity is checked separately by LocalDate.parse().
    private static final String DATE_FORMAT_REGEX = "[0-9]{4}-[0-9]{2}-[0-9]{2}";

    private final String dueDate;
    private final String description;

    /**
     * Constructs an adapter from JSON field values.
     * Validation occurs when converting to the model type.
     */
    @JsonCreator
    public JsonAdaptedFollowUp(@JsonProperty("dueDate") String dueDate,
            @JsonProperty("description") String description) {
        this.dueDate = dueDate;
        this.description = description;
    }

    /**
     * Converts a follow-up into its JSON representation.
     */
    public JsonAdaptedFollowUp(FollowUp source) {
        dueDate = source.getDueDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
        description = source.getDescription();
    }

    /**
     * Converts this adapter into an immutable follow-up.
     *
     * @throws IllegalValueException if a required field is missing or invalid.
     */
    public FollowUp toModelType() throws IllegalValueException {
        if (dueDate == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, "dueDate"));
        }
        final LocalDate modelDueDate = parseDueDate(dueDate);

        if (description == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, "description"));
        }
        if (!FollowUp.isValidDescription(description)) {
            throw new IllegalValueException(FollowUp.MESSAGE_CONSTRAINTS);
        }

        return new FollowUp(modelDueDate, description);
    }

    /**
     * Parses a date in the exact four-digit-year format {@code YYYY-MM-DD}.
     * Checks both the textual format and calendar validity, accepting past dates.
     *
     * @throws IllegalValueException if the format or calendar date is invalid.
     */
    private static LocalDate parseDueDate(String value) throws IllegalValueException {
        if (!value.matches(DATE_FORMAT_REGEX)) {
            throw new IllegalValueException(INVALID_DATE_MESSAGE);
        }

        try {
            return LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            throw new IllegalValueException(INVALID_DATE_MESSAGE);
        }
    }

}
