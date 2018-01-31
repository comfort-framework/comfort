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

package de.ugoe.cs.seppelshark.collection.metriccollector;

import static org.junit.Assert.assertEquals;

import de.ugoe.cs.seppelshark.BaseTest;
import de.ugoe.cs.seppelshark.configuration.GeneralConfiguration;
import de.ugoe.cs.seppelshark.data.CoverageData;
import de.ugoe.cs.seppelshark.data.graphs.CallEdge;
import de.ugoe.cs.seppelshark.data.graphs.CallGraph;
import de.ugoe.cs.seppelshark.data.graphs.CallType;
import de.ugoe.cs.seppelshark.data.graphs.DependencyGraph;
import de.ugoe.cs.seppelshark.data.models.IUnit;
import de.ugoe.cs.seppelshark.filer.models.Result;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Fabian Trautsch
 */
public class ISTQBTestTypeCollectorTest extends BaseTest{
    private final String basePath = getPathToResource("metricCollectorTestData/ieeeAndistqb");

    private ISTQBTestTypeCollector istqbTestTypeCollector;

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
    public void classifyPythonDependencyGraphTest() {
        DependencyGraph dependencyGraph = new DependencyGraph();
        dependencyGraph.putEdge(pyTest1, module1);
        dependencyGraph.putEdge(pyTest2, module1);
        dependencyGraph.putEdge(pyTest2, module2);
        dependencyGraph.putEdge(pyTest3, moduleInit);
        dependencyGraph.putEdge(moduleInit, module1);

        istqbTestTypeCollector = new ISTQBTestTypeCollector(pythonConfig);
        result = istqbTestTypeCollector.createResultsJavaPythonClassDepGraph(dependencyGraph);
        expectedResult.add(new Result("tests.test1", Paths.get("tests/test1.py"), "dep_istqb", TestType.UNIT.name()));
        expectedResult.add(new Result("tests.test2", Paths.get("tests/test2.py"), "dep_istqb", TestType.INTEGRATION.name()));
        expectedResult.add(new Result("tests.test3", Paths.get("tests/test3.py"), "dep_istqb", TestType.INTEGRATION.name()));
        assertEquals("Result set is not correct!", expectedResult, result);
    }


    @Test
    public void classifyDependencyGraphTest() {
        /*
         * Graph:
         * org.foo.t1.T1 -> org.foo.C2
         * org.foo.t2.T2 -> org.foo.C2
         * org.foo.t2.T2 -> org.foo.C3
         * org.foo.T3 -> org.foo.C1
         * org.foo.C1 -> org.foo.C2
         *
         * Result: T1 = unit test, T2 & T3 = integration test (T3, because we need to recursively look at dependencies)
         */
        DependencyGraph dependencyGraph = new DependencyGraph();
        dependencyGraph.putEdge(addressTest, C2);
        dependencyGraph.putEdge(blatestbla, C2);
        dependencyGraph.putEdge(blatestbla, C3);
        dependencyGraph.putEdge(fooTest, C1);
        dependencyGraph.putEdge(C1, C2);

        istqbTestTypeCollector = new ISTQBTestTypeCollector(javaConfig);
        result = istqbTestTypeCollector.createResultsJavaPythonClassDepGraph(dependencyGraph);
        expectedResult.add(new Result("org.foo.models.AddressTest", Paths.get("src/test/java/org/foo/models/AddressTest.java"), "dep_istqb",TestType.UNIT.name()));
        expectedResult.add(new Result("org.foo.view.blatestbla", Paths.get("src/test/java/org/foo/view/blatestbla.java"), "dep_istqb",TestType.INTEGRATION.name()));
        expectedResult.add(new Result("unit.fooTest", Paths.get("src/test/java/unit/fooTest.java"), "dep_istqb",TestType.INTEGRATION.name()));
        assertEquals("Result set is not correct!", expectedResult, result);
    }

