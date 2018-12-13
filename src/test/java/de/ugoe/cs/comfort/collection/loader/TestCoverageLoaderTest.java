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

package de.ugoe.cs.comfort.collection.loader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import de.ugoe.cs.comfort.BaseTest;
import de.ugoe.cs.comfort.configuration.GeneralConfiguration;
import de.ugoe.cs.comfort.configuration.LoaderConfiguration;
import de.ugoe.cs.comfort.data.CoverageData;
import de.ugoe.cs.comfort.data.models.IUnit;
import de.ugoe.cs.comfort.data.models.JavaClass;
import de.ugoe.cs.comfort.data.models.PythonMethod;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Fabian Trautsch
 */
public class TestCoverageLoaderTest extends BaseTest {
    private GeneralConfiguration javaConfiguration = new GeneralConfiguration();
    private GeneralConfiguration pythonConfiguration = new GeneralConfiguration();

    private LoaderConfiguration loaderJavaConfiguration = new LoaderConfiguration("TestCoverage");
    private LoaderConfiguration loaderPythonConfiguration = new LoaderConfiguration("TestCoverage");

    private TestCoverageLoader testCoverageLoader;

    @Before
    public void createConfiguration() {
        loaderJavaConfiguration.setCoverageLocation(getPathToResource("loaderTestData/testcoverage/codecoverage-maven/target/jacoco.exec"));
        javaConfiguration.setLanguage("java");
        javaConfiguration.setProjectDir(getPathToResource("loaderTestData/testcoverage/codecoverage-maven"));

        loaderPythonConfiguration.setCoverageLocation(getPathToResource("loaderTestData/testcoverage/python_coverage/.seppelsmother"));
        pythonConfiguration.setLanguage("python");
        pythonConfiguration.setProjectDir(getPathToResource("loaderTestData/testcoverage/python_coverage"));
    }



    @Test
    public void testPythonCoverageLoaderTestResultSize() {
        // Call method
        try {
            TestCoverageLoader covLoader = new TestCoverageLoader(pythonConfiguration, loaderPythonConfiguration);
            CoverageData covData = covLoader.loadPythonCoverageData();
            assertNotNull("coverage data null!", covData);
            assertEquals("Size is not correct!", 12, covData.getCoverageData().size());
        } catch (IOException e) {
            fail("Unexpected exception: "+e);
        }
    }

    @Test
    public void testPythonCoverageLoaderOneExampleResult() {
        // Expected data (tests.unittests.test_module3:Module3Test.testBlub)
        Set<IUnit> module3TestTestBlubtestedMethods = new HashSet<>();
        module3TestTestBlubtestedMethods.add(new PythonMethod("project.sup", "module3", null, "blub", new ArrayList<>(), Paths.get("project/sup/module3.py")));
        module3TestTestBlubtestedMethods.add(new PythonMethod("project", "module1", null, "sum", new ArrayList<>(), Paths.get("project/module1.py")));
        // Call method
        try {
            TestCoverageLoader covLoader = new TestCoverageLoader(pythonConfiguration, loaderPythonConfiguration);
            CoverageData covData = covLoader.loadPythonCoverageData();
            assertNotNull("coverage data null!", covData);

            // Compare test
            for (Map.Entry<IUnit, Set<IUnit>> entry : covData.getCoverageData().entrySet()) {
                if (entry.getKey().getFQN().equals("tests.unittests.test_module3:Module3Test.testBlub")) {
                    assertEquals("size is not correct", 2, entry.getValue().size());
                    assertEquals("not the same methods", module3TestTestBlubtestedMethods, entry.getValue());
                    return;
                }
            }

            fail("test not found!");
        } catch (IOException e) {
            fail("Unexpected exception: "+e);
        }
    }


