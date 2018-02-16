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
import static org.junit.Assert.assertNotNull;

import de.ugoe.cs.comfort.DatabaseTest;
import de.ugoe.cs.comfort.collection.metriccollector.TestType;
import de.ugoe.cs.comfort.configuration.GeneralConfiguration;
import de.ugoe.cs.smartshark.model.MutationResult;
import de.ugoe.cs.smartshark.model.TestState;
import de.ugoe.cs.comfort.filer.models.Result;
import de.ugoe.cs.comfort.filer.models.ResultSet;
import de.ugoe.cs.smartshark.model.Commit;
import de.ugoe.cs.smartshark.model.VCSSystem;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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

        configuration.setProjectDir(getPathToResource("filerTestData/SmartSHARKFilerData"));
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
        vcsSystem.setUrl("git@github.com:comfort-framework/comfort.git");
        vcsSystem.setProjectId(projectId);
        vcsSystem.setRepositoryType("git");
        vcsSystem.setLastUpdated(new Date());
        datastore.save(vcsSystem);

        commit = new Commit();
        commit.setVcSystemId(vcsSystem.getId());
        commit.setRevisionHash("13a2f32a6c91a472d186348d7dfbdf8b9e92f16f");
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
    public void storeSingleResultTest() throws IOException {
        SmartSHARKFiler smartSHARKFiler = new SmartSHARKFiler(configuration, configuration.getFilerConfiguration(),
                commit.getId(), new HashMap<Path, ObjectId>(){{
            put(Paths.get(modelTest.getPath()), modelTest.getId());
            put(Paths.get(modelTest1.getPath()), modelTest1.getId());
        }});

        res1.addMetric("istqb_call", TestType.UNIT.name());
        smartSHARKFiler.storeResult(res1);

        // Check stored results
        TestState testState = datastore.createQuery(TestState.class).order("name").get();


        TestState expectedState = new TestState();
        expectedState.setId(testState.getId());
        expectedState.setName("de.foo.bar.ModelTest");
        expectedState.setCommitId(commit.getId());
        expectedState.setFileId(modelTest.getId());
        expectedState.setMetrics(new HashMap<String, String>(){{
            put("istqb_call", TestType.UNIT.name());
        }});

        assertEquals(expectedState, testState);
    }

    @Test
    public void storeTwoSingleResultTest() throws IOException {
        SmartSHARKFiler smartSHARKFiler = new SmartSHARKFiler(configuration, configuration.getFilerConfiguration(),
                commit.getId(), new HashMap<Path, ObjectId>(){{
            put(Paths.get(modelTest.getPath()), modelTest.getId());
            put(Paths.get(modelTest1.getPath()), modelTest1.getId());
        }});

        res1.addMetric("istqb_call", TestType.UNIT.name());
        smartSHARKFiler.storeResult(res1);

        Result res3 = new Result("de.foo.bar.ModelTest", Paths.get("src/de/foo/bar/ModelTest.java"), "fantasy_metric", "5");
        smartSHARKFiler.storeResult(res3);

        // Check stored results
        TestState testState = datastore.createQuery(TestState.class).order("name").get();


        TestState expectedState = new TestState();
        expectedState.setId(testState.getId());
        expectedState.setName("de.foo.bar.ModelTest");
        expectedState.setCommitId(commit.getId());
        expectedState.setFileId(modelTest.getId());
        expectedState.setMetrics(new HashMap<String, String>(){{
            put("istqb_call", TestType.UNIT.name());
            put("fantasy_metric", "5");
        }});

        assertEquals(expectedState, testState);
    }

    @Test
    public void storeTwoDifferentResultsAfterAnotherTest() throws IOException {
        SmartSHARKFiler smartSHARKFiler = new SmartSHARKFiler(configuration, configuration.getFilerConfiguration(),
                commit.getId(), new HashMap<Path, ObjectId>(){{
            put(Paths.get(modelTest.getPath()), modelTest.getId());
            put(Paths.get(modelTest1.getPath()), modelTest1.getId());
        }});

        // Create test data
        res1.addMetric("istqb_call", TestType.UNIT.name());
        res2.addMetric("istqb_call", TestType.INTEGRATION.name());
        resultsToStore.add(res1);
        resultsToStore.add(res2);

        resultSet.addResults(resultsToStore);
        smartSHARKFiler.storeResults(resultSet.getResults());

        resultsToStore.clear();
        Result res3 = new Result("de.foo.bar.ModelTest", Paths.get("src/de/foo/bar/ModelTest.java"), "fantasy_metric", "5");
        Result res4 = new Result("de.foo.bar.ModelTest1", Paths.get("src/de/foo/bar/ModelTest1.java"), "fantasy_metric", "2");
        resultsToStore.add(res3);
        resultsToStore.add(res4);
        smartSHARKFiler.storeResults(resultsToStore);

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
            put("fantasy_metric", "5");
        }});
        expectedStates.add(res1State);

        TestState res2State = new TestState();
        res2State.setId(testStates.get(1).getId());
        res2State.setName("de.foo.bar.ModelTest1");
        res2State.setCommitId(commit.getId());
        res2State.setFileId(modelTest1.getId());
        res2State.setMetrics(new HashMap<String, String>(){{
            put("istqb_call", TestType.INTEGRATION.name());
            put("fantasy_metric", "2");
        }});
        expectedStates.add(res2State);

        assertEquals(expectedStates, testStates);
    }


    @Test
    public void storeResultsOnlyMetricsTest() throws Exception {
        // Create test data
        res1.addMetric("istqb_call", TestType.UNIT.name());
        res2.addMetric("istqb_call", TestType.INTEGRATION.name());
        resultsToStore.add(res1);
        resultsToStore.add(res2);

        resultSet.addResults(resultsToStore);

        SmartSHARKFiler smartSHARKFiler = new SmartSHARKFiler(configuration, configuration.getFilerConfiguration(),
                commit.getId(), new HashMap<Path, ObjectId>(){{
            put(Paths.get(modelTest.getPath()), modelTest.getId());
            put(Paths.get(modelTest1.getPath()), modelTest1.getId());
        }});
        smartSHARKFiler.storeResults(resultSet.getResults());

        // Verify that getRepository is called
        //verifyPrivate(smartSHARKFiler).invoke("getRepository", configuration.getProjectDir());

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
        expectedStates.add(res1State);

        TestState res2State = new TestState();
        res2State.setId(testStates.get(1).getId());
        res2State.setName("de.foo.bar.ModelTest1");
        res2State.setCommitId(commit.getId());
        res2State.setFileId(modelTest1.getId());
        res2State.setMetrics(new HashMap<String, String>(){{
            put("istqb_call", TestType.INTEGRATION.name());
        }});
        expectedStates.add(res2State);

        assertEquals(expectedStates, testStates);
    }

    @Test
    public void storeMutationDataTest() throws Exception {
        // Create test data
        res1.addMutationResults(mutations1);
        res1.addMetric("mut_genMut", "1402");
        res1.addMetric("mut_killMut", "100");
        res1.addMetric("mut_scoreMut", "2");

        res2.addMutationResults(mutations2);
        res2.addMetric("mut_genMut", "123");
        res2.addMetric("mut_killMut", "2");
        res2.addMetric("mut_scoreMut", "2");


        resultsToStore.add(res1);
        resultsToStore.add(res2);

        resultSet.addResults(resultsToStore);

        SmartSHARKFiler smartSHARKFiler = new SmartSHARKFiler(configuration, configuration.getFilerConfiguration(),
                commit.getId(), new HashMap<Path, ObjectId>(){{
            put(Paths.get(modelTest.getPath()), modelTest.getId());
            put(Paths.get(modelTest1.getPath()), modelTest1.getId());
        }});
        smartSHARKFiler.storeResults(resultSet.getResults());

        // Check stored results
        List<TestState> testStates = datastore.createQuery(TestState.class).order("name").asList();

        // Get stored mutations
        de.ugoe.cs.smartshark.model.Mutation dbMutation1 = datastore.createQuery(de.ugoe.cs.smartshark.model.Mutation.class)
                .field("location").equal("de.foo.bar.Model.addBatch")
                .field("m_type").equal("org.pitest.mutationtest.engine.gregor.mutators.VoidMethodCallMutator")
                .field("l_num").equal(10)
                .get();
        assertNotNull(dbMutation1);

        de.ugoe.cs.smartshark.model.Mutation dbMutation2 = datastore.createQuery(de.ugoe.cs.smartshark.model.Mutation.class)
                .field("location").equal("de.foo.bar.Model.addBatch")
                .field("m_type").equal("org.pitest.mutationtest.engine.gregor.mutators.VoidMethodCallMutator")
                .field("l_num").equal(11)
                .get();
        assertNotNull(dbMutation2);

        de.ugoe.cs.smartshark.model.Mutation dbMutation3 = datastore.createQuery(de.ugoe.cs.smartshark.model.Mutation.class)
                .field("location").equal("de.foo.bar.Model.addBatch")
                .field("m_type").equal("org.pitest.mutationtest.engine.gregor.mutators.VoidMethodCallMutator")
                .field("l_num").equal(30)
                .get();
        assertNotNull(dbMutation3);

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
        res1State.setMutations(new HashSet<MutationResult>(){{
            add(new MutationResult(dbMutation1.getId(), "NO_COVERAGE"));
            add(new MutationResult(dbMutation2.getId(), "SURVIVED"));
            add(new MutationResult(dbMutation3.getId(), "SURVIVED"));
        }});
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
        res2State.setMutations(new HashSet<MutationResult>(){{
            add(new MutationResult(dbMutation1.getId(), "NO_COVERAGE"));
            add(new MutationResult(dbMutation3.getId(), "KILLED"));
        }});
        expectedStates.add(res2State);

        assertEquals(expectedStates, testStates);

    }
}
