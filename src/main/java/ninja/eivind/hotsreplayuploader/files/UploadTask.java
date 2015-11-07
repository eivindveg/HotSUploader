package ninja.eivind.hotsreplayuploader.files;

import javafx.concurrent.Task;
import ninja.eivind.hotsreplayuploader.models.ReplayFile;
import ninja.eivind.hotsreplayuploader.models.Status;
import ninja.eivind.hotsreplayuploader.models.UploadStatus;
import ninja.eivind.hotsreplayuploader.providers.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;

public class UploadTask extends Task<ReplayFile> {
    private static final Logger LOG = LoggerFactory.getLogger(UploadTask.class);
    private final Collection<Provider> providers;
    private final ReplayFile take;

    public UploadTask(final Collection<Provider> providers, final ReplayFile take) {
        this.providers = providers;
        this.take = take;
    }

    @Override
    protected ReplayFile call() throws Exception {
        LOG.info("Uploading replay " + take);
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