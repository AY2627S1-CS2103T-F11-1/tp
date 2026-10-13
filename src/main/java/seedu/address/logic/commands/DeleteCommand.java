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
            + ": Deletes the person identified by the index number used in the displayed person list.\n"
            + "Parameters: INDEX (must be a positive integer)\n"
            + "Example: " + COMMAND_WORD + " 1";

    public static final String MESSAGE_DELETE_PERSON_SUCCESS = "Deleted person: %1$s";

    public static final String MESSAGE_INDEX_REQUIRED = "Error: INDEX is required";

    public static final String MESSAGE_EXTRA_ARGUMENTS = "Error: DELETE accepts only one INDEX";

    public static final String MESSAGE_NO_CONTACTS_TO_DELETE = "Error: There are no contacts to delete";

    public static final String MESSAGE_INDEX_OUT_OF_RANGE = "Error: INDEX %1$d is outside the displayed contact list "
            + "(valid range: 1-%2$d)";

    private final Index targetIndex;

    public DeleteCommand(Index targetIndex) {
        this.targetIndex = targetIndex;
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
            throw new CommandException(String.format(
                    MESSAGE_INDEX_OUT_OF_RANGE,
                    targetIndex.getOneBased(),
                    lastShownList.size()
            ));
        }

        Person personToDelete = lastShownList.get(targetIndex.getZeroBased());
        model.deletePerson(personToDelete);
        return new CommandResult(String.format(MESSAGE_DELETE_PERSON_SUCCESS, Messages.format(personToDelete)));
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
