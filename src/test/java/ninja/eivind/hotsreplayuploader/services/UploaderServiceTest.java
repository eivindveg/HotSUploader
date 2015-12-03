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

package ninja.eivind.hotsreplayuploader.services;

import ninja.eivind.hotsreplayuploader.utils.StormHandler;
import org.junit.Before;

import static org.mockito.Mockito.*;

public class UploaderServiceTest {

    private UploaderService uploaderService;
    private StormHandler stormHandlerMock;

    @Before
    public void setup() throws Exception {
        stormHandlerMock = mock(StormHandler.class);
        uploaderService = new UploaderService();
        //fileService.setStormHandler(stormHandlerMock);
    }

    /* Disabled due to separation of concerns

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void testCleanup() throws Exception {
        File nonExistingReplay = mock(File.class);
        File existingReplay = mock(File.class);
        File fileNotToBeTouched = mock(File.class);
        File fileToBeDeleted = mock(File.class);

        File accountFile = mock(File.class);
        when(stormHandlerMock.getApplicationAccountDirectories()).thenReturn(Collections.singletonList(accountFile));
        when(accountFile.listFiles()).thenReturn(new File[]{fileNotToBeTouched, fileToBeDeleted});

        when(existingReplay.exists()).thenReturn(true);
        when(nonExistingReplay.exists()).thenReturn(false);
        when(stormHandlerMock.getPropertiesFile(nonExistingReplay)).thenReturn(fileToBeDeleted);
        when(stormHandlerMock.getReplayFile(fileToBeDeleted)).thenReturn(nonExistingReplay);
        when(stormHandlerMock.getPropertiesFile(existingReplay)).thenReturn(fileNotToBeTouched);
        when(stormHandlerMock.getReplayFile(fileNotToBeTouched)).thenReturn(existingReplay);

        fileService.cleanup();
        verify(accountFile, times(1)).listFiles();
        verify(fileNotToBeTouched, never()).delete();
        verify(fileToBeDeleted, times(1)).delete();
    }
     */
}
