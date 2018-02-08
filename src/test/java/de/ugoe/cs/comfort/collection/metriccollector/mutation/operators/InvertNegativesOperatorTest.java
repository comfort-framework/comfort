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
public class InvertNegativesOperatorTest extends MutationOperatorBaseTest {
    private Path clazz;

    @Before
    public void createCopyOfFile() throws IOException {
        Path originalFile = Paths.get(getPathToResource("mutationOperatorsTestData/InvertNegativesOperatorData.java"));

        // Copy files
        clazz = File.createTempFile("comfort-", "-InvertNegativesOperatorTest").toPath();
        FileUtils.copyFile(originalFile.toFile(), clazz.toFile());
    }

    @Test
    public void negativeInLoopTest() throws IOException {
        InvertNegativesOperator operator = new InvertNegativesOperator(clazz, 3);
        operator.changeFile();
        assertNewLineOnLineNumber(clazz, "for(int j=k; j<number+1; j--) {", 3);
    }

    @Test
    public void negativeInInitializationTest() throws IOException {
        InvertNegativesOperator operator = new InvertNegativesOperator(clazz, 7);
        operator.changeFile();
        assertNewLineOnLineNumber(clazz, "int j = k;", 7);
    }

    @Test
    public void negativeInAssignmentTest() throws IOException {
        InvertNegativesOperator operator = new InvertNegativesOperator(clazz, 8);
        operator.changeFile();
        assertNewLineOnLineNumber(clazz, "i = j;", 8);
    }

    @Test
    public void negativeInReturnTest() throws IOException {
        InvertNegativesOperator operator = new InvertNegativesOperator(clazz, 9);
        operator.changeFile();
        assertNewLineOnLineNumber(clazz, "return i;", 9);
    }
}
