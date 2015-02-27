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

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.util.UUIDGenerator;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.Mediator;
import org.apache.synapse.SynapseException;
import org.apache.synapse.TestMessageContext;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.config.xml.MediatorFactoryFinder;
import org.apache.synapse.core.axis2.Axis2SynapseEnvironment;
import org.apache.synapse.mediators.AbstractMediatorTestCase;

import javax.xml.namespace.QName;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

/**
 * Tests the class mediator instantiation and setting of literal and
 * XPath parameters at runtime.
 */
public class ClassMediatorTest extends AbstractMediatorTestCase {

    private static OMFactory fac = OMAbstractFactory.getOMFactory();

    private static final OMNamespace ns = fac.createOMNamespace("http://www.w3.org/2005/Atom", "");

    private DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS");
    private DateFormat inputFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm");

    public void testMediationWithoutProperties() throws Exception {
        Mediator cm = MediatorFactoryFinder.getInstance().getMediator(createOMElement(
                "<class name='org.apache.synapse.mediators.ext.ClassMediatorTestMediator' " +
                        "xmlns='http://ws.apache.org/ns/synapse'/>"), new Properties());
        cm.mediate(new TestMessageContext());
        assertTrue(ClassMediatorTestMediator.invoked);
    }

    public void testMediationWithLiteralProperties() throws Exception {
        Mediator cm = MediatorFactoryFinder.getInstance().getMediator(createOMElement(
                "<class name='org.apache.synapse.mediators.ext.ClassMediatorTestMediator' " +
                        "xmlns='http://ws.apache.org/ns/synapse'><property name='testProp' value='testValue'/></class>"), new Properties());
        cm.mediate(new TestMessageContext());
        assertTrue(ClassMediatorTestMediator.invoked);
        assertTrue(ClassMediatorTestMediator.testProp.equals("testValue"));
    }

    public void testInitializationAndMedition() throws Exception {
        Mediator cm = MediatorFactoryFinder.getInstance().getMediator(createOMElement(
                "<class name='org.apache.synapse.mediators.ext.ClassMediatorTestMediator' " +
                        "xmlns='http://ws.apache.org/ns/synapse'/>"), new Properties());
        ((ManagedLifecycle) cm).init(new Axis2SynapseEnvironment(new SynapseConfiguration()));
        assertTrue(ClassMediatorTestMediator.initialized);
        cm.mediate(new TestMessageContext());
        assertTrue(ClassMediatorTestMediator.invoked);
    }

    public void testDestroy() throws Exception {
        Mediator cm = MediatorFactoryFinder.getInstance().getMediator(createOMElement(
                "<class name='org.apache.synapse.mediators.ext.ClassMediatorTestMediator' " +
                        "xmlns='http://ws.apache.org/ns/synapse'/>"), new Properties());
        cm.mediate(new TestMessageContext());
        assertTrue(ClassMediatorTestMediator.invoked);
        ((ManagedLifecycle) cm).destroy();
        assertTrue(ClassMediatorTestMediator.destroyed);
    }

//    public void testCreationWithXPathProperties() throws Exception {
//        ClassMediator cm = new ClassMediator();
//        MediatorProperty mp = new MediatorProperty();
//        mp.setName("testProp");
//        mp.setExpression(new SynapseXPath("concat('XPath ','is ','FUN!')"));
//        cm.addProperty(mp);
//        cm.setClazz(ClassMediatorTestMediator.class);
//        cm.mediate(new TestMessageContext());
//        assertTrue(ClassMediatorTestMediator.testProp.equals("XPath is FUN!"));
//    }

}
