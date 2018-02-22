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

import de.ugoe.cs.BugFixClassifier;
import de.ugoe.cs.comfort.collection.metriccollector.mutation.operators.BaseOperator;
import de.ugoe.cs.comfort.collection.metriccollector.mutation.operators.ConditionalsBoundaryOperator;
import de.ugoe.cs.comfort.collection.metriccollector.mutation.operators.IncrementsOperator;
import de.ugoe.cs.comfort.collection.metriccollector.mutation.operators.InvertNegativesOperator;
import de.ugoe.cs.comfort.collection.metriccollector.mutation.operators.MathOperator;
import de.ugoe.cs.comfort.collection.metriccollector.mutation.operators.NegateConditionalsOperator;
import de.ugoe.cs.comfort.collection.metriccollector.mutation.operators.RemoveIncrementsOperator;
import de.ugoe.cs.comfort.exception.MutationOperatorNotFittingException;
import de.ugoe.cs.comfort.exception.MutationResultException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Fabian Trautsch
 */
public class MutationChangeClassifier {
    private static Logger logger = LogManager.getLogger("MutationChangeClassifier");
    private static Pattern dataChangePattern = Pattern.compile("\\w+\\s\\w+\\s*=\\s*.+");

    public static String getChangeClassification(Path file, String mutationOperator, int lineNumber)
            throws MutationResultException {
        try {

            // Change File based on mutationOperator on LineNumber
            BaseOperator op;
            switch (mutationOperator) {
                case "org.pitest.mutationtest.engine.gregor.mutators.ArgumentPropagationMutator":
                    return "INTERFACE";
                case "org.pitest.mutationtest.engine.gregor.mutators.BooleanFalseReturnValsMutator":
                    return "LOGIC/CONTROL";
                case "org.pitest.mutationtest.engine.gregor.mutators.BooleanTrueReturnValsMutator":
                    return "LOGIC/CONTROL";
                case "org.pitest.mutationtest.engine.gregor.mutators.ConditionalsBoundaryMutator":
                    op = new ConditionalsBoundaryOperator();
                    break;
                case "org.pitest.mutationtest.engine.gregor.mutators.ConstructorCallMutator":
                    return "INTERFACE";
                case "org.pitest.mutationtest.engine.gregor.mutators.EmptyObjectReturnValsMutator":
                    return "INTERFACE";
                case "org.pitest.mutationtest.engine.gregor.mutators.IncrementsMutator":
                    op = new IncrementsOperator();
                    break;
                case "org.pitest.mutationtest.engine.gregor.mutators.InlineConstantMutator":
                    return "DATA";
                case "org.pitest.mutationtest.engine.gregor.mutators.InvertNegsMutator":
                    op = new InvertNegativesOperator();
                    break;
                case "org.pitest.mutationtest.engine.gregor.mutators.MathMutator":
                    op = new MathOperator();
                    break;
                case "org.pitest.mutationtest.engine.gregor.mutators.NegateConditionalsMutator":
                    op = new NegateConditionalsOperator();
                    break;
                case "org.pitest.mutationtest.engine.gregor.mutators.NonVoidMethodCallMutator":
                    return "INTERFACE";
                case "org.pitest.mutationtest.engine.gregor.mutators.NullReturnValsMutator":
                    return "LOGIC/CONTROL";
                case "org.pitest.mutationtest.engine.gregor.mutators.PrimitiveReturnsMutator":
                    return "LOGIC/CONTROL";
                case "org.pitest.mutationtest.engine.gregor.mutators.RemoveConditionalMutator_EQUAL_ELSE":
                case "org.pitest.mutationtest.engine.gregor.mutators.RemoveConditionalMutator_EQUAL_IF":
                case "org.pitest.mutationtest.engine.gregor.mutators.RemoveConditionalMutator_ORDER_ELSE":
                case "org.pitest.mutationtest.engine.gregor.mutators.RemoveConditionalMutator_ORDER_IF":
                    return "LOGIC/CONTROL";
                case "org.pitest.mutationtest.engine.gregor.mutators.ReturnValsMutator":
                    return "LOGIC/CONTROL";
                case "org.pitest.mutationtest.engine.gregor.mutators.VoidMethodCallMutator":
                    return "INTERFACE";

                    // Experimental Mutators
                case "org.pitest.mutationtest.engine.gregor.mutators.experimental.MemberVariableMutator":
                    return "COMPUTATION";
                case "org.pitest.mutationtest.engine.gregor.mutators.experimental.NakedReceiverMutator":
                    return "INTERFACE";
                case "org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveIncrementsMutator":
                    op = new RemoveIncrementsOperator();
                    break;
                case "org.pitest.mutationtest.engine.gregor.mutators.experimental.ReturnValuesMutator":
                    return "LOGIC/CONTROL";
                case "org.pitest.mutationtest.engine.gregor.mutators.experimental.SwitchMutator":
                    return "LOGIC/CONTROL";
                default:
                    if(mutationOperator.contains("RemoveSwitchMutator")) {
                        return "LOGIC/CONTROL";
                    }
                    throw new MutationResultException("Unsupported mutationoperator '"+mutationOperator+"'");
            }

            // If we do not have a line number we can not change the file to get the classification
            if(lineNumber == 0) {
                return null;
            }

            // Copy files
            Path changedFile = File.createTempFile("comfort-", "-suffix").toPath();
            FileUtils.copyFile(file.toFile(), changedFile.toFile());

            // Change file based on used mutationoperator
            Map<String, Integer> results = new HashMap<>();
            try {
                op.initialize(changedFile, lineNumber);
                op.changeFile();

                // Call bugfixclassifier to get results
                results = BugFixClassifier.getBugClassifications(file, changedFile);
            } catch (MutationOperatorNotFittingException e) {
                logger.debug("Got error: {}. Falling back to backup method...", e.getMessage());
            } finally {
                Files.delete(changedFile);
            }

            //Backup method if classificaton was not successful
            if(results.size() == 0) {
                return getClassificationBasedOnChangedLineInFile(file, lineNumber);
            }
            return Collections.max(results.entrySet(), Comparator.comparingInt(Map.Entry::getValue)).getKey();
        } catch (IOException e) {
            throw new MutationResultException(e.getMessage());
        }

    }

    private static String getClassificationBasedOnChangedLineInFile(Path file, int lineNumber) throws IOException {
        // Read in File
        List<String> lines = Files.readAllLines(file);

        // Set normalized line number (because Arrays start at 0 and not 1)
        int normalizedLineNumber = lineNumber-1;

        // Get line that should be changed
        String line = lines.get(normalizedLineNumber);
        logger.debug("Reading Line: "+line);

        // We need to check if we have a data change
        Matcher matcher = dataChangePattern.matcher(line);

        // Interface changes can not occur, as they are not mutated by the mutations on which we execute this method
        if(line.contains(" return") || line.contains(" if")
                || line.contains(" while") || line.contains(" for")) {
            return "LOGIC/CONTROL";
        } else if (matcher.find()) {
            return "DATA";
        } else {
            return "COMPUTATION";
        }

    }
}
