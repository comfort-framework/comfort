package de.ugoe.cs.comfort.collection.metriccollector.mutation;

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

import com.google.common.base.MoreObjects;

/**
 * @author Fabian Trautsch
 */
public class MutationExecutionResult {
    private int numTests = 0;
    private int numMutationUnits = 0;
    private int executionTime = 0;
    private int generatedMutations = 0;
    private int killedMutations = 0;
    private int mutationScore = 0;

    public MutationExecutionResult() {
    }

    public int getNumTests() {
        return numTests;
    }

    public void setNumTests(int numTests) {
        this.numTests = numTests;
    }

    public int getNumMutationUnits() {
        return numMutationUnits;
    }

    public void setNumMutationUnits(int numMutationUnits) {
        this.numMutationUnits = numMutationUnits;
    }

    public int getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(int executionTime) {
        this.executionTime = executionTime;
    }

    public int getGeneratedMutations() {
        return generatedMutations;
    }

    public void setGeneratedMutations(int generatedMutations) {
        this.generatedMutations = generatedMutations;
    }

    public int getKilledMutations() {
        return killedMutations;
    }

    public void setKilledMutations(int killedMutations) {
        this.killedMutations = killedMutations;
    }

    public int getMutationScore() {
        return mutationScore;
    }

    public void setMutationScore(int mutationScore) {
        this.mutationScore = mutationScore;
    }

    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("numTests", numTests)
                .add("numMutationUnits", numMutationUnits)
                .add("executionTime", executionTime)
                .add("generatedMutations", generatedMutations)
                .add("killedMutations", killedMutations)
                .add("mutationScore", mutationScore)
                .toString();
    }
}
