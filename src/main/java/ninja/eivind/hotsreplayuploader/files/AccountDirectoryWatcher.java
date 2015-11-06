package ninja.eivind.hotsreplayuploader.files;

import ninja.eivind.hotsreplayuploader.utils.StormHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public class AccountDirectoryWatcher {

    private static final Logger LOG = LoggerFactory.getLogger(AccountDirectoryWatcher.class);
    private final Set<File> watchDirectories;
    private StormHandler stormHandler;
    private Set<WatchHandler> watchHandlers = new HashSet<>();

    @Inject
    public AccountDirectoryWatcher(StormHandler stormHandler) {
        this.stormHandler = stormHandler;
        watchDirectories = new HashSet<>(stormHandler.getHotSAccountDirectories());
        beginWatch();
    }

    public void beginWatch() {
        LOG.info("Initiating watch against directories:");
        watchDirectories.stream().map(file -> Paths.get(file.toString())).forEach(path -> {
            try {
                LOG.info("\t" + path);
                WatchHandler watchHandler = new WatchHandler(stormHandler, path);
                watchHandlers.add(watchHandler);
                new Thread(watchHandler).start();
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
}
