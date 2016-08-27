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

package ninja.eivind.hotsreplayuploader.window;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import ninja.eivind.hotsreplayuploader.di.JavaFXController;
import ninja.eivind.hotsreplayuploader.services.platform.PlatformService;
import ninja.eivind.hotsreplayuploader.utils.SimpleHttpClient;
import ninja.eivind.hotsreplayuploader.versions.GitHubRelease;
import ninja.eivind.hotsreplayuploader.versions.ReleaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Optional;

/**
 * Window controller for the application. Contains references to all the components exposed to the user. Sets up events
 * and services to handle almost everything that happens in this application.
 */
public class HomeController implements JavaFXController {

    private static final Logger LOG = LoggerFactory.getLogger(HomeController.class);

    @FXML
    private VBox updatePane;
    @FXML
    private Label newVersionLabel;
    @FXML
    private Hyperlink updateLink;

    @Autowired
    private PlatformService platformService;
    @Autowired
    private ReleaseManager releaseManager;


    @Override
    public void afterPropertiesSet() {
        checkNewVersion();
    }

    private void checkNewVersion() {
        final Task<Optional<GitHubRelease>> task = new Task<Optional<GitHubRelease>>() {
            @Override
            protected Optional<GitHubRelease> call() throws Exception {
                return releaseManager.getNewerVersionIfAny();
            }
        };
        task.setOnSucceeded(event -> task.getValue().
                ifPresent(this::displayUpdateMessage));
        new Thread(task).start();
    }

    private void displayUpdateMessage(final GitHubRelease newerVersionIfAny) {
        newVersionLabel.setText(newerVersionIfAny.getTagName());
        updateLink.setOnMouseClicked(value -> platformService.browse(newerVersionIfAny.getHtmlUrl()));
        updatePane.setVisible(true);
    }

}
