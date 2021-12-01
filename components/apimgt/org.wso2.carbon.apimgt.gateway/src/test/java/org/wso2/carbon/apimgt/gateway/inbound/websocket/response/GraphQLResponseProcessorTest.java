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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.gateway.dto.GraphQLOperationDTO;
import org.wso2.carbon.apimgt.gateway.handlers.streaming.websocket.WebSocketApiConstants;
import org.wso2.carbon.apimgt.gateway.inbound.InboundMessageContext;
import org.wso2.carbon.apimgt.gateway.inbound.websocket.GraphQLProcessorResponseDTO;
import org.wso2.carbon.apimgt.gateway.inbound.websocket.InboundProcessorResponseDTO;
import org.wso2.carbon.apimgt.gateway.inbound.websocket.utils.InboundWebsocketProcessorUtil;
import org.wso2.carbon.apimgt.impl.dto.VerbInfoDTO;

/**
 * Test class for GraphQLResponseProcessor.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({InboundWebsocketProcessorUtil.class})
public class GraphQLResponseProcessorTest {

    @Test
    public void testHandleResponseSuccess() {

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
        InboundProcessorResponseDTO processorResponseDTO =
                responseProcessor.handleResponse(msgSize, msgText, inboundMessageContext);
        Assert.assertFalse(processorResponseDTO.isError());
        Assert.assertNull(processorResponseDTO.getErrorMessage());
    }

    @Test
    public void testHandleNonSubscribeResponse() {

        InboundMessageContext inboundMessageContext = new InboundMessageContext();
        int msgSize = 100;
        String msgText = "{\"type\":\"connection_ack\"}";
        PowerMockito.mockStatic(InboundWebsocketProcessorUtil.class);
        InboundProcessorResponseDTO responseDTO = new InboundProcessorResponseDTO();
        PowerMockito.when(InboundWebsocketProcessorUtil.authenticateToken(inboundMessageContext))
                .thenReturn(responseDTO);
        GraphQLResponseProcessor responseProcessor = new GraphQLResponseProcessor();
        InboundProcessorResponseDTO processorResponseDTO =
                responseProcessor.handleResponse(msgSize, msgText, inboundMessageContext);
        Assert.assertFalse(processorResponseDTO.isError());
        Assert.assertNull(processorResponseDTO.getErrorMessage());
        Assert.assertFalse(processorResponseDTO.isCloseConnection());
    }

    @Test
    public void testHandleBadResponse() {

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
    public void testHandleThrottleOut() {

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
    public void testHandleInvalidScope() {

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
}
