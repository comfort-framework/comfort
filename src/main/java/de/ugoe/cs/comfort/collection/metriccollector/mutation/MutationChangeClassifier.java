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
import de.ugoe.cs.comfort.Utils;
import de.ugoe.cs.comfort.collection.metriccollector.mutation.operators.BaseOperator;
import de.ugoe.cs.comfort.collection.metriccollector.mutation.operators.ConditionalsBoundaryOperator;
import de.ugoe.cs.comfort.collection.metriccollector.mutation.operators.IncrementsOperator;
import de.ugoe.cs.comfort.collection.metriccollector.mutation.operators.InvertNegativesOperator;
import de.ugoe.cs.comfort.collection.metriccollector.mutation.operators.MathOperator;
import de.ugoe.cs.comfort.collection.metriccollector.mutation.operators.NegateConditionalsOperator;
import de.ugoe.cs.comfort.collection.metriccollector.mutation.operators.RemoveIncrementsOperator;
import de.ugoe.cs.comfort.exception.MutationResultException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import org.apache.commons.io.FileUtils;

/**
 * @author Fabian Trautsch
 */
public class MutationChangeClassifier {
    private static Path findFileBasedOnName(Path projectDir, String name) throws IOException {
        Set<Path> foundFiles = Utils.getAllFilesFromProjectForRegex(projectDir, name + "$");
        if(foundFiles.size() > 1) {
            throw new IOException("More than one file with name "+name+" found in project directory!");
        } else {
            return foundFiles.iterator().next();
        }
    }

    public static String getChangeClassification(Path projectDir, String fileName,
                                                 String mutationOperator, int lineNumber)
            throws MutationResultException {
        try {
            // Get  filename
            Path file = findFileBasedOnName(projectDir, fileName);

            // Copy files
            Path changedFile = File.createTempFile("comfort-", "-suffix").toPath();
            FileUtils.copyFile(file.toFile(), changedFile.toFile());

            // Change File based on mutationOperator on LineNumber
            BaseOperator operator = null;
            switch (mutationOperator) {
                case "org.pitest.mutationtest.engine.gregor.mutators.ArgumentPropagationMutator":
                    return "INTERFACE";
                case "org.pitest.mutationtest.engine.gregor.mutators.BooleanFalseReturnValsMutator":
                    return "LOGIC/CONTROL";
                case "org.pitest.mutationtest.engine.gregor.mutators.BooleanTrueReturnValsMutator":
                    return "LOGIC/CONTROL";
                case "org.pitest.mutationtest.engine.gregor.mutators.ConditionalsBoundaryMutator":
                    operator = new ConditionalsBoundaryOperator(changedFile, lineNumber);
                    break;
                case "org.pitest.mutationtest.engine.gregor.mutators.ConstructorCallMutator":
                    return "INTERFACE";
                case "org.pitest.mutationtest.engine.gregor.mutators.EmptyObjectReturnValsMutator":
                    return "INTERFACE";
                case "org.pitest.mutationtest.engine.gregor.mutators.IncrementsMutator":
                    operator = new IncrementsOperator(changedFile, lineNumber);
                    break;
                case "org.pitest.mutationtest.engine.gregor.mutators.InlineConstantMutator":
                    return "DATA";
                case "org.pitest.mutationtest.engine.gregor.mutators.InvertNegsMutator":
                    operator = new InvertNegativesOperator(changedFile, lineNumber);
                    break;
                case "org.pitest.mutationtest.engine.gregor.mutators.MathMutator":
                    operator = new MathOperator(changedFile, lineNumber);
                    break;
                case "org.pitest.mutationtest.engine.gregor.mutators.NegateConditionalsMutator":
                    operator = new NegateConditionalsOperator(changedFile, lineNumber);
                    break;
                case "org.pitest.mutationtest.engine.gregor.mutators.NonVoidMethodCallMutator":
                    return "INTERFACE";
                case "org.pitest.mutationtest.engine.gregor.mutators.NullReturnValsMutator":
                    return "LOGIC/CONTROL";
                case "org.pitest.mutationtest.engine.gregor.mutators.PrimitiveReturnsMutator":
                    return "LOGIC/CONTROL";
                case "org.pitest.mutationtest.engine.gregor.mutators.RemoveConditionalMutator":
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
                    operator = new RemoveIncrementsOperator(changedFile, lineNumber);
                    break;
                case "org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator":
                    return "LOGIC/CONTROL";
                case "org.pitest.mutationtest.engine.gregor.mutators.experimental.ReturnValuesMutator":
                    return "LOGIC/CONTROL";
                case "org.pitest.mutationtest.engine.gregor.mutators.experimental.SwitchMutator":
                    operator = new RemoveIncrementsOperator(changedFile, lineNumber);
                    break;
                default:
                    throw new MutationResultException("Unsupported mutationoperator '"+mutationOperator+"'");

            }
            // Change file based on operator. E.g., RemoveIncrementsOperator removes "--" or "++" from line.
            operator.changeFile();

            // Call bugfixclassifier to get results
            Map<String, Integer> results = BugFixClassifier.getBugClassifications(file, changedFile);

            Files.delete(changedFile);

            // If no results were found, there was some kind of error...
            if(results.size() == 0) {
                throw new MutationResultException("No results calculated!");
            }

            // Get the result with the highest count
            return Collections.max(results.entrySet(), Comparator.comparingInt(Map.Entry::getValue)).getKey();

        } catch (IOException e) {
            throw new MutationResultException(e.getMessage());
        }

    }
}
