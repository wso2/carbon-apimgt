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

package org.apache.synapse.eventing;

import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.databinding.utils.ConverterUtil;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.eventing.managers.DefaultInMemorySubscriptionManager;
import org.apache.synapse.mediators.TestUtils;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2SynapseEnvironment;
import org.wso2.eventing.SubscriptionManager;
import org.wso2.eventing.EventingConstants;
import org.wso2.eventing.exceptions.EventException;
import junit.framework.TestCase;

import javax.xml.namespace.QName;
import java.util.Date;
import java.util.Calendar;

public class SynapseEventSourceTest extends TestCase {

    private SubscriptionManager subMan;
    private SynapseEventSource source;
    private String id;

    private static final String SUB_MAN_URL = "http://synapse.test.com/eventing/subscriptions";
    private static final String ADDR_URL = "http://www.other.example.com/OnStormWarning";
    private static final String FILTER_DIALECT = "http://www.example.org/topicFilter";
    private static final String FILTER = "weather.storms";

    protected void setUp() throws Exception {
        source = new SynapseEventSource("foo");
        subMan = new DefaultInMemorySubscriptionManager();
        source.setSubscriptionManager(subMan);
    }

    public void testSubscriptionHandling() {
        subscribeTest();
        renewTest();
        unsubscribeTest();
    }

    private void subscribeTest() {
        Date date = new Date(System.currentTimeMillis() + 3600000);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        String message =
                "<wse:Subscribe xmlns:wse=\"http://schemas.xmlsoap.org/ws/2004/08/eventing\" " +
                "   xmlns:wsa=\"http://schemas.xmlsoap.org/ws/2004/08/addressing\" " +
                "   xmlns:ew=\"http://www.example.com/warnings\">\n" +
                "   <wse:EndTo>\n" +
                "       <wsa:Address>http://www.example.com/MyEventSink</wsa:Address>\n" +
                "         <wsa:ReferenceProperties>\n" +
                "             <ew:MySubscription>2597</ew:MySubscription>\n" +
                "         </wsa:ReferenceProperties>\n" +
                "   </wse:EndTo>\n" +
                "   <wse:Delivery>\n" +
                "       <wse:NotifyTo>\n" +
                "         <wsa:Address>" + ADDR_URL + "</wsa:Address>\n" +
                "         <wsa:ReferenceProperties>\n" +
                "             <ew:MySubscription>2597</ew:MySubscription>\n" +
                "         </wsa:ReferenceProperties>\n" +
                "       </wse:NotifyTo>\n" +
                "    </wse:Delivery>\n" +
                "    <wse:Expires>" + ConverterUtil.convertToString(cal) + "</wse:Expires>\n" +
                "    <wse:Filter xmlns:ow=\"http://www.example.org/oceanwatch\"\n" +
                "              Dialect=\"" + FILTER_DIALECT + "\" >" + FILTER +"</wse:Filter>\n" +
                "</wse:Subscribe>";

        try {
            MessageContext msgCtx = createMessageContext(message, EventingConstants.WSE_SUBSCRIBE);
            source.receive(msgCtx);
        } catch (Exception ignored) {

        }

        try {
            assertEquals(1, subMan.getSubscriptions().size());
            SynapseSubscription s = (SynapseSubscription) subMan.getSubscriptions().get(0);
            assertEquals(SUB_MAN_URL, s.getSubManUrl());
            assertEquals(ADDR_URL, s.getAddressUrl());
            assertEquals(FILTER_DIALECT, s.getFilterDialect());
            assertEquals(FILTER, s.getFilterValue());
            assertEquals(date, s.getExpires().getTime());
            id = s.getId();
        } catch (EventException e) {
            fail("Eventing exception occured while accessing the subscription manager");
        }
    }

    public void renewTest() {
        Date date = new Date(System.currentTimeMillis() + 3600000 * 2);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        String message =
                "<wse:Renew xmlns:wse=\"http://schemas.xmlsoap.org/ws/2004/08/eventing\">\n" +
                "   <wse:Expires>" + ConverterUtil.convertToString(cal) + "</wse:Expires>\n" +
                "</wse:Renew>";

        try {
            MessageContext msgCtx = createMessageContext(message, EventingConstants.WSE_RENEW);
            QName qname = new QName(EventingConstants.WSE_EVENTING_NS,
                    EventingConstants.WSE_EN_IDENTIFIER, "wse");
            TestUtils.addSOAPHeaderBlock(msgCtx, qname, id);
            source.receive(msgCtx);
        } catch (Exception ignored) {

        }

        try {
            assertEquals(1, subMan.getSubscriptions().size());
            SynapseSubscription s = (SynapseSubscription) subMan.getSubscription(this.id);
            assertEquals(SUB_MAN_URL, s.getSubManUrl());
            assertEquals(ADDR_URL, s.getAddressUrl());
            assertEquals(FILTER_DIALECT, s.getFilterDialect());
            assertEquals(FILTER, s.getFilterValue());
            assertEquals(date, s.getExpires().getTime());
        } catch (EventException e) {
            fail("Eventing exception occured while accessing the subscription manager");
        }

    }

    public void unsubscribeTest() {
        String message =
                "<wse:Unsubscribe xmlns:wse=\"http://schemas.xmlsoap.org/ws/2004/08/eventing\"/>";

        try {
            MessageContext msgCtx = createMessageContext(message, EventingConstants.WSE_UNSUBSCRIBE);
            QName qname = new QName(EventingConstants.WSE_EVENTING_NS,
                    EventingConstants.WSE_EN_IDENTIFIER, "wse");
            TestUtils.addSOAPHeaderBlock(msgCtx, qname, id);
            source.receive(msgCtx);
        } catch (Exception ignored) {

        }

        try {
            assertEquals(0, subMan.getSubscriptions().size());
        } catch (EventException e) {
            fail("Eventing exception occured while accessing the subscription manager");
        }

    }

    private MessageContext createMessageContext(String payload, String action) {
        try {
            SynapseConfiguration synapseConfig = new SynapseConfiguration();
            AxisConfiguration axisConfig = new AxisConfiguration();
            synapseConfig.setAxisConfiguration(axisConfig);
            ConfigurationContext cfgCtx = new ConfigurationContext(axisConfig);
            SynapseEnvironment env = new Axis2SynapseEnvironment(cfgCtx, synapseConfig);
            axisConfig.addParameter(SynapseConstants.SYNAPSE_CONFIG, synapseConfig);
            axisConfig.addParameter(SynapseConstants.SYNAPSE_ENV, env);

            MessageContext msgCtx = TestUtils.getAxis2MessageContext(payload, null).
                    getAxis2MessageContext();
            msgCtx.setConfigurationContext(cfgCtx);
            msgCtx.setTo(new EndpointReference(SUB_MAN_URL));
            msgCtx.setWSAAction(action);
            return msgCtx;
        } catch (Exception e) {
            fail();
        }
        return null;
    }
}
