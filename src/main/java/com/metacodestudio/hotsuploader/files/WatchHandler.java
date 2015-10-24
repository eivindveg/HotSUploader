package com.metacodestudio.hotsuploader.files;

import com.metacodestudio.hotsuploader.models.ReplayFile;
import com.metacodestudio.hotsuploader.models.Status;
import com.metacodestudio.hotsuploader.utils.StormHandler;
import javafx.application.Platform;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Queue;

import static java.nio.file.StandardWatchEventKinds.*;

public class WatchHandler implements Runnable {

    private final WatchService watchService;
    private final StormHandler stormHandler;
    private final List<ReplayFile> files;
    private final Queue<ReplayFile> uploadQueue;
    private final Path path;

    public WatchHandler(final StormHandler stormHandler, final Path path, final List<ReplayFile> files, final Queue<ReplayFile> uploadQueue) throws IOException {
        this.stormHandler = stormHandler;
        this.path = path;
        watchService = FileSystems.getDefault().newWatchService();
        path.register(watchService, ENTRY_CREATE);
        this.files = files;
        this.uploadQueue = uploadQueue;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        WatchKey key = null;
        for (; ; ) {
            if (key != null) {
                if (!key.reset()) {
                    break;
                }
            }
            try {
                key = watchService.take();
            } catch (InterruptedException e) {
                break;
            }
            for (final WatchEvent<?> watchEvent : key.pollEvents()) {
                WatchEvent.Kind<?> kind = watchEvent.kind();
                if (kind == OVERFLOW) {
                    continue;
                }
                WatchEvent<Path> event = (WatchEvent<Path>) watchEvent;
                final Path fileName = event.context();
                System.out.println("\t" + fileName);

                File file = new File(path.toFile(), fileName.toString());
                if (!file.getName().endsWith(".StormReplay")) {
                    continue;
                }
                ReplayFile replayFile = getReplayFileForEvent(kind, file);
                File propertiesFile = stormHandler.getPropertiesFile(file);
                if (propertiesFile.exists()) {
                    if (!propertiesFile.delete()) {
                        throw new RuntimeException(new IOException("Could not delete file"));
                    }
                }
                Platform.runLater(() -> files.add(replayFile));
                uploadQueue.add(replayFile);

                boolean valid = key.reset();
                if (!valid) {
                    break;
                }
            }
        }
        try {
            watchService.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private ReplayFile getReplayFileForEvent(final WatchEvent.Kind<?> kind, final File file) {
        Handler handler;
        if (kind == ENTRY_MODIFY) {
            handler = new ModificationHandler(file);
        } else {
            handler = new CreationHandler(file);
        }
        return handler.getFile();
    }

    private abstract class Handler {

        private final File target;

        public Handler(File target) {
            this.target = target;
        }

        protected File getTarget() {
            return target;
        }

        public abstract ReplayFile getFile();

    }

    private class CreationHandler extends Handler {

        public CreationHandler(final File file) {
            super(file);
            System.out.println("CreationHandler");
        }

        @Override
        public ReplayFile getFile() {
            try {
                Thread.sleep(5000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            File file = new File(getTarget().toString());
            return new ReplayFile(file);
        }
    }

    private class ModificationHandler extends Handler {

        public ModificationHandler(File target) {
            super(target);
        }

        @Override
        public ReplayFile getFile() {
            try {
                do {
                    Thread.sleep(10000L);
                } while (getTarget().lastModified() > System.currentTimeMillis() - 20000L);
                return new ReplayFile(new File(getTarget().toString()));
            } catch (InterruptedException e) {
                return null;
            }
        }
    }

}
