/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl;

import org.apache.axis2.AxisFault;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.gateway.dto.stub.APIData;
import org.wso2.carbon.apimgt.impl.dto.Environment;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIGatewayAdminClient;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * APIGatewayManager test cases
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({APIGatewayManager.class, PrivilegedCarbonContext.class})
public class APIGatewayManagerTest {

    private APIManagerConfiguration config;
    private Environment environment;
    private APIGatewayManager gatewayManager;
    private APIGatewayAdminClient apiGatewayAdminClient;
    private PrivilegedCarbonContext carbonContext;

    private String apiName = "weatherAPI";
    private String provider = "admin";
    private String version = "v1";
    private String tenantDomain = "wso2.com";
    private String inSequence =
            "<sequence xmlns=\"http://ws.apache.org/ns/synapse\" name=\"in-sequence\">\n" +
                    " <log level=\"custom\">\n" +
                    "    <property name=\"Test\" value=\"API In Sequence\"/>\n" +
                    " </log>\n" +
                    "</sequence>";
    private String outSequence =
            "<sequence xmlns=\"http://ws.apache.org/ns/synapse\" name=\"out-sequence\">\n" +
                    " <log level=\"custom\">\n" +
                    "    <property name=\"Test\" value=\"API Out Sequence\"/>\n" +
                    " </log>\n" +
                    "</sequence>";
    private String faultSequence =
            "<sequence xmlns=\"http://ws.apache.org/ns/synapse\" name=\"fault-sequence\">\n" +
                    " <log level=\"custom\">\n" +
                    "    <property name=\"Test\" value=\"API Fault Sequence\"/>\n" +
                    " </log>\n" +
                    "</sequence>";

    @Before
    public void init() throws Exception {
        config = Mockito.mock(APIManagerConfiguration.class);
        APIManagerConfigurationService apiManagerConfigurationService = new APIManagerConfigurationServiceImpl(config);
        ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(apiManagerConfigurationService);
        System.setProperty("carbon.home", "");


        PowerMockito.mockStatic(PrivilegedCarbonContext.class);
        carbonContext = Mockito.mock(PrivilegedCarbonContext.class);

        PowerMockito.when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(carbonContext);
        PowerMockito.doNothing().when(carbonContext).setTenantDomain(tenantDomain, true);
        environment = new Environment();
        environment.setApiGatewayEndpoint("http://localhost:8280/");
        environment.setName("Production");
        environment.setServerURL("https://localhost:9443/services");
        environment.setUserName("admin");
        environment.setPassword("admin");
        Map<String, Environment> environments = new HashMap<String, Environment>(0);
        environments.put("production", environment);
        Mockito.when(config.getApiGatewayEnvironments()).thenReturn(environments);
        apiGatewayAdminClient = Mockito.mock(APIGatewayAdminClient.class);
        PowerMockito.whenNew(APIGatewayAdminClient.class).withAnyArguments().thenReturn(apiGatewayAdminClient);
        gatewayManager = APIGatewayManager.getInstance();
    }


    @Test
    public void testRemovingAPIFromGateway() throws AxisFault {
        APIIdentifier apiIdentifier = new APIIdentifier(provider, apiName, version);
        API api = new API(apiIdentifier);
        api.setType("HTTP");
        api.setInSequence(inSequence);
        api.setOutSequence(outSequence);
        api.setFaultSequence(faultSequence);
        Set<String> environments = new HashSet<String>();
        environments.add("production");
        api.setEnvironments(environments);
        APIData apiData = Mockito.mock(APIData.class);
        Mockito.when(apiGatewayAdminClient.getApi(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, apiIdentifier))
                .thenReturn(apiData);
        Mockito.when(apiGatewayAdminClient.isExistingSequence(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        Map<String, String> failedEnvironmentsMap = gatewayManager.removeFromGateway(api, MultitenantConstants
                .SUPER_TENANT_DOMAIN_NAME);
        Assert.assertEquals(failedEnvironmentsMap.size(), 0);
    }
}
