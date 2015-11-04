package ninja.eivind.hotsreplayuploader.repositories;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ninja.eivind.hotsreplayuploader.models.ReplayFile;
import ninja.eivind.hotsreplayuploader.utils.FileUtils;
import ninja.eivind.hotsreplayuploader.utils.StormHandler;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;

public class JsonStoreFileRepository implements FileRepository {

    @Inject
    private StormHandler stormHandler;
    @Inject
    private ObjectMapper mapper;

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
        String data = null;
        try {
            data = mapper.writeValueAsString(file.getUploadStatuses());
            FileUtils.writeStringToFile(propertiesFile, data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
