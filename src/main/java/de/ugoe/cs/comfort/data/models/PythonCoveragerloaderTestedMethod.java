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

package de.ugoe.cs.comfort.data.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * @author Fabian Trautsch
 */
public class PythonCoveragerloaderTestedMethod {
    private List<Integer> coveredLines;
    private List<Integer> uncoveredLines;
    private Path location;
    private String namespace;
    private String method;
    private String module;

    @JsonCreator
    public PythonCoveragerloaderTestedMethod(@JsonProperty("unique_id") String uniqueIdentifier,
                                             @JsonProperty("module") String module,
                                             @JsonProperty("namespace") String namespace,
                                             @JsonProperty("method") String method,
                                             @JsonProperty("covered_lines") List<Integer> coveredLines,
                                             @JsonProperty("uncovered_lines") List<Integer> uncoveredLines,
                                             @JsonProperty("location") String location) {
        this.namespace = namespace;
        this.module = module;
        this.method = method;
        this.coveredLines = coveredLines;
        this.uncoveredLines = uncoveredLines;
        this.location = Paths.get(location);
    }

    public String getModule() {
        return this.module;
    }

    public String getNameSpace() {
        return this.namespace;
    }

    public String getMethod() {
        return this.method;
    }

    public Integer getCoveredLines() {
        return coveredLines.size();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("module", module)
                .add("namespace", namespace)
                .add("method", method)
                .add("location", location)
                .add("coveredLines", coveredLines)
                .add("uncoveredLines", uncoveredLines)
                .toString();
    }
}
