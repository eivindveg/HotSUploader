package com.metacodestudio.hotsuploader.files;

import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

public class WatchHandler implements Runnable {

    private WatchService watchService;
    private Map<Status, List<ReplayFile>> fileMap;
    private Queue<ReplayFile> uploadQueue;

    public WatchHandler(final WatchService watchService, final Map<Status, List<ReplayFile>> fileMap, final Queue<ReplayFile> uploadQueue) {
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

                final Path parent = fileName.getParent();
                if(!parent.endsWith("Multiplayer")) {
                    System.err.println("Improperly initialized");
                    break;
                }

                ReplayFile replayFile = new ReplayFile(fileName.toFile());

                fileMap.get(Status.NEW).add(replayFile);
                uploadQueue.add(replayFile);
            }

            boolean valid = key.reset();
            if (!valid) {
                break;
            }
        }
    }

}
