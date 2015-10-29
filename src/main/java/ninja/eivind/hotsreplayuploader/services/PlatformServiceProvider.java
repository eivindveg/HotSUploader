package ninja.eivind.hotsreplayuploader.services;

import com.google.inject.Provides;

public class PlatformServiceProvider {

    private static final String OS_NAME = System.getProperty("os.name");

    @Provides
    public PlatformService provideNativeService() {
        if(OS_NAME.contains("Windows")) {
            return new WindowsService();
        } else if(OS_NAME.contains("Mac")) {
            return new OSXService();
        } else if(OS_NAME.contains("Linux")) {
            return new LinuxService();
        } else {
            throw new UnsupportedOperationException("Operating system not supported");
        }
    }
}
