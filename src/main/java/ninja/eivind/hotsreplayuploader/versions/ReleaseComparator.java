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

import java.util.Comparator;

/**
 * {@link Comparator} for ordering {@link GitHubRelease}s. Will take several matters into consideration:
 * <p/>
 * The version string
 * Prerelease status
 * <p/>
 * The end result will be releases before pre-releases, sorted by version String
 */
public class ReleaseComparator implements Comparator<GitHubRelease> {

    /**
     * @param release1 The first {@link GitHubRelease}
     * @param release2 The second {@link GitHubRelease}
     * @return 1 if the second release is considered newer than the first
     */
    @Override
    public int compare(final GitHubRelease release1, final GitHubRelease release2) {
        if (release1.isPrerelease() && !release2.isPrerelease()) {
            return 1;
        } else if (!release1.isPrerelease() && release2.isPrerelease()) {
            return -1;
        }

        final String release1Version = stripVersion(release1.getTagName());
        final String release2Version = stripVersion(release2.getTagName());

        return -release1Version.compareTo(release2Version);
    }

    private static String stripVersion(final String tagName) {
        return tagName.replaceFirst("v", "").replaceAll("-\\w*", "");
    }
}
