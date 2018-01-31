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

package de.ugoe.cs.seppelshark.configuration;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Fabian Trautsch
 */
public class DatabaseTest {

    @Test
    public void constructorWithDefaultValuesTest() {
        Database db = new Database();
        assertEquals("localhost", db.getHostname());
        assertEquals(27017, db.getPort());
        assertEquals("smartshark", db.getDatabase());
        assertNull(db.getUsername());
        assertNull(db.getPassword());
        assertNull(db.getAuthenticationDatabase());
    }

    @Test
    public void getAndSetHostnameTest() {
        Database db = new Database();
        db.setHostname("new");
        assertEquals("new", db.getHostname());
    }

    @Test
    public void getAndSetUsernameTest() {
        Database db = new Database();
        db.setUsername("new");
        assertEquals("new", db.getUsername());
    }

    @Test
    public void getAndSetPasswordTest() {
        Database db = new Database();
        db.setPassword("new");
        assertEquals("new", db.getPassword());
    }

    @Test
    public void getAndSetDatabaseTest() {
        Database db = new Database();
        db.setDatabase("new");
        assertEquals("new", db.getDatabase());
    }

    @Test
    public void getAndSetAuthenticationDatabaseTest() {
        Database db = new Database();
        db.setAuthenticationDatabase("new");
        assertEquals("new", db.getAuthenticationDatabase());
    }

    @Test
    public void getAndSetPortTest() {
        Database db = new Database();
        db.setPort(1);
        assertEquals(1, db.getPort());
    }
}
