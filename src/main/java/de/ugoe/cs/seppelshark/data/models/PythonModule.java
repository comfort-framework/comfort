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

package de.ugoe.cs.seppelshark.data.models;

import com.google.common.base.MoreObjects;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import javax.annotation.Nonnull;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;


/**
 * @author Fabian Trautsch
 */
public class PythonModule implements Comparable<PythonModule>, Comparator<PythonModule>, IUnit {
    protected String pPackage;
    protected String pModule;
    protected Path fileName;

    public PythonModule(String pPackage, String pModule, Path fileName) {
        this.pPackage = pPackage;
        this.pModule = pModule;
        this.fileName = fileName;
    }

    public PythonModule(String fqn, Path fileName) {
        String[] fqnParts = fqn.split("\\.");
        this.pModule = fqnParts[fqnParts.length-1];
        this.pPackage = String.join(".", Arrays.copyOfRange(fqnParts, 0, fqnParts.length-1));
        this.fileName = fileName;
    }

    @Override
    public String getPackage() {
        return pPackage;
    }

    @Override
    public Path getFilePath() {
        return fileName;
    }

    public Boolean isTestBasedOnFileName() {
        return fileName.toString().toLowerCase().contains("test");
    }

    public Boolean isTestBasedOnFQNofUnit() {
        return getFQNOfUnit().toLowerCase().contains("test");
    }

    public String getModule() {
        return pModule;
    }

    @Override
    public String getFQNOfUnit() {
        return pPackage+"."+pModule;
    }

    @Override
    public String getFQN() {
        return getFQNOfUnit();
    }

    @Override
    public int compareTo(@Nonnull PythonModule o) {
        return new CompareToBuilder()
                .append(pPackage, o.pPackage)
                .append(pModule, o.pModule)
                .append(fileName, o.fileName)
                .toComparison();
    }

    @Override
    public int compare(PythonModule o1, PythonModule o2) {
        return o1.compareTo(o2);
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof PythonModule)) {
            return false;
        }

        if(obj == this) {
            return true;
        }

        PythonModule otherNode = (PythonModule) obj;
        return new EqualsBuilder()
                .append(pPackage, otherNode.pPackage)
                .append(pModule, otherNode.pModule)
                .append(fileName, otherNode.fileName)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31)
                .append(pPackage)
                .append(pModule)
                .append(fileName)
                .toHashCode();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("pPackage", pPackage)
                .add("pModule", pModule)
                .add("fileName", fileName)
                .toString();
    }
}
