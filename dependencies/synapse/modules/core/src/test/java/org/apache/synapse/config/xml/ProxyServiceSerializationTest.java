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
import org.apache.synapse.core.axis2.ProxyService;

import java.util.Properties;

/**
 *
 *
 */

public class ProxyServiceSerializationTest extends AbstractTestCase {


    public void testProxyServiceSerializationSenarioOne() throws Exception {
        String inputXml = "<proxy xmlns=\"http://ws.apache.org/ns/synapse\" name=\"name\" " +
            "startOnLoad=\"true\"  transports=\"http\"><description>description</description>" +
            "<target inSequence=\"inseqname\" outSequence=\"outseqname\" faultSequence=\"faultseqname\" />" +
            "<publishWSDL uri=\"http://uri\" ></publishWSDL><policy key=\"key\"/>" +
            "<parameter name=\"para\">text</parameter></proxy>";
        OMElement inputOM = createOMElement(inputXml);
        ProxyService proxy = ProxyServiceFactory.createProxy(inputOM, new Properties());
        OMElement resultOM = ProxyServiceSerializer.serializeProxy(null, proxy);
        assertTrue(compare(resultOM, inputOM));
    }

    public void testProxyServiceSerializationSenarioTwo() throws Exception {
        String inputXml = "<proxy xmlns=\"http://ws.apache.org/ns/synapse\" startOnLoad=\"true\" " +
            "name=\"name\"  transports=\"http\"><description>description</description>" +
            "<target endpoint=\"epr\" outSequence=\"out\"/><publishWSDL key=\"key\">" +
            "</publishWSDL><policy key=\"key\"/><parameter name=\"para\">text</parameter></proxy>";
        OMElement inputOM = createOMElement(inputXml);
        ProxyService proxy = ProxyServiceFactory.createProxy(inputOM, new Properties());
        OMElement resultOM = ProxyServiceSerializer.serializeProxy(null, proxy);
        assertTrue(compare(resultOM, inputOM));
    }

    public void testProxyServiceSerializationSenarioThree() throws Exception {
        String inputXml = "<proxy xmlns=\"http://ws.apache.org/ns/synapse\" " +
            "name=\"name\" startOnLoad=\"true\"  transports=\"http\"><description>" +
            "description</description><target><inSequence onError=\"ref\"><description>Test documentation</description><send/></inSequence>" +
            "<outSequence><send/></outSequence></target><publishWSDL  key=\"key\"></publishWSDL>" +
            "<policy key=\"key\"/><parameter name=\"para\">text</parameter></proxy>";
        OMElement inputOM = createOMElement(inputXml);
        ProxyService proxy = ProxyServiceFactory.createProxy(inputOM, new Properties());
        OMElement resultOM = ProxyServiceSerializer.serializeProxy(null, proxy);
        assertTrue(compare(resultOM, inputOM));
    }

    public void testProxyServiceSerializationSenarioFour() throws Exception {
        String inputXml = "<proxy xmlns=\"http://ws.apache.org/ns/synapse\" " +
            "name=\"name\" startOnLoad=\"true\"  transports=\"http\"><description>" +
            "description</description><target><inSequence onError=\"ref\"><send/></inSequence>" +
            "<outSequence><send/></outSequence></target><enableAddressing/><publishWSDL  key=\"key\"></publishWSDL>" +
            "<policy key=\"key\"/><parameter name=\"para\">text</parameter></proxy>";
        OMElement inputOM = createOMElement(inputXml);
        ProxyService proxy = ProxyServiceFactory.createProxy(inputOM, new Properties());
        OMElement resultOM = ProxyServiceSerializer.serializeProxy(null, proxy);
        assertTrue(compare(resultOM, inputOM));
    }

    public void testProxyServiceSerializationSenarioFive() throws Exception {
        String inputXml = "<proxy xmlns=\"http://ws.apache.org/ns/synapse\" startOnLoad=\"true\" " +
            "name=\"name\"  transports=\"http\"><description>description</description><target>" +
            "<endpoint><address uri=\"http://www.example.com/testepr\"/></endpoint><outSequence><send/>" +
            "</outSequence></target><publishWSDL uri=\"http://uri\"></publishWSDL><policy key=\"key\"/>" +
            "<parameter name=\"para\">text</parameter></proxy>";
        OMElement inputOM = createOMElement(inputXml);
        ProxyService proxy = ProxyServiceFactory.createProxy(inputOM, new Properties());
        OMElement resultOM = ProxyServiceSerializer.serializeProxy(null, proxy);
        assertTrue(compare(resultOM, inputOM));
    }

    public void testProxyServiceSerializationScenarioSix() throws Exception {
        String inputXml = "<proxy xmlns=\"http://ws.apache.org/ns/synapse\" startOnLoad=\"true\" " +
                "name=\"name\"  transports=\"http\"><description>description</description><target>" +
                "<endpoint><address uri=\"http://www.example.com/testepr\"/></endpoint><outSequence><send/>" +
                "</outSequence></target><publishWSDL endpoint=\"wsdl_ep\"></publishWSDL><policy key=\"key\"/>" +
                "<parameter name=\"para\">text</parameter></proxy>";
        OMElement inputOM = createOMElement(inputXml);
        ProxyService proxy = ProxyServiceFactory.createProxy(inputOM, new Properties());
        OMElement resultOM = ProxyServiceSerializer.serializeProxy(null, proxy);
        assertTrue(compare(resultOM, inputOM));
    }
//     public void testProxyServiceSerializationSenarioSix() throws Exception {
//        String inputXml = "<proxy xmlns=\"http://ws.apache.org/ns/synapse\" startOnLoad=\"true\" name=\"name\"  transports=\"http\"><description>description</description><target><endpoint address=\"http://www.example.com/testepr\" /></target><publish-wsdl uri=\"http://uri\" key=\"key\"></publish-wsdl><policy key=\"key\"/><parameter name=\"para\"><inline xmlns=\"http://customns\"><test/></inline></parameter></proxy>";
//        OMElement inputOM = createOMElement(inputXml);
//        ProxyService proxy = ProxyServiceFactory.createProxy(inputOM);
//        OMElement resultOM = ProxyServiceSerializer.serializeProxy(null, proxy);
//        assertTrue(comparator.compare(resultOM, inputOM));
//    }

    public void testProxyServiceSerializationWithResourceMap() throws Exception {
        String inputXml = "<proxy xmlns=\"http://ws.apache.org/ns/synapse\" name=\"name\" " +
            "startOnLoad=\"true\"><target><endpoint><address uri=\"http://www.example.com/testepr\"/>" +
            "</endpoint><outSequence><send/></outSequence></target><publishWSDL uri=\"http://uri\">" +
            "<resource location=\"test1.xsd\" key=\"test-key1\"/><resource location=\"test2.xsd\"" +
            " key=\"test-key2\"/></publishWSDL></proxy>";
        OMElement inputOM = createOMElement(inputXml);
        ProxyService proxy = ProxyServiceFactory.createProxy(inputOM, new Properties());
        assertNotNull(proxy.getResourceMap());
        OMElement resultOM = ProxyServiceSerializer.serializeProxy(null, proxy);
        assertTrue(compare(resultOM, inputOM));
    }
}
