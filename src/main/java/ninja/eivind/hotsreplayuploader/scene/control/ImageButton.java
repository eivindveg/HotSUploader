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

import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * {@link Button}, which has a transparent background and shows an {@link Image} as icon.<br>
 * Provides a visual feedback, when clicked, by setting a minimal padding.
 */
public class ImageButton extends Button {

    private final String STYLE_NORMAL = "-fx-background-color: transparent; -fx-padding: 0;";
    private final String STYLE_PRESSED = "-fx-background-color: transparent; -fx-padding: 1 -1 -1 1;";

    private final double height;
    private final double width;

    /**
     * Create an {@link ImageButton} with the specified height and width.
     *
     * @param height
     * @param width
     */
    public ImageButton(double height, double width) {
        this.height = height;
        this.width = width;

        setStyle(STYLE_NORMAL);

        setOnMousePressed((event) -> setStyle(STYLE_PRESSED));
        setOnMouseReleased((event) -> setStyle(STYLE_NORMAL));
    }

    /**
     * Set an icon for this {@link Button} to a specified {@link Image}.
     */
    protected void setGraphic(Image image) {
        final ImageView imageView = new ImageView(image);
        imageView.setFitHeight(height);
        imageView.setFitWidth(width);
        super.setGraphic(imageView);
    }

    /**
     * Removes the icon of this {@link Button}.
     */
    protected void removeGraphic() {
        super.setGraphic(null);
    }
}
