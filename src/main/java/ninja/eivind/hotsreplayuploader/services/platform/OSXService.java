package ninja.eivind.hotsreplayuploader.services.platform;

import javafx.event.EventType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

public class OSXService implements PlatformService {
    private static final Logger LOG = LoggerFactory.getLogger(OSXService.class);
    private final String libraryPath = "/Library/Application Support";
    private Desktop desktop;

    public OSXService() {
        desktop = Desktop.getDesktop();
    }

    @Override
    public File getApplicationHome() {
        return new File(USER_HOME + "/" + libraryPath + "/" + APPLICATION_DIRECTORY_NAME);
    }

    @Override
    public File getHotSHome() {
        return new File(USER_HOME + "/" + libraryPath + "/" + "Blizzard/Heroes of the Storm/Accounts/");
    }

    @Override
    public void browse(final URI uri) throws IOException {
        desktop.browse(uri);
    }

    @Override
    public TrayIcon getTrayIcon(final Stage primaryStage) throws PlatformNotSupportedException {
        URL imageURL = ClassLoader.getSystemClassLoader().getResource("images/logo-desktop-black.png");
        EventType<KeyEvent> keyPressed = KeyEvent.KEY_PRESSED;
        primaryStage.addEventHandler(keyPressed, event -> {
            if (event.getCode() == KeyCode.Q && event.isMetaDown()) {
                LOG.info("Exiting application due to keyboard shortcut.");
                System.exit(0);
            }
        });
        return buildTrayIcon(imageURL, primaryStage);
    }
}
