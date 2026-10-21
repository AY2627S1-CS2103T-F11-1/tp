package seedu.address.model;

import java.util.function.Predicate;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.model.person.Email;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;

/**
 * The API of the Model component.
 */
public interface Model {
    /** {@code Predicate} that always evaluates to true */
    Predicate<Person> PREDICATE_SHOW_ALL_PERSONS = unused -> true;

    /**
     * Returns the user prefs.
     */
    ReadOnlyUserPrefs getUserPrefs();

    /**
     * Returns the user prefs' GUI settings.
     */
    GuiSettings getGuiSettings();

    /**
     * Sets the user prefs' GUI settings.
     */
    void setGuiSettings(GuiSettings guiSettings);

    /**
     * Replaces address book data with the data in {@code addressBook}.
     */
    void setAddressBook(ReadOnlyAddressBook addressBook);

    /** Returns the AddressBook */
    ReadOnlyAddressBook getAddressBook();

    /**
     * Saves the current address book state for undo/redo.
     * Discards any remaining redo history.
     */
    void commitAddressBook();

    /**
     * Restores the previous address book state and resets the filter to show all persons.
     *
     * @throws VersionedAddressBook.NoUndoableStateException if there is no previous state.
     */
    void undoAddressBook();

    /**
     * Restores the next address book state and resets the filter to show all persons.
     *
     * @throws VersionedAddressBook.NoRedoableStateException if there is no next state.
     */
    void redoAddressBook();

    /**
     * Returns true if there is a previous address book state to restore.
     */
    boolean canUndoAddressBook();

    /**
     * Returns true if there is a next address book state to restore.
     */
    boolean canRedoAddressBook();

    /**
     * Returns true if a person with the same identity as {@code person} exists in the address book.
     */
    boolean hasPerson(Person person);

    /**
     * Returns true if a person with the same phone number exists in the address book.
     */
    boolean hasPhone(Phone phone);

    /**
     * Returns true if a person with the same phone number exists in the address book,
     * excluding {@code excludePerson} if specified.
     */
    boolean hasPhone(Phone phone, Person excludePerson);

    /**
     * Returns true if a person with the same email address exists in the address book.
     */
    boolean hasEmail(Email email);

    /**
     * Returns true if a person with the same email address exists in the address book,
     * excluding {@code excludePerson} if specified.
     */
    boolean hasEmail(Email email, Person excludePerson);

    /**
     * Deletes the given person.
     * The person must exist in the address book.
     */
    void deletePerson(Person target);

    /**
     * Adds the given person.
     * {@code person} must not already exist in the address book.
     */
    void addPerson(Person person);

    /**
     * Replaces the given person {@code target} with {@code editedPerson}.
     * {@code target} must exist in the address book.
     * The person identity of {@code editedPerson} must not be the same as another existing person in the address book.
     */
    void setPerson(Person target, Person editedPerson);

    /** Returns an unmodifiable view of the filtered person list */
    ObservableList<Person> getFilteredPersonList();

    /**
     * Updates the filter of the filtered person list to filter by the given {@code predicate}.
     * @throws NullPointerException if {@code predicate} is null.
     */
    void updateFilteredPersonList(Predicate<Person> predicate);
}
