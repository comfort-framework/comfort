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

package de.ugoe.cs.comfort;

import de.ugoe.cs.comfort.configuration.GeneralConfiguration;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Fabian Trautsch
 */
public class FileNameUtils {
    private GeneralConfiguration configuration;
    private Set<Path> filesWithoutProjectDir = new HashSet<>();

    @SuppressFBWarnings("DM_EXIT")
    public FileNameUtils(GeneralConfiguration configuration) {
        this.configuration = configuration;

        try {
            // Substitute projectDir for all files, as this is not of interest and exclude venv
            for(Path file : this.getAllFilesFromProjectForConfiguration()) {
                Path fileWithoutProjectDir = file.subpath(configuration.getProjectDir().getNameCount(),
                        file.getNameCount());
                if(!fileWithoutProjectDir.toString().startsWith("venv")) {
                    filesWithoutProjectDir.add(fileWithoutProjectDir);
                }
            }
        } catch (FileNotFoundException e) {
            // We exit here directly, as if there was a problem with this, we can not determine the filepath of
            // extracted classes or similar. Better solution needs to be found
            e.printStackTrace();
            System.exit(1);
        }
    }

    private Set<Path> getAllFilesFromProjectForConfiguration() throws FileNotFoundException {
        String language = configuration.getLanguage();
        String regex = ".*\\.java";
        if (language.equals("python")) {
            regex = ".*\\.py";
        }

        try {
            return Utils.getAllFilesFromProjectForRegex(configuration.getProjectDir(), regex);
        } catch (IOException e) {
            throw new FileNotFoundException("Could not load Java Files! " + e);
        }
    }

    public boolean isPythonPackage(String fqn) throws FileNotFoundException {
        Path expectedModulePath = Paths.get(fqn.replace(".", "/")+".py");
        return !filesWithoutProjectDir.contains(expectedModulePath);
    }

    public Path getPathForJavaClassFQN(String fullyQualifiedName) throws FileNotFoundException {
        return Utils.getPathForFullyQualifiedClassNameInSetOfPaths(filesWithoutProjectDir,
                fullyQualifiedName.replace(configuration.getProjectDir().toString(), ""));
    }

    public Path getPathForPythonModuleFQN(String fullyQualifiedName) throws FileNotFoundException {
        return Utils.getPathForModuleNameInSetOfPaths(filesWithoutProjectDir,
                fullyQualifiedName.replace(configuration.getProjectDir().toString(), ""));
    }

    public Path getPathForIdentifier(String identifier, boolean executeOnMethodLevel) throws FileNotFoundException {

        if (this.configuration.getLanguage().equals("python")) {
            if (executeOnMethodLevel) {
                String[] testParts = identifier.split("\\:");
                String testFileName = testParts[0];
                return Utils.getPathForModuleNameInSetOfPaths(filesWithoutProjectDir,
                        testFileName.replace(configuration.getProjectDir().toString(), ""));
            } else {
                return Utils.getPathForModuleNameInSetOfPaths(filesWithoutProjectDir,
                        identifier.replace(configuration.getProjectDir().toString(), ""));
            }
        } else {
            if (executeOnMethodLevel) {
                String testWithoutAttributes = identifier.split("\\[")[0];
                String[] testParts = testWithoutAttributes.split("\\.");
                String testFileName = String.join(".", Arrays.copyOfRange(testParts, 0, testParts.length - 1));
                return Utils.getPathForFullyQualifiedClassNameInSetOfPaths(filesWithoutProjectDir,
                        testFileName.replace(configuration.getProjectDir().toString(), ""));
            } else {
                return getPathForJavaClassFQN(identifier);
            }
        }
    }


}
