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
import java.io.IOException;
import java.nio.file.*;
import java.util.function.Consumer;

import static java.nio.file.StandardWatchEventKinds.*;

public class RecursiveTempWatcher extends TempWatcher {
    private static final Logger logger = LoggerFactory.getLogger(RecursiveTempWatcher.class);
    private final BattleLobbyTempDirectories tempDirectories;
    private final File newRoot;
    private final String firstChild;
    private final TempWatcher child;

    public RecursiveTempWatcher(BattleLobbyTempDirectories tempDirectories) {
        this.tempDirectories = tempDirectories;

        final File root = tempDirectories.getRoot();
        final File remainder = tempDirectories.getRemainder();

        String relativeRemainder = getRelativeRemainder(root, remainder);
        if (relativeRemainder.startsWith(File.separator)) {
            relativeRemainder = relativeRemainder.substring(1);
        }

        final String remainderRegex = getRemainderRegex();

        String[] splitRemainder = relativeRemainder.split(remainderRegex);
        firstChild = splitRemainder[0];
        newRoot = new File(root, firstChild);
        child = getChild(relativeRemainder, firstChild, newRoot);
    }

    @Override
    public void start() {
        if (child.isRunning()) {
            child.cancel();
        }
        super.start();
        if (newRoot.exists()) {
            child.start();
        }
    }

    @Override
    protected Task<Void> createTask() {
        return new WatcherTask();
    }

    protected String getRemainderRegex() {
        final String remainderRegex;
        if (File.separator.equals("\\")) {
            remainderRegex = File.separator + File.separator;
        } else {
            remainderRegex = File.separator;
        }
        return remainderRegex;
    }

    private TempWatcher getChild(String relativeRemainder, String firstChild, File newRoot) {
        if (relativeRemainder.contains(File.separator)) {
            final String remainingChildren = relativeRemainder.replace(firstChild + File.separator, "");
            final File newRemainder = new File(newRoot, remainingChildren);

            return new RecursiveTempWatcher(new BattleLobbyTempDirectories(newRoot, newRemainder));
        } else {
            return new BattleLobbyWatcher(newRoot);
        }
    }

    @Override
    public boolean cancel() {
        return child.cancel() && super.cancel();
    }

    @Override
    public int getChildCount() {
        return child.getChildCount() + 1;
    }

    @Override
    public Consumer<File> getCallback() {
        return child.getCallback();
    }

    @Override
    public void setCallback(Consumer<File> callback) {
        child.setCallback(callback);
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

    private class WatcherTask extends Task<Void> {

        @Override
        protected Void call() throws Exception {
            final File root = tempDirectories.getRoot();
            logger.info("Starting watch for {} in {}", firstChild, root);
            final Path path = root.toPath();
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
                                    child.cancel();
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
            return null;
        }
    }
}
