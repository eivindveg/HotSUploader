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

package ninja.eivind.hotsreplayuploader.services;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import ninja.eivind.hotsreplayuploader.concurrent.tasks.UploadTask;
import ninja.eivind.hotsreplayuploader.files.AccountDirectoryWatcher;
import ninja.eivind.hotsreplayuploader.models.ReplayFile;
import ninja.eivind.hotsreplayuploader.models.Status;
import ninja.eivind.hotsreplayuploader.providers.Provider;
import ninja.eivind.hotsreplayuploader.repositories.FileRepository;
import ninja.eivind.hotsreplayuploader.repositories.ProviderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

/**
 * {@link ScheduledService}, that is responsible for uploading {@link ReplayFile}s
 * to {@link Provider}s. Does also take care of updating the UI in the process.
 */
@Component
public class UploaderService extends ScheduledService<ReplayFile> implements InitializingBean {

    private static final Logger LOG = LoggerFactory.getLogger(UploaderService.class);
    private final StringProperty uploadedCount = new SimpleStringProperty("0");
    private final BlockingQueue<ReplayFile> uploadQueue;
    private final ObservableList<ReplayFile> files;
    @Inject
    private AccountDirectoryWatcher watcher;
    @Inject
    private FileRepository fileRepository;
    @Inject
    private ProviderRepository providerRepository;

    public UploaderService() throws IOException {
        LOG.info("Instantiating " + getClass().getSimpleName());
        uploadQueue = new LinkedBlockingQueue<>();
        files = FXCollections.observableArrayList();
        setExecutor(Executors.newCachedThreadPool());
        LOG.info("Instantiated " + getClass().getSimpleName());
    }

    @Override
    public void afterPropertiesSet() {
        LOG.info("Initializing " + getClass().getSimpleName());
        watcher.addFileListener(file -> {
            fileRepository.updateReplay(file);
            files.add(file);
            uploadQueue.add(file);
        });

        registerInitial();
        LOG.info("Initialized " + getClass().getSimpleName());
    }

    private void registerInitial() {
        LOG.info("Registering initial replays.");
        final List<ReplayFile> fileList = fileRepository.getAll();
        uploadQueue.addAll(
                fileList.stream()
                        .filter(replayFile -> replayFile.getStatus() == Status.NEW)
                        .collect(Collectors.toList())
        );
        updateUploadedCount(fileList);
        getQueuableFiles(fileList);
    }


    private void getQueuableFiles(final List<ReplayFile> mapped) {
        LOG.info("Registering not yet uploaded replays.");
        files.addAll(
                mapped.stream()
                        .filter(replayFile -> replayFile.getStatus() == Status.NEW
                                || replayFile.getStatus() == Status.EXCEPTION)
                        .collect(Collectors.toList()));
    }

    private void updateUploadedCount(final List<ReplayFile> mapped) {
        LOG.info("Updating counter for uploaded replays.");
        uploadedCount.set(
                String.valueOf(
                        mapped.stream()
                                .filter(replayFile -> replayFile.getStatus() == Status.UPLOADED)
                                .count()
                )
        );
    }

    @Override
    protected Task<ReplayFile> createTask() {
            final UploadTask uploadTask = new UploadTask(providerRepository.getAll(), uploadQueue);

            uploadTask.setOnSucceeded(event -> {
                try {
                    final ReplayFile replayFile = uploadTask.get();
                    final Status status = replayFile.getStatus();

                    switch (status) {
                        case UPLOADED:
                            final int newCount = Integer.valueOf(uploadedCount.getValue()) + 1;
                            LOG.info("Upload count updated to " + newCount);
                            uploadedCount.setValue(String.valueOf(newCount));
                        case UNSUPPORTED_GAME_MODE:
                            replayFile.getFailedProperty().setValue(false);
                            files.remove(replayFile);
                            break;
                        case EXCEPTION:
                        case NEW:
                            LOG.warn("Upload failed for replay " + replayFile + ". Tagging replay.");
                            replayFile.getFailedProperty().set(true);
                            break;
                    }
                    fileRepository.updateReplay(replayFile);
                } catch (InterruptedException | ExecutionException e) {
                    LOG.error("Could not execute task successfully.", e);
                }
            });

            return uploadTask;
    }

    public boolean isIdle() {
        return uploadQueue.isEmpty();
    }

    public StringProperty getUploadedCount() {
        return uploadedCount;
    }

    public void invalidateReplay(ReplayFile item) {
        item.getUploadStatuses()
                .stream()
                .filter(status -> status.getStatus() != Status.UPLOADED)
                .forEach(status -> status.setStatus(Status.NEW));
        uploadQueue.add(item);
        LOG.info("Replay " + item + " invalidated and marked as new.");
    }

    public void deleteReplay(ReplayFile item) {
        files.remove(item);

        fileRepository.deleteReplay(item);
    }

    public ObservableList<ReplayFile> getFiles() {
        return files;
    }

}
