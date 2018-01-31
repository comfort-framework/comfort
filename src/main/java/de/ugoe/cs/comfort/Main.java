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

package de.ugoe.cs.comfort;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.ugoe.cs.comfort.collection.filter.BaseFilter;
import de.ugoe.cs.comfort.collection.loader.BaseLoader;
import de.ugoe.cs.comfort.collection.metriccollector.BaseMetricCollector;
import de.ugoe.cs.comfort.configuration.CollectionConfiguration;
import de.ugoe.cs.comfort.configuration.GeneralConfiguration;
import de.ugoe.cs.comfort.data.DataSet;
import de.ugoe.cs.comfort.exception.FilterException;
import de.ugoe.cs.comfort.exception.LoaderException;
import de.ugoe.cs.comfort.exception.MetricCollectorException;
import de.ugoe.cs.comfort.filer.IFiler;
import de.ugoe.cs.comfort.filer.models.ResultSet;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.layout.PatternLayout;


/**
 * @author Fabian Trautsch
 */
public class Main {
    private static final Logger LOGGER = LogManager.getLogger(Main.class.getName());

    public static void main(String[] args) {
        if(args.length != 1) {
            System.out.println("You need to give the location to the config.json as argument!");
            System.exit(1);
        }

        try {
            // Load configuration
            GeneralConfiguration config = loadConfiguration(args[0]);

            // SetUp Logging
            setupLogging(config);

            // Create object in which all results are stored
            ResultSet resultSet = new ResultSet();

            // Get collections that should be done
            List<CollectionConfiguration> collections = config.getCollections();

            // Execute each collection
            for(CollectionConfiguration collection: collections) {
                try {
                    // Get data first
                    BaseLoader loader = collection.getLoader(config);
                    LOGGER.info("Using loader {}...", loader.getClass().getName());
                    DataSet data = loader.loadData();
                    LOGGER.info("Loading data successful...");

                    // Afterwards, filter the data
                    List<BaseFilter> filters = collection.getFilter(config);
                    for (BaseFilter filter : filters) {
                        LOGGER.info("Using filter {}...", filter.getClass().getName());
                        data = filter.filterData(data);
                        LOGGER.info("Filtering successful...");
                    }

                    // Then, execute the metric collection
                    List<BaseMetricCollector> collectors = collection.getMetricCollectors(config);
                    for (BaseMetricCollector collector : collectors) {
                        LOGGER.info("Using collector {}...", collector.getClass().getName());
                        resultSet.addResults(collector.collectData(data));
                        LOGGER.info("Collection successful...");
                    }
                } catch(LoaderException | FilterException | MetricCollectorException e) {
                    LOGGER.catching(e);
                }
            }

            // Load filer
            IFiler filer = config.getFiler();
            LOGGER.info("Using filer {}...", filer.getClass().getName());
            filer.storeResults(config, resultSet);
            LOGGER.info("Storing of data successful.");

            System.exit(0);

        } catch (InvocationTargetException | NoSuchMethodException | ClassNotFoundException | IllegalAccessException
            | InstantiationException | IOException e) {
            LOGGER.catching(e);
            System.exit(1);
        }
    }

    private static GeneralConfiguration loadConfiguration(String configLocation) throws IOException {
        // Load config
        byte[] data  = Files.readAllBytes(Paths.get(configLocation));
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(data, GeneralConfiguration.class);
    }

    private static void setupLogging(GeneralConfiguration configuration) {
        // Configure logging
        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        final Configuration config = ctx.getConfiguration();
        PatternLayout layout = PatternLayout.newBuilder().withPattern("%d [%t] %-5p - %msg%n").build();
        Appender appender = FileAppender.newBuilder().withFileName(configuration.getLogFile())
                .withName("File").withLayout(layout).build();
        appender.start();
        config.addAppender(appender);
        config.getRootLogger().addAppender(appender, Level.getLevel(configuration.getLogLevel()), null);
        ctx.updateLoggers();
    }
}
