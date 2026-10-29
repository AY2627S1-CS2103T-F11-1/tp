package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class CommandSelectionChangeTest {
    @Test
    public void applyTo_withoutRequest_preservesReplacementAndIdentityRules() {
        Person edited = new PersonBuilder(ALICE).withName("Updated Supplier").build();
        CommandSelectionChange change = new CommandSelectionChange(Map.of(ALICE, edited), false);
        assertSame(edited, change.applyTo(ALICE));
        assertSame(BENSON, change.applyTo(BENSON));
        assertNull(change.applyTo(null));
        Person equalButDistinct = new PersonBuilder(ALICE).build();
        assertSame(equalButDistinct, change.applyTo(equalButDistinct));
    }

    @Test
    public void applyTo_explicitRequest_overridesPreviousAndReplacementSelection() {
        Person edited = new PersonBuilder(ALICE).withName("Updated Supplier").build();
        CommandSelectionChange change = new CommandSelectionChange(Map.of(ALICE, edited), false, Optional.of(BENSON));
        assertSame(BENSON, change.applyTo(null));
        assertSame(BENSON, change.applyTo(ALICE));
        assertSame(BENSON, change.applyTo(BENSON));
    }

    @Test
    public void applyTo_explicitRequest_keepsExactReferenceEvenWhenValuesEqual() {
        Person requested = new PersonBuilder(ALICE).build();
        CommandSelectionChange change = new CommandSelectionChange(Map.of(), false, Optional.of(requested));
        assertSame(requested, change.applyTo(ALICE));
    }

    @Test
    public void applyTo_reset_overridesExplicitRequestAndReplacement() {
        Person edited = new PersonBuilder(ALICE).withName("Updated Supplier").build();
        CommandSelectionChange change = new CommandSelectionChange(Map.of(ALICE, edited), true, Optional.of(BENSON));
        assertNull(change.applyTo(null));
        assertNull(change.applyTo(ALICE));
        assertNull(change.applyTo(BENSON));
    }
}
