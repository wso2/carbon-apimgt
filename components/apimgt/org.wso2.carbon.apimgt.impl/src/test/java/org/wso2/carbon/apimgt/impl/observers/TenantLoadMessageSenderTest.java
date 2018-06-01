/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl.observers;

import org.apache.axis2.AxisFault;
import org.apache.axis2.clustering.ClusteringAgent;
import org.apache.axis2.clustering.ClusteringCommand;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.ClusteringMessage;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.message.clustering.TenantLoadMessage;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.util.ArrayList;
import java.util.List;

import static org.wso2.carbon.base.CarbonBaseConstants.CARBON_HOME;

@RunWith(PowerMockRunner.class) @PrepareForTest({ PrivilegedCarbonContext.class, ServiceReferenceHolder.class })
public class TenantLoadMessageSenderTest {

    private static TenantLoadMessageSender tenantLoadMessageSender;

    @Before
    public void setUp() throws AxisFault {
        tenantLoadMessageSender = new TenantLoadMessageSender();
        System.setProperty(CARBON_HOME, "");
    }

    @Test
    public void testSendTenantRegistryLoadMessage() throws ClusteringFault {
        ClusteringAgent clusteringAgent = PowerMockito.mock(ClusteringAgent.class);
        ClusteringMessage request = new TenantLoadMessage(1, "a.com");
        ClusteringCommand command = PowerMockito.mock(ClusteringCommand.class);
        List<ClusteringCommand> commandList = new ArrayList<ClusteringCommand>();
        commandList.add(command);
        PowerMockito.when(clusteringAgent.sendMessage(request, true)).thenReturn(commandList);
        tenantLoadMessageSender.sendTenantLoadMessage(clusteringAgent, 1, "a.com", 1);
    }

    @Test(expected = ClusteringFault.class)
    public void testSendTenantRegistryLoadMessageFail() throws ClusteringFault {
        ClusteringAgent clusteringAgent = PowerMockito.mock(ClusteringAgent.class);
        ClusteringCommand command = PowerMockito.mock(ClusteringCommand.class);
        List<ClusteringCommand> commandList = new ArrayList<ClusteringCommand>();
        commandList.add(command);
        ClusteringFault clusteringFault = PowerMockito.mock(ClusteringFault.class);
        PowerMockito.when(clusteringAgent.sendMessage(Matchers.any(TenantLoadMessage.class), Matchers.anyBoolean())).
                thenThrow(clusteringFault);
        tenantLoadMessageSender.sendTenantLoadMessage(clusteringAgent, 1, "a.com", 2);
    }

    @Test
    public void testGetTenantId() {
        PrivilegedCarbonContext privilegedCarbonContext = Mockito.mock(PrivilegedCarbonContext.class);
        PowerMockito.mockStatic(PrivilegedCarbonContext.class);
        PowerMockito.when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(privilegedCarbonContext);
        PowerMockito.when(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId()).thenReturn(1);
        Assert.assertEquals("Expected tenant id 1 not returned", 1, tenantLoadMessageSender.getTenantId());
    }

    @Test
    public void testGetTenantDomain() {
        PrivilegedCarbonContext privilegedCarbonContext = Mockito.mock(PrivilegedCarbonContext.class);
        PowerMockito.mockStatic(PrivilegedCarbonContext.class);
        PowerMockito.when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(privilegedCarbonContext);
        PowerMockito.when(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain()).thenReturn("a.com");
        Assert.assertEquals("Expected tenant domain a.com not returned", "a.com",
                tenantLoadMessageSender.getTenantDomain());
    }

    @Test
    public void testGetClusteringAgent() {
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        ConfigurationContextService configurationContextService = PowerMockito.mock(ConfigurationContextService.class);
        PowerMockito.when(ServiceReferenceHolder.getContextService()).thenReturn(configurationContextService);
        ConfigurationContext serverConfigContext = PowerMockito.mock(ConfigurationContext.class);
        PowerMockito.when(ServiceReferenceHolder.getContextService().getServerConfigContext())
                .thenReturn(serverConfigContext);
        AxisConfiguration axisConfiguration = PowerMockito.mock(AxisConfiguration.class);
        PowerMockito.when(ServiceReferenceHolder.getContextService().getServerConfigContext().getAxisConfiguration())
                .thenReturn(axisConfiguration);
        ClusteringAgent clusteringAgent = PowerMockito.mock(ClusteringAgent.class);
        PowerMockito.when(ServiceReferenceHolder.getContextService().getServerConfigContext().getAxisConfiguration()
                .getClusteringAgent()).thenReturn(clusteringAgent);
        tenantLoadMessageSender.getClusteringAgent();
    }

    @Test
    public void testTerminatingConfigurationContext() {
        tenantLoadMessageSender.terminatingConfigurationContext(new ConfigurationContext(new AxisConfiguration()));
    }

    @AfterClass
    public static void tearDown() {
        tenantLoadMessageSender = null;
    }
}
