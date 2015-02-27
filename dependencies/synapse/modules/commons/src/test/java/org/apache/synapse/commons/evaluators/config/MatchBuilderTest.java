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

import junit.framework.TestCase;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.synapse.commons.evaluators.MatchEvaluator;
import org.apache.synapse.commons.evaluators.source.SourceTextRetriever;
import org.apache.synapse.commons.evaluators.source.HeaderTextRetriever;
import org.apache.synapse.commons.evaluators.source.ParameterTextRetriever;
import org.apache.synapse.commons.evaluators.source.URLTextRetriever;

public class MatchBuilderTest extends TestCase {

    private static final String SOURCE = "foo";
    private static final String REGEX = "bar";
    private static final String FRAGMENT = "protocol";

    private MatchFactory fac = new MatchFactory();

    public void testHeaderMatch() {
        String input = "<match type=\"header\" source=\"" + SOURCE +
                "\" regex=\"" + REGEX + "\"/>";

        try {
            MatchEvaluator eval = (MatchEvaluator) fac.create(AXIOMUtil.stringToOM(input));
            SourceTextRetriever txtRtvr = eval.getTextRetriever();
            assertTrue(txtRtvr instanceof HeaderTextRetriever);
            assertEquals(txtRtvr.getSource(), SOURCE);
            assertEquals(eval.getRegex().pattern(), REGEX);
        } catch (Exception e) {
            fail("Error while parsing the input XML");
        }
    }

    public void testParameterMatch() {
        String input = "<match type=\"param\" source=\"" + SOURCE +
                "\" regex=\"" + REGEX + "\"/>";

        try {
            MatchEvaluator eval = (MatchEvaluator) fac.create(AXIOMUtil.stringToOM(input));
            SourceTextRetriever txtRtvr = eval.getTextRetriever();
            assertTrue(txtRtvr instanceof ParameterTextRetriever);
            assertEquals(txtRtvr.getSource(), SOURCE);
            assertEquals(eval.getRegex().pattern(), REGEX);
        } catch (Exception e) {
            fail("Error while parsing the input XML");
        }
    }

    public void testURLMatch() {
        String input = "<match type=\"url\" regex=\"" + REGEX + "\"/>";

        try {
            MatchEvaluator eval = (MatchEvaluator) fac.create(AXIOMUtil.stringToOM(input));
            SourceTextRetriever txtRtvr = eval.getTextRetriever();
            assertTrue(txtRtvr instanceof URLTextRetriever);
            assertEquals(eval.getRegex().pattern(), REGEX);
            assertNull(txtRtvr.getSource());
        } catch (Exception e) {
            e.printStackTrace();
            fail("Error while parsing the input XML");
        }
    }

    public void testURLMatch2() {
        String input = "<match type=\"url\" regex=\"" + REGEX + "\" source=\"" +
                FRAGMENT +"\"/>";

        try {
            MatchEvaluator eval = (MatchEvaluator) fac.create(AXIOMUtil.stringToOM(input));
            SourceTextRetriever txtRtvr = eval.getTextRetriever();
            assertTrue(txtRtvr instanceof URLTextRetriever);
            assertEquals(eval.getRegex().pattern(), REGEX);
            assertEquals(txtRtvr.getSource(), FRAGMENT);
        } catch (Exception e) {
            fail("Error while parsing the input XML");
        }
    }
}
