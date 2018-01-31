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

import de.ugoe.cs.comfort.annotations.SupportsPython;
import de.ugoe.cs.comfort.configuration.GeneralConfiguration;
import de.ugoe.cs.comfort.data.graphs.DependencyGraph;
import de.ugoe.cs.comfort.data.models.IUnit;
import de.ugoe.cs.comfort.data.models.PythonModule;
import java.util.Set;

/**
 * @author Fabian Trautsch
 */
public class DeletePythonPackagesFilter extends BaseFilter{
    public DeletePythonPackagesFilter(GeneralConfiguration generalConfiguration) {
        super(generalConfiguration);
    }

    @SupportsPython
    public DependencyGraph filter(DependencyGraph dependencyGraph) {
        DependencyGraph filteredDependencyGraph = dependencyGraph.getCopyOfGraph();

        // If the source or the target is a package -> delete it
        Set<IUnit> nodes = dependencyGraph.nodes();
        nodes.forEach(node -> {
            PythonModule moduleNode = (PythonModule) node;
            if (moduleNode.getModule().equals("__init__")) {
                logger.debug("Deleting node {} (is Package)", moduleNode);
                filteredDependencyGraph.removeNode(node);
            }
        });

        filteredDependencyGraph.cleanGraphOfNodesThatAreSingle();

        return filteredDependencyGraph;
    }
}
