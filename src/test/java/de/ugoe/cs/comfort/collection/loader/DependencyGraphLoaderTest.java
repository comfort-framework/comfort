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

package de.ugoe.cs.comfort.collection.loader;

import com.google.common.graph.EndpointPair;
import de.ugoe.cs.comfort.BaseTest;
import de.ugoe.cs.comfort.configuration.GeneralConfiguration;
import de.ugoe.cs.comfort.configuration.LoaderConfiguration;
import de.ugoe.cs.comfort.data.graphs.DependencyGraph;
import de.ugoe.cs.comfort.data.models.JavaClass;
import de.ugoe.cs.comfort.data.models.PythonModule;
import de.ugoe.cs.comfort.exception.LoaderException;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * @author Fabian Trautsch
 */
public class DependencyGraphLoaderTest extends BaseTest{

    private GeneralConfiguration javaConfiguration = new GeneralConfiguration();
    private GeneralConfiguration pythonConfiguration = new GeneralConfiguration();
    private LoaderConfiguration loaderConf = new LoaderConfiguration("DependecyGraph");
    private DependencyGraphLoader dependencyGraphLoader;

    @Before
    public void createExecutionConfigurationForJava() {
        javaConfiguration.setProjectDir(getPathToResource("loaderTestData/dependencygraph/javaproject"));
    }

    @Before
    public void createExecutionConfigurationForPython() {
        pythonConfiguration.setLanguage("python");
        pythonConfiguration.setProjectDir(getPathToResource("loaderTestData/dependencygraph/pythonproject"));

        // Data Generation

    }

    @Test
    public void loadTestPythonCheckNodes() {
        DependencyGraph dependencyGraph = new DependencyGraph();
        try {
            dependencyGraphLoader = new DependencyGraphLoader(pythonConfiguration, loaderConf);
            dependencyGraph = dependencyGraphLoader.loadPythonDependencyGraph();
        } catch (LoaderException e) {
            e.printStackTrace();
        }

        Set<PythonModule> expectedNodes = new HashSet<>();
        expectedNodes.add(pyTest1);
        expectedNodes.add(pyTest2);
        expectedNodes.add(pyTest3);
        expectedNodes.add(pyTest4);
        expectedNodes.add(pyTest5);
        expectedNodes.add(moduleInit);
        expectedNodes.add(package1Init);
        expectedNodes.add(module2);
        expectedNodes.add(subPackage1Init);
        expectedNodes.add(module1);
        expectedNodes.add(module3);
        assertEquals("Missing nodes...", expectedNodes, dependencyGraph.nodes());
    }

    @Test
    public void loadTestPythonCheckEdges() {
        DependencyGraph dependencyGraph = new DependencyGraph();
        try {
            dependencyGraphLoader = new DependencyGraphLoader(pythonConfiguration, loaderConf);
            dependencyGraph = dependencyGraphLoader.loadPythonDependencyGraph();
        } catch (LoaderException e) {
            e.printStackTrace();
        }

        Set<EndpointPair<PythonModule>> expectedEdges = new HashSet<>();
        expectedEdges.add(EndpointPair.ordered(pyTest1, moduleInit));
        expectedEdges.add(EndpointPair.ordered(pyTest1, package1Init));
        expectedEdges.add(EndpointPair.ordered(pyTest1, subPackage1Init));
        expectedEdges.add(EndpointPair.ordered(pyTest1, module1));
        expectedEdges.add(EndpointPair.ordered(pyTest1, module2));

        expectedEdges.add(EndpointPair.ordered(pyTest2, moduleInit));
        expectedEdges.add(EndpointPair.ordered(pyTest2, package1Init));
        expectedEdges.add(EndpointPair.ordered(pyTest2, module2));
        expectedEdges.add(EndpointPair.ordered(pyTest2, subPackage1Init));
        expectedEdges.add(EndpointPair.ordered(pyTest2, module1));

        expectedEdges.add(EndpointPair.ordered(pyTest3, moduleInit));
        expectedEdges.add(EndpointPair.ordered(pyTest3, package1Init));
        expectedEdges.add(EndpointPair.ordered(pyTest3, module2));

        expectedEdges.add(EndpointPair.ordered(pyTest4, moduleInit));
        expectedEdges.add(EndpointPair.ordered(pyTest4, package1Init));
        expectedEdges.add(EndpointPair.ordered(pyTest4, module2));
        expectedEdges.add(EndpointPair.ordered(pyTest4, subPackage1Init));

        expectedEdges.add(EndpointPair.ordered(pyTest5, moduleInit));
        expectedEdges.add(EndpointPair.ordered(pyTest5, package1Init));
        expectedEdges.add(EndpointPair.ordered(pyTest5, module2));
        expectedEdges.add(EndpointPair.ordered(pyTest5, subPackage1Init));
        expectedEdges.add(EndpointPair.ordered(pyTest5, module3));

        assertEquals("Missing edges..", expectedEdges, dependencyGraph.edges());
    }

    @Test
    public void loadCheckDataTypeTest() {
        try {
            dependencyGraphLoader = new DependencyGraphLoader(javaConfiguration, loaderConf);
            DependencyGraph dependencyGraph = dependencyGraphLoader.loadJavaDependencyGraph();
            assertTrue(dependencyGraph != null);
        } catch(LoaderException e) {
            fail("Error in reading project directory at: " + javaConfiguration.getProjectDir());
        }
    }

