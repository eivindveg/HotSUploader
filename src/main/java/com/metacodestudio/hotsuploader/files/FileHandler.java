package com.metacodestudio.hotsuploader.files;

import com.google.gson.Gson;
import com.metacodestudio.hotsuploader.providers.Provider;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.nio.file.WatchService;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

public class FileHandler extends Service<ReplayFile> {

    public static final String ACCOUNT_FOLDER_FILTER = "(\\d+[^A-Za-z,.\\-()\\s])";
    public static final String HOTS_ACCOUNT_FILTER = "(\\d-Hero-\\d-\\d{6})";
    private final List<File> watchDirectories;
    private WatchService watchService;
    private Map<Status, ObservableList<ReplayFile>> fileMap;
    private List<Provider> providers = Provider.getAll();
    private BlockingQueue<ReplayFile> uploadQueue;
    private final Gson gson;

    public FileHandler(final File root) throws IOException {
        gson = new Gson();
        uploadQueue = new ArrayBlockingQueue<>(2500);
        fileMap = new HashMap<>();
        watchDirectories = getWatchDirectories(root);
        watchDirectories.forEach(System.out::println);

        watchService = FileSystems.getDefault().newWatchService();
        watchDirectories.stream().map(file -> Paths.get(file.toString())).forEach(path -> {
            try {
                path.register(watchService, ENTRY_CREATE, ENTRY_MODIFY);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        registerInitial();
        WatchHandler watchHandler = new WatchHandler(watchService, fileMap, uploadQueue);
        new Thread(watchHandler).start();
    }

    @SuppressWarnings("unchecked")
    private void registerInitial() {
        List<ReplayFile> fileList = watchDirectories.stream()
                .map(ReplayFile::fromDirectory)
                .flatMap(List::stream)
                .collect(Collectors.toList());
        fileMap = fileList.stream()
                .map(file -> {
                    File propertiesFile = getPropertiesFile(file.getFile());
                    try {
                        if (propertiesFile.exists()) {
                            file.addStatuses(ReplayUtils.fromJson(gson.fromJson(FileUtils.readFileToString(propertiesFile), List.class)));
                        } else {
                            file.addStatuses(providers.stream()
                                    .map(UploadStatus::new)
                                    .collect(Collectors.toList()));
                            FileUtils.write(propertiesFile, gson.toJson(file.getUploadStatuses()));
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    if (file.getStatus() == Status.NEW) {
                        uploadQueue.add(file);
                    }

                    return file;
                }).collect(Collectors.groupingBy(ReplayFile::getStatus,
                ConcurrentHashMap::new,
                Collectors.toCollection(FXCollections::observableArrayList)));
        verifyMap();
    }

    private void verifyMap() {
        Status[] keys = Status.values();
        for (final Status key : keys) {
            if(!fileMap.containsKey(key)) {
                fileMap.put(key, FXCollections.observableArrayList());
            }
        }
    }

    private File getPropertiesFile(final File file) {
        String propertiesFileName = file.toString().replaceAll(".StormReplay", "") + ".json";
        return new File(propertiesFileName);
    }

    public void updateFile(ReplayFile file) throws IOException {
        File propertiesFile = getPropertiesFile(file.getFile());
        Gson gson = new Gson();
        String data = gson.toJson(file.getUploadStatuses());
        FileUtils.write(propertiesFile, data);
    }

    private List<File> getWatchDirectories(final File root) {
        List<File> hotsAccounts = new ArrayList<>();
        File[] files = root.listFiles((dir, name) -> name.matches(ACCOUNT_FOLDER_FILTER));

        for (final File file : files) {
            File[] hotsFolders = file.listFiles((dir, name) -> name.matches(HOTS_ACCOUNT_FILTER));
            Arrays.stream(hotsFolders)
                    .map(folder -> new File(folder, "Replays"))
                    .map(folder -> new File(folder, "Multiplayer"))
                    .forEach(hotsAccounts::add);
        }
        return hotsAccounts;
    }

    @Override
    protected Task<ReplayFile> createTask() {
        if(isIdle()) {
           return new Task<ReplayFile>() {
               @Override
               protected ReplayFile call() throws Exception {
                   Thread.sleep(60000);
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
                    ObservableList<ReplayFile> replayFiles = fileMap.get(oldStatus);
                    if(replayFiles != null) {
                        replayFiles.remove(replayFile);
                    }
                    replayFiles = fileMap.get(replayFile.getStatus());
                    if(replayFiles == null) {
                        replayFiles = FXCollections.observableArrayList();
                        fileMap.put(status, replayFiles);
                        replayFiles.add(replayFile);
                    }
                    replayFiles.add(replayFile);
                    updateFile(replayFile);
                } catch (InterruptedException | ExecutionException | IOException e) {
                    e.printStackTrace();
                }
            });
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
}
