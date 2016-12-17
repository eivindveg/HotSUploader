package ninja.eivind.hotsreplayuploader.preloader;

import javafx.application.Preloader;

public interface PreloaderHandle {

    void notifyPreloader(Preloader.PreloaderNotification info);
}
