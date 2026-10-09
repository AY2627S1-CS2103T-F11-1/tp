package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.stage.Stage;

public class HelpWindowTest {

    @BeforeAll
    public static void initializeJavaFx() throws InterruptedException {
        CountDownLatch started = new CountDownLatch(1);
        Platform.startup(started::countDown);
        assertTrue(started.await(10, TimeUnit.SECONDS));
    }

    @AfterAll
    public static void shutdownJavaFx() {
        Platform.exit();
    }

    @Test
    public void helpMessage_containsCommandFormatsAndDescriptions() {
        assertTrue(HelpWindow.HELP_MESSAGE.contains("add n/NAME p/PHONE e/EMAIL a/ADDRESS r/ROLE [t/TAG]..."));
        assertTrue(HelpWindow.HELP_MESSAGE.contains("edit INDEX [n/NAME] [p/PHONE] [e/EMAIL] [a/ADDRESS] [t/TAG]..."));
        assertTrue(HelpWindow.HELP_MESSAGE.contains("delete INDEX"));
        assertTrue(HelpWindow.HELP_MESSAGE.contains("find KEYWORD [MORE_KEYWORDS]..."));
        assertTrue(HelpWindow.HELP_MESSAGE.contains("Role editing is not supported yet"));
        assertTrue(HelpWindow.HELP_MESSAGE.contains("\nlist\n"));
        assertTrue(HelpWindow.HELP_MESSAGE.contains("\nclear\n"));
        assertTrue(HelpWindow.HELP_MESSAGE.contains("\nhelp\n"));
        assertTrue(HelpWindow.HELP_MESSAGE.contains("\nexit\n"));
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
        AtomicReference<String> copiedUrl = new AtomicReference<>();
        FutureTask<Void> testOnFxThread = new FutureTask<>(() -> {
            Stage stage = new Stage();
            try {
                HelpWindow helpWindow = new HelpWindow(stage, () -> true, uri -> { }, copiedUrl::set);
                helpWindow.getCopyButton().fire();
            } finally {
                stage.close();
            }
            return null;
        });

        Platform.runLater(testOnFxThread);
        testOnFxThread.get(10, TimeUnit.SECONDS);

        assertEquals(HelpWindow.USERGUIDE_URL, copiedUrl.get());
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
