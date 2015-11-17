package ninja.eivind.hotsreplayuploader.repositories;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.spring.DaoFactory;
import com.j256.ormlite.spring.TableCreator;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import ninja.eivind.hotsreplayuploader.di.Initializable;
import ninja.eivind.hotsreplayuploader.files.AccountDirectoryWatcher;
import ninja.eivind.hotsreplayuploader.models.ReplayFile;
import ninja.eivind.hotsreplayuploader.models.UploadStatus;
import ninja.eivind.hotsreplayuploader.utils.FileUtils;
import ninja.eivind.hotsreplayuploader.utils.StormHandler;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class OrmLiteFileRepository implements FileRepository, Initializable {

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

            if(!dao.isTableExists()) {
                TableUtils.createTable(connectionSource, ReplayFile.class);
                TableUtils.createTable(connectionSource, UploadStatus.class);
            }
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
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ReplayFile> getAll() {
        List<ReplayFile> collect = accountDirectoryWatcher.getAllFiles()
                .map(ReplayFile::fromDirectory)
                .flatMap(List::stream)
                .map(this::getByFileName)
                .map(replayFile -> {
                    File file = replayFile.getFile();
                    if(!file.exists()) {
                        try {
                            dao.delete(replayFile);
                            return null;
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        return replayFile;
                    }
                })
                .collect(Collectors.toList());
        return collect;
    }

    private ReplayFile getByFileName(final ReplayFile replayFile) {
        try {
            final SelectArg selectArg = new SelectArg("fileName", replayFile.getFileName());
            final PreparedQuery<ReplayFile> query = dao.queryBuilder()
                    .where().eq("fileName", selectArg)
                    .prepare();
            final List<ReplayFile> result = dao.query(query);
            if (result.size() == 0) {
                return replayFile;
            } else {
                return result.get(0);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
}
