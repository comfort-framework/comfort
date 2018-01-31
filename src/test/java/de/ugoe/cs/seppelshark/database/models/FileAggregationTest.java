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

package de.ugoe.cs.seppelshark.database.models;

import org.bson.types.ObjectId;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Fabian Trautsch
 */
public class FileAggregationTest {

    @Test
    public void getAndSetFileId() {
        FileAggregation fA = new FileAggregation();

        ObjectId fileId = new ObjectId();
        fA.setFileId(fileId);
        assertEquals(fileId, fA.getFileId());
    }

    @Test
    public void getAndSetCommitIds() {
        FileAggregation fA = new FileAggregation();

        ObjectId c1 = new ObjectId();
        ObjectId c2 = new ObjectId();
        ObjectId c3 = new ObjectId();

        List<ObjectId> commit_ids = new ArrayList<>();
        commit_ids.add(c1);
        commit_ids.add(c2);
        commit_ids.add(c3);

        fA.setCommitIds(commit_ids);
        assertEquals(commit_ids, fA.getCommitIds());
    }
}
