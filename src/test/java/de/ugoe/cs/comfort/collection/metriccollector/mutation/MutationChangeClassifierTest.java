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
import static org.junit.Assert.fail;

import de.ugoe.cs.comfort.BaseTest;
import de.ugoe.cs.comfort.exception.MutationResultException;
import java.nio.file.Paths;
import org.junit.Test;

/**
 * @author Fabian Trautsch
 */
public class MutationChangeClassifierTest extends BaseTest {

    @Test
    public void conditionalsBoundaryMutatorTest() {
        try {
            String classification = MutationChangeClassifier.getChangeClassification(
                    Paths.get(getPathToResource("mutationOperatorsTestData")),
                    "ConditionalsBoundaryOperatorData.java",
                    "org.pitest.mutationtest.engine.gregor.mutators.ConditionalsBoundaryMutator",
                    3
            );
            assertEquals("Classification not correct!", "LOGIC/CONTROL", classification);
        } catch (MutationResultException e) {
            fail("Unexpected exception: "+e.getMessage());
        }
    }

    @Test
    public void returnConditionalsBoundaryMutatorTest() {
        try {
            String classification = MutationChangeClassifier.getChangeClassification(
                    Paths.get(getPathToResource("mutationOperatorsTestData")),
                    "ConditionalsBoundaryOperatorData.java",
                    "org.pitest.mutationtest.engine.gregor.mutators.ConditionalsBoundaryMutator",
                    19
            );
            assertEquals("Classification not correct!", "LOGIC/CONTROL", classification);
        } catch (MutationResultException e) {
            fail("Unexpected exception: "+e.getMessage());
        }
    }

    @Test
    public void incrementMutatorInLoopTest() {
        try {
            String classification = MutationChangeClassifier.getChangeClassification(
                    Paths.get(getPathToResource("mutationOperatorsTestData")),
                    "IncrementsOperatorData.java",
                    "org.pitest.mutationtest.engine.gregor.mutators.IncrementsMutator",
                    6
            );
            assertEquals("Classification not correct!", "LOGIC/CONTROL", classification);
        } catch (MutationResultException e) {
            fail("Unexpected exception: "+e.getMessage());
        }
    }

    @Test
    public void incrementMutatorOutsideLoopTest() {
        try {
            String classification = MutationChangeClassifier.getChangeClassification(
                    Paths.get(getPathToResource("mutationOperatorsTestData")),
                    "IncrementsOperatorData.java",
                    "org.pitest.mutationtest.engine.gregor.mutators.IncrementsMutator",
                    4
            );
            assertEquals("Classification not correct!", "COMPUTATION", classification);
        } catch (MutationResultException e) {
            fail("Unexpected exception: "+e.getMessage());
        }
    }

    @Test
    public void mathMutatorOutsideLoopTest() {
        try {
            String classification = MutationChangeClassifier.getChangeClassification(
                    Paths.get(getPathToResource("mutationOperatorsTestData")),
                    "MathOperatorData.java",
                    "org.pitest.mutationtest.engine.gregor.mutators.MathMutator",
                    7
            );
            assertEquals("Classification not correct!", "COMPUTATION", classification);
        } catch (MutationResultException e) {
            fail("Unexpected exception: "+e.getMessage());
        }
    }

    @Test
    public void mathMutatorInsideLoopTest() {
        try {
            String classification = MutationChangeClassifier.getChangeClassification(
                    Paths.get(getPathToResource("mutationOperatorsTestData")),
                    "MathOperatorData.java",
                    "org.pitest.mutationtest.engine.gregor.mutators.MathMutator",
                    3
            );
            assertEquals("Classification not correct!", "LOGIC/CONTROL", classification);
        } catch (MutationResultException e) {
            fail("Unexpected exception: "+e.getMessage());
        }
    }

    @Test
    public void mathMutatorInitializationTest() {
        try {
            String classification = MutationChangeClassifier.getChangeClassification(
                    Paths.get(getPathToResource("mutationOperatorsTestData")),
                    "MathOperatorData.java",
                    "org.pitest.mutationtest.engine.gregor.mutators.MathMutator",
                    22
            );
            assertEquals("Classification not correct!", "DATA", classification);
        } catch (MutationResultException e) {
            fail("Unexpected exception: "+e.getMessage());
        }
    }

    @Test
    public void negateConditionalsMutatorInForLoopTest() {
        try {
            String classification = MutationChangeClassifier.getChangeClassification(
                    Paths.get(getPathToResource("mutationOperatorsTestData")),
                    "NegateConditionalsOperatorData.java",
                    "org.pitest.mutationtest.engine.gregor.mutators.NegateConditionalsMutator",
                    3
            );
            assertEquals("Classification not correct!", "LOGIC/CONTROL", classification);
        } catch (MutationResultException e) {
            fail("Unexpected exception: "+e.getMessage());
        }
    }

