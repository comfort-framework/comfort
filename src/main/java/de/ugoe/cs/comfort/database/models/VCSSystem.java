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

package de.ugoe.cs.comfort.database.models;

import java.util.Date;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;
import org.mongodb.morphia.utils.IndexType;

/**
 * @author Fabian Trautsch
 */

@Entity("vcs_system")
@Indexes({
        @Index(fields = @Field(value = "url", type = IndexType.HASHED))
})
public class VCSSystem {
    @Id
    private ObjectId id;

    @Indexed(options = @IndexOptions(unique = true))
    private String url;

    @Property("project_id")
    private ObjectId projectId;

    @Property("repository_type")
    private String repositoryType;

    @Property("last_updated")
    private Date lastUpdated;

    public VCSSystem() {

    }

    public VCSSystem(String url, ObjectId projectId, String repositoryType, Date lastUpdated) {
        this.url = url;
        this.projectId = projectId;
        this.repositoryType = repositoryType;
        this.lastUpdated = (Date) lastUpdated.clone();
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public ObjectId getProjectId() {
        return projectId;
    }

    public void setProjectId(ObjectId projectId) {
        this.projectId = projectId;
    }

    public String getRepositoryType() {
        return repositoryType;
    }

    public void setRepositoryType(String repositoryType) {
        this.repositoryType = repositoryType;
    }

    public Date getLastUpdated() {
        return (Date)lastUpdated.clone();
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = (Date)lastUpdated.clone();
    }
}
