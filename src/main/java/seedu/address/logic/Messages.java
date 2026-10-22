package seedu.address.logic;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import seedu.address.logic.parser.Prefix;
import seedu.address.model.person.Person;

/**
 * Container for user visible messages.
 */
public class Messages {

    public static final String MESSAGE_UNKNOWN_COMMAND = "Unknown command.";
    public static final String MESSAGE_INVALID_COMMAND_FORMAT = "Invalid command format!\n%1$s";
    public static final String MESSAGE_INVALID_PERSON_DISPLAYED_INDEX = "The person index provided is invalid.";
    public static final String MESSAGE_PERSONS_LISTED_OVERVIEW = "%1$d person(s) listed!";
    public static final String MESSAGE_DUPLICATE_FIELDS =
                "Multiple values specified for the following single-valued field(s): ";
    public static final String MESSAGE_MISSING_PARAMETERS =
            "Missing mandatory parameter(s): ";
    public static final String MESSAGE_UNRECOGNIZED_PREFIXES =
            "Unrecognized prefix(es) provided: ";
    public static final String MESSAGE_DUPLICATE_PHONE =
            "This phone number already belongs to an existing contact.";
    public static final String MESSAGE_DUPLICATE_EMAIL =
            "This email address already belongs to an existing contact.";
    public static final String MESSAGE_DUPLICATE_PHONE_AND_EMAIL =
            "This phone number and email address are already used by existing contacts.";

    /**
     * Returns an error message indicating the duplicate prefixes.
     */
    public static String getErrorMessageForDuplicatePrefixes(Prefix... duplicatePrefixes) {
        assert duplicatePrefixes.length > 0;

        Set<String> duplicateFields =
                Stream.of(duplicatePrefixes).map(Prefix::toString).collect(Collectors.toSet());

        return MESSAGE_DUPLICATE_FIELDS + String.join(" ", duplicateFields);
    }

    /**
     * Returns an error message indicating the missing prefixes.
     */
    public static String getErrorMessageForMissingPrefixes(Prefix... missingPrefixes) {
        assert missingPrefixes.length > 0;

        List<String> missingFields = Stream.of(missingPrefixes)
                .map(Prefix::toString)
                .collect(Collectors.toList());

        return MESSAGE_MISSING_PARAMETERS + String.join(" ", missingFields);
    }

    /**
     * Returns an error message indicating the unrecognized prefixes.
     */
    public static String getErrorMessageForUnrecognizedPrefixes(Prefix... unrecognizedPrefixes) {
        assert unrecognizedPrefixes.length > 0;

        List<String> unrecognizedFields = Stream.of(unrecognizedPrefixes)
                .map(Prefix::toString)
                .distinct()
                .collect(Collectors.toList());

        return MESSAGE_UNRECOGNIZED_PREFIXES + String.join(" ", unrecognizedFields);
    }

    /**
     * Formats the {@code person} for display to the user.
     */
    public static String format(Person person) {
        final StringBuilder builder = new StringBuilder();
        builder.append(person.getName())
                .append("; Phone: ")
                .append(person.getPhone())
                .append("; Email: ")
                .append(person.getEmail())
                .append("; Address: ")
                .append(person.getAddress())
                .append("; Role: ")
                .append(person.getRole())
                .append("; Tags: ");
        person.getTags().forEach(builder::append);
        return builder.toString();
    }

}
