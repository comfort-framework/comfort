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

package de.ugoe.cs.comfort.filer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.github.danielfelgar.morphia.Log4JLoggerImplFactory;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import de.ugoe.cs.comfort.BaseTest;
import de.ugoe.cs.comfort.DatabaseTest;
import de.ugoe.cs.comfort.collection.metriccollector.TestType;
import de.ugoe.cs.comfort.configuration.Database;
import de.ugoe.cs.comfort.configuration.GeneralConfiguration;
import de.ugoe.cs.comfort.database.models.TestState;
import de.ugoe.cs.comfort.filer.models.Mutation;
import de.ugoe.cs.comfort.filer.models.Result;
import de.ugoe.cs.comfort.filer.models.ResultSet;
import de.ugoe.cs.smartshark.model.Commit;
import de.ugoe.cs.smartshark.model.FileAction;
import de.ugoe.cs.smartshark.model.VCSSystem;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.logging.MorphiaLoggerFactory;

/**
 * @author Fabian Trautsch
 */
public class SmartSHARKFilerTest extends DatabaseTest {
    private GeneralConfiguration configuration = new GeneralConfiguration();

    private Commit commit;

    private de.ugoe.cs.smartshark.model.File modelTest;
    private de.ugoe.cs.smartshark.model.File modelTest1;

    private Set<Result> resultsToStore = new HashSet<>();
    private ResultSet resultSet;

    @Before
    public void createFilerConfiguration() {
        configuration.getFilerConfiguration().setDatabase(getDatabaseConfiguration());

        configuration.setProjectDir(getPathToResource("filerTestData/SmartSHARKFilerData/HikariCP_64d91e8ea35799bae2739d8392b5705627d7758a"));
        configuration.setLanguage("java");

        resultSet = new ResultSet();
        resultsToStore.clear();
    }

    @Before
    public void setUpDatabase() {
        // Clear database
        mongoClient.dropDatabase(DB_NAME);

        // Random projectId
        ObjectId projectId = new ObjectId();

        // Generate vcsSystem
        final VCSSystem vcsSystem = new VCSSystem();
        vcsSystem.setUrl("https://github.com/brettwooldridge/HikariCP");
        vcsSystem.setProjectId(projectId);
        vcsSystem.setRepositoryType("git");
        vcsSystem.setLastUpdated(new Date());
        datastore.save(vcsSystem);

        commit = new Commit();
        commit.setVcSystemId(vcsSystem.getId());
        commit.setRevisionHash("64d91e8ea35799bae2739d8392b5705627d7758a");
        datastore.save(commit);

        // Create files of the repository
        modelTest = new de.ugoe.cs.smartshark.model.File();
        modelTest.setVcsSystemId(vcsSystem.getId());
        modelTest.setPath("src/de/foo/bar/ModelTest.java");

        modelTest1 = new de.ugoe.cs.smartshark.model.File();
        modelTest1.setVcsSystemId(vcsSystem.getId());
        modelTest1.setPath("src/de/foo/bar/ModelTest1.java");

        datastore.save(modelTest);
        datastore.save(modelTest1);
    }

    @After
    public void tearDownDatabase() {
        mongoClient.dropDatabase(DB_NAME);
    }

    @Test
    public void storeResultsOnlyMetricsTest() {
        // Create test data
        res1.addMetric("istqb_call", TestType.UNIT.name());
        res2.addMetric("istqb_call", TestType.INTEGRATION.name());
        resultsToStore.add(res1);
        resultsToStore.add(res2);

        resultSet.addResults(resultsToStore);

        SmartSHARKFiler smartSHARKFiler = new SmartSHARKFiler();
        smartSHARKFiler.storeResults(configuration, resultSet);

        // Check stored results
        List<TestState> testStates = datastore.createQuery(TestState.class).order("name").asList();

        List<TestState> expectedStates = new ArrayList<>();
        TestState res1State = new TestState();
        res1State.setId(testStates.get(0).getId());
        res1State.setName("de.foo.bar.ModelTest");
        res1State.setCommitId(commit.getId());
        res1State.setFileId(modelTest.getId());
        res1State.setMetrics(new HashMap<String, String>(){{
            put("istqb_call", TestType.UNIT.name());
        }});
        res1State.setMutations(null);
        expectedStates.add(res1State);

        TestState res2State = new TestState();
        res2State.setId(testStates.get(1).getId());
        res2State.setName("de.foo.bar.ModelTest1");
        res2State.setCommitId(commit.getId());
        res2State.setFileId(modelTest1.getId());
        res2State.setMetrics(new HashMap<String, String>(){{
            put("istqb_call", TestType.INTEGRATION.name());
        }});
        res2State.setMutations(null);
        expectedStates.add(res2State);

        assertEquals(expectedStates, testStates);
    }

    @Test
    public void storeMutationDataTest() {
        // Create test data
        res1.addMutationResults(mutationResultsRes1);
        res1.addMetric("mut_genMut", "1402");
        res1.addMetric("mut_killMut", "100");
        res1.addMetric("mut_scoreMut", "2");

        res2.addMutationResults(mutationResultsRes2);
        res2.addMetric("mut_genMut", "123");
        res2.addMetric("mut_killMut", "2");
        res2.addMetric("mut_scoreMut", "2");


        resultsToStore.add(res1);
        resultsToStore.add(res2);

        resultSet.addResults(resultsToStore);

        SmartSHARKFiler smartSHARKFiler = new SmartSHARKFiler();
        smartSHARKFiler.storeResults(configuration, resultSet);

        // Check stored results
        List<TestState> testStates = datastore.createQuery(TestState.class).order("name").asList();

        List<TestState> expectedStates = new ArrayList<>();
        TestState res1State = new TestState();
        res1State.setId(testStates.get(0).getId());
        res1State.setName("de.foo.bar.ModelTest");
        res1State.setCommitId(commit.getId());
        res1State.setFileId(modelTest.getId());
        res1State.setMetrics(new HashMap<String, String>(){{
            put("mut_genMut", "1402");
            put("mut_killMut", "100");
            put("mut_scoreMut", "2");
        }});
        res1State.setMutations(mutationResultsRes1);
        expectedStates.add(res1State);

        TestState res2State = new TestState();
        res2State.setId(testStates.get(1).getId());
        res2State.setName("de.foo.bar.ModelTest1");
        res2State.setCommitId(commit.getId());
        res2State.setFileId(modelTest1.getId());
        res2State.setMetrics(new HashMap<String, String>(){{
            put("mut_genMut", "123");
            put("mut_killMut", "2");
            put("mut_scoreMut", "2");
        }});
        res2State.setMutations(mutationResultsRes2);
        expectedStates.add(res2State);

        assertEquals(expectedStates, testStates);

    }

}
