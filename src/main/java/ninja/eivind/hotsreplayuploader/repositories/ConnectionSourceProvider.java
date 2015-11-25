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

import com.j256.ormlite.db.H2DatabaseType;
import com.j256.ormlite.jdbc.DataSourceConnectionSource;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import ninja.eivind.hotsreplayuploader.services.platform.PlatformService;
import ninja.eivind.hotsreplayuploader.versions.ReleaseManager;
import org.h2.jdbcx.JdbcDataSource;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.sql.DataSource;
import java.io.File;
import java.sql.SQLException;

/**
 * Defines the {@link ConnectionSource}, which is used by ORMLite
 * to connect to the underlying database.<br>
 * Database connection strings may differ for development or production mode.
 */
public class ConnectionSourceProvider implements Provider<ConnectionSource> {

    @Inject
    private ReleaseManager releaseManager;
    @Inject
    private DataSource dataSource;

    @Override
    public ConnectionSource get() {
        try {
            if (releaseManager == null || releaseManager.getCurrentVersion().equals("Development")) {
                return new JdbcConnectionSource("jdbc:h2:mem:", new H2DatabaseType());
            } else {
                return new DataSourceConnectionSource(dataSource, new H2DatabaseType());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
