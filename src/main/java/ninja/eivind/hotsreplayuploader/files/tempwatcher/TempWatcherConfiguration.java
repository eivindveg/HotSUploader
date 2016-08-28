package ninja.eivind.hotsreplayuploader.files.tempwatcher;

import ninja.eivind.hotsreplayuploader.services.platform.PlatformService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
public class TempWatcherConfiguration implements InitializingBean {

    private final PlatformService platformService;
    private File tempDirectory;

    @Autowired
    public TempWatcherConfiguration(PlatformService platformService) {
        this.platformService = platformService;
    }

    @Bean
    public TempReplayWatcher tempReplayWatcher(BattleLobbyWatcher watcher) {
        return new TempReplayWatcher(tempDirectory, watcher);
    }

    @Bean
    public BattleLobbyWatcher lobbyWatcher() {
        return new BattleLobbyWatcher(new File(tempDirectory, TempReplayWatcher.DIRECTORY_NAME));
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        tempDirectory = platformService.getTempDirectory();
    }
}
