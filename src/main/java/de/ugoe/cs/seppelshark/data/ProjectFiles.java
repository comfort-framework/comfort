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

import com.google.common.base.MoreObjects;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Fabian Trautsch
 */
public class ProjectFiles extends DataSet{
    private Path projectDir;
    private Set<Path> testFiles;
    private Set<Path> codeFiles;

    public ProjectFiles(Path projectDir, Set<Path> testFiles, Set<Path> codeFiles) {
        this.projectDir = projectDir;
        this.testFiles = testFiles;
        this.codeFiles = codeFiles;
    }

    public Path getProjectDir() {
        return projectDir;
    }

    public Set<Path> getTestFiles() {
        return testFiles;
    }

    public Set<Path> getCodeFiles() {
        return codeFiles;
    }

    public Set<Path> getTestFilesWithoutProjectDir() {
        Set<Path> testFilesWithoutProjectDir = new HashSet<>();
        testFiles.forEach(
                // Replace the path to the project directory
                path -> testFilesWithoutProjectDir.add(path.subpath(projectDir.getNameCount(), path.getNameCount()))
        );
        return testFilesWithoutProjectDir;
    }

    public Set<Path> getCodeFilesWithoutProjectDir() {
        Set<Path> codeFilesWithoutProjectDir = new HashSet<>();
        codeFiles.forEach(
                // Replace the path to the project directory
                path -> codeFilesWithoutProjectDir.add(path.subpath(projectDir.getNameCount(), path.getNameCount()))
        );
        return codeFilesWithoutProjectDir;
    }

    public Set<String> getCodeFilesWithoutProjectDirAsString() {
        Set<String> codeFilesWithoutProjectDir = new HashSet<>();
        this.getCodeFilesWithoutProjectDir().forEach(
                x -> codeFilesWithoutProjectDir.add(x.toString())
        );
        return codeFilesWithoutProjectDir;
    }

    public Set<String> getTestFilesWithoutProjectDirAsString() {
        Set<String> testFilesWithoutProjectDir = new HashSet<>();
        this.getTestFilesWithoutProjectDir().forEach(
                x -> testFilesWithoutProjectDir.add(x.toString())
        );
        return testFilesWithoutProjectDir;
    }

    public List<String> getTestFilesAsString() {
        List<String> testFileStrings = new ArrayList<>();
        for(Path testFilePath : testFiles) {
            testFileStrings.add(testFilePath.toString());
        }

        return testFileStrings;
    }

    public List<String> getCodeFilesAsString() {
        List<String> codeFilesString = new ArrayList<>();
        for(Path testFilePath : codeFiles) {
            codeFilesString.add(testFilePath.toString());
        }

        return codeFilesString;
    }

    public HashSet<Path> getCodeFilesWithoutTestFiles() {
        HashSet<Path> temp = new HashSet<>(codeFiles);
        temp.removeAll(testFiles);
        return temp;
    }

    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("projectDir", projectDir)
                .add("testFiles", getTestFilesAsString())
                .add("codeFiles", getCodeFilesAsString())
                .toString();
    }
}
