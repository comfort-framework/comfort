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
import de.ugoe.cs.comfort.collection.metriccollector.mutation.MutationChangeClassifier;
import de.ugoe.cs.comfort.collection.metriccollector.mutation.MutationExecutionResult;
import de.ugoe.cs.comfort.configuration.GeneralConfiguration;
import de.ugoe.cs.comfort.data.CoverageData;
import de.ugoe.cs.comfort.data.models.IUnit;
import de.ugoe.cs.comfort.exception.MutationResultException;
import de.ugoe.cs.comfort.filer.models.Mutation;
import de.ugoe.cs.comfort.filer.models.Result;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.core.lookup.StrSubstitutor;

/**
 * @author Fabian Trautsch
 */
public class MutationDataCollector extends BaseMetricCollector {
    public MutationDataCollector(GeneralConfiguration configuration) {
        super(configuration);
    }

    @SupportsMethod
    @SupportsJava
    public Set<Result> getMutationDataMetrics(CoverageData data) {
        Set<Result> results = new HashSet<>();
        ExecutorService executor = Executors.newFixedThreadPool(4);

        for (Map.Entry<IUnit, Set<IUnit>> entry : data.getCoverageData().entrySet()) {
            String className = entry.getKey().getFQNOfUnit();
            String[] parts = entry.getKey().getFQN().split("\\.");
            String methodName = parts[parts.length-1];
            logger.info("Getting mutation data for "+className+"."+methodName);

            try {
                Result result = new Result(entry.getKey().getFQN(),
                        fileNameUtils.getPathForIdentifier(entry.getKey().getFQN(), generalConf.getMethodLevel()));

                // Create new pom with corresponding values
                createNewPom(generalConf.getProjectDir(), className, methodName);

                // Execute Pitest
                MutationExecutionResult mutationExecutionResult = executePitest(generalConf.getProjectDir(), executor);

                // If execution was successful, we will read the results
                Set<Mutation> mutationResults = readPitestResults(generalConf.getProjectDir());

                // And add these results to this specific test
                result.addMutationResults(mutationResults);
                result.addMetric("mut_genMut", String.valueOf(mutationExecutionResult.getGeneratedMutations()));
                result.addMetric("mut_killMut", String.valueOf(mutationExecutionResult.getKilledMutations()));
                result.addMetric("mut_scoreMut", String.valueOf(mutationExecutionResult.getMutationScore()));

                results.add(result);
            } catch (IOException e) {
                // If not successful, print error but it is not necessary to cancel here
                logger.warn("Error \"{}\" for executing mutation testing for test {}", e.getMessage(),
                        entry.getKey().getFQN());
            }
        }
        return results;
    }

