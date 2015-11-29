// Copyright 2015 Eivind Vegsundvåg
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package ninja.eivind.hotsreplayuploader.scene.control;

import javafx.beans.property.BooleanProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import ninja.eivind.hotsreplayuploader.models.ReplayFile;
import ninja.eivind.hotsreplayuploader.models.Status;
import ninja.eivind.hotsreplayuploader.services.UploaderService;

/**
 * {@link ListCell} for a list of {@link ReplayFile}s.<br>
 * Shows, if exceptions occured on the upload and provides
 * controls for retrying failed uploads and deleting replay files.
 */
public class CustomListCell extends ListCell<ReplayFile> {

    private final BorderPane content;
    private final Label label;
    private final ImageButton updateImageView;
    private final ImageButton deleteImageView;
    private final ImageView exceptionImageView;
    private final Image updateImage;
    private final Image deleteImage;
    private final Image exceptionImage;
    private final UploaderService uploaderService;
    private ReplayFile lastItem;

    protected CustomListCell(Image updateImage, Image deleteImage,
            Image exceptionImage, UploaderService uploaderService) {
        super();
        this.updateImage = updateImage;
        this.deleteImage = deleteImage;
        this.exceptionImage = exceptionImage;
        this.uploaderService = uploaderService;

        label = new Label();
        label.setAlignment(Pos.CENTER_LEFT);

        updateImageView = new ImageButton(20, 20);
        deleteImageView = new ImageButton(20, 20);
        exceptionImageView = new ImageView();
        exceptionImageView.setFitHeight(20);
        exceptionImageView.setFitWidth(20);

        final HBox hBox = new HBox(updateImageView, deleteImageView);
        content = new BorderPane(null, null, hBox, null, label);
        content.setMaxWidth(Double.MAX_VALUE);
        content.setPrefWidth(USE_COMPUTED_SIZE);
    }

    @Override
    public void updateSelected(boolean selected) {
        super.updateSelected(selected);
        if(selected && lastItem != null && lastItem.getStatus() == Status.EXCEPTION) {
            updateImageView.setGraphic(updateImage);
            deleteImageView.setGraphic(deleteImage);
        } else {
            updateImageView.removeGraphic();
            deleteImageView.removeGraphic();
        }

    }

    @Override
    protected void updateItem(ReplayFile item, boolean empty) {
        super.updateItem(item, empty);
        lastItem = item;
        setText(null);
        if (empty) {
            setGraphic(null);
        } else {
            label.setGraphic(exceptionImageView);
            label.setText(item.getFile().getName());
            final BooleanProperty failedProperty = item.getFailedProperty();
            exceptionImageView.setImage(failedProperty.get() ? exceptionImage : null);
            failedProperty.addListener((observable, oldValue, newValue) -> {
                if(newValue != null && newValue) {
                    exceptionImageView.setImage(exceptionImage);
                }
            });

            setGraphic(content);
            content.setOnMouseClicked(event -> {
                if (2 == event.getClickCount()) {
                    uploaderService.invalidateReplay(item);
                }
            });
            updateImageView.setOnMouseClicked(event -> uploaderService.invalidateReplay(item));
            deleteImageView.setOnMouseClicked(event -> uploaderService.deleteReplay(item));
        }
    }
}
