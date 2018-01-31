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
import de.ugoe.cs.seppelshark.Utils;
import java.nio.file.Path;
import java.util.Comparator;
import javax.annotation.Nonnull;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * @author Fabian Trautsch
 */
public class JavaClass implements Comparable<JavaClass>, Comparator<JavaClass>, IUnit {
    protected String jPackage;
    protected String jClass;
    protected Path fileName;

    public JavaClass(String jPackage, String jClass, Path fileName) {
        this.jPackage = jPackage;
        this.jClass = jClass;
        this.fileName = fileName;
    }

    public JavaClass(String fullyQualifiedName, Path fileName) {
        this.jPackage = Utils.getPackageName(fullyQualifiedName);
        this.jClass = Utils.getClassName(fullyQualifiedName);
        this.fileName = fileName;
    }

    @Override
    public String getPackage() {
        return jPackage;
    }

    public Path getFilePath() {
        return fileName;
    }

    public Boolean isTestBasedOnFileName() {
        return fileName.toString().toLowerCase().contains("test");
    }

    public Boolean isTestBasedOnFQNofUnit() {
        return getFQNOfUnit().toLowerCase().contains("test");
    }

    @Override
    public String getFQNOfUnit() {
        if(jPackage == null || jPackage.equals("")) {
            return jClass;
        }

        return jPackage+"."+jClass;
    }

    @Override
    public String getFQN() {
        return getFQNOfUnit();
    }

    public Boolean isInnerClass() {
        return jClass.contains("$");
    }

    @Override
    public int compareTo(@Nonnull JavaClass o) {
        return new CompareToBuilder()
                .append(jPackage, o.jPackage)
                .append(jClass, o.jClass)
                .append(fileName, o.fileName)
                .toComparison();
    }

    @Override
    public int compare(JavaClass o1, JavaClass o2) {
        return o1.compareTo(o2);
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof JavaClass)) {
            return false;
        }

        if(obj == this) {
            return true;
        }

        JavaClass otherNode = (JavaClass) obj;
        return new EqualsBuilder()
                .append(jPackage, otherNode.jPackage)
                .append(jClass, otherNode.jClass)
                .append(fileName, otherNode.fileName)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31)
                .append(jPackage)
                .append(jClass)
                .append(fileName)
                .toHashCode();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("jPackage", jPackage)
                .add("jClass", jClass)
                .add("fileName", fileName)
                .toString();
    }
}
