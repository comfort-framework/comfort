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
import static org.junit.Assert.fail;

import de.ugoe.cs.seppelshark.BaseTest;
import de.ugoe.cs.seppelshark.configuration.GeneralConfiguration;
import de.ugoe.cs.seppelshark.data.ProjectFiles;
import de.ugoe.cs.seppelshark.exception.MetricCollectorException;
import de.ugoe.cs.seppelshark.filer.models.Result;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Fabian Trautsch
 */
public class LOCAndMcCabeCollectorTest extends BaseTest {
    private final String basePath = getPathToResource("metricCollectorTestData/locandmccabecollector");

    private LOCAndMcCabeCollector locAndMcCabeCollector;

    private GeneralConfiguration javaConfig = new GeneralConfiguration();
    private GeneralConfiguration pythonConfig = new GeneralConfiguration();

    private Set<Result> expectedResult = new HashSet<>();

    private ProjectFiles projectFiles;


    @Before
    public void createJavaConfig() {
        javaConfig.setProjectDir(basePath+"/java");
    }

    @Before
    public void createPythonConfig() {
        pythonConfig.setProjectDir(basePath+"/python/tests");
        pythonConfig.setLanguage("python");
    }

    @Before
    public void createData() {
        HashSet<Path> test_files = new HashSet<>();
        test_files.add(Paths.get(basePath, "java", "org/foo/JavaAbstractTestFile.java"));
        test_files.add(Paths.get(basePath, "java", "org/foo/JavaTestFile.java"));

        projectFiles = new ProjectFiles(Paths.get(basePath, "java"), test_files, null);
    }

