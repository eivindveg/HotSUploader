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
import javafx.util.StringConverter;
import ninja.eivind.hotsreplayuploader.di.JavaFXController;
import ninja.eivind.hotsreplayuploader.models.Account;
import ninja.eivind.hotsreplayuploader.models.LeaderboardRanking;
import ninja.eivind.hotsreplayuploader.models.ReplayFile;
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
        task.setOnSucceeded(event -> {
            final Optional<GitHubRelease> newerVersionIfAny = task.getValue();
            if (newerVersionIfAny.isPresent()) {
                displayUpdateMessage(newerVersionIfAny.get());
            }
        });
        new Thread(task).start();
    }

    private void displayUpdateMessage(final GitHubRelease newerVersionIfAny) {
        newVersionLabel.setText(newerVersionIfAny.getTagName());
        updateLink.setOnMouseClicked(value -> {
            final String htmlUrl = newerVersionIfAny.getHtmlUrl();
            try {
                platformService.browse(SimpleHttpClient.encode(htmlUrl));
            } catch (IOException e) {
                handleConnectionException(e, htmlUrl);
            }
        });
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

    private void handleConnectionException(IOException e, String url) {
        LOG.error("Could not open " + url + " in browser.", e);
    }

    private void doOpenHotsLogs() {
        final String url = "https://www.hotslogs.com/Default";
        try {
            platformService.browse(SimpleHttpClient.encode(url));
        } catch (IOException e) {
            handleConnectionException(e, url);
        }
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
        try {
            platformService.browse(SimpleHttpClient.encode(url));
        } catch (IOException e) {
            handleConnectionException(e, url);
        }
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
        try {
            platformService.browse(SimpleHttpClient.encode(url));
        } catch (IOException e) {
            handleConnectionException(e, url);
        }
    }

    @FXML
    private void doViewProfile() {
        final Account account = accountSelect.getValue();
        if (account == null) {
            return;
        }
        final String url = "https://www.hotslogs.com/Player/Profile?PlayerID=" + account.getPlayerId();
        try {
            platformService.browse(SimpleHttpClient.encode(url));
        } catch (IOException e) {
            handleConnectionException(e, url);
        }
    }

    private void setupAccounts() {
        accountSelect.converterProperty().setValue(new StringConverter<Account>() {
            @Override
            public String toString(final Account object) {
                if (object == null) {
                    return "";
                }
                return object.getName();
            }

            @Override
            public Account fromString(final String string) {
                return null;
            }
        });
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
        final String ifNotPresent = "N/A";
        if (account == null) {
            return;
        }

        final Optional<Integer> quickMatchMmr = readMmr(account.getLeaderboardRankings(), "QuickMatch");
        applyToLabel(quickMatchMmr, qmMmr, ifNotPresent);

        final Optional<Integer> heroLeagueMmr = readMmr(account.getLeaderboardRankings(), "HeroLeague");
        applyToLabel(heroLeagueMmr, hlMmr, ifNotPresent);

        final Optional<Integer> teamLeagueMmr = readMmr(account.getLeaderboardRankings(), "TeamLeague");
        applyToLabel(teamLeagueMmr, tlMmr, ifNotPresent);
    }

    private Optional<Integer> readMmr(final List<LeaderboardRanking> leaderboardRankings, final String mode) {
        return leaderboardRankings.stream()
                .filter(ranking -> ranking.getGameMode().equals(mode))
                .map(LeaderboardRanking::getCurrentMmr)
                .findAny();
    }

    private void applyToLabel(final Optional<?> value, final Label applyTo, final String ifNotPresent) {
        if (value.isPresent()) {
            applyTo.setText(String.valueOf(value.get()));
        } else {
            applyTo.setText(ifNotPresent);
        }
    }

    private void updatePlayers(final List<Account> newAccounts) {
        Account reference = null;
        if (!accountSelect.getItems().isEmpty()) {
            reference = accountSelect.getValue();
        }

        accountSelect.getItems().setAll(newAccounts);
        if (reference != null) {
            final Account finalReference = reference;
            final Optional<Account> optionalAccount = accountSelect.getItems()
                    .stream()
                    .filter(account -> account.getPlayerId().equals(finalReference.getPlayerId()))
                    .findFirst();
            if (optionalAccount.isPresent()) {
                accountSelect.setValue(optionalAccount.get());
            }
        } else if (!newAccounts.isEmpty()) {
            accountSelect.setValue(newAccounts.get(0));
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

    private void setIdle() {
        final String idle = "Idle";
        statusBinder.message().setValue(idle);
        status.textFillProperty().setValue(Paint.valueOf("#38d3ff"));
    }

    private void setMaintenance() {
        final String maintenance = "Maintenance";
        statusBinder.message().setValue(maintenance);
        status.textFillProperty().setValue(Paint.valueOf("#FF0000"));
    }

    private void setUploading() {
        final String uploading = "Uploading";
        statusBinder.message().setValue(uploading);
        status.textFillProperty().setValue(Paint.valueOf("#00B000"));
    }

    private void setError() {
        final String connectionError = "Connection error";
        statusBinder.message().setValue(connectionError);
        status.textFillProperty().setValue(Paint.valueOf("#FF0000"));
    }

}
