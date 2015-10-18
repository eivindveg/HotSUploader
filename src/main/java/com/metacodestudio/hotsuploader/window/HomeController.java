package com.metacodestudio.hotsuploader.window;

import com.metacodestudio.hotsuploader.AccountService;
import com.metacodestudio.hotsuploader.concurrent.tasks.HeroListTask;
import com.metacodestudio.hotsuploader.files.FileHandler;
import com.metacodestudio.hotsuploader.models.*;
import com.metacodestudio.hotsuploader.models.stringconverters.HeroConverter;
import com.metacodestudio.hotsuploader.providers.HotsLogsProvider;
import com.metacodestudio.hotsuploader.scene.control.ExceptionListCellFactory;
import com.metacodestudio.hotsuploader.services.HeroService;
import com.metacodestudio.hotsuploader.utils.DesktopWrapper;
import com.metacodestudio.hotsuploader.utils.FXUtils;
import com.metacodestudio.hotsuploader.utils.SimpleHttpClient;
import com.metacodestudio.hotsuploader.utils.StormHandler;
import com.metacodestudio.hotsuploader.versions.GitHubRelease;
import com.metacodestudio.hotsuploader.versions.ReleaseManager;
import io.datafx.controller.ViewController;
import io.datafx.controller.flow.action.ActionMethod;
import io.datafx.controller.flow.action.ActionTrigger;
import io.datafx.controller.flow.context.FXMLViewFlowContext;
import io.datafx.controller.flow.context.ViewFlowContext;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Paint;
import javafx.util.Callback;
import javafx.util.Duration;
import javafx.util.StringConverter;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ViewController(value = "Home.fxml", title = "HotSLogs UploaderFX")
public class HomeController {

    private SimpleHttpClient httpClient;
    @FXMLViewFlowContext
    private ViewFlowContext viewFlowContext;

    @FXML
    private Accordion accordion;

    @FXML
    private BorderPane updatePane;
    @FXML
    private Label newVersionLabel;
    @FXML
    private Hyperlink updateLink;

    @FXML
    private TitledPane newReplaysTitlePane;
    @FXML
    private TitledPane uploadedReplaysTitlePane;
    @FXML
    private TitledPane exceptionReplaysTitlePane;
    @FXML
    private TitledPane botReplaysTitlePane;

    @FXML
    private ListView<ReplayFile> newReplaysView;

    @FXML
    private ListView<ReplayFile> exceptionReplaysView;

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
    @ActionTrigger("playerSearch")
    private Button playerSearch;

    @FXML
    private TextField playerSearchInput;

    @FXML
    @ActionTrigger("viewProfile")
    private Button viewProfile;

    @FXML
    private ChoiceBox<Account> accountSelect;

    @FXML
    @ActionTrigger("lookupHero")
    private Button lookupHero;

    @FXML
    private ComboBox<Hero> heroName;

    @FXML
    @ActionTrigger("invalidateExceptions")
    private Button invalidateExceptions;

    private FileHandler fileHandler;
    private DesktopWrapper desktop;
    private StormHandler stormHandler;
    @FXML
    private Label uploadedReplays;


    @PostConstruct
    public void init() {
        desktop = new DesktopWrapper();
        stormHandler = viewFlowContext.getRegisteredObject(StormHandler.class);
        httpClient = viewFlowContext.getRegisteredObject(SimpleHttpClient.class);
        fileHandler = viewFlowContext.getRegisteredObject(FileHandler.class);
        fileHandler.verifyMap(fileHandler.getFileMap());
        logo.setOnMouseClicked(event -> doOpenHotsLogs());
        fetchHeroNames();
        prepareAccordion();
        setPlayerSearchActions();
        bindLists();
        setupFileHandler();
        if (fileHandler.isIdle()) {
            setIdle();
        }


        setupAccounts();

        checkNewVersion();
        fileHandler.beginWatch();
    }

    private void checkNewVersion() {
        ReleaseManager releaseManager = viewFlowContext.getRegisteredObject(ReleaseManager.class);
        Task<GitHubRelease> task = new Task<GitHubRelease>() {
            @Override
            protected GitHubRelease call() throws Exception {
                return releaseManager.getNewerVersionIfAny();
            }
        };
        task.setOnSucceeded(event -> {
            GitHubRelease newerVersionIfAny = task.getValue();
            if (newerVersionIfAny != null) {
                displayUpdateMessage(newerVersionIfAny);
            }
        });
        new Thread(task).start();
    }

