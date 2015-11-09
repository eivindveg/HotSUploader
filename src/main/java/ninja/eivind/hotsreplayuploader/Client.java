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
import com.gluonhq.ignite.guice.GuiceContext;
import com.sun.javafx.application.LauncherImpl;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import ninja.eivind.hotsreplayuploader.di.GuiceModule;
import ninja.eivind.hotsreplayuploader.models.stringconverters.StatusBinder;
import ninja.eivind.hotsreplayuploader.services.platform.PlatformNotSupportedException;
import ninja.eivind.hotsreplayuploader.services.platform.PlatformService;
import ninja.eivind.hotsreplayuploader.versions.ReleaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ninja.eivind.hotsreplayuploader.utils.Constants;

import javax.inject.Inject;
import java.awt.*;
import java.net.URL;
import java.util.Collections;

public class Client extends Application {

    private static final Logger LOG = LoggerFactory.getLogger(Client.class);
    private DIContext context = new GuiceContext(this, () -> Collections.singletonList(new GuiceModule()));
    @Inject
    private FXMLLoader fxmlLoader;
    @Inject
    private ReleaseManager releaseManager;
    @Inject
    private PlatformService platformService;
    @Inject
    private StatusBinder statusBinder;

    public static void main(String[] args) {
        LauncherImpl.launchApplication(Client.class, ClientPreloader.class, args);
    }

    @Override
    public void init() {
        context.init();
    }

    @Override
    public void start(final Stage primaryStage) throws Exception {
        URL logo = platformService.getLogoUrl();
        Image image = new Image(logo.toString());
        primaryStage.getIcons().add(image);
        primaryStage.setResizable(false);
        addToTray(primaryStage);

        // Set window title
        String windowTitle = Constants.APPLICATION_NAME + " v" + releaseManager.getCurrentVersion();
        primaryStage.setTitle(windowTitle);

        fxmlLoader.setLocation(getClass().getResource("/ninja/eivind/hotsreplayuploader/window/Home.fxml"));
        Parent root = fxmlLoader.load();

        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    private void addToTray(final Stage primaryStage) {
        try {
            TrayIcon trayIcon = platformService.getTrayIcon(primaryStage);
            SystemTray systemTray = SystemTray.getSystemTray();
            systemTray.add(trayIcon);
            statusBinder.message().addListener((observable, oldValue, newValue) -> {
                if (newValue != null && !newValue.isEmpty()) {
                    trayIcon.setToolTip("Status: " + newValue);
                }
            });
        } catch (PlatformNotSupportedException | AWTException e) {
            LOG.warn("Could not instantiate tray icon. Reverting to default behaviour", e);
            primaryStage.setOnCloseRequest(event -> System.exit(0));
        }
    }

}
