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

import de.ugoe.cs.comfort.BaseTest;
import de.ugoe.cs.comfort.data.graphs.DependencyGraph;
import de.ugoe.cs.comfort.data.models.IUnit;
import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * @author Fabian Trautsch
 */
public class TestTypeDetectionUtilsTest extends BaseTest {

    @Test
    public void getCallerCalleePairsOnClassLevelFromGraphWithCyclesTest() {
        /*
         * Graph:
         * org.foo.t1.T1 -> org.foo.C1
         * org.foo.C1 -> org.foo.C2
         * org.foo.C2 -> org.foo.C1
         *
         * Cycles can happen, e.g., if C1 makes use of C2 (e.g., by creating an instance of this class) and C2 uses
         * a static method of C1.
         *
         * Result: T1 = [C1, C2]
         */
        DependencyGraph dependencyGraph = new DependencyGraph();
        dependencyGraph.putEdge(test, C1);
        dependencyGraph.putEdge(C1, C2);
        dependencyGraph.putEdge(C2, C1);

        Map<IUnit, Set<IUnit>> callerCalleePairsOnClassLevel =
                TestTypeDetectionUtils.getCallPairsOnClassLevel(dependencyGraph);

        Map<IUnit, Set<IUnit>> expectedResult = new HashMap<>();
        Set<IUnit> test1Callees = new HashSet<>();
        test1Callees.add(C1);
        test1Callees.add(C2);
        expectedResult.put(test, test1Callees);

        assertEquals("Cycles are not handled correctly", expectedResult, callerCalleePairsOnClassLevel);
    }

    @Test
    public void getCallerCalleePairsOnClassLevelFromGraphRecursivly() {
        /*
         * Graph:
         * org.foo.t1.T1 -> org.foo.C1
         * org.foo.C1 -> org.foo.C2
         * org.foo.C2 -> org.foo.C3
         *
         * Result: T1 = [C1, C2, C3]
         */
        DependencyGraph dependencyGraph = new DependencyGraph();
        dependencyGraph.putEdge(test, C1);
        dependencyGraph.putEdge(C1, C2);
        dependencyGraph.putEdge(C2, C3);

        Map<IUnit, Set<IUnit>> callerCalleePairsOnClassLevel = TestTypeDetectionUtils.getCallPairsOnClassLevel(dependencyGraph);
        Map<IUnit, Set<IUnit>> expectedResult = new HashMap<>();
        Set<IUnit> test1Callees = new HashSet<>();
        test1Callees.add(C1);
        test1Callees.add(C2);
        test1Callees.add(C3);
        expectedResult.put(test, test1Callees);

        assertEquals("Cycles are not handled correctly", expectedResult, callerCalleePairsOnClassLevel);

    }

    /*
    @Test
    public void getTestGranularitiesForTestsOnMethodLevelUsingExampleFromPaperTest() {

         //Adaptions from paper example: In the paper the figure only shows "Test1", "Test2", and so on, but we have
         //also the test calls on method level, therefore we are only using ".m1" for each Test method, so that both
         //versions are similar

        CallGraph callGraph = new CallGraph();
        callGraph.addEdge(new CallEdge(CallType.INVOKE_VIRTUAL, 0, T1M1, C1M1));
        callGraph.addEdge(new CallEdge(CallType.INVOKE_VIRTUAL, 0, T2M1, C1M1));
        callGraph.addEdge(new CallEdge(CallType.INVOKE_VIRTUAL, 1, T2M1, C1M3));
        callGraph.addEdge(new CallEdge(CallType.INVOKE_VIRTUAL, 2, T2M1, C2M1));
        callGraph.addEdge(new CallEdge(CallType.INVOKE_VIRTUAL, 0, T3M1, C1M1));
        callGraph.addEdge(new CallEdge(CallType.INVOKE_VIRTUAL, 1, T3M1, C1M2));
        callGraph.addEdge(new CallEdge(CallType.INVOKE_VIRTUAL, 0, T4M1, C1M2));
        callGraph.addEdge(new CallEdge(CallType.INVOKE_VIRTUAL, 0, T4M1, C2M1));


        Map<String, Integer> expectedResult = new HashMap<>();
        expectedResult.put("org.foo.t1.Test1.m1", 1);
        expectedResult.put("org.foo.t2.Test2.m1", 3);
        expectedResult.put("org.foo.t3.Test3.m1", 2);
        expectedResult.put("org.foo.t4.Test4.m1", 2);

        assertEquals("Granularities are not correct!", expectedResult, TestTypeDetectionUtils.getTestGranularitiesForTestsOnMethodLevel(callGraph));
    }

    @Test
    public void getTestGranularitiesForTestsOnMethodLevelRecursively() {
        CallGraph callGraph = new CallGraph();
        callGraph.addEdge(new CallEdge(CallType.INVOKE_VIRTUAL, 0, T1M1, C1M1));
        callGraph.addEdge(new CallEdge(CallType.INVOKE_VIRTUAL, 0, C1M1, C2M1));


        Map<String, Integer> expectedResult = new HashMap<>();
        expectedResult.put("org.foo.t1.Test1.m1", 2);

        assertEquals("Granularities are not correct!", expectedResult, TestTypeDetectionUtils.getTestGranularitiesForTestsOnMethodLevel(callGraph));
    }

    @Test
    public void getTestGranularitiesForTestsOnMethodLevelWithLoop() {
        CallGraph callGraph = new CallGraph();
        callGraph.addEdge(new CallEdge(CallType.INVOKE_VIRTUAL, 0, T1M1, C1M1));
        callGraph.addEdge(new CallEdge(CallType.INVOKE_VIRTUAL, 0, C1M1, C2M1));
        callGraph.addEdge(new CallEdge(CallType.INVOKE_VIRTUAL, 0, C2M1, C1M1));


        Map<String, Integer> expectedResult = new HashMap<>();
        expectedResult.put("org.foo.t1.Test1.m1", 2);

        assertEquals("Granularities are not correct!", expectedResult, TestTypeDetectionUtils.getTestGranularitiesForTestsOnMethodLevel(callGraph));
    }

    @Test
    public void getTestGranularitiesForTestsOnMethodLevelTesthasMultipleMethods() {
        CallGraph callGraph = new CallGraph();
        callGraph.addEdge(new CallEdge(CallType.INVOKE_VIRTUAL, 0, T1M1, C1M1));
        callGraph.addEdge(new CallEdge(CallType.INVOKE_VIRTUAL, 0, T1M2, C2M1));


        Map<String, Integer> expectedResult = new HashMap<>();
        expectedResult.put("org.foo.t1.Test1.m1", 1);
        expectedResult.put("org.foo.t1.Test1.m2", 1);

        assertEquals("Granularities are not correct!", expectedResult, TestTypeDetectionUtils.getTestGranularitiesForTestsOnMethodLevel(callGraph));
    }
    */
}
