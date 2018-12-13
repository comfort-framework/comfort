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

package de.ugoe.cs.comfort.data;

import com.google.common.base.MoreObjects;
import de.ugoe.cs.comfort.Utils;
import de.ugoe.cs.comfort.data.models.IUnit;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Fabian Trautsch
 */
public class CoverageData extends DataSet {
    private Map<IUnit, Set<IUnit>> covfefeMethodLevel = new HashMap<>();
    private Map<IUnit, Set<IUnit>> covfefeClassLevel = new HashMap<>();

    public CoverageData() {
    }

    public void add(IUnit testMethod, Set<IUnit> testedMethods) {
        covfefeMethodLevel.put(testMethod, testedMethods);

        // As we also need the coverage on class level, we need to cast it here
        try {
            IUnit classLevelUnit = (IUnit) testMethod.getClass().getSuperclass()
                    .getConstructor(String.class, Path.class).newInstance(
                            testMethod.getFQNOfUnit(), testMethod.getFilePath()
                    );
            Set<IUnit> coveredClasses = covfefeClassLevel.getOrDefault(classLevelUnit, new HashSet<>());

            if(testedMethods != null) {
                coveredClasses.addAll(testedMethods);
            }
            covfefeClassLevel.put(classLevelUnit, coveredClasses);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException
            | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("coverage", covfefeMethodLevel)
                .toString();
    }

    public Map<IUnit, Set<IUnit>> getCoverageData() {
        // Return coverage data without test methods and classes
        return filterOutCoveredTests(covfefeMethodLevel);
    }

    public Map<IUnit, Set<IUnit>> getCoverageDataClassLevel() {
        return filterOutCoveredTests(covfefeClassLevel);
    }

    public Map<IUnit, Set<IUnit>> getCoverageDataForAll() {
        return covfefeMethodLevel;
    }

    public Map<IUnit, Set<IUnit>> getCoverageDataForAllClassLevel() {
        return covfefeClassLevel;
    }

    private Map<IUnit, Set<IUnit>> filterOutCoveredTests(Map<IUnit, Set<IUnit>> data) {
        // Here we filter out covered tests, as we only want to have return production code coverage and not test data
        // coverage
        Map<IUnit, Set<IUnit>> coverageWithoutTests = new HashMap<>();
        for(Map.Entry<IUnit, Set<IUnit>> entry: data.entrySet()) {
            Set<IUnit> coveredMethodsWithoutTests = new HashSet<>();
            if(entry.getValue() != null) {
                for (IUnit coveredMethod : entry.getValue()) {
                    if (!coveredMethod.getFilePath().toString().contains("test")
                            && !coveredMethod.getFQNOfUnit().equals(entry.getKey().getFQNOfUnit())
                            && !Utils.isTestBasedOnFQN(coveredMethod.getFQNOfUnit())) {
                        coveredMethodsWithoutTests.add(coveredMethod);
                    }
                }
                coverageWithoutTests.put(entry.getKey(), coveredMethodsWithoutTests);
            } else {
                coverageWithoutTests.put(entry.getKey(), null);
            }
        }
        return coverageWithoutTests;
    }
}
