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

package org.apache.synapse.mediators.ext;

import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.TestMessageContext;
import org.apache.synapse.config.xml.MediatorFactoryFinder;
import org.apache.synapse.mediators.AbstractMediatorTestCase;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.om.OMAbstractFactory;

import java.util.Properties;

/**
 * Tests the pojo command mediator instantiation and setting of literal and
 * XPath parameters at runtime.
 */
public class POJOCommandMediatorTest extends AbstractMediatorTestCase {

    public void testPojoWithoutPropertiesNotCommandImpl() throws Exception {
        Mediator pcm = MediatorFactoryFinder.getInstance().getMediator(createOMElement(
                "<pojoCommand name='org.apache.synapse.mediators.ext.POJOCommandTestMediator' " +
                        "xmlns='http://ws.apache.org/ns/synapse'/>"), new Properties());
        POJOCommandTestHelper.reset();
        pcm.mediate(new TestMessageContext());
        assertTrue(POJOCommandTestHelper.getInstance().isExecuted());
    }

    public void testPojoWithPropertiesNotCommandImpl() throws Exception {
        Mediator pcm = MediatorFactoryFinder.getInstance().getMediator(createOMElement(
                "<pojoCommand name='org.apache.synapse.mediators.ext.POJOCommandTestMediator' " +
                        "xmlns='http://ws.apache.org/ns/synapse'><property name=\"testProp\" " +
                        "expression=\"fn:concat('XPATH ', 'FUNC')\" action=\"ReadMessage\"/></pojoCommand>"), new Properties());
        POJOCommandTestHelper.reset();
        pcm.mediate(new TestMessageContext());
        assertEquals("XPATH FUNC", POJOCommandTestHelper.getInstance().getChangedProperty());
        assertTrue(POJOCommandTestHelper.getInstance().isExecuted());
    }

    public void testPojoWithoutPropertiesCommandImpl() throws Exception {
        Mediator pcm = MediatorFactoryFinder.getInstance().getMediator(createOMElement(
                "<pojoCommand name='org.apache.synapse.mediators.ext.POJOCommandTestImplementedMediator' " +
                        "xmlns='http://ws.apache.org/ns/synapse'/>"), new Properties());
        POJOCommandTestHelper.reset();
        pcm.mediate(new TestMessageContext());
        assertTrue(POJOCommandTestHelper.getInstance().isExecuted());
    }

    public void testPojoWithPropertiesCommandImpl() throws Exception {
        Mediator pcm = MediatorFactoryFinder.getInstance().getMediator(createOMElement(
                "<pojoCommand name='org.apache.synapse.mediators.ext.POJOCommandTestImplementedMediator' " +
                        "xmlns='http://ws.apache.org/ns/synapse'><property name=\"testProp\" " +
                        "expression=\"fn:concat('XPATH ', 'FUNC')\" action=\"ReadMessage\"/></pojoCommand>"), new Properties());
        POJOCommandTestHelper.reset();
        pcm.mediate(new TestMessageContext());
        assertEquals("XPATH FUNC", POJOCommandTestHelper.getInstance().getChangedProperty());
        assertTrue(POJOCommandTestHelper.getInstance().isExecuted());
    }

    public void testPojoWithStaticPropertiesCommandImpl() throws Exception {
        Mediator pcm = MediatorFactoryFinder.getInstance().getMediator(createOMElement(
                "<pojoCommand name='org.apache.synapse.mediators.ext.POJOCommandTestImplementedMediator' " +
                        "xmlns='http://ws.apache.org/ns/synapse'><property name=\"testProp\" " +
                        "value=\"Test Property\"/></pojoCommand>"), new Properties());
        POJOCommandTestHelper.reset();
        pcm.mediate(new TestMessageContext());
        assertEquals("Test Property", POJOCommandTestHelper.getInstance().getChangedProperty());
        assertTrue(POJOCommandTestHelper.getInstance().isExecuted());
    }

    public void testPojoWithContextPropertiesCommandImpl() throws Exception {
        Mediator pcm = MediatorFactoryFinder.getInstance().getMediator(createOMElement(
                "<pojoCommand name='org.apache.synapse.mediators.ext.POJOCommandTestImplementedMediator' " +
                        "xmlns='http://ws.apache.org/ns/synapse'><property name=\"testProp\" " +
                        "value=\"Test Property\" context-name=\"testPropInMC\"/></pojoCommand>"), new Properties());
        POJOCommandTestHelper.reset();
        MessageContext ctx = new TestMessageContext();
        pcm.mediate(ctx);
        assertEquals("Test Property", POJOCommandTestHelper.getInstance().getChangedProperty());
        assertEquals("Test Property", ctx.getProperty("testPropInMC"));
        assertTrue(POJOCommandTestHelper.getInstance().isExecuted());
    }

    public void testPojoWithMessagePropertiesCommandImpl() throws Exception {
        Mediator pcm = MediatorFactoryFinder.getInstance().getMediator(createOMElement(
                "<pojoCommand name='org.apache.synapse.mediators.ext.POJOCommandTestImplementedMediator' " +
                        "xmlns='http://ws.apache.org/ns/synapse'><property name=\"testProp\" " +
                        "value=\"TestProperty\" expression=\"//testNode\"/></pojoCommand>"), new Properties());
        POJOCommandTestHelper.reset();
        MessageContext ctx = new TestMessageContext();
        SOAPEnvelope envelope = OMAbstractFactory.getSOAP11Factory().getDefaultEnvelope();
        envelope.getBody().addChild(createOMElement("<original><testNode/></original>"));
        ctx.setEnvelope(envelope);
        pcm.mediate(ctx);
        assertEquals("TestProperty", POJOCommandTestHelper.getInstance().getChangedProperty());
        assertEquals("<original>TestProperty</original>", ctx.getEnvelope().getBody().getFirstOMChild().toString());
        assertTrue(POJOCommandTestHelper.getInstance().isExecuted());
    }

    public void testPojoWithContextR$UPropertiesCommandImpl() throws Exception {
        Mediator pcm = MediatorFactoryFinder.getInstance().getMediator(createOMElement(
                "<pojoCommand name='org.apache.synapse.mediators.ext.POJOCommandTestImplementedMediator' " +
                        "xmlns='http://ws.apache.org/ns/synapse'><property name=\"ctxTest\" " +
                        "context-name=\"testCtxProp\" action=\"ReadAndUpdateContext\"/></pojoCommand>"), new Properties());
        POJOCommandTestHelper.reset();
        MessageContext ctx = new TestMessageContext();
        ctx.setProperty("testCtxProp", "test");
        pcm.mediate(ctx);
        assertEquals("testcommand", ctx.getProperty("testCtxProp").toString());
        assertTrue(POJOCommandTestHelper.getInstance().isExecuted());
    }
}
