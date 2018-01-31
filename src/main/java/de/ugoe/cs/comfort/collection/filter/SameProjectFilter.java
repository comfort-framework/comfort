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

package de.ugoe.cs.comfort.collection.filter;

import de.ugoe.cs.comfort.annotations.SupportsJava;
import de.ugoe.cs.comfort.annotations.SupportsPython;
import de.ugoe.cs.comfort.configuration.GeneralConfiguration;
import de.ugoe.cs.comfort.data.graphs.CallGraph;
import de.ugoe.cs.comfort.data.graphs.DependencyGraph;

/**
 * @author Fabian Trautsch
 */
public class SameProjectFilter extends BaseFilter {

    public SameProjectFilter(GeneralConfiguration generalConfiguration) {
        super(generalConfiguration);
    }

    @SupportsJava
    @SupportsPython
    public DependencyGraph filterDependencyGraph(DependencyGraph dependencyGraph) {
        DependencyGraph filteredDepedencyGraph = dependencyGraph.getCopyOfGraph();

        // Go through all nodes and check if the node has a file path, if not it is not project related
        dependencyGraph.nodes().forEach(node -> {
            if(node.getFilePath() == null) {
                logger.debug("Removing the following node: {}", node);
                filteredDepedencyGraph.removeNode(node);
            }
        });

        filteredDepedencyGraph.cleanGraphOfNodesThatAreSingle();

        return filteredDepedencyGraph;
    }

    @SupportsJava
    @SupportsPython
    public CallGraph filterCallGraph(CallGraph callGraph) {
        CallGraph filteredCallGraph = callGraph.getCopyOfGraph();

        // Go through all nodes and check if the class can be found in the project folder
        callGraph.nodes().forEach(node -> {
            if(node.getFilePath() == null) {
                logger.debug("Removing the node: {}", node);
                filteredCallGraph.removeNode(node);
            }
        });

        // Remove all nodes that do not have any edges left
        filteredCallGraph.cleanGraphOfNodesThatAreSingle();

        return filteredCallGraph;
    }
}
