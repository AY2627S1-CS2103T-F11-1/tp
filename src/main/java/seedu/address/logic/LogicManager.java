package seedu.address.logic;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.logging.Logger;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.RedoCommand;
import seedu.address.logic.commands.UndoCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.storage.Storage;

/**
 * The main LogicManager of the app.
 */
public class LogicManager implements Logic {
    public static final String FILE_OPS_ERROR_FORMAT = "Could not save data due to the following error: %s";

    public static final String FILE_OPS_PERMISSION_ERROR_FORMAT =
            "Could not save data to file %s due to insufficient permissions to write to the file or the folder.";

    private final Logger logger = LogsCenter.getLogger(LogicManager.class);

    private final Model model;
    private final Storage storage;
    private final AddressBookParser addressBookParser;
    private CommandSelectionChange selectionChange = new CommandSelectionChange(Map.of(), false);

    /**
     * Constructs a {@code LogicManager} with the given {@code Model} and {@code Storage}.
     */
    public LogicManager(Model model, Storage storage) {
        this.model = model;
        this.storage = storage;
        addressBookParser = new AddressBookParser();
    }

    @Override
    public CommandResult execute(String commandText) throws CommandException, ParseException {
        selectionChange = new CommandSelectionChange(Map.of(), false);
        logger.info("----------------[USER COMMAND][" + commandText + "]");

        int pointerBefore = model.getAddressBookStatePointer();
        CommandResult commandResult;
        Command command = addressBookParser.parseCommand(commandText);
        Map<Person, Person> replacements = new IdentityHashMap<>();
        ListChangeListener<Person> replacementListener = change -> {
            while (change.next()) {
                if (change.wasReplaced() && change.getRemovedSize() == 1 && change.getAddedSize() == 1) {
                    replacements.put(change.getRemoved().get(0), change.getAddedSubList().get(0));
                }
            }
        };
        ObservableList<Person> persons = model.getAddressBook().getPersonList();
        persons.addListener(replacementListener);
        boolean executed = false;
        try {
            commandResult = command.execute(model);
            executed = true;
        } finally {
            persons.removeListener(replacementListener);
            selectionChange = new CommandSelectionChange(replacements,
                    executed && (command instanceof UndoCommand || command instanceof RedoCommand));
        }

        try {
            storage.saveAddressBook(model.getAddressBook());
        } catch (AccessDeniedException e) {
            rollbackModel(command, pointerBefore);
            throw new CommandException(String.format(FILE_OPS_PERMISSION_ERROR_FORMAT, e.getMessage()), e);
        } catch (IOException ioe) {
            rollbackModel(command, pointerBefore);
            throw new CommandException(String.format(FILE_OPS_ERROR_FORMAT, ioe.getMessage()), ioe);
        }

        return commandResult;
    }

    /**
     * Reverts changes to the model if persisting to storage fails.
     */
    private void rollbackModel(Command command, int pointerBefore) {
        if (command instanceof UndoCommand || command instanceof RedoCommand) {
            return;
        }
        int pointerAfter = model.getAddressBookStatePointer();
        if (pointerAfter > pointerBefore) {
            model.rollbackAddressBook();
        }
    }

    @Override
    public ObservableList<Person> getFilteredPersonList() {
        return model.getFilteredPersonList();
    }

    @Override
    public CommandSelectionChange getSelectionChange() {
        return selectionChange;
    }

    @Override
    public GuiSettings getGuiSettings() {
        return model.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        model.setGuiSettings(guiSettings);
    }
}
