package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Email;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/**
 * Exercises email validation through add/edit, duplicate detection and JSON persistence.
 */
public class EmailValidationIntegrationTest {
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
    public void addAndEdit_supportedEmails_preserveValuesAfterReload() throws Exception {
        // Workflow: accepted punctuation and casing survive parsing, editing, saving and loading.
        logic.execute("add n/John A. Tan p/91234567 e/  O'Connor@example.com  a/Test Street r/Supplier (PCB)");
        assertStoredEmails("O'Connor@example.com");
        reload();
        assertStoredEmails("O'Connor@example.com");

        logic.execute("edit 1 e/  Sales@Keyboard--Shop.COM  ");
        assertStoredEmails("Sales@Keyboard--Shop.COM");
        reload();
        assertStoredEmails("Sales@Keyboard--Shop.COM");

        // EP: slash is supported literally in an email; email does not use slash decoding.
        logic.execute("edit 1 e/sales/parts@example.com");
        reload();
        assertStoredEmails("sales/parts@example.com");
    }

    @Test
    public void addAndEdit_equivalentEmails_rejectDuplicatesWithoutMutation() throws Exception {
        logic.execute("add n/John p/91234567 e/O'Connor@Keyboard--Shop.COM a/Test Street r/Supplier");
        reload();
        // EP: a case-insensitive email match alone prevents duplicates, including after reload.
        assertThrows(CommandException.class, Messages.MESSAGE_DUPLICATE_EMAIL, () ->
                logic.execute("add n/Jane p/92345678 e/o'connor@keyboard--shop.com a/Other Street r/Customer"));
        assertStoredEmails("O'Connor@Keyboard--Shop.COM");

        logic.execute("add n/Jane p/92345678 e/other@example.com a/Other Street r/Customer");
        List<Person> beforeEdit = List.copyOf(model.getAddressBook().getPersonList());
        assertThrows(CommandException.class, Messages.MESSAGE_DUPLICATE_EMAIL, () ->
                logic.execute("edit 2 e/o'connor@keyboard--shop.com"));
        assertEquals(beforeEdit, model.getAddressBook().getPersonList());
        assertEquals(beforeEdit, storage.readAddressBook().orElseThrow().getPersonList());
    }

    @Test
    public void addAndEdit_unsupportedEmails_returnConstraintsWithoutMutation() throws Exception {
        // EP: invalid email syntax must fail gracefully through both command paths.
        for (String email : List.of("\"John Tan\"@example.com", "a@example..com", "a@" + "b".repeat(64))) {
            assertThrows(ParseException.class, Email.MESSAGE_CONSTRAINTS, () ->
                    logic.execute("add n/John p/91234567 e/" + email + " a/Test Street r/Supplier"));
            assertEquals(List.of(), model.getAddressBook().getPersonList());
        }
        logic.execute("add n/John p/91234567 e/o'connor@example.com a/Test Street r/Supplier");
        for (String email : List.of("\"John Tan\"@example.com", "a@example..com", "a@" + "b".repeat(64))) {
            assertThrows(ParseException.class, Email.MESSAGE_CONSTRAINTS, () -> logic.execute("edit 1 e/" + email));
            assertStoredEmails("o'connor@example.com");
        }
    }

    private void assertStoredEmails(String... expected) throws Exception {
        List<String> emails = model.getAddressBook().getPersonList().stream().map(person -> person.getEmail().value)
                .toList();
        List<String> savedEmails = storage.readAddressBook().orElseThrow().getPersonList().stream()
                .map(person -> person.getEmail().value).toList();
        assertEquals(List.of(expected), emails);
        assertEquals(List.of(expected), savedEmails);
    }

    private void reload() throws Exception {
        model = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        logic = new LogicManager(model, storage);
    }
}
