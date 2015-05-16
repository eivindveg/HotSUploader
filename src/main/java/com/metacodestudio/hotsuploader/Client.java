package com.metacodestudio.hotsuploader;

import com.metacodestudio.hotsuploader.files.FileHandler;
import com.metacodestudio.hotsuploader.utils.SimpleHttpClient;
import com.metacodestudio.hotsuploader.utils.StormHandler;
import com.metacodestudio.hotsuploader.window.HomeController;
import io.datafx.controller.flow.Flow;
import io.datafx.controller.flow.FlowHandler;
import io.datafx.controller.flow.container.DefaultFlowContainer;
import io.datafx.controller.flow.context.ViewFlowContext;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;


public class Client extends Application {

    public static void main(String[] args) {
        Application.launch(Client.class, args);
    }

    @Override
    public void start(final Stage primaryStage) throws Exception {
        ClassLoader loader = ClassLoader.getSystemClassLoader();
        URL logo = loader.getResource("images/logo-desktop.png");
        assert logo != null;
        primaryStage.getIcons().add(new Image(logo.toString()));
        primaryStage.setResizable(false);
        primaryStage.setTitle("HotSLogs UploaderFX");

        Flow flow = new Flow(HomeController.class);
        FlowHandler flowHandler = flow.createHandler(new ViewFlowContext());
        ViewFlowContext flowContext = flowHandler.getFlowContext();
        StormHandler stormHandler = new StormHandler();
        flowContext.register(stormHandler);
        flowContext.register(setupFileHandler(stormHandler));
        flowContext.register(new SimpleHttpClient());
        DefaultFlowContainer container = new DefaultFlowContainer();
        StackPane pane = flowHandler.start(container);
        primaryStage.setScene(new Scene(pane));
        primaryStage.setOnCloseRequest(event -> System.exit(0));
        primaryStage.show();
    }

    private FileHandler setupFileHandler(final StormHandler stormHandler) throws IOException {
        FileHandler fileHandler = new FileHandler(stormHandler);
        fileHandler.cleanup();
        fileHandler.registerInitial();
        return fileHandler;
    }


}
