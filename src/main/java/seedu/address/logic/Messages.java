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
    public static final String MESSAGE_INDEX_REQUIRED = "INDEX is required.";
    public static final String MESSAGE_INVALID_INDEX = "INDEX must be a positive integer.";
    public static final String MESSAGE_NO_CONTACTS_DISPLAYED = "No contacts are currently displayed.";
    public static final String MESSAGE_NO_CONTACTS_FOUND = "No contacts found.";
    public static final String MESSAGE_NO_FOLLOW_UPS_FOUND = "No follow-ups found.";
    public static final String MESSAGE_INDEX_OUT_OF_RANGE = "INDEX %1$d is outside the displayed contact list "
            + "(valid range: 1-%2$d).";
    public static final String MESSAGE_SINGLE_INDEX_OUT_OF_RANGE =
            "INDEX %1$d is outside the displayed contact list (valid index: 1).";
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
     * Returns an informative message for an index outside the currently displayed contact list.
     * Uses singular wording when only one contact is displayed.
     */
    public static String getIndexOutOfRangeMessage(int index, int displayedListSize) {
        if (displayedListSize == 1) {
            return String.format(MESSAGE_SINGLE_INDEX_OUT_OF_RANGE, index);
        }

        return String.format(MESSAGE_INDEX_OUT_OF_RANGE, index, displayedListSize);
    }

    /**
     * Returns a grammatically correct message for a contact search result.
     */
    public static String getContactsFoundMessage(int contactCount) {
        if (contactCount == 0) {
            return MESSAGE_NO_CONTACTS_FOUND;
        }

        return contactCount == 1
                ? "1 contact found."
                : String.format("%d contacts found.", contactCount);
    }

    /**
     * Returns a grammatically correct message for a follow-up query result.
     */
    public static String getFollowUpsFoundMessage(int followUpCount) {
        if (followUpCount == 0) {
            return MESSAGE_NO_FOLLOW_UPS_FOUND;
        }

        return followUpCount == 1
                ? "1 follow-up found."
                : String.format("%d follow-ups found.", followUpCount);
    }

    /**
     * Returns an error message indicating the duplicate prefixes.
     */
    public static String getErrorMessageForDuplicatePrefixes(Prefix... duplicatePrefixes) {
        assert duplicatePrefixes.length > 0;

        Set<String> duplicateFields =
                Stream.of(duplicatePrefixes).map(Prefix::toString).collect(Collectors.toSet());

        return MESSAGE_DUPLICATE_FIELDS + String.join(" ", duplicateFields) + ".";
    }

    /**
     * Returns an error message indicating the missing prefixes.
     */
    public static String getErrorMessageForMissingPrefixes(Prefix... missingPrefixes) {
        assert missingPrefixes.length > 0;

        List<String> missingFields = Stream.of(missingPrefixes)
                .map(Prefix::toString)
                .collect(Collectors.toList());

        return MESSAGE_MISSING_PARAMETERS + String.join(" ", missingFields) + ".";
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

        return MESSAGE_UNRECOGNIZED_PREFIXES + String.join(" ", unrecognizedFields) + ".";
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
        person.getFollowUp().ifPresent(followUp ->
                builder.append("; Follow-up: ")
                        .append(followUp.getDueDate())
                        .append(" (")
                        .append(followUp.getDescription())
                        .append(")"));
        return builder.toString();
    }

}