    private Set<Mutation> readPitestResults(Path projectRoot) throws IOException {
        String line;
        Set<Mutation> mutationResults = new HashSet<>();

        // Read out pitest results
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(
                Paths.get(generalConf.getProjectDir().toString(),"target", "pit-reports", "mutations.csv").toFile()),
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
                if (!result.equals("NO_COVERAGE") && lineNumber != 0) {
                    changeClassification = MutationChangeClassifier.getChangeClassification(
                            generalConf.getProjectDir(), cols[0], mutationOperator, lineNumber
                    );
                }
            } catch (MutationResultException e) {
                logger.catching(e);
            }
            mutationResults.add(new Mutation(location, mutationOperator, lineNumber, result, changeClassification));
        }
        br.close();

        return mutationResults;
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
        Files.write(Paths.get(generalConf.getProjectDir().toString(), "pom.xml"), resolvedString.getBytes("UTF-8"));
    }

    private MutationExecutionResult executePitest(Path projectRoot, ExecutorService executor) throws IOException {
        logger.info("Executing Pitest...");

        // monkeypatching for mybatis-3
        if(Files.exists(Paths.get(projectRoot.toString(), "ibderby"))) {
            FileUtils.forceDelete(Paths.get(projectRoot.toString(), "ibderby").toFile()); //delete directory
        }

        ProcessBuilder builder = new ProcessBuilder("mvn", "org.pitest:pitest-maven:mutationCoverage");
        builder.redirectErrorStream(true);
        builder.directory(new File(projectRoot.toString()));

        int exitCode;
        Future<MutationExecutionResult> future;
        try {
            Process piTest = builder.start();
            PITExecutionOutputParser outputParser =
                    new PITExecutionOutputParser(piTest.getInputStream());
            future = executor.submit(outputParser);
            logger.debug("Waiting to finish...");
            exitCode = piTest.waitFor();
            logger.debug("Finished!");
        } catch (IOException | InterruptedException e) {
            throw new IOException("Problem with execution!");
        }

        // Check if it returned successfully
        if (exitCode != 0) {
            throw new IOException("Error in executing loader: Program did not terminate with code 0");
        }

        MutationExecutionResult mutationExecutionResult;
        try {
            mutationExecutionResult = future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new IOException("Error in parsing results!");
        }
        logger.info("Execution successful...");

        if(mutationExecutionResult.getNumTests() != 1) {
            throw new IOException("Not only one test found by pit!");
        }

        if(mutationExecutionResult.getNumMutationUnits() < 1) {
            throw new IOException("No mutation units generated!");
        }

        logger.debug("Mutation Execution result: {}", mutationExecutionResult);

        return mutationExecutionResult;
    }

    private class PITExecutionOutputParser implements Callable<MutationExecutionResult> {
        private InputStream inputStream;

        Pattern receivedTestsPattern = Pattern.compile("(\\S*)(\\d*) tests received");
        Pattern mutationUnitsPattern = Pattern.compile("(\\S*)(\\d*) mutation test units");
        Pattern executionTimePattern = Pattern.compile("(\\S*) Completed in (\\d*) seconds");
        Pattern mutationScorePattern = Pattern.compile("(\\S*) Generated (\\d*) mutations Killed (\\d*) \\((\\d*)%\\)");

        PITExecutionOutputParser(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public MutationExecutionResult call() {
            MutationExecutionResult mutationExecutionResult = new MutationExecutionResult();
            try{
                parsePitExecutionOutput(mutationExecutionResult);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return mutationExecutionResult;
        }

        private void parsePitExecutionOutput(MutationExecutionResult mutationExecutionResult)
                throws UnsupportedEncodingException {
            new BufferedReader(new InputStreamReader(inputStream , "UTF-8")).lines()
                    .forEach(pitOutputLine -> {
                        Matcher receivedTestsMatcher = receivedTestsPattern.matcher(pitOutputLine);
                        Matcher mutationUnitsMatcher = mutationUnitsPattern.matcher(pitOutputLine);
                        Matcher executionTimeMatcher = executionTimePattern.matcher(pitOutputLine);
                        Matcher mutationScoreMatcher = mutationScorePattern.matcher(pitOutputLine);

                        logger.debug(pitOutputLine);

                        if(receivedTestsMatcher.find()) {
                            mutationExecutionResult.setNumTests(Integer.parseInt(receivedTestsMatcher.group(1)));
                        }

                        if(mutationUnitsMatcher.find()) {
                            mutationExecutionResult
                                    .setNumMutationUnits(Integer.parseInt(mutationUnitsMatcher.group(1)));
                        }

                        if(executionTimeMatcher.find()) {
                            mutationExecutionResult.setExecutionTime(Integer.parseInt(executionTimeMatcher.group(2)));
                        }

                        if(mutationScoreMatcher.find()) {
                            mutationExecutionResult
                                    .setGeneratedMutations(Integer.parseInt(mutationScoreMatcher.group(2)));
                            mutationExecutionResult.setKilledMutations(Integer.parseInt(mutationScoreMatcher.group(3)));
                            mutationExecutionResult.setMutationScore(Integer.parseInt(mutationScoreMatcher.group(4)));
                        }
                    });
        }
    }
}
