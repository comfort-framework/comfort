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

package de.ugoe.cs.comfort.data;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * @author Fabian Trautsch
 */
public class ChangeSetTest {
    private Map<Path, Multiset<Path>> changeMap;

    @Before
    public void setUpTestChangeMap() {
        changeMap = new HashMap<>();
        Multiset<Path> set1 = HashMultiset.create();
        set1.add(Paths.get("path1"));
        set1.add(Paths.get("path2"));
        set1.add(Paths.get("path1"));
        set1.add(Paths.get("path2"));
        set1.add(Paths.get("path3"));

        Multiset<Path> set2 = HashMultiset.create();
        set2.add(Paths.get("path2"));
        set2.add(Paths.get("path2"));
        set2.add(Paths.get("path1"));
        set2.add(Paths.get("path3"));
        set2.add(Paths.get("path5"));

        changeMap.put(Paths.get("file1"), set1);
        changeMap.put(Paths.get("file2"), set2);
    }

    @Test
    public void getAndSetChangeMapTest() {
         ChangeSet cs = new ChangeSet(null);
         cs.setChangeMap(changeMap);
         assertEquals(changeMap, cs.getChangeMap());
     }

    @Test
    public void getTestFilesTest() {
        ChangeSet cs = new ChangeSet(changeMap);
        Set<Path> testFiles = new HashSet<>();
        testFiles.add(Paths.get("file1"));
        testFiles.add(Paths.get("file2"));

        assertEquals(testFiles, cs.getTestFiles());
    }

    @Test
    public void getChangedWithFilesTest() {
        ChangeSet cs = new ChangeSet(changeMap);
        Multiset<Path> multiset = HashMultiset.create();
        multiset.add(Paths.get("path1"));
        multiset.add(Paths.get("path2"));
        multiset.add(Paths.get("path1"));
        multiset.add(Paths.get("path2"));
        multiset.add(Paths.get("path3"));

        assertEquals(multiset, cs.getChangedWithFiles(Paths.get("file1")));
    }


}
