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

import de.ugoe.cs.comfort.BaseTest;
import de.ugoe.cs.comfort.configuration.GeneralConfiguration;
import de.ugoe.cs.comfort.data.CoverageData;
import de.ugoe.cs.comfort.data.ProjectFiles;
import de.ugoe.cs.comfort.data.models.IUnit;
import de.ugoe.cs.comfort.data.models.JavaMethod;
import de.ugoe.cs.comfort.filer.models.Result;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Fabian Trautsch
 */
public class NamingConventionTestTypeCollectorTest extends BaseMetricCollectorTest {
    private final String basePath = getPathToResource("metricCollectorTestData/numassertcollector");

    private GeneralConfiguration configuration = new GeneralConfiguration();

    @Before
    public void setUp() {
    }

    @Test
    public void classificationTest() throws IOException {
        HashSet<Path> codeFiles = new HashSet<>();
        codeFiles.add(Paths.get("src/main/java/org/foo/Main.java"));
        codeFiles.add(Paths.get("src/main/java/org/foo/models/Person.java"));
        codeFiles.add(Paths.get("src/main/java/org/foo/models/Address.java"));
        codeFiles.add(Paths.get("src/main/java/org/foo/models/Telephonebook.java"));
        codeFiles.add(Paths.get("src/main/java/org/foo/controller/EntryController.java"));
        codeFiles.add(Paths.get("src/main/java/org/foo/view/PersonView.java"));
        codeFiles.add(Paths.get("src/main/java/org/foo/view/EntryView.java"));

        codeFiles.add(Paths.get("src/test/java/org/foo/models/AddressTest.java"));
        codeFiles.add(Paths.get("src/test/java/org/foo/view/TestEntryView.java"));
        codeFiles.add(Paths.get("src/test/java/org/foo/controller/testController.java"));
        codeFiles.add(Paths.get("src/test/java/org/foo/view/blatestbla.java"));
        codeFiles.add(Paths.get("src/test/java/org/foo/models/Persontest.java"));
        codeFiles.add(Paths.get("src/test/java/org/foo/models/PersonITtest.java"));
        codeFiles.add(Paths.get("src/test/java/org/foo/models/TestAddressIntegration.java"));
        codeFiles.add(Paths.get("src/test/java/org/foo/models/TelephoneTestIT.java"));
        codeFiles.add(Paths.get("src/main/java/integration/blubTest.java"));
        codeFiles.add(Paths.get("src/main/java/unit/fooTest.java"));

        HashSet<Path> testFiles = new HashSet<>();
        testFiles.add(Paths.get("src/test/java/org/foo/models/AddressTest.java"));
        testFiles.add(Paths.get("src/test/java/org/foo/view/TestEntryView.java"));
        testFiles.add(Paths.get("src/test/java/org/foo/controller/testController.java"));
        testFiles.add(Paths.get("src/test/java/org/foo/view/blatestbla.java"));
        testFiles.add(Paths.get("src/test/java/org/foo/models/Persontest.java"));
        testFiles.add(Paths.get("src/test/java/org/foo/models/PersonITtest.java"));
        testFiles.add(Paths.get("src/test/java/org/foo/models/TestAddressIntegration.java"));
        testFiles.add(Paths.get("src/test/java/org/foo/models/TelephoneTestIT.java"));
        testFiles.add(Paths.get("src/main/java/integration/blubTest.java"));
        testFiles.add(Paths.get("src/main/java/unit/fooTest.java"));

        ProjectFiles projectFiles = new ProjectFiles(null, testFiles, codeFiles);

        Set<Result> expectedResult = new HashSet<>();
        expectedResult.add(new Result(Paths.get("src/test/java/org/foo/models/AddressTest.java").toString(),
                Paths.get("src/test/java/org/foo/models/AddressTest.java"), "files_nc", TestType.UNIT.name()));
        expectedResult.add(new Result(Paths.get("src/test/java/org/foo/view/TestEntryView.java").toString(),
                Paths.get("src/test/java/org/foo/view/TestEntryView.java"), "files_nc",TestType.UNIT.name()));
        expectedResult.add(new Result(Paths.get("src/test/java/org/foo/controller/testController.java").toString(),
                Paths.get("src/test/java/org/foo/controller/testController.java"), "files_nc",TestType.UNKNOWN.name()));
        expectedResult.add(new Result(Paths.get("src/test/java/org/foo/view/blatestbla.java").toString(),
                Paths.get("src/test/java/org/foo/view/blatestbla.java"), "files_nc",TestType.UNKNOWN.name()));
        expectedResult.add(new Result(Paths.get("src/test/java/org/foo/models/Persontest.java").toString(),
                Paths.get("src/test/java/org/foo/models/Persontest.java"), "files_nc",TestType.UNIT.name()));
        expectedResult.add(new Result(Paths.get("src/test/java/org/foo/models/PersonITtest.java").toString(),
                Paths.get("src/test/java/org/foo/models/PersonITtest.java"), "files_nc",TestType.INTEGRATION.name()));
        expectedResult.add(new Result(Paths.get("src/test/java/org/foo/models/TestAddressIntegration.java").toString(),
                Paths.get("src/test/java/org/foo/models/TestAddressIntegration.java"), "files_nc",TestType.INTEGRATION.name()));
        expectedResult.add(new Result(Paths.get("src/test/java/org/foo/models/TelephoneTestIT.java").toString(),
                Paths.get("src/test/java/org/foo/models/TelephoneTestIT.java"), "files_nc",TestType.INTEGRATION.name()));
        expectedResult.add(new Result(Paths.get("src/main/java/integration/blubTest.java").toString(),
                Paths.get("src/main/java/integration/blubTest.java"), "files_nc",TestType.INTEGRATION.name()));
        expectedResult.add(new Result(Paths.get("src/main/java/unit/fooTest.java").toString(),
                Paths.get("src/main/java/unit/fooTest.java"), "files_nc",TestType.UNIT.name()));


        NamingConventionTestTypeCollector nc = new NamingConventionTestTypeCollector(configuration, filerMock);
        nc.createResults(projectFiles);

        assertEquals("size do not match", expectedResult.size(), filerMock.getResults().getResults().size());
        assertEquals("wrong classification", expectedResult, filerMock.getResults().getResults());
    }


