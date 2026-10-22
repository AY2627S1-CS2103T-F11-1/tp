package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Finds and lists contacts in KeyBossWarriorsPro using name, role, and/or tag criteria.
 */
public class FindCommand extends Command {

    public static final String COMMAND_WORD = "find";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Finds contacts whose names, roles, and/or tags contain at least one search value "
            + "as a case-insensitive substring.\n"
            + "Usage: " + COMMAND_WORD + " [n/NAME_PHRASE]... [r/ROLE_PHRASE]... [t/TAG_PHRASE]...\n"
            + "Repeated occurrences of the same prefix use OR logic; different prefix criteria use AND logic.\n"
            + "Example: " + COMMAND_WORD + " n/John Doe n/Alice r/Employee t/friend\n"
            + "Legacy mode is also supported: " + COMMAND_WORD + " alice bob";

    // User-facing error messages
    public static final String MESSAGE_MISSING_CRITERIA =
            "Error: FIND requires at least one search criterion";

    public static final String MESSAGE_MIXED_SEARCH_MODE =
            "Error: FIND cannot mix unprefixed keywords with prefixed search values";

    public static final String MESSAGE_EMPTY_PREFIX_VALUE =
            "Error: FIND requires a non-blank value after n/, r/, t/";

    private final List<Predicate<Person>> predicates;

    /**
     * Creates a {@code FindCommand} with the given search predicates.
     *
     * @param predicates predicates used to filter contacts
     * @throws NullPointerException if {@code predicates} or one of its elements is null
     */
    @SafeVarargs
    public FindCommand(Predicate<Person>... predicates) {
        requireNonNull(predicates);

        if (predicates.length == 0) {
            throw new IllegalArgumentException("At least one predicate is required");
        }

        this.predicates = Arrays.stream(predicates)
                .map(predicate -> requireNonNull(predicate))
                .toList();
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);

        model.updateFilteredPersonList(person ->
                predicates.stream().allMatch(predicate -> predicate.test(person)));

        return new CommandResult(
                String.format(Messages.MESSAGE_PERSONS_LISTED_OVERVIEW, model.getFilteredPersonList().size()));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof FindCommand otherFindCommand)) {
            return false;
        }

        return predicates.equals(otherFindCommand.predicates);
    }

    @Override
    public String toString() {
        Object predicateRepresentation = predicates.size() == 1
                ? predicates.get(0)
                : predicates;

        return new ToStringBuilder(this)
                .add("predicate", predicateRepresentation)
                .toString();
    }
}
