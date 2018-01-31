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
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * @author Fabian Trautsch
 */
public class JavaMethod extends JavaClass implements IUnit {
    private String method;
    private List<String> parameter = new ArrayList<>();

    public JavaMethod(String jPackage, String jClass, String method, List<String> parameter, Path fileName) {
        super(jPackage, jClass, fileName);
        this.method = method;
        this.parameter = parameter;
    }

    public JavaMethod(String jPackage, String jClass, String method, Path fileName) {
        super(jPackage, jClass, fileName);
        this.method = method;
    }

    public JavaMethod(String fullyQualifiedName, String methodName, List<String> parameter, Path fileName) {
        super(Utils.getPackageName(fullyQualifiedName), Utils.getClassName(fullyQualifiedName), fileName);
        this.method = methodName;
        this.parameter = parameter;
    }

    public Boolean isTestBasedOnFQNOfMethod() {
        return getFQN().toLowerCase().contains("test");
    }

    @Override
    public String getFQN() {
        return getFQNOfUnit()+"."+method;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof JavaMethod)) {
            return false;
        }

        if(obj == this) {
            return true;
        }

        JavaMethod otherNode = (JavaMethod) obj;
        return new EqualsBuilder()
                .appendSuper(super.equals(obj))
                .append(method, otherNode.method)
                .append(parameter, otherNode.parameter)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31)
                .appendSuper(super.hashCode())
                .append(method)
                .append(parameter)
                .toHashCode();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("jPackage", jPackage)
                .add("jClass", jClass)
                .add("fileName", fileName)
                .add("method", method)
                .add("parameter", parameter)
                .toString();

    }
}
