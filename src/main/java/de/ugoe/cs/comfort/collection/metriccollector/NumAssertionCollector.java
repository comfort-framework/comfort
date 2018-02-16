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

import de.ugoe.cs.comfort.annotations.SupportsClass;
import de.ugoe.cs.comfort.annotations.SupportsJava;
import de.ugoe.cs.comfort.annotations.SupportsMethod;
import de.ugoe.cs.comfort.annotations.SupportsPython;
import de.ugoe.cs.comfort.collection.metriccollector.parsing.MetricClassVisitor;
import de.ugoe.cs.comfort.configuration.GeneralConfiguration;
import de.ugoe.cs.comfort.data.ClassFiles;
import de.ugoe.cs.comfort.data.graphs.CallEdge;
import de.ugoe.cs.comfort.data.graphs.CallGraph;
import de.ugoe.cs.comfort.data.models.IUnit;
import de.ugoe.cs.comfort.exception.MetricCollectorException;
import de.ugoe.cs.comfort.filer.BaseFiler;
import de.ugoe.cs.comfort.filer.models.Result;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.bcel.classfile.ClassParser;

/**
 * @author Fabian Trautsch
 */
public class NumAssertionCollector extends BaseMetricCollector {
    public NumAssertionCollector(GeneralConfiguration configuration, BaseFiler filer) {
        super(configuration, filer);
    }

    @SupportsMethod
    @SupportsClass
    @SupportsJava
    public void getNumberOfAssertionsForJava(ClassFiles classFiles) throws MetricCollectorException {
        ClassParser cp;
        Set<Path> testClassFiles = classFiles.getTestFiles();

        try {
            Set<Result> results = new HashSet<>();
            for(Path classFilePath: testClassFiles) {
                cp = new ClassParser(classFilePath.toString());
                MetricClassVisitor visitor = new MetricClassVisitor(cp.parse(), results, generalConf,
                        generalConf.getMethodLevel(), classFiles);
                visitor.start();
            }
            filer.storeResults(results);
        } catch (IOException e) {
            throw new MetricCollectorException("Error in executing NumAssertionCollector: "+e.getMessage());
        }
    }

    @SupportsMethod
    @SupportsJava
    @SupportsPython
    public void getNumberOfAssertionsForCallGraph(CallGraph callGraph) throws IOException {
        Map<String, Result> allResults = new HashMap<>();
        for(IUnit testNode : callGraph.getTestNodes()) {

            Result result = allResults.getOrDefault(testNode.getFQN(),
                    new Result(testNode.getFQN(), testNode.getFilePath(), "num_asserts", "0"));
            Integer numAsserts = Integer.parseInt(result.getMetric("num_asserts"));

            for(CallEdge directlyConnectedEdge : callGraph.outEdges(testNode)) {
                if(directlyConnectedEdge.getCallee().getFQN().contains("assert")) {
                    numAsserts++;
                }
            }
            result.addMetric("num_asserts", String.valueOf(numAsserts));
            allResults.put(testNode.getFQN(), result);
        }

        Set<Result> results = new HashSet<>();
        results.addAll(allResults.values());
        filer.storeResults(results);
    }

    @SupportsClass
    @SupportsJava
    @SupportsPython
    public void getNumberOfAssertionsForCallGraphOnClassLevel(CallGraph callGraph) throws IOException {
        Map<String, Result> allResults = new HashMap<>();
        for(IUnit testNode : callGraph.getTestNodes()) {

            Result result = allResults.getOrDefault(testNode.getFQNOfUnit(),
                    new Result(testNode.getFQNOfUnit(), testNode.getFilePath(), "num_asserts", "0"));
            Integer numAsserts = Integer.parseInt(result.getMetric("num_asserts"));

            for(CallEdge directlyConnectedEdge : callGraph.outEdges(testNode)) {
                if(directlyConnectedEdge.getCallee().getFQN().contains("assert")) {
                    numAsserts++;
                }
            }

            result.addMetric("num_asserts", String.valueOf(numAsserts));
            allResults.put(testNode.getFQNOfUnit(), result);
        }

        Set<Result> results = new HashSet<>();
        results.addAll(allResults.values());
        filer.storeResults(results);
    }

}
