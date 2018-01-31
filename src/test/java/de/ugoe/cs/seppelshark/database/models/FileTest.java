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
public class FileTest {

    @Test
    public void constructorWithAttributesTest() {
        try {
            ObjectId vcsSystemId = new ObjectId();
            File file = new File(vcsSystemId, "path");
            assertEquals(vcsSystemId, file.getVcsSystemId());
            assertEquals("path", file.getPath());
        } catch (Exception e) {
            fail("Exception thrown!");
        }
    }

    @Test
    public void constructorWithoutAttributesTest() {
        try {
            new File();
        } catch (Exception e) {
            fail("Exception thrown!");
        }
    }

    @Test
    public void getAndSetIdTest() {
        File file = new File();

        ObjectId randomId = new ObjectId();
        file.setId(randomId);
        assertEquals(randomId, file.getId());
    }

    @Test
    public void getAndSetPathTest() {
        File file = new File();
        file.setPath("path");
        assertEquals("path", file.getPath());
    }

    @Test
    public void getAndSetVCSSystemIdTest() {
        File file = new File();

        ObjectId randomId = new ObjectId();
        file.setVcsSystemId(randomId);
        assertEquals(randomId, file.getVcsSystemId());
    }
}