    @Test
    public void getLOCAndMcCabeForPythonMethodTest() {
        try {
            pythonConfig.setMethodLevel(true);
            locAndMcCabeCollector = new LOCAndMcCabeCollector(pythonConfig);
            Path testDemo = Paths.get(getPathToResource("metricCollectorTestData/locandmccabecollector/python/tests/testme.py"));


            Set<Result> result = locAndMcCabeCollector.getLOCAndMcCabeForPythonMethod(null);
            Result findPort = new Result("testme.find_port", testDemo);
            findPort.addMetric("mc_cabe_sg", "1");
            findPort.addMetric("mc_cabe_all", "1");
            findPort.addMetric("cloc", "3");
            findPort.addMetric("lloc", "6");
            expectedResult.add(findPort);

            Result runAioHTTP = new Result("testme.run_aiohttp", testDemo);
            runAioHTTP.addMetric("mc_cabe_sg", "3");
            runAioHTTP.addMetric("mc_cabe_all", "7");
            runAioHTTP.addMetric("cloc", "2");
            runAioHTTP.addMetric("lloc", "35");
            expectedResult.add(runAioHTTP);

            Result runAioHTTPTest = new Result("testme.run_aiohttp.test", testDemo);
            runAioHTTPTest.addMetric("mc_cabe_sg", "1");
            runAioHTTPTest.addMetric("mc_cabe_all", "1");
            runAioHTTPTest.addMetric("cloc", "0");
            runAioHTTPTest.addMetric("lloc", "4");
            expectedResult.add(runAioHTTPTest);

            Result runAioHTTPPrepare = new Result("testme.run_aiohttp.prepare", testDemo);
            runAioHTTPPrepare.addMetric("mc_cabe_sg", "1");
            runAioHTTPPrepare.addMetric("mc_cabe_all", "1");
            runAioHTTPPrepare.addMetric("cloc", "1");
            runAioHTTPPrepare.addMetric("lloc", "4");
            expectedResult.add(runAioHTTPPrepare);

            Result runAioHTTPStop = new Result("testme.run_aiohttp.stop", testDemo);
            runAioHTTPStop.addMetric("mc_cabe_sg", "1");
            runAioHTTPStop.addMetric("mc_cabe_all", "1");
            runAioHTTPStop.addMetric("cloc", "0");
            runAioHTTPStop.addMetric("lloc", "4");
            expectedResult.add(runAioHTTPStop);

            Result runAioHTTPInit = new Result("testme.run_aiohttp.init", testDemo);
            runAioHTTPInit.addMetric("mc_cabe_sg", "1");
            runAioHTTPInit.addMetric("mc_cabe_all", "1");
            runAioHTTPInit.addMetric("cloc", "0");
            runAioHTTPInit.addMetric("lloc", "9");
            expectedResult.add(runAioHTTPInit);

            Result runTornado = new Result("testme.run_tornado", testDemo);
            runTornado.addMetric("mc_cabe_sg", "1");
            runTornado.addMetric("mc_cabe_all", "5");
            runTornado.addMetric("cloc", "7");
            runTornado.addMetric("lloc", "24");
            expectedResult.add(runTornado);

            Result runTornadoTestHandlerGet = new Result("testme.run_tornado.TestHandler.get", testDemo);
            runTornadoTestHandlerGet.addMetric("mc_cabe_sg", "1");
            runTornadoTestHandlerGet.addMetric("mc_cabe_all", "1");
            runTornadoTestHandlerGet.addMetric("cloc", "1");
            runTornadoTestHandlerGet.addMetric("lloc", "4");
            expectedResult.add(runTornadoTestHandlerGet);

            Result runTornadoPrepareHandlerGet = new Result("testme.run_tornado.PrepareHandler.get", testDemo);
            runTornadoPrepareHandlerGet.addMetric("mc_cabe_sg", "1");
            runTornadoPrepareHandlerGet.addMetric("mc_cabe_all", "1");
            runTornadoPrepareHandlerGet.addMetric("cloc", "1");
            runTornadoPrepareHandlerGet.addMetric("lloc", "3");
            expectedResult.add(runTornadoPrepareHandlerGet);

            Result runTornadoStopHandlerGet = new Result("testme.run_tornado.StopHandler.get", testDemo);
            runTornadoStopHandlerGet.addMetric("mc_cabe_sg", "1");
            runTornadoStopHandlerGet.addMetric("mc_cabe_all", "1");
            runTornadoStopHandlerGet.addMetric("cloc", "0");
            runTornadoStopHandlerGet.addMetric("lloc", "2");
            expectedResult.add(runTornadoStopHandlerGet);

            Result runTornadoStopHandlerOnFinish = new Result("testme.run_tornado.StopHandler.on_finish", testDemo);
            runTornadoStopHandlerOnFinish.addMetric("mc_cabe_sg", "1");
            runTornadoStopHandlerOnFinish.addMetric("mc_cabe_all", "1");
            runTornadoStopHandlerOnFinish.addMetric("cloc", "0");
            runTornadoStopHandlerOnFinish.addMetric("lloc", "2");
            expectedResult.add(runTornadoStopHandlerOnFinish);

            Result regSubappSignals = new Result("testme._reg_subapp_signals", testDemo);
            regSubappSignals.addMetric("mc_cabe_sg", "1");
            regSubappSignals.addMetric("mc_cabe_all", "5");
            regSubappSignals.addMetric("cloc", "12");
            regSubappSignals.addMetric("lloc", "15");
            expectedResult.add(regSubappSignals);

            Result regSubappSignalsRegHandler = new Result("testme._reg_subapp_signals.reg_handler", testDemo);
            regSubappSignalsRegHandler.addMetric("mc_cabe_sg", "2");
            regSubappSignalsRegHandler.addMetric("mc_cabe_all", "4");
            regSubappSignalsRegHandler.addMetric("cloc", "6");
            regSubappSignalsRegHandler.addMetric("lloc", "11");
            expectedResult.add(regSubappSignalsRegHandler);

            Result regSubappSignalsRegHandlerHandler = new Result("testme._reg_subapp_signals.reg_handler.handler", testDemo);
            regSubappSignalsRegHandlerHandler.addMetric("mc_cabe_sg", "2");
            regSubappSignalsRegHandlerHandler.addMetric("mc_cabe_all", "2");
            regSubappSignalsRegHandlerHandler.addMetric("cloc", "1");
            regSubappSignalsRegHandlerHandler.addMetric("lloc", "5");
            expectedResult.add(regSubappSignalsRegHandlerHandler);

            assertEquals("Result set is not correct!", expectedResult, result);

        } catch (MetricCollectorException e) {
            fail("Unexpected exception: "+e);
        }

    }

