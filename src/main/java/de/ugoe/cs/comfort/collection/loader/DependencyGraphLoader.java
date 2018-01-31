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

package de.ugoe.cs.comfort.collection.loader;

import de.ugoe.cs.comfort.FileNameUtils;
import de.ugoe.cs.comfort.annotations.SupportsJava;
import de.ugoe.cs.comfort.annotations.SupportsPython;
import de.ugoe.cs.comfort.configuration.GeneralConfiguration;
import de.ugoe.cs.comfort.configuration.LoaderConfiguration;
import de.ugoe.cs.comfort.data.graphs.DependencyGraph;
import de.ugoe.cs.comfort.data.models.JavaClass;
import de.ugoe.cs.comfort.data.models.PythonModule;
import de.ugoe.cs.comfort.exception.LoaderException;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;

/**
 * @author Fabian Trautsch
 */
public class DependencyGraphLoader extends BaseLoader {
    public DependencyGraphLoader(GeneralConfiguration generalConfiguration, LoaderConfiguration loaderConfiguration) {
        super(generalConfiguration, loaderConfiguration);
    }

    @SupportsJava
    public DependencyGraph loadJavaDependencyGraph() throws LoaderException {
        // Create process builder
        ProcessBuilder builder = new ProcessBuilder();
        builder.directory(generalConf.getProjectDir().toFile());
        builder.command("jdeps", "-v", generalConf.getProjectDir().toString());

        return executeCommandAndGenerateGraph(builder);
    }

