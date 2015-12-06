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

package ninja.eivind.hotsreplayuploader.window;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import ninja.eivind.hotsreplayuploader.di.JavaFXController;
import ninja.eivind.hotsreplayuploader.models.Account;
import ninja.eivind.hotsreplayuploader.models.LeaderboardRanking;
import ninja.eivind.hotsreplayuploader.models.ReplayFile;
import ninja.eivind.hotsreplayuploader.models.stringconverters.AccountConverter;
import ninja.eivind.hotsreplayuploader.models.stringconverters.HeroConverter;
import ninja.eivind.hotsreplayuploader.models.stringconverters.StatusBinder;
import ninja.eivind.hotsreplayuploader.providers.hotslogs.HotSLogsHero;
import ninja.eivind.hotsreplayuploader.providers.hotslogs.HotsLogsProvider;
import ninja.eivind.hotsreplayuploader.scene.control.CustomListCellFactory;
import ninja.eivind.hotsreplayuploader.services.AccountService;
import ninja.eivind.hotsreplayuploader.services.HeroService;
import ninja.eivind.hotsreplayuploader.services.UploaderService;
import ninja.eivind.hotsreplayuploader.services.platform.PlatformService;
import ninja.eivind.hotsreplayuploader.utils.FXUtils;
import ninja.eivind.hotsreplayuploader.utils.ReplayFileComparator;
import ninja.eivind.hotsreplayuploader.utils.SimpleHttpClient;
import ninja.eivind.hotsreplayuploader.versions.GitHubRelease;
import ninja.eivind.hotsreplayuploader.versions.ReleaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;
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
    @FXML
    private ListView<ReplayFile> newReplaysView;
    @FXML
    private Label status;
    @FXML
    private Label qmMmr;
    @FXML
    private Label hlMmr;
    @FXML
    private Label tlMmr;
    @FXML
    private ImageView logo;
    @FXML
    private Button playerSearch;
    @FXML
    private TextField playerSearchInput;
    @FXML
    private Button viewProfile;
    @FXML
    private ComboBox<Account> accountSelect;
    @FXML
    private Button lookupHero;
    @FXML
    private ComboBox<HotSLogsHero> heroName;
    @FXML
    private Label newReplaysCount;
    @FXML
    private Label uploadedReplays;

    @Inject
    private UploaderService uploaderService;
    @Inject
    private PlatformService platformService;
    @Inject
    private ReleaseManager releaseManager;
    @Inject
    private AccountService accountService;
    @Inject
    private StatusBinder statusBinder;
    @Inject
    private HeroService heroService;


    @Override
    public void initialize() {
        logo.setOnMouseClicked(event -> doOpenHotsLogs());
        fetchHeroNames();
        setPlayerSearchActions();
        bindList();
        setupFileHandler();
        if (uploaderService.isIdle()) {
            setIdle();
        }

        status.textProperty().bind(statusBinder.message());
        setupAccounts();

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
                ifPresent(version -> displayUpdateMessage(version)));
        new Thread(task).start();
    }

    private void displayUpdateMessage(final GitHubRelease newerVersionIfAny) {
        newVersionLabel.setText(newerVersionIfAny.getTagName());
        updateLink.setOnMouseClicked(value -> safeBrowse(newerVersionIfAny.getHtmlUrl()));
        updatePane.setVisible(true);
    }

    private void fetchHeroNames() {
        heroName.converterProperty().setValue(new HeroConverter());
        FXUtils.autoCompleteComboBox(heroName, FXUtils.AutoCompleteMode.STARTS_WITH);
        heroService.setOnSucceeded(event -> {
            if (null != heroService.getValue()) {
                heroName.getItems().setAll(heroService.getValue());
                LOG.info("Replaced list of heroes.");
            }
        });
        heroService.start();
    }

    private void safeBrowse(final String url) {
        try {
            platformService.browse(SimpleHttpClient.encode(url));
        } catch (IOException e) {
            LOG.error("Could not open " + url + " in browser.", e);
        }
    }

    private void doOpenHotsLogs() {
        safeBrowse("https://www.hotslogs.com/Default");
    }

    private void setPlayerSearchActions() {
        playerSearchInput.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                doPlayerSearch();
            }
        });
    }

    @FXML
    private void doLookupHero() {
        final HotSLogsHero hero = this.heroName.getValue();
        if (hero == null) {
            return;
        }
        final String heroName = hero.getPrimaryName();
        final String url = "https://www.hotslogs.com/Sitewide/HeroDetails?Hero=" + heroName;
        if (heroName.equals("")) {
            return;
        } else {
            this.heroName.setValue(null);
        }

        safeBrowse(url);
    }

    @FXML
    private void doPlayerSearch() {
        final String playerName = playerSearchInput.getText().replaceAll(" ", "");
        final String url = "https://www.hotslogs.com/PlayerSearch?Name=" + playerName;
        if (playerName.equals("")) {
            return;
        } else {
            playerSearchInput.setText("");
        }

        safeBrowse(url);
    }

    @FXML
    private void doViewProfile() {
        final Account account = accountSelect.getValue();
        if (account == null) {
            return;
        }

        String url = "https://www.hotslogs.com/Player/Profile?PlayerID=" + account.getPlayerId();
        safeBrowse(url);
    }

    private void setupAccounts() {
        accountSelect.converterProperty().setValue(new AccountConverter());
        accountSelect.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.intValue() != -1) {
                updateAccountView(accountSelect.getItems().get(newValue.intValue()));
                viewProfile.setDisable(false);
            }
        });

        accountService.setOnSucceeded(event -> updatePlayers(accountService.getValue()));
        accountService.start();
    }

    private void updateAccountView(final Account account) {
        if (account == null) {
            return;
        }

        qmMmr.setText(readMmr(account.getLeaderboardRankings(), "QuickMatch"));
        hlMmr.setText(readMmr(account.getLeaderboardRankings(), "HeroLeague"));
        tlMmr.setText(readMmr(account.getLeaderboardRankings(), "TeamLeague"));
    }

    private String readMmr(final List<LeaderboardRanking> leaderboardRankings, final String mode) {
        final String ifNotPresent = "N/A";
        return leaderboardRankings.stream()
                .filter(ranking -> ranking.getGameMode().equals(mode))
                .map(LeaderboardRanking::getCurrentMmr)
                .map(i -> Integer.toString(i))
                .findAny().orElse(ifNotPresent);
    }

    private void updatePlayers(final List<Account> newAccounts) {
        accountSelect.getItems().setAll(newAccounts);

        if (!accountSelect.getItems().isEmpty()) {
            Account reference = Optional.ofNullable(accountSelect.getValue()).orElse(newAccounts.get(0));
            accountSelect.getItems().stream()
                    .filter(account -> account.getPlayerId().equals(reference.getPlayerId()))
                    .findFirst().ifPresent(acc -> accountSelect.setValue(acc));
        }
    }

    private void setupFileHandler() {
        uploaderService.setRestartOnFailure(true);
        uploaderService.setOnSucceeded(event -> {
            if (HotsLogsProvider.isMaintenance()) {
                setMaintenance();
            } else if (uploaderService.isIdle()) {
                setIdle();
            } else {
                setUploading();
            }
        });
        uploaderService.setOnFailed(event -> setError());
        uploaderService.start();
    }

    private void bindList() {
        final ObservableList<ReplayFile> files = uploaderService.getFiles();
        newReplaysCount.setText(String.valueOf(files.size()));
        files.addListener((ListChangeListener<ReplayFile>) c -> newReplaysCount.setText(String.valueOf(files.size())));
        newReplaysView.setItems(files.sorted(new ReplayFileComparator()));
        newReplaysView.setCellFactory(new CustomListCellFactory(uploaderService));

        uploadedReplays.textProperty().bind(uploaderService.getUploadedCount());
    }

    private void setStatus(final String description, final Paint color) {
        statusBinder.message().setValue(description);
        status.textFillProperty().setValue(Paint.valueOf("#38d3ff"));
    }

    private void setIdle() {
        setStatus("Idle", Paint.valueOf("#38d3ff"));
    }

    private void setMaintenance() {
        setStatus("Maintenance", Paint.valueOf("#FF0000"));
    }

    private void setUploading() {
        setStatus("Uploading", Paint.valueOf("#00B000"));
    }

    private void setError() {
        setStatus("Connection error", Paint.valueOf("#FF0000"));
    }
}
