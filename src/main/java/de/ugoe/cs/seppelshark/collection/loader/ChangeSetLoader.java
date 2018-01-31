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

import static org.mongodb.morphia.aggregation.Group.grouping;
import static org.mongodb.morphia.aggregation.Group.push;

import com.github.danielfelgar.morphia.Log4JLoggerImplFactory;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import de.ugoe.cs.seppelshark.Utils;
import de.ugoe.cs.seppelshark.annotations.SupportsJava;
import de.ugoe.cs.seppelshark.annotations.SupportsPython;
import de.ugoe.cs.seppelshark.configuration.Database;
import de.ugoe.cs.seppelshark.configuration.GeneralConfiguration;
import de.ugoe.cs.seppelshark.configuration.LoaderConfiguration;
import de.ugoe.cs.seppelshark.data.ChangeSet;
import de.ugoe.cs.seppelshark.data.ProjectFiles;
import de.ugoe.cs.seppelshark.database.models.File;
import de.ugoe.cs.seppelshark.database.models.FileAction;
import de.ugoe.cs.seppelshark.database.models.FileAggregation;
import de.ugoe.cs.seppelshark.database.models.VCSSystem;
import de.ugoe.cs.seppelshark.exception.LoaderException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.logging.MorphiaLoggerFactory;
import org.mongodb.morphia.query.Query;


/**
 * @author Fabian Trautsch
 */
public class ChangeSetLoader extends BaseLoader {

    private final Morphia morphia = new Morphia();

    public ChangeSetLoader(GeneralConfiguration generalConfiguration, LoaderConfiguration loaderConfiguration) {
        super(generalConfiguration, loaderConfiguration);
    }

    @SupportsJava
    @SupportsPython
    public ChangeSet loadData() throws LoaderException{
        // Set up log4j logging
        MorphiaLoggerFactory.reset();
        MorphiaLoggerFactory.registerLogger(Log4JLoggerImplFactory.class);

        Database databaseConfiguration = loaderConf.getDatabase();
        System.out.println(databaseConfiguration);

        // Map models
        morphia.mapPackage("de.ugoe.cs.seppelshark.database.models");

        // Create database connection string
        String mongoDBConnectionString = Utils.createMongoDBConnectionString(
                databaseConfiguration.getUsername(),
                databaseConfiguration.getPassword(),
                databaseConfiguration.getHostname(),
                databaseConfiguration.getPort(),
                databaseConfiguration.getAuthenticationDatabase()
        );

        // Create database connection
        logger.debug("Connecting to database...");
        MongoClientURI connectionString = new MongoClientURI(mongoDBConnectionString);
        MongoClient mongoClient = new MongoClient(connectionString);
        final Datastore datastore = morphia.createDatastore(mongoClient, databaseConfiguration.getDatabase());
        datastore.ensureIndexes();



        // Get project files first, so that we already have a separation of the files
        ProjectFiles projectFiles;
        if (generalConf.getLanguage().equals("python")) {
            projectFiles = new ProjectFilesLoader(generalConf, null).loadPythonProjectFiles();
        } else {
            projectFiles = new ProjectFilesLoader(generalConf, null).loadJavaProjectFiles();
        }

        // Get the VCS so that we can use it for querying the files
        VCSSystem vcsSystem = datastore.createQuery(VCSSystem.class).field("url")
                .equal(loaderConf.getVcsSystemUrl()).get();

        // Get all code files from the database that belong to the files of the project
        Map<ObjectId, Path> fileIds = new HashMap<>();
        datastore.createQuery(File.class)
                .field("path").in(projectFiles.getCodeFilesWithoutProjectDirAsString())
                .field("vcs_system_id").equal(vcsSystem.getId())
                .project("id", true).project("path", true)
            .forEach(file -> fileIds.put(file.getId(), Paths.get(file.getPath())));
        logger.debug("Found the following code files in the database: {}", fileIds);

        // Get only test files from database that belong to the project
        Set<ObjectId> testFileIds = new HashSet<>();
        datastore.createQuery(File.class)
                .field("path").in(projectFiles.getTestFilesWithoutProjectDirAsString())
                .field("vcs_system_id").equal(vcsSystem.getId())
                .project("id", true)
            .forEach(file -> testFileIds.add(file.getId()));
        logger.debug("Found the following test file ids in the database: {}", testFileIds);



        // For each file, get the fileactions that touched these files (with aggregation it is faster)
        Query<FileAction> q = datastore.createQuery(FileAction.class).field("file_id").in(testFileIds);
        Iterator<FileAggregation> aggregate = datastore.createAggregation(FileAction.class)
                .match(q)
                .group("file_id", grouping("commit_ids", push("commit_id")))
                .aggregate(FileAggregation.class);


        Map<Path, Multiset<Path>> changeMap = new HashMap<>();
        while (aggregate.hasNext()) {
            FileAggregation fA = aggregate.next();
            Multiset<Path> changedTogetherWith = HashMultiset.create();

            for(ObjectId commitId : fA.getCommitIds()) {
                datastore.createQuery(FileAction.class).field("commit_id").equal(commitId)
                        .project("file_id", true).forEach(
                            fileAction -> changedTogetherWith.add(fileIds.get(fileAction.getFileId()))
                );
            }

            // Get test file as Path
            Path testFilePath = fileIds.get(fA.getFileId());

            // Remove all nulls and the test itself from the list. Nulls can appear, as we only look at code files but
            // of course there are also other files in the repository that can be changed together with tests
            changedTogetherWith.removeIf(Objects::isNull);
            changedTogetherWith.removeIf(testFilePath::equals);
            changeMap.put(testFilePath, changedTogetherWith);
            logger.debug("File {} changed together with: {}", testFilePath, changedTogetherWith);
        }

        ChangeSet changeSet = new ChangeSet(changeMap);
        logger.debug("Created the following ChangeSet: {}", changeSet);
        return changeSet;
    }
}
