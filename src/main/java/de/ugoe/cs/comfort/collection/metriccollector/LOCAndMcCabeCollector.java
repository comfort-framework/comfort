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

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import de.ugoe.cs.comfort.annotations.SupportsClass;
import de.ugoe.cs.comfort.annotations.SupportsJava;
import de.ugoe.cs.comfort.annotations.SupportsMethod;
import de.ugoe.cs.comfort.annotations.SupportsPython;
import de.ugoe.cs.comfort.collection.metriccollector.parsing.ASTClassNode;
import de.ugoe.cs.comfort.collection.metriccollector.parsing.ASTClassVisitor;
import de.ugoe.cs.comfort.collection.metriccollector.parsing.ASTMethodNode;
import de.ugoe.cs.comfort.collection.metriccollector.parsing.ASTMethodVisitor;
import de.ugoe.cs.comfort.configuration.GeneralConfiguration;
import de.ugoe.cs.comfort.data.ProjectFiles;
import de.ugoe.cs.comfort.exception.MetricCollectorException;
import de.ugoe.cs.comfort.filer.models.Result;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

/**
 * @author Fabian Trautsch
 */
public class LOCAndMcCabeCollector extends BaseMetricCollector {

    public LOCAndMcCabeCollector(GeneralConfiguration configuration) {
        super(configuration);
    }

