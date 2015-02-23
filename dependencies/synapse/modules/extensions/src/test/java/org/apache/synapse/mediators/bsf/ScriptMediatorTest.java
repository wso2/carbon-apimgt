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

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.extensions.RepeatedTest;

import org.apache.synapse.MessageContext;
import org.apache.synapse.mediators.TestUtils;

import java.util.Random;

public class ScriptMediatorTest extends TestCase {

    private static final String inlinescript = "var state=5;";

    private String threadsafetyscript = "var rno = mc.getPayloadXML().toString(); rno=rno*2; mc.setPayloadXML" +
            "(<randomNo>{rno}</randomNo>)";

    public void testInlineMediator() throws Exception {
        MessageContext mc = TestUtils.getTestContext("<foo/>", null);
        ScriptMediator mediator = new ScriptMediator("js", inlinescript,null);
        assertTrue(mediator.mediate(mc));
    }

    public void testThreadSafety() throws Exception {
        MessageContext mc = TestUtils.getTestContext("<randomNo/>", null);
        Random rand = new Random();
        String randomno = Integer.toString(rand.nextInt(200));
        mc.getEnvelope().getBody().getFirstElement().setText(randomno);
        ScriptMediator mediator = new ScriptMediator("js", threadsafetyscript,null);
        mediator.mediate(mc);
        assertEquals(Integer.parseInt(mc.getEnvelope().getBody().getFirstElement().getText()),
                Integer.parseInt(randomno) * 2);
    }


    public static Test suite() {
        TestSuite suite = new TestSuite();
        for (int i = 0; i < 10; i++) {
            suite.addTest(new RepeatedTest(new ScriptMediatorTest("testThreadSafety"), 10));
        }
        return suite;
    }

    public ScriptMediatorTest(String name) {
        super(name);
    }
}
