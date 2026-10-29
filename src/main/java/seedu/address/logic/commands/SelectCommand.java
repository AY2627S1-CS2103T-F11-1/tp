package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Selects a contact by its one-based index in the currently displayed contact list.
 */
public class SelectCommand extends Command {
    public static final String COMMAND_WORD = "select";
    public static final String COMMAND_DESCRIPTION = "Selects the contact identified by the index number in the "
            + "currently displayed contact list and shows its details.";
    public static final String COMMAND_PARAMETERS = "INDEX (must be a positive integer)";
    public static final String COMMAND_EXAMPLE = COMMAND_WORD + " 1";
    public static final String COMMAND_USAGE = COMMAND_WORD + ": " + COMMAND_DESCRIPTION + "\nParameters: "
            + COMMAND_PARAMETERS + "\nExample: " + COMMAND_EXAMPLE;

    public static final String MESSAGE_SELECT_CONTACT_SUCCESS = "Selected contact %1$d: %2$s";
    public static final String MESSAGE_INDEX_REQUIRED = Messages.MESSAGE_INDEX_REQUIRED;
    public static final String MESSAGE_EXTRA_ARGUMENTS = "select accepts only one INDEX.";
    public static final String MESSAGE_NO_CONTACTS_TO_SELECT = Messages.MESSAGE_NO_CONTACTS_DISPLAYED;
    public static final String MESSAGE_INDEX_OUT_OF_RANGE = Messages.MESSAGE_INDEX_OUT_OF_RANGE;
    public static final String MESSAGE_SINGLE_INDEX_OUT_OF_RANGE = Messages.MESSAGE_SINGLE_INDEX_OUT_OF_RANGE;

    private final Index targetIndex;

    /**
     * Creates a command selecting the given index from the displayed contact list.
     */
    public SelectCommand(Index targetIndex) {
        this.targetIndex = requireNonNull(targetIndex);
    }

    /**
     * Returns a selection request without changing contacts, filters, sorting, or undo/redo history.
     *
     * @throws CommandException if the displayed list is empty or the index is out of range.
     */
    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> displayedPersons = model.getFilteredPersonList();
        if (displayedPersons.isEmpty()) {
            throw new CommandException(MESSAGE_NO_CONTACTS_TO_SELECT);
        }
        if (targetIndex.getZeroBased() >= displayedPersons.size()) {
            throw new CommandException(
                    Messages.getIndexOutOfRangeMessage(targetIndex.getOneBased(), displayedPersons.size()));
        }

        Person selectedPerson = displayedPersons.get(targetIndex.getZeroBased());
        return new CommandResult(String.format(MESSAGE_SELECT_CONTACT_SUCCESS,
                targetIndex.getOneBased(), selectedPerson.getName().fullName), selectedPerson);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        return other instanceof SelectCommand otherCommand && targetIndex.equals(otherCommand.targetIndex);
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(targetIndex.getZeroBased());
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("targetIndex", targetIndex).toString();
    }
}