    @SupportsPython
    @SupportsMethod
    public Set<Result> getLOCAndMcCabeForPythonMethod(ProjectFiles projectFiles) throws MetricCollectorException {
        // Create process builder
        ProcessBuilder builder = new ProcessBuilder();
        builder.directory(generalConf.getProjectDir().toFile());


        // We need to create a temporary file as a copy of the existing file, as an execution is not possible
        // otherwise, if we execute it within a jar
        File tempFile;
        File pythonFile;
        try {
            tempFile = File.createTempFile("loc_mccabe_extract_temp", ".py");
        } catch (IOException e) {
            throw new MetricCollectorException("Error in creating temporary file:"+e.getMessage());
        }

        InputStream in = getClass().getResourceAsStream("/loc_mccabe_extract.py");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, Charset.forName("UTF-8")));
                OutputStreamWriter fstream =
                        new OutputStreamWriter(new FileOutputStream(tempFile), StandardCharsets.UTF_8)) {
            int c = reader.read();
            while(c!=-1) {
                fstream.write(c);
                c = reader.read();
            }
        } catch(IOException e) {
            throw new MetricCollectorException("Error in copying file contents:"+e.getMessage());
        }

        builder.command("python3", tempFile.toString(), generalConf.getProjectDir().toString());
        return executeCommandAndGenerateGraph(builder);
    }

    private Set<Result> executeCommandAndGenerateGraph(ProcessBuilder builder) throws MetricCollectorException {
        logger.info("Calling command: {}", builder.command());
        int exitCode;
        Future<Set<Result>> future;
        try {
            Process depExtract = builder.start();
            LOCAndMcCabeOutputParser outputParser =
                    new LOCAndMcCabeOutputParser(depExtract.getInputStream());

            ExecutorService executor = Executors.newFixedThreadPool(1);
            future = executor.submit(outputParser);
            exitCode = depExtract.waitFor();
        } catch (IOException | InterruptedException e) {
            throw new MetricCollectorException("Error in executing loader, while calling "
                + builder.command().toString()+" :"+e.getMessage());
        }

        // Check if it returned successfully
        if (exitCode != 0) {
            throw new MetricCollectorException("Error in executing loader: Program did not terminate with code 0");
        }

        Set<Result> results;
        try {
            results = future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new MetricCollectorException("Error in parsing results: "+e.getMessage());
        }
        logger.info("Execution successful...");
        return results;
    }

    private class LOCAndMcCabeOutputParser implements Callable<Set<Result>> {
        private InputStream inputStream;

        LOCAndMcCabeOutputParser(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public Set<Result> call() {
            Set<Result> results = new HashSet<>();
            try {
                parseOutput(results);
            } catch (UnsupportedEncodingException e) {
                logger.error("Charset not supported!");
            }
            return results;
        }

        private void parseOutput(Set<Result> results) throws UnsupportedEncodingException {
            new BufferedReader(new InputStreamReader(inputStream , "UTF-8")).lines()
                    .forEach(outputLine -> {
                        logger.debug("Original extractor line: {}", outputLine);
                        String[] parts = outputLine.split(Pattern.quote("::"));
                        Result result = new Result(parts[1], Paths.get(parts[0]));
                        result.addMetric("mc_cabe_sg", parts[2]);
                        result.addMetric("mc_cabe_all", parts[3]);
                        result.addMetric("cloc", parts[4]);
                        result.addMetric("lloc", parts[5]);
                        results.add(result);
                    });
        }
    }


    @SupportsJava
    @SupportsClass
    public Set<Result> getLOCAndMcCabeForJavaClass(ProjectFiles projectFiles) throws MetricCollectorException {
        try {
            Set<Result> results = new HashSet<>();
            for(Path testFile: projectFiles.getTestFiles()) {
                // creates a string with the code without blank lines
                String code = getSourceCodeWithoutBlankLines(testFile);

                // parse it
                CompilationUnit cu = JavaParser.parse(code);

                // visit and print the methods names
                ASTClassNode astClassNode = new ASTClassNode();
                ASTClassVisitor astClassVisitor = new ASTClassVisitor(astClassNode);
                cu.accept(astClassVisitor, null);
                logger.debug("Got the following results from the AST parsing: "+astClassNode);


                Path resultPath = Paths.get(testFile.toString()
                        .replace(generalConf.getProjectDir().toString()+"/", ""));
                results.add(createResultWithMetrics(astClassNode.getFQN(), resultPath, null,
                        astClassNode.getLLOC(), astClassNode.getCLOC()));
            }

            return results;
        } catch (IOException e) {
            throw new MetricCollectorException("Could not collect metrics: "+e.getMessage());
        }
    }

    @SupportsJava
    @SupportsMethod
    public Set<Result> getLOCAndMcCabeForJavaMethod(ProjectFiles projectFiles) throws MetricCollectorException {
        try {
            Set<Result> results = new HashSet<>();
            for(Path testFile: projectFiles.getTestFiles()) {
                // creates a string with the code without blank lines
                String code = getSourceCodeWithoutBlankLines(testFile);

                // parse it
                CompilationUnit cu = JavaParser.parse(code);

                // visit and print the methods names
                Set<ASTMethodNode> astResults = new HashSet<>();
                ASTMethodVisitor astMethodVisitor = new ASTMethodVisitor(astResults);
                cu.accept(astMethodVisitor, null);
                logger.debug("Got the following results from the AST parsing: "+astResults);

                Path resultPath = Paths.get(testFile.toString()
                        .replace(generalConf.getProjectDir().toString()+"/", ""));

                for(ASTMethodNode astMethodNode: astResults) {
                    results.add(createResultWithMetrics(astMethodNode.getFQNWithMethod(), resultPath,
                            astMethodNode.getMcCC(), astMethodNode.getLLOC(), astMethodNode.getCLOC()));
                }
            }

            return results;
        } catch (IOException e) {
            throw new MetricCollectorException("Could not collect metrics: "+e.getMessage());
        }
    }

    private Result createResultWithMetrics(String identifier, Path resultPath, Integer mcCabe,
                                           Integer lloc, Integer cloc) {
        Result result = new Result(identifier, resultPath);
        if(mcCabe != null) {
            result.addMetric("mc_cabe", String.valueOf(mcCabe));
        }

        result.addMetric("lloc", String.valueOf(lloc));
        result.addMetric("cloc", String.valueOf(cloc));
        return result;
    }

    private String getSourceCodeWithoutBlankLines(Path sourceCodePath) throws IOException {
        List<String> allLines = Files.readAllLines(sourceCodePath);
        StringBuilder code = new StringBuilder();
        for(String line: allLines) {
            line = line.trim();
            if(line.length() > 0) {
                code.append(line).append("\n");
            }
        }
        return code.toString();
    }
}
