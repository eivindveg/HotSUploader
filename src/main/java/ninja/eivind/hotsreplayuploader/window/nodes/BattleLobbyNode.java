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

import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import ninja.eivind.hotsreplayuploader.di.FXMLLoaderFactory;
import ninja.eivind.hotsreplayuploader.di.nodes.JavaFXNode;
import ninja.eivind.hotsreplayuploader.services.platform.PlatformService;
import ninja.eivind.stormparser.StandaloneBattleLobbyParser;
import ninja.eivind.stormparser.models.Player;
import ninja.eivind.stormparser.models.Replay;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

public class BattleLobbyNode extends VBox implements JavaFXNode {

    @Autowired
    private StandaloneBattleLobbyParser parser;

    @Autowired
    private PlatformService platformService;

    @FXML
    private ListView<String> teamOneList;

    @FXML
    private ListView<String> teamTwoList;

    @FXML
    private Hyperlink link;

    private static final String MATCH_PREVIEW_URL = "http://www.hotslogs.com/Player/MatchPreview?Data={{data}}";

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
        teamOneList.getItems().clear();
        teamTwoList.getItems().clear();
        Replay replay = parser.apply(file);

        List<Player> players = replay.getReplayDetails().getPlayers();
        for (int i = 0; i < players.size(); i++) {
            final Player player = players.get(i);
            final String playerName = getPlayerName(player);
            final ObservableList<String> items;
            if (i >= 5) {
                items = teamOneList.getItems();
            } else {
                items = teamTwoList.getItems();
            }
            items.add(playerName);
        }


        link.setOnMouseClicked(event -> {
            final String url = MATCH_PREVIEW_URL.replace("{{data}}", base64EncodeBattleLobby(replay));
            platformService.browse(url);
        });
    }

    private String getPlayerName(Player player) {
        String shortName = player.getShortName();
        if(!StringUtils.hasLength(shortName)) {
            return "AI Player";
        }
        return shortName;
    }

    public String base64EncodeBattleLobby(Replay replay) {
        Base64.Encoder encoder = Base64.getEncoder();
        List<String> asString = replay.getReplayDetails().getPlayers()
                .stream()
                .map(player -> player.getBattleNetRegionId()
                        + "#" + player.getShortName()
                        + "#" + player.getBattleTag()
                        + "#" + player.getTeam())
                .collect(Collectors.toList());
        try {
            byte[] data = String.join(",", asString).getBytes("UTF-8");
            return encoder.encodeToString(data);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
