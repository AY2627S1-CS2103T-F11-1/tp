package seedu.address.logic;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;

import seedu.address.model.person.Person;

/**
 * Describes contact replacements and selection resets from the most recent command attempt.
 * Replacement keys use object identity because every identifying field of a contact can be edited.
 */
public final class CommandSelectionChange {
    private final Map<Person, Person> replacements;
    private final boolean clearSelection;

    /**
     * Copies the command's explicit replacement relationships without relying on Person equality.
     */
    CommandSelectionChange(Map<Person, Person> replacements, boolean clearSelection) {
        this.replacements = Collections.unmodifiableMap(new IdentityHashMap<>(replacements));
        this.clearSelection = clearSelection;
    }

    /**
     * Returns the contact to retain after the command, or null when selection must be cleared.
     * The UI must also check whether the returned object is still in the displayed list.
     */
    public Person applyTo(Person selectedPerson) {
        if (clearSelection) {
            return null;
        }
        return replacements.getOrDefault(selectedPerson, selectedPerson);
    }
}
