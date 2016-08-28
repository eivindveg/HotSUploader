// Copyright 2016 Eivind Vegsundvåg
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


package ninja.eivind.hotsreplayuploader.files.tempwatcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.*;
import java.util.function.Consumer;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

public class BattleLobbyWatcher {
    private static final Logger logger = LoggerFactory.getLogger(BattleLobbyWatcher.class);
    public static final String REPLAY_SERVER_BATTLELOBBY = "replay.server.battlelobby";
    public static final long DELAY = 3000L;
    private Thread watcherThread;
    private File heroesDirectory;
    private FilenameFilter fileNameFilter;
    private Consumer<File> callback;

    public BattleLobbyWatcher(File heroesDirectory) {
        this.heroesDirectory = heroesDirectory;
        fileNameFilter = (dir, name) -> name.contains("TempWriteReplayP");
    }

    public void start() {
        logger.info("BattleLobbyWatcher starting...");
        Path path = heroesDirectory.toPath();

        new Thread(() -> {
            try {
                Thread.sleep(DELAY);
                File[] files = heroesDirectory.listFiles(fileNameFilter);
                for (File file : files != null ? files : new File[0]) {
                    File target = new File(file, REPLAY_SERVER_BATTLELOBBY);
                    if(target.exists()) {
                        handleFile(target);
                    }
                }
            } catch (InterruptedException ignored) {
            }
        }).start();

        Runnable runnable = getRunnable(path);
        watcherThread = new Thread(runnable);
        watcherThread.start();
    }

    private Runnable getRunnable(Path path) {
        return () -> {
            logger.info("BattleLobbyWatcher started");
            try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
                path.register(watchService, ENTRY_CREATE);
                while (true) {
                    WatchKey key = watchService.take();
                    key.pollEvents().forEach(event -> {
                        WatchEvent.Kind<?> kind = event.kind();
                        final WatchEvent<Path> pathEvent = (WatchEvent<Path>) event;
                        final Path pathName = pathEvent.context();
                        logger.info("Received " + kind + " for path " + pathName);
                        if (kind == OVERFLOW) {
                            return;
                        }
                        File file = new File(path.toFile(), pathName.toString());
                        String fileName = file.getName();
                        if (fileNameFilter.accept(path.toFile(), fileName)) {
                            File target = new File(file, REPLAY_SERVER_BATTLELOBBY);
                            handleFile(target);
                        }
                    });
                    if (!key.reset()) {
                        break;
                    }
                }
            } catch (IOException e) {
                logger.error("Watcher threw exception", e);
            } catch (InterruptedException e) {
                logger.info("TempReplayWatcher was interrupted. Winding down.", e);
            }
        };
    }

    private void handleFile(File target) {
        logger.info("Eureka! {} is discovered!", target);
        new Thread(() -> callback.accept(target)).start();
    }

    public void stop() {
        logger.info("Game exited. Stopping BattleLobbyWatcher");
        watcherThread.interrupt();
        watcherThread = null;
    }

    public void setCallback(Consumer<File> callback) {
        this.callback = callback;
    }
}
