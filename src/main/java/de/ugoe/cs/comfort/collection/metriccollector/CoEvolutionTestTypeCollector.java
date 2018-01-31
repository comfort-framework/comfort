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

import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multisets;
import de.ugoe.cs.comfort.annotations.SupportsClass;
import de.ugoe.cs.comfort.annotations.SupportsJava;
import de.ugoe.cs.comfort.annotations.SupportsPython;
import de.ugoe.cs.comfort.configuration.GeneralConfiguration;
import de.ugoe.cs.comfort.data.ChangeSet;
import de.ugoe.cs.comfort.filer.models.Result;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Fabian Trautsch
 */
public class CoEvolutionTestTypeCollector extends BaseMetricCollector {
    // TODO: Take as configuration parameter, maybe also for detecting integration tests
    private static final Integer DIFFERENCE = 1;

    public CoEvolutionTestTypeCollector(GeneralConfiguration configuration) {
        super(configuration);
    }

    @SupportsJava
    @SupportsPython
    @SupportsClass
    public Set<Result> createResults(ChangeSet changeSet) {
        // Create filer map
        Set<Result> result = new HashSet<>();
        for(Path testFile: changeSet.getTestFiles()) {
            ImmutableMultiset<Path> sortedChangeSet = Multisets
                    .copyHighestCountFirst(changeSet.getChangedWithFiles(testFile));
            result.add(classifyTestFile(testFile, sortedChangeSet));
        }

        return result;
    }

    private Result classifyTestFile(Path testFile, ImmutableMultiset<Path> changeSetOfTestFile) {
        Result result = new Result(testFile.toString(), testFile);
        if(changeSetOfTestFile.isEmpty()) {
            logger.warn("Could not classify {}, as changeset is empty.", testFile);
            result.addMetric("change_coevo", TestType.UNKNOWN.name());
            return result;
        }

        // If it is only one element that was changed together with the test, we classify it as a unit test
        if(changeSetOfTestFile.elementSet().size() == 1) {
            logger.debug("{} is a unit test because of the following change set: {}.", testFile, changeSetOfTestFile);
            result.addMetric("change_coevo", TestType.UNIT.name());
            return result;
        }

        // If the difference between the number of changes that the test file had together with the most often
        // changed item and the second most often changed item (that were changed together with the test) is
        // bigger than DIFFERENCE, we define the test as unit test
        Integer firstItemCount = changeSetOfTestFile.entrySet().asList().get(0).getCount();
        Integer secondItemCount = changeSetOfTestFile.entrySet().asList().get(1).getCount();
        if((firstItemCount - secondItemCount) >= DIFFERENCE) {
            logger.debug("{} is a unit test because of the following change set: {}.", testFile, changeSetOfTestFile);
            result.addMetric("change_coevo", TestType.UNIT.name());
            return result;
        }

        // Otherwise, we do not know what to do
        logger.warn("Could not classify {}.", testFile);
        result.addMetric("change_coevo", TestType.UNKNOWN.name());
        return result;
    }
}
