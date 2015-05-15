package com.metacodestudio.hotsuploader.versions;

import com.metacodestudio.hotsuploader.utils.SimpleHttpClient;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class ReleaseManagerTest {

    private SimpleHttpClient httpClient;
    private ReleaseManager releaseManager;

    @Before
    public void setup() {
        httpClient = mock(SimpleHttpClient.class);
        releaseManager = new ReleaseManager(httpClient);
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
}
