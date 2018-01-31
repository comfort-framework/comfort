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

package de.ugoe.cs.seppelshark.collection.loader;

import de.ugoe.cs.seppelshark.BaseTest;
import de.ugoe.cs.seppelshark.configuration.GeneralConfiguration;
import de.ugoe.cs.seppelshark.configuration.LoaderConfiguration;
import de.ugoe.cs.seppelshark.data.ProjectFiles;
import de.ugoe.cs.seppelshark.exception.LoaderException;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;

import static org.junit.Assert.*;

/**
 * @author Fabian Trautsch
 */
public class ProjectFilesLoaderTest extends BaseTest{
    private GeneralConfiguration javaConfiguration = new GeneralConfiguration();
    private GeneralConfiguration pythonConfiguration = new GeneralConfiguration();
    private LoaderConfiguration loaderConf = new LoaderConfiguration("ProjectFiles");

    private ProjectFilesLoader projectFilesLoader;

    @Before
    public void createExecutionConfigurationForJava() {
        javaConfiguration.setProjectDir(getPathToResource("loaderTestData/projectfiles/javaproject"));
        javaConfiguration.setLanguage("java");
    }

    @Before
    public void createExecutionConfigurationForPython() {
        pythonConfiguration.setProjectDir(getPathToResource("loaderTestData/projectfiles/pythonproject"));
        pythonConfiguration.setLanguage("python");
    }

    @Test
    public void loadCheckDataTypeTestJava() {
        try {
            projectFilesLoader = new ProjectFilesLoader(javaConfiguration, loaderConf);
            ProjectFiles files =  projectFilesLoader.loadJavaProjectFiles();
            assertTrue(files != null);
        } catch(LoaderException e) {
            fail("Error in reading project directory at: " + javaConfiguration.getProjectDir());
        }
    }

    @Test
    public void loadCheckDataTypeTestPython() {
        try {
            projectFilesLoader = new ProjectFilesLoader(pythonConfiguration, loaderConf);
            ProjectFiles files =  projectFilesLoader.loadPythonProjectFiles();
            assertTrue(files != null);
        } catch(LoaderException e) {
            fail("Error in reading project directory at: " + pythonConfiguration.getProjectDir());
        }
    }

    @Test
    public void loadCheckDataCodeFilesTest() {
        try {
            projectFilesLoader = new ProjectFilesLoader(javaConfiguration, loaderConf);
            ProjectFiles projectFiles = projectFilesLoader.loadJavaProjectFiles();

            HashSet<Path> expected_code_files = new HashSet<>();
            expected_code_files.add(Paths.get("src/main/java/org/foo/controller/EntryController.java"));
            expected_code_files.add(Paths.get("src/main/java/org/foo/controller/IController.java"));
            expected_code_files.add(Paths.get("src/main/java/org/foo/models/Address.java"));
            expected_code_files.add(Paths.get("src/main/java/org/foo/models/Person.java"));
            expected_code_files.add(Paths.get("src/main/java/org/foo/models/Telephonebook.java"));
            expected_code_files.add(Paths.get("src/main/java/org/foo/view/EntryView.java"));
            expected_code_files.add(Paths.get("src/main/java/org/foo/view/PersonView.java"));
            expected_code_files.add(Paths.get("src/main/java/org/foo/Main.java"));
            expected_code_files.add(Paths.get("src/test/java/org/foo/controller/testController.java"));
            expected_code_files.add(Paths.get("src/test/java/org/foo/models/AddressTest.java"));
            expected_code_files.add(Paths.get("src/test/java/org/foo/models/Persontest.java"));
            expected_code_files.add(Paths.get("src/test/java/org/foo/view/TestEntryView.java"));
            expected_code_files.add(Paths.get("src/test/java/org/foo/view/blatestbla.java"));
            expected_code_files.add(Paths.get("src/test/java/unit/fooTest.java"));
            expected_code_files.add(Paths.get("src/test/java/integration/blubTest.java"));

            assertEquals("Not all/too less code files detected", expected_code_files.size(), projectFiles.getCodeFilesWithoutProjectDir().size());
            assertEquals("Code files not correctly detected", expected_code_files, projectFiles.getCodeFilesWithoutProjectDir());
        } catch(LoaderException e) {
            fail("Error in reading project directory at: " + javaConfiguration.getProjectDir());
        }
    }

