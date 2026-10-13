package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ADDRESS_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BOB;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.exceptions.DuplicatePersonException;
import seedu.address.model.person.exceptions.PersonNotFoundException;
import seedu.address.testutil.PersonBuilder;

public class UniquePersonListTest {

    private final UniquePersonList uniquePersonList = new UniquePersonList();

    @Test
    public void contains_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniquePersonList.contains(null));
    }

    @Test
    public void contains_personNotInList_returnsFalse() {
        assertFalse(uniquePersonList.contains(ALICE));
    }

    @Test
    public void contains_personInList_returnsTrue() {
        uniquePersonList.add(ALICE);
        assertTrue(uniquePersonList.contains(ALICE));
    }

    @Test
    public void contains_personWithSameIdentityFieldsInList_returnsTrue() {
        uniquePersonList.add(ALICE);
        Person editedAlice = new PersonBuilder(ALICE).withAddress(VALID_ADDRESS_BOB).withTags(VALID_TAG_HUSBAND)
                .build();
        assertTrue(uniquePersonList.contains(editedAlice));
    }

    @Test
    public void containsOther_nullToCheck_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniquePersonList.containsOther(null, ALICE));
    }

    @Test
    public void containsOther_nullExcludePerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniquePersonList.containsOther(ALICE, null));
    }

    @Test
    public void containsOther_noOtherPerson_returnsFalse() {
        uniquePersonList.add(ALICE);
        assertFalse(uniquePersonList.containsOther(ALICE, ALICE));
    }

    @Test
    public void containsOther_otherPersonSharesPhone_returnsTrue() {
        uniquePersonList.add(ALICE);
        uniquePersonList.add(BOB);
        Person toCheck = new PersonBuilder().withPhone(BOB.getPhone().value).build();
        assertTrue(uniquePersonList.containsOther(toCheck, ALICE));
    }

    @Test
    public void containsOther_otherPersonSharesEmail_returnsTrue() {
        uniquePersonList.add(ALICE);
        uniquePersonList.add(BOB);
        Person toCheck = new PersonBuilder().withEmail(BOB.getEmail().value).build();
        assertTrue(uniquePersonList.containsOther(toCheck, ALICE));
    }

    @Test
    public void containsOther_distinctIdentity_returnsFalse() {
        uniquePersonList.add(ALICE);
        uniquePersonList.add(BOB);
        Person toCheck = new PersonBuilder().withPhone("11112222").withEmail("unique@example.com").build();
        assertFalse(uniquePersonList.containsOther(toCheck, ALICE));
    }

    @Test
    public void hasPhone_nullPhone_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniquePersonList.hasPhone(null));
        assertThrows(NullPointerException.class, () -> uniquePersonList.hasPhone(null, ALICE));
    }

    @Test
    public void hasPhone_phoneInList_returnsTrue() {
        uniquePersonList.add(ALICE);
        assertTrue(uniquePersonList.hasPhone(ALICE.getPhone()));
    }

    @Test
    public void hasPhone_phoneNotInList_returnsFalse() {
        assertFalse(uniquePersonList.hasPhone(ALICE.getPhone()));
    }

    @Test
    public void hasPhone_phoneOfExcludedPerson_returnsFalse() {
        uniquePersonList.add(ALICE);
        assertFalse(uniquePersonList.hasPhone(ALICE.getPhone(), ALICE));
    }

    @Test
    public void hasPhone_phoneOfNonExcludedPerson_returnsTrue() {
        uniquePersonList.add(ALICE);
        uniquePersonList.add(BOB);
        assertTrue(uniquePersonList.hasPhone(ALICE.getPhone(), BOB));
    }

    @Test
    public void hasEmail_nullEmail_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniquePersonList.hasEmail(null));
        assertThrows(NullPointerException.class, () -> uniquePersonList.hasEmail(null, ALICE));
    }

    @Test
    public void hasEmail_emailInList_returnsTrue() {
        uniquePersonList.add(ALICE);
        assertTrue(uniquePersonList.hasEmail(ALICE.getEmail()));
    }

    @Test
    public void hasEmail_emailNotInList_returnsFalse() {
        assertFalse(uniquePersonList.hasEmail(ALICE.getEmail()));
    }

    @Test
    public void hasEmail_emailOfExcludedPerson_returnsFalse() {
        uniquePersonList.add(ALICE);
        assertFalse(uniquePersonList.hasEmail(ALICE.getEmail(), ALICE));
    }

    @Test
    public void hasEmail_emailOfNonExcludedPerson_returnsTrue() {
        uniquePersonList.add(ALICE);
        uniquePersonList.add(BOB);
        assertTrue(uniquePersonList.hasEmail(ALICE.getEmail(), BOB));
    }

    @Test
    public void add_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniquePersonList.add(null));
    }

    @Test
    public void add_duplicatePerson_throwsDuplicatePersonException() {
        uniquePersonList.add(ALICE);
        assertThrows(DuplicatePersonException.class, () -> uniquePersonList.add(ALICE));
    }

    @Test
    public void add_personWithDuplicatePhone_throwsDuplicatePersonException() {
        uniquePersonList.add(ALICE);
        Person duplicatePhone = new PersonBuilder().withPhone(ALICE.getPhone().value).build();
        assertThrows(DuplicatePersonException.class, () -> uniquePersonList.add(duplicatePhone));
    }

    @Test
    public void add_personWithDuplicateEmail_throwsDuplicatePersonException() {
        uniquePersonList.add(ALICE);
        Person duplicateEmail = new PersonBuilder().withEmail(ALICE.getEmail().value).build();
        assertThrows(DuplicatePersonException.class, () -> uniquePersonList.add(duplicateEmail));
    }

    @Test
    public void add_personWithSameNameDifferentPhoneAndEmail_success() {
        uniquePersonList.add(ALICE);
        Person sameNameDifferentContact = new PersonBuilder().withName(ALICE.getName().fullName)
                .withPhone(BOB.getPhone().value).withEmail(BOB.getEmail().value).build();
        uniquePersonList.add(sameNameDifferentContact);
        assertTrue(uniquePersonList.contains(sameNameDifferentContact));
    }

    @Test
    public void setPerson_nullTargetPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniquePersonList.setPerson(null, ALICE));
    }

    @Test
    public void setPerson_nullEditedPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniquePersonList.setPerson(ALICE, null));
    }

    @Test
    public void setPerson_targetPersonNotInList_throwsPersonNotFoundException() {
        assertThrows(PersonNotFoundException.class, () -> uniquePersonList.setPerson(ALICE, ALICE));
    }

    @Test
    public void setPerson_editedPersonIsSamePerson_success() {
        uniquePersonList.add(ALICE);
        uniquePersonList.setPerson(ALICE, ALICE);
        UniquePersonList expectedUniquePersonList = new UniquePersonList();
        expectedUniquePersonList.add(ALICE);
        assertEquals(expectedUniquePersonList, uniquePersonList);
    }

    @Test
    public void setPerson_editedPersonHasSameIdentity_success() {
        uniquePersonList.add(ALICE);
        Person editedAlice = new PersonBuilder(ALICE).withAddress(VALID_ADDRESS_BOB).withTags(VALID_TAG_HUSBAND)
                .build();
        uniquePersonList.setPerson(ALICE, editedAlice);
        UniquePersonList expectedUniquePersonList = new UniquePersonList();
        expectedUniquePersonList.add(editedAlice);
        assertEquals(expectedUniquePersonList, uniquePersonList);
    }

    @Test
    public void setPerson_editedPersonHasDifferentIdentity_success() {
        uniquePersonList.add(ALICE);
        uniquePersonList.setPerson(ALICE, BOB);
        UniquePersonList expectedUniquePersonList = new UniquePersonList();
        expectedUniquePersonList.add(BOB);
        assertEquals(expectedUniquePersonList, uniquePersonList);
    }

    @Test
    public void setPerson_editedPersonHasNonUniqueIdentity_throwsDuplicatePersonException() {
        uniquePersonList.add(ALICE);
        uniquePersonList.add(BOB);
        assertThrows(DuplicatePersonException.class, () -> uniquePersonList.setPerson(ALICE, BOB));
    }

    @Test
    public void setPerson_editedPersonCollidesWithOtherPersonPhone_throwsDuplicatePersonException() {
        uniquePersonList.add(ALICE);
        uniquePersonList.add(BOB);
        Person editedBob = new PersonBuilder(BOB).withPhone(ALICE.getPhone().value).build();
        assertThrows(DuplicatePersonException.class, () -> uniquePersonList.setPerson(BOB, editedBob));
    }

    @Test
    public void setPerson_editedPersonCollidesWithOtherPersonEmail_throwsDuplicatePersonException() {
        uniquePersonList.add(ALICE);
        uniquePersonList.add(BOB);
        Person editedBob = new PersonBuilder(BOB).withEmail(ALICE.getEmail().value).build();
        assertThrows(DuplicatePersonException.class, () -> uniquePersonList.setPerson(BOB, editedBob));
    }

    @Test
    public void remove_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniquePersonList.remove(null));
    }

    @Test
    public void remove_personDoesNotExist_throwsPersonNotFoundException() {
        assertThrows(PersonNotFoundException.class, () -> uniquePersonList.remove(ALICE));
    }

    @Test
    public void remove_existingPerson_removesPerson() {
        uniquePersonList.add(ALICE);
        uniquePersonList.remove(ALICE);
        UniquePersonList expectedUniquePersonList = new UniquePersonList();
        assertEquals(expectedUniquePersonList, uniquePersonList);
    }

    @Test
    public void setPersons_nullUniquePersonList_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniquePersonList.setPersons((UniquePersonList) null));
    }

    @Test
    public void setPersons_uniquePersonList_replacesOwnListWithProvidedUniquePersonList() {
        uniquePersonList.add(ALICE);
        UniquePersonList expectedUniquePersonList = new UniquePersonList();
        expectedUniquePersonList.add(BOB);
        uniquePersonList.setPersons(expectedUniquePersonList);
        assertEquals(expectedUniquePersonList, uniquePersonList);
    }

    @Test
    public void setPersons_nullList_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniquePersonList.setPersons((List<Person>) null));
    }

    @Test
    public void setPersons_list_replacesOwnListWithProvidedList() {
        uniquePersonList.add(ALICE);
        List<Person> personList = List.of(BOB);
        uniquePersonList.setPersons(personList);
        UniquePersonList expectedUniquePersonList = new UniquePersonList();
        expectedUniquePersonList.add(BOB);
        assertEquals(expectedUniquePersonList, uniquePersonList);
    }

    @Test
    public void setPersons_listWithDuplicatePersons_throwsDuplicatePersonException() {
        List<Person> listWithDuplicatePersons = List.of(ALICE, ALICE);
        assertThrows(DuplicatePersonException.class, () -> uniquePersonList.setPersons(listWithDuplicatePersons));
    }

    @Test
    public void asUnmodifiableObservableList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, ()
            -> uniquePersonList.asUnmodifiableObservableList().remove(0));
    }

    @Test
    public void toStringMethod() {
        assertEquals(uniquePersonList.asUnmodifiableObservableList().toString(), uniquePersonList.toString());
    }
}
