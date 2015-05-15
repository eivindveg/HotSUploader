package com.metacodestudio.hotsuploader.versions;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class ReleaseComparatorTest {

    private ReleaseComparator comparator;

    @Before
    public void setup() {
        comparator = new ReleaseComparator();
    }

    @Test
    public void testPreReleaseIsNotNewest() {
        GitHubRelease preRelease = new GitHubRelease("v2.0-BETA", null);
        preRelease.setPrerelease(true);

        GitHubRelease normalRelease = new GitHubRelease("v1.0-RELEASE", null);
        preRelease.setPrerelease(false);

        int compare = comparator.compare(preRelease, normalRelease);
        assertTrue("Pre releases are not considered newer than a normal release", compare < 0);
    }

    @Test
    public void testNewerBugFixReleaseIsNewest() {
        GitHubRelease newRelease = new GitHubRelease("v2.0.1-RELEASE", null);
        newRelease.setPrerelease(false);

        GitHubRelease currentRelease = new GitHubRelease("v2.0-RELEASE", null);
        currentRelease.setPrerelease(false);

        int compare = comparator.compare(newRelease, currentRelease);
        assertTrue("Version 2.0.1 is newer than 2.0", compare > 0);
    }

    @Test
    public void testNewerGenerationalReleaseIsNewest() {
        GitHubRelease newRelease = new GitHubRelease("v2.0-RELEASE", null);
        newRelease.setPrerelease(false);

        GitHubRelease currentRelease = new GitHubRelease("v1.0-RELEASE", null);
        currentRelease.setPrerelease(false);

        int compare = comparator.compare(newRelease, currentRelease);
        assertTrue("Version 2.0 is newer than 1.0", compare > 0);
    }

    @Test
    public void testNewerFeatureReleaseIsNewest() {
        GitHubRelease newRelease = new GitHubRelease("v1.1-RELEASE", null);
        newRelease.setPrerelease(false);

        GitHubRelease currentRelease = new GitHubRelease("v1.0-RELEASE", null);
        currentRelease.setPrerelease(false);

        int compare = comparator.compare(newRelease, currentRelease);
        assertTrue("Version 1.1 is newer than 1.0", compare > 0);
    }

    @Test
    public void testOrderingOfList() {
        List<GitHubRelease> releases = new ArrayList<>();
        releases.add(new GitHubRelease("v1.0-RELEASE", null));
        releases.add(new GitHubRelease("v1.0.1-RELEASE", null));
        releases.add(new GitHubRelease("v1.1-RELEASE", null));
        releases.add(new GitHubRelease("v1.1.1-RELEASE", null));
        releases.add(new GitHubRelease("v2.0-RELEASE", null));
        releases.add(new GitHubRelease("v2.0.1-RELEASE", null));

        List<GitHubRelease> sortedList = new ArrayList<>(releases);
        sortedList.sort(comparator);

        for (int i = 0; i < releases.size(); i++) {
            GitHubRelease release1 = releases.get(i);
            GitHubRelease release2 = sortedList.get(i);

            assertSame("Comparator sorts according to expectations", release1, release2);
        }
    }
}
