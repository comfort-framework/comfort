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

import de.ugoe.cs.seppelshark.BaseTest;
import de.ugoe.cs.seppelshark.configuration.GeneralConfiguration;
import de.ugoe.cs.seppelshark.configuration.LoaderConfiguration;
import de.ugoe.cs.seppelshark.data.graphs.CallEdge;
import de.ugoe.cs.seppelshark.data.graphs.CallGraph;
import de.ugoe.cs.seppelshark.data.graphs.CallType;
import de.ugoe.cs.seppelshark.exception.LoaderException;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

/**
 * @author Fabian Trautsch
 */
public class CallGraphLoaderTest extends BaseTest{
    private GeneralConfiguration javaConfiguration = new GeneralConfiguration();
    private GeneralConfiguration pythonConfiguration = new GeneralConfiguration();

    private LoaderConfiguration loaderJavaConfiguration = new LoaderConfiguration("CallGraph");
    private LoaderConfiguration loaderPythonConfiguration = new LoaderConfiguration("CallGraph");

    private CallGraphLoader callGraphLoader;

    @Before
    public void createExecutionConfigurationForJava() {
        javaConfiguration.setProjectDir(getPathToResource("loaderTestData/callgraph/javaproject"));
    }

    @Before
    public void createExecutionConfigurationForPython() {
        pythonConfiguration.setProjectDir(getPathToResource("loaderTestData/callgraph/pythonproject"));
        pythonConfiguration.setLanguage("python");

        loaderPythonConfiguration.setCallGraphLocation(getPathToResource("loaderTestData/callgraph/pythonproject/.callgraph"));

    }

    @Test
    public void loadCheckDataTypeTest() {
        try {
            callGraphLoader = new CallGraphLoader(javaConfiguration, loaderJavaConfiguration);
            CallGraph callGraph = callGraphLoader.loadJavaCallGraph();
            assertTrue(callGraph != null);
        } catch(LoaderException e) {
            fail("Error in reading project directory at: " + javaConfiguration.getProjectDir());
        }

    }

    @Test
    public void loadJavaCallGraphTest() {
        // TODO: Make several tests with different use cases instead of one big one (e.g., separate class files into folders to check

        CallGraph network = new CallGraph();
        network.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, entryControllerInit, javaLangObjectInit));

        network.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, iControllerInit, personInit));

        network.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, addressInit, javaLangObjectInit));

        network.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, addressInitWithParam, javaLangObjectInit));

        network.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, personInit, javaLangObjectInit));
        network.addEdge(new CallEdge(CallType.INVOKE_STATIC, 1, personInit, javaLangIntegerValueOf));
        network.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 2, personInit, addressInitWithParam));

        network.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, telephoneBookInit, addressInit));

        network.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, entryViewInit, javaLangObjectInit));
        network.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 1, entryViewInit, javaLangExceptionInit));

        network.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, personViewInit, javaLangObjectInit));
        network.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 1, personViewInit, personInit));
        network.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 2, personViewInit, entryControllerInit));

        network.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, mainInit, javaLangObjectInit));

        network.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, blubTestInit, javaLangObjectInit));

        network.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, testControllerInit, javaLangObjectInit));

        network.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, personTestInit, javaLangObjectInit));

        network.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, addressTestInit, javaLangObjectInit));

        network.addEdge(new CallEdge(CallType.INVOKE_STATIC, 0, addressTestGetAddressTest , javaLangIntegerValueOf));
        network.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 1, addressTestGetAddressTest , addressInitWithParam));
        network.addEdge(new CallEdge(CallType.INVOKE_VIRTUAL, 2, addressTestGetAddressTest , addressGetStreet));
        network.addEdge(new CallEdge(CallType.INVOKE_STATIC, 3, addressTestGetAddressTest , junitAssertEquals));

        network.addEdge(new CallEdge(CallType.INVOKE_VIRTUAL, 0, addressTestGetAddressTest2 , addressTestGetAddressTest));
        network.addEdge(new CallEdge(CallType.INVOKE_STATIC, 1, addressTestGetAddressTest2 , javaLangIntegerValueOf));
        network.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 2, addressTestGetAddressTest2 , addressInitWithParam));
        network.addEdge(new CallEdge(CallType.INVOKE_VIRTUAL, 3, addressTestGetAddressTest2 , addressGetStreet));
        network.addEdge(new CallEdge(CallType.INVOKE_STATIC, 4, addressTestGetAddressTest2 , junitAssertEquals));


        network.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, blatestblaInit, javaLangObjectInit));

        network.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, testEntryViewInit, javaLangObjectInit));

        network.addEdge(new CallEdge(CallType.INVOKE_SPECIAL, 0, fooTestInit, javaLangObjectInit));


        try {
            callGraphLoader = new CallGraphLoader(javaConfiguration, loaderJavaConfiguration);
            CallGraph callGraph = callGraphLoader.loadJavaCallGraph();
            assertEquals("Nodes are not equal", network.nodes(), callGraph.nodes());
            assertEquals("Networks are not equal!", network, callGraph);
        } catch (LoaderException e) {
            fail("Exception occured!: "+e.getMessage());
        }

    }

    @Test
    public void loadPythonCallGraphTest() {
        try {
            callGraphLoader = new CallGraphLoader(pythonConfiguration, loaderPythonConfiguration);
            CallGraph callGraph = callGraphLoader.loadPythonCallGraph();

            CallGraph network = new CallGraph();
            network.addEdge(new CallEdge(CallType.INVOKE_PYTHON, 0, testCallDemo, callDemo));
            network.addEdge(new CallEdge(CallType.INVOKE_PYTHON, 0, callDemo, demoInit));
            network.addEdge(new CallEdge(CallType.INVOKE_PYTHON, 1, callDemo, demoBar));
            network.addEdge(new CallEdge(CallType.INVOKE_PYTHON, 0, demoBar, demoFoo));

            assertEquals("Networks not equal!", network, callGraph);
        } catch (LoaderException e) {
            fail("Exception occured!: "+e.getMessage());
        }
    }

    @Test(expected = LoaderException.class)
    public void loadTestWithNonExistingDirectory() throws LoaderException{
        Path emptyDirPath = Paths.get(javaConfiguration.getProjectDir().toString(), "NonExisting");
        javaConfiguration.setProjectDir(emptyDirPath.toString());

        callGraphLoader = new CallGraphLoader(javaConfiguration, loaderJavaConfiguration);
        callGraphLoader.loadJavaCallGraph();
    }
}
