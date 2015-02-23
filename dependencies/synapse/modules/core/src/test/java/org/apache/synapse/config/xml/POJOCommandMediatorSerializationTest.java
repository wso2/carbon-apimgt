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
 * This Testcase will test the Factory and Serializer of the POJOCommandMediator
 */
public class POJOCommandMediatorSerializationTest extends AbstractTestCase {

    POJOCommandMediatorFactory pojoCommandMediatorFactory;
    POJOCommandMediatorSerializer pojoCommandMediatorSerializer;

    public POJOCommandMediatorSerializationTest() {
        super(POJOCommandMediatorSerializationTest.class.getName());
        pojoCommandMediatorFactory = new POJOCommandMediatorFactory();
        pojoCommandMediatorSerializer = new POJOCommandMediatorSerializer();
    }

    public void testPOJOCommandNotImplementedMediatorWithoutPropertySerialization() throws Exception {
        String inputXml = "<pojoCommand xmlns=\"http://ws.apache.org/ns/synapse\" " +
                "name=\"org.apache.synapse.mediators.ext.POJOCommandTestMediator\"/> ";
        assertTrue(serialization(inputXml, pojoCommandMediatorFactory, pojoCommandMediatorSerializer));
        assertTrue(serialization(inputXml, pojoCommandMediatorSerializer));
    }

    public void testPOJOCommandMediatorImplementedWithoutPropertySerialization() throws Exception {
        String inputXml = "<pojoCommand xmlns=\"http://ws.apache.org/ns/synapse\" " +
                "name=\"org.apache.synapse.mediators.ext.POJOCommandTestImplementedMediator\"/> ";
        assertTrue(serialization(inputXml, pojoCommandMediatorFactory, pojoCommandMediatorSerializer));
        assertTrue(serialization(inputXml, pojoCommandMediatorSerializer));
    }

    public void testPOJOCommandNotImplementedMediatorWithPropertySerialization() throws Exception {
        String inputXml = "<pojoCommand xmlns=\"http://ws.apache.org/ns/synapse\" " +
                "name=\"org.apache.synapse.mediators.ext.POJOCommandTestMediator\">" +
                "<property name=\"testProp\" expression=\"fn:concat('XPATH ', 'FUNC')\" action=\"ReadMessage\"/></pojoCommand>";
        assertTrue(serialization(inputXml, pojoCommandMediatorFactory, pojoCommandMediatorSerializer));
        assertTrue(serialization(inputXml, pojoCommandMediatorSerializer));
    }

    public void testPOJOCommandMediatorImplementedWithPropertySerialization() throws Exception {
        String inputXml = "<pojoCommand xmlns=\"http://ws.apache.org/ns/synapse\" " +
                "name=\"org.apache.synapse.mediators.ext.POJOCommandTestImplementedMediator\">" +
                "<property name=\"testProp\" expression=\"fn:concat('XPATH ', 'FUNC')\" action=\"UpdateMessage\"/></pojoCommand>";
        assertTrue(serialization(inputXml, pojoCommandMediatorFactory, pojoCommandMediatorSerializer));
        assertTrue(serialization(inputXml, pojoCommandMediatorSerializer));
    }

    public void testPOJOCommandMediatorWithStaticPropertySerialization() throws Exception {
        String inputXml = "<pojoCommand xmlns=\"http://ws.apache.org/ns/synapse\" " +
                "name=\"org.apache.synapse.mediators.ext.POJOCommandTestMediator\">" +
                "<property name=\"testProp\" value=\"Test Property\"/></pojoCommand>";
        assertTrue(serialization(inputXml, pojoCommandMediatorFactory, pojoCommandMediatorSerializer));
        assertTrue(serialization(inputXml, pojoCommandMediatorSerializer));
    }

    public void testPOJOCommandMediatorWithMessagePropertySerialization() throws Exception {
        String inputXml = "<pojoCommand xmlns=\"http://ws.apache.org/ns/synapse\" " +
                "name=\"org.apache.synapse.mediators.ext.POJOCommandTestMediator\">" +
                "<property name=\"testProp\" expression=\"fn:concat('XPATH ', 'FUNC')\" action=\"ReadAndUpdateMessage\"/></pojoCommand>";
        assertTrue(serialization(inputXml, pojoCommandMediatorFactory, pojoCommandMediatorSerializer));
        assertTrue(serialization(inputXml, pojoCommandMediatorSerializer));
    }

    public void testPOJOCommandMediatorWithContextPropertySerialization() throws Exception {
        String inputXml = "<pojoCommand xmlns=\"http://ws.apache.org/ns/synapse\" " +
                "name=\"org.apache.synapse.mediators.ext.POJOCommandTestMediator\">" +
                "<property name=\"testProp\" value=\"Test Property\" context-name=\"prop\"/></pojoCommand>";
        assertTrue(serialization(inputXml, pojoCommandMediatorFactory, pojoCommandMediatorSerializer));
        assertTrue(serialization(inputXml, pojoCommandMediatorSerializer));
    }

    public void testPOJOCommandMediatorWithContextMessagePropertySerialization() throws Exception {
        String inputXml = "<pojoCommand xmlns=\"http://ws.apache.org/ns/synapse\" " +
                "name=\"org.apache.synapse.mediators.ext.POJOCommandTestMediator\">" +
                "<property name=\"testProp\" expression=\"fn:concat('XPATH ', 'FUNC')\" context-name=\"prop\" action=\"ReadMessage\"/></pojoCommand>";
        assertTrue(serialization(inputXml, pojoCommandMediatorFactory, pojoCommandMediatorSerializer));
        assertTrue(serialization(inputXml, pojoCommandMediatorSerializer));
    }

    public void testPOJOCommandMediatorWithMessage$ContextPropertySerialization() throws Exception {
        String inputXml = "<pojoCommand xmlns=\"http://ws.apache.org/ns/synapse\" " +
                "name=\"org.apache.synapse.mediators.ext.POJOCommandTestMediator\">" +
                "<property name=\"testProp\" expression=\"fn:concat('XPATH ', 'FUNC')\" context-name=\"prop\" action=\"ReadContext\"/></pojoCommand>";
        assertTrue(serialization(inputXml, pojoCommandMediatorFactory, pojoCommandMediatorSerializer));
        assertTrue(serialization(inputXml, pojoCommandMediatorSerializer));
    }

    public void testPOJOCommandMediatorWithContextUpdatePropertySerialization() throws Exception {
        String inputXml = "<pojoCommand xmlns=\"http://ws.apache.org/ns/synapse\" " +
                "name=\"org.apache.synapse.mediators.ext.POJOCommandTestMediator\">" +
                "<property name=\"testProp\" context-name=\"prop\" action=\"UpdateContext\"/></pojoCommand>";
        assertTrue(serialization(inputXml, pojoCommandMediatorFactory, pojoCommandMediatorSerializer));
        assertTrue(serialization(inputXml, pojoCommandMediatorSerializer));
    }

    public void testPOJOCommandMediatorWithContextR$UPropertySerialization() throws Exception {
        String inputXml = "<pojoCommand xmlns=\"http://ws.apache.org/ns/synapse\" " +
                "name=\"org.apache.synapse.mediators.ext.POJOCommandTestMediator\">" +
                "<property name=\"testProp\" context-name=\"prop\" action=\"ReadAndUpdateContext\"/></pojoCommand>";
        assertTrue(serialization(inputXml, pojoCommandMediatorFactory, pojoCommandMediatorSerializer));
        assertTrue(serialization(inputXml, pojoCommandMediatorSerializer));
    }
}
