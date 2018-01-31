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

import de.ugoe.cs.seppelshark.Utils;
import de.ugoe.cs.seppelshark.annotations.SupportsClass;
import de.ugoe.cs.seppelshark.annotations.SupportsJava;
import de.ugoe.cs.seppelshark.annotations.SupportsMethod;
import de.ugoe.cs.seppelshark.annotations.SupportsPython;
import de.ugoe.cs.seppelshark.configuration.GeneralConfiguration;
import de.ugoe.cs.seppelshark.data.graphs.CallGraph;
import de.ugoe.cs.seppelshark.data.graphs.DependencyGraph;
import de.ugoe.cs.seppelshark.data.models.IUnit;
import de.ugoe.cs.seppelshark.filer.models.Result;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Fabian Trautsch
 */
public class DirectnessCollector extends BaseMetricCollector {
    public DirectnessCollector(GeneralConfiguration configuration) {
        super(configuration);
    }

    @SupportsJava
    @SupportsPython
    @SupportsMethod
    public Set<Result> createDirectnessMetricForJavaOnMethodLevel(CallGraph callGraph) {
        Map<IUnit, Set<String>> numDirectConnections = new HashMap<>();
        Set<String> units = new HashSet<>();
        callGraph.getProductionNodes().forEach(x -> units.add(x.getFQNOfUnit()));

        for(IUnit node: callGraph.getTestNodes()) {
            Set<String> adjacentUnits = new HashSet<>();
            callGraph.adjacentNodes(node).forEach(x -> {
                if(!Utils.isTestBasedOnFQN(x.getFQN())) {
                    adjacentUnits.add(x.getFQNOfUnit());
                }
            });
            numDirectConnections.put(node, adjacentUnits);
        }
        return getResults(numDirectConnections, units.size());
    }

    @SupportsJava
    @SupportsPython
    @SupportsClass
    public Set<Result> createDirectnessMetricForJavaOnClassLevel(CallGraph callGraph) {
        // As we are only working on class level, we are using the dependencygraph here
        DependencyGraph depGraph = callGraph.getDependencyGraphRepresentation();

        Map<IUnit, Set<String>> numDirectConnections = new HashMap<>();
        Set<String> units = new HashSet<>();
        callGraph.getProductionNodes().forEach(x -> units.add(x.getFQNOfUnit()));

        for(IUnit node: depGraph.getTestNodes()) {
            Set<String> adjacentUnits = new HashSet<>();
            depGraph.adjacentNodes(node).forEach(x -> {
                if(!Utils.isTestBasedOnFQN(x.getFQN())) {
                    adjacentUnits.add(x.getFQNOfUnit());
                }
            });
            Set<String> connectionsToClass = numDirectConnections.getOrDefault(node, new HashSet<>());
            connectionsToClass.addAll(adjacentUnits);
            numDirectConnections.put(node, connectionsToClass);
        }
        return getResults(numDirectConnections, units.size());
    }

    private Set<Result> getResults(Map<IUnit, Set<String>> numDirectConnectionsPerTest, Integer numAllUnits) {
        logger.debug("Got the following connections: {}", numDirectConnectionsPerTest);

        Set<Result> results = new HashSet<>();
        DecimalFormat decimalFormat = new DecimalFormat("#0.0000");
        for(Map.Entry<IUnit, Set<String>> entry: numDirectConnectionsPerTest.entrySet()) {
            double percentDirectlyCovered = ((double) entry.getValue().size()*100) / (double) numAllUnits;
            results.add(new Result(entry.getKey().getFQN(), entry.getKey().getFilePath(),
                    "call_dire", decimalFormat.format(percentDirectlyCovered)));
        }

        return results;
    }

}
