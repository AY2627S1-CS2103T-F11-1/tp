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
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.FollowUpCommand;
import seedu.address.logic.commands.FollowUpsCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.RedoCommand;
import seedu.address.logic.commands.SelectCommand;
import seedu.address.logic.commands.SortCommand;
import seedu.address.logic.commands.UndoCommand;

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
        assertTrue(HelpWindow.HELP_MESSAGE.contains("MANAGE CONTACTS\n"));
        assertTrue(HelpWindow.HELP_MESSAGE.contains("BROWSE CONTACTS\n"));
        assertTrue(HelpWindow.HELP_MESSAGE.contains("UNDO AND REDO\n"));
        assertTrue(HelpWindow.HELP_MESSAGE.contains("APPLICATION\n"));
        assertTrue(HelpWindow.HELP_MESSAGE.contains("ADD\nAdds a person to the address book."));
        assertTrue(HelpWindow.HELP_MESSAGE.contains("Parameters: n/NAME"));
        assertTrue(HelpWindow.HELP_MESSAGE.contains("Example: add n/John Doe"));
        assertTrue(HelpWindow.HELP_MESSAGE.contains("\nEDIT\n"));
        assertTrue(HelpWindow.HELP_MESSAGE.contains("\nDELETE\n"));
        assertTrue(HelpWindow.HELP_MESSAGE.contains("\nFIND\n"));
        assertTrue(HelpWindow.HELP_MESSAGE.contains("\nSELECT\n" + SelectCommand.COMMAND_DESCRIPTION));
        assertTrue(HelpWindow.HELP_MESSAGE.contains("\nLIST\nShows all persons"));
        assertTrue(HelpWindow.HELP_MESSAGE.contains("\nLIST\n" + ListCommand.COMMAND_DESCRIPTION));
        assertTrue(HelpWindow.HELP_MESSAGE.contains("\nCLEAR\nClears all entries"));
        assertTrue(HelpWindow.HELP_MESSAGE.contains("\nHELP\n" + HelpCommand.COMMAND_DESCRIPTION));
        assertTrue(HelpWindow.HELP_MESSAGE.contains("Parameters: " + HelpCommand.COMMAND_PARAMETERS));
        assertTrue(HelpWindow.HELP_MESSAGE.contains("Example: " + HelpCommand.COMMAND_EXAMPLE));
        assertTrue(HelpWindow.HELP_MESSAGE.contains("\nEXIT\nExits the program."));
        assertTrue(HelpWindow.HELP_MESSAGE.contains("----------------------------------------"));
    }

    @Test
    public void helpMessage_placesCommandsInTaskCategories() {
        String[] categories = {"MANAGE CONTACTS", "BROWSE CONTACTS", "UNDO AND REDO", "APPLICATION"};
        String[][] commandsByCategory = {
            {"ADD", "EDIT", "DELETE", "CLEAR", "FOLLOWUP"},
            {"LIST", "FIND", "SORT", "SELECT", "FOLLOWUPS"},
            {"UNDO", "REDO"},
            {"HELP", "EXIT"}
        };

        for (int i = 0; i < categories.length; i++) {
            int start = HelpWindow.HELP_MESSAGE.indexOf(categories[i] + "\n");
            int end = i + 1 < categories.length
                    ? HelpWindow.HELP_MESSAGE.indexOf(categories[i + 1] + "\n")
                    : HelpWindow.HELP_MESSAGE.length();
            assertTrue(start >= 0 && end > start, "Categories should appear in the expected order");
            String section = HelpWindow.HELP_MESSAGE.substring(start, end);
            for (String command : commandsByCategory[i]) {
                assertTrue(section.contains("\n" + command + "\n"),
                        command + " should appear under " + categories[i]);
            }
        }
    }

    @Test
    public void helpMessage_containsUsageForEveryCommand() {
        assertUsageIsDisplayed(AddCommand.COMMAND_USAGE);
        assertUsageIsDisplayed(EditCommand.COMMAND_USAGE);
        assertUsageIsDisplayed(DeleteCommand.COMMAND_USAGE);
        assertUsageIsDisplayed(FindCommand.COMMAND_USAGE);
        assertUsageIsDisplayed(FollowUpCommand.COMMAND_USAGE);
        assertUsageIsDisplayed(FollowUpsCommand.COMMAND_USAGE);
        assertUsageIsDisplayed(SortCommand.COMMAND_USAGE);
        assertUsageIsDisplayed(SelectCommand.COMMAND_USAGE);
        assertUsageIsDisplayed(ListCommand.COMMAND_USAGE);
        assertUsageIsDisplayed(ClearCommand.COMMAND_USAGE);
        assertUsageIsDisplayed(UndoCommand.COMMAND_USAGE);
        assertUsageIsDisplayed(RedoCommand.COMMAND_USAGE);
        assertUsageIsDisplayed(HelpCommand.COMMAND_USAGE);
        assertUsageIsDisplayed(ExitCommand.COMMAND_USAGE);
    }

    @Test
    public void helpMessage_commandsWithoutParameters_haveDescriptionOnly() {
        String[] commands = {"LIST", "CLEAR", "UNDO", "REDO", "EXIT"};
        for (String command : commands) {
            String section = HelpWindow.HELP_MESSAGE.split("\n" + command + "\n", 2)[1];
            String description = section.split("\n", 2)[0];
            String remainder = section.substring(description.length());
            assertTrue(remainder.isEmpty() || remainder.startsWith("\n----------------------------------------")
                    || remainder.startsWith("\n\n"), command + " should contain only a description");
        }
    }

    private static void assertUsageIsDisplayed(String messageUsage) {
        int separator = messageUsage.indexOf(": ");
        assertTrue(separator >= 0, "Usage text should separate the command name and description");

        String expected = messageUsage.substring(0, separator).toUpperCase(Locale.ROOT)
                + "\n" + messageUsage.substring(separator + 2);
        String heading = "\n" + messageUsage.substring(0, separator).toUpperCase(Locale.ROOT) + "\n";
        assertEquals(HelpWindow.HELP_MESSAGE.indexOf(heading), HelpWindow.HELP_MESSAGE.lastIndexOf(heading),
                "Each command should appear only once");
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
    public void userGuideLink_afterClick_keepsReadableVisitedColour() throws Exception {
        FutureTask<Void> testOnFxThread = new FutureTask<>(() -> {
            Stage stage = new Stage();
            try {
                HelpWindow helpWindow = new HelpWindow(stage, () -> true, uri -> { }, url -> { });
                stage.getScene().getRoot().applyCss();
                assertEquals(Color.web("#8ab4f8"), helpWindow.getUserGuideLink().getTextFill());

                helpWindow.getUserGuideLink().fire();
                stage.getScene().getRoot().applyCss();

                assertTrue(helpWindow.getUserGuideLink().isVisited());
                assertEquals(Color.web("#c4b5fd"), helpWindow.getUserGuideLink().getTextFill());
            } finally {
                stage.close();
            }
            return null;
        });

        Platform.runLater(testOnFxThread);
        testOnFxThread.get(10, TimeUnit.SECONDS);
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
