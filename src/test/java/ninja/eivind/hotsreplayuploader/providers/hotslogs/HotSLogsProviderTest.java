// Copyright 2015-2016 Eivind Vegsundvåg
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

package ninja.eivind.hotsreplayuploader.providers.hotslogs;

import com.amazonaws.services.s3.AmazonS3Client;
import ninja.eivind.hotsreplayuploader.concurrent.tasks.UploadTask;
import ninja.eivind.hotsreplayuploader.models.ReplayFile;
import ninja.eivind.hotsreplayuploader.utils.SimpleHttpClient;
import ninja.eivind.stormparser.StormParser;
import ninja.eivind.stormparser.models.Replay;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * @author Eivind Vegsundvåg
 */
public class HotSLogsProviderTest {

    private HotsLogsProvider provider;
    private ReplayFile replayFile;
    private Replay parsedReplay;
    private AmazonS3Client s3ClientMock;

    @Before
    public void setUp() throws IOException {
        URL resource = ClassLoader.getSystemClassLoader().getResource("test.StormReplay");
        assertNotNull("Could not load test resource", resource);
        String fileName = resource.getFile();
        parsedReplay = new StormParser().apply(new File(fileName));
        provider = new HotsLogsProvider();
        s3ClientMock = mock(AmazonS3Client.class);
        SimpleHttpClient mock = mock(SimpleHttpClient.class);
        when(mock.simpleRequest(anyString())).thenReturn("Duplicate");
        provider.setHttpClient(mock);
        provider.setS3Client(s3ClientMock);
        replayFile = new ReplayFile(new File(fileName));
    }

    @Test
    public void testGetMatchId() throws NoSuchAlgorithmException {
        String expected = "5543abb9-af35-3ce6-a026-e9d5517f2964";
        String actual = provider.getMatchId(parsedReplay);

        assertEquals("Match ID is calculated as expected", expected, actual);
    }

    @Test
    public void testProviderDoesNotTryToUploadPresentReplay() {
        final UploadTask uploadTask = new UploadTask(Collections.singletonList(provider), replayFile, new StormParser());
        uploadTask.run();

        verifyZeroInteractions(s3ClientMock);
    }

}
