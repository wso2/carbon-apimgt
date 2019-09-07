/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.hybrid.gateway.usage.publisher;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.impl.APIManagerAnalyticsConfiguration;
import org.wso2.carbon.apimgt.hybrid.gateway.usage.publisher.constants.Constants;
import org.wso2.carbon.apimgt.usage.publisher.DataPublisherUtil;
import org.wso2.carbon.apimgt.usage.publisher.dto.AlertTypeDTO;
import org.wso2.carbon.apimgt.usage.publisher.dto.FaultPublisherDTO;
import org.wso2.carbon.apimgt.usage.publisher.dto.RequestResponseStreamDTO;
import org.wso2.carbon.apimgt.usage.publisher.dto.ThrottlePublisherDTO;
import org.wso2.carbon.databridge.agent.AgentHolder;
import org.wso2.carbon.databridge.agent.DataEndpointAgent;
import org.wso2.carbon.databridge.agent.conf.AgentConfiguration;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.io.PrintWriter;

/**
 * APIMgtUsageFileDataPublisherTest
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({DataPublisherUtil.class, AgentHolder.class, CarbonUtils.class})
public class APIMgtUsageFileDataPublisherTest {
    @Before
    public void setUp() throws Exception {
        TestUtil util = new TestUtil();
        util.setupCarbonHome();
    }

    @Test
    public void init() throws Exception {
        PowerMockito.mockStatic(AgentHolder.class);
        AgentHolder agentHolder = Mockito.mock(AgentHolder.class);
        PowerMockito.when(AgentHolder.getInstance()).thenReturn(agentHolder);
        DataEndpointAgent dataEndpointAgent = Mockito.mock(DataEndpointAgent.class);
        Mockito.when(agentHolder.getDefaultDataEndpointAgent()).thenReturn(dataEndpointAgent);
        AgentConfiguration agentConfig = Mockito.mock(AgentConfiguration.class);
        Mockito.when(dataEndpointAgent.getAgentConfiguration()).thenReturn(agentConfig);
        Mockito.when(agentConfig.getQueueSize()).thenReturn(32768);
        APIMgtUsageFileDataPublisher apiMgtUsageFileDataPublisher = new APIMgtUsageFileDataPublisher();
        apiMgtUsageFileDataPublisher.init();
    }

    public void mockCommonCases() throws Exception {
        PowerMockito.mockStatic(AgentHolder.class);
        AgentHolder agentHolder = Mockito.mock(AgentHolder.class);
        PowerMockito.when(AgentHolder.getInstance()).thenReturn(agentHolder);
        DataEndpointAgent dataEndpointAgent = Mockito.mock(DataEndpointAgent.class);
        Mockito.when(agentHolder.getDefaultDataEndpointAgent()).thenReturn(dataEndpointAgent);
        AgentConfiguration agentConfig = Mockito.mock(AgentConfiguration.class);
        Mockito.when(dataEndpointAgent.getAgentConfiguration()).thenReturn(agentConfig);
        Mockito.when(agentConfig.getQueueSize()).thenReturn(32768);
        String carbonHome = System.getProperty(Constants.CARBON_HOME);
        PowerMockito.mockStatic(CarbonUtils.class);
        PowerMockito.when(CarbonUtils.getCarbonHome()).thenReturn(carbonHome);
    }

    @Test
    public void publishEvent_withRequestResponseStreamDTO() throws Exception {
        mockCommonCases();
        PowerMockito.mockStatic(DataPublisherUtil.class);
        APIManagerAnalyticsConfiguration apiMngAnalyticsConfig = Mockito.mock(APIManagerAnalyticsConfiguration.class);
        PowerMockito.when(DataPublisherUtil.getApiManagerAnalyticsConfiguration()).thenReturn(apiMngAnalyticsConfig);
        Mockito.when(apiMngAnalyticsConfig.getRequestStreamName()).thenReturn("org.wso2.apimgt.statistics.request");
        Mockito.when(apiMngAnalyticsConfig.getRequestStreamVersion()).thenReturn("1.1.0");
        RequestResponseStreamDTO requestResponseStreamDTO = new RequestResponseStreamDTO();
        APIMgtUsageFileDataPublisher apiMgtUsageFileDataPublisher = new APIMgtUsageFileDataPublisher();
        apiMgtUsageFileDataPublisher.init();
        apiMgtUsageFileDataPublisher.publishEvent(requestResponseStreamDTO);
    }

    @Test
    public void publishEvent_withFaultPublisherDTO() throws Exception {
        mockCommonCases();
        PowerMockito.mockStatic(DataPublisherUtil.class);
        APIManagerAnalyticsConfiguration apiMngAnalyticsConfig = Mockito.mock(APIManagerAnalyticsConfiguration.class);
        PowerMockito.when(DataPublisherUtil.getApiManagerAnalyticsConfiguration()).thenReturn(apiMngAnalyticsConfig);
        Mockito.when(apiMngAnalyticsConfig.getRequestStreamName()).thenReturn("org.wso2.apimgt.statistics.fault");
        Mockito.when(apiMngAnalyticsConfig.getRequestStreamVersion()).thenReturn("1.0.0");
        FaultPublisherDTO faultPublisherDTO = new FaultPublisherDTO();
        APIMgtUsageFileDataPublisher apiMgtUsageFileDataPublisher = new APIMgtUsageFileDataPublisher();
        apiMgtUsageFileDataPublisher.init();
        apiMgtUsageFileDataPublisher.publishEvent(faultPublisherDTO);
    }

    @Test
    public void publishEvent_withThrottlePublisherDTO() throws Exception {
        mockCommonCases();
        PowerMockito.mockStatic(DataPublisherUtil.class);
        APIManagerAnalyticsConfiguration apiMngAnalyticsConfig = Mockito.mock(APIManagerAnalyticsConfiguration.class);
        PowerMockito.when(DataPublisherUtil.getApiManagerAnalyticsConfiguration()).thenReturn(apiMngAnalyticsConfig);
        Mockito.when(apiMngAnalyticsConfig.getRequestStreamName()).thenReturn("org.wso2.apimgt.statistics.throttle");
        Mockito.when(apiMngAnalyticsConfig.getRequestStreamVersion()).thenReturn("1.0.0");
        ThrottlePublisherDTO throttlePublisherDTO = new ThrottlePublisherDTO();
        APIMgtUsageFileDataPublisher apiMgtUsageFileDataPublisher = new APIMgtUsageFileDataPublisher();
        apiMgtUsageFileDataPublisher.init();
        apiMgtUsageFileDataPublisher.publishEvent(throttlePublisherDTO);
    }


    @Test
    public void publishEvent_withAlertTypeDTO() throws Exception {
        mockCommonCases();
        PowerMockito.mockStatic(DataPublisherUtil.class);
        APIManagerAnalyticsConfiguration apiMngAnalyticsConfig = Mockito.mock(APIManagerAnalyticsConfiguration.class);
        PowerMockito.when(DataPublisherUtil.getApiManagerAnalyticsConfiguration()).thenReturn(apiMngAnalyticsConfig);
        Mockito.when(apiMngAnalyticsConfig.getRequestStreamName())
                .thenReturn("org.wso2.analytics.apim.alertStakeholderInfo");
        Mockito.when(apiMngAnalyticsConfig.getRequestStreamVersion()).thenReturn("1.0.0");
        AlertTypeDTO alertTypeDTO = new AlertTypeDTO();
        APIMgtUsageFileDataPublisher apiMgtUsageFileDataPublisher = new APIMgtUsageFileDataPublisher();
        apiMgtUsageFileDataPublisher.init();
        apiMgtUsageFileDataPublisher.publishEvent(alertTypeDTO);
    }

    @AfterClass
    public static void cleanUp() throws Exception {
        //Cleaning the file
        String carbonHome = System.getProperty(Constants.CARBON_HOME);
        PrintWriter writer = new PrintWriter(
                carbonHome + File.separator + "api-usage-data" + File.separator + "api-usage-data.dat");
        writer.print("");
        writer.close();
    }
}
