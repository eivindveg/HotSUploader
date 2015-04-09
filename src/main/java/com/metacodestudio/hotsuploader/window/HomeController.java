package com.metacodestudio.hotsuploader.window;

import com.metacodestudio.hotsuploader.AccountService;
import com.metacodestudio.hotsuploader.files.FileHandler;
import com.metacodestudio.hotsuploader.models.Account;
import com.metacodestudio.hotsuploader.models.LeaderboardRanking;
import com.metacodestudio.hotsuploader.models.ReplayFile;
import com.metacodestudio.hotsuploader.models.Status;
import com.metacodestudio.hotsuploader.providers.HotSLogs;
import io.datafx.controller.ViewController;
import io.datafx.controller.flow.action.ActionMethod;
import io.datafx.controller.flow.action.ActionTrigger;
import io.datafx.controller.flow.context.FXMLViewFlowContext;
import io.datafx.controller.flow.context.ViewFlowContext;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Paint;
import javafx.util.Duration;
import javafx.util.StringConverter;

import javax.annotation.PostConstruct;
import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ViewController(value = "Home.fxml", title = "HotSLogs UploaderFX")
public class HomeController {

    @FXMLViewFlowContext
    private ViewFlowContext viewFlowContext;

    @FXML
    private Accordion accordion;

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
    private ListView<ReplayFile> uploadedReplaysView;

    @FXML
    private ListView<ReplayFile> exceptionReplaysView;

    @FXML
    private ListView<ReplayFile> botReplaysView;

    @FXML
    private Label status;

    @FXML
    private Label qmMmr;

    @FXML
    private Label hlMmr;

    @FXML
    private Label tlMmr;

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
    @ActionTrigger("invalidateExceptions")
    private Button invalidateExceptions;

    private FileHandler fileHandler;


