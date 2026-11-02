package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.GuiSettings;

public class UserPrefsTest {

    @Test
    public void constructor_default_hasDefaultGuiSettings() {
        UserPrefs userPrefs = new UserPrefs();
        assertNotNull(userPrefs.getGuiSettings());
        assertEquals(new GuiSettings(), userPrefs.getGuiSettings());
    }

    @Test
    public void constructor_withGuiSettings_success() {
        GuiSettings customSettings = new GuiSettings(1000, 500, 10, 20);
        UserPrefs userPrefs = new UserPrefs(customSettings);
        assertEquals(customSettings, userPrefs.getGuiSettings());
    }

    @Test
    public void constructor_withNullGuiSettings_setsDefaultGuiSettings() {
        UserPrefs userPrefs = new UserPrefs((GuiSettings) null);
        assertNotNull(userPrefs.getGuiSettings());
        assertEquals(new GuiSettings(), userPrefs.getGuiSettings());
    }

    @Test
    public void constructor_withReadOnlyUserPrefs_copiesValues() {
        UserPrefs original = new UserPrefs();
        original.setGuiSettings(new GuiSettings(800, 600, 0, 0));
        UserPrefs copy = new UserPrefs(original);
        assertEquals(original, copy);
    }

    @Test
    public void constructor_nullUserPrefs_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new UserPrefs((ReadOnlyUserPrefs) null));
    }

    @Test
    public void setGuiSettings_nullGuiSettings_throwsNullPointerException() {
        UserPrefs userPref = new UserPrefs();
        assertThrows(NullPointerException.class, () -> userPref.setGuiSettings(null));
    }

    @Test
    public void equals() {
        UserPrefs userPrefs = new UserPrefs();
        userPrefs.setGuiSettings(new GuiSettings(1000, 500, 0, 0));

        // same values -> returns true
        UserPrefs userPrefsCopy = new UserPrefs();
        userPrefsCopy.setGuiSettings(new GuiSettings(1000, 500, 0, 0));
        assertTrue(userPrefs.equals(userPrefsCopy));

        // same object -> returns true
        assertTrue(userPrefs.equals(userPrefs));

        // null -> returns false
        assertFalse(userPrefs.equals(null));

        // different types -> returns false
        assertFalse(userPrefs.equals(5));

        // different guiSettings -> returns false
        UserPrefs differentUserPrefs = new UserPrefs();
        differentUserPrefs.setGuiSettings(new GuiSettings(500, 500, 0, 0));
        assertFalse(userPrefs.equals(differentUserPrefs));
    }

    @Test
    public void hashCode_sameValues_sameHashCode() {
        UserPrefs userPrefs1 = new UserPrefs();
        userPrefs1.setGuiSettings(new GuiSettings(1200, 800, 10, 10));

        UserPrefs userPrefs2 = new UserPrefs();
        userPrefs2.setGuiSettings(new GuiSettings(1200, 800, 10, 10));

        assertEquals(userPrefs1.hashCode(), userPrefs2.hashCode());

        UserPrefs userPrefsDifferent = new UserPrefs();
        userPrefsDifferent.setGuiSettings(new GuiSettings(900, 700, 0, 0));
        assertNotEquals(userPrefs1.hashCode(), userPrefsDifferent.hashCode());
    }

    @Test
    public void toString_validGuiSettings_containsGuiSettingsString() {
        UserPrefs userPrefs = new UserPrefs();
        GuiSettings guiSettings = new GuiSettings(1280, 720, 15, 25);
        userPrefs.setGuiSettings(guiSettings);

        assertTrue(userPrefs.toString().contains("Gui Settings : " + guiSettings));
    }

}
