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
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Fabian Trautsch
 */
public class DeletePythonPackagesFilterTest extends BaseTest {
    private GeneralConfiguration configuration = new GeneralConfiguration();

    @Test
    public void testFilter() {
        DependencyGraph dependencyGraph = new DependencyGraph();
        dependencyGraph.putEdge(pyTest1, moduleInit);
        dependencyGraph.putEdge(pyTest1, package1Init);
        dependencyGraph.putEdge(pyTest1, subPackage1Init);
        dependencyGraph.putEdge(pyTest1, module1);
        dependencyGraph.putEdge(pyTest1, module2);

        dependencyGraph.putEdge(pyTest2, moduleInit);
        dependencyGraph.putEdge(pyTest2, package1Init);
        dependencyGraph.putEdge(pyTest2, subPackage1Init);

        DependencyGraph expectedGraph = new DependencyGraph();
        expectedGraph.putEdge(pyTest1, module1);
        expectedGraph.putEdge(pyTest1, module2);

        DeletePythonPackagesFilter filter = new DeletePythonPackagesFilter(configuration);
        DependencyGraph filteredDependencyGraph = filter.filter(dependencyGraph);
        assertEquals("Graphs are not equal...", filteredDependencyGraph, expectedGraph);
    }




}
