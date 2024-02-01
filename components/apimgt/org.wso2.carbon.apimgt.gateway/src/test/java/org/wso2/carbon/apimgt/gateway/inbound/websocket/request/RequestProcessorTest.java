/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.t
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.gateway.inbound.websocket.request;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.gateway.handlers.WebsocketUtil;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.apimgt.gateway.inbound.InboundMessageContext;
import org.wso2.carbon.apimgt.gateway.inbound.websocket.InboundProcessorResponseDTO;
import org.wso2.carbon.apimgt.gateway.inbound.websocket.utils.InboundWebsocketProcessorUtil;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

/**
 * Test class for RequestProcessor.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ InboundWebsocketProcessorUtil.class, ServiceReferenceHolder.class, APIUtil.class, WebsocketUtil.class})
public class RequestProcessorTest {
    private RequestProcessor requestProcessor;
    private int msgSize = 22;
    private String msgText = "This is a test message";
    private InboundMessageContext inboundMessageContext;

    @Before
    public void setup() throws Exception {
        requestProcessor = new RequestProcessor();
        PowerMockito.mockStatic(InboundWebsocketProcessorUtil.class);
        inboundMessageContext = Mockito.mock(InboundMessageContext.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        PowerMockito.when(serviceReferenceHolder.getAPIManagerConfiguration()).thenReturn(apiManagerConfiguration);

        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.when(APIUtil.getOAuthConfigurationFromAPIMConfig(Mockito.anyString())).thenReturn("");

        PowerMockito.mockStatic(WebsocketUtil.class);
    }

    @Test
    public void testHandleRequest() throws APISecurityException {
        InboundProcessorResponseDTO responseDTO = new InboundProcessorResponseDTO();
        PowerMockito.when(InboundWebsocketProcessorUtil.authenticateToken(inboundMessageContext))
                .thenReturn(responseDTO);
        PowerMockito.when(InboundWebsocketProcessorUtil.doThrottle(msgSize, null, inboundMessageContext, responseDTO))
                .thenReturn(responseDTO);
        PowerMockito.when(WebsocketUtil.validateDenyPolicies(Mockito.anyObject())).thenReturn(responseDTO);
        responseDTO = requestProcessor.handleRequest(msgSize, msgText, inboundMessageContext);
        Assert.assertFalse(responseDTO.isError());
    }

    @Test
    public void testHandleRequestAuthenticationTokenError() throws APISecurityException {
        InboundProcessorResponseDTO errorResponseDTO = new InboundProcessorResponseDTO();
        errorResponseDTO.setError(true);
        PowerMockito.when(InboundWebsocketProcessorUtil.authenticateToken(inboundMessageContext))
                .thenReturn(errorResponseDTO);
        InboundProcessorResponseDTO responseDTO = requestProcessor.handleRequest(msgSize, msgText,
                inboundMessageContext);
        Assert.assertTrue(responseDTO.isError());
    }

    @Test
    public void testHandleRequestDoThrottleError() throws APISecurityException {
        InboundProcessorResponseDTO responseDTO = new InboundProcessorResponseDTO();
        InboundProcessorResponseDTO errorResponseDTO = new InboundProcessorResponseDTO();
        errorResponseDTO.setError(true);
        PowerMockito.when(InboundWebsocketProcessorUtil.authenticateToken(inboundMessageContext))
                .thenReturn(responseDTO);
        PowerMockito.when(InboundWebsocketProcessorUtil.doThrottle(msgSize, null, inboundMessageContext, responseDTO))
                .thenReturn(errorResponseDTO);
        PowerMockito.when(WebsocketUtil.validateDenyPolicies(Mockito.anyObject())).thenReturn(responseDTO);
        responseDTO = requestProcessor.handleRequest(msgSize, msgText, inboundMessageContext);
        Assert.assertTrue(responseDTO.isError());
    }
}
