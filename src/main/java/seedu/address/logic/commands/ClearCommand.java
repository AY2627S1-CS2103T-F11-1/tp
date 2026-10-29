package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;

/**
 * Clears the address book.
 */
public class ClearCommand extends Command {

    public static final String COMMAND_WORD = "clear";
    public static final String COMMAND_DESCRIPTION = "Clears all entries from KeyBossWarriorsPro.";
    public static final String COMMAND_USAGE = COMMAND_WORD + ": " + COMMAND_DESCRIPTION;

    public static final String MESSAGE_SUCCESS = "KeyBossWarriorsPro has been cleared!";
    public static final String MESSAGE_EMPTY_ADDRESS_BOOK =
            "KeyBossWarriorsPro is already empty; no contacts were cleared.";
    public static final String MESSAGE_IGNORED_ARGUMENTS =
            "Warning: additional arguments after clear were ignored.";

    private final boolean hasExtraArgs;

    public ClearCommand() {
        this(false);
    }

    public ClearCommand(boolean hasExtraArgs) {
        this.hasExtraArgs = hasExtraArgs;
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);

        if (model.getAddressBook().getPersonList().isEmpty()) {
            return new CommandResult(MESSAGE_EMPTY_ADDRESS_BOOK);
        }

        model.setAddressBook(new AddressBook());
        model.commitAddressBook();

        String feedback = MESSAGE_SUCCESS;
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

        if (!(other instanceof ClearCommand otherClearCommand)) {
            return false;
        }

        return hasExtraArgs == otherClearCommand.hasExtraArgs;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("hasExtraArgs", hasExtraArgs)
                .toString();
    }
}

