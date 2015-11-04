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
 * @author Eivind Vegsundvåg
 */
public class CustomListCell extends ListCell<ReplayFile> {

    private final BorderPane content;
    private final Label label;
    private final ImageView updateImageView;
    private final ImageView deleteImageView;
    private final ImageView exceptionImageView;
    private final Image updateImage;
    private final Image deleteImage;
    private final Image exceptionImage;
    private final UploaderService uploaderService;
    private ReplayFile lastItem;

    protected CustomListCell(Image updateImage, Image deleteImage, Image exceptionImage, UploaderService uploaderService) {
        super();
        this.updateImage = updateImage;
        this.deleteImage = deleteImage;
        this.exceptionImage = exceptionImage;
        this.uploaderService = uploaderService;
        label = new Label();
        updateImageView = new ImageView();
        updateImageView.setFitHeight(20);
        updateImageView.setFitWidth(20);
        deleteImageView = new ImageView();
        deleteImageView.setFitHeight(20);
        deleteImageView.setFitWidth(20);
        exceptionImageView = new ImageView();
        exceptionImageView.setFitHeight(20);
        exceptionImageView.setFitWidth(20);
        label.setAlignment(Pos.CENTER_LEFT);
        HBox hBox = new HBox(updateImageView, deleteImageView);
        content = new BorderPane(null, null, hBox, null, label);
        content.setMaxWidth(Double.MAX_VALUE);
        content.setPrefWidth(USE_COMPUTED_SIZE);
    }

    @Override
    public void updateSelected(boolean selected) {
        super.updateSelected(selected);
        if(selected && lastItem != null && lastItem.getStatus() == Status.EXCEPTION) {
            updateImageView.setImage(updateImage);
            deleteImageView.setImage(deleteImage);
        } else {
            updateImageView.setImage(null);
            deleteImageView.setImage(null);
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
            BooleanProperty failedProperty = item.getFailedProperty();
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
            updateImageView.setOnMouseClicked(event -> {
                System.out.println("Clicked");
                uploaderService.invalidateReplay(item);
            });
            deleteImageView.setOnMouseClicked(event -> uploaderService.deleteReplay(item));
        }
    }
}
