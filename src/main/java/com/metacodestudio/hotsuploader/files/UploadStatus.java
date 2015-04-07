package com.metacodestudio.hotsuploader.files;

import com.metacodestudio.hotsuploader.providers.Provider;

public class UploadStatus {

    private String host;
    private Status status;

    public UploadStatus(final Provider host) {
        this.host = host.getName();
        this.status = Status.NEW;
    }

    public UploadStatus(final String host, final Status status) {
        this.host = host;
        this.status = status;
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
