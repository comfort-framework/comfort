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

import de.ugoe.cs.seppelshark.annotations.SupportsClass;
import de.ugoe.cs.seppelshark.annotations.SupportsJava;
import de.ugoe.cs.seppelshark.annotations.SupportsMethod;
import de.ugoe.cs.seppelshark.annotations.SupportsPython;
import de.ugoe.cs.seppelshark.configuration.GeneralConfiguration;
import de.ugoe.cs.seppelshark.data.CoverageData;
import de.ugoe.cs.seppelshark.data.graphs.CallGraph;
import de.ugoe.cs.seppelshark.data.graphs.DependencyGraph;
import de.ugoe.cs.seppelshark.data.graphs.IGraph;
import de.ugoe.cs.seppelshark.data.models.IUnit;
import de.ugoe.cs.seppelshark.filer.models.Result;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Fabian Trautsch
 */
public class IEEETestTypeCollector extends BaseMetricCollector {

    public IEEETestTypeCollector(GeneralConfiguration configuration) {
        super(configuration);
    }

    @SupportsClass
    @SupportsJava
    @SupportsPython
    public Set<Result> createResultsJavaPythonClassLevelCallGraph(CallGraph callGraph) {
        return classifyUsingGraph(callGraph.getDependencyGraphRepresentation(), "call_ieee");
    }



    @SupportsMethod
    @SupportsJava
    @SupportsPython
    public Set<Result> createResultsJavaPythonMethodLevelCallGraph(CallGraph graph) {
        return classifyUsingGraph(graph, "call_ieee_met");
    }


    @SupportsJava
    @SupportsPython
    @SupportsClass
    public Set<Result> createResultsJavaPythonDepGraph(DependencyGraph dependencyGraph) {
        return classifyUsingGraph(dependencyGraph, "dep_ieee");
    }


    @SupportsJava
    @SupportsPython
    @SupportsClass
    public Set<Result> createResultsJavaPythonCoverageClass(CoverageData dataSet) {
        Map<IUnit, Integer> unitAndAmountOfPackagesTested =
                getUniqueTestedPackages(dataSet.getCoverageDataClassLevel());

        // Merge results from different methods on class level
        return TestTypeDetectionUtils.generateResults(generalConf, unitAndAmountOfPackagesTested,
                generalConf.getMethodLevel(), "cov_ieee");
    }


    @SupportsJava
    @SupportsPython
    @SupportsMethod
    public Set<Result> createResultsJavaPythonCoverageMethod(CoverageData dataSet) {
        Map<IUnit, Integer> unitAndAmountOfPackagesTested = getUniqueTestedPackages(dataSet.getCoverageData());

        // Merge results from different methods on class level
        return TestTypeDetectionUtils.generateResults(generalConf, unitAndAmountOfPackagesTested,
                generalConf.getMethodLevel(), "cov_ieee_met");
    }




    private Set<Result> classifyUsingGraph(IGraph graph, String metricName) {
        Map<IUnit, Set<IUnit>> callerCalleePairs =
                TestTypeDetectionUtils.getCallPairsOnClassLevel(graph);
        Map<IUnit, Integer> testAndAmountOfPackagesTested = getUniqueTestedPackages(callerCalleePairs);

        return TestTypeDetectionUtils.generateResults(generalConf, testAndAmountOfPackagesTested,
                generalConf.getMethodLevel(), metricName);
    }


    private Map<IUnit, Integer> getUniqueTestedPackages(Map<IUnit, Set<IUnit>> callerCalleePairs) {
        Map<IUnit, Integer> testAndAmountOfPackagesTested = new HashMap<>();

        for (Map.Entry<IUnit, Set<IUnit>> entry : callerCalleePairs.entrySet()) {
            Set<String> packages = new HashSet<>();
            entry.getValue().forEach(iUnit -> packages.add(iUnit.getPackage()));
            testAndAmountOfPackagesTested.put(entry.getKey(), packages.size());
        }

        return testAndAmountOfPackagesTested;
    }

}
