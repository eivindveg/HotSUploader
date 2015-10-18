package com.metacodestudio.hotsuploader.scene.control;

import com.metacodestudio.hotsuploader.files.FileHandler;
import com.metacodestudio.hotsuploader.models.ReplayFile;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;

/**
 * @author Eivind Vegsundvåg
 */
public class ExceptionListCell extends ListCell<ReplayFile> {

    private final BorderPane hBox;
    private final Label label;
    private final Button button;
    private final Image image;
    private FileHandler fileHandler;

    protected ExceptionListCell(Image image, FileHandler fileHandler) {
        super();
        this.image = image;
        this.fileHandler = fileHandler;
        label = new Label();
        button = new Button("");
        label.setAlignment(Pos.CENTER_LEFT);
        button.setAlignment(Pos.CENTER_RIGHT);
        button.setBackground(Background.EMPTY);
        hBox = new BorderPane(null, null, button, null, label);
        hBox.setMaxWidth(Double.MAX_VALUE);
        hBox.setPrefWidth(USE_COMPUTED_SIZE);

    }

    @Override
    public void updateSelected(boolean selected) {
        super.updateSelected(selected);
        if(selected) {
            ImageView imageView = new ImageView(image);
            imageView.setFitHeight(20);
            imageView.setFitWidth(20);
            button.setGraphic(imageView);
        } else {
            button.setGraphic(null);
        }
    }

    @Override
    protected ObservableList<Node> getChildren() {
        return super.getChildren();
    }

    @Override
    protected void updateItem(ReplayFile item, boolean empty) {
        super.updateItem(item, empty);
        setText(null);
        if (empty) {
            setGraphic(null);
        } else {
            label.setText(item.getFile().getName());

            setGraphic(hBox);
            hBox.setOnMouseClicked(event -> {
                if (2 == event.getClickCount()) {
                    fileHandler.invalidateReplay(item);
                }
            });
            button.setOnAction(event -> fileHandler.invalidateReplay(item));
        }
    }
}
