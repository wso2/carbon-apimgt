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
package org.wso2.carbon.apimgt.impl.observers;

import org.apache.axis2.context.ConfigurationContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import static org.wso2.carbon.base.CarbonBaseConstants.CARBON_HOME;


@RunWith(PowerMockRunner.class)
@PrepareForTest({APIUtil.class, PrivilegedCarbonContext.class, ServiceReferenceHolder.class })
public class CommonConfigDeployerTestCase {

    private final int TENANT_ID = 1234;
    private final String TENANT_DOMAIN = "foo.com";

    //@Test
    public void testCreatedConfigurationContext() throws APIManagementException {
        System.setProperty(CARBON_HOME, "");
        PrivilegedCarbonContext privilegedCarbonContext = Mockito.mock(PrivilegedCarbonContext.class);
        PowerMockito.mockStatic(PrivilegedCarbonContext.class);
        PowerMockito.when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(privilegedCarbonContext);
        Mockito.when(privilegedCarbonContext.getTenantDomain()).thenReturn(TENANT_DOMAIN);
        Mockito.when(privilegedCarbonContext.getTenantId()).thenReturn(TENANT_ID);

        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);

        APIManagerConfigurationService apiManagerConfigurationService = Mockito
                .mock(APIManagerConfigurationService.class);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfigurationService())
                .thenReturn(apiManagerConfigurationService);

        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        Mockito.when(apiManagerConfigurationService.getAPIManagerConfiguration()).thenReturn(apiManagerConfiguration);
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.API_GATEWAY_TYPE))
                .thenReturn(APIConstants.API_GATEWAY_TYPE_SYNAPSE);

        ConfigurationContext configurationContext = Mockito.mock(ConfigurationContext.class);
        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.when(APIUtil.isAdvanceThrottlingEnabled()).thenReturn(true);

        CommonConfigDeployer commonConfigDeployer = new CommonConfigDeployer();
        commonConfigDeployer.createdConfigurationContext(configurationContext);

        PowerMockito.verifyStatic(APIUtil.class);
        APIUtil.createDefaultRoles(TENANT_ID);

        PowerMockito.verifyStatic(APIUtil.class);
        APIUtil.loadTenantConf(TENANT_ID);

        PowerMockito.verifyStatic(APIUtil.class);
        APIUtil.addDefaultTenantAdvancedThrottlePolicies(TENANT_DOMAIN, TENANT_ID);
    }


    @Test
    public void testExceptions() throws Exception {
        System.setProperty(CARBON_HOME, "");
        PrivilegedCarbonContext privilegedCarbonContext = Mockito.mock(PrivilegedCarbonContext.class);
        PowerMockito.mockStatic(PrivilegedCarbonContext.class);
        PowerMockito.when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(privilegedCarbonContext);
        Mockito.when(privilegedCarbonContext.getTenantDomain()).thenReturn(TENANT_DOMAIN);
        Mockito.when(privilegedCarbonContext.getTenantId()).thenReturn(TENANT_ID);

        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);

        APIManagerConfigurationService apiManagerConfigurationService = Mockito
                .mock(APIManagerConfigurationService.class);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfigurationService())
                .thenReturn(apiManagerConfigurationService);

        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        Mockito.when(apiManagerConfigurationService.getAPIManagerConfiguration()).thenReturn(apiManagerConfiguration);
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.API_GATEWAY_TYPE))
                .thenReturn(APIConstants.API_GATEWAY_TYPE_SYNAPSE);

        ConfigurationContext configurationContext = Mockito.mock(ConfigurationContext.class);
        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.when(APIUtil.isAdvanceThrottlingEnabled()).thenReturn(true);

        PowerMockito.doThrow(new APIManagementException("error")).when(APIUtil.class);
        APIUtil.loadTenantAPIPolicy(TENANT_DOMAIN, TENANT_ID);
        PowerMockito.doThrow(new APIManagementException("error")).when(APIUtil.class);
        APIUtil.addDefaultTenantAdvancedThrottlePolicies(TENANT_DOMAIN, TENANT_ID);
        PowerMockito.doThrow(new APIManagementException("error")).when(APIUtil.class);
        APIUtil.writeDefinedSequencesToTenantRegistry(TENANT_ID);
        PowerMockito.doThrow(new APIManagementException("error")).when(APIUtil.class);
        APIUtil.loadTenantExternalStoreConfig(TENANT_ID);
        PowerMockito.doThrow(new APIManagementException("error")).when(APIUtil.class);
        APIUtil.loadTenantGAConfig(TENANT_ID);
        PowerMockito.doThrow(new APIManagementException("error")).when(APIUtil.class);
        APIUtil.loadTenantWorkFlowExtensions(TENANT_ID);
        PowerMockito.doThrow(new APIManagementException("error")).when(APIUtil.class);
        APIUtil.loadTenantSelfSignUpConfigurations(TENANT_ID);
        PowerMockito.doThrow(new APIManagementException("error")).when(APIUtil.class);
        APIUtil.loadTenantConf(TENANT_ID);
        PowerMockito.doThrow(new APIManagementException("error")).when(APIUtil.class);
        APIUtil.createDefaultRoles(TENANT_ID);

        CommonConfigDeployer commonConfigDeployer = new CommonConfigDeployer();
        commonConfigDeployer.createdConfigurationContext(configurationContext);

        PowerMockito.verifyStatic(APIUtil.class, Mockito.times(1));
        APIUtil.createDefaultRoles(TENANT_ID);

    }

    @Test
    public void testCreatedConfigurationContextRuntimeException() throws APIManagementException {
        System.setProperty(CARBON_HOME, "");
        PrivilegedCarbonContext privilegedCarbonContext = Mockito.mock(PrivilegedCarbonContext.class);
        PowerMockito.mockStatic(PrivilegedCarbonContext.class);
        PowerMockito.when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(privilegedCarbonContext);
        Mockito.when(privilegedCarbonContext.getTenantDomain()).thenReturn(TENANT_DOMAIN);
        Mockito.when(privilegedCarbonContext.getTenantId()).thenReturn(TENANT_ID);

        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);

        APIManagerConfigurationService apiManagerConfigurationService = Mockito
                .mock(APIManagerConfigurationService.class);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfigurationService())
                .thenReturn(apiManagerConfigurationService);

        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        Mockito.when(apiManagerConfigurationService.getAPIManagerConfiguration()).thenReturn(apiManagerConfiguration);
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.API_GATEWAY_TYPE))
                .thenReturn(APIConstants.API_GATEWAY_TYPE_SYNAPSE);

        ConfigurationContext configurationContext = Mockito.mock(ConfigurationContext.class);
        PowerMockito.mockStatic(APIUtil.class);

        PowerMockito.doThrow(new RuntimeException("error")).when(APIUtil.class);
        APIUtil.isAdvanceThrottlingEnabled();

        PowerMockito.doThrow(new RuntimeException("error")).when(APIUtil.class);
        APIUtil.loadTenantConf(TENANT_ID);
        PowerMockito.doThrow(new RuntimeException("error")).when(APIUtil.class);
        APIUtil.createDefaultRoles(TENANT_ID);
        CommonConfigDeployer commonConfigDeployer = new CommonConfigDeployer();
        commonConfigDeployer.createdConfigurationContext(configurationContext);

        PowerMockito.verifyStatic(APIUtil.class, Mockito.times(1));
        APIUtil.createDefaultRoles(TENANT_ID);
    }
}
