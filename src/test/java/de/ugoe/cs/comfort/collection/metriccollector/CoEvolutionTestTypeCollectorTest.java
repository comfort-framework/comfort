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

import static org.junit.Assert.assertEquals;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import de.ugoe.cs.comfort.BaseTest;
import de.ugoe.cs.comfort.configuration.GeneralConfiguration;
import de.ugoe.cs.comfort.data.ChangeSet;
import de.ugoe.cs.comfort.filer.models.Result;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Fabian Trautsch
 */
public class CoEvolutionTestTypeCollectorTest extends BaseTest {
    private Map<Path, Multiset<Path>> changeMap = new HashMap<>();
    private GeneralConfiguration configuration = new GeneralConfiguration();

    private void addToChangeMap(String testName, String[] changedWith) {
        Multiset<Path> changedWithTest = HashMultiset.create();
        Arrays.stream(changedWith).forEach(file -> changedWithTest.add(Paths.get(file)));

        changeMap.put(Paths.get(testName), changedWithTest);
    }

    @Before
    public void clearTestData() {
        changeMap.clear();
    }

    @Test
    public void classifyTest() {
        // Create data for test
        String[] changedWithTest1 = {"fileA", "fileB", "fileB"};
        addToChangeMap("test1", changedWithTest1);

        String[] changedWithTest2 = {"fileA", "fileB"};
        addToChangeMap("test2", changedWithTest2);

        // Set into dataset
        ChangeSet changeSet = new ChangeSet(changeMap);

        // Expected results
        Set<Result> expectedResult = new HashSet<>();
        expectedResult.add(new Result(Paths.get("test1").toString(), Paths.get("test1"), "change_coevo", TestType.UNIT.name()));
        expectedResult.add(new Result(Paths.get("test2").toString(), Paths.get("test2"), "change_coevo", TestType.UNKNOWN.name()));
        executeCollector(changeSet, expectedResult);


    }

    private void executeCollector(ChangeSet changeSet, Set<Result> expectedResult) {
        // Execute strategy
        CoEvolutionTestTypeCollector coEvolutionTestTypeCollector = new CoEvolutionTestTypeCollector(configuration);
        Set<Result> result = coEvolutionTestTypeCollector.createResults(changeSet);

        assertEquals("size not equal", expectedResult.size(), result.size());
        assertEquals("detection is wrong", expectedResult, result);

    }

    @Test
    public void classifyWithEmptyChangeSet() {
        // Create data for test
        String[] changedWithTest1 = {};
        addToChangeMap("test1", changedWithTest1);

        // Set into dataset
        ChangeSet changeSet = new ChangeSet(changeMap);

        // Expected results
        Set<Result> expectedResult = new HashSet<>();
        expectedResult.add(new Result(Paths.get("test1").toString(), Paths.get("test1"), "change_coevo", TestType.UNKNOWN.name()));

        // Execute strategy
        executeCollector(changeSet, expectedResult);
    }

    @Test
    public void classifyWithChangeSetOfSizeOne() {
        // Create data for test
        String[] changedWithTest1 = {"fileA"};
        addToChangeMap("test1", changedWithTest1);

        // Set into dataset
        ChangeSet changeSet = new ChangeSet(changeMap);

        // Expected results
        Set<Result> expectedResult = new HashSet<>();
        expectedResult.add(new Result(Paths.get("test1").toString(), Paths.get("test1"), "change_coevo", TestType.UNIT.name()));

        // Execute strategy
        executeCollector(changeSet, expectedResult);
    }
}
