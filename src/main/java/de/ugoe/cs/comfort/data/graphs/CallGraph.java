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

package de.ugoe.cs.comfort.data.graphs;

import com.google.common.base.MoreObjects;
import com.google.common.graph.*;
import de.ugoe.cs.comfort.Utils;
import de.ugoe.cs.comfort.data.DataSet;
import de.ugoe.cs.comfort.data.models.IUnit;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.apache.commons.lang3.builder.HashCodeBuilder;


/**
 * @author Fabian Trautsch
 */
public class CallGraph extends DataSet implements MutableNetwork<IUnit, CallEdge>, IGraph {

    private final MutableNetwork<IUnit, CallEdge> graph;

    public CallGraph() {
        graph = NetworkBuilder.directed().allowsSelfLoops(true).allowsParallelEdges(true).build();
    }

    public CallGraph getCopyOfGraph() {
        CallGraph clonedGraph = new CallGraph();
        edges().forEach(clonedGraph::addEdge);
        return clonedGraph;
    }

    public Set<IUnit> getTestNodes() {
        Set<IUnit> testNodes = new HashSet<>();
        for(IUnit node: this.nodes()) {
            if(Utils.isTestBasedOnFQN(node.getFQN())) {
                testNodes.add(node);
            }
        }
        return testNodes;
    }

    public Set<IUnit> getProductionNodes() {
        Set<IUnit> productionNodes = new HashSet<>();
        for(IUnit node: this.nodes()) {
            if(!Utils.isTestBasedOnFQN(node.getFQNOfUnit())) {
                productionNodes.add(node);
            }
        }
        return productionNodes;
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean addNode(IUnit node) {
        return graph.addNode(node);
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean addEdge(IUnit nodeU, IUnit nodeV, CallEdge edge) {
        return graph.addEdge(nodeU, nodeV, edge);
    }

    public boolean addEdge(CallEdge edge) {
        return graph.addEdge(edge.getCaller(), edge.getCallee(), edge);
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean removeNode(IUnit node) {
        return graph.removeNode(node);
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean removeEdge(CallEdge edge) {
        return graph.removeEdge(edge);
    }

    @Override
    public Set<IUnit> nodes() {
        return graph.nodes();
    }

    @Override
    public Set<CallEdge> edges() {
        return graph.edges();
    }

    @Override
    public Graph<IUnit> asGraph() {
        return graph.asGraph();
    }

    @Override
    public boolean isDirected() {
        return graph.isDirected();
    }

    @Override
    public boolean allowsParallelEdges() {
        return graph.allowsParallelEdges();
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
    public ElementOrder<CallEdge> edgeOrder() {
        return graph.edgeOrder();
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
    public Set<CallEdge> incidentEdges(IUnit node) {
        return graph.incidentEdges(node);
    }

    @Override
    @ParametersAreNonnullByDefault
    public Set<CallEdge> inEdges(IUnit node) {
        return graph.inEdges(node);
    }

    @Override
    @ParametersAreNonnullByDefault
    public Set<CallEdge> outEdges(IUnit node) {
        return graph.outEdges(node);
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
    public EndpointPair<IUnit> incidentNodes(CallEdge edge) {
        return graph.incidentNodes(edge);
    }

    @Override
    @ParametersAreNonnullByDefault
    public Set<CallEdge> adjacentEdges(CallEdge edge) {
        return graph.adjacentEdges(edge);
    }

    @Override
    @ParametersAreNonnullByDefault
    public Set<CallEdge> edgesConnecting(IUnit nodeU, IUnit nodeV) {
        return graph.edgesConnecting(nodeU, nodeV);
    }

    @Override
    @ParametersAreNonnullByDefault
    public Optional<CallEdge> edgeConnecting(IUnit nodeU, IUnit nodeV) {
        return graph.edgeConnecting(nodeU, nodeV);
    }

    @Nullable
    @Override
    @ParametersAreNonnullByDefault
    public CallEdge edgeConnectingOrNull(IUnit nodeU, IUnit nodeV) {
        return graph.edgeConnectingOrNull(nodeU, nodeV);
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

        if (!(obj instanceof CallGraph)) {
            return false;
        }
        CallGraph other = (CallGraph) obj;

        return isDirected() == other.isDirected()
                && nodes().equals(other.nodes())
                && edges().equals(other.edges());
    }

    public void cleanGraphOfNodesThatAreSingle() {
        Set<IUnit> nodesToRemove = new HashSet<>();
        this.nodes().forEach(node -> {
            if(this.adjacentNodes(node).size() == 0) {
                nodesToRemove.add(node);
            }
        });
        nodesToRemove.forEach(this::removeNode);
    }

    public void printGraphToFileInDotFormat(Path outputPath) throws IOException{
        List<String> lines = new ArrayList<>();
        lines.add("digraph callgraph {");
        for(CallEdge edge: graph.edges()) {
            lines.add("\"" + edge.getCaller() + "\" -> \"" + edge.getCallee() + "\" [ label = \""
                    + edge.getOrderNumber() +"\" ];");
        }
        lines.add("}");
        Files.write(outputPath, lines);
    }


    public DependencyGraph getDependencyGraphRepresentation() {
        DependencyGraph dependencyGraph = new DependencyGraph();

        this.edges().forEach(
                callEdge -> {
                    try {
                        IUnit sourceNode = (IUnit) callEdge.getCaller().getClass().getSuperclass()
                                .getConstructor(String.class, Path.class).newInstance(
                                        callEdge.getCaller().getFQNOfUnit(), callEdge.getCaller().getFilePath());
                        IUnit targetNode = (IUnit) callEdge.getCallee().getClass().getSuperclass()
                                .getConstructor(String.class, Path.class).newInstance(
                                        callEdge.getCallee().getFQNOfUnit(), callEdge.getCallee().getFilePath());
                        dependencyGraph.putEdge(sourceNode, targetNode);
                    } catch (InstantiationException | IllegalAccessException | NoSuchMethodException
                        | InvocationTargetException e) {
                        e.printStackTrace();
                        System.exit(1);
                    }
                }
        );

        return dependencyGraph;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("Edges", edges())
                .add("Nodes", nodes())
                .toString();
    }
}
