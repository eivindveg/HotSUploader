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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.time.LocalDateTime;

/**
 * Utility model for communicating with Github.com's release API. Contains only the fields we need.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubRelease {

    @JsonProperty
    private String id;

    @JsonProperty("tag_name")
    private String tagName;

    @JsonProperty("html_url")
    private String htmlUrl;

    @JsonProperty
    private Boolean prerelease;
    @JsonProperty("published_at")
    private Instant publishedAt;

    public GitHubRelease() {
    }

    public GitHubRelease(final String tagName, final String htmlUrl, final boolean prerelease) {
        this.tagName = tagName;
        this.htmlUrl = htmlUrl;
        this.prerelease = prerelease;
    }

    @Override
    public String toString() {
        return "GitHubRelease{" +
                "id='" + id + '\'' +
                ", tagName='" + tagName + '\'' +
                ", htmlUrl='" + htmlUrl + '\'' +
                ", prerelease=" + prerelease +
                ", publishedAt=" + publishedAt +
                '}';
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(final Instant publishedAt) {
        this.publishedAt = publishedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(final String tagName) {
        this.tagName = tagName;
    }

    public String getHtmlUrl() {
        return htmlUrl;
    }

    public void setHtmlUrl(final String htmlUrl) {
        this.htmlUrl = htmlUrl;
    }

    public boolean isPrerelease() {
        if (prerelease == null) {
            return false;
        }
        return prerelease;
    }

    public void setPrerelease(final boolean prerelease) {
        this.prerelease = prerelease;
    }
}
