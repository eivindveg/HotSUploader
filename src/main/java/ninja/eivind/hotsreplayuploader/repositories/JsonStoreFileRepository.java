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

package ninja.eivind.hotsreplayuploader.repositories;

import com.fasterxml.jackson.databind.ObjectMapper;
import ninja.eivind.hotsreplayuploader.files.AccountDirectoryWatcher;
import ninja.eivind.hotsreplayuploader.models.ReplayFile;
import ninja.eivind.hotsreplayuploader.models.UploadStatus;
import ninja.eivind.hotsreplayuploader.utils.FileUtils;
import ninja.eivind.hotsreplayuploader.utils.StormHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class JsonStoreFileRepository implements FileRepository {

    private static final Logger LOG = LoggerFactory.getLogger(JsonStoreFileRepository.class);
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
            LOG.info("ReplayFile " + file + " successfully deleted.");
            if (stormHandler.getPropertiesFile(file).delete()) {
                LOG.info("Property file for " + file + " successfully deleted.");
            } else {
                LOG.error("Could not delete property file for " + file);
            }
        } else {
            LOG.error("ReplayFile " + file + " could not be deleted.");
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
                .map(stormHandler::getPropertiesFile)
                .forEach((file) -> {
                    if (file.delete()) {
                        LOG.info("Deleted property file" + file + " for non-existing replay file.");
                    }
                });
    }

    @Override
    public void updateReplay(final ReplayFile file) {
        File propertiesFile = stormHandler.getPropertiesFile(file.getFile());
        String data;
        try {
            data = mapper.writeValueAsString(file.getUploadStatuses());
            FileUtils.writeStringToFile(propertiesFile, data);
            LOG.info("Property file for " + file + " updated.");
        } catch (IOException e) {
            throw new RuntimeException("Could not update property file for replay " + file, e);
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
