package com.metacodestudio.hotsuploader;

import com.metacodestudio.hotsuploader.files.FileHandler;
import com.metacodestudio.hotsuploader.utils.OSUtils;
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

import java.io.File;
import java.net.URL;


public class Client extends Application {

    private static File hotsRoot;

    public static File getHotsRoot() {
        return hotsRoot;
    }

    public static void main(String[] args) {
        hotsRoot = OSUtils.getHotSHome();
        Application.launch(Client.class, args);
    }

    @Override
    public void start(final Stage primaryStage) throws Exception {
        ClassLoader loader = ClassLoader.getSystemClassLoader();
        URL logo = loader.getResource("images/logo-desktop.png");
        assert logo != null;
        primaryStage.getIcons().add(new Image(logo.toString()));
        primaryStage.setResizable(false);

        Flow flow = new Flow(HomeController.class);
        FlowHandler flowHandler = flow.createHandler(new ViewFlowContext());
        flowHandler.getFlowContext().register(new FileHandler(OSUtils.getHotSHome()));
        DefaultFlowContainer container = new DefaultFlowContainer();
        StackPane pane = flowHandler.start(container);
        primaryStage.setScene(new Scene(pane));
        primaryStage.setOnCloseRequest(event -> System.exit(0));
        primaryStage.show();
    }


}
