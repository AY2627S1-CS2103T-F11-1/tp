package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DELETE_TAG;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ROLE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.CollectionUtil;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.FollowUp;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Role;
import seedu.address.model.tag.Tag;

/**
 * Edits the details of an existing person in the address book.
 */
public class EditCommand extends Command {

    public static final String COMMAND_WORD = "edit";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Edits the details of the person identified "
            + "by the index number used in the displayed person list. "
            + "Supplied name, phone, email, address, and role values replace existing values. "
            + "Use t/ to add tags and dt/ to delete tags; tag names must be explicitly provided.\n"
            + "Parameters: INDEX (must be a positive integer) "
            + "[" + PREFIX_NAME + "NAME] "
            + "[" + PREFIX_PHONE + "PHONE] "
            + "[" + PREFIX_EMAIL + "EMAIL] "
            + "[" + PREFIX_ADDRESS + "ADDRESS] "
            + "[" + PREFIX_ROLE + "ROLE] "
            + "[" + PREFIX_TAG + "TAG_TO_ADD]... "
            + "[" + PREFIX_DELETE_TAG + "TAG_TO_DELETE]...\n"
            + "Example: " + COMMAND_WORD + " 1 "
            + PREFIX_ROLE + "Supplier";

    public static final String MESSAGE_EDIT_PERSON_SUCCESS = "Edited person: %1$s";
    public static final String MESSAGE_NOT_EDITED = "At least one field to edit must be provided.";
    public static final String MESSAGE_EMPTY_TAG =
            "Tag names cannot be empty. Specify a tag after t/ or dt/.";
    public static final String MESSAGE_TAG_CONFLICT =
            "Cannot add and delete the same tag: %1$s. Remove it from either t/ or dt/.";
    public static final String MESSAGE_TAG_NOT_FOUND =
            "Cannot delete tag: %1$s. The selected contact does not have this tag.";

    private static final Logger logger = LogsCenter.getLogger(EditCommand.class);

    private final Index index;
    private final EditPersonDescriptor editPersonDescriptor;

    /**
     * @param index of the person in the filtered person list to edit
     * @param editPersonDescriptor details to edit the person with
     */
    public EditCommand(Index index, EditPersonDescriptor editPersonDescriptor) {
        requireNonNull(index);
        requireNonNull(editPersonDescriptor);

        this.index = index;
        this.editPersonDescriptor = new EditPersonDescriptor(editPersonDescriptor);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        logger.fine("Executing EditCommand for displayed contact index: " + index.getOneBased());
        List<Person> lastShownList = model.getFilteredPersonList();

        if (index.getZeroBased() >= lastShownList.size()) {
            logger.warning("EditCommand rejected: Displayed contact index is out of bounds");
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Person personToEdit = lastShownList.get(index.getZeroBased());
        Person editedPerson = createEditedPerson(personToEdit, editPersonDescriptor);

        boolean hasPhone = model.hasPhone(editedPerson.getPhone(), personToEdit);
        boolean hasEmail = model.hasEmail(editedPerson.getEmail(), personToEdit);

        if (hasPhone && hasEmail) {
            logger.warning("EditCommand rejected: Duplicate phone and email exist in address book");
            throw new CommandException(Messages.MESSAGE_DUPLICATE_PHONE_AND_EMAIL);
        } else if (hasPhone) {
            logger.warning("EditCommand rejected: Duplicate phone exists in address book");
            throw new CommandException(Messages.MESSAGE_DUPLICATE_PHONE);
        } else if (hasEmail) {
            logger.warning("EditCommand rejected: Duplicate email exists in address book");
            throw new CommandException(Messages.MESSAGE_DUPLICATE_EMAIL);
        }

        model.setPerson(personToEdit, editedPerson);
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        model.commitAddressBook();

        logger.finer(() -> "Person edited in model: displayed contact index=" + index.getOneBased()
                + ", name supplied=" + editPersonDescriptor.getName().isPresent()
                + ", phone supplied=" + editPersonDescriptor.getPhone().isPresent()
                + ", email supplied=" + editPersonDescriptor.getEmail().isPresent()
                + ", address supplied=" + editPersonDescriptor.getAddress().isPresent()
                + ", role supplied=" + editPersonDescriptor.getRole().isPresent()
                + ", requested tag additions="
                + editPersonDescriptor.getTagsToAdd().orElse(Collections.emptySet()).size()
                + ", requested tag deletions="
                + editPersonDescriptor.getTagsToDelete().orElse(Collections.emptySet()).size());

        return new CommandResult(String.format(MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson)));
    }

