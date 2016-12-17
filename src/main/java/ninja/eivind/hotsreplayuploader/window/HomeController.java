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

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.BuilderFactory;
import ninja.eivind.hotsreplayuploader.di.FXMLLoaderFactory;
import ninja.eivind.hotsreplayuploader.di.JavaFXController;
import ninja.eivind.hotsreplayuploader.di.nodes.JavaFXNode;
import ninja.eivind.hotsreplayuploader.files.AccountDirectoryWatcher;
import ninja.eivind.hotsreplayuploader.files.tempwatcher.BattleLobbyWatcher;
import ninja.eivind.hotsreplayuploader.files.tempwatcher.TempWatcher;
import ninja.eivind.hotsreplayuploader.services.platform.PlatformService;
import ninja.eivind.hotsreplayuploader.versions.GitHubRelease;
import ninja.eivind.hotsreplayuploader.versions.ReleaseManager;
import ninja.eivind.hotsreplayuploader.window.nodes.BattleLobbyNode;
import ninja.eivind.hotsreplayuploader.window.nodes.UploaderNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

/**
 * Window controller for the application. Contains references to all the components exposed to the user. Sets up notifications
 * and services to handle almost everything that happens in this application.
 */
public class HomeController implements JavaFXController, InitializingBean {

    private static final Logger LOG = LoggerFactory.getLogger(HomeController.class);

    @FXML
    private VBox updatePane;
    @FXML
    private Label newVersionLabel;
    @FXML
    private Hyperlink updateLink;

    @FXML
    private Pane nodeHolder;

    @Autowired
    private PlatformService platformService;
    @Autowired
    private ReleaseManager releaseManager;
    @Autowired
    private BuilderFactory builderFactory;
    @Autowired
    private TempWatcher lobbyWatcher;
    @Autowired
    private AccountDirectoryWatcher accountWatcher;
    private JavaFXNode currentContext;

    private UploaderNode uploaderNode;
    private BattleLobbyNode battleLobbyNode;


    @Override
    public void initialize() {
        LOG.info("Initializing HomeController");

        checkNewVersion();

        currentContext = loadInitialContext();
        uploaderNode = (UploaderNode) currentContext;
        LOG.info("Initialized HomeController");
    }

    private JavaFXNode loadInitialContext() {
        Node node = nodeHolder.getChildren().get(0);
        return (JavaFXNode) node;
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

    public void switchToUploaderView() {
        Platform.runLater(() -> {
            if (uploaderNode == null) {
                uploaderNode = (UploaderNode) builderFactory.getBuilder(UploaderNode.class).build();
            }
            if (currentContext == uploaderNode) {
                return;
            }
            uploaderNode.activate();

            nodeHolder.getChildren().clear();
            nodeHolder.getChildren().add(uploaderNode);

        });
    }

    public void switchToBattleLobbyView(File file) {
        Platform.runLater(() -> {
            if (battleLobbyNode == null) {
                battleLobbyNode = (BattleLobbyNode) builderFactory.getBuilder(BattleLobbyNode.class).build();
            }
            uploaderNode.passivate();
            battleLobbyNode.setFile(file);

            LOG.info("Setting battle lobby node!");
            nodeHolder.getChildren().clear();
            nodeHolder.getChildren().add(battleLobbyNode);

            currentContext = battleLobbyNode;
        });

    }

    private void displayUpdateMessage(final GitHubRelease newerVersionIfAny) {
        newVersionLabel.setText(newerVersionIfAny.getTagName());
        updateLink.setOnMouseClicked(value -> platformService.browse(newerVersionIfAny.getHtmlUrl()));
        updatePane.setVisible(true);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        lobbyWatcher.setCallback(this::switchToBattleLobbyView);
        accountWatcher.addFileListener(file -> switchToUploaderView());
        lobbyWatcher.start();
    }
}
