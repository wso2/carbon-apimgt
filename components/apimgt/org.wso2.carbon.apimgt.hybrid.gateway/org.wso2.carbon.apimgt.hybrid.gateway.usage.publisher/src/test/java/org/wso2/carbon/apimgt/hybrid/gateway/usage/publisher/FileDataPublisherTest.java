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
import org.wso2.carbon.apimgt.hybrid.gateway.common.config.ConfigManager;
import org.wso2.carbon.apimgt.hybrid.gateway.usage.publisher.constants.Constants;
import org.wso2.carbon.apimgt.usage.publisher.dto.DataBridgeRequestResponseStreamPublisherDTO;
import org.wso2.carbon.databridge.agent.AgentHolder;
import org.wso2.carbon.databridge.agent.DataEndpointAgent;
import org.wso2.carbon.databridge.agent.conf.AgentConfiguration;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.io.PrintWriter;

/**
 * FileDataPublisher Test Class
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({AgentHolder.class, CarbonUtils.class, FileDataPublisher.class})
public class FileDataPublisherTest {
    public static final String CARBON_HOME = "carbon.home";
    public static final String CARBON_CONFIGS_PATH = "/repository/conf";

    @Before
    public void setUp() throws Exception {
        TestUtil util = new TestUtil();
        util.setupCarbonHome();
    }

    @Test
    public void tryPublish() throws Exception {
        PowerMockito.mockStatic(AgentHolder.class);
        AgentHolder agentHolder = Mockito.mock(AgentHolder.class);
        PowerMockito.when(AgentHolder.getInstance()).thenReturn(agentHolder);
        DataEndpointAgent dataEndpointAgent = Mockito.mock(DataEndpointAgent.class);
        Mockito.when(agentHolder.getDefaultDataEndpointAgent()).thenReturn(dataEndpointAgent);
        AgentConfiguration agentConfig = Mockito.mock(AgentConfiguration.class);
        Mockito.when(dataEndpointAgent.getAgentConfiguration()).thenReturn(agentConfig);
        Mockito.when(agentConfig.getQueueSize()).thenReturn(32768);
        String carbonHome = System.getProperty(Constants.CARBON_HOME);
        String carbonConfigPath = System.getProperty(CARBON_HOME) + CARBON_CONFIGS_PATH;
        PowerMockito.mockStatic(CarbonUtils.class);
        PowerMockito.when(CarbonUtils.getCarbonHome()).thenReturn(carbonHome);
        PowerMockito.when(CarbonUtils.getCarbonConfigDirPath()).thenReturn(carbonConfigPath);
        Mockito.when(ConfigManager.getConfigManager().getProperty("MaxUsageFileSize")).thenReturn("sizeInMb");
        DataBridgeRequestResponseStreamPublisherDTO dataBridgeRequestStreamPublisherDTO = Mockito.mock(DataBridgeRequestResponseStreamPublisherDTO.class);
        FileDataPublisher fileDataPublisher = new FileDataPublisher();
        fileDataPublisher.tryPublish("org.wso2.apimgt.statistics.request:1.0.0", 12324343,
                (Object[]) dataBridgeRequestStreamPublisherDTO.createMetaData(), null,
                (Object[]) dataBridgeRequestStreamPublisherDTO.createPayload());
    }

    @Test
    public void shutdown() throws Exception {
        String carbonHome = System.getProperty(Constants.CARBON_HOME);
        PowerMockito.mockStatic(CarbonUtils.class);
        PowerMockito.when(CarbonUtils.getCarbonHome()).thenReturn(carbonHome);
        PowerMockito.mockStatic(AgentHolder.class);
        AgentHolder agentHolder = Mockito.mock(AgentHolder.class);
        PowerMockito.when(AgentHolder.getInstance()).thenReturn(agentHolder);
        DataEndpointAgent dataEndpointAgent = Mockito.mock(DataEndpointAgent.class);
        Mockito.when(agentHolder.getDefaultDataEndpointAgent()).thenReturn(dataEndpointAgent);
        AgentConfiguration agentConfig = Mockito.mock(AgentConfiguration.class);
        Mockito.when(dataEndpointAgent.getAgentConfiguration()).thenReturn(agentConfig);
        Mockito.when(agentConfig.getQueueSize()).thenReturn(32768);
        FileDataPublisher fileDataPublisher = new FileDataPublisher();
        fileDataPublisher.shutdown();
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
