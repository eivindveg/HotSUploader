package ninja.eivind.hotsreplayuploader.di;

/**
 * Functional interface for objects that need to be initialized before they can accept calls. Used for classes
 * that are not expected to operate before all fields are set.
 */
@FunctionalInterface
public interface Initializable {

    /**
     * Initializes this object after all members have been injected. Called automatically by the IoC context.
     */
    void initialize();
}
