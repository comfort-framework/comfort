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

import de.ugoe.cs.comfort.configuration.GeneralConfiguration;
import de.ugoe.cs.comfort.data.CoverageData;
import de.ugoe.cs.comfort.data.models.IUnit;
import de.ugoe.cs.comfort.filer.models.Result;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;

/**
 * @author Fabian Trautsch
 */
public class CoveredLinesCollectorTest extends BaseMetricCollectorTest {
    private GeneralConfiguration javaConfig = new GeneralConfiguration();
    private Set<Result> expectedResults = new HashSet<>();

    @Test
    public void coveredTestOnlyTest() throws IOException {
        javaConfig.setMethodLevel(true);

        Set<IUnit> testedMethodsOfTest1 = new HashSet<>();
        T2Test1.setCoveredLines(6);
        testedMethodsOfTest1.add(T2Test1);

        CoverageData covData = new CoverageData();
        covData.add(T1Test1, testedMethodsOfTest1);

        CoveredLinesCollector coveredLinesCollectorTest = new CoveredLinesCollector(javaConfig, filerMock);
        coveredLinesCollectorTest.getCoveredTestAndProductionLinesMethodLevel(covData);

        Result expectedResult = new Result("org.foo.t1.Test1.test1", null);
        expectedResult.addMetric("cov_tlines", "6");
        expectedResult.addMetric("cov_plines", "0");

        expectedResults.add(expectedResult);
        assertEquals("Result set is not correct!", expectedResults, filerMock.getResults().getResults());
    }

    @Test
    public void coveredItselfTest() throws IOException {
        javaConfig.setMethodLevel(true);

        Set<IUnit> testedMethodsOfTest1 = new HashSet<>();
        C1M1_p1.setCoveredLines(6);
        testedMethodsOfTest1.add(C1M1_p1);

        CoverageData covData = new CoverageData();
        covData.add(C1M1_p1, testedMethodsOfTest1);

        CoveredLinesCollector coveredLinesCollectorTest = new CoveredLinesCollector(javaConfig, filerMock);
        coveredLinesCollectorTest.getCoveredTestAndProductionLinesMethodLevel(covData);

        Result expectedResult = new Result("org.foo.bar.C1.m1", null);
        expectedResult.addMetric("cov_tlines", "6");
        expectedResult.addMetric("cov_plines", "0");

        expectedResults.add(expectedResult);
        assertEquals("Result set is not correct!", expectedResults, filerMock.getResults().getResults());
    }

    @Test
    public void coveredTestAndProductionCodeWithSeparationBasedOnClassNameTest() throws IOException {
        javaConfig.setMethodLevel(true);

        Set<IUnit> testedMethodsOfTest1 = new HashSet<>();
        T2M1.setCoveredLines(6);
        testedMethodsOfTest1.add(T2M1);

        C1M1_p1.setCoveredLines(14);
        testedMethodsOfTest1.add(C1M1_p1);


        CoverageData covData = new CoverageData();
        covData.add(T1M2, testedMethodsOfTest1);

        CoveredLinesCollector coveredLinesCollectorTest = new CoveredLinesCollector(javaConfig, filerMock);
        coveredLinesCollectorTest.getCoveredTestAndProductionLinesMethodLevel(covData);

        Result expectedResult = new Result("org.foo.t1.Test1.m2", null);
        expectedResult.addMetric("cov_tlines", "6");
        expectedResult.addMetric("cov_plines", "14");


        expectedResults.add(expectedResult);
        assertEquals("Result set is not correct!", expectedResults, filerMock.getResults().getResults());
    }

    @Test
    public void coveredTestAndProductionCodeWithSeparationBasedOnMethodNameTest() throws IOException {
        javaConfig.setMethodLevel(true);

        Set<IUnit> testedMethodsOfTest1 = new HashSet<>();
        C5Test1.setCoveredLines(6);
        testedMethodsOfTest1.add(C5Test1);

        C1M1_p1.setCoveredLines(14);
        testedMethodsOfTest1.add(C1M1_p1);


        CoverageData covData = new CoverageData();
        covData.add(T1M2, testedMethodsOfTest1);

        CoveredLinesCollector coveredLinesCollectorTest = new CoveredLinesCollector(javaConfig, filerMock);
        coveredLinesCollectorTest.getCoveredTestAndProductionLinesMethodLevel(covData);

        Result expectedResult = new Result("org.foo.t1.Test1.m2", null);
        expectedResult.addMetric("cov_tlines", "6");
        expectedResult.addMetric("cov_plines", "14");


        expectedResults.add(expectedResult);
        assertEquals("Result set is not correct!", expectedResults, filerMock.getResults().getResults());
    }

    @Test
    public void coveredTestAndProductionCodeSeveralTestsTest() throws IOException {
        javaConfig.setMethodLevel(true);

        Set<IUnit> testedMethodsOfTest1 = new HashSet<>();
        T2Test1.setCoveredLines(6);
        testedMethodsOfTest1.add(T2Test1);

        Set<IUnit> testedMethodsOfTest2 = new HashSet<>();
        C1M1_p1.setCoveredLines(14);
        testedMethodsOfTest2.add(C1M1_p1);


        CoverageData covData = new CoverageData();
        covData.add(T1M1, testedMethodsOfTest1);
        covData.add(T1M2, testedMethodsOfTest2);

        CoveredLinesCollector coveredLinesCollectorTest = new CoveredLinesCollector(javaConfig, filerMock);
        coveredLinesCollectorTest.getCoveredTestAndProductionLinesMethodLevel(covData);

        Result expectedResult1 = new Result("org.foo.t1.Test1.m1", null);
        expectedResult1.addMetric("cov_tlines", "6");
        expectedResult1.addMetric("cov_plines", "0");

        Result expectedResult2 = new Result("org.foo.t1.Test1.m2", null);
        expectedResult2.addMetric("cov_tlines", "0");
        expectedResult2.addMetric("cov_plines", "14");


        expectedResults.add(expectedResult1);
        expectedResults.add(expectedResult2);
        assertEquals("Result set is not correct!", expectedResults, filerMock.getResults().getResults());
    }

    @Test
    public void testOnClassLevelTest() throws IOException {
        javaConfig.setMethodLevel(false);

        Set<IUnit> testedMethodsOfTest1 = new HashSet<>();
        T2Test1.setCoveredLines(6);
        testedMethodsOfTest1.add(T2Test1);

        Set<IUnit> testedMethodsOfTest2 = new HashSet<>();
        C1M1_p1.setCoveredLines(14);
        testedMethodsOfTest2.add(C1M1_p1);


        CoverageData covData = new CoverageData();
        covData.add(T1M1, testedMethodsOfTest1);
        covData.add(T1M2, testedMethodsOfTest2);

        CoveredLinesCollector coveredLinesCollectorTest = new CoveredLinesCollector(javaConfig, filerMock);
        coveredLinesCollectorTest.getCoveredTestAndProductionLinesClassLevel(covData);

        Result expectedResult1 = new Result("org.foo.t1.Test1", null);
        expectedResult1.addMetric("cov_tlines", "6");
        expectedResult1.addMetric("cov_plines", "14");

        expectedResults.add(expectedResult1);
        assertEquals("Result set is not correct!", expectedResults, filerMock.getResults().getResults());
    }
}
