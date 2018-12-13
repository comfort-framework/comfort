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
import static org.junit.Assert.fail;

import de.ugoe.cs.comfort.configuration.GeneralConfiguration;
import de.ugoe.cs.comfort.data.ClassFiles;
import de.ugoe.cs.comfort.data.graphs.CallEdge;
import de.ugoe.cs.comfort.data.graphs.CallGraph;
import de.ugoe.cs.comfort.data.graphs.CallType;
import de.ugoe.cs.comfort.exception.MetricCollectorException;
import de.ugoe.cs.comfort.filer.models.Result;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Fabian Trautsch
 */
public class NumAssertionCollectorTest extends BaseMetricCollectorTest {
    private final String basePath = getPathToResource("metricCollectorTestData/numassertcollector");

    private NumAssertionCollector numAssertionCollector;

    private GeneralConfiguration javaConfig = new GeneralConfiguration();
    private GeneralConfiguration pythonConfig = new GeneralConfiguration();

    private Set<Result> expectedResult = new HashSet<>();

    private HashSet<Path> testFiles = new HashSet<>();
    private CallGraph javaCallGraph = new CallGraph();


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
    public void createData() {
        testFiles.add(Paths.get(basePath, "java", "target/test-classes/org/foo/models/AddressTest.class"));
        testFiles.add(Paths.get(basePath, "java", "target/test-classes/org/foo/models/PersonTest.class"));

        javaCallGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, T1Test1, C3M1));
        javaCallGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 1, T1Test1, junitAssertEquals));
        javaCallGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 2, T1Test1, junitAssertEquals));
        javaCallGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 3, T1Test1, junitAssertEquals));
        javaCallGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, T1M1, junitAssertEquals));
        javaCallGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, T2Test1, C3M2));
        javaCallGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 1, T2Test1, junitAssertNotNull));
        javaCallGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 2, T2Test1, junitAssertEquals));
        javaCallGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, T2Test2, C1M1_p1));
        javaCallGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 1, T2Test2, junitAssertEquals));
    }

    @Test
    public void getNumberOfAssertionsForJavaOnMethodLevelTest() {
        try {
            javaConfig.setMethodLevel(true);
            numAssertionCollector = new NumAssertionCollector(javaConfig, filerMock);
            ClassFiles classFiles = new ClassFiles(javaConfig, testFiles, null);
            numAssertionCollector.getNumberOfAssertionsForJava(classFiles);

            expectedResult.add(new Result("org.foo.models.AddressTest.getAddressTest", Paths.get("src/test/java/org/foo/models/AddressTest.java"), "num_asserts", "1"));
            expectedResult.add(new Result("org.foo.models.AddressTest.<init>", Paths.get("src/test/java/org/foo/models/AddressTest.java"), "num_asserts", "0"));
            expectedResult.add(new Result("org.foo.models.AddressTest.getAddressTest2", Paths.get("src/test/java/org/foo/models/AddressTest.java"), "num_asserts", "1"));
            expectedResult.add(new Result("org.foo.models.PersonTest.getPersonTest1", Paths.get("src/test/java/org/foo/models/PersonTest.java"), "num_asserts", "3"));
            expectedResult.add(new Result("org.foo.models.PersonTest.<init>", Paths.get("src/test/java/org/foo/models/PersonTest.java"), "num_asserts", "0"));
            assertEquals("Result set is not correct!", expectedResult, filerMock.getResults().getResults());
        } catch (MetricCollectorException e) {
            fail("Unexpected exception: "+e);
        }
    }

    @Test
    public void getNumberOfAssertionsForJavaOnClassLevelTest() {
        try {
            numAssertionCollector = new NumAssertionCollector(javaConfig, filerMock);
            ClassFiles classFiles = new ClassFiles(javaConfig, testFiles, null);
            numAssertionCollector.getNumberOfAssertionsForJava(classFiles);

            expectedResult.add(new Result("org.foo.models.AddressTest", Paths.get("src/test/java/org/foo/models/AddressTest.java"), "num_asserts", "2"));
            expectedResult.add(new Result("org.foo.models.PersonTest", Paths.get("src/test/java/org/foo/models/PersonTest.java"), "num_asserts", "3"));
            assertEquals("Result set is not correct!", expectedResult, filerMock.getResults().getResults());
        } catch (MetricCollectorException e) {
            fail("Unexpected exception: "+e);
        }
    }

    @Test
    public void getNumberOfAssertionsForCallGraphClassLevelTest() throws IOException {
        numAssertionCollector = new NumAssertionCollector(javaConfig, filerMock);
        numAssertionCollector.getNumberOfAssertionsForCallGraphOnClassLevel(javaCallGraph);

        expectedResult.add(new Result("org.foo.t1.Test1", Paths.get("src/main/java/org/foo/t1/Test1.java"), "num_asserts", "3"));
        expectedResult.add(new Result("org.foo.t2.Test2", Paths.get("src/main/java/org/foo/t2/Test2.java"), "num_asserts", "3"));
        assertEquals("Result set is not correct!", expectedResult, filerMock.getResults().getResults());
    }

    @Test
    public void getNumberOfAssertionsForCallGraphMethodLevelTest() throws IOException {
        javaConfig.setMethodLevel(true);
        numAssertionCollector = new NumAssertionCollector(javaConfig, filerMock);
        numAssertionCollector.getNumberOfAssertionsForCallGraph(javaCallGraph);

        expectedResult.add(new Result("org.foo.t1.Test1.test1", Paths.get("src/main/java/org/foo/t1/Test1.java"), "num_asserts", "3"));
        expectedResult.add(new Result("org.foo.t2.Test2.test1", Paths.get("src/main/java/org/foo/t2/Test2.java"), "num_asserts", "2"));
        expectedResult.add(new Result("org.foo.t2.Test2.test2", Paths.get("src/main/java/org/foo/t2/Test2.java"), "num_asserts", "1"));
        assertEquals("Result set is not correct!", expectedResult, filerMock.getResults().getResults());
    }
}
