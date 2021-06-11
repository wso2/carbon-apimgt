/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.WorkflowStatus;
import org.wso2.carbon.apimgt.api.dto.KeyManagerConfigurationDTO;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIKey;
import org.wso2.carbon.apimgt.api.model.APIRating;
import org.wso2.carbon.apimgt.api.model.AccessTokenInfo;
import org.wso2.carbon.apimgt.api.model.AccessTokenRequest;
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.Comment;
import org.wso2.carbon.apimgt.api.model.KeyManager;
import org.wso2.carbon.apimgt.api.model.KeyManagerConfiguration;
import org.wso2.carbon.apimgt.api.model.OAuthAppRequest;
import org.wso2.carbon.apimgt.api.model.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.SubscriptionWorkflowDTO;
import org.wso2.carbon.apimgt.impl.dto.TierPermissionDTO;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.impl.factory.KeyManagerHolder;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APINameComparator;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.utils.ApplicationUtils;
import org.wso2.carbon.apimgt.impl.workflow.AbstractApplicationRegistrationWorkflowExecutor;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowException;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowExecutorFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifactImpl;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.common.TermData;
import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.Tag;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.dataobjects.ResourceDO;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.AuthorizationManager;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.xml.namespace.QName;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.wso2.carbon.base.CarbonBaseConstants.CARBON_HOME;


@RunWith(PowerMockRunner.class)
@PrepareForTest({PrivilegedCarbonContext.class, WorkflowExecutorFactory.class, APIUtil.class, GovernanceUtils.class, ApplicationUtils.class,
        KeyManagerHolder.class, WorkflowExecutorFactory.class, AbstractApplicationRegistrationWorkflowExecutor.class,
        ServiceReferenceHolder.class, MultitenantUtils.class, RegistryUtils.class, Caching.class})
@SuppressStaticInitializationFor({"org.wso2.carbon.apimgt.impl.utils.ApplicationUtils"})
public class APIConsumerImplTest {

    private static final Log log = LogFactory.getLog(APIConsumerImplTest.class);
    private ApiMgtDAO apiMgtDAO;
    private UserRealm userRealm;
    private RealmService realmService;
    private PrivilegedCarbonContext privilegedCarbonContext;
    private ServiceReferenceHolder serviceReferenceHolder;
    private TenantManager tenantManager;
    private UserStoreManager userStoreManager;
    private KeyManager keyManager;
    private GenericArtifactManager genericArtifactManager;
    private Registry registry;
    private UserRegistry userRegistry;
    private AuthorizationManager authorizationManager;
    private KeyManagerConfigurationDTO keyManagerConfigurationDTO;
    private static final String SAMPLE_API_NAME = "test";
    private static final String API_PROVIDER = "admin";
    private static final String SAMPLE_API_VERSION = "1.0.0";
    private RegistryService registryService;
    public static final String SAMPLE_TENANT_DOMAIN_1 = "abc.com";

