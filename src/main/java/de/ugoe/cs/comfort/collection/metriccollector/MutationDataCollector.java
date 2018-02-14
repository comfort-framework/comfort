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
import de.ugoe.cs.comfort.configuration.GeneralConfiguration;
import de.ugoe.cs.comfort.data.CoverageData;
import de.ugoe.cs.comfort.data.models.IUnit;
import de.ugoe.cs.comfort.filer.models.Result;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Fabian Trautsch
 */
public class MutationDataCollector extends BaseMetricCollector {
    private Set<Result> results = new HashSet<>();

    public MutationDataCollector(GeneralConfiguration configuration) {
        super(configuration);
    }

    @SupportsMethod
    @SupportsJava
    public Set<Result> getMutationDataMetrics(CoverageData data) {

        final ExecutorService executor = Executors.newFixedThreadPool(generalConf.getNThreads());
        CompletionService<Result> pool = new ExecutorCompletionService<>(executor);


        for (Map.Entry<IUnit, Set<IUnit>> entry : data.getCoverageData().entrySet()) {
            pool.submit(new MutationDataCollectorThread(entry.getKey(), generalConf, fileNameUtils));
        }

        for (int i=0; i<data.getCoverageData().size(); i++) {
            try {
                Result result = pool.take().get();
                results.add(result);
            } catch (InterruptedException | ExecutionException e) {
                logger.catching(e);
            }
        }
        executor.shutdown();
        return results;
    }
}
