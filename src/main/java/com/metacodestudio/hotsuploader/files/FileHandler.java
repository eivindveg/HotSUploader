package com.metacodestudio.hotsuploader.files;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.metacodestudio.hotsuploader.models.ReplayFile;
import com.metacodestudio.hotsuploader.models.Status;
import com.metacodestudio.hotsuploader.models.UploadStatus;
import com.metacodestudio.hotsuploader.providers.Provider;
import com.metacodestudio.hotsuploader.utils.FileUtils;
import com.metacodestudio.hotsuploader.utils.StormHandler;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class FileHandler extends ScheduledService<ReplayFile> {

    private final Set<File> watchDirectories;
    private final ObjectMapper mapper;
    private final StormHandler stormHandler;
    private Map<Status, ObservableList<ReplayFile>> fileMap;
    private List<Provider> providers = Provider.getAll();
    private BlockingQueue<ReplayFile> uploadQueue;
    private final StringProperty uploadedCount;

    public FileHandler(final StormHandler stormHandler) throws IOException {
        uploadedCount = new SimpleStringProperty();
        watchDirectories = new HashSet<>();
        this.stormHandler = stormHandler;
        mapper = new ObjectMapper();
        uploadQueue = new ArrayBlockingQueue<>(2500);
        fileMap = new HashMap<>();
        watchDirectories.addAll(stormHandler.getAccountDirectories(stormHandler.getHotSHome()));
    }

    public void beginWatch() {
        watchDirectories.stream().map(file -> Paths.get(file.toString())).forEach(path -> {
            try {
                WatchHandler watchHandler = new WatchHandler(stormHandler, path, fileMap, uploadQueue);
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
        fileMap = fileList.stream()
                .map(replay -> {
                    File propertiesFile = stormHandler.getPropertiesFile(replay.getFile());
                    try {
                        if (propertiesFile.exists()) {
                            String properties = FileUtils.readFileToString(propertiesFile);
                            replay.addStatuses(Arrays.asList(mapper.readValue(properties, UploadStatus[].class)));
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
                }).collect(Collectors.groupingBy(ReplayFile::getStatus, ConcurrentHashMap::new,
                        Collectors.toCollection(FXCollections::observableArrayList)));
    }

    public void verifyMap(Map<Status, ObservableList<ReplayFile>> fileMap) {
        Status[] keys = Status.values();
        for (final Status key : keys) {
            if (!fileMap.containsKey(key)) {
                fileMap.put(key, FXCollections.observableArrayList());
            }
        }
        if(fileMap.containsKey(Status.UPLOADED)) {
            int size = fileMap.get(Status.UPLOADED).size();
            uploadedCount.setValue(String.valueOf(size));
            fileMap.remove(Status.UPLOADED);
        } else {
            uploadedCount.setValue(String.valueOf(0));
        }
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
                    Thread.sleep(20000);
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
                    if(status == Status.UPLOADED) {
                        int oldCount = Integer.valueOf(uploadedCount.getValue());
                        int newCount = oldCount + 1;
                        uploadedCount.setValue(String.valueOf(newCount));
                    } else {
                        fileMap.get(status).add(replayFile);
                    }
                    fileMap.get(oldStatus).remove(replayFile);
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

    public Map<Status, ObservableList<ReplayFile>> getFileMap() {
        return fileMap;
    }

    public boolean isIdle() {
        return uploadQueue.isEmpty();
    }

    public void invalidateByStatus(final Status status) {
        ObservableList<ReplayFile> replayFiles = fileMap.get(status);
        replayFiles.stream()
                .flatMap(replayFile -> replayFile.getUploadStatuses()
                        .stream())
                .forEach(uploadStatus -> uploadStatus.setStatus(Status.NEW));
        uploadQueue.addAll(replayFiles);
        Platform.runLater(() -> {
            fileMap.get(Status.NEW).addAll(replayFiles);
            replayFiles.clear();
        });
        restart();
    }

    public StringProperty getUploadedCount() {
        return uploadedCount;
    }

    public void invalidateReplay(ReplayFile item) {
        ObservableList<ReplayFile> replayFiles = fileMap.get(item.getStatus());
        replayFiles.remove(item);
        uploadQueue.add(item);
        fileMap.get(Status.NEW).add(item);
    }

    public void deleteReplay(ReplayFile item) {
        ObservableList<ReplayFile> replayFiles = fileMap.get(item.getStatus());
        replayFiles.remove(item);

        File file = item.getFile();
        if(file.delete()) {
            stormHandler.getPropertiesFile(file).delete();
        }
    }
}
