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

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * {@link PlatformService} that is active on GNU/Linux systems.
 */
public class LinuxService implements PlatformService {

    private static final Logger LOG = LoggerFactory.getLogger(LinuxService.class);
    private static final String XDG_DEFAULT_DOCS_PATH = "Documents";
    private Desktop desktop;
    private String xdgDocsPath;

    public LinuxService() {
        if (Desktop.isDesktopSupported()) {
            desktop = Desktop.getDesktop();
        }
        xdgDocsPath = null;
    }

    @Override
    public File getApplicationHome() {
        return new File(USER_HOME, APPLICATION_DIRECTORY_NAME);
    }

    /**
     * Returns user-configured path for "Documents" folder, which can be localized string.
     * Tries to open ~/.config/user-dirs.dirs and parse its content. If opening or parsing fails,
     * return a default value ("Documents").
     * @see <a href="http://freedesktop.org/wiki/Software/xdg-user-dirs/">freedesktop specs</a>.
     * @return String path to user "Documents" folder.
     */
    private String getXDGDocumentsPath() {
        if (xdgDocsPath != null)
            return xdgDocsPath;
        final String lineStart = "XDG_DOCUMENTS_DIR=\"$HOME/";
        final File file = new File(USER_HOME, ".config/user-dirs.dirs");
        // try-with-resources
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while((line = br.readLine()) != null) {
                // we want to find a line like:
                // XDG_DOCUMENTS_DIR="$HOME/Documents"
                line = line.trim();  // remove whitespace
                if (line.charAt(0) == '#') continue;  // skip comments
                // check for our magic line
                if (line.startsWith(lineStart)) {
                    // cut the contents of line, after "...$HOME/" up to last symbol "
                    // XDG_DOCUMENTS_DIR="$HOME/Documents"
                    //                          ^_______^
                    xdgDocsPath = line.substring(lineStart.length(), line.length() - 1);
                    break;
                }
            }
            br.close();
        } catch (IOException ioe) {
            LOG.error("Failed to read XDG user-dirs config file " + file.toString());
        } catch (StringIndexOutOfBoundsException e) {
            LOG.error("Error parsing XDG user-dirs config file " + file.toString());
        }
        return (xdgDocsPath != null ? xdgDocsPath : XDG_DEFAULT_DOCS_PATH);
    }

    @Override
    public File getHotSHome() {
        final File file = new File(USER_HOME, "Heroes of the Storm/Accounts/");
        if (file.exists()) {
            return file;
        } else {
            String docsPath = getXDGDocumentsPath();
            return new File(USER_HOME, docsPath + "/Heroes of the Storm/Accounts/");
        }
    }

    @Override
    public void browse(final URI uri) throws IOException {
        if (Desktop.isDesktopSupported()) {
            desktop.browse(uri);
        } else {
            Runtime.getRuntime().exec("xdg-open " + uri.toURL());
        }
    }

    @Override
    public URL getLogoUrl() {
        return getClass().getResource("/images/logo-desktop.png");
    }

    @Override
    public boolean isPreloaderSupported() {
        return false;
    }
}
