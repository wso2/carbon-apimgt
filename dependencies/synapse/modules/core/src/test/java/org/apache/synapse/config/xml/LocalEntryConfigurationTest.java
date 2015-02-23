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
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.synapse.config.Entry;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;
import java.util.Properties;

public class LocalEntryConfigurationTest extends AbstractTestCase {

    private String key = "myEntry";

    public void testSimpleTextEntry() {
        String text = "Apache Synapse - 2.0";
        String entrySrc = "<localEntry xmlns=\"http://ws.apache.org/ns/synapse\" " +
                "key=\"" + key + "\">" + text + "</localEntry>";
        String serializedSrc = "<localEntry xmlns=\"http://ws.apache.org/ns/synapse\" " +
                "key=\"" + key + "\"><![CDATA[" + text + "]]></localEntry>";

        try {
            OMElement elem = parseXMLString(entrySrc, true);
            Entry entry = EntryFactory.createEntry(elem, new Properties());
            assertEquals(key, entry.getKey());
            assertEquals(Entry.INLINE_TEXT, entry.getType());
            assertEquals(text, (String) entry.getValue());

            OMElement serialization = EntrySerializer.serializeEntry(entry, null);
            OMElement expectedSerialization = parseXMLString(serializedSrc, false);
            assertTrue(compare(expectedSerialization, serialization));
        } catch (XMLStreamException e) {
            fail("Error while parsing entry definition: " + e.getMessage());
        }
    }

    public void testTextEntryWithMarkup() {
        textEntryWithMarkup(true);
        textEntryWithMarkup(false);
    }

    private void textEntryWithMarkup(boolean coalesced) {
        System.out.println("Testing text entry with markup characters; Coalesced " +
                "parsing: " + coalesced);
        String text = "mc.setPayloadXML(<xml>data</xml>);";
        String entrySrc = "<localEntry xmlns=\"http://ws.apache.org/ns/synapse\" " +
                "key=\"" + key + "\"><![CDATA[" + text + "]]></localEntry>";

        try {
            OMElement elem = parseXMLString(entrySrc, coalesced);
            Entry entry = EntryFactory.createEntry(elem, new Properties());
            assertEquals(key, entry.getKey());
            assertEquals(Entry.INLINE_TEXT, entry.getType());
            assertEquals(text, (String) entry.getValue());

            OMElement serialization = EntrySerializer.serializeEntry(entry, null);
            OMElement expectedSerialization = parseXMLString(entrySrc, false);
            assertTrue(compare(expectedSerialization, serialization));
        } catch (XMLStreamException e) {
            fail("Error while parsing entry definition: " + e.getMessage());
        }
    }

    public void testTextEntryWithNestedCDATA() {
        textEntryWithNestedCDATA(true);
        textEntryWithNestedCDATA(false);
    }

    private void textEntryWithNestedCDATA(boolean coalesced) {
        System.out.println("Testing text entry with nested CDATA elements; Coalesced " +
                "parsing: " + coalesced);

        String actualText = "mc.setPayloadXML(<xml><![CDATA[data]]></xml>);";
        String escapedText = "mc.setPayloadXML(<xml><![CDATA[data]]]]><![CDATA[></xml>);";
        String entrySrc = "<localEntry xmlns=\"http://ws.apache.org/ns/synapse\" " +
                "key=\"" + key + "\"><![CDATA[" + escapedText + "]]></localEntry>";

        try {
            OMElement elem = parseXMLString(entrySrc, coalesced);
            Entry entry = EntryFactory.createEntry(elem, new Properties());
            assertEquals(key, entry.getKey());
            assertEquals(Entry.INLINE_TEXT, entry.getType());
            assertEquals(actualText, (String) entry.getValue());

            OMElement expectedSerialization = parseXMLString(entrySrc, false);
            OMElement serialization = EntrySerializer.serializeEntry(entry, null);
            assertTrue(compare(expectedSerialization, serialization));

        } catch (XMLStreamException e) {
            fail("Error while parsing entry definition: " + e.getMessage());
        }
    }

