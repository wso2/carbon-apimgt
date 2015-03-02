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

package org.apache.synapse.mediators.eip;

import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.xml.CloneMediatorFactory;

import java.util.Properties;

/**
 *
 */
public class CloneMediatorTest extends AbstractSplitMediatorTestCase {

    protected void setUp() throws Exception {
        super.setUp();
        fac = new CloneMediatorFactory();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        fac = null;
    }

    public void testClonningScenarioOne() throws Exception {
        Mediator clone = fac.createMediator(createOMElement("<clone " +
            "xmlns=\"http://ws.apache.org/ns/synapse\"><target soapAction=\"urn:clone\" " +
            "sequence=\"seqRef\"/><target to=\"http://test\"><sequence><sequence " +
            "key=\"seqRef\"/></sequence></target></clone>"), new Properties());
        clone.mediate(testCtx);
        while(helperMediator.getMediatedContext(1) == null) {
            Thread.sleep(100);
        }
        MessageContext mediatedCtx = helperMediator.getMediatedContext(0);
        String formerSAction = mediatedCtx.getSoapAction();
        mediatedCtx = helperMediator.getMediatedContext(1);
        if ("urn:clone".equals(formerSAction)) {
            assertEquals(mediatedCtx.getSoapAction(), "urn:test");
            assertEquals(mediatedCtx.getTo().getAddress(), "http://test");
        } else {
            assertEquals(mediatedCtx.getSoapAction(), "urn:clone");
        }
    }

    public void testClonningWithContinueParent() throws Exception {
        Mediator clone = fac.createMediator(createOMElement("<clone continueParent=\"true\" " +
            "xmlns=\"http://ws.apache.org/ns/synapse\"><target soapAction=\"urn:clone\" " +
            "sequence=\"seqRef\"/><target to=\"http://test\"><sequence><sequence " +
            "key=\"seqRef\"/></sequence></target></clone>"), new Properties());
        assertTrue(clone.mediate(testCtx));
        while(helperMediator.getMediatedContext(1) == null) {
            Thread.sleep(100);
        }
        MessageContext mediatedCtx = helperMediator.getMediatedContext(0);
        assertTrue(mediatedCtx.getEnvelope().getBody().getFirstElement() == null);
        String formerSAction = mediatedCtx.getSoapAction();
        mediatedCtx = helperMediator.getMediatedContext(1);
        if ("urn:clone".equals(formerSAction)) {
            assertEquals(mediatedCtx.getSoapAction(), "urn:test");
            assertEquals(mediatedCtx.getTo().getAddress(), "http://test");
        } else {
            assertEquals(mediatedCtx.getSoapAction(), "urn:clone");
        }
        assertEquals(testCtx.getSoapAction(), "urn:test");
        assertEquals(testCtx.getTo(), null);
    }
}
