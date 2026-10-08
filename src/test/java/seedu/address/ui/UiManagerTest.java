package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.model.ModelManager;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

public class UiManagerTest {

    @Test
    public void constructor_validParameters_success() {
        Logic logic = new LogicManager(new ModelManager(), new StorageManager(
                new JsonAddressBookStorage(Path.of("dummyAddressBook.json")),
                new JsonUserPrefsStorage(Path.of("dummyUserPrefs.json"))));

        UiManager uiManager = new UiManager(logic, Path.of("dummyAddressBook.json"));
        assertNotNull(uiManager);

        UiManager uiManagerWithStatus = new UiManager(logic, Path.of("dummyAddressBook.json"), "Warning message");
        assertNotNull(uiManagerWithStatus);
    }
}
