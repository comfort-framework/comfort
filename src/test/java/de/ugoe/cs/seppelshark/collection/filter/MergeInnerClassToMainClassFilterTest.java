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

import de.ugoe.cs.seppelshark.BaseTest;
import de.ugoe.cs.seppelshark.configuration.GeneralConfiguration;
import de.ugoe.cs.seppelshark.data.graphs.DependencyGraph;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Fabian Trautsch
 */
public class MergeInnerClassToMainClassFilterTest extends BaseTest{

    private GeneralConfiguration configuration = new GeneralConfiguration();

    @Before
    public void setUpConfiguration() {
        configuration.setLanguage("java");
    }

    private DependencyGraph filterDependencyGraph(DependencyGraph dependencyGraph) {
        MergeInnerClassToMainClassFilter filter = new MergeInnerClassToMainClassFilter(configuration);
        return filter.filter(dependencyGraph);
    }

    @Test
    public void filterGraphTest() {
        /*
         * Graph:
         * C1 -> C2
         * C1$1 -> C3
         * C1$subclass$subclass1 -> C4
         *
         * expected filer: C1 -> C2, C1 -> C3, C1 -> C4
         */
        DependencyGraph dependencyGraph = new DependencyGraph();
        dependencyGraph.putEdge(C1, C2);
        dependencyGraph.putEdge(C11, C3);
        dependencyGraph.putEdge(C1subclass, C4);

        // Apply filter
        DependencyGraph filteredGraph = filterDependencyGraph(dependencyGraph);

        // Expected Graph
        DependencyGraph expectedDependencyGraph = new DependencyGraph();
        expectedDependencyGraph.putEdge(C1, C2);
        expectedDependencyGraph.putEdge(C1, C3);
        expectedDependencyGraph.putEdge(C1, C4);
        assertEquals("Graphs not equal!", expectedDependencyGraph, filteredGraph);

    }

    @Test
    public void filterGraphWithMultipleEdgesToSameNodeTest() {
        /*
         * Graph:
         * org.foo.C1 -> org.foo.C2
         * org.foo.C1$1 -> org.foo.C2
         *
         * expected filer: org.foo.C1 -> org.foo.C2
         */
        DependencyGraph dependencyGraph = new DependencyGraph();
        dependencyGraph.putEdge(C1, C2);
        dependencyGraph.putEdge(C11, C2);

        // Apply filter
        DependencyGraph filteredGraph = filterDependencyGraph(dependencyGraph);

        // Expected Graph
        DependencyGraph expectedDependencyGraph = new DependencyGraph();
        expectedDependencyGraph.putEdge(C1, C2);
        assertEquals("Graphs not equal!", expectedDependencyGraph, filteredGraph);

    }

    @Test
    public void filterGraphWithTargetIsInnerClassTest() {
        /*
         * Graph:
         * org.foo.C1 -> org.foo.C2$1
         * org.foo.C2$1 -> org.foo.C3
         * org.foo.C2 -> org.foo.C1
         *
         * expected filer: org.foo.C1 -> org.foo.C2, org.foo.C2 -> org.foo.C3, org.foo.C2 -> org.foo.C1
         */
        DependencyGraph dependencyGraph = new DependencyGraph();
        dependencyGraph.putEdge(C1, C21);
        dependencyGraph.putEdge(C21, C3);
        dependencyGraph.putEdge(C2, C1);

        // Apply filter
        DependencyGraph filteredGraph = filterDependencyGraph(dependencyGraph);

        // Expected Graph
        DependencyGraph expectedDependencyGraph = new DependencyGraph();
        expectedDependencyGraph.putEdge(C1, C2);
        expectedDependencyGraph.putEdge(C2, C3);
        expectedDependencyGraph.putEdge(C2, C1);
        assertEquals("Graphs not equal!", expectedDependencyGraph, filteredGraph);

    }

    @Test
    public void filterGraphBothAreInnerClassTest() {
        /*
         * Graph:
         * org.foo.C1 -> org.foo.C3
         * org.foo.C2 -> org.foo.C4
         * org.foo.C1$1 -> org.foo.C2$1
         *
         * expected filer: org.foo.C1 -> org.foo.C3, org.foo.C1 -> org.foo.C2, org.foo.C2 -> org.foo.C4
         */
        DependencyGraph dependencyGraph = new DependencyGraph();
        dependencyGraph.putEdge(C1, C3);
        dependencyGraph.putEdge(C2, C4);
        dependencyGraph.putEdge(C11, C21);

        // Apply filter
        DependencyGraph filteredGraph = filterDependencyGraph(dependencyGraph);

        // Expected Graph
        DependencyGraph expectedDependencyGraph = new DependencyGraph();
        expectedDependencyGraph.putEdge(C1, C3);
        expectedDependencyGraph.putEdge(C1, C2);
        expectedDependencyGraph.putEdge(C2, C4);
        assertEquals("Graphs not equal!", expectedDependencyGraph, filteredGraph);

    }

}
