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
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.synapse.endpoints.AddressEndpoint;
import org.apache.synapse.endpoints.FailoverEndpoint;
import org.apache.synapse.endpoints.LoadbalanceEndpoint;
import org.apache.synapse.endpoints.WSDLEndpoint;
import org.apache.synapse.mediators.builtin.CallMediator;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;
import java.util.List;
import java.util.Properties;

public class CallMediatorSerializationTest extends AbstractTestCase {

    private CallMediatorFactory factory = null;
    private CallMediatorSerializer serializer = null;

    public CallMediatorSerializationTest() {
        factory = new CallMediatorFactory();
        serializer = new CallMediatorSerializer();
    }

    public void testAddressEndpointSerialization() {

        String callConfig = "<call xmlns=\"http://ws.apache.org/ns/synapse\">" +
                     "<endpoint>" +
                          "<address uri='http://localhost:9000/services/MyService1'>" +
                               "<enableAddressing/>" +
                                "<timeout>" +
                                    "<duration>60</duration>" +
                                    "<responseAction>discard</responseAction>" +
                                "</timeout>" +
                          "</address>" +
                     "</endpoint>" +
                "</call>";

        OMElement config1 = createOMElement(callConfig);
        CallMediator call1 = (CallMediator) factory.createMediator(config1, new Properties());

        OMElement config2 = serializer.serializeMediator(null, call1);
        CallMediator call2 = (CallMediator) factory.createMediator(config2, new Properties());

        assertTrue("Top level endpoint should be a address endpoint.",
                call1.getEndpoint() instanceof AddressEndpoint);
        AddressEndpoint ep1 = (AddressEndpoint) call1.getEndpoint();

        assertTrue("Top level endpoint should be a WSDL endpoint.",
                call2.getEndpoint() instanceof AddressEndpoint);
        AddressEndpoint ep2 = (AddressEndpoint) call2.getEndpoint();

        assertEquals("Address URI is not serialized properly",
                ep1.getDefinition().getAddress(), ep2.getDefinition().getAddress());

        assertEquals(
                "Addressing information is not serialized properly",
                ep1.getDefinition().isAddressingOn(),
                ep2.getDefinition().isAddressingOn());
    }

    public void testWSDLEndpointSerialization() {

        String callConfig = "<call xmlns=\"http://ws.apache.org/ns/synapse\">" +
                     "<endpoint>" +
                          "<wsdl uri='file:src/test/resources/esbservice.wsdl' service='esbservice' port='esbserviceSOAP11port_http'>" +
                               "<enableAddressing/>" +
                          "</wsdl>" +
                     "</endpoint>" +
                "</call>";

        OMElement config1 = createOMElement(callConfig);
        CallMediator call1 = (CallMediator) factory.createMediator(config1, new Properties());

        OMElement config2 = serializer.serializeMediator(null, call1);
        CallMediator call2 = (CallMediator) factory.createMediator(config2, new Properties());

        assertTrue("Top level endpoint should be a WSDL endpoint.",
                call1.getEndpoint() instanceof WSDLEndpoint);
        WSDLEndpoint ep1 = (WSDLEndpoint) call1.getEndpoint();

        assertTrue("Top level endpoint should be a WSDL endpoint.",
                call2.getEndpoint() instanceof WSDLEndpoint);
        WSDLEndpoint ep2 = (WSDLEndpoint) call2.getEndpoint();

        assertEquals("Service name is not serialized properly.",
                ep1.getServiceName(), ep2.getServiceName());

        assertEquals("Port name is not serialized properly", ep1.getPortName(), ep2.getPortName());

        assertEquals("WSDL URI is not serialized properly", ep1.getWsdlURI(), ep2.getWsdlURI());

        assertEquals(
                "Addressing information is not serialized properly",
                ep1.getDefinition().isAddressingOn(),
                ep2.getDefinition().isAddressingOn());
    }

    public void testSimpleLoadbalanceCallSerialization() {

        String callConfig = "<call xmlns=\"http://ws.apache.org/ns/synapse\">" +
                "<endpoint>" +
                    "<loadbalance>" +
                        "<endpoint>" +
                            "<address uri=\"http://localhost:9001/services/Service1\">" +
                                "<enableAddressing/>" +
                            "</address>" +
                        "</endpoint>" +
                        "<endpoint>" +
                            "<address uri=\"http://localhost:9002/services/Service1\">" +
                                "<enableAddressing/>" +
                            "</address>" +
                        "</endpoint>" +
                        "<endpoint>" +
                            "<address uri=\"http://localhost:9003/services/Service1\">" +
                                "<enableAddressing/>" +
                            "</address>" +
                        "</endpoint>" +
                    "</loadbalance>" +
                "</endpoint>" +
                "</call>";

        OMElement config1 = createOMElement(callConfig);
        CallMediator call1 = (CallMediator) factory.createMediator(config1, new Properties());

        OMElement config2 = serializer.serializeMediator(null, call1);
        CallMediator call2 = (CallMediator) factory.createMediator(config2, new Properties());

        assertTrue("Top level endpoint should be a load balance endpoint.",
                call2.getEndpoint() instanceof LoadbalanceEndpoint);

        LoadbalanceEndpoint endpoint = (LoadbalanceEndpoint) call2.getEndpoint();
        List addresses = endpoint.getChildren();
        assertEquals("There should be 3 leaf level address endpoints", addresses.size(), 3);

        assertTrue("Leaf level endpoints should be address endpoints",
                addresses.get(0) instanceof AddressEndpoint);
        assertTrue("Leaf level endpoints should be address endpoints",
                addresses.get(1) instanceof AddressEndpoint);
        assertTrue("Leaf level endpoints should be address endpoints",
                addresses.get(2) instanceof AddressEndpoint);

        AddressEndpoint addressEndpoint = (AddressEndpoint) addresses.get(0);
        assertTrue("URI of address endpoint is not serialized properly",
                "http://localhost:9001/services/Service1".equals(addressEndpoint.getDefinition().getAddress()));
    }

