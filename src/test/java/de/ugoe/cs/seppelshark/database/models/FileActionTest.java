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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Fabian Trautsch
 */
public class FileActionTest {
    @Test
    public void constructorWithAttributesTest() {
        try {
            ObjectId fileId = new ObjectId();
            ObjectId commitId = new ObjectId();

            FileAction fileAction = new FileAction(fileId, commitId);
            assertEquals(fileId, fileAction.getFileId());
            assertEquals(commitId, fileAction.getCommitId());
        } catch (Exception e) {
            fail("Exception thrown!");
        }
    }

    @Test
    public void constructorWithoutAttributesTest() {
        try {
            new FileAction();
        } catch (Exception e) {
            fail("Exception thrown!");
        }
    }

    @Test
    public void getAndSetIdTest() {
        FileAction fileAction = new FileAction();

        ObjectId randomId = new ObjectId();
        fileAction.setId(randomId);
        assertEquals(randomId, fileAction.getId());
    }

    @Test
    public void getAndSetFileIdTest() {
        FileAction fileAction = new FileAction();

        ObjectId randomId = new ObjectId();
        fileAction.setFileId(randomId);
        assertEquals(randomId, fileAction.getFileId());
    }

    @Test
    public void getAndSetCommitIdTest() {
        FileAction fileAction = new FileAction();

        ObjectId randomId = new ObjectId();
        fileAction.setCommitId(randomId);
        assertEquals(randomId, fileAction.getCommitId());
    }

    @Test
    public void getAndSetOldFileIdTest() {
        FileAction fileAction = new FileAction();

        ObjectId randomId = new ObjectId();
        fileAction.setOldFileId(randomId);
        assertEquals(randomId, fileAction.getOldFileId());
    }

    @Test
    public void getAndSetModeTest() {
        FileAction fileAction = new FileAction();
        fileAction.setMode("M");
        assertEquals("M", fileAction.getMode());
    }

    @Test
    public void getAndSetSizeAtCommitTest() {
        FileAction fileAction = new FileAction();
        fileAction.setSizeAtCommit(10);
        assertEquals(Integer.valueOf(10), fileAction.getSizeAtCommit());
    }

    @Test
    public void getAndSetLinesAddedTest() {
        FileAction fileAction = new FileAction();
        fileAction.setLinesAdded(10);
        assertEquals(Integer.valueOf(10), fileAction.getLinesAdded());
    }

    @Test
    public void getAndSetLinesDeletedTest() {
        FileAction fileAction = new FileAction();
        fileAction.setLinesDeleted(10);
        assertEquals(Integer.valueOf(10), fileAction.getLinesDeleted());
    }

    @Test
    public void getAndSetisBinaryTest() {
        FileAction fileAction = new FileAction();
        fileAction.setIsBinary(true);
        assertEquals(true, fileAction.getIsBinary());
    }
}
