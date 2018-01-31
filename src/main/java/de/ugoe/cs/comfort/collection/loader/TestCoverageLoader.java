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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.ugoe.cs.comfort.FileNameUtils;
import de.ugoe.cs.comfort.Utils;
import de.ugoe.cs.comfort.annotations.SupportsJava;
import de.ugoe.cs.comfort.annotations.SupportsPython;
import de.ugoe.cs.comfort.collection.loader.testcoverage.ExecutionDataVisitor;
import de.ugoe.cs.comfort.collection.loader.testcoverage.JacocoReportReader;
import de.ugoe.cs.comfort.configuration.GeneralConfiguration;
import de.ugoe.cs.comfort.configuration.LoaderConfiguration;
import de.ugoe.cs.comfort.data.CoverageData;
import de.ugoe.cs.comfort.data.models.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import org.apache.bcel.generic.Type;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.IMethodCoverage;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.report.JavaNames;

/**
 * @author Fabian Trautsch
 */
public class TestCoverageLoader extends BaseLoader {
    private static final JavaNames JN = new JavaNames();
    private FileNameUtils fileNameUtils;

    public TestCoverageLoader(GeneralConfiguration generalConfiguration, LoaderConfiguration loaderConfiguration) {
        super(generalConfiguration, loaderConfiguration);
        fileNameUtils = new FileNameUtils(generalConfiguration);
    }


    @SupportsPython
    public CoverageData loadPythonCoverageData() throws IOException {
        CoverageData covfefe = new CoverageData();

        JsonParser jp = new JsonFactory().createParser(loaderConf.getCoverageLocation().toFile());
        ObjectMapper mapper = new ObjectMapper();

        jp.nextToken(); // START_ARRAY
        while (jp.nextToken() != JsonToken.END_ARRAY) {
            // Read PythonTest from JSON Coverage
            PythonCoverageLoaderTestMethod testMethod = mapper.readValue(jp, PythonCoverageLoaderTestMethod.class);

            // Convert
            PythonMethod pythonMethod = new PythonMethod(testMethod,
                    fileNameUtils.getPathForPythonModuleFQN(testMethod.getModule()));

            Set<IUnit> testedMethodsWithoutTestsItself = new HashSet<>();

            // We need to check all tested methods, if there is a test itself is in it
            // This can be the case if the tests are part of the program itself (e.g. networkx/util/tests)
            for(PythonCoveragerloaderTestedMethod testedMethod: testMethod.getTestedMethods()) {
                // Convert
                PythonMethod pythonTestedMethod = new PythonMethod(testedMethod,
                        fileNameUtils.getPathForPythonModuleFQN(testedMethod.getModule()));

                if(pythonTestedMethod.isTestBasedOnMethod()) {
                    logger.debug("Excluding test {}, because it is a test!", pythonTestedMethod);
                } else {
                    testedMethodsWithoutTestsItself.add(pythonTestedMethod);
                }
            }
            covfefe.add(pythonMethod, testedMethodsWithoutTestsItself);
        }

        return covfefe;
    }


    @SupportsJava
    public CoverageData loadJavaCoverageData() throws IOException {
        // Get jacoco reader and visitor for the execution data
        JacocoReportReader reader = new JacocoReportReader(loaderConf.getCoverageLocation().toFile());
        ExecutionDataVisitor visitor = new ExecutionDataVisitor();

        // Read the jacoco execution report
        reader.readJacocoReport(visitor, visitor);

        // Create coverage data
        CoverageData covData = new CoverageData();

        // Go through each session and parse the data. Only include sessions that have a name
        for(Map.Entry<String, ExecutionDataStore> entry: visitor.getSessions().entrySet()) {
            String sessionId = entry.getKey();

            // If it does not have a session id, we do not know which test was executed
            if(!sessionId.isEmpty() && sessionId.contains("%%")) {
                logger.info("Analyzing Session {}...", entry.getKey());

                // Create test class & method
                String fqnOfTest = sessionId.split("%%")[0];
                String methodName = sessionId.split("%%")[1];
                logger.debug("Created the following TestMethod: {}", fqnOfTest+"."+methodName);


                // Read the execution data
                ExecutionDataStore data = entry.getValue();

                // Analyze the files, where all class files in the project dir are looked at
                CoverageBuilder covfefe = reader.analyzeFiles(data, Utils.getAllFilesFromProjectForRegex(
                        generalConf.getProjectDir(), ".*\\.class"));

                // Parse the class coverage data
                Set<IUnit> testedMethods = parseClassCoverageDataForJavaTestMethod(fqnOfTest, covfefe);
                IUnit testMethod = new JavaMethod(fqnOfTest, methodName, new ArrayList<>(),
                        fileNameUtils.getPathForJavaClassFQN(fqnOfTest));

                covData.add(testMethod, testedMethods);
                if(testedMethods.size() == 0) {
                    logger.warn("Could not find tested methods!");
                }

            }
        }
        return covData;
    }

