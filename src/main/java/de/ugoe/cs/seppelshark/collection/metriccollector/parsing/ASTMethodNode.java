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

package de.ugoe.cs.seppelshark.collection.metriccollector.parsing;

import com.google.common.base.MoreObjects;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * @author Fabian Trautsch
 */
public class ASTMethodNode {
    private String nodePackage;
    private String nodeClass;
    private String nodeName;
    private Integer lloc;
    private Integer cloc;
    private Integer mcCC;

    public ASTMethodNode(String nodePackage, String nodeClass, String nodeName) {
        this.nodePackage = nodePackage;
        this.nodeClass = nodeClass;
        this.nodeName = nodeName;
    }

    public String getFQNWithMethod() {
        return this.nodePackage+"."+this.nodeClass+"."+this.nodeName;
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

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
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

    public Integer getMcCC() {
        return mcCC;
    }

    public void setMcCC(Integer mcCC) {
        this.mcCC = mcCC;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof ASTMethodNode)) {
            return false;
        }

        if(obj == this) {
            return true;
        }

        ASTMethodNode otherNode = (ASTMethodNode) obj;
        return new EqualsBuilder()
                .append(nodePackage, otherNode.nodePackage)
                .append(nodeClass, otherNode.nodeClass)
                .append(nodeName, otherNode.nodeName)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31)
                .append(nodePackage)
                .append(nodeClass)
                .append(nodeName)
                .toHashCode();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("package", nodePackage)
                .add("class", nodeClass)
                .add("method", nodeName)
                .add("lloc", lloc)
                .add("cloc", cloc)
                .add("mcCC", mcCC)
                .toString();
    }
}
