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

package org.apache.synapse.commons.evaluators.config;

import org.apache.synapse.commons.evaluators.Evaluator;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.om.OMElement;
import org.custommonkey.xmlunit.XMLTestCase;

public class MatchSerializerTest extends XMLTestCase {
    
    private MatchSerializer serializer = new MatchSerializer();
    private MatchFactory fac = new MatchFactory();

    public void testHeaderMatchSerializer() {
        String input = "<match xmlns=\"http://ws.apache.org/ns/synapse\" type=\"header\" source=\"foo\" regex=\"bar\"/>";

        try {
            Evaluator eval = fac.create(AXIOMUtil.stringToOM(input));
            OMElement output = serializer.serialize(null, eval);
            assertXMLEqual(input, output.toString());
        } catch (Exception e) {
            fail("Error while parsing the input XML");
        }
    }

    public void testParameterMatchSerializer() {
        String input = "<match xmlns=\"http://ws.apache.org/ns/synapse\" type=\"param\" source=\"foo\" regex=\"bar\"/>";

        try {
            Evaluator eval = fac.create(AXIOMUtil.stringToOM(input));
            OMElement output = serializer.serialize(null, eval);
            assertXMLEqual(input, output.toString());
        } catch (Exception e) {
            fail("Error while parsing the input XML");
        }
    }

    public void testURLMatchSerializer() {
        String input = "<match xmlns=\"http://ws.apache.org/ns/synapse\" type=\"url\" regex=\"http://foo.org\"/>";

        try {
            Evaluator eval = fac.create(AXIOMUtil.stringToOM(input));
            OMElement output = serializer.serialize(null, eval);
            assertXMLEqual(input, output.toString());
        } catch (Exception e) {
            fail("Error while parsing the input XML");
        }
    }

    public void testURLMatchSerializer2() {
        String input = "<match xmlns=\"http://ws.apache.org/ns/synapse\" type=\"url\" regex=\"http://foo.org\" source=\"protocol\"/>";

        try {
            Evaluator eval = fac.create(AXIOMUtil.stringToOM(input));
            OMElement output = serializer.serialize(null, eval);
            assertXMLEqual(input, output.toString());
        } catch (Exception e) {
            fail("Error while parsing the input XML");
        }
    }
}
