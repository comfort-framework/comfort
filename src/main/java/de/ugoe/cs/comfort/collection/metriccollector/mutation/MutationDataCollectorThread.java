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
import de.ugoe.cs.comfort.collection.metriccollector.mutation.executors.PITExecutor;
import de.ugoe.cs.comfort.configuration.GeneralConfiguration;
import de.ugoe.cs.comfort.data.models.IUnit;
import de.ugoe.cs.comfort.exception.MutationResultException;
import de.ugoe.cs.comfort.filer.models.Mutation;
import de.ugoe.cs.comfort.filer.models.Result;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.lookup.StrSubstitutor;

/**
 * @author Fabian Trautsch
 */
public class MutationDataCollectorThread implements Runnable {
    private final String threadName = Thread.currentThread().getName();
    private final Logger logger = LogManager.getLogger(this.getClass().getName());

    private IUnit unit;
    private GeneralConfiguration generalConf;
    private FileNameUtils fileNameUtils;
    private Set<Result> results;



    public MutationDataCollectorThread(IUnit unit, GeneralConfiguration generalConf, FileNameUtils fileNameUtils,
                                       Set<Result> results) {
        this.unit = unit;
        this.generalConf = generalConf;
        this.fileNameUtils = fileNameUtils;
        this.results = results;
    }

    @Override
    public void run() {
        String className = unit.getFQNOfUnit();
        String[] parts = unit.getFQN().split("\\.");
        String methodName = parts[parts.length-1];
        logger.info("{} - Getting mutation data for {}.{}", threadName, className, methodName);

        try {
            Result result = new Result(unit.getFQN(),
                    fileNameUtils.getPathForIdentifier(unit.getFQN(), generalConf.getMethodLevel()));

            // Copy project folder
            Path tempDir = Files.createTempDirectory("comfort-");
            FileUtils.copyDirectory(generalConf.getProjectDir().toFile(), tempDir.toFile());

            // Create new pom with corresponding values
            createNewPom(tempDir, className, methodName);

            // Execute Pitest
            MutationExecutionResult mutationExecutionResult = PITExecutor.getExecutor().execute(tempDir);

            // If execution was successful, we will read the results
            Set<Mutation> mutationResults = readPitestResults(tempDir);

            // And add these results to this specific test
            result.addMutationResults(mutationResults);
            result.addMetric("mut_genMut", String.valueOf(mutationExecutionResult.getGeneratedMutations()));
            result.addMetric("mut_killMut", String.valueOf(mutationExecutionResult.getKilledMutations()));
            result.addMetric("mut_scoreMut", String.valueOf(mutationExecutionResult.getMutationScore()));

            results.add(result);
        } catch (IOException e) {
            // If not successful, print error but it is not necessary to cancel here
            logger.warn("Error \"{}\" for executing mutation testing for test {}", e.getMessage(),
                    unit.getFQN());
        }
    }



    private void createNewPom(Path projectRoot, String className, String methodName) throws IOException {
        Path template = Paths.get(projectRoot.toString(), "pom_template.xml");

        // Read tamplate
        String content = new String(Files.readAllBytes(template), StandardCharsets.UTF_8);

        // Substitute placeholder with the correct test name that should be tested
        Map<String, String> valuesMap = new HashMap<>();
        valuesMap.put("pitclass", className);
        valuesMap.put("pitmethod", methodName);
        StrSubstitutor sub = new StrSubstitutor(valuesMap);
        String resolvedString = sub.replace(content);

        // Store as pom.xml
        Files.write(Paths.get(projectRoot.toString(), "pom.xml"), resolvedString.getBytes("UTF-8"));
    }

    private Set<Mutation> readPitestResults(Path projectRoot) throws IOException {
        String line;
        Set<Mutation> mutationResults = new HashSet<>();

        // Read out pitest results
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(
                Paths.get(projectRoot.toString(),"target", "pit-reports", "mutations.csv").toFile()),
                StandardCharsets.UTF_8));
        while ((line = br.readLine()) != null) {
            String[] cols = line.split(",");
            logger.debug("Result Line: {}", line);
            String location = cols[1]+"."+cols[3];
            String mutationOperator = cols[2];
            int lineNumber =  Integer.parseInt(cols[4]);
            String result = cols[5];

            // Try to get a change clasification for the mutation
            // But we catch the exceptions here, as this kind of data is not crucial
            String changeClassification = null;
            try {
                changeClassification = MutationChangeClassifier.getChangeClassification(
                        projectRoot, cols[0], mutationOperator, lineNumber
                );
                logger.debug("Got the following change classification {}", changeClassification);
            } catch (MutationResultException e) {
                logger.catching(e);
            }
            mutationResults.add(new Mutation(location, mutationOperator, lineNumber, result, changeClassification));
        }
        br.close();

        return mutationResults;
    }
}
