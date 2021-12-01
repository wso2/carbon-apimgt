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
package org.wso2.carbon.apimgt.gateway.inbound.websocket.utils;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.testng.Assert;
import org.wso2.carbon.apimgt.gateway.handlers.WebsocketUtil;
import org.wso2.carbon.apimgt.gateway.handlers.graphQL.GraphQLConstants;
import org.wso2.carbon.apimgt.gateway.handlers.streaming.websocket.WebSocketApiConstants;
import org.wso2.carbon.apimgt.gateway.inbound.InboundMessageContext;
import org.wso2.carbon.apimgt.gateway.inbound.websocket.InboundProcessorResponseDTO;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.gateway.throttling.publisher.ThrottleDataPublisher;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.impl.dto.VerbInfoDTO;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.agent.DataPublisher;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

@RunWith(PowerMockRunner.class)
@PrepareForTest({PrivilegedCarbonContext.class, ServiceReferenceHolder.class, WebsocketUtil.class,
        ThrottleDataPublisher.class})
public class InboundWebsocketProcessorUtilTest {

    private DataPublisher dataPublisher;

    @Before
    public void init() {
        System.setProperty("carbon.home", "jhkjn");
        PowerMockito.mockStatic(PrivilegedCarbonContext.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        PrivilegedCarbonContext privilegedCarbonContext = Mockito.mock(PrivilegedCarbonContext.class);
        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        PowerMockito.when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(privilegedCarbonContext);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        PowerMockito.mockStatic(ThrottleDataPublisher.class);
        dataPublisher = Mockito.mock(DataPublisher.class);
        ThrottleDataPublisher throttleDataPublisher = Mockito.mock(ThrottleDataPublisher.class);
        Mockito.when(serviceReferenceHolder.getThrottleDataPublisher()).thenReturn(throttleDataPublisher);
        PowerMockito.when(ThrottleDataPublisher.getDataPublisher()).thenReturn(dataPublisher);
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        PowerMockito.when(serviceReferenceHolder.getAPIManagerConfiguration()).thenReturn(apiManagerConfiguration);
        PowerMockito.mockStatic(WebsocketUtil.class);
    }

    @Test
    public void testDoThrottleSuccessForGraphQL() throws ParseException {

        InboundMessageContext inboundMessageContext = new InboundMessageContext();
        int msgSize = 100;
        VerbInfoDTO verbInfoDTO = new VerbInfoDTO();
        verbInfoDTO.setThrottling("Gold");
        verbInfoDTO.setRequestKey("liftStatusChange");
        String operationId = "1";
        APIKeyValidationInfoDTO apiKeyValidationInfoDTO = new APIKeyValidationInfoDTO();
        apiKeyValidationInfoDTO.setApplicationTier(APIConstants.UNLIMITED_TIER);
        apiKeyValidationInfoDTO.setTier(APIConstants.UNLIMITED_TIER);
        apiKeyValidationInfoDTO.setSubscriberTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        apiKeyValidationInfoDTO.setSubscriber("admin");
        apiKeyValidationInfoDTO.setApiName("GraphQLAPI");
        apiKeyValidationInfoDTO.setApplicationId("12");
        inboundMessageContext.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        inboundMessageContext.setApiContext("/graphql");
        inboundMessageContext.setVersion("1.0.0");
        inboundMessageContext.setUserIP("198.162.10.2");
        inboundMessageContext.setInfoDTO(apiKeyValidationInfoDTO);

        String subscriptionLevelThrottleKey = apiKeyValidationInfoDTO.getApplicationId() + ":"
                + inboundMessageContext.getApiContext() + ":" + inboundMessageContext.getVersion();
        String applicationLevelThrottleKey = apiKeyValidationInfoDTO.getApplicationId() + ":"
                + apiKeyValidationInfoDTO.getSubscriber() + "@" + apiKeyValidationInfoDTO.getSubscriberTenantDomain();
        PowerMockito.when(WebsocketUtil.isThrottled(verbInfoDTO.getRequestKey(), subscriptionLevelThrottleKey,
                applicationLevelThrottleKey)).thenReturn(false);
        Mockito.when(dataPublisher.tryPublish(Mockito.anyObject())).thenReturn(true);
        InboundProcessorResponseDTO inboundProcessorResponseDTO =
                InboundWebsocketProcessorUtil.doThrottleForGraphQL(msgSize, verbInfoDTO, inboundMessageContext,
                        operationId);
        Assert.assertFalse(inboundProcessorResponseDTO.isError());
        Assert.assertNull(inboundProcessorResponseDTO.getErrorMessage());
        Assert.assertFalse(inboundProcessorResponseDTO.isCloseConnection());
    }

    @Test
    public void testDoThrottleFail() throws ParseException {
        InboundMessageContext inboundMessageContext = new InboundMessageContext();
        int msgSize = 100;
        VerbInfoDTO verbInfoDTO = new VerbInfoDTO();
        verbInfoDTO.setThrottling("Gold");
        verbInfoDTO.setRequestKey("liftStatusChange");
        String operationId = "1";
        APIKeyValidationInfoDTO apiKeyValidationInfoDTO = new APIKeyValidationInfoDTO();
        apiKeyValidationInfoDTO.setApplicationTier(APIConstants.UNLIMITED_TIER);
        apiKeyValidationInfoDTO.setTier(APIConstants.UNLIMITED_TIER);
        apiKeyValidationInfoDTO.setSubscriberTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        apiKeyValidationInfoDTO.setSubscriber("admin");
        apiKeyValidationInfoDTO.setApiName("GraphQLAPI");
        apiKeyValidationInfoDTO.setApplicationId("12");
        inboundMessageContext.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        inboundMessageContext.setApiContext("/graphql");
        inboundMessageContext.setVersion("1.0.0");
        inboundMessageContext.setUserIP("198.162.10.2");
        inboundMessageContext.setInfoDTO(apiKeyValidationInfoDTO);

        String subscriptionLevelThrottleKey = apiKeyValidationInfoDTO.getApplicationId() + ":"
                + inboundMessageContext.getApiContext() + ":" + inboundMessageContext.getVersion();
        String applicationLevelThrottleKey = apiKeyValidationInfoDTO.getApplicationId() + ":"
                + apiKeyValidationInfoDTO.getSubscriber() + "@" + apiKeyValidationInfoDTO.getSubscriberTenantDomain();
        Mockito.when(dataPublisher.tryPublish(Mockito.anyObject())).thenReturn(true);

        PowerMockito.when(WebsocketUtil.isThrottled(verbInfoDTO.getRequestKey(), subscriptionLevelThrottleKey,
                applicationLevelThrottleKey)).thenReturn(true);
        InboundProcessorResponseDTO inboundProcessorResponseDTO =
                InboundWebsocketProcessorUtil.doThrottleForGraphQL(msgSize, verbInfoDTO, inboundMessageContext,
                        operationId);
        Assert.assertTrue(inboundProcessorResponseDTO.isError());
        Assert.assertEquals(inboundProcessorResponseDTO.getErrorMessage(),
                WebSocketApiConstants.FrameErrorConstants.THROTTLED_OUT_ERROR_MESSAGE);
        Assert.assertEquals(inboundProcessorResponseDTO.getErrorCode(),
                WebSocketApiConstants.FrameErrorConstants.THROTTLED_OUT_ERROR);
        Assert.assertFalse(inboundProcessorResponseDTO.isCloseConnection());

        JSONParser jsonParser = new JSONParser();
        JSONObject errorJson = (JSONObject) jsonParser.parse(inboundProcessorResponseDTO.getErrorResponseString());
        org.junit.Assert.assertEquals(errorJson.get(GraphQLConstants.SubscriptionConstants.PAYLOAD_FIELD_NAME_TYPE),
                GraphQLConstants.SubscriptionConstants.PAYLOAD_FIELD_TYPE_ERROR);
        org.junit.Assert.assertEquals(errorJson.get(GraphQLConstants.SubscriptionConstants.PAYLOAD_FIELD_NAME_ID), "1");
        JSONObject payload = (JSONObject) errorJson.get(
                GraphQLConstants.SubscriptionConstants.PAYLOAD_FIELD_NAME_PAYLOAD);
        org.junit.Assert.assertEquals(payload.get(WebSocketApiConstants.FrameErrorConstants.ERROR_MESSAGE),
                WebSocketApiConstants.FrameErrorConstants.THROTTLED_OUT_ERROR_MESSAGE);
        org.junit.Assert.assertEquals(String.valueOf(payload.get(WebSocketApiConstants.FrameErrorConstants.ERROR_CODE)),
                String.valueOf(WebSocketApiConstants.FrameErrorConstants.THROTTLED_OUT_ERROR));
    }
}
