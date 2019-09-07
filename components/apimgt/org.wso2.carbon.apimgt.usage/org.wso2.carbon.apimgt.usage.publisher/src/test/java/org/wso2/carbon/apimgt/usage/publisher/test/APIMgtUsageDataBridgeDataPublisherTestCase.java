/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.usage.publisher.test;

import org.apache.axis2.context.MessageContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.impl.APIManagerAnalyticsConfiguration;
import org.wso2.carbon.apimgt.usage.publisher.APIMgtUsageDataBridgeDataPublisher;
import org.wso2.carbon.apimgt.usage.publisher.DataPublisherUtil;
import org.wso2.carbon.apimgt.usage.publisher.dto.ExecutionTimeDTO;
import org.wso2.carbon.apimgt.usage.publisher.dto.FaultPublisherDTO;
import org.wso2.carbon.apimgt.usage.publisher.dto.RequestResponseStreamDTO;
import org.wso2.carbon.apimgt.usage.publisher.dto.ThrottlePublisherDTO;
import org.wso2.carbon.databridge.agent.DataPublisher;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

/**
 * Testing Data publisher event.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({DataPublisherUtil.class})
public class APIMgtUsageDataBridgeDataPublisherTestCase {

    public final String REQUEST_START_TIME = "api.ut.requestTime";
    public final String CONTEXT = "context";
    public final String API_VERSION = "1.0.0";
    public final String API_NAME = "APIName";
    public final String RESOURCE_PATH = "/menu";
    public final String METHOD = "GET";
    public final String USERNAME = "ishara";
    public final String HOST_NAME = "abc.com";
    public final String TENAT_DOMAIN = "carbon.super";
    public final String APP_NAME = "test";
    public final String APP_ID = "1";
    public final String PROTOCOL = "HTTP";
    public final String ERROR_CODE = "ERROR_CODE";
    public final String ERROR_MESSAGE = "ERROR_MESSAGE";
    public final String SYNAPDE_GW_LABEL = "Synapse";
    public final String KEYTYPE = "PRODUCTION";
    public final String CONSUMER_KEY = "consumerKey";
    public final String CORRELATION_ID = "correlationID";
    public final String USER_AGENT = "UserAgent";

    APIMgtUsageDataBridgeDataPublisher publisher = new APIMgtUsageDataBridgeDataPublisher();

    MessageContext messageContext = null;

    @Before
    public void init() throws Exception {

        messageContext = Mockito.mock(MessageContext.class);
        PowerMockito.mockStatic(DataPublisherUtil.class);
        publisher.init();

    }

    @Test
    public void testFaultStreamPublishEvent() throws Exception {

        FaultPublisherDTO faultPublisherDTO = new FaultPublisherDTO();

        faultPublisherDTO.setApplicationConsumerKey(CONSUMER_KEY);
        faultPublisherDTO.setApiContext(CONTEXT);
        faultPublisherDTO.setApiVersion(API_VERSION);
        faultPublisherDTO.setApiName(API_NAME);
        faultPublisherDTO.setApiResourcePath(RESOURCE_PATH);
        faultPublisherDTO.setApiMethod(METHOD);
        faultPublisherDTO.setErrorCode(String.valueOf(ERROR_CODE));
        faultPublisherDTO.setErrorMessage(ERROR_MESSAGE);
        faultPublisherDTO.setRequestTimestamp(System.currentTimeMillis());
        faultPublisherDTO.setUsername(USERNAME);
        faultPublisherDTO.setUserTenantDomain(MultitenantUtils.getTenantDomain(faultPublisherDTO.getUsername()));
        faultPublisherDTO.setHostname(HOST_NAME);
        faultPublisherDTO.setApiCreator(USERNAME);
        faultPublisherDTO.setApiCreatorTenantDomain(TENAT_DOMAIN);
        faultPublisherDTO.setApplicationName(APP_NAME);
        faultPublisherDTO.setApplicationId(APP_ID);
        faultPublisherDTO.setProtocol(PROTOCOL);
        faultPublisherDTO.setMetaClientType(KEYTYPE);
        faultPublisherDTO.setGatewaType(SYNAPDE_GW_LABEL);
        APIMgtUsageDataBridgeDataPublisher publisher = new APIMgtUsageDataBridgeDataPublisher();
        APIManagerAnalyticsConfiguration analyticsConfiguration = Mockito.mock(APIManagerAnalyticsConfiguration.class);
        PowerMockito.when(DataPublisherUtil.getApiManagerAnalyticsConfiguration()).thenReturn(analyticsConfiguration);
        PowerMockito.when(DataPublisherUtil.getHostAddress()).thenReturn(HOST_NAME);
        Mockito.when(analyticsConfiguration.getFaultStreamName()).thenReturn("org.wso2.apimgt.statistics.fault");
        Mockito.when(analyticsConfiguration.getFaultStreamVersion()).thenReturn("3.0.0");
        Mockito.when(analyticsConfiguration.getDasReceiverUrlGroups()).thenReturn("tcp://localhost:7612");
        Mockito.when(analyticsConfiguration.getDasReceiverServerUser()).thenReturn("admin");
        Mockito.when(analyticsConfiguration.getDasReceiverServerPassword()).thenReturn("admin");
        Mockito.when(analyticsConfiguration.getDasReceiverAuthUrlGroups()).thenReturn("ssl://localhost:7712");
        DataPublisher dataPublisher = Mockito.mock(DataPublisher.class);
        PowerMockito.whenNew(DataPublisher.class).withAnyArguments().thenReturn(dataPublisher);

        //test data publish path.
        publisher.publishEvent(faultPublisherDTO);

        // test data publishing fails due to null value in mandatory values.
        faultPublisherDTO.setHostname(null);
        publisher.publishEvent(faultPublisherDTO);
    }

    @Test
    public void testRequestResponseStreamPublishEvent() throws Exception {

        RequestResponseStreamDTO stream = new RequestResponseStreamDTO();
        stream.setApiContext(CONTEXT);
        stream.setApiMethod(METHOD);
        stream.setApiName(API_NAME);
        stream.setApiHostname(HOST_NAME);
        stream.setApiCreatorTenantDomain(TENAT_DOMAIN);
        stream.setApiCreator(USERNAME);
        stream.setApiResourcePath(RESOURCE_PATH);
        stream.setApiResourceTemplate("");
        stream.setApiTier("unlimitted");
        stream.setApiVersion(API_VERSION);
        stream.setApplicationConsumerKey(CONSUMER_KEY);
        stream.setApplicationId(APP_ID);
        stream.setApplicationName(APP_NAME);
        stream.setApplicationOwner(USERNAME);
        stream.setBackendTime(System.currentTimeMillis());
        stream.setDestination("description");
        stream.setExecutionTime(new ExecutionTimeDTO());
        stream.setMetaClientType(KEYTYPE); // check meta type
        stream.setProtocol(PROTOCOL);
        stream.setRequestTimestamp(System.currentTimeMillis());
        stream.setResponseCacheHit(true);
        stream.setResponseCode(200);
        stream.setResponseSize(1232);
        stream.setServiceTime(1213);
        stream.setThrottledOut(false);
        stream.setUserAgent(USER_AGENT);
        stream.setUserIp("10.103.5.130");
        stream.setUsername(USERNAME);
        stream.setUserTenantDomain(TENAT_DOMAIN);
        stream.setResponseTime(2233);
        stream.setCorrelationID(CORRELATION_ID);
        stream.setGatewayType(SYNAPDE_GW_LABEL);
        stream.setLabel("abc");

        APIManagerAnalyticsConfiguration analyticsConfiguration = Mockito.mock(APIManagerAnalyticsConfiguration.class);
        PowerMockito.when(DataPublisherUtil.getApiManagerAnalyticsConfiguration()).thenReturn(analyticsConfiguration);
        PowerMockito.when(DataPublisherUtil.getHostAddress()).thenReturn("abc.com");
        Mockito.when(analyticsConfiguration.getRequestStreamName()).thenReturn("org.wso2.apimgt.statistics.request");
        Mockito.when(analyticsConfiguration.getRequestStreamVersion()).thenReturn("3.0.0");
        Mockito.when(analyticsConfiguration.getDasReceiverUrlGroups()).thenReturn("tcp://localhost:7612");
        Mockito.when(analyticsConfiguration.getDasReceiverServerUser()).thenReturn("admin");
        Mockito.when(analyticsConfiguration.getDasReceiverServerPassword()).thenReturn("admin");
        Mockito.when(analyticsConfiguration.getDasReceiverAuthUrlGroups()).thenReturn("ssl://localhost:7712");
        DataPublisher dataPublisher = Mockito.mock(DataPublisher.class);
        PowerMockito.whenNew(DataPublisher.class).withAnyArguments().thenReturn(dataPublisher);

        //test data publish path.
        publisher.publishEvent(stream);

        // test data publishing fails due to null value in mandatory values.
        stream.setApplicationName(null);
        publisher.publishEvent(stream);
    }

    @Test
    public void testThrottleStreamPublishEvent() throws Exception {

        ThrottlePublisherDTO throttlePublisherDTO = new ThrottlePublisherDTO();

        throttlePublisherDTO.setAccessToken("accesstoken");
        throttlePublisherDTO.setUsername(USERNAME);
        throttlePublisherDTO.setTenantDomain(TENAT_DOMAIN);
        throttlePublisherDTO.setApiCreatorTenantDomain(TENAT_DOMAIN);
        throttlePublisherDTO.setApiname(API_NAME);
        throttlePublisherDTO.setVersion(API_VERSION);
        throttlePublisherDTO.setContext(CONTEXT);
        throttlePublisherDTO.setApiCreator(USERNAME);
        throttlePublisherDTO.setApplicationName(APP_NAME);
        throttlePublisherDTO.setApplicationId(APP_ID);
        throttlePublisherDTO.setThrottledTime(System.currentTimeMillis());
        throttlePublisherDTO.setThrottledOutReason("ThrottledOutReason");

        throttlePublisherDTO.setSubscriber(USERNAME);
        throttlePublisherDTO.setKeyType(KEYTYPE);
        throttlePublisherDTO.setCorrelationID(CORRELATION_ID);
        throttlePublisherDTO.setGatewayType(SYNAPDE_GW_LABEL);
        throttlePublisherDTO.setHostName(HOST_NAME);

        publisher.publishEvent(throttlePublisherDTO);

        APIManagerAnalyticsConfiguration analyticsConfiguration = Mockito.mock(APIManagerAnalyticsConfiguration.class);
        PowerMockito.when(DataPublisherUtil.getApiManagerAnalyticsConfiguration()).thenReturn(analyticsConfiguration);
        PowerMockito.when(DataPublisherUtil.getHostAddress()).thenReturn("abc.com");
        Mockito.when(analyticsConfiguration.getThrottleStreamName()).thenReturn("org.wso2.apimgt.statistics.throttle");
        Mockito.when(analyticsConfiguration.getThrottleStreamVersion()).thenReturn("3.0.0");
        Mockito.when(analyticsConfiguration.getDasReceiverUrlGroups()).thenReturn("tcp://localhost:7612");
        Mockito.when(analyticsConfiguration.getDasReceiverServerUser()).thenReturn("admin");
        Mockito.when(analyticsConfiguration.getDasReceiverServerPassword()).thenReturn("admin");
        Mockito.when(analyticsConfiguration.getDasReceiverAuthUrlGroups()).thenReturn("ssl://localhost:7712");
        DataPublisher dataPublisher = Mockito.mock(DataPublisher.class);
        PowerMockito.whenNew(DataPublisher.class).withAnyArguments().thenReturn(dataPublisher);

        //test data publish path.
        publisher.publishEvent(throttlePublisherDTO);

        // test data publishing fails due to null value in mandatory values.
        throttlePublisherDTO.setApiname(null);
        publisher.publishEvent(throttlePublisherDTO);
    }

}
