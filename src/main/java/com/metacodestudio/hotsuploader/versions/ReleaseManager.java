package com.metacodestudio.hotsuploader.versions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.metacodestudio.hotsuploader.utils.SimpleHttpClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReleaseManager {

    protected static final String CURRENT_VERSION = "1.0-RELEASE";
    protected static final String GITHUB_MAINTAINER = "eivindveg";
    protected static final String GITHUB_REPOSITORY = "HotSUploader";
    protected static final String GITHUB_RELEASES_ALL
            = "https://api.github.com/repos/{maintainer}/{repository}/releases";
    protected static final String GITHUB_FORMAT_VERSION
            = "http://github.com/{maintainer}/{repository}/releases/tag/{version}";
    private final SimpleHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final GitHubRelease currentRelease;

    public ReleaseManager(SimpleHttpClient httpClient) {
        this.httpClient = httpClient;
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JodaModule());
        currentRelease = buildCurrentRelease();
    }

    private GitHubRelease buildCurrentRelease() {
        String htmlUrl = GITHUB_FORMAT_VERSION.replace("{maintainer}", GITHUB_MAINTAINER)
                .replace("{repository}", GITHUB_REPOSITORY)
                .replace("{version}", CURRENT_VERSION);
        return new GitHubRelease(CURRENT_VERSION, htmlUrl);
    }

    public GitHubRelease getNewerVersionIfAny() throws IOException {
        ReleaseComparator releaseComparator = new ReleaseComparator();
        List<GitHubRelease> latest = getLatest();
        latest.sort(releaseComparator);

        GitHubRelease latestRelease = latest.get(0);
        if(latest.size() > 0 && releaseComparator.compare(currentRelease, latestRelease) < 0) {
            return latestRelease;
        }

        return null;
    }

    private List<GitHubRelease> getLatest() throws IOException {
        ArrayList<GitHubRelease> releases = new ArrayList<>();

        String apiUrl = GITHUB_RELEASES_ALL.replace("{maintainer}", GITHUB_MAINTAINER)
                .replace("{repository}", GITHUB_REPOSITORY);
        String response = httpClient.simpleRequest(apiUrl);
        releases.addAll(Arrays.asList(objectMapper.readValue(response, GitHubRelease[].class)));
        return releases;
    }

}