    @Test
    public void loadCheckDataTestFilesTest() {
        try {
            projectFilesLoader = new ProjectFilesLoader(javaConfiguration, loaderConf);
            ProjectFiles projectFiles = projectFilesLoader.loadJavaProjectFiles();

            HashSet<Path> expected_test_files = new HashSet<>();
            expected_test_files.add(Paths.get("src/test/java/org/foo/controller/testController.java"));
            expected_test_files.add(Paths.get("src/test/java/org/foo/models/AddressTest.java"));
            expected_test_files.add(Paths.get("src/test/java/org/foo/models/Persontest.java"));
            expected_test_files.add(Paths.get("src/test/java/org/foo/view/TestEntryView.java"));
            expected_test_files.add(Paths.get("src/test/java/org/foo/view/blatestbla.java"));
            expected_test_files.add(Paths.get("src/test/java/integration/blubTest.java"));
            expected_test_files.add(Paths.get("src/test/java/unit/fooTest.java"));

            assertEquals("Not all/too less test files detected", expected_test_files.size(), projectFiles.getTestFilesWithoutProjectDir().size());
            assertEquals("Test files not correctly detected", expected_test_files, projectFiles.getTestFilesWithoutProjectDir());
        } catch(LoaderException e) {
            fail("Error in reading project directory at: " + javaConfiguration.getProjectDir());
        }
    }

    @Test
    public void loadCheckDataPythonCodeFilesTest() {
        try {
            projectFilesLoader = new ProjectFilesLoader(pythonConfiguration, loaderConf);
            ProjectFiles projectFiles = projectFilesLoader.loadPythonProjectFiles();

            HashSet<Path> expected_code_files = new HashSet<>();
            expected_code_files.add(Paths.get("src/pythonproject/__init__.py"));
            expected_code_files.add(Paths.get("src/pythonproject/unit1.py"));
            expected_code_files.add(Paths.get("src/pythonproject/unit2.py"));
            expected_code_files.add(Paths.get("src/pythonproject/module1/__init__.py"));
            expected_code_files.add(Paths.get("src/pythonproject/module1/unit1.py"));
            expected_code_files.add(Paths.get("tests/__init__.py"));
            expected_code_files.add(Paths.get("tests/test_1.py"));
            expected_code_files.add(Paths.get("tests/unit/__init__.py"));
            expected_code_files.add(Paths.get("tests/unit/test_unit1.py"));
            expected_code_files.add(Paths.get("tests/unit/test2.py"));
            expected_code_files.add(Paths.get("tests/integration/__init__.py"));
            expected_code_files.add(Paths.get("tests/integration/test2.py"));
            expected_code_files.add(Paths.get("tests/integration/test_integration1.py"));

            assertEquals("Not all/too less code files detected", expected_code_files.size(), projectFiles.getCodeFilesWithoutProjectDir().size());
            assertEquals("Code files not correctly detected", expected_code_files, projectFiles.getCodeFilesWithoutProjectDir());
        } catch(LoaderException e) {
            fail("Error in reading project directory at: " + pythonConfiguration.getProjectDir());
        }
    }

    @Test
    public void loadCheckDataPythonTestFilesTest() {
        try {
            projectFilesLoader = new ProjectFilesLoader(pythonConfiguration, loaderConf);
            ProjectFiles projectFiles = projectFilesLoader.loadPythonProjectFiles();

            HashSet<Path> expected_test_files = new HashSet<>();
            expected_test_files.add(Paths.get("tests/test_1.py"));
            expected_test_files.add(Paths.get("tests/unit/test_unit1.py"));
            expected_test_files.add(Paths.get("tests/unit/test2.py"));
            expected_test_files.add(Paths.get("tests/integration/test2.py"));
            expected_test_files.add(Paths.get("tests/integration/test_integration1.py"));

            assertEquals("Not all/too less test files detected", expected_test_files.size(), projectFiles.getTestFilesWithoutProjectDir().size());
            assertEquals("Test files not correctly detected", expected_test_files, projectFiles.getTestFilesWithoutProjectDir());
        } catch(LoaderException e) {
            fail("Error in reading project directory at: " + pythonConfiguration.getProjectDir());
        }
    }



}
