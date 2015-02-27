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

package org.apache.synapse.config.xml.endpoints;

import org.apache.axiom.om.OMElement;
import org.apache.synapse.config.xml.AbstractTestCase;
import org.apache.synapse.endpoints.Endpoint;

public class LoadBalanceEndpointSerializationTest extends AbstractTestCase {

    public void testLoadBalanceEndpointScenarioOne()throws Exception {
        String inputXml = "<endpoint xmlns=\"http://ws.apache.org/ns/synapse\">" +
                "<session type=\"simpleClientSession\"/>" +
                "<loadbalance algorithm=\"org.apache.synapse.endpoints.algorithms.RoundRobin\">" +
                "<endpoint>" +
                "<address uri=\"http://localhost:9001/soap/LBService1\">" +
                "<enableAddressing/>" +
                "</address>" +
                "</endpoint>" +
                "<endpoint>" +
                "<address uri=\"http://localhost:9002/soap/LBService1\">" +
                "<enableAddressing/>" +
                "</address>" +
                "</endpoint>" +
                "<endpoint>" +
                "<address uri=\"http://localhost:9003/soap/LBService1\">" +
                "<enableAddressing/>" +
                "</address>" +
                "</endpoint>" +
                "</loadbalance>" +
                "</endpoint>";

        OMElement inputElement = createOMElement(inputXml);
        Endpoint endpoint = LoadbalanceEndpointFactory.getEndpointFromElement(
                inputElement,true,null);
        OMElement serializedOut = LoadbalanceEndpointSerializer.getElementFromEndpoint(endpoint);

        assertTrue(compare(serializedOut,inputElement));
    }
}
