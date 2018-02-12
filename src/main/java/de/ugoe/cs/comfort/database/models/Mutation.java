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

import com.google.common.base.MoreObjects;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

/**
 * @author Fabian Trautsch
 */
@Entity(value = "mutation", noClassnameStored = true)
public class Mutation {
    @Id
    @Property("_id")
    private ObjectId id;

    @Property("m_type")
    private String mType;

    private String location;

    @Property("l_num")
    private Integer lineNumber;

    private String classification;

    public Mutation() {

    }

    public Mutation(String location, String mType, Integer lineNumber, String changeClassification) {
        this.location = location;
        this.mType = mType;
        this.lineNumber = lineNumber;
        this.classification = changeClassification;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getMType() {
        return mType;
    }

    public void setMType(String type) {
        this.mType = type;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getClassification() {
        return classification;
    }

    public void setClassification(String classification) {
        this.classification = classification;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("location", location)
                .add("mType", mType)
                .add("lineNumber", lineNumber)
                .add("classification", classification)
                .toString();
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Mutation)) {
            return false;
        }

        if(obj == this) {
            return true;
        }

        Mutation otherNode = (Mutation) obj;
        return new EqualsBuilder()
                .append(location, otherNode.location)
                .append(mType, otherNode.mType)
                .append(lineNumber, otherNode.lineNumber)
                .append(classification, otherNode.classification)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31)
                .append(location)
                .append(mType)
                .append(lineNumber)
                .append(classification)
                .toHashCode();
    }
}