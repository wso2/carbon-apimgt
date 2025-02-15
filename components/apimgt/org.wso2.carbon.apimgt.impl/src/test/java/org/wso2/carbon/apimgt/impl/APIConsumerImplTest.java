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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
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
import org.wso2.carbon.apimgt.api.ExceptionCodes;
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
import org.wso2.carbon.apimgt.api.model.SubscriptionResponse;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.SubscriptionWorkflowDTO;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.impl.factory.KeyManagerHolder;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.utils.ApplicationUtils;
import org.wso2.carbon.apimgt.impl.workflow.AbstractApplicationRegistrationWorkflowExecutor;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowException;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowExecutorFactory;
import org.wso2.carbon.apimgt.persistence.APIPersistence;
import org.wso2.carbon.apimgt.persistence.dto.DevPortalAPI;
import org.wso2.carbon.apimgt.persistence.dto.Organization;
import org.wso2.carbon.apimgt.persistence.exceptions.APIPersistenceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
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
import java.util.UUID;
import javax.cache.Caching;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.wso2.carbon.base.CarbonBaseConstants.CARBON_HOME;


@RunWith(PowerMockRunner.class)
@PrepareForTest({ WorkflowExecutorFactory.class, APIUtil.class, GovernanceUtils.class,
        ApplicationUtils.class, KeyManagerHolder.class, WorkflowExecutorFactory.class,
        AbstractApplicationRegistrationWorkflowExecutor.class, ServiceReferenceHolder.class, MultitenantUtils.class,
        RegistryUtils.class, Caching.class, APIPersistence.class, ApiMgtDAO.class })
@SuppressStaticInitializationFor({"org.wso2.carbon.apimgt.impl.utils.ApplicationUtils"})
public class APIConsumerImplTest {

    private static final Log log = LogFactory.getLog(APIConsumerImplTest.class);
    private ApiMgtDAO apiMgtDAO;
    private UserRealm userRealm;
    private RealmService realmService;
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
    private APIPersistence apiPersistenceInstance;

