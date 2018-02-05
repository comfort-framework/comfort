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

import de.ugoe.cs.comfort.BaseTest;
import de.ugoe.cs.comfort.collection.metriccollector.TestType;
import de.ugoe.cs.comfort.configuration.GeneralConfiguration;
import de.ugoe.cs.comfort.filer.models.Result;
import de.ugoe.cs.comfort.filer.models.ResultSet;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Fabian Trautsch
 */
public class CSVFilerTest extends BaseTest{
    private GeneralConfiguration configuration = new GeneralConfiguration();
    private final String filerOutput = getPathToResource("filerTestData/CSVFilerData");


    private Set<Result> resultsToStore = new HashSet<>();
    private ResultSet resultSet;
    private Path metricsCSVPath = Paths.get(filerOutput, "metrics.csv");
    private Path mutationCSVPath = Paths.get(filerOutput, "mutation.csv");

    @After
    public void clearOutputDirectory() throws IOException {
        FileUtils.cleanDirectory(new File(filerOutput));
    }

    @Before
    public void createFilerConfiguration() {
        configuration.getFilerConfiguration().setMetricsCSVPath(metricsCSVPath.toString());
        configuration.getFilerConfiguration().setMutationCSVPath(mutationCSVPath.toString());

        resultSet = new ResultSet();
        resultsToStore.clear();
    }

    @Test
    public void csvFilerStoreResultsOnlyPathsTest() {
        // Create test data
        res1.addMetric("istqb_call", TestType.UNIT.name());
        res2.addMetric("istqb_call", TestType.INTEGRATION.name());
        resultsToStore.add(res1);
        resultsToStore.add(res2);

        resultSet.addResults(resultsToStore);

        String expectedContent =
                "id,path,istqb_call\n" +
                "de.foo.bar.ModelTest,src/de/foo/bar/ModelTest.java,UNIT\n" +
                "de.foo.bar.ModelTest1,src/de/foo/bar/ModelTest1.java,INTEGRATION";

        testResultsAgainstExpectedOutput(resultSet, expectedContent, metricsCSVPath);
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

        String expectedContent =
                "id,path,location,m_type,line_number,result\n" +
                "de.foo.bar.ModelTest,src/de/foo/bar/ModelTest.java,de.foo.bar.Model.addBatch,org.pitest.mutationtest.engine.gregor.mutators.VoidMethodCallMutator,10,NO_COVERAGE\n" +
                "de.foo.bar.ModelTest,src/de/foo/bar/ModelTest.java,de.foo.bar.Model.addBatch,org.pitest.mutationtest.engine.gregor.mutators.VoidMethodCallMutator,11,SURVIVED\n" +
                "de.foo.bar.ModelTest1,src/de/foo/bar/ModelTest1.java,de.foo.bar.Model.addBatch,org.pitest.mutationtest.engine.gregor.mutators.VoidMethodCallMutator,10,NO_COVERAGE\n" +
                "de.foo.bar.ModelTest1,src/de/foo/bar/ModelTest1.java,de.foo.bar.Model.addBatch,org.pitest.mutationtest.engine.gregor.mutators.VoidMethodCallMutator,30,KILLED";

        testResultsAgainstExpectedOutput(resultSet, expectedContent, mutationCSVPath);

        expectedContent =
                "id,path,mut_killMut,mut_genMut,mut_scoreMut\n" +
                "de.foo.bar.ModelTest,src/de/foo/bar/ModelTest.java,100,1402,2\n" +
                "de.foo.bar.ModelTest1,src/de/foo/bar/ModelTest1.java,2,123,2";

        testResultsAgainstExpectedOutput(resultSet, expectedContent, metricsCSVPath);

    }

    private void testResultsAgainstExpectedOutput(ResultSet results, String expectedContent, Path csvPath) {
        Path createdCSVFile;
        CSVFiler csvFiler = new CSVFiler();
        try {
            // Store results
            csvFiler.storeResults(configuration, results);

            // Check contents of file
            String content = new String(Files.readAllBytes(csvPath), ("UTF-8"));
            assertEquals("Contents not the same!", expectedContent, content);
        } catch (IOException e) {
            fail("Unexpected exception! "+e);
        }
    }
}
