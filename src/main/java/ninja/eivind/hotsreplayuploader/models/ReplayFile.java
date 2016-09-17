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
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import ninja.eivind.hotsreplayuploader.providers.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Repesents a physical replay file, which can be parsed and uploaded to {@link Provider}s.<br>
 * Also keeps track of this file's {@link UploadStatus}.
 */
@DatabaseTable(tableName = "ReplayFile")
public class ReplayFile implements Serializable {
    private static final Logger LOG = LoggerFactory.getLogger(ReplayFile.class);
    private static final long serialVersionUID = 5416365418274150142L;
    private final BooleanProperty failedProperty = new SimpleBooleanProperty(null, "failed", false);
    @DatabaseField(generatedId = true)
    private long id;
    private File file;
    @DatabaseField(width = 1023, unique = true)
    private String fileName;

    @ForeignCollectionField(eager = true)
    private Collection<UploadStatus> uploadStatuses = new ArrayList<>();

    public ReplayFile() {}

    public ReplayFile(final File file) {
        this.file = file;
        this.fileName = file.toString();
    }

    public static List<ReplayFile> fromDirectory(File file) {
        final List<ReplayFile> replayFiles = new ArrayList<>();
        final File[] children = file.listFiles((dir, name) -> name.endsWith(".StormReplay"));
        for (final File child : children) {
            if (child.isDirectory()) {
                replayFiles.addAll(fromDirectory(child));
            } else {
                replayFiles.add(new ReplayFile(child));
            }
        }

        return replayFiles;
    }

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public BooleanProperty getFailedProperty() {
        return failedProperty;
    }

    @Override
    public String toString() {
        return getFile().getName();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final ReplayFile that = (ReplayFile) o;

        return !(fileName != null ? !fileName.equals(that.fileName) : that.fileName != null);

    }

    @Override
    public int hashCode() {
        return file != null ? file.hashCode() : 0;
    }

    @JsonIgnore
    public Status getStatus() {
        // TODO MAKE MULTIPROVIDER-FRIENDLY
        if (uploadStatuses.isEmpty()) {
            return Status.NEW;
        }
        return uploadStatuses.iterator().next().getStatus();
    }

    public File getFile() {
        if (file == null && fileName != null) {
            file = new File(fileName);
        }
        return file;
    }

    public void addStatuses(final List<UploadStatus> list) {
        uploadStatuses.addAll(list);
    }

    public Collection<UploadStatus> getUploadStatuses() {
        return uploadStatuses;
    }

    public boolean hasExceptions() {
        return uploadStatuses.stream()
                .filter(uploadStatus -> uploadStatus.getStatus() == Status.EXCEPTION)
                .findAny()
                .isPresent();
    }

    public String getFileName() {
        return fileName;
    }

    public UploadStatus getUploadStatusForProvider(String provider) {
        return getUploadStatuses().stream()
                .filter(status -> status.getHost().equals(provider))
                .findFirst()
                .orElse(null);
    }
}
