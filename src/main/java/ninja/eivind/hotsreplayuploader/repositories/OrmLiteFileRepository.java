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
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.spring.DaoFactory;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.support.ConnectionSource;
import ninja.eivind.hotsreplayuploader.di.Initializable;
import ninja.eivind.hotsreplayuploader.files.AccountDirectoryWatcher;
import ninja.eivind.hotsreplayuploader.models.ReplayFile;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

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
            TransactionManager.callInTransaction(connectionSource, (Callable<Void>) () -> {
                dao.createOrUpdate(file);
                return null;
            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ReplayFile> getAll() {
        return accountDirectoryWatcher.getAllFiles()
                .map(ReplayFile::fromDirectory)
                .map(this::refreshAll)
                // TODO Replace with SELECT IN statement. Needs readding of missing files. Improves performance
                .flatMap(List::stream)
                .map(replayFile -> {
                    File file = replayFile.getFile();
                    if (file.exists()) {
                        return replayFile;
                    } else {
                        try {
                            dao.delete(replayFile);
                            return null;
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }
                })
                .filter(file -> file != null) //dont list already deleted files
                .collect(Collectors.toList());
    }

    private List<ReplayFile> refreshAll(List<ReplayFile> replayFiles) {
        try {
            final List<ReplayFile> collect = new ArrayList<>();
            final List<ReplayFile> fromDb = dao.queryForAll();
            collect.addAll(fromDb);
            replayFiles.stream().filter(file -> !fromDb.contains(file)).forEach(collect::add);
            return collect;
        } catch (SQLException e) {
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

    private ReplayFile getByFileName(final ReplayFile replayFile) {
        try {
            final SelectArg selectArg = new SelectArg("fileName", replayFile.getFileName());
            final PreparedQuery<ReplayFile> query = dao.queryBuilder()
                    .where().eq("fileName", selectArg)
                    .prepare();

            return dao.query(query).stream()
                    .findAny()
                    .orElse(replayFile);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void close() throws IOException {
        connectionSource.closeQuietly();
    }
}
