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

package org.foo.models;

import org.junit.Test;
import static org.junit.Assert.assertEquals;


/**
 * @author Fabian Trautsch
 */
public class AddressTest {

    @Test
    public void getAddressTest() {
        Address address = new Address("blub", 42);
        assertEquals("blub", address.getStreet());
    }

    @Test
    public void getAddressTest2() {
        getAddressTest();
        Address address = new Address("hui", 52);
        assertEquals("hui", address.getStreet());
    }
}
