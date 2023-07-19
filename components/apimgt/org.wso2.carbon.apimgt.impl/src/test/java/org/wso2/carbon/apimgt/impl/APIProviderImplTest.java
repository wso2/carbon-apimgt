/*
 *  Copyright (c), WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.util.JavaUtils;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.BlockConditionNotFoundException;
import org.wso2.carbon.apimgt.api.FaultGatewaysException;
import org.wso2.carbon.apimgt.api.dto.UserApplicationAPIUsage;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.api.model.APIProductIdentifier;
import org.wso2.carbon.apimgt.api.model.APIPublisher;
import org.wso2.carbon.apimgt.api.model.APIRevision;
import org.wso2.carbon.apimgt.api.model.APISearchResult;
import org.wso2.carbon.apimgt.api.model.APIStateChangeResponse;
import org.wso2.carbon.apimgt.api.model.APIStore;
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;
import org.wso2.carbon.apimgt.api.model.BlockConditionsDTO;
import org.wso2.carbon.apimgt.api.model.CORSConfiguration;
import org.wso2.carbon.apimgt.api.model.Documentation;
import org.wso2.carbon.apimgt.api.model.Documentation.DocumentSourceType;
import org.wso2.carbon.apimgt.api.model.Documentation.DocumentVisibility;
import org.wso2.carbon.apimgt.api.model.DocumentationType;
import org.wso2.carbon.apimgt.api.model.KeyManager;
import org.wso2.carbon.apimgt.api.model.OperationPolicy;
import org.wso2.carbon.apimgt.api.model.OperationPolicyData;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.api.model.policy.APIPolicy;
import org.wso2.carbon.apimgt.api.model.policy.ApplicationPolicy;
import org.wso2.carbon.apimgt.api.model.policy.Condition;
import org.wso2.carbon.apimgt.api.model.policy.GlobalPolicy;
import org.wso2.carbon.apimgt.api.model.policy.HTTPVerbCondition;
import org.wso2.carbon.apimgt.api.model.policy.Pipeline;
import org.wso2.carbon.apimgt.api.model.policy.PolicyConstants;
import org.wso2.carbon.apimgt.api.model.policy.QuotaPolicy;
import org.wso2.carbon.apimgt.api.model.policy.RequestCountLimit;
import org.wso2.carbon.apimgt.api.model.policy.SubscriptionPolicy;
import org.wso2.carbon.apimgt.impl.certificatemgt.CertificateManagerImpl;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dao.GatewayArtifactsMgtDAO;
import org.wso2.carbon.apimgt.impl.dao.ScopesDAO;
import org.wso2.carbon.apimgt.impl.definitions.OASParserUtil;
import org.wso2.carbon.apimgt.impl.dto.GatewayArtifactSynchronizerProperties;
import org.wso2.carbon.apimgt.impl.dto.KeyManagerDto;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.impl.factory.KeyManagerHolder;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.ArtifactSaver;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.exception.ArtifactSynchronizerException;
import org.wso2.carbon.apimgt.impl.importexport.APIImportExportException;
import org.wso2.carbon.apimgt.impl.importexport.ImportExportAPI;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowConstants;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowException;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowExecutor;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowExecutorFactory;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowStatus;
import org.wso2.carbon.apimgt.persistence.APIPersistence;
import org.wso2.carbon.apimgt.persistence.dto.MediationInfo;
import org.wso2.carbon.apimgt.persistence.dto.Organization;
import org.wso2.carbon.apimgt.persistence.dto.PublisherAPI;
import org.wso2.carbon.apimgt.persistence.dto.PublisherAPIInfo;
import org.wso2.carbon.apimgt.persistence.dto.PublisherAPISearchResult;
import org.wso2.carbon.apimgt.persistence.dto.UserContext;
import org.wso2.carbon.apimgt.persistence.exceptions.APIPersistenceException;
import org.wso2.carbon.apimgt.persistence.exceptions.MediationPolicyPersistenceException;
import org.wso2.carbon.apimgt.persistence.utils.RegistryPersistenceUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.custom.lifecycles.checklist.util.LifecycleBeanPopulator;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.pagination.PaginationContext;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.UUID;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.ArrayList;

import javax.cache.Caching;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.wso2.carbon.apimgt.impl.token.ClaimsRetriever.DEFAULT_DIALECT_URI;

@RunWith(PowerMockRunner.class)
@SuppressStaticInitializationFor("org.wso2.carbon.context.PrivilegedCarbonContext")
@PrepareForTest({ ServiceReferenceHolder.class, ApiMgtDAO.class, APIUtil.class, APIGatewayManager.class,
        GovernanceUtils.class, PrivilegedCarbonContext.class, WorkflowExecutorFactory.class, JavaUtils.class,
        APIProviderImpl.class, APIManagerFactory.class, RegistryUtils.class, LifecycleBeanPopulator.class,
        Caching.class, PaginationContext.class, MultitenantUtils.class, AbstractAPIManager.class, OASParserUtil.class,
        KeyManagerHolder.class, CertificateManagerImpl.class , PublisherAPI.class, Organization.class,
        APIPersistence.class, GatewayArtifactsMgtDAO.class, RegistryPersistenceUtil.class})
@PowerMockIgnore("org.mockito.*")
public class APIProviderImplTest {

    private ApiMgtDAO apimgtDAO;
    private GatewayArtifactsMgtDAO gatewayArtifactsMgtDAO;
    private ScopesDAO scopesDAO;
    private KeyManager keyManager;
    private Registry registry;
    private GenericArtifactManager artifactManager;
    private APIGatewayManager gatewayManager;
    private GenericArtifact artifact;
    private CertificateManagerImpl certificateManager;
    private APIManagerConfiguration config;
    private APIPersistence apiPersistenceInstance;
    private String superTenantDomain;
    private String apiUUID = "12640983654";
    private final String artifactPath = "artifact/path";

    @Before
    public void init() throws Exception {
        System.setProperty("carbon.home", APIProviderImplTest.class.getResource("/").getFile());
        PowerMockito.mockStatic(ApiMgtDAO.class);
        PowerMockito.mockStatic(GatewayArtifactsMgtDAO.class);
        PowerMockito.mockStatic(PrivilegedCarbonContext.class);
        PowerMockito.mockStatic(RegistryUtils.class);
        PowerMockito.mockStatic(GovernanceUtils.class);
        PowerMockito.mockStatic(WorkflowExecutorFactory.class);
        PowerMockito.mockStatic(LifecycleBeanPopulator.class);
        PowerMockito.mockStatic(KeyManagerHolder.class);
        PowerMockito.mockStatic(Caching.class);
        PowerMockito.mockStatic(PaginationContext.class);
        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.mockStatic(APIGatewayManager.class);
        PowerMockito.mockStatic(CertificateManagerImpl.class);
        PowerMockito.mockStatic(RegistryPersistenceUtil.class);
        apimgtDAO = Mockito.mock(ApiMgtDAO.class);
        gatewayArtifactsMgtDAO = Mockito.mock(GatewayArtifactsMgtDAO.class);
        scopesDAO = Mockito.mock(ScopesDAO.class);
        keyManager = Mockito.mock(KeyManager.class);
        apiPersistenceInstance = Mockito.mock(APIPersistence.class);
        certificateManager = Mockito.mock(CertificateManagerImpl.class);
        Mockito.when(keyManager.getResourceByApiId(Mockito.anyString())).thenReturn(null);
        Mockito.when(keyManager.registerNewResource(Mockito.any(API.class), Mockito.any(Map.class))).thenReturn(true);
        KeyManagerDto keyManagerDto = new KeyManagerDto();
        keyManagerDto.setName("default");
        keyManagerDto.setKeyManager(keyManager);
        keyManagerDto.setIssuer("https://localhost");
        Map<String, KeyManagerDto> tenantKeyManagerDtoMap = new HashMap<>();
        tenantKeyManagerDtoMap.put("default", keyManagerDto);
        PowerMockito.when(KeyManagerHolder.getTenantKeyManagers("carbon.super")).thenReturn(tenantKeyManagerDtoMap);
        PowerMockito.when(CertificateManagerImpl.getInstance()).thenReturn(certificateManager);

        PowerMockito.when(APIUtil.isAPIManagementEnabled()).thenReturn(false);
        PowerMockito.when(APIUtil.replaceEmailDomainBack(Mockito.anyString())).thenReturn("admin");
        Mockito.when(APIUtil.replaceEmailDomain(Mockito.anyString())).thenReturn("admin");

        PrivilegedCarbonContext prcontext = Mockito.mock(PrivilegedCarbonContext.class);
        PowerMockito.when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(prcontext);

        PowerMockito.doNothing().when(prcontext).setUsername(Mockito.anyString());
        PowerMockito.doNothing().when(prcontext).setTenantDomain(Mockito.anyString(), Mockito.anyBoolean());

        artifactManager = Mockito.mock(GenericArtifactManager.class);
        registry = Mockito.mock(Registry.class);
        PowerMockito.when(APIUtil.getArtifactManager(any(Registry.class), Mockito.anyString()))
                .thenReturn(artifactManager);
        artifact = Mockito.mock(GenericArtifact.class);
        gatewayManager = Mockito.mock(APIGatewayManager.class);
        Mockito.when(APIGatewayManager.getInstance()).thenReturn(gatewayManager);

        TestUtils.mockRegistryAndUserRealm(-1234);
        TestUtils.mockAPICacheClearence();
        TestUtils.mockAPIMConfiguration();
        mockDocumentationCreation();

        config = Mockito.mock(APIManagerConfiguration.class);
        APIManagerConfigurationService apiManagerConfigurationService = new APIManagerConfigurationServiceImpl(config);
        ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(apiManagerConfigurationService);
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                .getAPIManagerConfiguration();
        GatewayArtifactSynchronizerProperties synchronizerProperties = new GatewayArtifactSynchronizerProperties();
        Mockito.when(config.getGatewayArtifactSynchronizerProperties()).thenReturn(synchronizerProperties);
        Mockito.when(config.getApiRecommendationEnvironment()).thenReturn(null);

        PowerMockito.when(APIUtil.replaceSystemProperty(Mockito.anyString())).thenAnswer((Answer<String>) invocation -> {
            Object[] args = invocation.getArguments();
            return (String) args[0];
        });
        TestUtils.initConfigurationContextService(true);
        superTenantDomain = "carbon.super";
    }



    @Test
    public void testGetSubscribersOfProvider() throws APIManagementException {
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, scopesDAO);
        Set<Subscriber> subscriberSet = new HashSet<Subscriber>();
        Mockito.when(apimgtDAO.getSubscribersOfProvider("testID")).thenReturn(subscriberSet);
        Assert.assertNotNull(apiProvider.getSubscribersOfProvider("testID"));
        Mockito.when(apimgtDAO.getSubscribersOfProvider("testID")).thenThrow(APIManagementException.class);
        try {
            apiProvider.getSubscribersOfProvider("testID");
            assertTrue(false);
        } catch (APIManagementException e) {
            Assert.assertEquals("Failed to get Subscribers for : testID", e.getMessage());
        }
    }

    @Test
    public void testGetAllAPIUsageByProvider() throws APIManagementException {
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, scopesDAO);
        UserApplicationAPIUsage[] userApplicationAPIUsages = new UserApplicationAPIUsage[]{};
        Mockito.when(apimgtDAO.getAllAPIUsageByProvider("testProvider")).
                thenReturn((userApplicationAPIUsages));
        assertNotNull(apiProvider.getAllAPIUsageByProvider(("testProvider")));
    }

    @Test
    public void testGetSubscribersOfAPI() throws APIManagementException {
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, scopesDAO);
        Set<Subscriber> subscriberSet = new HashSet<Subscriber>();
        APIIdentifier apiId = new APIIdentifier("admin", "API1", "1.0.1");
        Mockito.when(apimgtDAO.getSubscribersOfAPI(apiId)).thenReturn(subscriberSet);
        Assert.assertNotNull(apiProvider.getSubscribersOfAPI(apiId));
        Mockito.when(apimgtDAO.getSubscribersOfAPI(apiId)).thenThrow(APIManagementException.class);
        try {
            apiProvider.getSubscribersOfAPI(apiId);
            assertTrue(false);
        } catch (APIManagementException e) {
            Assert.assertEquals("Failed to get subscribers for API : API1", e.getMessage());
        }
    }

    @Test
    public void testGetAPISubscriptionCountByAPI() throws APIManagementException {
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, scopesDAO);
        Long count = Long.parseLong("10");
        APIIdentifier apiId = new APIIdentifier("admin", "API1", "1.0.1");
        Mockito.when(apimgtDAO.getAPISubscriptionCountByAPI(apiId)).thenReturn(count);
        assertEquals(count, (Long) apiProvider.getAPISubscriptionCountByAPI(apiId));
        Mockito.when(apimgtDAO.getAPISubscriptionCountByAPI(apiId)).thenThrow(APIManagementException.class);
        try {
            apiProvider.getAPISubscriptionCountByAPI(apiId);
            assertTrue(false);
        } catch (APIManagementException e) {
            Assert.assertEquals("Failed to get APISubscriptionCount for: API1", e.getMessage());
        }
    }

    @Test
    public void testUpdateAPIsInExternalAPIStores() throws APIManagementException {
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, scopesDAO);
        APIIdentifier apiId = new APIIdentifier("admin", "API1", "1.0.1");
        API api = new API(apiId);
        api.setContext("/test");
        api.setStatus(APIConstants.CREATED);
        api.setUuid(UUID.randomUUID().toString());
        APIPublisher publisher = Mockito.mock(APIPublisher.class);

        Set<APIStore> apiStores = new HashSet<APIStore>();
        APIStore apiStore = new APIStore();
        apiStore.setDisplayName("testName");
        apiStore.setName("testStoreName");
        apiStore.setPublisher(publisher);
        apiStore.setEndpoint("testEndpoint");
        apiStore.setType("testType");
        apiStores.add(apiStore);
        APIStore apiStore1 = new APIStore();
        apiStore1.setDisplayName("testName1");
        apiStore1.setName("testStoreName1");
        apiStore1.setEndpoint("testEndpoint");
        apiStore1.setType("testType");
        apiStore1.setPublisher(publisher);
        apiStores.add(apiStore1);

        PowerMockito.when(APIUtil.getExternalStores(-1)).thenReturn(apiStores);
        PowerMockito.when(APIUtil.isAPIsPublishToExternalAPIStores(-1)).thenReturn(true);
        Mockito.when(apimgtDAO.getExternalAPIStoresDetails(api.getUuid())).thenReturn(apiStores);
        Mockito.when(publisher.isAPIAvailable(api, apiStore)).thenReturn(true);
        Mockito.when(publisher.isAPIAvailable(api, apiStore1)).thenReturn(true);
        Mockito.when(APIUtil.getExternalAPIStore(apiStore.getName(), -1)).thenReturn(apiStore);
        Mockito.when(APIUtil.getExternalAPIStore(apiStore1.getName(), -1)).thenReturn(apiStore1);
        Assert.assertTrue(apiProvider.updateAPIsInExternalAPIStores(api, apiStores, true));
    }

    @Test
    public void testGetExternalAPIStores() throws APIManagementException {
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, scopesDAO);
        APIIdentifier apiId = new APIIdentifier("admin", "API1", "1.0.1");
        String uuid = UUID.randomUUID().toString();
        PowerMockito.when(APIUtil.isAPIsPublishToExternalAPIStores(-1)).thenReturn(true, false);
        Set<APIStore> apiStores = new HashSet<APIStore>();
        APIStore apiStore = new APIStore();
        apiStore.setDisplayName("testName");
        apiStore.setName("testStoreName");
        apiStores.add(apiStore);

        APIStore apiStore1 = new APIStore();
        apiStore1.setDisplayName("testName1");
        apiStore1.setName("testStoreName1");
        apiStores.add(apiStore1);
        Mockito.when(apimgtDAO.getExternalAPIStoresDetails(uuid)).thenReturn(apiStores);
        Assert.assertNotNull(apiProvider.getExternalAPIStores(uuid));
        // return null
        Assert.assertNull(apiProvider.getExternalAPIStores(uuid));
    }

    @Test
    public void testIsSynapseGateway() throws APIManagementException {
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, scopesDAO);
        ServiceReferenceHolder sh = PowerMockito.mock(ServiceReferenceHolder.class);
        APIManagerConfigurationService apiManagerConfigurationService =
                Mockito.mock(APIManagerConfigurationService.class);
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(sh);
        Mockito.when(sh.getAPIManagerConfigurationService()).thenReturn(apiManagerConfigurationService);
        Mockito.when(apiManagerConfigurationService.getAPIManagerConfiguration()).thenReturn(apiManagerConfiguration);
        Mockito.when(apiManagerConfiguration.getFirstProperty(Mockito.anyString())).thenReturn("synapse");
        assertTrue(apiProvider.isSynapseGateway());
    }

    @Test
    public void testGetPolicyNames() throws APIManagementException {
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, scopesDAO);
        String[] test = new String[]{};
        Mockito.when(apimgtDAO.getPolicyNames("testLevel", "testName")).thenReturn(test);
        assertNotNull(apiProvider.getPolicyNames("testName", "testLevel"));
    }

    @Test
    public void testIsGlobalPolicyKeyTemplateExists() throws APIManagementException {
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, scopesDAO);
        GlobalPolicy globalPolicy = Mockito.mock(GlobalPolicy.class);
        Mockito.when(apimgtDAO.isKeyTemplatesExist(globalPolicy)).thenReturn(true);
        assertTrue(apiProvider.isGlobalPolicyKeyTemplateExists(globalPolicy));
    }

    @Test
    public void testHasAttachments() throws APIManagementException {
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, scopesDAO);
        Mockito.when(apimgtDAO.hasApplicationPolicyAttachedToApplication("testId","carbon.super")).thenReturn(true);
        assertTrue(apiProvider.hasAttachments("testName", "testId", PolicyConstants.POLICY_LEVEL_APP, "carbon.super"));
    }
    @Test
    public void testHasAttachmentsForAPI() throws APIManagementException {
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, scopesDAO);
        Mockito.when(apimgtDAO.hasAPIPolicyAttached("testId","carbon.super")).thenReturn(true);
        assertTrue(apiProvider.hasAttachments("testName", "testId", PolicyConstants.POLICY_LEVEL_API, "carbon.super"));
    }
    @Test
    public void testHasAttachmentsForSubscription() throws APIManagementException {
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, scopesDAO);
        Mockito.when(apimgtDAO.hasSubscriptionPolicyAttached("testId","carbon.super")).thenReturn(true);
        assertTrue(apiProvider.hasAttachments("testName", "testId", PolicyConstants.POLICY_LEVEL_SUB, "carbon.super"));
    }

    @Test
    public void testGetBlockConditions() throws APIManagementException {
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, scopesDAO);
        List<BlockConditionsDTO> list = new ArrayList<BlockConditionsDTO>();
        Mockito.when(apimgtDAO.getBlockConditions(Mockito.anyString())).thenReturn(list);
        assertNotNull(apiProvider.getBlockConditions());
    }

    @Test
    public void testGetBlockCondition() throws APIManagementException {
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, scopesDAO);
        BlockConditionsDTO blockConditionsDTO = new BlockConditionsDTO();
        Mockito.when(apimgtDAO.getBlockCondition(Mockito.anyInt())).thenReturn(blockConditionsDTO);
        assertNotNull(apiProvider.getBlockCondition(Mockito.anyInt()));
    }

    @Test
    public void testGetBlockConditionByUUID() throws APIManagementException {
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, scopesDAO);
        BlockConditionsDTO blockConditionsDTO = new BlockConditionsDTO();
        Mockito.when(apimgtDAO.getBlockConditionByUUID("testUUID")).thenReturn(blockConditionsDTO);
        // Normal Path
        assertNotNull(apiProvider.getBlockConditionByUUID("testUUID"));
        Mockito.when(apimgtDAO.getBlockConditionByUUID("testUUID")).thenThrow(BlockConditionNotFoundException.class);
        // BlockConditionNotFound exception
        try {
            assertNull(apiProvider.getBlockConditionByUUID("testUUID"));
        } catch (APIManagementException e) {
            Assert.assertEquals(null, e.getMessage());
        }
    }

    @Test
    public void testUpdateBlockCondition() throws APIManagementException {
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, scopesDAO);
        Mockito.when(apimgtDAO.updateBlockConditionState(1, "testState")).thenReturn(false, true);
        BlockConditionsDTO blockConditionsDTO = new BlockConditionsDTO();
        blockConditionsDTO.setConditionType("testType");
        blockConditionsDTO.setConditionValue("USER");
        PowerMockito.mockStatic(MultitenantUtils.class);
        PowerMockito.when(MultitenantUtils.getTenantAwareUsername("User")).thenReturn("testValue");
        Mockito.when(apimgtDAO.getBlockCondition(1)).thenReturn(blockConditionsDTO);
        //updateState false
        assertFalse(apiProvider.updateBlockCondition(1, "testState"));
        //updateState true
        assertTrue(apiProvider.updateBlockCondition(1, "testState"));
    }

    @Test
    public void testUpdateBlockConditionByUUID() throws APIManagementException {
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, scopesDAO);
        Mockito.when(apimgtDAO.updateBlockConditionStateByUUID("testID", "testState")).
                thenReturn(false, true);
        BlockConditionsDTO blockConditionsDTO = new BlockConditionsDTO();
        blockConditionsDTO.setConditionType("testType");
        blockConditionsDTO.setConditionValue("USER");
        Mockito.when(apimgtDAO.getBlockConditionByUUID("testState")).thenReturn(blockConditionsDTO);
        //updateState false
        assertFalse(apiProvider.updateBlockConditionByUUID("testID", "testState"));
        //updateState true
        assertTrue(apiProvider.updateBlockConditionByUUID("testID", "testState"));
    }

    @Test
    public void testAddBlockCondition() throws APIManagementException {
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, scopesDAO);
        BlockConditionsDTO blockConditionsDTO = new BlockConditionsDTO();
        blockConditionsDTO.setUUID("12345");
        Mockito.when(apimgtDAO.addBlockConditions(Mockito.any(BlockConditionsDTO.class))).thenReturn(blockConditionsDTO);
        //condition type IP
        assertEquals("12345", apiProvider.addBlockCondition("IP", "testValue"));
        //condition type User
        assertEquals("12345", apiProvider.addBlockCondition("USER", "testValue"));
    }

    @Test
    public void testDeleteBlockCondition() throws APIManagementException {
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, scopesDAO);
        BlockConditionsDTO blockConditionsDTO = new BlockConditionsDTO();
        Mockito.when(apimgtDAO.getBlockCondition(1111)).thenReturn(blockConditionsDTO);
        Mockito.when(apimgtDAO.deleteBlockCondition(1111)).thenReturn(false, true);
        //deleteState false
        assertFalse(apiProvider.deleteBlockCondition(1111));
        //deleteState true
        assertTrue(apiProvider.deleteBlockCondition(1111));
    }

    @Test
    public void testDeleteBlockConditionByUUID() throws APIManagementException {
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, scopesDAO);
        BlockConditionsDTO blockConditionsDTO = new BlockConditionsDTO();
        blockConditionsDTO.setConditionType("testType");
        blockConditionsDTO.setConditionValue("USER");
        blockConditionsDTO.setConditionId(1111);
        Mockito.when(apimgtDAO.getBlockConditionByUUID("testId")).thenReturn(blockConditionsDTO);
        Mockito.when(apimgtDAO.deleteBlockCondition(1111)).thenReturn(false, true);
        PowerMockito.mockStatic(MultitenantUtils.class);
        PowerMockito.when(MultitenantUtils.getTenantAwareUsername("User")).thenReturn("testValue");
        //deleteState false
        assertFalse(apiProvider.deleteBlockConditionByUUID("testId"));
        //deleteState true
        assertTrue(apiProvider.deleteBlockConditionByUUID("testId"));
    }

    @Test
    public void testGetSubscriberClaims() throws APIManagementException, UserStoreException, XMLStreamException {
        String configuredClaims = "http://wso2.org/claim1,http://wso2.org/claim2";
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, scopesDAO);
        // Mock retrieving the tenant domain
        PowerMockito.mockStatic(MultitenantUtils.class);
        PowerMockito.mockStatic(APIUtil.class);
        Mockito.when(MultitenantUtils.getTenantDomain("admin")).thenReturn("carbon.super");
        ServiceReferenceHolder serviceReferenceHolder = TestUtils.getServiceReferenceHolder();
        RealmService realmService = Mockito.mock(RealmService.class);
        TenantManager tenantManager = Mockito.mock(TenantManager.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getRealmService()).thenReturn(realmService);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        PowerMockito.when(tenantManager.getTenantId(ArgumentMatchers.anyString())).thenReturn(-1234);

        UserStoreManager userStoreManager = Mockito.mock(UserStoreManager.class);
        UserRealm userRealm = Mockito.mock(UserRealm.class);
        PowerMockito.when(realmService.getTenantUserRealm(-1234)).thenReturn(userRealm);
        PowerMockito.when(userRealm.getUserStoreManager()).thenReturn(userStoreManager);
        PowerMockito.when(userStoreManager.isExistingUser("admin")).thenReturn(true);

        SortedMap<String, String> claimValues = new TreeMap<String, String>();
        claimValues.put("claim1", "http://wso2.org/claim1");
        claimValues.put("claim2", "http://wso2.org/claim2");
        claimValues.put("claim3", "http://wso2.org/claim3");
        PowerMockito.when(APIUtil.getClaims("admin", -1234, DEFAULT_DIALECT_URI))
                .thenReturn(claimValues);
        APIManagerConfiguration configuration = Mockito.mock(APIManagerConfiguration.class);
        APIManagerConfigurationService configurationService = Mockito.mock(APIManagerConfigurationService.class);
        PowerMockito.when(serviceReferenceHolder.getAPIManagerConfigurationService()).thenReturn(configurationService);
        PowerMockito.when(configurationService.getAPIManagerConfiguration()).thenReturn(configuration);
        Mockito.when(configuration.getFirstProperty(APIConstants.API_PUBLISHER_SUBSCRIBER_CLAIMS)).
                thenReturn(configuredClaims);
        Map subscriberClaims = apiProvider.getSubscriberClaims("admin");
        assertNotNull(subscriberClaims);
        assertEquals(configuredClaims.split(",").length, subscriberClaims.size());
    }



    @Test
    public void testGetExternalWorkflowReferenceId() throws APIManagementException {
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, scopesDAO);
        Mockito.when(apimgtDAO.getExternalWorkflowReferenceForSubscription(Mockito.anyInt())).thenReturn("testValue");
        assertNotNull("testValue", apiProvider.getExternalWorkflowReferenceId(Mockito.anyInt()));
    }

    @Test
    public void testGetAPIPolicy() throws APIManagementException {
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, scopesDAO);
        PowerMockito.when(APIUtil.getTenantId("testUser")).thenReturn(1111);
        APIPolicy apiPolicy = Mockito.mock(APIPolicy.class);
        Mockito.when(apimgtDAO.getAPIPolicy("testPolicy", 1111)).thenReturn(apiPolicy);
        assertNotNull(apiProvider.getAPIPolicy("testUser", "testPolicy"));
    }

    @Test
    public void testGetAPIPolicyByUUID() throws APIManagementException {
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, scopesDAO);
        APIPolicy apiPolicy = Mockito.mock(APIPolicy.class);
        Mockito.when(apimgtDAO.getAPIPolicyByUUID("1111")).thenReturn(apiPolicy, null);
        apiProvider.getAPIPolicyByUUID("1111");
        try {
            assertNotNull(apiProvider.getAPIPolicyByUUID("1111"));
        } catch (APIManagementException e) {
            assertEquals("Advanced Policy: 1111 was not found.", e.getMessage());
        }
    }

    @Test
    public void testGetApplicationPolicy() throws APIManagementException {
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, scopesDAO);
        PowerMockito.when(APIUtil.getTenantId("testUser")).thenReturn(1111);
        ApplicationPolicy applicationPolicy = Mockito.mock(ApplicationPolicy.class);
        Mockito.when(apimgtDAO.getApplicationPolicy("testPolicy", 1111)).
                thenReturn(applicationPolicy);
        assertNotNull(apiProvider.getApplicationPolicy("testUser", "testPolicy"));
    }

    @Test
    public void testGetApplicationPolicyByUUID() throws APIManagementException {
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, scopesDAO);
        ApplicationPolicy applicationPolicy = Mockito.mock(ApplicationPolicy.class);
        Mockito.when(apimgtDAO.getApplicationPolicyByUUID("1111")).thenReturn(applicationPolicy, null);
        apiProvider.getApplicationPolicyByUUID("1111");
        try {
            assertNotNull(apiProvider.getApplicationPolicyByUUID("1111"));
        } catch (APIManagementException e) {
            assertEquals("Application Policy: 1111 was not found.", e.getMessage());
        }
    }

    @Test
    public void testGetSubscriptionPolicy() throws APIManagementException {
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, scopesDAO);
        PowerMockito.when(APIUtil.getTenantId("testUser")).thenReturn(1111);
        SubscriptionPolicy subscriptionPolicy = Mockito.mock(SubscriptionPolicy.class);
        Mockito.when(apimgtDAO.getSubscriptionPolicy("testPolicy", 1111)).
                thenReturn(subscriptionPolicy);
        assertNotNull(apiProvider.getSubscriptionPolicy("testUser", "testPolicy"));
    }

    @Test
    public void testGetSubscriptionPolicyByUUID() throws APIManagementException {
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, scopesDAO);
        SubscriptionPolicy subscriptionPolicy = Mockito.mock(SubscriptionPolicy.class);
        Mockito.when(apimgtDAO.getSubscriptionPolicyByUUID("1111")).thenReturn(subscriptionPolicy, null);
        apiProvider.getSubscriptionPolicyByUUID("1111");
        try {
            assertNotNull(apiProvider.getSubscriptionPolicyByUUID("1111"));
        } catch (APIManagementException e) {
            assertEquals("Subscription Policy: 1111 was not found.", e.getMessage());
        }
    }

    @Test
    public void testGetGlobalPolicy() throws APIManagementException {
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, scopesDAO);
        GlobalPolicy globalPolicy = Mockito.mock(GlobalPolicy.class);
        Mockito.when(apimgtDAO.getGlobalPolicy("testName")).
                thenReturn(globalPolicy);
        assertNotNull(apiProvider.getGlobalPolicy("testName"));
    }

    @Test
    public void testGetGlobalPolicyByUUID() throws APIManagementException {
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, scopesDAO);
        GlobalPolicy globalPolicy = Mockito.mock(GlobalPolicy.class);
        Mockito.when(apimgtDAO.getGlobalPolicyByUUID("1111")).thenReturn(globalPolicy, null);
        apiProvider.getGlobalPolicyByUUID("1111");
        try {
            assertNotNull(apiProvider.getGlobalPolicyByUUID("1111"));
        } catch (APIManagementException e) {
            assertEquals("Global Policy: 1111 was not found.", e.getMessage());
        }
    }


    @Test
    public void testDeleteWorkflowTask() throws APIManagementException, WorkflowException {
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, scopesDAO);
        Mockito.when(apimgtDAO.getAPIID(apiUUID)).thenReturn(1111);
        WorkflowExecutorFactory wfe = PowerMockito.mock(WorkflowExecutorFactory.class);
        Mockito.when(WorkflowExecutorFactory.getInstance()).thenReturn(wfe);
        WorkflowExecutor apiStateChangeWFExecutor = Mockito.mock(WorkflowExecutor.class);
        Mockito.when(wfe.getWorkflowExecutor(WorkflowConstants.WF_TYPE_AM_API_STATE)).
                thenReturn(apiStateChangeWFExecutor);
        WorkflowDTO workflowDTO = Mockito.mock(WorkflowDTO.class);
        Mockito.when(apimgtDAO.retrieveWorkflowFromInternalReference(Integer.toString(1111),
                WorkflowConstants.WF_TYPE_AM_API_STATE)).thenReturn(workflowDTO);
        APIIdentifier identifier = new APIIdentifier("admin", "API1", "1.0.0", apiUUID);
        apiProvider.deleteWorkflowTask(identifier);
        Mockito.verify(apimgtDAO, Mockito.times(1)).getAPIID(apiUUID);
    }

    @Test
    public void testDeleteAPIProductWorkflowTask() throws APIManagementException, WorkflowException {

        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, scopesDAO);
        Mockito.when(apimgtDAO.getAPIID(apiUUID)).thenReturn(1111);
        WorkflowExecutorFactory wfe = PowerMockito.mock(WorkflowExecutorFactory.class);
        Mockito.when(WorkflowExecutorFactory.getInstance()).thenReturn(wfe);
        WorkflowExecutor productStateChangeWorkflowExecutor = Mockito.mock(WorkflowExecutor.class);
        Mockito.when(wfe.getWorkflowExecutor(WorkflowConstants.WF_TYPE_AM_API_PRODUCT_STATE))
                .thenReturn(productStateChangeWorkflowExecutor);
        WorkflowDTO workflowDTO = Mockito.mock(WorkflowDTO.class);
        Mockito.when(apimgtDAO.retrieveWorkflowFromInternalReference(Integer.toString(1111),
                WorkflowConstants.WF_TYPE_AM_API_PRODUCT_STATE)).thenReturn(workflowDTO);
        APIProductIdentifier identifier = new APIProductIdentifier("admin", "APIProduct", "1.0.0",
                apiUUID);
        apiProvider.deleteWorkflowTask(identifier);
        Mockito.verify(apimgtDAO, Mockito.times(1)).getAPIID(apiUUID);
    }


    @Test
    public void testGetAPIUsageByAPIId() throws APIManagementException, RegistryException, UserStoreException {
        String uuid = UUID.randomUUID().toString();
        SubscribedAPI subscribedAPI1 = new SubscribedAPI(new Subscriber("user1"),
                new APIIdentifier("admin", "API1", "1.0.0"));
        SubscribedAPI subscribedAPI2 = new SubscribedAPI(new Subscriber("user1"),
                new APIIdentifier("admin", "API2", "1.0.0"));

        UserApplicationAPIUsage apiResult1 = new UserApplicationAPIUsage();
        apiResult1.addApiSubscriptions(subscribedAPI1);
        apiResult1.addApiSubscriptions(subscribedAPI2);

        SubscribedAPI subscribedAPI3 = new SubscribedAPI(new Subscriber("user2"),
                new APIIdentifier("admin", "API1", "1.0.0"));
        SubscribedAPI subscribedAPI4 = new SubscribedAPI(new Subscriber("user2"),
                new APIIdentifier("admin", "API2", "1.0.0"));

        UserApplicationAPIUsage apiResult2 = new UserApplicationAPIUsage();
        apiResult2.addApiSubscriptions(subscribedAPI3);
        apiResult2.addApiSubscriptions(subscribedAPI4);

        UserApplicationAPIUsage[] apiResults = {apiResult1, apiResult2};

        Mockito.when(apimgtDAO.getAllAPIUsageByProviderAndApiId(uuid, "org1"))
                .thenReturn(apiResults);
        Mockito.when(apimgtDAO.getAPIIdentifierFromUUID(uuid)).thenReturn(new APIIdentifier("admin", "API1", "1.0.0"));

        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, scopesDAO);

        List<SubscribedAPI> subscribedAPIs = apiProvider.getAPIUsageByAPIId(uuid, "org1");

        Assert.assertEquals(2, subscribedAPIs.size());
        Assert.assertEquals("user1", subscribedAPIs.get(0).getSubscriber().getName());
        Assert.assertEquals("user2", subscribedAPIs.get(1).getSubscriber().getName());
    }


    private APIProduct createMockAPIProduct(String provider) {

        APIProductIdentifier productIdentifier = new APIProductIdentifier(provider, APIConstants.API_PRODUCT,
                APIConstants.API_PRODUCT_VERSION);
        APIProduct apiProduct = new APIProduct(productIdentifier);
        apiProduct.setContext("/test");
        apiProduct.setState(APIConstants.CREATED);
        apiProduct.setType(APIConstants.API_PRODUCT);
        apiProduct.setOrganization(APIConstants.SUPER_TENANT_DOMAIN);
        return apiProduct;
    }

    private void mockSequences(String seqLoc, String apiSeqLoc, APIIdentifier apiId) throws Exception {
        ServiceReferenceHolder sh = PowerMockito.mock(ServiceReferenceHolder.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(sh);
        RegistryService registryService = Mockito.mock(RegistryService.class);
        PowerMockito.when(sh.getRegistryService()).thenReturn(registryService);
        UserRegistry registry = Mockito.mock(UserRegistry.class);
        PowerMockito.when(registryService.getGovernanceSystemRegistry(ArgumentMatchers.anyInt())).thenReturn(registry);
        Mockito.when(registry.resourceExists(seqLoc)).thenReturn(true);
        Collection seqCollection = Mockito.mock(Collection.class);
        Mockito.when(registry.get(seqLoc)).thenReturn(
                seqCollection);
        String[] seqChildPaths = {"path1"};
        Mockito.when(seqCollection.getChildren()).thenReturn(seqChildPaths);
        Resource sequence = Mockito.mock(Resource.class);
        Mockito.when(registry.get(seqChildPaths[0])).thenReturn(sequence);
        InputStream responseStream = IOUtils.toInputStream("<sequence name=\"fault-seq\"></sequence>", "UTF-8");
        OMElement seqElment = buildOMElement(responseStream);
        PowerMockito.when(APIUtil.buildOMElement(responseStream)).thenReturn(seqElment);
        Mockito.when(sequence.getContentStream()).thenReturn(responseStream);
        String customSeqFileLocation = "/custom/fault";
        Mockito.when(APIUtil.getSequencePath(apiId, apiSeqLoc)).thenReturn(
                customSeqFileLocation);
        Mockito.when(registry.resourceExists(customSeqFileLocation)).thenReturn(true);
        Collection customSeqCollection = Mockito.mock(Collection.class);
        Mockito.when(registry.get(customSeqFileLocation)).thenReturn(
                customSeqCollection);
        String[] customSeqChildPaths = {"path2"};
        Mockito.when(customSeqCollection.getChildren()).thenReturn(customSeqChildPaths);
        Resource customSequence = Mockito.mock(Resource.class);
        Mockito.when(registry.get(customSeqChildPaths[0])).thenReturn(customSequence);
        InputStream responseStream1 = IOUtils.toInputStream("<sequence name=\"custom-fault-seq\"></sequence>", "UTF-8");
        OMElement seqElment1 = buildOMElement(responseStream1);
        PowerMockito.when(APIUtil.buildOMElement(responseStream1)).thenReturn(seqElment1);
        Mockito.when(customSequence.getContentStream()).thenReturn(responseStream1);
    }

    private GlobalPolicy getPolicyGlobalLevel(){
        GlobalPolicy policy = new GlobalPolicy("1");

        policy.setDescription("Description");
        String siddhiQuery =
                "FROM RequestStream\n"
                        + "SELECT 'global_1' AS rule, messageID, true AS isEligible, (cast(map:get(propertiesMap,’ip’),’string’)"
                        + " == 3232235778) as isLocallyThrottled,"
                        + " 'global_1_key' AS throttle_key\n"
                        + "INSERT INTO EligibilityStream;";
        policy.setSiddhiQuery(siddhiQuery);
        return policy;
    }

    private static OMElement buildOMElement(InputStream inputStream) throws APIManagementException {
        XMLStreamReader parser;
        StAXOMBuilder builder;
        try {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
            parser = factory.createXMLStreamReader(inputStream);
            builder = new StAXOMBuilder(parser);
        } catch (XMLStreamException e) {
            String msg = "Error in initializing the parser.";
            throw new APIManagementException(msg, e);
        }

        return builder.getDocumentElement();
    }

    private APIPolicy getPolicyAPILevelPerUser(){
        APIPolicy policy = new APIPolicy("custom1");
        policy.setUserLevel(PolicyConstants.PER_USER);
        policy.setDescription("Description");
        //policy.setPolicyLevel("api");
        policy.setTenantDomain("carbon.super");

        RequestCountLimit defaultLimit = new RequestCountLimit();
        defaultLimit.setTimeUnit("min");
        defaultLimit.setUnitTime(5);
        defaultLimit.setRequestCount(400);

        QuotaPolicy defaultQuotaPolicy = new QuotaPolicy();
        defaultQuotaPolicy.setLimit(defaultLimit);
        defaultQuotaPolicy.setType("RequestCount");
        policy.setDefaultQuotaPolicy(defaultQuotaPolicy);

        List<Pipeline> pipelines;
        Pipeline p;
        QuotaPolicy quotaPolicy;
        List<Condition> condition;
        RequestCountLimit countlimit;
        Condition cond;
        pipelines = new ArrayList<Pipeline>();

        ///////////pipeline item start//////
        p = new Pipeline();
        quotaPolicy = new QuotaPolicy();
        quotaPolicy.setType("RequestCount");
        countlimit = new RequestCountLimit();
        countlimit.setTimeUnit("min");
        countlimit.setUnitTime(5);
        countlimit.setRequestCount(100);
        quotaPolicy.setLimit(countlimit);

        condition =  new ArrayList<Condition>();
        HTTPVerbCondition verbCond = new HTTPVerbCondition();
        verbCond.setHttpVerb("POST");
        condition.add(verbCond);
        p.setQuotaPolicy(quotaPolicy);
        p.setConditions(condition);
        pipelines.add(p);
        ///////////pipeline item end//////

        policy.setPipelines(pipelines);
        return policy;
    }

    private ApplicationPolicy getPolicyAppLevel(){
        ApplicationPolicy policy = new ApplicationPolicy("gold");
        policy.setDescription("Description");
        policy.setTenantDomain("carbon.super");
        RequestCountLimit defaultLimit = new RequestCountLimit();
        defaultLimit.setTimeUnit("min");
        defaultLimit.setUnitTime(5);
        defaultLimit.setRequestCount(1000);

        QuotaPolicy defaultQuotaPolicy = new QuotaPolicy();
        defaultQuotaPolicy.setLimit(defaultLimit);
        defaultQuotaPolicy.setType("RequestCount");
        policy.setDefaultQuotaPolicy(defaultQuotaPolicy);
        return policy;
    }

    private SubscriptionPolicy getPolicySubscriptionLevelperUser(){
        SubscriptionPolicy policy = new SubscriptionPolicy("gold");
        policy.setDescription("Description");

        RequestCountLimit defaultLimit = new RequestCountLimit();
        defaultLimit.setTimeUnit("min");
        defaultLimit.setUnitTime(5);
        defaultLimit.setRequestCount(200);

        QuotaPolicy defaultQuotaPolicy = new QuotaPolicy();
        defaultQuotaPolicy.setLimit(defaultLimit);
        defaultQuotaPolicy.setType("RequestCount");
        policy.setDefaultQuotaPolicy(defaultQuotaPolicy);
        return policy;
    }

    private byte[] getTenantConfigContent() {
        String tenantConf = "{\"EnableMonetization\":false,\"IsUnlimitedTierPaid\":false,\"ExtensionHandlerPosition\":\"bottom\","
                + "\"RESTAPIScopes\":{\"Scope\":[{\"Name\":\"apim:api_publish\",\"Roles\":\"admin,Internal/publisher\"},"
                + "{\"Name\":\"apim:api_create\",\"Roles\":\"admin,Internal/creator\"},{\"Name\":\"apim:api_view\","
                + "\"Roles\":\"admin,Internal/publisher,Internal/creator\"},{\"Name\":\"apim:subscribe\",\"Roles\":"
                + "\"admin,Internal/subscriber\"},{\"Name\":\"apim:tier_view\",\"Roles\":\"admin,Internal/publisher,"
                + "Internal/creator\"},{\"Name\":\"apim:tier_manage\",\"Roles\":\"admin\"},{\"Name\":\"apim:bl_view\","
                + "\"Roles\":\"admin\"},{\"Name\":\"apim:bl_manage\",\"Roles\":\"admin\"},{\"Name\":"
                + "\"apim:subscription_view\",\"Roles\":\"admin,Internal/creator\"},{\"Name\":"
                + "\"apim:subscription_block\",\"Roles\":\"admin,Internal/creator\"},{\"Name\":"
                + "\"apim:mediation_policy_view\",\"Roles\":\"admin\"},{\"Name\":\"apim:mediation_policy_create\","
                + "\"Roles\":\"admin\"},{\"Name\":\"apim:api_workflow_approve\",\"Roles\":\"admin\"},{\"Name\":"
                + "\"apim:apim:api_workflow_view\",\"Roles\":\"admin\"}]},\"NotificationsEnabled\":"
                + "\"true\",\"Notifications\":[{\"Type\":\"new_api_version\",\"Notifiers\":[{\"Class\":"
                + "\"org.wso2.carbon.apimgt.impl.notification.NewAPIVersionEmailNotifier\",\"ClaimsRetrieverImplClass\":"
                + "\"org.wso2.carbon.apimgt.impl.token.DefaultClaimsRetriever\",\"Title\":\"Version $2 of $1 Released\","
                + "\"Template\":\" <html> <body> <h3 style=\\\"color:Black;\\\">We’re happy to announce the arrival of"
                + " the next major version $2 of $1 API which is now available in Our API Store.</h3><a href=\\\"https:"
                + "//localhost:9443/store\\\">Click here to Visit WSO2 API Store</a></body></html>\"}]}],"
                + "\"DefaultRoles\":{\"PublisherRole\":{\"CreateOnTenantLoad\":true,\"RoleName\":"
                + "\"Internal/publisher\"},\"CreatorRole\":{\"CreateOnTenantLoad\":true,\"RoleName\":"
                + "\"Internal/creator\"},\"SubscriberRole\":{\"CreateOnTenantLoad\":true}}}";
        return tenantConf.getBytes();
    }

    private CORSConfiguration getCORSConfiguration() {
        List<String> acceessControlAllowHeaders = new ArrayList<String>();
        acceessControlAllowHeaders.add("Authorization");
        acceessControlAllowHeaders.add("Content-Type");

        List<String> acceessControlAllowOrigins = new ArrayList<String>();
        acceessControlAllowOrigins.add("*");

        List<String> acceessControlAllowMethods = new ArrayList<String>();
        acceessControlAllowMethods.add("GET");
        acceessControlAllowMethods.add("POST");

        return new CORSConfiguration(true, acceessControlAllowOrigins, true,
                acceessControlAllowHeaders, acceessControlAllowMethods);
    }

    private List<Documentation> getDocumentationList() {
        Documentation doc1 = new Documentation(DocumentationType.HOWTO, "How To");
        doc1.setVisibility(DocumentVisibility.API_LEVEL);
        doc1.setSourceType(DocumentSourceType.INLINE);
        doc1.setId("678ghk");
        doc1.setFilePath(APIConstants.REGISTRY_RESOURCE_URL_PREFIX + "files/provider/fileName");

        Documentation doc2 = new Documentation(DocumentationType.SUPPORT_FORUM, "Support Docs");
        doc2.setVisibility(DocumentVisibility.API_LEVEL);
        doc2.setSourceType(DocumentSourceType.FILE);
        doc2.setId("678ghk");
        doc2.setFilePath("/registry/resource/_system/governance/apimgt/applicationdata/provider/"
                + "files/provider/fileName");

        List<Documentation> docList = new ArrayList<Documentation>();
        docList.add(doc1);
        docList.add(doc2);

        return docList;
    }

    private void mockDocumentationCreation() throws Exception {
        GenericArtifactManager artifactManager = Mockito.mock(GenericArtifactManager.class);
        PowerMockito.whenNew(GenericArtifactManager.class).withAnyArguments().thenReturn(artifactManager);
        GenericArtifact artifact = Mockito.mock(GenericArtifact.class);
        Mockito.when(artifactManager.newGovernanceArtifact(any(QName.class))).thenReturn(artifact);
    }

    private static void setFinalStatic(Field field, Object newValue) throws Exception {
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(null, newValue);
    }

    /**
     * This method tests the retrieval of API Security Audit Properties from the Global Config
     * (i.e. deployment.toml)
     * @throws APIManagementException
     */
    @Test
    public void testGetSecurityAuditAttributesFromGlobalConfig() throws APIManagementException {
        // Instantiate required variables
        String username = "admin";
        String apiToken = "2780f0ca-3423-435f-0e9f-a634e0do65915";
        String collectionId = "8750f8ca-34f9-4baf-8b9f-c6854ed06534";
        String global = "true";
        // Mock retrieving the tenant domain
        PowerMockito.mockStatic(MultitenantUtils.class);
        Mockito.when(MultitenantUtils.getTenantDomain(username)).thenReturn("carbon.super");
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO,scopesDAO);

        // Create a mock APIManagerConfiguration Object for retrieving properties from the deployment.toml
        APIManagerConfiguration apiManagerConfiguration = PowerMockito.mock(APIManagerConfiguration.class);
        ServiceReferenceHolder serviceReferenceHolder = PowerMockito.mock(ServiceReferenceHolder.class);
        APIManagerConfigurationService apiManagerConfigurationService = PowerMockito.mock(APIManagerConfigurationService.class);

        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfigurationService()).thenReturn(apiManagerConfigurationService);
        Mockito.when(apiManagerConfigurationService.getAPIManagerConfiguration()).thenReturn(apiManagerConfiguration);
        // Mock the properties read from the deployment.toml
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.API_SECURITY_AUDIT_API_TOKEN)).thenReturn(apiToken);
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.API_SECURITY_AUDIT_CID)).thenReturn(collectionId);
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.API_SECURITY_AUDIT_GLOBAL)).thenReturn(global);

        // Pass the mocked properties into the getSecurityAuditAttributesFromConfig method
        JSONObject jsonObject = apiProvider.getSecurityAuditAttributesFromConfig(username);
        // Compare the properties from the returned object and the mocked ones.
        Assert.assertEquals(jsonObject.get(APIConstants.SECURITY_AUDIT_API_TOKEN), apiToken);
        Assert.assertEquals(jsonObject.get(APIConstants.SECURITY_AUDIT_COLLECTION_ID), collectionId);
    }

    /**
     * This method tests the retrieval of API Security Audit Properties from the Tenant Config
     * (i.e. tenant-conf.json)
     * @throws RegistryException
     * @throws APIManagementException
     */
    @Test
    public void testGetSecurityAuditAttributesFromTenantConfig()
            throws RegistryException, APIManagementException {
        // Instantiating required variables
        final int tenantId = -1234;

        String apiToken = "1234f0ca-9879-112f-0e8f-a098e0do12456";
        String collectionId = "467f8ca-40f8-4baf-8b0f-c6854ed04653";
        boolean overrideGlobal = true;

        // Sample JSONObject for mocking
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("apiToken", apiToken);
        jsonObject.put("collectionId", collectionId);
        jsonObject.put("overrideGlobal", overrideGlobal);

        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO,scopesDAO);
        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        RegistryService registryService = Mockito.mock(RegistryService.class);
        UserRegistry userRegistry = Mockito.mock(UserRegistry.class);
        Resource resource = Mockito.mock(Resource.class);
        Mockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getRegistryService()).thenReturn(registryService);
        Mockito.when(registryService.getConfigSystemRegistry(tenantId)).thenReturn(userRegistry);
        PowerMockito.mockStatic(APIUtil.class);
        Mockito.when(APIUtil.getSecurityAuditAttributesFromRegistry(superTenantDomain)).thenReturn(jsonObject);

        // Pass the mock values to the method call
        JSONObject jsonObject1 = apiProvider.getSecurityAuditAttributesFromConfig("admin");

        // Compare the API Token and Collection ID returned from the method call
        Assert.assertEquals(jsonObject1.get(APIConstants.SECURITY_AUDIT_API_TOKEN), apiToken);
        Assert.assertEquals(jsonObject1.get(APIConstants.SECURITY_AUDIT_COLLECTION_ID), collectionId);
    }

    /**
     * This method tests the retrieval of API Security Audit Properties when both the global config
     * and tenant configs return null
     * @throws APIManagementException
     */
    @Test
    public void testGetSecurityAuditAttributesFromNullConfig() throws APIManagementException {
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO,scopesDAO);
        // Make method call with null values
        JSONObject jsonObject = apiProvider.getSecurityAuditAttributesFromConfig("admin");
        // Check if the JSONObject returned is null
        assertNull(jsonObject);
    }

    /**
     * This method tests the retrieval of API Security Audit Properties when both the tenant and global
     * configs contain values
     * @throws APIManagementException
     * @throws RegistryException
     */
    @Test
    public void testGetSecurityAuditAttributesFromAllConfigs() throws APIManagementException, RegistryException {
        // Mock values from global config
        String username = "admin";
        String apiToken = "2780f0ca-3423-435f-0e9f-a634e0do65915";
        String collectionId = "8750f8ca-34f9-4baf-8b9f-c6854ed06534";
        String global = "true";
        // Mock retrieving the tenant domain
        PowerMockito.mockStatic(MultitenantUtils.class);
        Mockito.when(MultitenantUtils.getTenantDomain(username)).thenReturn("carbon.super");
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO,scopesDAO);

        // Create a mock APIManagerConfiguration Object for retrieving properties from the deployment.toml
        APIManagerConfiguration apiManagerConfiguration = PowerMockito.mock(APIManagerConfiguration.class);
        ServiceReferenceHolder serviceReferenceHolder = PowerMockito.mock(ServiceReferenceHolder.class);
        APIManagerConfigurationService apiManagerConfigurationService = PowerMockito.mock(APIManagerConfigurationService.class);

        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfigurationService()).thenReturn(apiManagerConfigurationService);
        Mockito.when(apiManagerConfigurationService.getAPIManagerConfiguration()).thenReturn(apiManagerConfiguration);
        // Mock the properties read from the deployment.toml
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.API_SECURITY_AUDIT_API_TOKEN)).thenReturn(apiToken);
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.API_SECURITY_AUDIT_CID)).thenReturn(collectionId);
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.API_SECURITY_AUDIT_GLOBAL)).thenReturn(global);

        // Mock values from tenant config
        final int tenantId = -1234;

        String apiToken1 = "1234f0ca-9879-112f-0e8f-a098e0do12456";
        String collectionId1 = "467f8ca-40f8-4baf-8b0f-c6854ed04653";
        boolean overrideGlobal = true;

        // Sample JSONObject for mocking
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("apiToken", apiToken1);
        jsonObject.put("collectionId", collectionId1);
        jsonObject.put("overrideGlobal", overrideGlobal);

        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        RegistryService registryService = Mockito.mock(RegistryService.class);
        UserRegistry userRegistry = Mockito.mock(UserRegistry.class);
        Resource resource = Mockito.mock(Resource.class);
        Mockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getRegistryService()).thenReturn(registryService);
        PowerMockito.mockStatic(APIUtil.class);
        Mockito.when(APIUtil.getSecurityAuditAttributesFromRegistry(superTenantDomain)).thenReturn(jsonObject);

        // Test the object to be returned when overrideGlobal is true
        JSONObject jsonObject1 = apiProvider.getSecurityAuditAttributesFromConfig("admin");
        Assert.assertEquals(jsonObject1.get(APIConstants.SECURITY_AUDIT_API_TOKEN), apiToken1);
        Assert.assertEquals(jsonObject1.get(APIConstants.SECURITY_AUDIT_COLLECTION_ID), collectionId1);

        // Test the object to be returned when overrideGlobal is false
        jsonObject.put("overrideGlobal", false);
        JSONObject jsonObject2 = apiProvider.getSecurityAuditAttributesFromConfig("admin");
        Assert.assertEquals(jsonObject2.get(APIConstants.SECURITY_AUDIT_API_TOKEN), apiToken);
        Assert.assertEquals(jsonObject2.get(APIConstants.SECURITY_AUDIT_COLLECTION_ID), collectionId);
    }

    /**
     * This method tests adding a new API Revision
     *
     * @throws APIManagementException
     */
    @Test
    public void testAddAPIRevision() throws APIManagementException, APIPersistenceException, APIImportExportException,
            ArtifactSynchronizerException {
        ImportExportAPI importExportAPI = Mockito.mock(ImportExportAPI.class);
        ArtifactSaver artifactSaver = Mockito.mock(ArtifactSaver.class);
        Mockito.doNothing().when(artifactSaver)
                .saveArtifact(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                        Mockito.anyString(), Mockito.any(File.class));
        Mockito.when(GatewayArtifactsMgtDAO.getInstance()).thenReturn(gatewayArtifactsMgtDAO);
        APIProviderImplWrapper apiProvider =
                new APIProviderImplWrapper(apiPersistenceInstance, apimgtDAO, importExportAPI, gatewayArtifactsMgtDAO,
                        artifactSaver);
        APIIdentifier apiId = new APIIdentifier("admin", "PizzaShackAPI", "1.0.0",
                "63e1e37e-a5b8-4be6-86a5-d6ae0749f131");

        API api = new API(apiId);
        api.setContext("/test");
        api.setStatus(APIConstants.CREATED);
        String apiPath = "/apimgt/applicationdata/provider/admin/PizzaShackAPI/1.0.0/api";

        APIRevision apiRevision = new APIRevision();
        apiRevision.setApiUUID("63e1e37e-a5b8-4be6-86a5-d6ae0749f131");
        apiRevision.setDescription("test description revision 1");
        Mockito.when(apimgtDAO.getRevisionCountByAPI(Mockito.anyString())).thenReturn(0);
        Mockito.when(apimgtDAO.getMostRecentRevisionId(Mockito.anyString())).thenReturn(0);
        Mockito.when(APIUtil.getAPIIdentifierFromUUID(Mockito.anyString())).thenReturn(apiId);
        Mockito.when(APIUtil.getAPIPath(apiId)).thenReturn(apiPath);
        PowerMockito.when(apiPersistenceInstance.addAPIRevision(any(Organization.class), Mockito.anyString(), Mockito.anyInt()))
                .thenReturn("b55e0fc3-9829-4432-b99e-02056dc91838");
        Mockito.when(APIUtil.getTenantConfig(Mockito.anyString())).thenReturn(new JSONObject());
        try {
            apiProvider.addAPIRevision(apiRevision, superTenantDomain);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    /**
     * This method tests adding a new API Revision and then retrieving API Revision by Revision UUID
     *
     * @throws APIManagementException
     */
    @Test
    public void testGetAPIRevision() throws APIManagementException, APIPersistenceException {
        ImportExportAPI importExportAPI = Mockito.mock(ImportExportAPI.class);
        ArtifactSaver artifactSaver = Mockito.mock(ArtifactSaver.class);
        APIProviderImplWrapper apiProvider =
                new APIProviderImplWrapper(apiPersistenceInstance, apimgtDAO, importExportAPI, gatewayArtifactsMgtDAO,
                        artifactSaver);
        APIIdentifier apiId = new APIIdentifier("admin", "PizzaShackAPI", "1.0.0",
                "63e1e37e-a5b8-4be6-86a5-d6ae0749f131");
        API api = new API(apiId);
        api.setContext("/test");
        api.setStatus(APIConstants.CREATED);
        String apiPath = "/apimgt/applicationdata/provider/admin/PizzaShackAPI/1.0.0/api";

        APIRevision apiRevision = new APIRevision();
        apiRevision.setApiUUID("63e1e37e-a5b8-4be6-86a5-d6ae0749f131");
        apiRevision.setDescription("test description revision 1");
        Mockito.when(apimgtDAO.getRevisionCountByAPI(Mockito.anyString())).thenReturn(0);
        Mockito.when(apimgtDAO.getMostRecentRevisionId(Mockito.anyString())).thenReturn(0);
        Mockito.when(APIUtil.getAPIIdentifierFromUUID(Mockito.anyString())).thenReturn(apiId);
        Mockito.when(APIUtil.getAPIPath(apiId)).thenReturn(apiPath);

        PowerMockito.when(apiPersistenceInstance.addAPIRevision(any(Organization.class), Mockito.anyString(), Mockito.anyInt()))
                .thenReturn("b55e0fc3-9829-4432-b99e-02056dc91838");
        Mockito.when(APIUtil.getTenantConfig(Mockito.anyString())).thenReturn(new JSONObject());
        try {
            apiProvider.addAPIRevision(apiRevision, superTenantDomain);
            apiProvider.getAPIRevision("b55e0fc3-9829-4432-b99e-02056dc91838");
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    /**
     * This method tests adding a new API Revision and then retrieving API Revisions by API UUID
     *
     * @throws APIManagementException
     */
    @Test
    public void testGetAPIRevisions()
            throws APIManagementException, APIPersistenceException, ArtifactSynchronizerException {
        ImportExportAPI importExportAPI = Mockito.mock(ImportExportAPI.class);
        ArtifactSaver artifactSaver = Mockito.mock(ArtifactSaver.class);
        APIProviderImplWrapper apiProvider =
                new APIProviderImplWrapper(apiPersistenceInstance, apimgtDAO, importExportAPI, gatewayArtifactsMgtDAO,
                        artifactSaver);
        APIIdentifier apiId = new APIIdentifier("admin", "PizzaShackAPI", "1.0.0",
                "63e1e37e-a5b8-4be6-86a5-d6ae0749f131");
        API api = new API(apiId);
        api.setContext("/test");
        api.setStatus(APIConstants.CREATED);
        String apiPath = "/apimgt/applicationdata/provider/admin/PizzaShackAPI/1.0.0/api";

        APIRevision apiRevision = new APIRevision();
        apiRevision.setApiUUID("63e1e37e-a5b8-4be6-86a5-d6ae0749f131");
        apiRevision.setDescription("test description revision 1");
        Mockito.when(apimgtDAO.getRevisionCountByAPI(Mockito.anyString())).thenReturn(0);
        Mockito.when(apimgtDAO.getMostRecentRevisionId(Mockito.anyString())).thenReturn(0);
        Mockito.when(APIUtil.getAPIIdentifierFromUUID(Mockito.anyString())).thenReturn(apiId);
        Mockito.when(APIUtil.getAPIPath(apiId)).thenReturn(apiPath);
        Mockito.when(APIUtil.getTenantConfig(Mockito.anyString())).thenReturn(new JSONObject());
        PowerMockito.when(apiPersistenceInstance.addAPIRevision(any(Organization.class), Mockito.anyString(), Mockito.anyInt()))
                .thenReturn("b55e0fc3-9829-4432-b99e-02056dc91838");
        try {
            apiProvider.addAPIRevision(apiRevision, superTenantDomain);
            apiProvider.getAPIRevisions("63e1e37e-a5b8-4be6-86a5-d6ae0749f131");
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }



    /**
     * This method tests restoring an API Revision to Working Copy
     *
     * @throws APIManagementException
     */
    @Test
    public void testRestoreAPIRevision() throws APIManagementException, APIPersistenceException {
        ImportExportAPI importExportAPI = Mockito.mock(ImportExportAPI.class);
        ArtifactSaver artifactSaver = Mockito.mock(ArtifactSaver.class);
        APIProviderImplWrapper apiProvider =
                new APIProviderImplWrapper(apiPersistenceInstance, apimgtDAO, importExportAPI, gatewayArtifactsMgtDAO,
                        artifactSaver);
        APIIdentifier apiId = new APIIdentifier("admin", "PizzaShackAPI", "1.0.0",
                "63e1e37e-a5b8-4be6-86a5-d6ae0749f131");
        API api = new API(apiId);
        api.setContext("/test");
        api.setStatus(APIConstants.CREATED);
        String apiPath = "/apimgt/applicationdata/provider/admin/PizzaShackAPI/1.0.0/api";

        APIRevision apiRevision = new APIRevision();
        apiRevision.setApiUUID("63e1e37e-a5b8-4be6-86a5-d6ae0749f131");
        apiRevision.setDescription("test description revision 1");
        Mockito.when(apimgtDAO.getRevisionCountByAPI(Mockito.anyString())).thenReturn(0);
        Mockito.when(apimgtDAO.getMostRecentRevisionId(Mockito.anyString())).thenReturn(0);
        Mockito.when(APIUtil.getAPIIdentifierFromUUID(Mockito.anyString())).thenReturn(apiId);
        Mockito.when(APIUtil.getAPIPath(apiId)).thenReturn(apiPath);
        Mockito.when(APIUtil.getTenantConfig(Mockito.anyString())).thenReturn(new JSONObject());
        PowerMockito.when(apiPersistenceInstance.addAPIRevision(any(Organization.class), Mockito.anyString(), Mockito.anyInt()))
                .thenReturn("b55e0fc3-9829-4432-b99e-02056dc91838");
        try {
            apiProvider.addAPIRevision(apiRevision, superTenantDomain);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        Mockito.when(apimgtDAO.getRevisionByRevisionUUID(Mockito.anyString())).thenReturn(apiRevision);
        PowerMockito.doNothing().when(apiPersistenceInstance).restoreAPIRevision(any(Organization.class),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyInt());
        try {
            apiProvider.restoreAPIRevision("63e1e37e-a5b8-4be6-86a5-d6ae0749f131",
                    "b55e0fc3-9829-4432-b99e-02056dc91838", superTenantDomain);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    /**
     * This method tests deleting an API Revision
     *
     * @throws APIManagementException
     */
    @Test
    public void testDeleteAPIRevision() throws APIManagementException, APIPersistenceException {
        ImportExportAPI importExportAPI = Mockito.mock(ImportExportAPI.class);
        ArtifactSaver artifactSaver = Mockito.mock(ArtifactSaver.class);
        APIProviderImplWrapper apiProvider =
                new APIProviderImplWrapper(apiPersistenceInstance, apimgtDAO, importExportAPI, gatewayArtifactsMgtDAO,
                        artifactSaver);
        APIIdentifier apiId = new APIIdentifier("admin", "PizzaShackAPI", "1.0.0",
                "63e1e37e-a5b8-4be6-86a5-d6ae0749f131");
        API api = new API(apiId);
        api.setContext("/test");
        api.setStatus(APIConstants.CREATED);
        String apiPath = "/apimgt/applicationdata/provider/admin/PizzaShackAPI/1.0.0/api";

        APIRevision apiRevision = new APIRevision();
        apiRevision.setApiUUID("63e1e37e-a5b8-4be6-86a5-d6ae0749f131");
        apiRevision.setDescription("test description revision 1");
        Mockito.when(apimgtDAO.getRevisionCountByAPI(Mockito.anyString())).thenReturn(0);
        Mockito.when(apimgtDAO.getMostRecentRevisionId(Mockito.anyString())).thenReturn(0);
        Mockito.when(APIUtil.getAPIIdentifierFromUUID(Mockito.anyString())).thenReturn(apiId);
        Mockito.when(APIUtil.getAPIPath(apiId)).thenReturn(apiPath);
        Mockito.when(APIUtil.getTenantConfig(Mockito.anyString())).thenReturn(new JSONObject());
        PowerMockito.when(apiPersistenceInstance.addAPIRevision(any(Organization.class), Mockito.anyString(), Mockito.anyInt()))
                .thenReturn("b55e0fc3-9829-4432-b99e-02056dc91838");
        try {
            apiProvider.addAPIRevision(apiRevision, superTenantDomain);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        Mockito.when(apimgtDAO.getRevisionByRevisionUUID(Mockito.anyString())).thenReturn(apiRevision);
        PowerMockito.doNothing().when(apiPersistenceInstance)
                .deleteAPIRevision(any(Organization.class), Mockito.anyString(), Mockito.anyString(), Mockito.anyInt());
        try {
            apiProvider.deleteAPIRevision("63e1e37e-a5b8-4be6-86a5-d6ae0749f131",
                    "b55e0fc3-9829-4432-b99e-02056dc91838", superTenantDomain);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testChangeLifeCycleStatusOfAPIProduct() throws APIManagementException, FaultGatewaysException {

        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, scopesDAO);
        String provider = "admin";
        PrivilegedCarbonContext carbonContext = Mockito.mock(PrivilegedCarbonContext.class);
        PowerMockito.when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(carbonContext);

        PowerMockito.doNothing().when(carbonContext).setUsername(Mockito.anyString());
        PowerMockito.doNothing().when(carbonContext).setTenantDomain(Mockito.anyString(), Mockito.anyBoolean());

        APIProduct product = createMockAPIProduct(provider);
        Mockito.when(apimgtDAO.getAPIProductId(product.getId())).thenReturn(1);

        WorkflowDTO workflowDTO = Mockito.mock(WorkflowDTO.class);
        Mockito.when(workflowDTO.getStatus()).thenReturn(WorkflowStatus.CREATED);
        Mockito.when(apimgtDAO.retrieveWorkflowFromInternalReference(Integer.toString(1),
                WorkflowConstants.WF_TYPE_AM_API_PRODUCT_STATE)).thenReturn(workflowDTO);

        APIStateChangeResponse response = apiProvider.changeLifeCycleStatus("carbon.super",
                new ApiTypeWrapper(product), "Publish", null);
        Assert.assertNotNull(response);
    }

    @Test
    public void testApiPolicyListingWhenMediationPoliciesExists() throws APIManagementException {

        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, scopesDAO);
        APIIdentifier apiId = new APIIdentifier("admin", "PizzaShackAPI", "1.0.0",
                "63e1e37e-a5b8-4be6-86a5-d6ae0749f131");
        API api = new API(apiId);
        api.setContext("/test");
        api.setStatus(APIConstants.CREATED);

        Set<URITemplate> uriTemplates = new HashSet<URITemplate>();

        URITemplate uriTemplate1 = new URITemplate();
        uriTemplate1.setHTTPVerb("POST");
        uriTemplate1.setAuthType("Application");
        uriTemplate1.setUriTemplate("/add");
        uriTemplate1.setThrottlingTier("Gold");
        uriTemplates.add(uriTemplate1);

        URITemplate uriTemplate2 = new URITemplate();
        uriTemplate2.setHTTPVerb("PUT");
        uriTemplate2.setAuthType("Application");
        uriTemplate2.setUriTemplate("/update");
        uriTemplate2.setThrottlingTier("Gold");
        uriTemplates.add(uriTemplate2);

        api.setUriTemplates(uriTemplates);
        api.setInSequence("test-sequence");
        api.setOutSequence("test-sequence");
        api.setFaultSequence("test-sequence");

        Mockito.when(apiProvider.getAPISpecificOperationPolicyByPolicyName(api.getInSequence(),
                APIConstants.DEFAULT_POLICY_VERSION, api.getUuid(), null, superTenantDomain, false)).thenReturn(null);
        Mockito.when(apiProvider.getAPISpecificOperationPolicyByPolicyName(api.getOutSequence(),
                APIConstants.DEFAULT_POLICY_VERSION, api.getUuid(), null, superTenantDomain, false)).thenReturn(null);
        Mockito.when(apiProvider.getAPISpecificOperationPolicyByPolicyName(api.getFaultSequence(),
                APIConstants.DEFAULT_POLICY_VERSION, api.getUuid(), null, superTenantDomain, false)).thenReturn(null);

        PowerMockito.when(APIUtil.isSequenceDefined(Mockito.anyString())).thenReturn(true);
        apiProvider.loadMediationPoliciesFromMigratedAPIToAPI(api, superTenantDomain);

        Assert.assertNotNull(api.getApiPolicies());
        Assert.assertEquals(api.getApiPolicies().size(), 3);
        Assert.assertEquals(api.getApiPolicies().get(0).getPolicyName(), "test-sequence");
    }

    @Test
    public void testApiPolicyListingWhenMediationPoliciesExistsAndPolicyAlreadyMigrated() throws APIManagementException {

        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, scopesDAO);
        APIIdentifier apiId = new APIIdentifier("admin", "PizzaShackAPI", "1.0.0",
                "63e1e37e-a5b8-4be6-86a5-d6ae0749f131");
        API api = new API(apiId);
        api.setContext("/test");
        api.setStatus(APIConstants.CREATED);

        Set<URITemplate> uriTemplates = new HashSet<URITemplate>();

        URITemplate uriTemplate1 = new URITemplate();
        uriTemplate1.setHTTPVerb("POST");
        uriTemplate1.setAuthType("Application");
        uriTemplate1.setUriTemplate("/add");
        uriTemplate1.setThrottlingTier("Gold");
        uriTemplates.add(uriTemplate1);

        URITemplate uriTemplate2 = new URITemplate();
        uriTemplate2.setHTTPVerb("PUT");
        uriTemplate2.setAuthType("Application");
        uriTemplate2.setUriTemplate("/update");
        uriTemplate2.setThrottlingTier("Gold");
        uriTemplates.add(uriTemplate2);

        api.setUriTemplates(uriTemplates);
        api.setInSequence("in-sequence");
        api.setOutSequence("out-sequence");
        api.setFaultSequence("fault-sequence");

        String policyId = "11111";
        OperationPolicyData policyData = new OperationPolicyData();
        policyData.setPolicyId(policyId);

        Mockito.when(apiProvider.getAPISpecificOperationPolicyByPolicyName(api.getInSequence(),
                APIConstants.DEFAULT_POLICY_VERSION, api.getUuid(), null, superTenantDomain, false)).thenReturn(policyData);
        Mockito.when(apiProvider.getAPISpecificOperationPolicyByPolicyName(api.getOutSequence(),
                APIConstants.DEFAULT_POLICY_VERSION, api.getUuid(), null, superTenantDomain, false)).thenReturn(null);
        Mockito.when(apiProvider.getAPISpecificOperationPolicyByPolicyName(api.getFaultSequence(),
                APIConstants.DEFAULT_POLICY_VERSION, api.getUuid(), null, superTenantDomain, false)).thenReturn(policyData);

        PowerMockito.when(APIUtil.isSequenceDefined(Mockito.anyString())).thenReturn(true);
        apiProvider.loadMediationPoliciesFromMigratedAPIToAPI(api, superTenantDomain);


        for (OperationPolicy policy : api.getApiPolicies()) {
            if (APIConstants.OPERATION_SEQUENCE_TYPE_REQUEST.equals(policy.getDirection())) {
                Assert.assertEquals(policy.getPolicyId(), policyId);
            }
            if (APIConstants.OPERATION_SEQUENCE_TYPE_RESPONSE.equals(policy.getDirection())) {
                Assert.assertNull(policy.getPolicyId());
            }
            if (APIConstants.OPERATION_SEQUENCE_TYPE_FAULT.equals(policy.getDirection())) {
                Assert.assertEquals(policy.getPolicyId(), policyId);
            }
        }
    }

    @Test
    public void testMigrationOfMediationPoliciesToAPIPolicies()
            throws APIManagementException, MediationPolicyPersistenceException {

        String apiuuid = "63e1e37e-a5b8-4be6-86a5-d6ae0749f131";
        List<MediationInfo> localPolicies = new ArrayList<>();
        MediationInfo mediationInfo = new MediationInfo();
        mediationInfo.setId("1");
        mediationInfo.setName("in-policy");
        mediationInfo.setType(APIConstants.API_CUSTOM_SEQUENCE_TYPE_IN);
        localPolicies.add(mediationInfo);

        org.wso2.carbon.apimgt.persistence.dto.Mediation mediationPolicy
                = new org.wso2.carbon.apimgt.persistence.dto.Mediation();
        mediationPolicy.setId("1");
        mediationPolicy.setName("in-policy");
        mediationPolicy.setConfig("<sequence/>");
        mediationPolicy.setType(APIConstants.API_CUSTOM_SEQUENCE_TYPE_IN);

        OperationPolicyData policyData = new OperationPolicyData();
        policyData.setPolicyId("11111");

        PowerMockito.when(apiPersistenceInstance.getAllMediationPolicies(any(Organization.class), any(String.class))).thenReturn(localPolicies);
        PowerMockito.when(apiPersistenceInstance.getMediationPolicy(any(Organization.class), any(String.class), any(String.class))).thenReturn(mediationPolicy);

        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apiPersistenceInstance, apimgtDAO, scopesDAO);
        APIIdentifier apiId = new APIIdentifier("admin", "PizzaShackAPI", "1.0.0", apiuuid);
        API api = new API(apiId);
        api.setContext("/test");
        api.setUuid(apiuuid);
        api.setStatus(APIConstants.CREATED);
        Set<URITemplate> uriTemplates = new HashSet<URITemplate>();

        OperationPolicy appliedPolicy = new OperationPolicy();
        appliedPolicy.setPolicyName("in-policy");
        appliedPolicy.setOrder(1);
        appliedPolicy.setDirection(APIConstants.OPERATION_SEQUENCE_TYPE_REQUEST);

        List<OperationPolicy> policyList = new ArrayList<>();
        policyList.add(APIProviderImpl.cloneOperationPolicy(appliedPolicy));

        URITemplate uriTemplate1 = new URITemplate();
        uriTemplate1.setHTTPVerb("POST");
        uriTemplate1.setAuthType("Application");
        uriTemplate1.setUriTemplate("/add");
        uriTemplate1.setThrottlingTier("Gold");
        uriTemplates.add(uriTemplate1);

        URITemplate uriTemplate2 = new URITemplate();
        uriTemplate2.setHTTPVerb("PUT");
        uriTemplate2.setAuthType("Application");
        uriTemplate2.setUriTemplate("/update");
        uriTemplate2.setThrottlingTier("Gold");
        uriTemplates.add(uriTemplate2);

        api.setUriTemplates(uriTemplates);
        api.setInSequence("in-policy");
        api.setApiPolicies(policyList);

        PowerMockito.when(APIUtil.isSequenceDefined(api.getInSequence())).thenReturn(true);

        Mockito.when(apiProvider.getAPISpecificOperationPolicyByPolicyName("in-policy",
                APIConstants.DEFAULT_POLICY_VERSION, api.getUuid(), null, superTenantDomain, false)).thenReturn(null);
        PowerMockito.when(APIUtil.getPolicyDataForMediationFlow(api, APIConstants.OPERATION_SEQUENCE_TYPE_REQUEST,
                superTenantDomain)).thenReturn(policyData);
        Mockito.when(apiProvider.addAPISpecificOperationPolicy(apiuuid, policyData, superTenantDomain)).thenReturn("11111");

        apiProvider.migrateMediationPoliciesOfAPI(api, superTenantDomain, false);

        for (OperationPolicy policy : api.getApiPolicies()) {
            if (APIConstants.OPERATION_SEQUENCE_TYPE_REQUEST.equals(policy.getDirection())) {
                Assert.assertEquals(policy.getPolicyId(), "11111");
            } else {
                Assert.fail("should not contain other paths for api policies");
            }
        }
        Assert.assertNull(api.getInSequence());
        Assert.assertNull(api.getInSequenceMediation());
    }


    @Test
    public void testMigrationOfMediationPoliciesToAPIPoliciesIfPoliciesAlreadyMigrated()
            throws APIManagementException, MediationPolicyPersistenceException {

        String apiuuid = "63e1e37e-a5b8-4be6-86a5-d6ae0749f131";
        List<MediationInfo> localPolicies = new ArrayList<>();
        MediationInfo mediationInfo = new MediationInfo();
        mediationInfo.setId("1");
        mediationInfo.setName("in-policy");
        mediationInfo.setType(APIConstants.API_CUSTOM_SEQUENCE_TYPE_IN);
        localPolicies.add(mediationInfo);

        org.wso2.carbon.apimgt.persistence.dto.Mediation mediationPolicy
                = new org.wso2.carbon.apimgt.persistence.dto.Mediation();
        mediationPolicy.setId("1");
        mediationPolicy.setName("in-policy");
        mediationPolicy.setConfig("<sequence/>");
        mediationPolicy.setType(APIConstants.API_CUSTOM_SEQUENCE_TYPE_IN);

        OperationPolicyData policyData = new OperationPolicyData();
        policyData.setPolicyId("11111");

        PowerMockito.when(apiPersistenceInstance.getAllMediationPolicies(any(Organization.class), any(String.class))).thenReturn(localPolicies);
        PowerMockito.when(apiPersistenceInstance.getMediationPolicy(any(Organization.class), any(String.class), any(String.class))).thenReturn(mediationPolicy);

        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apiPersistenceInstance, apimgtDAO, scopesDAO);
        APIIdentifier apiId = new APIIdentifier("admin", "PizzaShackAPI", "1.0.0", apiuuid);
        API api = new API(apiId);
        api.setUuid(apiuuid);
        api.setContext("/test");
        api.setStatus(APIConstants.CREATED);
        Set<URITemplate> uriTemplates = new HashSet<URITemplate>();

        OperationPolicy appliedPolicy = new OperationPolicy();
        appliedPolicy.setPolicyName("in-policy");
        appliedPolicy.setOrder(1);
        appliedPolicy.setDirection(APIConstants.OPERATION_SEQUENCE_TYPE_REQUEST);

        List<OperationPolicy> policyList = new ArrayList<>();
        policyList.add(APIProviderImpl.cloneOperationPolicy(appliedPolicy));

        URITemplate uriTemplate1 = new URITemplate();
        uriTemplate1.setHTTPVerb("POST");
        uriTemplate1.setAuthType("Application");
        uriTemplate1.setUriTemplate("/add");
        uriTemplate1.setThrottlingTier("Gold");
        uriTemplates.add(uriTemplate1);

        URITemplate uriTemplate2 = new URITemplate();
        uriTemplate2.setHTTPVerb("PUT");
        uriTemplate2.setAuthType("Application");
        uriTemplate2.setUriTemplate("/update");
        uriTemplate2.setThrottlingTier("Gold");
        uriTemplates.add(uriTemplate2);

        api.setUriTemplates(uriTemplates);
        api.setInSequence("in-policy");
        api.setApiPolicies(policyList);

        PowerMockito.when(APIUtil.isSequenceDefined(api.getInSequence())).thenReturn(true);

        Mockito.when(apiProvider.getAPISpecificOperationPolicyByPolicyName("in-policy",
                APIConstants.DEFAULT_POLICY_VERSION, api.getUuid(), null, superTenantDomain, false)).thenReturn(policyData);

        apiProvider.migrateMediationPoliciesOfAPI(api, superTenantDomain, false);

        for (OperationPolicy policy : api.getApiPolicies()) {
            if (APIConstants.OPERATION_SEQUENCE_TYPE_REQUEST.equals(policy.getDirection())) {
                Assert.assertEquals(policy.getPolicyId(), "11111");
            } else {
                Assert.fail("Should not contain other paths for API policies");
            }
        }

        Assert.assertNull(api.getInSequence());
        Assert.assertNull(api.getInSequenceMediation());
    }

    @Test
    public void testSearchPaginatedAPIsByFQDNWithCorrectInputs() throws APIManagementException, APIPersistenceException {

        int API_COUNT = 10;
        int TOTAL_API_COUNT = API_COUNT + 5;

        String[] returnRoles = {
                "Internal/subscriber",
                "Internal/publisher",
                "admin"
        };
        Map<String, Object> returnProperties = new HashMap<>();
        returnProperties.put("isAdmin", true);
        returnProperties.put("skipRoles", null);

        Mockito.when(APIUtil.getTenantAdminUserName(Mockito.anyString())).thenReturn("admin");
        Mockito.when(APIUtil.getFilteredUserRoles(Mockito.anyString())).thenReturn(returnRoles);
        Mockito.when(APIUtil.getUserProperties(Mockito.anyString())).thenReturn(returnProperties);

        PublisherAPISearchResult returnSearchAPIs = new PublisherAPISearchResult();
        List<PublisherAPIInfo> list = createMockPublisherAPIInfoList(API_COUNT);
        returnSearchAPIs.setPublisherAPIInfoList(list);
        returnSearchAPIs.setReturnedAPIsCount(API_COUNT);
        returnSearchAPIs.setTotalAPIsCount(TOTAL_API_COUNT);

        Mockito.when(apiPersistenceInstance.searchAPIsForPublisher(
                Mockito.any(Organization.class),
                Mockito.anyString(),
                Mockito.anyInt(),
                Mockito.anyInt(),
                Mockito.any(UserContext.class),
                Mockito.anyString(),
                Mockito.anyString())).thenReturn(returnSearchAPIs);

        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apiPersistenceInstance, apimgtDAO, scopesDAO);

        APISearchResult response = apiProvider.searchPaginatedAPIsByFQDN("https://abc.test.com",
                "carbon.super", 1 , 6);

        Assert.assertNotNull(response);
        Assert.assertEquals(response.getApiCount(), TOTAL_API_COUNT);
        Assert.assertEquals(response.getApis().size(), API_COUNT);
    }

    @Test
    public void testSearchPaginatedAPIsByFQDNWhenSearchResultIsNull() throws APIManagementException, APIPersistenceException {

        String[] returnRoles = {
                "Internal/subscriber",
                "Internal/publisher",
                "admin"
        };
        Map<String, Object> returnProperties = new HashMap<>();
        returnProperties.put("isAdmin", true);
        returnProperties.put("skipRoles", null);

        Mockito.when(APIUtil.getTenantAdminUserName(Mockito.anyString())).thenReturn("admin");
        Mockito.when(APIUtil.getFilteredUserRoles(Mockito.anyString())).thenReturn(returnRoles);
        Mockito.when(APIUtil.getUserProperties(Mockito.anyString())).thenReturn(returnProperties);

        Mockito.when(apiPersistenceInstance.searchAPIsForPublisher(
                Mockito.any(Organization.class),
                Mockito.anyString(),
                Mockito.anyInt(),
                Mockito.anyInt(),
                Mockito.any(UserContext.class),
                Mockito.anyString(),
                Mockito.anyString())).thenReturn(null);

        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apiPersistenceInstance, apimgtDAO, scopesDAO);

        APISearchResult response = apiProvider.searchPaginatedAPIsByFQDN("https://abc.test.com",
                "carbon.super", 1 , 6);

        Assert.assertNotNull(response);
        Assert.assertEquals(response.getApiCount(), 0);
        Assert.assertEquals(response.getApis().size(), 0);
    }

    @Test(expected = APIManagementException.class)
    public void testSearchPaginatedAPIsByFQDNWhenEndpointIsInvalid() throws APIManagementException, APIPersistenceException {
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apiPersistenceInstance, apimgtDAO, scopesDAO);
        APISearchResult response = apiProvider.searchPaginatedAPIsByFQDN("this is invalid url",
                "carbon.super", 1 , 6);
    }

    private List<PublisherAPIInfo> createMockPublisherAPIInfoList(int num) {
        List<PublisherAPIInfo> list = new ArrayList<>();
        for(int i = 0; i < num; i++) {
            PublisherAPIInfo publisherAPIInfo = new PublisherAPIInfo();
            publisherAPIInfo.setId(String.valueOf(i));
            publisherAPIInfo.setApiName("api" + i);
            publisherAPIInfo.setContext("/test" + i);
            publisherAPIInfo.setVersion("v1");
            publisherAPIInfo.setProviderName("admin");
            list.add(publisherAPIInfo);
        }
        return list;
    }
}
