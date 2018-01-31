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

import static java.lang.Math.max;

import de.ugoe.cs.comfort.Utils;
import de.ugoe.cs.comfort.annotations.SupportsClass;
import de.ugoe.cs.comfort.annotations.SupportsJava;
import de.ugoe.cs.comfort.annotations.SupportsMethod;
import de.ugoe.cs.comfort.annotations.SupportsPython;
import de.ugoe.cs.comfort.configuration.GeneralConfiguration;
import de.ugoe.cs.comfort.data.graphs.CallGraph;
import de.ugoe.cs.comfort.data.models.IUnit;
import de.ugoe.cs.comfort.filer.models.Result;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Fabian Trautsch
 */

public class MaximumCallGraphDepthCollector extends BaseMetricCollector {
    public MaximumCallGraphDepthCollector(GeneralConfiguration configuration) {
        super(configuration);
    }

    @SupportsJava
    @SupportsPython
    @SupportsMethod
    public Set<Result> getMaximumDepthForJavaOnMethodLevel(CallGraph callGraph) {
        Map<String, Integer> longestPathForTest = getLongestPathToNodes(callGraph);
        return generateResults(longestPathForTest);
    }

    @SupportsJava
    @SupportsPython
    @SupportsClass
    public Set<Result> getMaximumDepthForJavaOnClassLevel(CallGraph callGraph) {
        Map<String, Integer> longestPathForTest = getLongestPathToNodes(callGraph);

        Map<String, Integer> classResults = new HashMap<>();
        for(Map.Entry<String, Integer> entry : longestPathForTest.entrySet()) {
            int maximumDepth = classResults.getOrDefault(Utils.getFQNWithoutMethod(entry.getKey()), 0);
            classResults.put(Utils.getFQNWithoutMethod(entry.getKey()), max(maximumDepth, entry.getValue()));
        }
        logger.debug("Generated the following results for class level {}", classResults);

        return generateResults(classResults);
    }

    private Map<String, Integer> getLongestPathToNodes(CallGraph callGraph) {
        Map<String, Integer> longestPathForTest = new HashMap<>();

        // We do concurrency here, as it is much faster for this heavy task
        final ExecutorService executorService = Executors.newFixedThreadPool(4);
        CompletionService<Map<String, Integer>> pool = new ExecutorCompletionService<>(executorService);


        for(IUnit node: callGraph.getTestNodes()) {
            pool.submit(new LongestPathCalculator(node, callGraph));
        }

        for(IUnit node: callGraph.getTestNodes()) {
            try {
                Map<String, Integer> result = pool.take().get();
                longestPathForTest.putAll(result);
            } catch (InterruptedException | ExecutionException e) {
                logger.catching(e);
            }
        }
        executorService.shutdown();

        return longestPathForTest;
    }

    private Set<Result> generateResults(Map<String, Integer> longesPathToNodes) {
        Set<Result> results = new HashSet<>();
        for(Map.Entry<String, Integer> entry: longesPathToNodes.entrySet()) {
            try {
                results.add(new Result(entry.getKey(),
                        fileNameUtils.getPathForIdentifier(entry.getKey(), generalConf.getMethodLevel()),
                        "call_path", String.valueOf(entry.getValue())));
            } catch (FileNotFoundException e) {
                logger.warn("Could not find Path for node {}...", entry.getKey());
            }
        }
        return results;
    }



    private class LongestPathCalculator implements Callable<Map<String, Integer>> {
        private IUnit node;
        private DijkstraAlgorithm dijkstraAlgorithm;
        private CallGraph callGraph;

        public LongestPathCalculator(IUnit node, CallGraph callGraph) {
            this.node = node;
            this.dijkstraAlgorithm = new DijkstraAlgorithm(callGraph);
            this.callGraph = callGraph;
        }

        @Override
        public Map<String, Integer> call() throws Exception {
            // Get depth
            dijkstraAlgorithm.execute(node);
            int maximumPath = 0;
            for(IUnit productionNode: callGraph.getProductionNodes()) {
                LinkedList<IUnit> shortestPathToNode = dijkstraAlgorithm.getPath(productionNode);
                if(shortestPathToNode != null) {
                    maximumPath = max(maximumPath, shortestPathToNode.size()-1);
                }
            }
            logger.debug("Node {} has the maximum path of {}", node.getFQN(), maximumPath);
            Map<String, Integer> callResult = new HashMap<>(1);
            callResult.put(node.getFQN(), maximumPath);
            return callResult;
        }
    }
}
