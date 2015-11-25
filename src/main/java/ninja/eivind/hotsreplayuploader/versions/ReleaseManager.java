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

package ninja.eivind.hotsreplayuploader.versions;

import com.fasterxml.jackson.databind.ObjectMapper;

import ninja.eivind.hotsreplayuploader.utils.SimpleHttpClient;
import ninja.eivind.hotsreplayuploader.utils.StormHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ReleaseManager {

    private static final Logger LOG = LoggerFactory.getLogger(ReleaseManager.class);
    /**implementation version will be set by the maven build, so may be null at development**/
    protected static final String CURRENT_VERSION
            = ReleaseManager.class.getPackage().getImplementationVersion();
    protected static final String GITHUB_MAINTAINER = "eivindveg";
    protected static final String GITHUB_REPOSITORY = "HotSUploader";
    protected static final String GITHUB_RELEASES_ALL
            = "https://api.github.com/repos/{maintainer}/{repository}/releases";
    protected static final String GITHUB_FORMAT_VERSION
            = "http://github.com/{maintainer}/{repository}/releases/tag/{version}";
    @Inject
    private ObjectMapper objectMapper;
    private final GitHubRelease currentRelease;

    @Inject
    private SimpleHttpClient httpClient;

    @Inject
    public ReleaseManager() {
        currentRelease = buildCurrentRelease();
    }

    private GitHubRelease buildCurrentRelease() {
        String htmlUrl = getCurrentReleaseString();
        return new GitHubRelease(CURRENT_VERSION, htmlUrl, false);
    }

    public Optional<GitHubRelease> getNewerVersionIfAny() {
        ReleaseComparator releaseComparator = new ReleaseComparator();
        try {
            List<GitHubRelease> latest = getLatest();
            latest.sort(releaseComparator);

            GitHubRelease latestRelease = latest.get(0);
            int compare = releaseComparator.compare(currentRelease, latestRelease);
            if (!latest.isEmpty() && compare > 0) {
                LOG.info("Newer  release is: " + latestRelease);
                return Optional.of(latestRelease);
            } else {
                LOG.info(currentRelease + " is the newest version.");
            }
        } catch (IOException e) {
            LOG.error("Unable to get latest versions", e);
        }
        return Optional.empty();
    }

    private List<GitHubRelease> getLatest() throws IOException {
        ArrayList<GitHubRelease> releases = new ArrayList<>();

        String apiUrl = getAllReleasesString();
        String response = httpClient.simpleRequest(apiUrl);
        releases.addAll(Arrays.asList(objectMapper.readValue(response, GitHubRelease[].class)));
        return releases;
    }

    protected String getAllReleasesString() {
        return GITHUB_RELEASES_ALL.replace("{maintainer}", GITHUB_MAINTAINER)
                .replace("{repository}", GITHUB_REPOSITORY);
    }

    protected String getCurrentReleaseString() {
        return GITHUB_FORMAT_VERSION.replace("{maintainer}", GITHUB_MAINTAINER)
                .replace("{repository}", GITHUB_REPOSITORY)
                .replace("{version}", getCurrentVersion());
    }

    public String getCurrentVersion() {
        return CURRENT_VERSION != null ? CURRENT_VERSION : "Development";
    }

    public void setHttpClient(SimpleHttpClient httpClient) {
        this.httpClient = httpClient;
    }
}