    @Before
    public void init() throws UserStoreException, RegistryException, APIManagementException {
        System.setProperty(CARBON_HOME, "");
        privilegedCarbonContext = Mockito.mock(PrivilegedCarbonContext.class);
        apiMgtDAO = Mockito.mock(ApiMgtDAO.class);
        userRealm = Mockito.mock(UserRealm.class);
        serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        realmService = Mockito.mock(RealmService.class);
        tenantManager = Mockito.mock(TenantManager.class);
        userStoreManager = Mockito.mock(UserStoreManager.class);
        keyManager = Mockito.mock(KeyManager.class);
        registryService = Mockito.mock(RegistryService.class);
        genericArtifactManager = Mockito.mock(GenericArtifactManager.class);
        registry = Mockito.mock(Registry.class);
        userRegistry = Mockito.mock(UserRegistry.class);
        authorizationManager = Mockito.mock(AuthorizationManager.class);
        authorizationManager = Mockito.mock(AuthorizationManager.class);
        keyManagerConfigurationDTO = Mockito.mock(KeyManagerConfigurationDTO.class);
        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.mockStatic(PrivilegedCarbonContext.class);
        PowerMockito.mockStatic(ApplicationUtils.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        PowerMockito.mockStatic(MultitenantUtils.class);
        PowerMockito.mockStatic(KeyManagerHolder.class);
        PowerMockito.mockStatic(RegistryUtils.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        PowerMockito.when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(privilegedCarbonContext);
        PowerMockito.when(privilegedCarbonContext.getUsername()).thenReturn("admin");
        Mockito.when(serviceReferenceHolder.getRealmService()).thenReturn(realmService);
        Mockito.when(realmService.getTenantUserRealm(Mockito.anyInt())).thenReturn(userRealm);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        Mockito.when(userRealm.getUserStoreManager()).thenReturn(userStoreManager);
        Mockito.when(serviceReferenceHolder.getRegistryService()).thenReturn(registryService);
        Mockito.when(registryService.getGovernanceSystemRegistry(Mockito.anyInt())).thenReturn(userRegistry);
        Mockito.when(userRealm.getAuthorizationManager()).thenReturn(authorizationManager);
        Mockito.when(apiMgtDAO.getKeyManagerConfigurationByName(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(keyManagerConfigurationDTO);
        Mockito.when(KeyManagerHolder.getKeyManagerInstance(Mockito.anyString(),Mockito.anyString()))
                .thenReturn(keyManager);
        PowerMockito.when(APIUtil.replaceSystemProperty(anyString())).thenAnswer((Answer<String>) invocation -> {
            Object[] args = invocation.getArguments();
            return (String) args[0];
        });

        PowerMockito.when(keyManagerConfigurationDTO.getTenantDomain()).thenReturn("carbon.super");
        PowerMockito.when(keyManagerConfigurationDTO.getUuid()).thenReturn("kmv72L9T0oGtQcBwvgROZWCqd7oa");
        PowerMockito.when(keyManagerConfigurationDTO.isEnabled()).thenReturn(true);
    }

    @Test
    public void testReadMonetizationConfig() throws UserStoreException, RegistryException,
            APIManagementException {

        APIMRegistryService apimRegistryService = Mockito.mock(APIMRegistryService.class);
        String json = "{\"EnableMonetization\":\"true\"}";
        when(apimRegistryService.getConfigRegistryResourceContent(Mockito.anyString(), Mockito.anyString())).thenReturn(json);
        APIConsumerImpl apiConsumer = new APIConsumerImplWrapper();
        apiConsumer.apimRegistryService = apimRegistryService;
        boolean isEnabled = apiConsumer.isMonetizationEnabled(MultitenantConstants.TENANT_DOMAIN);
        assertTrue("Expected true but returned " + isEnabled, isEnabled);
        Mockito.reset(apimRegistryService);

        // error path UserStoreException
        when(apimRegistryService.getConfigRegistryResourceContent(Mockito.anyString(), Mockito.anyString()))
                .thenThrow(UserStoreException.class);
        try {
            apiConsumer.isMonetizationEnabled(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            assertFalse(true);
        } catch (APIManagementException e) {
            assertEquals("UserStoreException thrown when getting API tenant config from registry", e.getMessage());
        }

        // error path apimRegistryService
        Mockito.reset(apimRegistryService);
        when(apimRegistryService.getConfigRegistryResourceContent(Mockito.anyString(), Mockito.anyString()))
                .thenThrow(RegistryException.class);
        try {
            apiConsumer.isMonetizationEnabled(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            assertFalse(true);
        } catch (APIManagementException e) {
            assertEquals("RegistryException thrown when getting API tenant config from registry", e.getMessage());
        }

        // error path ParseException
        Mockito.reset(apimRegistryService);
        String jsonInvalid = "{EnableMonetization:true}";
        when(apimRegistryService.getConfigRegistryResourceContent(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(jsonInvalid);
        try {
            apiConsumer.isMonetizationEnabled(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            assertFalse(true);
        } catch (APIManagementException e) {
            assertEquals("ParseException thrown when passing API tenant config from registry", e.getMessage());
        }

    }

    @Test
    public void testGetSubscriber() throws APIManagementException {
        APIConsumerImpl apiConsumer = new APIConsumerImplWrapper();
        when(apiMgtDAO.getSubscriber(Mockito.anyString())).thenReturn(new Subscriber(UUID.randomUUID().toString()));
        apiConsumer.apiMgtDAO = apiMgtDAO;
        assertNotNull(apiConsumer.getSubscriber(UUID.randomUUID().toString()));

        when(apiMgtDAO.getSubscriber(Mockito.anyString())).thenThrow(APIManagementException.class);
        try {
            apiConsumer.getSubscriber(UUID.randomUUID().toString());
            assertTrue(false);
        } catch (APIManagementException e) {
            assertEquals("Failed to get Subscriber", e.getMessage());
        }
    }

    @Test
    public void testGetAPIsWithTag() throws Exception {
        APIConsumerImpl apiConsumer = new APIConsumerImplWrapper();
        PowerMockito.doNothing().when(APIUtil.class, "loadTenantRegistry", Mockito.anyInt());
        PowerMockito.mockStatic(GovernanceUtils.class);
        GenericArtifactManager artifactManager = Mockito.mock(GenericArtifactManager.class);
        PowerMockito.when(APIUtil.getArtifactManager(apiConsumer.registry, APIConstants.API_KEY)).
                thenReturn(artifactManager);
        List<GovernanceArtifact> governanceArtifacts = new ArrayList<GovernanceArtifact>();
        GenericArtifact artifact = Mockito.mock(GenericArtifact.class);
        governanceArtifacts.add(artifact);
        Mockito.when(GovernanceUtils.findGovernanceArtifacts(Mockito.anyString(),(UserRegistry)Mockito.anyObject(),
                Mockito.anyString())).thenReturn(governanceArtifacts);
        APIIdentifier apiId1 = new APIIdentifier("admin", "API1", "1.0.0");
        API api = new API(apiId1);
        Mockito.when(APIUtil.getAPI(artifact)).thenReturn(api);
        Mockito.when(artifact.getAttribute("overview_status")).thenReturn("PUBLISHED");
        assertNotNull(apiConsumer.getAPIsWithTag("testTag", "testDomain"));
    }

    @Test
    public void testGetAllPaginatedPublishedAPIs() throws Exception {
        APIConsumerImpl apiConsumer = new APIConsumerImplWrapper();
        PowerMockito.doNothing().when(APIUtil.class, "loadTenantRegistry", Mockito.anyInt());
        PowerMockito.when(APIUtil.isAllowDisplayMultipleVersions()).thenReturn(false, true);
        System.setProperty(CARBON_HOME, "");

        GenericArtifactManager artifactManager = Mockito.mock(GenericArtifactManager.class);
        PowerMockito.when(APIUtil.getArtifactManager(apiConsumer.registry, APIConstants.API_KEY)).
                thenReturn(artifactManager);
        GenericArtifact artifact = Mockito.mock(GenericArtifact.class);
        GenericArtifact[] genericArtifacts = new GenericArtifact[]{artifact};
        Mockito.when(artifactManager.findGenericArtifacts(Mockito.anyMap())).thenReturn(genericArtifacts);
        APIIdentifier apiId1 = new APIIdentifier("admin", "API1", "1.0.0");
        API api = new API(apiId1);
        Mockito.when(APIUtil.getAPI(artifact)).thenReturn(api);
        assertNotNull(apiConsumer.getAllPaginatedPublishedAPIs(MultitenantConstants
                .SUPER_TENANT_DOMAIN_NAME, 0, 10));
        assertNotNull(apiConsumer.getAllPaginatedPublishedAPIs(MultitenantConstants
                .SUPER_TENANT_DOMAIN_NAME, 0, 10));

        //artifact manager null path
        PowerMockito.when(APIUtil.getArtifactManager((UserRegistry)(Mockito.anyObject()), Mockito.anyString())).
                thenReturn(null);
        assertNotNull(apiConsumer.getAllPaginatedPublishedAPIs(MultitenantConstants
                .SUPER_TENANT_DOMAIN_NAME, 0, 10));

        //generic artifact null path
        PowerMockito.when(APIUtil.getArtifactManager((UserRegistry)(Mockito.anyObject()), Mockito.anyString())).
                thenReturn(artifactManager);
        Mockito.when(artifactManager.findGenericArtifacts(Mockito.anyMap())).thenReturn(null);
        assertNotNull(apiConsumer.getAllPaginatedPublishedAPIs(MultitenantConstants
                .SUPER_TENANT_DOMAIN_NAME, 0, 10));
    }

    @Test
    public void testGetAllPaginatedPublishedLightWeightAPIs() throws Exception {
        APIConsumerImpl apiConsumer = new APIConsumerImplWrapper();
        PowerMockito.doNothing().when(APIUtil.class, "loadTenantRegistry", Mockito.anyInt());
        PowerMockito.when(APIUtil.isAllowDisplayMultipleVersions()).thenReturn(false, true);
        System.setProperty(CARBON_HOME, "");

        GenericArtifactManager artifactManager = Mockito.mock(GenericArtifactManager.class);
        PowerMockito.when(APIUtil.getArtifactManager(apiConsumer.registry, APIConstants.API_KEY)).
                thenReturn(artifactManager);
        GenericArtifact artifact = Mockito.mock(GenericArtifact.class);
        GenericArtifact[] genericArtifacts = new GenericArtifact[]{artifact};
        Mockito.when(artifactManager.findGenericArtifacts(Mockito.anyMap())).thenReturn(genericArtifacts);
        APIIdentifier apiId1 = new APIIdentifier("admin", "API1", "1.0.0");
        API api = new API(apiId1);
        Mockito.when(APIUtil.getLightWeightAPI(artifact)).thenReturn(api);
        assertNotNull(apiConsumer.getAllPaginatedPublishedLightWeightAPIs(MultitenantConstants
                .SUPER_TENANT_DOMAIN_NAME, 0, 10));
        assertNotNull(apiConsumer.getAllPaginatedPublishedLightWeightAPIs(MultitenantConstants
                .SUPER_TENANT_DOMAIN_NAME, 0, 10));

        //artifact manager null path
        PowerMockito.when(APIUtil.getArtifactManager((UserRegistry)(Mockito.anyObject()), Mockito.anyString())).
                thenReturn(null);
        assertNotNull(apiConsumer.getAllPaginatedPublishedLightWeightAPIs(MultitenantConstants
                .SUPER_TENANT_DOMAIN_NAME, 0, 10));

        //generic artifact null path
        PowerMockito.when(APIUtil.getArtifactManager((UserRegistry)(Mockito.anyObject()), Mockito.anyString())).
                thenReturn(artifactManager);
        Mockito.when(artifactManager.findGenericArtifacts(Mockito.anyMap())).thenReturn(null);
        assertNotNull(apiConsumer.getAllPaginatedPublishedLightWeightAPIs(MultitenantConstants
                .SUPER_TENANT_DOMAIN_NAME, 0, 10));
    }

    @Test
    public void testGetAllPaginatedAPIs() throws Exception {
        Registry userRegistry = Mockito.mock(Registry.class);
        APIConsumerImpl apiConsumer = new APIConsumerImplWrapper(userRegistry, apiMgtDAO);
        PowerMockito.mockStatic(GovernanceUtils.class);
        PowerMockito.doNothing().when(APIUtil.class, "loadTenantRegistry", Mockito.anyInt());
        PowerMockito.when(APIUtil.isAllowDisplayMultipleVersions()).thenReturn(false, true);
        UserRegistry userRegistry1 = Mockito.mock(UserRegistry.class);
        Mockito.when(registryService.getGovernanceUserRegistry(Mockito.anyString(), Mockito.anyInt())).
                thenReturn(userRegistry1);
        Mockito.when(registryService.getGovernanceSystemRegistry(Mockito.anyInt())).thenReturn(userRegistry1);
        GenericArtifactManager artifactManager = Mockito.mock(GenericArtifactManager.class);
        PowerMockito.when(APIUtil.getArtifactManager((UserRegistry)(Mockito.anyObject()), Mockito.anyString())).
                thenReturn(artifactManager);
        GenericArtifact artifact = Mockito.mock(GenericArtifact.class);
        GenericArtifact[] genericArtifacts = new GenericArtifact[]{artifact};
        Mockito.when(artifactManager.findGenericArtifacts(Mockito.anyMap())).thenReturn(genericArtifacts);
        APIIdentifier apiId1 = new APIIdentifier("admin", "API1", "1.0.0");
        API api = new API(apiId1);
        Mockito.when(APIUtil.getAPI(artifact)).thenReturn(api);
        assertNotNull(apiConsumer.getAllPaginatedAPIs(MultitenantConstants
                .SUPER_TENANT_DOMAIN_NAME, 0, 10));
        assertNotNull(apiConsumer.getAllPaginatedAPIs(MultitenantConstants
                .SUPER_TENANT_DOMAIN_NAME, 0, 10));

        //artifact manager null path
        PowerMockito.when(APIUtil.getArtifactManager((UserRegistry)(Mockito.anyObject()), Mockito.anyString())).
                thenReturn(null);
        assertNotNull(apiConsumer.getAllPaginatedAPIs(MultitenantConstants
                .SUPER_TENANT_DOMAIN_NAME, 0, 10));

        //generic artifact null path
        PowerMockito.when(APIUtil.getArtifactManager((UserRegistry)(Mockito.anyObject()), Mockito.anyString())).
                thenReturn(artifactManager);
        Mockito.when(artifactManager.findGenericArtifacts(Mockito.anyMap())).thenReturn(null);
        assertNotNull(apiConsumer.getAllPaginatedAPIs(MultitenantConstants
                .SUPER_TENANT_DOMAIN_NAME, 0, 10));
    }

    @Test
    public void testGetAllPaginatedAPIsByStatus() throws Exception {
        Registry userRegistry = Mockito.mock(Registry.class);
        APIConsumerImpl apiConsumer = new APIConsumerImplWrapper(userRegistry, apiMgtDAO);
        System.setProperty(CARBON_HOME, "");

        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        APIManagerConfigurationService apiManagerConfigurationService = Mockito.
                mock(APIManagerConfigurationService.class);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfigurationService()).thenReturn(apiManagerConfigurationService);
        Mockito.when(apiManagerConfigurationService.getAPIManagerConfiguration()).thenReturn(apiManagerConfiguration);
        Mockito.when(apiManagerConfiguration.getFirstProperty(Mockito.anyString())).thenReturn("10", "20");

        PowerMockito.mockStatic(GovernanceUtils.class);
        PowerMockito.mockStatic(GovernanceUtils.class);
        PowerMockito.doNothing().when(APIUtil.class, "loadTenantRegistry", Mockito.anyInt());
        GenericArtifactManager artifactManager = Mockito.mock(GenericArtifactManager.class);
        PowerMockito.when(APIUtil.isAllowDisplayMultipleVersions()).thenReturn(false, true);
        PowerMockito.when(APIUtil.getArtifactManager((UserRegistry)(Mockito.anyObject()), Mockito.anyString())).
                thenReturn(artifactManager);
        GenericArtifact artifact = Mockito.mock(GenericArtifact.class);
        GenericArtifact[] genericArtifacts = new GenericArtifact[]{artifact};
        Mockito.when(artifactManager.findGenericArtifacts(Mockito.anyMap())).thenReturn(genericArtifacts);
        APIIdentifier apiId1 = new APIIdentifier("admin", "API1", "1.0.0");
        API api = new API(apiId1);
        Mockito.when(APIUtil.getAPI(artifact)).thenReturn(api);
        String artifactPath = "artifact/path";
        PowerMockito.when(GovernanceUtils.getArtifactPath(userRegistry, artifact.getId())).
                thenReturn(artifactPath);
        Tag tag = new Tag();
        Tag[] tags = new Tag[]{tag};
        Mockito.when(userRegistry.getTags(artifactPath)).thenReturn(tags);
        assertNotNull(apiConsumer.getAllPaginatedAPIsByStatus(MultitenantConstants
                .SUPER_TENANT_DOMAIN_NAME, 0, 10, "testStatus", false));
        assertNotNull(apiConsumer.getAllPaginatedAPIsByStatus(MultitenantConstants
                .SUPER_TENANT_DOMAIN_NAME, 0, 10, "testStatus", true));

        //artifact manager null path
        PowerMockito.when(APIUtil.getArtifactManager((UserRegistry)(Mockito.anyObject()), Mockito.anyString())).
                thenReturn(null);
        assertNotNull(apiConsumer.getAllPaginatedAPIsByStatus(MultitenantConstants
                .SUPER_TENANT_DOMAIN_NAME, 0, 10, "testStatus", true));

        //generic artifact null path
        PowerMockito.when(APIUtil.getArtifactManager((UserRegistry)(Mockito.anyObject()), Mockito.anyString())).
                thenReturn(artifactManager);
        Mockito.when(artifactManager.findGenericArtifacts(Mockito.anyMap())).thenReturn(null);
        assertNotNull(apiConsumer.getAllPaginatedAPIsByStatus(MultitenantConstants
                .SUPER_TENANT_DOMAIN_NAME, 0, 10, "testStatus", true));
    }

    @Test
    public void testGetAllPaginatedAPIsByStatusSet() throws Exception {
        Registry userRegistry = Mockito.mock(Registry.class);
        APIConsumerImpl apiConsumer = new APIConsumerImplWrapper(userRegistry, apiMgtDAO);
        PowerMockito.mockStatic(GovernanceUtils.class);
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        APIManagerConfigurationService apiManagerConfigurationService = Mockito.
                mock(APIManagerConfigurationService.class);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfigurationService()).thenReturn(apiManagerConfigurationService);
        Mockito.when(apiManagerConfigurationService.getAPIManagerConfiguration()).thenReturn(apiManagerConfiguration);
        Mockito.when(apiManagerConfiguration.getFirstProperty(Mockito.anyString())).thenReturn("10", "20");

        PowerMockito.doNothing().when(APIUtil.class, "loadTenantRegistry", Mockito.anyInt());
        GenericArtifactManager artifactManager = Mockito.mock(GenericArtifactManager.class);
        PowerMockito.when(APIUtil.isAllowDisplayMultipleVersions()).thenReturn(false, true);
        PowerMockito.when(APIUtil.getArtifactManager((UserRegistry)(Mockito.anyObject()), Mockito.anyString())).
                thenReturn(artifactManager);
        List<GovernanceArtifact> governanceArtifacts = new ArrayList<GovernanceArtifact>();
        GenericArtifact artifact = Mockito.mock(GenericArtifact.class);
        governanceArtifacts.add(artifact);
        Mockito.when(GovernanceUtils.findGovernanceArtifacts(Mockito.anyString(),(UserRegistry)Mockito.anyObject(),
                Mockito.anyString())).thenReturn(governanceArtifacts);
        APIIdentifier apiId1 = new APIIdentifier("admin", "API1", "1.0.0");
        API api = new API(apiId1);
        Mockito.when(APIUtil.getAPI(artifact)).thenReturn(api);
        String artifactPath = "artifact/path";
        PowerMockito.when(GovernanceUtils.getArtifactPath(userRegistry, artifact.getId())).
                thenReturn(artifactPath);
        Tag tag = new Tag();
        Tag[] tags = new Tag[]{tag};
        Mockito.when(userRegistry.getTags(artifactPath)).thenReturn(tags);
        assertNotNull(apiConsumer.getAllPaginatedAPIsByStatus(MultitenantConstants
                .SUPER_TENANT_DOMAIN_NAME, 0, 10, new String[]{"testStatus"}, false));
        assertNotNull(apiConsumer.getAllPaginatedAPIsByStatus(MultitenantConstants
                .SUPER_TENANT_DOMAIN_NAME, 0, 10, new String[]{"testStatus"}, true));

        //artifact manager null path
        PowerMockito.when(APIUtil.getArtifactManager((UserRegistry)(Mockito.anyObject()), Mockito.anyString())).
                thenReturn(null);
        assertNotNull(apiConsumer.getAllPaginatedAPIsByStatus(MultitenantConstants
                .SUPER_TENANT_DOMAIN_NAME, 0, 10, new String[]{"testStatus"}, true));

        //generic artifact null path
        PowerMockito.when(APIUtil.getArtifactManager((UserRegistry)(Mockito.anyObject()), Mockito.anyString())).
                thenReturn(artifactManager);
        Mockito.when(GovernanceUtils.findGovernanceArtifacts(Mockito.anyString(),(UserRegistry)Mockito.anyObject(),
                Mockito.anyString())).thenReturn(null);
        assertNotNull(apiConsumer.getAllPaginatedAPIsByStatus(MultitenantConstants
                .SUPER_TENANT_DOMAIN_NAME, 0, 10, new String[]{"testStatus"}, true));
    }

    @Test
    public void testGetRecentlyAddedAPIs() throws Exception {
        Registry userRegistry = Mockito.mock(Registry.class);
        APIConsumerImpl apiConsumer = new APIConsumerImplWrapper(userRegistry, apiMgtDAO);
        UserRegistry userRegistry1 = Mockito.mock(UserRegistry.class);
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        APIManagerConfigurationService apiManagerConfigurationService = Mockito.
                mock(APIManagerConfigurationService.class);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfigurationService()).thenReturn(apiManagerConfigurationService);
        Mockito.when(apiManagerConfigurationService.getAPIManagerConfiguration()).thenReturn(apiManagerConfiguration);
        Mockito.when(apiManagerConfiguration.getFirstProperty(Mockito.anyString())).thenReturn("true", "false");

        API api = Mockito.mock(API.class);
        Set<API> recentlyAddedAPI = new HashSet<API>();
        recentlyAddedAPI.add(api);
        PowerMockito.mockStatic(Caching.class);

        CacheManager cacheManager = Mockito.mock(CacheManager.class);
        Cache<Object, Object> cache = Mockito.mock(Cache.class);
        Mockito.when(Caching.getCacheManager(Mockito.anyString())).thenReturn(cacheManager);
        Mockito.when(cacheManager.getCache(Mockito.anyString())).thenReturn(cache);
        Mockito.when(cache.get(Mockito.anyObject())).thenReturn(recentlyAddedAPI);
        Resource resource = new ResourceImpl();
        resource.setProperty("overview_status", "overview_status");
        resource.setProperty("store_view_roles", "store_view_roles");
        String path = "testPath";
        Mockito.when(APIUtil.getAPIPath((APIIdentifier) Mockito.anyObject())).thenReturn(path);
        Mockito.when(userRegistry1.get(Mockito.anyString())).thenReturn(resource);

        PowerMockito.mockStatic(GovernanceUtils.class);
        PowerMockito.doNothing().when(APIUtil.class, "loadTenantRegistry", Mockito.anyInt());
        PowerMockito.when(APIUtil.isAllowDisplayMultipleVersions()).thenReturn(true, false);
        System.setProperty(CARBON_HOME, "");

        Mockito.when(registryService.getGovernanceUserRegistry(Mockito.anyString(), Mockito.anyInt())).
                thenReturn(userRegistry1);
        Mockito.when(registryService.getGovernanceSystemRegistry(Mockito.anyInt())).thenReturn(userRegistry1);
        GenericArtifactManager artifactManager = Mockito.mock(GenericArtifactManager.class);
        PowerMockito.when(APIUtil.getArtifactManager((UserRegistry)(Mockito.anyObject()), Mockito.anyString())).
                thenReturn(artifactManager);
        GenericArtifact artifact = Mockito.mock(GenericArtifact.class);
        GenericArtifact[] genericArtifacts = new GenericArtifact[]{artifact};
        Mockito.when(artifactManager.findGovernanceArtifacts(Mockito.anyString())).thenReturn(genericArtifacts);

        APIIdentifier apiId1 = new APIIdentifier("admin", "API1", "1.0.0");
        API api1 = new API(apiId1);
        Mockito.when(APIUtil.getAPI(artifact)).thenReturn(api1);
        //set isAllowDisplayMultipleVersions true
        assertNotNull(apiConsumer.getRecentlyAddedAPIs(10, "testDomain"));
        //set isAllowDisplayMultipleVersions false
        assertNotNull(apiConsumer.getRecentlyAddedAPIs(10, "testDomain"));
    }

    @Test
    public void testGetTagsWithAttributes() throws Exception {
        Registry userRegistry = Mockito.mock(Registry.class);
        APIConsumerImpl apiConsumer = new APIConsumerImplWrapper(userRegistry, apiMgtDAO);
        System.setProperty(CARBON_HOME, "");
        PowerMockito.mockStatic(GovernanceUtils.class);
        UserRegistry userRegistry1 = Mockito.mock(UserRegistry.class);
        Mockito.when(registryService.getGovernanceUserRegistry(Mockito.anyString(), Mockito.anyInt())).
                thenReturn(userRegistry1);
        Mockito.when(registryService.getGovernanceSystemRegistry(Mockito.anyInt())).thenReturn(userRegistry1);
        List<TermData> list = new ArrayList<TermData>();
        TermData termData = new TermData("testTerm", 10);
        list.add(termData);
        Mockito.when(GovernanceUtils.getTermDataList(Mockito.anyMap(), Mockito.anyString(), Mockito.anyString(),
                Mockito.anyBoolean())).thenReturn(list);
        ResourceDO resourceDO = Mockito.mock(ResourceDO.class);
        Resource resource = new ResourceImpl("dw", resourceDO);
        resource.setContent("testContent");
        Mockito.when(userRegistry1.resourceExists(Mockito.anyString())).thenReturn(true);
        Mockito.when(userRegistry1.get(Mockito.anyString())).thenReturn(resource);
        assertNotNull(apiConsumer.getTagsWithAttributes("testDomain"));
    }

    @Test
    public void testGetTopRatedAPIs() throws APIManagementException, RegistryException {
        Registry userRegistry = Mockito.mock(Registry.class);
        APIConsumerImpl apiConsumer = new APIConsumerImplWrapper(userRegistry, apiMgtDAO);
        GenericArtifactManager artifactManager = Mockito.mock(GenericArtifactManager.class);
        PowerMockito.when(APIUtil.getArtifactManager((UserRegistry)(Mockito.anyObject()), Mockito.anyString())).
                thenReturn(artifactManager);
        GenericArtifact artifact = Mockito.mock(GenericArtifact.class);
        GenericArtifact[] genericArtifacts = new GenericArtifact[]{artifact};
        Mockito.when(artifactManager.getAllGenericArtifacts()).thenReturn(genericArtifacts);
        Mockito.when(artifact.getAttribute(Mockito.anyString())).thenReturn("PUBLISHED");
        Mockito.when(artifact.getPath()).thenReturn("testPath");

        Mockito.when(userRegistry.getAverageRating("testPath")).thenReturn((float)20.0);
        APIIdentifier apiId1 = new APIIdentifier("admin", "API1", "1.0.0");
        API api = new API(apiId1);
        Mockito.when(APIUtil.getAPI(artifact, userRegistry)).thenReturn(api);
        assertNotNull(apiConsumer.getTopRatedAPIs(10));
    }

    @Test
    public void testGetSubscribedAPIsIdNull() throws APIManagementException {
        APIConsumerImpl apiConsumer = new APIConsumerImplWrapper();
        Set<SubscribedAPI> originalSubscribedAPIs = new HashSet<SubscribedAPI>();
        Subscriber subscriber = new Subscriber("Subscriber");
        when(apiMgtDAO.getSubscribedAPIs(subscriber, null)).
                thenReturn(originalSubscribedAPIs);
        apiConsumer.apiMgtDAO = apiMgtDAO;
        assertEquals(originalSubscribedAPIs, apiConsumer.getSubscribedAPIs(subscriber));
    }

    @Test
    public void testGetPublishedAPIsByProvider() throws APIManagementException, RegistryException {
        Registry userRegistry = Mockito.mock(Registry.class);
        APIConsumerImpl apiConsumer = new APIConsumerImplWrapper(userRegistry, apiMgtDAO);
        PowerMockito.when(APIUtil.isAllowDisplayMultipleVersions()).thenReturn(true, false);
        PowerMockito.when(APIUtil.isAllowDisplayAPIsWithMultipleStatus()).thenReturn(true, false);
        GenericArtifactManager artifactManager = Mockito.mock(GenericArtifactManager.class);
        PowerMockito.when(APIUtil.getArtifactManager((UserRegistry)(Mockito.anyObject()), Mockito.anyString())).
                thenReturn(artifactManager);
        Association association = Mockito.mock(Association.class);
        Association[] associations = new Association[]{association};
        Mockito.when(userRegistry.getAssociations(Mockito.anyString(), Mockito.anyString())).thenReturn(associations);
        Mockito.when(association.getDestinationPath()).thenReturn("testPath");
        Resource resource = Mockito.mock(Resource.class);
        Mockito.when(userRegistry.get("testPath")).thenReturn(resource);
        Mockito.when(resource.getUUID()).thenReturn("testID");
        GenericArtifact genericArtifact = Mockito.mock(GenericArtifact.class);
        Mockito.when(artifactManager.getGenericArtifact(Mockito.anyString())).thenReturn(genericArtifact);
        Mockito.when(genericArtifact.getAttribute("overview_status")).thenReturn("PUBLISHED");
        APIIdentifier apiId1 = new APIIdentifier("admin", "API1", "1.0.0");
        API api = new API(apiId1);
        Mockito.when(APIUtil.getAPI(genericArtifact)).thenReturn(api);
        assertNotNull(apiConsumer.getPublishedAPIsByProvider("testID", 10));
        assertNotNull(apiConsumer.getPublishedAPIsByProvider("testID", 10));
    }

    @Test
    public void testGetUserRating() throws APIManagementException {
        APIConsumerImpl apiConsumer = new APIConsumerImplWrapper();
        APIIdentifier apiIdentifier = new APIIdentifier(API_PROVIDER, SAMPLE_API_NAME, SAMPLE_API_VERSION);
        when(apiMgtDAO.getUserRating(apiIdentifier, "admin")).thenReturn(2);
        apiConsumer.apiMgtDAO = apiMgtDAO;
        assertEquals(2, apiConsumer.getUserRating(apiIdentifier, "admin"));
    }



    @Test
    public void testResumeWorkflow() throws Exception {
        APIConsumerImpl apiConsumer = new APIConsumerImplWrapper();
        apiConsumer.apiMgtDAO = apiMgtDAO;
        WorkflowDTO workflowDTO = new WorkflowDTO();
        workflowDTO.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        when(apiMgtDAO.retrieveWorkflow(Mockito.anyString())).thenReturn(workflowDTO);

        // Null input case
        assertNotNull(apiConsumer.resumeWorkflow(null));
        String args[] = {UUID.randomUUID().toString(), WorkflowStatus.CREATED.toString(), UUID.randomUUID().toString
                ()};
        assertNotNull(apiConsumer.resumeWorkflow(args));

        Mockito.reset(apiMgtDAO);
        workflowDTO.setTenantDomain("wso2.com");
        when(apiMgtDAO.retrieveWorkflow(Mockito.anyString())).thenReturn(workflowDTO);
        JSONObject row = apiConsumer.resumeWorkflow(args);
        assertNotNull(row);

        Mockito.reset(apiMgtDAO);
        when(apiMgtDAO.retrieveWorkflow(Mockito.anyString())).thenThrow(APIManagementException.class);
        when(APIUtil.isStringArray(args)).thenReturn(true);
        row = apiConsumer.resumeWorkflow(args);
        assertEquals("Error while resuming the workflow. null",
                row.get("message"));

        // Workflow DAO null case
        Mockito.reset(apiMgtDAO);
        when(apiMgtDAO.retrieveWorkflow(Mockito.anyString())).thenReturn(null);
        row = apiConsumer.resumeWorkflow(args);
        assertNotNull(row);
        assertEquals(true, row.get("error"));
        assertEquals(500, row.get("statusCode"));

        //Invalid status test
        args[1] = "Invalid status";
        Mockito.reset(apiMgtDAO);
        when(apiMgtDAO.retrieveWorkflow(Mockito.anyString())).thenReturn(workflowDTO);
        row = apiConsumer.resumeWorkflow(args);
        assertEquals("Illegal argument provided. Valid values for status are APPROVED and REJECTED.",
                row.get("message"));

    }

    @Test
    public void testGetPaginatedAPIsWithTag() throws Exception {
        APIConsumerImpl apiConsumer = new APIConsumerImplWrapper();
        PowerMockito.doNothing().when(APIUtil.class, "loadTenantRegistry", Mockito.anyInt());

        PowerMockito.mockStatic(GovernanceUtils.class);
        GovernanceArtifact governanceArtifact = new GenericArtifactImpl(UUID.randomUUID().toString(), new QName(UUID.randomUUID().toString(), "UUID.randomUUID().toString()"),
                "api");
        List<GovernanceArtifact> governanceArtifactList = new ArrayList();
        governanceArtifactList.add(governanceArtifact);
        Mockito.when(GovernanceUtils.findGovernanceArtifacts(Mockito.anyString(), (Registry) Mockito.anyObject(),
                Mockito.anyString())).thenReturn(governanceArtifactList);

        assertNotNull(apiConsumer.getPaginatedAPIsWithTag("testTag", 0, 10, MultitenantConstants
                .SUPER_TENANT_DOMAIN_NAME));
    }

    @Test
    public void testRenewConsumerSecret() throws APIManagementException {
        APIConsumerImpl apiConsumer = new APIConsumerImplWrapper();
        String clientId = UUID.randomUUID().toString();
        Mockito.when(keyManager.getNewApplicationConsumerSecret((AccessTokenRequest) Mockito.anyObject())).thenReturn
                ("updatedClientSecret");
        assertNotNull(apiConsumer.renewConsumerSecret(clientId, APIConstants.KeyManager.DEFAULT_KEY_MANAGER));
    }


    @Test
    public void testGetSubscriptionCount() throws APIManagementException {
        APIConsumerImpl apiConsumer = new APIConsumerImplWrapper();
        Subscriber subscriber = new Subscriber("Subscriber");
        apiConsumer.apiMgtDAO = apiMgtDAO;
        when(apiMgtDAO.getSubscriptionCount((Subscriber) Mockito.anyObject(), Mockito.anyString(),
                Mockito.anyString())).thenReturn(10);
        apiConsumer.apiMgtDAO = apiMgtDAO;
        assertEquals((Integer) 10, apiConsumer.getSubscriptionCount(subscriber, "testApplication",
                "testId"));
    }

    @Test
    public void testGetSubscribedIdentifiers() throws APIManagementException {
        APIConsumerImpl apiConsumer = new APIConsumerImplWrapper(apiMgtDAO);
        Set<SubscribedAPI> originalSubscribedAPIs = new HashSet<>();
        SubscribedAPI subscribedAPI = Mockito.mock(SubscribedAPI.class);
        originalSubscribedAPIs.add(subscribedAPI);
        Subscriber subscriber = new Subscriber("Subscriber");
        APIIdentifier apiId1 = new APIIdentifier(API_PROVIDER, SAMPLE_API_NAME, SAMPLE_API_VERSION);
        Tier tier = Mockito.mock(Tier.class);

        when(apiMgtDAO.getSubscribedAPIs(subscriber, "testID")).thenReturn(originalSubscribedAPIs);
        when(subscribedAPI.getTier()).thenReturn(tier);
        when(tier.getName()).thenReturn("tier");
        when(subscribedAPI.getApiId()).thenReturn(apiId1);
        Application app = Mockito.mock(Application.class);
        when(app.getId()).thenReturn(1);
        when(subscribedAPI.getApplication()).thenReturn(app);
        Set<APIKey> apiKeys = new HashSet<>();
        APIKey apiKey = new APIKey();
        apiKey.setType("Production");
        apiKeys.add(apiKey);
        Mockito.when(apiMgtDAO.getKeyMappingsFromApplicationId(Mockito.anyInt())).thenReturn(apiKeys);

        AccessTokenInfo accessTokenInfo = new AccessTokenInfo();
        accessTokenInfo.setAccessToken(UUID.randomUUID().toString());
        Mockito.when(keyManager.getAccessTokenByConsumerKey(Mockito.anyString())).thenReturn(accessTokenInfo);
        assertNotNull(apiConsumer.getSubscribedIdentifiers(subscriber, apiId1,"testID"));
    }

    @Test
    public void testIsSubscribed() throws APIManagementException {
        APIConsumerImpl apiConsumer = new APIConsumerImplWrapper();
        apiConsumer.apiMgtDAO = apiMgtDAO;
        APIIdentifier apiIdentifier = new APIIdentifier(API_PROVIDER, SAMPLE_API_NAME, SAMPLE_API_VERSION);
        Mockito.when(apiMgtDAO.isSubscribed(apiIdentifier, "testID")).thenReturn(true);
        assertEquals(true, apiConsumer.isSubscribed(apiIdentifier, "testID"));

        // Error Path
        Mockito.when(apiMgtDAO.isSubscribed(apiIdentifier, "testID")).thenThrow(APIManagementException.class);
        try {
            apiConsumer.isSubscribed(apiIdentifier, "testID");
            assertTrue(false);
        } catch (APIManagementException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testGetSubscribedAPIs() throws APIManagementException {
        APIConsumerImpl apiConsumer = new APIConsumerImplWrapper();
        apiConsumer.apiMgtDAO = apiMgtDAO;
        Set<SubscribedAPI> originalSubscribedAPIs = new HashSet<SubscribedAPI>();

        SubscribedAPI subscribedAPI = Mockito.mock(SubscribedAPI.class);
        originalSubscribedAPIs.add(subscribedAPI);
        Subscriber subscriber = new Subscriber("Subscriber");
        Tier tier = Mockito.mock(Tier.class);

        when(apiMgtDAO.getSubscribedAPIs(subscriber, "testID")).thenReturn(originalSubscribedAPIs);
        when(subscribedAPI.getTier()).thenReturn(tier);
        when(tier.getName()).thenReturn("tier");
        assertNotNull(apiConsumer.getSubscribedAPIs(subscriber, "testID"));
    }

    @Test
    public void testGetAllPublishedAPIs() throws APIManagementException, GovernanceException {
        APIConsumerImpl apiConsumer = new APIConsumerImplWrapper();
        APINameComparator apiNameComparator = Mockito.mock(APINameComparator.class);
        SortedSet<API> apiSortedSet = new TreeSet<API>(apiNameComparator);
        GenericArtifactManager artifactManager = Mockito.mock(GenericArtifactManager.class);
        PowerMockito.when(APIUtil.getArtifactManager(apiConsumer.registry, APIConstants.API_KEY)).
                thenReturn(artifactManager);
        GenericArtifact artifact = Mockito.mock(GenericArtifact.class);
        GenericArtifact[] genericArtifacts = new GenericArtifact[]{artifact};
        APIIdentifier apiId1 = new APIIdentifier(API_PROVIDER, SAMPLE_API_NAME, SAMPLE_API_VERSION);
        API api = new API(apiId1);

        Mockito.when(artifactManager.getAllGenericArtifacts()).thenReturn(genericArtifacts);
        Mockito.when(artifact.getAttribute(APIConstants.API_OVERVIEW_STATUS)).thenReturn("PUBLISHED");
        Mockito.when(APIUtil.getAPI(artifact)).thenReturn(api);

        Map<String, API> latestPublishedAPIs = new HashMap<String, API>();
        latestPublishedAPIs.put("user:key", api);
        apiSortedSet.addAll(latestPublishedAPIs.values());
        assertNotNull(apiConsumer.getAllPublishedAPIs("testDomain"));
    }

    @Test
    public void testAddApplication() throws APIManagementException, UserStoreException {
        APIConsumerImpl apiConsumer = new APIConsumerImplWrapper(apiMgtDAO);
        Application application = Mockito.mock(Application.class);
        Mockito.when(application.getName()).thenReturn("app");
        PowerMockito.when(application.getSubscriber()).thenReturn(new Subscriber("User1"));
        PowerMockito.when(MultitenantUtils.getTenantDomain("userID")).thenReturn("carbon.super");
        PowerMockito.when(APIUtil.isApplicationExist("userID", "app", "1")).
                thenReturn(false);
        Mockito.when(apiMgtDAO.addApplication(application, "userID")).thenReturn(1);
        assertEquals(1, apiConsumer.addApplication(application, "userID"));
    }
    @Test
    public void testGetScopesBySubscribedAPIs() throws APIManagementException {
        APIConsumerImpl apiConsumer = new APIConsumerImplWrapper(apiMgtDAO);
        List<APIIdentifier> identifiers = new ArrayList<>();
        Set<String> scopes = new HashSet<>();
        when(apiMgtDAO.getScopesBySubscribedAPIs(identifiers)).thenReturn(scopes);
        assertEquals(scopes, apiConsumer.getScopesBySubscribedAPIs(identifiers));
    }


    @Test
    public void testGetScopesByScopeKeys() throws APIManagementException {
        APIConsumerImpl apiConsumer = new APIConsumerImplWrapper(apiMgtDAO);
        Set<Scope> scopes = new HashSet<>();
        when(apiMgtDAO.getScopesByScopeKeys("testKey", 1234)).thenReturn(scopes);
        assertEquals(scopes, apiConsumer.getScopesByScopeKeys("testKey", 1234));
    }

    @Test
    public void testAddComment() throws APIManagementException {
        APIConsumerImpl apiConsumer = new APIConsumerImplWrapper(apiMgtDAO);
        APIIdentifier apiIdentifier = new APIIdentifier(API_PROVIDER, SAMPLE_API_NAME, SAMPLE_API_VERSION);
        Mockito.when(apiMgtDAO.addComment(apiIdentifier, "testComment", "testUser")).thenReturn(1111);
        apiConsumer.addComment(apiIdentifier, "testComment", "testUser");
        Mockito.verify(apiMgtDAO, Mockito.times(1)).
                addComment(apiIdentifier, "testComment", "testUser");
    }

    @Test
    public void testGetSubscribedAPIsWithApp() throws APIManagementException {
        APIConsumerImpl apiConsumer = new APIConsumerImplWrapper(apiMgtDAO);
        Set<SubscribedAPI> originalSubscribedAPIs = new HashSet<SubscribedAPI>();
        SubscribedAPI subscribedAPI = Mockito.mock(SubscribedAPI.class);
        originalSubscribedAPIs.add(subscribedAPI);
        Subscriber subscriber = new Subscriber("Subscriber");
        Tier tier = Mockito.mock(Tier.class);
        when(apiMgtDAO.getSubscribedAPIs(subscriber, "testApplication","testID")).
                thenReturn(originalSubscribedAPIs);
        when(subscribedAPI.getTier()).thenReturn(tier);
        when(tier.getName()).thenReturn("tier");
        assertNotNull(apiConsumer.getSubscribedAPIs(subscriber, "testApplication","testID"));
    }




    @Test
    public void testGetApplicationById() throws APIManagementException {
        APIConsumerImpl apiConsumer = new APIConsumerImplWrapper(apiMgtDAO);
        apiConsumer.apiMgtDAO = apiMgtDAO;
        Application application = Mockito.mock(Application.class);
        PowerMockito.when(application.getSubscriber()).thenReturn(new Subscriber("testId"));
        PowerMockito.when(MultitenantUtils.getTenantDomain("testId")).thenReturn("carbon.super");
        application.setUUID("testID");
        Mockito.when(apiMgtDAO.getApplicationById(1111)).
                thenReturn(application);
        assertEquals(application, apiConsumer.getApplicationById(1111));
    }

    @Test
    public void testGetApplicationStatusById() throws APIManagementException {
        APIConsumerImpl apiConsumer = new APIConsumerImplWrapper(apiMgtDAO);
        Mockito.when(apiMgtDAO.getApplicationStatusById(1111)).
                thenReturn("testStatus");
        assertEquals("testStatus", apiConsumer.getApplicationStatusById(1111));
    }





    @Test
    public void testUpdateAuthClient() throws APIManagementException {
        String consumerKey = "aNTf-EFga";
        OAuthApplicationInfo oAuthApplicationInfo = new OAuthApplicationInfo();
        OAuthAppRequest oAuthAppRequest = new OAuthAppRequest();
        oAuthAppRequest.setOAuthApplicationInfo(oAuthApplicationInfo);
        BDDMockito.when(ApplicationUtils
                .createOauthAppRequest(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                        Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                        Mockito.anyString()))
                .thenReturn(oAuthAppRequest);
        Mockito.when(apiMgtDAO
                .getConsumerKeyByApplicationIdKeyTypeKeyManager(Mockito.anyInt(),Mockito.anyString(),
                        Mockito.anyString())).thenReturn(consumerKey);
        Mockito.when(apiMgtDAO
                .getKeyMappingsFromApplicationIdKeyManagerAndKeyType(Mockito.anyInt(), Mockito.anyString(),
                        Mockito.anyString(), Mockito.anyString())).thenReturn(null);
        OAuthApplicationInfo updatedAppInfo = new OAuthApplicationInfo();
        String clientName = "sample client";
        updatedAppInfo.setClientName(clientName);
        Mockito.when(keyManager.updateApplication((OAuthAppRequest) Mockito.any())).thenReturn(updatedAppInfo);
        KeyManagerConfigurationDTO keyManagerConfiguration = new KeyManagerConfigurationDTO();
        keyManagerConfiguration.setEnabled(true);
        Mockito.when(apiMgtDAO.getKeyManagerConfigurationByName(Mockito.anyString(),Mockito.anyString())).thenReturn(keyManagerConfiguration);
        System.setProperty(CARBON_HOME, "");
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        APIManagerConfigurationService apiManagerConfigurationService = Mockito.
                mock(APIManagerConfigurationService.class);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfigurationService()).thenReturn(apiManagerConfigurationService);
        Mockito.when(apiManagerConfigurationService.getAPIManagerConfiguration()).thenReturn(apiManagerConfiguration);
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.KEYMANAGER_SERVERURL)).
                thenReturn("http://localhost");

        int appId = 123456;
        String appName = "app1";
        String userId = "1";
        String tokenType = "access";
        String kmName = "defaultTestKM";
        String kmUUID = "kmuuid";

        Application application = Mockito.mock(Application.class);
        Subscriber subscriber = Mockito.mock(Subscriber.class);
        Mockito.when(ApplicationUtils
                .retrieveApplication(appName, userId, null))
                .thenReturn(application);
        Mockito.when(application.getSubscriber()).thenReturn(subscriber);
        Mockito.when(application.getId()).thenReturn(appId);
        Mockito.when(subscriber.getName()).thenReturn(userId);

        APIKey apiKey = Mockito.mock(APIKey.class);
        Mockito.when(apiKey.getCreateMode()).thenReturn(APIConstants.OAuthAppMode.CREATED.name());
        Mockito.when(apiMgtDAO.getKeyMappingsFromApplicationIdKeyManagerAndKeyType(appId, kmName, kmUUID, tokenType))
                .thenReturn(apiKey);

        KeyManagerConfigurationDTO testKeyManagerConfiguration = new KeyManagerConfigurationDTO();
        testKeyManagerConfiguration.setEnabled(true);
        testKeyManagerConfiguration.setUuid(kmUUID);
        Mockito.when(apiMgtDAO.getKeyManagerConfigurationByName(SAMPLE_TENANT_DOMAIN_1, kmName))
                .thenReturn(testKeyManagerConfiguration);

        APIConsumerImpl apiConsumer = new APIConsumerImplWrapper(apiMgtDAO);
        apiConsumer.tenantDomain = SAMPLE_TENANT_DOMAIN_1;
        Assert.assertEquals(apiConsumer
                .updateAuthClient(userId, appName, tokenType, "www.host.com", new String[0], null, null, null, null,
                        kmName)
                .getClientName(), clientName);
    }


    @Test
    public void testGetApplicationsWithPagination() throws APIManagementException {
        Application[] applications = new Application[] { new Application(1), new Application(2) };
        Mockito.when(apiMgtDAO
                .getApplicationsWithPagination((Subscriber) Mockito.any(), Mockito.anyString(), Mockito.anyInt(),
                        Mockito.anyInt(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(applications);
        APIConsumerImpl apiConsumer = new APIConsumerImplWrapper(apiMgtDAO);
        Assert.assertEquals(
                apiConsumer.getApplicationsWithPagination(new Subscriber("sub1"), "1", 0, 5,
                        "", "", "ASC").length, 2);
    }

    @Test
    public void testGetGroupIds()
            throws APIManagementException, IllegalAccessException, InstantiationException, ClassNotFoundException {
        PowerMockito.when(APIUtil.getGroupingExtractorImplementation()).thenReturn(null);
        APIConsumerImpl apiConsumer = new APIConsumerImplWrapper();
        Assert.assertNull(apiConsumer.getGroupIds("login"));

        String groupIdExtractorClass = "org.wso2.carbon.apimgt.impl.SampleLoginPostExecutor";
        String[] array = new String[]{"a", "b", "c"};
        PowerMockito.when(APIUtil.getGroupIdsFromExtractor("login", groupIdExtractorClass)).thenReturn(array);
        apiConsumer.getGroupIds("login");
    }

    @Test
    public void testIsTierDenied() throws APIManagementException, org.wso2.carbon.user.core.UserStoreException {
        UserRegistry userRegistry = Mockito.mock(UserRegistry.class);
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        APIManagerConfigurationService apiManagerConfigurationService = Mockito.
                mock(APIManagerConfigurationService.class);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfigurationService()).thenReturn(apiManagerConfigurationService);
        Mockito.when(apiManagerConfigurationService.getAPIManagerConfiguration()).thenReturn(apiManagerConfiguration);
        Mockito.when(apiManagerConfiguration.getFirstProperty(Mockito.anyString())).thenReturn("true", "false");
        APIConsumerImpl apiConsumer = new UserAwareAPIConsumerWrapper(userRegistry, apiMgtDAO);
        Mockito.when(userRegistry.getUserRealm()).thenReturn(userRealm);
        Mockito.when(userRealm.getUserStoreManager()).thenReturn(userStoreManager);
        Mockito.when(userStoreManager.getRoleListOfUser(Mockito.anyString())).thenThrow(UserStoreException.class).
                thenReturn(new String[] { "role1", "role2" });
        Assert.assertFalse(apiConsumer.isTierDeneid("tier1"));
        TierPermissionDTO tierPermissionDTO = new TierPermissionDTO();
        tierPermissionDTO.setRoles(new String[] { "role1" });
        Mockito.when(apiMgtDAO.getThrottleTierPermission(Mockito.anyString(), Mockito.anyInt()))
                .thenReturn(tierPermissionDTO);
        Assert.assertTrue(apiConsumer.isTierDeneid("tier1"));
        tierPermissionDTO.setRoles(new String[] { "role3" });
        Assert.assertFalse(apiConsumer.isTierDeneid("tier1"));
        Assert.assertFalse(apiConsumer.isTierDeneid("tier1"));
        tierPermissionDTO.setPermissionType(APIConstants.TIER_PERMISSION_ALLOW);
        Mockito.when(userStoreManager.getRoleListOfUser(Mockito.anyString())).thenReturn(new String[0]);
        Assert.assertTrue(apiConsumer.isTierDeneid("tier1"));

    }

    @Test
    public void testGetDeniedTiers() throws APIManagementException, org.wso2.carbon.user.core.UserStoreException {
        UserRegistry userRegistry = Mockito.mock(UserRegistry.class);
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        APIManagerConfigurationService apiManagerConfigurationService = Mockito.
                mock(APIManagerConfigurationService.class);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfigurationService()).thenReturn(apiManagerConfigurationService);
        Mockito.when(apiManagerConfigurationService.getAPIManagerConfiguration()).thenReturn(apiManagerConfiguration);
        Mockito.when(apiManagerConfiguration.getFirstProperty(Mockito.anyString())).thenReturn("true", "false");
        APIConsumerImpl apiConsumer = new UserAwareAPIConsumerWrapper(userRegistry, apiMgtDAO);
        Mockito.when(userRegistry.getUserRealm()).thenReturn(userRealm);
        Mockito.when(userRealm.getUserStoreManager()).thenReturn(userStoreManager);
        Mockito.when(userStoreManager.getRoleListOfUser(Mockito.anyString())).thenThrow(UserStoreException.class).
                thenReturn(new String[] { "role1", "role2" });
        Assert.assertEquals(apiConsumer.getDeniedTiers().size(), 0);
        TierPermissionDTO tierPermissionDTO = new TierPermissionDTO();
        TierPermissionDTO tierPermissionDTO1 = new TierPermissionDTO();
        tierPermissionDTO.setRoles(new String[] { "role1" });
        Set<TierPermissionDTO> tierPermissionDTOs = new HashSet<TierPermissionDTO>();
        tierPermissionDTOs.add(tierPermissionDTO);
        Mockito.when(apiMgtDAO.getThrottleTierPermissions(Mockito.anyInt())).thenReturn(tierPermissionDTOs);
        Assert.assertEquals(apiConsumer.getDeniedTiers().size(), 1);
        tierPermissionDTO.setRoles(new String[] { "role3" });
        Assert.assertEquals(apiConsumer.getDeniedTiers().size(), 0);
        Mockito.when(apiMgtDAO.getTierPermissions(Mockito.anyInt())).thenReturn(tierPermissionDTOs);
        Assert.assertEquals(apiConsumer.getDeniedTiers().size(), 0);
        tierPermissionDTO.setPermissionType(APIConstants.TIER_PERMISSION_ALLOW);
        Mockito.when(userStoreManager.getRoleListOfUser(Mockito.anyString())).thenReturn(new String[0]);
        tierPermissionDTOs.add(tierPermissionDTO1);
        tierPermissionDTO1.setRoles(new String[] { "role4" });
        Assert.assertEquals(apiConsumer.getDeniedTiers().size(), 1);
        Mockito.when(userStoreManager.getRoleListOfUser(Mockito.anyString()))
                .thenReturn(new String[] { "role1", "role2" });
        tierPermissionDTO1.setRoles(new String[] { "role2" });
        tierPermissionDTO1.setTierName("Silver");
        Assert.assertEquals(apiConsumer.getDeniedTiers().size(), 2);
    }


    @Test
    public void testRequestApprovalForApplicationRegistration() throws APIManagementException, UserStoreException {
        Scope scope1 = new Scope();
        scope1.setName("api_view");
        Scope scope2 = new Scope();
        scope2.setName("api_create");
        Set<Scope> scopes = new HashSet<Scope>();
        scopes.add(scope1);
        scopes.add(scope2);
        PowerMockito.when(MultitenantUtils.getTenantDomain(Mockito.anyString())).thenReturn("abc.org");
        Mockito.when(apiMgtDAO.getScopesByScopeKeys(Mockito.anyString(), Mockito.anyInt())).thenReturn(scopes);
        KeyManagerConfigurationDTO keyManagerConfigurationsDto = new KeyManagerConfigurationDTO();
        keyManagerConfigurationsDto.setEnabled(true);
        Mockito.when(apiMgtDAO.getKeyManagerConfigurationByName("abc.org", "default"))
                .thenReturn(keyManagerConfigurationsDto);
        Mockito.when(tenantManager.getTenantId(Mockito.anyString())).thenThrow(UserStoreException.class)
                .thenReturn(-1234, 1);
        APIConsumerImpl apiConsumer = new APIConsumerImplWrapper(apiMgtDAO);
        try {
            apiConsumer
                    .requestApprovalForApplicationRegistration("1", "app1", "access", "identity.com/auth", null, "3600",
                            "api_view", "2", null, "default", null, false);
            Assert.fail("User store exception not thrown for invalid token type");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Unable to retrieve the tenant information of the current user"));
        }
        Mockito.when(userStoreManager.getRoleListOfUser(Mockito.anyString())).thenThrow(UserStoreException.class).
                thenReturn(new String[] { "role1", "role2" });

        Application application = Mockito.mock(Application.class);
        Subscriber subscriber = Mockito.mock(Subscriber.class);
        Mockito.when(subscriber.getName()).thenReturn("1");
        Mockito.when(application.getSubscriber()).thenReturn(subscriber);
        Mockito.when(ApplicationUtils
                .retrieveApplication(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(application);

        try {
            apiConsumer
                    .requestApprovalForApplicationRegistration("1", "app1", "access", "identity.com/auth", null, "3600",
                            "api_view", "2", null, "default", null, false);
            Assert.fail("API management exception not thrown for invalid token type");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Invalid Token Type"));
        }
        scope1.setRoles("role1");
        scope2.setRoles("role2");
        OAuthApplicationInfo oAuthApplicationInfo = new OAuthApplicationInfo();
        OAuthAppRequest oAuthAppRequest = new OAuthAppRequest();
        oAuthAppRequest.setOAuthApplicationInfo(oAuthApplicationInfo);
        application = new Application("app1", new Subscriber("1"));
        BDDMockito.when(ApplicationUtils
                .createOauthAppRequest(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                        Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                        Mockito.anyString(), Mockito.anyString())).thenReturn(oAuthAppRequest);
        BDDMockito.when(ApplicationUtils
                .retrieveApplication(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(application);
        Map<String, Object> result = apiConsumer
                .requestApprovalForApplicationRegistration("1", "app1", APIConstants.API_KEY_TYPE_PRODUCTION,
                        "identity.com/auth", null, "3600", "api_view", "2", null, "default", null, false);
        Assert.assertEquals(result.size(), 10);
        Assert.assertEquals(result.get("keyState"), "APPROVED");

        result = apiConsumer
                .requestApprovalForApplicationRegistration("1", "app1", APIConstants.API_KEY_TYPE_SANDBOX, "", null,
                        "3600", "api_view", "2", null, "default", null, false);
        Assert.assertEquals(result.size(), 10);
        Assert.assertEquals(result.get("keyState"), "APPROVED");

    }

    @Test
    public void testUpdateApplication() throws APIManagementException {
        Application newApplication = new Application("app1", new Subscriber("sub1"));
        Application oldApplication = new Application("app", new Subscriber("sub1"));
        oldApplication.setStatus(APIConstants.ApplicationStatus.APPLICATION_CREATED);
        Mockito.when(apiMgtDAO.getApplicationById(Mockito.anyInt())).thenReturn(oldApplication);
        Mockito.when(apiMgtDAO.getApplicationByUUID(Mockito.anyString())).thenReturn(oldApplication);
        APIConsumerImpl apiConsumer = new APIConsumerImplWrapper(apiMgtDAO);
        try {
            apiConsumer.updateApplication(newApplication);
            Assert.fail("API management exception not thrown for error scenario");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Cannot update the application while it is INACTIVE"));
        }
    }

    @Test
    public void testGetComments() throws APIManagementException {
        Comment comment = new Comment();
        Comment[] comments = new Comment[] { comment };
        APIIdentifier identifier = new APIIdentifier(API_PROVIDER, SAMPLE_API_NAME, SAMPLE_API_VERSION);
        Mockito.when(apiMgtDAO.getComments(identifier)).thenReturn(comments);
        Assert.assertEquals(new APIConsumerImplWrapper(apiMgtDAO).getComments(identifier).length, 1);
        Mockito.verify(apiMgtDAO, Mockito.times(1)).getComments(identifier);
    }

    @Test
    public void testUpdateSubscriptions() throws APIManagementException {
        APIIdentifier identifier = new APIIdentifier(API_PROVIDER, SAMPLE_API_NAME, SAMPLE_API_VERSION);
        ApiTypeWrapper apiTypeWrapper = Mockito.mock(ApiTypeWrapper.class);
        Mockito.doNothing().when(apiMgtDAO).updateSubscriptions(apiTypeWrapper, 2, "1");
        new APIConsumerImplWrapper(apiMgtDAO).updateSubscriptions(identifier, "1", 2);
        Mockito.verify(apiMgtDAO, Mockito.times(1)).
                updateSubscriptions(Mockito.any(ApiTypeWrapper.class), eq(2), eq("1"));
    }

    @Test
    public void testRemoveSubscriber() throws APIManagementException {
        APIIdentifier identifier = new APIIdentifier(API_PROVIDER, SAMPLE_API_NAME, SAMPLE_API_VERSION);
        try {
            new APIConsumerImplWrapper(apiMgtDAO).removeSubscriber(identifier, "1");
            // no need of assert fail here since the method is not implemented yet.
        } catch (UnsupportedOperationException e) {
            Assert.assertTrue(e.getMessage().contains("Unsubscribe operation is not yet implemented"));
        }
    }

    @Test
    public void testRemoveSubscription() throws APIManagementException, WorkflowException, RegistryException {
        String uuid = UUID.randomUUID().toString();
        Subscriber subscriber = new Subscriber("sub1");
        Application application = new Application("app1", subscriber);
        application.setId(1);
        APIIdentifier identifier = new APIIdentifier(API_PROVIDER, SAMPLE_API_NAME, SAMPLE_API_VERSION);
        SubscribedAPI subscribedAPIOld = new SubscribedAPI(subscriber, identifier);
        subscribedAPIOld.setApplication(application);
        Mockito.when(apiMgtDAO.getSubscriptionByUUID(uuid)).thenReturn(null, subscribedAPIOld);
        Mockito.when(apiMgtDAO.isAppAllowed(Mockito.anyInt(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(true);
        SubscribedAPI subscribedAPINew = new SubscribedAPI(uuid);
        APIConsumerImpl apiConsumer = new APIConsumerImplWrapper(apiMgtDAO);
        try {
            apiConsumer.removeSubscription(subscribedAPINew);
            Assert.fail("API manager exception not thrown when subscription does not exist with UUID");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Subscription for UUID"));
        }
        String path = "testPath";
        Mockito.when(APIUtil.getAPIPath((APIIdentifier) Mockito.anyObject())).thenReturn(path);
        Resource resource = new ResourceImpl();
        resource.setUUID(UUID.randomUUID().toString());
        Mockito.when(userRegistry.get(Mockito.anyString())).thenReturn(resource);
        GenericArtifact genericArtifact = Mockito.mock(GenericArtifactImpl.class);
        Mockito.when(genericArtifactManager.getGenericArtifact(Mockito.anyString())).thenReturn(genericArtifact);
        PowerMockito.when(MultitenantUtils.getTenantDomain(Mockito.anyString())).thenReturn("carbon.super");
        GenericArtifactManager artifactManager = Mockito.mock(GenericArtifactManager.class);
        PowerMockito.when(APIUtil.getArtifactManager((UserRegistry)(Mockito.anyObject()), Mockito.anyString())).
                thenReturn(artifactManager);
        API api = new API(new APIIdentifier("admin", "test", "1.0.0"));
        PowerMockito.when(APIUtil.getAPIInformation((GenericArtifact)(Mockito.anyObject()),
                (UserRegistry)(Mockito.anyObject()))).thenReturn(api);
        apiConsumer.removeSubscription(subscribedAPINew);
        Mockito.verify(apiMgtDAO, Mockito.times(1)).getApplicationNameFromId(Mockito.anyInt());
        String workflowExtRef = "test_wf_ref";
        String workflowExtRef1 = "complete_wf_ref";
        Mockito.when(
                apiMgtDAO.getExternalWorkflowReferenceForSubscription((APIIdentifier) Mockito.any(), Mockito.anyInt()))
                .thenReturn(workflowExtRef, workflowExtRef1);
        SubscriptionWorkflowDTO subscriptionWorkflowDTO = new SubscriptionWorkflowDTO();
        subscriptionWorkflowDTO.setWorkflowReference("1");
        Mockito.when(apiMgtDAO.retrieveWorkflow(workflowExtRef)).thenReturn(subscriptionWorkflowDTO);
        SubscribedAPI subscribedAPI = new SubscribedAPI("api1");
        subscribedAPI.setTier(new Tier("Gold"));
        ;
        Mockito.when(apiMgtDAO.getSubscriptionById(Mockito.anyInt())).thenReturn(subscribedAPI);
        Mockito.when(apiMgtDAO.getSubscriptionStatus(identifier, 1))
                .thenReturn(APIConstants.SubscriptionStatus.ON_HOLD);
        apiConsumer.removeSubscription(subscribedAPINew);
        Mockito.when(apiMgtDAO.retrieveWorkflow(workflowExtRef1)).thenReturn(subscriptionWorkflowDTO);
        PowerMockito.when(MultitenantUtils.getTenantDomain(Mockito.anyString())).thenReturn("abc.org");
        apiConsumer.removeSubscription(subscribedAPINew);
        Mockito.verify(apiMgtDAO, Mockito.times(2)).retrieveWorkflow(Mockito.anyString());

    }

    @Test
    public void testGetSubscriptionStatusById() throws APIManagementException {
        Mockito.when(apiMgtDAO.getSubscriptionStatusById(1)).thenReturn("success");
        Assert.assertEquals(new APIConsumerImplWrapper(apiMgtDAO).getSubscriptionStatusById(1), "success");
    }

    @Test
    public void testAddSubscription() throws APIManagementException {
        ApiTypeWrapper apiTypeWrapper = Mockito.mock(ApiTypeWrapper.class);
        API api  = Mockito.mock(API.class);
        Mockito.when(apiTypeWrapper.getApi()).thenReturn(api);
        Mockito.when(api.getStatus()).thenReturn(APIConstants.PUBLISHED);
        Mockito.when(api.getId()).thenReturn(new APIIdentifier(API_PROVIDER, "published_api",
                SAMPLE_API_VERSION));
        Mockito.when(apiMgtDAO.addSubscription(Mockito.any(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyString())).thenReturn(1);
        Mockito.when(apiMgtDAO.isAppAllowed(Mockito.anyInt(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(true);
        APIConsumerImpl apiConsumer = new APIConsumerImplWrapper(apiMgtDAO);
        SubscribedAPI subscribedAPI = new SubscribedAPI("api1");
        Mockito.when(apiMgtDAO.getSubscriptionById(Mockito.anyInt())).thenReturn(subscribedAPI);
        apiConsumer.tenantDomain = SAMPLE_TENANT_DOMAIN_1;
        Assert.assertEquals(apiConsumer.addSubscription(apiTypeWrapper, "sub1", 1).getSubscriptionUUID()
                ,"api1");
        try {
            Mockito.when(api.getStatus()).thenReturn(APIConstants.CREATED);
            apiConsumer.addSubscription(apiTypeWrapper, "sub1", 1);
            Assert.fail("Resource not found exception not thrown for worng api state");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Subscriptions not allowed on APIs/API Products in the state"));
        }

    }

    @Test
    public void testGetPaginatedSubscribedAPIs() throws APIManagementException {
        APIConsumerImpl apiConsumer = new APIConsumerImplWrapper(apiMgtDAO);
        Subscriber subscriber = new Subscriber("sub1");
        Application application = new Application("app1", subscriber);
        application.setId(1);
        SubscribedAPI subscribedAPI = new SubscribedAPI(subscriber,
                new APIIdentifier(API_PROVIDER, SAMPLE_API_NAME, SAMPLE_API_VERSION));
        subscribedAPI.setUUID(UUID.randomUUID().toString());
        subscribedAPI.setApplication(application);
        subscribedAPI.setTier(new Tier("Silver"));
        Set<SubscribedAPI> subscribedAPIs = new HashSet<SubscribedAPI>();
        subscribedAPIs.add(subscribedAPI);
        PowerMockito.mockStatic(APIUtil.class);
        Map<String, Tier> tierMap = new HashMap<String, Tier>();
        tierMap.put("tier1", new Tier("Platinum"));
        PowerMockito.when(APIUtil.getTiers(Mockito.anyInt())).thenThrow(APIManagementException.class)
                .thenReturn(tierMap);
        Mockito.when(apiMgtDAO.getPaginatedSubscribedAPIs(subscriber, "app1", 0, 5, "group_id_1"))
                .thenReturn(subscribedAPIs, null, subscribedAPIs);
        try {
            apiConsumer.getPaginatedSubscribedAPIs(subscriber, "app1", 0, 5, "group_id_1");
            Assert.fail("API Management exception not thrown for error scenario");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Failed to get APIs of"));
        }
        Assert.assertNull(apiConsumer.getPaginatedSubscribedAPIs(subscriber, "app1", 0, 5, "group_id_1"));
        Assert.assertEquals(1, apiConsumer.getPaginatedSubscribedAPIs(subscriber, "app1", 0, 5, "group_id_1").size());

    }

    @Test
    public void testMapExistingOAuthClient() throws APIManagementException {
        APIConsumerImpl apiConsumer = new APIConsumerImplWrapper(apiMgtDAO);
        apiConsumer.tenantDomain = "carbon.super";
        OAuthApplicationInfo oAuthApplicationInfo = new OAuthApplicationInfo();
        OAuthAppRequest oAuthAppRequest = new OAuthAppRequest();
        oAuthAppRequest.setOAuthApplicationInfo(oAuthApplicationInfo);
        BDDMockito.when(ApplicationUtils
                .createOauthAppRequest(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                        Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                        Mockito.anyString(), Mockito.anyString())).thenReturn(oAuthAppRequest);
        Mockito.when(apiMgtDAO.isKeyMappingExistsForConsumerKeyOrApplication(Mockito.anyInt(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(true, false);
        Mockito.when(keyManager.mapOAuthApplication((OAuthAppRequest) Mockito.any())).thenReturn(oAuthApplicationInfo);
        Mockito.doNothing().when(apiMgtDAO).createApplicationKeyTypeMappingForManualClients(Mockito.anyString(),
                Mockito.anyInt(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        KeyManagerConfigurationDTO keyManagerConfigurationsDto = new KeyManagerConfigurationDTO();
        AccessTokenRequest accessTokenRequest = new AccessTokenRequest();
        AccessTokenInfo accessTokenInfo = new AccessTokenInfo();
        KeyManagerConfiguration keyManagerConfiguration = new KeyManagerConfiguration();
        Mockito.when(keyManager.getKeyManagerConfiguration()).thenReturn(keyManagerConfiguration);
        BDDMockito.when(ApplicationUtils.createAccessTokenRequest(keyManager,oAuthApplicationInfo, null)).thenReturn
                (accessTokenRequest);
        Mockito.when(keyManager.getNewApplicationAccessToken(accessTokenRequest)).thenReturn(accessTokenInfo);
        try {
            apiConsumer.mapExistingOAuthClient("", "admin", "1", "app1",
                    "PRODUCTION", "DEFAULT", "Resident Key Manager");
            Assert.fail("Exception is not thrown when client id is already mapped to an application");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Key Mappings already exists for application"));
        }
        Assert.assertEquals(8, apiConsumer.mapExistingOAuthClient("", "admin", "1",
                "app1", "PRODUCTION", "DEFAULT",
                "Resident Key Manager").size());
    }

    @Test
    public void testCleanUpApplicationRegistration() throws APIManagementException {
        Application application = new Application(1);
        Mockito.when(apiMgtDAO.getApplicationByName(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(application);
        new APIConsumerImplWrapper(apiMgtDAO).cleanUpApplicationRegistration("app1", "access",
                "2", API_PROVIDER);
        Mockito.verify(apiMgtDAO, Mockito.times(1))
                .deleteApplicationRegistration(Mockito.anyInt(), Mockito.anyString(),Mockito.anyString());
    }

    @Test
    public void testGetPublishedAPIsByProvider1()
            throws APIManagementException, RegistryException, org.wso2.carbon.user.core.UserStoreException {
        APIConsumerImpl apiConsumer = new APIConsumerImplWrapper(userRegistry, apiMgtDAO);
        String providerId = "1";
        API api = new API(new APIIdentifier(API_PROVIDER, SAMPLE_API_NAME, SAMPLE_API_VERSION));
        API api1 = new API(new APIIdentifier(API_PROVIDER, "pizza_api", "2.0.0"));
        PowerMockito.when(APIUtil.isAllowDisplayMultipleVersions()).thenReturn(true, false);
        PowerMockito.when(APIUtil.isAllowDisplayAPIsWithMultipleStatus()).thenReturn(true, false);
        PowerMockito.when(APIUtil.getArtifactManager(userRegistry, APIConstants.API_KEY))
                .thenReturn(genericArtifactManager);
        PowerMockito.when(APIUtil.getMountedPath(Mockito.any(), Mockito.anyString())).thenReturn("system/governance");
        PowerMockito.when(APIUtil.getAPI(Mockito.any())).thenReturn(api);
        PowerMockito.when(APIUtil.replaceEmailDomainBack(Mockito.anyString())).thenReturn(providerId);
        PowerMockito.when(APIUtil.getLcStateFromArtifact((GovernanceArtifact) Mockito.any()))
                .thenReturn(APIConstants.PUBLISHED);
        GenericArtifact genericArtifact1 = new GenericArtifactImpl(new QName("local"), "artifact1");
        GenericArtifact genericArtifact2 = new GenericArtifactImpl(new QName("local"), "artifact2");
        GenericArtifact[] genericArtifacts = new GenericArtifact[] { genericArtifact1, genericArtifact2 };
        Mockito.when(genericArtifactManager.findGenericArtifacts((Map<String, List<String>>) Mockito.any()))
                .thenThrow(GovernanceException.class).thenReturn(genericArtifacts);
        PowerMockito.mockStatic(GovernanceUtils.class);
        PowerMockito.when(GovernanceUtils.getArtifactPath(Mockito.any(), Mockito.anyString())).thenReturn("/path1");
        PowerMockito.when(RegistryUtils.getAbsolutePath(Mockito.any(), Mockito.anyString())).thenReturn("/path1");
        Assert.assertNull(apiConsumer.getPublishedAPIsByProvider("1", "test_user", 5, API_PROVIDER, "John"));
        Assert.assertEquals(0,
                apiConsumer.getPublishedAPIsByProvider("1", "test_user", 5, API_PROVIDER, "John").size());
        Mockito.when(
                authorizationManager.isUserAuthorized(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(true);
        Resource resource = new ResourceImpl();
        resource.setUUID(UUID.randomUUID().toString());
        Mockito.when(userRegistry.get(Mockito.anyString())).thenReturn(resource);
        GenericArtifact genericArtifact = Mockito.mock(GenericArtifactImpl.class);
        Mockito.when(genericArtifactManager.getGenericArtifact(Mockito.anyString())).thenReturn(genericArtifact);
        Assert.assertEquals(1,
                apiConsumer.getPublishedAPIsByProvider("1", "test_user", 1, API_PROVIDER, "John").size());
        api.setVisibility("specific_to_roles");
        PowerMockito.when(MultitenantUtils.getTenantDomain(Mockito.anyString()))
                .thenReturn("carbon.super", "carbon.super", SAMPLE_TENANT_DOMAIN_1);
        PowerMockito.when(APIUtil.getAPI((GenericArtifact) Mockito.any())).thenReturn(api, api1);
        Assert.assertEquals(1,
                apiConsumer.getPublishedAPIsByProvider("1", "test_user", 5, API_PROVIDER, "John").size());
        Mockito.when(
                authorizationManager.isRoleAuthorized(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(true);
        Assert.assertEquals(1, apiConsumer.getPublishedAPIsByProvider("1", "", 5, API_PROVIDER, "John").size());

    }

    @Test
    public void testGetPublishedAPIsByProvider2()
            throws APIManagementException, RegistryException, org.wso2.carbon.user.core.UserStoreException {
        APIConsumerImpl apiConsumer = new APIConsumerImplWrapper(userRegistry, apiMgtDAO);
        String providerId = "2";
        API api = new API(new APIIdentifier(API_PROVIDER, SAMPLE_API_NAME, SAMPLE_API_VERSION));
        API api1 = new API(new APIIdentifier(API_PROVIDER, SAMPLE_API_NAME, "2.0.0"));
        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.when(APIUtil.isAllowDisplayMultipleVersions()).thenReturn(true, false);
        PowerMockito.when(APIUtil.isAllowDisplayAPIsWithMultipleStatus()).thenReturn(true, false);
        PowerMockito.when(APIUtil.getArtifactManager(userRegistry, APIConstants.API_KEY))
                .thenReturn(genericArtifactManager);
        PowerMockito.when(APIUtil.getMountedPath(Mockito.any(), Mockito.anyString())).thenReturn("system/governance");
        PowerMockito.when(APIUtil.getAPI(Mockito.any())).thenReturn(api);
        PowerMockito.when(APIUtil.replaceEmailDomainBack(Mockito.anyString())).thenReturn(providerId);
        PowerMockito.when(APIUtil.getLcStateFromArtifact((GovernanceArtifact) Mockito.any()))
                .thenReturn(APIConstants.PUBLISHED);
        GenericArtifact genericArtifact1 = new GenericArtifactImpl(new QName("local"), "artifact1");
        GenericArtifact genericArtifact2 = new GenericArtifactImpl(new QName("local"), "artifact2");
        GenericArtifact[] genericArtifacts = new GenericArtifact[] { genericArtifact1, genericArtifact2 };
        Mockito.when(genericArtifactManager.findGenericArtifacts((Map<String, List<String>>) Mockito.any()))
                .thenReturn(genericArtifacts);
        PowerMockito.mockStatic(GovernanceUtils.class);
        PowerMockito.when(GovernanceUtils.getArtifactPath(Mockito.any(), Mockito.anyString())).thenReturn("/path1");
        PowerMockito.when(RegistryUtils.getAbsolutePath(Mockito.any(), Mockito.anyString())).thenReturn("/path1");
        Association association = new Association();
        association.setDestinationPath("/destPath1");
        Association association2 = new Association();
        association2.setDestinationPath("/destPath2");
        Association[] associations = new Association[] { association, association2 };
        Mockito.when(userRegistry.getAssociations(Mockito.anyString(), Mockito.anyString()))
                .thenThrow(RegistryException.class).thenReturn(associations);
        try {
            apiConsumer.getPublishedAPIsByProvider("1", "test_user", 5, API_PROVIDER, "");
            Assert.fail("Registry exception not thrown for error scenario");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Failed to get Published APIs for provider :"));
        }
        Assert.assertEquals(0, apiConsumer.getPublishedAPIsByProvider("1", "test_user", 2, API_PROVIDER, "").size());
        Mockito.when(
                authorizationManager.isUserAuthorized(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenThrow(UserStoreException.class).thenThrow(org.wso2.carbon.user.core.UserStoreException.class)
                .thenReturn(true);
        try {
            apiConsumer.getPublishedAPIsByProvider("1", "test_user", 5, API_PROVIDER, "");
            Assert.fail("User store exception not thrown for error scenario");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Failed to get Published APIs for provider :"));
        }
        try {
            apiConsumer.getPublishedAPIsByProvider("1", "test_user", 5, API_PROVIDER, "");
            Assert.fail("User store exception not thrown for error scenario");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Failed to get Published APIs for provider :"));
        }
        Resource resource = new ResourceImpl();
        resource.setUUID(UUID.randomUUID().toString());
        Mockito.when(userRegistry.get(Mockito.anyString())).thenReturn(resource);
        GenericArtifact genericArtifact = Mockito.mock(GenericArtifactImpl.class);
        Mockito.when(genericArtifact.getLifecycleState(Mockito.anyString())).thenReturn(APIConstants.PUBLISHED);
        Mockito.when(genericArtifactManager.getGenericArtifact(Mockito.anyString())).thenReturn(genericArtifact);
        Assert.assertEquals(1, apiConsumer.getPublishedAPIsByProvider("1", "test_user", 1, API_PROVIDER, "").size());
        PowerMockito.when(APIUtil.getAPI((GenericArtifact) Mockito.any())).thenReturn(api, api1);
        Assert.assertEquals(1, apiConsumer.getPublishedAPIsByProvider("1", "test_user", 5, API_PROVIDER, "").size());
        PowerMockito.when(APIUtil.isAllowDisplayAPIsWithMultipleStatus()).thenReturn(true);
        Set<API> apiSet = apiConsumer.getPublishedAPIsByProvider("1", "test_user", 5, API_PROVIDER, "");
        Assert.assertEquals(1, apiSet.size());
        Assert.assertTrue(apiSet.contains(api1));

        String apiOwner = "Smith";
        PowerMockito.when(APIUtil.replaceEmailDomainBack(apiOwner)).thenReturn(apiOwner);
        api.setApiOwner("John");
        PowerMockito.when(APIUtil.isAllowDisplayMultipleVersions()).thenReturn(true);
        Assert.assertEquals(1, apiConsumer.getPublishedAPIsByProvider(apiOwner, "test_user", 1, apiOwner, "").size());
        Assert.assertEquals(0, apiConsumer.getPublishedAPIsByProvider("1", "test_user", 1, apiOwner, "").size());
    }

    @Test
    public void testRemoveAPIRating() throws APIManagementException {
        APIIdentifier identifier = new APIIdentifier(API_PROVIDER, SAMPLE_API_NAME, SAMPLE_API_VERSION);
        String user = "Tom";
        APIConsumerImpl apiConsumer = new APIConsumerImplWrapper(apiMgtDAO);
        Mockito.doNothing().when(apiMgtDAO).removeAPIRating(identifier, user);
        apiConsumer.removeAPIRating(identifier, user);
        Mockito.verify(apiMgtDAO, Mockito.times(1)).removeAPIRating(identifier, user);

    }

    @Test
    public void testRateAPI() throws APIManagementException {
        APIIdentifier identifier = new APIIdentifier(API_PROVIDER, SAMPLE_API_NAME, SAMPLE_API_VERSION);
        APIRating apiRating = APIRating.RATING_FOUR;
        String user = "Tom";
        APIConsumerImpl apiConsumer = new APIConsumerImplWrapper(apiMgtDAO);
        Mockito.doNothing().when(apiMgtDAO).addRating(identifier, apiRating.getRating(), user);
        apiConsumer.rateAPI(identifier, apiRating, user);
        Mockito.verify(apiMgtDAO, Mockito.times(1)).addRating(identifier, apiRating.getRating(), user);
    }

    @Test
    public void testGetApplicationKeys() throws APIManagementException {
        APIKey apiKey1 = new APIKey();
        apiKey1.setConsumerKey(UUID.randomUUID().toString());
        apiKey1.setType(APIConstants.API_KEY_TYPE_PRODUCTION);
        apiKey1.setState(UUID.randomUUID().toString());
        APIKey apiKey2 = new APIKey();
        apiKey2.setConsumerKey(UUID.randomUUID().toString());
        apiKey2.setType(APIConstants.API_KEY_TYPE_SANDBOX);
        apiKey2.setState(UUID.randomUUID().toString());
        APIConsumerImpl apiConsumer = new APIConsumerImplWrapper(apiMgtDAO);
        Map<String,String> consumerKeyMap = new HashMap<>();
        consumerKeyMap.put("default",apiKey1.getConsumerKey());
        Set<APIKey> apiKeys = new HashSet<>();
        apiKeys.add(apiKey1);
        apiKeys.add(apiKey2);
        Mockito.when(apiMgtDAO.getKeyMappingsFromApplicationId(Mockito.anyInt())).thenReturn(apiKeys);
        Mockito.when(apiMgtDAO.getConsumerkeyByApplicationIdAndKeyType(Mockito.anyInt(), Mockito.anyString()))
                .thenReturn(consumerKeyMap,consumerKeyMap);
        AccessTokenInfo accessTokenInfo = new AccessTokenInfo();
        accessTokenInfo.setAccessToken(UUID.randomUUID().toString());
        Mockito.when(keyManager.getAccessTokenByConsumerKey(Mockito.anyString())).thenReturn(accessTokenInfo);

        Mockito.when(keyManagerConfigurationDTO.isEnabled()).thenReturn(true);
        assertNotNull(apiConsumer.getApplicationKeys(1));
        assertEquals(apiConsumer.getApplicationKeys(1).size(),2);
        assertNotNull(apiConsumer.getApplicationKeys(1).iterator().next().getAccessToken());
    }
}
