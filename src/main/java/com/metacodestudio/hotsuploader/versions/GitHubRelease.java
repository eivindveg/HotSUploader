package com.metacodestudio.hotsuploader.versions;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

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
    private LocalDateTime publishedAt;

    public GitHubRelease() {
    }

    public GitHubRelease(final String tagName, final String htmlUrl) {
        this.tagName = tagName;
        this.htmlUrl = htmlUrl;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(final LocalDateTime publishedAt) {
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
            return true;
        }
        return prerelease;
    }

    public void setPrerelease(final boolean prerelease) {
        this.prerelease = prerelease;
    }
}
