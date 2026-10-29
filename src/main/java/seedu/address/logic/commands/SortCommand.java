package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Sorts the contacts in KeyBossWarriorsPro using name and/or role criteria.
 */
public class SortCommand extends Command {
    public static final String COMMAND_WORD = "sort";

    public static final String COMMAND_DESCRIPTION = "Shows all contacts sorted alphabetically by name, role, or both, "
            + "ignoring letter case. Defaults to name when no fields are specified. The first field takes priority; "
            + "the second breaks ties. Each field may appear only once and takes no value.";
    public static final String COMMAND_PARAMETERS = "[n/] [r/] or [r/] [n/]";
    public static final String COMMAND_EXAMPLE = COMMAND_WORD + " r/ n/";
    public static final String COMMAND_USAGE = COMMAND_WORD + ": " + COMMAND_DESCRIPTION + "\nParameters: "
            + COMMAND_PARAMETERS + "\nExample: " + COMMAND_EXAMPLE;

    public static final String MESSAGE_SUCCESS = "Listed all contacts in sorted order.";

    public static final String MESSAGE_EMPTY_ADDRESS_BOOK = "There are no contacts to sort.";

    public static final String MESSAGE_DUPLICATE_FIELD =
            "sort accepts each field only once: %s.";

    public static final String MESSAGE_UNEXPECTED_VALUE =
            "sort does not accept values after %s.";

    public static final String MESSAGE_UNEXPECTED_TOKEN =
            "sort expects n/ or r/, but received: %s.";

    /**
     * Fields supported for sorting contacts.
     */
    public enum SortField {
        NAME, ROLE
    }

    private final List<SortField> sortFields;

    /**
     * Creates a sort command with one or two fields in priority order.
     */
    public SortCommand(List<SortField> sortFields) {
        requireNonNull(sortFields);

        if (sortFields.isEmpty() || sortFields.size() > 2
                || sortFields.stream().anyMatch(Objects::isNull)
                || sortFields.stream().distinct().count() != sortFields.size()) {
            throw new IllegalArgumentException("Expected one or two distinct sort fields");
        }

        // Defensive copying
        this.sortFields = List.copyOf(sortFields);
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);

        Comparator<Person> comparator = comparatorFor(sortFields.get(0));
        for (int i = 1; i < sortFields.size(); i++) {
            comparator = comparator.thenComparing(comparatorFor(sortFields.get(i)));
        }

        // Design choice: sort should always show ALL contacts, not only the filtered list
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);

        model.updateSortedPersonList(comparator);

        String message = model.getAddressBook().getPersonList().isEmpty()
                ? MESSAGE_EMPTY_ADDRESS_BOOK
                : MESSAGE_SUCCESS;

        return new CommandResult(message);
    }

    /**
     * Returns a {@code Comparator<Person>} that orders contacts by the specified {@code field},
     * ignoring differences in letter-casing.
     */
    private static Comparator<Person> comparatorFor(SortField field) {
        return switch (field) {
            case NAME -> Comparator.comparing(
                    person -> person.getName().fullName, String.CASE_INSENSITIVE_ORDER);
            case ROLE -> Comparator.comparing(
                    person -> person.getRole().value, String.CASE_INSENSITIVE_ORDER);
        };
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof SortCommand otherSortCommand)) {
            return false;
        }

        return sortFields.equals(otherSortCommand.sortFields);
    }

    @Override
    public int hashCode() {
        return sortFields.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("sortFields", sortFields)
                .toString();
    }
}
