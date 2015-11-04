package ninja.eivind.hotsreplayuploader.services.platform;

import java.io.File;
import java.io.IOException;
import java.net.URI;

public class LinuxService implements PlatformService {
    @Override
    public File getApplicationHome() {
        return new File(USER_HOME, APPLICATION_DIRECTORY_NAME);
    }

    @Override
    public File getHotSHome() {
        return new File(USER_HOME, "Heroes of the Storm/Accounts/");
    }

    @Override
    public void browse(final URI uri) throws IOException {
        Runtime.getRuntime().exec("xdg-open " + uri.toURL());
    }
}
