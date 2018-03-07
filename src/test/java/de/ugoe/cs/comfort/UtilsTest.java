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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;

/**
 * @author Fabian Trautsch
 */
public class UtilsTest extends BaseTest {
    private final Path basePath = Paths.get(getPathToResource("utilsdata"));


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
            assertEquals(expectedPath, Utils.getPathForFullyQualifiedClassNameInSetOfPaths(javaFiles, fqn, null));
        } catch (IOException e) {
            fail("Unexpected exception!");
        }
    }

    @Test
    public void getPathForFullyQualifiedClassNameForSubClassesTest() {
        String fqn = "org.jfree.data.xy.WindDataItem";
        Path expectedPath = Paths.get("/org/jfree/data/xy/DefaultWindDataset.java");
        try {
            Set<Path> javaFiles = new HashSet<>();
            javaFiles.add(Paths.get("/org/jfree/data/xy/DefaultWindDataset.java"));
            javaFiles.add(Paths.get("/org/jfree/data/yy/DefaultWindDataset.java"));
            javaFiles.add(Paths.get("/org/jfree/data/DefaultWindDataset.java"));
            javaFiles.add(Paths.get("/org/jfree/DefaultWindDataset.java"));
            javaFiles.add(Paths.get("/org/DefaultWindDataset.java"));
            assertEquals(expectedPath, Utils.getPathForFullyQualifiedClassNameInSetOfPaths(javaFiles, fqn, basePath));
        } catch (IOException e) {
            fail("Unexpected exception!: "+e.getMessage());
        }
    }

    @Test
    public void getPathForFullyQualifiedClassNameForSubEnumsTest() {
        String fqn = "org.apache.commons.lang3.Traffic";
        Path expectedPath = Paths.get("/org/apache/commons/lang3/EnumUtilsTest.java");
        try {
            Set<Path> javaFiles = new HashSet<>();
            javaFiles.add(Paths.get("/org/apache/commons/lang3/EnumUtilsTest.java"));
            javaFiles.add(Paths.get("/org/apache/commons/EnumUtilsTest.java"));
            javaFiles.add(Paths.get("/org/apache/EnumUtilsTest.java"));
            javaFiles.add(Paths.get("/org/EnumUtilsTest.java"));
            assertEquals(expectedPath, Utils.getPathForFullyQualifiedClassNameInSetOfPaths(javaFiles, fqn, basePath));
        } catch (IOException e) {
            fail("Unexpected exception!");
        }
    }

    @Test(expected = FileNotFoundException.class)
    public void getPathForFullyQualifiedClassNameForSubEnumsNegativeTest() throws FileNotFoundException {
        String fqn = "org.apache.commons.lang3.Traffics";
        Path expectedPath = Paths.get("/org/apache/commons/lang3/EnumUtilsTest.java");
        Set<Path> javaFiles = new HashSet<>();
        assertEquals(expectedPath, Utils.getPathForFullyQualifiedClassNameInSetOfPaths(javaFiles, fqn, basePath));
    }

}
