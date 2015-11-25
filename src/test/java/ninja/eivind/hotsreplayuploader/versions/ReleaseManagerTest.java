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
        releaseManager = new ReleaseManager(mock(StormHandler.class));
        releaseManager.setHttpClient(httpClient);
    }

    @Test
    public void testReleaseManagerBuildsValidUrlForCurrentRelease() {
        String currentReleaseString = releaseManager.getCurrentReleaseString();

        URI.create(currentReleaseString);
    }

    @Test
    public void testReleaseManagerHandlesExceptionFromClient() throws Exception {
        when(httpClient.simpleRequest(anyString())).thenThrow(new IOException("Mock HTTP Client."));

        releaseManager.getNewerVersionIfAny();
        verify(httpClient, times(1)).simpleRequest(anyString());
    }

}
