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

@Entity("file_action")
@Indexes({
        @Index(fields = @Field(value = "id", type = IndexType.HASHED)),
        @Index(fields = @Field(value = "commit_id", type = IndexType.ASC)),
        @Index(fields = {
                @Field(value = "commit_id", type = IndexType.ASC),
                @Field(value = "file_id", type = IndexType.ASC)
        })
})
public class FileAction {
    @Id
    private ObjectId id;

    @Property("file_id")
    private ObjectId fileId;

    @Property("commit_id")
    private ObjectId commitId;

    private String mode;

    @Property("size_at_commit")
    private Integer sizeAtCommit;

    @Property("lines_added")
    private Integer linesAdded;

    @Property("lines_deleted")
    private Integer linesDeleted;

    @Property("is_binary")
    private Boolean isBinary;

    @Property("old_file_id")
    private ObjectId oldFileId;

    public FileAction() {

    }

    public FileAction(ObjectId fileId, ObjectId commitId) {
        this.fileId = fileId;
        this.commitId = commitId;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public ObjectId getFileId() {
        return fileId;
    }

    public void setFileId(ObjectId fileId) {
        this.fileId = fileId;
    }

    public ObjectId getCommitId() {
        return commitId;
    }

    public void setCommitId(ObjectId commitId) {
        this.commitId = commitId;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public Integer getSizeAtCommit() {
        return sizeAtCommit;
    }

    public void setSizeAtCommit(Integer sizeAtCommit) {
        this.sizeAtCommit = sizeAtCommit;
    }

    public Integer getLinesAdded() {
        return linesAdded;
    }

    public void setLinesAdded(Integer linesAdded) {
        this.linesAdded = linesAdded;
    }

    public Integer getLinesDeleted() {
        return linesDeleted;
    }

    public void setLinesDeleted(Integer linesDeleted) {
        this.linesDeleted = linesDeleted;
    }

    public Boolean getIsBinary() {
        return isBinary;
    }

    public void setIsBinary(Boolean binary) {
        isBinary = binary;
    }

    public ObjectId getOldFileId() {
        return oldFileId;
    }

    public void setOldFileId(ObjectId oldFileId) {
        this.oldFileId = oldFileId;
    }
}
