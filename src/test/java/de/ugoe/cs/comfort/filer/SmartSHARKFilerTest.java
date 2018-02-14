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
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.verifyPrivate;
import static org.powermock.api.mockito.PowerMockito.when;

import de.ugoe.cs.comfort.DatabaseTest;
import de.ugoe.cs.comfort.collection.metriccollector.TestType;
import de.ugoe.cs.comfort.configuration.GeneralConfiguration;
import de.ugoe.cs.comfort.database.models.MutationResult;
import de.ugoe.cs.comfort.database.models.TestState;
import de.ugoe.cs.comfort.filer.models.Result;
import de.ugoe.cs.comfort.filer.models.ResultSet;
import de.ugoe.cs.smartshark.model.Commit;
import de.ugoe.cs.smartshark.model.VCSSystem;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bson.types.ObjectId;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * @author Fabian Trautsch
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(SmartSHARKFiler.class)
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

    private SmartSHARKFiler getMockedSmartSHARKFiler() throws Exception {
        // Mock Repository and the config so that the right values are eturned
        Repository repositoryMock = mock(Repository.class);
        StoredConfig storedConfigMock = mock(StoredConfig.class);
        when(storedConfigMock.getString("remote", "origin", "url")).thenReturn("git@github.com:comfort-framework/comfort.git");
        when(repositoryMock.getConfig()).thenReturn(storedConfigMock);
        org.eclipse.jgit.lib.ObjectId objectId = org.eclipse.jgit.lib.ObjectId.fromString("13a2f32a6c91a472d186348d7dfbdf8b9e92f16f");
        when(repositoryMock.resolve(Constants.HEAD)).thenReturn(objectId);

        SmartSHARKFiler smartSHARKFiler = spy(new SmartSHARKFiler());
        when(smartSHARKFiler, "getRepository", configuration.getProjectDir()).thenReturn(repositoryMock);

        return smartSHARKFiler;
    }

    @Test
    public void storeResultsOnlyMetricsTest() throws Exception {
        // Create test data
        res1.addMetric("istqb_call", TestType.UNIT.name());
        res2.addMetric("istqb_call", TestType.INTEGRATION.name());
        resultsToStore.add(res1);
        resultsToStore.add(res2);

        resultSet.addResults(resultsToStore);

        SmartSHARKFiler smartSHARKFiler = getMockedSmartSHARKFiler();
        smartSHARKFiler.storeResults(configuration, resultSet);

        // Verify that getRepository is called
        verifyPrivate(smartSHARKFiler).invoke("getRepository", configuration.getProjectDir());

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

        SmartSHARKFiler smartSHARKFiler = getMockedSmartSHARKFiler();
        smartSHARKFiler.storeResults(configuration, resultSet);

        // Verify that getRepository is called
        verifyPrivate(smartSHARKFiler).invoke("getRepository", configuration.getProjectDir());

        // Check stored results
        List<TestState> testStates = datastore.createQuery(TestState.class).order("name").asList();

        // Get stored mutations
        de.ugoe.cs.comfort.database.models.Mutation dbMutation1 = datastore.createQuery(de.ugoe.cs.comfort.database.models.Mutation.class)
                .field("location").equal("de.foo.bar.Model.addBatch")
                .field("m_type").equal("org.pitest.mutationtest.engine.gregor.mutators.VoidMethodCallMutator")
                .field("l_num").equal(10)
                .get();
        assertNotNull(dbMutation1);

        de.ugoe.cs.comfort.database.models.Mutation dbMutation2 = datastore.createQuery(de.ugoe.cs.comfort.database.models.Mutation.class)
                .field("location").equal("de.foo.bar.Model.addBatch")
                .field("m_type").equal("org.pitest.mutationtest.engine.gregor.mutators.VoidMethodCallMutator")
                .field("l_num").equal(11)
                .get();
        assertNotNull(dbMutation2);

        de.ugoe.cs.comfort.database.models.Mutation dbMutation3 = datastore.createQuery(de.ugoe.cs.comfort.database.models.Mutation.class)
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
