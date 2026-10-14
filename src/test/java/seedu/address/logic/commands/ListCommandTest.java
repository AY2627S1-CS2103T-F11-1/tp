package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;

/**
 * Contains integration tests (interaction with the Model) and unit tests for ListCommand.
 */
public class ListCommandTest {

    private Model model;
    private Model expectedModel;

    @BeforeEach
    public void setUp() {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
    }

    @Test
    public void execute_listIsNotFiltered_showsSameList() {
        assertCommandSuccess(new ListCommand(), model, ListCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void execute_listIsFiltered_showsEverything() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        assertCommandSuccess(new ListCommand(), model, ListCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void execute_listWithExtraArguments_showsWarning() {
        String expectedMessage = ListCommand.MESSAGE_SUCCESS
                + "\n"
                + ListCommand.MESSAGE_IGNORED_ARGUMENTS;

        assertCommandSuccess(new ListCommand(true), model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_listWithExtraArguments_doesNotModifyAddressBook() throws Exception {
        AddressBook addressBookBeforeExecution = new AddressBook(model.getAddressBook());

        new ListCommand(true).execute(model);

        assertEquals(addressBookBeforeExecution, model.getAddressBook());
    }

    @Test
    public void execute_emptyAddressBook_showsNoContactsMessage() {
        Model emptyModel = new ModelManager();
        Model expectedEmptyModel = new ModelManager();

        assertCommandSuccess(new ListCommand(), emptyModel, ListCommand.MESSAGE_NO_CONTACTS, expectedEmptyModel);
    }

    @Test
    public void execute_emptyAddressBookWithExtraArguments_showsWarning() {
        Model emptyModel = new ModelManager();
        Model expectedEmptyModel = new ModelManager();

        String expectedMessage = ListCommand.MESSAGE_NO_CONTACTS + "\n" + ListCommand.MESSAGE_IGNORED_ARGUMENTS;

        assertCommandSuccess(new ListCommand(true), emptyModel, expectedMessage, expectedEmptyModel);
    }
}
