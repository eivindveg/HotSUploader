package ninja.eivind.hotsreplayuploader.files.tempwatcher;

import ninja.eivind.hotsreplayuploader.services.platform.PlatformService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.util.function.Consumer;

@Configuration
public class TempWatcherConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(TempWatcherConfiguration.class);

    @Bean
    public TempWatcher tempWatcher(PlatformService platformService) {
        BattleLobbyTempDirectories tempDirectories = platformService.getBattleLobbyTempDirectories();
        if(tempDirectories == null) {
            logger.warn("Platform has no BattleLobbyTempDirectories. Watcher will be no-op");
            return new TempWatcher() {
                @Override
                public void start() {

                }

                @Override
                public void stop() {

                }

                @Override
                public void setCallback(Consumer<File> callback) {

                }
            };
        }
        return new RecursiveTempWatcher(tempDirectories);
    }
}
