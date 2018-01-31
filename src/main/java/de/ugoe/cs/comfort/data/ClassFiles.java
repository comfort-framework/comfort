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
import de.ugoe.cs.comfort.FileNameUtils;
import de.ugoe.cs.comfort.configuration.GeneralConfiguration;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.Set;

/**
 * @author Fabian Trautsch
 */
public class ClassFiles extends DataSet {
    private Path projectDir;
    private Set<Path> testFiles;
    private Set<Path> codeFiles;
    private FileNameUtils fileNameUtils;

    public ClassFiles(GeneralConfiguration configuration, Set<Path> testFiles, Set<Path> codeFiles) {
        this.projectDir = configuration.getProjectDir();
        this.testFiles = testFiles;
        this.codeFiles = codeFiles;
        this.fileNameUtils = new FileNameUtils(configuration);
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

    public Path getFilenameForClass(String identifier) throws FileNotFoundException {
        return fileNameUtils.getPathForIdentifier(identifier, false);
    }

    public Path getFilenameForMethod(String identifier) throws FileNotFoundException {
        return fileNameUtils.getPathForIdentifier(identifier, true);
    }

    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("projectDir", projectDir)
                .add("testFiles", testFiles)
                .add("codeFiles", codeFiles)
                .toString();
    }
}
