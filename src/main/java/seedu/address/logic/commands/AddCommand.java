package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ROLE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Adds a person to the address book.
 */
public class AddCommand extends Command {

    public static final String COMMAND_WORD = "add";

    public static final String COMMAND_DESCRIPTION = "Adds a person to the address book.";
    public static final String COMMAND_PARAMETERS = PREFIX_NAME + "NAME "
            + PREFIX_PHONE + "PHONE "
            + PREFIX_EMAIL + "EMAIL "
            + PREFIX_ADDRESS + "ADDRESS "
            + PREFIX_ROLE + "ROLE "
            + "[" + PREFIX_TAG + "TAG]...";
    public static final String COMMAND_EXAMPLE = COMMAND_WORD + " "
            + PREFIX_NAME + "John Doe "
            + PREFIX_PHONE + "98765432 "
            + PREFIX_EMAIL + "johnd@example.com "
            + PREFIX_ADDRESS + "311, Clementi Ave 2, #02-25 "
            + PREFIX_ROLE + "Supplier "
            + PREFIX_TAG + "friends "
            + PREFIX_TAG + "owesMoney";
    public static final String COMMAND_USAGE = COMMAND_WORD + ": " + COMMAND_DESCRIPTION + "\nParameters: "
            + COMMAND_PARAMETERS + "\nExample: " + COMMAND_EXAMPLE;

    public static final String MESSAGE_SUCCESS = "New person added: %1$s";

    private static final Logger logger = LogsCenter.getLogger(AddCommand.class);

    private final Person toAdd;

    /**
     * Creates an AddCommand to add the specified {@code Person}
     */
    public AddCommand(Person person) {
        requireNonNull(person);
        toAdd = person;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        logger.fine("Executing AddCommand for person: " + toAdd.getName());

        boolean hasPhone = model.hasPhone(toAdd.getPhone());
        boolean hasEmail = model.hasEmail(toAdd.getEmail());

        if (hasPhone && hasEmail) {
            logger.warning("AddCommand rejected: Duplicate phone (" + toAdd.getPhone()
                    + ") and email (" + toAdd.getEmail() + ") exist in address book");
            throw new CommandException(Messages.MESSAGE_DUPLICATE_PHONE_AND_EMAIL);
        } else if (hasPhone) {
            logger.warning("AddCommand rejected: Duplicate phone (" + toAdd.getPhone()
                    + ") exists in address book");
            throw new CommandException(Messages.MESSAGE_DUPLICATE_PHONE);
        } else if (hasEmail) {
            logger.warning("AddCommand rejected: Duplicate email (" + toAdd.getEmail()
                    + ") exists in address book");
            throw new CommandException(Messages.MESSAGE_DUPLICATE_EMAIL);
        }

        model.addPerson(toAdd);
        model.commitAddressBook();

        logger.finer("Person added to model: " + toAdd.getName());
        return new CommandResult(String.format(MESSAGE_SUCCESS, Messages.format(toAdd)));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof AddCommand otherAddCommand)) {
            return false;
        }

        return toAdd.equals(otherAddCommand.toAdd);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("toAdd", toAdd)
                .toString();
    }
}
