package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_FOLLOW_UP_DATE;
import static seedu.address.logic.commands.CommandTestUtil.VALID_FOLLOW_UP_DESCRIPTION;
import static seedu.address.storage.JsonAdaptedPerson.MISSING_FIELD_MESSAGE_FORMAT;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Role;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.PersonBuilder;

public class JsonAdaptedPersonTest {
    private static final String INVALID_NAME = "R@chel";
    private static final String INVALID_PHONE = "+651234";
    private static final String INVALID_ADDRESS = " ";
    private static final String INVALID_EMAIL = "example.com";
    private static final String INVALID_ROLE = " ";
    private static final String INVALID_TAG = "#friend";

    private static final String VALID_NAME = BENSON.getName().toString();
    private static final String VALID_PHONE = BENSON.getPhone().toString();
    private static final String VALID_EMAIL = BENSON.getEmail().toString();
    private static final String VALID_ADDRESS = BENSON.getAddress().toString();
    private static final String VALID_ROLE = BENSON.getRole().toString();
    private static final List<JsonAdaptedTag> VALID_TAGS = BENSON.getTags().stream()
            .map(JsonAdaptedTag::new)
            .collect(Collectors.toList());

    @Test
    public void toModelType_validPersonDetails_returnsPerson() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson(BENSON);
        assertEquals(BENSON, person.toModelType());
    }

    @Test
    public void toModelType_multipleSpaces_returnsNormalisedPerson() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson("Anne-Marie   O'Connor", VALID_PHONE, VALID_EMAIL,
                VALID_ADDRESS, "R&D   Partner", List.of(new JsonAdaptedTag("lubed/filmed   switches")));
        Person expectedPerson = new PersonBuilder(BENSON)
                .withName("Anne-Marie O'Connor")
                .withRole("R&D Partner")
                .withTags("lubed/filmed switches")
                .build();

        Person normalisedPerson = person.toModelType();
        assertEquals(expectedPerson, normalisedPerson);
        assertEquals(expectedPerson, new JsonAdaptedPerson(normalisedPerson).toModelType());
    }

    @Test
    public void toModelType_invalidName_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(INVALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS, VALID_ROLE, VALID_TAGS);
        String expectedMessage = Name.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullName_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(null, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                VALID_ROLE, VALID_TAGS);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Name.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidPhone_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, INVALID_PHONE, VALID_EMAIL, VALID_ADDRESS, VALID_ROLE, VALID_TAGS);
        String expectedMessage = Phone.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullPhone_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, null, VALID_EMAIL, VALID_ADDRESS,
                VALID_ROLE, VALID_TAGS);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Phone.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidEmail_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, INVALID_EMAIL, VALID_ADDRESS, VALID_ROLE, VALID_TAGS);
        String expectedMessage = Email.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullEmail_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, null, VALID_ADDRESS,
                VALID_ROLE, VALID_TAGS);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Email.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidAddress_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, INVALID_ADDRESS, VALID_ROLE, VALID_TAGS);
        String expectedMessage = Address.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_blankUnicodeAddress_throwsIllegalValueException() {
        String[] addresses = {
            "", " ", "   ", "\u200B", "\u00A0", "\u2060", "\u2003",
            "\u200B\u00A0\u2060\u2003", " \u200B \u00A0\t\u2060 \u2003 "
        };

        for (String address : addresses) {
            JsonAdaptedPerson person = new JsonAdaptedPerson(
                    VALID_NAME, VALID_PHONE, VALID_EMAIL, address, VALID_ROLE, VALID_TAGS);
            assertThrows(IllegalValueException.class, Address.MESSAGE_CONSTRAINTS, person::toModelType);
        }
    }

    @Test
    public void toModelType_nullAddress_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, null,
                VALID_ROLE, VALID_TAGS);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Address.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidRole_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS, INVALID_ROLE, VALID_TAGS);
        String expectedMessage = Role.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullRole_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                null, VALID_TAGS);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Role.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidTags_throwsIllegalValueException() {
        List<JsonAdaptedTag> invalidTags = new ArrayList<>(VALID_TAGS);
        invalidTags.add(new JsonAdaptedTag(INVALID_TAG));
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS, VALID_ROLE, invalidTags);
        assertThrows(IllegalValueException.class, person::toModelType);
    }

    @Test
    public void toModelType_nullTagElement_throwsIllegalValueException() {
        List<JsonAdaptedTag> invalidTags = new ArrayList<>(VALID_TAGS);
        invalidTags.add(null);
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS, VALID_ROLE, invalidTags);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Tag.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_personWithFollowUp_returnsPerson() throws Exception {
        Person original = new PersonBuilder(BENSON)
                .withFollowUp(VALID_FOLLOW_UP_DATE, VALID_FOLLOW_UP_DESCRIPTION)
                .build();

        assertEquals(original, new JsonAdaptedPerson(original).toModelType());
    }

    @Test
    public void toModelType_nullFollowUp_returnsPersonWithoutFollowUp() throws Exception {
        JsonAdaptedPerson adapter = new JsonAdaptedPerson(
                VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                VALID_ROLE, VALID_TAGS, null);

        assertEquals(BENSON, adapter.toModelType());
        assertTrue(adapter.toModelType().getFollowUp().isEmpty());
    }

    @Test
    public void toModelType_invalidFollowUp_throwsIllegalValueException() {
        JsonAdaptedFollowUp invalidFollowUp =
                new JsonAdaptedFollowUp("2026-02-30", VALID_FOLLOW_UP_DESCRIPTION);
        JsonAdaptedPerson adapter = new JsonAdaptedPerson(
                VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                VALID_ROLE, VALID_TAGS, invalidFollowUp);

        assertThrows(IllegalValueException.class,
                JsonAdaptedFollowUp.INVALID_DATE_MESSAGE, adapter::toModelType);
    }

    @Test
    public void jsonRoundTrip_personWithFollowUp_preservesValues() throws Exception {
        Person original = new PersonBuilder(BENSON)
                .withFollowUp(VALID_FOLLOW_UP_DATE, VALID_FOLLOW_UP_DESCRIPTION)
                .build();

        String json = JsonUtil.toJsonString(new JsonAdaptedPerson(original));
        JsonAdaptedPerson restored = JsonUtil.fromJsonString(json, JsonAdaptedPerson.class);

        assertEquals(original, restored.toModelType());
    }

    @Test
    public void fromJson_missingFollowUp_returnsPersonWithoutFollowUp() throws Exception {
        JsonAdaptedPerson adapter = JsonUtil.fromJsonString(legacyPersonJson(), JsonAdaptedPerson.class);

        assertTrue(adapter.toModelType().getFollowUp().isEmpty());
    }

    @Test
    public void fromJson_missingNestedFollowUpFields_throwsIllegalValueException() throws Exception {
        String[] followUps = {
            "{}",
            "{\"description\": \"Confirm quotation\"}",
            "{\"dueDate\": \"2026-11-01\"}"
        };

        for (String followUp : followUps) {
            String json = legacyPersonJson().replace(
                    "\"tags\": []", "\"tags\": [], \"followUp\": " + followUp);
            JsonAdaptedPerson adapter = JsonUtil.fromJsonString(json, JsonAdaptedPerson.class);

            assertThrows(IllegalValueException.class, adapter::toModelType);
        }
    }

    /**
     * Returns a valid older contact record without a follow-up property.
     */
    private static String legacyPersonJson() {
        return """
            {
              "name": "Test Contact",
              "phone": "91234567",
              "email": "test@example.com",
              "address": "Test address",
              "role": "Employee",
              "tags": []
            }
            """;
    }

}
