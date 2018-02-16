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
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Fabian Trautsch
 */
public class DependencyCollectorTest extends BaseMetricCollectorTest {
    private final String basePath = getPathToResource("metricCollectorTestData/ieeeAndistqb");

    private DependencyCollector dependencyCollector;

    private GeneralConfiguration javaConfig = new GeneralConfiguration();
    private GeneralConfiguration pythonConfig = new GeneralConfiguration();

    private Set<Result> expectedResult = new HashSet<>();

    private CallGraph javaCallGraph = new CallGraph();
    private DependencyGraph javaDependencyGraph = new DependencyGraph();

    private Set<Result> result;


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
    public void createJavaData() {
        /*
         * Graph:
         * org.foo.t1.Test1:test1 -> org.foo.C1:<init>
         * org.foo.C1:<init> -> org.foo.C2:<init>
         * org.foo.C2:<init> -> org.foo.C3:<init>
         * org.foo.C2:<init> -> org.foo.C2:foo
         * org.foo.C3:<init> -> org.foo.C2:foo
         * org.foo.t2.Test2:test1 -> org.foo.C2:<init>
         * org.foo.t2.Test2:test1 -> org.foo.C2:foo
         * org.foo.t2.Test2:test2 -> org.foo.C3:foo
         * org.foo.t2.Test2:C3:foo -> org.foo.C4:<init>
         * org.foo.C4:<init> -> org.foo.C3:<init>
         */

        javaCallGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, T1Test1, C1M1_p1));
        javaCallGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 1, T1Test1, T2Test1));
        javaCallGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, C1M1_p1, C2M1_p1));
        javaCallGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, C2M1_p1, C3M1));
        javaCallGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 1, C2M1_p1, C2Foo_p1));
        javaCallGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, C3M1, C2Foo_p1));
        javaCallGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, T2Test1, C2M1_p1));
        javaCallGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 1, T2Test1, C2Foo_p1));
        javaCallGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, T2Test2, C3M2));
        javaCallGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 1, C3M2, C4M1));
        javaCallGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, C4M1, C3M1));

        /*
         * DependencyGraph:
         * T1 -> C1
         * C1 -> C2
         * C2 -> C3
         * C3 -> C1
         * T2 -> C2
         * T2 -> C3
         * T3 -> C4
         * C4 -> C3
         */
        javaDependencyGraph.putEdge(T1, C1);
        javaDependencyGraph.putEdge(C1, C2);
        javaDependencyGraph.putEdge(C2, C3);
        javaDependencyGraph.putEdge(C3, C1);
        javaDependencyGraph.putEdge(T2, C2);
        javaDependencyGraph.putEdge(T2, C3);
        javaDependencyGraph.putEdge(T3, C4);
        javaDependencyGraph.putEdge(T3, T1);
        javaDependencyGraph.putEdge(C4, C3);
    }

    @Test
    public void getDependencyForJavaOnMethodLevelTest() throws IOException {
        javaConfig.setMethodLevel(true);
        dependencyCollector = new DependencyCollector(javaConfig, filerMock);
        dependencyCollector.getNumberOfDependentUnitsForMethod(javaCallGraph);

        result = filerMock.getResults().getResults();

        expectedResult.add(new Result("org.foo.t1.Test1.test1", null, "call_dep","3"));
        expectedResult.add(new Result("org.foo.t2.Test2.test1", null, "call_dep","2"));
        expectedResult.add(new Result("org.foo.t2.Test2.test2", null, "call_dep","3"));

        assertEquals(expectedResult, result);
    }

    @Test
    public void getDependencyForJavaOnClassLevelTest() throws IOException {
        dependencyCollector = new DependencyCollector(javaConfig, filerMock);
        dependencyCollector.getNumberOfDependentUnitsForClass(javaDependencyGraph);

        result = filerMock.getResults().getResults();

        expectedResult.add(new Result("org.foo.Test1", null, "call_dep","3"));
        expectedResult.add(new Result("org.foo.Test2", null, "call_dep","3"));
        expectedResult.add(new Result("org.foo.Test3", null, "call_dep","4"));

        assertEquals(expectedResult, result);
    }

    @Test
    public void getDependencyForJavaUsingCoverageData() throws IOException{
        javaConfig.setMethodLevel(true);
        Set<IUnit> testedMethodsOfTest1 = new HashSet<>();
        testedMethodsOfTest1.add(covP1C1M1);

        Set<IUnit> testedMethodsOfTest2 = new HashSet<>();
        testedMethodsOfTest2.add(covP1C1M1);
        testedMethodsOfTest2.add(covP1C2M1);

        CoverageData covData = new CoverageData();
        covData.add(T1M1, testedMethodsOfTest1);
        covData.add(T2M1, testedMethodsOfTest2);
        covData.add(T1Test1, new HashSet<>());

        dependencyCollector = new DependencyCollector(javaConfig, filerMock);
        dependencyCollector.getNumberOfDependentUnitsForCoverageData(covData);

        result = filerMock.getResults().getResults();

        expectedResult.add(new Result("org.foo.t1.Test1.m1", null, "cov_dep", "1"));
        expectedResult.add(new Result("org.foo.t2.Test2.m1", null, "cov_dep", "2"));
        expectedResult.add(new Result("org.foo.t1.Test1.test1", null, "cov_dep", "0"));

        assertEquals(expectedResult, result);
    }
}
