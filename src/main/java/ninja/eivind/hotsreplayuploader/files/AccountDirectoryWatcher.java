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
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

/**
 * Service that manages multiple {@link WatchHandler}s, which in turn manages different
 * {@link java.nio.file.WatchService}s. Runs concurrently.
 */
@Component
public class AccountDirectoryWatcher implements InitializingBean, DisposableBean {

    private static final Logger LOG = LoggerFactory.getLogger(AccountDirectoryWatcher.class);
    private final Set<File> watchDirectories;
    private final Set<WatchHandler> watchHandlers = new HashSet<>();
    private final ExecutorService executor;

    @Autowired
    public AccountDirectoryWatcher(StormHandler stormHandler) {
        watchDirectories = new HashSet<>(stormHandler.getReplayDirectories());
        executor = Executors.newFixedThreadPool(watchDirectories.isEmpty() ? 1 : watchDirectories.size());
    }

    public void beginWatch() {
        LOG.info("Initiating watch against directories:");
        watchDirectories.stream()
                .map(file -> Paths.get(file.toString()))
                .map(path -> {
                    try {
                        return new WatchHandler(path);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .forEach(watchHandlers::add);
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
    public void afterPropertiesSet() throws Exception {
        beginWatch();
        watchHandlers.forEach(executor::execute);
    }

    @Override
    public void destroy() throws Exception {
        LOG.info("Shutting down watch handlers");
        executor.shutdownNow();
    }
}
