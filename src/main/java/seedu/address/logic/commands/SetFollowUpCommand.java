package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.List;
import java.util.Optional;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.FollowUp;
import seedu.address.model.person.Person;

/**
 * Creates or replaces the follow-up of a contact in the displayed list.
 */
public class SetFollowUpCommand extends FollowUpCommand {

    public static final String MESSAGE_SUCCESS = "Follow-up set for %1$s: %2$s - %3$s";

    private final Index index;
    private final FollowUp followUp;

    /**
     * Creates a SetFollowUpCommand to add a FollowUp to the specified {@code Person}
     */
    public SetFollowUpCommand(Index index, FollowUp followUp) {
        requireAllNonNull(index, followUp);
        this.index = index;
        this.followUp = followUp;
    }

    /**
     * Sets the selected contact's follow-up and records one undoable change.
     * The current filter is preserved.
     *
     * @throws CommandException if the index is outside the displayed contact list.
     */
    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = model.getFilteredPersonList();

        if (index.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Person personToUpdate = lastShownList.get(index.getZeroBased());
        Person updatedPerson = createPersonWithFollowUp(personToUpdate);

        model.setPerson(personToUpdate, updatedPerson);
        model.commitAddressBook();

        return new CommandResult(String.format(MESSAGE_SUCCESS,
                personToUpdate.getName(), followUp.getDueDate(), followUp.getDescription()));
    }

    /**
     * Returns a copy of the given person with this command's follow-up,
     * preserving all other contact details.
     *
     * @throws NullPointerException if person is null.
     */
    private Person createPersonWithFollowUp(Person person) {
        requireNonNull(person);

        return new Person(person.getName(), person.getPhone(), person.getEmail(),
                person.getAddress(), person.getRole(), person.getTags(),
                Optional.of(followUp));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof SetFollowUpCommand otherCommand)) {
            return false;
        }

        return index.equals(otherCommand.index) && followUp.equals(otherCommand.followUp);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("index", index)
                .add("followUp", followUp)
                .toString();
    }
}
