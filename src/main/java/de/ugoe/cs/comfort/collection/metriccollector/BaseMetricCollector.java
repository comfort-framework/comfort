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

package de.ugoe.cs.comfort.collection.metriccollector;

import de.ugoe.cs.comfort.FileNameUtils;
import de.ugoe.cs.comfort.collection.BaseModel;
import de.ugoe.cs.comfort.configuration.GeneralConfiguration;
import de.ugoe.cs.comfort.data.DataSet;
import de.ugoe.cs.comfort.exception.MetricCollectorException;
import de.ugoe.cs.comfort.filer.models.Result;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;



/**
 * @author Fabian Trautsch
 */
public class BaseMetricCollector extends BaseModel {
    protected FileNameUtils fileNameUtils;


    BaseMetricCollector(GeneralConfiguration configuration) {
        super(configuration);

        if(generalConf.getProjectDir() != null) {
            this.fileNameUtils = new FileNameUtils(generalConf);
        }
    }

    public Set<Result> collectData(DataSet data) throws MetricCollectorException {
        Set<String> classNamesOfArguments = new HashSet<>();
        classNamesOfArguments.add(data.getClass().getName());

        for(Method method : this.getClass().getDeclaredMethods()) {
            try {
                // We need to find the method where the annotations are fitting to the configuration and
                // where the parameter types are fitting
                if (shouldMethodBeExecuted(method) && checkIfParameterTypesAreFitting(method, classNamesOfArguments)) {
                    return (Set<Result>) method.invoke(this, data);
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new MetricCollectorException(e.getMessage());
            }
        }
        throw new MetricCollectorException("No method found that supports "+generalConf.getLanguage()
                +" on "+generalConf.getMethodLevel()+" level");
    }


}
