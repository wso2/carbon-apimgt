/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.apimgt.gateway.handlers.analytics;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.http.HttpHeaders;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.Entry;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.usage.publisher.APIMgtUsagePublisherConstants;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.util.TreeMap;

import static junit.framework.Assert.fail;

/**
 * Test class for APIMgtGoogleAnalyticsTrackingHandler
 */
public class APIMgtGoogleAnalyticsTrackingHandlerTestCase {

    @Test
    public void testHandleRequest() {
        MessageContext msgCtx1 = Mockito.mock(MessageContext.class);
        APIMgtGoogleAnalyticsTrackingHandler apiMgtGoogleAnalyticsTrackingHandler
                = new APIMgtGoogleAnalyticsTrackingHandler();
        try {
            apiMgtGoogleAnalyticsTrackingHandler.handleRequest(msgCtx1);
        } catch (Exception e) {
//             test for exception, hence ignoring
        }
        MessageContext msgCtx = Mockito.mock(MessageContext.class);
        SynapseConfiguration synapseConfiguration = Mockito.mock(SynapseConfiguration.class);
        Mockito.when(msgCtx.getConfiguration()).thenReturn(synapseConfiguration);
        Mockito.when(synapseConfiguration.getEntryDefinition("abc")).thenReturn(null);

        apiMgtGoogleAnalyticsTrackingHandler.setConfigKey("abc");
        //test when entry is null
        Assert.assertTrue(apiMgtGoogleAnalyticsTrackingHandler.handleRequest(msgCtx));


        OMElement entryvalue = Mockito.mock(OMElement.class);
        try {
            OMElement documentElement = AXIOMUtil.stringToOM("<GoogleAnalyticsTracking>\n" +
                    "\t<!--Enable/Disable Google Analytics Tracking -->\n" +
                    "\t<Enabled>false</Enabled>\n" +
                    "\n" +
                    "\t<!-- Google Analytics Tracking ID -->\n" +
                    "\t<TrackingID>UA-XXXXXXXX-X</TrackingID>\n" +
                    "\n" +
                    "</GoogleAnalyticsTracking>");
            Mockito.when(msgCtx.getEntry("abc")).thenReturn(documentElement);

            //test when entry is not null
            Entry entry = new Entry();
            Mockito.when(synapseConfiguration.getEntryDefinition("abc")).thenReturn(entry);
            Assert.assertTrue(apiMgtGoogleAnalyticsTrackingHandler.handleRequest(msgCtx));

        } catch (XMLStreamException e) {
            fail(e.getMessage());
        }


        //test when config.enable = true
        OMElement documentElement = null;
        try {
            documentElement = AXIOMUtil.stringToOM("<GoogleAnalyticsTracking>\n" +
                    "\t<!--Enable/Disable Google Analytics Tracking -->\n" +
                    "\t<Enabled>true</Enabled>\n" +
                    "\n" +
                    "\t<!-- Google Analytics Tracking ID -->\n" +
                    "\t<TrackingID>UA-XXXXXXXX-X</TrackingID>\n" +
                    "\n" +
                    "</GoogleAnalyticsTracking>");
            Mockito.when(msgCtx.getEntry("abc")).thenReturn(documentElement);

            Entry entry = new Entry();
            Mockito.when(synapseConfiguration.getEntryDefinition("abc")).thenReturn(entry);
            //test when entry is dynamic and version == 1
            entry.setType(3);
            entry.setVersion(1);
            Assert.assertTrue(apiMgtGoogleAnalyticsTrackingHandler.handleRequest(msgCtx));

        } catch (XMLStreamException e) {
            fail(e.getMessage());
        }
        Mockito.when(entryvalue.getFirstChildWithName(new QName(
                APIMgtUsagePublisherConstants.API_GOOGLE_ANALYTICS_TRACKING_ENABLED))).thenReturn(entryvalue);
        Mockito.when(entryvalue.getText()).thenReturn("true");
        // Test exception thrown from trackPageView

        MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
        Mockito.when(messageContext.getConfiguration()).thenReturn(synapseConfiguration);

        TreeMap transportHeaders = new TreeMap();
        transportHeaders.put(APIConstants.USER_AGENT, "");
        transportHeaders.put(APIMgtGatewayConstants.AUTHORIZATION, "gsu64r874tcin7ry8oe");
        org.apache.axis2.context.MessageContext axis2MsgCntxt = Mockito.mock(org.apache.axis2.context.MessageContext.class);
        Mockito.when(axis2MsgCntxt.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS)).thenReturn(transportHeaders);
        Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgCntxt);
        OMElement entryvalue1 = Mockito.mock(OMElement.class);
        Mockito.when(msgCtx.getEntry("abc")).thenReturn(entryvalue1);

        Assert.assertTrue(apiMgtGoogleAnalyticsTrackingHandler.handleRequest(messageContext));

        //test when HOST and X_FORWARDED_FOR_HEADER are set
        transportHeaders.put(HttpHeaders.HOST, "localhost:8080");
        transportHeaders.put(APIMgtUsagePublisherConstants.X_FORWARDED_FOR_HEADER, "192.168.0.34");
        Assert.assertTrue(apiMgtGoogleAnalyticsTrackingHandler.handleRequest(messageContext));

    }
}
