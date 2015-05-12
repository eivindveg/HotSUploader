package com.metacodestudio.hotsuploader.files;

import com.metacodestudio.hotsuploader.utils.StormHandler;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Collections;

import static org.mockito.Mockito.*;

public class FileHandlerTest {

    private FileHandler fileHandler;
    private StormHandler stormHandlerMock;

    @Before
    public void setup() throws Exception {
        stormHandlerMock = mock(StormHandler.class);
        fileHandler = new FileHandler(stormHandlerMock);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void testCleanup() throws Exception {
        File nonExistingReplay = mock(File.class);
        File existingReplay = mock(File.class);
        File fileNotToBeTouched = mock(File.class);
        File fileToBeDeleted = mock(File.class);

        File accountFile = mock(File.class);
        when(stormHandlerMock.getAccountDirectories(any())).thenReturn(Collections.singletonList(accountFile));
        when(accountFile.listFiles()).thenReturn(new File[]{fileNotToBeTouched, fileToBeDeleted});

        when(existingReplay.exists()).thenReturn(true);
        when(nonExistingReplay.exists()).thenReturn(false);
        when(stormHandlerMock.getPropertiesFile(nonExistingReplay)).thenReturn(fileToBeDeleted);
        when(stormHandlerMock.getReplayFile(fileToBeDeleted)).thenReturn(nonExistingReplay);
        when(stormHandlerMock.getPropertiesFile(existingReplay)).thenReturn(fileNotToBeTouched);
        when(stormHandlerMock.getReplayFile(fileNotToBeTouched)).thenReturn(existingReplay);

        fileHandler.cleanup();
        verify(accountFile, times(1)).listFiles();
        verify(fileNotToBeTouched, never()).delete();
        verify(fileToBeDeleted, times(1)).delete();
    }
}
