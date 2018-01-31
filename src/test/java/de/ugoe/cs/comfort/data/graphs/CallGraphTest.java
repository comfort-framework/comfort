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

import de.ugoe.cs.comfort.BaseTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Fabian Trautsch
 */
public class CallGraphTest extends BaseTest {
    @Test
    public void getDependencyGraphRepresentationTest() {
        CallGraph callGraph = new CallGraph();
        callGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, addressTestInit, addressGetStreet));
        callGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, addressGetStreet, personInit));
        callGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, addressInit, javaLangObjectInit));
        callGraph.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 1, addressInit, javaLangObjectInit));

        DependencyGraph expectedDependencyGraph = new DependencyGraph();
        expectedDependencyGraph.putEdge(addressTest, address);
        expectedDependencyGraph.putEdge(address, person);
        expectedDependencyGraph.putEdge(address, object);
        assertEquals("Not the correct graph representation!", expectedDependencyGraph, callGraph.getDependencyGraphRepresentation());
    }
}
