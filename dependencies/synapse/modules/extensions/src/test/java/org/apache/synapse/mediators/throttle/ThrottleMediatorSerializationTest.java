/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.synapse.mediators.throttle;

import org.apache.synapse.mediators.AbstractTestCase;


/**
 *
 *
 */

public class ThrottleMediatorSerializationTest extends AbstractTestCase {

    ThrottleMediatorFactory throttleMediatorFactory;
    ThrottleMediatorSerializer throttleMediatorSerializer;

    public ThrottleMediatorSerializationTest() {
        throttleMediatorFactory = new ThrottleMediatorFactory();
        throttleMediatorSerializer = new ThrottleMediatorSerializer();
    }

    public void testThrottleMediatorSerializationSenarioOne() throws Exception {
        String inputXml = "<throttle id=\"A\" xmlns=\"http://ws.apache.org/ns/synapse\" >" +
                "<policy key=\"thottleKey\"/></throttle>";
        assertTrue(serialization(inputXml, throttleMediatorFactory, throttleMediatorSerializer));
        assertTrue(serialization(inputXml, throttleMediatorSerializer));
    }
}
