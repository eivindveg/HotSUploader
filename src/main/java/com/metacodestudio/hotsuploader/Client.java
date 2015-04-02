package com.metacodestudio.hotsuploader;

import com.metacodestudio.hotsuploader.controllers.HomeController;
import io.datafx.controller.flow.Flow;
import io.datafx.controller.flow.FlowHandler;
import io.datafx.controller.flow.container.DefaultFlowContainer;
import io.datafx.controller.flow.context.ViewFlowContext;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.File;


public class Client extends Application {

    private static File hotsRoot;

    public static File getHotsRoot() {
        return hotsRoot;
    }

    public static void main(String[] args) {
        hotsRoot = getRootDirectory();
        Application.launch(Client.class, args);
    }

    private static File getRootDirectory() {
        StringBuilder builder = new StringBuilder(System.getProperty("user.home"));
        if (isWindows()) {
            builder.append("\\Documents\\Heroes of the Storm\\Accounts\\");
        } else if (isMacintosh()) {
            builder.append("/Library/Application Support/Blizzard/Heroes of the Storm/Accounts/");
        } else {
            throw new UnsupportedOperationException("This application requires Windows or Macintosh OSX to run");
        }
        return new File(builder.toString());
    }

    private static boolean isMacintosh() {
        // TODO DETERMINE MACINTOSH PROPERTIES
        throw new UnsupportedOperationException("Macintosh check not yet implemented");
    }

    private static boolean isWindows() {
        String osName = System.getProperty("os.name");
        return osName.contains("Windows");
    }

    @Override
    public void start(final Stage primaryStage) throws Exception {
        Flow flow = new Flow(HomeController.class);
        FlowHandler flowHandler = flow.createHandler(new ViewFlowContext());
        flowHandler.getFlowContext().register(getHotsRoot());
        DefaultFlowContainer container = new DefaultFlowContainer();
        StackPane pane = flowHandler.start(container);
        primaryStage.setScene(new Scene(pane));
        primaryStage.setOnCloseRequest(event -> System.exit(0));
        primaryStage.show();
    }

}
