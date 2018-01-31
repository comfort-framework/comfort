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

package de.ugoe.cs.seppelshark.filer.models;

import com.google.common.base.MoreObjects;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;


/**
 * @author Fabian Trautsch
 */
public class Result {
    private String id;
    private Path pathToFile;
    private Map<String, String> metrics = new HashMap<>();
    private Set<Mutation> mutationResults = new HashSet<>();

    public Result(String id, Path pathToFile) {
        this.id = id;
        this.pathToFile = pathToFile;
    }

    public Result(String id) {
        this.id = id;
    }

    public Result(String id, Path pathToFile, String metricName, String metricValue) {
        this.id = id;
        this.pathToFile = pathToFile;
        this.addMetric(metricName, metricValue);
    }

    public String getId() {
        return id;
    }

    public void mergeResult(Result resultToMerge) {
        // We do not need to merge the id, as it is used to define which results can be merged.

        this.pathToFile = resultToMerge.pathToFile;
        this.metrics.putAll(resultToMerge.metrics);
        this.mutationResults.addAll(resultToMerge.mutationResults);
    }

    public void addMutationResults(Set<Mutation> mutationResults) {
        this.mutationResults = mutationResults;
    }

    public void addMetric(String name, String value) {
        this.metrics.put(name, value);
    }

    public String getMetric(String name) {
        return this.metrics.get(name);
    }

    public Set<Mutation> getMutationResults() {
        return mutationResults;
    }

    public Path getPathToFile() {
        return pathToFile;
    }

    public Map<String, String> getMetrics() {
        return metrics;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("path", pathToFile)
                .add("metrics", metrics)
                .add("mutationResults", mutationResults)
                .toString();
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Result)) {
            return false;
        }

        if(obj == this) {
            return true;
        }

        Result otherNode = (Result) obj;
        return new EqualsBuilder()
                .append(id, otherNode.id)
                .append(pathToFile, otherNode.pathToFile)
                .append(metrics, otherNode.metrics)
                .append(mutationResults, otherNode.mutationResults)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31)
                .append(id)
                .append(pathToFile)
                .append(metrics)
                .append(mutationResults)
                .toHashCode();
    }

}
