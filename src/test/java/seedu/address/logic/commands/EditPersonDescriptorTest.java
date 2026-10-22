package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ADDRESS_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ROLE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.EditPersonDescriptorBuilder;

public class EditPersonDescriptorTest {

    @Test
    public void equals() {
        // same values -> returns true
        EditPersonDescriptor descriptorWithSameValues = new EditPersonDescriptor(DESC_AMY);
        assertTrue(DESC_AMY.equals(descriptorWithSameValues));

        // same object -> returns true
        assertTrue(DESC_AMY.equals(DESC_AMY));

        // null -> returns false
        assertFalse(DESC_AMY.equals(null));

        // different types -> returns false
        assertFalse(DESC_AMY.equals(5));

        // different values -> returns false
        assertFalse(DESC_AMY.equals(DESC_BOB));

        // different name -> returns false
        EditPersonDescriptor editedAmy = new EditPersonDescriptorBuilder(DESC_AMY).withName(VALID_NAME_BOB).build();
        assertFalse(DESC_AMY.equals(editedAmy));

        // different phone -> returns false
        editedAmy = new EditPersonDescriptorBuilder(DESC_AMY).withPhone(VALID_PHONE_BOB).build();
        assertFalse(DESC_AMY.equals(editedAmy));

        // different email -> returns false
        editedAmy = new EditPersonDescriptorBuilder(DESC_AMY).withEmail(VALID_EMAIL_BOB).build();
        assertFalse(DESC_AMY.equals(editedAmy));

        // different address -> returns false
        editedAmy = new EditPersonDescriptorBuilder(DESC_AMY).withAddress(VALID_ADDRESS_BOB).build();
        assertFalse(DESC_AMY.equals(editedAmy));

        // different role -> returns false
        editedAmy = new EditPersonDescriptorBuilder(DESC_AMY).withRole(VALID_ROLE_BOB).build();
        assertFalse(DESC_AMY.equals(editedAmy));

        // different tags to add -> returns false
        editedAmy = new EditPersonDescriptorBuilder(DESC_AMY).withTagsToAdd(VALID_TAG_HUSBAND).build();
        assertFalse(DESC_AMY.equals(editedAmy));

        // different tags to delete -> returns false
        editedAmy = new EditPersonDescriptorBuilder(DESC_AMY).withTagsToDelete(VALID_TAG_HUSBAND).build();
        assertFalse(DESC_AMY.equals(editedAmy));
    }

    @Test
    public void isAnyFieldEdited_tagOperations_detectedIndependently() {
        EditPersonDescriptor descriptor = new EditPersonDescriptor();
        assertFalse(descriptor.isAnyFieldEdited());
        assertTrue(descriptor.getTagsToAdd().isEmpty());
        assertTrue(descriptor.getTagsToDelete().isEmpty());

        descriptor.setTagsToAdd(Set.of(new Tag("solderer")));
        assertTrue(descriptor.isAnyFieldEdited());
        assertTrue(descriptor.getTagsToDelete().isEmpty());

        descriptor.setTagsToAdd(null);
        assertFalse(descriptor.isAnyFieldEdited());

        descriptor.setTagsToDelete(Set.of(new Tag("trainee")));
        assertTrue(descriptor.isAnyFieldEdited());
        assertTrue(descriptor.getTagsToAdd().isEmpty());

        descriptor.setTagsToDelete(null);
        assertFalse(descriptor.isAnyFieldEdited());
    }

    @Test
    public void tagSets_defensiveCopiesAndUnmodifiableGetters() {
        Tag tagToAdd = new Tag("solderer");
        Tag tagToDelete = new Tag("trainee");
        Set<Tag> tagsToAdd = new HashSet<>(Set.of(tagToAdd));
        Set<Tag> tagsToDelete = new HashSet<>(Set.of(tagToDelete));
        EditPersonDescriptor descriptor = new EditPersonDescriptor();
        descriptor.setTagsToAdd(tagsToAdd);
        descriptor.setTagsToDelete(tagsToDelete);

        tagsToAdd.clear();
        tagsToDelete.clear();
        assertEquals(Set.of(tagToAdd), descriptor.getTagsToAdd().orElseThrow());
        assertEquals(Set.of(tagToDelete), descriptor.getTagsToDelete().orElseThrow());
        assertThrows(UnsupportedOperationException.class, () -> descriptor.getTagsToAdd().orElseThrow().clear());
        assertThrows(UnsupportedOperationException.class, () -> descriptor.getTagsToDelete().orElseThrow().clear());
    }

    @Test
    public void copyConstructor_copiesBothTagOperations() {
        EditPersonDescriptor original = new EditPersonDescriptorBuilder()
                .withTagsToAdd("solderer").withTagsToDelete("trainee").build();
        EditPersonDescriptor copy = new EditPersonDescriptor(original);
        assertEquals(original, copy);

        original.setTagsToAdd(null);
        original.setTagsToDelete(null);
        assertEquals(Set.of(new Tag("solderer")), copy.getTagsToAdd().orElseThrow());
        assertEquals(Set.of(new Tag("trainee")), copy.getTagsToDelete().orElseThrow());
    }

    @Test
    public void toStringMethod() {
        EditPersonDescriptor editPersonDescriptor = new EditPersonDescriptor();
        String expected = EditPersonDescriptor.class.getCanonicalName() + "{name="
                + editPersonDescriptor.getName().orElse(null) + ", phone="
                + editPersonDescriptor.getPhone().orElse(null) + ", email="
                + editPersonDescriptor.getEmail().orElse(null) + ", address="
                + editPersonDescriptor.getAddress().orElse(null) + ", role="
                + editPersonDescriptor.getRole().orElse(null) + ", tagsToAdd="
                + editPersonDescriptor.getTagsToAdd().orElse(null) + ", tagsToDelete="
                + editPersonDescriptor.getTagsToDelete().orElse(null) + "}";
        assertEquals(expected, editPersonDescriptor.toString());
    }
}
