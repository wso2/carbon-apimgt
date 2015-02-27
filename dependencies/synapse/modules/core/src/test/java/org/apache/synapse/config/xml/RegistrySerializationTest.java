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

package org.apache.synapse.config.xml;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.exception.XMLComparisonException;
import org.apache.synapse.registry.Registry;

import java.util.Properties;

public class RegistrySerializationTest extends AbstractTestCase {

    public RegistrySerializationTest() {
    }

    public void testRegistrySerialization() {

        String regitryConfiguration = "<syn:registry xmlns:syn=\"http://ws.apache.org/ns/synapse\" " +
                "provider=\"org.apache.synapse.registry.url.SimpleURLRegistry\">" +
                "<syn:parameter name=\"root\">file:./../../repository/</syn:parameter>" +
                "<syn:parameter name=\"cachableDuration\">15000</syn:parameter>" +
                "</syn:registry>";

        OMElement registryElement = createOMElement(regitryConfiguration);
        Registry registry = RegistryFactory.createRegistry(registryElement, new Properties());
        OMElement serializedElement = RegistrySerializer.serializeRegistry(null, registry);
        try {
            assertTrue(compare(registryElement, serializedElement));
        } catch (Exception e) {
            fail("Exception in test.");
        }
    }
}
