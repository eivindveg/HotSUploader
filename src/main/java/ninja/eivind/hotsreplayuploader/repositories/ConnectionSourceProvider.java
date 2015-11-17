package ninja.eivind.hotsreplayuploader.repositories;

import com.j256.ormlite.db.H2DatabaseType;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import ninja.eivind.hotsreplayuploader.services.platform.PlatformService;
import ninja.eivind.hotsreplayuploader.versions.ReleaseManager;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.File;
import java.sql.SQLException;

public class ConnectionSourceProvider implements Provider<ConnectionSource> {

    @Inject
    private ReleaseManager releaseManager;
    @Inject
    private PlatformService platformService;

    @Override
    public ConnectionSource get() {
        try {
            if (releaseManager == null || releaseManager.getCurrentVersion().equals("Development")) {
                return new JdbcConnectionSource("jdbc:h2:mem:", new H2DatabaseType());
            } else {
                final File database = new File(platformService.getApplicationHome(), "database");
                return new JdbcConnectionSource(
                        "jdbc:h2:" + database.toString(),
                        new H2DatabaseType()
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
