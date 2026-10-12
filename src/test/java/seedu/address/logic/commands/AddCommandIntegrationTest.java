package seedu.address.logic.commands;

import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) for {@code AddCommand}.
 */
public class AddCommandIntegrationTest {

    private Model model;

    @BeforeEach
    public void setUp() {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
    }

    @Test
    public void execute_newPerson_success() {
        Person validPerson = new PersonBuilder().build();

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.addPerson(validPerson);

        assertCommandSuccess(new AddCommand(validPerson), model,
                String.format(AddCommand.MESSAGE_SUCCESS, Messages.format(validPerson)),
                expectedModel);
    }

    @Test
    public void execute_duplicatePhoneAndEmail_throwsCommandException() {
        Person personInList = model.getAddressBook().getPersonList().get(0);
        assertCommandFailure(new AddCommand(personInList), model,
                Messages.MESSAGE_DUPLICATE_PHONE_AND_EMAIL);
    }

    @Test
    public void execute_duplicatePhoneOnly_throwsCommandException() {
        Person personInList = model.getAddressBook().getPersonList().get(0);
        Person duplicatePhonePerson = new PersonBuilder().withPhone(personInList.getPhone().value)
                .withEmail("unique_email@example.com").build();
        assertCommandFailure(new AddCommand(duplicatePhonePerson), model,
                Messages.MESSAGE_DUPLICATE_PHONE);
    }

    @Test
    public void execute_duplicateEmailOnly_throwsCommandException() {
        Person personInList = model.getAddressBook().getPersonList().get(0);
        Person duplicateEmailPerson = new PersonBuilder().withPhone("11112222")
                .withEmail(personInList.getEmail().value).build();
        assertCommandFailure(new AddCommand(duplicateEmailPerson), model,
                Messages.MESSAGE_DUPLICATE_EMAIL);
    }

    @Test
    public void execute_sameNameDifferentPhoneAndEmail_success() {
        Person personInList = model.getAddressBook().getPersonList().get(0);
        Person sameNamePerson = new PersonBuilder().withName(personInList.getName().fullName)
                .withPhone("11112222").withEmail("unique_email@example.com").build();

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.addPerson(sameNamePerson);

        assertCommandSuccess(new AddCommand(sameNamePerson), model,
                String.format(AddCommand.MESSAGE_SUCCESS, Messages.format(sameNamePerson)),
                expectedModel);
    }

}
