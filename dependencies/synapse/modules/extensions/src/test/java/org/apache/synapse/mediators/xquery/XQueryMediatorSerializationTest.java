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

import org.apache.synapse.mediators.AbstractTestCase;

/**
 *
 */

public class XQueryMediatorSerializationTest extends AbstractTestCase {
    private XQueryMediatorFactory factory;
    private XQueryMediatorSerializer serializer;

    public XQueryMediatorSerializationTest() {
        factory = new XQueryMediatorFactory();
        serializer = new XQueryMediatorSerializer();
    }

    public void testXQueryMediatorSerializationSenarioOne() throws Exception {
        String inputXml = "<xquery xmlns=\"http://ws.apache.org/ns/synapse\" key=\"querykey\" target=\"target\">" +
                          "<dataSource>" +
                          "<property name=\"username\" value=\"valueone\" />" +
                          "</dataSource>" +
                          "<variable name=\"b1\" value=\"23\" type=\"INT\" />" +
                          "<variable name=\"b1\" value=\"true\" type=\"BOOLEAN\" />" +
                          "<variable name=\"b1\" value=\"23.44\" type=\"DOUBLE\" />" +
                          "<variable name=\"b1\" value=\"23\" type=\"LONG\" />" +
                          "<variable name=\"b1\" value=\"23.1\" type=\"FLOAT\" />" +
                          "<variable name=\"b1\" value=\"23\" type=\"SHORT\" />" +
                          "<variable name=\"b1\" value=\"23\" type=\"BYTE\" />" +
                          "<variable name=\"b1\" value=\"synapse\" type=\"STRING\" />" +
                          "<variable name=\"b1\" key=\"xmlkey\" type=\"DOCUMENT\" />" +
                          "<variable name=\"b1\" key=\"xmlkey\" type=\"DOCUMENT_ELEMENT\" />" +
                          "<variable name=\"b1\" key=\"xmlkey\" type=\"ELEMENT\" />" +
                          "</xquery>";
        assertTrue(serialization(inputXml, factory, serializer));

    }

}
