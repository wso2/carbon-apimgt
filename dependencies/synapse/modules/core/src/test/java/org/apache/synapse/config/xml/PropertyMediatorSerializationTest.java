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

/**
 *
 *
 */

public class PropertyMediatorSerializationTest extends AbstractTestCase {
    PropertyMediatorFactory propertyMediatorFactory;
    PropertyMediatorSerializer propertyMediatorSerializer;

    public PropertyMediatorSerializationTest() {
        super(AbstractTestCase.class.getName());
        propertyMediatorFactory = new PropertyMediatorFactory();
        propertyMediatorSerializer = new PropertyMediatorSerializer();
    }

    public void testPropertyMediatorSerializationSenarioOne() throws Exception {
        String inputXml = "<property xmlns=\"http://ws.apache.org/ns/synapse\" name=\"To\" value=\"http://127.0.0.1:10001/services/Services\" action=\"remove\" />";
        assertTrue(serialization(inputXml, propertyMediatorFactory, propertyMediatorSerializer));
        assertTrue(serialization(inputXml, propertyMediatorSerializer));
    }

    public void testPropertyMediatorSerializationSenarioTwo() throws Exception {
        String inputXml = "<property xmlns=\"http://ws.apache.org/ns/synapse\" expression=\"child::*\" name=\"To\" />";
        assertTrue(serialization(inputXml, propertyMediatorFactory, propertyMediatorSerializer));
        assertTrue(serialization(inputXml, propertyMediatorSerializer));
    }

    public void testPropertyMediatorSerializationSenarioThree() throws Exception {
        String inputXml = "<property xmlns=\"http://ws.apache.org/ns/synapse\" expression=\"child::*\" name=\"To\" action=\"remove\"/>";
        assertTrue(serialization(inputXml, propertyMediatorFactory, propertyMediatorSerializer));
        assertTrue(serialization(inputXml, propertyMediatorSerializer));
    }

    public void testPropertyMediatorSerializationSenarioFour() throws Exception {
        String inputXml = "<property xmlns=\"http://ws.apache.org/ns/synapse\"  name=\"To\" action=\"remove\"/>";
        assertTrue(serialization(inputXml, propertyMediatorFactory, propertyMediatorSerializer));
        assertTrue(serialization(inputXml, propertyMediatorSerializer));
    }

    public void testPropertyMediatorSerializationScenarioFive() throws Exception {
        String inputXml = "<property xmlns=\"http://ws.apache.org/ns/synapse\"  name=\"DoubleProperty\" type=\"DOUBLE\" value=\"123.456\"/>";
        assertTrue(serialization(inputXml, propertyMediatorFactory, propertyMediatorSerializer));
        assertTrue(serialization(inputXml, propertyMediatorSerializer));
    }

    public void testPropertyMediatorSerializationScenarioSix() throws Exception {
        String inputXml = "<property xmlns=\"http://ws.apache.org/ns/synapse\"  name=\"OMProperty\"><name>Synapse</name></property>";
        assertTrue(serialization(inputXml, propertyMediatorFactory, propertyMediatorSerializer));
        assertTrue(serialization(inputXml, propertyMediatorSerializer));
    }

    public void testPropertyMediatorSerializationScenarioSeven() throws Exception {
        String inputXml = "<property xmlns=\"http://ws.apache.org/ns/synapse\"  name=\"To\" value=\"myValue\" pattern=\".*\" group=\"1\"/>";
        assertTrue(serialization(inputXml, propertyMediatorFactory, propertyMediatorSerializer));
        assertTrue(serialization(inputXml, propertyMediatorSerializer));
    }

    public void testPropertyMediatorSerializationScenarioEight() throws Exception {
        String inputXml = "<property xmlns=\"http://ws.apache.org/ns/synapse\"  name=\"To\" expression=\"get-property('To')\" pattern=\".*\" group=\"1\"/>";
        assertTrue(serialization(inputXml, propertyMediatorFactory, propertyMediatorSerializer));
        assertTrue(serialization(inputXml, propertyMediatorSerializer));
    }
}
