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
package de.ugoe.cs.seppelshark;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * @author Fabian Trautsch
 */
public class MainTest{
    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    @Test
    public void mainTestUsingDependencyGraph() {
        try {
            // Get test-config
            URL configResource = ClassLoader.getSystemClassLoader().getResource("test-configurations/test-config.json");
            assertNotNull("Configuration file test-config.json was not found in resources folder.", configResource);

            // If config was found execute main method
            String pathToConfigFile = Paths.get(configResource.toURI()).toFile().getAbsolutePath();

            exit.expectSystemExitWithStatus(0);
            Main.main(new String[]{pathToConfigFile});
        } catch (URISyntaxException e) {
            fail("Problem in reading configuration file.");
        }
    }

    @Test
    public void mainConfigNotAvailableTest() {
        exit.expectSystemExitWithStatus(1);
        Main.main(new String[]{"nothing"});
    }
}
