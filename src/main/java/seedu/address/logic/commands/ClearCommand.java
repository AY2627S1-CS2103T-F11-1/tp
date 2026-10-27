package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

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


    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.setAddressBook(new AddressBook());
        model.commitAddressBook();
        return new CommandResult(MESSAGE_SUCCESS);
    }
}
