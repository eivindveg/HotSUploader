package ninja.eivind.hotsreplayuploader.services.platform;

import com.google.inject.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlatformServiceProvider implements Provider<PlatformService> {

    private static final Logger LOG = LoggerFactory.getLogger(PlatformServiceProvider.class);
    private static final String OS_NAME = System.getProperty("os.name");

    @Override
    public PlatformService get() {
        LOG.info("Constructing PlatformService for " + OS_NAME);
        if (OS_NAME.contains("Windows")) {
            return new WindowsService();
        } else if (OS_NAME.contains("Mac")) {
            return new OSXService();
        } else if (OS_NAME.contains("Linux")) {
            return new LinuxService();
        } else {
            throw new UnsupportedOperationException("Operating system not supported");
        }
    }
}
