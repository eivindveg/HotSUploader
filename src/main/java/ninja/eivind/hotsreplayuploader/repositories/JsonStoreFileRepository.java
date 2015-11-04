package ninja.eivind.hotsreplayuploader.repositories;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ninja.eivind.hotsreplayuploader.files.AccountDirectoryWatcher;
import ninja.eivind.hotsreplayuploader.models.ReplayFile;
import ninja.eivind.hotsreplayuploader.models.Status;
import ninja.eivind.hotsreplayuploader.models.UploadStatus;
import ninja.eivind.hotsreplayuploader.utils.FileUtils;
import ninja.eivind.hotsreplayuploader.utils.StormHandler;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class JsonStoreFileRepository implements FileRepository {

    private StormHandler stormHandler;
    @Inject
    private ObjectMapper mapper;
    @Inject
    private AccountDirectoryWatcher accountDirectoryWatcher;
    @Inject
    private ProviderRepository providerRepository;

    @Inject
    public JsonStoreFileRepository(StormHandler stormHandler) {
        this.stormHandler = stormHandler;
        cleanup();
    }

    @Override
    public void deleteReplay(final ReplayFile replayFile) {
        File file = replayFile.getFile();
        if (file.delete()) {
            stormHandler.getPropertiesFile(file).delete();
        }
    }

    private void cleanup() {
        List<File> accounts = stormHandler.getApplicationAccountDirectories();
        accounts.stream()
                .flatMap(folder -> {
                    File[] children = folder.listFiles();
                    return Arrays.asList(children != null ? children : new File[0]).stream();
                }).map(stormHandler::getReplayFile)
                .filter(file -> !file.exists())
                .map(stormHandler::getPropertiesFile).forEach(File::delete);
    }

    @Override
    public void updateReplay(final ReplayFile file) {
        File propertiesFile = stormHandler.getPropertiesFile(file.getFile());
        String data;
        try {
            data = mapper.writeValueAsString(file.getUploadStatuses());
            FileUtils.writeStringToFile(propertiesFile, data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ReplayFile> getAll() {
        return accountDirectoryWatcher.getAllFiles()
                .map(ReplayFile::fromDirectory)
                .flatMap(List::stream)
                .map(replay -> {
                    File propertiesFile = stormHandler.getPropertiesFile(replay.getFile());
                    try {
                        if (propertiesFile.exists()) {
                            String properties = FileUtils.readFileToString(propertiesFile);
                            replay.addStatuses(Arrays.asList(mapper.readValue(properties, UploadStatus[].class)));
                            if (replay.hasExceptions()) {
                                replay.getFailedProperty().set(true);
                            }
                        } else {
                            replay.addStatuses(providerRepository.getAll().stream()
                                    .map(UploadStatus::new)
                                    .collect(Collectors.toList()));
                            FileUtils.writeStringToFile(propertiesFile, mapper.writeValueAsString(replay.getUploadStatuses()));
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    return replay;
                })
                .collect(Collectors.toList());
    }
}
