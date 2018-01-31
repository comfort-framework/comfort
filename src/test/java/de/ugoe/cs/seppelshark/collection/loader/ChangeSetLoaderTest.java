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

package de.ugoe.cs.seppelshark.collection.loader;

import com.github.danielfelgar.morphia.Log4JLoggerImplFactory;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import de.ugoe.cs.seppelshark.BaseTest;
import de.ugoe.cs.seppelshark.Utils;
import de.ugoe.cs.seppelshark.configuration.Database;
import de.ugoe.cs.seppelshark.configuration.GeneralConfiguration;
import de.ugoe.cs.seppelshark.configuration.LoaderConfiguration;
import de.ugoe.cs.seppelshark.data.ChangeSet;
import de.ugoe.cs.seppelshark.database.models.File;
import de.ugoe.cs.seppelshark.database.models.FileAction;
import de.ugoe.cs.seppelshark.database.models.VCSSystem;
import de.ugoe.cs.seppelshark.exception.LoaderException;
import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.logging.MorphiaLoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Fabian Trautsch
 */
public class ChangeSetLoaderTest extends BaseTest {
    private final static Morphia morphia = new Morphia();
    private final static String DB_USERNAME = null;
    private final static String DB_PASSWORD = null;
    private final static String DB_HOSTNAME = "localhost";
    private final static int DB_PORT = 27017;
    private final static String DB_AUTHENTICATION_DATABASE = null;
    private final static String DB_NAME = "seppelSHARKTest";

    private static Datastore datastore;
    private static MongoClient mongoClient;

    private File addressTest;
    private File personTest;
    private File confXML;
    private File address;
    private File person;
    private File main;

    private GeneralConfiguration configuration = new GeneralConfiguration();
    private LoaderConfiguration loaderConf = new LoaderConfiguration("ChangeSet");

    @BeforeClass
    public static void setUpDatabaseConnection() {
        // Set up log4j logging
        MorphiaLoggerFactory.reset();
        MorphiaLoggerFactory.registerLogger(Log4JLoggerImplFactory.class);

        // Map models
        morphia.mapPackage("de.ugoe.cs.seppelshark.database.models");

        // Create database connection string
        String mongoDBConnectionString = Utils.createMongoDBConnectionString(
                DB_USERNAME,
                DB_PASSWORD,
                DB_HOSTNAME,
                DB_PORT,
                DB_AUTHENTICATION_DATABASE
        );

        // Create database connection
        MongoClientURI connectionString = new MongoClientURI(mongoDBConnectionString);
        mongoClient = new MongoClient(connectionString);
        datastore = morphia.createDatastore(mongoClient, DB_NAME);
        datastore.ensureIndexes();
    }

    @Before
    public void createConfiguration() {
        Database dbConfiguration = new Database();
        dbConfiguration.setUsername(DB_USERNAME);
        dbConfiguration.setPassword(DB_PASSWORD);
        dbConfiguration.setHostname(DB_HOSTNAME);
        dbConfiguration.setPort(DB_PORT);
        dbConfiguration.setAuthenticationDatabase(DB_AUTHENTICATION_DATABASE);
        dbConfiguration.setDatabase(DB_NAME);

        loaderConf.setDatabase(dbConfiguration);
        loaderConf.setVcsSystemUrl("http://github.com/ftrautsch/seppelSHARK");

        configuration.setProjectDir(getPathToResource("loaderTestData/changeset"));
        configuration.setLanguage("java");
    }

    @After
    public void tearDownDatabase() {
        mongoClient.dropDatabase(DB_NAME);
    }

