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

package de.ugoe.cs.comfort.filer.models;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Fabian Trautsch
 */
public class ResultSet {
    private Set<Result> results = new HashSet<>();

    public ResultSet() {

    }

    public Set<Result> getResults() {
        return results;
    }

    public void addResults(Set<Result> results) {
        Set<Result> newResultSet = new HashSet<>();
        for(Result resultToStore: results) {
            Result foundResult = getResultById(resultToStore.getId());
            foundResult.mergeResult(resultToStore);
            newResultSet.add(foundResult);
        }
        this.results = newResultSet;
    }

    private Result getResultById(String id) {
        for(Result result: this.results) {
            if(result.getId().equals(id)) {
                return result;
            }
        }

        return new Result(id);
    }


}
