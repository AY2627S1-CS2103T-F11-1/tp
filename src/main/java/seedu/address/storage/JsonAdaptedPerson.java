package seedu.address.storage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.FollowUp;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Role;
import seedu.address.model.tag.Tag;

/**
 * Jackson-friendly version of {@link Person}.
 */
class JsonAdaptedPerson {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Person's %s field is missing!";

    private final String name;
    private final String phone;
    private final String email;
    private final String address;
    private final String role;
    private final List<JsonAdaptedTag> tags = new ArrayList<>();
    // Null represents an absent follow-up, including in older data files.
    private final JsonAdaptedFollowUp followUp;

    /**
     * Constructs an adapter without a follow-up.
     */
    public JsonAdaptedPerson(String name, String phone, String email,
            String address, String role, List<JsonAdaptedTag> tags) {
        this(name, phone, email, address, role, tags, null);
    }

    /**
     * Constructs a {@code JsonAdaptedPerson} with the given person details.
     * A missing or null follow-up represents a contact without a follow-up.
     */
    @JsonCreator
    public JsonAdaptedPerson(@JsonProperty("name") String name, @JsonProperty("phone") String phone,
            @JsonProperty("email") String email, @JsonProperty("address") String address,
            @JsonProperty("role") String role, @JsonProperty("tags") List<JsonAdaptedTag> tags,
            @JsonProperty("followUp") JsonAdaptedFollowUp followUp) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.role = role;
        if (tags != null) {
            this.tags.addAll(tags);
        }
        this.followUp = followUp;
    }

    /**
     * Converts a given {@code Person} into this class for Jackson use.
     */
    public JsonAdaptedPerson(Person source) {
        name = source.getName().fullName;
        phone = source.getPhone().value;
        email = source.getEmail().value;
        address = source.getAddress().value;
        role = source.getRole().value;
        tags.addAll(source.getTags().stream()
                .map(JsonAdaptedTag::new)
                .collect(Collectors.toList()));
        followUp = source.getFollowUp()
                .map(JsonAdaptedFollowUp::new)
                .orElse(null);
    }

    /**
     * Converts this Jackson-friendly adapted person object into the model's {@code Person} object.
     *
     * @throws IllegalValueException if there were any data constraints violated in the adapted person.
     */
    public Person toModelType() throws IllegalValueException {
        if (name == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Name.class.getSimpleName()));
        }
        if (!Name.isValidName(name)) {
            throw new IllegalValueException(Name.MESSAGE_CONSTRAINTS);
        }
        final Name modelName = new Name(name);

        if (phone == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Phone.class.getSimpleName()));
        }
        if (!Phone.isValidPhone(phone)) {
            throw new IllegalValueException(Phone.MESSAGE_CONSTRAINTS);
        }
        final Phone modelPhone = new Phone(phone);

        if (email == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Email.class.getSimpleName()));
        }
        if (!Email.isValidEmail(email)) {
            throw new IllegalValueException(Email.MESSAGE_CONSTRAINTS);
        }
        final Email modelEmail = new Email(email);

        if (address == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Address.class.getSimpleName()));
        }
        if (!Address.isValidAddress(address)) {
            throw new IllegalValueException(Address.MESSAGE_CONSTRAINTS);
        }
        final Address modelAddress = new Address(address);

        if (role == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Role.class.getSimpleName()));
        }
        if (!Role.isValidRole(role)) {
            throw new IllegalValueException(Role.MESSAGE_CONSTRAINTS);
        }
        final Role modelRole = new Role(role);

        final Set<Tag> modelTags = toModelTags();

        final Optional<FollowUp> modelFollowUp = toModelFollowUp();

        return new Person(modelName, modelPhone, modelEmail, modelAddress, modelRole, modelTags, modelFollowUp);
    }

    /**
     * Converts stored tags into a set of validated model tags.
     *
     * @throws IllegalValueException if a tag is null or invalid.
     */
    private Set<Tag> toModelTags() throws IllegalValueException {
        final Set<Tag> modelTags = new HashSet<>();
        for (JsonAdaptedTag tag : tags) {
            if (tag == null) {
                throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Tag.class.getSimpleName()));
            }
            modelTags.add(tag.toModelType());
        }
        return modelTags;
    }

    /**
     * Converts the stored follow-up, treating a missing value as absent.
     *
     * @throws IllegalValueException if a present follow-up is invalid.
     */
    private Optional<FollowUp> toModelFollowUp() throws IllegalValueException {
        return followUp == null ? Optional.empty() : Optional.of(followUp.toModelType());
    }

}
