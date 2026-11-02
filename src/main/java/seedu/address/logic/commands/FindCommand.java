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

    public static final String COMMAND_DESCRIPTION = "Finds contacts by name, role, or tag using partial matches, "
            + "ignoring letter case.";
    public static final String COMMAND_FORMATS = "Search by name, role, or tag:\n"
            + "  " + COMMAND_WORD + " [n/NAME_PHRASE]... [r/ROLE_PHRASE]... [t/TAG_PHRASE]...\n"
            + "  At least one criterion is required; every prefix needs a nonblank value.\n"
            + "  Each value is one continuous phrase. Repeated prefixes use OR; different prefixes use AND.\n"
            + "Quick name search:\n"
            + "  " + COMMAND_WORD + " NAME_KEYWORD [NAME_KEYWORD]...\n"
            + "  Searches names containing any keyword (OR); spaces separate keywords.\n"
            + "Do not mix unprefixed keywords with prefixes.";
    public static final String COMMAND_EXAMPLES = "  " + COMMAND_WORD + " n/John Doe n/Alice r/Employee t/friend\n"
            + "  " + COMMAND_WORD + " John Doe";
    public static final String COMMAND_USAGE = COMMAND_WORD + ": " + COMMAND_DESCRIPTION + "\nFormats:\n"
            + COMMAND_FORMATS + "\nExamples:\n" + COMMAND_EXAMPLES;

    public static final String MESSAGE_MISSING_CRITERIA =
            "find requires at least one search criterion.";

    public static final String MESSAGE_MIXED_SEARCH_MODE =
            "find cannot mix unprefixed keywords with prefixed search values.";

    public static final String MESSAGE_EMPTY_PREFIX_VALUE =
            "find requires a non-blank value after n/, r/, t/.";

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

        return new CommandResult(Messages.getContactsFoundMessage(model.getFilteredPersonList().size()));
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
