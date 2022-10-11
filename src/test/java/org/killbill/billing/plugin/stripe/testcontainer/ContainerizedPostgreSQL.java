/*
 * Copyright 2020-2022 Equinix, Inc
 * Copyright 2014-2022 The Billing Project, LLC
 *
 * The Billing Project licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.killbill.billing.plugin.stripe.testcontainer;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.killbill.commons.embeddeddb.EmbeddedDB;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public class ContainerizedPostgreSQL extends EmbeddedDB {

    private final PostgreSQLContainer<?> container;

    protected ContainerizedPostgreSQL() {
        super(null, null, null, null);
        container = new PostgreSQLContainer<>(DockerImageName.parse("postgres:10.21"));
        super.username = container.getUsername();
        super.password = container.getPassword();
        super.databaseName = container.getDatabaseName();
        super.jdbcConnectionString = container.getJdbcUrl();
    }

    @Override
    public DBEngine getDBEngine() {
        return DBEngine.POSTGRESQL;
    }

    @Override
    public void initialize() throws IOException, SQLException {
    }

    @Override
    public void start() throws IOException, SQLException {
        if (!container.isRunning()) {
            container.start();
        }
    }

    @Override
    public void stop() throws IOException {
        if (container.isRunning()) {
            container.stop();
        }
        super.stop();
    }

    @Override
    public void refreshTableNames() throws IOException {
        final String query = "select table_name from information_schema.tables where table_schema = current_schema() and table_type = 'BASE TABLE';";
        try {
            executeQuery(query, new ResultSetJob() {
                @Override
                public void work(final ResultSet resultSet) throws SQLException {
                    allTables.clear();
                    while (resultSet.next()) {
                        allTables.add(resultSet.getString(1));
                    }
                }
            });
        } catch (final SQLException e) {
            throw new IOException(e);
        }
    }
}
