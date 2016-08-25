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
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.support.ConnectionSource;
import ninja.eivind.hotsreplayuploader.files.AccountDirectoryWatcher;
import ninja.eivind.hotsreplayuploader.models.ReplayFile;
import ninja.eivind.hotsreplayuploader.models.UploadStatus;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.Closeable;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * Implementation of a {@link FileRepository}, which is based on a database backend.<br>
 * Uses ORMLite to abstract database access.
 */
@Repository
public class OrmLiteFileRepository implements FileRepository, InitializingBean, DisposableBean {

    private static final String FILE_NAME = "fileName";
    @Inject
    private ConnectionSource connectionSource;
    private Dao<ReplayFile, Long> dao;
    @Inject
    private AccountDirectoryWatcher accountDirectoryWatcher;
    private Dao<UploadStatus, Long> statusDao;

    public OrmLiteFileRepository() {
    }

    public OrmLiteFileRepository(ConnectionSource connectionSource) {
        this.connectionSource = connectionSource;
    }

    /**
     * Initializes this object after all members have been injected. Called automatically by the IoC context.
     */
    @Override
    public void afterPropertiesSet() {
        try {
            dao = DaoFactory.createDao(connectionSource, ReplayFile.class);
            statusDao = DaoFactory.createDao(connectionSource, UploadStatus.class);
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
            for (UploadStatus uploadStatus : file.getUploadStatuses()) {
                statusDao.createOrUpdate(uploadStatus);
            }
            dao.refresh(file);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ReplayFile> getAll() {
        //update the DB with any file changes first
        checkForDatabaseIntegrity(accountDirectoryWatcher.getAllFiles()
                .map(ReplayFile::fromDirectory)
                .flatMap(List::stream)
                .collect(Collectors.toList()));

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
            dao.callBatchTasks((Callable<Void>) () -> {
                //create a db entry for every new physical file
                replayFiles.stream().filter(r -> !fromDb.contains(r)).forEach(this::createReplay);
                //remove non-existing files from the db
                fromDb.stream().filter(r -> !replayFiles.contains(r)).forEach(this::deleteReplay);

                return null;
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteByFileName(ReplayFile file) {
        final SelectArg selectArg = new SelectArg(FILE_NAME, file.getFileName());
        try {
            final DeleteBuilder<ReplayFile, Long> deleteBuilder = dao.deleteBuilder();
            deleteBuilder.where()
                    .eq(FILE_NAME, selectArg);
            dao.delete(deleteBuilder.prepare());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ReplayFile findById(long id) {
        try {
            return dao.queryForId(id);
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

    private ReplayFile getByFileName(final ReplayFile replayFile) {
        try {
            final SelectArg selectArg = new SelectArg(FILE_NAME, replayFile.getFileName());
            final PreparedQuery<ReplayFile> query = dao.queryBuilder()
                    .where().eq(FILE_NAME, selectArg)
                    .prepare();

            return dao.query(query).stream()
                    .findAny()
                    .orElse(replayFile);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void destroy() throws IOException {
        connectionSource.closeQuietly();
    }
}