    public void testSimpleFailoverCallSerialization() {

        String callConfig = "<call xmlns=\"http://ws.apache.org/ns/synapse\">" +
                "<endpoint>" +
                    "<failover>" +
                        "<endpoint>" +
                            "<address uri=\"http://localhost:9001/services/Service1\">" +
                                "<enableAddressing/>" +
                            "</address>" +
                        "</endpoint>" +
                        "<endpoint>" +
                            "<address uri=\"http://localhost:9002/services/Service1\">" +
                                "<enableAddressing/>" +
                            "</address>" +
                        "</endpoint>" +
                        "<endpoint>" +
                            "<address uri=\"http://localhost:9003/services/Service1\">" +
                                "<enableAddressing/>" +
                            "</address>" +
                        "</endpoint>" +
                    "</failover>" +
                "</endpoint>" +
                "</call>";

        OMElement config1 = createOMElement(callConfig);
        CallMediator call1 = (CallMediator) factory.createMediator(config1, new Properties());

        OMElement config2 = serializer.serializeMediator(null, call1);
        CallMediator call2 = (CallMediator) factory.createMediator(config2, new Properties());

        assertTrue("Top level endpoint should be a failover endpoint.",
                call2.getEndpoint() instanceof FailoverEndpoint);

        FailoverEndpoint endpoint = (FailoverEndpoint) call2.getEndpoint();
        List addresses = endpoint.getChildren();
        assertEquals("There should be 3 leaf level address endpoints", addresses.size(), 3);

        assertTrue("Leaf level endpoints should be address endpoints",
                addresses.get(0) instanceof AddressEndpoint);
        assertTrue("Leaf level endpoints should be address endpoints",
                addresses.get(1) instanceof AddressEndpoint);
        assertTrue("Leaf level endpoints should be address endpoints",
                addresses.get(2) instanceof AddressEndpoint);

        AddressEndpoint addressEndpoint = (AddressEndpoint) addresses.get(0);
        assertTrue("URI of address endpoint is not serialized properly",
                "http://localhost:9001/services/Service1".equals(addressEndpoint.getDefinition().getAddress()));
    }

    public void testNestedLoadbalanceFailoverCallSerialization() {

        String callConfig = "<call xmlns=\"http://ws.apache.org/ns/synapse\">" +
                "<endpoint>" +
                    "<loadbalance>" +
                        "<endpoint>" +
                            "<address uri=\"http://localhost:9001/services/Service1\">" +
                                "<enableAddressing/>" +
                            "</address>" +
                        "</endpoint>" +
                        "<endpoint>" +
                            "<failover>" +
                                "<endpoint>" +
                                    "<address uri=\"http://localhost:9002/services/Service1\">" +
                                        "<enableAddressing/>" +
                                    "</address>" +
                                "</endpoint>" +
                                "<endpoint>" +
                                    "<address uri=\"http://localhost:9003/services/Service1\">" +
                                        "<enableAddressing/>" +
                                    "</address>" +
                                "</endpoint>" +
                            "</failover>" +
                        "</endpoint>" +
                    "</loadbalance>" +
                "</endpoint>" +
                "</call>";

        OMElement config1 = createOMElement(callConfig);
        CallMediator call1 = (CallMediator) factory.createMediator(config1, new Properties());

        OMElement config2 = serializer.serializeMediator(null, call1);
        CallMediator call2 = (CallMediator) factory.createMediator(config2, new Properties());

        assertTrue("Top level endpoint should be a load balance endpoint.",
                call2.getEndpoint() instanceof LoadbalanceEndpoint);

        LoadbalanceEndpoint loadbalanceEndpoint = (LoadbalanceEndpoint) call2.getEndpoint();

        List children = loadbalanceEndpoint.getChildren();
        assertEquals("Top level endpoint should have 2 child endpoints.", children.size(), 2);

        assertTrue("First child should be a address endpoint",
                children.get(0) instanceof AddressEndpoint);

        assertTrue("Second child should be a fail over endpoint",
                children.get(1) instanceof FailoverEndpoint);

        FailoverEndpoint failoverEndpoint = (FailoverEndpoint) children.get(1);
        List children2 = failoverEndpoint.getChildren();

        assertEquals("Fail over endpoint should have 2 children.", children2.size(), 2);
        assertTrue("Children of the fail over endpoint should be address endpoints.",
                children2.get(0) instanceof AddressEndpoint);
        assertTrue("Children of the fail over endpoint should be address endpoints.",
                children2.get(1) instanceof AddressEndpoint);
    }

    protected OMElement createOMElement(String xml) {
        try {

            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(xml));
            StAXOMBuilder builder = new StAXOMBuilder(reader);
            OMElement omElement = builder.getDocumentElement();
            return omElement;

        }
        catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

}
