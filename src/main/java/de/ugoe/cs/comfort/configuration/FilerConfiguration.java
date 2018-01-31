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

import com.google.common.base.MoreObjects;

/**
 * @author Fabian Trautsch
 */
public class FilerConfiguration extends BaseConfiguration {
    private String metricsCSVPath;
    private String mutationCSVPath;
    private String fileName;

    public FilerConfiguration() {

    }

    public FilerConfiguration(String name) {
        super(name);
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setMetricsCSVPath(String metricsCSVPath) {
        this.metricsCSVPath = metricsCSVPath;
    }

    public String getMetricsCSVPath() {
        return metricsCSVPath;
    }

    public void setMutationCSVPath(String mutationCSVPath) {
        this.mutationCSVPath = mutationCSVPath;
    }

    public String getMutationCSVPath() {
        return mutationCSVPath;
    }


    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("Name", this.name)
                .add("metricsCSVPath", metricsCSVPath)
                .add("mutationCSVPath", mutationCSVPath)
                .add("fileName", fileName)
                .toString();
    }
}
