package ninja.eivind.hotsreplayuploader.repositories;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ninja.eivind.hotsreplayuploader.files.AccountDirectoryWatcher;
import ninja.eivind.hotsreplayuploader.models.ReplayFile;
import ninja.eivind.hotsreplayuploader.utils.FileUtils;
import ninja.eivind.hotsreplayuploader.utils.StormHandler;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class JsonStoreFileRepository implements FileRepository {

    @Inject
    private StormHandler stormHandler;
    @Inject
    private ObjectMapper mapper;
    @Inject
    private AccountDirectoryWatcher accountDirectoryWatcher;

    @Override
    public void deleteReplay(final ReplayFile replayFile) {
        File file = replayFile.getFile();
        if (file.delete()) {
            stormHandler.getPropertiesFile(file).delete();
        }
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
                .collect(Collectors.toList());
    }
}
