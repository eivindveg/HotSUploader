// Copyright 2016 Eivind Vegsundvåg
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

package ninja.eivind.hotsreplayuploader.window.nodes;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;
import ninja.eivind.hotsreplayuploader.di.FXMLLoaderFactory;
import ninja.eivind.hotsreplayuploader.di.nodes.JavaFXNode;
import ninja.eivind.stormparser.StandaloneBattleLobbyParser;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class BattleLobbyNode extends VBox implements JavaFXNode {

    @Autowired
    private StandaloneBattleLobbyParser parser;

    public BattleLobbyNode(FXMLLoaderFactory factory) throws IOException {
        URL resource = getClass().getResource("BattleLobbyNode.fxml");
        FXMLLoader loader = factory.get();
        loader.setLocation(resource);
        loader.setRoot(this);
        loader.setController(this);
        loader.load();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
    }

    public void setFile(File file) {

    }
}
