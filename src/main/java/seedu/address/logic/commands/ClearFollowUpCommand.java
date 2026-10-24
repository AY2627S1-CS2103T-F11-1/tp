package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Optional;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Removes the follow-up of a contact in the currently displayed list.
 */
public class ClearFollowUpCommand extends FollowUpCommand {

    public static final String MESSAGE_SUCCESS = "Follow-up cleared for %1$s.";
    public static final String MESSAGE_NO_FOLLOW_UP = "%1$s has no follow-up.";

    private final Index index;

    /**
     * Creates a ClearFollowUpCommand to clear the specified {@code Person}'s follow-up.
     *
     * @throws NullPointerException if index is null.
     */
    public ClearFollowUpCommand(Index index) {
        requireNonNull(index);
        this.index = index;
    }

    /**
     * Clears the selected contact's follow-up and records one undoable change.
     * The current filter is preserved.
     *
     * @throws CommandException if the index is outside the displayed list
     *     or the selected contact has no follow-up
     */
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = model.getFilteredPersonList();

        if (index.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Person personToUpdate = lastShownList.get(index.getZeroBased());
        if (personToUpdate.getFollowUp().isEmpty()) {
            throw new CommandException(String.format(MESSAGE_NO_FOLLOW_UP, personToUpdate.getName()));
        }

        Person updatedPerson = createPersonWithoutFollowUp(personToUpdate);
        model.setPerson(personToUpdate, updatedPerson);
        model.commitAddressBook();

        return new CommandResult(String.format(MESSAGE_SUCCESS, personToUpdate.getName()));
    }

    /**
     * Returns a copy of the given person without a follow-up,
     * preserving all other contact details.
     *
     * @throws NullPointerException if person is null
     */
    private Person createPersonWithoutFollowUp(Person person) {
        requireNonNull(person);

        return new Person(person.getName(), person.getPhone(), person.getEmail(),
                person.getAddress(), person.getRole(), person.getTags(),
                Optional.empty());
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof ClearFollowUpCommand otherCommand)) {
            return false;
        }

        return index.equals(otherCommand.index);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("index", index)
                .toString();
    }
}
