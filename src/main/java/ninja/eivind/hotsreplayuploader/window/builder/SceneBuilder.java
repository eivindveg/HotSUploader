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

package ninja.eivind.hotsreplayuploader.window.builder;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import java.io.IOException;

public class SceneBuilder {
    private FXMLLoader loader;
    private String location;


    public SceneBuilder(FXMLLoader loader) {
        this.loader = loader;
    }

    public SceneBuilder setLocation(String location) {
        this.location = location;
        return this;
    }

    public Scene build() {
        loader.setLocation(getClass().getResource(location));
        try {
            final Parent root = loader.load();
            return new Scene(root);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
