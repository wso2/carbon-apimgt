/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.gateway.inbound.websocket.handshake;

import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.subscription.URLMapping;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.apimgt.gateway.handlers.streaming.websocket.WebSocketApiConstants;
import org.wso2.carbon.apimgt.gateway.inbound.InboundMessageContext;
import org.wso2.carbon.apimgt.gateway.inbound.websocket.InboundProcessorResponseDTO;
import org.wso2.carbon.apimgt.gateway.inbound.websocket.utils.InboundWebsocketProcessorUtil;
import org.wso2.carbon.apimgt.keymgt.model.entity.API;

@RunWith(PowerMockRunner.class)
@PrepareForTest({InboundWebsocketProcessorUtil.class})
public class HandshakeProcessorTest {

    @Test
    public void handleSuccessfulHandshake() throws Exception {

        InboundMessageContext inboundMessageContext = new InboundMessageContext();
        URLMapping urlMapping = new URLMapping();
        urlMapping.setHttpMethod("SUBSCRIPTION");
        urlMapping.setThrottlingPolicy("Unlimited");
        urlMapping.setUrlPattern("liftStatusChange");
        org.wso2.carbon.apimgt.keymgt.model.entity.API api = new API();
        api.addResource(urlMapping);
        inboundMessageContext.setElectedAPI(api);
        PowerMockito.mockStatic(InboundWebsocketProcessorUtil.class);
        PowerMockito.when(InboundWebsocketProcessorUtil.isAuthenticated(inboundMessageContext)).thenReturn(true);
        HandshakeProcessor handshakeProcessor = new HandshakeProcessor();
        InboundProcessorResponseDTO inboundProcessorResponseDTO =
                handshakeProcessor.processHandshake(inboundMessageContext);
        Assert.assertFalse(inboundProcessorResponseDTO.isError());
        Assert.assertNull(inboundProcessorResponseDTO.getErrorMessage());
        Assert.assertFalse(inboundProcessorResponseDTO.isCloseConnection());
    }

    @Test
    public void handleFailedAuthentication() throws Exception {

        InboundMessageContext inboundMessageContext = new InboundMessageContext();
        PowerMockito.mockStatic(InboundWebsocketProcessorUtil.class);
        PowerMockito.when(InboundWebsocketProcessorUtil.isAuthenticated(inboundMessageContext)).thenReturn(false);
        HandshakeProcessor handshakeProcessor = new HandshakeProcessor();
        InboundProcessorResponseDTO errorResponseDTO = new InboundProcessorResponseDTO();
        errorResponseDTO.setError(true);
        errorResponseDTO.setErrorCode(WebSocketApiConstants.HandshakeErrorConstants.API_AUTH_ERROR);
        errorResponseDTO
                .setErrorMessage(WebSocketApiConstants.HandshakeErrorConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE);
        errorResponseDTO.setCloseConnection(true);
        PowerMockito.when(InboundWebsocketProcessorUtil.getHandshakeErrorDTO(
                        WebSocketApiConstants.HandshakeErrorConstants.API_AUTH_ERROR,
                        WebSocketApiConstants.HandshakeErrorConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE))
                .thenReturn(errorResponseDTO);
        InboundProcessorResponseDTO inboundProcessorResponseDTO =
                handshakeProcessor.processHandshake(inboundMessageContext);
        Assert.assertTrue(inboundProcessorResponseDTO.isError());
        Assert.assertNotNull(inboundProcessorResponseDTO.getErrorMessage());
        Assert.assertTrue(inboundProcessorResponseDTO.isCloseConnection());
        Assert.assertEquals(inboundProcessorResponseDTO.getErrorResponseString(),
                errorResponseDTO.getErrorResponseString());
        Assert.assertEquals(inboundProcessorResponseDTO.getErrorCode(), errorResponseDTO.getErrorCode());
        Assert.assertEquals(inboundProcessorResponseDTO.getErrorMessage(), errorResponseDTO.getErrorMessage());
    }


