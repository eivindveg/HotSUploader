package com.metacodestudio.hotsuploader.files;

import com.metacodestudio.hotsuploader.models.ReplayFile;
import com.metacodestudio.hotsuploader.models.Status;
import com.metacodestudio.hotsuploader.models.UploadStatus;
import com.metacodestudio.hotsuploader.providers.Provider;
import javafx.concurrent.Task;

import java.util.List;

public class UploadTask extends Task<ReplayFile> {
    private final List<Provider> providers;
    private final ReplayFile take;

    public UploadTask(final List<Provider> providers, final ReplayFile take) {
        this.providers = providers;
        this.take = take;
    }

    @Override
    protected ReplayFile call() throws Exception {
        providers.forEach(provider -> {
            Status upload = provider.upload(take);
            if (upload == null) {
                throw new RuntimeException("Failed");
            }
            List<UploadStatus> uploadStatuses = take.getUploadStatuses();
            UploadStatus status = uploadStatuses.stream()
                    .filter(uploadStatus -> uploadStatus.getHost().equals(provider.getName()))
                    .findFirst()
                    .orElse(null);
            if (status == null) {
                uploadStatuses.add(new UploadStatus(provider.getName(), upload));
            } else {
                status.setStatus(upload);
            }
            succeeded();
        });
        return take;
    }
}
