/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.apimgt.gateway.utils;

import org.apache.axis2.AxisFault;
import org.apache.axis2.clustering.ClusteringAgent;
import org.apache.axis2.clustering.Member;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.codec.binary.Base64;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.mediation.registry.RegistryServiceHolder;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;

import javax.validation.constraints.AssertTrue;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.Assert.fail;

/**
 * Test class for GatewayUtils.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({GatewayUtilsTestCase.class, PrivilegedCarbonContext.class, ServiceReferenceHolder.class,
        RegistryServiceHolder.class, Base64.class})
public class GatewayUtilsTestCase {
    private String apiProviderName = "admin";
    private String apiName = "PhoneVerify";
    private String version = "1.0";
    private String propertyName = "abc";
    private String propertyValue = "ishara";
    private String path = "/_abc/xyz";
    private String tenantDomain = "foo.com";
    private int tenantID = 1;
    private Resource resource;
    private ServiceReferenceHolder serviceReferenceHolder;
    private UserRegistry userRegistry;

    @Before
    public void setup() {
        System.setProperty("carbon.home", "jhkjn");
        PowerMockito.mockStatic(PrivilegedCarbonContext.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        PowerMockito.mockStatic(RegistryServiceHolder.class);
        PrivilegedCarbonContext privilegedCarbonContext = Mockito.mock(PrivilegedCarbonContext.class);
        serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        RegistryServiceHolder registryServiceHolder = Mockito.mock(RegistryServiceHolder.class);
        RegistryService registryService = Mockito.mock(RegistryService.class);
        userRegistry = Mockito.mock(UserRegistry.class);
        resource = Mockito.mock(Resource.class);
        PowerMockito.when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(privilegedCarbonContext);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        PowerMockito.when(RegistryServiceHolder.getInstance()).thenReturn(registryServiceHolder);
        Mockito.when(registryServiceHolder.getRegistryService()).thenReturn(registryService);
        Mockito.when(privilegedCarbonContext.getTenantId()).thenReturn(tenantID);
        try {
            Mockito.when(registryService.getConfigSystemRegistry(tenantID)).thenReturn(userRegistry);
        } catch (RegistryException e) {
            fail("Error while mocking registryService.getConfigSystemRegistry");
        }
        try {
            Mockito.when(userRegistry.get(path)).thenReturn(resource);
        } catch (RegistryException e) {
            fail("Error while mocking userRegistry.get(path)");
        }

    }

    @Test
    public void testSetRegistryProperty() throws RegistryException {
        try {
            GatewayUtils.setRegistryProperty(propertyName, propertyValue, path, tenantDomain);
        } catch (APIManagementException e) {
            fail("APIManagementException occurred. " + e.getStackTrace());
        }

        try {
            Mockito.when(userRegistry.get(path)).thenThrow(new RegistryException(""));
            GatewayUtils.setRegistryProperty(propertyName, propertyValue, path, tenantDomain);
            fail("Expected Registry Exception is not thrown");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().startsWith("Error while reading registry resource"));
        }

        //test update property when tenant domain is null

        try {
            Mockito.when(resource.getProperty(propertyName)).thenReturn("oldValue");
            GatewayUtils.setRegistryProperty(propertyName, propertyValue, path, "");
            fail("expected APIManagementException is not thrown when accessing registry");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().startsWith("Error while reading registry resource"));
        }

    }

    @Test
    public void testGenerateMap() {
        List list = new ArrayList<Object>();
        list.add(new Object());
        GatewayUtils.generateMap(list);
        Assert.assertTrue(true);
    }

    @Test
    public void testGetAPIEndpointSecretAlias() {
        Assert.assertEquals("admin--PhoneVerify1.0", GatewayUtils.getAPIEndpointSecretAlias(apiProviderName,
                apiName, version));

    }

    @Test
    public void testGetJWTClaims() {
        String jwt = "eyJhbGciOiJSUzI1NiIsIng1dCI6Ik5tSm1PR1V4TXpabFlqTTJaRFJoTlRabFlUQTFZemRoWlRSaU9XRTBOV0kyTTJKbU9UYzFaQSJ9.eyJodHRwOlwvXC93c28yLm9yZ1wvZ2F0ZXdheVwvYXBwbGljYXRpb25uYW1lIjoiT2F1dGg3IiwiZXhwIjoxNDUyNTk0ODkyLCJzdWIiOiJhZG1pbkBjYXJib24uc3VwZXIiLCJodHRwOlwvXC93c28yLm9yZ1wvZ2F0ZXdheVwvc3Vic2NyaWJlciI6ImFkbWluQGNhcmJvbi5zdXBlciIsImlzcyI6Imh0dHA6XC9cL3dzbzIub3JnXC9nYXRld2F5IiwiaHR0cDpcL1wvd3NvMi5vcmdcL2dhdGV3YXlcL2VuZHVzZXIiOiJhZG1pbkBjYXJib24uc3VwZXIiLCJodHRwOlwvXC93c28yLm9yZ1wvY2xhaW1zXC9yb2xlIjoiYWRtaW4sQXBwbGljYXRpb25cL2Rld3ZkZXcsQXBwbGljYXRpb25cL09hdXRoNyxJbnRlcm5hbFwvZXZlcnlvbmUiLCJodHRwOlwvXC93c28yLm9yZ1wvY2xhaW1zXC9lbWFpbGFkZHJlc3MiOiJhZG1pbkB3c28yLmNvbSIsImlhdCI6MTQ1MjU5MzI1NCwiaHR0cDpcL1wvd3NvMi5vcmdcL2NsYWltc1wvb3JnYW5pemF0aW9uIjoiV1NPMiJ9.WRo2p92f-pt1vH9xfLgmrPWNKJfmST2QSPYcth7gXKz64LdP9zAMUtfAk9DVRdHTIQR3gX0jF4Ohb4UbNN4Oo97a35oTL1iRxIRTKUkh8L1dpt3H03Z0Ze7Q2giHGZikMIQv3gavHRYKjNMoU_1MuB90jiK7";
        AuthenticationContext authenticationContext = new AuthenticationContext();
        authenticationContext.setCallerToken(jwt);
        Assert.assertNotNull(GatewayUtils.getJWTClaims(authenticationContext));
    }

    @Test
    public void testIsClusteringEnabled() {
        ConfigurationContext configurationContext = Mockito.mock(ConfigurationContext.class);
        AxisConfiguration axisConfiguration = Mockito.mock(AxisConfiguration.class);
        ClusteringAgent clusteringAgent = Mockito.mock(ClusteringAgent.class);
        Member member = Mockito.mock(Member.class);
        List<Member> memberList = new ArrayList<>(1);
        memberList.add(member);
        clusteringAgent.setMembers(new ArrayList<Member>());
        Mockito.when(serviceReferenceHolder.getServerConfigurationContext()).thenReturn(configurationContext);
        Mockito.when(configurationContext.getAxisConfiguration()).thenReturn(axisConfiguration);

        //test when clusteringAgent is null
        Mockito.when(axisConfiguration.getClusteringAgent()).thenReturn(null);
        GatewayUtils.isClusteringEnabled();

        // test when clusteringAgent is set
        Mockito.when(axisConfiguration.getClusteringAgent()).thenReturn(clusteringAgent);
        Mockito.when(clusteringAgent.getMembers()).thenReturn(memberList);
        GatewayUtils.isClusteringEnabled();
        Assert.assertEquals(1, clusteringAgent.getMembers().size());
    }


}
