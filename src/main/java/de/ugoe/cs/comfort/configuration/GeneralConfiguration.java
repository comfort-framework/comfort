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

package de.ugoe.cs.comfort.configuration;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import de.ugoe.cs.comfort.filer.BaseFiler;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


/**
 * @author Fabian Trautsch
 */
public class GeneralConfiguration {
    private Path projectDir;
    private String projectName = "default";
    private String language = "java";
    private String logLevel = "DEBUG";
    private String logFile = "/tmp/comfort.out";

    @JsonProperty("nThreads")
    private int nThreads = 1;

    private boolean methodLevel = false;

    @JsonProperty("collections")
    private List<CollectionConfiguration> collections = new ArrayList<>();

    @JsonProperty("filer")
    private FilerConfiguration filerConfiguration = new FilerConfiguration();

    @JsonGetter("filer")
    public FilerConfiguration getFilerConfiguration() {
        return filerConfiguration;
    }

    public void setFilerConfiguration(FilerConfiguration filerConfiguration) {
        this.filerConfiguration = filerConfiguration;
    }

    @JsonGetter("collections")
    public List<CollectionConfiguration> getCollections() {
        return collections;
    }

    @JsonProperty("forceRerun")
    private boolean forceRerun = false;

    public boolean isForceRerun() {
        return forceRerun;
    }

    public void setForceRerun(boolean forceRerun) {
        this.forceRerun = forceRerun;
    }

    public void setMethodLevel(boolean methodLevel) {
        this.methodLevel = methodLevel;
    }

    public boolean getMethodLevel() {
        return methodLevel;
    }

    public void setProjectDir(String projectDir) {
        this.projectDir = Paths.get(projectDir);
    }

    public Path getProjectDir() {
        return this.projectDir;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getLanguage() {
        return language.toLowerCase();
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    public String getLogLevel() {
        return logLevel;
    }

    public void setLogFile(String logFile) {
        this.logFile = logFile;
    }

    public String getLogFile() {
        return logFile;
    }

    public int getNThreads() {
        return nThreads;
    }

    public void setNThreads(int nThreads) {
        this.nThreads = nThreads;
    }

    public BaseFiler getFiler() throws ClassNotFoundException, IllegalAccessException,
            InstantiationException, NoSuchMethodException, InvocationTargetException {
        return (BaseFiler) Class.forName("de.ugoe.cs.comfort.filer." + this.filerConfiguration.getName())
                .getConstructor(GeneralConfiguration.class, FilerConfiguration.class)
                .newInstance(this, filerConfiguration);
    }

    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("projectDir", projectDir)
                .add("filerConfiguration", filerConfiguration)
                .toString();
    }
}
