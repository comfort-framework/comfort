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

package de.ugoe.cs.comfort.collection.metriccollector.mutation;

import com.google.common.base.MoreObjects;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * @author Fabian Trautsch
 */
public class MutationLocation {
    private String mType;

    private String location;

    private Integer lineNumber;

    public MutationLocation(String mType, String location, Integer lineNumber) {
        this.mType = mType;
        this.location = location;
        this.lineNumber = lineNumber;
    }

    public String getmType() {
        return mType;
    }

    public void setmType(String mType) {
        this.mType = mType;
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

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("location", location)
                .add("mType", mType)
                .add("lineNumber", lineNumber)
                .toString();
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof MutationLocation)) {
            return false;
        }

        if(obj == this) {
            return true;
        }

        MutationLocation otherNode = (MutationLocation) obj;
        return new EqualsBuilder()
                .append(location, otherNode.location)
                .append(mType, otherNode.mType)
                .append(lineNumber, otherNode.lineNumber)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31)
                .append(location)
                .append(mType)
                .append(lineNumber)
                .toHashCode();
    }
}
