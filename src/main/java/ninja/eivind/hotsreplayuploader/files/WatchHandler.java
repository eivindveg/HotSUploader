package ninja.eivind.hotsreplayuploader.files;

import javafx.application.Platform;
import ninja.eivind.hotsreplayuploader.models.ReplayFile;
import ninja.eivind.hotsreplayuploader.utils.StormHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.StandardWatchEventKinds.*;

public class WatchHandler implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(WatchHandler.class);
    private final WatchService watchService;
    private final StormHandler stormHandler;
    private final List<FileListener> fileListeners;
    private final Path path;

    public WatchHandler(final StormHandler stormHandler, final Path path) throws IOException {
        fileListeners = new ArrayList<>();
        this.stormHandler = stormHandler;
        this.path = path;
        watchService = FileSystems.getDefault().newWatchService();
        path.register(watchService, ENTRY_CREATE);

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
                LOG.info("Received " + kind + " for path " + fileName);

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
                Platform.runLater(() -> {
                    fileListeners.forEach(fileListener -> fileListener.handle(replayFile));
                    LOG.info("File " + replayFile + " registered with listeners.");
                });
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

    public void addListener(final FileListener fileListener) {
        fileListeners.add(fileListener);
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
        }

        @Override
        public ReplayFile getFile() {
            try {
                Thread.sleep(5000L);
            } catch (InterruptedException e) {
                LOG.warn("Thread interrupted while awaiting file stabilization.", e);
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
                LOG.warn("Thread interrupted while awaiting file modification check.", e);
                return null;
            }
        }
    }

}
