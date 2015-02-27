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

public class XSLTMediatorSerializationTest extends AbstractTestCase {

    private XSLTMediatorFactory xsltMediatorFactory;
    private XSLTMediatorSerializer xsltMediatorSerializer;

    public XSLTMediatorSerializationTest() {
        xsltMediatorFactory = new XSLTMediatorFactory();
        xsltMediatorSerializer = new XSLTMediatorSerializer();
    }

    public void testRMSequenceSerializationTestScenarioOne() {
        String inputXml = "<xslt xmlns=\"http://ws.apache.org/ns/synapse\" key=\"xslt-key-req\"/>";
        assertTrue(serialization(inputXml, xsltMediatorFactory, xsltMediatorSerializer));
        assertTrue(serialization(inputXml, xsltMediatorSerializer));
    }

    public void testRMSequenceSerializationTestScenarioTwo() {
        String inputXml = "<xslt xmlns=\"http://ws.apache.org/ns/synapse\" " +
                          "key=\"xslt-key-req\" source=\"get-property('To')\">" +
                          "<property name=\"propName\" value=\"val\"/>" +
                          "<feature name=\"http://javax.xml.XMLConstants/feature/secure-processing\" value=\"true\" />" +
                          "</xslt>";
        assertTrue(serialization(inputXml, xsltMediatorFactory, xsltMediatorSerializer));
        assertTrue(serialization(inputXml, xsltMediatorSerializer));
    }

    public void testRMSequenceSerializationTestScenarioThree() {
        String inputXml = "<xslt xmlns=\"http://ws.apache.org/ns/synapse\" " +
                          "key=\"xslt-key-req\" source=\"get-property('To')\">" +
                          "<property name=\"propName0\" value=\"val\"/>" +
                          "<property name=\"propName1\" expression=\"get-property('To')\"/>" +
                          "<feature name=\"http://javax.xml.XMLConstants/feature/secure-processing\" value=\"false\" />" +
                          "</xslt>";
        assertTrue(serialization(inputXml, xsltMediatorFactory, xsltMediatorSerializer));
        assertTrue(serialization(inputXml, xsltMediatorSerializer));
    }
}