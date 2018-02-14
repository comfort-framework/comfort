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

import com.github.danielfelgar.morphia.Log4JLoggerImplFactory;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import de.ugoe.cs.comfort.configuration.Database;
import de.ugoe.cs.comfort.configuration.GeneralConfiguration;
import de.ugoe.cs.comfort.database.models.MutationResult;
import de.ugoe.cs.comfort.database.models.TestState;
import de.ugoe.cs.comfort.filer.models.Mutation;
import de.ugoe.cs.comfort.filer.models.Result;
import de.ugoe.cs.comfort.filer.models.ResultSet;
import de.ugoe.cs.smartshark.model.Commit;
import de.ugoe.cs.smartshark.model.File;
import de.ugoe.cs.smartshark.model.VCSSystem;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.types.ObjectId;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.FindAndModifyOptions;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.logging.MorphiaLoggerFactory;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;

/**
 * @author Fabian Trautsch
 */
public class SmartSHARKFiler implements IFiler {
    private final Morphia morphia = new Morphia();
    private Logger logger = LogManager.getLogger(this.getClass().getName());
    private Datastore datastore;

    @Override
    public void storeResults(GeneralConfiguration configuration, ResultSet results) {
        // Connect to database
        datastore = connectToDatabase(configuration.getFilerConfiguration().getDatabase());


        try {
            Repository repo = getRepository(configuration.getProjectDir());

            // Get VCS System
            String vcsSystemUrl = repo.getConfig().getString("remote", "origin", "url");
            List<String> possibleVCSSystemValues = new ArrayList<String>(){{
                    add(vcsSystemUrl);
                    add(vcsSystemUrl+"/");
                }
            };
            ObjectId vcsSystemId = datastore.createQuery(VCSSystem.class)
                    .field("url").in(possibleVCSSystemValues).get().getId();

            // Get commit id via jgit
            ObjectId commitId = datastore.createQuery(Commit.class)
                    .field("vcs_system_id").equal(vcsSystemId)
                    .field("revision_hash").equal(repo.resolve(Constants.HEAD).getName())
                    .get().getId();

            // Get all files for this vcs system in a map (Map<String path, ObjectId oid>)
            Map<Path, ObjectId> files = new HashMap<>();
            datastore.createQuery(File.class)
                    .field("vcs_system_id").equal(vcsSystemId)
                    .asList().forEach(
                            file -> files.put(Paths.get(file.getPath()), file.getId())
                );

            // Go through all results -> create a test state and store it
            for(Result result: results.getResults()) {
                ObjectId fileId = files.get(result.getPathToFile());

                // Create mutation results
                Set<MutationResult> mutationResults = createMutationResults(result);

                TestState testState = new TestState(result, fileId, commitId, mutationResults);
                storeTestState(testState);
            }

        } catch (IOException e) {
            logger.catching(e);
        }
    }

    private Set<MutationResult> createMutationResults(Result result) {
        Set<MutationResult> mutationResults = new HashSet<>();
        for(Mutation mutation: result.getMutationResults()) {
            ObjectId mutationId = getOrCreateMutation(mutation);
            mutationResults.add(new MutationResult(mutationId, mutation.getResult()));
        }

        return mutationResults;
    }

    private ObjectId getOrCreateMutation(Mutation mutation) {
        de.ugoe.cs.comfort.database.models.Mutation dbMutation;
        dbMutation = datastore.createQuery(de.ugoe.cs.comfort.database.models.Mutation.class)
                .field("location").equal(mutation.getLocation())
                .field("m_type").equal(mutation.getMType())
                .field("l_num").equal(mutation.getLineNumber())
                .get();

        if (dbMutation == null) {
            dbMutation = new de.ugoe.cs.comfort.database.models.Mutation(
                    mutation.getLocation(), mutation.getMType(), mutation.getLineNumber(), mutation.getClassification()
            );
            datastore.save(dbMutation);
        }

        return dbMutation.getId();
    }

    private void storeTestState(TestState testState) {
        TestState dbTestState = datastore.createQuery(TestState.class)
                .field("name").equal(testState.getName())
                .field("commit_id").equal(testState.getCommitId())
                .get();

        if(dbTestState == null) {
            dbTestState = new TestState();
            dbTestState.setName(testState.getName());
            dbTestState.setCommitId(testState.getCommitId());
            dbTestState.setFileId(testState.getFileId());
        }

        dbTestState.getMetrics().putAll(testState.getMetrics());

        if(testState.getMutationResults().size() != 0) {
            dbTestState.getMutationResults().addAll(testState.getMutationResults());
        }

        datastore.save(dbTestState);
    }

    private Repository getRepository(Path projectDir) throws IOException {
        return new RepositoryBuilder()
                .readEnvironment()
                .findGitDir(projectDir.toFile())
                .build();
    }

    private Datastore connectToDatabase(Database databaseConfiguration) {
        // Set up log4j logging
        MorphiaLoggerFactory.reset();
        MorphiaLoggerFactory.registerLogger(Log4JLoggerImplFactory.class);

        // Map models
        morphia.mapPackage("de.ugoe.cs.smartshark.model");
        morphia.mapPackage("de.ugoe.cs.comfort.database.models");

        logger.debug("Connecting to database...");
        // Create database connection
        MongoClientURI uri = new MongoClientURI(de.ugoe.cs.smartshark.Utils.createMongoDBURI(
                databaseConfiguration.getUsername(),
                databaseConfiguration.getPassword(),
                databaseConfiguration.getHostname(),
                String.valueOf(databaseConfiguration.getPort()),
                databaseConfiguration.getAuthenticationDatabase(),
                databaseConfiguration.getSSL()));
        MongoClient mongoClient = new MongoClient(uri);
        final Datastore datastore = morphia.createDatastore(mongoClient, databaseConfiguration.getDatabase());
        datastore.ensureIndexes();

        return datastore;
    }
}
