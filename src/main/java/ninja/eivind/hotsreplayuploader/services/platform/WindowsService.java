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

import com.jasongoodwin.monads.Try;
import javafx.stage.Stage;
import ninja.eivind.hotsreplayuploader.utils.CloseableProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * {@link PlatformService} that is active on Microsoft Windows systems.
 */
public class WindowsService implements PlatformService {

    private static final Logger LOG = LoggerFactory.getLogger(WindowsService.class);
    private Desktop desktop;

    private Pattern pathPattern = Pattern.compile("[A-Z]:(\\\\|\\w+| )+");
    private File documentsHome;

    public WindowsService() {
        desktop = Desktop.getDesktop();
    }

    @Override
    public File getApplicationHome() {
        if (documentsHome == null) {
            documentsHome = findMyDocumentsWithFallback();
        }
        return new File(documentsHome, APPLICATION_DIRECTORY_NAME);
    }

    private File findMyDocumentsWithFallback() {
        return findMyDocuments()
                .map(File::new)
                .orElse(new File(getDefaultMyDocumentsLocation()));
    }

    @Override
    public File getHotSHome() {
        if (documentsHome == null) {
            documentsHome = findMyDocumentsWithFallback();
        }
        return new File(documentsHome, "Heroes of the Storm\\Accounts");
    }

    @Override
    public TrayIcon getTrayIcon(Stage primaryStage) {
        final URL imageURL = getLogoUrl();
        return buildTrayIcon(imageURL, primaryStage);
    }

    @Override
    public void browse(final URI uri) throws IOException {
        desktop.browse(uri);
    }

    @Override
    public URL getLogoUrl() {
        return getClass().getResource("/images/logo-desktop.png");
    }

    @Override
    public boolean isPreloaderSupported() {
        return true;
    }


    private Try<String> findMyDocuments() {
        return Try.ofFailable(() -> {
            try (CloseableProcess p = CloseableProcess.of(Runtime.getRuntime().exec("reg query \"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\Shell Folders\" /v personal"))) {
                LOG.info("Querying registry for Documents folder location.");
                p.waitFor();

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                    return reader.lines()
                            .map(line -> line.split("\\s{2,}"))
                            .flatMap(Arrays::stream)
                            .filter(WindowsService.this::matches)
                            .findFirst()
                            .orElseGet(WindowsService.this::getDefaultMyDocumentsLocation);
                }
            }
        });
    }


    private boolean matches(final String entry) {
        return pathPattern.matcher(entry).matches();
    }

    private String getDefaultMyDocumentsLocation() {
        LOG.warn("Could not reliably query register for My Documents folder. This usually means you have" +
                " a unicode name and standard location. Falling back to legacy selection:");
        final String file = USER_HOME + "\\Documents";
        LOG.warn("Result: " + file);
        return file;
    }
}
