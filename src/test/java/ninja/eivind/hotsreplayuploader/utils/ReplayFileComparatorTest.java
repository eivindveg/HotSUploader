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

package ninja.eivind.hotsreplayuploader.utils;

import ninja.eivind.hotsreplayuploader.models.ReplayFile;
import ninja.eivind.hotsreplayuploader.models.Status;
import ninja.eivind.hotsreplayuploader.models.UploadStatus;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
