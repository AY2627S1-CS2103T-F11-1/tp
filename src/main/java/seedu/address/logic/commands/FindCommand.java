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

    public static final String COMMAND_DESCRIPTION = "Finds contacts by case-insensitive substrings in names, roles, "
            + "and/or tags. Repeated values of the same prefix use OR; different prefixes use AND. "
            + "Legacy unprefixed keywords search names and cannot be mixed with prefixes.";
    public static final String COMMAND_PARAMETERS = "[n/NAME_PHRASE]... [r/ROLE_PHRASE]... [t/TAG_PHRASE]... "
            + "(at least one required), or KEYWORD [MORE_KEYWORDS]...";
    public static final String COMMAND_EXAMPLE = COMMAND_WORD + " n/John Doe n/Alice r/Employee t/friend";
    public static final String COMMAND_USAGE = COMMAND_WORD + ": " + COMMAND_DESCRIPTION + "\nParameters: "
            + COMMAND_PARAMETERS + "\nExample: " + COMMAND_EXAMPLE;

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
