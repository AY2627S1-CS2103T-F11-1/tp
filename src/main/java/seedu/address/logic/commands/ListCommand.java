package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.model.Model;

/**
 * Lists all contacts in the address book to the user.
 */
public class ListCommand extends Command {

    public static final String COMMAND_WORD = "list";
    public static final String COMMAND_DESCRIPTION = "Shows all contacts in KeyBossWarriorsPro.";
    public static final String COMMAND_USAGE = COMMAND_WORD + ": " + COMMAND_DESCRIPTION;

    public static final String MESSAGE_SUCCESS = "Listed all contacts.";

    public static final String MESSAGE_NO_CONTACTS = Messages.MESSAGE_NO_CONTACTS_FOUND;

    public static final String MESSAGE_IGNORED_ARGUMENTS = "Warning: Additional arguments after list were ignored.";

    private final boolean hasExtraArgs;

    public ListCommand() {
        this(false);
    }

    public ListCommand(boolean hasExtraArgs) {
        this.hasExtraArgs = hasExtraArgs;
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);

        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);

        String feedback = model.getAddressBook().getPersonList().isEmpty()
                ? MESSAGE_NO_CONTACTS
                : MESSAGE_SUCCESS;

        if (hasExtraArgs) {
            feedback += "\n" + MESSAGE_IGNORED_ARGUMENTS;
        }

        return new CommandResult(feedback);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof ListCommand otherListCommand)) {
            return false;
        }

        return hasExtraArgs == otherListCommand.hasExtraArgs;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("hasExtraArgs", hasExtraArgs)
                .toString();
    }

    @Override
    public int hashCode() {
        return Boolean.hashCode(hasExtraArgs);
    }
}
