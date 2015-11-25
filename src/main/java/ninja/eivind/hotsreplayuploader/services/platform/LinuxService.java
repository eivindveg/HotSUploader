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
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

public class LinuxService implements PlatformService {

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

    @Override
    public File getHotSHome() {
        File file = new File(USER_HOME, "Heroes of the Storm/Accounts/");
        if(file.exists()) {
            return file;
        } else {
            return new File(USER_HOME, "Documents/Heroes of the Storm/Accounts/");
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
}
