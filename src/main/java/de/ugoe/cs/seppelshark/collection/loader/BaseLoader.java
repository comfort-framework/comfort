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

package de.ugoe.cs.seppelshark.collection.loader;

import de.ugoe.cs.seppelshark.collection.BaseModel;
import de.ugoe.cs.seppelshark.configuration.GeneralConfiguration;
import de.ugoe.cs.seppelshark.configuration.LoaderConfiguration;
import de.ugoe.cs.seppelshark.data.DataSet;
import de.ugoe.cs.seppelshark.exception.LoaderException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


/**
 * @author Fabian Trautsch
 */
public class BaseLoader extends BaseModel {
    protected LoaderConfiguration loaderConf;

    public BaseLoader(GeneralConfiguration generalConfiguration, LoaderConfiguration loaderConfiguration) {
        super(generalConfiguration);
        this.loaderConf = loaderConfiguration;
    }

    public DataSet loadData() throws LoaderException {
        for(Method method : this.getClass().getDeclaredMethods()) {
            try {
                if (shouldMethodBeExecutedIgnoreLevel(method)) {
                    this.logger.info("Using data loader method {} from class {}...", method.getName(),
                            this.getClass().getName());
                    return (DataSet) method.invoke(this);
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new LoaderException("Could not load data: " + e.getMessage());
            }
        }
        throw new LoaderException("No method found that supports "+generalConf.getLanguage());
    }
}
