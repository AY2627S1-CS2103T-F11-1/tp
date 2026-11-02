package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.tag.Tag;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/**
 * Exercises slash escaping through parsing, commands, searching, history, and JSON persistence.
 */
public class SlashEscapingIntegrationTest {
    private static final Person ORIGINAL = new PersonBuilder().withName("Original Contact")
            .withPhone("91234567").withEmail("original@example.com").withAddress("Test Street")
            .withRole("Supplier").withTags("key/board").build();

    private static final Person ESCAPED_CONTACT = new PersonBuilder(ORIGINAL)
            .withName("david s/o john n/junior").withEmail("david@example.com")
            .withRole("supplier p/manufacturer").withTags("key/board", "parts xx/custom", "lubed/filmed").build();

    private static final Person OTHER_CONTACT = new PersonBuilder().withName("Other Contact")
            .withPhone("92345678").withEmail("other@example.com").withAddress("Other Street")
            .withRole("Customer").withTags().build();

    @TempDir
    public Path temporaryFolder;

    private Model model;
    private Logic logic;
    private StorageManager storage;

    @BeforeEach
    public void setUp() {
        model = new ModelManager();
        storage = new StorageManager(new JsonAddressBookStorage(temporaryFolder.resolve("addressbook.json")),
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json")));
        logic = new LogicManager(model, storage);
    }

    @Test
    public void add_escapedTextFields_persistDecodedValuesAfterReload() throws Exception {
        addEscapedContact();
        assertStoredContacts(ESCAPED_CONTACT);

        reload();
        assertStoredContacts(ESCAPED_CONTACT);
    }

    @Test
    public void find_escapedSearchValues_matchDecodedFieldsAfterReload() throws Exception {
        addEscapedContact();
        addOtherContact();
        reload();

        // EP (search mode): prefixed name/role/tag phrases and legacy name keywords.
        assertSearchResults(ESCAPED_CONTACT,
                "find n/DAVID S\\/O JOHN N\\/JUNIOR", "find s\\/o", "find n\\/junior",
                "find r/SUPPLIER P\\/MANUFACTURER", "find t/ key\\/board", "find t/parts xx\\/custom",
                "find t/lubed/filmed");

        // Search behaviour: OR within a field, AND across fields, and case-insensitive matching.
        assertSearchResults(ESCAPED_CONTACT,
                "find n/david s\\/o john n\\/junior r/supplier p\\/manufacturer t/key\\/board",
                "find n/missing n/david s\\/o john n\\/junior r/missing r/supplier p\\/manufacturer"
                        + " t/missing t/key\\/board");

        logic.execute("find n/david s\\/o john r/Customer");
        assertEquals(List.of(), logic.getFilteredPersonList(),
                "Different fields must all match the same contact");
    }

    @Test
    public void edit_escapedFieldsAndTags_supportFilteredEditsUndoRedoAndReload() throws Exception {
        // Workflow: edit a filtered contact, replace its tags, then undo/redo and reload.
        addOriginalContact();
        addOtherContact();
        logic.execute("find Original");
        logic.execute("edit 1 n/David s\\/o John r/Supplier n\\/Manager t/parts xx\\/custom");
        Person edited = new PersonBuilder(ORIGINAL).withName("David s/o John").withRole("Supplier n/Manager")
                .withTags("key/board", "parts xx/custom").build();
        assertStoredContacts(edited, OTHER_CONTACT);

        logic.execute("find n/David s\\/o John r/Supplier n\\/Manager t/parts xx\\/custom");
        assertEquals(List.of(edited), logic.getFilteredPersonList());
        logic.execute("edit 1 dt/KEY\\/BOARD dt/PARTS XX\\/CUSTOM t/new r\\/tag");
        Person retagged = new PersonBuilder(edited).withTags("new r/tag").build();
        assertStoredContacts(retagged, OTHER_CONTACT);
        logic.execute("undo");
        assertStoredContacts(edited, OTHER_CONTACT);
        logic.execute("redo");
        assertStoredContacts(retagged, OTHER_CONTACT);
        reload();
        assertStoredContacts(retagged, OTHER_CONTACT);
        logic.execute("find t/new r\\/tag");
        assertEquals(List.of(retagged), logic.getFilteredPersonList());
    }

    @Test
    public void addAndEdit_equivalentTagSpellings_doNotCreateDuplicateTags() throws Exception {
        // EP (tag spelling): plain slash, escaped slash, and different letter casing.
        logic.execute("add n/Original Contact p/91234567 e/original@example.com a/Test Street"
                + " r/Supplier t/key/board t/KEY\\/BOARD");
        assertStoredContacts(ORIGINAL);

        logic.execute("edit 1 t/KEY\\/BOARD");
        assertStoredContacts(ORIGINAL);
    }

    @Test
    public void edit_sameDecodedTagAddedAndDeleted_rejectsEntireEdit() throws Exception {
        addOriginalContact();

        assertThrows(ParseException.class,
                String.format(EditCommand.MESSAGE_TAG_CONFLICT, "Key/Board"), () ->
                        logic.execute("edit 1 n/Changed t/Key\\/Board dt/key/board"));
        assertStoredContacts(ORIGINAL);
    }

    @Test
    public void escaping_preservesUnknownDuplicateAndMissingPrefixErrors() throws Exception {
        // EP (syntax validity): unknown prefix, duplicate prefix, missing prefix, and unescaped prefix-like text.
        addOriginalContact();
        assertThrows(ParseException.class, "Unrecognized prefix(es) provided: xx/.", () ->
                logic.execute("edit 1 n/David s\\/o John xx/value"));
        assertStoredContacts(ORIGINAL);
        assertThrows(ParseException.class,
                "Multiple values specified for the following single-valued field(s): n/.", () ->
                        logic.execute("edit 1 n/David s\\/o John n/Second"));
        assertStoredContacts(ORIGINAL);
        assertThrows(ParseException.class, "Missing mandatory parameter(s): p/.", () ->
                logic.execute("add n/David p\\/Manufacturer e/other@example.com a/Test Street r/Supplier"));
        assertStoredContacts(ORIGINAL);
        assertThrows(ParseException.class, "Unrecognized prefix(es) provided: s/.", () ->
                logic.execute("edit 1 n/David s/o John"));
        assertStoredContacts(ORIGINAL);
    }

    @Test
    public void find_escapedPrefixesRemainLiteral_andRealPrefixesKeepTheirMeaning() throws Exception {
        logic.execute("add n/Original p\\/Supplier p/91234567 e/original@example.com a/Test Street r/Supplier");
        Person expected = new PersonBuilder(ORIGINAL).withName("Original p/Supplier").withTags().build();

        // EP (prefix interpretation): escaped literal text versus real command syntax below.
        assertSearchResults(expected, "find p\\/Supplier", "find n/Original p\\/Supplier");
        assertThrows(ParseException.class, "Unrecognized prefix(es) provided: p/.", () ->
                logic.execute("find p/Supplier"));
        assertThrows(ParseException.class, "find cannot mix unprefixed keywords with prefixed search values.", () ->
                logic.execute("find p\\/Supplier n/Original"));
        assertStoredContacts(expected);
    }

    @Test
    public void edit_decodedTagOverLimit_rejectsWithoutMutation() throws Exception {
        // BVA: decoded tag lengths of 50 and 51; escape backslashes do not count.
        String escapedTag = "A".repeat(48) + "\\/Z";
        String decodedTag = "A".repeat(48) + "/Z";
        logic.execute("add n/Original Contact p/91234567 e/original@example.com a/Test Street"
                + " r/Supplier t/" + escapedTag);
        Person expected = new PersonBuilder(ORIGINAL).withTags(decodedTag).build();
        assertStoredContacts(expected);
        logic.execute("find t/" + escapedTag);
        assertEquals(List.of(expected), logic.getFilteredPersonList());

        assertThrows(ParseException.class, Tag.MESSAGE_CONSTRAINTS, () ->
                logic.execute("edit 1 t/" + "A".repeat(49) + "\\/Z"));
        assertStoredContacts(expected);
    }

    @Test
    public void addressAndDescription_decodeSlashEscapesAndPreserveOtherBackslashes() throws Exception {
        // EP (backslash sequence): slash escapes are decoded; unrelated backslashes stay literal.
        String address = "C:\\Users\\John\\/Archive";
        String description = "Check s\\/o and C:\\Users\\John";
        logic.execute("add n/Original Contact p/91234567 e/original@example.com a/" + address + " r/Supplier");
        logic.execute("followup 1 by/2026-11-01 abt/" + description);
        Person expected = new PersonBuilder(ORIGINAL).withAddress("C:\\Users\\John/Archive").withTags()
                .withFollowUp("2026-11-01", "Check s/o and C:\\Users\\John").build();
        assertStoredContacts(expected);
        reload();
        assertStoredContacts(expected);
    }

    @Test
    public void editAndFollowup_literalBackslashSlash_survivesUndoRedoAndReload() throws Exception {
        // Workflow: decoded free text and a literal backslash-slash must survive history and reload.
        addOriginalContact();
        logic.execute("edit 1 a/123  Street c\\/o John n\\/Office");
        logic.execute("followup 1 by/2026-11-15 abt/Confirm  PCB\\/Case by\\/email and \\\\/");
        Person expected = new PersonBuilder(ORIGINAL).withAddress("123  Street c/o John n/Office")
                .withFollowUp("2026-11-15", "Confirm  PCB/Case by/email and \\/").build();
        assertStoredContacts(expected);
        logic.execute("undo");
        assertStoredContacts(new PersonBuilder(expected).withoutFollowUp().build());
        logic.execute("redo");
        reload();
        assertStoredContacts(expected);
    }

    @Test
    public void followup_decodedDescriptionLength_enforcesExistingLimit() throws Exception {
        // BVA: decoded description lengths of 199, 200 and 201 against the existing 200-unit limit.
        addOriginalContact();
        for (int length : List.of(199, 200)) {
            String description = "a".repeat(length - 2) + "\\/b";
            logic.execute("followup 1 by/2026-11-15 abt/" + description);
            Person expected = new PersonBuilder(ORIGINAL)
                    .withFollowUp("2026-11-15", "a".repeat(length - 2) + "/b").build();
            assertStoredContacts(expected);
        }
        Person expected = model.getAddressBook().getPersonList().getFirst();
        assertThrows(ParseException.class, () ->
                logic.execute("followup 1 by/2026-11-15 abt/" + "a".repeat(199) + "\\/b"));
        assertStoredContacts(expected);
    }

    private void addOriginalContact() throws Exception {
        logic.execute("add n/Original Contact p/91234567 e/original@example.com a/Test Street"
                + " r/Supplier t/key/board");
    }

    private void addEscapedContact() throws Exception {
        logic.execute("add n/ david s\\/o john n\\/junior p/91234567 e/david@example.com a/Test Street"
                + " r/ supplier p\\/manufacturer t/ key\\/board t/parts xx\\/custom t/lubed/filmed");
    }

    private void addOtherContact() throws Exception {
        logic.execute("add n/Other Contact p/92345678 e/other@example.com a/Other Street r/Customer");
    }

    private void assertSearchResults(Person expected, String... commands) throws Exception {
        for (String command : commands) {
            logic.execute(command);
            assertEquals(List.of(expected), logic.getFilteredPersonList(), command);
        }
    }

    private void assertStoredContacts(Person... expected) throws Exception {
        assertEquals(List.of(expected), model.getAddressBook().getPersonList());
        assertEquals(List.of(expected), storage.readAddressBook().orElseThrow().getPersonList());
    }

    private void reload() throws Exception {
        model = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        logic = new LogicManager(model, storage);
    }
}
