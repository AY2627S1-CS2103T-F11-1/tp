package seedu.address.ui;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
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

/**
 * Controller for a help page
 */
public class HelpWindow extends UiPart<Stage> {

    public static final String USERGUIDE_URL = "https://ay2627s1-cs2103t-f11-1.github.io/tp/UserGuide.html#quick-start";
    public static final String HELP_MESSAGE = ""
            + "add n/NAME p/PHONE e/EMAIL a/ADDRESS r/ROLE [t/TAG]...\n"
            + "  Add a person. Name, phone, email, address, and role are required; tags are optional.\n\n"
            + "edit INDEX [n/NAME] [p/PHONE] [e/EMAIL] [a/ADDRESS] [t/TAG]...\n"
            + "  Edit a person's details. Role editing is not supported yet; update this help entry when it is.\n\n"
            + "delete INDEX\n"
            + "  Delete the person at the displayed index.\n\n"
            + "find KEYWORD [MORE_KEYWORDS]...\n"
            + "  Find people whose names contain any of the keywords (case-insensitive).\n\n"
            + "list\n"
            + "  Show all people.\n\n"
            + "clear\n"
            + "  Clear the address book.\n\n"
            + "help\n"
            + "  Show this help window.\n\n"
            + "exit\n"
            + "  Close the application.";

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