    public void testLargeTextEntry() {
        String text = "Apache Synapse is designed to be a simple, lightweight and high performance " +
                "Enterprise Service Bus (ESB) from Apache. Based on a small asynchronous core, " +
                "Apache Synapse has excellent support for XML and Web services - as well as binary " +
                "and text formats. The Synapse engine is configured with a simple XML format and " +
                "comes with a set of ready-to-use transports and mediators. We recommend you start " +
                "by reading the QuickStart and then trying out the samples. Synapse is made " +
                "available under the Apache Software License 2.0. For more information please visit " +
                "http://synapse.apache.org.";

        String entrySrc = "<localEntry xmlns=\"http://ws.apache.org/ns/synapse\" " +
                "key=\"" + key + "\">" + text + "</localEntry>";
        String serializedSrc = "<localEntry xmlns=\"http://ws.apache.org/ns/synapse\" " +
                "key=\"" + key + "\"><![CDATA[" + text + "]]></localEntry>";

        try {
            OMElement elem = parseXMLString(entrySrc, true);
            Entry entry = EntryFactory.createEntry(elem, new Properties());
            assertEquals(key, entry.getKey());
            assertEquals(Entry.INLINE_TEXT, entry.getType());
            assertEquals(text, (String) entry.getValue());

            OMElement serialization = EntrySerializer.serializeEntry(entry, null);
            OMElement expectedSerialization = parseXMLString(serializedSrc, false);
            assertEquals(text, serialization.getText());
            serialization = parseXMLString(serialization.toString(), false);
            assertTrue(compare(expectedSerialization, serialization));
        } catch (XMLStreamException e) {
            fail("Error while parsing entry definition: " + e.getMessage());
        }       
    }

    public void testLargeTextEntryWithMarkup() {
        larseTextEntryWithMarkup(true);
        larseTextEntryWithMarkup(false);
    }

    private void larseTextEntryWithMarkup(boolean coalesced) {
        System.out.println("Testing large text entry with markup characters; Coalesced " +
                "parsing: " + coalesced);

        String text = "Apache Synapse is designed to be a simple, lightweight and high performance " +
                "Enterprise Service Bus (ESB) from Apache. Based on a small asynchronous core, " +
                "Apache Synapse has excellent support for <XML/> and Web services - as well as binary " +
                "and text formats. The Synapse engine is configured with a simple XML format and " +
                "comes with a set of ready-to-use transports and mediators. We recommend you start " +
                "by reading the QuickStart and then trying out the samples. Synapse is made " +
                "available under the Apache Software License 2.0. For more information please visit " +
                "http://synapse.apache.org.";

        String entrySrc = "<localEntry xmlns=\"http://ws.apache.org/ns/synapse\" " +
                "key=\"" + key + "\"><![CDATA[" + text + "]]></localEntry>";

        try {
            OMElement elem = parseXMLString(entrySrc, coalesced);
            Entry entry = EntryFactory.createEntry(elem, new Properties());
            assertEquals(key, entry.getKey());
            assertEquals(Entry.INLINE_TEXT, entry.getType());
            assertEquals(text, (String) entry.getValue());

            OMElement serialization = EntrySerializer.serializeEntry(entry, null);
            assertEquals(text, serialization.getText());

            OMElement expectedSerialization = parseXMLString(entrySrc, false);
            serialization = parseXMLString(serialization.toString(), false);
            assertTrue(compare(expectedSerialization, serialization));
        } catch (XMLStreamException e) {
            fail("Error while parsing entry definition: " + e.getMessage());
        }
    }

    public void testSimpleXMLEntry() {
        String xml = "<m:project xmlns:m=\"http://testing.synapse.apache.org\"><m:id>001</m:id>" +
                "<m:name>Synapse</m:name></m:project>";
        String entrySrc = "<localEntry xmlns=\"http://ws.apache.org/ns/synapse\" " +
                "key=\"" + key + "\">" + xml + "</localEntry>";

        try {
            OMElement elem = parseXMLString(entrySrc, true);
            OMElement expectedSerialization = elem.cloneOMElement();
            Entry entry = EntryFactory.createEntry(elem, new Properties());
            assertEquals(key, entry.getKey());
            assertEquals(Entry.INLINE_XML, entry.getType());

            OMElement valueElem = parseXMLString(xml, true);
            assertTrue(compare(valueElem, (OMElement) entry.getValue()));

            OMElement serialization = EntrySerializer.serializeEntry(entry, null);
            assertTrue(compare(expectedSerialization, serialization));
        } catch (XMLStreamException e) {
            fail("Error while parsing entry definition: " + e.getMessage());
        }
    }

    public void testXMLEntryWithCDATA() {
        xmlEntryWithCDATA(true);
        xmlEntryWithCDATA(false);
    }