    @Test
    public void getLOCAndMcCabeForJavaMethodTest() {
        try {
            javaConfig.setMethodLevel(true);
            locAndMcCabeCollector = new LOCAndMcCabeCollector(javaConfig);
            Set<Result> result = locAndMcCabeCollector.getLOCAndMcCabeForJavaMethod(projectFiles);
            Result doSomethingResult = new Result("org.foo.JavaAbstractTestFile.doSomething", Paths.get("org/foo/JavaAbstractTestFile.java"));
            doSomethingResult.addMetric("mc_cabe", "0");
            doSomethingResult.addMetric("lloc", "1");
            doSomethingResult.addMetric("cloc", "0");
            expectedResult.add(doSomethingResult);

            Result myMethodResult = new Result("org.foo.JavaTestFile.myMethod", Paths.get("org/foo/JavaTestFile.java"));
            myMethodResult.addMetric("mc_cabe", "1");
            myMethodResult.addMetric("lloc", "1");
            myMethodResult.addMetric("cloc", "0");
            expectedResult.add(myMethodResult);

            Result ifMethodResult = new Result("org.foo.JavaTestFile.ifMethod", Paths.get("org/foo/JavaTestFile.java"));
            ifMethodResult.addMetric("mc_cabe", "3");
            ifMethodResult.addMetric("lloc", "10");
            ifMethodResult.addMetric("cloc", "1");
            expectedResult.add(ifMethodResult);

            Result whileTestResult = new Result("org.foo.JavaTestFile.whileTest", Paths.get("org/foo/JavaTestFile.java"));
            whileTestResult.addMetric("mc_cabe", "4");
            whileTestResult.addMetric("lloc", "8");
            whileTestResult.addMetric("cloc", "1");
            expectedResult.add(whileTestResult);

            Result forTestResult = new Result("org.foo.JavaTestFile.forTest", Paths.get("org/foo/JavaTestFile.java"));
            forTestResult.addMetric("mc_cabe", "3");
            forTestResult.addMetric("lloc", "7");
            forTestResult.addMetric("cloc", "1");
            expectedResult.add(forTestResult);

            Result switchTestResult = new Result("org.foo.JavaTestFile.switchTest", Paths.get("org/foo/JavaTestFile.java"));
            switchTestResult.addMetric("mc_cabe", "8");
            switchTestResult.addMetric("lloc", "20");
            switchTestResult.addMetric("cloc", "1");
            expectedResult.add(switchTestResult);

            Result foreachTestResult = new Result("org.foo.JavaTestFile.foreachCatchTest", Paths.get("org/foo/JavaTestFile.java"));
            foreachTestResult.addMetric("mc_cabe", "4");
            foreachTestResult.addMetric("lloc", "8");
            foreachTestResult.addMetric("cloc", "1");
            expectedResult.add(foreachTestResult);

            Result expressionTestResult = new Result("org.foo.JavaTestFile.expressionTest", Paths.get("org/foo/JavaTestFile.java"));
            expressionTestResult.addMetric("mc_cabe", "3");
            expressionTestResult.addMetric("lloc", "5");
            expressionTestResult.addMetric("cloc", "8");
            expectedResult.add(expressionTestResult);

            Result ternaryOperatorTestResult = new Result("org.foo.JavaTestFile.ternaryOperatorTest", Paths.get("org/foo/JavaTestFile.java"));
            ternaryOperatorTestResult.addMetric("mc_cabe", "2");
            ternaryOperatorTestResult.addMetric("lloc", "3");
            ternaryOperatorTestResult.addMetric("cloc", "4");
            expectedResult.add(ternaryOperatorTestResult);

            Result complete1Result = new Result("org.foo.JavaTestFile.complete1", Paths.get("org/foo/JavaTestFile.java"));
            complete1Result.addMetric("mc_cabe", "11");
            complete1Result.addMetric("lloc", "34");
            complete1Result.addMetric("cloc", "1");
            expectedResult.add(complete1Result);

            Result complete2Result = new Result("org.foo.JavaTestFile.complete2", Paths.get("org/foo/JavaTestFile.java"));
            complete2Result.addMetric("mc_cabe", "12");
            complete2Result.addMetric("lloc", "25");
            complete2Result.addMetric("cloc", "2");
            expectedResult.add(complete2Result);


            assertEquals("Result set is not correct!", expectedResult, result);

        } catch (MetricCollectorException e) {
            fail("Unexpected exception: "+e);
        }
    }

    @Test
    public void getLOCAndMcCabeForJavaClassTest() {
        try {
            locAndMcCabeCollector = new LOCAndMcCabeCollector(javaConfig);
            Set<Result> result = locAndMcCabeCollector.getLOCAndMcCabeForJavaClass(projectFiles);
            Result javaTestFileResult = new Result("org.foo.JavaTestFile", Paths.get("org/foo/JavaTestFile.java"));
            javaTestFileResult.addMetric("lloc", "126");
            javaTestFileResult.addMetric("cloc", "20");

            Result javaAbstractTestFile = new Result("org.foo.JavaAbstractTestFile", Paths.get("org/foo/JavaAbstractTestFile.java"));
            javaAbstractTestFile.addMetric("lloc", "3");
            javaAbstractTestFile.addMetric("cloc", "0");

            expectedResult.add(javaTestFileResult);
            expectedResult.add(javaAbstractTestFile);
            assertEquals("Result set is not correct!", expectedResult, result);
        } catch (MetricCollectorException e) {
            fail("Unexpected exception: "+e);
        }
    }
}
