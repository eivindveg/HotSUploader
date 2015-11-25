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

import javafx.application.Preloader;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application preloader. This displays a splash screen while {@link Client} is loading.
 */
public class ClientPreloader extends Preloader {

    private static final Logger LOG = LoggerFactory.getLogger(ClientPreloader.class);
    private Stage preloaderStage;

    @Override
    public void init() {

    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        preloaderStage = primaryStage;
        LOG.info("Preloading application");
        primaryStage.initStyle(StageStyle.UNDECORATED);

        Parent root = FXMLLoader.load(getClass().getResource("window/Preloader.fxml"));

        Scene scene = new Scene(root);

        primaryStage.setScene(scene);
        primaryStage.sizeToScene();
        primaryStage.show();
    }

    @Override
    public void handleStateChangeNotification(StateChangeNotification stateChangeNotification) {
        if (stateChangeNotification.getType() == StateChangeNotification.Type.BEFORE_START) {
            try {
                preloaderStage.close();
            } catch (Exception e) {
                LOG.warn("Failed to stop preloader", e);
            }
        }
    }
}
