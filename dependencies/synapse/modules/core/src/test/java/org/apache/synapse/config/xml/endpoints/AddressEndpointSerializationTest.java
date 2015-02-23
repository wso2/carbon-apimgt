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
import org.apache.synapse.endpoints.AddressEndpoint;

public class AddressEndpointSerializationTest extends AbstractTestCase {

    public void testAddressEndpointScenarioOne() throws Exception {
        String inputXML = "<endpoint  xmlns=\"http://ws.apache.org/ns/synapse\">" +
                "<address uri=\"http://localhost:9000/services/SimpleStockQuoteService\" />" +
                "</endpoint>" ;

        OMElement inputElement = createOMElement(inputXML);
        AddressEndpoint endpoint = (AddressEndpoint) AddressEndpointFactory.getEndpointFromElement(
                inputElement,true,null);

        OMElement serializedOut = AddressEndpointSerializer.getElementFromEndpoint(endpoint);
        assertTrue(compare(serializedOut,inputElement));

    }

    public void testAddressEndpointScenarioTwo() throws Exception {
        String inputXML =
                "<endpoint name=\"testEndpoint\" onError=\"foo\" xmlns=" +
                        "\"http://ws.apache.org/ns/synapse\">" +
                "<address uri=\"http://localhost:9000/services/SimpleStockQuoteService\" >" +
                "</address>"+
                "</endpoint>" ;

        OMElement inputElement = createOMElement(inputXML);
        AddressEndpoint endpoint = (AddressEndpoint) AddressEndpointFactory.getEndpointFromElement(
                inputElement,false,null);
        OMElement serializedOut = AddressEndpointSerializer.getElementFromEndpoint(endpoint);

        assertTrue(compare(serializedOut,inputElement));
    }

    public void testAddressEndpointScenarioThree() throws Exception {
        String inputXML = "<endpoint  xmlns=\"http://ws.apache.org/ns/synapse\">" +
                "<address uri=\"http://localhost:9000/services/SimpleStockQuoteService\" >" +
                "<markForSuspension>" +
                "<errorCodes>101507,101508</errorCodes>" +
                "<retriesBeforeSuspension>3</retriesBeforeSuspension>" +
                "<retryDelay>1000</retryDelay>" +
                "</markForSuspension>" +
                "<suspendOnFailure>" +
                "<errorCodes>101505,101506</errorCodes>" +
                "<initialDuration>5000</initialDuration>" +
                "<progressionFactor>2.0</progressionFactor>" +
                "<maximumDuration>60000</maximumDuration>" +
                "</suspendOnFailure>" +
                "<retryConfig>" +
                "<disabledErrorCodes>101501,101502</disabledErrorCodes>" +
                "</retryConfig>" +
                "</address>" +
                "</endpoint>" ;

        OMElement inputElement = createOMElement(inputXML);
        AddressEndpoint endpoint = (AddressEndpoint) AddressEndpointFactory.getEndpointFromElement(
                inputElement,true,null);

        OMElement serializedOut = AddressEndpointSerializer.getElementFromEndpoint(endpoint);
        assertTrue(compare(serializedOut,inputElement));

    }

    public void testAddressEndpointScenarioFour() throws Exception {
         String inputXML =
                "<endpoint xmlns=\"http://ws.apache.org/ns/synapse\">" +
                "<address uri=\"http://localhost:9000/services/SimpleStockQuoteService\" >" +
                "</address>"+
                "</endpoint>" ;

        OMElement inputElement = createOMElement(inputXML);
        AddressEndpoint endpoint = (AddressEndpoint) AddressEndpointFactory.getEndpointFromElement(
                inputElement, true, null);
        //assertNotNull(endpoint.getName()); // make sure we generate names for anonymous endpoints
        OMElement serializedOut = AddressEndpointSerializer.getElementFromEndpoint(endpoint);
        // the generated name should not show up in the serialization
        assertTrue(compare(serializedOut,inputElement));
    }

    public void testAddressEndpointScenarioDe() throws Exception {
        String inputXML = "<endpoint  xmlns=\"http://ws.apache.org/ns/synapse\">" +
                "<address uri=\"http://localhost:9000/services/SimpleStockQuoteService\" >" +
                "<markForSuspension>" +
                "<errorCodes>101507,101508</errorCodes>" +
                "<retriesBeforeSuspension>3</retriesBeforeSuspension>" +
                "<retryDelay>1000</retryDelay>" +
                "</markForSuspension>" +
                "<suspendOnFailure>" +
                "<errorCodes>101505,101506</errorCodes>" +
                "<initialDuration>5000</initialDuration>" +
                "<progressionFactor>2.0</progressionFactor>" +
                "<maximumDuration>60000</maximumDuration>" +
                "</suspendOnFailure>" +
                "<retryConfig>" +
                "<enabledErrorCodes>101501,101502</enabledErrorCodes>" +
                "</retryConfig>" +
                "</address>" +
                "</endpoint>" ;

        OMElement inputElement = createOMElement(inputXML);
        AddressEndpoint endpoint = (AddressEndpoint) AddressEndpointFactory.getEndpointFromElement(
                inputElement,true,null);

        OMElement serializedOut = AddressEndpointSerializer.getElementFromEndpoint(endpoint);
        assertTrue(compare(serializedOut,inputElement));

    }


}
