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

package org.apache.synapse.core.axis2;

import junit.framework.TestCase;
import org.apache.synapse.MessageContext;
import org.apache.synapse.mediators.TestUtils;
import org.apache.synapse.util.xpath.SynapseXPath;

public class Axis2MessageContextTest extends TestCase {
    String nsSoapEnv = "http://schemas.xmlsoap.org/soap/envelope/";
    String nsNamespace1 = "namespace1";
    private String sampleBody = "<ns1:a xmlns:ns1='namespace1'>" +
                                    "<ns1:b>first</ns1:b>" +
                                    "<ns1:c>second</ns1:c>" +
                                "</ns1:a>";


    public void testMessageContextGetStringValueBody() throws Exception {
        SynapseXPath axiomXpath = new SynapseXPath("$body/ns1:a/ns1:c");
        axiomXpath.addNamespace("ns1", nsNamespace1);
        MessageContext synCtx = TestUtils.getTestContext(sampleBody);

        String result = axiomXpath.stringValueOf(synCtx);
        assertEquals("second", result);
    }

    public void testMessageContextGetStringValueEnvelope() throws Exception {
        SynapseXPath axiomXpath = new SynapseXPath("/s11:Envelope/s11:Body/ns1:a/ns1:c");
        axiomXpath.addNamespace("s11", nsSoapEnv);
        axiomXpath.addNamespace("ns1", nsNamespace1);

        MessageContext synCtx = TestUtils.getTestContext(sampleBody);

        String result = axiomXpath.stringValueOf(synCtx);
        assertEquals("second", result);
    }

}
