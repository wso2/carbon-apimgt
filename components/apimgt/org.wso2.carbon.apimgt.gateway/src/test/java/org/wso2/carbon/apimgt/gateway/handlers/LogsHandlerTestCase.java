/*
 *   Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.gateway.handlers;

import org.apache.logging.log4j.ThreadContext;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.gateway.APILoggerManager;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.impl.APIConstants;

import java.util.TreeMap;
import java.util.UUID;

@RunWith(PowerMockRunner.class)
@PrepareForTest({LogUtils.class, ThreadContext.class, System.class, LogsHandler.class})
public class LogsHandlerTestCase {

    @Before
    public void init() {
        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.mockStatic(ThreadContext.class);
        System.setProperty(APIConstants.ENABLE_CORRELATION_LOGS, "true");
    }

    @Test
    public void testHandleRequestInFlow() throws Exception {
        LogsHandler logsHandler = PowerMockito.spy(new LogsHandler());
        MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
        PowerMockito.when(LogUtils.getTo(messageContext)).thenReturn("pizzashack/1.0.0/menu");
        PowerMockito.doReturn(null).when(logsHandler, "getAPILogLevel", messageContext);
        Assert.assertTrue(logsHandler.handleRequestInFlow(messageContext));
    }

    @Test
    public void testHandleRequestOutFlow() {
        MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
        org.apache.axis2.context.MessageContext axis2MC =
                Mockito.mock(org.apache.axis2.context.MessageContext.class);
        Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MC);

        // For websocket APIs
        Mockito.when(axis2MC.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS)).thenReturn(null);
        PowerMockito.when(LogUtils.getTransportHeaders(messageContext)).thenReturn(null);
        PowerMockito.when(ThreadContext.get(APIConstants.CORRELATION_ID)).thenReturn(null);
        PowerMockito.when(messageContext.getMessageID()).thenReturn("urn:uuid:"+ UUID.randomUUID().toString());
        PowerMockito.when(LogUtils.getResourceCacheKey(messageContext)).thenReturn(null);

        LogsHandler logsHandler = new LogsHandler();
        Assert.assertTrue(logsHandler.handleRequestOutFlow(messageContext));

        // For normal APIs
        TreeMap<String, String> treeMap = new TreeMap<>();
        // since the all requested header values in the code was not available in normal rest API header object,
        // did not add any values to the map

        PowerMockito.when(axis2MC.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS))
                .thenReturn(treeMap);
        PowerMockito.when(LogUtils.getTransportHeaders(messageContext)).thenReturn(treeMap);
        String uuid = UUID.randomUUID().toString();
        PowerMockito.when(ThreadContext.get(APIConstants.CORRELATION_ID)).thenReturn(uuid);
        PowerMockito.when(messageContext.getMessageID()).thenReturn("urn:uuid:"+uuid);
        PowerMockito.when(LogUtils.getResourceCacheKey(messageContext)).thenReturn("/pizzashack/1.0.0/1.0.0/menu:GET");

        Assert.assertTrue(logsHandler.handleRequestOutFlow(messageContext));
    }

    @Test
    public void testHandleResponseInFlow() {
        MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);

        // For non-default APIs
        PowerMockito.when(messageContext.getProperty("DefaultAPI")).thenReturn(null);

        messageContext.setProperty(APIMgtGatewayConstants.REQUEST_EXECUTION_START_TIME,
                String.valueOf(System.currentTimeMillis() - 60000));
        messageContext.setProperty(APIMgtGatewayConstants.BACKEND_REQUEST_START_TIME,
                (String.valueOf(System.currentTimeMillis() - 60000)));
        org.apache.axis2.context.MessageContext axis2MC =
                Mockito.mock(org.apache.axis2.context.MessageContext.class);
        Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MC);

        TreeMap<String, String> treeMap = new TreeMap<>();
        treeMap.put("Connection", "keep-alive");
        treeMap.put("Content-Type", "application/json");
        treeMap.put("Keep-Alive", "timeout=60");
        treeMap.put("Server", "WSO2 Carbon Server");
        treeMap.put("Transfer-Encoding", "chunked");
        PowerMockito.when(axis2MC.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS))
                .thenReturn(treeMap);
        messageContext.setProperty("CORRELATION_ID_HEADER", UUID.randomUUID().toString());

        PowerMockito.when(LogUtils.getAPIName(messageContext)).thenReturn("PizzaShackAPI:v1.0.0");
        PowerMockito.when(LogUtils.getAPICtx(messageContext)).thenReturn("/pizzashack/1.0.0");
        PowerMockito.when(LogUtils.getRestMethod(messageContext)).thenReturn(null);
        PowerMockito.when(LogUtils.getElectedResource(messageContext)).thenReturn("/menu");
        PowerMockito.when(LogUtils.getRestReqFullPath(messageContext)).thenReturn("/pizzashack/1.0.0/menu");
        PowerMockito.when(LogUtils.getTo(messageContext)).thenReturn("pizzashack/1.0.0/menu");
        PowerMockito.when(LogUtils.getRestHttpResponseStatusCode(messageContext)).thenReturn("200");
        PowerMockito.when(LogUtils.getApplicationName(messageContext)).thenReturn("SampleApplication");
        PowerMockito.when(LogUtils.getConsumerKey(messageContext)).thenReturn(Mockito.anyString());

        LogsHandler logsHandler = new LogsHandler();
        Assert.assertTrue(logsHandler.handleResponseInFlow(messageContext));
    }
}
