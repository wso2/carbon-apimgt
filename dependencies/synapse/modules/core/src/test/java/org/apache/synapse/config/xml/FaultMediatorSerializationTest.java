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

public class FaultMediatorSerializationTest extends AbstractTestCase {

    FaultMediatorFactory faultMediatorFactory;
    FaultMediatorSerializer faultMediatorSerializer;
    private static final String SOAP11 = "soap11";
    private static final String SOAP12 = "soap12";
    private static final String EMPTY = "";
    private final static String DETAILS = "<test>Test Details</test><test2>Test2 Details</test2>";

    public FaultMediatorSerializationTest() {
        super(FaultMediatorSerializationTest.class.getName());
        faultMediatorFactory = new FaultMediatorFactory();
        faultMediatorSerializer = new FaultMediatorSerializer();
    }

    public void testFaultMediatorSerializationSOAP11() throws Exception {
        String inputXml = getXmlOfMediatorForSOAP11(SOAP11, "ns2:Client", "reason", EMPTY, EMPTY);
        assertTrue(serialization(inputXml, faultMediatorFactory, faultMediatorSerializer));
        assertTrue(serialization(inputXml, faultMediatorSerializer));
    }

    public void testFaultMediatorSerializationSOAP11NonEmptyDetails() throws Exception {
        String inputXml = getXmlOfMediatorForSOAP11(SOAP11, "ns2:Client", "reason", EMPTY, DETAILS);
        assertTrue(serialization(inputXml, faultMediatorFactory, faultMediatorSerializer));
        assertTrue(serialization(inputXml, faultMediatorSerializer));
    }

    public void testFaultMediatorSerializationSOAP12() throws Exception {
        String inputXml = getXmlOfMediatorForSOAP12(SOAP12, "soap:Sender", "reason", EMPTY, EMPTY, EMPTY, "false");
        assertTrue(serialization(inputXml, faultMediatorFactory, faultMediatorSerializer));
        assertTrue(serialization(inputXml, faultMediatorSerializer));
    }

    public void testFaultMediatorSerializationSOAP12NonEmptyDetails() throws Exception {
        String inputXml = getXmlOfMediatorForSOAP12(SOAP12, "soap:Sender", "reason", EMPTY, EMPTY, DETAILS, "false");
        assertTrue(serialization(inputXml, faultMediatorFactory, faultMediatorSerializer));
        assertTrue(serialization(inputXml, faultMediatorSerializer));
    }

    public void testFaultMediatorSerializationSOAP12withResponse() throws Exception {
        String inputXml = getXmlOfMediatorForSOAP12(SOAP12, "soap:Sender", "reason", EMPTY, EMPTY, EMPTY, "true");
        assertTrue(serialization(inputXml, faultMediatorFactory, faultMediatorSerializer));
        assertTrue(serialization(inputXml, faultMediatorSerializer));
    }

    private String getXmlOfMediatorForSOAP11(String version, String attrOfCode, String attrOfReasion
            , String role, String details) throws Exception {
        return "<makefault  version=\"" + version + "\" xmlns=\"http://ws.apache.org/ns/synapse\"><code value=\"" + attrOfCode + "\" xmlns:ns2=\"http://ws.apache.org/ns/synapse\"/><reason value=\"" + attrOfReasion + "\"/>" +
                "<role>" + role + "</role><detail>" + details + "</detail></makefault>";

    }

    private String getXmlOfMediatorForSOAP12(String version, String attrOfCode, String attrOfReasion
            , String node, String role, String details, String response) throws Exception {
        return "<makefault xmlns=\"http://ws.apache.org/ns/synapse\" version=\"" + version + "\" response=\"" + response + "\"><code value=\"" + attrOfCode + "\" xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\"/><reason value=\"" + attrOfReasion + "\"/>" +
                "<node>" + node + "</node><role>" + role + "</role><detail>" + details + "</detail></makefault>";

    }

}
