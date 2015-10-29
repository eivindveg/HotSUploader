package ninja.eivind.hotsreplayuploader.services;

import java.io.File;
import java.io.IOException;
import java.net.URI;

public class LinuxService implements PlatformService {
    @Override
    public File getApplicationHome() {
        return null;
    }

    @Override
    public File getHotSHome() {
        return null;
    }

    @Override
    public void browse(final URI uri) throws IOException {
        Runtime.getRuntime().exec("xdg-open " + uri.toURL());
    }
}
