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

package ninja.eivind.hotsreplayuploader.preloader;

import javafx.application.Preloader;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Labeled;
import javafx.scene.control.ProgressIndicator;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import ninja.eivind.hotsreplayuploader.Client;
import ninja.eivind.hotsreplayuploader.preloader.notifications.BeanLoadedNotification;
import ninja.eivind.hotsreplayuploader.preloader.notifications.BeanNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application preloader. This displays a splash screen while {@link Client} is loading.
 */
public class ClientPreloader extends Preloader {

    private static final Logger LOG = LoggerFactory.getLogger(ClientPreloader.class);
    private Stage preloaderStage;
    private Parent root;
    private Labeled messageBox;
    private ProgressIndicator progress;

    @Override
    public void handleApplicationNotification(PreloaderNotification info) {
        super.handleApplicationNotification(info);
        if (info instanceof BeanNotification) {
            BeanNotification beanNotification = (BeanNotification) info;
            final String beanName = beanNotification.getBeanName();
            if (beanNotification instanceof BeanLoadedNotification) {
                messageBox.setText("Loaded bean " + beanName);
                double progress = ((BeanLoadedNotification) beanNotification).getProgress();
                this.progress.setProgress(progress);
            } else {
                messageBox.setText("Loading bean " + beanName);
            }
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        preloaderStage = primaryStage;
        LOG.info("Preloading application");
        primaryStage.initStyle(StageStyle.UNDECORATED);

        root = FXMLLoader.load(getClass().getResource("Preloader.fxml"));
        messageBox = (Labeled) root.lookup("#message");
        progress = (ProgressIndicator) root.lookup("#progress");

        final Scene scene = new Scene(root);

        primaryStage.setScene(scene);
        primaryStage.sizeToScene();
        primaryStage.show();
    }

    @Override
    public void handleStateChangeNotification(StateChangeNotification stateChangeNotification) {
        if (stateChangeNotification.getType() == StateChangeNotification.Type.BEFORE_START) {
            try {
                preloaderStage.close();
                preloaderStage = null;
                root = null;
            } catch (Exception e) {
                LOG.warn("Failed to stop preloader", e);
            }
        }
    }
}