    private void displayUpdateMessage(final GitHubRelease newerVersionIfAny) {
        newVersionLabel.setText(newerVersionIfAny.getTagName());
        updateLink.setOnMouseClicked(value -> {
            try {
                desktop.browse(SimpleHttpClient.encode(newerVersionIfAny.getHtmlUrl()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        updatePane.setVisible(true);
    }

    private void fetchHeroNames() {
        heroName.converterProperty().setValue(new HeroConverter());
        FXUtils.autoCompleteComboBox(heroName, FXUtils.AutoCompleteMode.STARTS_WITH);
        HeroService heroService = new HeroService(httpClient);
        heroService.setOnSucceeded(event -> {
            if (null != heroService.getValue()) {
                heroName.getItems().setAll(heroService.getValue());
            }
        });
        heroService.start();
    }

    private void doOpenHotsLogs() {
        try {
            desktop.browse(SimpleHttpClient.encode("https://www.hotslogs.com/Default"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void setPlayerSearchActions() {
        playerSearchInput.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                try {
                    doPlayerSearch();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void prepareAccordion() {
        TitledPane defaultPane = newReplaysTitlePane;
        accordion.setExpandedPane(defaultPane);
        defaultPane.setCollapsible(false);
        accordion.expandedPaneProperty().addListener((property, oldPane, newPane) -> {
            if (oldPane != null) oldPane.setCollapsible(true);
            if (newPane != null) Platform.runLater(() -> newPane.setCollapsible(false));
        });
    }

    @ActionMethod("lookupHero")
    private void doLookupHero() throws IOException {
        Hero hero = this.heroName.getValue();
        if (hero == null) {
            return;
        }
        String heroName = hero.getPrimaryName();
        if (heroName.equals("")) {
            return;
        } else {
            this.heroName.setValue(null);
        }
        desktop.browse(SimpleHttpClient.encode("https://www.hotslogs.com/Sitewide/HeroDetails?Hero=" + heroName));
    }

    @ActionMethod("playerSearch")
    private void doPlayerSearch() throws IOException {
        String playerName = playerSearchInput.getText().replaceAll(" ", "");
        if (playerName.equals("")) {
            return;
        } else {
            playerSearchInput.setText("");
        }
        desktop.browse(SimpleHttpClient.encode("https://www.hotslogs.com/PlayerSearch?Name=" + playerName));
    }

    @ActionMethod("viewProfile")
    private void doViewProfile() throws IOException {
        Account account = accountSelect.getValue();
        if (account == null) {
            return;
        }
        desktop.browse(SimpleHttpClient.encode("https://www.hotslogs.com/Player/Profile?PlayerID=" + account.getPlayerId()));
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
        ScheduledService<List<Account>> service = new AccountService(stormHandler, httpClient);
        service.setDelay(Duration.ZERO);
        service.setPeriod(Duration.minutes(10));

        service.setOnSucceeded(event -> updatePlayers(service.getValue()));
        service.start();
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
            Optional<Account> optionalAccount = accountSelect.getItems()
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


    @ActionMethod("invalidateExceptions")
    private void doInvalidateExceptions() {
        fileHandler.invalidateByStatus(Status.EXCEPTION);
        setUploading();
    }

    private void setupFileHandler() {
        fileHandler.setRestartOnFailure(true);
        fileHandler.setOnSucceeded(event -> {
            if (HotsLogsProvider.isMaintenance()) {
                setMaintenance();
            } else if (fileHandler.isIdle()) {
                setIdle();
            } else {
                setUploading();
            }
        });
        fileHandler.setOnFailed(event -> setError());
        fileHandler.start();
    }

    private void bindLists() {
        Map<Status, ObservableList<ReplayFile>> fileMap = fileHandler.getFileMap();

        final String newReplaysTitle = newReplaysTitlePane.textProperty().get();
        final ObservableList<ReplayFile> newReplays = fileMap.get(Status.NEW);
        newReplays.addListener((ListChangeListener<ReplayFile>) c -> updatePaneTitle(newReplaysTitlePane, newReplaysTitle, newReplays));
        newReplaysView.setItems(newReplays);

        uploadedReplays.textProperty().bind(fileHandler.getUploadedCount());

        final String exceptionReplaysTitle = exceptionReplaysTitlePane.textProperty().get();
        final ObservableList<ReplayFile> exceptionReplays = fileMap.get(Status.EXCEPTION);
        exceptionReplays.addListener((ListChangeListener<ReplayFile>) c -> updatePaneTitle(exceptionReplaysTitlePane, exceptionReplaysTitle, exceptionReplays));
        exceptionReplaysView.setItems(exceptionReplays);
        exceptionReplaysView.setCellFactory(new ExceptionListCellFactory(fileHandler));

        updatePaneTitle(newReplaysTitlePane, newReplaysTitle, newReplays);
        updatePaneTitle(exceptionReplaysTitlePane, exceptionReplaysTitle, exceptionReplays);
    }

    private void updatePaneTitle(final TitledPane pane, final String baseTitle, final ObservableList<ReplayFile> list) {
        pane.setText(baseTitle + " (" + list.size() + ")");
    }

    private void setIdle() {
        status.setText("Idle");
        status.textFillProperty().setValue(Paint.valueOf("#0099DA"));
    }

    private void setMaintenance() {
        status.setText("Maintenance");
        status.textFillProperty().setValue(Paint.valueOf("#FF0000"));
    }

    private void setUploading() {
        status.setText("Uploading");
        status.textFillProperty().setValue(Paint.valueOf("#00B000"));
    }

    private void setError() {
        status.setText("Connection error");
        status.textFillProperty().setValue(Paint.valueOf("#FF0000"));
    }

}
