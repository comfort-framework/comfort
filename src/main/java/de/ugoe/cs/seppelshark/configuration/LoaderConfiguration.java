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

package de.ugoe.cs.seppelshark.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Fabian Trautsch
 */
public class LoaderConfiguration extends BaseConfiguration {
    private String vcsSystemUrl;
    private Database database = new Database();
    private String coverageLocation = null;
    private String callGraphLocation = null;

    public LoaderConfiguration(@JsonProperty("name") String name) {
        super(name);
    }


    public Path getCallGraphLocation() {
        return Paths.get(callGraphLocation);
    }

    public void setCallGraphLocation(String callGraphLocation) {
        this.callGraphLocation = callGraphLocation;
    }

    public Path getCoverageLocation() {
        return Paths.get(coverageLocation);
    }

    public void setCoverageLocation(String coverageLocation) {
        this.coverageLocation = coverageLocation;
    }

    public String getVcsSystemUrl() {
        return vcsSystemUrl;
    }

    public void setVcsSystemUrl(String vcsSystemUrl) {
        this.vcsSystemUrl = vcsSystemUrl;
    }

    public void setDatabase(Database database) {
        this.database = database;
    }

    public Database getDatabase() {
        return this.database;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("Name", this.name)
                .add("database", database)
                .add("vcsSystemUrl", vcsSystemUrl)
                .add("coverageLocation", coverageLocation)
                .add("callGraphLocation", callGraphLocation)
                .toString();
    }
}
