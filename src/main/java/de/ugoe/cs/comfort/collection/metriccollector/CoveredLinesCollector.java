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

import de.ugoe.cs.comfort.Utils;
import de.ugoe.cs.comfort.annotations.SupportsClass;
import de.ugoe.cs.comfort.annotations.SupportsJava;
import de.ugoe.cs.comfort.annotations.SupportsMethod;
import de.ugoe.cs.comfort.annotations.SupportsPython;
import de.ugoe.cs.comfort.configuration.GeneralConfiguration;
import de.ugoe.cs.comfort.data.CoverageData;
import de.ugoe.cs.comfort.data.models.IUnit;
import de.ugoe.cs.comfort.filer.BaseFiler;
import de.ugoe.cs.comfort.filer.models.Result;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Fabian Trautsch
 */
public class CoveredLinesCollector  extends BaseMetricCollector {
    public CoveredLinesCollector(GeneralConfiguration configuration, BaseFiler filer) {
        super(configuration, filer);
    }

    @SupportsJava
    @SupportsMethod
    public void getCoveredTestAndProductionLinesMethodLevel(CoverageData coverageData) throws IOException {

        Set<Result> results = generateResults(coverageData.getCoverageDataForAll());
        filer.storeResults(results);
    }


    @SupportsPython
    @SupportsMethod
    public void getCoveredTestAndProductionLinesMethodLevelPython(CoverageData coverageData) throws IOException {

        Set<Result> results = new HashSet<>();
        for(Map.Entry<IUnit, Set<IUnit>> entry: coverageData.getCoverageDataForAll().entrySet()) {
            Integer coveredProductionLines = 0;
            Integer coveredTestLines = 0;
            logger.debug("Looking at test {}", entry.getKey());
            if(entry.getValue() != null) {
                for (IUnit coveredMethod : entry.getValue()) {
                    // If we have a standard python projects, tests are hold in folders called "test" or "tests",
                    // which can also be in sub folders, thats why we look for a contains
                    if(coveredMethod.getFilePath().toString().contains("test")) {
                        logger.debug("Covered Test (by path): {}", coveredMethod);
                        coveredTestLines += coveredMethod.getCoveredLines();
                    } else {
                        if (!coveredMethod.getFQNOfUnit().equals(entry.getKey().getFQNOfUnit())
                                && !(coveredMethod.getFQNOfUnit().contains("test")
                                || coveredMethod.getFQNOfUnit().contains("validate"))) {
                            logger.debug("Covered production code: {}", coveredMethod);
                            coveredProductionLines += coveredMethod.getCoveredLines();
                        } else {
                            logger.debug("Covered Test: {}", coveredMethod);
                            coveredTestLines += coveredMethod.getCoveredLines();
                        }
                    }
                }
            }
            logger.debug("Test {} covered {} test and {} production lines", entry.getKey(),
                    coveredTestLines, coveredProductionLines);
            Result result = new Result(entry.getKey().getFQN(), entry.getKey().getFilePath());
            result.addMetric("cov_tlines", String.valueOf(coveredTestLines));
            result.addMetric("cov_plines", String.valueOf(coveredProductionLines));
            results.add(result);
        }
        filer.storeResults(results);
    }

    @SupportsJava
    @SupportsPython
    @SupportsClass
    public void getCoveredTestAndProductionLinesClassLevel(CoverageData coverageData) throws IOException {

        Set<Result> results = generateResults(coverageData.getCoverageDataForAllClassLevel());
        filer.storeResults(results);
    }

    private Set<Result> generateResults(Map<IUnit, Set<IUnit>> data) {
        Set<Result> results = new HashSet<>();
        for(Map.Entry<IUnit, Set<IUnit>> entry: data.entrySet()) {
            Integer coveredProductionLines = 0;
            Integer coveredTestLines = 0;
            logger.debug("Looking at test {}", entry.getKey());
            if(entry.getValue() != null) {
                for (IUnit coveredMethod : entry.getValue()) {
                    // If we have a standard java project, tests are always in src/test
                    if(coveredMethod.getFilePath().startsWith("src/test")) {
                        logger.debug("Covered Test (by path): {}", coveredMethod);
                        coveredTestLines += coveredMethod.getCoveredLines();
                    } else {
                        // Fallback method: If filename has "test" or "validate" in it, it is a test
                        if (!coveredMethod.getFQNOfUnit().equals(entry.getKey().getFQNOfUnit())
                                && !Utils.isTestBasedOnFQN(coveredMethod.getFQNOfUnit())) {
                            logger.debug("Covered production code: {}", coveredMethod);
                            coveredProductionLines += coveredMethod.getCoveredLines();
                        } else {
                            logger.debug("Covered Test: {}", coveredMethod);
                            coveredTestLines += coveredMethod.getCoveredLines();
                        }
                    }
                }
            }
            logger.debug("Test {} covered {} test and {} production lines", entry.getKey(),
                    coveredTestLines, coveredProductionLines);
            Result result = new Result(entry.getKey().getFQN(), entry.getKey().getFilePath());
            result.addMetric("cov_tlines", String.valueOf(coveredTestLines));
            result.addMetric("cov_plines", String.valueOf(coveredProductionLines));
            results.add(result);
        }
        return results;
    }
}