    /**
     * Creates and returns a {@code Person} with the details of {@code personToEdit}
     * edited with {@code editPersonDescriptor}.
     * @throws CommandException if any tag to delete is not present on the original person
     */
    private static Person createEditedPerson(Person personToEdit, EditPersonDescriptor editPersonDescriptor)
            throws CommandException {
        assert personToEdit != null : "The selected person must exist before applying edits";

        Set<Tag> tagsToAdd = editPersonDescriptor.getTagsToAdd().orElse(Collections.emptySet());
        Set<Tag> tagsToDelete = editPersonDescriptor.getTagsToDelete().orElse(Collections.emptySet());
        assert Collections.disjoint(tagsToAdd, tagsToDelete)
                : "Validated tag additions and deletions must not overlap";

        Optional<Tag> missingTag = tagsToDelete.stream()
                .filter(tag -> !personToEdit.getTags().contains(tag))
                .sorted((first, second) -> first.tagName.compareToIgnoreCase(second.tagName))
                .findFirst();
        if (missingTag.isPresent()) {
            logger.warning("EditCommand rejected: A requested tag deletion is absent from the selected contact");
            throw new CommandException(String.format(MESSAGE_TAG_NOT_FOUND, missingTag.get().tagName));
        }

        Name updatedName = editPersonDescriptor.getName().orElse(personToEdit.getName());
        Phone updatedPhone = editPersonDescriptor.getPhone().orElse(personToEdit.getPhone());
        Email updatedEmail = editPersonDescriptor.getEmail().orElse(personToEdit.getEmail());
        Address updatedAddress = editPersonDescriptor.getAddress().orElse(personToEdit.getAddress());
        Role updatedRole = editPersonDescriptor.getRole().orElse(personToEdit.getRole());
        Set<Tag> updatedTags = new HashSet<>(personToEdit.getTags());
        updatedTags.removeAll(tagsToDelete);
        updatedTags.addAll(tagsToAdd);
        Optional<FollowUp> currentFollowUp = personToEdit.getFollowUp();

        assert updatedTags.containsAll(tagsToAdd) : "The edited tags must contain every requested addition";
        assert Collections.disjoint(updatedTags, tagsToDelete)
                : "The edited tags must not contain any requested deletion";

        return new Person(updatedName, updatedPhone, updatedEmail,
                updatedAddress, updatedRole, updatedTags, currentFollowUp);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof EditCommand otherEditCommand)) {
            return false;
        }

        return index.equals(otherEditCommand.index)
                && editPersonDescriptor.equals(otherEditCommand.editPersonDescriptor);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("index", index)
                .add("editPersonDescriptor", editPersonDescriptor)
                .toString();
    }

    /**
     * Stores replacement values for contact details and separate sets of tags to add and delete.
     */
    public static class EditPersonDescriptor {
        private Name name;
        private Phone phone;
        private Email email;
        private Address address;
        private Role role;
        private Set<Tag> tagsToAdd;
        private Set<Tag> tagsToDelete;

        public EditPersonDescriptor() {}

        /**
         * Copy constructor.
         * Defensive copies of both tag sets are used internally.
         */
        public EditPersonDescriptor(EditPersonDescriptor toCopy) {
            setName(toCopy.name);
            setPhone(toCopy.phone);
            setEmail(toCopy.email);
            setAddress(toCopy.address);
            setRole(toCopy.role);
            setTagsToAdd(toCopy.tagsToAdd);
            setTagsToDelete(toCopy.tagsToDelete);
        }

        /**
         * Returns true if at least one field is edited.
         */
        public boolean isAnyFieldEdited() {
            return CollectionUtil.isAnyNonNull(name, phone, email, address, role, tagsToAdd, tagsToDelete);
        }

        public void setName(Name name) {
            this.name = name;
        }

        public Optional<Name> getName() {
            return Optional.ofNullable(name);
        }

        public void setPhone(Phone phone) {
            this.phone = phone;
        }

        public Optional<Phone> getPhone() {
            return Optional.ofNullable(phone);
        }

        public void setEmail(Email email) {
            this.email = email;
        }

        public Optional<Email> getEmail() {
            return Optional.ofNullable(email);
        }

        public void setAddress(Address address) {
            this.address = address;
        }

        public Optional<Address> getAddress() {
            return Optional.ofNullable(address);
        }

        public void setRole(Role role) {
            this.role = role;
        }

        public Optional<Role> getRole() {
            return Optional.ofNullable(role);
        }

        /**
         * Sets the tags to add, using a defensive copy. Null means no additions were supplied.
         */
        public void setTagsToAdd(Set<Tag> tagsToAdd) {
            this.tagsToAdd = (tagsToAdd != null) ? new HashSet<>(tagsToAdd) : null;
        }

        /**
         * Returns an unmodifiable set of tags to add, or an empty Optional if none were supplied.
         */
        public Optional<Set<Tag>> getTagsToAdd() {
            return (tagsToAdd != null) ? Optional.of(Collections.unmodifiableSet(tagsToAdd)) : Optional.empty();
        }

        /**
         * Sets the tags to delete, using a defensive copy. Null means no deletions were supplied.
         */
        public void setTagsToDelete(Set<Tag> tagsToDelete) {
            this.tagsToDelete = (tagsToDelete != null) ? new HashSet<>(tagsToDelete) : null;
        }

        /**
         * Returns an unmodifiable set of tags to delete, or an empty Optional if none were supplied.
         */
        public Optional<Set<Tag>> getTagsToDelete() {
            return (tagsToDelete != null) ? Optional.of(Collections.unmodifiableSet(tagsToDelete)) : Optional.empty();
        }

        @Override
        public boolean equals(Object other) {
            if (other == this) {
                return true;
            }

            // instanceof handles nulls
            if (!(other instanceof EditPersonDescriptor otherEditPersonDescriptor)) {
                return false;
            }

            return Objects.equals(name, otherEditPersonDescriptor.name)
                    && Objects.equals(phone, otherEditPersonDescriptor.phone)
                    && Objects.equals(email, otherEditPersonDescriptor.email)
                    && Objects.equals(address, otherEditPersonDescriptor.address)
                    && Objects.equals(role, otherEditPersonDescriptor.role)
                    && Objects.equals(tagsToAdd, otherEditPersonDescriptor.tagsToAdd)
                    && Objects.equals(tagsToDelete, otherEditPersonDescriptor.tagsToDelete);
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .add("name", name)
                    .add("phone", phone)
                    .add("email", email)
                    .add("address", address)
                    .add("role", role)
                    .add("tagsToAdd", tagsToAdd)
                    .add("tagsToDelete", tagsToDelete)
                    .toString();
        }
    }
}
