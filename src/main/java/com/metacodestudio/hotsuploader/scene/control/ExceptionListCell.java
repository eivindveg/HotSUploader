package com.metacodestudio.hotsuploader.scene.control;

import com.metacodestudio.hotsuploader.files.FileHandler;
import com.metacodestudio.hotsuploader.models.ReplayFile;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

/**
 * @author Eivind Vegsundvåg
 */
public class ExceptionListCell extends ListCell<ReplayFile> {

    private final BorderPane content;
    private final Label label;
    private final ImageView updateButton;
    private final ImageView deleteButton;
    private final Image updateImage;
    private final Image deleteImage;
    private final FileHandler fileHandler;

    protected ExceptionListCell(Image updateImage, Image deleteImage, FileHandler fileHandler) {
        super();
        this.updateImage = updateImage;
        this.deleteImage = deleteImage;
        this.fileHandler = fileHandler;
        label = new Label();
        updateButton = new ImageView();
        updateButton.setFitHeight(20);
        updateButton.setFitWidth(20);
        deleteButton = new ImageView();
        deleteButton.setFitHeight(20);
        deleteButton.setFitWidth(20);
        label.setAlignment(Pos.CENTER_LEFT);
        HBox hBox = new HBox(updateButton, deleteButton);
        content = new BorderPane(null, null, hBox, null, label);
        content.setMaxWidth(Double.MAX_VALUE);
        content.setPrefWidth(USE_COMPUTED_SIZE);
    }

    @Override
    public void updateSelected(boolean selected) {
        super.updateSelected(selected);
        if(selected) {
            updateButton.setImage(updateImage);
            deleteButton.setImage(deleteImage);
        } else {
            updateButton.setImage(null);
            deleteButton.setImage(null);
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

            setGraphic(content);
            content.setOnMouseClicked(event -> {
                if (2 == event.getClickCount()) {
                    fileHandler.invalidateReplay(item);
                }
            });
            updateButton.setOnMouseClicked(event -> fileHandler.invalidateReplay(item));
            deleteButton.setOnMouseClicked(event -> fileHandler.deleteReplay(item));
        }
    }
}
