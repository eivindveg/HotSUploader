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
import java.io.IOException;
import java.nio.file.*;
import java.util.function.Consumer;

import static java.nio.file.StandardWatchEventKinds.*;

public class RecursiveTempWatcher implements TempWatcher {
    private static final Logger logger = LoggerFactory.getLogger(RecursiveTempWatcher.class);
    private final BattleLobbyTempDirectories tempDirectories;
    private TempWatcher child;
    private Thread watcherThread;
    private Consumer<File> callback;

    public RecursiveTempWatcher(BattleLobbyTempDirectories tempDirectories) {
        this.tempDirectories = tempDirectories;
    }

    public RecursiveTempWatcher(BattleLobbyTempDirectories battleLobbyTempDirectories, Consumer<File> callback) {
        this(battleLobbyTempDirectories);
        this.callback = callback;
    }

    @Override
    public void start() {
        final File root = tempDirectories.getRoot();
        final File remainder = tempDirectories.getRemainder();

        String relativeRemainder = getRelativeRemainder(root, remainder);
        if (relativeRemainder.startsWith(File.separator)) {
            relativeRemainder = relativeRemainder.substring(1);
        }
        String[] splitRemainder = relativeRemainder.split(File.pathSeparator);
        final String firstChild = splitRemainder[0];
        final File newRoot = new File(root, firstChild);
        if (child == null) {
            child = getChild(relativeRemainder, firstChild, newRoot, file -> callback.accept(file));
        }

        if (newRoot.exists()) {
            child.start();
        }

        watcherThread = prepareWatch(root, firstChild);
        watcherThread.start();
    }

    private TempWatcher getChild(String relativeRemainder, String firstChild, File newRoot, Consumer<File> callback) {
        if (relativeRemainder.contains(File.pathSeparator)) {
            final String remainingChildren = relativeRemainder.replace(firstChild + File.pathSeparator, "");
            final File newRemainder = new File(newRoot, remainingChildren);

            return new RecursiveTempWatcher(new BattleLobbyTempDirectories(newRoot, newRemainder), callback);
        } else {
            return new BattleLobbyWatcher(newRoot, callback);
        }
    }

    private Thread prepareWatch(File root, String firstChild) {
        final Path path = root.toPath();

        return new Thread(() -> {
            logger.info("Starting watch for {} in {}", firstChild, root);
            try {
                Thread.sleep(50L);
                try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
                    path.register(watchService, ENTRY_CREATE, ENTRY_DELETE);

                    while (true) {
                        WatchKey key = watchService.take();
                        key.pollEvents().forEach(event -> {
                            WatchEvent.Kind<?> kind = event.kind();
                            if (kind == OVERFLOW) {
                                return;
                            }
                            @SuppressWarnings("unchecked")
                            final WatchEvent<Path> pathEvent = (WatchEvent<Path>) event;
                            final Path pathName = pathEvent.context();
                            if (pathName.toString().equals(firstChild.replaceAll(File.pathSeparator, ""))) {
                                if (kind == ENTRY_CREATE) {
                                    child.start();
                                } else if (kind == ENTRY_DELETE) {
                                    child.stop();
                                }
                            }
                        });
                        if (!key.reset()) {
                            break;
                        }
                    }
                } catch (IOException e) {
                    logger.error("Watcher threw exception", e);
                }
            } catch (InterruptedException e) {
                logger.info("Watcher for {} in {} interrupted", firstChild, root);
            }
        });
    }

    @Override
    public void stop() {
        child.stop();
        if (watcherThread != null) {
            watcherThread.interrupt();
            watcherThread = null;
        }
    }

    @Override
    public int getChildCount() {
        if (child != null) {
            return child.getChildCount() + 1;
        } else {
            return 0;
        }
    }

    @Override
    public Consumer<File> getCallback() {
        return callback;
    }

    @Override
    public void setCallback(Consumer<File> callback) {
        this.callback = callback;
    }

    protected TempWatcher getChild() {
        return child;
    }

    private String getRelativeRemainder(File root, File remainder) {
        String remainderString = remainder.toString().replace(root.toString(), "");
        if (remainderString.startsWith(File.pathSeparator)) {
            return remainderString.replace(File.pathSeparator, "");
        }
        return remainderString;
    }
}
