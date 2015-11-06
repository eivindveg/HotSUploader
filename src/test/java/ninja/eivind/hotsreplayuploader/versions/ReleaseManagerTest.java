package ninja.eivind.hotsreplayuploader.versions;

import ninja.eivind.hotsreplayuploader.utils.SimpleHttpClient;
import ninja.eivind.hotsreplayuploader.utils.StormHandler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class ReleaseManagerTest {

    private SimpleHttpClient httpClient;
    private ReleaseManager releaseManager;

    @Before
    public void setup() {
        httpClient = mock(SimpleHttpClient.class);
        releaseManager = new ReleaseManager(new StormHandler());
        releaseManager.setHttpClient(httpClient);
    }

    @Test
    public void testReleaseManagerBuildsValidUrlForCurrentRelease() {
        String currentReleaseString = releaseManager.getCurrentReleaseString();

        URI.create(currentReleaseString);
    }

    @Test
    public void testReleaseManagerHandlesExceptionFromClient() throws Exception {
        when(httpClient.simpleRequest(anyString())).thenThrow(new IOException("Test"));

        releaseManager.getNewerVersionIfAny();
        verify(httpClient, times(1)).simpleRequest(anyString());
    }

    @Test
    public void testCurrentReleaseIsTheSameAsPomVersion() throws Exception {
        String currentVersion = ReleaseManager.CURRENT_VERSION;

        Document parse = Jsoup.parse(new File("pom.xml"), "UTF-8");
        String pomVersion = parse.select("project > version").text();

        assertEquals("The release manager has a version number matching the pom.xml", currentVersion, pomVersion);
    }
}
