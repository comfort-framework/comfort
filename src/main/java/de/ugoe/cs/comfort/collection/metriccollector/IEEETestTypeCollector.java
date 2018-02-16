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
public class IEEETestTypeCollector extends BaseMetricCollector {

    public IEEETestTypeCollector(GeneralConfiguration configuration, BaseFiler filer) {
        super(configuration, filer);
    }

    @SupportsClass
    @SupportsJava
    @SupportsPython
    public void createResultsJavaPythonClassLevelCallGraph(CallGraph callGraph) throws IOException {
        filer.storeResults(classifyUsingGraph(callGraph.getDependencyGraphRepresentation(), "call_ieee"));
    }



    @SupportsMethod
    @SupportsJava
    @SupportsPython
    public void createResultsJavaPythonMethodLevelCallGraph(CallGraph graph) throws IOException {
        filer.storeResults(classifyUsingGraph(graph, "call_ieee_met"));
    }


    @SupportsJava
    @SupportsPython
    @SupportsClass
    public void createResultsJavaPythonDepGraph(DependencyGraph dependencyGraph) throws IOException {
        filer.storeResults(classifyUsingGraph(dependencyGraph, "dep_ieee"));
    }


    @SupportsJava
    @SupportsPython
    @SupportsClass
    public void createResultsJavaPythonCoverageClass(CoverageData dataSet) throws IOException {
        Map<IUnit, Integer> unitAndAmountOfPackagesTested =
                getUniqueTestedPackages(dataSet.getCoverageDataClassLevel());

        // Merge results from different methods on class level
        filer.storeResults(TestTypeDetectionUtils.generateResults(generalConf, unitAndAmountOfPackagesTested,
                generalConf.getMethodLevel(), "cov_ieee"));
    }


    @SupportsJava
    @SupportsPython
    @SupportsMethod
    public void createResultsJavaPythonCoverageMethod(CoverageData dataSet) throws IOException {
        Map<IUnit, Integer> unitAndAmountOfPackagesTested = getUniqueTestedPackages(dataSet.getCoverageData());

        // Merge results from different methods on class level
        filer.storeResults(TestTypeDetectionUtils.generateResults(generalConf, unitAndAmountOfPackagesTested,
                generalConf.getMethodLevel(), "cov_ieee_met"));
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
