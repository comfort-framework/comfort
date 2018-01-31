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

import de.ugoe.cs.comfort.BaseTest;

/**
 * @author Fabian Trautsch
 */
public class CodeCoverStrategyTest extends BaseTest {

}

//public class CodeCoverStrategyTest extends BaseTest {
//    private GeneralConfiguration configuration = new GeneralConfiguration();
//
//    private final MethodNode T1M1 = new MethodNode("org.foo.t1.Test1", "m1", new Type[]{Type.INT});
//    private final MethodNode T1M2 = new MethodNode("org.foo.t1.Test1", "m2", new Type[]{});
//    private final MethodNode T3M1 = new MethodNode("org.foo.t3.Test3", "m1", new Type[]{});
//    private final MethodNode T4M1 = new MethodNode("org.foo.t4.Test4", "m1", new Type[]{});
//    private final MethodNode C1M1 = new MethodNode("org.foo.C1", "m1", new Type[]{});
//    private final MethodNode C1M2 = new MethodNode("org.foo.C1", "m2", new Type[]{});
//    private final MethodNode C1M3 = new MethodNode("org.foo.C1", "m3", new Type[]{});
//    private final MethodNode C2M1 = new MethodNode("org.foo.C2", "m1", new Type[]{});
//
//    private final ClassNode T1 = new ClassNode("Test1");
//    private final ClassNode T2 = new ClassNode("Test2");
//    private final ClassNode T3 = new ClassNode("Test3");
//    private final ClassNode C1 = new ClassNode("C1");
//    private final ClassNode C2 = new ClassNode("C2");
//    private final ClassNode C3 = new ClassNode("C3");
//
//    private final ModuleNode pyt1 = new ModuleNode("test1");
//    private final ModuleNode pyt2 = new ModuleNode("test2");
//    private final ModuleNode pyt3 = new ModuleNode("test3");
//    private final ModuleNode pyc1 = new ModuleNode("c1");
//    private final ModuleNode pyc2 = new ModuleNode("c2");
//    private final ModuleNode pyc3 = new ModuleNode("c3");
//
//    private final JavaTestedMethod covC1M1 = new JavaTestedMethod("org.foo.C1", "m1()", new ArrayList<>(), new ArrayList<>());
//    private final JavaTestedMethod covC1M2 = new JavaTestedMethod("org.foo.C1", "m2()", new ArrayList<>(), new ArrayList<>());
//    private final JavaTestedMethod covC1M3 = new JavaTestedMethod("org.foo.C1", "m3()", new ArrayList<>(), new ArrayList<>());
//    private final JavaTestedMethod covC2M1 = new JavaTestedMethod("org.foo.C2", "m1()", new ArrayList<>(), new ArrayList<>());
//
//    private final PythonCoveragerloaderTestedMethod pycovC1M1 = new PythonCoveragerloaderTestedMethod("testproject.module1:m1(F)", "testproject.module1", null, "m1", new ArrayList<>(), new ArrayList<>(), "dummy");
//    private final PythonCoveragerloaderTestedMethod pycovC1M2 = new PythonCoveragerloaderTestedMethod("testproject.module1:m2(F)", "testproject.module1", null, "m2", new ArrayList<>(), new ArrayList<>(), "dummy");
//    private final PythonCoveragerloaderTestedMethod pycovC1M3 = new PythonCoveragerloaderTestedMethod("testproject.module1:m3(F)", "testproject.module1", null, "m3", new ArrayList<>(), new ArrayList<>(), "dummy");
//    private final PythonCoveragerloaderTestedMethod pycovC2M1 = new PythonCoveragerloaderTestedMethod("testproject.module2:m1(F)", "testproject.module2", null, "m1", new ArrayList<>(), new ArrayList<>(), "dummy");
//
//    private final Map<Integer, Integer> expectedResultForGranularitiesFromPaper = new HashMap<>();
//
//    private final String granularitiesOutput = getPathToResource("metricCollctorTestData/codecover");
//    @Before
//    public void createConfiguration() {
//        configuration.getConfigurationDetectionStrategy().getConfigurationOptions().setCsvPath(granularitiesOutput);
//    }
//
//    @Before
//    public void fillExpectedResultsForGranuliaritiesFromPaper() {
//        expectedResultForGranularitiesFromPaper.put(1, 1);
//        expectedResultForGranularitiesFromPaper.put(2, 2);
//        expectedResultForGranularitiesFromPaper.put(3, 1);
//    }
//
//    @After
//    public void clearDirectory() throws IOException {
//        FileNameUtils.cleanDirectory(new File(granularitiesOutput));
//    }
//
//    @Test
//    public void codeCoverJavaTestFromPaperGranularitiesCSVTest() {
//        /*
//         * T1.m1 -> C1.m1
//         * T1.m2 -> C1.m1, C1.m3, C2.m1 (In paper T1.m2 is called "Test2")
//         * T3.m1 -> C1.m1, C1.m2
//         * T4.m1 -> C1.m2, C2.m1
//         */
//        Set<JavaTestedMethod> t1m1testedMethods = new HashSet<>();
//        t1m1testedMethods.add(covC1M1);
//        ITestMethod t1m1test = new JavaTestMethod("org.foo.t1.Test1", "m1", t1m1testedMethods);
//
//        Set<JavaTestedMethod> t1m2testedMethods = new HashSet<>();
//        t1m2testedMethods.add(covC1M1);
//        t1m2testedMethods.add(covC1M2);
//        t1m2testedMethods.add(covC2M1);
//        ITestMethod t1m2test = new JavaTestMethod("org.foo.t1.Test1", "m2", t1m2testedMethods);
//
//        Set<JavaTestedMethod> t3m1testedMethods = new HashSet<>();
//        t3m1testedMethods.add(covC1M1);
//        t3m1testedMethods.add(covC1M2);
//        ITestMethod t3m1test = new JavaTestMethod("org.foo.t3.Test3", "m1", t3m1testedMethods);
//
//        Set<JavaTestedMethod> t4m1testedMethods = new HashSet<>();
//        t4m1testedMethods.add(covC1M2);
//        t4m1testedMethods.add(covC2M1);
//        ITestMethod t4m1test = new JavaTestMethod("org.foo.t4.Test4", "m1", t4m1testedMethods);
//
//
//        CoverageData covData = new CoverageData();
//        covData.add(t1m1test);
//        covData.add(t1m2test);
//        covData.add(t3m1test);
//        covData.add(t4m1test);
//
//        CodeCoverStrategy strategy = new CodeCoverStrategy();
//        try {
//            strategy.classify(configuration, covData);
//            testResultsAgainstExpectedOutput(expectedResultForGranularitiesFromPaper);
//        } catch (MetricCollectorException e) {
//            fail("Unexpected exception! "+e);
//        }
//
//    }
//
//    @Test
//    public void codeCoverPythonTestFromPaperGranularitiesCSVTest() {
//        /*
//         * same as above, but with python test methods
//         */
//        Set<PythonCoveragerloaderTestedMethod> t1m1testedMethods = new HashSet<>();
//        t1m1testedMethods.add(pycovC1M1);
//        ITestMethod t1m1test = new PythonCoverageLoaderTestMethod("tests.test_module1:Module1Test.m1", "tests.test_module1", "Module1Test" , "m1", t1m1testedMethods);
//
//        Set<PythonCoveragerloaderTestedMethod> t1m2testedMethods = new HashSet<>();
//        t1m2testedMethods.add(pycovC1M1);
//        t1m2testedMethods.add(pycovC1M2);
//        t1m2testedMethods.add(pycovC2M1);
//        ITestMethod t1m2test = new PythonCoverageLoaderTestMethod("tests.test_module1:Module1Test.m2", "tests.test_module1", "Module1Test" , "m2", t1m2testedMethods);
//
//
//        Set<PythonCoveragerloaderTestedMethod> t3m1testedMethods = new HashSet<>();
//        t3m1testedMethods.add(pycovC1M1);
//        t3m1testedMethods.add(pycovC1M2);
//        ITestMethod t3m1test = new PythonCoverageLoaderTestMethod("tests.test_module3:Module3Test.m1", "tests.test_module3", "Module3Test" , "m1", t3m1testedMethods);
//
//        Set<PythonCoveragerloaderTestedMethod> t4m1testedMethods = new HashSet<>();
//        t4m1testedMethods.add(pycovC1M2);
//        t4m1testedMethods.add(pycovC2M1);
//        ITestMethod t4m1test = new PythonCoverageLoaderTestMethod("tests.test_module4:Module4Test.m1", "tests.test_module4", "Module4Test" , "m1", t4m1testedMethods);
//
//        CoverageData covData = new CoverageData();
//        covData.add(t1m1test);
//        covData.add(t1m2test);
//        covData.add(t3m1test);
//        covData.add(t4m1test);
//
//        CodeCoverStrategy strategy = new CodeCoverStrategy();
//        try {
//            strategy.classify(configuration, covData);
//            testResultsAgainstExpectedOutput(expectedResultForGranularitiesFromPaper);
//        } catch (MetricCollectorException e) {
//            fail("Unexpected exception! "+e);
//        }
//    }
//
//
//    @Test
//    public void callGraphTestFromPaperGranularitiesCSVTest() {
//        /*
//         * Graph:
//         * T1.m1 -> C1.m1
//         * T1.m2 -> C1.m1
//         * T1.m2 -> C1.m3
//         * T1.m2 -> C2.m1
//         * T3.m1 -> C1.m1
//         * T3.m1 -> C1.m2
//         * T4.m1 -> C1.m2
//         * T4.m1 -> C2.m1
//         *
//         * Adaptions from paper example: In the paper the figure only shows "Test1", "Test2", and so on, but we have
//         * also the test calls on method level. Therefore, we have Test1 = Test1.m1, Test2 = Test1.m2, Test3 = Test3.m1
//         * Test4 = Test4.m1.
//         */
//        CallGraph callGraph = new CallGraph();
//        callGraph.addEdge(new CallEdge(CallType.INVOKE_VIRTUAL, 0, T1M1, C1M1));
//        callGraph.addEdge(new CallEdge(CallType.INVOKE_VIRTUAL, 0, T1M2, C1M1));
//        callGraph.addEdge(new CallEdge(CallType.INVOKE_VIRTUAL, 1, T1M2, C1M3));
//        callGraph.addEdge(new CallEdge(CallType.INVOKE_VIRTUAL, 2, T1M2, C2M1));
//        callGraph.addEdge(new CallEdge(CallType.INVOKE_VIRTUAL, 0, T3M1, C1M1));
//        callGraph.addEdge(new CallEdge(CallType.INVOKE_VIRTUAL, 1, T3M1, C1M2));
//        callGraph.addEdge(new CallEdge(CallType.INVOKE_VIRTUAL, 0, T4M1, C1M2));
//        callGraph.addEdge(new CallEdge(CallType.INVOKE_VIRTUAL, 0, T4M1, C2M1));
//
//        CodeCoverStrategy strategy = new CodeCoverStrategy();
//        try {
//            strategy.classify(configuration, callGraph);
//            testResultsAgainstExpectedOutput(expectedResultForGranularitiesFromPaper);
//        } catch (MetricCollectorException e) {
//            fail("Unexpected exception! "+e);
//        }
//    }
//
//    @Test
//    public void dependencyGraphTestGranularitiesCSVTest() {
//        DependencyGraph dependencyGraph = new DependencyGraph();
//        dependencyGraph.putEdge(T1, C1);
//        dependencyGraph.putEdge(T1, C3);
//        dependencyGraph.putEdge(T2, C3);
//        dependencyGraph.putEdge(T2, C2);
//        dependencyGraph.putEdge(T3, C3);
//        dependencyGraph.putEdge(C1, C2);
//        dependencyGraph.putEdge(C2, C1);
//
//
//        Map<Integer, Integer> expectedResult = new HashMap<>();
//        expectedResult.put(1, 1);
//        expectedResult.put(3, 2);
//
//        CodeCoverStrategy strategy = new CodeCoverStrategy();
//        try {
//            strategy.classify(configuration, dependencyGraph);
//            testResultsAgainstExpectedOutput(expectedResult);
//        } catch (MetricCollectorException e) {
//            fail("Unexpected exception! "+e);
//        }
//    }
//
//    @Test
//    public void pythonDependencyGraphTestGranularitiesCSVTest() {
//        DependencyGraph dependencyGraph = new DependencyGraph();
//        dependencyGraph.putEdge(pyt1, pyc1);
//        dependencyGraph.putEdge(pyt1, pyc3);
//        dependencyGraph.putEdge(pyt2, pyc3);
//        dependencyGraph.putEdge(pyt2, pyc2);
//        dependencyGraph.putEdge(pyt3, pyc3);
//        dependencyGraph.putEdge(pyc1, pyc2);
//        dependencyGraph.putEdge(pyc2, pyc1);
//
//
//        Map<Integer, Integer> expectedResult = new HashMap<>();
//        expectedResult.put(1, 1);
//        expectedResult.put(3, 2);
//
//        CodeCoverStrategy strategy = new CodeCoverStrategy();
//        try {
//            strategy.classify(configuration, dependencyGraph);
//            testResultsAgainstExpectedOutput(expectedResult);
//        } catch (MetricCollectorException e) {
//            fail("Unexpected exception! "+e);
//        }
//    }
//
//    private void testResultsAgainstExpectedOutput(Map<Integer, Integer> expectedResult) {
//        Path createdCSVFile;
//        try {
//            // Get stored csv file
//            createdCSVFile = getFileInDirectory(Paths.get(granularitiesOutput));
//
//            // Check contents of file
//            String content = new String(Files.readAllBytes(createdCSVFile), ("UTF-8"));
//            StringBuilder expectedContent = new StringBuilder();
//            for(Map.Entry<Integer, Integer> entry: expectedResult.entrySet()) {
//                expectedContent.append(entry.getKey()).append(",").append(entry.getValue()).append("\n");
//            }
//
//            assertEquals("Contents not the same!", expectedContent.toString().trim(), content);
//        } catch (IOException e) {
//            fail("Unexpected exception! "+e);
//        }
//    }
//
//    private Path getFileInDirectory(Path directory) throws IOException {
//        Set<Path> pathsSet = new HashSet<>();
//        Files.list(directory).filter(file -> Files.isRegularFile(file) && file.toString().endsWith(".csv")).forEach(pathsSet::add);
//        return pathsSet.iterator().next();
//    }
//
//}
