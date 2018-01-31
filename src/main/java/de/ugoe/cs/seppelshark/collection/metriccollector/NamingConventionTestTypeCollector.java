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

import com.google.common.io.Files;
import de.ugoe.cs.seppelshark.annotations.SupportsClass;
import de.ugoe.cs.seppelshark.annotations.SupportsJava;
import de.ugoe.cs.seppelshark.annotations.SupportsPython;
import de.ugoe.cs.seppelshark.configuration.GeneralConfiguration;
import de.ugoe.cs.seppelshark.data.ProjectFiles;
import de.ugoe.cs.seppelshark.filer.models.Result;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Fabian Trautsch
 */
public class NamingConventionTestTypeCollector extends BaseMetricCollector {

    public NamingConventionTestTypeCollector(GeneralConfiguration configuration) {
        super(configuration);
    }

    @SupportsPython
    @SupportsJava
    @SupportsClass
    public Set<Result> createResults(ProjectFiles projectFiles) {
        // Get all code files without tests in a list for comparison later on
        List<String> codeFileNamesWithoutTests = new ArrayList<>();
        projectFiles.getCodeFilesWithoutTestFiles().forEach(
                codeFilePath ->
                    codeFileNamesWithoutTests.add(Files.getNameWithoutExtension(codeFilePath.toString()).toLowerCase())
        );
        logger.debug("Found the following java code filenames: {}", codeFileNamesWithoutTests);

        // Create filer map
        Set<Result> result = new HashSet<>();
        for(Path testFile: projectFiles.getTestFiles()) {
            result.add(classifyTestFile(testFile, codeFileNamesWithoutTests));
        }

        logger.debug("Got the following classification results: {}", result);
        return result;
    }

    private Result classifyTestFile(Path testFile, List<String> codeFileNamesWithoutTests) {
        Result result = new Result(testFile.toString(), testFile);
        if(isIntegrationTest(testFile)) {
            result.addMetric("files_nc", TestType.INTEGRATION.name());
        } else if(isUnitTest(testFile, codeFileNamesWithoutTests)) {
            result.addMetric("files_nc", TestType.UNIT.name());
        } else {
            logger.warn("Could not classify {}.", testFile);
            result.addMetric("files_nc", TestType.UNKNOWN.name());
        }

        return result;
    }

    private Boolean isIntegrationTest(Path testFilePath) {
        // List of patterns to check
        String[] filenamePatterns = new String[]{"IT", "Integration", "integration", "IntegrationTest"};
        String[] pathPatterns = new String[]{"IT", "Integration", "integration"};

        // If any of the filename patterns is in the filename, we suspect it is an integration test
        String testFileName = Files.getNameWithoutExtension(testFilePath.toString());
        if(Arrays.stream(filenamePatterns).parallel().anyMatch(testFileName::contains)) {
            return true;
        }

        // If IT, Integration, integration is in filepath, we suspect it is an integration test
        if(testFilePath.getNameCount() > 1) {
            String testFilePathWithoutFileName = testFilePath.subpath(0, testFilePath.getNameCount() - 1).toString();
            if (Arrays.stream(pathPatterns).parallel().anyMatch(testFilePathWithoutFileName::contains)) {
                return true;
            }
        }

        return false;
    }

    private Boolean isUnitTest(Path testFilePath, List<String> codeFileNamesWithoutTests) {
        // List of patterns to check
        String[] pathPatterns = new String[]{"unit", "Unit", "UnitTest", "unitTest", "unittest", "unittests"};

        // If the name of a code file is in the test, we suspect it is an unit test
        // We filter out every other code file, where the name of it is not in the test case filename
        String lowerCaseFilename = Files.getNameWithoutExtension(testFilePath.toString()).toLowerCase();

        List<String> filteredCodeFileNames = codeFileNamesWithoutTests.stream()
                .filter(name -> lowerCaseFilename.equals(name + "test") || lowerCaseFilename.equals("test" + name)
                    || lowerCaseFilename.equals(name + "_test") || lowerCaseFilename.equals("test_" + name)
                    || lowerCaseFilename.equals(name + "_tests") || lowerCaseFilename.equals("tests_" + name))
                .collect(Collectors.toList());

        if (filteredCodeFileNames.size() >= 1) {
            String mostProbableMatch = Collections.max(filteredCodeFileNames, Comparator.comparing(String::length));
            logger.debug("{} is matching the following code files: {}. Most probable match: {}",
                    Files.getNameWithoutExtension(testFilePath.toString()), filteredCodeFileNames, mostProbableMatch);
            return true;
        }

        // If the path contains the word unit,
        if(testFilePath.getNameCount() > 1) {
            String testFilePathWithoutFileName = testFilePath.subpath(0, testFilePath.getNameCount() - 1).toString();
            if (Arrays.stream(pathPatterns).parallel().anyMatch(testFilePathWithoutFileName::contains)) {
                return true;
            }
        }

        return false;
    }
}
