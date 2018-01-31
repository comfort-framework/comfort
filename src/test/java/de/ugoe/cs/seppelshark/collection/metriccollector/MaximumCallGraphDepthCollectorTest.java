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
import de.ugoe.cs.seppelshark.data.graphs.CallEdge;
import de.ugoe.cs.seppelshark.data.graphs.CallGraph;
import de.ugoe.cs.seppelshark.data.graphs.CallType;
import de.ugoe.cs.seppelshark.filer.models.Result;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Fabian Trautsch
 */
public class MaximumCallGraphDepthCollectorTest extends BaseTest {
    private final String basePath = getPathToResource("metricCollectorTestData/ieeeAndistqb");

    private MaximumCallGraphDepthCollector maximumCallGraphDepthCollector;

    private GeneralConfiguration javaConfig = new GeneralConfiguration();
    private GeneralConfiguration pythonConfig = new GeneralConfiguration();

    private Set<Result> expectedResult = new HashSet<>();

    private CallGraph javaCallGraph = new CallGraph();

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
        javaCallGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, C1M1_p1, C2M1_p1));
        javaCallGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, C2M1_p1, C3M1));
        javaCallGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 1, C2M1_p1, C2Foo_p1));
        javaCallGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, C3M1, C2Foo_p1));
        javaCallGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, T2Test1, C2M1_p1));
        javaCallGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 1, T2Test1, C2Foo_p1));
        javaCallGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, T2Test2, C3M2));
        javaCallGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 1, C3M2, C4M1));
        javaCallGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, C4M1, C3M1));
        javaCallGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, T2M2, C3M1));
    }

    @Test
    public void getMaximumDepthForJavaOnMethodLevelTest() {
        javaConfig.setMethodLevel(true);
        maximumCallGraphDepthCollector = new MaximumCallGraphDepthCollector(javaConfig);
        result = maximumCallGraphDepthCollector.getMaximumDepthForJavaOnMethodLevel(javaCallGraph);

        expectedResult.add(new Result("org.foo.t1.Test1.test1", Paths.get("src/test/java/org/foo/t1/Test1.java"), "call_path","3"));
        expectedResult.add(new Result("org.foo.t2.Test2.test1", Paths.get("src/test/java/org/foo/t2/Test2.java"), "call_path","2"));
        expectedResult.add(new Result("org.foo.t2.Test2.test2", Paths.get("src/test/java/org/foo/t2/Test2.java"), "call_path","4"));

        assertEquals(expectedResult, result);
    }

    @Test
    public void getMaximumDepthForJavaOnClassLevelTest() {
        maximumCallGraphDepthCollector = new MaximumCallGraphDepthCollector(javaConfig);
        result = maximumCallGraphDepthCollector.getMaximumDepthForJavaOnClassLevel(javaCallGraph);

        expectedResult.add(new Result("org.foo.t1.Test1", Paths.get("src/test/java/org/foo/t1/Test1.java"), "call_path","3"));
        expectedResult.add(new Result("org.foo.t2.Test2", Paths.get("src/test/java/org/foo/t2/Test2.java"), "call_path","4"));

        assertEquals(expectedResult, result);
    }
}
