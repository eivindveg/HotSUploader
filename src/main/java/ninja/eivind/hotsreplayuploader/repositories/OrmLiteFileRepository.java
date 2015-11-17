package ninja.eivind.hotsreplayuploader.repositories;

import com.j256.ormlite.spring.DaoFactory;
import com.j256.ormlite.support.ConnectionSource;
import ninja.eivind.hotsreplayuploader.di.Initializable;
import ninja.eivind.hotsreplayuploader.models.ReplayFile;

import javax.inject.Inject;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class OrmLiteFileRepository implements FileRepository, Initializable {

    @Inject
    private ConnectionSource connectionSource;

    /**
     * Initializes this object after all members have been injected. Called automatically by the IoC context.
     */
    @Override
    public void initialize() {
        try {
            DaoFactory.createDao(connectionSource, ReplayFile.class);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteReplay(final ReplayFile replayFile) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void updateReplay(final ReplayFile file) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public List<ReplayFile> getAll() {
        return Collections.emptyList();
    }
}