    @Test
    public void negateConditionalsMutatorOutsideLoopTest() {
        try {
            String classification = MutationChangeClassifier.getChangeClassification(
                    Paths.get(getPathToResource("mutationOperatorsTestData")),
                    "NegateConditionalsOperatorData.java",
                    "org.pitest.mutationtest.engine.gregor.mutators.NegateConditionalsMutator",
                    7
            );
            assertEquals("Classification not correct!", "COMPUTATION", classification);
        } catch (MutationResultException e) {
            fail("Unexpected exception: "+e.getMessage());
        }
    }

    @Test
    public void negateConditionalsMutatorInWhileLoopTest() {
        try {
            String classification = MutationChangeClassifier.getChangeClassification(
                    Paths.get(getPathToResource("mutationOperatorsTestData")),
                    "NegateConditionalsOperatorData.java",
                    "org.pitest.mutationtest.engine.gregor.mutators.NegateConditionalsMutator",
                    14
            );
            assertEquals("Classification not correct!", "LOGIC/CONTROL", classification);
        } catch (MutationResultException e) {
            fail("Unexpected exception: "+e.getMessage());
        }
    }

    @Test
    public void removeIncrementsOperatorInForLoopTest() throws MutationResultException {
        String classification = MutationChangeClassifier.getChangeClassification(
                Paths.get(getPathToResource("mutationOperatorsTestData")),
                "RemoveIncrementsOperatorData.java",
                "org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveIncrementsMutator",
                3
        );
        assertEquals("Classification not correct!", "LOGIC/CONTROL", classification);
    }

    @Test
    public void removeIncrementsOperatorInComputationTest() {
        try {
            String classification = MutationChangeClassifier.getChangeClassification(
                    Paths.get(getPathToResource("mutationOperatorsTestData")),
                    "RemoveIncrementsOperatorData.java",
                    "org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveIncrementsMutator",
                    7
            );
            assertEquals("Classification not correct!", "COMPUTATION", classification);
        } catch (MutationResultException e) {
            fail("Unexpected exception: "+e.getMessage());
        }
    }

    @Test
    public void removeIncrementsOperatorInInitializationComputationTest() {
        try {
            String classification = MutationChangeClassifier.getChangeClassification(
                    Paths.get(getPathToResource("mutationOperatorsTestData")),
                    "RemoveIncrementsOperatorData.java",
                    "org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveIncrementsMutator",
                    12
            );
            assertEquals("Classification not correct!", "DATA", classification);
        } catch (MutationResultException e) {
            fail("Unexpected exception: "+e.getMessage());
        }
    }

    @Test
    public void invertNegsInLoopTest() {
        try {
            String classification = MutationChangeClassifier.getChangeClassification(
                    Paths.get(getPathToResource("mutationOperatorsTestData")),
                    "InvertNegativesOperatorData.java",
                    "org.pitest.mutationtest.engine.gregor.mutators.InvertNegsMutator",
                    3
            );
            assertEquals("Classification not correct!", "LOGIC/CONTROL", classification);
        } catch (MutationResultException e) {
            fail("Unexpected exception: "+e.getMessage());
        }
    }

    @Test
    public void invertNegsInInitializationTest() {
        try {
            String classification = MutationChangeClassifier.getChangeClassification(
                    Paths.get(getPathToResource("mutationOperatorsTestData")),
                    "InvertNegativesOperatorData.java",
                    "org.pitest.mutationtest.engine.gregor.mutators.InvertNegsMutator",
                    7
            );
            assertEquals("Classification not correct!", "DATA", classification);
        } catch (MutationResultException e) {
            fail("Unexpected exception: "+e.getMessage());
        }
    }

    @Test
    public void invertNegsInAssignmentTest() {
        try {
            String classification = MutationChangeClassifier.getChangeClassification(
                    Paths.get(getPathToResource("mutationOperatorsTestData")),
                    "InvertNegativesOperatorData.java",
                    "org.pitest.mutationtest.engine.gregor.mutators.InvertNegsMutator",
                    8
            );
            assertEquals("Classification not correct!", "COMPUTATION", classification);
        } catch (MutationResultException e) {
            fail("Unexpected exception: "+e.getMessage());
        }
    }

    @Test
    public void invertNegsInReturnStatementTest() {
        try {
            String classification = MutationChangeClassifier.getChangeClassification(
                    Paths.get(getPathToResource("mutationOperatorsTestData")),
                    "InvertNegativesOperatorData.java",
                    "org.pitest.mutationtest.engine.gregor.mutators.InvertNegsMutator",
                    9
            );
            assertEquals("Classification not correct!", "LOGIC/CONTROL", classification);
        } catch (MutationResultException e) {
            fail("Unexpected exception: "+e.getMessage());
        }
    }
}
