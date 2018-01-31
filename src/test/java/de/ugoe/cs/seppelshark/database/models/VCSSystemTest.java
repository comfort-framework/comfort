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

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Fabian Trautsch
 */
public class VCSSystemTest {

    @Test
    public void constructorWithAttributesTest() {
        try {
            ObjectId projectId = new ObjectId();
            Date randomDate = new Date();

            VCSSystem vcsSystem = new VCSSystem("url", projectId, "git", randomDate);
            assertEquals("url", vcsSystem.getUrl());
            assertEquals(projectId, vcsSystem.getProjectId());
            assertEquals("git", vcsSystem.getRepositoryType());
            assertEquals(randomDate, vcsSystem.getLastUpdated());
        } catch (Exception e) {
            fail("Exception thrown!");
        }
    }

    @Test
    public void constructorWithoutAttributesTest() {
        try {
            new VCSSystem();
        } catch (Exception e) {
            fail("Exception thrown!");
        }
    }

    @Test
    public void getAndSetIdTest() {
        VCSSystem vcsSystem = new VCSSystem();

        ObjectId randomId = new ObjectId();
        vcsSystem.setId(randomId);
        assertEquals(randomId, vcsSystem.getId());
    }

    @Test
    public void getAndSetURLTest() {
        VCSSystem vcsSystem = new VCSSystem();
        vcsSystem.setUrl("url");
        assertEquals("url", vcsSystem.getUrl());
    }

    @Test
    public void getAndSetProjectIdTest() {
        VCSSystem vcsSystem = new VCSSystem();

        ObjectId randomId = new ObjectId();
        vcsSystem.setProjectId(randomId);
        assertEquals(randomId, vcsSystem.getProjectId());
    }

    @Test
    public void getAndSetRepositoryTypeTest() {
        VCSSystem vcsSystem = new VCSSystem();
        vcsSystem.setRepositoryType("git");
        assertEquals("git", vcsSystem.getRepositoryType());
    }

    @Test
    public void getAndSetDateTest() {
        VCSSystem vcsSystem = new VCSSystem();

        Date randomDate = new Date();
        vcsSystem.setLastUpdated(randomDate);
        assertEquals(randomDate, vcsSystem.getLastUpdated());
    }

}
