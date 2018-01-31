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

package de.ugoe.cs.comfort.data.models;

import com.google.common.base.MoreObjects;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;


/**
 * @author Fabian Trautsch
 */
public class PythonMethod extends PythonModule implements IUnit {
    private String nameSpace;
    private String method;
    private List<String> parameter = new ArrayList<>();

    public PythonMethod(String pPackage, String pModule, String nameSpace, String method, List<String> parameter,
                        Path fileName) {
        super(pPackage, pModule, fileName);
        this.nameSpace = nameSpace;
        this.method = method;
        this.parameter = parameter;
    }

    public PythonMethod(String pPackage, String pModule, String nameSpace, String method, Path fileName) {
        super(pPackage, pModule, fileName);
        this.nameSpace = nameSpace;
        this.method = method;
    }

    // Constructor for converting JSON Python coverage object to this class
    public PythonMethod(PythonCoverageLoaderTestMethod covPythonMethod, Path fileName) {
        super(covPythonMethod.getModule(), fileName);
        this.nameSpace = covPythonMethod.getNameSpace();
        this.method = covPythonMethod.getMethod();
    }

    // Constructor for converting JSON Python coverage object to this class
    public PythonMethod(PythonCoveragerloaderTestedMethod covPythonMethod, Path fileName) {
        super(covPythonMethod.getModule(), fileName);
        this.nameSpace = covPythonMethod.getNameSpace();
        this.method = covPythonMethod.getMethod();
    }

    public void setFileName(Path fileName) {
        this.fileName = fileName;
    }

    public Boolean isTestBasedOnFQNOfMethod() {
        return getFQN().toLowerCase().contains("test");
    }

    public Boolean isTestBasedOnMethod() {
        return this.method.toLowerCase().contains("test");
    }

    @Override
    public String getFQN() {
        return getFQNOfUnit()+"."+nameSpace+"."+method;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof PythonMethod)) {
            return false;
        }

        if(obj == this) {
            return true;
        }

        PythonMethod otherNode = (PythonMethod) obj;
        return new EqualsBuilder()
                .appendSuper(super.equals(obj))
                .append(nameSpace, otherNode.nameSpace)
                .append(method, otherNode.method)
                .append(parameter, otherNode.parameter)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31)
                .appendSuper(super.hashCode())
                .append(nameSpace)
                .append(method)
                .append(parameter)
                .toHashCode();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("pPackage", pPackage)
                .add("pModule", pModule)
                .add("namespace", nameSpace)
                .add("method", method)
                .add("parameter", parameter)
                .add("fileName", fileName)
                .toString();

    }
}
