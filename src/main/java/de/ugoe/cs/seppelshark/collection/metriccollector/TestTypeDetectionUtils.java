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

import de.ugoe.cs.seppelshark.configuration.GeneralConfiguration;
import de.ugoe.cs.seppelshark.data.graphs.IGraph;
import de.ugoe.cs.seppelshark.data.models.IUnit;
import de.ugoe.cs.seppelshark.filer.models.Result;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * @author Fabian Trautsch
 */
class TestTypeDetectionUtils {
    private static final Logger LOGGER = LogManager.getLogger(TestTypeDetectionUtils.class.getName());

    static Map<IUnit, Set<IUnit>> getCallPairsOnClassLevel(IGraph graph) {
        LOGGER.info("Getting caller/callee pairs on class level...");

        Map<IUnit, Set<IUnit>> classesWithItsCallees = new HashMap<>();
        for(IUnit classNode: graph.nodes()) {
            String fqnOfClass = classNode.getFQNOfUnit();

            // Only check tests
            if(classNode.isTestBasedOnFQNofUnit()) {
                LOGGER.debug("Looking at test {}", classNode);

                // Only visit every node once
                Set<IUnit> visitedNodes = new HashSet<>();

                // Traverse Graph from this node and store all visited nodes in the set
                traverseGraphFromNode(graph, classNode, visitedNodes);

                // Remove the test node (as it do not have a dependency on itself)
                visitedNodes.remove(classNode);

                // Get all callees from the class that we are looking at and add the calls from the method of this
                // class to the set of callees
                Set<IUnit> callees = classesWithItsCallees.containsKey(classNode)
                        ? classesWithItsCallees.get(classNode) : new HashSet<>();
                callees.addAll(visitedNodes);
                classesWithItsCallees.put(classNode, callees);

                LOGGER.debug("It has the following nodes as dependencies: {}", visitedNodes);
            }
        }
        return classesWithItsCallees;
    }

    static Set<Result> generateResults(GeneralConfiguration configuration, Map<IUnit, Integer> testAndUniqueUnitsTested,
                                       Boolean executeOnMethodLevel, String metricName) {
        // Create our filer set
        Set<Result> results = new HashSet<>();

        // Go through each entry of the caller-callee-pairs
        for (Map.Entry<IUnit, Integer> entry : testAndUniqueUnitsTested.entrySet()) {
            Result result = new Result(entry.getKey().getFQN(), entry.getKey().getFilePath());

            // If the test only has one dependency -> its an unit test
            // If it has 0 dependencies -> its unknown
            // Otherwise -> integration test
            switch (entry.getValue()) {
                case 0:
                    result.addMetric(metricName, TestType.UNKNOWN.name());
                    break;
                case 1:
                    result.addMetric(metricName, TestType.UNIT.name());
                    break;
                default:
                    result.addMetric(metricName, TestType.INTEGRATION.name());
            }

            results.add(result);
        }

        return results;
    }

    /*
    static Map<String, Integer> getTestGranularitiesForTestsOnMethodLevel(CallGraph callGraph) {
        Map<String, Set<IMethod>> visitedNodesPerTestClass = new HashMap<>();
        Map<String, Integer> testGranularities = new HashMap<>();

        for(IMethod node: callGraph.nodes()) {
            if(Utils.isTestBasedOnName(node.getFQNOfUnit())) {
                // Only class name of test without method
                String nodeNameWithoutMethod = node.getFQNOfUnit();

                // Only visit every node once
                Set<IMethod> visitedNodes = new HashSet<>();

                // Traverse Graph
                traverseCallGraphFromNode(callGraph, node, visitedNodes);

                // Add all visited nodes to the set of the test class
                visitedNodes.remove(node);
                Set<IMethod> existingNodes = visitedNodesPerTestClass.containsKey(nodeNameWithoutMethod) ?
                        visitedNodesPerTestClass.get(nodeNameWithoutMethod) : new HashSet<>();
                existingNodes.addAll(visitedNodes);
                visitedNodesPerTestClass.put(nodeNameWithoutMethod, existingNodes);

                // Put results in (-1, because the test node itself is in the map)
                testGranularities.put(nodeNameWithoutMethod,
                visitedNodesPerTestClass.get(nodeNameWithoutMethod).size());
            }
        }
        for(Map.Entry<String, Set<IMethod>> testClass: visitedNodesPerTestClass.entrySet()) {
            LOGGER.debug("Test {} has the following visited nodes:", testClass.getKey());
            for(IMethod node: testClass.getValue()) {
                LOGGER.debug("Node: {}", node);
            }
        }
        LOGGER.debug("Got the following test granularities: {}", testGranularities);
        return testGranularities;
    }
    */

    private static void traverseGraphFromNode(IGraph graph, IUnit node, Set<IUnit> visitedNodes) {
        visitedNodes.add(node);

        if(graph.successors(node).size() == 0) {
            return;
        }

        for(IUnit successorNode: graph.successors(node)) {
            if(!visitedNodes.contains(successorNode)) {
                traverseGraphFromNode(graph, successorNode, visitedNodes);
            }
        }
    }
}
