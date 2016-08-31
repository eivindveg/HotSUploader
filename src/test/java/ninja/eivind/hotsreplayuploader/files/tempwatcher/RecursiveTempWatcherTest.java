package ninja.eivind.hotsreplayuploader.files.tempwatcher;

import ninja.eivind.hotsreplayuploader.HotsReplayUploaderTest;
import ninja.eivind.hotsreplayuploader.services.platform.PlatformService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@HotsReplayUploaderTest
public class RecursiveTempWatcherTest {

    @Autowired
    private PlatformService platformService;
    @Autowired
    private RecursiveTempWatcher tempWatcher;
    private BattleLobbyTempDirectories directories;

    @Before
    public void setUp() throws Exception {
        directories = platformService.getBattleLobbyTempDirectories();

        tempWatcher.start();
    }

    @Test
    public void testSetCallback() throws Exception {
        fail("Test not implemented");
    }

    @Test
    public void testGetChildCount() throws Exception {
        final File remainder = directories.getRemainder();
        final String remainderString = remainder.toString();
        if(!remainder.mkdirs()) {
            throw new AssertionError("Failed to create files.");
        }

        final String rootString = directories.getRoot().toString();
        final String difference = remainderString.replace(rootString, "");

        final int expected = difference.split(File.pathSeparator).length;
        final int actual = tempWatcher.getChildCount();

        assertSame(expected, actual);
    }

    @After
    public void tearDown() throws Exception {
        tempWatcher.stop();
        // give watchers some time to wind down recursively
        Thread.sleep(250L);
        cleanupRecursive(directories.getRoot());
    }

    private void cleanupRecursive(File file) {
        File[] children = file.listFiles();
        for (File child : children != null ? children : new File[0]) {
            cleanupRecursive(child);
        }
        if(!file.delete()) {
            throw new AssertionError("Failed to clean up file " + file);
        }
    }
}
