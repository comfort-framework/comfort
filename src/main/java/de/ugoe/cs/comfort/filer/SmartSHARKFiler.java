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
import de.ugoe.cs.comfort.collection.metriccollector.mutation.MutationLocation;
import de.ugoe.cs.comfort.configuration.Database;
import de.ugoe.cs.comfort.configuration.FilerConfiguration;
import de.ugoe.cs.comfort.configuration.GeneralConfiguration;
import de.ugoe.cs.comfort.filer.models.Mutation;
import de.ugoe.cs.comfort.filer.models.Result;
import de.ugoe.cs.smartshark.model.Commit;
import de.ugoe.cs.smartshark.model.File;
import de.ugoe.cs.smartshark.model.MutationResult;
import de.ugoe.cs.smartshark.model.TestState;
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
import org.bson.types.ObjectId;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.logging.MorphiaLoggerFactory;

/**
 * @author Fabian Trautsch
 */
public class SmartSHARKFiler extends BaseFiler {
    private final Morphia morphia = new Morphia();
    private Datastore datastore;
    private ObjectId commitId;
    private Map<Path, ObjectId> files = new HashMap<>();
    private Map<MutationLocation, ObjectId> storedMutations = new HashMap<>();
    private Map<MutationLocation, String> mutationClassification = new HashMap<>();

    // For testing purpose
    public SmartSHARKFiler(GeneralConfiguration generalConfiguration, FilerConfiguration filerConfiguration,
                           ObjectId commitId, Map<Path, ObjectId> files) {
        super(generalConfiguration, filerConfiguration);

        // Connect to database
        datastore = connectToDatabase(filerConfiguration.getDatabase());
        this.commitId = commitId;
        this.files = files;
    }

    public SmartSHARKFiler(GeneralConfiguration generalConfiguration, FilerConfiguration filerConfiguration)
            throws IOException {
        super(generalConfiguration, filerConfiguration);

        // Connect to database
        datastore = connectToDatabase(filerConfiguration.getDatabase());

        Repository repo = getRepository();

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
        commitId = datastore.createQuery(Commit.class)
                .field("vcs_system_id").equal(vcsSystemId)
                .field("revision_hash").equal(repo.resolve(Constants.HEAD).getName())
                .get().getId();

        // Get all files for this vcs system in a map (Map<String path, ObjectId oid>)
        datastore.createQuery(File.class)
                .field("vcs_system_id").equal(vcsSystemId)
                .asList().forEach(
                    file -> files.put(Paths.get(file.getPath()), file.getId())
            );

        datastore.createQuery(de.ugoe.cs.smartshark.model.Mutation.class)
                .asList().forEach(
                        mutation ->  {
                            MutationLocation ml = new MutationLocation(mutation.getMType(), mutation.getLocation(),
                                    mutation.getLineNumber());
                            storedMutations.put(ml, mutation.getId());
                            mutationClassification.put(ml, mutation.getClassification());
                        }

            );
    }


    @Override
    public synchronized void storeResults(Set<Result> results) {
        resultSet.addResults(results);

        storeResultsInSmartSHARKDatabase();
    }

    @Override
    public synchronized void storeResult(Result result) {
        if(result == null) {
            return;
        }
        // Merge with other results
        resultSet.addResult(result);

        storeResultsInSmartSHARKDatabase();
    }

    private void storeResultsInSmartSHARKDatabase() {
        // Go through all results -> create a test state and store it
        for(Result result: resultSet.getResults()) {
            ObjectId fileId = files.get(result.getPathToFile());

            // Create mutation results
            Set<MutationResult> mutationResults = createMutationResults(result);

            TestState testState = new TestState(result.getId(), result.getMetrics(), fileId, commitId, mutationResults);
            storeTestState(testState);
        }

    }

    public Set<String> getTestStateWithMutationResults() {
        Set<String> testStatesWithMutationResults = new HashSet<>();

        datastore.createQuery(TestState.class)
                .field("commit_id").equal(commitId)
                .field("mutation_res").exists()
                .field("mutation_res").notEqual(null)
                .project("name", true)
                .forEach(
                        testState ->  {
                            testStatesWithMutationResults.add(testState.getName());
                        }
            );


        return testStatesWithMutationResults;
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
        MutationLocation mutationLocation = new MutationLocation(mutation.getMType(),
                mutation.getLocation(), mutation.getLineNumber());
        ObjectId mutationId = storedMutations.getOrDefault(mutationLocation, null);

        if(mutationId == null) {
            de.ugoe.cs.smartshark.model.Mutation dbMutation = new de.ugoe.cs.smartshark.model.Mutation(
                    mutation.getLocation(), mutation.getMType(), mutation.getLineNumber(), mutation.getClassification()
            );
            datastore.save(dbMutation);
            storedMutations.put(mutationLocation, dbMutation.getId());
            mutationId = dbMutation.getId();
        }

        return mutationId;
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

    private Repository getRepository() throws IOException {
        return new RepositoryBuilder()
                .readEnvironment()
                .findGitDir(generalConf.getProjectDir().toFile())
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

    public Map<MutationLocation, String> getMutationsAndClassification() {
        return mutationClassification;
    }
}
