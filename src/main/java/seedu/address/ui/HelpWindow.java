package seedu.address.ui;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.util.Locale;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.logging.Logger;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.Stage;
import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.ListCommand;

/**
 * Controller for a help page
 */
public class HelpWindow extends UiPart<Stage> {

    public static final String USERGUIDE_URL = "https://ay2627s1-cs2103t-f11-1.github.io/tp/UserGuide.html#quick-start";
    private static final String COMMAND_DIVIDER = "----------------------------------------";
    private static final String SECTION_DIVIDER = "========================================";

    public static final String HELP_MESSAGE = String.join("\n",
            "PEOPLE",
            SECTION_DIVIDER,
            formatCommandUsage(AddCommand.MESSAGE_USAGE),
            COMMAND_DIVIDER,
            formatCommandUsage(EditCommand.MESSAGE_USAGE),
            COMMAND_DIVIDER,
            formatCommandUsage(DeleteCommand.MESSAGE_USAGE),
            COMMAND_DIVIDER,
            formatCommandUsage(FindCommand.MESSAGE_USAGE),
            "",
            "ADDRESS BOOK",
            SECTION_DIVIDER,
            formatCommandUsage(ListCommand.MESSAGE_USAGE),
            COMMAND_DIVIDER,
            formatCommandUsage(ClearCommand.MESSAGE_USAGE),
            "",
            "APPLICATION",
            SECTION_DIVIDER,
            formatCommandUsage(HelpCommand.MESSAGE_USAGE),
            COMMAND_DIVIDER,
            formatCommandUsage(ExitCommand.MESSAGE_USAGE));

    private static final Logger logger = LogsCenter.getLogger(HelpWindow.class);
    private static final String FXML = "HelpWindow.fxml";

    @FXML
    private Button copyButton;

    @FXML
    private TextArea helpMessage;

    @FXML
    private Hyperlink userGuideLink;

    private final BooleanSupplier desktopSupport;
    private final BrowserLauncher browserLauncher;
    private final Consumer<String> clipboardWriter;

    /**
     * Creates a new HelpWindow.
     *
     * @param root Stage to use as the root of the HelpWindow.
     */
    public HelpWindow(Stage root) {
        this(root, Desktop::isDesktopSupported, uri -> Desktop.getDesktop().browse(uri),
                HelpWindow::copyUrlToSystemClipboard);
    }

    HelpWindow(Stage root, BooleanSupplier desktopSupport, BrowserLauncher browserLauncher,
            Consumer<String> clipboardWriter) {
        super(FXML, root);
        this.desktopSupport = desktopSupport;
        this.browserLauncher = browserLauncher;
        this.clipboardWriter = clipboardWriter;
        helpMessage.setText(HELP_MESSAGE);
    }

    /**
     * Creates a new HelpWindow.
     */
    public HelpWindow() {
        this(new Stage());
    }

    /**
     * Formats a command's usage message with its command name on a separate uppercase line.
     *
     * @param messageUsage the usage message to format
     * @return the formatted usage message, or the uppercase message if it has no description separator
     */
    private static String formatCommandUsage(String messageUsage) {
        int descriptionStart = messageUsage.indexOf(": ");
        if (descriptionStart < 0) {
            return messageUsage.toUpperCase(Locale.ROOT);
        }
        String commandName = messageUsage.substring(0, descriptionStart).toUpperCase(Locale.ROOT);
        String descriptionAndDetails = messageUsage.substring(descriptionStart + 2);
        return commandName + "\n" + descriptionAndDetails;
    }

    /**
     * Shows the help window.
     * @throws IllegalStateException
     *     <ul>
     *         <li>
     *             if this method is called on a thread other than the JavaFX Application Thread.
     *         </li>
     *         <li>
     *             if this method is called during animation or layout processing.
     *         </li>
     *         <li>
     *             if this method is called on the primary stage.
     *         </li>
     *         <li>
     *             if {@code dialogStage} is already showing.
     *         </li>
     *     </ul>
     */
    public void show() {
        logger.fine("Showing help page about the application.");
        getRoot().show();
        getRoot().centerOnScreen();
    }

    /**
     * Returns true if the help window is currently being shown.
     */
    public boolean isShowing() {
        return getRoot().isShowing();
    }

    Hyperlink getUserGuideLink() {
        return userGuideLink;
    }

    Button getCopyButton() {
        return copyButton;
    }

    /**
     * Hides the help window.
     */
    public void hide() {
        getRoot().hide();
    }

    /**
     * Focuses on the help window.
     */
    public void focus() {
        getRoot().requestFocus();
    }

    /**
     * Copies the URL to the user guide to the clipboard.
     */
    @FXML
    private void copyUrl() {
        copyUrl(clipboardWriter);
    }

    static void copyUrl(Consumer<String> copyAction) {
        copyAction.accept(USERGUIDE_URL);
    }

    private static void copyUrlToSystemClipboard(String url) {
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent content = new ClipboardContent();
        content.putString(url);
        clipboard.setContent(content);
    }

    /** Opens the user guide in the system's default browser. */
    @FXML
    private void openUserGuide() {
        openUserGuide(desktopSupport.getAsBoolean(), browserLauncher, logger::warning);
    }

    static void openUserGuide(boolean desktopSupported, BrowserLauncher browserLauncher,
            Consumer<String> warningLogger) {
        if (!desktopSupported) {
            warningLogger.accept("Unable to open user guide: desktop browsing is not supported.");
            return;
        }
        try {
            browserLauncher.open(URI.create(USERGUIDE_URL));
        } catch (IOException | UnsupportedOperationException e) {
            warningLogger.accept("Unable to open user guide: " + e.getMessage());
        }
    }

    @FunctionalInterface
    interface BrowserLauncher {
        void open(URI uri) throws IOException;
    }
}
