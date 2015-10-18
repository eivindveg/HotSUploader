package com.metacodestudio.hotsuploader.scene.control;

import com.metacodestudio.hotsuploader.files.FileHandler;
import com.metacodestudio.hotsuploader.models.ReplayFile;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.SVGPath;
import javafx.util.Callback;

import java.net.URL;

/**
 * @author Eivind Vegsundvåg
 */
public class ExceptionListCellFactory implements Callback<ListView<ReplayFile>, ListCell<ReplayFile>> {

    private final Image image;
    private final FileHandler fileHandler;

    public ExceptionListCellFactory(FileHandler fileHandler) {
        this.fileHandler = fileHandler;
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL resource = classLoader.getResource("images/update.png");
        assert null != resource;
        String url = resource.toExternalForm();
        image = new Image(url);
        ///svgPath
    }
    @Override
    public ListCell<ReplayFile> call(ListView<ReplayFile> param) {
        return new ExceptionListCell(image, fileHandler);
    }
}
