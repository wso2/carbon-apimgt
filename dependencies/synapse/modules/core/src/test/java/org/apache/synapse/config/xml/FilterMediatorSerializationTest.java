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

public class FilterMediatorSerializationTest extends AbstractTestCase {

    FilterMediatorFactory filterMediatorFactory;
    FilterMediatorSerializer filterMediatorSerializer;

    public FilterMediatorSerializationTest() {
        super(FilterMediatorSerializationTest.class.getName());
        filterMediatorFactory = new FilterMediatorFactory();
        filterMediatorSerializer = new FilterMediatorSerializer();
    }

    public void testFilterMediatorSerializationSenarioOne() throws Exception {
        String inputXml = "<filter xmlns=\"http://ws.apache.org/ns/synapse\" xpath=\"//*[wsx:symbol='MSFT']\" xmlns:wsx=\"http://services.samples/xsd\"/>";
        assertTrue(serialization(inputXml, filterMediatorFactory, filterMediatorSerializer));
        assertTrue(serialization(inputXml, filterMediatorSerializer));
    }

    public void testFilterMediatorSerializationSenarioTwo() throws Exception {
        String inputXml = "<filter xmlns=\"http://ws.apache.org/ns/synapse\" source=\"get-property('To')\" regex=\".*/StockQuote.*\"></filter>";
        assertTrue(serialization(inputXml, filterMediatorFactory, filterMediatorSerializer));
        assertTrue(serialization(inputXml, filterMediatorSerializer));
    }

    public void testFilterMediatorSerializationSenarioThree() throws Exception {
        String inputXml = "<filter xmlns=\"http://ws.apache.org/ns/synapse\" source=\"get-property('To')\" regex=\".*/StockQuote.*\"><send/></filter>";
        assertTrue(serialization(inputXml, filterMediatorFactory, filterMediatorSerializer));
        assertTrue(serialization(inputXml, filterMediatorSerializer));
    }

    public void testFilterMediatorSerializationSenarioFour() throws Exception {
        String inputXml = "<filter xmlns=\"http://ws.apache.org/ns/synapse\" source=\"get-property('To')\" regex=\".*/StockQuote.*\"><then><send/></then></filter>";
        assertTrue(serialization(inputXml, filterMediatorFactory, filterMediatorSerializer));
        assertTrue(serialization(inputXml, filterMediatorSerializer));
    }

    public void testFilterMediatorSerializationSenarioFive() throws Exception {
        String inputXml = "<filter xmlns=\"http://ws.apache.org/ns/synapse\" source=\"get-property('To')\" regex=\".*/StockQuote.*\"><then><send/></then><else><drop/></else></filter>";
        assertTrue(serialization(inputXml, filterMediatorFactory, filterMediatorSerializer));
        assertTrue(serialization(inputXml, filterMediatorSerializer));
    }

    public void testFilterMediatorSerializationSenarioSix() throws Exception {
        String inputXml = "<filter xmlns=\"http://ws.apache.org/ns/synapse\" source=\"get-property('To')\" regex=\".*/StockQuote.*\"><then sequence=\"test\"/></filter>";
        assertTrue(serialization(inputXml, filterMediatorFactory, filterMediatorSerializer));
        assertTrue(serialization(inputXml, filterMediatorSerializer));
    }

    public void testFilterMediatorSerializationSenarioSeven() throws Exception {
        String inputXml = "<filter xmlns=\"http://ws.apache.org/ns/synapse\" source=\"get-property('To')\" regex=\".*/StockQuote.*\"><then sequence=\"test\"/><else sequence=\"test2\"/></filter>";
        assertTrue(serialization(inputXml, filterMediatorFactory, filterMediatorSerializer));
        assertTrue(serialization(inputXml, filterMediatorSerializer));
    }

}
