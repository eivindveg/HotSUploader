// Copyright 2016 Eivind Vegsundvåg
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

package ninja.eivind.hotsreplayuploader.window.nodes;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import ninja.eivind.hotsreplayuploader.di.FXMLLoaderFactory;
import ninja.eivind.hotsreplayuploader.di.nodes.JavaFXNode;
import ninja.eivind.hotsreplayuploader.models.Account;
import ninja.eivind.hotsreplayuploader.models.LeaderboardRanking;
import ninja.eivind.hotsreplayuploader.models.stringconverters.AccountConverter;
import ninja.eivind.hotsreplayuploader.models.stringconverters.HeroConverter;
import ninja.eivind.hotsreplayuploader.providers.hotslogs.HotSLogsHero;
import ninja.eivind.hotsreplayuploader.services.AccountService;
import ninja.eivind.hotsreplayuploader.services.HeroService;
import ninja.eivind.hotsreplayuploader.services.platform.PlatformService;
import ninja.eivind.hotsreplayuploader.utils.FXUtils;
import ninja.eivind.hotsreplayuploader.utils.SimpleHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;

public class HotsLogsNode extends VBox implements JavaFXNode {

    private static final Logger logger = LoggerFactory.getLogger(HotsLogsNode.class);
    @FXML
    private ComboBox<Account> accountSelect;

    @FXML
    private Button viewProfile;

    @FXML
    private ImageView logo;
    @FXML
    private Label qmMmr;
    @FXML
    private Label hlMmr;
    @FXML
    private Label tlMmr;

    @FXML
    private Button playerSearch;
    @FXML
    private TextField playerSearchInput;

    @FXML
    private Button lookupHero;
    @FXML
    private ComboBox<HotSLogsHero> heroName;

    @Autowired
    private PlatformService platformService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private HeroService heroService;

    public HotsLogsNode(FXMLLoaderFactory factory) throws IOException {
        URL resource = getClass().getResource("HotsLogsNode.fxml");
        FXMLLoader loader = factory.get();
        loader.setLocation(resource);
        loader.setRoot(this);
        loader.setController(this);
        loader.load();
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

    private void fetchHeroNames() {
        heroName.converterProperty().setValue(new HeroConverter());
        FXUtils.autoCompleteComboBox(heroName, FXUtils.AutoCompleteMode.STARTS_WITH);
        heroService.setOnSucceeded(event -> {
            if (null != heroService.getValue()) {
                heroName.getItems().setAll(heroService.getValue());
                logger.info("Replaced list of heroes.");
            }
        });
        heroService.start();
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

        platformService.browse(url);
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

        platformService.browse(url);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        setupAccounts();
        fetchHeroNames();
        setPlayerSearchActions();
        logo.setOnMouseClicked(event -> doOpenHotsLogs());
    }

    private void doOpenHotsLogs() {
        platformService.browse("https://www.hotslogs.com/Default");
    }

    private void setPlayerSearchActions() {
        playerSearchInput.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                doPlayerSearch();
            }
        });
    }

    private void updateAccountView(final Account account) {
        if (account == null) {
            return;
        }

        qmMmr.setText(readMmr(account.getLeaderboardRankings(), "QuickMatch"));
        hlMmr.setText(readMmr(account.getLeaderboardRankings(), "HeroLeague"));
        tlMmr.setText(readMmr(account.getLeaderboardRankings(), "TeamLeague"));
    }

    @FXML
    private void doViewProfile() {
        final Account account = accountSelect.getValue();
        if (account == null) {
            return;
        }

        String url = "https://www.hotslogs.com/Player/Profile?PlayerID=" + account.getPlayerId();
        platformService.browse(url);
    }

    private String readMmr(final List<LeaderboardRanking> leaderboardRankings, final String mode) {
        final String ifNotPresent = "N/A";
        return leaderboardRankings.stream()
                .filter(ranking -> ranking.getGameMode().equals(mode))
                .map(LeaderboardRanking::getCurrentMmr)
                .map(i -> Integer.toString(i))
                .findAny().orElse(ifNotPresent);
    }
}
