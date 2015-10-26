package ninja.eivind.hotsuploader.utils;

import ninja.eivind.hotsuploader.models.ReplayFile;
import ninja.eivind.hotsuploader.models.Status;
import ninja.eivind.hotsuploader.models.UploadStatus;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Eivind Vegsundvåg
 */
public class ReplayFileComparatorTest {

    private ReplayFileComparator comparator;

    @Before
    public void setUp() throws Exception {
        comparator = new ReplayFileComparator();
    }

    @Test
    public void testCompareByStatus() throws Exception {
        ReplayFile expection = new ReplayFile(mock(File.class));
        expection.getUploadStatuses().add(new UploadStatus("test", Status.EXCEPTION));
        ReplayFile uploaded = new ReplayFile(mock(File.class));
        uploaded.getUploadStatuses().add(new UploadStatus("test", Status.UPLOADED));

        List<ReplayFile> list = Arrays.asList(expection, uploaded);
        list.sort(comparator);

        assertEquals("An uploaded replay is ordered before a failed upload.", uploaded, list.get(0));
        assertEquals("An uploaded replay is ordered before a failed upload.", expection, list.get(1));
    }

    @Test
    public void testCompareByLastModified() throws Exception {
        File olderMock = mock(File.class);
        when(olderMock.lastModified()).thenReturn(1L);
        ReplayFile older = new ReplayFile(olderMock);
        older.getUploadStatuses().add(new UploadStatus("test", Status.UPLOADED));

        File newerMock = mock(File.class);
        when(newerMock.lastModified()).thenReturn(2L);
        ReplayFile newer = new ReplayFile(newerMock);
        newer.getUploadStatuses().add(new UploadStatus("test", Status.UPLOADED));

        List<ReplayFile> list = Arrays.asList(older, newer);
        list.sort(comparator);

        when(olderMock.toString()).thenReturn("Older File");
        when(newerMock.toString()).thenReturn("Newer File");

        assertEquals("A more recent replay is ordered before an older one", newer, list.get(0));
        assertEquals("A more recent replay is ordered before an older one", older, list.get(1));
    }


}
