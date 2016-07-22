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

package ninja.eivind.hotsreplayuploader.utils;

import ninja.eivind.hotsreplayuploader.models.Account;
import ninja.eivind.hotsreplayuploader.models.Player;
import ninja.eivind.hotsreplayuploader.services.platform.PlatformService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class, which holds several application properties like paths
 * and provides several convencience methods like OS detection or
 * data mining game related information .
 */
@Singleton
public class StormHandler {

    private static final String ACCOUNT_FOLDER_FILTER = "(\\d+[^A-Za-z,.\\-()\\s])";
    private static final String hotsAccountFilter = "(\\d-Hero-\\d-\\d{1,20})";

    private File hotsHome;
    @Inject
    private PlatformService platformService;

    public StormHandler() {
    }

    public File getHotSHome() {
        if (hotsHome == null) {
            hotsHome = buildHotSHome();
        }
        return hotsHome;
    }

    /**
     * Retrieves a {@link List} of {@link File}s, which represent
     * the replay file directory for a specific {@link Account}.
     * @return {@link List} of directories or an empty {@link List}
     */
    public List<File> getReplayDirectories() {
        return getAccountDirectories().stream()
                .map(StormHandler::getReplayDirectory)
                .collect(Collectors.toList());
    }

    private static File getReplayDirectory(File dir) {
        return new File(dir, "Replays" + File.separator + "Multiplayer");
    }

    /**
     * Builds a {@link List} of URIs to the HOTSlogs API, which
     * can be used to retrieve {@link Player} information.
     * @return {@link List} of uris or an empty {@link List}
     */
    public List<String> getAccountStringUris() {
        final List<File> accountDirectories = getAccountDirectories();

        return accountDirectories.stream()
                .map(folder -> {
                    String[] split = folder.getName().replace("-Hero", "").split("-");
                    StringBuilder accountNameBuilder = new StringBuilder();
                    for (final String s : split) {
                        accountNameBuilder.append("/").append(s);
                    }

                    return "https://www.hotslogs.com/API/Players" + accountNameBuilder.toString();
                }).collect(Collectors.toList());
    }

    public String getHotSAccountFilter() {
        return hotsAccountFilter;
    }

    private File buildHotSHome() {
        String hotsHome = System.getProperty("hots.home");
        if (hotsHome != null) {
            return new File(hotsHome);
        } else {
            return platformService.getHotSHome();
        }
    }

    private static long maxLastModified(File dir) {
        File[] files = getReplayDirectory(dir).listFiles();
        if (files == null || files.length < 1) return Long.MIN_VALUE;
        return Arrays.stream(files)
                .mapToLong(File::lastModified)
                .max().orElse(Long.MIN_VALUE);
    }

    /**
     * Retrieves a {@link List} of {@link File}s, each containing files for a specific {@link Account}.
     * @return {@link List} of directories or an empty {@link List}
     */
    private List<File> getAccountDirectories() {
        final List<File> hotsAccounts = new ArrayList<>();
        File[] files = getHotSHome().listFiles((dir, name) -> name.matches(ACCOUNT_FOLDER_FILTER));
        if (files == null) {
            files = new File[0];
        }
        for (final File file : files) {
            final File[] hotsFolders = file.listFiles((dir, name) -> name.matches(hotsAccountFilter));
            Arrays.stream(hotsFolders).forEach(hotsAccounts::add);
        }

         return hotsAccounts.stream()
                .sorted((f1, f2) -> Long.compare(maxLastModified(f2), maxLastModified(f1)))
                .collect(Collectors.toList());
    }
}
