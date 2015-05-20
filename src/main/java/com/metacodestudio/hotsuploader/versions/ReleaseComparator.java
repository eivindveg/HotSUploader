package com.metacodestudio.hotsuploader.versions;

import java.util.Comparator;

public class ReleaseComparator implements Comparator<GitHubRelease> {

    public int compare(final GitHubRelease release1, final GitHubRelease release2) {
        if (release1.isPrerelease() && !release2.isPrerelease()) {
            return 1;
        } else if (!release1.isPrerelease() && release2.isPrerelease()) {
            return -1;
        }

        String release1Version = stripVersion(release1.getTagName());
        String release2Version = stripVersion(release2.getTagName());

        return -release1Version.compareTo(release2Version);
    }

    private String stripVersion(final String tagName) {
        return tagName.replaceFirst("v", "").replaceAll("-\\w*", "");
    }
}
