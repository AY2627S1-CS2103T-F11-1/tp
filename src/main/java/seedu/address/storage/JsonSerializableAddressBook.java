package seedu.address.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Person;

/**
 * An Immutable AddressBook that is serializable to JSON format.
 */
@JsonRootName(value = "addressbook")
class JsonSerializableAddressBook {

    public static final String MESSAGE_DUPLICATE_PERSON = "Persons list contains duplicate person(s).";
    public static final String MESSAGE_NULL_PERSON = "Person entry cannot be null.";

    private final List<JsonAdaptedPerson> persons = new ArrayList<>();

    /**
     * Constructs a {@code JsonSerializableAddressBook} with the given persons.
     */
    @JsonCreator
    public JsonSerializableAddressBook(@JsonProperty("persons") List<JsonAdaptedPerson> persons) {
        if (persons != null) {
            this.persons.addAll(persons);
        }
    }

    /**
     * Converts a given {@code ReadOnlyAddressBook} into this class for Jackson use.
     *
     * @param source future changes to this will not affect the created {@code JsonSerializableAddressBook}.
     */
    public JsonSerializableAddressBook(ReadOnlyAddressBook source) {
        persons.addAll(source.getPersonList().stream().map(JsonAdaptedPerson::new).collect(Collectors.toList()));
    }

    /**
     * Converts this address book into the model's {@code AddressBook} object.
     *
     * @throws IllegalValueException if there were any data constraints violated.
     */
    public AddressBook toModelType() throws IllegalValueException {
        AddressBook addressBook = new AddressBook();
        for (JsonAdaptedPerson jsonAdaptedPerson : persons) {
            if (jsonAdaptedPerson == null) {
                throw new IllegalValueException(MESSAGE_NULL_PERSON);
            }
            Person person = jsonAdaptedPerson.toModelType();
            if (addressBook.hasPerson(person)) {
                throw new IllegalValueException(getDuplicatePersonMessage(addressBook, person));
            }
            addressBook.addPerson(person);
        }
        return addressBook;
    }

    private String getDuplicatePersonMessage(AddressBook addressBook, Person duplicate) {
        for (Person existing : addressBook.getPersonList()) {
            boolean samePhone = existing.getPhone().equals(duplicate.getPhone());
            boolean sameEmail = existing.getEmail().equals(duplicate.getEmail());
            if (!samePhone && !sameEmail) {
                continue;
            }
            String field = samePhone && sameEmail
                    ? "phone number " + duplicate.getPhone() + " and email address " + duplicate.getEmail()
                    : samePhone ? "phone number " + duplicate.getPhone() : "email address " + duplicate.getEmail();
            return existing.getName().equals(duplicate.getName())
                    ? String.format("Duplicate contact detected: '%s' is defined multiple times with %s.",
                            duplicate.getName(), field)
                    : String.format("Duplicate contact detected: '%s' shares %s with '%s'.",
                            duplicate.getName(), field, existing.getName());
        }
        return MESSAGE_DUPLICATE_PERSON;
    }

}
