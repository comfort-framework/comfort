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

import de.ugoe.cs.seppelshark.Utils;
import de.ugoe.cs.seppelshark.annotations.SupportsJava;
import de.ugoe.cs.seppelshark.annotations.SupportsPython;
import de.ugoe.cs.seppelshark.configuration.GeneralConfiguration;
import de.ugoe.cs.seppelshark.configuration.LoaderConfiguration;
import de.ugoe.cs.seppelshark.data.ProjectFiles;
import de.ugoe.cs.seppelshark.exception.LoaderException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Fabian Trautsch
 */
public class ProjectFilesLoader extends BaseLoader {
    public ProjectFilesLoader(GeneralConfiguration generalConfiguration, LoaderConfiguration loaderConfiguration) {
        super(generalConfiguration, loaderConfiguration);
    }

    @SupportsPython
    public ProjectFiles loadPythonProjectFiles() throws LoaderException {
        return getProjectFilesBasedOnRegex(".*\\.py");
    }

    @SupportsJava
    public ProjectFiles loadJavaProjectFiles() throws LoaderException{
        return getProjectFilesBasedOnRegex(".*\\.java");
    }

    private ProjectFiles getProjectFilesBasedOnRegex(String fileRegex) throws LoaderException{
        // Find all code files by going through the projectDir and detect all files with the given regex
        try {
            Set<Path> codeFiles = Utils.getAllFilesFromProjectForRegex(generalConf.getProjectDir(), fileRegex);
            // Find all java test files by going through detected code files and looking if the file name
            // starts with Test oder test or ends with Test or test
            HashSet<Path> testFiles = new HashSet<>();
            for (Path codeFile: codeFiles) {
                String fileName = codeFile.subpath(codeFile.getNameCount()-1, codeFile.getNameCount()).toString();
                if(Utils.isTestBasedOnName(fileName)) {
                    testFiles.add(codeFile);
                }
            }

            ProjectFiles projectFiles = new ProjectFiles(generalConf.getProjectDir(), testFiles, codeFiles);
            logger.debug("Found {} test files and {} code files for the project.", testFiles.size(), codeFiles.size());
            logger.debug("Found the following ProjectFiles files for the project: {}", projectFiles);
            return projectFiles;
        } catch (IOException e) {
            throw new LoaderException("Could not load data: "+e.getMessage());
        }
    }

}
