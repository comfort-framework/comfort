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

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Fabian Trautsch
 */
public class UtilsTest extends BaseTest{

    @Test
    public void createMongoDBStringWithoutAuthentication() {
        String expected = "mongodb://localhost:27017";
        assertEquals("Wrong mongodb connection string", expected, Utils.createMongoDBConnectionString(null, null, "localhost", 27017, null));
    }

    @Test
    public void createMongoDBStringWithAuthenticationButNoAuthenticationDatabase() {
        String expected = "mongodb://admin:password@127.234.21.55:3473";
        assertEquals("Wrong mongodb connection string", expected, Utils.createMongoDBConnectionString("admin", "password", "127.234.21.55", 3473, null));
    }

    @Test
    public void createMongoDBStringWithoutAuthenticationButWithAuthenticationDatabase() {
        String expected = "mongodb://127.234.21.55:3473";
        assertEquals("Wrong mongodb connection string", expected, Utils.createMongoDBConnectionString(null, null, "127.234.21.55", 3473, "smartshark"));
    }

    @Test
    public void createMongoDBStringWitAuthenticationAndWithAuthenticationDatabase() {
        String expected = "mongodb://admin:password@127.234.21.55:3473/?authSource=smartshark";
        assertEquals("Wrong mongodb connection string", expected, Utils.createMongoDBConnectionString("admin", "password", "127.234.21.55", 3473, "smartshark"));
    }

    @Test
    public void getPathForFullyQualifiedClassNameTest() {
        String fqn = "org.foo.Bar";
        Path expectedPath = Paths.get("/root/src/main/java/org/foo/Bar.java");
        try {
            Set<Path> javaFiles = new HashSet<>();
            javaFiles.add(Paths.get("/root/src/main/java/org/foo/Bar.java"));
            javaFiles.add(Paths.get("/root/src/main/java/org/bar/Bar.java"));
            javaFiles.add(Paths.get("/root/src/main/java/org/foo/Bar1.java"));
            javaFiles.add(Paths.get("/root/src/main/java/Bar.java"));
            assertEquals(expectedPath, Utils.getPathForFullyQualifiedClassNameInSetOfPaths(javaFiles, fqn));
        } catch (IOException e) {
            fail("Unexpected exception!");
        }
    }

}
