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
import ninja.eivind.hotsreplayuploader.services.platform.PlatformService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
                protected Task<Void> createTask() {
                    return null;
                }

                @Override
                public void setCallback(Consumer<File> callback) {

                }

                @Override
                public int getChildCount() {
                    return 0;
                }

                @Override
                public Consumer<File> getCallback() {
                    return null;
                }
            };
        }
        return new RecursiveTempWatcher(tempDirectories);
    }
}
