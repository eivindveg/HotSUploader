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

import ninja.eivind.hotsreplayuploader.services.platform.PlatformService;
import ninja.eivind.hotsreplayuploader.versions.ReleaseManager;
import org.flywaydb.core.Flyway;
import org.h2.jdbcx.JdbcDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.sql.DataSource;
import java.io.File;

public class DataSourceProvider implements Provider<DataSource> {

    private static final Logger LOG = LoggerFactory.getLogger(DataSourceProvider.class);
    @Inject
    private PlatformService platformService;
    @Inject
    private ReleaseManager releaseManager;

    @Override
    public DataSource get() {
        final File database = new File(platformService.getApplicationHome(), "database");
        final JdbcDataSource dataSource = new JdbcDataSource();
        final String databaseName;
        if (releaseManager == null || releaseManager.getCurrentVersion().equals("Development")) {
            databaseName = database.toString() + "-dev";
        } else {
            databaseName  = database.toString();
        }

        final String url = "jdbc:h2:" + databaseName;

        LOG.info("Setting up DataSource with URL " + url);
        dataSource.setUrl(url);

        migrateDataSource(dataSource);

        return dataSource;
    }

    private void migrateDataSource(JdbcDataSource dataSource) {
        final Flyway flyway = new Flyway();

        flyway.setDataSource(dataSource);

        flyway.migrate();
    }
}