    private Set<IUnit> parseClassCoverageDataForJavaTestMethod(String fqnOfTest, CoverageBuilder covfefe) {
        // Go through all classes that were found and have a look if it is covered
        Set<IUnit> result = new HashSet<>();
        for(IClassCoverage classCoverage: covfefe.getClasses()) {
            String className = JN.getQualifiedClassName(classCoverage.getName());
            // If it is covered: go through its methods to see which methods are covered
            // Exclude the test method itself and if the class is a test class
            if(classCoverage.getClassCounter().getCoveredCount() == 1 && !className.equals(fqnOfTest)
                    && !Utils.isTestBasedOnFQN(className)) {
                result.addAll(parseMethodCoverageDataForJavaTestMethod(classCoverage));
            }
        }

        return result;
    }

    private Set<JavaMethod> parseMethodCoverageDataForJavaTestMethod(IClassCoverage classCoverage) {
        Set<JavaMethod> allTestedMethods = new HashSet<>();
        for(IMethodCoverage methodCoverage: classCoverage.getMethods()) {
            if(methodCoverage.getMethodCounter().getCoveredCount() == 1) {
                // Calculate covered and uncovered lines
                //Map<String, List<Integer>> lines = coveredLines(methodCoverage);


                String packageName = classCoverage.getPackageName().replace("/", ".");
                String className = classCoverage.getName().replace(classCoverage.getPackageName()+"/", "");

                // Add parameter
                List<String> parameter = new ArrayList<>();
                for(Type type : Type.getArgumentTypes(methodCoverage.getDesc())) {
                    parameter.add(type.toString());
                }

                // Create Java Method for tested method
                JavaMethod testedJavaMethod;
                try {
                    testedJavaMethod = new JavaMethod(packageName, className, methodCoverage.getName(), parameter,
                            fileNameUtils.getPathForJavaClassFQN(packageName+"."+className));
                } catch (FileNotFoundException e) {
                    testedJavaMethod = new JavaMethod(packageName, className, methodCoverage.getName(), parameter,
                            null);
                    logger.warn("Could not find file for class {}", packageName+"."+className);
                }
                logger.debug("Covered unit: {}", testedJavaMethod.getFQN());

                // Add to result set
                allTestedMethods.add(testedJavaMethod);
            }
        }

        return allTestedMethods;
    }

    /*
    private static Map<String, List<Integer>> coveredLines(IMethodCoverage coverage) {
        List<Integer> coveredLines = new ArrayList<>();
        List<Integer> uncoveredLines = new ArrayList<>();
        for (int lineId = coverage.getFirstLine(); lineId <= coverage.getLastLine(); lineId++) {
            ILine line = coverage.getLine(lineId);
            switch (line.getInstructionCounter().getStatus()) {
                case ICounter.FULLY_COVERED:
                case ICounter.PARTLY_COVERED:
                    coveredLines.add(lineId);
                    break;
                case ICounter.NOT_COVERED:
                    uncoveredLines.add(lineId);
                    break;
                default:
            }
        }

        Map<String, List<Integer>> result = new HashMap<>();
        result.put("covered", coveredLines);
        result.put("uncovered", uncoveredLines);

        return result;
    }*/
}
