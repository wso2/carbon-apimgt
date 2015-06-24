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

package org.apache.synapse.mediators.bsf.javascript;

import junit.framework.TestCase;

import org.apache.synapse.MessageContext;
import org.apache.synapse.mediators.TestUtils;
import org.apache.synapse.mediators.bsf.ScriptMediator;

public class JavaScriptMediatorTest extends TestCase {

    public void testInlineMediator() throws Exception {
        ScriptMediator mediator = new ScriptMediator("js", "mc.getPayloadXML().b == 'petra';",null);

        MessageContext mc = TestUtils.getTestContext("<a><b>petra</b></a>", null);
        assertTrue(mediator.mediate(mc));

        mc = TestUtils.getTestContext("<a><b>sue</b></a>", null);
        assertFalse(mediator.mediate(mc));

        mc = TestUtils.getTestContext("<a><b>petra</b></a>", null);
        assertTrue(mediator.mediate(mc));
    }

    public void testInlineMediator2() throws Exception {
        ScriptMediator mediator = new ScriptMediator("js", "mc.getPayloadXML().b == 'petra';",null);

        MessageContext mc = TestUtils.getTestContext("<a><b>petra</b></a>", null);
        assertTrue(mediator.mediate(mc));

        mc = TestUtils.getTestContext("<a><b>sue</b></a>", null);
        assertFalse(mediator.mediate(mc));

        mc = TestUtils.getTestContext("<a><b>petra</b></a>", null);
        assertTrue(mediator.mediate(mc));
    }
}
