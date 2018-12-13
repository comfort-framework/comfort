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

import de.ugoe.cs.comfort.configuration.GeneralConfiguration;
import de.ugoe.cs.comfort.data.graphs.CallEdge;
import de.ugoe.cs.comfort.data.graphs.CallGraph;
import de.ugoe.cs.comfort.data.graphs.CallType;
import de.ugoe.cs.comfort.filer.models.Result;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Fabian Trautsch
 */
public class DirectnessCollectorTest extends BaseMetricCollectorTest {
    private final String basePath = getPathToResource("metricCollectorTestData/ieeeAndistqb");

    private DirectnessCollector directnessCollector;

    private GeneralConfiguration javaConfig = new GeneralConfiguration();
    private GeneralConfiguration pythonConfig = new GeneralConfiguration();

    private Set<Result> expectedResult = new HashSet<>();

    private CallGraph javaCallGraph = new CallGraph();
    private CallGraph pythonCallGraph = new CallGraph();

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
         * org.foo.t1.Test1:test1 -> org.foo.bar.C1:m1
         * org.foo.bar.C1:m1 -> org.foo.bar.C2:m1
         * org.foo.bar.C1:m1 -> org.foo.bar.C3:m1
         * org.foo.t2.Test2:test1 -> org.foo.bar.C1:m1
         * org.foo.t2.Test2:test1 -> org.foo.bar.C3:m1
         * org.foo.t2.Test2:test1 -> org.foo.bar.C3:m2
         * org.foo.t2.Test2:m1 -> org.foo.bar.C1:m1
         */
        javaCallGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, T1Test1, C1M1_p1));
        javaCallGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, C1M1_p1, C2M1_p1));
        javaCallGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 1, C1M1_p1, C3M1));
        javaCallGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, T2Test1, C1M1_p1));
        javaCallGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 1, T2Test1, C3M1));
        javaCallGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 1, T2Test1, C3M2));
        javaCallGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, T2M1, C1M1_p1));
    }

    @Before
    public void createPythonData() {
        pythonCallGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, pyTestDemoInit, pyModule1Init));
        pythonCallGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, pyModule1Init, pyModule3Init));
        pythonCallGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 1, pyModule1Init, pyModule2Init));
        pythonCallGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, pyTest1Test, pyModule1Init));
        pythonCallGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 1, pyTest1Test, pyModule2Init));
        pythonCallGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 1, pyTest1Test, pyModule2Foo));
        pythonCallGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, pyTest1Test2, pyModule1Init));
    }

    @Test
    public void createDirectnessMetricForJavaOnMethodLevelTest() throws IOException {
        javaConfig.setMethodLevel(true);
        directnessCollector = new DirectnessCollector(javaConfig, filerMock);
        directnessCollector.createDirectnessMetricForJavaOnMethodLevel(javaCallGraph);

        expectedResult.add(new Result("org.foo.t1.Test1.test1", Paths.get("src/main/java/org/foo/t1/Test1.java"), "call_dire","33.3333"));
        expectedResult.add(new Result("org.foo.t2.Test2.test1", Paths.get("src/main/java/org/foo/t2/Test2.java"), "call_dire","66.6667"));

        assertEquals(expectedResult, filerMock.getResults().getResults());

    }

    @Test
    public void createDirectnessMetricForJavaOnClassLevelTest() throws IOException {
        directnessCollector = new DirectnessCollector(javaConfig, filerMock);
        directnessCollector.createDirectnessMetricForJavaOnClassLevel(javaCallGraph);

        expectedResult.add(new Result("org.foo.t1.Test1", Paths.get("src/main/java/org/foo/t1/Test1.java"), "call_dire","33.3333"));
        expectedResult.add(new Result("org.foo.t2.Test2", Paths.get("src/main/java/org/foo/t2/Test2.java"), "call_dire","66.6667"));

        assertEquals(expectedResult, filerMock.getResults().getResults());
    }

    @Test
    public void createDirectnessMetricForPythonOnMethodLevelTest() throws IOException {
        pythonConfig.setMethodLevel(true);
        directnessCollector = new DirectnessCollector(pythonConfig, filerMock);
        directnessCollector.createDirectnessMetricForJavaOnMethodLevel(pythonCallGraph);

        expectedResult.add(new Result("tests.test_module1:Module1Test.test", Paths.get("tests/test_module1.py"), "call_dire","66.6667"));
        expectedResult.add(new Result("tests.test_module1:Module1Test.test2", Paths.get("tests/test_module1.py"), "call_dire","33.3333"));

        assertEquals(expectedResult, filerMock.getResults().getResults());
    }

    @Test
    public void createDirectnessMetricForPythonOnClassLevelTest() throws IOException {
        directnessCollector = new DirectnessCollector(pythonConfig, filerMock);
        directnessCollector.createDirectnessMetricForJavaOnClassLevel(pythonCallGraph);

        expectedResult.add(new Result("tests.test_module1", Paths.get("tests/test_module1.py"), "call_dire","66.6667"));
        expectedResult.add(new Result("package1.package2.test_demo", Paths.get("package1/package2/test_demo.py"), "call_dire","33.3333"));

        assertEquals(expectedResult, filerMock.getResults().getResults());
    }


}
