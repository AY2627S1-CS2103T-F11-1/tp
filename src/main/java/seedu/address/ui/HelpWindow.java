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
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import seedu.address.commons.core.LogsCenter;
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
import seedu.address.logic.commands.SortCommand;
import seedu.address.logic.commands.UndoCommand;

/**
 * Controller for a help page
 */
public class HelpWindow extends UiPart<Stage> {

    public static final String USERGUIDE_URL = "https://ay2627s1-cs2103t-f11-1.github.io/tp/UserGuide.html#quick-start";
    private static final String COMMAND_DIVIDER = "----------------------------------------";
    private static final String SECTION_DIVIDER = "========================================";

    public static final String HELP_MESSAGE = String.join("\n",
            "MANAGE CONTACTS",
            SECTION_DIVIDER,
            formatCommandUsage(AddCommand.COMMAND_USAGE),
            COMMAND_DIVIDER,
            formatCommandUsage(EditCommand.COMMAND_USAGE),
            COMMAND_DIVIDER,
            formatCommandUsage(DeleteCommand.COMMAND_USAGE),
            COMMAND_DIVIDER,
            formatCommandUsage(ClearCommand.COMMAND_USAGE),
            COMMAND_DIVIDER,
            formatCommandUsage(FollowUpCommand.COMMAND_USAGE),
            "",
            "",
            "",
            "BROWSE CONTACTS",
            SECTION_DIVIDER,
            formatCommandUsage(ListCommand.COMMAND_USAGE),
            COMMAND_DIVIDER,
            formatCommandUsage(FindCommand.COMMAND_USAGE),
            COMMAND_DIVIDER,
            formatCommandUsage(SortCommand.COMMAND_USAGE),
            COMMAND_DIVIDER,
            formatCommandUsage(FollowUpsCommand.COMMAND_USAGE),
            "",
            "",
            "",
            "UNDO AND REDO",
            SECTION_DIVIDER,
            formatCommandUsage(UndoCommand.COMMAND_USAGE),
            COMMAND_DIVIDER,
            formatCommandUsage(RedoCommand.COMMAND_USAGE),
            "",
            "",
            "",
            "APPLICATION",
            SECTION_DIVIDER,
            formatCommandUsage(HelpCommand.COMMAND_USAGE),
            COMMAND_DIVIDER,
            formatCommandUsage(ExitCommand.COMMAND_USAGE));

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
        configureKeyboardScrolling();
    }

    /**
     * Creates a new HelpWindow.
     */
    public HelpWindow() {
        this(new Stage());
    }

    /** Formats shared usage text with an uppercase heading for the help window. */
    private static String formatCommandUsage(String commandUsage) {
        int separator = commandUsage.indexOf(": ");
        return commandUsage.substring(0, separator).toUpperCase(Locale.ROOT)
                + "\n" + commandUsage.substring(separator + 2);
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
        helpMessage.requestFocus();
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

    TextArea getHelpMessage() {
        return helpMessage;
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
        helpMessage.requestFocus();
    }

    /**
     * Configures keyboard navigation and scrolling for the help message content.
     */
    private void configureKeyboardScrolling() {
        final double lineScrollDelta = 40.0;
        getRoot().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            double pageScrollDelta = Math.max(lineScrollDelta, helpMessage.getHeight() - lineScrollDelta);
            switch (event.getCode()) {
                case DOWN:
                    helpMessage.setScrollTop(helpMessage.getScrollTop() + lineScrollDelta);
                    event.consume();
                    break;
                case UP:
                    helpMessage.setScrollTop(Math.max(0, helpMessage.getScrollTop() - lineScrollDelta));
                    event.consume();
                    break;
                case PAGE_DOWN:
                    helpMessage.setScrollTop(helpMessage.getScrollTop() + pageScrollDelta);
                    event.consume();
                    break;
                case PAGE_UP:
                    helpMessage.setScrollTop(Math.max(0, helpMessage.getScrollTop() - pageScrollDelta));
                    event.consume();
                    break;
                case HOME:
                    helpMessage.setScrollTop(0);
                    event.consume();
                    break;
                case END:
                    helpMessage.setScrollTop(Double.MAX_VALUE);
                    event.consume();
                    break;
                default:
                    break;
            }
        });
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
