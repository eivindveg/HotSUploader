package com.metacodestudio.hotsuploader.controllers;

import com.metacodestudio.hotsuploader.files.FileHandler;
import com.metacodestudio.hotsuploader.files.ReplayFile;
import com.metacodestudio.hotsuploader.files.Status;
import com.metacodestudio.hotsuploader.providers.HotSLogs;
import io.datafx.controller.ViewController;
import io.datafx.controller.flow.action.ActionMethod;
import io.datafx.controller.flow.action.ActionTrigger;
import io.datafx.controller.flow.context.FXMLViewFlowContext;
import io.datafx.controller.flow.context.ViewFlowContext;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TitledPane;
import javafx.scene.paint.Paint;

import javax.annotation.PostConstruct;
import java.util.Map;

@ViewController(value = "../views/Home.fxml", iconPath = "../../../../images/logo.png", title = "HotSLogs Uploader")
public class HomeController {

    @FXMLViewFlowContext
    private ViewFlowContext viewFlowContext;

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
    @ActionTrigger("invalidateExceptions")
    private Button invalidateExceptions;

    private FileHandler fileHandler;


    @PostConstruct
    public void init() {
        fileHandler = viewFlowContext.getRegisteredObject(FileHandler.class);
        bindLists();
        setFileHandlerOnSucceeded();
        fileHandler.start();
        if(fileHandler.isIdle()) {
            setIdle();
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
