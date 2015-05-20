package com.metacodestudio.hotsuploader.versions;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.joda.time.DateTime;

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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    @JsonProperty("published_at")
    private DateTime publishedAt;

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

    public DateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(final DateTime publishedAt) {
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
