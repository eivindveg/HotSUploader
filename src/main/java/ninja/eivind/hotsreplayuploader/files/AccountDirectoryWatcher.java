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

import ninja.eivind.hotsreplayuploader.utils.StormHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Service that manages multiple {@link WatchHandler}s, which in turn manages different
 * {@link java.nio.file.WatchService}s. Runs concurrently.
 */
@Singleton
public class AccountDirectoryWatcher implements Closeable {

    private static final Logger LOG = LoggerFactory.getLogger(AccountDirectoryWatcher.class);
    private final Set<File> watchDirectories;
    private Set<WatchHandler> watchHandlers = new HashSet<>();
    private Collection<Thread> threads;

    @Inject
    public AccountDirectoryWatcher(StormHandler stormHandler) {
        watchDirectories = new HashSet<>(stormHandler.getHotSAccountDirectories());
        threads = new HashSet<>();
        beginWatch();
    }

    public void beginWatch() {
        LOG.info("Initiating watch against directories:");
        watchDirectories.stream().map(file -> Paths.get(file.toString())).forEach(path -> {
            try {
                LOG.info("\t" + path);
                final WatchHandler watchHandler = new WatchHandler(path);
                watchHandlers.add(watchHandler);
                final Thread thread = new Thread(watchHandler);
                thread.start();
                threads.add(thread);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        LOG.info("Watcher initiated.");
    }

    public Stream<File> getAllFiles() {
        return watchDirectories.stream();
    }

    public void addFileListener(FileListener fileListener) {
        LOG.info("Adding listener " + fileListener.getClass());
        for (final WatchHandler watchHandler : watchHandlers) {
            watchHandler.addListener(fileListener);
        }
    }

    @Override
    public void close() {
        threads.stream()
                .filter(thread -> !thread.isInterrupted())
                .forEach(Thread::interrupt);
    }
}