    @Test(expected = LoaderException.class)
    public void loadTestWithNotExistingDirectoryJava() throws LoaderException{
        Path emptyDirPath = Paths.get(javaConfiguration.getProjectDir().toString(), "notExistingDir");
        javaConfiguration.setProjectDir(emptyDirPath.toString());
        dependencyGraphLoader = new DependencyGraphLoader(javaConfiguration, loaderConf);
        dependencyGraphLoader.loadJavaDependencyGraph();
    }

    @Test(expected = LoaderException.class)
    public void loadTestWithNotExistingDirectoryPython() throws LoaderException{
        Path emptyDirPath = Paths.get(pythonConfiguration.getProjectDir().toString(), "notExistingDir");
        pythonConfiguration.setProjectDir(emptyDirPath.toString());
        dependencyGraphLoader = new DependencyGraphLoader(pythonConfiguration, loaderConf);
        dependencyGraphLoader.loadPythonDependencyGraph();
    }

    @Test
    public void loadCheckJavaNodesTest() {
        DependencyGraph dependencyGraph = new DependencyGraph();
        try {
            dependencyGraphLoader = new DependencyGraphLoader(javaConfiguration, loaderConf);
            dependencyGraph = dependencyGraphLoader.loadJavaDependencyGraph();
        } catch (LoaderException e) {
            fail("Exception in executing loader: "+e.getMessage());
        }
        Set<JavaClass> expectedNodes = new HashSet<>();
        expectedNodes.add(main);
        expectedNodes.add(entryController);
        expectedNodes.add(iController);
        expectedNodes.add(address);
        expectedNodes.add(person);
        expectedNodes.add(telephonebook);
        expectedNodes.add(entryView);
        expectedNodes.add(personView);
        expectedNodes.add(fooTest);
        expectedNodes.add(blubTest);
        expectedNodes.add(testController);
        expectedNodes.add(addressTest);
        expectedNodes.add(personTest);
        expectedNodes.add(blatestbla);
        expectedNodes.add(testEntryView);
        expectedNodes.add(object);
        expectedNodes.add(exception);
        expectedNodes.add(string);
        expectedNodes.add(integer);
        expectedNodes.add(jassert);
        expectedNodes.add(test);

        assertEquals("Not same size...", expectedNodes.size(), dependencyGraph.nodes().size());
        assertEquals("Missing nodes..", expectedNodes, dependencyGraph.nodes());
    }

    @Test
    public void loadCheckJavaEdgesTest() {
        DependencyGraph dependencyGraph = new DependencyGraph();
        try {
            dependencyGraphLoader = new DependencyGraphLoader(javaConfiguration, loaderConf);
            dependencyGraph = dependencyGraphLoader.loadJavaDependencyGraph();
        } catch (LoaderException e) {
            fail("Exception in executing loader: "+e.getMessage());
        }

        // 15 classes + java.lang.Object + java.lang.String + java.lang.Exception
        Set<EndpointPair<JavaClass>> expectedEdges = new HashSet<>();
        expectedEdges.add(EndpointPair.ordered(main, object));
        expectedEdges.add(EndpointPair.ordered(entryController, object));
        expectedEdges.add(EndpointPair.ordered(iController, object));
        expectedEdges.add(EndpointPair.ordered(address, object));
        expectedEdges.add(EndpointPair.ordered(person, object));
        expectedEdges.add(EndpointPair.ordered(entryView, object));
        expectedEdges.add(EndpointPair.ordered(main, object));
        expectedEdges.add(EndpointPair.ordered(personView, object));
        expectedEdges.add(EndpointPair.ordered(fooTest, object));
        expectedEdges.add(EndpointPair.ordered(blubTest, object));
        expectedEdges.add(EndpointPair.ordered(addressTest, object));
        expectedEdges.add(EndpointPair.ordered(personTest, object));
        expectedEdges.add(EndpointPair.ordered(blatestbla, object));
        expectedEdges.add(EndpointPair.ordered(testEntryView, object));
        expectedEdges.add(EndpointPair.ordered(testController, object));

        expectedEdges.add(EndpointPair.ordered(entryController, iController));
        expectedEdges.add(EndpointPair.ordered(iController, person));
        expectedEdges.add(EndpointPair.ordered(address, string));
        expectedEdges.add(EndpointPair.ordered(address, integer));
        expectedEdges.add(EndpointPair.ordered(person, string));
        expectedEdges.add(EndpointPair.ordered(person, integer));
        expectedEdges.add(EndpointPair.ordered(person, address));
        expectedEdges.add(EndpointPair.ordered(telephonebook, address));
        expectedEdges.add(EndpointPair.ordered(telephonebook, person));
        expectedEdges.add(EndpointPair.ordered(telephonebook, entryController));
        expectedEdges.add(EndpointPair.ordered(entryView, exception));
        expectedEdges.add(EndpointPair.ordered(entryView, string));
        expectedEdges.add(EndpointPair.ordered(personView, entryController));
        expectedEdges.add(EndpointPair.ordered(personView, person));
        expectedEdges.add(EndpointPair.ordered(addressTest, test));
        expectedEdges.add(EndpointPair.ordered(addressTest, string));
        expectedEdges.add(EndpointPair.ordered(addressTest, integer));
        expectedEdges.add(EndpointPair.ordered(addressTest, jassert));
        expectedEdges.add(EndpointPair.ordered(addressTest, address));
        assertEquals("Missing edges..", expectedEdges, dependencyGraph.edges());
    }

}
