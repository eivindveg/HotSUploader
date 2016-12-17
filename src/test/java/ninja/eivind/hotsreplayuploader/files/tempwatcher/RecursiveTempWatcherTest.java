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

import javafx.application.Platform;
import ninja.eivind.hotsreplayuploader.HotsReplayUploaderTest;
import ninja.eivind.hotsreplayuploader.services.platform.PlatformService;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import rules.JavaFXThreadingRule;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.function.Consumer;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@HotsReplayUploaderTest
public class RecursiveTempWatcherTest {

    private static final Logger logger = LoggerFactory.getLogger(RecursiveTempWatcherTest.class);
    @Autowired
    private PlatformService platformService;
    private RecursiveTempWatcher tempWatcher;
    private BattleLobbyTempDirectories directories;

    @Rule
    public JavaFXThreadingRule rule = new JavaFXThreadingRule();

    @Before
    public void setUp() throws Exception {
        directories = platformService.getBattleLobbyTempDirectories();
        if(!(directories.getRoot().exists() || directories.getRoot().mkdirs())) {
            fail("Could not create tmp root");
        }

        // give file creation some time to complete
        Thread.sleep(250);

        tempWatcher = new RecursiveTempWatcher(directories);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                tempWatcher.start();
                latch.countDown();
            }
        });
        if(!latch.await(1, TimeUnit.SECONDS)) {
            fail("Service did not start.");
        }
        // give watchers some time to wind up
        Thread.sleep(250);
    }

    @Test
    public void testSetCallbackIsAppliedProperly() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        tempWatcher.setCallback(file -> latch.countDown());
        TempWatcher lastChild = getChildRecursively(tempWatcher);
        Consumer<File> callback = lastChild.getCallback();

        callback.accept(null);

        if(!latch.await(500, TimeUnit.MILLISECONDS)) {
            throw new TimeoutException("Latch was not tripped.");
        }
    }

    @Test
    public void testCallbackIsInvokedWhenRelevantFileIsDiscovered() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        final File[] fileAccessor = new File[1];
        final File tempWriteReplayFolder = new File(directories.getRemainder(), "TempWriteReplayP99");
        final File target = new File(tempWriteReplayFolder, BattleLobbyWatcher.REPLAY_SERVER_BATTLELOBBY);

        tempWatcher.setCallback(file -> {
            fileAccessor[0] = file;
            latch.countDown();
        });

        if(!(tempWriteReplayFolder.mkdirs() && target.createNewFile())) {
            fail("Could not create file to drop target " + target + " in");
        }

        long stopAt = System.currentTimeMillis() + 50_000;
        while (latch.getCount() != 0) {
            if(System.currentTimeMillis() > stopAt) {
                fail("Latch was not tripped");
            }
            Thread.sleep(1_000);
        }

        final String expected = target.getName();
        final String actual = fileAccessor[0].getName();

        assertEquals(expected, actual);
    }

    private TempWatcher getChildRecursively(RecursiveTempWatcher tempWatcher) {
        TempWatcher child = tempWatcher.getChild();
        //noinspection InstanceofConcreteClass
        if(child instanceof RecursiveTempWatcher) {
            return getChildRecursively((RecursiveTempWatcher) child);
        }
        return tempWatcher;
    }

    @Test
    public void testGetChildCount() throws Exception {
        final File remainder = directories.getRemainder();
        if(!remainder.mkdirs()) {
            fail("Failed to create files.");
        }

        final String rootString = directories.getRoot().toString();
        final String remainderString = remainder.toString();
        final String difference = remainderString.replace(rootString, "").substring(1);

        final String remainderRegex = tempWatcher.getRemainderRegex();

        logger.info("Remainder string: {}", remainderString);
        logger.info("Root string: {}", rootString);
        logger.info("Difference string: {}", difference);
        logger.info("Splitting using regex string: {}", remainderRegex);
        final int expected = difference.split(remainderRegex).length;
        final int actual = tempWatcher.getChildCount();

        assertSame(String.format("Expecting %d children for tempwatcher", expected), expected, actual);
    }

    @After
    public void tearDown() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            if(tempWatcher.cancel()) {
                latch.countDown();
            }
        });
        if(!latch.await(1, TimeUnit.SECONDS)) {
            fail("Service failed to stop");
        }
        // give watchers some time to wind down recursively
        Thread.sleep(1000L);
        cleanupRecursive(directories.getRoot());
    }

    private void cleanupRecursive(File file) throws InterruptedException {
        File[] children = file.listFiles();
        for (File child : children != null ? children : new File[0]) {
            cleanupRecursive(child);
        }
        if(file.exists() && !file.delete()) {
            fail("Failed to clean up file " + file);
        }

        // give io some time to complete
        Thread.sleep(250);
    }
}
