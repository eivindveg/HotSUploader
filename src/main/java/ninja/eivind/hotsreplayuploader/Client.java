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
import com.sun.javafx.application.LauncherImpl;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import ninja.eivind.hotsreplayuploader.di.CloseableGuiceContext;
import ninja.eivind.hotsreplayuploader.di.GuiceModule;
import ninja.eivind.hotsreplayuploader.models.stringconverters.StatusBinder;
import ninja.eivind.hotsreplayuploader.services.platform.PlatformNotSupportedException;
import ninja.eivind.hotsreplayuploader.services.platform.PlatformService;
import ninja.eivind.hotsreplayuploader.services.platform.PlatformServiceProvider;
import ninja.eivind.hotsreplayuploader.utils.Constants;
import ninja.eivind.hotsreplayuploader.versions.ReleaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.awt.*;
import java.net.URL;
import java.util.Collections;

/**
 * Application entry point. Sets up the actions that connect to the underlying platform.
 */
public class Client extends Application {

    private static final Logger LOG = LoggerFactory.getLogger(Client.class);
    private final DIContext context = new CloseableGuiceContext(this, () -> Collections.singletonList(new GuiceModule()));
    @Inject
    private FXMLLoader fxmlLoader;
    @Inject
    private ReleaseManager releaseManager;
    @Inject
    private PlatformService platformService;
    @Inject
    private StatusBinder statusBinder;

    public static void main(String... args) {
        PlatformService platformService = new PlatformServiceProvider().get();
        if (platformService.isPreloaderSupported()) {
            LOG.info("Launching with preloader.");
            LauncherImpl.launchApplication(Client.class, ClientPreloader.class, args);
        } else {
            LOG.info("Launching without preloader.");
            launch(Client.class, args);
        }
    }

    @Override
    public void stop() throws Exception {
        context.dispose();
        super.stop();
        System.exit(0);
    }

    @Override
    public void init() {
        context.init();

        //add a shutdown hook to be really sure, resources are closed properly
        Runtime.getRuntime().addShutdownHook(new Thread(context::dispose));
    }

    @Override
    public void start(final Stage primaryStage) throws Exception {
        try {
            final URL logo = platformService.getLogoUrl();
            final Image image = new Image(logo.toString());
            primaryStage.getIcons().add(image);
            primaryStage.setResizable(false);
            addToTray(primaryStage);
            platformService.setupWindowBehaviour(primaryStage);

            // Set window title
            final String windowTitle = Constants.APPLICATION_NAME + " v" + releaseManager.getCurrentVersion();
            primaryStage.setTitle(windowTitle);

            fxmlLoader.setLocation(getClass().getResource("/ninja/eivind/hotsreplayuploader/window/Home.fxml"));
            final Parent root = fxmlLoader.load();

            primaryStage.setScene(new Scene(root));
            primaryStage.show();
        } catch (Throwable e) {
            LOG.error("Failed to start application", e);
            throw new RuntimeException(e);
        }
    }

    private void addToTray(final Stage primaryStage) {
        try {
            TrayIcon trayIcon = platformService.getTrayIcon(primaryStage);

            // update tooltip when the statusbinder changes status
            statusBinder.message().addListener((observable, oldValue, newValue) -> {
                if (newValue != null && !newValue.isEmpty()) {
                    trayIcon.setToolTip("Status: " + newValue);
                }
            });

            final SystemTray systemTray = SystemTray.getSystemTray();
            systemTray.add(trayIcon);

        } catch (PlatformNotSupportedException | AWTException e) {
            LOG.warn("Could not instantiate tray icon. Reverting to default behaviour");
            primaryStage.setOnCloseRequest(event -> Platform.exit());
        }
    }

}
