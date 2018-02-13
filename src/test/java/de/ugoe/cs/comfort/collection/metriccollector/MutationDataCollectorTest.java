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

package de.ugoe.cs.comfort.collection.metriccollector;

import static org.junit.Assert.assertEquals;

import de.ugoe.cs.comfort.BaseTest;
import de.ugoe.cs.comfort.configuration.GeneralConfiguration;
import de.ugoe.cs.comfort.data.CoverageData;
import de.ugoe.cs.comfort.data.models.JavaMethod;
import de.ugoe.cs.comfort.filer.models.Mutation;
import de.ugoe.cs.comfort.filer.models.Result;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Fabian Trautsch
 */
public class MutationDataCollectorTest extends BaseTest {
    private final String basePath = getPathToResource("metricCollectorTestData/mutationdatacollector");
    private GeneralConfiguration javaConfig = new GeneralConfiguration();


    @Before
    public void createJavaConfig() {
        javaConfig.setProjectDir(basePath);
        javaConfig.setMethodLevel(true);
    }

    @Test
    public void collectMutationDataTestSingleThreaded() {
        collectMutationData();
    }

    @Test
    public void collectMutationDataMultiThreaded() {
        javaConfig.setNThreads(4);
        collectMutationData();
    }

    private void collectMutationData() {
        CoverageData covData = new CoverageData();
        covData.add(new JavaMethod("Module1Test", "getNameTest", new ArrayList<>(), null), null);
        covData.add(new JavaMethod("Module1Test", "getNumberTest", new ArrayList<>(), null), null);
        covData.add(new JavaMethod("Module2Test", "getNameTest", new ArrayList<>(), null), null);
        covData.add(new JavaMethod("Module2Test", "getNumberTest", new ArrayList<>(), null), null);


        MutationDataCollector mutationDataCollector = new MutationDataCollector(javaConfig);
        Set<Result> results = mutationDataCollector.getMutationDataMetrics(covData);

        Set<Result> expectedResult = new HashSet<>();
        Result mod1NameTest = new Result("Module1Test.getNameTest", Paths.get("src/test/java/Module1Test.java"));
        mod1NameTest.addMetric("mut_killMut", "5");
        mod1NameTest.addMetric("mut_genMut", "5");
        mod1NameTest.addMetric("mut_scoreMut", "100");
        mod1NameTest.addMutationResults(new HashSet<Mutation>(){
            {
                add(new Mutation("Module1.getName", "org.pitest.mutationtest.engine.gregor.mutators.MathMutator", 30, "KILLED", "COMPUTATION"));
                add(new Mutation("Module1.getName", "org.pitest.mutationtest.engine.gregor.mutators.ReturnValsMutator", 31, "KILLED", "LOGIC/CONTROL"));
                add(new Mutation("Module1.getNumber", "org.pitest.mutationtest.engine.gregor.mutators.MathMutator", 36, "KILLED", "COMPUTATION"));
                add(new Mutation("Module1.getNumber", "org.pitest.mutationtest.engine.gregor.mutators.MathMutator", 37, "KILLED", "LOGIC/CONTROL"));
                add(new Mutation("Module1.getNumber", "org.pitest.mutationtest.engine.gregor.mutators.ReturnValsMutator", 37, "KILLED", "LOGIC/CONTROL"));
            }
        });
        Result mod1NumberTest = new Result("Module1Test.getNumberTest", Paths.get("src/test/java/Module1Test.java"));
        mod1NumberTest.addMetric("mut_killMut", "3");
        mod1NumberTest.addMetric("mut_genMut", "5");
        mod1NumberTest.addMetric("mut_scoreMut", "60");
        mod1NumberTest.addMutationResults(new HashSet<Mutation>(){
            {
                add(new Mutation("Module1.getName", "org.pitest.mutationtest.engine.gregor.mutators.MathMutator", 30, "NO_COVERAGE", "COMPUTATION"));
                add(new Mutation("Module1.getName", "org.pitest.mutationtest.engine.gregor.mutators.ReturnValsMutator", 31, "NO_COVERAGE", "LOGIC/CONTROL"));
                add(new Mutation("Module1.getNumber", "org.pitest.mutationtest.engine.gregor.mutators.MathMutator", 36, "KILLED", "COMPUTATION"));
                add(new Mutation("Module1.getNumber", "org.pitest.mutationtest.engine.gregor.mutators.MathMutator", 37, "KILLED", "LOGIC/CONTROL"));
                add(new Mutation("Module1.getNumber", "org.pitest.mutationtest.engine.gregor.mutators.ReturnValsMutator", 37, "KILLED", "LOGIC/CONTROL"));
            }
        });
        Result mod2NameTest = new Result("Module2Test.getNameTest", Paths.get("src/test/java/Module2Test.java"));
        mod2NameTest.addMetric("mut_killMut", "5");
        mod2NameTest.addMetric("mut_genMut", "5");
        mod2NameTest.addMetric("mut_scoreMut", "100");
        mod2NameTest.addMutationResults(new HashSet<Mutation>(){
            {
                add(new Mutation("Module1.getName", "org.pitest.mutationtest.engine.gregor.mutators.MathMutator", 30, "KILLED", "COMPUTATION"));
                add(new Mutation("Module1.getName", "org.pitest.mutationtest.engine.gregor.mutators.ReturnValsMutator", 31, "KILLED", "LOGIC/CONTROL"));
                add(new Mutation("Module1.getNumber", "org.pitest.mutationtest.engine.gregor.mutators.MathMutator", 36, "KILLED", "COMPUTATION"));
                add(new Mutation("Module1.getNumber", "org.pitest.mutationtest.engine.gregor.mutators.MathMutator", 37, "KILLED", "LOGIC/CONTROL"));
                add(new Mutation("Module1.getNumber", "org.pitest.mutationtest.engine.gregor.mutators.ReturnValsMutator", 37, "KILLED", "LOGIC/CONTROL"));
            }
        });
        Result mod2NumberTest = new Result("Module2Test.getNumberTest", Paths.get("src/test/java/Module2Test.java"));
        mod2NumberTest.addMetric("mut_killMut", "3");
        mod2NumberTest.addMetric("mut_genMut", "5");
        mod2NumberTest.addMetric("mut_scoreMut", "60");
        mod2NumberTest.addMutationResults(new HashSet<Mutation>(){
            {
                add(new Mutation("Module1.getName", "org.pitest.mutationtest.engine.gregor.mutators.MathMutator", 30, "NO_COVERAGE", "COMPUTATION"));
                add(new Mutation("Module1.getName", "org.pitest.mutationtest.engine.gregor.mutators.ReturnValsMutator", 31, "NO_COVERAGE", "LOGIC/CONTROL"));
                add(new Mutation("Module1.getNumber", "org.pitest.mutationtest.engine.gregor.mutators.MathMutator", 36, "KILLED", "COMPUTATION"));
                add(new Mutation("Module1.getNumber", "org.pitest.mutationtest.engine.gregor.mutators.MathMutator", 37, "KILLED", "LOGIC/CONTROL"));
                add(new Mutation("Module1.getNumber", "org.pitest.mutationtest.engine.gregor.mutators.ReturnValsMutator", 37, "KILLED", "LOGIC/CONTROL"));
            }
        });


        expectedResult.add(mod1NameTest);
        expectedResult.add(mod1NumberTest);
        expectedResult.add(mod2NameTest);
        expectedResult.add(mod2NumberTest);
        assertEquals("Result set is not correct!", expectedResult, results);
    }
}
