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

/**
 * Service object that manages a single {@link WatchService}. This handler manages new {@link ReplayFile}s as they are
 * detected as {@link File}s, mapping them into correct models and notifying
 * {@link ninja.eivind.hotsreplayuploader.services.UploaderService}
 */
public class WatchHandler implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(WatchHandler.class);
    private final WatchService watchService;
    private final List<FileListener> fileListeners;
    private final Path path;

    public WatchHandler(final Path path) throws IOException {
        fileListeners = new ArrayList<>();
        this.path = path;
        watchService = FileSystems.getDefault().newWatchService();
        path.register(watchService, ENTRY_CREATE);

    }

    @Override
    public void run() {
        WatchKey key = null;
        while (true) {
            if (key != null) {
                if (!key.reset()) {
                    break;
                }
            }
            try {
                key = watchService.take();
            } catch (InterruptedException e) {
                LOG.info(getClass().getSimpleName() + " was interrupted. Winding down thread.");
                break;
            }
            for (final WatchEvent<?> watchEvent : key.pollEvents()) {
                if (!handleEvent(watchEvent)) {
                    continue;
                }
                final boolean valid = key.reset();
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

    @SuppressWarnings("unchecked")
    private boolean handleEvent(WatchEvent<?> watchEvent) {
        final WatchEvent.Kind<?> kind = watchEvent.kind();
        if (kind == OVERFLOW) {
            return false;
        }
        final WatchEvent<Path> event = (WatchEvent<Path>) watchEvent;
        final Path fileName = event.context();
        LOG.info("Received " + kind + " for path " + fileName);

        final File file = new File(path.toFile(), fileName.toString());
        if (!file.getName().endsWith(".StormReplay")) {
            return false;
        }
        final ReplayFile replayFile = getReplayFileForEvent(kind, file);
        Platform.runLater(() -> {
            fileListeners.forEach(fileListener -> fileListener.handle(replayFile));
            LOG.info("File " + replayFile + " registered with listeners.");
        });
        return true;
    }

    private ReplayFile getReplayFileForEvent(final WatchEvent.Kind<?> kind, final File file) {
        final Handler handler;
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

        private Handler(File target) {
            this.target = target;
        }

        protected File getTarget() {
            return target;
        }

        public abstract ReplayFile getFile();

    }

    private class CreationHandler extends Handler {

        private CreationHandler(final File file) {
            super(file);
        }

        @Override
        public ReplayFile getFile() {
            try {
                Thread.sleep(5000L);
            } catch (InterruptedException e) {
                LOG.warn("Thread interrupted while awaiting file stabilization.", e);
            }
            final File file = new File(getTarget().toString());
            return new ReplayFile(file);
        }
    }

    private class ModificationHandler extends Handler {

        private ModificationHandler(File target) {
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
