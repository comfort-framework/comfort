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

package de.ugoe.cs.seppelshark.data.graphs;

import com.google.common.base.MoreObjects;
import com.google.common.graph.ElementOrder;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import de.ugoe.cs.seppelshark.Utils;
import de.ugoe.cs.seppelshark.data.DataSet;
import de.ugoe.cs.seppelshark.data.models.IUnit;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.ParametersAreNonnullByDefault;
import org.apache.commons.lang3.builder.HashCodeBuilder;




/**
 * @author Fabian Trautsch
 */
public class DependencyGraph extends DataSet implements MutableGraph<IUnit>, IGraph {
    private final MutableGraph<IUnit> graph;

    public DependencyGraph() {
        graph = GraphBuilder.directed().allowsSelfLoops(true).build();
    }

    public DependencyGraph getCopyOfGraph() {
        DependencyGraph clonedGraph = new DependencyGraph();
        edges().forEach(
                edge -> clonedGraph.putEdge(edge.source(), edge.target())
        );
        return clonedGraph;
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean addNode(IUnit node) {
        return graph.addNode(node);
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean putEdge(IUnit nodeU, IUnit nodeV) {
        return graph.putEdge(nodeU, nodeV);
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean removeNode(IUnit node) {
        return graph.removeNode(node);
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean removeEdge(IUnit nodeU, IUnit nodeV) {
        return graph.removeEdge(nodeU, nodeV);
    }

    @Override
    public Set<IUnit> nodes() {
        return graph.nodes();
    }

    @Override
    public Set<EndpointPair<IUnit>> edges() {
        return graph.edges();
    }

    @Override
    public boolean isDirected() {
        return graph.isDirected();
    }

    @Override
    public boolean allowsSelfLoops() {
        return graph.allowsSelfLoops();
    }

    @Override
    public ElementOrder<IUnit> nodeOrder() {
        return graph.nodeOrder();
    }

    @Override
    @ParametersAreNonnullByDefault
    public Set<IUnit> adjacentNodes(IUnit node) {
        return graph.adjacentNodes(node);
    }

    @Override
    @ParametersAreNonnullByDefault
    public Set<IUnit> predecessors(IUnit node) {
        return graph.predecessors(node);
    }

    @Override
    @ParametersAreNonnullByDefault
    public Set<IUnit> successors(IUnit node) {
        return graph.successors(node);
    }

    @Override
    @ParametersAreNonnullByDefault
    public int degree(IUnit node) {
        return graph.degree(node);
    }

    @Override
    @ParametersAreNonnullByDefault
    public int inDegree(IUnit node) {
        return graph.inDegree(node);
    }

    @Override
    @ParametersAreNonnullByDefault
    public int outDegree(IUnit node) {
        return graph.outDegree(node);
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean hasEdgeConnecting(IUnit nodeU, IUnit nodeV) {
        return graph.hasEdgeConnecting(nodeU, nodeV);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31)
                .append(graph)
                .toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this) {
            return true;
        }

        if (!(obj instanceof DependencyGraph)) {
            return false;
        }
        DependencyGraph other = (DependencyGraph) obj;

        return isDirected() == other.isDirected()
                && nodes().equals(other.nodes())
                && edges().equals(other.edges());
    }

    public Set<IUnit> getTestNodes() {
        Set<IUnit> testNodes = new HashSet<>();
        for(IUnit node: this.nodes()) {
            if(Utils.isTestBasedOnFQN(node.getFQNOfUnit())) {
                testNodes.add(node);
            }
        }
        return testNodes;
    }

    public void cleanGraphOfNodesThatAreSingle() {
        // Go through all nodes and check if they have any connections left. If not -> put them in a list to remove
        Set<IUnit> nodesToRemove = new HashSet<>();
        this.nodes().forEach(node -> {
            if(this.adjacentNodes(node).size() == 0) {
                nodesToRemove.add(node);
            }
        });

        nodesToRemove.forEach(this::removeNode);
    }


    public void printToFileInDotFormat(Path outputPath) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add("digraph graph {");
        for(EndpointPair<IUnit> edge: graph.edges()) {
            lines.add("\"" + edge.source() + "\" -> \"" + edge.target() + "\";");
        }
        lines.add("}");
        Files.write(outputPath, lines);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("Edges", edges())
                .add("Nodes", nodes())
                .toString();
    }
}
