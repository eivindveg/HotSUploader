package ninja.eivind.hotsreplayuploader.services.platform;

import javafx.application.Platform;
import javafx.event.EventType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

public class OSXService implements PlatformService {
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
    public TrayIcon getTrayIcon(final URL imageURL, final Stage primaryStage) throws PlatformNotSupportedException {
        EventType<KeyEvent> keyPressed = KeyEvent.KEY_PRESSED;
        primaryStage.addEventHandler(keyPressed, event -> {
            if (event.getCode() == KeyCode.Q && event.isMetaDown()) {
                Platform.exit();
            }
        });
        return buildTrayIcon(imageURL, primaryStage);
    }
}
