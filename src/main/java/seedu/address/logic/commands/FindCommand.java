package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.person.NameContainsKeywordsPredicate;

/**
 * Finds and lists contacts in KeyBossWarriorsPro whose name contains at least one search value as a case-insensitive
 * substring.
 */
public class FindCommand extends Command {

    public static final String COMMAND_WORD = "find";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Finds contacts whose names contain at least one search value "
            + "as a case-insensitive substring.\n"
            + "Legacy mode: space-separated keywords use OR logic.\n"
            + "  Example: " + COMMAND_WORD + " alice bob\n"
            + "Prefixed mode: each n/ value is treated as one phrase; "
            + "repeat n/ for OR logic.\n"
            + "  Example: " + COMMAND_WORD + " n/John Doe n/Alice\n"
            + "Do not mix legacy keywords with n/ values.";

    // User-facing error messages
    public static final String MESSAGE_MISSING_CRITERIA =
            "Error: FIND requires at least one search criterion";

    public static final String MESSAGE_MIXED_SEARCH_MODE =
            "Error: FIND cannot mix unprefixed keywords with n/ values";

    public static final String MESSAGE_EMPTY_NAME_VALUE =
            "Error: FIND requires a non-blank value after n/";

    private final NameContainsKeywordsPredicate predicate;

    /**
     * Creates a {@code FindCommand} with the given name-search predicate.
     *
     * @param predicate predicate used to filter contacts by name
     * @throws NullPointerException if {@code predicate} is null
     */
    public FindCommand(NameContainsKeywordsPredicate predicate) {
        requireNonNull(predicate);
        this.predicate = predicate;
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredPersonList(predicate);
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

        return predicate.equals(otherFindCommand.predicate);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("predicate", predicate)
                .toString();
    }
}