    @Test
    public void handleAuthenticationException() throws Exception {

        InboundMessageContext inboundMessageContext = new InboundMessageContext();
        PowerMockito.mockStatic(InboundWebsocketProcessorUtil.class);
        PowerMockito.when(InboundWebsocketProcessorUtil.isAuthenticated(inboundMessageContext)).thenReturn(false);
        HandshakeProcessor handshakeProcessor = new HandshakeProcessor();
        InboundProcessorResponseDTO errorResponseDTO = new InboundProcessorResponseDTO();
        errorResponseDTO.setError(true);
        errorResponseDTO.setErrorCode(WebSocketApiConstants.HandshakeErrorConstants.API_AUTH_ERROR);
        errorResponseDTO
                .setErrorMessage(WebSocketApiConstants.HandshakeErrorConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE);
        errorResponseDTO.setCloseConnection(true);
        PowerMockito.when(InboundWebsocketProcessorUtil.getHandshakeErrorDTO(
                        WebSocketApiConstants.HandshakeErrorConstants.API_AUTH_ERROR,
                        WebSocketApiConstants.HandshakeErrorConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE))
                .thenReturn(errorResponseDTO);
        InboundProcessorResponseDTO inboundProcessorResponseDTO =
                handshakeProcessor.processHandshake(inboundMessageContext);
        Assert.assertTrue(inboundProcessorResponseDTO.isError());
        Assert.assertNotNull(inboundProcessorResponseDTO.getErrorMessage());
        Assert.assertTrue(inboundProcessorResponseDTO.isCloseConnection());
        Assert.assertEquals(inboundProcessorResponseDTO.getErrorResponseString(),
                errorResponseDTO.getErrorResponseString());
        Assert.assertEquals(inboundProcessorResponseDTO.getErrorCode(), errorResponseDTO.getErrorCode());
        Assert.assertEquals(inboundProcessorResponseDTO.getErrorMessage(), errorResponseDTO.getErrorMessage());
    }

    @Test
    public void handleAPISecurityException() throws Exception {

        InboundMessageContext inboundMessageContext = new InboundMessageContext();
        PowerMockito.mockStatic(InboundWebsocketProcessorUtil.class);
        PowerMockito.when(InboundWebsocketProcessorUtil.isAuthenticated(inboundMessageContext)).thenThrow(
                new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                        APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE));
        HandshakeProcessor handshakeProcessor = new HandshakeProcessor();
        InboundProcessorResponseDTO errorResponseDTO = new InboundProcessorResponseDTO();
        errorResponseDTO.setError(true);
        errorResponseDTO.setErrorCode(WebSocketApiConstants.HandshakeErrorConstants.API_AUTH_ERROR);
        errorResponseDTO
                .setErrorMessage(WebSocketApiConstants.HandshakeErrorConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE);
        errorResponseDTO.setCloseConnection(true);
        PowerMockito.when(InboundWebsocketProcessorUtil.getHandshakeErrorDTO(
                        WebSocketApiConstants.HandshakeErrorConstants.API_AUTH_ERROR,
                        APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE))
                .thenReturn(errorResponseDTO);
        InboundProcessorResponseDTO inboundProcessorResponseDTO =
                handshakeProcessor.processHandshake(inboundMessageContext);
        Assert.assertTrue(inboundProcessorResponseDTO.isError());
        Assert.assertNotNull(inboundProcessorResponseDTO.getErrorMessage());
        Assert.assertTrue(inboundProcessorResponseDTO.isCloseConnection());
        Assert.assertEquals(inboundProcessorResponseDTO.getErrorResponseString(),
                errorResponseDTO.getErrorResponseString());
        Assert.assertEquals(inboundProcessorResponseDTO.getErrorCode(), errorResponseDTO.getErrorCode());
        Assert.assertEquals(inboundProcessorResponseDTO.getErrorMessage(), errorResponseDTO.getErrorMessage());
    }
}
