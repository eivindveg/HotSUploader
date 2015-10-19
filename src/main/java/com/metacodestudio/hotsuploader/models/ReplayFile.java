package com.metacodestudio.hotsuploader.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ReplayFile implements Serializable {

    private static final long serialVersionUID = 1L;
    private final File file;
    private final List<UploadStatus> uploadStatuses;
    private final BooleanProperty failedProperty = new SimpleBooleanProperty(null, "failed", false);

    public ReplayFile(final File file) {
        uploadStatuses = new ArrayList<>();
        this.file = file;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
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

    public BooleanProperty getFailedProperty() {
        return failedProperty;
    }

    public BooleanProperty failedPropertyProperty() {
        return failedProperty;
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
        // TODO MAKE MULTIPROVIDER-FRIENDLY
        if (uploadStatuses.size() < 1) {
            System.out.println("Empty status list");
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

    public boolean hasExceptions() {
        return uploadStatuses.stream()
                .filter(uploadStatus -> uploadStatus.getStatus() == Status.EXCEPTION)
                .findAny()
                .isPresent();
    }
}
