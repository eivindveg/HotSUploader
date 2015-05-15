package com.metacodestudio.hotsuploader.versions;

import java.util.Comparator;

public class ReleaseComparator implements Comparator<GitHubRelease> {

    public int compare(final GitHubRelease release1, final GitHubRelease release2) {
        if (release1.isPrerelease() && !release2.isPrerelease()) {
            return 1;
        } else if (!release1.isPrerelease() && release2.isPrerelease()) {
            return -1;
        }

        double release1Version = stripVersion(release1.getTagName());
        double release2Version = stripVersion(release2.getTagName());

        return Double.compare(release1Version, release2Version);
    }

    private double stripVersion(final String tagName) {
        return Double.parseDouble(tagName.replaceAll("-\\w*", ""));
    }
}
