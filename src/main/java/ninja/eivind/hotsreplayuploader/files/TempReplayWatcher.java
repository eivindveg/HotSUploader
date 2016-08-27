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

import ninja.eivind.hotsreplayuploader.services.platform.PlatformService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

@Component
public class TempReplayWatcher implements InitializingBean, DisposableBean {

    private static final Logger logger = LoggerFactory.getLogger(TempReplayWatcher.class);
    private final PlatformService platformService;
    private Thread watcherThread;

    @Autowired
    public TempReplayWatcher(PlatformService platformService) {
        this.platformService = platformService;
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        logger.info("");
        File tempDirectory = platformService.getTempDirectory();

        File heroesTemp = new File(tempDirectory, "Heroes of the Storm");
        Path path = heroesTemp.toPath();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
                    path.register(watchService, ENTRY_CREATE);
                    while (true) {
                        WatchKey key = watchService.take();
                        key.pollEvents().forEach(event -> {
                            WatchEvent.Kind<?> kind = event.kind();
                            if(kind == OVERFLOW) {
                                return;
                            }
                            final WatchEvent<Path> pathEvent = (WatchEvent<Path>) event;
                            final Path fileName = pathEvent.context();
                            logger.info("Received " + kind + " for path " + fileName);
                            if(fileName.toFile().getName().equals("TempWriteReplayP1")) {
                                System.out.println("Temp file spotted");
                                System.out.println("Temp file spotted");
                                System.out.println("Temp file spotted");
                                System.out.println("Temp file spotted");
                                System.out.println("Temp file spotted");
                                System.out.println("Temp file spotted");
                                System.out.println("Temp file spotted");
                                System.out.println("Temp file spotted");
                                System.out.println("Temp file spotted");
                                System.out.println("Temp file spotted");
                                System.out.println("Temp file spotted");
                                System.out.println("Temp file spotted");
                                System.out.println("Temp file spotted");
                                System.out.println("Temp file spotted");
                                System.out.println("Temp file spotted");
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
            }
        };

        watcherThread = new Thread(runnable);
        watcherThread.start();
    }

    @Override
    public void destroy() throws Exception {
        watcherThread.interrupt();
    }
}
