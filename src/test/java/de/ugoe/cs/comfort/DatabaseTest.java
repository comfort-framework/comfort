/*
 * Copyright (C) 2017 University of Goettingen, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.ugoe.cs.comfort;

import com.github.danielfelgar.morphia.Log4JLoggerImplFactory;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import de.ugoe.cs.comfort.configuration.Database;
import org.junit.BeforeClass;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.logging.MorphiaLoggerFactory;

/**
 * @author Fabian Trautsch
 */
public class DatabaseTest extends BaseTest {
    protected final static Morphia morphia = new Morphia();
    protected final static String DB_USERNAME = null;
    protected final static String DB_PASSWORD = null;
    protected final static String DB_HOSTNAME = "localhost";
    protected final static int DB_PORT = 27017;
    protected final static String DB_AUTHENTICATION_DATABASE = null;
    protected final static String DB_NAME = "comfortTest";
    protected final static Boolean SSL_ENABLED = false;
    protected static Datastore datastore;
    protected static MongoClient mongoClient;

    @BeforeClass
    public static void setUpDatabaseConnection() {
        // Set up log4j logging
        MorphiaLoggerFactory.reset();
        MorphiaLoggerFactory.registerLogger(Log4JLoggerImplFactory.class);

        // Map models
        morphia.mapPackage("de.ugoe.cs.smartshark.model");
        morphia.mapPackage("de.ugoe.cs.comfort.database.models");

        // Create database connection string
        String mongoDBConnectionString = de.ugoe.cs.smartshark.Utils.createMongoDBURI(
                DB_USERNAME,
                DB_PASSWORD,
                DB_HOSTNAME,
                String.valueOf(DB_PORT),
                DB_AUTHENTICATION_DATABASE,
                SSL_ENABLED
        );

        // Create database connection
        MongoClientURI connectionString = new MongoClientURI(mongoDBConnectionString);
        mongoClient = new MongoClient(connectionString);
        datastore = morphia.createDatastore(mongoClient, DB_NAME);
        datastore.ensureIndexes();
    }

    protected Database getDatabaseConfiguration() {
        Database dbConfiguration = new Database();
        dbConfiguration.setUsername(DB_USERNAME);
        dbConfiguration.setPassword(DB_PASSWORD);
        dbConfiguration.setHostname(DB_HOSTNAME);
        dbConfiguration.setPort(DB_PORT);
        dbConfiguration.setAuthenticationDatabase(DB_AUTHENTICATION_DATABASE);
        dbConfiguration.setDatabase(DB_NAME);
        return dbConfiguration;
    }

}
