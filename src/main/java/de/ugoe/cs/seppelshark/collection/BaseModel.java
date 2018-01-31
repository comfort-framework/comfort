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

package de.ugoe.cs.seppelshark.collection;

import de.ugoe.cs.seppelshark.annotations.SupportsClass;
import de.ugoe.cs.seppelshark.annotations.SupportsJava;
import de.ugoe.cs.seppelshark.annotations.SupportsMethod;
import de.ugoe.cs.seppelshark.annotations.SupportsPython;
import de.ugoe.cs.seppelshark.configuration.GeneralConfiguration;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * @author Fabian Trautsch
 */
public class BaseModel {
    protected GeneralConfiguration generalConf;
    protected Logger logger;

    public BaseModel(GeneralConfiguration generalConfiguration) {
        generalConf = generalConfiguration;
        logger = LogManager.getLogger(this.getClass().getName());
    }


    protected boolean checkIfParameterTypesAreFitting(Method method, Set<String> classNamesOfArguments) {
        // We also need to check the parameter types
        Set<String> parameterTypes = new HashSet<>();

        for(Class paramType: method.getParameterTypes()) {
            parameterTypes.add(paramType.getName());
        }

        return parameterTypes.equals(classNamesOfArguments);
    }

    protected boolean shouldMethodBeExecutedIgnoreLevel(Method method) {
        boolean shouldBeExecuted = false;

        // First check for language
        if(generalConf.getLanguage().equals("java")) {
            shouldBeExecuted = method.isAnnotationPresent(SupportsJava.class);
        } else if(generalConf.getLanguage().equals("python")) {
            shouldBeExecuted = method.isAnnotationPresent(SupportsPython.class);
        }

        return shouldBeExecuted;
    }

    protected boolean shouldMethodBeExecuted(Method method) {
        boolean shouldBeExecuted = shouldMethodBeExecutedIgnoreLevel(method);

        // If the level of execution is important we need to check for SupportsX annotations
        if (generalConf.getMethodLevel()) {
            return shouldBeExecuted && method.isAnnotationPresent(SupportsMethod.class);
        } else {
            return shouldBeExecuted && method.isAnnotationPresent(SupportsClass.class);
        }
    }
}
