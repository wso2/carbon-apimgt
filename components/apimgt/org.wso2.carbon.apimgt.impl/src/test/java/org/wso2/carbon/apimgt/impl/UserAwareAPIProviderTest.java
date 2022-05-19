/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.apimgt.impl;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.Identifier;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ApiMgtDAO.class, ServiceReferenceHolder.class, APIUtil.class, RegistryUtils.class,
        PrivilegedCarbonContext.class })
public class UserAwareAPIProviderTest {
    private UserAwareAPIProvider userAwareAPIProvider;
    private UserRegistry userRegistry;
    private Resource resource;
    private final String ADMIN_ROLE_NAME = "admin";
    private final String SAMPLE_IDENTIFIER = "identifier1";
    private APIIdentifier apiIdentifier;

    @Before
    public void init() throws Exception {
        System.setProperty("carbon.home", "");
        apiIdentifier = new APIIdentifier("admin_identifier1_v1.0");
        PowerMockito.mockStatic(ApiMgtDAO.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.mockStatic(RegistryUtils.class);
        PowerMockito.mockStatic(PrivilegedCarbonContext.class);
        ApiMgtDAO apiMgtDAO = Mockito.mock(ApiMgtDAO.class);
        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        TenantManager tenantManager = Mockito.mock(TenantManager.class);
        RealmService realmService = Mockito.mock(RealmService.class);
        RegistryService registryService = Mockito.mock(RegistryService.class);
        userRegistry = Mockito.mock(UserRegistry.class);
        GenericArtifactManager artifactManager = Mockito.mock(GenericArtifactManager.class);
        APIManagerConfigurationService apiManagerConfigurationService = Mockito
                .mock(APIManagerConfigurationService.class);
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        resource = Mockito.mock(Resource.class, Mockito.CALLS_REAL_METHODS);
        Mockito.doReturn(apiManagerConfiguration).when(apiManagerConfigurationService).getAPIManagerConfiguration();
        Mockito.doReturn(APIConstants.API_GATEWAY_TYPE_SYNAPSE).when(apiManagerConfiguration)
                .getFirstProperty(APIConstants.API_GATEWAY_TYPE);
        Mockito.doReturn("true").when(apiManagerConfiguration)
                .getFirstProperty(APIConstants.API_PUBLISHER_ENABLE_ACCESS_CONTROL_LEVELS);
        Mockito.doReturn(userRegistry).when(registryService)
                .getGovernanceUserRegistry(Mockito.anyString(), Mockito.anyInt());
        Mockito.doReturn(userRegistry).when(registryService)
                .getGovernanceSystemRegistry();
        Mockito.doReturn(userRegistry).when(registryService).getConfigSystemRegistry(Mockito.anyInt());
        Mockito.doReturn(userRegistry).when(registryService).getConfigSystemRegistry();
        Mockito.doReturn(resource).when(userRegistry).newResource();
        Mockito.doReturn(null).when(userRegistry).getUserRealm();
        PowerMockito.when(ApiMgtDAO.getInstance()).thenReturn(apiMgtDAO);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        PowerMockito.when(APIUtil.getAPIPath(Mockito.any(APIIdentifier.class))).thenReturn("test");
        PowerMockito.when(APIUtil.getArtifactManager(Mockito.any(Registry.class), Mockito.anyString()))
                .thenReturn(artifactManager);
        PowerMockito.when(APIUtil.getInternalOrganizationDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME))
                .thenReturn(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        PowerMockito.doNothing().when(APIUtil.class, "loadTenantRegistry", Mockito.anyInt());
        PowerMockito.when(APIUtil.replaceEmailDomainBack(apiIdentifier.getProviderName())).
                thenReturn(apiIdentifier.getProviderName());
        Mockito.doReturn(realmService).when(serviceReferenceHolder).getRealmService();
        Mockito.doReturn(tenantManager).when(realmService).getTenantManager();
        Mockito.doReturn(registryService).when(serviceReferenceHolder).getRegistryService();
        Mockito.doReturn(apiManagerConfigurationService).when(serviceReferenceHolder)
                .getAPIManagerConfigurationService();
        PowerMockito.when(APIUtil.compareRoleList(Mockito.any(String[].class), Mockito.anyString()))
                .thenCallRealMethod();
        ConfigurationContextService configurationContextService = TestUtils.initConfigurationContextService(true);
        PowerMockito.when(ServiceReferenceHolder.getContextService()).thenReturn(configurationContextService);
        userAwareAPIProvider = new UserAwareAPIProvider(ADMIN_ROLE_NAME);
        PrivilegedCarbonContext prcontext = Mockito.mock(PrivilegedCarbonContext.class);
        PowerMockito.when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(prcontext);
    }


    /**
     * This methos checks the getLifecycleEvents method of a non-existing API.
     *
     * @throws APIManagementException API Management Exception.
     */
    @Test
    public void testGetLifeCycleEvents() throws APIManagementException {
        Assert.assertTrue("Lifecycle events is not null for a non-existing API",
                userAwareAPIProvider.getLifeCycleEvents(apiIdentifier.getUUID()).isEmpty());
    }
}
