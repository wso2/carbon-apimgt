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
package org.apache.synapse.mediators.xquery;

import junit.framework.TestCase;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.SynapseConfigUtils;
import org.apache.synapse.mediators.TestUtils;

import javax.xml.namespace.QName;
import java.util.List;
import java.util.ArrayList;

import javax.xml.xquery.XQItemType;

/**
 *
 */

public class XQueryMediatorTest extends TestCase {

    public final static String sampleXml = "<bookstore><book category=\"COOKING\"> " +
            "<title lang=\"en\">Everyday Italian</title>\n" +
            "  <author>Giada De Laurentiis</author>\n" +
            "  <year>2005</year>\n" +
            "  <price>30.00</price>\n" +
            "\n" +
            "</book>\n" +
            "\n" +
            "<book category=\"CHILDREN\">\n" +
            "  <title lang=\"en\">Harry Potter</title>\n" +
            "  <author>J K. Rowling</author>\n" +
            "  <year>2005</year>\n" +
            "  <price>29.99</price>\n" +
            "</book>\n" +
            "\n" +
            "<book category=\"WEB\">\n" +
            "  <title lang=\"en\">XQuery Kick Start</title>\n" +
            "  <author>James McGovern</author>\n" +
            "  <author>Per Bothner</author>\n" +
            "  <author>Kurt Cagle</author>\n" +
            "  <author>James Linn</author>\n" +
            "  <author>Vaidyanathan Nagarajan</author>\n" +
            "\n" +
            "  <year>2003</year>\n" +
            "  <price>49.99</price>\n" +
            "</book>\n" +
            "\n" +
            "<book category=\"WEB\">\n" +
            "  <title lang=\"en\">Learning XML</title>\n" +
            "  <author>Erik T. Ray</author>\n" +
            "  <year>2003</year>\n" +
            "\n" +
            "  <price>39.95</price>\n" +
            "</book>\n" +
            "\n" +
            "</bookstore>";

    public final static String sampleXml2 = "<m0:CheckPriceRequest" +
            " xmlns:m0=\"http://www.apache-synapse.org/test\">\n" +
            "    <m0:Code>IBM</m0:Code>\n" +
            "</m0:CheckPriceRequest>";
    public final static String sampleXml3 = "<m0:return xmlns:m0=\"http://services.samples/xsd\">\n" +
            "\t<m0:symbol>IBM</m0:symbol>\n" +
            "\t<m0:last>122222</m0:last>\n" +
            "</m0:return>";
    public final static String externalXMl = "<commission>\n" +
            "    <vendor symbol=\"IBM\">44444</vendor>\n" +
            "    <vendor symbol=\"MSFT\">55555</vendor>\n" +
            "    <vendor symbol=\"SUN\">66666</vendor>\n" +
            "</commission>";

