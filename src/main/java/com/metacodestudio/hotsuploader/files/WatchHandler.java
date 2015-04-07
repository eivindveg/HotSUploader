package com.metacodestudio.hotsuploader.files;

import javafx.application.Platform;
import javafx.collections.ObservableList;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Map;
import java.util.Queue;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

public class WatchHandler implements Runnable {

    private WatchService watchService;
    private Map<Status, ObservableList<ReplayFile>> fileMap;
    private Queue<ReplayFile> uploadQueue;

    public WatchHandler(final WatchService watchService, final Map<Status, ObservableList<ReplayFile>> fileMap, final Queue<ReplayFile> uploadQueue) {
        this.watchService = watchService;
        this.fileMap = fileMap;
        this.uploadQueue = uploadQueue;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        for (; ; ) {
            WatchKey key;
            try {
                key = watchService.take();
            } catch (InterruptedException e) {
                return;
            }

            for (final WatchEvent<?> watchEvent : key.pollEvents()) {
                WatchEvent.Kind<?> kind = watchEvent.kind();
                if (kind == OVERFLOW || kind != ENTRY_CREATE) {
                    continue;
                }
                WatchEvent<Path> event = (WatchEvent<Path>) watchEvent;
                final Path fileName = event.context();

                File file = fileName.toFile();
                if(!file.getName().endsWith(".StormReplay")) {
                    continue;
                }

                long stamp = 0;
                try {
                    do {
                        System.out.println("Waiting");
                        System.out.println(file.getName());

                        Thread.sleep(10000L);
                        System.out.println(stamp);
                        System.out.println(System.currentTimeMillis() - 20000L);
                    } while ((stamp = file.lastModified()) > System.currentTimeMillis() - 20000L);
                    ReplayFile replayFile = new ReplayFile(file);
                    Platform.runLater(() -> fileMap.get(Status.NEW).add(replayFile));
                    uploadQueue.add(replayFile);
                } catch (InterruptedException e) {
                    break;
                }

                boolean valid = key.reset();
                if (!valid) {
                    break;
                }
            }
        }
    }
}
