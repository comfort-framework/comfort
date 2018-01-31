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

package de.ugoe.cs.seppelshark.collection.loader.testcoverage;

import java.io.*;
import java.nio.file.Path;
import java.util.Collection;
import javax.annotation.Nullable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.data.ExecutionDataReader;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.IExecutionDataVisitor;
import org.jacoco.core.data.ISessionInfoVisitor;

/**
 * Based on: https://github.com/SonarSource/sonar-java/blob/master/java-jacoco/src/main/java/org/sonar/plugins/jacoco/JacocoReportReader.java
 * @author Fabian Trautsch
 */
public class JacocoReportReader {

    @Nullable
    private final File jacocoExecutionData;

    private static final Logger LOGGER = LogManager.getLogger(JacocoReportReader.class.getName());

    public JacocoReportReader(@Nullable File jacocoExecutionData) {
        this.jacocoExecutionData = jacocoExecutionData;
    }

    /**
     * Read JaCoCo report determining the format to be used.
     * @param executionDataVisitor visitor to store execution data.
     * @param sessionInfoStore visitor to store info session.
     * @return true if binary format is the latest one.
     * @throws IOException in case of error or binary format not supported.
     */
    public JacocoReportReader readJacocoReport(IExecutionDataVisitor executionDataVisitor,
                                               ISessionInfoVisitor sessionInfoStore) throws IOException {
        if (jacocoExecutionData == null) {
            return this;
        }

        LOGGER.info("Analyzing {}...", jacocoExecutionData);
        try (InputStream inputStream = new BufferedInputStream(new FileInputStream(jacocoExecutionData))) {
            ExecutionDataReader reader = new ExecutionDataReader(inputStream);
            reader.setSessionInfoVisitor(sessionInfoStore);
            reader.setExecutionDataVisitor(executionDataVisitor);
            reader.read();
        } catch (IOException e) {
            throw new IOException(String.format("Unable to read %s", jacocoExecutionData.getAbsolutePath()), e);
        }
        return this;
    }


    /**
     * Caller must guarantee that {@code classFiles} are actually class file.
     */
    public CoverageBuilder analyzeFiles(ExecutionDataStore executionDataStore, Collection<Path> classFiles) {
        CoverageBuilder coverageBuilder = new CoverageBuilder();
        Analyzer analyzer = new Analyzer(executionDataStore, coverageBuilder);
        for (Path classFilePath : classFiles) {
            analyzeClassFile(analyzer, classFilePath.toFile());
        }

        return coverageBuilder;
    }

    private static void analyzeClassFile(Analyzer analyzer, File classFile) {
        try (InputStream inputStream = new FileInputStream(classFile)) {
            analyzer.analyzeClass(inputStream, classFile.getPath());
        } catch (IOException e) {
            LOGGER.warn("Exception during analysis of file " + classFile.getAbsolutePath(), e);
        }
    }

}

