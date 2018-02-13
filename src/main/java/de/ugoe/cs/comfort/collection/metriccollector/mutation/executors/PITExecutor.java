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

package de.ugoe.cs.comfort.collection.metriccollector.mutation.executors;

import de.ugoe.cs.comfort.collection.metriccollector.mutation.MutationExecutionResult;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Fabian Trautsch
 */
public class PITExecutor implements IMutationExecutor {
    private final Logger logger = LogManager.getLogger(this.getClass().getName());
    private static PITExecutor instance = new PITExecutor();

    public static PITExecutor getExecutor() {
        return instance;
    }

    private PITExecutor() {
    }

    @Override
    public MutationExecutionResult execute(Path projectRoot) throws IOException {
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
        ExecutorService executor = Executors.newSingleThreadExecutor();
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

    private static class PITExecutionOutputParser implements Callable<MutationExecutionResult> {
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

                        //logger.debug(pitOutputLine);

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
