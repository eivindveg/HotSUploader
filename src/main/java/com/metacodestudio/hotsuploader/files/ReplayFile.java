package com.metacodestudio.hotsuploader.files;

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

    @JsonIgnore
    public Status getStatus() {
        // TODO MAKE MULTIPROVIDER-FRIENDLY
        if(uploadStatuses.size() < 1) {
            return Status.NEW;
        }
        return uploadStatuses.get(0).getStatus();
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
