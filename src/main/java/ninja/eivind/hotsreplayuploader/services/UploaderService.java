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
import ninja.eivind.stormparser.StormParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

    private static final Logger logger = LoggerFactory.getLogger(UploaderService.class);
    private final StringProperty uploadedCount = new SimpleStringProperty("0");
    private final BlockingQueue<ReplayFile> uploadQueue;
    private final ObservableList<ReplayFile> files;
    @Autowired
    private AccountDirectoryWatcher watcher;
    @Autowired
    private FileRepository fileRepository;
    @Autowired
    private ProviderRepository providerRepository;
    @Autowired
    private StormParser parser;

    public UploaderService() throws IOException {
        logger.info("Instantiating " + getClass().getSimpleName());
        uploadQueue = new LinkedBlockingQueue<>();
        files = FXCollections.observableArrayList();
        setExecutor(Executors.newCachedThreadPool());
        logger.info("Instantiated " + getClass().getSimpleName());
    }

    @Override
    public void afterPropertiesSet() {
        logger.info("Initializing " + getClass().getSimpleName());
        watcher.addFileListener(file -> {
            fileRepository.updateReplay(file);
            files.add(file);
            uploadQueue.add(file);
        });

        registerInitial();
        logger.info("Initialized " + getClass().getSimpleName());
    }

    private void registerInitial() {
        logger.info("Registering initial replays.");
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
        logger.info("Registering not yet uploaded replays.");
        files.addAll(
                mapped.stream()
                        .filter(replayFile -> replayFile.getStatus() == Status.NEW
                                || replayFile.getStatus() == Status.EXCEPTION)
                        .collect(Collectors.toList()));
    }

    private void updateUploadedCount(final List<ReplayFile> mapped) {
        logger.info("Updating counter for uploaded replays.");
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
        if (isIdle()) {
            return new Task<ReplayFile>() {
                @Override
                protected ReplayFile call() throws Exception {
                    Thread.sleep(2000);
                    return null;
                }
            };
        }
        try {
            logger.info("Attempting to take file from queue");
            final ReplayFile replayFile = uploadQueue.take();
            if (!replayFile.getFile().exists()) {
                fileRepository.deleteReplay(replayFile);
                files.remove(replayFile);
                return createTask();
            }
            final UploadTask uploadTask = new UploadTask(providerRepository.getAll(), replayFile, parser);

            uploadTask.setOnSucceeded(event -> {
                try {
                    final ReplayFile result = uploadTask.get();
                    final Status status = result.getStatus();
                    logger.info("Resolved status {} for {}", status, result);
                    switch (status) {
                        case UPLOADED:
                            final int newCount = Integer.valueOf(uploadedCount.getValue()) + 1;
                            uploadedCount.setValue(String.valueOf(newCount));
                            logger.info("Upload count updated to " + newCount);
                        case UNSUPPORTED_GAME_MODE:
                            result.getFailedProperty().setValue(false);
                            logger.info("Removing {} from display list", result);
                            files.remove(result);
                            break;
                        case EXCEPTION:
                        case NEW:
                            logger.warn("Upload failed for replay " + result + ". Tagging replay.");
                            result.getFailedProperty().set(true);
                            break;
                    }
                    logger.info("Updating {} in database.", result);
                    fileRepository.updateReplay(result);
                    logger.info("Finished handling file.");
                } catch (InterruptedException | ExecutionException e) {
                    logger.error("Could not execute task successfully.", e);
                }
            });
            uploadTask.setOnFailed(event -> {
                logger.error("UploadTask failed.", event.getSource().getException());
                uploadQueue.add(replayFile);
            });
            logger.info("Prepared task");
            return uploadTask;
        } catch (InterruptedException e) {
            logger.warn("Service interrupted while waiting for task", e);
            return null;
        }
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
        logger.info("Replay " + item + " invalidated and marked as new.");
    }

    public void deleteReplay(ReplayFile item) {
        files.remove(item);

        fileRepository.deleteReplay(item);
    }

    public ObservableList<ReplayFile> getFiles() {
        return files;
    }

}
