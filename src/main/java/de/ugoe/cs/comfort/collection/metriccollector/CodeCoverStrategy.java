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

import de.ugoe.cs.comfort.configuration.GeneralConfiguration;
import de.ugoe.cs.comfort.filer.BaseFiler;

/**
 * @author Fabian Trautsch
 */
public class CodeCoverStrategy extends BaseMetricCollector {

    public CodeCoverStrategy(GeneralConfiguration configuration, BaseFiler filer) {
        super(configuration, filer);
    }
}
    /*
public class CodeCoverStrategy implements IMetricCollector {

    @Override
    public Set<Result> classify(GeneralConfiguration configuration, CallGraph callGraph) {
        // Get all test granularities
        Map<String, Integer> testGranularities = DetectionUtils.getTestGranularitiesForTestsOnMethodLevel(callGraph);

        try {
            storeGranularitiesInCSV(configuration, testGranularities);
        } catch (IOException e) {
            throw new MetricCollectorException("Could not write granularities in csv file!" +e);
        }

        return new HashSet<>();
    }

    @Override
    public Set<Result> classify(GeneralConfiguration configuration, DependencyGraph dependencyGraph) {
        Map<String, Set<String>> callerCalleePaitsOnClassLevel = DetectionUtils
        .getCallPairsOnClassLevel(dependencyGraph);
        Map<String, Integer> testGranularities =
                callerCalleePaitsOnClassLevel.entrySet().stream().collect(Collectors
                .toMap(Map.Entry::getKey, entry -> entry.getValue().size()));

        try {
            storeGranularitiesInCSV(configuration, testGranularities);
        } catch (IOException e) {
            throw new MetricCollectorException("Could not write granularities in csv file!" +e);
        }
        return new HashSet<>();
    }

    private void storeGranularitiesInCSV(GeneralConfiguration configuration, Map<String, Integer> testGranularities)
    throws IOException{
        // Get storage path
        String storagePath = configuration.getConfigurationDetectionStrategy().getConfigurationOptions().getCsvPath();

        // Check if stuff should be stored
        if(storagePath == null || storagePath.isEmpty()) {
            return;
        }

        // Get the tests per granularity, e.g., for the granularity 1 there are 5 tests, for 2 there are 2 tests, etc.
        Map<Integer, Long> testsPerGranularity =
                testGranularities.values().stream().collect(Collectors.groupingBy(e -> e, Collectors.counting()));

        // Sort the testsPerGranularity by key (Granularity 0 -> 1 test, 1-> 3 tests, etc.)
        Map<Integer, Long> sortedTestsPerGranularity = testsPerGranularity.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));

        // Create csv string from the whole sorted map
        String csvString = sortedTestsPerGranularity.entrySet().stream()
                .map(entry -> entry.getKey()+","+entry.getValue())
                .collect(Collectors.joining(System.getProperty("line.separator")));

        // Create filename basted on projectname and current date
        String fileName = String.format(
                "%s_testGranularities_%s.csv",
                configuration.getProjectName(),
                new SimpleDateFormat("ddMMyyyy_HHmm").format(new Date()));

        // Write the file to the storage path
        Files.write(Paths.get(storagePath, fileName), csvString.getBytes(UTF_8));

    }

    @Override
    public Set<Result> classify(GeneralConfiguration configuration, CoverageData dataSet) {
        Map<String, Integer> testGranularities =
                dataSet.getCoverageData().stream().collect(Collectors
                .toMap(ITestMethod::getUniqueIdentifier, entry -> entry.getTestedMethods().size()));
        System.out.println(testGranularities);

        try {
            storeGranularitiesInCSV(configuration, testGranularities);
        } catch (IOException e) {
            throw new MetricCollectorException("Could not write granularities in csv file!" +e);
        }

        return null;
    }

    @Override
    public Set<Result> classify(GeneralConfiguration configuration, ChangeSet dataSet) {
        throw new MetricCollectorException("Not implemented!");
    }

    @Override
    public Set<Result> classify(GeneralConfiguration configuration, ProjectFiles dataSet) {
        throw new MetricCollectorException("Not implemented!");
    }

}
   */
