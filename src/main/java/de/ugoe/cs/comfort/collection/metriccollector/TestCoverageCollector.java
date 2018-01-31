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

import de.ugoe.cs.comfort.annotations.SupportsClass;
import de.ugoe.cs.comfort.annotations.SupportsJava;
import de.ugoe.cs.comfort.annotations.SupportsMethod;
import de.ugoe.cs.comfort.annotations.SupportsPython;
import de.ugoe.cs.comfort.configuration.GeneralConfiguration;
import de.ugoe.cs.comfort.data.CoverageData;
import de.ugoe.cs.comfort.data.graphs.CallGraph;
import de.ugoe.cs.comfort.data.graphs.DependencyGraph;
import de.ugoe.cs.comfort.data.models.IUnit;
import de.ugoe.cs.comfort.filer.models.Result;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Fabian Trautsch
 */
public class TestCoverageCollector extends BaseMetricCollector{

    public TestCoverageCollector(GeneralConfiguration configuration) {
        super(configuration);
    }

    @SupportsMethod
    @SupportsJava
    @SupportsPython
    public Set<Result> createResultsJavaPythonMethodLevel(CoverageData coverageData) {
        Map<IUnit, Set<String>> testAndCoveredMethods = new HashMap<>();

        for (Map.Entry<IUnit, Set<IUnit>> entry : coverageData.getCoverageData().entrySet()) {
            Set<String> coveredMethods = testAndCoveredMethods.getOrDefault(entry.getKey(), new HashSet<>());
            entry.getValue().forEach(testedMethod -> coveredMethods.add(testedMethod.getFQNOfUnit()));
            testAndCoveredMethods.put(entry.getKey(), coveredMethods);
        }

        return createResultsForMap(testAndCoveredMethods, true, "cov_tcov_met");
    }


    @SupportsClass
    @SupportsJava
    @SupportsPython
    public Set<Result> createResultsJavaPythonClassLevel(CoverageData coverageData) {
        Map<IUnit, Set<String>> testAndCoveredMethods = new HashMap<>();

        for (Map.Entry<IUnit, Set<IUnit>> entry : coverageData.getCoverageDataClassLevel().entrySet()) {
            Set<String> coveredMethods = testAndCoveredMethods.getOrDefault(entry.getKey(), new HashSet<>());
            entry.getValue().forEach(testedMethod -> coveredMethods.add(testedMethod.getFQNOfUnit()));
            testAndCoveredMethods.put(entry.getKey(), coveredMethods);
        }

        return createResultsForMap(testAndCoveredMethods, false, "cov_tcov");
    }

    @SupportsJava
    @SupportsPython
    @SupportsMethod
    public Set<Result> createResultsForCallGraphMethodLevel(CallGraph callGraph) {
        Map<IUnit, Set<IUnit>> callerCalleePairs =
                TestTypeDetectionUtils.getCallPairsOnClassLevel(callGraph);
        Map<IUnit, Set<String>> testAndCoveredMethods = new HashMap<>();
        for (Map.Entry<IUnit, Set<IUnit>> entry : callerCalleePairs.entrySet()) {
            Set<String> coveredMethods = testAndCoveredMethods.getOrDefault(entry.getKey(), new HashSet<>());
            entry.getValue().forEach(testedMethod -> coveredMethods.add(testedMethod.getFQNOfUnit()));
            testAndCoveredMethods.put(entry.getKey(), coveredMethods);
        }
        return createResultsForMap(testAndCoveredMethods, true, "call_tcov_met");
    }

    @SupportsJava
    @SupportsPython
    @SupportsClass
    public Set<Result> createResultsForCallGraphClassLevel(CallGraph callGraph) {
        Map<IUnit, Set<IUnit>> callerCalleePairs =
                TestTypeDetectionUtils.getCallPairsOnClassLevel(callGraph.getDependencyGraphRepresentation());
        Map<IUnit, Set<String>> testAndCoveredMethods = new HashMap<>();
        for (Map.Entry<IUnit, Set<IUnit>> entry : callerCalleePairs.entrySet()) {
            Set<String> coveredMethods = testAndCoveredMethods.getOrDefault(entry.getKey(), new HashSet<>());
            entry.getValue().forEach(testedMethod -> coveredMethods.add(testedMethod.getFQNOfUnit()));
            testAndCoveredMethods.put(entry.getKey(), coveredMethods);
        }
        return createResultsForMap(testAndCoveredMethods, false, "call_tcov");
    }

    @SupportsPython
    @SupportsJava
    @SupportsClass
    public Set<Result> createResultsForDependencyGraph(DependencyGraph dependencyGraph) {
        Map<IUnit, Set<IUnit>> callerCalleePairs =
                TestTypeDetectionUtils.getCallPairsOnClassLevel(dependencyGraph);
        Map<IUnit, Set<String>> testAndCoveredMethods = new HashMap<>();
        for (Map.Entry<IUnit, Set<IUnit>> entry : callerCalleePairs.entrySet()) {
            Set<String> coveredMethods = testAndCoveredMethods.getOrDefault(entry.getKey(), new HashSet<>());
            entry.getValue().forEach(testedMethod -> coveredMethods.add(testedMethod.getFQNOfUnit()));
            testAndCoveredMethods.put(entry.getKey(), coveredMethods);
        }
        return createResultsForMap(testAndCoveredMethods, false, "dep_tcov");
    }

    private Set<Result> createResultsForMap(Map<IUnit, Set<String>> testAndCoveredMethods,
                                            boolean methodLevel, String metricName) {
        // First we need to go through everything and get the number of all covered methods by all tests
        Set<String> allCoveredMethods = new HashSet<>();
        for(Map.Entry<IUnit, Set<String>> entry: testAndCoveredMethods.entrySet()) {
            allCoveredMethods.addAll(entry.getValue());
        }
        int overallCoveredMethods = allCoveredMethods.size();

        // Afterwards we calculate the percentage of covered methods for all tests
        Set<Result> results = new HashSet<>();
        for (Map.Entry<IUnit, Set<String>> entry : testAndCoveredMethods.entrySet()) {
            int percent = (100 * entry.getValue().size() / overallCoveredMethods);
            results.add(new Result(entry.getKey().getFQN(), entry.getKey().getFilePath(), metricName,
                    String.valueOf(percent)));
        }
        return results;
    }
}
