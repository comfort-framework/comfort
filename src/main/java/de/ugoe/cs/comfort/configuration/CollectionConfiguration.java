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

package de.ugoe.cs.comfort.configuration;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.ugoe.cs.comfort.collection.filter.BaseFilter;
import de.ugoe.cs.comfort.collection.loader.BaseLoader;
import de.ugoe.cs.comfort.collection.metriccollector.BaseMetricCollector;
import de.ugoe.cs.comfort.filer.BaseFiler;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Fabian Trautsch
 */
public class CollectionConfiguration {

    @JsonProperty("loader")
    private LoaderConfiguration loaderConf;

    @JsonGetter("loader")
    public LoaderConfiguration getLoaderConfiguration() {
        return loaderConf;
    }

    @JsonProperty("filters")
    private List<String> filterConf = new ArrayList<>();

    @JsonProperty("collectors")
    private List<String> collectorConf = new ArrayList<>();

    public BaseLoader getLoader(GeneralConfiguration conf) throws ClassNotFoundException, NoSuchMethodException,
            IllegalAccessException, InvocationTargetException, InstantiationException {
        return (BaseLoader) Class
                .forName("de.ugoe.cs.comfort.collection.loader." + loaderConf.getName())
                .getConstructor(GeneralConfiguration.class, LoaderConfiguration.class)
                .newInstance(conf, loaderConf);
    }

    public List<BaseFilter> getFilter(GeneralConfiguration conf) throws ClassNotFoundException, NoSuchMethodException,
            IllegalAccessException, InvocationTargetException, InstantiationException {
        List<BaseFilter> filters = new ArrayList<>();
        for(String filter : filterConf) {
            filters.add((BaseFilter) Class
                        .forName("de.ugoe.cs.comfort.collection.filter." + filter)
                        .getConstructor(GeneralConfiguration.class)
                        .newInstance(conf));
        }
        return filters;
    }

    public List<BaseMetricCollector> getMetricCollectors(GeneralConfiguration conf, BaseFiler filer)
            throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException,
            InstantiationException {
        List<BaseMetricCollector> collectors = new ArrayList<>();
        for(String collector : collectorConf) {
            collectors.add((BaseMetricCollector) Class
                    .forName("de.ugoe.cs.comfort.collection.metriccollector." + collector)
                    .getConstructor(GeneralConfiguration.class, BaseFiler.class)
                    .newInstance(conf, filer));
        }
        return collectors;
    }
}
