package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.CARL;

import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.Person;
import seedu.address.testutil.AddressBookBuilder;

public class ModelManagerTest {

    private ModelManager modelManager = new ModelManager();

    @Test
    public void constructor() {
        assertEquals(new UserPrefs(), modelManager.getUserPrefs());
        assertEquals(new GuiSettings(), modelManager.getGuiSettings());
        assertEquals(new AddressBook(), new AddressBook(modelManager.getAddressBook()));
    }

    @Test
    public void constructor_validUserPrefs_copiesUserPrefs() {
        UserPrefs userPrefs = new UserPrefs();
        userPrefs.setGuiSettings(new GuiSettings(1, 2, 3, 4));
        modelManager = new ModelManager(new AddressBook(), userPrefs);
        assertEquals(userPrefs, modelManager.getUserPrefs());

        // Modifying userPrefs should not modify modelManager's userPrefs
        UserPrefs oldUserPrefs = new UserPrefs(userPrefs);
        userPrefs.setGuiSettings(new GuiSettings(5, 6, 7, 8));
        assertEquals(oldUserPrefs, modelManager.getUserPrefs());
    }

    @Test
    public void setGuiSettings_nullGuiSettings_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.setGuiSettings(null));
    }

    @Test
    public void setGuiSettings_validGuiSettings_setsGuiSettings() {
        GuiSettings guiSettings = new GuiSettings(1, 2, 3, 4);
        modelManager.setGuiSettings(guiSettings);
        assertEquals(guiSettings, modelManager.getGuiSettings());
    }

    @Test
    public void setAddressBook_validAddressBook_replacesData() {
        modelManager.addPerson(ALICE);
        AddressBook replacement = new AddressBookBuilder().withPerson(BENSON).build();
        modelManager.setAddressBook(replacement);
        assertEquals(replacement.getPersonList(), modelManager.getAddressBook().getPersonList());
    }

    @Test
    public void setAddressBook_nullAddressBook_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.setAddressBook(null));
    }

    @Test
    public void getAddressBook_returnsCurrentData() {
        AddressBook initial = new AddressBookBuilder().withPerson(ALICE).build();
        ModelManager model = new ModelManager(initial, new UserPrefs());
        assertEquals(initial.getPersonList(), model.getAddressBook().getPersonList());

        // Modifying model should reflect updated AddressBook
        model.addPerson(BENSON);
        assertEquals(List.of(ALICE, BENSON), model.getAddressBook().getPersonList());
    }

    @Test
    public void commitAddressBook_modifiedData_savesCurrentState() {
        modelManager.addPerson(ALICE);
        modelManager.commitAddressBook();
        assertTrue(modelManager.canUndoAddressBook());
        assertEquals(List.of(ALICE), modelManager.getAddressBook().getPersonList());

        modelManager.addPerson(BENSON);
        modelManager.commitAddressBook();
        assertTrue(modelManager.canUndoAddressBook());
        assertEquals(List.of(ALICE, BENSON), modelManager.getAddressBook().getPersonList());

        modelManager.undoAddressBook();
        modelManager.undoAddressBook();
        assertTrue(modelManager.getAddressBook().getPersonList().isEmpty());

        modelManager.redoAddressBook();
        assertEquals(List.of(ALICE), modelManager.getAddressBook().getPersonList());
    }

    @Test
    public void commitAddressBook_afterUndo_discardsRedoHistory() {
        modelManager.addPerson(ALICE);
        modelManager.commitAddressBook();
        modelManager.addPerson(BENSON);
        modelManager.commitAddressBook();

        modelManager.undoAddressBook();
        assertTrue(modelManager.canRedoAddressBook());

        modelManager.deletePerson(ALICE);
        modelManager.commitAddressBook();
        assertFalse(modelManager.canRedoAddressBook());

        // New branch remains undoable and redoable
        modelManager.undoAddressBook();
        assertEquals(List.of(ALICE), modelManager.getAddressBook().getPersonList());
        modelManager.redoAddressBook();
        assertTrue(modelManager.getAddressBook().getPersonList().isEmpty());
    }

    @Test
    public void undoAddressBook_validHistory_restoresDataAndShowsAllPersons() {
        modelManager.addPerson(ALICE);
        modelManager.addPerson(BENSON);
        modelManager.addPerson(CARL);
        modelManager.commitAddressBook();

        modelManager.deletePerson(BENSON);
        modelManager.commitAddressBook();
        modelManager.updateFilteredPersonList(new NameContainsKeywordsPredicate(List.of("Alice")));

        // Keep existing list reference to check that it updates.
        ObservableList<Person> filteredPersons = modelManager.getFilteredPersonList();
        assertEquals(List.of(ALICE), filteredPersons);

        modelManager.undoAddressBook();
        assertEquals(List.of(ALICE, BENSON, CARL), modelManager.getAddressBook().getPersonList());
        assertEquals(List.of(ALICE, BENSON, CARL), filteredPersons);
        assertTrue(modelManager.canRedoAddressBook());
    }

    @Test
    public void undoAddressBook_noUndoableState_throwsAndPreservesState() {
        AddressBook initial = new AddressBookBuilder().withPerson(ALICE).build();
        modelManager = new ModelManager(initial, new UserPrefs());
        modelManager.updateFilteredPersonList(new NameContainsKeywordsPredicate(List.of("Benson")));

        assertThrows(VersionedAddressBook.NoUndoableStateException.class, () -> modelManager.undoAddressBook());
        assertEquals(List.of(ALICE), modelManager.getAddressBook().getPersonList());
        assertTrue(modelManager.getFilteredPersonList().isEmpty());
        assertFalse(modelManager.canUndoAddressBook());
        assertFalse(modelManager.canRedoAddressBook());
    }

    @Test
    public void redoAddressBook_validHistory_restoresDataAndShowsAllPersons() {
        modelManager.addPerson(ALICE);
        modelManager.addPerson(BENSON);
        modelManager.commitAddressBook();

        modelManager.addPerson(CARL);
        modelManager.commitAddressBook();
        modelManager.undoAddressBook();
        modelManager.updateFilteredPersonList(new NameContainsKeywordsPredicate(List.of("Alice")));

        // Keep existing list reference to check that it updates.
        ObservableList<Person> filteredPersons = modelManager.getFilteredPersonList();
        assertEquals(List.of(ALICE), filteredPersons);

        modelManager.redoAddressBook();
        assertEquals(List.of(ALICE, BENSON, CARL), modelManager.getAddressBook().getPersonList());
        assertEquals(List.of(ALICE, BENSON, CARL), filteredPersons);
        assertFalse(modelManager.canRedoAddressBook());
    }

    @Test
    public void redoAddressBook_noRedoableState_throwsAndPreservesState() {
        AddressBook initial = new AddressBookBuilder().withPerson(ALICE).build();
        modelManager = new ModelManager(initial, new UserPrefs());
        modelManager.updateFilteredPersonList(new NameContainsKeywordsPredicate(List.of("Benson")));

        assertThrows(VersionedAddressBook.NoRedoableStateException.class, () -> modelManager.redoAddressBook());
        assertEquals(List.of(ALICE), modelManager.getAddressBook().getPersonList());
        assertTrue(modelManager.getFilteredPersonList().isEmpty());
        assertFalse(modelManager.canUndoAddressBook());
        assertFalse(modelManager.canRedoAddressBook());
    }

    @Test
    public void canUndoAddressBook_initialState_returnsFalse() {
        assertFalse(modelManager.canUndoAddressBook());
    }

    @Test
    public void canUndoAddressBook_afterCommit_returnsTrue() {
        modelManager.addPerson(ALICE);
        modelManager.commitAddressBook();

        assertTrue(modelManager.canUndoAddressBook());
    }

    @Test
    public void canUndoAddressBook_afterUndoToInitialState_returnsFalse() {
        modelManager.addPerson(ALICE);
        modelManager.commitAddressBook();
        modelManager.undoAddressBook();

        assertFalse(modelManager.canUndoAddressBook());
    }

    @Test
    public void canRedoAddressBook_initialState_returnsFalse() {
        assertFalse(modelManager.canRedoAddressBook());
    }

    @Test
    public void canRedoAddressBook_afterUndo_returnsTrue() {
        modelManager.addPerson(ALICE);
        modelManager.commitAddressBook();
        modelManager.undoAddressBook();

        assertTrue(modelManager.canRedoAddressBook());
    }

    @Test
    public void canRedoAddressBook_afterRedoToLatestState_returnsFalse() {
        modelManager.addPerson(ALICE);
        modelManager.commitAddressBook();
        modelManager.undoAddressBook();
        modelManager.redoAddressBook();

        assertFalse(modelManager.canRedoAddressBook());
    }

    @Test
    public void hasPerson_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.hasPerson(null));
    }

    @Test
    public void hasPerson_personNotInAddressBook_returnsFalse() {
        assertFalse(modelManager.hasPerson(ALICE));
    }

    @Test
    public void hasPerson_personInAddressBook_returnsTrue() {
        modelManager.addPerson(ALICE);
        assertTrue(modelManager.hasPerson(ALICE));
    }

    @Test
    public void hasPhone_nullPhone_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.hasPhone(null));
        assertThrows(NullPointerException.class, () -> modelManager.hasPhone(null, ALICE));
    }

    @Test
    public void hasPhone_phoneInAddressBook_returnsTrue() {
        modelManager.addPerson(ALICE);
        assertTrue(modelManager.hasPhone(ALICE.getPhone()));
    }

    @Test
    public void hasPhone_phoneNotInAddressBook_returnsFalse() {
        assertFalse(modelManager.hasPhone(ALICE.getPhone()));
    }

    @Test
    public void hasPhone_phoneWithExcludedPerson_returnsFalse() {
        modelManager.addPerson(ALICE);
        assertFalse(modelManager.hasPhone(ALICE.getPhone(), ALICE));
    }

    @Test
    public void hasEmail_nullEmail_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.hasEmail(null));
        assertThrows(NullPointerException.class, () -> modelManager.hasEmail(null, ALICE));
    }

    @Test
    public void hasEmail_emailInAddressBook_returnsTrue() {
        modelManager.addPerson(ALICE);
        assertTrue(modelManager.hasEmail(ALICE.getEmail()));
    }

    @Test
    public void hasEmail_emailNotInAddressBook_returnsFalse() {
        assertFalse(modelManager.hasEmail(ALICE.getEmail()));
    }

    @Test
    public void hasEmail_emailWithExcludedPerson_returnsFalse() {
        modelManager.addPerson(ALICE);
        assertFalse(modelManager.hasEmail(ALICE.getEmail(), ALICE));
    }

    @Test
    public void getFilteredPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> modelManager.getFilteredPersonList().remove(0));
    }

    @Test
    public void equals() {
        AddressBook addressBook = new AddressBookBuilder().withPerson(ALICE).withPerson(BENSON).build();
        AddressBook differentAddressBook = new AddressBook();
        UserPrefs userPrefs = new UserPrefs();

        // same values -> returns true
        modelManager = new ModelManager(addressBook, userPrefs);
        ModelManager modelManagerCopy = new ModelManager(addressBook, userPrefs);
        assertTrue(modelManager.equals(modelManagerCopy));

        // same object -> returns true
        assertTrue(modelManager.equals(modelManager));

        // null -> returns false
        assertFalse(modelManager.equals(null));

        // different types -> returns false
        assertFalse(modelManager.equals(5));

        // different addressBook -> returns false
        assertFalse(modelManager.equals(new ModelManager(differentAddressBook, userPrefs)));

        // different filteredList -> returns false
        String[] keywords = ALICE.getName().fullName.split("\\s+");
        modelManager.updateFilteredPersonList(new NameContainsKeywordsPredicate(List.of(keywords)));
        assertFalse(modelManager.equals(new ModelManager(addressBook, userPrefs)));

        // resets modelManager to initial state for upcoming tests
        modelManager.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);

        // different userPrefs -> returns false
        UserPrefs differentUserPrefs = new UserPrefs();
        differentUserPrefs.setGuiSettings(new GuiSettings(1, 2, 3, 4));
        assertFalse(modelManager.equals(new ModelManager(addressBook, differentUserPrefs)));
    }
}
