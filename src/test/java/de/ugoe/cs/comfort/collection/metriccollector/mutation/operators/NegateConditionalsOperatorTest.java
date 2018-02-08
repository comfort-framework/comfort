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
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Fabian Trautsch
 */
public class NegateConditionalsOperatorTest extends MutationOperatorBaseTest {
    private Path clazz;

    @Before
    public void createCopyOfFile() throws IOException {
        Path originalFile = Paths.get(getPathToResource("mutationOperatorsTestData/NegateConditionalsOperatorData.java"));

        // Copy files
        clazz = File.createTempFile("comfort-", "-NegateConditionalsOperatorTest").toPath();
        FileUtils.copyFile(originalFile.toFile(), clazz.toFile());
    }

    @Test
    public void assignmentTest() throws IOException {
        List<String> expectedValues = new ArrayList<String>() {{
            add("!=");
            add("==");
            add(">");
            add("<");
            add(">=");
            add("<=");
        }};

        int i = 7;
        for(String expectedValue: expectedValues) {
            NegateConditionalsOperator operator = new NegateConditionalsOperator(clazz, i);
            operator.changeFile();
            assertNewLineOnLineNumber(clazz, "i = i"+expectedValue+"1;", i);
            i++;
        }
    }


    @Test
    public void equalsInForLoopTest() throws IOException {
        NegateConditionalsOperator operator = new NegateConditionalsOperator(clazz, 3);
        operator.changeFile();
        assertNewLineOnLineNumber(clazz, "for(int j=0; j!=number+1; j++) {", 3);
    }

    @Test
    public void lessThanInWhileLoopTest() throws IOException {
        NegateConditionalsOperator operator = new NegateConditionalsOperator(clazz, 14);
        operator.changeFile();
        assertNewLineOnLineNumber(clazz, "while (i>=1) {", 14);
    }
}