    @Test
    public void classifyJavaCallGraphClassLevelTest() {
        /*
         * Graph:
         * org.foo.t1.Test1:<init> -> org.foo.C2:<init>
         * org.foo.t1.Test1:testC2 -> org.foo.C2:<init>
         * org.foo.t2.Test2:<init> -> org.foo.C2:<init>
         * org.foo.t2.Test2:testC3 -> org.foo.C3:<init>
         * org.foo.Test3:<init> -> org.foo.C1:<init>
         * org.foo.C1:<init> -> org.foo.C4:<init>
         *
         * Result: T1 = unit test, T2, T3  = integration test
         */


        CallGraph callGraph = new CallGraph();
        callGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, T1M1, C2M1_p1));
        callGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, T1Test1, C2M1_p1));
        callGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, T2M1, C2M1_p1));
        callGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, T2Test1, C3M1));
        callGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, T3Test1, C1M1_p1));
        callGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, C1M1_p1, C4M1));

        istqbTestTypeCollector = new ISTQBTestTypeCollector(javaConfig);
        result = istqbTestTypeCollector.createResultsJavaClassLevelCallGraph(callGraph);
        expectedResult.add(new Result("org.foo.t1.Test1", null, "call_istqb",TestType.UNIT.name()));
        expectedResult.add(new Result("org.foo.t2.Test2", null, "call_istqb",TestType.INTEGRATION.name()));
        expectedResult.add(new Result("org.foo.t3.Test3", null, "call_istqb",TestType.INTEGRATION.name()));

        assertEquals("Result set is not correct!", expectedResult, result);
    }

    @Test
    public void classifyCallGraphMethodLevelTest() {
        /*
         * Graph:
         * org.foo.t1.Test1:<init> -> org.foo.C2:<init>
         * org.foo.t1.Test1:testC2 -> org.foo.C2:<init>
         * org.foo.t2.Test2:<init> -> org.foo.C2:<init>
         * org.foo.t2.Test2:testC3 -> org.foo.C3:<init>
         * org.foo.Test3:<init> -> org.foo.C1:<init>
         * org.foo.C1:<init> -> org.foo.C4:<init>
         *
         */


        CallGraph callGraph = new CallGraph();
        callGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, T1M1, C2M1_p1));
        callGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, T1Test1, C2M1_p1));
        callGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, T2M1, C2M1_p1));
        callGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, T2M1, C3M1));
        callGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, T3Test1, C1M1_p1));
        callGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, C1M1_p1, C4M1));

        javaConfig.setMethodLevel(true);
        istqbTestTypeCollector = new ISTQBTestTypeCollector(javaConfig);
        result = istqbTestTypeCollector.createResultsJavaMethodLevelCallGraph(callGraph);
        expectedResult.add(new Result("org.foo.t1.Test1.m1", null, "call_istqb_met",TestType.UNIT.name()));
        expectedResult.add(new Result("org.foo.t1.Test1.test1", null, "call_istqb_met",TestType.UNIT.name()));
        expectedResult.add(new Result("org.foo.t2.Test2.m1", null, "call_istqb_met",TestType.INTEGRATION.name()));
        expectedResult.add(new Result("org.foo.t3.Test3.test1", null, "call_istqb_met",TestType.INTEGRATION.name()));

        assertEquals("Result set is not correct!", expectedResult, result);
    }

    @Test
    public void classifyCodeCoveragePythonTest() {
        Set<IUnit> testedMethodsOfTest1 = new HashSet<>();
        testedMethodsOfTest1.add(pyModule2Init);
        testedMethodsOfTest1.add(pyModule2Foo);

        Set<IUnit> testedMethodsOfTest2 = new HashSet<>();
        testedMethodsOfTest2.add(pyModule1Init);
        testedMethodsOfTest2.add(pyModule3Init);

        CoverageData covData = new CoverageData();
        covData.add(pyTest1Test, testedMethodsOfTest1);
        covData.add(pyTest2Test, testedMethodsOfTest2);

        istqbTestTypeCollector = new ISTQBTestTypeCollector(pythonConfig);
        result = istqbTestTypeCollector.createResultsJavaPythonClassCoverage(covData);
        expectedResult.add(new Result("tests.test_module1", null, "cov_istqb", TestType.UNIT.name()));
        expectedResult.add(new Result("tests.test_module2", null, "cov_istqb", TestType.INTEGRATION.name()));
        assertEquals("Result set is not correct!", expectedResult, result);
    }

    @Test
    public void classifyCodeCoverageJavaMethodLevelTest() {
        Set<IUnit> testedMethodsOfTest1 = new HashSet<>();
        testedMethodsOfTest1.add(covP1C1M1);
        testedMethodsOfTest1.add(covP1C1M2);

        Set<IUnit> testedMethodsOfTest2 = new HashSet<>();
        testedMethodsOfTest2.add(covP1C1M1);
        testedMethodsOfTest2.add(covP1C2M1);

        CoverageData covData = new CoverageData();
        covData.add(T1M1, testedMethodsOfTest1);
        covData.add(T2M1, testedMethodsOfTest2);
        covData.add(T1Test1, testedMethodsOfTest2);


        istqbTestTypeCollector = new ISTQBTestTypeCollector(javaConfig);
        result = istqbTestTypeCollector.createResultsJavaPythonMethodCoverage(covData);
        expectedResult.add(new Result("org.foo.t1.Test1.m1", null, "cov_istqb_met", TestType.UNIT.name()));
        expectedResult.add(new Result("org.foo.t2.Test2.m1", null, "cov_istqb_met", TestType.INTEGRATION.name()));
        expectedResult.add(new Result("org.foo.t1.Test1.test1", null,"cov_istqb_met",  TestType.INTEGRATION.name()));
        assertEquals("Result set is not correct!", expectedResult, result);
    }

    @Test
    public void classifyCodeCoverageJavaClassLevelTest() {
        Set<IUnit> testedMethodsOfTest1 = new HashSet<>();
        testedMethodsOfTest1.add(covP1C1M1);
        testedMethodsOfTest1.add(covP1C1M2);

        Set<IUnit> testedMethodsOfTest2 = new HashSet<>();
        testedMethodsOfTest2.add(covP1C1M1);
        testedMethodsOfTest2.add(covP1C2M1);

        CoverageData covData = new CoverageData();
        covData.add(T1Test1, testedMethodsOfTest1);
        covData.add(T2M1, testedMethodsOfTest2);

        istqbTestTypeCollector = new ISTQBTestTypeCollector(javaConfig);
        result = istqbTestTypeCollector.createResultsJavaPythonClassCoverage(covData);
        expectedResult.add(new Result("org.foo.t1.Test1", null, "cov_istqb",TestType.UNIT.name()));
        expectedResult.add(new Result("org.foo.t2.Test2", null, "cov_istqb", TestType.INTEGRATION.name()));
        assertEquals("Result set is not correct!", expectedResult, result);
    }

    @Test
    public void classifyCodeCoverageJavaClassLevelSameClassDifferentMethodsTest() {
        Set<IUnit> testedMethodsOfTest1 = new HashSet<>();
        testedMethodsOfTest1.add(covP1C1M1);
        testedMethodsOfTest1.add(covP1C1M2);

        Set<IUnit> testedMethodsOfTest2 = new HashSet<>();
        testedMethodsOfTest2.add(covP1C1M1);
        testedMethodsOfTest2.add(covP1C2M1);

        CoverageData covData = new CoverageData();
        covData.add(T1Test1, testedMethodsOfTest1);
        covData.add(T1M1, testedMethodsOfTest2);

        istqbTestTypeCollector = new ISTQBTestTypeCollector(javaConfig);
        result = istqbTestTypeCollector.createResultsJavaPythonClassCoverage(covData);
        expectedResult.add(new Result("org.foo.t1.Test1", null, "cov_istqb",TestType.INTEGRATION.name()));
        assertEquals("Result set is not correct!", expectedResult, result);
    }

}
