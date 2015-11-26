// Copyright 2015 Eivind Vegsundvåg
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package ninja.eivind.hotsreplayuploader;

import com.gluonhq.ignite.DIContext;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import ninja.eivind.hotsreplayuploader.di.ContextTask;
import ninja.eivind.hotsreplayuploader.models.stringconverters.StatusBinder;
import ninja.eivind.hotsreplayuploader.services.platform.PlatformNotSupportedException;
import ninja.eivind.hotsreplayuploader.services.platform.PlatformService;
import ninja.eivind.hotsreplayuploader.utils.Constants;
import ninja.eivind.hotsreplayuploader.versions.ReleaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.awt.*;
import java.io.IOException;
import java.net.URL;

/**
 * Application entry point. Sets up the actions that connect to the underlying platform.
 */
public class Client extends Application {

    private static final Logger LOG = LoggerFactory.getLogger(Client.class);
    private DIContext context;
    @Inject
    private FXMLLoader fxmlLoader;
    @Inject
    private ReleaseManager releaseManager;
    @Inject
    private PlatformService platformService;
    @Inject
    private StatusBinder statusBinder;
    private TrayIcon trayIcon;

    public static void main(String... args) {
        launch(Client.class, args);
    }

    @Override
    public void stop() throws Exception {
        if (trayIcon != null) {
            SystemTray.getSystemTray().remove(trayIcon);
        }
        context.dispose();
        super.stop();
    }

    @Override
    public void init() {

    }

    @Override
    public void start(final Stage primaryStage) throws Exception {
        Stage preLoaderStage = launchPreloader(primaryStage);

        LOG.info("Initializing application context.");
        Task<DIContext> initTask = new ContextTask();
        LOG.info("Application context initialized.");
        initTask.setOnSucceeded(value -> launchApplication(primaryStage, preLoaderStage, initTask));

        new Thread(initTask).start();
    }

    private void launchApplication(Stage primaryStage, Stage preLoaderStage, Task<DIContext> initTask) {
        try {
            context = initTask.getValue();
            context.injectMembers(Client.this);
            LOG.info("Loading primary view.");
            URL logo = platformService.getLogoUrl();
            Image image = new Image(logo.toString());
            primaryStage.getIcons().add(image);
            primaryStage.setResizable(false);

            // Set window title
            String windowTitle = Constants.APPLICATION_NAME + " v" + releaseManager.getCurrentVersion();

            fxmlLoader.setLocation(getClass().getResource("/ninja/eivind/hotsreplayuploader/window/Home.fxml"));

            Parent root = fxmlLoader.load();
            LOG.info("Application loaded. Showing.");
            primaryStage.setTitle(windowTitle);
            addToTray(primaryStage);
            primaryStage.initStyle(StageStyle.DECORATED);
            primaryStage.setScene(new Scene(root));
            preLoaderStage.hide();
            primaryStage.show();
            primaryStage.toFront();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Stage launchPreloader(Stage owner) throws IOException {
        LOG.info("Preloading application");
        Stage preLoaderStage = new Stage();
        preLoaderStage.initOwner(owner);

        preLoaderStage.initModality(Modality.WINDOW_MODAL);
        preLoaderStage.initStyle(StageStyle.UNDECORATED);

        Parent root = FXMLLoader.load(getClass().getResource("window/Preloader.fxml"));

        Scene scene = new Scene(root);

        preLoaderStage.setScene(scene);
        preLoaderStage.sizeToScene();
        preLoaderStage.show();
        return preLoaderStage;
    }

    private void addToTray(final Stage primaryStage) {
        try {
            trayIcon = platformService.getTrayIcon(primaryStage);
            SystemTray systemTray = SystemTray.getSystemTray();
            systemTray.add(trayIcon);

            statusBinder.message().addListener((observable, oldValue, newValue) -> {
                if (newValue != null && !newValue.isEmpty()) {
                    trayIcon.setToolTip("Status: " + newValue);
                }
            });
        } catch (PlatformNotSupportedException | AWTException e) {
            LOG.warn("Could not instantiate tray icon. Reverting to default behaviour", e);
            primaryStage.setOnCloseRequest(event -> Platform.exit());
        }
    }

}
