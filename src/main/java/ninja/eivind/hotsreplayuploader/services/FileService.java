package ninja.eivind.hotsreplayuploader.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import ninja.eivind.hotsreplayuploader.files.AccountDirectoryWatcher;
import ninja.eivind.hotsreplayuploader.files.UploadTask;
import ninja.eivind.hotsreplayuploader.models.ReplayFile;
import ninja.eivind.hotsreplayuploader.models.Status;
import ninja.eivind.hotsreplayuploader.repositories.FileRepository;
import ninja.eivind.hotsreplayuploader.repositories.ProviderRepository;
import ninja.eivind.hotsreplayuploader.utils.StormHandler;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class FileService extends ScheduledService<ReplayFile> {

    private final StringProperty uploadedCount = new SimpleStringProperty("0");
    private final BlockingQueue<ReplayFile> uploadQueue;
    @Inject
    private AccountDirectoryWatcher watcher;
    private ObservableList<ReplayFile> files;
    @Inject
    private FileRepository fileRepository;
    @Inject
    private ProviderRepository providerRepository;

    public FileService() throws IOException {
        uploadQueue = new ArrayBlockingQueue<>(2500);
        files = FXCollections.observableArrayList();
    }

    public void init() {
        System.out.println("Initializing FileHandler");
        watcher.addFileListener(file -> {
            files.add(file);
            uploadQueue.add(file);
        });
        registerInitial();
    }

    public void registerInitial() {
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
        files = FXCollections.observableArrayList(
                mapped.stream()
                        .filter(replayFile -> replayFile.getStatus() == Status.NEW
                                || replayFile.getStatus() == Status.EXCEPTION)
                        .collect(Collectors.toList()));
    }

    private void updateUploadedCount(final List<ReplayFile> mapped) {
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
                        uploadedCount.setValue(String.valueOf(newCount));
                        replayFile.getFailedProperty().setValue(false);
                        files.remove(replayFile);
                    } else if (status == Status.EXCEPTION) {
                        replayFile.getFailedProperty().set(true);
                    }
                    fileRepository.updateReplay(replayFile);
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            });
            uploadTask.setOnFailed(event -> uploadQueue.add(take));
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
    }

    public void deleteReplay(ReplayFile item) {
        files.remove(item);

        fileRepository.deleteReplay(item);
    }

    public ObservableList<ReplayFile> getFiles() {
        return files;
    }

}
