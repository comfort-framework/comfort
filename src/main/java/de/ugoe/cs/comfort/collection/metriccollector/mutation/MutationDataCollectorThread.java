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

import de.ugoe.cs.comfort.FileNameUtils;
import de.ugoe.cs.comfort.collection.metriccollector.mutation.executors.IMutationExecutor;
import de.ugoe.cs.comfort.collection.metriccollector.mutation.executors.PITExecutor;
import de.ugoe.cs.comfort.configuration.GeneralConfiguration;
import de.ugoe.cs.comfort.data.models.IUnit;
import de.ugoe.cs.comfort.filer.models.Mutation;
import de.ugoe.cs.comfort.filer.models.Result;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Fabian Trautsch
 */
public class MutationDataCollectorThread implements Callable<Result> {
    private final IUnit unit;
    private final GeneralConfiguration generalConf;
    private final FileNameUtils fileNameUtils;
    private final Logger logger = LogManager.getLogger(this.getClass().getName());
    private final String threadName = Thread.currentThread().getName();

    private Map<MutationLocation, String> generatedMutationsAndItsClassification;

    public MutationDataCollectorThread(IUnit unit, GeneralConfiguration generalConfiguration,
                                       FileNameUtils fileNameUtils,
                                       Map<MutationLocation, String> generatedMutationsAndItsClassification) {
        this.unit = unit;
        this.generalConf = generalConfiguration;
        this.fileNameUtils = fileNameUtils;
        this.generatedMutationsAndItsClassification = generatedMutationsAndItsClassification;
    }

    @Override
    public Result call() {
        String className = unit.getFQNOfUnit();
        String[] parts = unit.getFQN().split("\\.");
        String methodName = parts[parts.length-1];
        logger.info("{} - Getting mutation data for {}.{}", threadName, className, methodName);

        // Choose correct mutation executor
        IMutationExecutor mutationExecutor = new PITExecutor();

        try {
            Result result = new Result(unit.getFQN(),
                    fileNameUtils.getPathForIdentifier(unit.getFQN(), generalConf.getMethodLevel()));


            // Execute mutation executor -> do mutation testing for className.methodName
            MutationExecutionResult mutationExecutionResult = mutationExecutor.execute(
                    generalConf.getProjectDir(),
                    className,
                    methodName
            );

            // If execution was successful, we will read the results
            Set<Mutation> mutationResults = mutationExecutor.getDetailedResults(
                    generalConf.getProjectDir(), generatedMutationsAndItsClassification);

            // And add these results to this specific test
            result.addMutationResults(mutationResults);
            result.addMetric("mut_genMut", String.valueOf(mutationExecutionResult.getGeneratedMutations()));
            result.addMetric("mut_killMut", String.valueOf(mutationExecutionResult.getKilledMutations()));
            result.addMetric("mut_scoreMut", String.valueOf(mutationExecutionResult.getMutationScore()));

            mutationExecutor.cleanup();
            return result;
        } catch (IOException e) {
            try {
                mutationExecutor.cleanup();
            } catch (IOException e1) {
                logger.catching(e1);
            }
            // If not successful, print error but it is not necessary to cancel here
            logger.warn("Error \"{}\" for executing mutation testing for test {}", e.getMessage(),
                    unit.getFQN());
        }
        return null;

    }


}
