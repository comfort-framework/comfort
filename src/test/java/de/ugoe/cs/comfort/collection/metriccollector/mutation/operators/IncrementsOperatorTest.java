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
public class IncrementsOperatorTest extends MutationOperatorBaseTest {
    private Path clazz;

    @Before
    public void createCopyOfFile() throws IOException {
        Path originalFile = Paths.get(getPathToResource("mutationOperatorsTestData/IncrementsOperatorData.java"));

        // Copy files
        clazz = File.createTempFile("comfort-", "-IncrementsOperatorTest").toPath();
        FileUtils.copyFile(originalFile.toFile(), clazz.toFile());
    }

    @Test
    public void incrementAsCommandTest() throws IOException, MutationOperatorNotFittingException {
        IncrementsOperator operator = new IncrementsOperator();
        operator.initialize(clazz, 4);
        operator.changeFile();
        assertNewLineOnLineNumber(clazz, "i--;", 4);
    }

    @Test
    public void incrementInForLoopTest() throws IOException, MutationOperatorNotFittingException {
        IncrementsOperator operator = new IncrementsOperator();
        operator.initialize(clazz, 6);
        operator.changeFile();
        assertNewLineOnLineNumber(clazz, "for(int j=0; j<5; j--) {", 6);
    }



    @Test
    public void decrementAsCommandTest() throws IOException, MutationOperatorNotFittingException {
        IncrementsOperator operator = new IncrementsOperator();
        operator.initialize(clazz, 11);
        operator.changeFile();
        assertNewLineOnLineNumber(clazz, "l++;", 11);
    }

    @Test
    public void decrementInForLoopTest() throws IOException, MutationOperatorNotFittingException {
        IncrementsOperator operator = new IncrementsOperator();
        operator.initialize(clazz, 13);
        operator.changeFile();
        assertNewLineOnLineNumber(clazz, "for(int k=10; k>1; k++) {", 13);
    }
}
