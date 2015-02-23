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
import org.apache.synapse.Startup;

import java.util.Properties;

/**
 *
 */
public class StartupSerializationTest extends AbstractTestCase {

    public void testStartupSerializationSenarioOne() throws Exception {
        String inputXml = "<task class=\"org.apache.synapse.util.TestTask\" group=\"org\" " +
                "name=\"TestTask\" xmlns=\"http://ws.apache.org/ns/synapse\">" +
                "<property name=\"name\" value=\"foo\"/>" +
                "<trigger interval=\"5\"/></task>";
        OMElement inputOM = createOMElement(inputXml);
        Startup startup = StartupFinder.getInstance().getStartup(inputOM, new Properties());
        OMElement resultOM = StartupFinder.getInstance().serializeStartup(null, startup);
        assertTrue(compare(resultOM, inputOM));
    }

    public void testStartupSerializationSenarioTwo() throws Exception {
        String inputXml = "<task class=\"org.apache.synapse.util.TestTask\" group=\"org\" " +
                "name=\"TestTask\" xmlns=\"http://ws.apache.org/ns/synapse\">" +
                "<description>Test description</description>" +
                "<property name=\"name\" value=\"foo\"/>" +
                "<trigger interval=\"5\"/></task>";
        OMElement inputOM = createOMElement(inputXml);
        Startup startup = StartupFinder.getInstance().getStartup(inputOM, new Properties());
        OMElement resultOM = StartupFinder.getInstance().serializeStartup(null, startup);
        assertTrue(compare(resultOM, inputOM));
    }
}