    @Test
    public void testPythonCoverageLoaderExcludeTestsTest() {
        // Expected data (tests.unittests.test_module3:Module3Test.testBlub)
        Set<IUnit> module3TestTestBlubtestedMethods = new HashSet<>();
        module3TestTestBlubtestedMethods.add(new PythonMethod("project.sup", "module3", null, "blub", new ArrayList<>(), Paths.get("project/sup/module3.py")));

        // Call method
        loaderPythonConfiguration.setCoverageLocation(getPathToResource("loaderTestData/testcoverage/python_coverage/.seppelsmother2"));
        try {
            TestCoverageLoader covLoader = new TestCoverageLoader(pythonConfiguration, loaderPythonConfiguration);
            CoverageData covData = covLoader.loadPythonCoverageData();
            assertNotNull("coverage data null!", covData);

            // Compare test
            for (Map.Entry<IUnit, Set<IUnit>> entry : covData.getCoverageData().entrySet()) {
                System.out.println(entry.getValue());
                if (entry.getKey().getFQN().equals("tests.unittests.test_module3:Module3Test.testBlub")) {
                    assertEquals("size is not correct", 1, entry.getValue().size());
                    assertEquals("not the same methods", module3TestTestBlubtestedMethods, entry.getValue());
                    return;
                }
            }
            fail("test not found!");
        } catch (IOException e) {
            fail("Unexpected exception: "+e);
        }
    }

    @Test
    public void testJavaCoverageLoaderClassLevel() {

        IUnit javaTestClass = new JavaClass("de.ugoe.cs.comfort.codecoverage.WithDifferentMethodsCallDifferentMethodsTest",
                Paths.get("src/test/java/de/ugoe/cs/comfort/codecoverage/WithDifferentMethodsCallDifferentMethodsTest.java"));

        // Expected Data (test1)
        Set<IUnit> test1TestedMethods = new HashSet<>();
        test1TestedMethods.add(Module1Init);
        test1TestedMethods.add(Module1Sum);
        test1TestedMethods.add(Module2module1Call);
        test1TestedMethods.add(Module2Sum);

        // Call method
        try {
            TestCoverageLoader covLoader = new TestCoverageLoader(javaConfiguration, loaderJavaConfiguration);
            CoverageData covData = covLoader.loadJavaCoverageData();
            assertNotNull("coverage data null!", covData);

            assertNotNull("Testclass not in set", covData.getCoverageDataClassLevel().get(javaTestClass));
            assertEquals("Not all tested methods are correct!", covData.getCoverageDataClassLevel().get(javaTestClass), test1TestedMethods);
        } catch (IOException e) {
            fail("Unexpected exception: "+e.getMessage());
        }
    }

    @Test
    public void testJavaCoverageLoaderSingleThread() {
        testJavaCoverageLoader();
    }

    @Test
    public void testJavaCoverageLoaderMultiThread() {
        javaConfiguration.setNThreads(4);
        testJavaCoverageLoader();
    }

