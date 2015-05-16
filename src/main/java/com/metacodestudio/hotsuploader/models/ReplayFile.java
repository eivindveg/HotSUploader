package com.metacodestudio.hotsuploader.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ReplayFile {

    private final File file;

    private final List<UploadStatus> uploadStatuses;

    public ReplayFile(final File file) {
        uploadStatuses = new ArrayList<>();
        this.file = file;
    }

    public static List<ReplayFile> fromDirectory(File file) {
        List<ReplayFile> replayFiles = new ArrayList<>();
        File[] children = file.listFiles((dir, name) -> name.endsWith(".StormReplay"));
        for (final File child : children) {
            if (child.isDirectory()) {
                replayFiles.addAll(fromDirectory(child));
            } else {
                replayFiles.add(new ReplayFile(child));
            }
        }

        return replayFiles;

    }

    @Override
    public String toString() {
        return file.getName();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final ReplayFile that = (ReplayFile) o;

        return !(file != null ? !file.equals(that.file) : that.file != null);

    }

    @Override
    public int hashCode() {
        return file != null ? file.hashCode() : 0;
    }

    @JsonIgnore
    public Status getStatus() {
        if (uploadStatuses.size() < 2) {
            System.out.println("Empty status list");
            return Status.NEW;
        }

        Status hotsLogStatus = uploadStatuses.get(0).getStatus();
        Status heroGGStatus = uploadStatuses.get(1).getStatus();
        if (heroGGStatus == Status.EXCEPTION || heroGGStatus == Status.NEW)
            return heroGGStatus;
        else
            return hotsLogStatus;
    }

    public File getFile() {
        return file;
    }

    public void addStatuses(final List<UploadStatus> list) {
        uploadStatuses.addAll(list);
    }

    public List<UploadStatus> getUploadStatuses() {
        return uploadStatuses;
    }
}
