package ninja.eivind.hotsreplayuploader.di;

import javafx.fxml.FXMLLoader;

/**
 *  Functional interface used by JavaFX controllers that need to be initialized by the {@link FXMLLoader}
 *  Not to be confused with {@link Initializable}, which does the same in the IoC container, but at an earlier point.
 */
@FunctionalInterface
public interface JavaFXController {

    /**
     * Initializes this object after all dependencies have been injected.
     */
    void initialize();
}
