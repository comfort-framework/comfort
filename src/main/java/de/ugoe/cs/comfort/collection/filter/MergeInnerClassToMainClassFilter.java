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

import com.google.common.graph.Graph;
import de.ugoe.cs.comfort.annotations.SupportsJava;
import de.ugoe.cs.comfort.configuration.GeneralConfiguration;
import de.ugoe.cs.comfort.data.graphs.DependencyGraph;
import de.ugoe.cs.comfort.data.models.IUnit;
import de.ugoe.cs.comfort.data.models.JavaClass;
import java.util.Optional;

/**
 * @author Fabian Trautsch
 */
public class MergeInnerClassToMainClassFilter extends BaseFilter{

    public MergeInnerClassToMainClassFilter(GeneralConfiguration generalConfiguration) {
        super(generalConfiguration);
    }

    @SupportsJava
    public DependencyGraph filter(DependencyGraph dependencyGraph) {
        DependencyGraph filteredDependencyGraph = dependencyGraph.getCopyOfGraph();

        dependencyGraph.edges().forEach(
                edge -> {
                    JavaClass source = (JavaClass) edge.source();
                    JavaClass target = (JavaClass) edge.target();
                    if(source.isInnerClass() || target.isInnerClass()) {
                        if(source.isInnerClass() && !target.isInnerClass()) {
                            logger.debug("Source {} is an inner class.", source);
                            Optional<IUnit> baseClass = getBaseClassForInnerClass(dependencyGraph, source);
                            if(baseClass.isPresent()) {
                                filteredDependencyGraph.putEdge(baseClass.get(), target);
                                logger.debug("Putting edge between {} and {}", baseClass.get(), target);
                            }
                        } else if(!source.isInnerClass() && target.isInnerClass()) {
                            logger.debug("Target {} is an inner class.", target);
                            Optional<IUnit> baseClass = getBaseClassForInnerClass(dependencyGraph, target);
                            if(baseClass.isPresent()) {
                                filteredDependencyGraph.putEdge(source, baseClass.get());
                                logger.debug("Putting edge between {} and {}", source, baseClass.get());
                            }
                        } else if(source.isInnerClass() && target.isInnerClass()) {
                            logger.debug("Source {} and Target {} are inner classes.", source, target);
                            Optional<IUnit> baseClassSrc = getBaseClassForInnerClass(dependencyGraph, source);
                            Optional<IUnit> baseClassTarget = getBaseClassForInnerClass(dependencyGraph, target);
                            if(baseClassSrc.isPresent() && baseClassTarget.isPresent()) {
                                filteredDependencyGraph.putEdge(baseClassSrc.get(), baseClassTarget.get());
                                logger.debug("Putting edge between {} and {}", baseClassSrc.get(),
                                        baseClassTarget.get());
                            }
                        }
                        filteredDependencyGraph.removeEdge(source, target);
                        logger.debug("Removing edge between {} and {}", source, target);
                    }
                }
        );

        // Go through all nodes and check if they have any connections left. If not -> put them in a list to remove
        filteredDependencyGraph.cleanGraphOfNodesThatAreSingle();

        return filteredDependencyGraph;
    }

    private Optional<IUnit> getBaseClassForInnerClass(Graph<IUnit> graph, JavaClass node) {
        String baseClass = node.getFQNOfUnit().split("\\$")[0];
        logger.debug("Baseclass of {} is {}", node, baseClass);
        return graph.nodes().stream()
                .filter(graphNode -> graphNode.getFQNOfUnit().equals(baseClass))
                .findFirst();
    }
}
