package ninja.eivind.hotsreplayuploader.files;

import com.fasterxml.jackson.databind.ObjectMapper;
import ninja.eivind.hotsreplayuploader.models.ReplayFile;
import ninja.eivind.hotsreplayuploader.models.Status;
import ninja.eivind.hotsreplayuploader.models.UploadStatus;
import ninja.eivind.hotsreplayuploader.providers.Provider;
import ninja.eivind.hotsreplayuploader.utils.FileUtils;
import ninja.eivind.hotsreplayuploader.utils.StormHandler;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class FileHandler extends ScheduledService<ReplayFile> {

    private final Set<File> watchDirectories;
    private final ObjectMapper mapper;
    @Inject
    private StormHandler stormHandler;
    private final StringProperty uploadedCount = new SimpleStringProperty("0");
    private ObservableList<ReplayFile> files;
    private final List<Provider> providers = Provider.getAll();
    private final BlockingQueue<ReplayFile> uploadQueue;

    public FileHandler() throws IOException {
        watchDirectories = new HashSet<>();
        mapper = new ObjectMapper();
        uploadQueue = new ArrayBlockingQueue<>(2500);
        files = FXCollections.observableArrayList();
    }

    public void init() {
        System.out.println("Initializing FileHandler");
        watchDirectories.addAll(stormHandler.getAccountDirectories(stormHandler.getHotSHome()));
        cleanup();
        registerInitial();
    }

    public void beginWatch() {
        watchDirectories.stream().map(file -> Paths.get(file.toString())).forEach(path -> {
            try {
                WatchHandler watchHandler = new WatchHandler(stormHandler, path, files, uploadQueue);
                new Thread(watchHandler).start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void cleanup() {
        List<File> accounts = stormHandler.getAccountDirectories(new File(stormHandler.getApplicationHome(), "Accounts"));
        accounts.stream()
                .flatMap(folder -> {
                    File[] children = folder.listFiles();
                    return Arrays.asList(children != null ? children : new File[0]).stream();
                }).map(stormHandler::getReplayFile)
                .filter(file -> !file.exists())
                .map(stormHandler::getPropertiesFile).forEach(File::delete);
    }

    public void registerInitial() {
        List<ReplayFile> fileList = watchDirectories.stream()
                .map(ReplayFile::fromDirectory)
                .flatMap(List::stream)
                .collect(Collectors.toList());
        List<ReplayFile> mapped = mapFiles(fileList);
        updateUploadedCount(mapped);
        getQueuableFiles(mapped);
    }

    private List<ReplayFile> mapFiles(final List<ReplayFile> fileList) {
        return fileList.stream()
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
                                replay.addStatuses(providers.stream()
                                        .map(UploadStatus::new)
                                        .collect(Collectors.toList()));
                                FileUtils.writeStringToFile(propertiesFile, mapper.writeValueAsString(replay.getUploadStatuses()));
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        if (replay.getStatus() == Status.NEW) {
                            uploadQueue.add(replay);
                        }

                        return replay;
                    })
                    .collect(Collectors.toList());
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

    public void updateFile(ReplayFile file) throws IOException {
        File propertiesFile = stormHandler.getPropertiesFile(file.getFile());
        String data = mapper.writeValueAsString(file.getUploadStatuses());
        FileUtils.writeStringToFile(propertiesFile, data);
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
            UploadTask uploadTask = new UploadTask(providers, take);
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
                    updateFile(replayFile);
                } catch (InterruptedException | ExecutionException | IOException e) {
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

        File file = item.getFile();
        if (file.delete()) {
            stormHandler.getPropertiesFile(file).delete();
        }
    }

    public ObservableList<ReplayFile> getFiles() {
        return files;
    }

    public void setStormHandler(StormHandler stormHandler) {
        this.stormHandler = stormHandler;
    }
}
