package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.stage.Stage;
import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.HelpCommand;

public class HelpWindowTest {

    @BeforeAll
    public static void initializeJavaFx() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        Runnable initialize = () -> {
            Platform.setImplicitExit(false);
            started.countDown();
        };

        try {
            Platform.startup(initialize);
        } catch (IllegalStateException e) {
            Platform.runLater(initialize);
        }

        assertTrue(started.await(10, TimeUnit.SECONDS), "JavaFX did not initialize");
    }

    @Test
    public void helpMessage_groupsCommandsAndUsesConsistentTextPattern() {
        assertTrue(HelpWindow.HELP_MESSAGE.contains("PEOPLE\n"));
        assertTrue(HelpWindow.HELP_MESSAGE.contains("ADDRESS BOOK\n"));
        assertTrue(HelpWindow.HELP_MESSAGE.contains("APPLICATION\n"));
        assertTrue(HelpWindow.HELP_MESSAGE.contains("ADD\nAdds a person to the address book."));
        assertTrue(HelpWindow.HELP_MESSAGE.contains("Parameters: n/NAME"));
        assertTrue(HelpWindow.HELP_MESSAGE.contains("Example: add n/John Doe"));
        assertTrue(HelpWindow.HELP_MESSAGE.contains("\nEDIT\n"));
        assertTrue(HelpWindow.HELP_MESSAGE.contains("\nDELETE\n"));
        assertTrue(HelpWindow.HELP_MESSAGE.contains("\nFIND\n"));
        assertTrue(HelpWindow.HELP_MESSAGE.contains("\nLIST\nShows all persons"));
        assertTrue(HelpWindow.HELP_MESSAGE.contains("\nCLEAR\nClears all entries"));
        assertTrue(HelpWindow.HELP_MESSAGE.contains("\nHELP\n" + HelpCommand.COMMAND_DESCRIPTION));
        assertTrue(HelpWindow.HELP_MESSAGE.contains("Parameters: " + HelpCommand.COMMAND_PARAMETERS));
        assertTrue(HelpWindow.HELP_MESSAGE.contains("Example: " + HelpCommand.COMMAND_EXAMPLE));
        assertTrue(HelpWindow.HELP_MESSAGE.contains("\nEXIT\nExits the program."));
        assertTrue(HelpWindow.HELP_MESSAGE.contains("----------------------------------------"));
    }

    @Test
    public void helpMessage_containsUsageForEveryCommand() {
        assertUsageIsDisplayed(AddCommand.COMMAND_USAGE);
        assertUsageIsDisplayed(EditCommand.COMMAND_USAGE);
        assertUsageIsDisplayed(DeleteCommand.COMMAND_USAGE);
        assertUsageIsDisplayed(FindCommand.COMMAND_USAGE);
        assertUsageIsDisplayed("list: Shows all persons in the address book.");
        assertUsageIsDisplayed("clear: Clears all entries from KeyBossWarriorsPro.");
        assertTrue(HelpWindow.HELP_MESSAGE.contains(HelpCommand.COMMAND_DESCRIPTION));
        assertUsageIsDisplayed("exit: Exits the program.");
    }

    private static void assertUsageIsDisplayed(String messageUsage) {
        int separator = messageUsage.indexOf(": ");
        assertTrue(separator >= 0, "Usage text should separate the command name and description");

        String expected = messageUsage.substring(0, separator).toUpperCase(Locale.ROOT)
                + "\n" + messageUsage.substring(separator + 2);
        assertTrue(HelpWindow.HELP_MESSAGE.contains(expected),
                "Help output should include the usage text: " + messageUsage);
    }

    @Test
    public void openUserGuide_whenDesktopSupported_opensTpUserGuideUrl() {
        AtomicReference<URI> openedUri = new AtomicReference<>();

        HelpWindow.openUserGuide(true, openedUri::set, message -> {
            throw new AssertionError("Unexpected warning: " + message);
        });

        assertEquals(URI.create(HelpWindow.USERGUIDE_URL), openedUri.get());
    }

    @Test
    public void userGuideLink_whenClicked_opensTpUserGuideUrl() throws Exception {
        AtomicReference<URI> openedUri = new AtomicReference<>();
        FutureTask<Void> testOnFxThread = new FutureTask<>(() -> {
            Stage stage = new Stage();
            try {
                HelpWindow helpWindow = new HelpWindow(stage, () -> true, openedUri::set, url -> { });
                helpWindow.getUserGuideLink().fire();
            } finally {
                stage.close();
            }
            return null;
        });

        Platform.runLater(testOnFxThread);
        testOnFxThread.get(10, TimeUnit.SECONDS);

        assertEquals(URI.create(HelpWindow.USERGUIDE_URL), openedUri.get());
    }

    @Test
    public void copyUrlButton_whenClicked_copiesTpUserGuideUrl() throws Exception {
        FutureTask<Void> testOnFxThread = new FutureTask<>(() -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent previousContent = saveClipboardContent(clipboard);
            Stage stage = new Stage();
            try {
                HelpWindow helpWindow = new HelpWindow(stage);
                helpWindow.getCopyButton().fire();
                assertEquals(HelpWindow.USERGUIDE_URL, clipboard.getString());
            } finally {
                restoreClipboardContent(clipboard, previousContent);
                stage.close();
            }
            return null;
        });

        Platform.runLater(testOnFxThread);
        testOnFxThread.get(10, TimeUnit.SECONDS);
    }

    private static ClipboardContent saveClipboardContent(Clipboard clipboard) {
        ClipboardContent content = new ClipboardContent();
        for (DataFormat format : clipboard.getContentTypes()) {
            Object value = clipboard.getContent(format);
            if (value != null) {
                content.put(format, value);
            }
        }
        return content;
    }

    private static void restoreClipboardContent(Clipboard clipboard, ClipboardContent content) {
        if (content.isEmpty()) {
            clipboard.clear();
        } else {
            clipboard.setContent(content);
        }
    }

    @Test
    public void openUserGuide_whenDesktopUnsupported_logsWarningWithoutOpeningUrl() {
        AtomicReference<String> warning = new AtomicReference<>();
        AtomicReference<URI> openedUri = new AtomicReference<>();

        HelpWindow.openUserGuide(false, openedUri::set, warning::set);

        assertTrue(warning.get().contains("desktop browsing is not supported"));
        assertNull(openedUri.get());
    }

    @Test
    public void openUserGuide_whenBrowserFails_logsWarning() {
        AtomicReference<String> warning = new AtomicReference<>();

        HelpWindow.openUserGuide(true, uri -> {
            throw new IOException("Browser unavailable");
        }, warning::set);

        assertTrue(warning.get().contains("Browser unavailable"));
    }
}
