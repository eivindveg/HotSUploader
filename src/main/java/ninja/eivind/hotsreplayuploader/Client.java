package ninja.eivind.hotsreplayuploader;

import com.gluonhq.ignite.DIContext;
import com.gluonhq.ignite.guice.GuiceContext;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import ninja.eivind.hotsreplayuploader.di.GuiceModule;
import ninja.eivind.hotsreplayuploader.services.PlatformService;
import ninja.eivind.hotsreplayuploader.services.WindowsService;
import ninja.eivind.hotsreplayuploader.utils.StormHandler;
import ninja.eivind.hotsreplayuploader.versions.ReleaseManager;

import javax.inject.Inject;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;
import java.util.Collections;


public class Client extends Application {

    public static void main(String[] args) {
        Application.launch(Client.class, args);
    }

    private DIContext context = new GuiceContext(this, () -> Collections.singletonList(new GuiceModule()));

    @Inject
    private FXMLLoader fxmlLoader;

    @Inject ReleaseManager releaseManager;

    @Inject
    PlatformService platformService;

    @Override
    public void start(final Stage primaryStage) throws Exception {
        context.init();
        ClassLoader loader = ClassLoader.getSystemClassLoader();
        URL logo = loader.getResource("images/logo-desktop.png");
        assert logo != null;
        Image image = new Image(logo.toString());
        primaryStage.getIcons().add(image);
        primaryStage.setResizable(false);
        addToTray(logo, primaryStage);

        // Set window title
        String windowTitle = "HotSLogs UploaderFX v" + releaseManager.getCurrentVersion();
        primaryStage.setTitle(windowTitle);

        releaseManager.verifyLocalVersion();

        fxmlLoader.setLocation(loader.getResource("ninja/eivind/hotsreplayuploader/window/Home.fxml"));
        Parent root = fxmlLoader.load();

        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    private void addToTray(final URL imageURL, final Stage primaryStage) {
        // TODO FIND A WAY TO MAKE THIS SWEET ON OSX
        boolean support = SystemTray.isSupported() && platformService instanceof WindowsService;

        if (support) {
            final SystemTray tray = SystemTray.getSystemTray();
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
            trayIcon.addMouseListener(mouseListener(openAction));
            showItem.addActionListener(e -> openAction.run());
            exitItem.addActionListener(event -> {
                Platform.exit();
                System.exit(0);
            });

            try {
                tray.add(trayIcon);
            } catch (AWTException e) {
                e.printStackTrace();
            }
        } else {
            primaryStage.setOnCloseRequest(event -> System.exit(0));
        }
    }

    private MouseListener mouseListener(final Runnable result) {
        return new MouseListener() {
            @Override
            public void mouseClicked(final MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    result.run();
                }
            }

            @Override
            public void mousePressed(final MouseEvent e) {

            }

            @Override
            public void mouseReleased(final MouseEvent e) {

            }

            @Override
            public void mouseEntered(final MouseEvent e) {

            }

            @Override
            public void mouseExited(final MouseEvent e) {

            }
        };
    }

}
