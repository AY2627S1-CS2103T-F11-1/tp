package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_FOLLOW_UP_DESCRIPTION;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.CARL;
import static seedu.address.testutil.TypicalPersons.DANIEL;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class FollowUpsCommandTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 11, 1);
    private static final Instant CURRENT_INSTANT =
            Instant.parse("2026-11-01T12:00:00Z");
    private static final Clock FIXED_CLOCK =
            Clock.fixed(CURRENT_INSTANT, ZoneOffset.UTC);

    // Deliberately use an order different from due-date order.
    private static final Person FUTURE_CONTACT = new PersonBuilder(ALICE)
            .withFollowUp(TODAY.plusDays(1).toString(), VALID_FOLLOW_UP_DESCRIPTION)
            .build();
    private static final Person PAST_CONTACT = new PersonBuilder(BENSON)
            .withFollowUp(TODAY.minusDays(1).toString(), VALID_FOLLOW_UP_DESCRIPTION)
            .build();
    private static final Person TODAY_CONTACT = new PersonBuilder(CARL)
            .withFollowUp(TODAY.toString(), VALID_FOLLOW_UP_DESCRIPTION)
            .build();
    private static final Person NO_FOLLOW_UP_CONTACT =
            new PersonBuilder(DANIEL).withoutFollowUp().build();

    private final Model model = new ModelManager(createAddressBook(), new UserPrefs());

    @Test
    public void overdue_nullClock_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> FollowUpsCommand.overdue(null));
    }

    @Test
    public void overdue_defaultFactory_usesSystemDefaultClock() {
        assertEquals(FollowUpsCommand.overdue(Clock.systemDefaultZone()),
                FollowUpsCommand.overdue());
    }

    @Test
    public void dueBy_nullDate_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> FollowUpsCommand.dueBy(null));
    }

    @Test
    public void dueBy_unsupportedYear_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                FollowUpsCommand.dueBy(LocalDate.of(-1, 1, 1)));
        assertThrows(IllegalArgumentException.class, () ->
                FollowUpsCommand.dueBy(LocalDate.of(10000, 1, 1)));
    }

    @Test
    public void dueBy_yearBoundaries_success() {
        assertQueryResult(FollowUpsCommand.dueBy(LocalDate.of(0, 1, 1)),
                List.of());
        assertQueryResult(FollowUpsCommand.dueBy(LocalDate.of(9999, 12, 31)),
                List.of(FUTURE_CONTACT, PAST_CONTACT, TODAY_CONTACT));
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> FollowUpsCommand.all().execute(null));
        assertThrows(NullPointerException.class, () -> FollowUpsCommand.overdue(FIXED_CLOCK).execute(null));
        assertThrows(NullPointerException.class, () -> FollowUpsCommand.dueBy(TODAY).execute(null));
    }

    @Test
    public void execute_all_returnsOnlyContactsWithFollowUpsInAddressBookOrder() {
        assertQueryResult(FollowUpsCommand.all(),
                List.of(FUTURE_CONTACT, PAST_CONTACT, TODAY_CONTACT));
    }

    @Test
    public void execute_overdue_excludesTodayAndFutureDates() {
        assertQueryResult(FollowUpsCommand.overdue(FIXED_CLOCK),
                List.of(PAST_CONTACT));
    }

    @Test
    public void execute_dueBy_includesCutoffDate() {
        assertQueryResult(FollowUpsCommand.dueBy(TODAY),
                List.of(PAST_CONTACT, TODAY_CONTACT));
    }

    @Test
    public void execute_dueByPastDate_success() {
        assertQueryResult(FollowUpsCommand.dueBy(TODAY.minusDays(1)),
                List.of(PAST_CONTACT));
    }

    @Test
    public void execute_dueByFutureDate_preservesAddressBookOrder() {
        assertQueryResult(FollowUpsCommand.dueBy(TODAY.plusDays(1)),
                List.of(FUTURE_CONTACT, PAST_CONTACT, TODAY_CONTACT));
    }

    @Test
    public void execute_noMatches_displaysEmptyList() {
        assertQueryResult(FollowUpsCommand.dueBy(TODAY.minusDays(2)),
                List.of());
    }

    @Test
    public void execute_emptyAddressBook_displaysNoFollowUpsForEveryMode() {
        model.setAddressBook(new AddressBook());

        assertQueryResult(FollowUpsCommand.all(), List.of());
        assertQueryResult(FollowUpsCommand.overdue(FIXED_CLOCK), List.of());
        assertQueryResult(FollowUpsCommand.dueBy(TODAY), List.of());
    }

    @Test
    public void execute_contactsWithoutFollowUps_displaysNoFollowUpsForEveryMode() {
        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(NO_FOLLOW_UP_CONTACT);
        model.setAddressBook(addressBook);

        assertQueryResult(FollowUpsCommand.all(), List.of());
        assertQueryResult(FollowUpsCommand.overdue(FIXED_CLOCK), List.of());
        assertQueryResult(FollowUpsCommand.dueBy(TODAY), List.of());
    }

    @Test
    public void execute_existingFilter_searchesEntireAddressBook() {
        model.updateFilteredPersonList(person -> person.equals(NO_FOLLOW_UP_CONTACT));
        assertQueryResult(FollowUpsCommand.all(),
                List.of(FUTURE_CONTACT, PAST_CONTACT, TODAY_CONTACT));

        model.updateFilteredPersonList(person -> person.equals(FUTURE_CONTACT));
        assertQueryResult(FollowUpsCommand.overdue(FIXED_CLOCK),
                List.of(PAST_CONTACT));

        model.updateFilteredPersonList(person -> false);
        assertQueryResult(FollowUpsCommand.dueBy(TODAY),
                List.of(PAST_CONTACT, TODAY_CONTACT));
    }

    @Test
    public void execute_overdue_usesClockTimeZone() {
        Instant instant = Instant.parse("2026-11-01T18:00:00Z");
        Clock utcClock = Clock.fixed(instant, ZoneOffset.UTC);
        Clock singaporeClock = Clock.fixed(instant, ZoneId.of("Asia/Singapore"));

        // It is November 1 in UTC, but November 2 in Singapore.
        assertQueryResult(FollowUpsCommand.overdue(utcClock),
                List.of(PAST_CONTACT));
        assertQueryResult(FollowUpsCommand.overdue(singaporeClock),
                List.of(PAST_CONTACT, TODAY_CONTACT));
    }

    @Test
    public void execute_queries_preserveUndoAndRedoHistory() {
        AddressBook initialState = new AddressBook(model.getAddressBook());
        Person modified = new PersonBuilder(NO_FOLLOW_UP_CONTACT)
                .withName("Updated Contact").build();

        model.setPerson(NO_FOLLOW_UP_CONTACT, modified);
        model.commitAddressBook();
        AddressBook modifiedState = new AddressBook(model.getAddressBook());

        model.undoAddressBook();
        assertTrue(model.canRedoAddressBook());

        assertQueryResult(FollowUpsCommand.all(),
                List.of(FUTURE_CONTACT, PAST_CONTACT, TODAY_CONTACT));
        assertQueryResult(FollowUpsCommand.overdue(FIXED_CLOCK),
                List.of(PAST_CONTACT));
        assertQueryResult(FollowUpsCommand.dueBy(TODAY),
                List.of(PAST_CONTACT, TODAY_CONTACT));

        model.redoAddressBook();
        assertEquals(modifiedState, model.getAddressBook());
        assertFalse(model.canRedoAddressBook());

        model.undoAddressBook();
        assertEquals(initialState, model.getAddressBook());
        assertFalse(model.canUndoAddressBook());
    }

    @Test
    public void equals() {
        FollowUpsCommand allCommand = FollowUpsCommand.all();
        FollowUpsCommand overdueCommand = FollowUpsCommand.overdue(FIXED_CLOCK);
        FollowUpsCommand dueByCommand = FollowUpsCommand.dueBy(TODAY);

        // same object -> returns true
        assertTrue(allCommand.equals(allCommand));

        // FollowUpsCommand of type Model.ALL are equal -> returns true
        assertTrue(allCommand.equals(FollowUpsCommand.all()));

        // FollowUpsCommand of type Model.OVERDUE of same day are equal -> returns true
        assertTrue(overdueCommand.equals(FollowUpsCommand.overdue(
                Clock.fixed(CURRENT_INSTANT, ZoneOffset.UTC))));

        // FollowUpsCommand of type Model.BY_DATE of same day are equal -> returns true
        assertTrue(dueByCommand.equals(FollowUpsCommand.dueBy(TODAY)));

        // null -> returns false
        assertFalse(allCommand.equals(null));

        // different types -> returns false
        assertFalse(allCommand.equals(new UndoCommand()));

        // different modes -> returns false
        assertFalse(allCommand.equals(overdueCommand));
        assertFalse(overdueCommand.equals(dueByCommand));
        assertFalse(dueByCommand.equals(allCommand));

        // different dates in same mode -> returns false
        assertFalse(dueByCommand.equals(FollowUpsCommand.dueBy(TODAY.plusDays(1))));
        assertFalse(overdueCommand.equals(FollowUpsCommand.overdue(
                Clock.offset(FIXED_CLOCK, java.time.Duration.ofDays(1)))));
        assertFalse(overdueCommand.equals(FollowUpsCommand.overdue(
                FIXED_CLOCK.withZone(ZoneId.of("Asia/Singapore")))));
    }

    @Test
    public void toStringMethod() {
        FollowUpsCommand command = FollowUpsCommand.dueBy(TODAY);
        String expected = FollowUpsCommand.class.getCanonicalName()
                + "{mode=BY_DATE, cutoffDate=" + TODAY + "}";

        assertEquals(expected, command.toString());
    }

    /**
     * Verifies the result and displayed order without changing contact data or history.
     */
    private void assertQueryResult(FollowUpsCommand command, List<Person> expectedContacts) {
        AddressBook originalState = new AddressBook(model.getAddressBook());
        boolean couldUndo = model.canUndoAddressBook();
        boolean couldRedo = model.canRedoAddressBook();

        CommandResult result = command.execute(model);

        String expectedMessage = expectedContacts.isEmpty()
                ? FollowUpsCommand.MESSAGE_NO_FOLLOW_UPS
                : String.format(FollowUpsCommand.MESSAGE_SUCCESS, expectedContacts.size());

        assertEquals(new CommandResult(expectedMessage), result);
        assertEquals(expectedContacts, model.getFilteredPersonList());
        assertEquals(originalState, model.getAddressBook());
        assertEquals(couldUndo, model.canUndoAddressBook());
        assertEquals(couldRedo, model.canRedoAddressBook());
    }

    /**
     * Creates contacts covering future, past, present, and absent follow-up dates.
     */
    private static AddressBook createAddressBook() {
        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(FUTURE_CONTACT);
        addressBook.addPerson(PAST_CONTACT);
        addressBook.addPerson(TODAY_CONTACT);
        addressBook.addPerson(NO_FOLLOW_UP_CONTACT);
        return addressBook;
    }
}
