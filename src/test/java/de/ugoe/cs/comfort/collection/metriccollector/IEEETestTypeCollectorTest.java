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
import de.ugoe.cs.comfort.data.graphs.CallEdge;
import de.ugoe.cs.comfort.data.graphs.CallGraph;
import de.ugoe.cs.comfort.data.graphs.CallType;
import de.ugoe.cs.comfort.data.graphs.DependencyGraph;
import de.ugoe.cs.comfort.data.models.IUnit;
import de.ugoe.cs.comfort.filer.models.Result;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Fabian Trautsch
 */
public class IEEETestTypeCollectorTest extends BaseTest {
    private final String basePath = getPathToResource("metricCollectorTestData/ieeeAndistqb");

    private IEEETestTypeCollector ieeeTestTypeCollector;

    private GeneralConfiguration javaConfig = new GeneralConfiguration();
    private GeneralConfiguration pythonConfig = new GeneralConfiguration();

    private Set<Result> result = new HashSet<>();
    private Set<Result> expectedResult = new HashSet<>();

    @Before
    public void createJavaConfig() {
        javaConfig.setProjectDir(basePath+"/java");
    }

    @Before
    public void createPythonConfig() {
        pythonConfig.setProjectDir(basePath+"/python");
        pythonConfig.setLanguage("python");
    }


    @Before
    public void clearResults() {
        result.clear();
        expectedResult.clear();
    }

