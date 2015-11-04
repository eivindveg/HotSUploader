package ninja.eivind.hotsreplayuploader.services.platform;

public class PlatformNotSupportedException extends Exception {
    public PlatformNotSupportedException(final String message) {
        super(message);
    }
}