    @Test
    public void classificationTwoMatchesTest() throws IOException {
        HashSet<Path> codeFiles = new HashSet<>();
        codeFiles.add(Paths.get("src/main/java/org/foo/models/Person.java"));
        codeFiles.add(Paths.get("src/main/java/org/foo/models/PersonStream.java"));

        HashSet<Path> testFiles = new HashSet<>();
        testFiles.add(Paths.get("src/test/java/org/foo/models/PersonStreamTest.java"));

        ProjectFiles projectFiles = new ProjectFiles(null, testFiles, codeFiles);

        Set<Result> expectedResult = new HashSet<>();
        expectedResult.add(new Result(Paths.get("src/test/java/org/foo/models/PersonStreamTest.java").toString(),
                Paths.get("src/test/java/org/foo/models/PersonStreamTest.java"), "files_nc", TestType.UNIT.name()));


        NamingConventionTestTypeCollector nc = new NamingConventionTestTypeCollector(configuration, filerMock);
        nc.createResults(projectFiles);

        assertEquals("size do not match", expectedResult.size(), filerMock.getResults().getResults().size());
        assertEquals("wrong classification", expectedResult, filerMock.getResults().getResults());
    }

    @Test
    public void classificationWithCoverageDataJavaTest() {
        configuration.setProjectDir(getPathToResource("loaderTestData/projectfiles/javaproject"));
        configuration.setMethodLevel(true);


        CoverageData covData = new CoverageData();
        covData.add(personTestInit, new HashSet<IUnit>() {{ add(personInit);}});
        covData.add(personTestm2, new HashSet<IUnit>() {{ add(personInit); add(entryViewInit);}});
        covData.add(blubTestInit, new HashSet<IUnit>());
        covData.add(testEntryViewInit, new HashSet<IUnit>() {{ add(entryViewInit); add(fooIT);}});
        covData.add(blatestblaInit, new HashSet<>());
        covData.add(fooIT, new HashSet<>());
        covData.add(fooIT2, new HashSet<>());
        covData.add(fooTestInit, new HashSet<>());

        NamingConventionTestTypeCollector nc = new NamingConventionTestTypeCollector(configuration, filerMock);
        nc.createResults(covData);

        Set<Result> expectedResult = new HashSet<>();
        expectedResult.add(new Result("org.foo.models.Persontest.<init>",
                Paths.get("src/test/java/org/foo/models/Persontest.java"), "cov_nc", TestType.UNIT.name()));
        expectedResult.add(new Result("org.foo.models.Persontest.m2",
                Paths.get("src/test/java/org/foo/models/Persontest.java"), "cov_nc", TestType.UNIT.name()));
        expectedResult.add(new Result("integration.blubTest.<init>",
                Paths.get("src/test/java/integration/blubTest.java"), "cov_nc", TestType.INTEGRATION.name()));
        expectedResult.add(new Result("org.foo.view.TestEntryView.<init>",
                Paths.get("src/test/java/org/foo/view/TestEntryView.java"), "cov_nc", TestType.UNIT.name()));
        expectedResult.add(new Result("org.foo.view.blatestbla.<init>",
                Paths.get("src/test/java/org/foo/view/blatestbla.java"), "cov_nc", TestType.UNKNOWN.name()));
        expectedResult.add(new Result("org.foo.FooIT.<init>",
                Paths.get("src/test/java/org/foo/FooIT.java"), "cov_nc", TestType.INTEGRATION.name()));
        expectedResult.add(new Result("org.foo.FooIT.m2",
                Paths.get("src/test/java/org/foo/FooIT.java"), "cov_nc", TestType.INTEGRATION.name()));
        expectedResult.add(new Result("unit.fooTest.<init>",
                Paths.get("src/test/java/unit/fooTest.java"), "cov_nc", TestType.UNIT.name()));

        assertEquals("wrong classification", expectedResult, filerMock.getResults().getResults());
    }
}
