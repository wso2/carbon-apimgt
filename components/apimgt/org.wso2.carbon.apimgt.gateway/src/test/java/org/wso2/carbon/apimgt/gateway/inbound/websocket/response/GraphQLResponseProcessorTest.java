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
package org.wso2.carbon.apimgt.gateway.inbound.websocket.response;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.gateway.dto.GraphQLOperationDTO;
import org.wso2.carbon.apimgt.gateway.handlers.WebsocketUtil;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.apimgt.gateway.handlers.streaming.websocket.WebSocketApiConstants;
import org.wso2.carbon.apimgt.gateway.handlers.streaming.websocket.WebSocketUtils;
import org.wso2.carbon.apimgt.gateway.inbound.InboundMessageContext;
import org.wso2.carbon.apimgt.gateway.inbound.websocket.GraphQLProcessorResponseDTO;
import org.wso2.carbon.apimgt.gateway.inbound.websocket.InboundProcessorResponseDTO;
import org.wso2.carbon.apimgt.gateway.inbound.websocket.utils.InboundWebsocketProcessorUtil;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.VerbInfoDTO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Test class for GraphQLResponseProcessor.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({InboundWebsocketProcessorUtil.class, WebSocketUtils.class, WebsocketUtil.class,
        ServiceReferenceHolder.class, APIUtil.class})
public class GraphQLResponseProcessorTest {

