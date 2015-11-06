package ninja.eivind.hotsreplayuploader;

import com.gluonhq.ignite.DIContext;
import com.gluonhq.ignite.guice.GuiceContext;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import ninja.eivind.hotsreplayuploader.di.GuiceModule;
import ninja.eivind.hotsreplayuploader.services.platform.PlatformNotSupportedException;
import ninja.eivind.hotsreplayuploader.services.platform.PlatformService;
import ninja.eivind.hotsreplayuploader.versions.ReleaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.awt.*;
import java.net.URL;
import java.util.Collections;


public class Client extends Application {

    public static void main(String[] args) {
        Application.launch(Client.class, args);
    }

    private static final Logger LOG = LoggerFactory.getLogger(Client.class);
    private DIContext context = new GuiceContext(this, () -> Collections.singletonList(new GuiceModule()));

    @Inject
    private FXMLLoader fxmlLoader;

    @Inject
    private ReleaseManager releaseManager;

    @Inject
    private PlatformService platformService;

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

        fxmlLoader.setLocation(loader.getResource("ninja/eivind/hotsreplayuploader/window/Home.fxml"));
        Parent root = fxmlLoader.load();

        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    private void addToTray(final URL imageURL, final Stage primaryStage) {
        try {
            TrayIcon trayIcon = platformService.getTrayIcon(imageURL, primaryStage);
            SystemTray systemTray = SystemTray.getSystemTray();
            systemTray.add(trayIcon);
        }catch (PlatformNotSupportedException | AWTException e ){
            LOG.warn("Could not instantiate tray icon. Reverting to default behaviour", e);
            primaryStage.setOnCloseRequest(event -> System.exit(0));
        }
    }

}
