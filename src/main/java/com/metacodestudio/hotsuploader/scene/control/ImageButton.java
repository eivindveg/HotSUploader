package com.metacodestudio.hotsuploader.scene.control;

import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;

/**
 * @author Eivind Vegsundvåg
 */
public class ImageButton extends Button {

    private final double height;
    private final double width;

    public ImageButton(double height, double width) {
        this.height = height;
        this.width = width;
        setBackground(Background.EMPTY);
    }

    public void setGraphic(Image image) {
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(height);
        imageView.setFitWidth(width);
        super.setGraphic(imageView);
    }

    public void removeGraphic() {
        super.setGraphic(null);
    }
}
