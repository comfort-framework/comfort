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
import de.ugoe.cs.comfort.data.graphs.IGraph;
import de.ugoe.cs.comfort.data.models.IUnit;
import de.ugoe.cs.comfort.filer.BaseFiler;
import de.ugoe.cs.comfort.filer.models.Result;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Fabian Trautsch
 */
public class ISTQBTestTypeCollector extends BaseMetricCollector {

    public ISTQBTestTypeCollector(GeneralConfiguration configuration, BaseFiler filer) {
        super(configuration, filer);
    }

    @SupportsMethod
    @SupportsJava
    @SupportsPython
    public void createResultsJavaMethodLevelCallGraph(CallGraph graph) throws IOException {
        filer.storeResults(classifyUsingGraph(graph, "call_istqb_met"));
    }

    @SupportsClass
    @SupportsJava
    @SupportsPython
    public void createResultsJavaClassLevelCallGraph(CallGraph callGraph) throws IOException {
        logger.warn("Using depedency graph representation of call graph for strategy {}", this.getClass().getName());
        filer.storeResults(classifyUsingGraph(callGraph.getDependencyGraphRepresentation(), "call_istqb"));
    }

    @SupportsPython
    @SupportsJava
    @SupportsClass
    public void createResultsJavaPythonClassDepGraph(DependencyGraph dependencyGraph) throws IOException {
        filer.storeResults(classifyUsingGraph(dependencyGraph, "dep_istqb"));
    }

    @SupportsPython
    @SupportsJava
    @SupportsClass
    public void createResultsJavaPythonClassCoverage(CoverageData dataSet) throws IOException {
        Map<IUnit, Integer> unitAndAmountOfPackagesTested = getUniqueTestedClasses(dataSet.getCoverageDataClassLevel());

        // Merge results from different methods on class level
        filer.storeResults(TestTypeDetectionUtils.generateResults(generalConf, unitAndAmountOfPackagesTested,
                generalConf.getMethodLevel(), "cov_istqb"));
    }

    @SupportsPython
    @SupportsJava
    @SupportsMethod
    public void createResultsJavaPythonMethodCoverage(CoverageData dataSet) throws IOException {
        Map<IUnit, Integer> unitAndAmountOfPackagesTested = getUniqueTestedClasses(dataSet.getCoverageData());

        // Merge results from different methods on class level
        filer.storeResults(TestTypeDetectionUtils.generateResults(generalConf, unitAndAmountOfPackagesTested,
                generalConf.getMethodLevel(), "cov_istqb_met"));
    }

    private Map<IUnit, Integer> getUniqueTestedClasses(Map<IUnit, Set<IUnit>> callerCalleePairs) {
        Map<IUnit, Integer> testAndAmountOfClassesTested = new HashMap<>();

        for (Map.Entry<IUnit, Set<IUnit>> entry : callerCalleePairs.entrySet()) {
            Set<String> classes = new HashSet<>();
            entry.getValue().forEach(iUnit -> classes.add(iUnit.getFQNOfUnit()));
            testAndAmountOfClassesTested.put(entry.getKey(), classes.size());
        }

        return testAndAmountOfClassesTested;
    }

    private Set<Result> classifyUsingGraph(IGraph graph, String metricName) {
        Map<IUnit, Set<IUnit>> callerCalleePairs =
                TestTypeDetectionUtils.getCallPairsOnClassLevel(graph);
        Map<IUnit, Integer> testAndAmountOfUnitsTested = new HashMap<>();
        callerCalleePairs.forEach((caller, callees) -> testAndAmountOfUnitsTested.put(caller, callees.size()));

        return TestTypeDetectionUtils.generateResults(generalConf, testAndAmountOfUnitsTested,
                generalConf.getMethodLevel(), metricName);
    }


}
