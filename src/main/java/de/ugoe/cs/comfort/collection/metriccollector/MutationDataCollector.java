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

import de.ugoe.cs.comfort.annotations.SupportsJava;
import de.ugoe.cs.comfort.annotations.SupportsMethod;
import de.ugoe.cs.comfort.collection.metriccollector.mutation.MutationDataCollectorThread;
import de.ugoe.cs.comfort.collection.metriccollector.mutation.MutationLocation;
import de.ugoe.cs.comfort.configuration.GeneralConfiguration;
import de.ugoe.cs.comfort.data.CoverageData;
import de.ugoe.cs.comfort.data.models.IUnit;
import de.ugoe.cs.comfort.filer.BaseFiler;
import de.ugoe.cs.comfort.filer.SmartSHARKFiler;
import de.ugoe.cs.comfort.filer.models.Result;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Fabian Trautsch
 */
public class MutationDataCollector extends BaseMetricCollector {
    public MutationDataCollector(GeneralConfiguration configuration, BaseFiler filer) {
        super(configuration, filer);
    }

    @SupportsMethod
    @SupportsJava
    public void getMutationDataMetrics(CoverageData data) throws IOException {

        final ExecutorService executor = Executors.newFixedThreadPool(generalConf.getNThreads());
        CompletionService<Result> pool = new ExecutorCompletionService<>(executor);


        Set<String> alreadyAnalyzedTests = new HashSet<>();
        // We can only get this if we are using the smartsharkfiler
        if(generalConf.getFilerConfiguration().getName().endsWith("SmartSHARKFiler")) {
            SmartSHARKFiler shFiler = (SmartSHARKFiler) filer;
            alreadyAnalyzedTests.addAll(shFiler.getTestStateWithMutationResults());
        }

        int analyzedTests = 0;
        Map<MutationLocation, String> generatedMutationsAndItsClassification = new ConcurrentHashMap<>();
        for (Map.Entry<IUnit, Set<IUnit>> entry : data.getCoverageData().entrySet()) {
            if(alreadyAnalyzedTests.contains(entry.getKey().getFQN()) && !generalConf.isForceRerun()) {
                logger.debug("Test {} already has mutation data...", entry.getKey().getFQN());
            } else {
                analyzedTests++;
                pool.submit(new MutationDataCollectorThread(entry.getKey(), generalConf, fileNameUtils,
                        generatedMutationsAndItsClassification));
            }
        }

        for (int i=0; i<analyzedTests; i++) {
            try {
                Result result = pool.take().get();

                // Added here as it can be the case that a result could not be produced which would then result
                // in a null result.
                if(result != null) {
                    synchronized(this) {
                        filer.storeResult(result);
                    }
                } else {
                    logger.info("No result generated...");
                }
            } catch (InterruptedException | ExecutionException e) {
                logger.catching(e);
            }
        }
        executor.shutdown();
    }
}
