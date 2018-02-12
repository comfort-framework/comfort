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

package de.ugoe.cs.comfort.collection.metriccollector.mutation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;

import de.ugoe.cs.comfort.BaseTest;
import de.ugoe.cs.comfort.exception.MutationResultException;
import java.nio.file.Paths;
import org.junit.Test;

/**
 * @author Fabian Trautsch
 */
public class MutationChangeClassifierTest extends BaseTest {
    private static final int  DC_START = 3;
    private static final int  DC_END = 19;

    private static final int  LC_START = 3;
    private static final int  LC_END = 7;

    private static final int  CC_START = 3;
    private static final int  CC_END = 31;

    private void runningTestsOnDataChanges(String expectedValue, boolean shouldBeEqual) {
        try {
            for(int i=DC_START; i<=DC_END; i++) {
                String classification = MutationChangeClassifier.getChangeClassification(
                        Paths.get(getPathToResource("mutationChangeClassifierData")),
                        "DataChanges.java",
                        "org.pitest.mutationtest.engine.gregor.mutators.ConditionalsBoundaryMutator",
                        i
                );

                if(shouldBeEqual) {
                    assertEquals("Classification not correct!", expectedValue, classification);
                } else {
                    assertNotEquals("Classification not correct!", expectedValue, classification);
                }
            }
        } catch (MutationResultException e) {
            fail("Unexpected exception: "+e.getMessage());
        }
    }

    private void runningTestsOnLogicControlChanges(String expectedValue, boolean shouldBeEqual) {
        try {
            for(int i=LC_START; i<=LC_END; i++) {
                String classification = MutationChangeClassifier.getChangeClassification(
                        Paths.get(getPathToResource("mutationChangeClassifierData")),
                        "LogicControlChanges.java",
                        "org.pitest.mutationtest.engine.gregor.mutators.ConditionalsBoundaryMutator",
                        i
                );

                if(shouldBeEqual) {
                    assertEquals("Classification not correct!", expectedValue, classification);
                } else {
                    assertNotEquals("Classification not correct!", expectedValue, classification);
                }
            }
        } catch (MutationResultException e) {
            fail("Unexpected exception: "+e.getMessage());
        }
    }

    private void runningTestsOnComputationChanges(String expectedValue, boolean shouldBeEqual) {
        try {
            for(int i=CC_START; i<=CC_END; i++) {
                String classification = MutationChangeClassifier.getChangeClassification(
                        Paths.get(getPathToResource("mutationChangeClassifierData")),
                        "ComputationChanges.java",
                        "org.pitest.mutationtest.engine.gregor.mutators.ConditionalsBoundaryMutator",
                        i
                );

                if(shouldBeEqual) {
                    assertEquals("Classification not correct!", expectedValue, classification);
                } else {
                    assertNotEquals("Classification not correct!", expectedValue, classification);
                }
            }
        } catch (MutationResultException e) {
            fail("Unexpected exception: "+e.getMessage());
        }
    }


    @Test
    public void checkDataChangesTest() {
        runningTestsOnDataChanges("DATA", true);
    }

    @Test
    public void checkDataChangesUsingLogicControlChangesTest() {
        runningTestsOnLogicControlChanges("DATA", false);
    }

    @Test
    public void checkDataChangesUsingComputationChangesTest() {
        runningTestsOnComputationChanges("DATA", false);
    }

    @Test
    public void checkLogicControlTest() {
        runningTestsOnLogicControlChanges("LOGIC/CONTROL", true);
    }

    @Test
    public void checkLogicControlUsingDataChangesTest() {
        runningTestsOnDataChanges("LOGIC/CONTROL", false);
    }

    @Test
    public void checkLogicControlUsingComputationChangesTest() {
        runningTestsOnComputationChanges("LOGIC/CONTROL", false);

    }

    @Test
    public void checkComputationTest() {
        runningTestsOnComputationChanges("COMPUTATION", true);
    }

    @Test
    public void checkComputationUsingDataChangesTest() {
        runningTestsOnDataChanges("COMPUTATION", false);
    }

    @Test
    public void checkComputationUsingLogicControlChangesTest() {
        runningTestsOnLogicControlChanges("COMPUTATION", false);
    }

    @Test
    public void checkTwoLinesIfTest() {
        try {
            String classification1 = MutationChangeClassifier.getChangeClassification(
                    Paths.get(getPathToResource("mutationChangeClassifierData")),
                    "SpecialCases.java",
                    "org.pitest.mutationtest.engine.gregor.mutators.NegateConditionalsMutator",
                    3);
            String classification2 = MutationChangeClassifier.getChangeClassification(
                    Paths.get(getPathToResource("mutationChangeClassifierData")),
                    "SpecialCases.java",
                    "org.pitest.mutationtest.engine.gregor.mutators.NegateConditionalsMutator",
                    4);
            assertEquals("Classification not correct", "LOGIC/CONTROL", classification1);
            assertEquals("Classification not correct", "LOGIC/CONTROL", classification2);
        } catch (MutationResultException e) {
            fail("Unexpected exception: "+e.getMessage());
        }
    }
}
