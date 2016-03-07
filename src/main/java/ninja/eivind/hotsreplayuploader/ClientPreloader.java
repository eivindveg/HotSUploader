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

package ninja.eivind.hotsreplayuploader;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Preloader;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import ninja.eivind.hotsreplayuploader.versions.VersionHandshakeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Application preloader. This displays a splash screen while {@link Client} is loading.
 */
public class ClientPreloader extends Preloader {

    private static final Logger LOG = LoggerFactory.getLogger(ClientPreloader.class);
    private Stage preloaderStage;

    @Override
    public void stop() throws Exception {
        super.stop();

        System.exit(0);
    }

    @Override
    public void init() throws Exception {
        int port = 27000;
        if (checkForActiveProcess(port)) {
            stop();
        }
    }

    private boolean checkForActiveProcess(int port) {
        final VersionHandshakeToken token = new VersionHandshakeToken();
        final ObjectMapper mapper = new ObjectMapper();
        try (Socket socket = new Socket(InetAddress.getLoopbackAddress(), port);
             DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
             DataInputStream dis = new DataInputStream(socket.getInputStream())) {

            dos.writeUTF(mapper.writeValueAsString(token));
            String handshakeResponse = dis.readUTF();
            VersionHandshakeToken tokenB = mapper.
                    readValue(handshakeResponse, VersionHandshakeToken.class);

            //same version, exit
            if (token.equals(tokenB)) {
                return true;
            } else if (token.isLessThan(tokenB)) {
                return true;
            }

        } catch (ConnectException e) {
            LOG.info("Couldn't establish connection to " +
                    InetAddress.getLoopbackAddress() + ":" + port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        preloaderStage = primaryStage;
        LOG.info("Preloading application");
        primaryStage.initStyle(StageStyle.UNDECORATED);

        final Parent root = FXMLLoader.load(getClass().getResource("window/Preloader.fxml"));

        final Scene scene = new Scene(root);

        primaryStage.setScene(scene);
        primaryStage.sizeToScene();
        primaryStage.show();
    }

    @Override
    public void handleStateChangeNotification(StateChangeNotification stateChangeNotification) {
        if (stateChangeNotification.getType() == StateChangeNotification.Type.BEFORE_START) {
            try {
                preloaderStage.close();
            } catch (Exception e) {
                LOG.warn("Failed to stop preloader", e);
            }
        }
    }
}
