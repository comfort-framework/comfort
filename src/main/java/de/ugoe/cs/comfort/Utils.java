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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * @author Fabian Trautsch
 */
public class Utils {
    private static final Logger LOGGER = LogManager.getLogger(Utils.class.getName());

    public static Boolean isTestBasedOnName(String nameOfFile) {
        String lowerCaseName = nameOfFile.toLowerCase();
        return lowerCaseName.contains("test") || lowerCaseName.contains("validate");

    }

    public static Boolean isTestBasedOnFQN(String fullyQualifiedName) {
        String lowerCaseName = fullyQualifiedName.substring(fullyQualifiedName.lastIndexOf('.') + 1).toLowerCase();
        return lowerCaseName.contains("test") || lowerCaseName.contains("validate");
    }

    public static String getPackageName(String fullyQualifiedName) {
        String[] parts = fullyQualifiedName.split(Pattern.quote("."));
        return String.join(".", Arrays.copyOfRange(parts, 0, parts.length-1));
    }

    public static String getClassName(String fullyQualifiedName) {
        fullyQualifiedName = fullyQualifiedName.replaceAll("\\[", "")
                .replaceAll("\\]", "");
        String[] parts = fullyQualifiedName.split(Pattern.quote("."));
        return String.join(".", Arrays.copyOfRange(parts, parts.length-1, parts.length));
    }

    public static String getFQNWithoutMethod(String fqnWithMethod) {
        String[] parts = fqnWithMethod.split(Pattern.quote("."));
        return String.join(".", Arrays.copyOfRange(parts, 0, parts.length-1));
    }

    public static Set<Path> getAllFilesFromProjectForRegex(Path projectDir, String regex) throws IOException {
        Set<Path> javaFiles = new HashSet<>();
        Files.find(
                projectDir,
                999,
                (p, bfa) -> bfa.isRegularFile() && p.getFileName().toString().matches(regex)
        ).forEach(javaFiles::add);
        return javaFiles;
    }

    public static Set<Path> getAllFilesFromProjectForRegexWithoutProjectDirPath(Path projectDir,
                                                                                String regex) throws  IOException {
        Set<Path> files = new HashSet<>();
        Files.find(
                projectDir,
                999,
                (p, bfa) -> bfa.isRegularFile() && p.getFileName().toString().matches(regex)
        ).forEach(
                // Replace the path to the project directory
                path -> files.add(path.subpath(projectDir.getNameCount(), path.getNameCount()))
        );
        return files;
    }

    public static Path getPathForModuleNameInSetOfPaths(Set<Path> pythonFiles, String moduleName)
            throws FileNotFoundException {
        String pythonFile = moduleName.replace(".", "/").concat(".py");

        // First look if the whole module name can be found, e.g.
        // tests.new_tests.test_module1 == tests/new_tests/test_module.py
        List<Path> candidatePaths = pythonFiles.stream()
                .filter(path -> path.toString().equals(pythonFile)).collect(Collectors.toList());
        if(candidatePaths.size() == 1) {
            return candidatePaths.get(0);
        }

        // If it is not found, we just look if we find a module that ends with the test name, this can also happen
        candidatePaths = pythonFiles.stream()
                .filter(path -> path.toString().endsWith(pythonFile)).collect(Collectors.toList());
        if(candidatePaths.size() == 1) {
            return candidatePaths.get(0);
        }

        // If a specific module does not exist, we check if it is a package, if yes, than it is a doctest in the
        // __init__ of the package
        Path initFile = Paths.get(pythonFile.replace(".py", ""), "__init__.py");
        if(pythonFiles.contains(initFile)) {
            return initFile;
        }

        LOGGER.debug("PythonFile: {}, Paths: {}", pythonFile, candidatePaths);
        throw new FileNotFoundException("File for module"+moduleName+" was not found!");
    }

    public static Path getPathForFullyQualifiedClassNameInSetOfPaths(Set<Path> javaFiles,
                                                                     String fullyQualifiedClassName,
                                                                     Path projectRoot)
            throws FileNotFoundException {
        /*
        Create a set of possible names:
        First -> original name; Afterwards, cut of parts that start with $ (to handle subclasses).
        E.g., $Gson$Types$GenericArrayTypeImpl would result in the following list:
        [$Gson$Types$GenericArrayTypeImpl.java, $Gson$Types.java, $Gson.java, '']
         */
        Set<String> possibleNames = new HashSet<>();
        possibleNames.add(fullyQualifiedClassName.replace(".", "/").concat(".java"));

        String[] parts = fullyQualifiedClassName.split("\\$");
        for(int i=0; i<parts.length; i++) {
            possibleNames.add(
                    String.join("$", Arrays.copyOfRange(parts, 0, parts.length-i))
                            .replace(".", "/")
                            .concat(".java")
            );
        }

        Predicate<Path> endsWithSameString =
                p -> possibleNames.stream().anyMatch(pn -> p.toString().endsWith(pn));

        List<Path> candidatePaths = javaFiles.stream()
                .filter(endsWithSameString)
                .collect(Collectors.toList());


        Optional<Path> bestFit = candidatePaths.stream().max(Comparator.comparingInt(p -> p.toString().length()));
        if(bestFit.isPresent()) {
            LOGGER.debug("{} is best fit for {}", bestFit.get(), fullyQualifiedClassName);
            return bestFit.get();
        } else {
            /*
            We need to look if we find a class or enum definition in any of the other class files, as it can be the
            case, that a class or enum is defined in another class source file.
             */
            for(String possibleName: possibleNames) {
                String[] allParts = possibleName.split("\\/");
                String lastPart = allParts[allParts.length-1].replace(".java", "");
                String pathExceptLastPart = String.join("/", Arrays.copyOfRange(allParts, 0, allParts.length-1));
                for(Path possibleFile: javaFiles) {
                    // The package declaration must be the same, therefore, we only look for files that are in
                    // the same package
                    if(!possibleFile.toString().contains(pathExceptLastPart)) {
                        continue;
                    }

                    Path searchInPath = projectRoot;

                    if(!possibleFile.startsWith(projectRoot)) {
                        searchInPath = Paths.get(projectRoot.toString(), possibleFile.toString());
                    }

                    Boolean classFound = searchFile(searchInPath, "class "+lastPart);
                    Boolean enumFound = searchFile(searchInPath, "enum "+lastPart);

                    if(classFound || enumFound) {
                        LOGGER.debug("{} is best fit for {}", possibleFile, fullyQualifiedClassName);
                        return possibleFile;
                    }
                }
            }
            throw new FileNotFoundException("File for class "+fullyQualifiedClassName+" was not found!");
        }
    }

    private static Boolean searchFile(Path file, String pattern) {
        Scanner scanner = null;
        try {
            scanner = new Scanner(file.toFile(), "utf-8");
            if (scanner.findWithinHorizon(pattern, 0) != null) {
                return true;
            }
        } catch (FileNotFoundException e) {
            return false;
        } finally {
            if(scanner != null) {
                scanner.close();
            }
        }

        return false;
    }
}
