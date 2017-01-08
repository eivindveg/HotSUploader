// Copyright 2015-2016 Eivind Vegsundvåg
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

import com.sun.javafx.application.LauncherImpl;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import ninja.eivind.hotsreplayuploader.models.stringconverters.StatusBinder;
import ninja.eivind.hotsreplayuploader.preloader.ClientPreloader;
import ninja.eivind.hotsreplayuploader.preloader.ProgressMonitor;
import ninja.eivind.hotsreplayuploader.services.platform.PlatformNotSupportedException;
import ninja.eivind.hotsreplayuploader.services.platform.PlatformService;
import ninja.eivind.hotsreplayuploader.services.platform.PlatformServiceFactoryBean;
import ninja.eivind.hotsreplayuploader.utils.Constants;
import ninja.eivind.hotsreplayuploader.versions.ReleaseManager;
import ninja.eivind.hotsreplayuploader.window.builder.SceneBuilderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.*;

import java.awt.*;
import java.net.URL;

/**
 * Application entry point. Sets up the actions that connect to the underlying platform.
 */
@SpringBootApplication
public class Client extends Application implements ApplicationContextAware {

    private static final Logger LOG = LoggerFactory.getLogger(Client.class);

    private static String[] launchArgs;

    @Autowired
    private ReleaseManager releaseManager;
    @Autowired
    private PlatformService platformService;
    @Autowired
    private StatusBinder statusBinder;

    @Autowired
    private SceneBuilderFactory sceneBuilderFactory;

    private static boolean preloaderSupported;

    private ConfigurableApplicationContext context;

    public static void main(String... args) throws Exception {
        launchArgs = args;
        PlatformService platformService = new PlatformServiceFactoryBean().getObject();
        preloaderSupported = platformService.isPreloaderSupported();
        if (preloaderSupported) {
            LOG.info("Launching with preloader.");
            LauncherImpl.launchApplication(Client.class, ClientPreloader.class, args);
        } else {
            LOG.info("Launching without preloader.");
            launch(Client.class, args);
        }
    }


    @Override
    public void stop() throws Exception {
        context.close();
        super.stop();
        System.exit(0);
    }


    @Override
    public void init() {
        SpringApplicationBuilder builder = new SpringApplicationBuilder(Client.class);
        if(preloaderSupported) {
            builder.initializers(new ProgressMonitor(this::notifyPreloader));
        }
        context = builder.headless(false).run(launchArgs);
        context.getAutowireCapableBeanFactory().autowireBean(this);

        //add a shutdown hook to be really sure, resources are closed properly
        Runtime.getRuntime().addShutdownHook(new Thread(context::close));
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

            Scene scene = sceneBuilderFactory.builder()
                    .setLocation("/ninja/eivind/hotsreplayuploader/window/Home.fxml")
                    .build();


            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            LOG.error("Failed to start", e);
            throw e;
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

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = (ConfigurableApplicationContext) applicationContext;
    }
}
