package com.metacodestudio.hotsuploader.versions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.metacodestudio.hotsuploader.models.ReplayFile;
import com.metacodestudio.hotsuploader.utils.FileUtils;
import com.metacodestudio.hotsuploader.utils.SimpleHttpClient;
import com.metacodestudio.hotsuploader.utils.StormHandler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReleaseManager {

    protected static final String CURRENT_VERSION = "1.2.2";
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
        String htmlUrl = getCurrentReleaseString();
        return new GitHubRelease(CURRENT_VERSION, htmlUrl, false);
    }

    public GitHubRelease getNewerVersionIfAny() {
        ReleaseComparator releaseComparator = new ReleaseComparator();
        List<GitHubRelease> latest;
        try {
            latest = getLatest();
        } catch (IOException e) {
            System.out.println("Unable to get latest versions");
            return null;
        }
        latest.sort(releaseComparator);

        GitHubRelease latestRelease = latest.get(0);
        int compare = releaseComparator.compare(currentRelease, latestRelease);
        if (latest.size() > 0 && compare > 0) {
            return latestRelease;
        }

        return null;
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
                .replace("{version}", CURRENT_VERSION);
    }

    public void verifyLocalVersion(final StormHandler stormHandler) {
        try {
            File file = new File(stormHandler.getApplicationHome(), "model_version");
            Long modelVersion;
            if (file.exists()) {
                System.out.println("Reading model version");
                String fileContent = FileUtils.readFileToString(file);
                modelVersion = Long.valueOf(fileContent);

                if (modelVersion < ReplayFile.getSerialVersionUID()) {
                    // TODO IMPLEMENT MIGRATION

                    FileUtils.writeStringToFile(file, String.valueOf(modelVersion));
                }
            } else {
                // Assume first run
                System.out.println("First run: assigning model version");
                FileUtils.writeStringToFile(file, String.valueOf(ReplayFile.getSerialVersionUID()));
            }
        } catch (IOException e) {
            // Expect this to be first run?
            e.printStackTrace();
        }
    }

    public String getCurrentVersion() {
        return CURRENT_VERSION;
    }
}
