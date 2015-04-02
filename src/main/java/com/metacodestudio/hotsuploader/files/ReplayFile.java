package com.metacodestudio.hotsuploader.files;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ReplayFile {

    private final File file;
    private Status status;

    public ReplayFile(final File file) {
        this.file = file;
        File parent = file.getParentFile();
        switch (parent.getName()) {
            case "uploaded":
                status = Status.UPLOADED;
                break;
            case "exception":
                status = Status.EXCEPTION;
                break;
            default:
                status = Status.NEW;
        }
    }

    public static List<ReplayFile> fromDirectory(File file) throws IOException {
        if (!file.isDirectory()) {
            throw new IOException("File must be directory");
        } else {
            List<ReplayFile> replayFiles = new ArrayList<>();
            File[] children = file.listFiles((dir, name) -> name.endsWith(".StormReplay"));
            for (final File child : children) {
                if (child.isDirectory()) {
                    replayFiles.addAll(fromDirectory(child));
                } else {
                    replayFiles.add(new ReplayFile(file));
                }
            }

            return replayFiles;
        }
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(final Status status) {
        this.status = status;
    }

    public File getFile() {
        return file;
    }
}
