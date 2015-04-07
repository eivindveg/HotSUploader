package com.metacodestudio.hotsuploader.files;

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
            System.out.println(upload);
            if (upload == null) {
                System.out.println("Cancelling");
                cancelled();
                return;
            }
            List<UploadStatus> uploadStatuses = take.getUploadStatuses();
            uploadStatuses.stream()
                    .filter(st -> st.getHost().equals(provider.getName()))
                    .findFirst()
                    .ifPresent(st -> st.setStatus(upload));
            succeeded();
        });
        return take;
    }
}
