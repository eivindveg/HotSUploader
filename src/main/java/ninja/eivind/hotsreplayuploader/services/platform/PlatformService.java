package ninja.eivind.hotsreplayuploader.services.platform;

import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.stage.Window;
import ninja.eivind.hotsreplayuploader.utils.StormHandler;


import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

public interface PlatformService {

    String USER_HOME = System.getProperty("user.home");
    String APPLICATION_DIRECTORY_NAME = "HotSLogs UploaderFX";

    File getApplicationHome();

    File getHotSHome();

    default TrayIcon getTrayIcon(Stage primaryStage) throws PlatformNotSupportedException {
        throw new PlatformNotSupportedException("Not implemented in " + getClass());
    }

    default TrayIcon buildTrayIcon(URL imageURL, Stage primaryStage) {
        final java.awt.Image image = Toolkit.getDefaultToolkit().getImage(imageURL);
        final PopupMenu popup = new PopupMenu();
        final MenuItem showItem = new MenuItem("Show");
        final MenuItem exitItem = new MenuItem("Exit");

        // Deal with window events
        Platform.setImplicitExit(false);
        primaryStage.setOnHiding(value -> {
            primaryStage.hide();
            value.consume();
        });

        // Declare shared action for showItem and trayicon click
        Runnable openAction = () -> Platform.runLater(() -> {
            primaryStage.show();
            primaryStage.toFront();
        });
        popup.add(showItem);
        popup.add(exitItem);

        final TrayIcon trayIcon = new TrayIcon(image, StormHandler.getApplicationName(), popup);
        trayIcon.setImageAutoSize(true);

        // Add listeners
        trayIcon.addMouseListener(new TrayMouseListenerBase() {
            @Override
            public void mouseClicked(final MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() % 2 == 0) {
                    openAction.run();
                }
            }
        });
        showItem.addActionListener(e -> openAction.run());
        exitItem.addActionListener(event -> {
            Platform.exit();
            System.exit(0);
        });
        return trayIcon;

    }

    void browse(URI uri) throws IOException;
}
