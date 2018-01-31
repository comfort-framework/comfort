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

import de.ugoe.cs.comfort.data.graphs.CallEdge;
import de.ugoe.cs.comfort.data.graphs.CallGraph;
import de.ugoe.cs.comfort.data.models.IUnit;
import java.util.*;


/**
 * Implementation of Dijkstra Algorithm. Based on:
 * http://www.vogella.com/tutorials/JavaAlgorithmsDijkstra/article.html
 * @author Fabian Trautsch
 */
public class DijkstraAlgorithm {

    private final List<IUnit> nodes;
    private final List<CallEdge> edges;
    private Set<IUnit> settledNodes;
    private Set<IUnit> unSettledNodes;
    private Map<IUnit, IUnit> predecessors;
    private Map<IUnit, Integer> distance;

    public DijkstraAlgorithm(CallGraph graph) {
        // create a copy of the array so that we can operate on this array
        this.nodes = new ArrayList<IUnit>(graph.nodes());
        this.edges = new ArrayList<CallEdge>(graph.edges());
    }

    public void execute(IUnit source) {
        settledNodes = new HashSet<IUnit>();
        unSettledNodes = new HashSet<IUnit>();
        distance = new HashMap<IUnit, Integer>();
        predecessors = new HashMap<IUnit, IUnit>();
        distance.put(source, 0);
        unSettledNodes.add(source);
        while (unSettledNodes.size() > 0) {
            IUnit node = getMinimum(unSettledNodes);
            settledNodes.add(node);
            unSettledNodes.remove(node);
            findMinimalDistances(node);
        }
    }

    private void findMinimalDistances(IUnit node) {
        List<IUnit> adjacentNodes = getNeighbors(node);
        for (IUnit target : adjacentNodes) {
            if (getShortestDistance(target) > getShortestDistance(node)
                    + getDistance(node, target)) {
                distance.put(target, getShortestDistance(node)
                        + getDistance(node, target));
                predecessors.put(target, node);
                unSettledNodes.add(target);
            }
        }

    }

    private int getDistance(IUnit node, IUnit target) {
        for (CallEdge edge : edges) {
            if (edge.getCaller().equals(node)
                    && edge.getCallee().equals(target)) {
                return 1;
            }
        }
        throw new RuntimeException("Should not happen");
    }

    private List<IUnit> getNeighbors(IUnit node) {
        List<IUnit> neighbors = new ArrayList<IUnit>();
        for (CallEdge edge : edges) {
            if (edge.getCaller().equals(node)
                    && !isSettled(edge.getCallee())) {
                neighbors.add(edge.getCallee());
            }
        }
        return neighbors;
    }

    private IUnit getMinimum(Set<IUnit> vertexes) {
        IUnit minimum = null;
        for (IUnit vertex : vertexes) {
            if (minimum == null) {
                minimum = vertex;
            } else {
                if (getShortestDistance(vertex) < getShortestDistance(minimum)) {
                    minimum = vertex;
                }
            }
        }
        return minimum;
    }

    private boolean isSettled(IUnit vertex) {
        return settledNodes.contains(vertex);
    }

    private int getShortestDistance(IUnit destination) {
        Integer d = distance.get(destination);
        if (d == null) {
            return Integer.MAX_VALUE;
        } else {
            return d;
        }
    }

    /*
     * This method returns the path from the source to the selected target and
     * NULL if no path exists
     */
    public LinkedList<IUnit> getPath(IUnit target) {
        LinkedList<IUnit> path = new LinkedList<IUnit>();
        IUnit step = target;
        // check if a path exists
        if (predecessors.get(step) == null) {
            return null;
        }
        path.add(step);
        while (predecessors.get(step) != null) {
            step = predecessors.get(step);
            path.add(step);
        }
        // Put it into the correct order
        Collections.reverse(path);
        return path;
    }

}
