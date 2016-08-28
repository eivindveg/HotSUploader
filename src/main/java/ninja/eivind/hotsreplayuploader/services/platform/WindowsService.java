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

package ninja.eivind.hotsreplayuploader.services.platform;

import javafx.stage.Stage;
import ninja.eivind.hotsreplayuploader.files.tempwatcher.BattleLobbyTempDirectories;
import ninja.eivind.hotsreplayuploader.utils.SimpleHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link PlatformService} that is active on Microsoft Windows systems.
 */
public class WindowsService implements PlatformService {

    private static final Logger LOG = LoggerFactory.getLogger(WindowsService.class);
    private Desktop desktop;
    private Pattern pathPattern = Pattern.compile("[A-Z]:\\\\(\\\\|(\\w+|\\.)| )+");
    private File documentsHome;
    public WindowsService() {
        desktop = Desktop.getDesktop();
    }

    @Override
    public File getApplicationHome() {
        if (documentsHome == null) {
            try {
                documentsHome = findMyDocuments();
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return new File(documentsHome, APPLICATION_DIRECTORY_NAME);
    }

    @Override
    public File getHotSHome() {
        if (documentsHome == null) {
            try {
                documentsHome = findMyDocuments();
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return new File(documentsHome, "Heroes of the Storm\\Accounts");
    }

    @Override
    public TrayIcon getTrayIcon(Stage primaryStage) {
        final URL imageURL = getLogoUrl();
        return buildTrayIcon(imageURL, primaryStage);
    }

    @Override
    public void browse(final String uri) {
        try {
            desktop.browse(SimpleHttpClient.encode(uri));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public URL getLogoUrl() {
        return getClass().getResource("/images/logo-desktop.png");
    }

    @Override
    public boolean isPreloaderSupported() {
        return true;
    }

    @Override
    public BattleLobbyTempDirectories getBattleLobbyTempDirectories() {
        final String tempDirProperty = System.getProperty("java.io.tmpdir");
        final File root = new File(tempDirProperty);
        final File remainder = new File(tempDirProperty + File.pathSeparator + "Heroes of the Storm");

        return new BattleLobbyTempDirectories(root, remainder);
    }

    private File findMyDocuments() throws FileNotFoundException {
        Process p = null;
        String myDocuments = null;
        try {
            LOG.info("Querying registry for Documents folder location.");
            p = Runtime.getRuntime().exec("reg query \"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\Shell Folders\" /v personal");
            p.waitFor();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                final StringBuilder builder = new StringBuilder();
                reader.lines().forEach(builder::append);
                final String[] values = builder.toString().trim().split("\\s\\s+");
                for (final String value : values) {
                    Optional<String> matchForPath = getMatchForPath(value);
                    if(matchForPath.isPresent()) {
                        myDocuments = matchForPath.get();
                        break;
                    }
                }
            }

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if (p != null) {
                p.destroy();
            }
        }

        if (myDocuments == null) {
            LOG.warn("Could not reliably query register for My Documents folder. This usually means you have" +
                    " a unicode name and standard location. Falling back to legacy selection:");
            myDocuments = USER_HOME + "\\Documents";
            LOG.warn("Result: " + myDocuments);
        }
        return new File(myDocuments);
    }

    protected Optional<String> getMatchForPath(final String path) {
        final Matcher matcher = pathPattern.matcher(path);

        if(matcher.matches()) {
            return Optional.of(matcher.group());
        }
        return Optional.empty();
    }
}