    @Before
    public void init() throws UserStoreException, RegistryException, APIManagementException {
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
        apiPersistenceInstance = Mockito.mock(APIPersistence.class);
        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.mockStatic(ApplicationUtils.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        PowerMockito.mockStatic(MultitenantUtils.class);
        PowerMockito.mockStatic(KeyManagerHolder.class);
        PowerMockito.mockStatic(RegistryUtils.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getRealmService()).thenReturn(realmService);
        Mockito.when(realmService.getTenantUserRealm(Mockito.anyInt())).thenReturn(userRealm);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        Mockito.when(userRealm.getUserStoreManager()).thenReturn(userStoreManager);
        Mockito.when(serviceReferenceHolder.getRegistryService()).thenReturn(registryService);
        Mockito.when(registryService.getGovernanceSystemRegistry(Mockito.anyInt())).thenReturn(userRegistry);
        Mockito.when(userRealm.getAuthorizationManager()).thenReturn(authorizationManager);
        Mockito.when(apiMgtDAO.getKeyManagerConfigurationByName(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(keyManagerConfigurationDTO);
        PowerMockito.when(KeyManagerHolder.getKeyManagerInstance(Mockito.anyString(),Mockito.anyString()))
                .thenReturn(keyManager);
        PowerMockito.when(APIUtil.replaceSystemProperty(anyString())).thenAnswer((Answer<String>) invocation -> {
            Object[] args = invocation.getArguments();
            return (String) args[0];
        });

        PowerMockito.when(keyManagerConfigurationDTO.getOrganization()).thenReturn("carbon.super");
        PowerMockito.when(keyManagerConfigurationDTO.getUuid()).thenReturn("kmv72L9T0oGtQcBwvgROZWCqd7oa");
        PowerMockito.when(keyManagerConfigurationDTO.isEnabled()).thenReturn(true);
    }

    @Test
    public void testReadMonetizationConfig() throws Exception {

        String json = "{\"EnableMonetization\":\"true\"}";
        JSONObject jsonObject = (JSONObject) new JSONParser().parse(json);
        PowerMockito.when(APIUtil.class, "getTenantConfig", Mockito.anyString()).thenReturn(jsonObject);
        APIConsumerImpl apiConsumer = new APIConsumerImplWrapper();
        boolean isEnabled = apiConsumer.isMonetizationEnabled(MultitenantConstants.TENANT_DOMAIN);
        assertTrue("Expected true but returned " + isEnabled, isEnabled);
        // error path UserStoreException
        PowerMockito.when(APIUtil.class, "getTenantConfig", Mockito.anyString()).thenThrow(APIManagementException.class);
        try {
            apiConsumer.isMonetizationEnabled(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            assertFalse(true);
        } catch (APIManagementException e) {
            assertTrue(true);
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
    public void testGetUserRating() throws APIManagementException {
        APIConsumerImpl apiConsumer = new APIConsumerImplWrapper();
        String uuid = UUID.randomUUID().toString();
        when(apiMgtDAO.getUserRating(uuid, "admin")).thenReturn(2);
        apiConsumer.apiMgtDAO = apiMgtDAO;
        assertEquals(2, apiConsumer.getUserRating(uuid, "admin"));
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
    public void testRenewConsumerSecret() throws APIManagementException {
        APIConsumerImpl apiConsumer = new APIConsumerImplWrapper(apiMgtDAO, SAMPLE_TENANT_DOMAIN_1);
        String clientId = UUID.randomUUID().toString();
        KeyManagerConfigurationDTO keyManagerConfiguration = new KeyManagerConfigurationDTO();
        keyManagerConfiguration.setEnabled(true);
        Mockito.when(apiMgtDAO.getKeyManagerConfigurationByName(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(keyManagerConfiguration);
        Mockito.when(keyManager.getNewApplicationConsumerSecret(Mockito.anyObject())).thenReturn
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

        when(apiMgtDAO.getSubscribedAPIs("testorg", subscriber, "testID" )).thenReturn(originalSubscribedAPIs);
        when(subscribedAPI.getTier()).thenReturn(tier);
        when(tier.getName()).thenReturn("tier");
        when(subscribedAPI.getAPIIdentifier()).thenReturn(apiId1);
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
        assertNotNull(apiConsumer.getSubscribedIdentifiers(subscriber, apiId1, "testID", "testorg"));
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

        when(apiMgtDAO.getSubscribedAPIs("testorg", subscriber, "testID")).thenReturn(originalSubscribedAPIs);
        when(subscribedAPI.getTier()).thenReturn(tier);
        when(tier.getName()).thenReturn("tier");
        assertNotNull(apiConsumer.getSubscribedAPIs("testorg", subscriber, "testID"));
    }

    @Test
    public void testAddApplication() throws APIManagementException, UserStoreException {
        APIConsumerImpl apiConsumer = new APIConsumerImplWrapper(apiMgtDAO);
        Application application = Mockito.mock(Application.class);
        Mockito.when(application.getName()).thenReturn("app");
        Mockito.when(application.getTier()).thenReturn("tier1");
        PowerMockito.when(application.getSubscriber()).thenReturn(new Subscriber("User1"));
        PowerMockito.when(MultitenantUtils.getTenantDomain("userID")).thenReturn("carbon.super");
        PowerMockito.when(APIUtil.isApplicationExist("userID", "app", "1", "testorg")).
                thenReturn(false);
        Map<String,Tier> tierMap = new HashMap<>();
        tierMap.put("tier1",new Tier("tier1"));
        PowerMockito.when(APIUtil.getTiers(APIConstants.TIER_APPLICATION_TYPE, "testorg")).thenReturn(tierMap);
        PowerMockito.when(APIUtil.findTier(tierMap.values(), "tier1")).thenReturn(new Tier("tier1"));
        Mockito.when(apiMgtDAO.addApplication(application, "userID", "testorg")).thenReturn(1);
        Mockito.when(apiMgtDAO.getApplicationById(Mockito.anyInt())).thenReturn(application);
        assertEquals(1, apiConsumer.addApplication(application, "userID", "testorg"));
    }
    @Test
    public void testAddApplicationInvalidTier() throws APIManagementException {
        APIConsumerImpl apiConsumer = new APIConsumerImplWrapper(apiMgtDAO);
        Application application = Mockito.mock(Application.class);
        Mockito.when(application.getName()).thenReturn("app");
        Mockito.when(application.getTier()).thenReturn("tier1");
        PowerMockito.when(application.getSubscriber()).thenReturn(new Subscriber("User1"));
        PowerMockito.when(MultitenantUtils.getTenantDomain("userID")).thenReturn("carbon.super");
        PowerMockito.when(APIUtil.isApplicationExist("userID", "app", "1", "testorg")).
                thenReturn(false);
        Map<String,Tier> tierMap = new HashMap<>();
        tierMap.put("tier2",new Tier("tier2"));
        PowerMockito.when(APIUtil.getTiers(APIConstants.TIER_APPLICATION_TYPE, "testorg")).thenReturn(tierMap);
        PowerMockito.when(APIUtil.findTier(tierMap.values(), "tier1")).thenReturn(null);
        Mockito.when(apiMgtDAO.addApplication(application, "userID", "testorg")).thenReturn(1);
        try{
            apiConsumer.addApplication(application, "userID", "testorg");
            Assert.fail();
        }catch (APIManagementException e){
            Assert.assertEquals(e.getErrorHandler(), ExceptionCodes.TIER_NAME_INVALID);
        }
    }

    @Test
    public void testAddApplicationWithSpecialCharacter() throws APIManagementException {
        APIConsumerImpl apiConsumer = new APIConsumerImplWrapper(apiMgtDAO);
        String appName = "ÅÄÖÅÄÖ";
        Application application = Mockito.mock(Application.class);
        Mockito.when(application.getName()).thenReturn(appName);
        Mockito.when(application.getTier()).thenReturn("tier1");
        Map<String,Tier> tierMap = new HashMap<>();
        tierMap.put("tier1",new Tier("tier1"));
        PowerMockito.when(APIUtil.getTiers(APIConstants.TIER_APPLICATION_TYPE, "testorg")).thenReturn(tierMap);
        PowerMockito.when(APIUtil.findTier(tierMap.values(), "tier1")).thenReturn(new Tier("tier1"));
        PowerMockito.when(application.getSubscriber()).thenReturn(new Subscriber("User1"));
        PowerMockito.when(MultitenantUtils.getTenantDomain("userID")).thenReturn("carbon.super");
        PowerMockito.when(APIUtil.isApplicationExist("userID", "app", "1", "testorg")).thenReturn(false);
        Mockito.when(apiMgtDAO.addApplication(application, "userID", "testorg")).thenReturn(1);
        Mockito.when(apiMgtDAO.getApplicationById(Mockito.anyInt())).thenReturn(application);
        assertEquals(1, apiConsumer.addApplication(application, "userID", "testorg"));
    }

    @Test
    public void testGetScopesBySubscribedAPIs() throws APIManagementException {
        APIConsumerImpl apiConsumer = new APIConsumerImplWrapper(apiMgtDAO);
        List<String> identifiers = new ArrayList<>();
        Set<String> scopes = new HashSet<>();
        when(apiMgtDAO.getScopesBySubscribedAPIs(identifiers)).thenReturn(scopes);
        assertEquals(scopes, apiConsumer.getScopesBySubscribedAPIs(identifiers));
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
    public void testUpdateAuthClient() throws APIManagementException {
        String consumerKey = "aNTf-EFga";
        OAuthApplicationInfo oAuthApplicationInfo = new OAuthApplicationInfo();
        OAuthAppRequest oAuthAppRequest = new OAuthAppRequest();
        oAuthApplicationInfo.setClientId(UUID.randomUUID().toString());
        oAuthAppRequest.setOAuthApplicationInfo(oAuthApplicationInfo);
        BDDMockito.when(ApplicationUtils
                .createOauthAppRequest(Mockito.isNull(), Mockito.isNull(), Mockito.anyString(),
                        Mockito.isNull(), Mockito.isNull(), Mockito.isNull(), Mockito.anyString(),
                        Mockito.anyString()))
                .thenReturn(oAuthAppRequest);
        Mockito.when(apiMgtDAO
                .getConsumerKeyByApplicationIdKeyTypeKeyManager(Mockito.anyInt(),Mockito.anyString(),
                        Mockito.anyString())).thenReturn(consumerKey);
        OAuthApplicationInfo updatedAppInfo = new OAuthApplicationInfo();
        String clientName = "sample client";
        updatedAppInfo.setClientName(clientName);
        Mockito.when(keyManager.updateApplication((OAuthAppRequest) Mockito.any())).thenReturn(updatedAppInfo);
        KeyManagerConfigurationDTO keyManagerConfiguration = new KeyManagerConfigurationDTO();
        keyManagerConfiguration.setEnabled(true);
        keyManagerConfiguration.setOrganization(SAMPLE_TENANT_DOMAIN_1);
        Mockito.when(apiMgtDAO.getKeyManagerConfigurationByName(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(keyManagerConfiguration);
        System.setProperty(CARBON_HOME, "");
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        APIManagerConfigurationService apiManagerConfigurationService = Mockito.
                mock(APIManagerConfigurationService.class);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfigurationService()).thenReturn(apiManagerConfigurationService);
        Mockito.when(apiManagerConfigurationService.getAPIManagerConfiguration()).thenReturn(apiManagerConfiguration);
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.KEYMANAGER_SERVERURL)).
                thenReturn("http://localhost");

        Application application = Mockito.mock(Application.class);
        application.setUUID(UUID.nameUUIDFromBytes("app1".getBytes()).toString());
        Subscriber subscriber = Mockito.mock(Subscriber.class);
        Mockito.when(ApplicationUtils
                .retrieveApplication("app1", "1", null))
                .thenReturn(application);
        Mockito.when(application.getSubscriber()).thenReturn(subscriber);
        Mockito.when(subscriber.getName()).thenReturn("1");

        APIConsumerImpl apiConsumer = new APIConsumerImplWrapper(apiMgtDAO, SAMPLE_TENANT_DOMAIN_1);
        Assert.assertEquals(apiConsumer
                .updateAuthClient("1", application, "access", "www.host.com", new String[0], null, null, null, null,
                        "default")
                .getClientName(), clientName);
    }


    @Test
    public void testGetApplicationsWithPagination() throws APIManagementException {
        Application[] applications = new Application[] { new Application(1), new Application(2) };
        Mockito.when(apiMgtDAO
                .getApplicationsWithPagination(Mockito.any(), Mockito.anyString(), Mockito.anyInt(),
                        Mockito.anyInt(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                        Mockito.anyString(), Mockito.anyString())).thenReturn(applications);
        APIConsumerImpl apiConsumer = new APIConsumerImplWrapper(apiMgtDAO);
        Assert.assertEquals(
                apiConsumer.getApplicationsWithPagination(new Subscriber("sub1"), "1", 0, 5,
                        "", "", "ASC", "testorg",
                        "sharedOrg").length, 2);
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
    public void testRequestApprovalForApplicationRegistration() throws APIManagementException, UserStoreException {
        Scope scope1 = new Scope();
        scope1.setName("api_view");
        Scope scope2 = new Scope();
        scope2.setName("api_create");
        Set<Scope> scopes = new HashSet<Scope>();
        scopes.add(scope1);
        scopes.add(scope2);
        PowerMockito.when(MultitenantUtils.getTenantDomain(Mockito.anyString())).thenReturn("abc.org");
        KeyManagerConfigurationDTO keyManagerConfigurationsDto = new KeyManagerConfigurationDTO();
        keyManagerConfigurationsDto.setEnabled(true);
        Mockito.when(apiMgtDAO.getKeyManagerConfigurationByName("abc.org", "default"))
                .thenReturn(keyManagerConfigurationsDto);
        Mockito.when(tenantManager.getTenantId(Mockito.anyString())).thenThrow(UserStoreException.class)
                .thenReturn(-1234, 1);
        APIConsumerImpl apiConsumer = new APIConsumerImplWrapper(apiMgtDAO);
        Application app = new Application("app1", new Subscriber("1"));
        app.setGroupId("2");
        app.setUUID(UUID.randomUUID().toString());

//        Mockito.when(userStoreManager.getRoleListOfUser(Mockito.anyString())).thenThrow(UserStoreException.class);

        Application application = Mockito.mock(Application.class);
        Subscriber subscriber = Mockito.mock(Subscriber.class);
        Mockito.when(subscriber.getName()).thenReturn("1");
        Mockito.when(application.getSubscriber()).thenReturn(subscriber);
        Mockito.when(ApplicationUtils
                .retrieveApplication(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(application);

        try {
            apiConsumer
                    .requestApprovalForApplicationRegistration("1", app, "access", "identity.com/auth", null, "3600",
                            "api_view", null, "default", null, false);
            Assert.fail("API management exception not thrown for invalid token type");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Invalid Token Type"));
        }
        Mockito.when(userStoreManager.getRoleListOfUser(Mockito.anyString())).thenReturn(new String[] { "role1", "role2" });
        scope1.setRoles("role1");
        scope2.setRoles("role2");
        OAuthApplicationInfo oAuthApplicationInfo = new OAuthApplicationInfo();
        OAuthAppRequest oAuthAppRequest = new OAuthAppRequest();
        oAuthAppRequest.setOAuthApplicationInfo(oAuthApplicationInfo);
        application = new Application("app1", new Subscriber("1"));
        BDDMockito.when(ApplicationUtils
                .createOauthAppRequest(Mockito.anyString(), Mockito.isNull(), Mockito.anyString(),
                        Mockito.anyString(), Mockito.isNull(), Mockito.anyString(),
                        Mockito.anyString(), Mockito.anyString())).thenReturn(oAuthAppRequest);
        BDDMockito.when(ApplicationUtils
                .retrieveApplication(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(application);
        Map<String, Object> result = apiConsumer
                .requestApprovalForApplicationRegistration("1", app, APIConstants.API_KEY_TYPE_PRODUCTION,
                        "identity.com/auth", null, "3600", "api_view", null, "default", null, false);
        Assert.assertEquals(result.size(),10);
        Assert.assertEquals(result.get("keyState"), "APPROVED");

        BDDMockito.when(ApplicationUtils
                .createOauthAppRequest(Mockito.anyString(), Mockito.isNull(), Mockito.isNull(),
                        Mockito.anyString(), Mockito.isNull(), Mockito.anyString(),
                        Mockito.anyString(), Mockito.anyString())).thenReturn(oAuthAppRequest);
        result = apiConsumer
                .requestApprovalForApplicationRegistration("1", app, APIConstants.API_KEY_TYPE_SANDBOX, "", null,
                        "3600", "api_view", null, "default", null, false);
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
    public void testResetApplicationThrottlePolicy() throws APIManagementException {
        Application application = new Application("app", new Subscriber("sub1"));
        application.setGroupId("testGroupId");
        application.setId(5);
        application.setTier("testTier");

        Mockito.when(apiMgtDAO.getApplicationByUUID(Mockito.anyString())).thenReturn(application);
        APIConsumerImpl apiConsumer = new APIConsumerImplWrapper(apiMgtDAO);
        try {
            apiConsumer.resetApplicationThrottlePolicy("1", "testUser", "testOrg");
            Assert.fail("API management exception not thrown for error scenario");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Application is not accessible to user"));
        }
    }

    @Test
    public void testTokenTypeChangeWhenUpdatingApplications() throws APIManagementException {

        Application oldApplication = new Application("app1", new Subscriber("sub1"));
        oldApplication.setTier("tier1");
        oldApplication.setOrganization("testorg");
        Application newApplication = new Application("app1", new Subscriber("sub1"));
        newApplication.setOrganization("testorg");
        newApplication.setTier("tier2");
        Mockito.when(apiMgtDAO.getApplicationById(Mockito.anyInt())).thenReturn(oldApplication);
        Mockito.when(apiMgtDAO.getApplicationByUUID(Mockito.anyString())).thenReturn(oldApplication);
        APIConsumerImpl apiConsumer = new APIConsumerImplWrapper(apiMgtDAO);
        Map<String,Tier> tierMap = new HashMap<>();
        tierMap.put("tier1",new Tier("tier1"));
        tierMap.put("tier2",new Tier("tier2"));
        PowerMockito.when(APIUtil.getTiers(APIConstants.TIER_APPLICATION_TYPE, "testorg")).thenReturn(tierMap);
        PowerMockito.when(APIUtil.findTier(tierMap.values(), "tier2")).thenReturn(new Tier("tier2"));

        // When token type of existing application is 'JWT' and request body contains 'OAUTH' as the token type.
        oldApplication.setTokenType(APIConstants.TOKEN_TYPE_JWT);
        newApplication.setTokenType(APIConstants.TOKEN_TYPE_OAUTH);
        try {
            // An exception will be thrown during this operation.
            apiConsumer.updateApplication(newApplication);
            Assert.fail("API management exception not thrown for error scenario");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains(
                    "Cannot change application token type from " + APIConstants.TOKEN_TYPE_JWT + " to " +
                            newApplication.getTokenType()));
        }

        // When token type of existing application is 'JWT' and request body contains 'JWT' as the token type.
        oldApplication.setTokenType(APIConstants.TOKEN_TYPE_JWT);
        newApplication.setTokenType(APIConstants.TOKEN_TYPE_JWT);
        try {
            // Token type of newApplication will not change during this operation.
            apiConsumer.updateApplication(newApplication);
            Assert.assertEquals(APIConstants.TOKEN_TYPE_JWT, newApplication.getTokenType());
        } catch (APIManagementException e) {
            Assert.fail("API management exception is thrown due to an error");
        }

        // When token type of existing application is 'OAUTH' and request body contains 'OAUTH' as the token type.
        oldApplication.setTokenType(APIConstants.TOKEN_TYPE_OAUTH);
        newApplication.setTokenType(APIConstants.TOKEN_TYPE_OAUTH);
        try {
            // Token type of newApplication will not change during this operation.
            apiConsumer.updateApplication(newApplication);
            Assert.assertEquals(APIConstants.TOKEN_TYPE_OAUTH, newApplication.getTokenType());
        } catch (APIManagementException e) {
            Assert.fail("API management exception is thrown due to an error");
        }

        // When token type of existing application is 'OAUTH' and request body contains 'JWT' as the token type.
        oldApplication.setTokenType(APIConstants.TOKEN_TYPE_OAUTH);
        newApplication.setTokenType(APIConstants.TOKEN_TYPE_JWT);
        try {
            // Token type of newApplication will not change during this operation.
            apiConsumer.updateApplication(newApplication);
            Assert.assertEquals(APIConstants.TOKEN_TYPE_JWT, newApplication.getTokenType());
        } catch (APIManagementException e) {
            Assert.fail("API management exception is thrown due to an error");
        }

        // When token type of existing application is 'DEFAULT' and request body contains 'OAUTH' as the token type.
        oldApplication.setTokenType(APIConstants.DEFAULT_TOKEN_TYPE);
        newApplication.setTokenType(APIConstants.TOKEN_TYPE_OAUTH);
        try {
            // Token type of newApplication will change to 'DEFAULT' during this operation.
            apiConsumer.updateApplication(newApplication);
            Assert.assertEquals(APIConstants.DEFAULT_TOKEN_TYPE, newApplication.getTokenType());
        } catch (APIManagementException e) {
            Assert.fail("API management exception is thrown due to an error");
        }

        // When token type of existing application is 'DEFAULT' and request body contains 'JWT' as the token type.
        oldApplication.setTokenType(APIConstants.DEFAULT_TOKEN_TYPE);
        newApplication.setTokenType(APIConstants.TOKEN_TYPE_JWT);
        try {
            // Token type of newApplication will not change during this operation.
            apiConsumer.updateApplication(newApplication);
            Assert.assertEquals(APIConstants.TOKEN_TYPE_JWT, newApplication.getTokenType());
        } catch (APIManagementException e) {
            Assert.fail("API management exception is thrown due to an error");
        }
    }

    @Test
    public void testGetComments() throws APIManagementException {
        Comment comment = new Comment();
        Comment[] comments = new Comment[] { comment };
        String uuid = UUID.randomUUID().toString();
        Mockito.when(apiMgtDAO.getComments(uuid, null)).thenReturn(comments);
        Assert.assertEquals(new APIConsumerImplWrapper(apiMgtDAO).getComments(uuid, null).length, 1);
        Mockito.verify(apiMgtDAO, Mockito.times(1)).getComments(uuid, null);
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
    public void testRemoveSubscription() throws APIManagementException, WorkflowException, APIPersistenceException {
        String uuid = UUID.randomUUID().toString();
        String apiUUID = UUID.randomUUID().toString();
        Subscriber subscriber = new Subscriber("sub1");
        Application application = new Application("app1", subscriber);
        application.setId(1);
        APIIdentifier identifier = new APIIdentifier(API_PROVIDER, SAMPLE_API_NAME, SAMPLE_API_VERSION);
        identifier.setUuid(apiUUID);
        SubscribedAPI subscribedAPIOld = new SubscribedAPI(subscriber, identifier);
        subscribedAPIOld.setApplication(application);
        Mockito.when(apiMgtDAO.isAppAllowed(Mockito.anyInt(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(true);
        PowerMockito.mockStatic(ApiMgtDAO.class);
        PowerMockito.when(ApiMgtDAO.getInstance()).thenReturn(apiMgtDAO);
        Mockito.when(apiMgtDAO.checkAPIUUIDIsARevisionUUID(Mockito.anyString())).thenReturn(null);
        DevPortalAPI devPortalAPI = Mockito.mock(DevPortalAPI.class);
        Mockito.when(apiPersistenceInstance.getDevPortalAPI(any(Organization.class), any(String.class)))
                .thenReturn(devPortalAPI);
        SubscribedAPI subscribedAPINew = new SubscribedAPI(subscriber, identifier);
        subscribedAPINew.setUUID(uuid);
        subscribedAPINew.setApplication(application);
        APIConsumerImpl apiConsumer = new APIConsumerImplWrapper(apiMgtDAO, apiPersistenceInstance);
        apiConsumer.removeSubscription(subscribedAPINew, "org1");
        Mockito.verify(apiMgtDAO, Mockito.times(1)).getApplicationNameFromId(Mockito.anyInt());
        String workflowExtRef = "test_wf_ref";
        String workflowExtRef1 = "complete_wf_ref";
        Mockito.when(apiMgtDAO
                .getExternalWorkflowReferenceForSubscription((APIIdentifier) Mockito.any(), Mockito.anyInt(),
                        Mockito.anyString())).thenReturn(workflowExtRef, workflowExtRef1);
        SubscriptionWorkflowDTO subscriptionWorkflowDTO = new SubscriptionWorkflowDTO();
        subscriptionWorkflowDTO.setWorkflowReference("1");
        Mockito.when(apiMgtDAO.retrieveWorkflow(workflowExtRef)).thenReturn(subscriptionWorkflowDTO);
        SubscribedAPI subscribedAPI = new SubscribedAPI("api1");
        subscribedAPI.setTier(new Tier("Gold"));
        Mockito.when(apiMgtDAO.getSubscriptionById(Mockito.anyInt())).thenReturn(subscribedAPI);
        Mockito.when(apiMgtDAO.getSubscriptionStatus(uuid, 1))
                .thenReturn(APIConstants.SubscriptionStatus.ON_HOLD);
        apiConsumer.removeSubscription(subscribedAPINew, "org1");
        Mockito.when(apiMgtDAO.retrieveWorkflow(workflowExtRef1)).thenReturn(subscriptionWorkflowDTO);
        PowerMockito.when(MultitenantUtils.getTenantDomain(Mockito.anyString())).thenReturn("abc.org");
        apiConsumer.removeSubscription(subscribedAPINew, "org1");
        Mockito.verify(apiMgtDAO, Mockito.times(2)).retrieveWorkflow(Mockito.anyString());

    }

    @Test
    public void testRemoveSubscriptionWhenDeletePending() throws APIManagementException, WorkflowException, APIPersistenceException {
        String uuid = UUID.randomUUID().toString();
        String apiUUID = UUID.randomUUID().toString();
        final String deletionWorkflowRef = "SUB_DELETION_WORKFLOW_REF";
        WorkflowDTO deletionWorkflow = new SubscriptionWorkflowDTO();
        deletionWorkflow.setStatus(org.wso2.carbon.apimgt.impl.workflow.WorkflowStatus.CREATED);
        Subscriber subscriber = new Subscriber("sub1");
        Application application = new Application("app1", subscriber);
        application.setId(1);
        APIIdentifier identifier = new APIIdentifier(API_PROVIDER, SAMPLE_API_NAME, SAMPLE_API_VERSION);
        identifier.setUuid(apiUUID);
        SubscribedAPI subscribedAPIOld = new SubscribedAPI(subscriber, identifier);
        subscribedAPIOld.setApplication(application);
        Mockito.when(apiMgtDAO.getExternalWorkflowReferenceForSubscriptionAndWFType(Mockito.anyInt(), Mockito.anyString()))
                .thenReturn(deletionWorkflowRef);
        Mockito.when(apiMgtDAO.retrieveWorkflow(deletionWorkflowRef)).thenReturn(deletionWorkflow);

        PowerMockito.mockStatic(ApiMgtDAO.class);
        PowerMockito.when(ApiMgtDAO.getInstance()).thenReturn(apiMgtDAO);
        Mockito.when(apiMgtDAO.checkAPIUUIDIsARevisionUUID(Mockito.anyString())).thenReturn(null);
        DevPortalAPI devPortalAPI = Mockito.mock(DevPortalAPI.class);
        Mockito.when(apiPersistenceInstance.getDevPortalAPI(any(Organization.class), any(String.class)))
                .thenReturn(devPortalAPI);
        SubscribedAPI subscribedAPINew = new SubscribedAPI(subscriber, identifier);
        subscribedAPINew.setUUID(uuid);
        subscribedAPINew.setApplication(application);
        APIConsumerImpl apiConsumer = new APIConsumerImplWrapper(apiMgtDAO, apiPersistenceInstance);
        apiConsumer.removeSubscription(subscribedAPINew, "org1");
        Assert.assertEquals(-1, subscribedAPINew.getSubscriptionId());
        Assert.assertEquals(APIConstants.SubscriptionStatus.DELETE_PENDING, subscribedAPINew.getSubStatus());
    }


    @Test
    public void testAddSubscription() throws APIManagementException {
        API api  = new API(new APIIdentifier(API_PROVIDER, "published_api", SAMPLE_API_VERSION));
        api.setSubscriptionAvailability(APIConstants.SUBSCRIPTION_TO_ALL_TENANTS);
        Application application = new Application(1);
        api.setStatus(APIConstants.PUBLISHED);
        Set<Tier> tiers = new HashSet<>();
        tiers.add(new Tier("tier1"));
        api.setAvailableTiers(tiers);
        ApiTypeWrapper apiTypeWrapper = new ApiTypeWrapper(api);
        apiTypeWrapper.setTier("tier1");
        String tenantAwareUsername = "user1@"+SAMPLE_TENANT_DOMAIN_1;
        Mockito.when(MultitenantUtils.getTenantAwareUsername(Mockito.eq("user1"))).thenReturn(tenantAwareUsername);
        Mockito.when(apiMgtDAO.addSubscription(apiTypeWrapper, application, APIConstants.SubscriptionStatus.ON_HOLD,
                tenantAwareUsername)).thenReturn(1);
        SubscribedAPI subscribedAPI = new SubscribedAPI(UUID.randomUUID().toString());
        Mockito.when(apiMgtDAO.getSubscriptionById(1)).thenReturn(subscribedAPI);
        APIConsumerImpl apiConsumer = new APIConsumerImplWrapper(apiMgtDAO, SAMPLE_TENANT_DOMAIN_1);
        SubscriptionResponse subscriptionResponse = apiConsumer.addSubscription(apiTypeWrapper, "user1",application);
        Assert.assertEquals(subscriptionResponse.getSubscriptionUUID(), subscribedAPI.getUUID());
        try {
            api.setStatus(APIConstants.CREATED);
            apiConsumer.addSubscription(apiTypeWrapper, "sub1", application);
            Assert.fail("Resource not found exception not thrown for wrong api state");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Subscriptions not allowed on APIs/API Products in the state"));
        }
    }

    @Test
    public void testAddSubscriptionInvalidTier() throws APIManagementException {
        API api  = new API(new APIIdentifier(API_PROVIDER, "published_api", SAMPLE_API_VERSION));
        api.setSubscriptionAvailability(APIConstants.SUBSCRIPTION_TO_ALL_TENANTS);
        Application application = new Application(1);
        api.setStatus(APIConstants.PUBLISHED);
        Set<Tier> tiers = new HashSet<>();
        tiers.add(new Tier("tier1"));
        api.setAvailableTiers(tiers);
        ApiTypeWrapper apiTypeWrapper = new ApiTypeWrapper(api);
        apiTypeWrapper.setTier("tier2");
        Mockito.when(apiMgtDAO.addSubscription(Mockito.eq(apiTypeWrapper), Mockito.eq(application), Mockito.anyString(),
                Mockito.anyString())).thenReturn(1);
        SubscribedAPI subscribedAPI = new SubscribedAPI(UUID.randomUUID().toString());
        Mockito.when(apiMgtDAO.getSubscriptionById(1)).thenReturn(subscribedAPI);
        APIConsumerImpl apiConsumer = new APIConsumerImplWrapper(apiMgtDAO, SAMPLE_TENANT_DOMAIN_1);
        try {
            apiConsumer.addSubscription(apiTypeWrapper, "sub1", application);
            Assert.fail("Invalid Tier error not thrown.");
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getErrorHandler().getErrorCode(),
                    ExceptionCodes.SUBSCRIPTION_TIER_NOT_ALLOWED.getErrorCode());
        }
    }


    @Test
    public void testMapExistingOAuthClient() throws APIManagementException {
        APIConsumerImpl apiConsumer = new APIConsumerImplWrapper(apiMgtDAO);
        apiConsumer.tenantDomain = "carbon.super";
        OAuthApplicationInfo oAuthApplicationInfo = new OAuthApplicationInfo();
        oAuthApplicationInfo.setClientId(UUID.randomUUID().toString());
        OAuthAppRequest oAuthAppRequest = new OAuthAppRequest();
        oAuthAppRequest.setOAuthApplicationInfo(oAuthApplicationInfo);
        BDDMockito.when(ApplicationUtils
                .createOauthAppRequest(Mockito.anyString(), Mockito.anyString(), Mockito.isNull(),
                        Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                        Mockito.anyString(), Mockito.anyString())).thenReturn(oAuthAppRequest);
        Mockito.when(apiMgtDAO.isKeyMappingExistsForConsumerKeyOrApplication(Mockito.anyInt(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(true, false);
        Mockito.when(keyManager.mapOAuthApplication((OAuthAppRequest) Mockito.any())).thenReturn(oAuthApplicationInfo);
        Mockito.doNothing().when(apiMgtDAO).createApplicationKeyTypeMappingForManualClients(Mockito.anyString(),
                Mockito.anyInt(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        KeyManagerConfigurationDTO keyManagerConfigurationsDto = new KeyManagerConfigurationDTO();
        keyManagerConfigurationsDto.setUuid(UUID.randomUUID().toString());
        keyManagerConfigurationsDto.setEnabled(true);
        Mockito.when(apiMgtDAO.isKeyManagerConfigurationExistByName( "default","carbon.super"))
                .thenReturn(true);
        Mockito.when(apiMgtDAO.getKeyManagerConfigurationByName("carbon.super", "default")).thenReturn(keyManagerConfigurationsDto);
        AccessTokenRequest accessTokenRequest = new AccessTokenRequest();
        AccessTokenInfo accessTokenInfo = new AccessTokenInfo();
        KeyManagerConfiguration keyManagerConfiguration = new KeyManagerConfiguration();
        Mockito.when(keyManager.getKeyManagerConfiguration()).thenReturn(keyManagerConfiguration);
        BDDMockito.when(ApplicationUtils.createAccessTokenRequest(keyManager,oAuthApplicationInfo, null)).thenReturn
                (accessTokenRequest);
        Mockito.when(keyManager.getNewApplicationAccessToken(accessTokenRequest)).thenReturn(accessTokenInfo);
        Application application = new Application(1);
        application.setName("app1");
        try {
            apiConsumer.mapExistingOAuthClient("", "admin", "1", application,
                    "refresh", "DEFAULT", "Resident Key Manager", "carbon.super");
            Assert.fail("Exception is not thrown when client id is already mapped to an application");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Key Mappings already exists for application"));
        }
        Assert.assertEquals(8, apiConsumer.mapExistingOAuthClient("", "admin", "1",
                application, "PRODUCTION", "DEFAULT", "Resident Key Manager",
                "carbon.super").size());
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
    public void testRemoveAPIRating() throws APIManagementException {
        String uuid = UUID.randomUUID().toString();
        String user = "Tom";
        APIConsumerImpl apiConsumer = new APIConsumerImplWrapper(apiMgtDAO);
        Mockito.doNothing().when(apiMgtDAO).removeAPIRating(uuid, user);
        apiConsumer.removeAPIRating(uuid, user);
        Mockito.verify(apiMgtDAO, Mockito.times(1)).removeAPIRating(uuid, user);

    }

    @Test
    public void testRateAPI() throws APIManagementException {
        String uuid = UUID.randomUUID().toString();
        APIRating apiRating = APIRating.RATING_FOUR;
        String user = "Tom";
        APIConsumerImpl apiConsumer = new APIConsumerImplWrapper(apiMgtDAO);
        Mockito.doNothing().when(apiMgtDAO).addRating(uuid, apiRating.getRating(), user);
        apiConsumer.rateAPI(uuid, apiRating, user);
        Mockito.verify(apiMgtDAO, Mockito.times(1)).addRating(uuid, apiRating.getRating(), user);
    }

    @Test
    public void testGetApplicationKeys() throws APIManagementException {
        APIKey apiKey1 = new APIKey();
        apiKey1.setConsumerKey(UUID.randomUUID().toString());
        apiKey1.setType(APIConstants.API_KEY_TYPE_PRODUCTION);
        apiKey1.setState(UUID.randomUUID().toString());
        apiKey1.setKeyManager(APIConstants.KeyManager.DEFAULT_KEY_MANAGER);
        APIKey apiKey2 = new APIKey();
        apiKey2.setConsumerKey(UUID.randomUUID().toString());
        apiKey2.setType(APIConstants.API_KEY_TYPE_SANDBOX);
        apiKey2.setState(UUID.randomUUID().toString());
        apiKey2.setKeyManager(APIConstants.KeyManager.DEFAULT_KEY_MANAGER);
        APIConsumerImpl apiConsumer = new APIConsumerImplWrapper(apiMgtDAO);
        Map<String, String> consumerKeyMap = new HashMap<>();
        consumerKeyMap.put("default", apiKey1.getConsumerKey());
        Set<APIKey> apiKeys = new HashSet<>();
        apiKeys.add(apiKey1);
        apiKeys.add(apiKey2);
        Mockito.when(apiMgtDAO.getKeyMappingsFromApplicationId(Mockito.anyInt())).thenReturn(apiKeys);
        Mockito.when(apiMgtDAO.getConsumerkeyByApplicationIdAndKeyType(Mockito.anyInt(), Mockito.anyString()))
                .thenReturn(consumerKeyMap);
        AccessTokenInfo accessTokenInfo = new AccessTokenInfo();
        accessTokenInfo.setAccessToken(UUID.randomUUID().toString());
        Mockito.when(keyManager.getAccessTokenByConsumerKey(Mockito.anyString())).thenReturn(accessTokenInfo);
        Mockito.when(keyManagerConfigurationDTO.isEnabled()).thenReturn(true);

        Set<APIKey> apiKeySet = apiConsumer.getApplicationKeys(1);
        assertNotNull(apiKeySet);
        assertEquals(apiKeySet.size(), 2);
        assertNotNull(apiKeySet.iterator().next().getAccessToken());
    }
}
