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

package ninja.eivind.hotsreplayuploader.services.platform;

import ninja.eivind.hotsreplayuploader.files.tempwatcher.BattleLobbyTempDirectories;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.URL;

@Component
public class TestEnvironmentPlatformService implements PlatformService {
    public static final File TEST_ROOT = new File("target" + File.pathSeparator + "test");

    @Override
    public File getApplicationHome() {
        return new File(TEST_ROOT, APPLICATION_DIRECTORY_NAME);
    }

    @Override
    public File getHotSHome() {
        return new File(TEST_ROOT, "Heroes of the Storm");
    }

    @Override
    public void browse(String uri) {
        // no-op
    }

    @Override
    public URL getLogoUrl() {
        // no-op
        return null;
    }

    @Override
    public boolean isPreloaderSupported() {
        return false;
    }

    @Override
    public BattleLobbyTempDirectories getBattleLobbyTempDirectories() {
        final File root = new File(TEST_ROOT, "tmp");
        final File remainder = new File(root + File.pathSeparator + "tmp" + File.pathSeparator + "Heroes of the Storm");
        return new BattleLobbyTempDirectories(
                root,
                remainder
        );
    }
}
