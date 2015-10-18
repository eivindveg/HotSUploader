package com.metacodestudio.hotsuploader.scene.control;

import com.metacodestudio.hotsuploader.files.FileHandler;
import com.metacodestudio.hotsuploader.models.ReplayFile;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

/**
 * @author Eivind Vegsundvåg
 */
public class ExceptionListCell extends ListCell<ReplayFile> {

    private final BorderPane content;
    private final Label label;
    private final ImageButton updateButton;
    private final ImageButton deleteButton;
    private final Image updateImage;
    private final Image deleteImage;
    private final FileHandler fileHandler;

    protected ExceptionListCell(Image updateImage, Image deleteImage, FileHandler fileHandler) {
        super();
        this.updateImage = updateImage;
        this.deleteImage = deleteImage;
        this.fileHandler = fileHandler;
        label = new Label();
        updateButton = new ImageButton(20, 20);
        deleteButton = new ImageButton(20, 20);
        label.setAlignment(Pos.CENTER_LEFT);
        updateButton.setAlignment(Pos.CENTER_RIGHT);
        HBox hBox = new HBox(updateButton, deleteButton);
        content = new BorderPane(null, null, hBox, null, label);
        content.setMaxWidth(Double.MAX_VALUE);
        content.setPrefWidth(USE_COMPUTED_SIZE);
    }

    @Override
    public void updateSelected(boolean selected) {
        super.updateSelected(selected);
        if(selected) {
            updateButton.setGraphic(updateImage);
            deleteButton.setGraphic(deleteImage);
        } else {
            updateButton.removeGraphic();
            deleteButton.removeGraphic();
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
            updateButton.setOnAction(event -> fileHandler.invalidateReplay(item));
            deleteButton.setOnAction(event -> fileHandler.deleteReplay(item));
        }
    }
}
