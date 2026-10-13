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
 * Deletes a contact identified by a one-based index in the currently displayed contact list.
 */
public class DeleteCommand extends Command {

    public static final String COMMAND_WORD = "delete";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Deletes the contact identified by the index number used in the currently displayed contact list.\n"
            + "Parameters: INDEX (must be a positive integer)\n"
            + "Example: " + COMMAND_WORD + " 1";

    public static final String MESSAGE_DELETE_CONTACT_SUCCESS = "Deleted contact: %1$s";

    public static final String MESSAGE_INDEX_REQUIRED = "Error: INDEX is required";

    public static final String MESSAGE_EXTRA_ARGUMENTS = "Error: DELETE accepts only one INDEX";

    public static final String MESSAGE_NO_CONTACTS_TO_DELETE = "Error: There are no contacts to delete";

    public static final String MESSAGE_INDEX_OUT_OF_RANGE = "Error: INDEX %1$d is outside the displayed contact list "
            + "(valid range: 1-%2$d)";

    public static final String MESSAGE_SINGLE_INDEX_OUT_OF_RANGE =
            "Error: INDEX %1$d is outside the displayed contact list (valid index: 1)";

    private final Index targetIndex;

    public DeleteCommand(Index targetIndex) {
        this.targetIndex = targetIndex;
    }

    /**
     * Returns an error message for an index outside the displayed contact list.
     * Uses singular wording when only one contact is displayed.
     *
     * @param index the one-based index supplied by the user
     * @param displayedListSize the number of contacts currently displayed
     * @return an appropriate out-of-range error message
     */
    private static String getIndexOutOfRangeMessage(int index, int displayedListSize) {
        if (displayedListSize == 1) {
            return String.format(MESSAGE_SINGLE_INDEX_OUT_OF_RANGE, index);
        }

        return String.format(MESSAGE_INDEX_OUT_OF_RANGE, index, displayedListSize);
    }

    /**
     * Deletes the contact at the target one-based index from the currently displayed contact list.
     *
     * @param model {@code Model} containing the displayed contact list
     * @return a success result containing the deleted contact's details
     * @throws CommandException if no contacts are displayed or the target index is outside the displayed contact list
     */
    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = model.getFilteredPersonList();

        // Execution error: empty list
        if (lastShownList.isEmpty()) {
            throw new CommandException(MESSAGE_NO_CONTACTS_TO_DELETE);
        }

        // Execution error: index out of range
        if (targetIndex.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(getIndexOutOfRangeMessage(targetIndex.getOneBased(), lastShownList.size()));
        }

        Person personToDelete = lastShownList.get(targetIndex.getZeroBased());
        model.deletePerson(personToDelete);
        return new CommandResult(String.format(MESSAGE_DELETE_CONTACT_SUCCESS, Messages.format(personToDelete)));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof DeleteCommand otherDeleteCommand)) {
            return false;
        }

        return targetIndex.equals(otherDeleteCommand.targetIndex);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetIndex", targetIndex)
                .toString();
    }
}
