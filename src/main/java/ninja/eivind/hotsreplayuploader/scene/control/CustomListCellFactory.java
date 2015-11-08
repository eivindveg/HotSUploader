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

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.util.Callback;
import ninja.eivind.hotsreplayuploader.models.ReplayFile;
import ninja.eivind.hotsreplayuploader.services.UploaderService;

import java.net.URL;

/**
 * @author Eivind Vegsundvåg
 */
public class CustomListCellFactory implements Callback<ListView<ReplayFile>, ListCell<ReplayFile>> {

    private final Image updateImage;
    private final Image deleteImage;
    private final Image failedImage;
    private final UploaderService uploaderService;

    public CustomListCellFactory(UploaderService uploaderService) {
        this.uploaderService = uploaderService;
        URL updateResource = getClass().getResource("update.png");
        URL deleteResource = getClass().getResource("delete.png");
        URL failedResource = getClass().getResource("failed.png");
        String updateUrl = updateResource.toExternalForm();
        String deleteUrl = deleteResource.toExternalForm();
        String failedUrl = failedResource.toExternalForm();
        updateImage = new Image(updateUrl);
        deleteImage = new Image(deleteUrl);
        failedImage = new Image(failedUrl);
        ///svgPath
    }
    @Override
    public ListCell<ReplayFile> call(ListView<ReplayFile> param) {
        return new CustomListCell(updateImage, deleteImage, failedImage, uploaderService);
    }
}
