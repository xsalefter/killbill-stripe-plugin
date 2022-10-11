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

import org.killbill.commons.embeddeddb.EmbeddedDB;
import org.killbill.commons.embeddeddb.h2.H2EmbeddedDB;
import org.killbill.commons.embeddeddb.mysql.MySQLEmbeddedDB;
import org.killbill.commons.embeddeddb.mysql.MySQLStandaloneDB;
import org.killbill.commons.embeddeddb.postgresql.PostgreSQLStandaloneDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Do the same thing as {@link org.killbill.billing.platform.test.PlatformDBTestingHelper}.
 */
public class EmbeddedDbFactory {

    private static final Logger log = LoggerFactory.getLogger(EmbeddedDbFactory.class);

    private static EmbeddedDbFactory instance;

    private final EmbeddedDB embeddedDB;

    protected EmbeddedDbFactory() {
        if ("true".equals(System.getProperty("org.killbill.billing.dbi.test.h2"))) {
            log.info("Using h2 as the embedded database");
            this.embeddedDB = new H2EmbeddedDB();
        } else {
            String databaseName;
            String username;
            String password;
            if ("true".equals(System.getProperty("org.killbill.billing.dbi.test.postgresql"))) {
                if (this.isUsingLocalInstance()) {
                    log.info("Using PostgreSQL local database");
                    databaseName = System.getProperty("org.killbill.billing.dbi.test.localDb.database", "killbill");
                    username = System.getProperty("org.killbill.billing.dbi.test.localDb.username", "postgres");
                    password = System.getProperty("org.killbill.billing.dbi.test.localDb.password", "postgres");
                    this.embeddedDB = new PostgreSQLStandaloneDB(databaseName, username, password);
                } else {
                    log.info("Using PostgreSQL as the embedded database");
                    this.embeddedDB = new ContainerizedPostgreSQL();
                }
            } else if (this.isUsingLocalInstance()) {
                log.info("Using MySQL local database");
                databaseName = System.getProperty("org.killbill.billing.dbi.test.localDb.database", "killbill");
                username = System.getProperty("org.killbill.billing.dbi.test.localDb.username", "root");
                password = System.getProperty("org.killbill.billing.dbi.test.localDb.password", "root");
                this.embeddedDB = new MySQLStandaloneDB(databaseName, username, password);
            } else {
                log.info("Using MySQL as the embedded database");
                this.embeddedDB = new MySQLEmbeddedDB();
            }
        }
    }

    public static synchronized EmbeddedDbFactory getInstance() {
        if (instance == null) {
            instance = new EmbeddedDbFactory();
        }
        return instance;
    }

    public EmbeddedDB getEmbeddedDB() {
        return embeddedDB;
    }

    private boolean isUsingLocalInstance() {
        return System.getProperty("org.killbill.billing.dbi.test.useLocalDb") != null;
    }
}
