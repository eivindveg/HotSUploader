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

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.spring.DaoFactory;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.support.ConnectionSource;
import ninja.eivind.hotsreplayuploader.di.Initializable;
import ninja.eivind.hotsreplayuploader.files.AccountDirectoryWatcher;
import ninja.eivind.hotsreplayuploader.models.ReplayFile;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.Closeable;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Implementation of a {@link FileRepository}, which is based on a database backend.<br>
 * Uses ORMLite to abstract database access.
 */
@Singleton
public class OrmLiteFileRepository implements FileRepository, Initializable, Closeable {

    @Inject
    private ConnectionSource connectionSource;
    private Dao<ReplayFile, Long> dao;
    @Inject
    private AccountDirectoryWatcher accountDirectoryWatcher;

    /**
     * Initializes this object after all members have been injected. Called automatically by the IoC context.
     */
    @Override
    public void initialize() {
        try {
            dao = DaoFactory.createDao(connectionSource, ReplayFile.class);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteReplay(final ReplayFile replayFile) {
        try {
            dao.delete(replayFile);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateReplay(final ReplayFile file) {
        try {
            dao.createOrUpdate(file);
            dao.refresh(file);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ReplayFile> getAll() {
        //update the DB with any file changes first
        accountDirectoryWatcher.getAllFiles()
                .map(ReplayFile::fromDirectory)
                .forEach(this::checkForDatabaseIntegrity);

       //get a fully refreshed copy containing all changes from the db
        try {
            return dao.queryForAll();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void checkForDatabaseIntegrity(List<ReplayFile> replayFiles) {
        try {
            final List<ReplayFile> fromDb = dao.queryForAll();

            //start a batch task to speed up initial startups
            dao.callBatchTasks(new Callable<Void>() {
                public Void call() throws Exception {
                    for(ReplayFile replayFile : replayFiles) {
                        if(!fromDb.contains(replayFile))
                            createReplay(replayFile);
                        else if(!replayFile.getFile().exists())
                            deleteReplay(replayFile);
                    }

                    return null;
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteByFileName(ReplayFile file) {
        final SelectArg selectArg = new SelectArg("fileName", file.getFileName());
        try {
            final DeleteBuilder<ReplayFile, Long> deleteBuilder = dao.deleteBuilder();
            deleteBuilder.where()
                    .eq("fileName", selectArg);
            dao.delete(deleteBuilder.prepare());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void createReplay(final ReplayFile replayFile) {
        try {
            dao.create(replayFile);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws IOException {
        connectionSource.closeQuietly();
    }
}
