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
import java.util.HashSet;
import java.util.Set;

/**
 * @author Fabian Trautsch
 */
public class PythonCoverageLoaderTestMethod {
    private Set<PythonCoveragerloaderTestedMethod> tests;
    private String module;
    private String namespace;
    private String method;
    private String location;

    @JsonCreator
    public PythonCoverageLoaderTestMethod(@JsonProperty("unique_id") String uniqueIdentifier,
                                          @JsonProperty("module") String module,
                                          @JsonProperty("namespace") String namespace,
                                          @JsonProperty("method") String method,
                                          @JsonProperty("location") String location,
                                          @JsonProperty("tests") Set<PythonCoveragerloaderTestedMethod> tests) {
        this.namespace = namespace;
        this.module = module;
        this.method = method;
        this.location = location;

        // Little hack, as jackson can not creaet the correct python objects otherwise
        Set<PythonCoveragerloaderTestedMethod> testsSet = new HashSet<>();
        testsSet.addAll(tests);
        this.tests = testsSet;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("module", module)
                .add("namespace", namespace)
                .add("method", method)
                .add("location", location)
                .add("tests", tests)
                .toString();
    }

    public Set<PythonCoveragerloaderTestedMethod> getTestedMethods() {
        return tests;
    }

    public String getFileNameRepresentation() {
        return module;
    }

    public void setTestedMethods(Set<PythonCoveragerloaderTestedMethod> tests) {
        this.tests = tests;
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

    public String getLocation() {
        return this.location;
    }
}
