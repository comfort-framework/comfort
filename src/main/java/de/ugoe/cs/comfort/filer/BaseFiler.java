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

package de.ugoe.cs.comfort.filer;

import de.ugoe.cs.comfort.collection.BaseModel;
import de.ugoe.cs.comfort.configuration.FilerConfiguration;
import de.ugoe.cs.comfort.configuration.GeneralConfiguration;
import de.ugoe.cs.comfort.filer.models.Result;
import de.ugoe.cs.comfort.filer.models.ResultSet;
import java.io.IOException;
import java.util.Set;

/**
 * @author Fabian Trautsch
 */
public abstract class BaseFiler extends BaseModel {
    protected ResultSet resultSet = new ResultSet();
    protected FilerConfiguration filerConfiguration;

    public BaseFiler(GeneralConfiguration generalConfiguration, FilerConfiguration filerConfiguration) {
        super(generalConfiguration);

        this.filerConfiguration = filerConfiguration;
    }

    public abstract void storeResults(Set<Result> results) throws IOException;

    /**
     *
     * Stores the result in the database. The result is NOT merged
     * with earlier results!
     * @param result result of the metriccollector execution
     * @throws IOException thrown if there is a problem with storing the result
     */
    public abstract void storeResult(Result result) throws IOException;
}
