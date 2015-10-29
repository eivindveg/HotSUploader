package ninja.eivind.hotsreplayuploader.services;

import java.io.File;
import java.io.IOException;
import java.net.URI;

public interface PlatformService {

    String USER_HOME = System.getProperty("user.home");
    String APPLICATION_DIRECTORY_NAME = "HotSLogs UploaderFX";

    File getApplicationHome();

    File getHotSHome();

    void browse(URI uri) throws IOException;
}