    @Test
    public void createResultsJavaClassLevelCallGraphTest() {
        /*
         * Graph:
         * org.foo.t1.T1.m1 -> org.foo.bar.C2.m1
         * org.foo.t1.T1.m2 -> org.foo.bar.blub.C2.m1
         * Result: T1 = integration test
         */

        CallGraph callGraph = new CallGraph();
        callGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, T1M1, C2M1_p1));
        callGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, T1M2, C2M1_p2));

        ieeeTestTypeCollector = new IEEETestTypeCollector(javaConfig);
        result = ieeeTestTypeCollector.createResultsJavaPythonClassLevelCallGraph(callGraph);

        expectedResult.add(new Result("org.foo.t1.Test1", null, "call_ieee",TestType.INTEGRATION.name()));
        assertEquals("Result set is not correct!", expectedResult, result);
    }

    @Test
    public void createResultsPythonClassLevelCallGraphTest() {
        CallGraph callGraph = new CallGraph();
        callGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, pyTest1Test, pyModule4Init));
        callGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, pyTest1Test2, pyModule5Init));

        ieeeTestTypeCollector = new IEEETestTypeCollector(javaConfig);
        result = ieeeTestTypeCollector.createResultsJavaPythonClassLevelCallGraph(callGraph);

        expectedResult.add(new Result("tests.test_module1", null, "call_ieee",TestType.INTEGRATION.name()));
        assertEquals("Result set is not correct!", expectedResult, result);
    }

    @Test
    public void createResultsPythonClassLevelCallGraphWithUnitAndIntegrationTest() {
        CallGraph callGraph = new CallGraph();
        callGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, pyTest1Test, pyModule5Init));
        callGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, pyTest1Test, pyModule4Init));
        callGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, pyTest2Test, pyModule2Foo));
        callGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, pyTest2Test, pyModule2Init));

        pythonConfig.setMethodLevel(true);
        ieeeTestTypeCollector = new IEEETestTypeCollector(pythonConfig);
        result = ieeeTestTypeCollector.createResultsJavaPythonMethodLevelCallGraph(callGraph);

        expectedResult.add(new Result("tests.test_module1.Module1Test.test", null, "call_ieee_met",TestType.INTEGRATION.name()));
        expectedResult.add(new Result("tests.test_module2.Module2Test.test", null, "call_ieee_met",TestType.UNIT.name()));
        assertEquals("Result set is not correct!", expectedResult, result);
    }

    @Test
    public void createResultsJavaClassLevelCallGraphWithUnitAndIntegrationTest() {
        /*
         * Graph:
         * org.foo.t1.T1.m1 -> org.foo.bar.C2.m1
         * org.foo.t1.T1.m2 -> org.foo.bar.C3.m1
         * org.foo.t2.T2.m1 -> org.foo.bar.C1.m1
         * org.foo.t2.T2.m2 -> org.foo.C1.m1
         * Result: T1 = unit test, T2 = integration test
         */

        CallGraph callGraph = new CallGraph();
        callGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, T1M1, C2M1_p1));
        callGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, T1M2, C3M1));
        callGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, T2M1, C1M1_p1));
        callGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, T2M2, C1M1_p2));

        ieeeTestTypeCollector = new IEEETestTypeCollector(javaConfig);
        result = ieeeTestTypeCollector.createResultsJavaPythonClassLevelCallGraph(callGraph);

        expectedResult.add(new Result("org.foo.t1.Test1", null, "call_ieee",TestType.UNIT.name()));
        expectedResult.add(new Result("org.foo.t2.Test2", null, "call_ieee",TestType.INTEGRATION.name()));
        assertEquals("Result set is not correct!", expectedResult, result);
    }

    @Test
    public void createResultsJavaClassLevelCallGraphCorrectPathTest() {
        CallGraph callGraph = new CallGraph();
        callGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, addressTestGetAddressTest, javaLangObjectInit));

        ieeeTestTypeCollector = new IEEETestTypeCollector(javaConfig);
        result = ieeeTestTypeCollector.createResultsJavaPythonClassLevelCallGraph(callGraph);

        expectedResult.add(new Result("org.foo.models.AddressTest",
                Paths.get("src/test/java/org/foo/models/AddressTest.java"),
                "call_ieee",TestType.UNIT.name()));
        assertEquals("Result set is not correct!", expectedResult, result);
    }

    @Test
    public void classifyCallGraphWithUnitAndIntegrationTestOnMethodLevelTest() {
        CallGraph callGraph = new CallGraph();
        callGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, T1Test1, C2M1_p1));
        callGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 1, T1Test1, C3M1));
        callGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, T2Test1, C1M1_p1));
        callGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 1, T2Test1, C1M1_p2));

        ieeeTestTypeCollector = new IEEETestTypeCollector(javaConfig);
        result = ieeeTestTypeCollector.createResultsJavaPythonMethodLevelCallGraph(callGraph);

        expectedResult.add(new Result("org.foo.t1.Test1.test1", null, "call_ieee_met",TestType.UNIT.name()));
        expectedResult.add(new Result("org.foo.t2.Test2.test1", null, "call_ieee_met",TestType.INTEGRATION.name()));
        assertEquals("Result set is not correct!", expectedResult, result);
    }

    @Test
    public void classifyPythonDependencyGraphWithIntegrationTestTest() {
        DependencyGraph dependencyGraph = new DependencyGraph();
        dependencyGraph.putEdge(pyTest1, module1);
        dependencyGraph.putEdge(pyTest1, module2);

        ieeeTestTypeCollector = new IEEETestTypeCollector(pythonConfig);
        result = ieeeTestTypeCollector.createResultsJavaPythonDepGraph(dependencyGraph);

        expectedResult.add(new Result("tests.test1", Paths.get("tests/test1.py"), "dep_ieee",TestType.INTEGRATION.name()));
        assertEquals("Result set is not correct!", expectedResult, result);
    }

    @Test
    public void classifyPythonDependencyGraphWithUnitAndIntegrationTest() {
        DependencyGraph dependencyGraph = new DependencyGraph();
        dependencyGraph.putEdge(pyTest1, module1);
        dependencyGraph.putEdge(pyTest1, module3);
        dependencyGraph.putEdge(pyTest2, module1);
        dependencyGraph.putEdge(pyTest2, module2);

        ieeeTestTypeCollector = new IEEETestTypeCollector(pythonConfig);
        result = ieeeTestTypeCollector.createResultsJavaPythonDepGraph(dependencyGraph);

        expectedResult.add(new Result("tests.test1", Paths.get("tests/test1.py"), "dep_ieee",TestType.UNIT.name()));
        expectedResult.add(new Result("tests.test2", Paths.get("tests/test2.py"),"dep_ieee",TestType.INTEGRATION.name()));
        assertEquals("Result set is not correct!", expectedResult, result);
    }

    @Test
    public void classifyDependencyGraphWithIntegrationTestTest() {
        DependencyGraph dependencyGraph = new DependencyGraph();
        dependencyGraph.putEdge(blatestbla, entryView);
        dependencyGraph.putEdge(blatestbla, telephonebook);

        ieeeTestTypeCollector = new IEEETestTypeCollector(javaConfig);
        result = ieeeTestTypeCollector.createResultsJavaPythonDepGraph(dependencyGraph);

        expectedResult.add(new Result("org.foo.view.blatestbla", Paths.get("src/test/java/org/foo/view/blatestbla.java"), "dep_ieee",TestType.INTEGRATION.name()));
        assertEquals("Result set is not correct!", expectedResult, result);
    }



    @Test
    public void classifyDependencyGraphWithUnitAndIntegrationTestTest() {
        DependencyGraph dependencyGraph = new DependencyGraph();
        dependencyGraph.putEdge(blatestbla, C2);
        dependencyGraph.putEdge(blatestbla, C3);
        dependencyGraph.putEdge(personTest, C1);
        dependencyGraph.putEdge(personTest, address);

        ieeeTestTypeCollector = new IEEETestTypeCollector(javaConfig);
        result = ieeeTestTypeCollector.createResultsJavaPythonDepGraph(dependencyGraph);

        expectedResult.add(new Result("org.foo.view.blatestbla",  Paths.get("src/test/java/org/foo/view/blatestbla.java"),"dep_ieee", TestType.UNIT.name()));
        expectedResult.add(new Result("org.foo.models.Persontest", Paths.get("src/test/java/org/foo/models/Persontest.java"), "dep_ieee",TestType.INTEGRATION.name()));
        assertEquals("Result set is not correct!", expectedResult, result);
    }

    @Test
    public void classifyCodeCoverageJavaClassLevelTest() {
        Set<IUnit> testedMethodsOfTest1 = new HashSet<>();
        testedMethodsOfTest1.add(covP1C1M1);
        testedMethodsOfTest1.add(covP1C2M1);

        Set<IUnit> testedMethodsOfTest2 = new HashSet<>();
        testedMethodsOfTest2.add(covP1C1M1);
        testedMethodsOfTest2.add(covP2C1M1);

        CoverageData covData = new CoverageData();
        covData.add(T1Test1, testedMethodsOfTest1);
        covData.add(T2Test1, testedMethodsOfTest2);

        ieeeTestTypeCollector = new IEEETestTypeCollector(javaConfig);
        result = ieeeTestTypeCollector.createResultsJavaPythonCoverageClass(covData);

        expectedResult.add(new Result("org.foo.t1.Test1", null, "cov_ieee",TestType.UNIT.name()));
        expectedResult.add(new Result("org.foo.t2.Test2", null , "cov_ieee",TestType.INTEGRATION.name()));
        assertEquals("Result set is not correct!", expectedResult, result);
    }

    @Test
    public void classifyCodeCoverageJavaClassLevelSameClassDifferentMethodsTest() {
        Set<IUnit> testedMethodsOfTest1 = new HashSet<>();
        testedMethodsOfTest1.add(covP1C1M1);
        testedMethodsOfTest1.add(covP1C2M1);

        Set<IUnit> testedMethodsOfTest2 = new HashSet<>();
        testedMethodsOfTest2.add(covP1C1M1);
        testedMethodsOfTest2.add(covP2C1M1);

        CoverageData covData = new CoverageData();
        covData.add(T1Test1, testedMethodsOfTest1);
        covData.add(T1M1, testedMethodsOfTest2);

        ieeeTestTypeCollector = new IEEETestTypeCollector(javaConfig);
        result = ieeeTestTypeCollector.createResultsJavaPythonCoverageClass(covData);

        expectedResult.add(new Result("org.foo.t1.Test1", null, "cov_ieee",TestType.INTEGRATION.name()));
        assertEquals("Result set is not correct!", expectedResult, result);
    }

    @Test
    public void classifyCodeCoverageJavaMethodLevelTest() {
        Set<IUnit> testedMethodsOfTest1 = new HashSet<>();
        testedMethodsOfTest1.add(covP1C1M1);
        testedMethodsOfTest1.add(covP1C2M1);

        Set<IUnit> testedMethodsOfTest2 = new HashSet<>();
        testedMethodsOfTest2.add(covP1C1M1);
        testedMethodsOfTest2.add(covP2C1M1);

        CoverageData covData = new CoverageData();
        covData.add(T1Test1, testedMethodsOfTest1);
        covData.add(T1M1, testedMethodsOfTest2);

        javaConfig.setMethodLevel(true);
        ieeeTestTypeCollector = new IEEETestTypeCollector(javaConfig);
        result = ieeeTestTypeCollector.createResultsJavaPythonCoverageMethod(covData);

        expectedResult.add(new Result("org.foo.t1.Test1.test1", null, "cov_ieee_met",TestType.UNIT.name()));
        expectedResult.add(new Result("org.foo.t1.Test1.m1", null, "cov_ieee_met",TestType.INTEGRATION.name()));
        assertEquals("Result set is not correct!", expectedResult, result);
    }




    @Test
    public void classifyCodeCoveragePythonClassLevelTest() {
        Set<IUnit> testedMethodsOfTest1 = new HashSet<>();
        testedMethodsOfTest1.add(pyModule1Init);
        testedMethodsOfTest1.add(pyModule2Init);
        Set<IUnit> testedMethodsOfTest2 = new HashSet<>();
        testedMethodsOfTest2.add(pyModule4Init);
        testedMethodsOfTest2.add(pyModule5Init);

        CoverageData covData = new CoverageData();
        covData.add(pyTest1Test, testedMethodsOfTest1);
        covData.add(pyTest2Test, testedMethodsOfTest2);

        ieeeTestTypeCollector = new IEEETestTypeCollector(pythonConfig);
        result = ieeeTestTypeCollector.createResultsJavaPythonCoverageClass(covData);

        expectedResult.add(new Result("tests.test_module1", null, "cov_ieee", TestType.UNIT.name()));
        expectedResult.add(new Result("tests.test_module2", null, "cov_ieee", TestType.INTEGRATION.name()));
        assertEquals("Result set is not correct!", expectedResult, result);
    }


    @Test
    public void classifyCodeCoveragePythonMethodLevelTest() {
        Set<IUnit> testedMethodsOfTest1 = new HashSet<>();
        testedMethodsOfTest1.add(pyModule1Init);
        testedMethodsOfTest1.add(pyModule2Init);

        Set<IUnit> testedMethodsOfTest2 = new HashSet<>();
        testedMethodsOfTest2.add(pyModule4Init);
        testedMethodsOfTest2.add(pyModule5Init);

        CoverageData covData = new CoverageData();
        covData.add(pyTest1Test, testedMethodsOfTest1);
        covData.add(pyTest2Test, testedMethodsOfTest2);

        pythonConfig.setMethodLevel(true);
        ieeeTestTypeCollector = new IEEETestTypeCollector(pythonConfig);
        result = ieeeTestTypeCollector.createResultsJavaPythonCoverageMethod(covData);

        expectedResult.add(new Result("tests.test_module1.Module1Test.test", null, "cov_ieee_met", TestType.UNIT.name()));
        expectedResult.add(new Result("tests.test_module2.Module2Test.test", null, "cov_ieee_met", TestType.INTEGRATION.name()));
        assertEquals("Result set is not correct!", expectedResult, result);
    }
}
