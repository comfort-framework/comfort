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

import com.google.common.base.MoreObjects;
import com.google.common.collect.Multiset;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

/**
 * @author Fabian Trautsch
 */
public class ChangeSet extends DataSet {
    private Map<Path, Multiset<Path>> changeMap;

    public ChangeSet(Map<Path, Multiset<Path>> changeMap) {
        this.changeMap = changeMap;
    }

    public Map<Path, Multiset<Path>> getChangeMap() {
        return changeMap;
    }

    public void setChangeMap(Map<Path, Multiset<Path>> changeMap) {
        this.changeMap = changeMap;
    }

    public Multiset<Path> getChangedWithFiles(Path filePath) {
        return changeMap.get(filePath);
    }

    public Set<Path> getTestFiles() {
        return changeMap.keySet();
    }

    public String toString() {
        return MoreObjects.toStringHelper(this).add("changeMap", changeMap).toString();
    }
}
