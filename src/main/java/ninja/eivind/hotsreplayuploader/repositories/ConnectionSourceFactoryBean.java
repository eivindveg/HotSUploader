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
import com.j256.ormlite.support.ConnectionSource;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * Defines the {@link ConnectionSource}, which is used by ORMLite
 * to connect to the underlying database.<br>
 * Database connection strings may differ for development or production mode.
 */
@Component
public class ConnectionSourceFactoryBean implements Provider<ConnectionSource>, FactoryBean<ConnectionSource> {

    @Inject
    private DataSource dataSource;

    @Override
    public ConnectionSource get() {
        try {
            return new DataSourceConnectionSource(dataSource, new H2DatabaseType());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ConnectionSource getObject() throws Exception {
        return get();
    }

    @Override
    public Class<?> getObjectType() {
        return ConnectionSource.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
