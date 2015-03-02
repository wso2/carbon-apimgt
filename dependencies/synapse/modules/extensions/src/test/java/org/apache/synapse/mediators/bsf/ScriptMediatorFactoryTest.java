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

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.xml.stream.XMLStreamException;

import junit.framework.TestCase;

import org.apache.axiom.om.OMElement;
import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.Entry;
import org.apache.synapse.mediators.TestUtils;

public class ScriptMediatorFactoryTest extends TestCase {

    private static final OMElement INLINE_MEDIATOR_CONFIG = TestUtils.createOMElement(
       "<script language='js'>true</script>");

    private static final OMElement REG_PROP_MEDIATOR_CONFIG = TestUtils.createOMElement(
       "<script language='js' key='MyMediator'/>");
    
    private static final OMElement REG_PROP_FOO_FUNC_MEDIATOR_CONFIG = TestUtils.createOMElement(
       "<script language='js' key='MyFooMediator' function='foo'/>");

    private static final OMElement MY_MEDIATOR = TestUtils.createOMElement(
       "<x><![CDATA[ function mediate(mc) { return true;} ]]></x>");

    private static final OMElement MY_MEDIATOR_FOO_FUNC = TestUtils.createOMElement(
       "<x><![CDATA[ function foo(mc) { return true;} ]]></x>");

    public void testInlineScriptMediatorFactory() throws XMLStreamException {
        ScriptMediatorFactory mf = new ScriptMediatorFactory();
        Mediator mediator = mf.createMediator(INLINE_MEDIATOR_CONFIG, new Properties());
        try{
            MessageContext mc = TestUtils.getTestContext("<foo/>",null);
            assertTrue(mediator.mediate(mc));
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void testRegPropMediatorFactory() throws Exception {

        Entry prop = new Entry();
        prop.setKey("MyMediator");
        prop.setValue(MY_MEDIATOR);
        Map<String,Entry> props = new HashMap<String,Entry>();
        props.put("MyMediator", prop);
        MessageContext mc = TestUtils.getTestContext("<foo/>", props);

        ScriptMediatorFactory mf = new ScriptMediatorFactory();
        Mediator mediator = mf.createMediator(REG_PROP_MEDIATOR_CONFIG, new Properties());
        assertTrue(mediator.mediate(mc));
    }

    public void testRegPropWithFunctionMediatorFactory() throws Exception {
        Entry prop = new Entry();
        prop.setValue(MY_MEDIATOR_FOO_FUNC);
        Map<String,Entry> props = new HashMap<String,Entry>();
        props.put("MyFooMediator", prop);
        MessageContext mc = TestUtils.getTestContext("<foo/>", props);

        ScriptMediatorFactory mf = new ScriptMediatorFactory();
        Mediator mediator = mf.createMediator(REG_PROP_FOO_FUNC_MEDIATOR_CONFIG, new Properties());
        assertTrue(mediator.mediate(mc));
    }

}
