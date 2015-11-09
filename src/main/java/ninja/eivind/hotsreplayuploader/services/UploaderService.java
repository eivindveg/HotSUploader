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

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * {@link ScheduledService}, that is responsible for uploading {@link ReplayFile}s
 * to {@link Provider}s. Does also take care of updating the UI in the process.
 */
public class UploaderService extends ScheduledService<ReplayFile> {

    private static final Logger LOG = LoggerFactory.getLogger(UploaderService.class);
    private final StringProperty uploadedCount = new SimpleStringProperty("0");
    private final BlockingQueue<ReplayFile> uploadQueue;
    @Inject
    private AccountDirectoryWatcher watcher;
    private ObservableList<ReplayFile> files;
    @Inject
    private FileRepository fileRepository;
    @Inject
    private ProviderRepository providerRepository;

    public UploaderService() throws IOException {
        LOG.info("Instantiating " + getClass().getSimpleName());
        uploadQueue = new ArrayBlockingQueue<>(2500);
        files = FXCollections.observableArrayList();
        LOG.info("Instantiated " + getClass().getSimpleName());
    }

    public void init() {
        LOG.info("Initializing " + getClass().getSimpleName());
        watcher.addFileListener(file -> {
            files.add(file);
            uploadQueue.add(file);
        });
        registerInitial();
        LOG.info("Initialized " + getClass().getSimpleName());
    }

    public void registerInitial() {
        LOG.info("Registering initial replays.");
        List<ReplayFile> fileList = fileRepository.getAll();
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
        files = FXCollections.observableArrayList(
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
            ReplayFile take = uploadQueue.take();
            UploadTask uploadTask = new UploadTask(providerRepository.getAll(), take);
            final Status oldStatus = take.getStatus();

            uploadTask.setOnSucceeded(event -> {
                try {
                    ReplayFile replayFile = uploadTask.get();
                    Status status = replayFile.getStatus();
                    if (status == oldStatus) {
                        return;
                    }
                    if (status == Status.UPLOADED) {
                        int oldCount = Integer.valueOf(uploadedCount.getValue());
                        int newCount = oldCount + 1;
                        LOG.info("Upload count updated to " + newCount);
                        uploadedCount.setValue(String.valueOf(newCount));
                        replayFile.getFailedProperty().setValue(false);
                        files.remove(replayFile);
                    } else if (status == Status.EXCEPTION) {
                        LOG.warn("Upload failed for replay " + replayFile + ". Tagging replay.");
                        replayFile.getFailedProperty().set(true);
                    }
                    fileRepository.updateReplay(replayFile);
                } catch (InterruptedException | ExecutionException e) {
                    LOG.error("Could not execute task success.", e);
                }
            });
            uploadTask.setOnFailed(event -> {
                LOG.error("UploadTask failed.", event.getSource().getException());
                uploadQueue.add(take);
            });
            return uploadTask;
        } catch (InterruptedException e) {
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
