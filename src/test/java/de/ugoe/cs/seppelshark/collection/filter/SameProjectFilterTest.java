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
import de.ugoe.cs.seppelshark.data.graphs.CallEdge;
import de.ugoe.cs.seppelshark.data.graphs.CallGraph;
import de.ugoe.cs.seppelshark.data.graphs.CallType;
import de.ugoe.cs.seppelshark.data.graphs.DependencyGraph;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Fabian Trautsch
 */
public class SameProjectFilterTest extends BaseTest{

    private GeneralConfiguration configuration = new GeneralConfiguration();


    private DependencyGraph filterDependencyGraph(DependencyGraph dependencyGraph) {
        SameProjectFilter filter = new SameProjectFilter(configuration);
        return filter.filterDependencyGraph(dependencyGraph);
    }

    private CallGraph filterCallGraph(CallGraph callGraph) {
        SameProjectFilter filter = new SameProjectFilter(configuration);
        return filter.filterCallGraph(callGraph);
    }

    @Test
    public void filterJavaDependencyGraphTest() {
        DependencyGraph dependencyGraph = new DependencyGraph();
        dependencyGraph.putEdge(fooTest, main);
        dependencyGraph.putEdge(fooTest, object);
        dependencyGraph.putEdge(fooTest, jassert);
        dependencyGraph.putEdge(entryView, object);

        // Apply filter
        DependencyGraph filteredGraph = filterDependencyGraph(dependencyGraph);

        // Expected Graph
        DependencyGraph expectedDependencyGraph = new DependencyGraph();
        expectedDependencyGraph.putEdge(fooTest, main);
        assertEquals("Graphs not equal!", expectedDependencyGraph, filteredGraph);
    }

    @Test
    public void filterJavaCallGraphTest() {
        CallGraph callGraph = new CallGraph();

        callGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, addressTestInit, addressInitWithParam));
        callGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, addressInitWithParam, personInit));
        callGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, addressGetStreet, javaLangObjectInit));
        callGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 1, addressGetStreet, javaLangObjectInit));

        // Apply filter
        CallGraph filteredGraph = filterCallGraph(callGraph);

        // Expected Graph
        CallGraph expectedCallGraph = new CallGraph();
        expectedCallGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, addressTestInit, addressInitWithParam));
        expectedCallGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, addressInitWithParam, personInit));
        assertEquals("Graphs not equal!", expectedCallGraph, filteredGraph);
    }

    @Test
    public void filterPythonDependencyGraphTest() {
        DependencyGraph dependencyGraph = new DependencyGraph();
        dependencyGraph.putEdge(pyTest1, module2);
        dependencyGraph.putEdge(pyTest1, pyMock);
        dependencyGraph.putEdge(pyTest1, pyUnittest);
        dependencyGraph.putEdge(module1, pyMock);

        // Apply filter
        DependencyGraph filteredGraph = filterDependencyGraph(dependencyGraph);

        // Expected Graph
        DependencyGraph expectedDependencyGraph = new DependencyGraph();
        expectedDependencyGraph.putEdge(pyTest1, module2);
        assertEquals("Graphs not equal!", expectedDependencyGraph, filteredGraph);
    }

    @Test
    public void filterPythonCallGraphTest() {
        CallGraph callGraph = new CallGraph();

        callGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, testCallDemo, demoInit));
        callGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, demoInit, callDemo));
        callGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, demoBar, pyUnittestInit));
        callGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 1, demoBar, pyUnittestInit));

        // Apply filter
        CallGraph filteredGraph = filterCallGraph(callGraph);

        // Expected Graph
        CallGraph expectedCallGraph = new CallGraph();
        expectedCallGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, testCallDemo, demoInit));
        expectedCallGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, demoInit, callDemo));
        assertEquals("Graphs not equal!", expectedCallGraph, filteredGraph);
    }
}
