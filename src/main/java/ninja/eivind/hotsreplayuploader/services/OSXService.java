package ninja.eivind.hotsreplayuploader.services;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;

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
}
