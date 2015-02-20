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
package org.apache.synapse.mediators.bsf;

import org.apache.axiom.om.impl.exception.XMLComparisonException;
import org.apache.synapse.mediators.AbstractTestCase;

public class ScriptMediatorSerializationTest extends AbstractTestCase {

    ScriptMediatorFactory mediatorFactory;
    ScriptMediatorSerializer scriptMediatorSerializer;

    public ScriptMediatorSerializationTest() {
        mediatorFactory = new ScriptMediatorFactory();
        scriptMediatorSerializer = new ScriptMediatorSerializer();
    }

    public void testScriptMediatorSerializationScenarioOne() throws XMLComparisonException {
        String inputXml = "<script xmlns=\"http://ws.apache.org/ns/synapse\" key=\"script-key\" function=\"funOne\" language=\"js\"></script> ";
        assertTrue(serialization(inputXml, mediatorFactory, scriptMediatorSerializer));
        assertTrue(serialization(inputXml, scriptMediatorSerializer));
    }

    public void testScriptMediatorSerializationScenarioTwo() throws XMLComparisonException {
        String inputXml = "<script xmlns=\"http://ws.apache.org/ns/synapse\" language=\"js\" key=\"script-key\" ></script> ";
        assertTrue(serialization(inputXml, mediatorFactory, scriptMediatorSerializer));
        assertTrue(serialization(inputXml, scriptMediatorSerializer));
    }

    public void testInlineScriptMediatorSerializationScenarioOne() throws XMLComparisonException {
        String inputXml = "<syn:script xmlns:syn=\"http://ws.apache.org/ns/synapse\" language='js'>" +
                "<![CDATA[var symbol = mc.getPayloadXML()..*::Code.toString();mc.setPayloadXML(<m:getQuote xmlns:m=\"http://services.samples/xsd\">\n" +
                "<m:request><m:symbol>{symbol}</m:symbol></m:request></m:getQuote>);]]></syn:script> ";
        assertTrue(serialization(inputXml, mediatorFactory, scriptMediatorSerializer));
        assertTrue(serialization(inputXml, scriptMediatorSerializer));
    }

//    public void testInlineScriptMediatorSerializationScenarioTwo() throws XMLComparisonException {
//        String inputXml = "<syn:script xmlns:syn=\"http://ws.apache.org/ns/synapse\" language='rb'>" +
//                "<![CDATA[" +
//                "require 'rexml/document'\n" +
//                "include REXML\n" +
//                "newRequest= Document.new '<m:getQuote xmlns:m=\"http://services.samples/xsd\"><m:request><m:symbol>...test...</m:symbol></m:request></m:getQuote>'\n" +
//                "newRequest.root.elements[1].elements[1].text = $mc.getPayloadXML().root.elements[1].get_text\n" +
//                "$mc.setPayloadXML(newRequest)" +
//                "]]></syn:script>";
//        assertTrue(serialization(inputXml, mediatorFactory, scriptMediatorSerializer));
//        assertTrue(serialization(inputXml, scriptMediatorSerializer));
//    }
}
