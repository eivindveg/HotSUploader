package com.metacodestudio.hotsuploader.controllers;

import com.metacodestudio.hotsuploader.files.FileHandler;
import com.metacodestudio.hotsuploader.files.ReplayFile;
import com.metacodestudio.hotsuploader.files.Status;
import io.datafx.controller.ViewController;
import io.datafx.controller.flow.context.FXMLViewFlowContext;
import io.datafx.controller.flow.context.ViewFlowContext;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;

import javax.annotation.PostConstruct;
import java.util.Map;

@ViewController("../views/Home.fxml")
public class HomeController {

    @FXMLViewFlowContext
    private ViewFlowContext viewFlowContext;

    @FXML
    private ListView<ReplayFile> newReplaysView;

    @FXML
    private ListView<ReplayFile> uploadedReplaysView;

    @FXML
    private ListView<ReplayFile> exceptionReplaysView;

    private FileHandler fileHandler;


    @PostConstruct
    public void init() {
        newReplaysView.requestFocus();
        fileHandler = viewFlowContext.getRegisteredObject(FileHandler.class);
        Map<Status, ObservableList<ReplayFile>> fileMap = fileHandler.getFileMap();
        newReplaysView.setItems(fileMap.get(Status.NEW));
        uploadedReplaysView.setItems(fileMap.get(Status.UPLOADED));
        exceptionReplaysView.setItems(fileMap.get(Status.EXCEPTION));

        fileHandler.setOnSucceeded(event -> fileHandler.restart());
        fileHandler.start();
    }

}
