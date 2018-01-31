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

import de.ugoe.cs.seppelshark.configuration.GeneralConfiguration;
import de.ugoe.cs.seppelshark.data.ClassFiles;
import de.ugoe.cs.seppelshark.filer.models.Result;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.EmptyVisitor;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.MethodGen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The simplest of class visitors, invokes the method visitor class for each
 * method found. Based on: http://www.spinellis.gr/sw/ckjm/
 * @author Fabian Trautsch
 */
public class MetricClassVisitor extends EmptyVisitor {

    private JavaClass clazz;
    private ConstantPoolGen constants;
    private GeneralConfiguration configuration;
    private Map<String, Integer> numAssertions = new HashMap<>();
    private Set<Result> results;
    private boolean executeOnMethodLevel;
    private ClassFiles classFiles;
    private static final Logger LOGGER = LogManager.getLogger(MetricClassVisitor.class.getName());

    public MetricClassVisitor(JavaClass jc, Set<Result> results, GeneralConfiguration configuration,
                              boolean executeOnMethodLevel, ClassFiles classFiles) {
        clazz = jc;
        constants = new ConstantPoolGen(clazz.getConstantPool());
        this.results = results;
        this.configuration = configuration;
        this.executeOnMethodLevel = executeOnMethodLevel;
        this.classFiles = classFiles;
    }

    public void visitJavaClass(JavaClass jc) {
        jc.getConstantPool().accept(this);
        Method[] methods = jc.getMethods();
        for (Method method : methods) {
            method.accept(this);
        }
    }

    public void visitConstantPool(ConstantPool constantPool) {
        for (int i = 0; i < constantPool.getLength(); i++) {
            Constant constant = constantPool.getConstant(i);
            if (constant == null) {
                continue;
            }
            if (constant.getTag() == 7) {
                String referencedClass =
                        constantPool.constantToString(constant);
                LOGGER.debug("Class {} references Class {}", clazz.getClassName(), referencedClass);
            }
        }
    }

    public void visitMethod(Method method) {
        MethodGen mg = new MethodGen(method, clazz.getClassName(), constants);
        MetricMethodVisitor visitor = new MetricMethodVisitor(mg, clazz);

        // Get identifier (class or method level, depending on configuration) and the number of assertions
        // If class level: just sum up the number of assertions
        String identifier = getIdentifier(mg);
        int numberOfAssertions = numAssertions.getOrDefault(identifier, 0);
        numAssertions.put(identifier, numberOfAssertions+visitor.start());
    }

    public Map<String, Integer> start() {
        visitJavaClass(clazz);

        // Store results
        try {
            for(Map.Entry<String, Integer> entry: numAssertions.entrySet()) {
                Path pathToTest;
                if(executeOnMethodLevel) {
                    pathToTest = classFiles.getFilenameForMethod(entry.getKey());
                } else {
                    pathToTest = classFiles.getFilenameForClass(entry.getKey());
                }
                results.add(
                        new Result(entry.getKey(), pathToTest,"num_asserts", String.valueOf(entry.getValue()))
                );
            }
        } catch (FileNotFoundException e) {
            LOGGER.error("Could not create filename utils!");
        }
        return numAssertions;
    }

    private String getIdentifier(MethodGen mg) {
        if(executeOnMethodLevel) {
            return clazz.getClassName()+"."+mg.getName();
        } else {
            return clazz.getClassName();
        }
    }
}
