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
import de.ugoe.cs.seppelshark.data.graphs.CallEdge;
import de.ugoe.cs.seppelshark.data.graphs.CallGraph;
import de.ugoe.cs.seppelshark.data.graphs.CallType;
import de.ugoe.cs.seppelshark.data.graphs.DependencyGraph;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Fabian Trautsch
 */
public class DirectConnectionToTestFilterTest extends BaseTest{
    private final DirectConnectionToTestFilter filter = new DirectConnectionToTestFilter(null);


    private DependencyGraph getFilteredDependencyGraph(DependencyGraph dependencyGraph) {
        return filter.filter(dependencyGraph);
    }

    private CallGraph getFilteredCallGraph(CallGraph callGraph) {
        return filter.filter(callGraph);
    }

    @Test
    public void filterPythonDependencyGraphTest() {
        DependencyGraph dependencyGraph = new DependencyGraph();
        dependencyGraph.putEdge(pyTest1, module1);
        dependencyGraph.putEdge(pyTest1, module3);
        dependencyGraph.putEdge(module1, module2);

        DependencyGraph filteredGraph = getFilteredDependencyGraph(dependencyGraph);

        DependencyGraph expectedGraph = new DependencyGraph();
        expectedGraph.putEdge(pyTest1, module1);
        expectedGraph.putEdge(pyTest1, module3);
        assertEquals("Graphs not equal", expectedGraph, filteredGraph);
    }
    
    @Test
    public void filterPythonDependencyGraphTransitiveTest() {
        DependencyGraph dependencyGraph = new DependencyGraph();
        dependencyGraph.putEdge(pyTest1, module1);
        dependencyGraph.putEdge(module1, module2);

        DependencyGraph filteredGraph = getFilteredDependencyGraph(dependencyGraph);

        DependencyGraph expectedGraph = new DependencyGraph();
        expectedGraph.putEdge(pyTest1, module1);
        assertEquals("Graphs not equal", expectedGraph, filteredGraph);
    }

    @Test
    public void filterJavaGraphCheckTestHandlingTest() {
        DependencyGraph dependencyGraph = new DependencyGraph();
        dependencyGraph.putEdge(blatestbla, testEntryView);
        dependencyGraph.putEdge(address, addressTest);

        DependencyGraph filteredGraph = getFilteredDependencyGraph(dependencyGraph);

        DependencyGraph expectedGraph = new DependencyGraph();
        expectedGraph.putEdge(blatestbla, testEntryView);

        assertEquals("Graphs are not equal", expectedGraph, filteredGraph);
    }


    @Test
    public void filterJavaCallGraphTest() {
        CallGraph callGraph = new CallGraph();
        callGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, blatestblaInit, mainInit));
        callGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 1, blatestblaInit, mainInit));
        callGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, addressInitWithParam, telephoneBookInit));
        callGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, addressGetStreet, entryViewInit));

        CallGraph filteredCallGraph = getFilteredCallGraph(callGraph);

        CallGraph expectedCallGraph = new CallGraph();
        expectedCallGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, blatestblaInit, mainInit));
        expectedCallGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 1, blatestblaInit, mainInit));

        assertEquals("Graphs are not equal", expectedCallGraph, filteredCallGraph);
    }

    @Test
    public void filterPythonCallGraphTest() {
        CallGraph callGraph = new CallGraph();
        callGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, pyTestDemoInit, pyModule1Init));
        callGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 1, pyTestDemoInit, pyModule1Init));
        callGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, pyModule2Init, pyModule3Init));
        callGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, pyModule2Foo, pyModule4Init));

        CallGraph filteredCallGraph = getFilteredCallGraph(callGraph);

        CallGraph expectedCallGraph = new CallGraph();
        expectedCallGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, pyTestDemoInit, pyModule1Init));
        expectedCallGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 1, pyTestDemoInit, pyModule1Init));

        assertEquals("Graphs are not equal", expectedCallGraph, filteredCallGraph);
    }


}
