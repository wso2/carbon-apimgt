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

public class HeaderMediatorSerializationTest extends AbstractTestCase {

    HeaderMediatorFactory headerMediatorFactory;
    HeaderMediatorSerializer headerMediatorSerializer;

    public HeaderMediatorSerializationTest() {
        super(AbstractTestCase.class.getName());
        headerMediatorFactory = new HeaderMediatorFactory();
        headerMediatorSerializer = new HeaderMediatorSerializer();
    }

    public void testHeaderMediatorSerializationSenarioOne() throws Exception {
        String inputXml = "<header xmlns=\"http://ws.apache.org/ns/synapse\" name=\"To\" value=\"http://127.0.0.1:10001/services/Services\"/>";
        assertTrue(serialization(inputXml, headerMediatorFactory, headerMediatorSerializer));
        assertTrue(serialization(inputXml, headerMediatorSerializer));
    }

    public void testHeaderMediatorSerializationSenarioTwo() throws Exception {
        String inputXml = "<header xmlns=\"http://ws.apache.org/ns/synapse\" name=\"To\" action=\"remove\"/>";
        assertTrue(serialization(inputXml, headerMediatorFactory, headerMediatorSerializer));
        assertTrue(serialization(inputXml, headerMediatorSerializer));
    }
}
