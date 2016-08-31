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

import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.function.Consumer;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

public class BattleLobbyWatcher extends TempWatcher {
    public static final String REPLAY_SERVER_BATTLELOBBY = "replay.server.battlelobby";
    public static final long DELAY = 250L;
    private static final Logger logger = LoggerFactory.getLogger(BattleLobbyWatcher.class);
    private final File heroesDirectory;
    private final FilenameFilter fileNameFilter;
    private Consumer<File> callback;

    public BattleLobbyWatcher(File heroesDirectory, Consumer<File> callback) {
        this.heroesDirectory = heroesDirectory;
        this.callback = callback;
        fileNameFilter = (dir, name) -> name.contains("TempWriteReplayP");
    }

    @Override
    protected Task<Void> createTask() {
        logger.info("BattleLobbyWatcher starting...");
        Path path = heroesDirectory.toPath();

        new Thread(getInstantFileChecker()).start();

        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                logger.info("BattleLobbyWatcher started");
                try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
                    path.register(watchService, ENTRY_CREATE);
                    while (true) {
                        WatchKey key = watchService.take();
                        key.pollEvents().forEach(event -> {
                            WatchEvent.Kind<?> kind = event.kind();
                            @SuppressWarnings("unchecked")
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
                    logger.info("TempReplayWatcher was interrupted. Winding down.");
                }
                logger.info("Game exited. Stopping BattleLobbyWatcher");
                return null;
            }
        };
    }

    private Runnable getInstantFileChecker() {
        return () -> {
            try {
                LocalDateTime end = LocalDateTime.now().plus(10, ChronoUnit.SECONDS);
                while (LocalDateTime.now().isBefore(end)) {
                    File[] files = heroesDirectory.listFiles(fileNameFilter);
                    for (File file : files != null ? files : new File[0]) {
                        File target = new File(file, REPLAY_SERVER_BATTLELOBBY);
                        if (target.exists()) {
                            handleFile(target);
                            return;
                        }
                    }
                    Thread.sleep(DELAY);
                }
            } catch (InterruptedException ignored) {
            }
        };
    }

    private void handleFile(File target) {
        logger.info("Discovered BattleLobby: {}", target);
        new Thread(() -> {
            try {
                Thread.sleep(250L);
                callback.accept(target);
            } catch (InterruptedException e) {
                logger.error("", e);
            }
        }).start();
    }

    @Override
    public int getChildCount() {
        return 0;
    }

    @Override
    public Consumer<File> getCallback() {
        return callback;
    }

    @Override
    public void setCallback(Consumer<File> callback) {
        this.callback = callback;
    }
}
