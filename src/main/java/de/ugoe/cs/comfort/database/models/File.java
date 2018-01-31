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

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;
import org.mongodb.morphia.utils.IndexType;

/**
 * @author Fabian Trautsch
 */

@Entity("file")
@Indexes({
        @Index(fields = @Field(value = "vcs_system_id", type = IndexType.ASC)),
        @Index(fields = {
                @Field(value = "path", type = IndexType.ASC),
                @Field(value = "vcs_system_id", type = IndexType.ASC)
        }, options = @IndexOptions(unique = true))
})
public class File {
    @Id
    private ObjectId id;

    @Property("vcs_system_id")
    private ObjectId vcsSystemId;

    private String path;

    public File() {

    }

    public File(ObjectId vcsSystemId, String path) {
        this.vcsSystemId = vcsSystemId;
        this.path = path;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public ObjectId getVcsSystemId() {
        return vcsSystemId;
    }

    public void setVcsSystemId(ObjectId vcsSystemId) {
        this.vcsSystemId = vcsSystemId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
