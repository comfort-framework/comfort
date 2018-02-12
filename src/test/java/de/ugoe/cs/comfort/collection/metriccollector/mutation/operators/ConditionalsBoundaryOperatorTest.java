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

package de.ugoe.cs.comfort.collection.metriccollector.mutation.operators;

import de.ugoe.cs.comfort.collection.metriccollector.mutation.MutationOperatorBaseTest;
import de.ugoe.cs.comfort.exception.MutationOperatorNotFittingException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Fabian Trautsch
 */
public class ConditionalsBoundaryOperatorTest extends MutationOperatorBaseTest {
    private Path clazz;

    @Before
    public void createCopyOfFile() throws IOException {
        Path originalFile = Paths.get(getPathToResource("mutationOperatorsTestData/ConditionalsBoundaryOperatorData.java"));

        // Copy files
        clazz = File.createTempFile("comfort-", "-ConditionalsBoundaryOperatorTest").toPath();
        FileUtils.copyFile(originalFile.toFile(), clazz.toFile());
    }

    @Test
    public void lessThanTest() throws IOException, MutationOperatorNotFittingException {
        ConditionalsBoundaryOperator operator = new ConditionalsBoundaryOperator();
        operator.initialize(clazz, 3);
        operator.changeFile();
        assertNewLineOnLineNumber(clazz, "if(number <= 5 && number<=6) {", 3);
    }

    @Test
    public void lessThanEqualTest() throws IOException, MutationOperatorNotFittingException {
        ConditionalsBoundaryOperator operator = new ConditionalsBoundaryOperator();
        operator.initialize(clazz, 7);
        operator.changeFile();
        assertNewLineOnLineNumber(clazz, "if(number < 5) {", 7);
    }

    @Test
    public void greaterThanTest() throws IOException, MutationOperatorNotFittingException {
        ConditionalsBoundaryOperator operator = new ConditionalsBoundaryOperator();
        operator.initialize(clazz, 11);
        operator.changeFile();
        assertNewLineOnLineNumber(clazz, "if(number >= 5) {", 11);
    }

    @Test
    public void greaterThanEqualTest() throws IOException, MutationOperatorNotFittingException {
        ConditionalsBoundaryOperator operator = new ConditionalsBoundaryOperator();
        operator.initialize(clazz, 15);
        operator.changeFile();
        assertNewLineOnLineNumber(clazz, "if(number >5) {", 15);
    }

}
