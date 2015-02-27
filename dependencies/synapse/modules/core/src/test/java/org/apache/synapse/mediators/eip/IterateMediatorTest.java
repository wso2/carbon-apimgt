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

import org.apache.synapse.config.xml.IterateMediatorFactory;
import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;

import java.util.Properties;

/**
 *
 */
public class IterateMediatorTest extends AbstractSplitMediatorTestCase {

    protected void setUp() throws Exception {
        super.setUp();
        SOAPEnvelope envelope = OMAbstractFactory.getSOAP11Factory().getDefaultEnvelope();
        envelope.getBody().addChild(createOMElement("<original>" +
            "<itr>test-split-context-itr1-body</itr>" + "<itr>test-split-context-itr2-body</itr>" +
            "</original>"));
        testCtx.setEnvelope(envelope);
        fac = new IterateMediatorFactory();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        fac = null;
    }

    public void testIterationScenarioOne() throws Exception {
        Mediator iterate = fac.createMediator(createOMElement("<iterate " +
            "expression=\"//original/itr\" xmlns=\"http://ws.apache.org/ns/synapse\">" +
            "<target soapAction=\"urn:iterate\" sequence=\"seqRef\"/></iterate>"), new Properties());
        helperMediator.clearMediatedContexts();
        iterate.mediate(testCtx);
        while(helperMediator.getMediatedContext(1) == null) {
            Thread.sleep(100);
        }
        MessageContext mediatedCtx = helperMediator.getMediatedContext(0);
        assertEquals(mediatedCtx.getSoapAction(), "urn:iterate");
        OMElement formerBody = mediatedCtx.getEnvelope().getBody().getFirstElement();
        mediatedCtx = helperMediator.getMediatedContext(1);
        assertEquals(mediatedCtx.getSoapAction(), "urn:iterate");
        if (formerBody == null) {
            assertEquals(mediatedCtx.getEnvelope()
                .getBody().getFirstElement().getText(), helperMediator.getCheckString());
        }
    }

    public void testIterationWithPreservePayload() throws Exception {
        Mediator iterate = fac.createMediator(createOMElement("<iterate " +
            "expression=\"//original/itr\" preservePayload=\"true\" attachPath=\"//original\" " +
            "xmlns=\"http://ws.apache.org/ns/synapse\"><target soapAction=\"urn:iterate\" " +
            "sequence=\"seqRef\"/></iterate>"), new Properties());
        iterate.mediate(testCtx);
        while(helperMediator.getMediatedContext(1) == null) {
            Thread.sleep(100);
        }
        MessageContext mediatedCtx = helperMediator.getMediatedContext(0);
        assertEquals(mediatedCtx.getSoapAction(), "urn:iterate");
        OMElement formerBody = mediatedCtx.getEnvelope().getBody().getFirstElement();
        mediatedCtx = helperMediator.getMediatedContext(1);
        assertEquals(mediatedCtx.getSoapAction(), "urn:iterate");
        if (formerBody == null) {
            assertEquals(mediatedCtx.getEnvelope().getBody()
                .getFirstElement().getFirstElement().getText(), helperMediator.getCheckString());
        }
    }
}
