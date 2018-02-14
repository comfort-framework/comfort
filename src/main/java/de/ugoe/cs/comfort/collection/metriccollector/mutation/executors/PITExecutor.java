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

import de.ugoe.cs.comfort.collection.metriccollector.mutation.MutationChangeClassifier;
import de.ugoe.cs.comfort.collection.metriccollector.mutation.MutationExecutionResult;
import de.ugoe.cs.comfort.exception.MutationResultException;
import de.ugoe.cs.comfort.filer.models.Mutation;
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
import java.util.Collections;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.lookup.StrSubstitutor;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationOutputHandler;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;

/**
 * @author Fabian Trautsch
 */
public class PITExecutor implements IMutationExecutor {
    private final Logger logger = LogManager.getLogger(this.getClass().getName());

    private Path pitReportFolder;

    private void createNewPom(Path projectRoot, Path newPom, String className, String methodName) throws IOException {
        Path template = Paths.get(projectRoot.toString(), "pom_template.xml");
        pitReportFolder = Files.createTempDirectory("comfort-");

        // Read template
        String content = new String(Files.readAllBytes(template), StandardCharsets.UTF_8);

        // Substitute placeholder with the correct test name that should be tested
        Map<String, String> valuesMap = new HashMap<>();
        valuesMap.put("pitclass", className);
        valuesMap.put("pitmethod", methodName);
        valuesMap.put("pitreport", pitReportFolder.toString());

        StrSubstitutor sub = new StrSubstitutor(valuesMap);
        String resolvedString = sub.replace(content);

        // Store as pom.xml
        Files.write(newPom, resolvedString.getBytes("UTF-8"));
    }

    @Override
    public MutationExecutionResult execute(Path projectRoot, String className, String methodName) throws IOException {
        try {
            // Create newPOMFile
            Path newPomFile = File.createTempFile("comfort-", ".xml", projectRoot.toFile()).toPath();

            // Create new pom with corresponding values
            createNewPom(projectRoot, newPomFile, className, methodName);

            logger.info("Executing Pitest...");

            // monkeypatching for mybatis-3
            if (Files.exists(Paths.get(projectRoot.toString(), "ibderby"))) {
                FileUtils.forceDelete(Paths.get(projectRoot.toString(), "ibderby").toFile()); //delete directory
            }

            // Create request -> pitest execute
            InvocationRequest request = new DefaultInvocationRequest();
            request.setPomFile(newPomFile.toFile());
            request.setGoals(Collections.singletonList("org.pitest:pitest-maven:mutationCoverage"));

            // Create new invoker and set attributes + outputparser
            PITExecutionOutputParser outputParser = new PITExecutionOutputParser();
            Invoker invoker = new DefaultInvoker();
            invoker.setOutputHandler(outputParser);
            invoker.setErrorHandler(outputParser);

            // Invoke
            InvocationResult result = invoker.execute(request);
            Files.delete(newPomFile);

            // Check if it returned successfully
            if (result.getExitCode() != 0) {
                throw new IOException("Error in executing loader: Program did not terminate with code 0");
            }


            MutationExecutionResult mutationExecutionResult = outputParser.getMutationExecutionResult();


            if (mutationExecutionResult.getNumTests() != 1) {
                throw new IOException("Not only one test found by pit!");
            }

            if (mutationExecutionResult.getNumMutationUnits() < 1) {
                throw new IOException("No mutation units generated!");
            }

            logger.debug("Mutation Execution result: {}", mutationExecutionResult);
            return mutationExecutionResult;
        } catch (MavenInvocationException e) {
            throw new IOException("Maven execution not successful!");
        }
    }

    @Override
    public Set<Mutation> getDetailedResults(Path projectRoot) throws IOException {
        String line;
        Set<Mutation> mutationResults = new HashSet<>();

        // Read out pitest results
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(
                Paths.get(pitReportFolder.toString(), "mutations.csv").toFile()),
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

    private static class PITExecutionOutputParser implements InvocationOutputHandler {
        MutationExecutionResult mutationExecutionResult = new MutationExecutionResult();

        Pattern receivedTestsPattern = Pattern.compile("(\\S*)(\\d*) tests received");
        Pattern mutationUnitsPattern = Pattern.compile("(\\S*)(\\d*) mutation test units");
        Pattern executionTimePattern = Pattern.compile("(\\S*) Completed in (\\d*) seconds");
        Pattern mutationScorePattern = Pattern.compile("(\\S*) Generated (\\d*) mutations Killed (\\d*) \\((\\d*)%\\)");

        @Override
        public void consumeLine(String line) {

            Matcher receivedTestsMatcher = receivedTestsPattern.matcher(line);
            Matcher mutationUnitsMatcher = mutationUnitsPattern.matcher(line);
            Matcher executionTimeMatcher = executionTimePattern.matcher(line);
            Matcher mutationScoreMatcher = mutationScorePattern.matcher(line);

            if(receivedTestsMatcher.find()) {
                mutationExecutionResult.setNumTests(Integer.parseInt(receivedTestsMatcher.group(1)));
            }

            if(mutationUnitsMatcher.find()) {
                mutationExecutionResult.setNumMutationUnits(Integer.parseInt(mutationUnitsMatcher.group(1)));
            }

            if(executionTimeMatcher.find()) {
                mutationExecutionResult.setExecutionTime(Integer.parseInt(executionTimeMatcher.group(2)));
            }

            if(mutationScoreMatcher.find()) {
                mutationExecutionResult.setGeneratedMutations(Integer.parseInt(mutationScoreMatcher.group(2)));
                mutationExecutionResult.setKilledMutations(Integer.parseInt(mutationScoreMatcher.group(3)));
                mutationExecutionResult.setMutationScore(Integer.parseInt(mutationScoreMatcher.group(4)));
            }
        }

        public MutationExecutionResult getMutationExecutionResult() {
            return mutationExecutionResult;
        }
    }
}