    public void testQueryWithAll() throws Exception {
        MessageContext mc = TestUtils.getTestContext("<foo/>", null);
        XQueryMediator mediator = new XQueryMediator();
        mediator.setQuerySource("declare variable $intVar as xs:int external;" +
                "declare variable $boolVar as xs:boolean external;" +
                "declare variable $byteVar as xs:byte external;" +
                "declare variable $longVar as xs:long external;" +
                "declare variable $doubleVar as xs:double external;" +
                "declare variable $shortVar as xs:short external;" +
                "declare variable $floatVar as xs:float external;" +
                "declare variable $stringVar as xs:string external;" +
                "declare variable $integerVar as xs:integer external;" +
                "document { " +
                "<a xmlns='http://a/uri' z:in='out' xmlns:z='http://z/uri'>" +
                "<b>{$intVar+2}<e>{$boolVar}</e>" +
                "<all>" +
                "{$byteVar}," +
                "{$shortVar}," +
                "{$doubleVar}," +
                "{$longVar}," +
                "{$floatVar}," +
                "{$stringVar}," +
                "{$integerVar+xs:integer('5')}," +
                "</all></b></a> }");
        List<MediatorVariable> list = new ArrayList<MediatorVariable>();
        MediatorVariable intVariable = new MediatorBaseVariable(new QName("intVar"));
        intVariable.setType(XQItemType.XQBASETYPE_INT);
        intVariable.setValue(8);
        list.add(intVariable);
        MediatorVariable boolVariable = new MediatorBaseVariable(new QName("boolVar"));
        boolVariable.setType(XQItemType.XQBASETYPE_BOOLEAN);
        boolVariable.setValue(Boolean.TRUE);
        list.add(boolVariable);
        MediatorVariable doubleVariable = new MediatorBaseVariable(new QName("doubleVar"));
        doubleVariable.setType(XQItemType.XQBASETYPE_DOUBLE);
        doubleVariable.setValue(23.33);
        list.add(doubleVariable);
        MediatorVariable floatVariable = new MediatorBaseVariable(new QName("floatVar"));
        floatVariable.setType(XQItemType.XQBASETYPE_FLOAT);
        floatVariable.setValue(new Float(23.33));
        list.add(floatVariable);
        MediatorVariable shortVariable = new MediatorBaseVariable(new QName("shortVar"));
        shortVariable.setType(XQItemType.XQBASETYPE_SHORT);
        shortVariable.setValue((short) 327);
        list.add(shortVariable);
        MediatorVariable byteVariable = new MediatorBaseVariable(new QName("byteVar"));
        byteVariable.setType(XQItemType.XQBASETYPE_BYTE);
        byteVariable.setValue((byte) 3);
        list.add(byteVariable);
        MediatorVariable longVariable = new MediatorBaseVariable(new QName("longVar"));
        longVariable.setType(XQItemType.XQBASETYPE_LONG);
        longVariable.setValue((long) 334);
        list.add(longVariable);
        MediatorVariable stringValue = new MediatorBaseVariable(new QName("stringVar"));
        stringValue.setType(XQItemType.XQBASETYPE_STRING);
        stringValue.setValue("synapse");
        list.add(stringValue);
        MediatorVariable integerValue = new MediatorBaseVariable(new QName("integerVar"));
        integerValue.setType(XQItemType.XQBASETYPE_INTEGER);
        integerValue.setValue(5);
        list.add(integerValue);
        mediator.addAllVariables(list);
        assertTrue(mediator.mediate(mc));
        assertEquals("10", mc.getEnvelope().getBody().getFirstElement().
                getFirstElement().getText().trim());
        assertEquals("true", mc.getEnvelope().getBody().getFirstElement().
                getFirstElement().getFirstElement().getText());
    }

    public void testQueryWithPayload() throws Exception {
        MessageContext mc = TestUtils.getTestContext(sampleXml, null);
        XQueryMediator mediator = new XQueryMediator();
        List<MediatorVariable> list = new ArrayList<MediatorVariable>();
        MediatorVariable variable = new MediatorCustomVariable(new QName("payload"));
        variable.setType(XQItemType.XQITEMKIND_DOCUMENT);
        list.add(variable);
        mediator.addAllVariables(list);
        mediator.setQuerySource("declare variable $payload as document-node() external;" +
                "$payload//bookstore/book/title");
        assertTrue(mediator.mediate(mc));
        assertEquals("Everyday Italian", mc.getEnvelope().getBody().getFirstElement().getText());
    }

    public void testQueryWithPayloadTwo() throws Exception {
        MessageContext mc = TestUtils.getTestContext(sampleXml2, null);
        XQueryMediator mediator = new XQueryMediator();
        List<MediatorVariable> list = new ArrayList<MediatorVariable>();
        MediatorVariable variable = new MediatorCustomVariable(new QName("payload"));
        variable.setType(XQItemType.XQITEMKIND_DOCUMENT);
        list.add(variable);
        mediator.addAllVariables(list);
        mediator.setQuerySource("declare namespace m0=\"http://www.apache-synapse.org/test\"; " +
                "declare variable $payload as document-node() external;" +
                "<m:getQuote xmlns:m=\"http://services.samples/xsd\">\n" +
                "<m:request>" +
                "   <m:symbol>{$payload//m0:CheckPriceRequest/m0:Code/child::text()}" +
                "   </m:symbol><" +
                "/m:request>\n" +
                "</m:getQuote> ");
        assertTrue(mediator.mediate(mc));

        assertEquals("IBM", mc.getEnvelope().getBody().getFirstElement().
                getFirstElement().getFirstElement().getText());
    }

