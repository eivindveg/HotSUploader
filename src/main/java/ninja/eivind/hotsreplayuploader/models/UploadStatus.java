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

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import ninja.eivind.hotsreplayuploader.providers.Provider;

/**
 * Defines a more specific upload status by adding a specific host to an {@link Status}.
 */
@DatabaseTable
public class UploadStatus {

    @DatabaseField(generatedId = true)
    private Long id;
    @DatabaseField(width = 15)
    private String host;
    @DatabaseField
    private Status status;
    @DatabaseField(foreign = true, foreignAutoCreate = true)
    private ReplayFile replayFile;

    public UploadStatus() {

    }

    public UploadStatus(final Provider host) {
        this.host = host.getName();
        this.status = Status.NEW;
    }

    public UploadStatus(final String host, final Status status) {
        this.host = host;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getHost() {
        return host;
    }

    @Override
    public String toString() {
        return "UploadStatus{" +
                "host='" + host + '\'' +
                ", status=" + status +
                '}';
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(final Status status) {
        this.status = status;
    }
}
