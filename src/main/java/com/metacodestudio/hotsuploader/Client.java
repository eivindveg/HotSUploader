package com.metacodestudio.hotsuploader;

import com.metacodestudio.hotsuploader.files.FileHandler;
import com.metacodestudio.hotsuploader.utils.SimpleHttpClient;
import com.metacodestudio.hotsuploader.utils.StormHandler;
import com.metacodestudio.hotsuploader.versions.ReleaseManager;
import com.metacodestudio.hotsuploader.window.HomeController;
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
        primaryStage.setTitle("HotSLogs UploaderFX");
        addToTray(logo, primaryStage);

        Flow flow = new Flow(HomeController.class);
        FlowHandler flowHandler = flow.createHandler(new ViewFlowContext());
        ViewFlowContext flowContext = flowHandler.getFlowContext();

        StormHandler stormHandler = new StormHandler();
        SimpleHttpClient httpClient = new SimpleHttpClient();
        ReleaseManager releaseManager = new ReleaseManager(httpClient);

        flowContext.register(stormHandler);
        flowContext.register(releaseManager);
        flowContext.register(setupFileHandler(stormHandler));
        flowContext.register(httpClient);

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

    private void addToTray(final URL imageURL, Stage primaryStage) {
        if (SystemTray.isSupported()) {
            Platform.setImplicitExit(false);
            primaryStage.setOnCloseRequest(value -> {
                primaryStage.hide();
                value.consume();
            });
            SystemTray tray = SystemTray.getSystemTray();
            java.awt.Image image = Toolkit.getDefaultToolkit().getImage(imageURL);
            PopupMenu popup = new PopupMenu();
            MenuItem item = new MenuItem("Exit");

            popup.add(item);

            TrayIcon trayIcon = new TrayIcon(image, StormHandler.getApplicationName(), popup);
            trayIcon.setImageAutoSize(true);

            trayIcon.addActionListener(event -> Platform.runLater(primaryStage::show));
            item.addActionListener(event -> {
                Platform.exit();
                System.exit(0);
            });

            try{
                tray.add(trayIcon);
            } catch (AWTException e) {
                e.printStackTrace();
            }
        }
    }


}
