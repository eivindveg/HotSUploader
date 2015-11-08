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

package ninja.eivind.hotsreplayuploader.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ReplayFile implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(ReplayFile.class);
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
            LOG.warn(this + " has no statuses.");
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
