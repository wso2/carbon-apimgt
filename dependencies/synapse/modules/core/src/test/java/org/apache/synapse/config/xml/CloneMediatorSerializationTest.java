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
 * Factory and Serializer tests for the CloneMediator
 */
public class CloneMediatorSerializationTest extends AbstractTestCase {

    CloneMediatorFactory cloneMediatorFactory;
    CloneMediatorSerializer cloneMediatorSerializer;

    public CloneMediatorSerializationTest() {
        super(CloneMediatorSerializationTest.class.getName());
        cloneMediatorFactory = new CloneMediatorFactory();
        cloneMediatorSerializer = new CloneMediatorSerializer();
    }

    public void testCloneMediatorSerializationScenarioOne() throws Exception {
        String inputXml = "<clone xmlns=\"http://ws.apache.org/ns/synapse\" " +
            "continueParent=\"true\"><target sequence=\"sequenceRef1\" " +
            "endpoint=\"endpointRef1\"/><target sequence=\"sequenceRef2\" " +
            "endpoint=\"endpointRef2\"/></clone> ";
        assertTrue(serialization(inputXml, cloneMediatorFactory, cloneMediatorSerializer));
        assertTrue(serialization(inputXml, cloneMediatorSerializer));
    }

    public void testCloneMediatorSerializationScenarioTwo() throws Exception {
        String inputXml = "<clone xmlns=\"http://ws.apache.org/ns/synapse\">" +
            "<target endpoint=\"endpointRef1\"><sequence><log/></sequence></target>" +
            "<target sequence=\"sequenceRef2\"><endpoint><address uri=\"http://testURL\"/>" +
            "</endpoint></target></clone> ";
        assertTrue(serialization(inputXml, cloneMediatorFactory, cloneMediatorSerializer));
        assertTrue(serialization(inputXml, cloneMediatorSerializer));
    }

    public void testCloneMediatorSerializationScenarioThree() throws Exception {
        String inputXml = "<clone xmlns=\"http://ws.apache.org/ns/synapse\">" +
            "<target><sequence><send/></sequence><endpoint><address uri=\"http://testURL2\"/>" +
            "</endpoint></target><target sequence=\"sequenceRef2\" " +
            "endpoint=\"endpointRef2\"/></clone> ";
        assertTrue(serialization(inputXml, cloneMediatorFactory, cloneMediatorSerializer));
        assertTrue(serialization(inputXml, cloneMediatorSerializer));
    }

    public void testCloneMediatorSerializationScenarioFour() throws Exception {
        String inputXml = "<clone xmlns=\"http://ws.apache.org/ns/synapse\" " +
            "continueParent=\"true\"><target to=\"http://localhost:7777\"><sequence><send/>" +
            "</sequence><endpoint><address uri=\"http://testURL2\"/></endpoint></target>" +
            "<target soapAction=\"urn:test\" sequence=\"sequenceRef2\" " +
            "endpoint=\"endpointRef2\"/></clone> ";
        assertTrue(serialization(inputXml, cloneMediatorFactory, cloneMediatorSerializer));
        assertTrue(serialization(inputXml, cloneMediatorSerializer));
    }
}
