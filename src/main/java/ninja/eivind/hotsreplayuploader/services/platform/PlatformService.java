package ninja.eivind.hotsreplayuploader.services.platform;

import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

public interface PlatformService {

    String USER_HOME = System.getProperty("user.home");
    String APPLICATION_DIRECTORY_NAME = "HotSLogs UploaderFX";

    File getApplicationHome();

    File getHotSHome();

    default TrayIcon getTrayIcon(final URL imageURL, Stage primaryStage) throws PlatformNotSupportedException {
        throw new PlatformNotSupportedException("Not implemented in " + getClass());
    }

    void browse(URI uri) throws IOException;
}
