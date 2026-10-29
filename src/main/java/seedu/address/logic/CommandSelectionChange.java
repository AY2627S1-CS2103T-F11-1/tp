package seedu.address.logic;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;

import seedu.address.model.person.Person;

/**
 * Describes contact replacements, explicit selection requests, and resets from the most recent command attempt.
 * Replacement keys use object identity because every identifying field of a contact can be edited.
 */
public final class CommandSelectionChange {
    private final Map<Person, Person> replacements;
    private final boolean clearSelection;
    private final Optional<Person> requestedPerson;

    /**
     * Copies the command's explicit replacement relationships without relying on Person equality.
     */
    CommandSelectionChange(Map<Person, Person> replacements, boolean clearSelection) {
        this(replacements, clearSelection, Optional.empty());
    }

    /**
     * Copies replacement relationships and an optional exact contact selection request.
     */
    CommandSelectionChange(Map<Person, Person> replacements, boolean clearSelection, Optional<Person> requestedPerson) {
        this.replacements = Collections.unmodifiableMap(new IdentityHashMap<>(replacements));
        this.clearSelection = clearSelection;
        this.requestedPerson = requireNonNull(requestedPerson);
    }

    /**
     * Returns the contact to retain after the command, or null when selection must be cleared.
     * Resets take precedence over explicit requests, which take precedence over retaining the previous selection.
     * The UI must also check whether the returned object is still in the displayed list.
     */
    public Person applyTo(Person selectedPerson) {
        if (clearSelection) {
            return null;
        }
        if (requestedPerson.isPresent()) {
            return requestedPerson.get();
        }
        return replacements.getOrDefault(selectedPerson, selectedPerson);
    }
}
