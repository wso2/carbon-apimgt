/*
 *Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *WSO2 Inc. licenses this file to you under the Apache License,
 *Version 2.0 (the "License"); you may not use this file except
 *in compliance with the License.
 *You may obtain a copy of the License at
 *
 *http://www.apache.org/licenses/LICENSE-2.0
 *
 *Unless required by applicable law or agreed to in writing,
 *software distributed under the License is distributed on an
 *"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *KIND, either express or implied.  See the License for the
 *specific language governing permissions and limitations
 *under the License.
 */
package org.wso2.carbon.apimgt.gateway.handlers;

import org.apache.synapse.MessageContext;
import org.apache.synapse.api.ApiUtils;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.RESTConstants;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.keymgt.model.entity.API;
import org.wso2.carbon.inbound.endpoint.protocol.websocket.InboundWebsocketConstants;

import java.util.Set;
import java.util.TreeMap;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ApiUtils.class, Utils.class, GatewayUtils.class})
public class DefaultAPIHandlerTest {

    @Before
    public void init() {
        PowerMockito.mockStatic(ApiUtils.class);
        PowerMockito.mockStatic(Utils.class);
        PowerMockito.mockStatic(GatewayUtils.class);
    }

    @Test
    public void testHandleRequestIn() {
        MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
        org.apache.axis2.context.MessageContext axis2MsgCntxt =
                Mockito.mock(org.apache.axis2.context.MessageContext.class);
        Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgCntxt);
        Mockito.when(axis2MsgCntxt.getProperty(APIConstants.TRANSPORT_URL_IN)).thenReturn("/api1/abc/cde?c=a");
        PowerMockito.when(GatewayUtils.getTenantDomain()).thenReturn("carbon.super");
        PowerMockito.when(ApiUtils.getFullRequestPath(messageContext)).thenReturn("/api1/abc/cde?c=a");
        TreeMap<String, API> apiTreeMap = new TreeMap<>();
        apiTreeMap.put("/api1/abc", new API("1234566", 1, "admin", "API1", "1.0.0", "/api1/abc/1.0.0", null,
                "HTTP", "PUBLISHED", true));
        PowerMockito.when(Utils.getSelectedAPIList("/api1/abc/cde?c=a", "carbon.super")).thenReturn(apiTreeMap);
        Mockito.doNothing().when(axis2MsgCntxt).setProperty(APIConstants.TRANSPORT_URL_IN, "/api1/abc/1.0.0/cde?c=a");
        Set<String> properties = Mockito.mock(Set.class);
        Mockito.when(messageContext.getPropertyKeySet()).thenReturn(properties);
        Mockito.when(properties.remove(RESTConstants.REST_FULL_REQUEST_PATH)).thenReturn(true);
        DefaultAPIHandler defaultAPIHandler = new DefaultAPIHandler();
        Assert.assertTrue(defaultAPIHandler.handleRequestInFlow(messageContext));
        Mockito.verify(axis2MsgCntxt, Mockito.times(1)).setProperty(APIConstants.TRANSPORT_URL_IN, "/api1/abc/1.0" +
                ".0/cde?c=a");
        Mockito.verify(properties, Mockito.times(1)).remove(RESTConstants.REST_FULL_REQUEST_PATH);
    }

    @Test
    public void testHandleRequestInWhenZeroAPIs() {
        MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
        org.apache.axis2.context.MessageContext axis2MsgCntxt =
                Mockito.mock(org.apache.axis2.context.MessageContext.class);
        Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgCntxt);
        Mockito.when(axis2MsgCntxt.getProperty(APIConstants.TRANSPORT_URL_IN)).thenReturn("/api1/abc/cde?c=a");
        PowerMockito.when(GatewayUtils.getTenantDomain()).thenReturn("carbon.super");
        PowerMockito.when(ApiUtils.getFullRequestPath(messageContext)).thenReturn("/api1/abc/cde?c=a");
        TreeMap<String, API> apiTreeMap = new TreeMap<>();
        PowerMockito.when(Utils.getSelectedAPIList("/api1/abc/cde?c=a", "carbon.super")).thenReturn(apiTreeMap);
        DefaultAPIHandler defaultAPIHandler = new DefaultAPIHandler();
        Assert.assertTrue(defaultAPIHandler.handleRequestInFlow(messageContext));
        Mockito.verify(axis2MsgCntxt, Mockito.times(0)).setProperty(APIConstants.TRANSPORT_URL_IN, "/api1/abc/1.0" +
                ".0/cde?c=a");
    }

    @Test
    public void testHandleRequestInWhenAPIISWebSocket() {
        MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
        org.apache.axis2.context.MessageContext axis2MsgCntxt =
                Mockito.mock(org.apache.axis2.context.MessageContext.class);
        Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgCntxt);
        Mockito.when(axis2MsgCntxt.getProperty(APIConstants.TRANSPORT_URL_IN)).thenReturn("/api1/abc/cde?c=a");
        PowerMockito.when(GatewayUtils.getTenantDomain()).thenReturn("carbon.super");
        PowerMockito.when(ApiUtils.getFullRequestPath(messageContext)).thenReturn("/api1/abc/cde?c=a");
        TreeMap<String, API> apiTreeMap = new TreeMap<>();
        apiTreeMap.put("/api1/abc", new API("1234566", 1, "admin", "API1", "1.0.0", "/api1/abc/1.0.0", null,
                "HTTP", "PUBLISHED", true));
        PowerMockito.when(Utils.getSelectedAPIList("/api1/abc/cde?c=a", "carbon.super")).thenReturn(apiTreeMap);
        Mockito.doNothing().when(axis2MsgCntxt).setProperty(APIConstants.TRANSPORT_URL_IN, "/api1/abc/1.0.0/cde?c=a");
        Set<String> properties = Mockito.mock(Set.class);
        Mockito.when(messageContext.getPropertyKeySet()).thenReturn(properties);
        Mockito.when(properties.contains(InboundWebsocketConstants.WEBSOCKET_SUBSCRIBER_PATH)).thenReturn(true);
        Mockito.when(properties.remove(RESTConstants.REST_FULL_REQUEST_PATH)).thenReturn(true);
        DefaultAPIHandler defaultAPIHandler = new DefaultAPIHandler();
        Assert.assertTrue(defaultAPIHandler.handleRequestInFlow(messageContext));
        Mockito.verify(axis2MsgCntxt, Mockito.times(0)).setProperty(APIConstants.TRANSPORT_URL_IN, "/api1/abc/1.0" +
                ".0/cde?c=a");
        Mockito.verify(properties, Mockito.times(0)).remove(RESTConstants.REST_FULL_REQUEST_PATH);
    }
}