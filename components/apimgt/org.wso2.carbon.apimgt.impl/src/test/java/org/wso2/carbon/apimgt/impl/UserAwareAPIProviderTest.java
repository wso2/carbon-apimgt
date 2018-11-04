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
        Mockito.doReturn(userRegistry).when(registryService).getConfigSystemRegistry(Mockito.anyInt());
        Mockito.doReturn(userRegistry).when(registryService).getConfigSystemRegistry();
        Mockito.doReturn(resource).when(userRegistry).newResource();
        Mockito.doReturn(null).when(userRegistry).getUserRealm();
        PowerMockito.when(ApiMgtDAO.getInstance()).thenReturn(apiMgtDAO);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        PowerMockito.when(APIUtil.getAPIPath(Mockito.any(APIIdentifier.class))).thenReturn("test");
        PowerMockito.when(APIUtil.getArtifactManager(Mockito.any(Registry.class), Mockito.anyString()))
                .thenReturn(artifactManager);
        PowerMockito.doNothing().when(ServiceReferenceHolder.class, "setUserRealm", Mockito.any());
        PowerMockito.doNothing().when(APIUtil.class, "loadTenantRegistry", Mockito.anyInt());
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
     * This method checks whether checkAccessControlPermission does not throw error, when there is invalid identifier
     * passed.
     *
     * @throws APIManagementException API Management Exception.
     */
    @Test
    public void testCheckAccessControlPermissionWithoutProperIdentifier() throws APIManagementException {
        userAwareAPIProvider.checkAccessControlPermission(null);
    }

    /**
     * This method checks whether checkAccessPermission throws any un-intended errors, when the resource does not
     * exist in the given API path.
     *
     * @throws APIManagementException API Management Exception.
     * @throws RegistryException      Registry Exception.
     */
    @Test
    public void testCheckAccessControlPermissionWhenResource() throws APIManagementException, RegistryException {
        PowerMockito.when(APIUtil.getAPIPath(apiIdentifier)).thenReturn(SAMPLE_IDENTIFIER);
        Mockito.doReturn(false).when(userRegistry).resourceExists(SAMPLE_IDENTIFIER);
        userAwareAPIProvider.checkAccessControlPermission(apiIdentifier);

        Mockito.doReturn(true).when(userRegistry).resourceExists(SAMPLE_IDENTIFIER);
        Mockito.doReturn(null).when(userRegistry).get(SAMPLE_IDENTIFIER);
        userAwareAPIProvider.checkAccessControlPermission(apiIdentifier);
    }

    /**
     * This method checks the behaviour of the checkAccessControlPermission when the API does not have any access
     * control restrictions.
     */
    @Test
    public void testCheckAccessControlPermissionWithoutAccessControlPermission()
            throws RegistryException, APIManagementException {
        PowerMockito.when(APIUtil.getAPIPath(apiIdentifier)).thenReturn(SAMPLE_IDENTIFIER);
        Mockito.doReturn(true).when(userRegistry).resourceExists(SAMPLE_IDENTIFIER);
        Mockito.doReturn(resource).when(userRegistry).get(SAMPLE_IDENTIFIER);
        Mockito.doReturn(APIConstants.NO_ACCESS_CONTROL).when(resource).getProperty(APIConstants.ACCESS_CONTROL);
        userAwareAPIProvider.checkAccessControlPermission(apiIdentifier);
    }

    /**
     * This method checks the behaviour of the checkAccessControlPermission when an admin user tries to access the API.
     *
     * @throws RegistryException      Registry Exception.
     * @throws APIManagementException API Management Exception.
     */
    @Test
    public void testCheckAccessControlPermissionForAPIMAdmin() throws RegistryException, APIManagementException {
        PowerMockito.when(APIUtil.getAPIPath(apiIdentifier)).thenReturn(SAMPLE_IDENTIFIER);
        Mockito.doReturn(true).when(userRegistry).resourceExists(SAMPLE_IDENTIFIER);
        Mockito.doReturn(resource).when(userRegistry).get(SAMPLE_IDENTIFIER);
        Mockito.doReturn(APIConstants.API_RESTRICTED_VISIBILITY).when(resource)
                .getProperty(APIConstants.ACCESS_CONTROL);
        PowerMockito.when(APIUtil.hasPermission(ADMIN_ROLE_NAME, APIConstants.Permissions.APIM_ADMIN))
                .thenReturn(true);
        userAwareAPIProvider.checkAccessControlPermission(apiIdentifier);
    }

    /**
     * This method checks the behaviour of checkAccessControlPermission when the publisher roles is null
     *
     * @throws RegistryException      Registry Exception.
     * @throws APIManagementException API Management Exception.
     */
    @Test
    public void testCheckAccessControlPermissionWithoutPublisherRoles() throws RegistryException,
            APIManagementException {
        PowerMockito.when(APIUtil.getAPIPath(apiIdentifier)).thenReturn(SAMPLE_IDENTIFIER);
        Mockito.doReturn(true).when(userRegistry).resourceExists(SAMPLE_IDENTIFIER);
        Mockito.doReturn(resource).when(userRegistry).get(SAMPLE_IDENTIFIER);
        Mockito.doReturn(APIConstants.API_RESTRICTED_VISIBILITY).when(resource)
                .getProperty(APIConstants.ACCESS_CONTROL);
        PowerMockito.when(APIUtil.hasPermission(ADMIN_ROLE_NAME, APIConstants.Permissions.APIM_ADMIN))
                .thenReturn(false);
        Mockito.doReturn(null).when(resource).getProperty(APIConstants.PUBLISHER_ROLES);
        userAwareAPIProvider.checkAccessControlPermission(apiIdentifier);
    }

    /**
     * This method checks the behaviour of checkAccessControlPermission when the publisher roles is specified.
     *
     * @throws RegistryException      Registry Exception.
     * @throws APIManagementException API Management Exception.
     */
    @Test
    public void testCheckAccessControlPermissionWithPublisherRoles() throws RegistryException, APIManagementException {
        PowerMockito.when(APIUtil.getAPIPath(apiIdentifier)).thenReturn(SAMPLE_IDENTIFIER);
        Mockito.doReturn(true).when(userRegistry).resourceExists(SAMPLE_IDENTIFIER);
        Mockito.doReturn(resource).when(userRegistry).get(SAMPLE_IDENTIFIER);
        Mockito.doReturn(APIConstants.API_RESTRICTED_VISIBILITY).when(resource)
                .getProperty(APIConstants.ACCESS_CONTROL);
        PowerMockito.when(APIUtil.hasPermission(ADMIN_ROLE_NAME, APIConstants.Permissions.APIM_ADMIN))
                .thenReturn(false);
        Mockito.doReturn(ADMIN_ROLE_NAME).when(resource).getProperty(APIConstants.PUBLISHER_ROLES);
        PowerMockito.when(APIUtil.getListOfRoles(ADMIN_ROLE_NAME)).thenReturn(new String[] { ADMIN_ROLE_NAME });
        userAwareAPIProvider.checkAccessControlPermission(apiIdentifier);
    }

    /**
     * This method checks the behaviour of the checkAccessControlPermission when the user is not authorized to view
     * the specific API.
     *
     * @throws RegistryException Registry Exception.
     */
    @Test
    public void testCheckAccessControlPermissionForUnAuthorizedUser() throws RegistryException {
        try {
            PowerMockito.when(APIUtil.getAPIPath(apiIdentifier)).thenReturn(SAMPLE_IDENTIFIER);
            Mockito.doReturn(true).when(userRegistry).resourceExists(SAMPLE_IDENTIFIER);
            Mockito.doReturn(resource).when(userRegistry).get(SAMPLE_IDENTIFIER);
            Mockito.doReturn(APIConstants.API_RESTRICTED_VISIBILITY).when(resource)
                    .getProperty(APIConstants.ACCESS_CONTROL);
            PowerMockito.when(APIUtil.hasPermission(ADMIN_ROLE_NAME, APIConstants.Permissions.APIM_ADMIN))
                    .thenReturn(false);
            Mockito.doReturn(ADMIN_ROLE_NAME).when(resource).getProperty(APIConstants.DISPLAY_PUBLISHER_ROLES);
            PowerMockito.when(APIUtil.getListOfRoles(ADMIN_ROLE_NAME))
                    .thenReturn(new String[] { "Internal/everyone" });
            userAwareAPIProvider.checkAccessControlPermission(apiIdentifier);
            Assert.fail("For a user, who is un-authorized access an API was able to successfully access the API");
        } catch (APIManagementException e) {
            Assert.assertNotNull("Exception is not thrown for an user who is trying to access an un-authorized API", e);
            Assert.assertTrue("Required error message is not present in exception error log",
                    e.getMessage().contains(APIConstants.UN_AUTHORIZED_ERROR_MESSAGE));
        }
    }

    /**
     * This method checks the behaviour of the checkAccessControlPermission method when the registry exception is
     * thrown.
     *
     * @throws RegistryException Registry Exception.
     */
    @Test
    public void testCheckAccessControlPermissionWithRegistryError() throws RegistryException {
        try {
            PowerMockito.when(APIUtil.getAPIPath(apiIdentifier)).thenReturn(SAMPLE_IDENTIFIER);
            Mockito.doThrow(new RegistryException("Error")).when(userRegistry).resourceExists(SAMPLE_IDENTIFIER);
            userAwareAPIProvider.checkAccessControlPermission(apiIdentifier);
            Assert.fail("Registry Exception is not passed to the front-end");
        } catch (APIManagementException e) {
            Assert.assertNotNull("Exception is not thrown for a registry exception", e);
            Assert.assertTrue("Required error message is not present in exception error log",
                    e.getMessage().contains("Registry Exception while"));
        }
    }

    /**
     * This method checks the behaviour of Delete API specific mediation policy method for a non-existing mediation
     * policy.
     *
     * @throws APIManagementException API Management Exception.
     */
    @Test
    public void testDeleteApiSpecificMediationPolicy() throws APIManagementException {
        Assert.assertFalse(userAwareAPIProvider.deleteApiSpecificMediationPolicy("test", "test"));
    }

    /**
     * This method checks the behaviour of getAllDocumentation method when there is no documentation for particular api.
     *
     * @throws APIManagementException API Management Exception.
     */
    @Test
    public void testGetAllDocumentation() throws APIManagementException {
        Assert.assertTrue("Non-existing documentation list for an API ",
                userAwareAPIProvider.getAllDocumentation(apiIdentifier).isEmpty());
    }

    /**
     * This method checks the behaviour of getWsdl method when there is no wsdl for the relevant API.
     *
     * @throws APIManagementException API Management Exception.
     */
    @Test
    public void testGetWsdl() throws APIManagementException {
        Assert.assertNull("Non-existing WSDL file was retrieved successfully",
                userAwareAPIProvider.getWsdl(apiIdentifier));
    }

    /**
     * This methos checks the getLifecycleEvents method of a non-existing API.
     *
     * @throws APIManagementException API Management Exception.
     */
    @Test
    public void testGetLifeCycleEvents() throws APIManagementException {
        Assert.assertTrue("Lifeyclce events is not null for a non-existing API",
                userAwareAPIProvider.getLifeCycleEvents(apiIdentifier).isEmpty());
    }
}
