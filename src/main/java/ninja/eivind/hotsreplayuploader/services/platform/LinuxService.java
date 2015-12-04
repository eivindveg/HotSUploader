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
    private Desktop desktop;

    public LinuxService() {
        if (Desktop.isDesktopSupported()) {
            desktop = Desktop.getDesktop();
        }
    }

    @Override
    public File getApplicationHome() {
        return new File(USER_HOME, APPLICATION_DIRECTORY_NAME);
    }

    /**
     * Returns user-configured path for 'Documents' folder, or null if failed.
     * Tries to open ~/.config/user-dirs.dirs and parse its content.
     * @see <a href="http://freedesktop.org/wiki/Software/xdg-user-dirs/">freedesktop specs</a>.
     * @return String path to folder, or null on error
     */
    private String getXDGDocumentsPath() {
        String path = null;
        final File file = new File(USER_HOME, ".config/user-dirs.dirs");
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while((line = br.readLine()) != null) {
                // we want to find a line like:
                // XDG_DOCUMENTS_DIR="$HOME/Documents"
                if (line.charAt(0) == '#') continue;  // skip comments
                // check for our magic line
                if (line.startsWith("XDG_DOCUMENTS_DIR=\"$HOME/")) {
                    // cut the contents of line, after $HOME/ up to last symbol "
                    // XDG_DOCUMENTS_DIR="$HOME/Documents"
                    //                          ^_______^
                    path = line.substring(25, line.length()-1); // 25 == "XDG_DOCUMENTS_DIR=\"$HOME/".length()
                    break;
                }
            }
            br.close();
        } catch (IOException ioe) {
            LOG.error("Failed to read XDG user-dirs config file " + file.toString());
        } catch (StringIndexOutOfBoundsException e) {
            LOG.error("Error parsing XDG user-dirs config file " + file.toString());
        }
        return path;
    }

    @Override
    public File getHotSHome() {
        final File file = new File(USER_HOME, "Heroes of the Storm/Accounts/");
        if (file.exists()) {
            return file;
        } else {
            String xdgDefaultDocsPath = "Documents";
            String xdgDocsPath = this.getXDGDocumentsPath();
            if (xdgDocsPath == null)
                xdgDocsPath = xdgDefaultDocsPath;
            return new File(USER_HOME, xdgDocsPath + "/Heroes of the Storm/Accounts/");
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