    private void testJavaCoverageLoader() {

        // Expected data (CCallsSubClassMethodTest)
        Set<IUnit> testCallToSubClassMethodTestedMethods = new HashSet<>();
        testCallToSubClassMethodTestedMethods.add(M1SubclassInit);
        testCallToSubClassMethodTestedMethods.add(M1SubclassSum);

        // Expected data (MethodThatCallsOtherMethodTest)
        Set<IUnit> testMethodThatCallsOtherMethodTestedMethods = new HashSet<>();
        testMethodThatCallsOtherMethodTestedMethods.add(Module1Init);
        testMethodThatCallsOtherMethodTestedMethods.add(Module1Sum);
        testMethodThatCallsOtherMethodTestedMethods.add(Module2module1Call);

        // Expected data (SameMethod1Test)
        Set<IUnit> testSameMethodTestedMethods = new HashSet<>();
        testSameMethodTestedMethods.add(Module1Init);
        testSameMethodTestedMethods.add(Module1Sum);

        // Expected data (SameMethod2Test)
        Set<IUnit> sameMethod2TestTestedMethods = new HashSet<>();
        sameMethod2TestTestedMethods.add(Module1Init);
        sameMethod2TestTestedMethods.add(Module1Sum);

        // Expected Data (testSameMethodInDifferentClasses)
        Set<IUnit> testSameMethodInDifferentClassesTestedMethods = new HashSet<>();
        testSameMethodInDifferentClassesTestedMethods.add(Module1Init);
        testSameMethodInDifferentClassesTestedMethods.add(Module1Sum);
        testSameMethodInDifferentClassesTestedMethods.add(Module2Sum);

        // Expected Data (test1)
        Set<IUnit> test1TestedMethods = new HashSet<>();
        test1TestedMethods.add(Module1Init);
        test1TestedMethods.add(Module1Sum);
        test1TestedMethods.add(Module2module1Call);

        // Expected Data (test2)
        Set<IUnit> test2TestedMethods2 = new HashSet<>();
        test2TestedMethods2.add(Module2Sum);

        // Expected Data (WithDifferentMethodsCallSameMethodTest.test1)
        Set<IUnit> withDifferentMethodsCallSameMethodTestTestedMethods = new HashSet<>();
        withDifferentMethodsCallSameMethodTestTestedMethods.add(Module1Init);
        withDifferentMethodsCallSameMethodTestTestedMethods.add(Module1Sum);

        // Expected Data (WithDifferentMethodsCallSameMethodTest.test2)
        Set<IUnit> withDifferentMethodsCallSameMethodTest2TestedMethods = new HashSet<>();
        withDifferentMethodsCallSameMethodTest2TestedMethods.add(Module1Init);
        withDifferentMethodsCallSameMethodTest2TestedMethods.add(Module1Sum);

        // Expected Data (IfInMethodTest.test1)
        Set<IUnit> ifInMethodTesttestedMethods = new HashSet<>();
        ifInMethodTesttestedMethods.add(Module3BiggerThan);

        // Expected Data (CallMethodWithSameParameterFromDifferentPackagesTest.test1)
        Set<IUnit> callMethodWithSameParameterFromDifferentPackagesTesttestedMethods = new HashSet<>();
        callMethodWithSameParameterFromDifferentPackagesTesttestedMethods.add(Module4Init);
        callMethodWithSameParameterFromDifferentPackagesTesttestedMethods.add(Module4PkgInit);
        callMethodWithSameParameterFromDifferentPackagesTesttestedMethods.add(Module3CompareModule);
        callMethodWithSameParameterFromDifferentPackagesTesttestedMethods.add(Module3CompareModule2);

        Map<IUnit, Set<IUnit>> result = new HashMap<>();
        result.put(testCallToSubClassMethod, testCallToSubClassMethodTestedMethods);
        result.put(testMethodThatCallsOtherMethod, testMethodThatCallsOtherMethodTestedMethods);
        result.put(testSameMethodTested, testSameMethodTestedMethods);
        result.put(sameMethod2Test, sameMethod2TestTestedMethods);
        result.put(testSameMethodInDifferentClasses, testSameMethodInDifferentClassesTestedMethods);
        result.put(test1, test1TestedMethods);
        result.put(test2, test2TestedMethods2);
        result.put(withDifferentMethodsCallSameMethodTest, withDifferentMethodsCallSameMethodTestTestedMethods);
        result.put(withDifferentMethodsCallSameMethodTest2, withDifferentMethodsCallSameMethodTest2TestedMethods);
        result.put(ifInMethodTest, ifInMethodTesttestedMethods);
        result.put(callMethodWithSameParameterFromDifferentPackagesTest, callMethodWithSameParameterFromDifferentPackagesTesttestedMethods);

        // Call method
        try {
            TestCoverageLoader covLoader = new TestCoverageLoader(javaConfiguration, loaderJavaConfiguration);
            CoverageData covData = covLoader.loadJavaCoverageData();
            assertNotNull("coverage data null!", covData);
            assertEquals("Not all tested methods are correct!", result, covData.getCoverageData());
        } catch (IOException e) {
            fail("Unexpected exception: "+e.getMessage());
        }
    }
}

