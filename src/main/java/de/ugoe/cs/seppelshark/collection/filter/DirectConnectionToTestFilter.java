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

package de.ugoe.cs.seppelshark.collection.filter;

import com.google.common.graph.EndpointPair;
import de.ugoe.cs.seppelshark.annotations.SupportsJava;
import de.ugoe.cs.seppelshark.annotations.SupportsPython;
import de.ugoe.cs.seppelshark.configuration.GeneralConfiguration;
import de.ugoe.cs.seppelshark.data.graphs.CallEdge;
import de.ugoe.cs.seppelshark.data.graphs.CallGraph;
import de.ugoe.cs.seppelshark.data.graphs.DependencyGraph;
import de.ugoe.cs.seppelshark.data.models.IUnit;
import java.util.Set;

/**
 * @author Fabian Trautsch
 */
public class DirectConnectionToTestFilter extends BaseFilter {

    public DirectConnectionToTestFilter(GeneralConfiguration generalConfiguration) {
        super(generalConfiguration);
    }

    @SupportsJava
    @SupportsPython
    public CallGraph filter(CallGraph callGraph) {
        CallGraph filteredCallGraph = callGraph.getCopyOfGraph();

        // If the source is not a test -> delete the edge, because we only want to have the direct connections to tests
        Set<CallEdge> edgesToIterateOver = callGraph.edges();
        edgesToIterateOver.forEach(edge -> {
            if(!edge.getCaller().isTestBasedOnFQNofUnit()) {
                filteredCallGraph.removeEdge(edge);
            }
        });

        filteredCallGraph.cleanGraphOfNodesThatAreSingle();

        return filteredCallGraph;
    }

    @SupportsJava
    @SupportsPython
    public DependencyGraph filter(DependencyGraph dependencyGraph) {
        DependencyGraph filteredDependencyGraph = dependencyGraph.getCopyOfGraph();

        // If the source is not a test -> delete the edge, because we only want to have the direct connections to tests
        Set<EndpointPair<IUnit>> edgesToIterateOver = dependencyGraph.edges();
        edgesToIterateOver.forEach(edge -> {
            if(!edge.source().isTestBasedOnFQNofUnit()) {
                filteredDependencyGraph.removeEdge(edge.source(), edge.target());
            }
        });

        filteredDependencyGraph.cleanGraphOfNodesThatAreSingle();

        return filteredDependencyGraph;
    }
}
