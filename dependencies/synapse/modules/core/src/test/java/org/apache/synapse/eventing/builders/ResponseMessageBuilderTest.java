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

package org.apache.synapse.eventing.builders;

import org.apache.synapse.eventing.SynapseSubscription;
import org.apache.synapse.mediators.TestUtils;
import org.apache.synapse.config.xml.AbstractTestCase;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.databinding.utils.ConverterUtil;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.util.UIDGenerator;

import java.util.Date;
import java.util.Calendar;

public class ResponseMessageBuilderTest extends AbstractTestCase {

    public void testSubscriptionResponse() {
        String id = UIDGenerator.generateURNString();
        String addressUrl = "http://synapse.test.com/eventing/sunscriptions";

        SynapseSubscription sub = new SynapseSubscription();
        sub.setId(id);
        sub.setSubManUrl(addressUrl);

        String expected =
                "<wse:SubscribeResponse xmlns:wse=\"http://schemas.xmlsoap.org/ws/2004/08/eventing\">" +
                "<wse:SubscriptionManager>" +
                "<wsa:Address xmlns:wsa=\"http://schemas.xmlsoap.org/ws/2004/08/addressing\">" + addressUrl + "</wsa:Address>" +
                "<wsa:ReferenceParameters xmlns:wsa=\"http://schemas.xmlsoap.org/ws/2004/08/addressing\">" +
                "<wse:Identifier>" + id + "</wse:Identifier>" +
                "</wsa:ReferenceParameters>" +
                "</wse:SubscriptionManager>" +
                "</wse:SubscribeResponse>";

        try {
            MessageContext msgCtx = TestUtils.getAxis2MessageContext("<empty/>", null).
                    getAxis2MessageContext();
            ResponseMessageBuilder builder = new ResponseMessageBuilder(msgCtx);
            SOAPEnvelope env = builder.genSubscriptionResponse(sub);
            OMElement resultOm = env.getBody().getFirstElement();
            OMElement expectedOm = AXIOMUtil.stringToOM(expected);
            assertTrue(compare(expectedOm, resultOm));

        } catch (Exception e) {
            fail("Error while constructing the test message context: " + e.getMessage());
        }
    }

    public void testUnsubscriptionResponse() {
        String id = UIDGenerator.generateURNString();
        String addressUrl = "http://synapse.test.com/eventing/sunscriptions";

        SynapseSubscription sub = new SynapseSubscription();
        sub.setId(id);
        sub.setSubManUrl(addressUrl);

        String expected =
                "<wse:UnsubscribeResponse xmlns:wse=\"http://schemas.xmlsoap.org/ws/2004/08/eventing\"/>";

        try {
            MessageContext msgCtx = TestUtils.getAxis2MessageContext("<empty/>", null).
                    getAxis2MessageContext();
            ResponseMessageBuilder builder = new ResponseMessageBuilder(msgCtx);
            SOAPEnvelope env = builder.genUnSubscribeResponse(sub);
            OMElement resultOm = env.getBody().getFirstElement();
            OMElement expectedOm = AXIOMUtil.stringToOM(expected);
            assertTrue(compare(expectedOm, resultOm));

        } catch (Exception e) {
            fail("Error while constructing the test message context: " + e.getMessage());
        }
    }

    public void testRenewResponse() {
        String id = UIDGenerator.generateURNString();
        String addressUrl = "http://synapse.test.com/eventing/sunscriptions";
        Date date = new Date(System.currentTimeMillis() + 3600000);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        SynapseSubscription sub = new SynapseSubscription();
        sub.setId(id);
        sub.setSubManUrl(addressUrl);
        sub.setExpires(cal);

        String expected =
                "<wse:RenewResponse xmlns:wse=\"http://schemas.xmlsoap.org/ws/2004/08/eventing\">" +
                        "<wse:Expires>" + ConverterUtil.convertToString(cal) + "</wse:Expires>" +
                        "</wse:RenewResponse>";

        try {
            MessageContext msgCtx = TestUtils.getAxis2MessageContext("<empty/>", null).
                    getAxis2MessageContext();
            ResponseMessageBuilder builder = new ResponseMessageBuilder(msgCtx);
            SOAPEnvelope env = builder.genRenewSubscriptionResponse(sub);
            OMElement resultOm = env.getBody().getFirstElement();
            OMElement expectedOm = AXIOMUtil.stringToOM(expected);
            assertTrue(compare(expectedOm, resultOm));

        } catch (Exception e) {
            fail("Error while constructing the test message context: " + e.getMessage());
        }
    }

    public void testGetStatusResponse() {
        String id = UIDGenerator.generateURNString();
        String addressUrl = "http://synapse.test.com/eventing/sunscriptions";
        Date date = new Date(System.currentTimeMillis() + 3600000);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        SynapseSubscription sub = new SynapseSubscription();
        sub.setId(id);
        sub.setSubManUrl(addressUrl);
        sub.setExpires(cal);

        String expected =
                "<wse:GetStatusResponse xmlns:wse=\"http://schemas.xmlsoap.org/ws/2004/08/eventing\">" +
                        "<wse:Expires>" + ConverterUtil.convertToString(cal) + "</wse:Expires>" +
                        "</wse:GetStatusResponse>";

        try {
            MessageContext msgCtx = TestUtils.getAxis2MessageContext("<empty/>", null).
                    getAxis2MessageContext();
            ResponseMessageBuilder builder = new ResponseMessageBuilder(msgCtx);
            SOAPEnvelope env = builder.genGetStatusResponse(sub);
            OMElement resultOm = env.getBody().getFirstElement();
            OMElement expectedOm = AXIOMUtil.stringToOM(expected);
            assertTrue(compare(expectedOm, resultOm));

        } catch (Exception e) {
            fail("Error while constructing the test message context: " + e.getMessage());
        }
    }
}
