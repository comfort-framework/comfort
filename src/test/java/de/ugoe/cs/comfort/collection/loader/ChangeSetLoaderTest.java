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

package de.ugoe.cs.comfort.collection.loader;

import com.github.danielfelgar.morphia.Log4JLoggerImplFactory;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import de.ugoe.cs.comfort.BaseTest;
import de.ugoe.cs.comfort.DatabaseTest;
import de.ugoe.cs.comfort.configuration.Database;
import de.ugoe.cs.comfort.configuration.GeneralConfiguration;
import de.ugoe.cs.comfort.configuration.LoaderConfiguration;
import de.ugoe.cs.comfort.data.ChangeSet;
import de.ugoe.cs.smartshark.model.File;
import de.ugoe.cs.smartshark.model.FileAction;
import de.ugoe.cs.smartshark.model.VCSSystem;
import de.ugoe.cs.comfort.exception.LoaderException;
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
public class ChangeSetLoaderTest extends DatabaseTest {
    private File addressTest;
    private File personTest;
    private File confXML;
    private File address;
    private File person;
    private File main;

    private GeneralConfiguration configuration = new GeneralConfiguration();
    private LoaderConfiguration loaderConf = new LoaderConfiguration("ChangeSet");

    @Before
    public void createConfiguration() {
        loaderConf.setDatabase(getDatabaseConfiguration());
        loaderConf.setVcsSystemUrl("http://github.com/comfort-framework/comfort");

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
        final VCSSystem vcsSystem = new VCSSystem();
        vcsSystem.setUrl("http://github.com/comfort-framework/comfort");
        vcsSystem.setProjectId(projectId);
        vcsSystem.setRepositoryType("git");
        vcsSystem.setLastUpdated(new Date());
        datastore.save(vcsSystem);

        // Create files of the repository
        addressTest = new File();
        addressTest.setVcsSystemId(vcsSystem.getId());
        addressTest.setPath("src/test/java/org/foo/models/AddressTest.java");

        personTest = new File();
        personTest.setVcsSystemId(vcsSystem.getId());
        personTest.setPath("src/test/java/org/foo/models/Persontest.java");

        confXML = new File();
        confXML.setVcsSystemId(vcsSystem.getId());
        confXML.setPath("conf.xml");

        address = new File();
        address.setVcsSystemId(vcsSystem.getId());
        address.setPath("src/main/java/org/foo/models/Address.java");

        person = new File();
        person.setVcsSystemId(vcsSystem.getId());
        person.setPath("src/main/java/org/foo/models/Person.java");

        main = new File();
        main.setVcsSystemId(vcsSystem.getId());
        main.setPath("src/main/java/org/foo/Main.java");

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
        final FileAction fAc1_1 = new FileAction();
        fAc1_1.setFileId(addressTest.getId());
        fAc1_1.setCommitId(commit1);

        final FileAction fAc1_2 = new FileAction();
        fAc1_2.setFileId(confXML.getId());
        fAc1_2.setCommitId(commit1);

        final FileAction fAc1_3 = new FileAction();
        fAc1_3.setFileId(person.getId());
        fAc1_3.setCommitId(commit1);

        final FileAction fAc1_4 = new FileAction();
        fAc1_4.setFileId(address.getId());
        fAc1_4.setCommitId(commit1);

        datastore.save(fAc1_1);
        datastore.save(fAc1_2);
        datastore.save(fAc1_3);
        datastore.save(fAc1_4);

        // Commit2: addressTest, person, main changed
        final FileAction fAc2_1 = new FileAction();
        fAc2_1.setFileId(addressTest.getId());
        fAc2_1.setCommitId(commit2);

        final FileAction fAc2_3 = new FileAction();
        fAc2_3.setFileId(person.getId());
        fAc2_3.setCommitId(commit2);

        final FileAction fAc2_4 = new FileAction();
        fAc2_4.setFileId(main.getId());
        fAc2_4.setCommitId(commit2);

        final FileAction fAc2_5 = new FileAction();
        fAc2_5.setFileId(address.getId());
        fAc2_5.setCommitId(commit2);

        datastore.save(fAc2_1);
        datastore.save(fAc2_3);
        datastore.save(fAc2_4);
        datastore.save(fAc2_5);

        // Commit3: personTest, person, confXML changed
        final FileAction fAc3_1 = new FileAction();
        fAc3_1.setFileId(personTest.getId());
        fAc3_1.setCommitId(commit3);

        final FileAction fAc3_2 = new FileAction();
        fAc3_2.setFileId(person.getId());
        fAc3_2.setCommitId(commit3);

        final FileAction fAc3_3 = new FileAction();
        fAc3_3.setFileId(confXML.getId());
        fAc3_3.setCommitId(commit3);

        datastore.save(fAc3_1);
        datastore.save(fAc3_2);
        datastore.save(fAc3_3);

        // Commit4: personTest, person, address changed
        final FileAction fAc4_1 = new FileAction();
        fAc4_1.setFileId(personTest.getId());
        fAc4_1.setCommitId(commit4);

        final FileAction fAc4_2 = new FileAction();
        fAc4_2.setFileId(person.getId());
        fAc4_2.setCommitId(commit4);

        final FileAction fAc4_3 = new FileAction();
        fAc4_3.setFileId(address.getId());
        fAc4_3.setCommitId(commit4);

        datastore.save(fAc4_1);
        datastore.save(fAc4_2);
        datastore.save(fAc4_3);

        // Commit4: personTest, addresstest changed
        final FileAction fAc5_1 = new FileAction();
        fAc5_1.setFileId(addressTest.getId());
        fAc5_1.setCommitId(commit5);

        final FileAction fAc5_2 = new FileAction();
        fAc5_2.setFileId(personTest.getId());
        fAc5_2.setCommitId(commit5);

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