    public void testQueryWithPayloadThree() throws Exception {
        MessageContext mc = TestUtils.getTestContext(sampleXml3, null);
        XQueryMediator mediator = new XQueryMediator();
        List<MediatorVariable> list = new ArrayList<MediatorVariable>();
        MediatorVariable variable = new MediatorCustomVariable(new QName("payload"));
        variable.setType(XQItemType.XQITEMKIND_DOCUMENT);
        list.add(variable);
        mediator.addAllVariables(list);
        mediator.setQuerySource("declare namespace m0=\"http://services.samples/xsd\";" +
                " declare variable $payload as document-node() external;\n" +
                "<m:CheckPriceResponse xmlns:m=\"http://www.apache-synapse.org/test\">\n" +
                "\t<m:Code>{$payload//m0:return/m0:symbol/child::text()}</m:Code>\n" +
                "\t<m:Price>{$payload//m0:return/m0:last/child::text()}</m:Price>\n" +
                "</m:CheckPriceResponse>");
        assertTrue(mediator.mediate(mc));
    }

    public void testQueryWithPayloadFour() throws Exception {
        MessageContext mc = TestUtils.getTestContext(sampleXml3, null);
        XQueryMediator mediator = new XQueryMediator();
        List<MediatorVariable> list = new ArrayList<MediatorVariable>();
        MediatorVariable variable = new MediatorCustomVariable(new QName("payload"));
        variable.setType(XQItemType.XQITEMKIND_DOCUMENT);
        list.add(variable);
        MediatorCustomVariable variableForXml = new MediatorCustomVariable(new QName("commission"));
        variableForXml.setType(XQItemType.XQITEMKIND_DOCUMENT);
        variableForXml.setRegKey("file:key");
        variableForXml.setValue(SynapseConfigUtils.stringToOM(externalXMl));
        list.add(variableForXml);
        mediator.addAllVariables(list);
        mediator.setQuerySource(" declare namespace m0=\"http://services.samples/xsd\";\n" +
                " declare variable $payload as document-node() external;\n" +
                " declare variable $commission as document-node() external;\n" +
                " <m0:return xmlns:m0=\"http://services.samples/xsd\">\n" +
                "  \t<m0:symbol>{$payload//m0:return/m0:symbol/child::text()}" +
                "   </m0:symbol>\n" +
                "  \t<m0:last>{$payload//m0:return/m0:last/child::text()+ " +
                "$commission//commission/vendor[@symbol=$payload//m0:return/m0:symbol/child::text()]}" +
                "</m0:last>\n" +
                " </m0:return>");
        assertTrue(mediator.mediate(mc));
    }

    public void testQueryReturnInt() throws Exception {
        MessageContext mc = TestUtils.getTestContext("<foo/>", null);
        XQueryMediator mediator = new XQueryMediator();
        mediator.setQuerySource("for $n in 1 to 10 return $n*$n");
        assertTrue(mediator.mediate(mc));
        assertEquals("1", mc.getEnvelope().getBody().getFirstElement().getText());
    }

    public void testQueryReturnBoolean() throws Exception {
        MessageContext mc = TestUtils.getTestContext("<foo/>", null);
        XQueryMediator mediator = new XQueryMediator();
        mediator.setQuerySource("declare variable $boolVar as xs:boolean external; $boolVar");
        List<MediatorVariable> list = new ArrayList<MediatorVariable>();
        MediatorVariable boolVariable = new MediatorBaseVariable(new QName("boolVar"));
        boolVariable.setType(XQItemType.XQBASETYPE_BOOLEAN);
        boolVariable.setValue(Boolean.TRUE);
        list.add(boolVariable);
        mediator.addAllVariables(list);
        assertTrue(mediator.mediate(mc));
        assertEquals("true", mc.getEnvelope().getBody().getFirstElement().getText());
    }
}
