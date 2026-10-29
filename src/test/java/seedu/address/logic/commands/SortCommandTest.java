package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.SortCommand.SortField.NAME;
import static seedu.address.logic.commands.SortCommand.SortField.ROLE;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.AMY;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.BOB;
import static seedu.address.testutil.TypicalPersons.CARL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.SortCommand.SortField;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests SortCommand and its interaction with the model.
 */
public class SortCommandTest {
    private Model model = new ModelManager();
    private Model expectedModel = new ModelManager();

    @Test
    public void constructor_nullFields_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new SortCommand(null));
    }

    @Test
    public void constructor_invalidFieldCount_throwsIllegalArgumentException() {
        // BVA: zero fields is below the minimum of one
        assertThrows(IllegalArgumentException.class, () -> new SortCommand(List.of()));

        // BVA: three fields is above the maximum of two
        assertThrows(IllegalArgumentException.class, () -> new SortCommand(List.of(NAME, ROLE, NAME)));
    }

    @Test
    public void constructor_duplicateFields_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new SortCommand(List.of(NAME, NAME)));
        assertThrows(IllegalArgumentException.class, () -> new SortCommand(List.of(ROLE, ROLE)));
    }

    @Test
    public void constructor_nullField_throwsIllegalArgumentException() {
        // Arrays.asList allows null elements, unlike List.of
        assertThrows(IllegalArgumentException.class, () -> new SortCommand(Arrays.asList((SortField) null)));
        assertThrows(IllegalArgumentException.class, () -> new SortCommand(Arrays.asList(null, NAME)));
        assertThrows(IllegalArgumentException.class, () -> new SortCommand(Arrays.asList(NAME, null)));
    }

    @Test
    public void constructor_mutableFields_defensivelyCopiesFields() {
        List<SortField> fields = new ArrayList<>(List.of(NAME, ROLE));
        SortCommand command = new SortCommand(fields);

        fields.clear();
        fields.add(ROLE);

        assertEquals(new SortCommand(List.of(NAME, ROLE)), command);

        prepareModels(List.of(CARL, ALICE, BENSON), List.of(ALICE, BENSON, CARL));

        assertCommandSuccess(command, model, SortCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        SortCommand command = new SortCommand(List.of(NAME));

        assertThrows(NullPointerException.class, () -> command.execute(null));
    }

    @Test
    public void execute_emptyAddressBook_success() {
        // BVA: zero contacts.
        SortCommand command = new SortCommand(List.of(NAME));

        assertCommandSuccess(command, model, SortCommand.MESSAGE_EMPTY_ADDRESS_BOOK, expectedModel);
    }

    @Test
    public void execute_emptyAddressBook_sortsSubsequentlyAddedContacts() {
        SortCommand command = new SortCommand(List.of(NAME));

        assertCommandSuccess(command, model,
                SortCommand.MESSAGE_EMPTY_ADDRESS_BOOK, expectedModel);

        model.addPerson(CARL);
        model.addPerson(ALICE);
        model.addPerson(BENSON);

        assertEquals(List.of(ALICE, BENSON, CARL),
                model.getFilteredPersonList());
    }

    @Test
    public void execute_emptyAddressBook_sortsSubsequentlyAddedContactsByRole() {
        SortCommand command = new SortCommand(List.of(ROLE));

        assertCommandSuccess(command, model,
                SortCommand.MESSAGE_EMPTY_ADDRESS_BOOK, expectedModel);

        Person zetaRole = new PersonBuilder(ALICE).withRole("Zeta").build();
        Person alphaRole = new PersonBuilder(BOB).withRole("Alpha").build();
        model.addPerson(zetaRole);
        model.addPerson(alphaRole);

        assertEquals(List.of(alphaRole, zetaRole), model.getFilteredPersonList());
    }

    @Test
    public void execute_singleContact_success() {
        // BVA: one contact and the maximum valid number of fields
        prepareModels(List.of(ALICE), List.of(ALICE));
        SortCommand command = new SortCommand(List.of(ROLE, NAME));

        assertCommandSuccess(command, model, SortCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void execute_nameOnly_sortsByName() {
        List<Person> input = List.of(CARL, ALICE, BENSON);
        prepareModels(input, List.of(ALICE, BENSON, CARL));
        SortCommand command = new SortCommand(List.of(NAME));

        assertCommandSuccess(command, model, SortCommand.MESSAGE_SUCCESS, expectedModel);

        assertEquals(input, model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_roleOnly_sortsByRole() {
        // Role order conflicts with name order
        prepareModels(List.of(ALICE, BOB, AMY), List.of(AMY, BOB, ALICE));
        SortCommand command = new SortCommand(List.of(ROLE));

        assertCommandSuccess(command, model, SortCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void execute_nameThenRole_usesRoleToBreakNameTies() {
        Person employeeAlice = new PersonBuilder(ALICE)
                .withRole("Employee")
                .build();
        Person distributorAlice = new PersonBuilder(AMY)
                .withName(ALICE.getName().fullName)
                .withRole("Distributor")
                .build();
        Person accountantBob = new PersonBuilder(BOB)
                .withRole("Accountant")
                .build();

        // Both Alices precede Bob despite Bob's alphabetically earlier role
        prepareModels(List.of(employeeAlice, accountantBob, distributorAlice),
                List.of(distributorAlice, employeeAlice, accountantBob));
        SortCommand command = new SortCommand(List.of(NAME, ROLE));

        assertCommandSuccess(command, model, SortCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void execute_roleThenName_usesNameToBreakRoleTies() {
        // AMY is a Distributor; ALICE and BENSON are Suppliers.
        prepareModels(List.of(BENSON, ALICE, AMY), List.of(AMY, ALICE, BENSON));
        SortCommand command = new SortCommand(List.of(ROLE, NAME));

        assertCommandSuccess(command, model, SortCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void execute_nameOnly_ignoresCase() {
        Person lowercaseAlice = new PersonBuilder(ALICE).withName("alice").build();

        // Case-sensitive sorting would place Bob before alice
        prepareModels(List.of(BOB, lowercaseAlice), List.of(lowercaseAlice, BOB));
        SortCommand command = new SortCommand(List.of(NAME));

        assertCommandSuccess(command, model, SortCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void execute_roleOnly_ignoresCase() {
        Person lowercaseDistributor = new PersonBuilder(AMY).withRole("distributor").build();

        // Case-sensitive sorting would place Employee before distributor
        prepareModels(List.of(BOB, lowercaseDistributor), List.of(lowercaseDistributor, BOB));
        SortCommand command = new SortCommand(List.of(ROLE));

        assertCommandSuccess(command, model, SortCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void execute_nameThenRole_caseOnlyNameDifferenceUsesRole() {
        Person supplierAlice = new PersonBuilder(ALICE).withName("ALICE").withRole("Supplier").build();
        Person distributorAlice = new PersonBuilder(AMY).withName("alice").withRole("Distributor").build();

        prepareModels(List.of(supplierAlice, distributorAlice), List.of(distributorAlice, supplierAlice));
        SortCommand command = new SortCommand(List.of(NAME, ROLE));

        assertCommandSuccess(command, model, SortCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void execute_roleThenName_caseOnlyRoleDifferenceUsesName() {
        Person supplierAlice = new PersonBuilder(ALICE).withRole("supplier").build();
        Person supplierBenson = new PersonBuilder(BENSON).withRole("Supplier").build();

        prepareModels(List.of(supplierBenson, supplierAlice), List.of(supplierAlice, supplierBenson));
        SortCommand command = new SortCommand(List.of(ROLE, NAME));

        assertCommandSuccess(command, model, SortCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void execute_partiallyFilteredList_showsAllSortedContacts() {
        prepareModels(List.of(CARL, ALICE, BENSON), List.of(ALICE, BENSON, CARL));
        model.updateFilteredPersonList(new NameContainsKeywordsPredicate(List.of("Alice")));
        assertEquals(List.of(ALICE), model.getFilteredPersonList());

        SortCommand command = new SortCommand(List.of(NAME));

        assertCommandSuccess(command, model, SortCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void execute_emptyFilteredList_showsAllSortedContacts() {
        prepareModels(List.of(CARL, ALICE, BENSON), List.of(ALICE, BENSON, CARL));
        model.updateFilteredPersonList(person -> false);
        assertTrue(model.getFilteredPersonList().isEmpty());

        SortCommand command = new SortCommand(List.of(NAME));

        assertCommandSuccess(command, model, SortCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void execute_newSort_replacesPreviousPriority() {
        prepareModels(List.of(ALICE, BOB, AMY), List.of(ALICE, AMY, BOB));

        new SortCommand(List.of(ROLE)).execute(model);
        assertEquals(List.of(AMY, BOB, ALICE), model.getFilteredPersonList());

        SortCommand command = new SortCommand(List.of(NAME));

        assertCommandSuccess(command, model, SortCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void execute_alreadySortedContacts_preservesOrder() {
        prepareModels(List.of(ALICE, BENSON, CARL), List.of(ALICE, BENSON, CARL));
        SortCommand command = new SortCommand(List.of(NAME));

        assertCommandSuccess(command, model, SortCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void execute_clearSort_restoresInsertionOrder() {
        List<Person> insertionOrder = List.of(CARL, ALICE, BENSON);
        insertionOrder.forEach(model::addPerson);

        new SortCommand(List.of(NAME)).execute(model);
        assertEquals(List.of(ALICE, BENSON, CARL), model.getFilteredPersonList());

        CommandResult result = SortCommand.clearSort().execute(model);

        assertEquals(SortCommand.MESSAGE_CLEAR_SUCCESS, result.getFeedbackToUser());
        assertEquals(insertionOrder, model.getFilteredPersonList());
        assertEquals(insertionOrder, model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_clearSort_clearsActiveFilter() {
        List<Person> insertionOrder = List.of(CARL, ALICE, BENSON);
        insertionOrder.forEach(model::addPerson);
        model.updateFilteredPersonList(new NameContainsKeywordsPredicate(List.of("Alice")));
        assertEquals(List.of(ALICE), model.getFilteredPersonList());

        CommandResult result = SortCommand.clearSort().execute(model);

        assertEquals(SortCommand.MESSAGE_CLEAR_SUCCESS, result.getFeedbackToUser());
        assertEquals(insertionOrder, model.getFilteredPersonList());
    }

    @Test
    public void clearSort_emptyAddressBook_success() {
        CommandResult result = SortCommand.clearSort().execute(model);

        assertEquals(SortCommand.MESSAGE_CLEAR_SUCCESS, result.getFeedbackToUser());
        assertTrue(model.getFilteredPersonList().isEmpty());
    }

    @Test
    public void clearSort_repeatedly_isIdempotent() {
        List<Person> insertionOrder = List.of(CARL, ALICE, BENSON);
        insertionOrder.forEach(model::addPerson);

        SortCommand.clearSort().execute(model);
        CommandResult result = SortCommand.clearSort().execute(model);

        assertEquals(SortCommand.MESSAGE_CLEAR_SUCCESS, result.getFeedbackToUser());
        assertEquals(insertionOrder, model.getFilteredPersonList());
    }

    @Test
    public void equals() {
        SortCommand command = new SortCommand(List.of(NAME, ROLE));
        SortCommand sameFields = new SortCommand(List.of(NAME, ROLE));

        assertTrue(command.equals(command));
        assertTrue(command.equals(sameFields));
        assertFalse(command.equals(null));
        assertFalse(command.equals("sort"));
        assertFalse(command.equals(new SortCommand(List.of(ROLE, NAME))));
        assertFalse(command.equals(new SortCommand(List.of(NAME))));
        assertFalse(new SortCommand(List.of(NAME)).equals(new SortCommand(List.of(ROLE))));
        assertFalse(command.equals(SortCommand.clearSort()));
        assertEquals(SortCommand.clearSort(), SortCommand.clearSort());

        assertEquals(command.hashCode(), sameFields.hashCode());
        assertEquals(SortCommand.clearSort().hashCode(), SortCommand.clearSort().hashCode());
    }

    @Test
    public void toStringMethod() {
        SortCommand command = new SortCommand(List.of(ROLE, NAME));

        String expected = SortCommand.class.getCanonicalName() + "{sortFields=[ROLE, NAME]}";

        assertEquals(expected, command.toString());
    }

    @Test
    public void clearSort_toStringMethod() {
        String expected = SortCommand.class.getCanonicalName() + "{sortFields=CLEAR}";

        assertEquals(expected, SortCommand.clearSort().toString());
    }

    /**
     * Adds the same contacts to both models and configures the expected
     * displayed order independently of the command's comparator logic.
     */
    private void prepareModels(List<Person> input, List<Person> expectedOrder) {
        input.forEach(model::addPerson);
        input.forEach(expectedModel::addPerson);

        expectedModel.updateSortedPersonList(
                Comparator.comparingInt(expectedOrder::indexOf));
    }
}