    @Before
    public void setup() throws Exception {
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
    public void testHandleResponseSuccess() throws APISecurityException {

        InboundMessageContext inboundMessageContext = new InboundMessageContext();
        int msgSize = 100;
        String msgText = "{\"type\":\"data\",\"id\":\"1\",\"payload\":{\"data\":"
                + "{\"liftStatusChange\":{\"name\":\"Astra Express\"}}}}";
        PowerMockito.mockStatic(InboundWebsocketProcessorUtil.class);
        InboundProcessorResponseDTO responseDTO = new InboundProcessorResponseDTO();
        PowerMockito.when(InboundWebsocketProcessorUtil.authenticateToken(inboundMessageContext))
                .thenReturn(responseDTO);
        VerbInfoDTO verbInfoDTO = new VerbInfoDTO();
        verbInfoDTO.setHttpVerb("SUBSCRIPTION");
        verbInfoDTO.setThrottling("Unlimited");
        GraphQLOperationDTO graphQLOperationDTO = new GraphQLOperationDTO(verbInfoDTO, "liftStatusChange");
        inboundMessageContext.addVerbInfoForGraphQLMsgId("1", graphQLOperationDTO);
        PowerMockito.when(InboundWebsocketProcessorUtil
                .validateScopes(inboundMessageContext, "liftStatusChange", "1")).thenReturn(responseDTO);
        PowerMockito.when(InboundWebsocketProcessorUtil.doThrottleForGraphQL(msgSize, verbInfoDTO,
                inboundMessageContext, "1")).thenReturn(responseDTO);
        GraphQLResponseProcessor responseProcessor = new GraphQLResponseProcessor();
        setChannelAttributeMap(inboundMessageContext);
        PowerMockito.when(WebsocketUtil.validateDenyPolicies(Mockito.anyObject())).thenReturn(responseDTO);
        InboundProcessorResponseDTO processorResponseDTO =
                responseProcessor.handleResponse(msgSize, msgText, inboundMessageContext);
        Assert.assertFalse(processorResponseDTO.isError());
        Assert.assertNull(processorResponseDTO.getErrorMessage());
    }

    private void setChannelAttributeMap(InboundMessageContext inboundMessageContext) {

        ChannelHandlerContext ctx = Mockito.mock(ChannelHandlerContext.class);
        inboundMessageContext.setCtx(ctx);
        Channel channel = Mockito.mock(Channel.class);
        Mockito.when(ctx.channel()).thenReturn(channel);
        PowerMockito.mockStatic(WebSocketUtils.class);
        Mockito.when(channel.attr(WebSocketUtils.WSO2_PROPERTIES)).thenReturn(getChannelAttributeMap());
        PowerMockito.when(WebSocketUtils.getApiProperties(ctx)).thenReturn(new HashMap<>());
    }

    @Test
    public void testHandleNonSubscribeResponse() throws APISecurityException {

        InboundMessageContext inboundMessageContext = new InboundMessageContext();
        int msgSize = 100;
        String msgText = "{\"type\":\"connection_ack\"}";
        PowerMockito.mockStatic(InboundWebsocketProcessorUtil.class);
        InboundProcessorResponseDTO responseDTO = new InboundProcessorResponseDTO();
        PowerMockito.when(InboundWebsocketProcessorUtil.authenticateToken(inboundMessageContext))
                .thenReturn(responseDTO);
        PowerMockito.when(WebsocketUtil.validateDenyPolicies(Mockito.anyObject())).thenReturn(responseDTO);
        PowerMockito.when(WebsocketUtil.validateDenyPolicies(Mockito.anyObject())).thenReturn(responseDTO);
        setChannelAttributeMap(inboundMessageContext);
        GraphQLResponseProcessor responseProcessor = new GraphQLResponseProcessor();
        InboundProcessorResponseDTO processorResponseDTO =
                responseProcessor.handleResponse(msgSize, msgText, inboundMessageContext);
        Assert.assertFalse(processorResponseDTO.isError());
        Assert.assertNull(processorResponseDTO.getErrorMessage());
        Assert.assertFalse(processorResponseDTO.isCloseConnection());
    }

    @Test
    public void testHandleBadResponse() throws APISecurityException {

        InboundMessageContext inboundMessageContext = new InboundMessageContext();
        int msgSize = 100;
        String msgText = "{\"type\":\"data\",\"payload\":{\"data\":"
                + "{\"liftStatusChange\":{\"name\":\"Astra Express\"}}}}";
        PowerMockito.mockStatic(InboundWebsocketProcessorUtil.class);
        InboundProcessorResponseDTO responseDTO = new InboundProcessorResponseDTO();
        PowerMockito.when(InboundWebsocketProcessorUtil.authenticateToken(inboundMessageContext))
                .thenReturn(responseDTO);
        GraphQLResponseProcessor responseProcessor = new GraphQLResponseProcessor();
        InboundProcessorResponseDTO inboundProcessorResponseDTO = new InboundProcessorResponseDTO();
        inboundProcessorResponseDTO.setError(true);
        inboundProcessorResponseDTO.setErrorCode(WebSocketApiConstants.FrameErrorConstants.BAD_REQUEST);
        inboundProcessorResponseDTO.setErrorMessage("Missing mandatory id field in the message");
        PowerMockito.when(InboundWebsocketProcessorUtil
                        .getBadRequestFrameErrorDTO("Missing mandatory id field in the message"))
                .thenReturn(inboundProcessorResponseDTO);
        PowerMockito.when(WebsocketUtil.validateDenyPolicies(Mockito.anyObject())).thenReturn(responseDTO);
        setChannelAttributeMap(inboundMessageContext);
        InboundProcessorResponseDTO processorResponseDTO =
                responseProcessor.handleResponse(msgSize, msgText, inboundMessageContext);
        Assert.assertTrue(processorResponseDTO.isError());
        Assert.assertNotNull(processorResponseDTO.getErrorMessage());
        Assert.assertFalse(processorResponseDTO.isCloseConnection());
        Assert.assertEquals(processorResponseDTO.getErrorResponseString(),
                inboundProcessorResponseDTO.getErrorResponseString());
        Assert.assertEquals(processorResponseDTO.getErrorMessage(), inboundProcessorResponseDTO.getErrorMessage());
        Assert.assertEquals(processorResponseDTO.getErrorCode(), inboundProcessorResponseDTO.getErrorCode());
    }

    @Test
    public void testHandleThrottleOut() throws APISecurityException {

        InboundMessageContext inboundMessageContext = new InboundMessageContext();
        int msgSize = 100;
        String msgText = "{\"type\":\"data\",\"id\":\"1\",\"payload\":{\"data\":"
                + "{\"liftStatusChange\":{\"name\":\"Astra Express\"}}}}";
        PowerMockito.mockStatic(InboundWebsocketProcessorUtil.class);
        InboundProcessorResponseDTO responseDTO = new InboundProcessorResponseDTO();
        PowerMockito.when(InboundWebsocketProcessorUtil.authenticateToken(inboundMessageContext))
                .thenReturn(responseDTO);
        VerbInfoDTO verbInfoDTO = new VerbInfoDTO();
        verbInfoDTO.setHttpVerb("SUBSCRIPTION");
        verbInfoDTO.setThrottling("Unlimited");
        GraphQLOperationDTO graphQLOperationDTO = new GraphQLOperationDTO(verbInfoDTO, "liftStatusChange");
        inboundMessageContext.addVerbInfoForGraphQLMsgId("1", graphQLOperationDTO);
        PowerMockito.when(InboundWebsocketProcessorUtil
                .validateScopes(inboundMessageContext, "liftStatusChange", "1")).thenReturn(responseDTO);

        GraphQLProcessorResponseDTO throttleResponseDTO = new GraphQLProcessorResponseDTO();
        throttleResponseDTO.setError(true);
        throttleResponseDTO.setErrorCode(WebSocketApiConstants.FrameErrorConstants.THROTTLED_OUT_ERROR);
        throttleResponseDTO.setErrorMessage(WebSocketApiConstants.FrameErrorConstants.THROTTLED_OUT_ERROR_MESSAGE);
        throttleResponseDTO.setId("1");
        PowerMockito.when(InboundWebsocketProcessorUtil.doThrottleForGraphQL(msgSize, verbInfoDTO,
                inboundMessageContext, "1")).thenReturn(throttleResponseDTO);
        GraphQLResponseProcessor responseProcessor = new GraphQLResponseProcessor();
        setChannelAttributeMap(inboundMessageContext);
        PowerMockito.when(WebsocketUtil.validateDenyPolicies(Mockito.anyObject())).thenReturn(responseDTO);
        InboundProcessorResponseDTO processorResponseDTO =
                responseProcessor.handleResponse(msgSize, msgText, inboundMessageContext);
        Assert.assertTrue(processorResponseDTO.isError());
        Assert.assertNotNull(processorResponseDTO.getErrorMessage());
        Assert.assertFalse(processorResponseDTO.isCloseConnection());
        Assert.assertEquals(processorResponseDTO.getErrorResponseString(),
                throttleResponseDTO.getErrorResponseString());
        Assert.assertEquals(processorResponseDTO.getErrorMessage(), throttleResponseDTO.getErrorMessage());
        Assert.assertEquals(processorResponseDTO.getErrorCode(), throttleResponseDTO.getErrorCode());
    }

    @Test
    public void testHandleInvalidScope() throws APISecurityException {

        InboundMessageContext inboundMessageContext = new InboundMessageContext();
        int msgSize = 100;
        String msgText = "{\"type\":\"data\",\"id\":\"1\",\"payload\":{\"data\":"
                + "{\"liftStatusChange\":{\"name\":\"Astra Express\"}}}}";
        PowerMockito.mockStatic(InboundWebsocketProcessorUtil.class);
        InboundProcessorResponseDTO responseDTO = new InboundProcessorResponseDTO();
        PowerMockito.when(InboundWebsocketProcessorUtil.authenticateToken(inboundMessageContext))
                .thenReturn(responseDTO);
        VerbInfoDTO verbInfoDTO = new VerbInfoDTO();
        verbInfoDTO.setHttpVerb("SUBSCRIPTION");
        verbInfoDTO.setThrottling("Unlimited");
        verbInfoDTO.setAuthType("Any");
        GraphQLOperationDTO graphQLOperationDTO = new GraphQLOperationDTO(verbInfoDTO, "liftStatusChange");
        inboundMessageContext.addVerbInfoForGraphQLMsgId("1", graphQLOperationDTO);

        GraphQLProcessorResponseDTO graphQLProcessorResponseDTO = new GraphQLProcessorResponseDTO();
        graphQLProcessorResponseDTO.setError(true);
        graphQLProcessorResponseDTO.setErrorCode(WebSocketApiConstants.FrameErrorConstants.RESOURCE_FORBIDDEN_ERROR);
        graphQLProcessorResponseDTO.setErrorMessage("User is NOT authorized to access the Resource");
        graphQLProcessorResponseDTO.setCloseConnection(false);
        graphQLProcessorResponseDTO.setId("1");
        PowerMockito.when(InboundWebsocketProcessorUtil.validateScopes(inboundMessageContext, "liftStatusChange", "1"))
                .thenReturn(graphQLProcessorResponseDTO);
        PowerMockito.when(InboundWebsocketProcessorUtil.doThrottleForGraphQL(msgSize, verbInfoDTO,
                inboundMessageContext, "1")).thenReturn(responseDTO);
        GraphQLResponseProcessor responseProcessor = new GraphQLResponseProcessor();
        setChannelAttributeMap(inboundMessageContext);
        PowerMockito.when(WebsocketUtil.validateDenyPolicies(Mockito.anyObject())).thenReturn(responseDTO);
        InboundProcessorResponseDTO processorResponseDTO =
                responseProcessor.handleResponse(msgSize, msgText, inboundMessageContext);
        Assert.assertTrue(processorResponseDTO.isError());
        Assert.assertNotNull(processorResponseDTO.getErrorMessage());
        Assert.assertFalse(processorResponseDTO.isCloseConnection());
        Assert.assertEquals(processorResponseDTO.getErrorResponseString(),
                graphQLProcessorResponseDTO.getErrorResponseString());
        Assert.assertEquals(processorResponseDTO.getErrorMessage(), graphQLProcessorResponseDTO.getErrorMessage());
        Assert.assertEquals(processorResponseDTO.getErrorCode(), graphQLProcessorResponseDTO.getErrorCode());
    }

    @Test
    public void testHandleResponseScopeValidationSkipWhenSecurityDisabled() throws APISecurityException {

        InboundMessageContext inboundMessageContext = new InboundMessageContext();
        int msgSize = 100;
        String msgText = "{\"type\":\"data\",\"id\":\"1\",\"payload\":{\"data\":"
                + "{\"liftStatusChange\":{\"name\":\"Astra Express\"}}}}";
        PowerMockito.mockStatic(InboundWebsocketProcessorUtil.class);
        InboundProcessorResponseDTO responseDTO = new InboundProcessorResponseDTO();
        PowerMockito.when(InboundWebsocketProcessorUtil.authenticateToken(inboundMessageContext))
                .thenReturn(responseDTO);

        // VerbInfoDTO with security disabled
        VerbInfoDTO verbInfoDTO = new VerbInfoDTO();
        verbInfoDTO.setHttpVerb("SUBSCRIPTION");
        verbInfoDTO.setThrottling("Unlimited");
        verbInfoDTO.setAuthType("None");
        GraphQLOperationDTO graphQLOperationDTO = new GraphQLOperationDTO(verbInfoDTO, "liftStatusChange");
        inboundMessageContext.addVerbInfoForGraphQLMsgId("1", graphQLOperationDTO);

        // Creating response for scope validation
        GraphQLProcessorResponseDTO graphQLProcessorResponseDTO = new GraphQLProcessorResponseDTO();
        graphQLProcessorResponseDTO.setError(true);
        graphQLProcessorResponseDTO.setErrorCode(WebSocketApiConstants.FrameErrorConstants.RESOURCE_FORBIDDEN_ERROR);
        graphQLProcessorResponseDTO.setErrorMessage("User is NOT authorized to access the Resource");
        graphQLProcessorResponseDTO.setCloseConnection(false);
        graphQLProcessorResponseDTO.setId("1");

        PowerMockito.when(InboundWebsocketProcessorUtil.validateScopes(inboundMessageContext,
                "liftStatusChange", "1"))
                .thenReturn(graphQLProcessorResponseDTO);
        PowerMockito.when(InboundWebsocketProcessorUtil
                .doThrottleForGraphQL(msgSize, verbInfoDTO, inboundMessageContext, "1"))
                .thenReturn(responseDTO);

        GraphQLResponseProcessor responseProcessor = new GraphQLResponseProcessor();
        setChannelAttributeMap(inboundMessageContext);
        PowerMockito.when(WebsocketUtil.validateDenyPolicies(Mockito.anyObject())).thenReturn(responseDTO);
        InboundProcessorResponseDTO processorResponseDTO = responseProcessor
                .handleResponse(msgSize, msgText, inboundMessageContext);
        Assert.assertFalse(processorResponseDTO.isError());
        Assert.assertNull(processorResponseDTO.getErrorMessage());
        Assert.assertNotEquals(processorResponseDTO.getErrorMessage(),
                "User is NOT authorized to access the Resource");
    }

    private Attribute<Map<String, Object>> getChannelAttributeMap() {
        return new Attribute<Map<String, Object>>() {
            @Override
            public AttributeKey<Map<String, Object>> key() {
                return null;
            }

            @Override
            public Map<String, Object> get() {
                return null;
            }

            @Override
            public void set(Map<String, Object> stringObjectMap) {

            }

            @Override
            public Map<String, Object> getAndSet(Map<String, Object> stringObjectMap) {
                return null;
            }

            @Override
            public Map<String, Object> setIfAbsent(Map<String, Object> stringObjectMap) {
                return null;
            }

            @Override
            public Map<String, Object> getAndRemove() {
                return null;
            }

            @Override
            public boolean compareAndSet(Map<String, Object> stringObjectMap, Map<String, Object> t1) {
                return false;
            }

            @Override
            public void remove() {

            }
        };
    }
}
