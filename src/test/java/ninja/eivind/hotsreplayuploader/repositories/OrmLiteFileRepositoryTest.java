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

package ninja.eivind.hotsreplayuploader.repositories;

import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import ninja.eivind.hotsreplayuploader.files.AccountDirectoryWatcher;
import ninja.eivind.hotsreplayuploader.models.ReplayFile;
import ninja.eivind.hotsreplayuploader.models.Status;
import ninja.eivind.hotsreplayuploader.models.UploadStatus;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class OrmLiteFileRepositoryTest {

    private OrmLiteFileRepository repository;

    @Before
    public void setUp() throws Exception {
        ConnectionSource connectionSource = new JdbcConnectionSource("jdbc:h2:mem:test");
        TableUtils.createTable(connectionSource, ReplayFile.class);
        TableUtils.createTable(connectionSource, UploadStatus.class);

        repository = new OrmLiteFileRepository(connectionSource, mock(AccountDirectoryWatcher.class));
        repository.afterPropertiesSet();
    }

    @Test
    public void testUpdateReplay() throws Exception {
        final ReplayFile replayFile = new ReplayFile(mock(File.class));
        repository.updateReplay(replayFile);
        final UploadStatus status = new UploadStatus("test", Status.EXCEPTION);
        replayFile.addStatuses(Collections.singletonList(status));
        status.setReplayFile(replayFile);
        repository.updateReplay(replayFile);
        assertNotEquals("ReplayFile was assigned an ID.", 0, replayFile.getId());
        assertNotNull("Status was persisted.", status.getId());

        final UploadStatus persistentStatus = replayFile.getUploadStatusForProvider("test");
        assertNotNull("The persistent status was refreshed.", persistentStatus);
        persistentStatus.setStatus(Status.UPLOADED);
        repository.updateReplay(replayFile);

        final Status expected = Status.UPLOADED;
        final Status actual = repository.findById(replayFile.getId()).getStatus();

        assertSame("Updated status is persistent.", expected, actual);
    }
}
