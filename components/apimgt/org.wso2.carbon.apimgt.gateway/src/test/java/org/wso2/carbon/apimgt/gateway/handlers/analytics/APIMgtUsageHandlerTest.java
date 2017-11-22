/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.apimgt.gateway.handlers.analytics;

import junit.framework.Assert;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.RESTConstants;
import org.junit.Test;
import org.mockito.Mockito;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerAnalyticsConfiguration;
import org.wso2.carbon.apimgt.usage.publisher.APIMgtUsageDataPublisher;
import org.wso2.carbon.apimgt.usage.publisher.dto.AlertTypeDTO;
import org.wso2.carbon.apimgt.usage.publisher.dto.ExecutionTimePublisherDTO;
import org.wso2.carbon.apimgt.usage.publisher.dto.FaultPublisherDTO;
import org.wso2.carbon.apimgt.usage.publisher.dto.RequestPublisherDTO;
import org.wso2.carbon.apimgt.usage.publisher.dto.ResponsePublisherDTO;
import org.wso2.carbon.apimgt.usage.publisher.dto.ThrottlePublisherDTO;

import java.util.HashMap;
import java.util.Map;

public class APIMgtUsageHandlerTest {
    @Test
    public void testHandleRequest() throws Exception {
        APIMgtUsageDataPublisher apiMgtUsageDataPublisher = Mockito.mock(APIMgtUsageDataPublisher.class);
        APIManagerAnalyticsConfiguration apiManagerAnalyticsConfiguration = Mockito.mock
                (APIManagerAnalyticsConfiguration.class);
        Mockito.when(apiManagerAnalyticsConfiguration.isBuildMsg()).thenReturn(false);
        Mockito.when(apiManagerAnalyticsConfiguration.isAnalyticsEnabled()).thenReturn(true);
        Mockito.when(apiManagerAnalyticsConfiguration.getPublisherClass()).thenReturn("");
        APIMgtUsageHandler apiMgtUsageHandlerWrapper = new APIMgtUsageHandlerWrapper
                (apiManagerAnalyticsConfiguration, apiMgtUsageDataPublisher);

        MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
        org.apache.axis2.context.MessageContext axis2MsgCntxt = Mockito.mock(org.apache.axis2.context.MessageContext
                .class);
        Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgCntxt);
        Map headers = new HashMap();
        headers.put(APIConstants.USER_AGENT, "chrome");
        Mockito.when(axis2MsgCntxt.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS)).thenReturn
                (headers);
        Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgCntxt);
        Mockito.when(axis2MsgCntxt.getProperty(SynapseConstants.HTTP_SC)).thenReturn(200);
        Mockito.when(messageContext.getProperty("REST_FULL_REQUEST_PATH")).thenReturn("/abc/1.0.0/a");
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.REQUEST_START_TIME)).thenReturn("12345678");
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.CONSUMER_KEY)).thenReturn("abc-def-ghi");
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.CONTEXT)).thenReturn("/abc/1.0.0");
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.API_VERSION)).thenReturn("1.0.0");
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.API)).thenReturn("api1");
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.RESOURCE)).thenReturn("/a");
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.HTTP_METHOD)).thenReturn("GET");
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.VERSION)).thenReturn("1.0.0");
        Mockito.when((messageContext.getProperty(SynapseConstants.ERROR_CODE))).thenReturn("404");
        Mockito.when(messageContext.getProperty(SynapseConstants.ERROR_MESSAGE)).thenReturn("not found");
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.USER_ID)).thenReturn("admin");
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.HOST_NAME)).thenReturn("127.0.0.1");
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.API_PUBLISHER)).thenReturn("admin");
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.APPLICATION_NAME)).thenReturn("App1");
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.APPLICATION_ID)).thenReturn("1");
        Mockito.when(messageContext.getProperty(SynapseConstants.TRANSPORT_IN_NAME)).thenReturn("https");
        Mockito.when(messageContext.getProperty(RESTConstants.SYNAPSE_REST_API)).thenReturn("admin--api1-1.0.0");
        Mockito.when(messageContext.getProperty(RESTConstants.REST_URL_PREFIX)).thenReturn("https://localhost");
        apiMgtUsageHandlerWrapper.handleRequest(messageContext);
    }

    @Test
    public void testHandleRequestWithAuthContext() throws Exception {
        APIMgtUsageDataPublisher apiMgtUsageDataPublisher = Mockito.mock(APIMgtUsageDataPublisher.class);
        APIManagerAnalyticsConfiguration apiManagerAnalyticsConfiguration = Mockito.mock
                (APIManagerAnalyticsConfiguration.class);
        Mockito.when(apiManagerAnalyticsConfiguration.isBuildMsg()).thenReturn(false);
        Mockito.when(apiManagerAnalyticsConfiguration.isAnalyticsEnabled()).thenReturn(true);
        Mockito.when(apiManagerAnalyticsConfiguration.getPublisherClass()).thenReturn("");
        APIMgtUsageHandler apiMgtUsageHandlerWrapper = new APIMgtUsageHandlerWrapper
                (apiManagerAnalyticsConfiguration, apiMgtUsageDataPublisher);

        MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
        org.apache.axis2.context.MessageContext axis2MsgCntxt = Mockito.mock(org.apache.axis2.context.MessageContext
                .class);
        Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgCntxt);
        Map headers = new HashMap();
        headers.put(APIConstants.USER_AGENT, "chrome");
        Mockito.when(axis2MsgCntxt.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS)).thenReturn
                (headers);
        AuthenticationContext authContext = new AuthenticationContext();
        authContext.setConsumerKey("ac-def");
        authContext.setUsername("admin");
        authContext.setApplicationName("D");
        authContext.setApplicationId("1");
        authContext.setTier("A");
        authContext.setSubscriber("aa");
        Mockito.when(messageContext.getProperty("__API_AUTH_CONTEXT")).thenReturn(authContext);
        Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgCntxt);
        Mockito.when(axis2MsgCntxt.getProperty(SynapseConstants.HTTP_SC)).thenReturn(200);
        Mockito.when(messageContext.getProperty("REST_FULL_REQUEST_PATH")).thenReturn("/abc/1.0.0/a");
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.REQUEST_START_TIME)).thenReturn("12345678");
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.CONSUMER_KEY)).thenReturn("abc-def-ghi");
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.CONTEXT)).thenReturn("/abc/1.0.0");
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.API_VERSION)).thenReturn("1.0.0");
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.API)).thenReturn("api1");
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.RESOURCE)).thenReturn("/a");
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.HTTP_METHOD)).thenReturn("GET");
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.VERSION)).thenReturn("1.0.0");
        Mockito.when((messageContext.getProperty(SynapseConstants.ERROR_CODE))).thenReturn("404");
        Mockito.when(messageContext.getProperty(SynapseConstants.ERROR_MESSAGE)).thenReturn("not found");
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.USER_ID)).thenReturn("admin");
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.HOST_NAME)).thenReturn("127.0.0.1");
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.API_PUBLISHER)).thenReturn("admin");
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.APPLICATION_NAME)).thenReturn("App1");
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.APPLICATION_ID)).thenReturn("1");
        Mockito.when(messageContext.getProperty(SynapseConstants.TRANSPORT_IN_NAME)).thenReturn("https");
        Mockito.when(messageContext.getProperty(RESTConstants.SYNAPSE_REST_API)).thenReturn("admin--api1-1.0.0");
        Mockito.when(messageContext.getProperty(RESTConstants.REST_URL_PREFIX)).thenReturn("https://localhost");
        apiMgtUsageHandlerWrapper.handleRequest(messageContext);
    }

    @Test
    public void testHandleRequestKeyTypeAndCorrelationID() throws Exception {
        TestAPIMgtUsageDataBridgeDataPublisher apiMgtUsageDataPublisher = new TestAPIMgtUsageDataBridgeDataPublisher();
        APIManagerAnalyticsConfiguration apiManagerAnalyticsConfiguration = Mockito
                .mock(APIManagerAnalyticsConfiguration.class);
        Mockito.when(apiManagerAnalyticsConfiguration.isBuildMsg()).thenReturn(false);
        Mockito.when(apiManagerAnalyticsConfiguration.isAnalyticsEnabled()).thenReturn(true);
        Mockito.when(apiManagerAnalyticsConfiguration.getPublisherClass())
                .thenReturn(TestAPIMgtUsageDataBridgeDataPublisher.class.getName());
        APIMgtUsageHandler apiMgtUsageHandlerWrapper = new APIMgtUsageHandlerWrapper(apiManagerAnalyticsConfiguration,
                apiMgtUsageDataPublisher);

        MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
        org.apache.axis2.context.MessageContext axis2MsgCntxt = Mockito
                .mock(org.apache.axis2.context.MessageContext.class);
        Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgCntxt);
        Map headers = new HashMap();
        headers.put(APIConstants.USER_AGENT, "chrome");
        Mockito.when(axis2MsgCntxt.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS))
                .thenReturn(headers);
        Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgCntxt);
        Mockito.when(axis2MsgCntxt.getProperty(SynapseConstants.HTTP_SC)).thenReturn(200);
        Mockito.when(messageContext.getProperty("REST_FULL_REQUEST_PATH")).thenReturn("/abc/1.0.0/a");
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.REQUEST_START_TIME)).thenReturn("12345678");
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.CONSUMER_KEY)).thenReturn("abc-def-ghi");
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.CONTEXT)).thenReturn("/abc/1.0.0");
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.API_VERSION)).thenReturn("1.0.0");
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.API)).thenReturn("api1");
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.RESOURCE)).thenReturn("/a");
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.HTTP_METHOD)).thenReturn("GET");
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.VERSION)).thenReturn("1.0.0");
        Mockito.when((messageContext.getProperty(SynapseConstants.ERROR_CODE))).thenReturn("404");
        Mockito.when(messageContext.getProperty(SynapseConstants.ERROR_MESSAGE)).thenReturn("not found");
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.USER_ID)).thenReturn("admin");
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.HOST_NAME)).thenReturn("127.0.0.1");
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.API_PUBLISHER)).thenReturn("admin");
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.APPLICATION_NAME)).thenReturn("App1");
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.APPLICATION_ID)).thenReturn("1");
        Mockito.when(messageContext.getProperty(SynapseConstants.TRANSPORT_IN_NAME)).thenReturn("https");
        Mockito.when(messageContext.getProperty(RESTConstants.SYNAPSE_REST_API)).thenReturn("admin--api1-1.0.0");
        Mockito.when(messageContext.getProperty(RESTConstants.REST_URL_PREFIX)).thenReturn("https://localhost");
        Mockito.when(messageContext.getProperty(APIConstants.API_KEY_TYPE))
                .thenReturn(APIConstants.API_KEY_TYPE_PRODUCTION);
        apiMgtUsageHandlerWrapper.handleRequest(messageContext);

        Assert.assertEquals(APIConstants.API_KEY_TYPE_PRODUCTION,
                apiMgtUsageDataPublisher.getRequestPublisherDTO().getKeyType());
        Assert.assertNotNull(apiMgtUsageDataPublisher.getRequestPublisherDTO().getCorrelationID());
    }
}

/**
 * Custom data publisher class implemented for testing request event data
 */
class TestAPIMgtUsageDataBridgeDataPublisher implements APIMgtUsageDataPublisher {
    private RequestPublisherDTO requestPublisherDTO;

    @Override
    public void init() {
    }

    @Override
    public void publishEvent(RequestPublisherDTO requestPublisherDTO) {
        this.requestPublisherDTO = requestPublisherDTO;
    }

    @Override
    public void publishEvent(ResponsePublisherDTO responsePublisherDTO) {

    }

    @Override
    public void publishEvent(FaultPublisherDTO faultPublisherDTO) {

    }

    @Override
    public void publishEvent(ThrottlePublisherDTO throttlePublisherDTO) {

    }

    @Override
    public void publishEvent(ExecutionTimePublisherDTO executionTimePublisherDTO) {

    }

    @Override
    public void publishEvent(AlertTypeDTO alertTypeDTO) throws APIManagementException {

    }

    public RequestPublisherDTO getRequestPublisherDTO() {
        return requestPublisherDTO;
    }
}