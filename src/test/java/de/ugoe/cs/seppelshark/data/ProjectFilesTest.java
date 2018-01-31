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

package de.ugoe.cs.seppelshark.data;

import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;

/**
 * @author Fabian Trautsch
 */
public class ProjectFilesTest {

    @Test
    public void getCodeFilesWithoutTestFilesTest() {
        HashSet<Path> test_files = new HashSet<>();
        test_files.add(Paths.get("src/test/java/org/foo/controller/testController.java"));
        test_files.add(Paths.get("src/test/java/org/foo/models/AddressTest.java"));
        test_files.add(Paths.get("src/test/java/org/foo/models/Persontest.java"));
        test_files.add(Paths.get("src/test/java/org/foo/view/TestEntryView.java"));

        HashSet<Path> code_files = new HashSet<>();
        code_files.add(Paths.get("src/main/java/org/foo/controller/EntryController.java"));
        code_files.add(Paths.get("src/main/java/org/foo/models/Address.java"));
        code_files.add(Paths.get("src/main/java/org/foo/models/Person.java"));
        code_files.add(Paths.get("src/main/java/org/foo/models/Telephonebook.java"));
        code_files.add(Paths.get("src/main/java/org/foo/view/EntryView.java"));
        code_files.add(Paths.get("src/main/java/org/foo/view/PersonView.java"));
        code_files.add(Paths.get("src/main/java/org/foo/Main.java"));
        code_files.add(Paths.get("src/test/java/org/foo/controller/testController.java"));
        code_files.add(Paths.get("src/test/java/org/foo/models/AddressTest.java"));
        code_files.add(Paths.get("src/test/java/org/foo/models/Persontest.java"));
        code_files.add(Paths.get("src/test/java/org/foo/view/TestEntryView.java"));

        ProjectFiles projectFiles = new ProjectFiles(Paths.get("project"), test_files, code_files);

        HashSet<Path> expected_files = new HashSet<>();
        expected_files.add(Paths.get("src/main/java/org/foo/controller/EntryController.java"));
        expected_files.add(Paths.get("src/main/java/org/foo/models/Address.java"));
        expected_files.add(Paths.get("src/main/java/org/foo/models/Person.java"));
        expected_files.add(Paths.get("src/main/java/org/foo/models/Telephonebook.java"));
        expected_files.add(Paths.get("src/main/java/org/foo/view/EntryView.java"));
        expected_files.add(Paths.get("src/main/java/org/foo/view/PersonView.java"));
        expected_files.add(Paths.get("src/main/java/org/foo/Main.java"));

        assertEquals("Number of files do not match", expected_files.size(), projectFiles.getCodeFilesWithoutTestFiles().size());
        assertEquals("Wrong separation of code and test files", expected_files, projectFiles.getCodeFilesWithoutTestFiles());

        // Check if other files are not touched
        assertEquals("Test files were changed", test_files, projectFiles.getTestFiles());
        assertEquals("Code files were changed", code_files, projectFiles.getCodeFiles());
    }

    @Test
    public void getProjectDirTest() {
        ProjectFiles projectFiles = new ProjectFiles(Paths.get("project"), null, null);
        assertEquals("Project dir not set correctly", Paths.get("project"), projectFiles.getProjectDir());
    }
}
