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

package de.ugoe.cs.comfort.collection.filter;

import de.ugoe.cs.comfort.collection.BaseModel;
import de.ugoe.cs.comfort.configuration.GeneralConfiguration;
import de.ugoe.cs.comfort.data.DataSet;
import de.ugoe.cs.comfort.exception.FilterException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;


/**
 * @author Fabian Trautsch
 */
public class BaseFilter extends BaseModel {
    public BaseFilter(GeneralConfiguration generalConfiguration) {
        super(generalConfiguration);
    }

    public DataSet filterData(DataSet dataSet) throws FilterException {
        DataSet filteredDataSet = dataSet;
        Set<String> classNamesOfArguments = new HashSet<>();
        classNamesOfArguments.add(filteredDataSet.getClass().getName());

        for(Method method : this.getClass().getDeclaredMethods()) {
            // We need to find the method where the annotations are fitting to the configuration and
            // where the parameter types are fitting
            try {
                if (shouldMethodBeExecutedIgnoreLevel(method)
                        && checkIfParameterTypesAreFitting(method, classNamesOfArguments)) {
                    logger.info("Using filter method {} from class {}...",
                            method.getName(), this.getClass().getName());
                    filteredDataSet = (DataSet) method.invoke(this, filteredDataSet);
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new FilterException("Could not filter data: " + e.getMessage());
            }
        }
        return filteredDataSet;
    }
}