    @PostConstruct
    public void init() {
        fileHandler = viewFlowContext.getRegisteredObject(FileHandler.class);
        prepareAccordion();
        playerSearchInput.setOnKeyPressed(event -> {
            if(event.getCode() == KeyCode.ENTER) {
                try {
                    doPlayerSearch();
                } catch (IOException | URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        bindLists();
        setFileHandlerOnSucceeded();
        fileHandler.start();
        if (fileHandler.isIdle()) {
            setIdle();
        }

        setupAccounts();
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

    @ActionMethod("playerSearch")
    private void doPlayerSearch() throws IOException, URISyntaxException {
        Desktop desktop = Desktop.getDesktop();
        String playerName = playerSearchInput.getText().replaceAll(" ", "");
        if (playerName.equals("")) {
            return;
        } else {
            playerSearchInput.setText("");
        }
        desktop.browse(new URL("https://www.hotslogs.com/PlayerSearch?Name=" + playerName).toURI());
    }

    @ActionMethod("viewProfile")
    private void doViewProfile() throws IOException, URISyntaxException {
        Desktop desktop = Desktop.getDesktop();
        Account account = accountSelect.getValue();
        if (account == null) {
            return;
        }
        desktop.browse(new URL("https://www.hotslogs.com/Player/Profile?PlayerID=" + account.getPlayerId()).toURI());
    }

    private void setupAccounts() {
        accountSelect.converterProperty().setValue(new StringConverter<Account>() {
            @Override
            public String toString(final Account object) {
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
        ScheduledService<List<Account>> service = new AccountService();
        service.setDelay(Duration.ZERO);
        service.setPeriod(Duration.minutes(10));

        service.setOnSucceeded(event -> updatePlayers(service.getValue()));
        service.start();
    }

    private void updateAccountView(final Account account) {
        Optional<Integer> quickMatchMmr = readMmr(account.getLeaderboardRankings(), "QuickMatch");
        if (quickMatchMmr.isPresent()) {
            qmMmr.setText(String.valueOf(quickMatchMmr.get()));
        }
        Optional<Integer> heroLeagueMmr = readMmr(account.getLeaderboardRankings(), "HeroLeague");
        if (heroLeagueMmr.isPresent()) {
            hlMmr.setText(String.valueOf(heroLeagueMmr.get()));
        }
        Optional<Integer> teamLeagueMmr = readMmr(account.getLeaderboardRankings(), "TeamLeague");
        if (teamLeagueMmr.isPresent()) {
            tlMmr.setText(String.valueOf(teamLeagueMmr.get()));
        }
    }

    private Optional<Integer> readMmr(final List<LeaderboardRanking> leaderboardRankings, final String mode) {
        return leaderboardRankings.stream()
                .filter(ranking -> ranking.getGameMode().equals(mode))
                .map(LeaderboardRanking::getCurrentMmr)
                .findAny();
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

    private void setFileHandlerOnSucceeded() {
        fileHandler.setOnSucceeded(event -> {
            if (HotSLogs.isMaintenance()) {
                setMaintenance();
            } else if (fileHandler.isIdle()) {
                setIdle();
            } else {
                setUploading();
            }

            fileHandler.restart();
        });
    }

    private void bindLists() {
        Map<Status, ObservableList<ReplayFile>> fileMap = fileHandler.getFileMap();

        final String newReplaysTitle = newReplaysTitlePane.textProperty().get();
        final ObservableList<ReplayFile> newReplays = fileMap.get(Status.NEW);
        newReplays.addListener((ListChangeListener<ReplayFile>) c -> updatePaneTitle(newReplaysTitlePane, newReplaysTitle, newReplays));
        newReplaysView.setItems(newReplays);

        final String uploadedReplaysTitle = uploadedReplaysTitlePane.textProperty().get();
        final ObservableList<ReplayFile> uploadedReplays = fileMap.get(Status.UPLOADED);
        uploadedReplays.addListener((ListChangeListener<ReplayFile>) c -> updatePaneTitle(uploadedReplaysTitlePane, uploadedReplaysTitle, uploadedReplays));
        uploadedReplaysView.setItems(uploadedReplays);

        final String exceptionReplaysTitle = exceptionReplaysTitlePane.textProperty().get();
        final ObservableList<ReplayFile> exceptionReplays = fileMap.get(Status.EXCEPTION);
        exceptionReplays.addListener((ListChangeListener<ReplayFile>) c -> updatePaneTitle(exceptionReplaysTitlePane, exceptionReplaysTitle, exceptionReplays));
        exceptionReplaysView.setItems(exceptionReplays);

        final String botReplaysTitle = botReplaysTitlePane.textProperty().get();
        final ObservableList<ReplayFile> botReplays = fileMap.get(Status.UNSUPPORTED_GAME_MODE);
        botReplays.addListener((ListChangeListener<ReplayFile>) c -> updatePaneTitle(botReplaysTitlePane, botReplaysTitle, botReplays));
        botReplaysView.setItems(botReplays);


        updatePaneTitle(newReplaysTitlePane, newReplaysTitle, newReplays);
        updatePaneTitle(uploadedReplaysTitlePane, uploadedReplaysTitle, uploadedReplays);
        updatePaneTitle(exceptionReplaysTitlePane, exceptionReplaysTitle, exceptionReplays);
        updatePaneTitle(botReplaysTitlePane, botReplaysTitle, botReplays);
    }

    private void updatePaneTitle(final TitledPane pane, final String baseTitle, final ObservableList<ReplayFile> list) {
        pane.setText(baseTitle + " (" + list.size() + ")");
    }

    private void setIdle() {
        status.setText("Idle");
        status.textFillProperty().setValue(Paint.valueOf("#00008f"));
    }

    private void setMaintenance() {
        status.setText("Maintenance");
        status.textFillProperty().setValue(Paint.valueOf("#FF0000"));
    }

    private void setUploading() {
        status.setText("Uploading");
        status.textFillProperty().setValue(Paint.valueOf("#008f00"));
    }

}