    @Before
    public void setUpDatabase() {
        // Clear database
        mongoClient.dropDatabase(DB_NAME);

        // Random projectId
        ObjectId projectId = new ObjectId();

        // Generate vcsSystem
        final VCSSystem vcsSystem = new VCSSystem("http://github.com/ftrautsch/seppelSHARK", projectId, "git", new Date());
        datastore.save(vcsSystem);

        // Create files of the repository
        addressTest = new File(vcsSystem.getId(), "src/test/java/org/foo/models/AddressTest.java");
        personTest = new File(vcsSystem.getId(), "src/test/java/org/foo/models/Persontest.java");
        confXML = new File(vcsSystem.getId(), "conf.xml");
        address = new File(vcsSystem.getId(), "src/main/java/org/foo/models/Address.java");
        person = new File(vcsSystem.getId(), "src/main/java/org/foo/models/Person.java");
        main = new File(vcsSystem.getId(), "src/main/java/org/foo/Main.java");
        datastore.save(addressTest);
        datastore.save(personTest);
        datastore.save(confXML);
        datastore.save(address);
        datastore.save(person);
        datastore.save(main);

        // Random commit ids
        ObjectId commit1 = new ObjectId();
        ObjectId commit2 = new ObjectId();
        ObjectId commit3 = new ObjectId();
        ObjectId commit4 = new ObjectId();
        ObjectId commit5 = new ObjectId();

        // Create FileActions for these commits

        // Commit1: addressTest, confXML, person, FileA1 changed
        final FileAction fAc1_1 = new FileAction(addressTest.getId(), commit1);
        final FileAction fAc1_2 = new FileAction(confXML.getId(), commit1);
        final FileAction fAc1_3 = new FileAction(person.getId(), commit1);
        final FileAction fAc1_4 = new FileAction(address.getId(), commit1);
        datastore.save(fAc1_1);
        datastore.save(fAc1_2);
        datastore.save(fAc1_3);
        datastore.save(fAc1_4);

        // Commit2: addressTest, person, main changed
        final FileAction fAc2_1 = new FileAction(addressTest.getId(), commit2);
        final FileAction fAc2_3 = new FileAction(person.getId(), commit1);
        final FileAction fAc2_4 = new FileAction(main.getId(), commit1);
        final FileAction fAc2_5 = new FileAction(address.getId(), commit1);
        datastore.save(fAc2_1);
        datastore.save(fAc2_3);
        datastore.save(fAc2_4);
        datastore.save(fAc2_5);

        // Commit3: personTest, person, confXML changed
        final FileAction fAc3_1 = new FileAction(personTest.getId(), commit3);
        final FileAction fAc3_2 = new FileAction(person.getId(), commit3);
        final FileAction fAc3_3 = new FileAction(confXML.getId(), commit3);
        datastore.save(fAc3_1);
        datastore.save(fAc3_2);
        datastore.save(fAc3_3);

        // Commit4: personTest, person, address changed
        final FileAction fAc4_1 = new FileAction(personTest.getId(), commit4);
        final FileAction fAc4_2 = new FileAction(person.getId(), commit4);
        final FileAction fAc4_3 = new FileAction(address.getId(), commit4);
        datastore.save(fAc4_1);
        datastore.save(fAc4_2);
        datastore.save(fAc4_3);

        // Commit4: personTest, addresstest changed
        final FileAction fAc5_1 = new FileAction(addressTest.getId(), commit5);
        final FileAction fAc5_2 = new FileAction(personTest.getId(), commit5);
        datastore.save(fAc5_1);
        datastore.save(fAc5_2);
    }

    @Test
    public void loadTest() {
        Map<Path, Multiset<Path>> expectedOutput = new HashMap<>();

        Multiset<Path> addressTestChangedWith = HashMultiset.create();
        addressTestChangedWith.add(Paths.get(person.getPath()));
        addressTestChangedWith.add(Paths.get(address.getPath()));
        addressTestChangedWith.add(Paths.get(person.getPath()));
        addressTestChangedWith.add(Paths.get(main.getPath()));
        addressTestChangedWith.add(Paths.get(address.getPath()));
        addressTestChangedWith.add(Paths.get(personTest.getPath()));

        Multiset<Path> personTestChangedWith = HashMultiset.create();
        personTestChangedWith.add(Paths.get(person.getPath()));
        personTestChangedWith.add(Paths.get(person.getPath()));
        personTestChangedWith.add(Paths.get(address.getPath()));
        personTestChangedWith.add(Paths.get(addressTest.getPath()));



        expectedOutput.put(Paths.get(addressTest.getPath()), addressTestChangedWith);
        expectedOutput.put(Paths.get(personTest.getPath()), personTestChangedWith);

        ChangeSetLoader loader = new ChangeSetLoader(configuration, loaderConf);
        try {
            ChangeSet changeSet = loader.loadData();

            // Check AddressTest
            assertEquals("ChangeSet is not the expected one", expectedOutput, changeSet.getChangeMap());
        } catch(LoaderException e) {
            fail("Error in reading project directory at: " + configuration.getProjectDir());
        }
    }
}
