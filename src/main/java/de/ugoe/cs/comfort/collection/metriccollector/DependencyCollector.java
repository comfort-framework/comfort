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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Fabian Trautsch
 */
public class DependencyCollector extends BaseMetricCollector {
    public DependencyCollector(GeneralConfiguration configuration) {
        super(configuration);
    }

    @SupportsJava
    @SupportsPython
    @SupportsMethod
    public Set<Result> getNumberOfDependentUnitsForMethod(CallGraph callGraph) {
        Map<IUnit, Set<IUnit>> callerCalleePairs = TestTypeDetectionUtils
                .getCallPairsOnClassLevel(callGraph);
        return generateResults(callerCalleePairs, "call_dep");
    }

    @SupportsJava
    @SupportsPython
    @SupportsClass
    public Set<Result> getNumberOfDependentUnitsForClass(DependencyGraph dependencyGraph) {
        Map<IUnit, Set<IUnit>> callerCalleePairs = TestTypeDetectionUtils
                .getCallPairsOnClassLevel(dependencyGraph);
        return generateResults(callerCalleePairs, "call_dep");
    }

    @SupportsJava
    @SupportsPython
    @SupportsMethod
    public Set<Result> getNumberOfDependentUnitsForCoverageData(CoverageData data) {
        return generateResults(data.getCoverageData(), "cov_dep");
    }

    private Set<Result> generateResults(Map<IUnit, Set<IUnit>> callerCalleePairs, String metricName) {
        Set<Result> results = new HashSet<>();
        for(Map.Entry<IUnit, Set<IUnit>> entry: callerCalleePairs.entrySet()) {
            Set<String> units = new HashSet<>();
            entry.getValue().forEach(unit -> {
                if(!unit.isTestBasedOnFQNofUnit()) {
                    units.add(unit.getFQNOfUnit());
                }
            });
            logger.debug("Dependency of {} is: {}", entry.getKey(), entry.getValue().size());
            results.add(new Result(entry.getKey().getFQN(), entry.getKey().getFilePath(),
                    metricName, String.valueOf(units.size())));
        }
        return results;
    }




}
