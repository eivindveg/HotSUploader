package ninja.eivind.hotsreplayuploader.services;

import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.net.URI;

@Singleton
public interface PlatformService {

    File getApplicationHome();

    File getHotSHome();

    void browse(URI uri) throws IOException;
}
