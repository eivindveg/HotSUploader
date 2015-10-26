package ninja.eivind.hotsuploader;

import ninja.eivind.hotsuploader.files.FileHandler;
import ninja.eivind.hotsuploader.utils.SimpleHttpClient;
import ninja.eivind.hotsuploader.utils.StormHandler;
import ninja.eivind.hotsuploader.versions.ReleaseManager;
import ninja.eivind.hotsuploader.window.HomeController;
import io.datafx.controller.flow.Flow;
import io.datafx.controller.flow.FlowHandler;
import io.datafx.controller.flow.container.DefaultFlowContainer;
import io.datafx.controller.flow.context.ViewFlowContext;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URL;


public class Client extends Application {

    public static void main(String[] args) {
        Application.launch(Client.class, args);
    }

    @Override
    public void start(final Stage primaryStage) throws Exception {
        ClassLoader loader = ClassLoader.getSystemClassLoader();
        URL logo = loader.getResource("images/logo-desktop.png");
        assert logo != null;
        Image image = new Image(logo.toString());
        primaryStage.getIcons().add(image);
        primaryStage.setResizable(false);
        StormHandler stormHandler = new StormHandler();
        addToTray(logo, primaryStage);

        Flow flow = new Flow(HomeController.class);
        FlowHandler flowHandler = flow.createHandler(new ViewFlowContext());
        ViewFlowContext flowContext = flowHandler.getFlowContext();

        SimpleHttpClient httpClient = new SimpleHttpClient();
        ReleaseManager releaseManager = new ReleaseManager(httpClient);

        // Set window title
        String windowTitle = "HotSLogs UploaderFX v" + releaseManager.getCurrentVersion();
        primaryStage.setTitle(windowTitle);

        registerInContext(flowContext, stormHandler, releaseManager, setupFileHandler(stormHandler), httpClient);

        DefaultFlowContainer container = new DefaultFlowContainer();

        releaseManager.verifyLocalVersion(stormHandler);

        StackPane pane = flowHandler.start(container);
        primaryStage.setScene(new Scene(pane));
        primaryStage.show();
    }

    private FileHandler setupFileHandler(final StormHandler stormHandler) throws IOException {
        FileHandler fileHandler = new FileHandler(stormHandler);
        fileHandler.cleanup();
        fileHandler.registerInitial();
        return fileHandler;
    }

    private void registerInContext(ViewFlowContext context, Object... itemsToAdd) {
        for (final Object itemToAdd : itemsToAdd) {
            context.register(itemToAdd);
        }
    }

    private void addToTray(final URL imageURL, final Stage primaryStage) {
        // TODO FIND A WAY TO MAKE THIS SWEET ON OSX
        boolean support = SystemTray.isSupported() && StormHandler.isWindows();

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
