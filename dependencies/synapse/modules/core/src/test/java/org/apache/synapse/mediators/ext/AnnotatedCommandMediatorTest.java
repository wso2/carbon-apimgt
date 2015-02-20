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
import org.apache.synapse.config.xml.MediatorFactoryFinder;
import org.apache.synapse.mediators.AbstractMediatorTestCase;
import org.apache.synapse.mediators.TestUtils;
import org.apache.synapse.util.xpath.SynapseXPath;

import java.util.Properties;

/**
 */
public class AnnotatedCommandMediatorTest extends AbstractMediatorTestCase {

    public void testAnnotations() throws Exception {
        AnnotatedCommandMediator m = new AnnotatedCommandMediator();
        m.setCommand(AnnotatedCommand.class);

        assertEquals(2, m.beforeFields.size());
        assertEquals(4, m.afterFields.size());

        assertTrue(m.beforeFields.containsKey(AnnotatedCommand.class.getDeclaredField("beforeField")));
        assertTrue(m.beforeFields.containsKey(AnnotatedCommand.class.getDeclaredField("ReadAndUpdateField")));
        assertTrue(m.afterFields.containsKey(AnnotatedCommand.class.getDeclaredField("afterField")));
        assertTrue(m.afterFields.containsKey(AnnotatedCommand.class.getDeclaredField("ReadAndUpdateField")));
    }

    public void testNamspaces() throws Exception {
        AnnotatedCommandMediator m = new AnnotatedCommandMediator();
        m.setCommand(AnnotatedCommand.class);

        SynapseXPath ax = m.afterFields.get(AnnotatedCommand.class.getDeclaredField("ReadAndUpdateField"));
        assertEquals(1, ax.getNamespaces().size());
        assertEquals("http://myns", ax.getNamespaces().values().iterator().next());
        assertEquals("myns", ax.getNamespaces().keySet().iterator().next());

        ax = m.afterFields.get(AnnotatedCommand.class.getDeclaredField("nsTest1"));
        assertEquals(2, ax.getNamespaces().size());
        assertTrue(ax.getNamespaces().keySet().contains("myns"));
        assertTrue(ax.getNamespaces().keySet().contains("ns"));
        assertTrue(ax.getNamespaces().values().contains("http://myns"));
        assertTrue(ax.getNamespaces().values().contains("http://ns"));

        ax = m.afterFields.get(AnnotatedCommand.class.getDeclaredField("nsTest2"));
        assertEquals(2, ax.getNamespaces().size());
        assertTrue(ax.getNamespaces().keySet().contains("myns"));
        assertTrue(ax.getNamespaces().keySet().contains("xns"));
        assertTrue(ax.getNamespaces().values().contains("http://myns"));
        assertTrue(ax.getNamespaces().values().contains("http://xns"));
    }

    public void testBasicExecute() throws Exception {
        AnnotatedCommandMediator m = new AnnotatedCommandMediator();
        m.setCommand(AnnotatedCommand.class);

        Mediator pcm = MediatorFactoryFinder.getInstance().getMediator(createOMElement(
           "<annotatedCommand name='org.apache.synapse.mediators.ext.AnnotatedCommand2' xmlns='http://ws.apache.org/ns/synapse'/>"), new Properties());

        MessageContext mc = TestUtils.getTestContext("<m:getQuote xmlns:m=\"http://services.samples/xsd\"><m:request><m:symbol>IBM</m:symbol></m:request></m:getQuote>");
        pcm.mediate(mc);
        assertEquals("IBM", AnnotatedCommand2.fieldResult);
        assertEquals("IBM", AnnotatedCommand2.methodResult);
    }
}