    private void xmlEntryWithCDATA(boolean coalesced) {
        System.out.println("Testing simple XML entry with CDATA elements; Coalesced " +
                "parsing: " + coalesced);

        String xml = "<m:project xmlns:m=\"http://testing.synapse.apache.org\">" +
                "<![CDATA[<xml>data</xml>]]></m:project>";
        String entrySrc = "<localEntry xmlns=\"http://ws.apache.org/ns/synapse\" " +
                "key=\"" + key + "\">" + xml + "</localEntry>";

        try {
            OMElement elem = parseXMLString(entrySrc, coalesced);
            OMElement expectedSerialization = elem.cloneOMElement();
            Entry entry = EntryFactory.createEntry(elem, new Properties());
            assertEquals(key, entry.getKey());
            assertEquals(Entry.INLINE_XML, entry.getType());

            OMElement valueElem = parseXMLString(xml, coalesced);
            assertTrue(compare(valueElem, (OMElement) entry.getValue()));

            OMElement serialization = EntrySerializer.serializeEntry(entry, null);
            assertTrue(compare(expectedSerialization, serialization));
        } catch (XMLStreamException e) {
            fail("Error while parsing entry definition: " + e.getMessage());
        }
    }

    public void testLargeXMLEntry() {
        String xml = "<wsdl:definitions xmlns:axis2=\"http://ws.apache.org/axis2\" xmlns:mime=\"http://schemas.xmlsoap.org/wsdl/mime/\" xmlns:ns0=\"http://ws.apache.org/axis2/xsd\" xmlns:soap12=\"http://schemas.xmlsoap.org/wsdl/soap12/\" xmlns:http=\"http://schemas.xmlsoap.org/wsdl/http/\" xmlns:ns1=\"http://org.apache.axis2/xsd\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/wsdl/soap/\" xmlns:wsdl=\"http://schemas.xmlsoap.org/wsdl/\" targetNamespace=\"http://ws.apache.org/axis2\">\n" +
                "    <wsdl:documentation>\n" +
                "        New web service to test esb\n" +
                "    </wsdl:documentation>\n" +
                "    <wsdl:types>\n" +
                "        <xs:schema xmlns:ns=\"http://ws.apache.org/axis2/xsd\" attributeFormDefault=\"qualified\" elementFormDefault=\"qualified\" targetNamespace=\"http://ws.apache.org/axis2/xsd\">\n" +
                "            <xs:element name=\"multiply\">\n" +
                "                <xs:complexType>\n" +
                "                    <xs:sequence>\n" +
                "                        <xs:element name=\"x\" nillable=\"true\" type=\"xs:double\" />\n" +
                "                        <xs:element name=\"y\" nillable=\"true\" type=\"xs:double\" />\n" +
                "                    </xs:sequence>\n" +
                "                </xs:complexType>\n" +
                "            </xs:element>\n" +
                "            <xs:element name=\"multiplyResponse\">\n" +
                "                <xs:complexType>\n" +
                "                    <xs:sequence>\n" +
                "                        <xs:element name=\"return\" nillable=\"true\" type=\"xs:double\" />\n" +
                "                    </xs:sequence>\n" +
                "                </xs:complexType>\n" +
                "            </xs:element>\n" +
                "        </xs:schema>\n" +
                "    </wsdl:types>\n" +
                "    <wsdl:message name=\"multiplyMessage\">\n" +
                "        <wsdl:part name=\"part1\" element=\"ns0:multiply\" />\n" +
                "    </wsdl:message>\n" +
                "    <wsdl:message name=\"multiplyResponse\">\n" +
                "        <wsdl:part name=\"part1\" element=\"ns0:multiplyResponse\" />\n" +
                "    </wsdl:message>\n" +
                "    <wsdl:portType name=\"esbservicePortType\">\n" +
                "        <wsdl:operation name=\"multiply\">\n" +
                "            <wsdl:input xmlns:wsaw=\"http://www.w3.org/2006/05/addressing/wsdl\" message=\"axis2:multiplyMessage\" wsaw:Action=\"urn:multiply\" />\n" +
                "            <wsdl:output message=\"axis2:multiplyResponse\" />\n" +
                "        </wsdl:operation>\n" +
                "    </wsdl:portType>\n" +
                "    <wsdl:binding name=\"esbserviceSOAP11Binding\" type=\"axis2:esbservicePortType\">\n" +
                "        <soap:binding transport=\"http://schemas.xmlsoap.org/soap/http\" style=\"document\" />\n" +
                "        <wsdl:operation name=\"multiply\">\n" +
                "            <soap:operation soapAction=\"urn:multiply\" style=\"document\" />\n" +
                "            <wsdl:input>\n" +
                "                <soap:body use=\"literal\" />\n" +
                "            </wsdl:input>\n" +
                "            <wsdl:output>\n" +
                "                <soap:body use=\"literal\" />\n" +
                "            </wsdl:output>\n" +
                "        </wsdl:operation>\n" +
                "    </wsdl:binding>\n" +
                "    <wsdl:binding name=\"esbserviceSOAP12Binding\" type=\"axis2:esbservicePortType\">\n" +
                "        <soap12:binding transport=\"http://schemas.xmlsoap.org/soap/http\" style=\"document\" />\n" +
                "        <wsdl:operation name=\"multiply\">\n" +
                "            <soap12:operation soapAction=\"urn:multiply\" style=\"document\" />\n" +
                "            <wsdl:input>\n" +
                "                <soap12:body use=\"literal\" />\n" +
                "            </wsdl:input>\n" +
                "            <wsdl:output>\n" +
                "                <soap12:body use=\"literal\" />\n" +
                "            </wsdl:output>\n" +
                "        </wsdl:operation>\n" +
                "    </wsdl:binding>\n" +
                "    <wsdl:binding name=\"esbserviceHttpBinding\" type=\"axis2:esbservicePortType\">\n" +
                "        <http:binding verb=\"POST\" />\n" +
                "        <wsdl:operation name=\"multiply\">\n" +
                "            <http:operation location=\"multiply\" />\n" +
                "            <wsdl:input>\n" +
                "                <mime:content type=\"text/xml\" />\n" +
                "            </wsdl:input>\n" +
                "            <wsdl:output>\n" +
                "                <mime:content type=\"text/xml\" />\n" +
                "            </wsdl:output>\n" +
                "        </wsdl:operation>\n" +
                "    </wsdl:binding>\n" +
                "    <wsdl:service name=\"esbservice\">\n" +
                "        <wsdl:port name=\"esbserviceSOAP11port_http\" binding=\"axis2:esbserviceSOAP11Binding\">\n" +
                "            <soap:address location=\"http://localhost:9001/services/Service1\" />\n" +
                "        </wsdl:port>\n" +
                "        <wsdl:port name=\"esbserviceSOAP12port_http\" binding=\"axis2:esbserviceSOAP12Binding\">\n" +
                "            <soap12:address location=\"http://localhost:9001/services/Service1\" />\n" +
                "        </wsdl:port>\n" +
                "        <wsdl:port name=\"esbserviceHttpport1\" binding=\"axis2:esbserviceHttpBinding\">\n" +
                "            <http:address location=\"http://localhost:9001/services/Service1\" />\n" +
                "        </wsdl:port>\n" +
                "    </wsdl:service>\n" +
                "</wsdl:definitions>";
        
        String entrySrc = "<localEntry xmlns=\"http://ws.apache.org/ns/synapse\" " +
                "key=\"" + key + "\">" + xml + "</localEntry>";

        try {
            OMElement elem = parseXMLString(entrySrc, true);
            OMElement expectedSerialization = elem.cloneOMElement();
            Entry entry = EntryFactory.createEntry(elem, new Properties());
            assertEquals(key, entry.getKey());
            assertEquals(Entry.INLINE_XML, entry.getType());

            OMElement valueElem = parseXMLString(xml, true);
            assertTrue(compare(valueElem, (OMElement) entry.getValue()));

            OMElement serialization = EntrySerializer.serializeEntry(entry, null);
            assertTrue(compare(expectedSerialization, serialization));
        } catch (XMLStreamException e) {
            fail("Error while parsing entry definition: " + e.getMessage());
        }
    }

    private OMElement parseXMLString(String src, boolean coalesced) throws XMLStreamException {
        if (coalesced) {
            return AXIOMUtil.stringToOM(src);
        } else {
            StringReader strReader = new StringReader(src);
            XMLInputFactory xmlInFac = XMLInputFactory.newInstance();
            //Non-Coalescing parsing
            xmlInFac.setProperty("javax.xml.stream.isCoalescing", false);

            XMLStreamReader parser = xmlInFac.createXMLStreamReader(strReader);
            StAXOMBuilder builder = new StAXOMBuilder(parser);
            return builder.getDocumentElement();
        }
    }
}
