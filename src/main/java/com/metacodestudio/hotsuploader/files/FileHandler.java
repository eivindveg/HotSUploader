package com.metacodestudio.hotsuploader.files;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.metacodestudio.hotsuploader.models.ReplayFile;
import com.metacodestudio.hotsuploader.models.Status;
import com.metacodestudio.hotsuploader.models.UploadStatus;
import com.metacodestudio.hotsuploader.providers.Provider;
import com.metacodestudio.hotsuploader.utils.FileUtils;
import com.metacodestudio.hotsuploader.utils.StormHandler;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class FileHandler extends ScheduledService<ReplayFile> {

    private final List<File> watchDirectories;
    private final ObjectMapper mapper;
    private final StormHandler stormHandler;
    private Map<Status, ObservableList<ReplayFile>> fileMap;
    private List<Provider> providers = Provider.getAll();
    private BlockingQueue<ReplayFile> uploadQueue;

    public FileHandler(final StormHandler stormHandler) throws IOException {
        this.stormHandler = stormHandler;
        mapper = new ObjectMapper();
        uploadQueue = new ArrayBlockingQueue<>(2500);
        fileMap = new HashMap<>();
        watchDirectories = stormHandler.getAccountDirectories(stormHandler.getHotSHome());

        cleanup();
        registerInitial();
        watchDirectories.stream().map(file -> Paths.get(file.toString())).forEach(path -> {
            try {
                WatchHandler watchHandler = new WatchHandler(stormHandler, path, fileMap, uploadQueue);
                new Thread(watchHandler).start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void cleanup() {
        List<File> accounts = stormHandler.getAccountDirectories(new File(stormHandler.getApplicationHome(), "Accounts"));
        accounts.stream()
                .flatMap(folder -> {
                    File[] children = folder.listFiles();
                    return Arrays.asList(children != null ? children : new File[0]).stream();
                }).map(stormHandler::getReplayFile)
                .filter(file -> !file.exists())
                .map(stormHandler::getPropertiesFile).forEach(File::delete);
    }

    private void registerInitial() {
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
        verifyMap(fileMap);
    }

    private void verifyMap(Map<Status, ObservableList<ReplayFile>> fileMap) {
        Status[] keys = Status.values();
        for (final Status key : keys) {
            if (!fileMap.containsKey(key)) {
                fileMap.put(key, FXCollections.observableArrayList());
            }
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
                    fileMap.get(oldStatus).remove(replayFile);
                    fileMap.get(status).add(replayFile);
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
}
