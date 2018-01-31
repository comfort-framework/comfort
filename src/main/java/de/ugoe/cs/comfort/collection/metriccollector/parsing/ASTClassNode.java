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

package de.ugoe.cs.comfort.collection.metriccollector.parsing;

import com.google.common.base.MoreObjects;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * @author Fabian Trautsch
 */
public class ASTClassNode {
    private String nodePackage;
    private String nodeClass;
    private Integer lloc;
    private Integer cloc;

    public ASTClassNode() {

    }

    public ASTClassNode(String nodePackage, String nodeClass) {
        this.nodePackage = nodePackage;
        this.nodeClass = nodeClass;
    }

    public String getFQN() {
        return this.nodePackage + "." + this.nodeClass;
    }

    public String getNodePackage() {
        return nodePackage;
    }

    public void setNodePackage(String nodePackage) {
        this.nodePackage = nodePackage;
    }

    public String getNodeClass() {
        return nodeClass;
    }

    public void setNodeClass(String nodeClass) {
        this.nodeClass = nodeClass;
    }

    public Integer getLLOC() {
        return lloc;
    }

    public void setLLOC(Integer lloc) {
        this.lloc = lloc;
    }

    public Integer getCLOC() {
        return cloc;
    }

    public void setCLOC(Integer cloc) {
        this.cloc = cloc;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof ASTClassNode)) {
            return false;
        }

        if(obj == this) {
            return true;
        }

        ASTClassNode otherNode = (ASTClassNode) obj;
        return new EqualsBuilder()
                .append(nodePackage, otherNode.nodePackage)
                .append(nodeClass, otherNode.nodeClass)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31)
                .append(nodePackage)
                .append(nodeClass)
                .toHashCode();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("package", nodePackage)
                .add("class", nodeClass)
                .add("LLOC", lloc)
                .add("CLOC", cloc)
                .toString();
    }
}