    @SupportsPython
    public DependencyGraph loadPythonDependencyGraph() throws LoaderException {
        // Create process builder
        ProcessBuilder builder = new ProcessBuilder();
        builder.directory(generalConf.getProjectDir().toFile());


        // We need to create a temporary file as a copy of the existing file, as an execution is not possible
        // otherwise, if we execute it within a jar
        File tempFile;
        try {
            tempFile = File.createTempFile("dependency_extract_temp", ".py");
        } catch (IOException e) {
            throw new LoaderException("Error in creating temporary file:"+e.getMessage());
        }
        InputStream in = getClass().getResourceAsStream("/dependency_extract.py");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, Charset.forName("UTF-8")));
                OutputStreamWriter fstream =
                        new OutputStreamWriter(new FileOutputStream(tempFile), StandardCharsets.UTF_8)) {
            int c = reader.read();
            while(c!=-1) {
                fstream.write(c);
                c = reader.read();
            }
        } catch(IOException e) {
            throw new LoaderException("Error in copying file contents:"+e.getMessage());
        }
        builder.command("python3", tempFile.toString(), generalConf.getProjectDir().toString());


        return executeCommandAndGenerateGraph(builder);
    }


    private DependencyGraph executeCommandAndGenerateGraph(ProcessBuilder builder) throws LoaderException {
        logger.info("Calling command: {}", builder.command());
        int exitCode;
        Future<DependencyGraph> future;
        try {
            Process depExtract = builder.start();
            DependencyExtractOutputParser outputParser =
                    new DependencyExtractOutputParser(depExtract.getInputStream(), generalConf);

            ExecutorService executor = Executors.newFixedThreadPool(1);
            future = executor.submit(outputParser);
            exitCode = depExtract.waitFor();
        } catch (IOException | InterruptedException e) {
            throw new LoaderException("Error in executing loader, while calling "
                    +builder.command().toString()+" :"+e.getMessage());
        }

        // Check if it returned successfully
        if (exitCode != 0) {
            throw new LoaderException("Error in executing loader: Program did not terminate with code 0");
        }

        DependencyGraph dependencyGraph;
        try {
            dependencyGraph = future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new LoaderException("Error in parsing results: "+e.getMessage());
        }
        logger.info("Execution successful...");

        if(dependencyGraph.nodes().size() == 0) {
            throw new LoaderException("No graph was created. Are there class files for production and test "
                + "classes available in the tree of: "+generalConf.getProjectDir());
        }

        return dependencyGraph;
    }

    private class DependencyExtractOutputParser implements Callable<DependencyGraph> {
        private InputStream inputStream;
        private GeneralConfiguration configuration;
        private FileNameUtils fileNameUtils;

        DependencyExtractOutputParser(InputStream inputStream, GeneralConfiguration configuration) {
            this.inputStream = inputStream;
            this.configuration = configuration;
            this.fileNameUtils = new FileNameUtils(configuration);
        }

        @Override
        public DependencyGraph call() {
            DependencyGraph dependencyGraph = new DependencyGraph();
            try {
                if(configuration.getLanguage().equals("java")) {
                    parseJdepsOutput(dependencyGraph);
                }

                if(configuration.getLanguage().equals("python")) {
                    parseDependencyExtractorOutput(dependencyGraph);
                }
            } catch (UnsupportedEncodingException e) {
                logger.error("Charset not supported!");
            }
            return dependencyGraph;
        }

        private PythonModule createPythonModuleNode(String fqn) {
            try {
                if(fileNameUtils.isPythonPackage(fqn)) {
                    return new PythonModule(fqn, "__init__",
                            fileNameUtils.getPathForPythonModuleFQN(fqn+".__init__"));
                } else {
                    return new PythonModule(fqn, fileNameUtils.getPathForPythonModuleFQN(fqn));
                }
            } catch (FileNotFoundException e) {
                logger.warn("Could not find file for PythonModule {}", fqn);
            }
            return new PythonModule(fqn, null);
        }

        private JavaClass createJavaClassNode(String fqn) {
            try {
                return new JavaClass(fqn, this.fileNameUtils.getPathForJavaClassFQN(fqn));
            } catch (FileNotFoundException e) {
                logger.warn("Could not find file for JavaClass {}", fqn);
                return new JavaClass(fqn, null);
            }
        }

        private void parseDependencyExtractorOutput(DependencyGraph dependencyGraph) throws
                UnsupportedEncodingException {
            new BufferedReader(new InputStreamReader(inputStream , "UTF-8")).lines()
                    .forEach(dependncyExtractLine -> {
                        String[] partsOfOutput = dependncyExtractLine.split(",");
                        String moduleThatHasDependency = partsOfOutput[0];
                        String moduleNameOfDependency = partsOfOutput[1];
                        logger.debug("Original extractor line: {}", dependncyExtractLine);
                        logger.debug("Parsed: {} has dependency on {}", moduleThatHasDependency,
                                moduleNameOfDependency);

                        PythonModule moduleHasDependency = this.createPythonModuleNode(moduleThatHasDependency);
                        PythonModule moduleDependency = this.createPythonModuleNode(moduleNameOfDependency);
                        dependencyGraph.putEdge(moduleHasDependency, moduleDependency);
                    });
        }

        private void parseJdepsOutput(DependencyGraph dependencyGraph) throws UnsupportedEncodingException {
            new BufferedReader(new InputStreamReader(inputStream , "UTF-8")).lines()
                    .forEach(jdepsOutputLine -> {
                        if(jdepsOutputLine.startsWith("   ")) {
                            String[] partsOfOutput = jdepsOutputLine.split("->");
                            String classNameThatHasDependency = partsOfOutput[0].trim();
                            String classNameOfDependency = partsOfOutput[1].trim().split(" ")[0];
                            logger.debug("Original jdeps line: {}", jdepsOutputLine);
                            logger.debug("Parsed: {} has dependency on {}", classNameThatHasDependency,
                                    classNameOfDependency);

                            JavaClass classThatHasDependency = this.createJavaClassNode(classNameThatHasDependency);
                            JavaClass classThatIsDependency = this.createJavaClassNode(classNameOfDependency);
                            dependencyGraph.putEdge(classThatHasDependency, classThatIsDependency);

                        }

                    });
        }
    }
}
