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

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import de.ugoe.cs.comfort.Utils;
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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import javax.annotation.ParametersAreNonnullByDefault;

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
        final ListeningExecutorService executor =
                MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(generalConf.getNThreads()));

        Set<String> alreadyAnalyzedTests = new HashSet<>();
        Map<MutationLocation, String> generatedMutationsAndItsClassification = new ConcurrentHashMap<>();
        // We can only get this if we are using the smartsharkfiler
        if(generalConf.getFilerConfiguration().getName().endsWith("SmartSHARKFiler")) {
            SmartSHARKFiler shFiler = (SmartSHARKFiler) filer;
            alreadyAnalyzedTests.addAll(shFiler.getTestStateWithMutationResults());
            generatedMutationsAndItsClassification.putAll(shFiler.getMutationsAndClassification());
        }

        // Get all java files in directory. This is needed later on for the change classification. We need to do
        // this here, as we need to store the build file in the corresponding directory. If we then delete it
        // afterwards (or one thread deletes it while another thread is getting all files in the directory) we
        // will throw an exception
        Set<Path> javaFiles = Utils.getAllFilesFromProjectForRegex(generalConf.getProjectDir(), ".*\\.java");

        // Create a list of futures, we add them here so that we can wait for completion
        List<ListenableFuture<Void>> futures = new ArrayList<>();
        System.out.println(data.getCoverageData().entrySet());
        for (Map.Entry<IUnit, Set<IUnit>> entry : data.getCoverageData().entrySet()) {
            // We skip already analyzed tests
            if(alreadyAnalyzedTests.contains(entry.getKey().getFQN()) && !generalConf.isForceRerun()) {
                logger.debug("Test {} already has mutation data...", entry.getKey().getFQN());
            } else {
                // We create a listenable future here so that we can reuse the thread to store its results
                ListenableFuture<Result> result = executor.submit(
                        new MutationDataCollectorThread(entry.getKey(), generalConf, fileNameUtils,
                                generatedMutationsAndItsClassification, javaFiles));

                //Blank runnable to evaluate write completion -> little hack as we can not wait for the completion
                // of the callbacks but only on the completion of the main task of the thread. Therefore, we
                // create a callback here that is called every time a thread finishes his storing process
                Callable<Void> callback = () -> null;
                final ListenableFutureTask<Void> callbackFuture = ListenableFutureTask.create(callback);
                futures.add(callbackFuture);

                // Add callback for the thread so that it stores its results
                Futures.addCallback(result, new FutureCallback<Result>() {
                    @Override
                    @ParametersAreNonnullByDefault
                    public void onSuccess(Result result) {
                        try {
                            filer.storeResult(result);
                            logger.info("Stored result for test {}", entry.getKey().getFQN());
                        } catch (IOException e) {
                            logger.catching(e);
                        } finally {
                            callbackFuture.run();
                        }
                    }

                    @Override
                    @ParametersAreNonnullByDefault
                    public void onFailure(Throwable t) {
                        logger.catching(t);
                        callbackFuture.run();
                    }
                }, MoreExecutors.directExecutor());
            }
        }

        // Wait for the completion of all threads
        ListenableFuture<List<Void>> listFuture = Futures.successfulAsList(futures);
        logger.info("Waiting for completion...");
        try {
            listFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            logger.catching(e);
        }
        executor.shutdown();
        logger.info("Done...");
    }
}
