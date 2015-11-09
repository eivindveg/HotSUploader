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
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

public class ClientPreloader extends Preloader {

    private static final Logger LOG = LoggerFactory.getLogger(ClientPreloader.class);
    private Stage preloaderStage;

    @Override
    public void init() {

    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        LOG.info("Preloading application");
        primaryStage.initStyle(StageStyle.UNDECORATED);
        InputStream logo = getClass().getResourceAsStream("/images/hotsreplayuploader-large.png");
        ImageView image = new ImageView(new Image(logo));
        this.preloaderStage = primaryStage;

        VBox loading = new VBox(20);
        loading.setPrefWidth(Region.USE_COMPUTED_SIZE);
        ProgressBar progress = new ProgressBar();
        progress.setPrefWidth(Double.MAX_VALUE);
        loading.getChildren().add(progress);

        BorderPane root = new BorderPane(image, null, null, loading, null);
        root.getStylesheets().add(getClass().getResource("/styles/window.css").toString());
        root.getStyleClass().add("header");
        Scene scene = new Scene(root);

        primaryStage.setWidth(600);
        primaryStage.setHeight(300);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void handleStateChangeNotification(StateChangeNotification stateChangeNotification) {
        if (stateChangeNotification.getType() == StateChangeNotification.Type.BEFORE_START) {
            preloaderStage.hide();
            preloaderStage = null;
        }
    }
}
