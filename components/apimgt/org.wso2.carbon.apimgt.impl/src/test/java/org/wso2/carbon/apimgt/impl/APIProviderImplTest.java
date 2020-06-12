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
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.util.JavaUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.entity.ContentType;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.BlockConditionNotFoundException;
import org.wso2.carbon.apimgt.api.FaultGatewaysException;
import org.wso2.carbon.apimgt.api.PolicyDeploymentFailureException;
import org.wso2.carbon.apimgt.api.dto.UserApplicationAPIUsage;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIPublisher;
import org.wso2.carbon.apimgt.api.model.APIStateChangeResponse;
import org.wso2.carbon.apimgt.api.model.APIStore;
import org.wso2.carbon.apimgt.api.model.BlockConditionsDTO;
import org.wso2.carbon.apimgt.api.model.CORSConfiguration;
import org.wso2.carbon.apimgt.api.model.Documentation;
import org.wso2.carbon.apimgt.api.model.Documentation.DocumentSourceType;
import org.wso2.carbon.apimgt.api.model.Documentation.DocumentVisibility;
import org.wso2.carbon.apimgt.api.model.DocumentationType;
import org.wso2.carbon.apimgt.api.model.DuplicateAPIException;
import org.wso2.carbon.apimgt.api.model.KeyManager;
import org.wso2.carbon.apimgt.api.model.Provider;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.api.model.policy.APIPolicy;
import org.wso2.carbon.apimgt.api.model.policy.ApplicationPolicy;
import org.wso2.carbon.apimgt.api.model.policy.Condition;
import org.wso2.carbon.apimgt.api.model.policy.GlobalPolicy;
import org.wso2.carbon.apimgt.api.model.policy.HTTPVerbCondition;
import org.wso2.carbon.apimgt.api.model.policy.Pipeline;
import org.wso2.carbon.apimgt.api.model.policy.Policy;
import org.wso2.carbon.apimgt.api.model.policy.PolicyConstants;
import org.wso2.carbon.apimgt.api.model.policy.QuotaPolicy;
import org.wso2.carbon.apimgt.api.model.policy.RequestCountLimit;
import org.wso2.carbon.apimgt.api.model.policy.SubscriptionPolicy;
import org.wso2.carbon.apimgt.impl.certificatemgt.CertificateManager;
import org.wso2.carbon.apimgt.impl.certificatemgt.CertificateManagerImpl;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.definitions.OASParserUtil;
import org.wso2.carbon.apimgt.impl.definitions.GraphQLSchemaDefinition;
import org.wso2.carbon.apimgt.impl.dto.GatewayArtifactSynchronizerProperties;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.impl.dto.WorkflowProperties;
import org.wso2.carbon.apimgt.impl.factory.KeyManagerHolder;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.notification.NotificationDTO;
import org.wso2.carbon.apimgt.impl.notification.NotificationExecutor;
import org.wso2.carbon.apimgt.impl.notification.NotifierConstants;
import org.wso2.carbon.apimgt.impl.template.APITemplateBuilder;
import org.wso2.carbon.apimgt.impl.template.APITemplateException;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.workflow.*;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifactImpl;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.custom.lifecycles.checklist.beans.LifecycleBean;
import org.wso2.carbon.governance.custom.lifecycles.checklist.util.CheckListItem;
import org.wso2.carbon.governance.custom.lifecycles.checklist.util.LifecycleBeanPopulator;
import org.wso2.carbon.governance.custom.lifecycles.checklist.util.Property;
import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.CollectionImpl;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.realm.RegistryAuthorizationManager;
import org.wso2.carbon.registry.core.pagination.PaginationContext;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
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
import static org.mockito.Matchers.any;
import static org.wso2.carbon.apimgt.impl.token.ClaimsRetriever.DEFAULT_DIALECT_URI;

@RunWith(PowerMockRunner.class)
@SuppressStaticInitializationFor("org.wso2.carbon.context.PrivilegedCarbonContext")
@PrepareForTest({ ServiceReferenceHolder.class, ApiMgtDAO.class, APIUtil.class, APIGatewayManager.class,
        GovernanceUtils.class, PrivilegedCarbonContext.class, WorkflowExecutorFactory.class, JavaUtils.class,
        APIProviderImpl.class, APIManagerFactory.class, RegistryUtils.class, ThrottlePolicyDeploymentManager.class,
        LifecycleBeanPopulator.class, Caching.class, PaginationContext.class, MultitenantUtils.class,
        AbstractAPIManager.class, OASParserUtil.class, KeyManagerHolder.class, CertificateManagerImpl.class })
public class APIProviderImplTest {

    private static String EP_CONFIG_WSDL = "{\"production_endpoints\":{\"url\":\"http://ws.cdyne.com/phoneverify/phoneverify.asmx?wsdl\""
            + ",\"config\":null,\"template_not_supported\":false},\"endpoint_type\":\"wsdl\"}";
    private static String WSDL_URL = "http://ws.cdyne.com/phoneverify/phoneverify.asmx?wsdl";
    private ApiMgtDAO apimgtDAO;
    private KeyManager keyManager;
    private Registry registry;
    private GenericArtifactManager artifactManager;
    private APIGatewayManager gatewayManager;
    private GenericArtifact artifact;
    private CertificateManagerImpl certificateManager;
    private APIManagerConfiguration config;

    @Before
    public void init() throws Exception {
        System.setProperty("carbon.home", APIProviderImplTest.class.getResource("/").getFile());
        PowerMockito.mockStatic(ApiMgtDAO.class);
        PowerMockito.mockStatic(PrivilegedCarbonContext.class);
        PowerMockito.mockStatic(RegistryUtils.class);
        PowerMockito.mockStatic(GovernanceUtils.class);
        PowerMockito.mockStatic(WorkflowExecutorFactory.class);
        PowerMockito.mockStatic(ThrottlePolicyDeploymentManager.class);
        PowerMockito.mockStatic(LifecycleBeanPopulator.class);
        PowerMockito.mockStatic(KeyManagerHolder.class);
        PowerMockito.mockStatic(Caching.class);
        PowerMockito.mockStatic(PaginationContext.class);
        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.mockStatic(APIGatewayManager.class);
        PowerMockito.mockStatic(CertificateManagerImpl.class);

        apimgtDAO = Mockito.mock(ApiMgtDAO.class);
        keyManager = Mockito.mock(KeyManager.class);
        certificateManager = Mockito.mock(CertificateManagerImpl.class);
        Mockito.when(keyManager.getResourceByApiId(Mockito.anyString())).thenReturn(null);
        Mockito.when(keyManager.registerNewResource(Mockito.any(API.class), Mockito.any(Map.class))).thenReturn(true);
        PowerMockito.when(KeyManagerHolder.getKeyManagerInstance(Mockito.anyString())).thenReturn(keyManager);
        PowerMockito.when(CertificateManagerImpl.getInstance()).thenReturn(certificateManager);

        PowerMockito.when(APIUtil.isAPIManagementEnabled()).thenReturn(false);
        PowerMockito.when(APIUtil.replaceEmailDomainBack(Mockito.anyString())).thenReturn("admin");
        Mockito.when(APIUtil.replaceEmailDomain(Mockito.anyString())).thenReturn("admin");

        PrivilegedCarbonContext prcontext = Mockito.mock(PrivilegedCarbonContext.class);
        PowerMockito.when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(prcontext);

        PowerMockito.doNothing().when(prcontext).setUsername(Mockito.anyString());
        PowerMockito.doNothing().when(prcontext).setTenantDomain(Mockito.anyString(), Mockito.anyBoolean());

        ThrottlePolicyDeploymentManager manager = Mockito.mock(ThrottlePolicyDeploymentManager.class);
        PowerMockito.when(ThrottlePolicyDeploymentManager.getInstance()).thenReturn(manager);

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

        PowerMockito.when(APIUtil.replaceSystemProperty(Mockito.anyString())).thenAnswer((Answer<String>) invocation -> {
            Object[] args = invocation.getArguments();
            return (String) args[0];
        });
        TestUtils.initConfigurationContextService(true);
    }

    @Test
    public void testGetAllProviders() throws APIManagementException, GovernanceException {
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        UserRegistry userReg = Mockito.mock(UserRegistry.class);

        API api1 = new API(new APIIdentifier("admin", "API1", "1.0.1"));
        api1.setContext("api1context");
        api1.setStatus(APIConstants.PUBLISHED);
        api1.setDescription("API 1 Desciption");
        GenericArtifact genericArtifact1 = Mockito.mock(GenericArtifact.class);
        GenericArtifact genericArtifact2 = Mockito.mock(GenericArtifact.class);

        Mockito.when(genericArtifact1.getAttribute(APIConstants.API_OVERVIEW_NAME)).thenReturn("API1");
        Mockito.when(genericArtifact1.getAttribute(APIConstants.API_OVERVIEW_VERSION)).thenReturn("1.0.1");
        Mockito.when(genericArtifact1.getAttribute(APIConstants.API_OVERVIEW_CONTEXT)).thenReturn("api1context");
        Mockito.when(genericArtifact1.getAttribute(APIConstants.API_OVERVIEW_DESCRIPTION)).thenReturn(
                "API 1 Desciption");
        Mockito.when(APIUtil.getAPI(genericArtifact1, apiProvider.registry)).thenReturn(api1);
        Mockito.when(APIUtil.getAPI(genericArtifact1)).thenReturn(api1);
        GenericArtifact[] genericArtifacts = {genericArtifact1, genericArtifact2};
        Mockito.when(artifactManager.getAllGenericArtifacts()).thenReturn(genericArtifacts);
        PowerMockito.when(APIUtil.getArtifactManager(userReg, APIConstants.API_KEY)).thenReturn(artifactManager);
        Mockito.when(artifactManager.getAllGenericArtifacts()).thenReturn(genericArtifacts);
        Assert.assertNotNull(apiProvider.getAllProviders());

        //generic artifact null
        Mockito.when(artifactManager.getAllGenericArtifacts()).thenReturn(genericArtifacts);
        PowerMockito.when(APIUtil.getArtifactManager(userReg, APIConstants.API_KEY)).thenReturn(artifactManager);
        Mockito.when(artifactManager.getAllGenericArtifacts()).thenReturn(null);
        Assert.assertNotNull(apiProvider.getAllProviders());
    }

    @Test
    public void testGetSubscribersOfProvider() throws APIManagementException {
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
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
    public void testGetProvider() throws APIManagementException, RegistryException {
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        Mockito.when(APIUtil.getMountedPath((RegistryContext) Mockito.anyObject(),
                Mockito.anyString())).thenReturn("testPath");
        UserRegistry userReg = Mockito.mock(UserRegistry.class);
        Resource resource = Mockito.mock(Resource.class);
        Mockito.when(apiProvider.registry.get("testPath/providers/testProvider")).thenReturn(resource);
        Mockito.when(resource.getUUID()).thenReturn("testID");
        PowerMockito.when(APIUtil.getArtifactManager(userReg, APIConstants.API_KEY)).thenReturn(artifactManager);
        GenericArtifact providerArtifact = Mockito.mock(GenericArtifact.class);
        Mockito.when(artifactManager.getGenericArtifact("testID")).thenReturn(providerArtifact);
        Provider provider = Mockito.mock(Provider.class);
        Mockito.when(APIUtil.getProvider(providerArtifact)).thenReturn(provider);
        Assert.assertNotNull(apiProvider.getProvider("testProvider"));
    }

    @Test
    public void testGetAllAPIUsageByProvider() throws APIManagementException {
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        UserApplicationAPIUsage[] userApplicationAPIUsages = new UserApplicationAPIUsage[]{};
        Mockito.when(apimgtDAO.getAllAPIUsageByProvider("testProvider")).
                thenReturn((userApplicationAPIUsages));
        assertNotNull(apiProvider.getAllAPIUsageByProvider(("testProvider")));
    }

    @Test
    public void testGetSubscribersOfAPI() throws APIManagementException {
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
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
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
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
    public void testCheckIfAPIExists() throws APIManagementException, UserStoreException, RegistryException {
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        APIIdentifier apiId = new APIIdentifier("admin", "API1", "1.0.1");
        Mockito.when(APIUtil.getAPIPath(apiId)).thenReturn("testPath");
        PowerMockito.mockStatic(MultitenantUtils.class);
        PowerMockito.when(MultitenantUtils.getTenantDomain(Mockito.anyString())).thenReturn("abc.org");
        //Mock Config system registry
        ServiceReferenceHolder sh = TestUtils.getServiceReferenceHolder();
        RegistryService registryService = Mockito.mock(RegistryService.class);
        PowerMockito.when(sh.getRegistryService()).thenReturn(registryService);
        UserRegistry systemReg = Mockito.mock(UserRegistry.class);
        RealmService realmService = Mockito.mock(RealmService.class);
        TenantManager tm = Mockito.mock(TenantManager.class);

        PowerMockito.when(sh.getRealmService()).thenReturn(realmService);
        PowerMockito.when(realmService.getTenantManager()).thenReturn(tm);
        PowerMockito.when(tm.getTenantId(Matchers.anyString())).thenReturn(-1234);
        PowerMockito.when(sh.getRegistryService()).thenReturn(registryService);
        PowerMockito.when(registryService.getGovernanceSystemRegistry(-1234)).thenReturn(systemReg);
        Mockito.when(systemReg.resourceExists("testPath")).thenReturn(true);
        Assert.assertEquals(true, apiProvider.checkIfAPIExists(apiId));

        PowerMockito.when(MultitenantUtils.getTenantDomain(Mockito.anyString())).thenReturn("carbon.super");
        apiProvider.tenantDomain = "carbon.super1";
        PowerMockito.when(registryService.getGovernanceUserRegistry("admin", -1234)).thenReturn(systemReg);
        Assert.assertEquals(true, apiProvider.checkIfAPIExists(apiId));
        apiProvider.tenantDomain = null;
        apiProvider.registry = systemReg;
        Assert.assertEquals(true, apiProvider.checkIfAPIExists(apiId));

    }

    @Test
    public void testUpdateAPIsInExternalAPIStores() throws APIManagementException {
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        APIIdentifier apiId = new APIIdentifier("admin", "API1", "1.0.1");
        API api = new API(apiId);
        api.setContext("/test");
        api.setStatus(APIConstants.CREATED);
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
        Mockito.when(apimgtDAO.getExternalAPIStoresDetails(apiId)).thenReturn(apiStores);
        Mockito.when(publisher.isAPIAvailable(api, apiStore)).thenReturn(true);
        Mockito.when(publisher.isAPIAvailable(api, apiStore1)).thenReturn(true);
        Mockito.when(APIUtil.getExternalAPIStore(apiStore.getName(), -1)).thenReturn(apiStore);
        Mockito.when(APIUtil.getExternalAPIStore(apiStore1.getName(), -1)).thenReturn(apiStore1);
        Assert.assertTrue(apiProvider.updateAPIsInExternalAPIStores(api, apiStores, true));
    }

    @Test
    public void testGetExternalAPIStores() throws APIManagementException {
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        APIIdentifier apiId = new APIIdentifier("admin", "API1", "1.0.1");
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
        Mockito.when(apimgtDAO.getExternalAPIStoresDetails(apiId)).thenReturn(apiStores);
        Assert.assertNotNull(apiProvider.getExternalAPIStores(apiId));
        // return null
        Assert.assertNull(apiProvider.getExternalAPIStores(apiId));
    }

    @Test
    public void testGetCustomInSequences1() throws Exception {
        APIIdentifier apiId = new APIIdentifier("admin", "API1", "1.0.1");
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        mockSequences(APIConstants.API_CUSTOM_INSEQUENCE_LOCATION, APIConstants.API_CUSTOM_SEQUENCE_TYPE_IN,
                apiId);
        List<String> sequenceList = apiProvider.getCustomInSequences();
        Assert.assertNotNull(sequenceList);
        Assert.assertEquals(1, sequenceList.size());

        // OMException when building OMElement
        PowerMockito.when(APIUtil.buildOMElement(any(InputStream.class))).thenThrow(new OMException());
        apiProvider.getCustomOutSequences(apiId);

        //org.wso2.carbon.registry.api.RegistryException
        ServiceReferenceHolder sh = PowerMockito.mock(ServiceReferenceHolder.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(sh);
        RegistryService registryService = Mockito.mock(RegistryService.class);
        PowerMockito.when(sh.getRegistryService()).thenReturn(registryService);
        UserRegistry registry = Mockito.mock(UserRegistry.class);
        PowerMockito.when(registryService.getGovernanceSystemRegistry(Matchers.anyInt())).thenReturn(registry);
        Mockito.when(registry.resourceExists(APIConstants.API_CUSTOM_INSEQUENCE_LOCATION)).thenThrow(
                org.wso2.carbon.registry.api.RegistryException.class);
        String msg = "Error while processing the in in the registry";
        try {
            apiProvider.getCustomInSequences();
        } catch (APIManagementException e) {
            Assert.assertEquals(msg, e.getMessage());
        }

        //Registry Exception
        PowerMockito.when(registryService.getGovernanceSystemRegistry(Matchers.anyInt())).thenThrow(
                RegistryException.class);
        String msg1 = "Error while retrieving registry for tenant -1";
        try {
            apiProvider.getCustomInSequences();
        } catch (APIManagementException e) {
            Assert.assertEquals(msg1, e.getMessage());
        }
    }

    @Test
    public void testGetCustomInSequencesSorted() throws Exception {
        APIIdentifier apiId = new APIIdentifier("admin", "API1", "1.0.1");
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        mockSequencesMultiple(APIConstants.API_CUSTOM_INSEQUENCE_LOCATION, APIConstants.API_CUSTOM_SEQUENCE_TYPE_IN,
                apiId);
        List<String> sequenceList = apiProvider.getCustomInSequences();
        Assert.assertNotNull(sequenceList);
        Assert.assertEquals(2, sequenceList.size());
        Assert.assertEquals(sequenceList.get(0), "abc");
        Assert.assertEquals(sequenceList.get(1), "pqr");
    }

    @Test
    public void testGetCustomOutSequences1() throws Exception {
        APIIdentifier apiId = new APIIdentifier("admin", "API1", "1.0.1");
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        mockSequences(APIConstants.API_CUSTOM_OUTSEQUENCE_LOCATION, APIConstants.API_CUSTOM_SEQUENCE_TYPE_OUT,
                apiId);
        List<String> sequenceList = apiProvider.getCustomOutSequences();
        Assert.assertNotNull(sequenceList);
        Assert.assertEquals(1, sequenceList.size());

        // OMException when building OMElement
        PowerMockito.when(APIUtil.buildOMElement(any(InputStream.class))).thenThrow(new OMException());
        apiProvider.getCustomOutSequences();

        //org.wso2.carbon.registry.api.RegistryException
        ServiceReferenceHolder sh = PowerMockito.mock(ServiceReferenceHolder.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(sh);
        RegistryService registryService = Mockito.mock(RegistryService.class);
        PowerMockito.when(sh.getRegistryService()).thenReturn(registryService);
        UserRegistry registry = Mockito.mock(UserRegistry.class);
        PowerMockito.when(registryService.getGovernanceSystemRegistry(Matchers.anyInt())).thenReturn(registry);
        Mockito.when(registry.resourceExists(APIConstants.API_CUSTOM_OUTSEQUENCE_LOCATION)).thenThrow(
                org.wso2.carbon.registry.api.RegistryException.class);
        String msg = "Error while processing the out in the registry";
        try {
            apiProvider.getCustomOutSequences();
        } catch (APIManagementException e) {
            Assert.assertEquals(msg, e.getMessage());
        }

        //Registry Exception
        PowerMockito.when(registryService.getGovernanceSystemRegistry(Matchers.anyInt())).thenThrow(
                RegistryException.class);
        String msg1 = "Error while retrieving registry for tenant -1";
        try {
            apiProvider.getCustomOutSequences();
        } catch (APIManagementException e) {
            Assert.assertEquals(msg1, e.getMessage());
        }
    }

    @Test
    public void testGetCustomFaultSequences1() throws Exception {
        APIIdentifier apiId = new APIIdentifier("admin", "API1", "1.0.1");
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        mockSequences(APIConstants.API_CUSTOM_FAULTSEQUENCE_LOCATION, APIConstants.API_CUSTOM_SEQUENCE_TYPE_FAULT,
                apiId);
        List<String> sequenceList = apiProvider.getCustomFaultSequences();
        Assert.assertNotNull(sequenceList);
        Assert.assertEquals(1, sequenceList.size());

        // OMException when building OMElement
        PowerMockito.when(APIUtil.buildOMElement(any(InputStream.class))).thenThrow(new OMException());
        apiProvider.getCustomFaultSequences();

        //org.wso2.carbon.registry.api.RegistryException
        ServiceReferenceHolder sh = PowerMockito.mock(ServiceReferenceHolder.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(sh);
        RegistryService registryService = Mockito.mock(RegistryService.class);
        PowerMockito.when(sh.getRegistryService()).thenReturn(registryService);
        UserRegistry registry = Mockito.mock(UserRegistry.class);
        PowerMockito.when(registryService.getGovernanceSystemRegistry(Matchers.anyInt())).thenReturn(registry);
        Mockito.when(registry.resourceExists(APIConstants.API_CUSTOM_FAULTSEQUENCE_LOCATION)).thenThrow(
                org.wso2.carbon.registry.api.RegistryException.class);
        String msg = "Error while processing the fault in the registry";
        try {
            apiProvider.getCustomFaultSequences();
        } catch (APIManagementException e) {
            Assert.assertEquals(msg, e.getMessage());
        }

        //Registry Exception
        PowerMockito.when(registryService.getGovernanceSystemRegistry(Matchers.anyInt())).thenThrow(
                RegistryException.class);
        String msg1 = "Error while retrieving registry for tenant -1";
        try {
            apiProvider.getCustomFaultSequences();
        } catch (APIManagementException e) {
            Assert.assertEquals(msg1, e.getMessage());
        }
    }

    @Test
    public void testGetConsumerKeys() throws APIManagementException {
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        APIIdentifier apiId = new APIIdentifier("admin", "API1", "1.0.1");
        String[] test = new String[]{};
        Mockito.when(apimgtDAO.getConsumerKeys(apiId)).thenReturn(test);
        assertNotNull(apiProvider.getConsumerKeys(apiId));
    }

    @Test
    public void testChangeAPILCCheckListItems() throws APIManagementException, GovernanceException {
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        APIIdentifier apiId = new APIIdentifier("admin", "API1", "1.0.1");
        GenericArtifact apiArtifact = Mockito.mock(GenericArtifact.class);
        Mockito.when(APIUtil.getAPIArtifact(apiId, apiProvider.registry)).thenReturn(apiArtifact);
        Mockito.when(apiArtifact.isLCItemChecked(10, "APILifeCycle")).thenThrow(GovernanceException.class).
                thenReturn(false, true);
        String msg = "Error while setting registry lifecycle checklist items for the API: API1";
        try {
            apiProvider.changeAPILCCheckListItems(apiId, 10, true);
        } catch (APIManagementException e) {
            Assert.assertEquals(msg, e.getMessage());
        }
        //status checked
        assertTrue(apiProvider.changeAPILCCheckListItems(apiId, 10, true));
        // status false
        assertTrue(apiProvider.changeAPILCCheckListItems(apiId, 10, true));

    }

    @Test
    public void testGetCustomApiInSequences() throws Exception {
        APIIdentifier apiId = new APIIdentifier("admin", "API1", "1.0.1");
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        mockSequences(APIConstants.API_CUSTOM_INSEQUENCE_LOCATION, APIConstants.API_CUSTOM_SEQUENCE_TYPE_IN,
                apiId);
        List<String> sequenceList = apiProvider.getCustomApiInSequences(apiId);
        Assert.assertEquals(1, sequenceList.size());

        // OMException when building OMElement
        PowerMockito.when(APIUtil.buildOMElement(any(InputStream.class))).thenThrow(new OMException());
        apiProvider.getCustomApiInSequences(apiId);

        //org.wso2.carbon.registry.api.RegistryException
        ServiceReferenceHolder sh = PowerMockito.mock(ServiceReferenceHolder.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(sh);
        RegistryService registryService = Mockito.mock(RegistryService.class);
        PowerMockito.when(sh.getRegistryService()).thenReturn(registryService);
        UserRegistry registry = Mockito.mock(UserRegistry.class);
        PowerMockito.when(registryService.getGovernanceSystemRegistry(Matchers.anyInt())).thenReturn(registry);
        Mockito.when(registry.resourceExists(APIUtil.getSequencePath(apiId,
                APIConstants.API_CUSTOM_SEQUENCE_TYPE_IN))).thenThrow(
                org.wso2.carbon.registry.api.RegistryException.class);
        String msg = "Error while processing the " + APIConstants.API_CUSTOM_SEQUENCE_TYPE_IN +
                " sequences of " + apiId + " in the registry";
        try {
            apiProvider.getCustomApiInSequences(apiId);
        } catch (APIManagementException e) {
            Assert.assertEquals(msg, e.getMessage());
        }

        //Registry Exception
        PowerMockito.when(registryService.getGovernanceSystemRegistry(Matchers.anyInt())).thenThrow(
                RegistryException.class);
        String msg1 = "Error while retrieving registry for tenant -1";
        try {
            apiProvider.getCustomApiInSequences(apiId);
        } catch (APIManagementException e) {
            Assert.assertEquals(msg1, e.getMessage());
        }
    }

    @Test
    public void testGetCustomApiOutSequences() throws Exception {
        APIIdentifier apiId = new APIIdentifier("admin", "API1", "1.0.1");
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        mockSequences(APIConstants.API_CUSTOM_OUTSEQUENCE_LOCATION, APIConstants.API_CUSTOM_SEQUENCE_TYPE_OUT,
                apiId);
        List<String> sequenceList = apiProvider.getCustomApiOutSequences(apiId);
        Assert.assertNotNull(sequenceList);
        Assert.assertEquals(1, sequenceList.size());

        // OMException when building OMElement
        PowerMockito.when(APIUtil.buildOMElement(any(InputStream.class))).thenThrow(new OMException());
        apiProvider.getCustomApiOutSequences(apiId);

        //org.wso2.carbon.registry.api.RegistryException
        ServiceReferenceHolder sh = PowerMockito.mock(ServiceReferenceHolder.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(sh);
        RegistryService registryService = Mockito.mock(RegistryService.class);
        PowerMockito.when(sh.getRegistryService()).thenReturn(registryService);
        UserRegistry registry = Mockito.mock(UserRegistry.class);
        PowerMockito.when(registryService.getGovernanceSystemRegistry(Matchers.anyInt())).thenReturn(registry);
        Mockito.when(registry.resourceExists(APIUtil.getSequencePath(apiId,
                APIConstants.API_CUSTOM_SEQUENCE_TYPE_OUT))).thenThrow(
                org.wso2.carbon.registry.api.RegistryException.class);
        String msg = "Error while processing the " + APIConstants.API_CUSTOM_SEQUENCE_TYPE_OUT +
                " sequences of " + apiId + " in the registry";
        try {
            apiProvider.getCustomApiOutSequences(apiId);
        } catch (APIManagementException e) {
            Assert.assertEquals(msg, e.getMessage());
        }

        //Registry Exception
        PowerMockito.when(registryService.getGovernanceSystemRegistry(Matchers.anyInt())).thenThrow(
                RegistryException.class);
        String msg1 = "Error while retrieving registry for tenant -1";
        try {
            apiProvider.getCustomApiOutSequences(apiId);
        } catch (APIManagementException e) {
            Assert.assertEquals(msg1, e.getMessage());
        }
    }

    @Test
    public void testGetCustomApiFaultSequences() throws Exception {
        APIIdentifier apiId = new APIIdentifier("admin", "API1", "1.0.1");
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        mockSequences(APIConstants.API_CUSTOM_FAULTSEQUENCE_LOCATION, APIConstants.API_CUSTOM_SEQUENCE_TYPE_FAULT,
                apiId);
        List<String> sequenceList = apiProvider.getCustomApiFaultSequences(apiId);
        Assert.assertNotNull(sequenceList);
        Assert.assertEquals(1, sequenceList.size());

        // OMException when building OMElement
        PowerMockito.when(APIUtil.buildOMElement(any(InputStream.class))).thenThrow(new OMException());
        apiProvider.getCustomApiFaultSequences(apiId);

        //org.wso2.carbon.registry.api.RegistryException
        ServiceReferenceHolder sh = PowerMockito.mock(ServiceReferenceHolder.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(sh);
        RegistryService registryService = Mockito.mock(RegistryService.class);
        PowerMockito.when(sh.getRegistryService()).thenReturn(registryService);
        UserRegistry registry = Mockito.mock(UserRegistry.class);
        PowerMockito.when(registryService.getGovernanceSystemRegistry(Matchers.anyInt())).thenReturn(registry);
        Mockito.when(registry.resourceExists(APIUtil.getSequencePath(apiId,
                APIConstants.API_CUSTOM_SEQUENCE_TYPE_FAULT))).thenThrow(
                org.wso2.carbon.registry.api.RegistryException.class);
        String msg = "Error while processing the " + APIConstants.API_CUSTOM_SEQUENCE_TYPE_FAULT +
                " sequences of " + apiId + " in the registry";
        try {
            apiProvider.getCustomApiFaultSequences(apiId);
        } catch (APIManagementException e) {
            Assert.assertEquals(msg, e.getMessage());
        }

        //Registry Exception
        PowerMockito.when(registryService.getGovernanceSystemRegistry(Matchers.anyInt())).thenThrow(
                RegistryException.class);
        String msg1 = "Error while retrieving registry for tenant -1";
        try {
            apiProvider.getCustomApiFaultSequences(apiId);
        } catch (APIManagementException e) {
            Assert.assertEquals(msg1, e.getMessage());
        }
    }

    @Test
    public void testIsSynapseGateway() throws APIManagementException {
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
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
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        String[] test = new String[]{};
        Mockito.when(apimgtDAO.getPolicyNames("testLevel", "testName")).thenReturn(test);
        assertNotNull(apiProvider.getPolicyNames("testName", "testLevel"));
    }

    @Test
    public void testIsGlobalPolicyKeyTemplateExists() throws APIManagementException {
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        GlobalPolicy globalPolicy = Mockito.mock(GlobalPolicy.class);
        Mockito.when(apimgtDAO.isKeyTemplatesExist(globalPolicy)).thenReturn(true);
        assertTrue(apiProvider.isGlobalPolicyKeyTemplateExists(globalPolicy));
    }

    @Test
    public void testHasAttachments() throws APIManagementException {
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        PowerMockito.when(APIUtil.getTenantId(Mockito.anyString())).thenReturn(0);
        PowerMockito.mockStatic(MultitenantUtils.class);
        PowerMockito.when(MultitenantUtils.getTenantDomain("testName")).thenReturn("carbon.super");
        Mockito.when(apimgtDAO.hasSubscription("testId", "testName", "testPolicy")).
                thenReturn(true);
        assertTrue(apiProvider.hasAttachments("testName", "testId", "testPolicy"));
    }

    @Test
    public void testGetBlockConditions() throws APIManagementException {
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        List<BlockConditionsDTO> list = new ArrayList<BlockConditionsDTO>();
        Mockito.when(apimgtDAO.getBlockConditions(Mockito.anyString())).thenReturn(list);
        assertNotNull(apiProvider.getBlockConditions());
    }

    @Test
    public void testGetBlockCondition() throws APIManagementException {
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        BlockConditionsDTO blockConditionsDTO = new BlockConditionsDTO();
        Mockito.when(apimgtDAO.getBlockCondition(Mockito.anyInt())).thenReturn(blockConditionsDTO);
        assertNotNull(apiProvider.getBlockCondition(Mockito.anyInt()));
    }

    @Test
    public void testGetBlockConditionByUUID() throws APIManagementException {
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
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
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
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
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
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
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
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
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
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
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
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
    public void testGetSubscriberClaims() throws APIManagementException, UserStoreException {
        String configuredClaims = "http://wso2.org/claim1,http://wso2.org/claim2";
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
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
        PowerMockito.when(tenantManager.getTenantId(Matchers.anyString())).thenReturn(-1234);

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
    public void testAddTier() throws APIManagementException, RegistryException {
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        Tier tier = new Tier("testTier");
        tier.setDescription("testDescription");
        tier.setTierPlan("testPlan");

        Map<String, Tier> tierMap = new HashMap<String, Tier>();
        tierMap.put("tier", tier);
        PowerMockito.when(APIUtil.getAllTiers()).thenReturn(tierMap);
        Resource resource = new ResourceImpl();
        Mockito.when(apiProvider.registry.newResource()).thenReturn(resource);
        apiProvider.addTier(tier);
        Mockito.verify(apiProvider.registry);
    }

    @Test
    public void testGetExternalWorkflowReferenceId() throws APIManagementException {
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        Mockito.when(apimgtDAO.getExternalWorkflowReferenceForSubscription(Mockito.anyInt())).thenReturn("testValue");
        assertNotNull("testValue", apiProvider.getExternalWorkflowReferenceId(Mockito.anyInt()));
    }

    @Test
    public void testGetAPIPolicy() throws APIManagementException {
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        PowerMockito.when(APIUtil.getTenantId("testUser")).thenReturn(1111);
        APIPolicy apiPolicy = Mockito.mock(APIPolicy.class);
        Mockito.when(apimgtDAO.getAPIPolicy("testPolicy", 1111)).thenReturn(apiPolicy);
        assertNotNull(apiProvider.getAPIPolicy("testUser", "testPolicy"));
    }

    @Test
    public void testGetAPIPolicyByUUID() throws APIManagementException {
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
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
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        PowerMockito.when(APIUtil.getTenantId("testUser")).thenReturn(1111);
        ApplicationPolicy applicationPolicy = Mockito.mock(ApplicationPolicy.class);
        Mockito.when(apimgtDAO.getApplicationPolicy("testPolicy", 1111)).
                thenReturn(applicationPolicy);
        assertNotNull(apiProvider.getApplicationPolicy("testUser", "testPolicy"));
    }

    @Test
    public void testGetApplicationPolicyByUUID() throws APIManagementException {
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
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
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        PowerMockito.when(APIUtil.getTenantId("testUser")).thenReturn(1111);
        SubscriptionPolicy subscriptionPolicy = Mockito.mock(SubscriptionPolicy.class);
        Mockito.when(apimgtDAO.getSubscriptionPolicy("testPolicy", 1111)).
                thenReturn(subscriptionPolicy);
        assertNotNull(apiProvider.getSubscriptionPolicy("testUser", "testPolicy"));
    }

    @Test
    public void testGetSubscriptionPolicyByUUID() throws APIManagementException {
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
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
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        GlobalPolicy globalPolicy = Mockito.mock(GlobalPolicy.class);
        Mockito.when(apimgtDAO.getGlobalPolicy("testName")).
                thenReturn(globalPolicy);
        assertNotNull(apiProvider.getGlobalPolicy("testName"));
    }

    @Test
    public void testGetGlobalPolicyByUUID() throws APIManagementException {
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
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
    public void testSearchAPIsByDoc() throws APIManagementException {
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        Map<Documentation, API> apiMap = new HashMap<Documentation, API>();
        PowerMockito.when(APIUtil.searchAPIsByDoc(apiProvider.registry, apiProvider.tenantId,
                apiProvider.username, "testTerm", APIConstants.PUBLISHER_CLIENT)).thenReturn(apiMap);
        assertEquals(apiMap, apiProvider.searchAPIsByDoc("testTerm", "testType"));
    }


    @Test
    public void testRemoveTier() throws APIManagementException, RegistryException {
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        Tier tier = new Tier("testTier");
        tier.setDescription("testDescription");
        tier.setTierPlan("testPlan");

        Map<String, Tier> tierMap = new HashMap<String, Tier>();
        tierMap.put("tier", tier);
        PowerMockito.when(APIUtil.getAllTiers()).thenReturn(tierMap);
        Resource resource = new ResourceImpl();
        Mockito.when(apiProvider.registry.newResource()).thenReturn(resource);

        PowerMockito.when(APIUtil.getArtifactManager(apiProvider.registry, APIConstants.API_KEY)).
                thenReturn(artifactManager);
        GenericArtifact genericArtifact1 = new GenericArtifactImpl(new QName("local"), "artifact1");
        GenericArtifact genericArtifact2 = new GenericArtifactImpl(new QName("local"), "artifact2");
        GenericArtifact[] genericArtifacts = new GenericArtifact[]{genericArtifact1, genericArtifact2};
        Mockito.when(artifactManager.findGovernanceArtifacts(Mockito.anyString()))
                .thenReturn(null, genericArtifacts);
        apiProvider.removeTier(tier);
        try {
            apiProvider.removeTier(tier);
        } catch (APIManagementException e) {
            assertEquals("Unable to remove this tier. Tier in use", e.getMessage());
        }
        Mockito.verify(apiProvider.registry);
    }


    @Test
    public void testRemoveDocumentation() throws APIManagementException, RegistryException {
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        APIIdentifier apiId = new APIIdentifier("admin", "API1", "1.0.1");
        PowerMockito.when(APIUtil.getAPIDocPath(apiId) + "testDoc").thenReturn("testPath");
        Resource resource = Mockito.mock(Resource.class);
        Mockito.when(apiProvider.registry.get("testPathtestDoc")).thenReturn(resource);
        Mockito.when(resource.getUUID()).thenReturn("1111");
        PowerMockito.when(APIUtil.getArtifactManager(apiProvider.registry, APIConstants.DOCUMENTATION_KEY)).
                thenReturn(artifactManager);
        GenericArtifact genericArtifact = Mockito.mock(GenericArtifact.class);
        Mockito.when(artifactManager.getGenericArtifact("1111")).thenReturn(genericArtifact);
        Mockito.when(genericArtifact.getAttribute(APIConstants.DOC_FILE_PATH)).thenReturn("testDocPath");
        Association association = Mockito.mock(Association.class);
        Association[] associations = new Association[]{association};
        Mockito.when(apiProvider.registry.getAssociations("testPathtestDoc", APIConstants.DOCUMENTATION_KEY)).
                thenReturn(associations);
        apiProvider.removeDocumentation(apiId, "testDoc", "testType");
        Mockito.verify(apiProvider.registry);
    }

    @Test
    public void testRemoveDocumentation1() throws APIManagementException, RegistryException {
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        APIIdentifier apiId = new APIIdentifier("admin", "API1", "1.0.1");
        PowerMockito.when(APIUtil.getArtifactManager(apiProvider.registry, APIConstants.DOCUMENTATION_KEY)).
                thenReturn(artifactManager);
        GenericArtifact artifact = Mockito.mock(GenericArtifact.class);
        Mockito.when(artifactManager.getGenericArtifact("testId")).thenReturn(artifact);
        Mockito.when(artifact.getPath()).thenReturn("docPath");
        Mockito.when(artifact.getAttribute(APIConstants.DOC_FILE_PATH)).thenReturn("docFilePath");
        Association association = Mockito.mock(Association.class);
        Association[] associations = new Association[]{association};
        Mockito.when(apiProvider.registry.getAssociations("docPath", APIConstants.DOCUMENTATION_KEY)).
                thenReturn(associations);
        apiProvider.removeDocumentation(apiId, "testId");
        Mockito.verify(apiProvider.registry);
    }

    @Test
    public void testCopyAllDocumentation() throws APIManagementException, RegistryException {
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        APIIdentifier apiId = new APIIdentifier("admin", "API1", "1.0.1");
        PowerMockito.when(APIUtil.getAPIDocPath(apiId)).thenReturn("oldVersion");
        Resource resource = new ResourceImpl();
        Mockito.when(apiProvider.registry.get("oldVersion")).thenReturn(resource);
        apiProvider.copyAllDocumentation(apiId, "testVersion");
        Mockito.verify(apiProvider.registry);
    }

    @Test
    public void testDeleteWorkflowTask() throws APIManagementException, WorkflowException {
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        APIIdentifier apiId = new APIIdentifier("admin", "API1", "1.0.1");
        Mockito.when(apimgtDAO.getAPIID(apiId, null)).thenReturn(1111);
        WorkflowExecutorFactory wfe = PowerMockito.mock(WorkflowExecutorFactory.class);
        Mockito.when(WorkflowExecutorFactory.getInstance()).thenReturn(wfe);
        WorkflowExecutor apiStateChangeWFExecutor = Mockito.mock(WorkflowExecutor.class);
        Mockito.when(wfe.getWorkflowExecutor(WorkflowConstants.WF_TYPE_AM_API_STATE)).
                thenReturn(apiStateChangeWFExecutor);
        WorkflowDTO workflowDTO = Mockito.mock(WorkflowDTO.class);
        Mockito.when(apimgtDAO.retrieveWorkflowFromInternalReference(Integer.toString(1111),
                WorkflowConstants.WF_TYPE_AM_API_STATE)).thenReturn(workflowDTO);
        apiProvider.deleteWorkflowTask(apiId);
        Mockito.verify(apimgtDAO, Mockito.times(1)).getAPIID(apiId, null);
    }

    @Test
    public void testAddAPI() throws APIManagementException, RegistryException {
        APIIdentifier apiId = new APIIdentifier("admin", "API1", "1.0.1");
        API api = new API(apiId);
        api.setContext("/test");
        api.setStatus(APIConstants.CREATED);

        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);

        Mockito.when(artifactManager.newGovernanceArtifact(any(QName.class))).thenReturn(artifact);
        Mockito.when(APIUtil.createAPIArtifactContent(artifact, api)).thenReturn(artifact);

        UserRegistry userRegistry = Mockito.mock(UserRegistry.class);
        RegistryService registryService = Mockito.mock(RegistryService.class);
        ServiceReferenceHolder serviceReferenceHolder = TestUtils.getServiceReferenceHolder();
        RealmService realmService = Mockito.mock(RealmService.class);
        TenantManager tenantManager = Mockito.mock(TenantManager.class);

        Mockito.when(artifactManager.newGovernanceArtifact(Matchers.any(QName.class))).thenReturn(artifact);
        Mockito.when(APIUtil.createAPIArtifactContent(artifact, api)).thenReturn(artifact);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getRegistryService()).thenReturn(registryService);
        Mockito.when(registryService.getConfigSystemRegistry(Mockito.anyInt())).thenReturn(userRegistry);
        Mockito.when(serviceReferenceHolder.getRealmService()).thenReturn(realmService);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        Mockito.when(apimgtDAO.addAPI(api, -1234)).thenReturn(1);
        Mockito.doNothing().when(apimgtDAO).addURITemplates(1, api, -1234);
        Mockito.doNothing().when(keyManager).attachResourceScopes(api, api.getUriTemplates());
        try {
            apiProvider.addAPI(api);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testAddAPINameWithIllegalCharacters() throws APIManagementException, GovernanceException {
        APIIdentifier apiId = new APIIdentifier("admin", "API2&", "1.0.2");
        API api = new API(apiId);
        api.setContext("/test");
        api.setStatus(APIConstants.CREATED);

        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);

        Mockito.when(artifactManager.newGovernanceArtifact(any(QName.class))).thenReturn(artifact);
        Mockito.when(APIUtil.createAPIArtifactContent(artifact, api)).thenReturn(artifact);

        try {
            apiProvider.addAPI(api);
            Assert.fail("Exception was expected, but wasn't thrown");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("API Name contains one or more illegal characters"));
        }
    }

    @Test
    public void testAddAPIVersionWithIllegalCharacters() throws APIManagementException, GovernanceException {
        APIIdentifier apiId = new APIIdentifier("admin", "API3", "1.0.2&");
        API api = new API(apiId);
        api.setContext("/test");
        api.setStatus(APIConstants.CREATED);

        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);

        Mockito.when(artifactManager.newGovernanceArtifact(any(QName.class))).thenReturn(artifact);
        Mockito.when(APIUtil.createAPIArtifactContent(artifact, api)).thenReturn(artifact);

        try {
            apiProvider.addAPI(api);
            Assert.fail("Exception was expected, but wasn't thrown");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("API Version contains one or more illegal characters"));
        }
    }

    @Test
    public void testUpdateAPIStatus() throws APIManagementException, FaultGatewaysException, UserStoreException,
            RegistryException, WorkflowException {
        APIIdentifier apiId = new APIIdentifier("admin", "API1", "1.0.1");
        API api = new API(apiId);
        api.setContext("/test");
        api.setStatus(APIConstants.CREATED);


        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);

        RegistryService registryService = Mockito.mock(RegistryService.class);
        UserRegistry userRegistry = Mockito.mock(UserRegistry.class);
        ServiceReferenceHolder serviceReferenceHolder = TestUtils.getServiceReferenceHolder();
        RealmService realmService = Mockito.mock(RealmService.class);
        TenantManager tenantManager = Mockito.mock(TenantManager.class);

        Mockito.when(artifactManager.newGovernanceArtifact(any(QName.class))).thenReturn(artifact);
        Mockito.when(APIUtil.createAPIArtifactContent(artifact, api)).thenReturn(artifact);
        Mockito.when(APIUtil.createAPIArtifactContent(artifact, api)).thenReturn(artifact);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getRegistryService()).thenReturn(registryService);
        Mockito.when(registryService.getConfigSystemRegistry(Mockito.anyInt())).thenReturn(userRegistry);
        Mockito.when(serviceReferenceHolder.getRealmService()).thenReturn(realmService);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        apiProvider.addAPI(api);

        //Existing APIs of the provider
        API api1 = new API(new APIIdentifier("admin", "API1", "1.0.0"));
        api1.setStatus(APIConstants.PUBLISHED);
        API api2 = new API(new APIIdentifier("admin", "API2", "1.0.0"));

        //Mock Updating API
        Resource apiSourceArtifact = Mockito.mock(Resource.class);
        Mockito.when(apiSourceArtifact.getUUID()).thenReturn("12640983654");
        String apiSourcePath = "path";
        PowerMockito.when(APIUtil.getAPIPath(api.getId())).thenReturn(apiSourcePath);
        PowerMockito.when(apiProvider.registry.get(apiSourcePath)).thenReturn(apiSourceArtifact);

        Mockito.when(artifact.getAttribute(APIConstants.API_OVERVIEW_STATUS)).thenReturn("CREATED");
        Mockito.when(artifactManager.getGenericArtifact(apiSourceArtifact.getUUID())).thenReturn(artifact);

        Mockito.when(gatewayManager.isAPIPublished(api, "carbon.super")).thenReturn(false);

        Mockito.when(artifact.getId()).thenReturn("12640983654");
        PowerMockito.when(GovernanceUtils.getArtifactPath(apiProvider.registry, "12640983654")).
                thenReturn(apiSourcePath);
        Mockito.doNothing().when(artifactManager).updateGenericArtifact(artifact);

        prepareForGetAPIsByProvider(artifactManager, apiProvider, "admin", api1, api2);
        prepareForChangeLifeCycleStatus(apiProvider, apimgtDAO, apiId, artifact);

        boolean status = apiProvider.updateAPIStatus(api.getId(), "PUBLISHED", true, true, true);
        Assert.assertTrue(status);

        //Test for non existing API Id
        APIIdentifier apiId1 = new APIIdentifier("admin", "API2", "1.0.0");
        try {
            apiProvider.updateAPIStatus(apiId1, "PUBLISHED", true, true, true);
        } catch (APIManagementException e) {
            Assert.assertEquals("Couldn't find an API with the name-" + apiId.getApiName() + "version-"
                    + apiId1.getVersion(), e.getMessage());
        }
    }

    @Test
    public void testUpdateAPIStatus_InvalidAPIId() throws APIManagementException, FaultGatewaysException, UserStoreException,
                                                                                RegistryException, WorkflowException {
        APIIdentifier apiId = new APIIdentifier("admin", "API1", "1.0.0");
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        try {
            apiProvider.updateAPIStatus(apiId, "PUBLISHED", true, true, true);
        } catch (APIManagementException e) {
            Assert.assertEquals("Couldn't find an API with the name-" + apiId.getApiName() + "version-"
                    + apiId.getVersion(), e.getMessage());
        }
    }

    @Test(expected = APIManagementException.class)
    public void testUpdateAPIStatus_WithFaultyGateways() throws Exception {
        API api = new API(new APIIdentifier("admin", "API1", "1.0.0"));
        api.setContext("/test");
        api.setStatus(APIConstants.CREATED);
        api.setUUID("1223332");

        Set<String> environments = new HashSet<String>();
        environments.add("Production");
        environments.add("Sandbox");

        api.setEnvironments(environments);
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);

        Mockito.when(artifactManager.newGovernanceArtifact(Matchers.any(QName.class))).thenReturn(artifact);
        Mockito.when(APIUtil.createAPIArtifactContent(artifact, api)).thenReturn(artifact);
        RegistryService registryService = Mockito.mock(RegistryService.class);
        UserRegistry userRegistry = Mockito.mock(UserRegistry.class);
        ServiceReferenceHolder serviceReferenceHolder = TestUtils.getServiceReferenceHolder();
        RealmService realmService = Mockito.mock(RealmService.class);
        TenantManager tenantManager = Mockito.mock(TenantManager.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getRegistryService()).thenReturn(registryService);
        Mockito.when(registryService.getConfigSystemRegistry(Mockito.anyInt())).thenReturn(userRegistry);
        Mockito.when(registryService.getGovernanceUserRegistry(Mockito.anyString(), Mockito.anyInt())).thenReturn(userRegistry);
        Mockito.when(serviceReferenceHolder.getRealmService()).thenReturn(realmService);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        apiProvider.addAPI(api);

        //Mock Updating API
        Resource apiSourceArtifact = Mockito.mock(Resource.class);
        Mockito.when(apiSourceArtifact.getUUID()).thenReturn("12640983654");
        String apiSourcePath = "path";
        PowerMockito.when(APIUtil.getAPIPath(api.getId())).thenReturn(apiSourcePath);
        PowerMockito.when(apiProvider.registry.get(apiSourcePath)).thenReturn(apiSourceArtifact);

        Mockito.when(artifact.getAttribute(APIConstants.API_OVERVIEW_STATUS)).thenReturn("CREATED");
        Mockito.when(artifactManager.getGenericArtifact(apiSourceArtifact.getUUID())).thenReturn(artifact);

        Mockito.when(gatewayManager.isAPIPublished(api, "carbon.super")).thenReturn(false);

        Mockito.when(artifact.getId()).thenReturn("12640983654");
        PowerMockito.when(GovernanceUtils.getArtifactPath(apiProvider.registry, "12640983654")).
                thenReturn(apiSourcePath);
        Mockito.doNothing().when(artifactManager).updateGenericArtifact(artifact);

        Map map = Mockito.mock(ConcurrentHashMap.class);
        PowerMockito.whenNew(ConcurrentHashMap.class).withNoArguments().thenReturn((ConcurrentHashMap) map);
        Mockito.when(map.isEmpty()).thenReturn(false);
        Mockito.when(map.get(Mockito.anyString())).thenReturn(map);
        String newStatusValue = "PUBLISHED";

        PowerMockito.when(serviceReferenceHolder.getRegistryService()).thenReturn(registryService);
        PowerMockito.when(registryService.getGovernanceUserRegistry(CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME, -1234))
                .thenReturn(userRegistry);

        APIManagerConfiguration amConfig = Mockito.mock(APIManagerConfiguration.class);
        APIManagerConfigurationService amConfigService = Mockito.mock(APIManagerConfigurationService.class);
        PowerMockito.when(serviceReferenceHolder.getAPIManagerConfigurationService()).thenReturn(amConfigService);
        PowerMockito.when(amConfigService.getAPIManagerConfiguration()).thenReturn(amConfig);
        Mockito.when(amConfig.getFirstProperty(APIConstants.API_GATEWAY_TYPE)).
                thenReturn(APIConstants.API_GATEWAY_TYPE_SYNAPSE);

        PowerMockito.when(serviceReferenceHolder.getRealmService()).thenReturn(realmService);
        PowerMockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        PowerMockito.when(tenantManager.getTenantId(Matchers.anyString())).thenReturn(-1234);
        apiProvider.updateAPIStatus(api.getId(), newStatusValue, true, false, true);
    }

    @Test(expected = APIManagementException.class)
    public void testUpdateAPIStatus_ToRetiredWithFaultyGateways() throws Exception {
        API api = new API(new APIIdentifier("admin", "API1", "1.0.0"));
        api.setContext("/test");
        api.setStatus(APIConstants.CREATED);

        Set<String> environments = new HashSet<String>();
        environments.add("Production");
        environments.add("Sandbox");
        api.setEnvironments(environments);

        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        Mockito.when(artifactManager.newGovernanceArtifact(any(QName.class))).thenReturn(artifact);
        Mockito.when(APIUtil.createAPIArtifactContent(artifact, api)).thenReturn(artifact);
        RegistryService registryService = Mockito.mock(RegistryService.class);
        UserRegistry userRegistry = Mockito.mock(UserRegistry.class);
        ServiceReferenceHolder serviceReferenceHolder = TestUtils.getServiceReferenceHolder();
        RealmService realmService = Mockito.mock(RealmService.class);
        TenantManager tenantManager = Mockito.mock(TenantManager.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getRegistryService()).thenReturn(registryService);
        Mockito.when(registryService.getConfigSystemRegistry(Mockito.anyInt())).thenReturn(userRegistry);
        Mockito.when(registryService.getGovernanceUserRegistry(Mockito.anyString(), Mockito.anyInt())).thenReturn(userRegistry);
        Mockito.when(serviceReferenceHolder.getRealmService()).thenReturn(realmService);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        apiProvider.addAPI(api);

        //Mock Updating API
        Resource apiSourceArtifact = Mockito.mock(Resource.class);
        Mockito.when(apiSourceArtifact.getUUID()).thenReturn("12640983654");
        String apiSourcePath = "path";
        PowerMockito.when(APIUtil.getAPIPath(api.getId())).thenReturn(apiSourcePath);
        PowerMockito.when(apiProvider.registry.get(apiSourcePath)).thenReturn(apiSourceArtifact);

        GenericArtifact artifact = Mockito.mock(GenericArtifact.class);
        Mockito.when(artifact.getAttribute(APIConstants.API_OVERVIEW_STATUS)).thenReturn("CREATED");
        Mockito.when(artifactManager.getGenericArtifact(apiSourceArtifact.getUUID())).thenReturn(artifact);
        Mockito.when(gatewayManager.isAPIPublished(api, "carbon.super")).thenReturn(false);
        PowerMockito.when(APIUtil.createAPIArtifactContent(artifact, api)).thenReturn(artifact);
        Mockito.when(artifact.getId()).thenReturn("12640983654");
        PowerMockito.when(GovernanceUtils.getArtifactPath(apiProvider.registry, "12640983654")).
                thenReturn(apiSourcePath);
        Mockito.doNothing().when(artifactManager).updateGenericArtifact(artifact);

        Map map = Mockito.mock(ConcurrentHashMap.class);
        PowerMockito.whenNew(ConcurrentHashMap.class).withNoArguments().thenReturn((ConcurrentHashMap) map);
        Mockito.when(map.isEmpty()).thenReturn(false);
        Mockito.when(map.get(Mockito.anyString())).thenReturn(map);
        APIManagerConfigurationService configurationService = Mockito.mock(APIManagerConfigurationService.class);
        APIManagerConfiguration configuration = Mockito.mock(APIManagerConfiguration.class);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfigurationService()).thenReturn(configurationService);
        Mockito.when(configurationService.getAPIManagerConfiguration()).thenReturn(configuration);
        Mockito.when(configuration.getFirstProperty(APIConstants.API_GATEWAY_TYPE)).thenReturn(APIConstants.API_GATEWAY_NONE);
        String newStatusValue = "RETIRED";
        apiProvider.updateAPIStatus(api.getId(), newStatusValue, true, false, true);
    }

    @Test
    public void testGetAPIUsageByAPIId() throws APIManagementException, RegistryException, UserStoreException {
        APIIdentifier apiId = new APIIdentifier("admin", "API1", "1.0.0");
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

        Mockito.when(apimgtDAO.getAllAPIUsageByProvider(apiId.getProviderName())).thenReturn(apiResults);

        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);

        List<SubscribedAPI> subscribedAPIs = apiProvider.getAPIUsageByAPIId(apiId);

        Assert.assertEquals(2, subscribedAPIs.size());
        Assert.assertEquals("user1", subscribedAPIs.get(0).getSubscriber().getName());
        Assert.assertEquals("user2", subscribedAPIs.get(1).getSubscriber().getName());
    }

    @Test
    public void testIsAPIUpdateValid() throws RegistryException, UserStoreException, APIManagementException {
        API api = new API(new APIIdentifier("admin", "API1", "1.0.0"));
        api.setContext("/test");
        api.setStatus(APIConstants.CREATED);
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);


        Resource apiSourceArtifact = Mockito.mock(Resource.class);
        Mockito.when(apiSourceArtifact.getUUID()).thenReturn("12640983654");
        String apiSourcePath = "path";

        PowerMockito.when(APIUtil.getAPIPath(api.getId())).thenReturn(apiSourcePath);
        PowerMockito.when(apiProvider.registry.get(apiSourcePath)).thenReturn(apiSourceArtifact);

        //API Status is CREATED and user has permission
        Mockito.when(artifact.getAttribute(APIConstants.API_OVERVIEW_STATUS)).thenReturn("CREATED");
        Mockito.when(artifactManager.getGenericArtifact(apiSourceArtifact.getUUID())).thenReturn(artifact);

        PowerMockito.when(APIUtil.hasPermission(null, APIConstants.Permissions.API_PUBLISH)).thenReturn(true);
        PowerMockito.when(APIUtil.hasPermission(null, APIConstants.Permissions.API_CREATE)).thenReturn(true);

        boolean status = apiProvider.isAPIUpdateValid(api);
        Assert.assertTrue(status);

        //API Status is CREATED and user doesn't have permission
        PowerMockito.when(APIUtil.hasPermission(null, APIConstants.Permissions.API_PUBLISH)).thenReturn(false);
        PowerMockito.when(APIUtil.hasPermission(null, APIConstants.Permissions.API_CREATE)).thenReturn(false);

        status = apiProvider.isAPIUpdateValid(api);
        Assert.assertFalse(status);

        //API Status is PROTOTYPED and user has permission
        api.setStatus(APIConstants.PROTOTYPED);
        Mockito.when(artifact.getAttribute(APIConstants.API_OVERVIEW_STATUS)).thenReturn("PROTOTYPED");
        //Mockito.when(artifactManager.getGenericArtifact(apiSourceArtifact.getUUID())).thenReturn(artifact);

        PowerMockito.when(APIUtil.hasPermission(null, APIConstants.Permissions.API_PUBLISH)).thenReturn(true);
        PowerMockito.when(APIUtil.hasPermission(null, APIConstants.Permissions.API_CREATE)).thenReturn(true);

        status = apiProvider.isAPIUpdateValid(api);
        Assert.assertTrue(status);

        //API Status is PROTOTYPED and user doesn't have permission
        api.setStatus(APIConstants.PROTOTYPED);
        Mockito.when(artifact.getAttribute(APIConstants.API_OVERVIEW_STATUS)).thenReturn("PROTOTYPED");
        //Mockito.when(artifactManager.getGenericArtifact(apiSourceArtifact.getUUID())).thenReturn(artifact);

        PowerMockito.when(APIUtil.hasPermission(null, APIConstants.Permissions.API_PUBLISH)).thenReturn(false);
        PowerMockito.when(APIUtil.hasPermission(null, APIConstants.Permissions.API_CREATE)).thenReturn(false);

        status = apiProvider.isAPIUpdateValid(api);
        Assert.assertFalse(status);

        //API Status is DEPRECATED and has publish permission
        api.setStatus(APIConstants.DEPRECATED);
        Mockito.when(artifact.getAttribute(APIConstants.API_OVERVIEW_STATUS)).thenReturn("DEPRECATED");
        //Mockito.when(artifactManager.getGenericArtifact(apiSourceArtifact.getUUID())).thenReturn(artifact);
        PowerMockito.when(APIUtil.hasPermission(null, APIConstants.Permissions.API_PUBLISH)).thenReturn(true);
        status = apiProvider.isAPIUpdateValid(api);
        Assert.assertTrue(status);

        //API Status is DEPRECATED and doesn't have publish permission
        api.setStatus(APIConstants.DEPRECATED);
        Mockito.when(artifact.getAttribute(APIConstants.API_OVERVIEW_STATUS)).thenReturn("DEPRECATED");
        //Mockito.when(artifactManager.getGenericArtifact(apiSourceArtifact.getUUID())).thenReturn(artifact);
        PowerMockito.when(APIUtil.hasPermission(null, APIConstants.Permissions.API_PUBLISH)).thenReturn(false);
        status = apiProvider.isAPIUpdateValid(api);
        Assert.assertFalse(status);

        //API Status is RETIRED and has publish permission
        api.setStatus(APIConstants.RETIRED);
        Mockito.when(artifact.getAttribute(APIConstants.API_OVERVIEW_STATUS)).thenReturn("RETIRED");
        //Mockito.when(artifactManager.getGenericArtifact(apiSourceArtifact.getUUID())).thenReturn(artifact);
        PowerMockito.when(APIUtil.hasPermission(null, APIConstants.Permissions.API_PUBLISH)).thenReturn(true);
        status = apiProvider.isAPIUpdateValid(api);
        Assert.assertTrue(status);

        //API Status is RETIRED and doesn't have publish permission
        api.setStatus(APIConstants.RETIRED);
        Mockito.when(artifact.getAttribute(APIConstants.API_OVERVIEW_STATUS)).thenReturn("RETIRED");
        //Mockito.when(artifactManager.getGenericArtifact(apiSourceArtifact.getUUID())).thenReturn(artifact);
        PowerMockito.when(APIUtil.hasPermission(null, APIConstants.Permissions.API_PUBLISH)).thenReturn(false);
        status = apiProvider.isAPIUpdateValid(api);
        Assert.assertFalse(status);
    }

    @Test
    public void testPropergateAPIStatusChangeToGateways() throws RegistryException, UserStoreException,
            APIManagementException {
        APIIdentifier apiId = new APIIdentifier("admin", "API1", "1.0.0");
        API api = new API(apiId);
        api.setContext("/test");
        api.setStatus(APIConstants.CREATED);
        TestUtils.mockRegistryAndUserRealm(-1);

        PowerMockito.mockStatic(ApiMgtDAO.class);
        PowerMockito.mockStatic(JavaUtils.class);
        ApiMgtDAO apimgtDAO = Mockito.mock(ApiMgtDAO.class);
        UserRegistry configRegistry = Mockito.mock(UserRegistry.class);
        RegistryService registryService = Mockito.mock(RegistryService.class);
        PowerMockito.when(ApiMgtDAO.getInstance()).thenReturn(apimgtDAO);
        Mockito.when(apimgtDAO.addAPI(api, -1)).thenReturn(1);
        Mockito.doNothing().when(apimgtDAO).addURITemplates(1, api, -1);
        Mockito.doNothing().when(keyManager).attachResourceScopes(api, api.getUriTemplates());

        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.when(APIUtil.replaceEmailDomain(apiId.getProviderName())).thenReturn("admin");
        PowerMockito.when(APIUtil.replaceEmailDomainBack(api.getId().getProviderName())).thenReturn("admin");

        Map<String, Map<String, String>> failedGateways = new ConcurrentHashMap<String, Map<String, String>>();

        PowerMockito.when(APIUtil.getArtifactManager(any(Registry.class), Matchers.anyString()))
                .thenReturn(artifactManager);
        Mockito.when(artifactManager.newGovernanceArtifact(any(QName.class))).thenReturn(artifact);
        Mockito.when(APIUtil.createAPIArtifactContent(artifact, api)).thenReturn(artifact);
        Mockito.doNothing().when(artifact).attachLifecycle(APIConstants.API_LIFE_CYCLE);
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        String artifactPath = "artifact/path";
        PowerMockito.when(GovernanceUtils.getArtifactPath(apiProvider.registry, artifact.getId())).
                thenReturn(artifactPath);
        RegistryService rs = Mockito.mock(RegistryService.class);
        UserRegistry userRegistry = Mockito.mock(UserRegistry.class);
        ServiceReferenceHolder srh = TestUtils.getServiceReferenceHolder();
        RealmService realmService = Mockito.mock(RealmService.class);
        TenantManager tenantManager = Mockito.mock(TenantManager.class);

        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(srh);
        Mockito.when(srh.getRegistryService()).thenReturn(rs);
        Mockito.when(rs.getConfigSystemRegistry(Mockito.anyInt())).thenReturn(userRegistry);
        Mockito.when(srh.getRealmService()).thenReturn(realmService);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        apiProvider.addAPI(api);

        //No state changes
        Map<String, String> failedGatewaysReturned = apiProvider.propergateAPIStatusChangeToGateways(apiId,
                APIConstants.CREATED);
        Assert.assertEquals(0, failedGatewaysReturned.size());
        Assert.assertEquals(APIConstants.CREATED, api.getStatus());

        ServiceReferenceHolder serviceReferenceHolder = TestUtils.mockAPIMConfiguration(APIConstants.API_GATEWAY_TYPE,
                APIConstants.API_GATEWAY_TYPE_SYNAPSE, -1);
        PowerMockito.when(apimgtDAO.getPublishedDefaultVersion(api.getId())).thenReturn("1.0.0");

        //Change to PUBLISHED state
        //Existing APIs of the provider
        API api1 = new API(new APIIdentifier("admin", "API1", "0.0.5"));
        api1.setStatus(APIConstants.PUBLISHED);
        API api2 = new API(new APIIdentifier("admin", "API2", "1.0.0"));

        prepareForGetAPIsByProvider(artifactManager, apiProvider, "admin", api1, api2);
        //  artifactManager, apiProvider, "admin", api1, api2
        PowerMockito.when(serviceReferenceHolder.getRegistryService()).thenReturn(registryService);
        PowerMockito.when(registryService.getConfigSystemRegistry(-1)).thenReturn(configRegistry);
        PowerMockito.when(configRegistry.resourceExists(APIConstants.API_TENANT_CONF_LOCATION)).thenReturn(false);
        PowerMockito.when(JavaUtils.isTrueExplicitly("false")).thenReturn(false);

        UserRegistry registry = Mockito.mock(UserRegistry.class);
        PowerMockito.when(registryService.getGovernanceUserRegistry(CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME, -1234))
                .thenReturn(registry);

        APIManagerConfiguration amConfig = Mockito.mock(APIManagerConfiguration.class);
        APIManagerConfigurationService amConfigService = Mockito.mock(APIManagerConfigurationService.class);
        PowerMockito.when(serviceReferenceHolder.getAPIManagerConfigurationService()).thenReturn(amConfigService);
        PowerMockito.when(amConfigService.getAPIManagerConfiguration()).thenReturn(amConfig);
        Mockito.when(amConfig.getFirstProperty(APIConstants.API_GATEWAY_TYPE)).
                thenReturn(APIConstants.API_GATEWAY_TYPE_SYNAPSE);

        TenantManager tm = Mockito.mock(TenantManager.class);
        PowerMockito.when(serviceReferenceHolder.getRealmService()).thenReturn(realmService);
        PowerMockito.when(realmService.getTenantManager()).thenReturn(tm);
        PowerMockito.when(tm.getTenantId(Matchers.anyString())).thenReturn(-1234);
        failedGatewaysReturned = apiProvider.propergateAPIStatusChangeToGateways(apiId, APIConstants.PUBLISHED);

        Assert.assertEquals(0, failedGatewaysReturned.size());
        Assert.assertEquals(APIConstants.PUBLISHED, api.getStatus());

        //Change to PUBLISHED state and error thrown while publishing
        api.setStatus(APIConstants.CREATED);
        Map<String, String> failedGWEnv = new HashMap<String, String>();
        failedGWEnv.put("Production", "Failed to publish");
        failedGateways.put("PUBLISHED", failedGWEnv);

        Mockito.when(gatewayManager.publishToGateway(any(API.class), any(APITemplateBuilder.class),
                Matchers.anyString())).thenReturn(failedGWEnv);
        failedGatewaysReturned = apiProvider.propergateAPIStatusChangeToGateways(apiId, APIConstants.PUBLISHED);
        Assert.assertEquals(1, failedGatewaysReturned.size());
        Assert.assertEquals(APIConstants.PUBLISHED, api.getStatus());

        //Change to RETIRED state
        api.setStatus(APIConstants.CREATED);
        failedGateways.remove("PUBLISHED");

        failedGatewaysReturned = apiProvider.propergateAPIStatusChangeToGateways(apiId, APIConstants.RETIRED);
        Assert.assertEquals(0, failedGatewaysReturned.size());
        Assert.assertEquals(APIConstants.RETIRED, api.getStatus());

        //Change to RETIRED state and error thrown while un-publishing
        api.setStatus(APIConstants.CREATED);
        failedGateways.put("UNPUBLISHED", failedGWEnv);
        Mockito.when(gatewayManager.removeFromGateway(any(API.class),
                Matchers.anyString())).thenReturn(failedGWEnv);

        failedGatewaysReturned = apiProvider.propergateAPIStatusChangeToGateways(apiId, APIConstants.RETIRED);
        Assert.assertEquals(1, failedGatewaysReturned.size());
        Assert.assertEquals(APIConstants.RETIRED, api.getStatus());
    }

    @Test
    public void testEmailSentWhenPropergateAPIStatusChangeToGateways() throws Exception {
        APIIdentifier apiId = new APIIdentifier("admin", "API1", "1.0.0");
        API api = new API(apiId);
        api.setContext("/test");
        api.setStatus(APIConstants.CREATED);

        TestUtils.mockRegistryAndUserRealm(-1);
        Resource resource = PowerMockito.mock(Resource.class);
        JSONObject tenantConfig = PowerMockito.mock(JSONObject.class);
        NotificationExecutor notificationExecutor = PowerMockito.mock(NotificationExecutor.class);
        NotificationDTO notificationDTO = PowerMockito.mock(NotificationDTO.class);
        UserRegistry configRegistry = PowerMockito.mock(UserRegistry.class);
        RegistryService registryService = PowerMockito.mock(RegistryService.class);
        Mockito.when(apimgtDAO.addAPI(api, -1)).thenReturn(1);
        Mockito.doNothing().when(apimgtDAO).addURITemplates(1, api, -1);
        Mockito.doNothing().when(keyManager).attachResourceScopes(api, api.getUriTemplates());
        Mockito.when(artifactManager.newGovernanceArtifact(Matchers.any(QName.class))).thenReturn(artifact);
        Mockito.when(APIUtil.createAPIArtifactContent(artifact, api)).thenReturn(artifact);
        Map<String, String> failedToPubGWEnv = new HashMap<String, String>();
        Mockito.when(gatewayManager.publishToGateway(Matchers.any(API.class), Matchers.any(APITemplateBuilder.class),
                Matchers.anyString())).thenReturn(failedToPubGWEnv);

        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        UserRegistry userRegistry = Mockito.mock(UserRegistry.class);
        ServiceReferenceHolder serviceReferenceHolder = TestUtils.getServiceReferenceHolder();
        RealmService realmService = Mockito.mock(RealmService.class);
        TenantManager tenantManager = Mockito.mock(TenantManager.class);

        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getRegistryService()).thenReturn(registryService);
        Mockito.when(registryService.getConfigSystemRegistry(Mockito.anyInt())).thenReturn(userRegistry);
        Mockito.when(serviceReferenceHolder.getRealmService()).thenReturn(realmService);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        apiProvider.addAPI(api);

        PowerMockito.when(APIUtil.replaceEmailDomain(apiId.getProviderName())).thenReturn("admin");
        PowerMockito.when(APIUtil.replaceEmailDomainBack(api.getId().getProviderName())).thenReturn("admin");
        PowerMockito.when(apimgtDAO.getPublishedDefaultVersion(api.getId())).thenReturn("1.0.0");

        //Change to PUBLISHED state
        //Existing APIs of the provider
        API api1 = new API(new APIIdentifier("admin", "API1", "0.0.5"));
        api1.setStatus(APIConstants.PUBLISHED);
        API api2 = new API(new APIIdentifier("admin", "API2", "1.0.0"));

        prepareForGetAPIsByProvider(artifactManager, apiProvider, "admin", api1, api2);
        PowerMockito.when(registryService.getConfigSystemRegistry(-1)).thenReturn(configRegistry);
        PowerMockito.when(configRegistry.resourceExists(APIConstants.API_TENANT_CONF_LOCATION)).thenReturn(true);
        PowerMockito.when(configRegistry.get(APIConstants.API_TENANT_CONF_LOCATION)).thenReturn(resource);
        Mockito.when(resource.getContent()).thenReturn(getTenantConfigContent());
        PowerMockito.when(tenantConfig.get(NotifierConstants.NOTIFICATIONS_ENABLED)).thenReturn("true");
        PowerMockito.when(tenantConfig.get(APIConstants.EXTENSION_HANDLER_POSITION)).thenReturn("bottom");
        PowerMockito.whenNew(NotificationDTO.class).withAnyArguments().thenReturn(notificationDTO);
        PowerMockito.whenNew(NotificationExecutor.class).withAnyArguments().thenReturn(notificationExecutor);

        APIManagerConfigurationService amConfigService = Mockito.mock(APIManagerConfigurationService.class);
        APIManagerConfiguration amConfig = Mockito.mock(APIManagerConfiguration.class);
        PowerMockito.when(serviceReferenceHolder.getAPIManagerConfigurationService()).thenReturn(amConfigService);
        PowerMockito.when(amConfigService.getAPIManagerConfiguration()).thenReturn(amConfig);
        PowerMockito.when(amConfig.getFirstProperty(APIConstants.API_GATEWAY_TYPE)).thenReturn(APIConstants.API_GATEWAY_TYPE_SYNAPSE);
        UserRegistry registry = Mockito.mock(UserRegistry.class);
        PowerMockito.when(registryService.getGovernanceUserRegistry(CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME, -1234))
                .thenReturn(registry);


        PowerMockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        PowerMockito.when(tenantManager.getTenantId(Matchers.anyString())).thenReturn(-1234);

        apiProvider.propergateAPIStatusChangeToGateways(apiId, APIConstants.PUBLISHED);
        Mockito.verify(notificationExecutor).sendAsyncNotifications(notificationDTO);
    }

    @Test
    public void testPropergateAPIStatusChangeToGateways_InvalidAPIID() throws RegistryException, UserStoreException,
            APIManagementException {
        APIIdentifier apiId = new APIIdentifier("admin", "API1", "1.0.0");
        API api = new API(apiId);
        api.setContext("/test");
        api.setStatus(APIConstants.CREATED);


        CORSConfiguration corsConfig = getCORSConfiguration();
        api.setCorsConfiguration(corsConfig);

        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);

        try {
            apiProvider.propergateAPIStatusChangeToGateways(apiId,
                    APIConstants.CREATED);
        } catch(APIManagementException e) {
            Assert.assertEquals("Couldn't find an API with the name-" + apiId.getApiName() + "version-"
                    + apiId.getVersion(), e.getMessage());
        }
    }

    @Test
    public void testCreateNewAPIVersion() throws Exception {
        //Create Original API
        APIIdentifier apiId = new APIIdentifier("admin", "API1", "1.0.0");
        API api = new API(apiId);
        api.setContext("/test");
        api.setVisibility("Public");
        api.setStatus(APIConstants.CREATED);
        api.setWsdlUrl("https://localhost:9443/services/echo?wsdl");

        String newVersion = "1.0.1";
        //Create new API object
        APIIdentifier newApiId = new APIIdentifier("admin", "API1", "1.0.1");
        final API newApi = new API(newApiId);
        newApi.setStatus(APIConstants.CREATED);
        newApi.setContext("/test");
        newApi.setWsdlUrl("/registry/resource/_system/governance/apimgt/applicationdata/wsdls/admin--API11.0.0.wsdl");


        //Create Documentation List
        List<Documentation> documentationList = getDocumentationList();

        final APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, documentationList, null);
        RegistryService registryService = Mockito.mock(RegistryService.class);
        UserRegistry userRegistry = Mockito.mock(UserRegistry.class);
        ServiceReferenceHolder serviceReferenceHolder = TestUtils.getServiceReferenceHolder();
        RealmService realmService = Mockito.mock(RealmService.class);
        TenantManager tenantManager = Mockito.mock(TenantManager.class);

        Mockito.when(artifactManager.newGovernanceArtifact(any(QName.class))).thenReturn(artifact);
        Mockito.when(APIUtil.createAPIArtifactContent(artifact, api)).thenReturn(artifact);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getRegistryService()).thenReturn(registryService);
        Mockito.when(registryService.getConfigSystemRegistry(Mockito.anyInt())).thenReturn(userRegistry);
        Mockito.when(serviceReferenceHolder.getRealmService()).thenReturn(realmService);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);

        GenericArtifact artifactNew = Mockito.mock(GenericArtifact.class);
        Mockito.when(APIUtil.createAPIArtifactContent(artifact, newApi)).thenReturn(artifactNew);
        apiProvider.addAPI(api);

        String targetPath = APIConstants.API_LOCATION + RegistryConstants.PATH_SEPARATOR +
                api.getId().getProviderName() +
                RegistryConstants.PATH_SEPARATOR + api.getId().getApiName() +
                RegistryConstants.PATH_SEPARATOR + newVersion +
                APIConstants.API_RESOURCE_NAME;

        String apiSourcePath = APIConstants.API_LOCATION + RegistryConstants.PATH_SEPARATOR +
                apiId.getProviderName() +
                RegistryConstants.PATH_SEPARATOR + apiId.getApiName() +
                RegistryConstants.PATH_SEPARATOR + apiId.getVersion() +
                APIConstants.API_RESOURCE_NAME;
        PowerMockito.when(APIUtil.getAPIPath(apiId)).thenReturn(apiSourcePath);
        String apiSourceUUID = "87ty543-899hyt";

        Mockito.when(apiProvider.registry.resourceExists(targetPath)).thenReturn(false);
        Mockito.doNothing().when(apiProvider.registry).beginTransaction();
        Mockito.doNothing().when(apiProvider.registry).commitTransaction();

        Resource apiSourceArtifact = Mockito.mock(Resource.class);
        Mockito.when(apiProvider.registry.get(apiSourcePath)).thenReturn(apiSourceArtifact);


        //Mocking Old API retrieval
        Mockito.when(apiSourceArtifact.getUUID()).thenReturn(apiSourceUUID);

        Mockito.when(artifact.getAttribute(APIConstants.API_OVERVIEW_STATUS)).thenReturn("PUBLISHED");
        Mockito.when(artifact.getAttribute(APIConstants.API_OVERVIEW_CONTEXT)).thenReturn("test");
        Mockito.when(artifact.getAttribute(APIConstants.API_OVERVIEW_CONTEXT_TEMPLATE)).thenReturn("test/{version}");
        Mockito.when(artifact.getAttribute(APIConstants.API_OVERVIEW_WEBSOCKET)).thenReturn("false");
        Mockito.when(artifact.getAttribute(APIConstants.API_OVERVIEW_VISIBLE_ROLES)).thenReturn("admin, subscriber");
        Mockito.when(artifactManager.getGenericArtifact(apiSourceUUID)).thenReturn(artifact);

        //Mocking thumbnail
        String thumbUrl = APIConstants.API_IMAGE_LOCATION + RegistryConstants.PATH_SEPARATOR +
                api.getId().getProviderName() + RegistryConstants.PATH_SEPARATOR +
                api.getId().getApiName() + RegistryConstants.PATH_SEPARATOR +
                api.getId().getVersion() + RegistryConstants.PATH_SEPARATOR + APIConstants.API_ICON_IMAGE;
        Resource image = Mockito.mock(Resource.class);
        Mockito.when(apiProvider.registry.get(thumbUrl)).thenReturn(image);
        Mockito.when(apiProvider.registry.resourceExists(thumbUrl)).thenReturn(true);

        //Mocking In sequence retrieval
        String inSeqFilePath = "API1/1.0.0/in";
        PowerMockito.when(APIUtil.getSequencePath(api.getId(), "in")).thenReturn(inSeqFilePath);
        Mockito.when(apiProvider.registry.resourceExists(inSeqFilePath)).thenReturn(true);
        Collection inSeqCollection = Mockito.mock(Collection.class);
        Mockito.when(apiProvider.registry.get(inSeqFilePath)).thenReturn(inSeqCollection);
        String[] inSeqChildPaths = {"path1"};
        Mockito.when(inSeqCollection.getChildren()).thenReturn(inSeqChildPaths);

        Mockito.when(apiProvider.registry.get(inSeqChildPaths[0])).thenReturn(apiSourceArtifact);
        InputStream responseStream = IOUtils.toInputStream("<sequence name=\"in-seq\"></sequence>", "UTF-8");
        OMElement seqElment = buildOMElement(responseStream);
        PowerMockito.when(APIUtil.buildOMElement(responseStream)).thenReturn(seqElment);
        Mockito.when(apiSourceArtifact.getContentStream()).thenReturn(responseStream);

        //Mocking Out sequence retrieval
        Resource apiSourceArtifact1 = Mockito.mock(Resource.class);
        String outSeqFilePath = "API1/1.0.0/out";
        PowerMockito.when(APIUtil.getSequencePath(api.getId(), "out")).thenReturn(outSeqFilePath);
        Mockito.when(apiProvider.registry.resourceExists(outSeqFilePath)).thenReturn(true);
        Collection outSeqCollection = Mockito.mock(Collection.class);
        Mockito.when(apiProvider.registry.get(outSeqFilePath)).thenReturn(outSeqCollection);
        String[] outSeqChildPaths = {"path2"};
        Mockito.when(outSeqCollection.getChildren()).thenReturn(outSeqChildPaths);

        Mockito.when(apiProvider.registry.get(outSeqChildPaths[0])).thenReturn(apiSourceArtifact1);
        InputStream responseStream2 = IOUtils.toInputStream("<sequence name=\"in-seq\"></sequence>", "UTF-8");
        OMElement seqElment2 = buildOMElement(responseStream2);
        PowerMockito.when(APIUtil.buildOMElement(responseStream2)).thenReturn(seqElment2);
        Mockito.when(apiSourceArtifact1.getContentStream()).thenReturn(responseStream2);

        //Mock Adding new API artifact with new version
        Mockito.doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                apiProvider.createAPI(newApi);
                return null;
            }
        }).when(artifactManager).addGenericArtifact(artifact);
        Mockito.doNothing().when(artifact).attachLifecycle(APIConstants.API_LIFE_CYCLE);
        PowerMockito.when(APIUtil.getAPIProviderPath(api.getId())).thenReturn("/dummy/provider/path");
        Mockito.doNothing().when(apiProvider.registry).addAssociation("/dummy/provider/path",
                targetPath, APIConstants.PROVIDER_ASSOCIATION);

        String artifactPath = "artifact/path";
        PowerMockito.when(GovernanceUtils.getArtifactPath(apiProvider.registry, artifact.getId())).
                thenReturn(artifactPath);
        PowerMockito.doNothing().when(APIUtil.class);
        String[] roles = {"admin", "subscriber"};
        APIUtil.setResourcePermissions("admin", "Public", roles, artifactPath);

        //Mock no tags case
        Mockito.when(apiProvider.registry.getTags(apiSourcePath)).thenReturn(null);

        // Mock WSDL retrieval
        String wsdlUrl = APIUtil.getWSDLDefinitionFilePath(api.getId().getApiName(), api.getId().getVersion(), api
                .getId().getProviderName());
        PowerMockito.when(apiProvider.registry.resourceExists(wsdlUrl)).thenReturn(true);

        //Mock new API retrieval
        String newApiPath = "API1/1.0.1/";
        PowerMockito.when(APIUtil.getAPIPath(newApi.getId())).thenReturn(newApiPath);
        String newApiUUID = "87ty543-899hy23";
        GenericArtifact newArtifact = Mockito.mock(GenericArtifact.class);
        Resource newApiResource = Mockito.mock(Resource.class);
        Mockito.when(newApiResource.getUUID()).thenReturn(newApiUUID);
        Mockito.when(apiProvider.registry.get(newApiPath)).thenReturn(newApiResource);
        Mockito.when(artifactManager.getGenericArtifact(newApiUUID)).thenReturn(newArtifact);
        PowerMockito.when(APIUtil.getAPI(newArtifact, apiProvider.registry, api.getId(), "test")).thenReturn(newApi);

        //Swagger resource
        String resourcePath = APIUtil.getOpenAPIDefinitionFilePath(api.getId().getApiName(),
                api.getId().getVersion(),
                api.getId().getProviderName());
        Mockito.when(apiProvider.registry.resourceExists(resourcePath + APIConstants.API_OAS_DEFINITION_RESOURCE_NAME)).
                thenReturn(true);
        PowerMockito.mockStatic(OASParserUtil.class);
        Mockito.when(OASParserUtil.getAPIDefinition(apiId, apiProvider.registry)).thenReturn(
                "{\"info\": {\"swagger\":\"data\"}}");
        Mockito.doNothing().when(artifactManager).updateGenericArtifact(artifact);

        // WSDL
        String newWsdlResourcePath = APIUtil.getWSDLDefinitionFilePath(newApi.getId().getApiName(), newApi
                .getId().getVersion(), newApi.getId().getProviderName());
        PowerMockito.when(apiProvider.registry.copy(resourcePath, newWsdlResourcePath)).thenReturn(newWsdlResourcePath);

        //Mock Config system registry
        PowerMockito.when(tenantManager.getTenantId(Matchers.anyString())).thenReturn(-1234);

        AuthorizationManager authManager = Mockito.mock(AuthorizationManager.class);
        UserRealm userRealm = Mockito.mock(UserRealm.class);
        PowerMockito.when(realmService.getTenantUserRealm(-1234)).thenReturn(userRealm);
        PowerMockito.when(userRealm.getAuthorizationManager()).thenReturn(authManager);

        PowerMockito.when(registryService.getConfigSystemRegistry(-1234)).thenReturn(userRegistry);
        Mockito.when(userRegistry.resourceExists(APIConstants.API_TENANT_CONF_LOCATION)).thenReturn(true);
        Resource tenantConfResource = Mockito.mock(Resource.class);
        Mockito.when(userRegistry.get(APIConstants.API_TENANT_CONF_LOCATION)).thenReturn(tenantConfResource);
        Mockito.when(tenantConfResource.getContent()).thenReturn(getTenantConfigContent());

        apiProvider.createNewAPIVersion(api, newVersion);
        Assert.assertEquals(newVersion, apiProvider.getAPI(newApi.getId()).getId().getVersion());
        Assert.assertEquals(newApi.getWsdlUrl(), apiProvider.getAPI(newApi.getId()).getWsdlUrl());
    }

    @Test
    public void testCreateNewAPIVersion_ForDefaultVersion() throws Exception {
        //Create Original API
        APIIdentifier apiId = new APIIdentifier("admin", "API1", "1.0.0");
        API api = new API(apiId);
        api.setContext("/test");
        api.setVisibility("Public");
        api.setStatus(APIConstants.CREATED);
        api.setAsDefaultVersion(true);

        String newVersion = "1.0.1";
        //Create new API object
        APIIdentifier newApiId = new APIIdentifier("admin", "API1", "1.0.1");
        final API newApi = new API(newApiId);
        newApi.setStatus(APIConstants.CREATED);
        newApi.setContext("/test");

        //Mock API as a default version
        Mockito.when(apimgtDAO.getDefaultVersion(apiId)).thenReturn("1.0.0");


        final APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, new ArrayList<Documentation>(), null);

        Mockito.when(artifactManager.newGovernanceArtifact(any(QName.class))).thenReturn(artifact);
        Mockito.when(APIUtil.createAPIArtifactContent(artifact, api)).thenReturn(artifact);

        GenericArtifact artifactNew = Mockito.mock(GenericArtifact.class);
        Mockito.when(APIUtil.createAPIArtifactContent(artifact, newApi)).thenReturn(artifactNew);
        RegistryService rs = Mockito.mock(RegistryService.class);
        UserRegistry ur = Mockito.mock(UserRegistry.class);
        ServiceReferenceHolder serviceReferenceHolder = TestUtils.getServiceReferenceHolder();
        RealmService realmService = Mockito.mock(RealmService.class);
        TenantManager tenantManager = Mockito.mock(TenantManager.class);

        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getRegistryService()).thenReturn(rs);
        Mockito.when(rs.getConfigSystemRegistry(Mockito.anyInt())).thenReturn(ur);
        Mockito.when(serviceReferenceHolder.getRealmService()).thenReturn(realmService);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        apiProvider.addAPI(api);

        String targetPath = APIConstants.API_LOCATION + RegistryConstants.PATH_SEPARATOR +
                api.getId().getProviderName() +
                RegistryConstants.PATH_SEPARATOR + api.getId().getApiName() +
                RegistryConstants.PATH_SEPARATOR + newVersion +
                APIConstants.API_RESOURCE_NAME;

        String apiSourcePath = APIConstants.API_LOCATION + RegistryConstants.PATH_SEPARATOR +
                apiId.getProviderName() +
                RegistryConstants.PATH_SEPARATOR + apiId.getApiName() +
                RegistryConstants.PATH_SEPARATOR + apiId.getVersion() +
                APIConstants.API_RESOURCE_NAME;
        PowerMockito.when(APIUtil.getAPIPath(apiId)).thenReturn(apiSourcePath);
        String apiSourceUUID = "87ty543-899hyt";

        Mockito.when(apiProvider.registry.resourceExists(targetPath)).thenReturn(false);

        Resource apiSourceArtifact = Mockito.mock(Resource.class);
        Mockito.when(apiProvider.registry.get(apiSourcePath)).thenReturn(apiSourceArtifact);

        //Mock API as a default version
        Mockito.when(apimgtDAO.getDefaultVersion(apiId)).thenReturn("1.0.0");

        Mockito.when(apiSourceArtifact.getUUID()).thenReturn(apiSourceUUID);

        Mockito.doNothing().when(artifactManager).updateGenericArtifact(artifact);

        //Mocking Old API retrieval
        Mockito.when(artifact.getAttribute(APIConstants.API_OVERVIEW_STATUS)).thenReturn("PUBLISHED");
        Mockito.when(artifact.getAttribute(APIConstants.API_OVERVIEW_CONTEXT)).thenReturn("test");
        Mockito.when(artifact.getAttribute(APIConstants.API_OVERVIEW_CONTEXT_TEMPLATE)).thenReturn("test/{version}");
        Mockito.when(artifact.getAttribute(APIConstants.API_OVERVIEW_WEBSOCKET)).thenReturn("false");
        Mockito.when(artifact.getAttribute(APIConstants.API_OVERVIEW_VISIBLE_ROLES)).thenReturn("admin, subscriber");
        Mockito.when(artifactManager.getGenericArtifact(apiSourceUUID)).thenReturn(artifact);

        //Mocking no thumbnail case
        String thumbUrl = APIConstants.API_IMAGE_LOCATION + RegistryConstants.PATH_SEPARATOR +
                api.getId().getProviderName() + RegistryConstants.PATH_SEPARATOR +
                api.getId().getApiName() + RegistryConstants.PATH_SEPARATOR +
                api.getId().getVersion() + RegistryConstants.PATH_SEPARATOR + APIConstants.API_ICON_IMAGE;
        Mockito.when(apiProvider.registry.resourceExists(thumbUrl)).thenReturn(false);

        //Mocking In sequence retrieval
        String inSeqFilePath = "API1/1.0.0/in";
        PowerMockito.when(APIUtil.getSequencePath(api.getId(), "in")).thenReturn(inSeqFilePath);
        Mockito.when(apiProvider.registry.resourceExists(inSeqFilePath)).thenReturn(false);


        //Mocking Out sequence retrieval
        String outSeqFilePath = "API1/1.0.0/out";
        PowerMockito.when(APIUtil.getSequencePath(api.getId(), "out")).thenReturn(outSeqFilePath);
        Mockito.when(apiProvider.registry.resourceExists(outSeqFilePath)).thenReturn(false);

        //Mock Adding new API artifact with new version
        Mockito.doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                apiProvider.createAPI(newApi);
                return null;
            }
        }).when(artifactManager).addGenericArtifact(artifact);
        Mockito.doNothing().when(artifact).attachLifecycle(APIConstants.API_LIFE_CYCLE);
        PowerMockito.when(APIUtil.getAPIProviderPath(api.getId())).thenReturn("/dummy/provider/path");
        Mockito.doNothing().when(apiProvider.registry).addAssociation("/dummy/provider/path",
                targetPath, APIConstants.PROVIDER_ASSOCIATION);

        String artifactPath = "artifact/path";
        PowerMockito.when(GovernanceUtils.getArtifactPath(apiProvider.registry, artifact.getId())).
                thenReturn(artifactPath);
        PowerMockito.doNothing().when(APIUtil.class);
        String[] roles = {"admin", "subscriber"};
        APIUtil.setResourcePermissions("admin", "Public", roles, artifactPath);

        //Mock no tags case
        Mockito.when(apiProvider.registry.getTags(apiSourcePath)).thenReturn(null);

        //Mock new API retrieval
        String newApiPath = "API1/1.0.1/";
        PowerMockito.when(APIUtil.getAPIPath(newApi.getId())).thenReturn(newApiPath);
        String newApiUUID = "87ty543-899hy23";
        GenericArtifact newArtifact = Mockito.mock(GenericArtifact.class);
        Resource newApiResource = Mockito.mock(Resource.class);
        Mockito.when(newApiResource.getUUID()).thenReturn(newApiUUID);
        Mockito.when(apiProvider.registry.get(newApiPath)).thenReturn(newApiResource);
        Mockito.when(artifactManager.getGenericArtifact(newApiUUID)).thenReturn(newArtifact);
        PowerMockito.when(APIUtil.getAPI(newArtifact, apiProvider.registry, api.getId(), "test")).thenReturn(newApi);

        String resourcePath = APIUtil.getOpenAPIDefinitionFilePath(api.getId().getApiName(),
                api.getId().getVersion(),
                api.getId().getProviderName());

        Mockito.when(apiProvider.registry.resourceExists(resourcePath + APIConstants.API_OAS_DEFINITION_RESOURCE_NAME)).
                thenReturn(false);

        //Mock Config system registry
        ServiceReferenceHolder sh = TestUtils.getServiceReferenceHolder();
        RegistryService registryService = Mockito.mock(RegistryService.class);
        PowerMockito.when(sh.getRegistryService()).thenReturn(registryService);
        UserRegistry systemReg = Mockito.mock(UserRegistry.class);
        PowerMockito.when(registryService.getConfigSystemRegistry(-1234)).thenReturn(systemReg);
        Mockito.when(systemReg.resourceExists(APIConstants.API_TENANT_CONF_LOCATION)).thenReturn(false);

        apiProvider.createNewAPIVersion(api, newVersion);

        Assert.assertEquals(newVersion, apiProvider.getAPI(newApi.getId()).getId().getVersion());
    }

    @Test(expected = DuplicateAPIException.class)
    public void testCreateNewAPIVersion_DuplicateAPI() throws RegistryException, UserStoreException,
            APIManagementException, IOException, DuplicateAPIException {
        //Create Original API
        APIIdentifier apiId = new APIIdentifier("admin", "API1", "1.0.0");
        API api = new API(apiId);
        api.setContext("/test");
        api.setVisibility("Public");
        api.setStatus(APIConstants.CREATED);

        String newVersion = "1.0.0";
        //Create new API object
        APIIdentifier newApiId = new APIIdentifier("admin", "API1", "1.0.1");
        final API newApi = new API(newApiId);
        newApi.setStatus(APIConstants.CREATED);
        newApi.setContext("/test");

        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);

        Mockito.when(artifactManager.newGovernanceArtifact(any(QName.class))).thenReturn(artifact);
        Mockito.when(APIUtil.createAPIArtifactContent(artifact, api)).thenReturn(artifact);

        GenericArtifact artifactNew = Mockito.mock(GenericArtifact.class);
        Mockito.when(APIUtil.createAPIArtifactContent(artifact, newApi)).thenReturn(artifactNew);
        RegistryService registryService = Mockito.mock(RegistryService.class);
        UserRegistry userRegistry = Mockito.mock(UserRegistry.class);
        ServiceReferenceHolder serviceReferenceHolder = TestUtils.getServiceReferenceHolder();
        RealmService realmService = Mockito.mock(RealmService.class);
        TenantManager tenantManager = Mockito.mock(TenantManager.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getRegistryService()).thenReturn(registryService);
        Mockito.when(registryService.getConfigSystemRegistry(Mockito.anyInt())).thenReturn(userRegistry);
        Mockito.when(serviceReferenceHolder.getRealmService()).thenReturn(realmService);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        apiProvider.addAPI(api);

        String targetPath = APIConstants.API_LOCATION + RegistryConstants.PATH_SEPARATOR +
                api.getId().getProviderName() +
                RegistryConstants.PATH_SEPARATOR + api.getId().getApiName() +
                RegistryConstants.PATH_SEPARATOR + newVersion +
                APIConstants.API_RESOURCE_NAME;
        String apiSourcePath = "API1/1.0.0/";
        PowerMockito.when(APIUtil.getAPIPath(apiId)).thenReturn(apiSourcePath);
        Mockito.when(apiProvider.registry.resourceExists(targetPath)).thenReturn(true);
        apiProvider.createNewAPIVersion(api, newVersion);
    }

    @Test(expected = APIManagementException.class)
    public void testCreateNewAPIVersion_Exception() throws RegistryException, UserStoreException,
            APIManagementException, IOException, DuplicateAPIException {
        //Create Original API
        APIIdentifier apiId = new APIIdentifier("admin", "API1", "1.0.0");
        API api = new API(apiId);
        api.setContext("/test");
        api.setVisibility("Public");
        api.setStatus(APIConstants.CREATED);

        String newVersion = "1.0.0";
        //Create new API object
        APIIdentifier newApiId = new APIIdentifier("admin", "API1", "1.0.1");
        final API newApi = new API(newApiId);
        newApi.setStatus(APIConstants.CREATED);
        newApi.setContext("/test");

        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);

        Mockito.when(artifactManager.newGovernanceArtifact(any(QName.class))).thenReturn(artifact);
        Mockito.when(APIUtil.createAPIArtifactContent(artifact, api)).thenReturn(artifact);

        GenericArtifact artifactNew = Mockito.mock(GenericArtifact.class);
        Mockito.when(APIUtil.createAPIArtifactContent(artifact, newApi)).thenReturn(artifactNew);
        RegistryService registryService = Mockito.mock(RegistryService.class);
        UserRegistry userRegistry = Mockito.mock(UserRegistry.class);
        ServiceReferenceHolder serviceReferenceHolder = TestUtils.getServiceReferenceHolder();
        RealmService realmService = Mockito.mock(RealmService.class);
        TenantManager tenantManager = Mockito.mock(TenantManager.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getRegistryService()).thenReturn(registryService);
        Mockito.when(registryService.getConfigSystemRegistry(Mockito.anyInt())).thenReturn(userRegistry);
        Mockito.when(serviceReferenceHolder.getRealmService()).thenReturn(realmService);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        apiProvider.addAPI(api);

        String targetPath = APIConstants.API_LOCATION + RegistryConstants.PATH_SEPARATOR +
                api.getId().getProviderName() +
                RegistryConstants.PATH_SEPARATOR + api.getId().getApiName() +
                RegistryConstants.PATH_SEPARATOR + newVersion +
                APIConstants.API_RESOURCE_NAME;

        String apiSourcePath = "API1/1.0.0/";
        PowerMockito.when(APIUtil.getAPIPath(apiId)).thenReturn(apiSourcePath);
        Mockito.when(apiProvider.registry.resourceExists(targetPath)).thenThrow(RegistryException.class);
        apiProvider.createNewAPIVersion(api, newVersion);
    }

    @Test
    public void testGetAPIsByProvider() throws RegistryException, UserStoreException, APIManagementException {

        String providerId = "admin";
        API api1 = new API(new APIIdentifier("admin", "API1", "1.0.0"));
        API api2 = new API(new APIIdentifier("admin", "API2", "1.0.0"));

        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);

        prepareForGetAPIsByProvider(artifactManager, apiProvider, providerId, api1, api2);

        List<API> apiResponse = apiProvider.getAPIsByProvider(providerId);

        Assert.assertEquals(2, apiResponse.size());
        Assert.assertEquals("API1", apiResponse.get(0).getId().getApiName());

        Mockito.when(apiProvider.registry.getAspectActions(Matchers.anyString(),
                Matchers.anyString())).thenThrow(RegistryException.class);
        try {
            apiProvider.getAPIsByProvider(providerId);
        } catch (APIManagementException e) {
            Assert.assertEquals("Failed to get APIs for provider : " + providerId, e.getMessage());
        }
    }

    @Test
    public void testChangeLifeCycleStatus_WFAlreadyStarted() throws RegistryException, UserStoreException,
            APIManagementException, FaultGatewaysException, WorkflowException {

        APIIdentifier apiId = new APIIdentifier("admin", "API1", "1.0.0");

        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);

        GenericArtifact apiArtifact = Mockito.mock(GenericArtifact.class);
        Mockito.when(APIUtil.getAPIArtifact(apiId, apiProvider.registry)).thenReturn(apiArtifact);
        Mockito.when(apiArtifact.getAttribute(APIConstants.API_OVERVIEW_PROVIDER)).thenReturn("admin");
        Mockito.when(apiArtifact.getAttribute(APIConstants.API_OVERVIEW_NAME)).thenReturn("API1");
        Mockito.when(apiArtifact.getAttribute(APIConstants.API_OVERVIEW_VERSION)).thenReturn("1.0.0");
        Mockito.when(apiArtifact.getLifecycleState()).thenReturn("CREATED");

        Mockito.when(apimgtDAO.getAPIID(apiId, null)).thenReturn(1);

        //Workflow has started already
        WorkflowDTO wfDTO = Mockito.mock(WorkflowDTO.class);
        Mockito.when(wfDTO.getStatus()).thenReturn(WorkflowStatus.CREATED);
        Mockito.when(apimgtDAO.retrieveWorkflowFromInternalReference("1",
                WorkflowConstants.WF_TYPE_AM_API_STATE)).thenReturn(wfDTO);
        APIStateChangeResponse response = apiProvider.
                changeLifeCycleStatus(apiId, APIConstants.API_LC_ACTION_DEPRECATE);
        Assert.assertNotNull(response);
    }

    @Test
    public void testChangeLifeCycleStatus() throws RegistryException, UserStoreException, APIManagementException,
            FaultGatewaysException, WorkflowException {

        APIIdentifier apiId = new APIIdentifier("admin", "API1", "1.0.0");

        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);

        prepareForChangeLifeCycleStatus(apiProvider, apimgtDAO, apiId, artifact);

        APIStateChangeResponse response1 = apiProvider.
                changeLifeCycleStatus(apiId, APIConstants.API_LC_ACTION_DEPRECATE);
        Assert.assertEquals("APPROVED", response1.getStateChangeStatus());
    }

    @Test(expected = APIManagementException.class)
    public void testChangeLifeCycleStatusAPIMgtException() throws RegistryException, UserStoreException,
            APIManagementException, FaultGatewaysException, WorkflowException {
        APIIdentifier apiId = new APIIdentifier("admin", "API1", "1.0.0");
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        prepareForChangeLifeCycleStatus(apiProvider, apimgtDAO, apiId, artifact);
        GovernanceException exception = new GovernanceException(new APIManagementException("APIManagementException:"
                + "Error while retrieving Life cycle state"));
        Mockito.when(artifact.getLifecycleState()).thenThrow(exception);
        apiProvider.changeLifeCycleStatus(apiId, APIConstants.API_LC_ACTION_DEPRECATE);
    }

    @Test(expected = FaultGatewaysException.class)
    public void testChangeLifeCycleStatus_FaultyGWException() throws RegistryException, UserStoreException,
            APIManagementException, FaultGatewaysException, WorkflowException {
        APIIdentifier apiId = new APIIdentifier("admin", "API1", "1.0.0");
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        prepareForChangeLifeCycleStatus(apiProvider, apimgtDAO, apiId, artifact);
        GovernanceException exception = new GovernanceException(new APIManagementException("FaultGatewaysException:"
                + "{\"PUBLISHED\":{\"PROD\":\"Error\"}}"));
        Mockito.when(artifact.getLifecycleState()).thenThrow(exception);
        apiProvider.changeLifeCycleStatus(apiId, APIConstants.API_LC_ACTION_DEPRECATE);
    }

    @Test(expected = APIManagementException.class)
    public void testUpdateAPI_WithStatusChange() throws RegistryException, UserStoreException, APIManagementException,
            FaultGatewaysException {
        APIIdentifier identifier = new APIIdentifier("admin", "API1", "1.0.0");
        API api = new API(identifier);
        api.setStatus(APIConstants.PUBLISHED);
        api.setVisibility("public");

        //API status change is not allowed in UpdateAPI(). Should throw an exception.
        API oldApi = new API(identifier);
        oldApi.setStatus(APIConstants.CREATED);
        oldApi.setVisibility("public");
        oldApi.setContext("/api1");

        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        RegistryService registryService = Mockito.mock(RegistryService.class);
        UserRegistry userRegistry = Mockito.mock(UserRegistry.class);
        ServiceReferenceHolder serviceReferenceHolder = TestUtils.getServiceReferenceHolder();
        RealmService realmService = Mockito.mock(RealmService.class);
        TenantManager tenantManager = Mockito.mock(TenantManager.class);

        Mockito.when(artifactManager.newGovernanceArtifact(any(QName.class))).thenReturn(artifact);
        Mockito.when(APIUtil.createAPIArtifactContent(artifact, oldApi)).thenReturn(artifact);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getRegistryService()).thenReturn(registryService);
        Mockito.when(registryService.getConfigSystemRegistry(Mockito.anyInt())).thenReturn(userRegistry);
        Mockito.when(serviceReferenceHolder.getRealmService()).thenReturn(realmService);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        apiProvider.addAPI(oldApi);

        //mock has permission
        Resource apiSourceArtifact = Mockito.mock(Resource.class);
        Mockito.when(apiSourceArtifact.getUUID()).thenReturn("12640983654");
        String apiSourcePath = "path";
        PowerMockito.when(APIUtil.getAPIPath(api.getId())).thenReturn(apiSourcePath);
        PowerMockito.when(apiProvider.registry.get(apiSourcePath)).thenReturn(apiSourceArtifact);

        //API Status is CREATED and user has permission
        Mockito.when(artifact.getAttribute(APIConstants.API_OVERVIEW_STATUS)).thenReturn("CREATED");
        Mockito.when(artifactManager.getGenericArtifact(apiSourceArtifact.getUUID())).thenReturn(artifact);
        PowerMockito.when(APIUtil.hasPermission(null, APIConstants.Permissions.API_PUBLISH)).thenReturn(true);
        PowerMockito.when(APIUtil.hasPermission(null, APIConstants.Permissions.API_CREATE)).thenReturn(true);
        apiProvider.updateAPI(api);
    }

    @Test
    public void testUpdateAPI_InCreatedState() throws Exception {
        APIIdentifier identifier = new APIIdentifier("admin-AT-carbon.super", "API1", "1.0.0");
        Set<String> environments = new HashSet<String>();
        Set<URITemplate> uriTemplates = new HashSet<URITemplate>();
        Set<URITemplate> newUriTemplates = new HashSet<URITemplate>();

        Tier tier = new Tier("Gold");
        Map<String, Tier> tiers = new TreeMap<>();
        tiers.put("Gold",tier);

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
        newUriTemplates.add(uriTemplate1);
        newUriTemplates.add(uriTemplate2);

        final API api = new API(identifier);
        api.setStatus(APIConstants.CREATED);
        api.setVisibility("public");
        api.setAccessControl("all");
        api.setTransports("http,https");
        api.setContext("/test");
        api.setEnvironments(environments);
        api.setUriTemplates(newUriTemplates);

        API oldApi = new API(identifier);
        oldApi.setStatus(APIConstants.CREATED);
        oldApi.setVisibility("public");
        oldApi.setAccessControl("all");
        oldApi.setContext("/test");
        oldApi.setEnvironments(environments);
        api.setUriTemplates(uriTemplates);


        List<Documentation> documentationList = getDocumentationList();
        Documentation documentation = documentationList.get(1);
        Mockito.when(APIUtil.getAPIDocPath(api.getId())).thenReturn(documentation.getFilePath());
        APIProviderImplWrapper apiProviderImplWrapper = new APIProviderImplWrapper(apimgtDAO, null, null);
        Resource docResource = Mockito.mock(Resource.class);
        Mockito.when(docResource.getUUID()).thenReturn(documentation.getId());
        Mockito.when(apiProviderImplWrapper.registry.get(documentation.getFilePath())).thenReturn(docResource);

        GenericArtifact docArtifact = Mockito.mock(GenericArtifact.class);
        Mockito.when(artifactManager.getGenericArtifact(documentation.getId())).thenReturn(docArtifact);
        Mockito.when(APIUtil.getDocumentation(docArtifact)).thenReturn(documentation);
        String artifactPath = "artifact/path";
        Mockito.when(docArtifact.getPath()).thenReturn(artifactPath);
        PowerMockito.doNothing().when(APIUtil.class, "clearResourcePermissions", Mockito.any(), Mockito.any(),
                Mockito.anyInt());

        String[] roles = {"admin", "subscriber"};
        APIUtil.setResourcePermissions("admin", "Public", roles, artifactPath);
        Mockito.when(docArtifact.getAttribute(APIConstants.DOC_FILE_PATH)).thenReturn("docFilePath");

        final APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, documentationList, null);
        RegistryService registryService = Mockito.mock(RegistryService.class);
        UserRegistry userRegistry = Mockito.mock(UserRegistry.class);
        ServiceReferenceHolder serviceReferenceHolder = TestUtils.getServiceReferenceHolder();
        RealmService realmService = Mockito.mock(RealmService.class);
        TenantManager tenantManager = Mockito.mock(TenantManager.class);

        Mockito.when(artifactManager.newGovernanceArtifact(any(QName.class))).thenReturn(artifact);
        Mockito.when(APIUtil.createAPIArtifactContent(artifact, oldApi)).thenReturn(artifact);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getRegistryService()).thenReturn(registryService);
        Mockito.when(registryService.getConfigSystemRegistry(Mockito.anyInt())).thenReturn(userRegistry);
        Mockito.when(serviceReferenceHolder.getRealmService()).thenReturn(realmService);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        apiProvider.addAPI(oldApi);

        //mock has permission
        Resource apiSourceArtifact = Mockito.mock(Resource.class);
        Mockito.when(apiSourceArtifact.getUUID()).thenReturn("12640983654");
        String apiSourcePath = "path";
        PowerMockito.when(APIUtil.getAPIPath(api.getId())).thenReturn(apiSourcePath);
        PowerMockito.when(APIUtil.getAPIPath(oldApi.getId())).thenReturn(apiSourcePath);
        PowerMockito.when(apiProvider.registry.get(apiSourcePath)).thenReturn(apiSourceArtifact);

        //API Status is CREATED and user has permission
        Mockito.when(artifact.getAttribute(APIConstants.API_OVERVIEW_STATUS)).thenReturn("CREATED");
        Mockito.when(artifactManager.getGenericArtifact(apiSourceArtifact.getUUID())).thenReturn(artifact);
        PowerMockito.when(APIUtil.hasPermission(null, APIConstants.Permissions.API_PUBLISH)).thenReturn(true);
        PowerMockito.when(APIUtil.hasPermission(null, APIConstants.Permissions.API_CREATE)).thenReturn(true);
        Mockito.when(apimgtDAO.getDefaultVersion(identifier)).thenReturn("1.0.0");
        Mockito.when(apimgtDAO.getPublishedDefaultVersion(identifier)).thenReturn("1.0.0");

        //updateDefaultAPIInRegistry
        String defaultAPIPath = APIConstants.API_LOCATION + RegistryConstants.PATH_SEPARATOR +
                identifier.getProviderName() +
                RegistryConstants.PATH_SEPARATOR + identifier.getApiName() +
                RegistryConstants.PATH_SEPARATOR + identifier.getVersion() +
                APIConstants.API_RESOURCE_NAME;
        Resource defaultAPISourceArtifact = Mockito.mock(Resource.class);
        String defaultAPIUUID = "12640983600";
        Mockito.when(defaultAPISourceArtifact.getUUID()).thenReturn(defaultAPIUUID);
        Mockito.when(apiProvider.registry.get(defaultAPIPath)).thenReturn(defaultAPISourceArtifact);
        GenericArtifact defaultAPIArtifact = Mockito.mock(GenericArtifact.class);
        Mockito.when(artifactManager.getGenericArtifact(defaultAPIUUID)).thenReturn(defaultAPIArtifact);
        Mockito.doNothing().when(artifactManager).updateGenericArtifact(defaultAPIArtifact);
        TestUtils.mockAPIMConfiguration(APIConstants.API_GATEWAY_TYPE, APIConstants.API_GATEWAY_TYPE_SYNAPSE, -1234);

        //updateApiArtifact
        PowerMockito.when(APIUtil.createAPIArtifactContent(artifact, api)).thenReturn(artifact);
        Mockito.when(artifact.getId()).thenReturn("12640983654");
        PowerMockito.when(GovernanceUtils.getArtifactPath(apiProvider.registry, "12640983654")).
                thenReturn(apiSourcePath);
        //Mock Updating API
        Mockito.doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                apiProvider.createAPI(api);
                return null;
            }
        }).when(artifactManager).updateGenericArtifact(artifact);

        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                .getAPIManagerConfiguration();
        GatewayArtifactSynchronizerProperties synchronizerProperties = new GatewayArtifactSynchronizerProperties();
        Mockito.when(config.getGatewayArtifactSynchronizerProperties()).thenReturn(synchronizerProperties);

        Mockito.when(gatewayManager.isAPIPublished(api, "carbon.super")).thenReturn(false);
        Mockito.when(APIUtil.getTiers(APIConstants.TIER_RESOURCE_TYPE, "carbon.super")).thenReturn(tiers);
        apiProvider.updateAPI(api);
        Assert.assertEquals(0, api.getEnvironments().size());

        tiers.remove("Gold", tier);
        tier = new Tier("Unlimited");
        tiers.put("Unlimited", tier);
        try {
            apiProvider.updateAPI(api);
        } catch (APIManagementException ex) {
            Assert.assertTrue(ex.getMessage().contains("Invalid x-throttling tier Gold found in api definition for " +
                    "resource POST /add"));
        }
    }


    @Test
    public void testUpdateAPI_InPublishedState() throws Exception {
        APIIdentifier identifier = new APIIdentifier("admin-AT-carbon.super", "API1", "1.0.0");
        Set<String> environments = new HashSet<String>();
        environments.add("PRODUCTION");

        Set<String> newEnvironments = new HashSet<String>();
        newEnvironments.add("SANDBOX");

        Set<URITemplate> uriTemplates = new HashSet<URITemplate>();
        Set<URITemplate> newUriTemplates = new HashSet<URITemplate>();

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
        newUriTemplates.add(uriTemplate1);
        newUriTemplates.add(uriTemplate2);

        final API api = new API(identifier);
        api.setStatus(APIConstants.PUBLISHED);
        api.setVisibility("private");
        api.setVisibleRoles("admin");
        api.setAccessControl("all");
        api.setTransports("http,https");
        api.setContext("/test");
        api.setEnvironments(newEnvironments);
        api.setUriTemplates(newUriTemplates);

        API oldApi = new API(identifier);
        oldApi.setStatus(APIConstants.PUBLISHED);
        oldApi.setVisibility("public");
        oldApi.setAccessControl("all");
        oldApi.setContext("/test");
        oldApi.setEnvironments(environments);
        api.setUriTemplates(uriTemplates);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("test", "new_test");
        api.setAdditionalProperties(jsonObject);
        api.addProperty("secured", "false");

        Tier tier = new Tier("Gold");
        Map<String, Tier> tiers = new TreeMap<>();
        tiers.put("Gold", tier);
        Mockito.when(APIUtil.getTiers(APIConstants.TIER_RESOURCE_TYPE, "carbon.super")).thenReturn(tiers);

        List<Documentation> documentationList = getDocumentationList();
        Documentation documentation = documentationList.get(1);
        Mockito.when(APIUtil.getAPIDocPath(api.getId())).thenReturn(documentation.getFilePath());
        APIProviderImplWrapper apiProviderImplWrapper = new APIProviderImplWrapper(apimgtDAO, null, null);
        Resource docResource = Mockito.mock(Resource.class);
        Mockito.when(docResource.getUUID()).thenReturn(documentation.getId());
        Mockito.when(apiProviderImplWrapper.registry.get(documentation.getFilePath())).thenReturn(docResource);

        GenericArtifact docArtifact = Mockito.mock(GenericArtifact.class);
        Mockito.when(artifactManager.getGenericArtifact(documentation.getId())).thenReturn(docArtifact);
        Mockito.when(APIUtil.getDocumentation(docArtifact)).thenReturn(documentation);
        String artifactPath = "artifact/path";
        Mockito.when(docArtifact.getPath()).thenReturn(artifactPath);
        PowerMockito.doNothing().when(APIUtil.class, "clearResourcePermissions", Mockito.any(), Mockito.any(),
                Mockito.anyInt());

        String[] roles = {"admin", "subscriber"};
        APIUtil.setResourcePermissions("admin", "Public", roles, artifactPath);
        Mockito.when(docArtifact.getAttribute(APIConstants.DOC_FILE_PATH)).thenReturn("docFilePath");

        final APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, documentationList, null);

        Mockito.when(artifactManager.newGovernanceArtifact(any(QName.class))).thenReturn(artifact);
        Mockito.when(APIUtil.createAPIArtifactContent(artifact, oldApi)).thenReturn(artifact);
        RegistryService registryService = Mockito.mock(RegistryService.class);
        UserRegistry userRegistry = Mockito.mock(UserRegistry.class);
        ServiceReferenceHolder serviceReferenceHolder = TestUtils.getServiceReferenceHolder();
        RealmService realmService = Mockito.mock(RealmService.class);
        TenantManager tenantManager = Mockito.mock(TenantManager.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getRegistryService()).thenReturn(registryService);
        Mockito.when(registryService.getConfigSystemRegistry(Mockito.anyInt())).thenReturn(userRegistry);
        Mockito.when(serviceReferenceHolder.getRealmService()).thenReturn(realmService);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        apiProvider.addAPI(oldApi);

        RegistryAuthorizationManager registryAuthorizationManager = Mockito.mock(RegistryAuthorizationManager.class);
        PowerMockito.whenNew(RegistryAuthorizationManager.class).withAnyArguments()
                .thenReturn(registryAuthorizationManager);

        //mock has permission
        Resource apiSourceArtifact = Mockito.mock(Resource.class);
        Mockito.when(apiSourceArtifact.getUUID()).thenReturn("12640983654");
        String apiSourcePath = "path";
        PowerMockito.when(APIUtil.getAPIPath(api.getId())).thenReturn(apiSourcePath);
        PowerMockito.when(APIUtil.getAPIPath(oldApi.getId())).thenReturn(apiSourcePath);
        PowerMockito.when(apiProvider.registry.get(apiSourcePath)).thenReturn(apiSourceArtifact);

        //API Status is CREATED and user has permission
        Mockito.when(artifact.getAttribute(APIConstants.API_OVERVIEW_STATUS)).thenReturn("PUBLISHED");
        Mockito.when(artifact.getAttribute(APIConstants.API_OVERVIEW_ENDPOINT_USERNAME)).thenReturn("user1");
        Mockito.when(artifact.getAttribute(APIConstants.API_OVERVIEW_ENDPOINT_PASSWORD)).thenReturn("password");
        Mockito.when(artifactManager.getGenericArtifact(apiSourceArtifact.getUUID())).thenReturn(artifact);

        PowerMockito.when(APIUtil.hasPermission(null, APIConstants.Permissions.API_PUBLISH)).thenReturn(true);
        PowerMockito.when(APIUtil.hasPermission(null, APIConstants.Permissions.API_CREATE)).thenReturn(true);

        Mockito.when(apimgtDAO.getDefaultVersion(identifier)).thenReturn("1.0.0");
        Mockito.when(apimgtDAO.getPublishedDefaultVersion(identifier)).thenReturn("1.0.0");

        //updateDefaultAPIInRegistry
        String defaultAPIPath = APIConstants.API_LOCATION + RegistryConstants.PATH_SEPARATOR +
                identifier.getProviderName() +
                RegistryConstants.PATH_SEPARATOR + identifier.getApiName() +
                RegistryConstants.PATH_SEPARATOR + identifier.getVersion() +
                APIConstants.API_RESOURCE_NAME;
        Resource defaultAPISourceArtifact = Mockito.mock(Resource.class);
        String defaultAPIUUID = "12640983600";
        Mockito.when(defaultAPISourceArtifact.getUUID()).thenReturn(defaultAPIUUID);
        Mockito.when(apiProvider.registry.get(defaultAPIPath)).thenReturn(defaultAPISourceArtifact);
        GenericArtifact defaultAPIArtifact = Mockito.mock(GenericArtifact.class);
        Mockito.when(artifactManager.getGenericArtifact(defaultAPIUUID)).thenReturn(defaultAPIArtifact);
        Mockito.doNothing().when(artifactManager).updateGenericArtifact(defaultAPIArtifact);
        TestUtils.mockAPIMConfiguration(APIConstants.API_GATEWAY_TYPE, APIConstants.API_GATEWAY_TYPE_SYNAPSE, -1234);

        //updateApiArtifact
        PowerMockito.when(APIUtil.createAPIArtifactContent(artifact, api)).thenReturn(artifact);
        Mockito.when(artifact.getId()).thenReturn("12640983654");
        PowerMockito.when(GovernanceUtils.getArtifactPath(apiProvider.registry, "12640983654")).
                thenReturn(apiSourcePath);
        //Mock Updating API
        Mockito.doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                apiProvider.createAPI(api);
                return null;
            }
        }).when(artifactManager).updateGenericArtifact(artifact);

        //Mocking API already not published and published
        Mockito.when(gatewayManager.isAPIPublished(any(API.class), Matchers.anyString())).thenReturn(true);
        Mockito.when(gatewayManager.getAPIEndpointSecurityType(any(API.class), Matchers.anyString()))
                .thenReturn(APIConstants.APIEndpointSecurityConstants.BASIC_AUTH,
                        APIConstants.APIEndpointSecurityConstants.DIGEST_AUTH);
        PowerMockito.mockStatic(OASParserUtil.class);
        Mockito.when(OASParserUtil.getAPIDefinition(api.getId(), apiProvider.registry)).thenReturn(
                "{\"info\": {\"swagger\":\"data\"}}");
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                .getAPIManagerConfiguration();
        GatewayArtifactSynchronizerProperties synchronizerProperties = new GatewayArtifactSynchronizerProperties();
        Mockito.when(config.getGatewayArtifactSynchronizerProperties()).thenReturn(synchronizerProperties);
        apiProvider.updateAPI(api);
        Assert.assertEquals(1, api.getEnvironments().size());
        Assert.assertEquals(true, api.getEnvironments().contains("SANDBOX"));

        //Previous updateAPI() call enabled API security. Therefore need to set it as false for the second test
        api.setEndpointSecured(false);
        apiProvider.updateAPI(api);
        Assert.assertEquals(1, api.getEnvironments().size());
        Assert.assertEquals(true, api.getEnvironments().contains("SANDBOX"));

        //Test WSDL endpoint API
        api.setEndpointConfig(EP_CONFIG_WSDL);
        PowerMockito.when(APIUtil.isValidWSDLURL(WSDL_URL, true)).thenReturn(true);
        PowerMockito.when(APIUtil.createWSDL(apiProvider.registry, api)).thenReturn("wsdl_path");

        apiProvider.updateAPI(api);
        Assert.assertEquals(1, api.getEnvironments().size());
        Assert.assertEquals(true, api.getEnvironments().contains("SANDBOX"));
        Assert.assertEquals("Additional properties that are set are not retrieved new_test", "new_test",
                api.getAdditionalProperties().get("test"));
        Assert.assertEquals("Additional properties that are set are not retrieved new_test", "false",
                api.getAdditionalProperties().get("secured"));

    }

    @Test(expected = APIManagementException.class)
    public void testUpdateAPI_WithExceptionInUpdateArtifact() throws RegistryException, UserStoreException,
            APIManagementException, FaultGatewaysException {
        APIIdentifier identifier = new APIIdentifier("admin-AT-carbon.super", "API1", "1.0.0");
        Set<String> environments = new HashSet<String>();

        Set<URITemplate> uriTemplates = new HashSet<URITemplate>();
        Set<URITemplate> newUriTemplates = new HashSet<URITemplate>();

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
        newUriTemplates.add(uriTemplate1);
        newUriTemplates.add(uriTemplate2);

        Tier tier = new Tier("Gold");
        Map<String, Tier> tiers = new TreeMap<>();
        tiers.put("Gold", tier);

        final API api = new API(identifier);
        api.setStatus(APIConstants.CREATED);
        api.setVisibility("public");
        api.setAccessControl("all");
        api.setTransports("http,https");
        api.setContext("/test");
        api.setEnvironments(environments);
        api.setUriTemplates(newUriTemplates);

        API oldApi = new API(identifier);
        oldApi.setStatus(APIConstants.CREATED);
        oldApi.setVisibility("public");
        oldApi.setAccessControl("all");
        oldApi.setContext("/test");
        oldApi.setEnvironments(environments);
        api.setUriTemplates(uriTemplates);


        List<Documentation> documentationList = getDocumentationList();

        final APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, documentationList, null);

        Mockito.when(artifactManager.newGovernanceArtifact(any(QName.class))).thenReturn(artifact);
        Mockito.when(APIUtil.createAPIArtifactContent(artifact, oldApi)).thenReturn(artifact);
        Mockito.when(APIUtil.getTiers(APIConstants.TIER_RESOURCE_TYPE, "carbon.super")).thenReturn(tiers);
        RegistryService registryService = Mockito.mock(RegistryService.class);
        UserRegistry userRegistry = Mockito.mock(UserRegistry.class);
        ServiceReferenceHolder serviceReferenceHolder = TestUtils.getServiceReferenceHolder();
        RealmService realmService = Mockito.mock(RealmService.class);
        TenantManager tenantManager = Mockito.mock(TenantManager.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getRegistryService()).thenReturn(registryService);
        Mockito.when(registryService.getConfigSystemRegistry(Mockito.anyInt())).thenReturn(userRegistry);
        Mockito.when(serviceReferenceHolder.getRealmService()).thenReturn(realmService);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        apiProvider.addAPI(oldApi);

        //mock has permission
        Resource apiSourceArtifact = Mockito.mock(Resource.class);
        Mockito.when(apiSourceArtifact.getUUID()).thenReturn("12640983654");
        String apiSourcePath = "path";
        PowerMockito.when(APIUtil.getAPIPath(api.getId())).thenReturn(apiSourcePath);
        PowerMockito.when(APIUtil.getAPIPath(oldApi.getId())).thenReturn(apiSourcePath);
        PowerMockito.when(apiProvider.registry.get(apiSourcePath)).thenReturn(apiSourceArtifact);

        //API Status is CREATED and user has permission
        Mockito.when(artifact.getAttribute(APIConstants.API_OVERVIEW_STATUS)).thenReturn("CREATED");
        Mockito.when(artifactManager.getGenericArtifact(apiSourceArtifact.getUUID())).thenReturn(artifact);

        PowerMockito.when(APIUtil.hasPermission(null, APIConstants.Permissions.API_PUBLISH)).thenReturn(true);
        PowerMockito.when(APIUtil.hasPermission(null, APIConstants.Permissions.API_CREATE)).thenReturn(true);

        Mockito.when(apimgtDAO.getDefaultVersion(identifier)).thenReturn("1.0.0");
        Mockito.when(apimgtDAO.getPublishedDefaultVersion(identifier)).thenReturn("1.0.0");

        //updateDefaultAPIInRegistry
        String defaultAPIPath = APIConstants.API_LOCATION + RegistryConstants.PATH_SEPARATOR +
                identifier.getProviderName() +
                RegistryConstants.PATH_SEPARATOR + identifier.getApiName() +
                RegistryConstants.PATH_SEPARATOR + identifier.getVersion() +
                APIConstants.API_RESOURCE_NAME;
        Resource defaultAPISourceArtifact = Mockito.mock(Resource.class);
        String defaultAPIUUID = "12640983600";
        Mockito.when(defaultAPISourceArtifact.getUUID()).thenReturn(defaultAPIUUID);
        Mockito.when(apiProvider.registry.get(defaultAPIPath)).thenReturn(defaultAPISourceArtifact);
        GenericArtifact defaultAPIArtifact = Mockito.mock(GenericArtifact.class);
        Mockito.when(artifactManager.getGenericArtifact(defaultAPIUUID)).thenReturn(defaultAPIArtifact);
        Mockito.doNothing().when(artifactManager).updateGenericArtifact(defaultAPIArtifact);
        TestUtils.mockAPIMConfiguration(APIConstants.API_GATEWAY_TYPE, APIConstants.API_GATEWAY_TYPE_SYNAPSE, 1234);

        //updateApiArtifact
        PowerMockito.when(APIUtil.createAPIArtifactContent(artifact, api)).thenThrow(APIManagementException.class);
        apiProvider.updateAPI(api);
    }

    @Test(expected = FaultGatewaysException.class)
    public void testUpdateAPI_WithFailedGWs() throws Exception {
        APIIdentifier identifier = new APIIdentifier("admin-AT-carbon.super", "API1", "1.0.0");
        Set<String> environments = new HashSet<String>();
        Set<URITemplate> uriTemplates = new HashSet<URITemplate>();
        Set<URITemplate> newUriTemplates = new HashSet<URITemplate>();

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
        newUriTemplates.add(uriTemplate1);
        newUriTemplates.add(uriTemplate2);

        final API api = new API(identifier);
        api.setStatus(APIConstants.PUBLISHED);
        api.setVisibility("public");
        api.setAccessControl("all");
        api.setTransports("http,https");
        api.setContext("/test");
        api.setEnvironments(environments);
        api.setUriTemplates(newUriTemplates);

        API oldApi = new API(identifier);
        oldApi.setStatus(APIConstants.PUBLISHED);
        oldApi.setVisibility("public");
        oldApi.setAccessControl("all");
        oldApi.setContext("/test");
        oldApi.setEnvironments(environments);
        api.setUriTemplates(uriTemplates);

        Tier tier = new Tier("Gold");
        Map<String, Tier> tiers = new TreeMap<>();
        tiers.put("Gold", tier);

        PowerMockito.when(APIUtil.getLcStateFromArtifact((GovernanceArtifact) any()))
                .thenReturn(APIConstants.PUBLISHED);

        final APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);

        Mockito.when(artifactManager.newGovernanceArtifact(any(QName.class))).thenReturn(artifact);
        Mockito.when(APIUtil.createAPIArtifactContent(artifact, oldApi)).thenReturn(artifact);

        GenericArtifact artifactNew = Mockito.mock(GenericArtifact.class);
        Mockito.when(APIUtil.createAPIArtifactContent(artifact, api)).thenReturn(artifactNew);
        RegistryService registryService = Mockito.mock(RegistryService.class);
        UserRegistry userRegistry = Mockito.mock(UserRegistry.class);
        ServiceReferenceHolder serviceReferenceHolder = TestUtils.getServiceReferenceHolder();
        RealmService realmService = Mockito.mock(RealmService.class);
        TenantManager tenantManager = Mockito.mock(TenantManager.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getRegistryService()).thenReturn(registryService);
        Mockito.when(registryService.getConfigSystemRegistry(Mockito.anyInt())).thenReturn(userRegistry);
        Mockito.when(serviceReferenceHolder.getRealmService()).thenReturn(realmService);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        apiProvider.addAPI(oldApi);

        //mock API artifact retrieval for has permission check
        Resource apiSourceArtifact = Mockito.mock(Resource.class);
        Mockito.when(apiSourceArtifact.getUUID()).thenReturn("12640983654");
        String apiSourcePath = "path";
        PowerMockito.when(APIUtil.getAPIPath(api.getId())).thenReturn(apiSourcePath);
        PowerMockito.when(APIUtil.getAPIPath(oldApi.getId())).thenReturn(apiSourcePath);
        PowerMockito.when(apiProvider.registry.get(apiSourcePath)).thenReturn(apiSourceArtifact);

        //API Status is CREATED and user has permission
        Mockito.when(artifact.getAttribute(APIConstants.API_OVERVIEW_STATUS)).thenReturn("CREATED");
        Mockito.when(artifactManager.getGenericArtifact(apiSourceArtifact.getUUID())).thenReturn(artifact);
        PowerMockito.when(APIUtil.hasPermission(null, APIConstants.Permissions.API_PUBLISH)).thenReturn(true);
        PowerMockito.when(APIUtil.hasPermission(null, APIConstants.Permissions.API_CREATE)).thenReturn(true);
        Mockito.when(apimgtDAO.getDefaultVersion(identifier)).thenReturn("1.0.0");
        Mockito.when(apimgtDAO.getPublishedDefaultVersion(identifier)).thenReturn("1.0.0");

        //updateDefaultAPIInRegistry
        String defaultAPIPath = APIConstants.API_LOCATION + RegistryConstants.PATH_SEPARATOR +
                identifier.getProviderName() +
                RegistryConstants.PATH_SEPARATOR + identifier.getApiName() +
                RegistryConstants.PATH_SEPARATOR + identifier.getVersion() +
                APIConstants.API_RESOURCE_NAME;
        Resource defaultAPISourceArtifact = Mockito.mock(Resource.class);
        String defaultAPIUUID = "12640983600";
        Mockito.when(defaultAPISourceArtifact.getUUID()).thenReturn(defaultAPIUUID);
        Mockito.when(apiProvider.registry.get(defaultAPIPath)).thenReturn(defaultAPISourceArtifact);
        GenericArtifact defaultAPIArtifact = Mockito.mock(GenericArtifact.class);
        Mockito.when(artifactManager.getGenericArtifact(defaultAPIUUID)).thenReturn(defaultAPIArtifact);
        Mockito.doNothing().when(artifactManager).updateGenericArtifact(defaultAPIArtifact);
        TestUtils.mockAPIMConfiguration(APIConstants.API_GATEWAY_TYPE, APIConstants.API_GATEWAY_TYPE_SYNAPSE, 1234);

        //updateApiArtifact
        Mockito.when(artifact.getId()).thenReturn("12640983654");
        PowerMockito.when(GovernanceUtils.getArtifactPath(apiProvider.registry, "12640983654")).
                thenReturn(apiSourcePath);
        //Mock Updating API
        Mockito.doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                apiProvider.createAPI(api);
                return null;
            }
        }).when(artifactManager).updateGenericArtifact(artifact);

        Mockito.when(gatewayManager.isAPIPublished(api, "carbon.super")).thenReturn(false);


        //Mock faulty GWs
        Map<String, String> failedToPubGWEnv = new HashMap<String, String>();
        failedToPubGWEnv.put("Production", "Failed to publish");
        Map<String, String> failedToUnPubGWEnv = new HashMap<String, String>();
        failedToUnPubGWEnv.put("Production", "Failed to unpublish");

        Mockito.when(gatewayManager.removeFromGateway(any(API.class),
                Matchers.anyString())).thenReturn(failedToUnPubGWEnv);
        Mockito.when(gatewayManager.publishToGateway(any(API.class), any(APITemplateBuilder.class),
                Matchers.anyString())).thenReturn(failedToPubGWEnv);
        Mockito.when(APIUtil.getTiers(APIConstants.TIER_RESOURCE_TYPE, "carbon.super")).thenReturn(tiers);
        PowerMockito.mockStatic(OASParserUtil.class);
        Mockito.when(OASParserUtil.getAPIDefinition(api.getId(), apiProvider.registry)).thenReturn(
                "{\"info\": {\"swagger\":\"data\"}}");
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                .getAPIManagerConfiguration();
        GatewayArtifactSynchronizerProperties synchronizerProperties = new GatewayArtifactSynchronizerProperties();
        Mockito.when(config.getGatewayArtifactSynchronizerProperties()).thenReturn(synchronizerProperties);
        apiProvider.updateAPI(api);
    }

    @Test
    public void testDeleteAPI() throws RegistryException, UserStoreException, APIManagementException,
            WorkflowException {
        APIIdentifier identifier = new APIIdentifier("admin-AT-carbon.super", "API1", "1.0.0");
        String apiUuid = "12345w";
        String path = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                identifier.getProviderName() + RegistryConstants.PATH_SEPARATOR +
                identifier.getApiName() + RegistryConstants.PATH_SEPARATOR + identifier.getVersion();

        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        Resource apiSourceArtifact = Mockito.mock(Resource.class);
        Mockito.when(apiSourceArtifact.getUUID()).thenReturn("12640983654");
        PowerMockito.when(APIUtil.getAPIPath(identifier)).thenReturn(path);
        PowerMockito.when(apiProvider.registry.get(path)).thenReturn(apiSourceArtifact);
        Mockito.when(artifactManager.getGenericArtifact(apiSourceArtifact.getUUID())).thenReturn(artifact);

        GovernanceArtifact[] dependenciesArray = {};
        Mockito.when(artifact.getDependencies()).thenReturn(dependenciesArray);
        Mockito.doNothing().when(artifactManager).removeGenericArtifact(artifact);
        Mockito.doNothing().when(artifactManager).removeGenericArtifact("12640983654");
        Mockito.when(apiProvider.registry.resourceExists("icon/path")).thenReturn(false);
        String apiDefinitionFilePath = APIConstants.API_DOC_LOCATION + RegistryConstants.PATH_SEPARATOR +
                identifier.getApiName() + '-' + identifier.getVersion() + '-' + identifier.getProviderName();
        Mockito.when(apiProvider.registry.resourceExists(apiDefinitionFilePath)).thenReturn(true);

        String apiCollectionPath = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                identifier.getProviderName() + RegistryConstants.PATH_SEPARATOR + identifier.getApiName();
        CollectionImpl collection = Mockito.mock(CollectionImpl.class);
        Mockito.when(apiProvider.registry.get(apiCollectionPath)).thenReturn(collection);
        Mockito.when(collection.getChildCount()).thenReturn(0);
        Mockito.when(apiProvider.registry.resourceExists(apiCollectionPath)).thenReturn(true);

        String apiProviderPath = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                identifier.getProviderName();
        CollectionImpl providerCollection = Mockito.mock(CollectionImpl.class);
        Mockito.when(apiProvider.registry.get(apiProviderPath)).thenReturn(providerCollection);
        Mockito.when(providerCollection.getChildCount()).thenReturn(0);
        Mockito.when(apiProvider.registry.resourceExists(apiProviderPath)).thenReturn(true);
        Mockito.when(APIUtil.getIconPath(identifier)).thenReturn(apiDefinitionFilePath);
        Mockito.when(apiProvider.registry.resourceExists(apiDefinitionFilePath)).thenReturn(true);

        WorkflowExecutorFactory wfe = PowerMockito.mock(WorkflowExecutorFactory.class);
        Mockito.when(WorkflowExecutorFactory.getInstance()).thenReturn(wfe);
        APIStateChangeSimpleWorkflowExecutor apiStateWFExecutor =
                Mockito.mock(APIStateChangeSimpleWorkflowExecutor.class);
        Mockito.when(wfe.getWorkflowExecutor(WorkflowConstants.WF_TYPE_AM_API_STATE)).thenReturn(apiStateWFExecutor);

        WorkflowDTO wfDTO = Mockito.mock(WorkflowDTO.class);
        Mockito.when(wfDTO.getStatus()).thenReturn(WorkflowStatus.APPROVED);
        Mockito.when(wfDTO.getExternalWorkflowReference()).thenReturn("REF");
        Mockito.when(apimgtDAO.retrieveWorkflowFromInternalReference("1",
                WorkflowConstants.WF_TYPE_AM_API_STATE)).thenReturn(wfDTO);
        Mockito.doNothing().when(apiStateWFExecutor).cleanUpPendingTask("REF");

        apiProvider.deleteAPI(identifier, apiUuid);
    }

    @Test(expected = APIManagementException.class)
    public void testDeleteAPI_RegistryException() throws RegistryException, UserStoreException, APIManagementException,
            WorkflowException {
        APIIdentifier identifier = new APIIdentifier("admin-AT-carbon.super", "API1", "1.0.0");
        String apiUuid = "12345w";

        String path = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                identifier.getProviderName() + RegistryConstants.PATH_SEPARATOR +
                identifier.getApiName() + RegistryConstants.PATH_SEPARATOR + identifier.getVersion();

        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);

        Resource apiSourceArtifact = Mockito.mock(Resource.class);
        Mockito.when(apiSourceArtifact.getUUID()).thenReturn("12640983654");
        PowerMockito.when(APIUtil.getAPIPath(identifier)).thenReturn(path);
        PowerMockito.when(apiProvider.registry.get(path)).thenThrow(RegistryException.class);
        apiProvider.deleteAPI(identifier, apiUuid);
    }

    @Test(expected = APIManagementException.class)
    public void testDeleteAPI_WFException() throws RegistryException, UserStoreException, APIManagementException,
            WorkflowException {
        APIIdentifier identifier = new APIIdentifier("admin-AT-carbon.super", "API1", "1.0.0");
        String apiUuid = "1234w";

        String path = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                identifier.getProviderName() + RegistryConstants.PATH_SEPARATOR +
                identifier.getApiName() + RegistryConstants.PATH_SEPARATOR + identifier.getVersion();


        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);

        Resource apiSourceArtifact = Mockito.mock(Resource.class);
        Mockito.when(apiSourceArtifact.getUUID()).thenReturn("12640983654");
        PowerMockito.when(APIUtil.getAPIPath(identifier)).thenReturn(path);
        PowerMockito.when(apiProvider.registry.get(path)).thenReturn(apiSourceArtifact);

        Mockito.when(artifactManager.getGenericArtifact(apiSourceArtifact.getUUID())).thenReturn(artifact);

        GovernanceArtifact[] dependenciesArray = {};
        Mockito.when(artifact.getDependencies()).thenReturn(dependenciesArray);
        Mockito.doNothing().when(artifactManager).removeGenericArtifact(artifact);
        Mockito.doNothing().when(artifactManager).removeGenericArtifact("12640983654");
        Mockito.when(APIUtil.getIconPath(identifier)).thenReturn("icon/path");
        Mockito.when(apiProvider.registry.resourceExists("icon/path")).thenReturn(false);

        String apiDefinitionFilePath = APIConstants.API_DOC_LOCATION + RegistryConstants.PATH_SEPARATOR +
                identifier.getApiName() + '-' + identifier.getVersion() + '-' + identifier.getProviderName();

        Mockito.when(APIUtil.getIconPath(identifier)).thenReturn(apiDefinitionFilePath);
        Mockito.when(apiProvider.registry.resourceExists(apiDefinitionFilePath)).thenReturn(true);

        String apiCollectionPath = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                identifier.getProviderName() + RegistryConstants.PATH_SEPARATOR + identifier.getApiName();
        CollectionImpl collection = Mockito.mock(CollectionImpl.class);
        Mockito.when(apiProvider.registry.get(apiCollectionPath)).thenReturn(collection);
        Mockito.when(collection.getChildCount()).thenReturn(0);
        Mockito.when(apiProvider.registry.resourceExists(apiCollectionPath)).thenReturn(true);

        String apiProviderPath = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                identifier.getProviderName();
        CollectionImpl providerCollection = Mockito.mock(CollectionImpl.class);
        Mockito.when(apiProvider.registry.get(apiProviderPath)).thenReturn(providerCollection);
        Mockito.when(providerCollection.getChildCount()).thenReturn(0);
        Mockito.when(apiProvider.registry.resourceExists(apiProviderPath)).thenReturn(true);

        Mockito.when(APIUtil.getIconPath(identifier)).thenReturn(apiDefinitionFilePath);
        Mockito.when(apiProvider.registry.resourceExists(apiDefinitionFilePath)).thenReturn(true);

        WorkflowExecutorFactory wfe = PowerMockito.mock(WorkflowExecutorFactory.class);
        Mockito.when(WorkflowExecutorFactory.getInstance()).thenReturn(wfe);
        APIStateChangeSimpleWorkflowExecutor apiStateWFExecutor =
                Mockito.mock(APIStateChangeSimpleWorkflowExecutor.class);
        Mockito.when(wfe.getWorkflowExecutor(WorkflowConstants.WF_TYPE_AM_API_STATE)).
                thenThrow(WorkflowException.class);

        apiProvider.deleteAPI(identifier, apiUuid);
    }

    @Test
    public void testDeleteAPI_WithActiveSubscriptions() throws RegistryException, UserStoreException,
            APIManagementException, WorkflowException {
        APIIdentifier identifier = new APIIdentifier("admin-AT-carbon.super", "API1", "1.0.0");
        String apiUuid = "12345w";
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        Mockito.when(apimgtDAO.getAPISubscriptionCountByAPI(identifier)).thenReturn(1L);
        try {
            apiProvider.deleteAPI(identifier, apiUuid);
        } catch (APIManagementException e) {
            Assert.assertEquals("Cannot remove the API as active subscriptions exist.", e.getMessage());
        }
    }

    @Test
    public void testAddPolicy_APIType() throws RegistryException, UserStoreException, APIManagementException {
        APIPolicy policy = getPolicyAPILevelPerUser();
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        Mockito.when(apimgtDAO.addAPIPolicy(policy)).thenReturn(policy);
        apiProvider.addPolicy(policy);
    }

    @Test
    public void testAddPolicy_APPType() throws RegistryException, UserStoreException, APIManagementException {
        ApplicationPolicy policy = getPolicyAppLevel();
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        apiProvider.addPolicy(policy);
    }

    @Test
    public void testAddPolicy_SubsType() throws RegistryException, UserStoreException, APIManagementException {
        SubscriptionPolicy policy = getPolicySubscriptionLevelperUser();
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        apiProvider.addPolicy(policy);
    }

    @Test
    public void testAddPolicy_GlobalType() throws RegistryException, UserStoreException, APIManagementException,
            APITemplateException {
        GlobalPolicy policy = getPolicyGlobalLevel();
        ThrottlePolicyDeploymentManager manager = Mockito.mock(ThrottlePolicyDeploymentManager.class);
        PowerMockito.when(ThrottlePolicyDeploymentManager.getInstance()).thenReturn(manager);
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        String policyString = apiProvider.getThrottlePolicyTemplateBuilder().getThrottlePolicyForGlobalLevel(policy);
        Mockito.when(manager.validateExecutionPlan(policyString)).thenReturn(true);
        apiProvider.addPolicy(policy);
    }

    @Test
    public void testAddPolicy_WrongType() throws RegistryException, UserStoreException, APIManagementException,
            APITemplateException {
        Policy policy = new Policy("Test");
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        try {
            apiProvider.addPolicy(policy);
        } catch (Exception e) {
            Assert.assertEquals("Policy type " + policy.getClass().getName() + " is not supported", e.getMessage());
        }
    }

    @Test
    public void testUpdatePolicy_APIType() throws RegistryException, UserStoreException, APIManagementException {
        APIPolicy policy = getPolicyAPILevelPerUser();
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        Mockito.when(apimgtDAO.getAPIPolicy(policy.getPolicyName(), policy.getTenantId())).thenReturn(policy);
        Mockito.when(apimgtDAO.updateAPIPolicy(policy)).thenReturn(policy);
        apiProvider.updatePolicy(policy);
    }

    @Test
    public void testUpdatePolicy_AppType() throws RegistryException, UserStoreException, APIManagementException {
        SubscriptionPolicy policy = getPolicySubscriptionLevelperUser();
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        Mockito.when(apimgtDAO.getSubscriptionPolicy(policy.getPolicyName(), policy.getTenantId())).thenReturn(policy);
        Mockito.doNothing().when(apimgtDAO).updateSubscriptionPolicy(policy);
        apiProvider.updatePolicy(policy);
    }

    @Test
    public void testUpdatePolicy_GlobalType() throws RegistryException, UserStoreException, APIManagementException,
            APITemplateException {
        GlobalPolicy policy = getPolicyGlobalLevel();
        ThrottlePolicyDeploymentManager manager = Mockito.mock(ThrottlePolicyDeploymentManager.class);
        PowerMockito.when(ThrottlePolicyDeploymentManager.getInstance()).thenReturn(manager);

        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        Mockito.when(apimgtDAO.getGlobalPolicy(policy.getPolicyName())).thenReturn(policy);
        Mockito.doNothing().when(apimgtDAO).updateGlobalPolicy(policy);

        String policyString = apiProvider.getThrottlePolicyTemplateBuilder().getThrottlePolicyForGlobalLevel(policy);
        Mockito.when(manager.validateExecutionPlan(policyString)).thenReturn(true);
        apiProvider.updatePolicy(policy);
    }

    @Test
    public void testUpdatePolicy_GlobalTypeInvalidPlan() throws RegistryException, UserStoreException,
            APIManagementException, APITemplateException {
        GlobalPolicy policy = getPolicyGlobalLevel();
        ThrottlePolicyDeploymentManager manager = Mockito.mock(ThrottlePolicyDeploymentManager.class);
        PowerMockito.when(ThrottlePolicyDeploymentManager.getInstance()).thenReturn(manager);

        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        Mockito.when(apimgtDAO.getGlobalPolicy(policy.getPolicyName())).thenReturn(policy);
        Mockito.doNothing().when(apimgtDAO).updateGlobalPolicy(policy);

        String policyString = apiProvider.getThrottlePolicyTemplateBuilder().getThrottlePolicyForGlobalLevel(policy);
        Mockito.when(manager.validateExecutionPlan(policyString)).thenReturn(false);
        try {
            apiProvider.updatePolicy(policy);
        } catch (Exception e) {
            Assert.assertEquals("Invalid Execution Plan", e.getMessage());
        }
    }

    @Test
    public void testUpdatePolicy_GlobalTypeAlreadyExist() throws RegistryException, UserStoreException,
            APIManagementException, APITemplateException {
        GlobalPolicy policy = getPolicyGlobalLevel();
        ThrottlePolicyDeploymentManager manager = Mockito.mock(ThrottlePolicyDeploymentManager.class);
        PowerMockito.when(ThrottlePolicyDeploymentManager.getInstance()).thenReturn(manager);

        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        Mockito.when(apimgtDAO.getGlobalPolicy(policy.getPolicyName())).thenReturn(policy);
        Mockito.doNothing().when(apimgtDAO).updateGlobalPolicy(policy);
        String policyString = apiProvider.getThrottlePolicyTemplateBuilder().getThrottlePolicyForGlobalLevel(policy);
        Mockito.when(manager.validateExecutionPlan(policyString)).thenReturn(true);
        Mockito.when(apimgtDAO.isKeyTemplatesExist(policy)).thenReturn(true);
        try {
            apiProvider.updatePolicy(policy);
        } catch (Exception e) {
            Assert.assertEquals("Key Template Already Exist", e.getMessage());
        }
    }

    @Test
    public void testUpdatePolicy_SubsType() throws RegistryException, UserStoreException, APIManagementException {
        ApplicationPolicy policy = getPolicyAppLevel();
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        Mockito.when(apimgtDAO.getApplicationPolicy(policy.getPolicyName(), policy.getTenantId())).thenReturn(policy);
        Mockito.doNothing().when(apimgtDAO).updateApplicationPolicy(policy);
        apiProvider.updatePolicy(policy);
    }

    @Test
    public void testUpdatePolicy_WrongType() throws RegistryException, UserStoreException, APIManagementException {

        Policy policy = new Policy("Test");
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        try {
            apiProvider.updatePolicy(policy);
        } catch (Exception e) {
            Assert.assertEquals("Policy type " + policy.getClass().getName() + " is not supported", e.getMessage());
        }
    }

    @Test
    public void testUpdatePolicy_APITypeErrorWhileDeploying() throws RegistryException, UserStoreException,
            APIManagementException {
        APIPolicy policy = getPolicyAPILevelPerUser();
        ThrottlePolicyDeploymentManager manager = Mockito.mock(ThrottlePolicyDeploymentManager.class);
        PowerMockito.when(ThrottlePolicyDeploymentManager.getInstance()).thenReturn(manager);

        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        Mockito.when(apimgtDAO.getAPIPolicy(policy.getPolicyName(), policy.getTenantId())).thenReturn(policy);
        Mockito.when(apimgtDAO.updateAPIPolicy(policy)).thenReturn(policy);
        Mockito.doThrow(APIManagementException.class).when(manager).undeployPolicyFromGlobalCEP(Matchers.anyString());
        try {
            apiProvider.updatePolicy(policy);
        } catch (Exception e) {
            Assert.assertEquals("Error while deploying policy to gateway", e.getMessage());
            Assert.assertEquals(PolicyDeploymentFailureException.class, e.getClass());
        }
    }

    @Test
    public void testDeletePolicy_APIType() throws RegistryException, UserStoreException, APIManagementException {
        APIPolicy policy = getPolicyAPILevelPerUser();
        policy.setDeployed(true);
        String policyName = "custom1";

        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        Mockito.when(apimgtDAO.getAPIPolicy(policyName, APIUtil.getTenantId("admin"))).thenReturn(policy);
        apiProvider.deletePolicy("admin", PolicyConstants.POLICY_LEVEL_API, policyName);
    }

    @Test
    public void testDeletePolicy_APItypeErrorWhileDeploying() throws RegistryException, UserStoreException,
            APIManagementException {
        APIPolicy policy = getPolicyAPILevelPerUser();
        policy.setDeployed(true);
        String policyName = "custom1";

        ThrottlePolicyDeploymentManager manager = Mockito.mock(ThrottlePolicyDeploymentManager.class);
        PowerMockito.when(ThrottlePolicyDeploymentManager.getInstance()).thenReturn(manager);
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        Mockito.when(apimgtDAO.getAPIPolicy(policyName, APIUtil.getTenantId("admin"))).thenReturn(policy);

        List<String> policyFileNames = new ArrayList<String>();
        String policyFile = policy.getTenantDomain() + "_" + PolicyConstants.POLICY_LEVEL_RESOURCE + "_" + policyName;
        policyFileNames.add(policyFile + "_default");
        for (Pipeline pipeline : policy.getPipelines()) {
            policyFileNames.add(policyFile + "_condition_" + pipeline.getId());
        }
        Mockito.doThrow(APIManagementException.class).when(manager).undeployPolicyFromGatewayManager(
                policyFileNames.toArray(new String[policyFileNames.size()]));
        try {
            apiProvider.deletePolicy("admin", PolicyConstants.POLICY_LEVEL_API, policyName);
        } catch (Exception e) {
            Assert.assertEquals("Error while undeploying policy: ", e.getMessage());
            Assert.assertEquals(APIManagementException.class, e.getClass());
        }
    }

    @Test
    public void testDeletePolicy_AppType() throws RegistryException, UserStoreException, APIManagementException {
        ApplicationPolicy policy = getPolicyAppLevel();
        policy.setDeployed(true);
        String policyName = "gold";

        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        Mockito.when(apimgtDAO.getApplicationPolicy(policyName, APIUtil.getTenantId("admin"))).thenReturn(policy);
        apiProvider.deletePolicy("admin", PolicyConstants.POLICY_LEVEL_APP, policyName);
    }

    @Test
    public void testDeletePolicy_AppTypeErrorWhileDeploying() throws RegistryException, UserStoreException,
            APIManagementException {
        ApplicationPolicy policy = getPolicyAppLevel();
        policy.setDeployed(true);
        String policyName = "gold";

        ThrottlePolicyDeploymentManager manager = Mockito.mock(ThrottlePolicyDeploymentManager.class);
        PowerMockito.when(ThrottlePolicyDeploymentManager.getInstance()).thenReturn(manager);
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        Mockito.when(apimgtDAO.getApplicationPolicy(policyName, APIUtil.getTenantId("admin"))).thenReturn(policy);

        List<String> policyFileNames = new ArrayList<String>();
        String policyFile = policy.getTenantDomain() + "_" + PolicyConstants.POLICY_LEVEL_APP + "_" + policyName;
        policyFileNames.add(policyFile);
        Mockito.doThrow(APIManagementException.class).when(manager).undeployPolicyFromGatewayManager(
                policyFileNames.toArray(new String[policyFileNames.size()]));
        try {
            apiProvider.deletePolicy("admin", PolicyConstants.POLICY_LEVEL_APP, policyName);
        } catch (Exception e) {
            Assert.assertEquals("Error while undeploying policy: ", e.getMessage());
            Assert.assertEquals(APIManagementException.class, e.getClass());
        }
    }

    @Test
    public void testDeletePolicy_SubType() throws RegistryException, UserStoreException, APIManagementException {
        SubscriptionPolicy policy = getPolicySubscriptionLevelperUser();
        policy.setDeployed(true);
        String policyName = "gold";

        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        Mockito.when(apimgtDAO.getSubscriptionPolicy(policyName, APIUtil.getTenantId("admin"))).thenReturn(policy);
        apiProvider.deletePolicy("admin", PolicyConstants.POLICY_LEVEL_SUB, policyName);
    }

    @Test
    public void testDeletePolicy_SubTypeErrorWhileDeploying() throws RegistryException, UserStoreException,
            APIManagementException {
        SubscriptionPolicy policy = getPolicySubscriptionLevelperUser();
        policy.setDeployed(true);
        String policyName = "gold";

        ThrottlePolicyDeploymentManager manager = Mockito.mock(ThrottlePolicyDeploymentManager.class);
        PowerMockito.when(ThrottlePolicyDeploymentManager.getInstance()).thenReturn(manager);
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        Mockito.when(apimgtDAO.getSubscriptionPolicy(policyName, APIUtil.getTenantId("admin"))).thenReturn(policy);

        List<String> policyFileNames = new ArrayList<String>();
        String policyFile = policy.getTenantDomain() + "_" + PolicyConstants.POLICY_LEVEL_SUB + "_" + policyName;
        policyFileNames.add(policyFile);
        Mockito.doThrow(APIManagementException.class).when(manager).undeployPolicyFromGatewayManager(
                policyFileNames.toArray(new String[policyFileNames.size()]));
        try {
            apiProvider.deletePolicy("admin", PolicyConstants.POLICY_LEVEL_SUB, policyName);
        } catch (Exception e) {
            Assert.assertEquals("Error while undeploying policy: ", e.getMessage());
            Assert.assertEquals(APIManagementException.class, e.getClass());
        }
    }

    @Test
    public void testDeletePolicy_GlobalType() throws RegistryException, UserStoreException, APIManagementException {
        GlobalPolicy policy = getPolicyGlobalLevel();
        policy.setDeployed(true);
        String policyName = "gold";

        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        Mockito.when(apimgtDAO.getGlobalPolicy(policyName)).thenReturn(policy);
        apiProvider.deletePolicy("admin", PolicyConstants.POLICY_LEVEL_GLOBAL, policyName);
    }

    @Test
    public void testDeletePolicy_GlobalTypeErrorWhileDeploying() throws RegistryException, UserStoreException,
            APIManagementException {
        GlobalPolicy policy = getPolicyGlobalLevel();
        policy.setDeployed(true);
        String policyName = "gold";

        ThrottlePolicyDeploymentManager manager = Mockito.mock(ThrottlePolicyDeploymentManager.class);
        PowerMockito.when(ThrottlePolicyDeploymentManager.getInstance()).thenReturn(manager);
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        Mockito.when(apimgtDAO.getGlobalPolicy(policyName)).thenReturn(policy);

        List<String> policyFileNames = new ArrayList<String>();
        String policyFile = PolicyConstants.POLICY_LEVEL_GLOBAL + "_" + policyName;
        policyFileNames.add(policyFile);
        Mockito.doThrow(APIManagementException.class).when(manager).undeployPolicyFromGatewayManager(
                policyFileNames.toArray(new String[policyFileNames.size()]));
        try {
            apiProvider.deletePolicy("admin", PolicyConstants.POLICY_LEVEL_GLOBAL, policyName);
        } catch (Exception e) {
            Assert.assertEquals("Error while undeploying policy: ", e.getMessage());
            Assert.assertEquals(APIManagementException.class, e.getClass());
        }
    }

    @Test
    public void testGetAPILifeCycleData() throws Exception {
        APIIdentifier identifier = new APIIdentifier("admin-AT-carbon.super", "API1", "1.0.0");
        String path = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                identifier.getProviderName() + RegistryConstants.PATH_SEPARATOR +
                identifier.getApiName() + RegistryConstants.PATH_SEPARATOR + identifier.getVersion();

        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        Resource apiSourceArtifact = Mockito.mock(Resource.class);
        Mockito.when(apiSourceArtifact.getUUID()).thenReturn("12640983654");
        PowerMockito.when(APIUtil.getAPIPath(identifier)).thenReturn(path);
        PowerMockito.when(apiProvider.registry.get(path)).thenReturn(apiSourceArtifact);
        Mockito.when(artifactManager.getGenericArtifact(apiSourceArtifact.getUUID())).thenReturn(artifact);
        Mockito.when(artifact.getLifecycleState()).thenReturn("Created");
        LifecycleBean bean = getLCBean();
        Mockito.when(LifecycleBeanPopulator.getLifecycleBean(path, (UserRegistry) apiProvider.registry,
                apiProvider.configRegistry)).thenReturn(bean);
        Map<String, Object> lcData = apiProvider.getAPILifeCycleData(identifier);
        List checkListItems = (List) lcData.get(APIConstants.LC_CHECK_ITEMS);

        Assert.assertEquals(2, checkListItems.size());
        if (checkListItems.get(0) instanceof CheckListItem) {
            CheckListItem checkListItem = (CheckListItem) checkListItems.get(0);
            Assert.assertTrue((APIConstants.RESUBSCRIBE_CHECK_LIST_ITEM).equals(checkListItem.getName()) ||
                    (APIConstants.DEPRECATE_CHECK_LIST_ITEM).equals(checkListItem.getName()));
            if ((APIConstants.RESUBSCRIBE_CHECK_LIST_ITEM).equals(checkListItem.getName())) {
                Assert.assertEquals("1", checkListItem.getOrder());
            } else {
                Assert.assertEquals("0", checkListItem.getOrder());
            }

            Assert.assertEquals("Created", checkListItem.getLifeCycleStatus());
        }
        if (checkListItems.get(1) instanceof CheckListItem) {
            CheckListItem checkListItem = (CheckListItem) checkListItems.get(1);
            Assert.assertTrue((APIConstants.RESUBSCRIBE_CHECK_LIST_ITEM).equals(checkListItem.getName()) ||
                    (APIConstants.DEPRECATE_CHECK_LIST_ITEM).equals(checkListItem.getName()));
            if ((APIConstants.RESUBSCRIBE_CHECK_LIST_ITEM).equals(checkListItem.getName())) {
                Assert.assertEquals("1", checkListItem.getOrder());
            } else {
                Assert.assertEquals("0", checkListItem.getOrder());
            }
            Assert.assertEquals("Created", checkListItem.getLifeCycleStatus());
        }
    }

    @Test
    public void testGetAPILifeCycleData_WithException() throws Exception {
        APIIdentifier identifier = new APIIdentifier("admin-AT-carbon.super", "API1", "1.0.0");
        String path = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                identifier.getProviderName() + RegistryConstants.PATH_SEPARATOR +
                identifier.getApiName() + RegistryConstants.PATH_SEPARATOR + identifier.getVersion();

        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        Resource apiSourceArtifact = Mockito.mock(Resource.class);
        Mockito.when(apiSourceArtifact.getUUID()).thenReturn("12640983654");
        PowerMockito.when(APIUtil.getAPIPath(identifier)).thenReturn(path);
        PowerMockito.when(apiProvider.registry.get(path)).thenReturn(apiSourceArtifact);
        Mockito.when(artifactManager.getGenericArtifact(apiSourceArtifact.getUUID())).thenReturn(artifact);
        Mockito.when(artifact.getLifecycleState()).thenReturn("Created");
        Mockito.when(LifecycleBeanPopulator.getLifecycleBean(path,
                (UserRegistry) apiProvider.registry, apiProvider.configRegistry)).
                thenThrow(new Exception("Failed to get LC data"));
        try {
            apiProvider.getAPILifeCycleData(identifier);
        } catch (APIManagementException e) {
            Assert.assertEquals("Failed to get LC data", e.getMessage());
        }
    }

    @Test
    public void testUpdateAPIforStateChange_ToPublished() throws RegistryException, UserStoreException,
            APIManagementException, FaultGatewaysException {
        APIIdentifier apiId = new APIIdentifier("admin", "API1", "1.0.0");
        Set<String> environments = new HashSet<String>();
        environments.add("Production");
        environments.add("Sandbox");

        API api = new API(apiId);
        api.setContext("/test");
        api.setStatus(APIConstants.CREATED);
        api.setAsDefaultVersion(true);
        api.setEnvironments(environments);

        Mockito.when(apimgtDAO.getPublishedDefaultVersion(apiId)).thenReturn("1.0.0");
        Map<String, String> failedGWEnv = new HashMap<String, String>();
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        Mockito.when(artifactManager.newGovernanceArtifact(any(QName.class))).thenReturn(artifact);
        Mockito.when(APIUtil.createAPIArtifactContent(artifact, api)).thenReturn(artifact);
        RegistryService registryService = Mockito.mock(RegistryService.class);
        UserRegistry userRegistry = Mockito.mock(UserRegistry.class);
        ServiceReferenceHolder serviceReferenceHolder = TestUtils.getServiceReferenceHolder();
        RealmService realmService = Mockito.mock(RealmService.class);
        TenantManager tenantManager = Mockito.mock(TenantManager.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getRegistryService()).thenReturn(registryService);
        Mockito.when(registryService.getConfigSystemRegistry(Mockito.anyInt())).thenReturn(userRegistry);
        Mockito.when(serviceReferenceHolder.getRealmService()).thenReturn(realmService);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        apiProvider.addAPI(api);

        //Mock Updating API
        Resource apiSourceArtifact = Mockito.mock(Resource.class);
        Mockito.when(apiSourceArtifact.getUUID()).thenReturn("12640983654");
        String apiSourcePath = "path";
        PowerMockito.when(APIUtil.getAPIPath(api.getId())).thenReturn(apiSourcePath);
        PowerMockito.when(apiProvider.registry.get(apiSourcePath)).thenReturn(apiSourceArtifact);
        Mockito.when(artifact.getAttribute(APIConstants.API_OVERVIEW_STATUS)).thenReturn("CREATED");
        Mockito.when(artifactManager.getGenericArtifact(apiSourceArtifact.getUUID())).thenReturn(artifact);
        Mockito.when(gatewayManager.isAPIPublished(api, "carbon.super")).thenReturn(false);
        PowerMockito.when(APIUtil.createAPIArtifactContent(artifact, api)).thenReturn(artifact);
        Mockito.when(artifact.getId()).thenReturn("12640983654");
        PowerMockito.when(GovernanceUtils.getArtifactPath(apiProvider.registry, "12640983654")).
                thenReturn(apiSourcePath);
        Mockito.doNothing().when(artifactManager).updateGenericArtifact(artifact);
        apiProvider.updateAPIforStateChange(apiId, APIConstants.PUBLISHED, failedGWEnv);
        //From the 2 environments, only 1 was successful
        Assert.assertEquals(2, api.getEnvironments().size());
    }

    @Test
    public void testUpdateAPIforStateChange_ToRetired()
            throws RegistryException, UserStoreException, APIManagementException, FaultGatewaysException,
            WorkflowException {
        APIIdentifier apiId = new APIIdentifier("admin", "API1", "1.0.0");
        Set<String> environments = new HashSet<String>();
        environments.add("Production");
        environments.add("Sandbox");

        API api = new API(apiId);
        api.setContext("/test");
        api.setStatus(APIConstants.CREATED);
        api.setAsDefaultVersion(true);
        api.setEnvironments(environments);

        Mockito.when(apimgtDAO.getPublishedDefaultVersion(apiId)).thenReturn("1.0.0");
        Map<String, String> failedGWEnv = new HashMap<String, String>();
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        Mockito.when(artifactManager.newGovernanceArtifact(any(QName.class))).thenReturn(artifact);
        Mockito.when(APIUtil.createAPIArtifactContent(artifact, api)).thenReturn(artifact);
        RegistryService registryService = Mockito.mock(RegistryService.class);
        UserRegistry userRegistry = Mockito.mock(UserRegistry.class);
        ServiceReferenceHolder serviceReferenceHolder = TestUtils.getServiceReferenceHolder();
        RealmService realmService = Mockito.mock(RealmService.class);
        TenantManager tenantManager = Mockito.mock(TenantManager.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getRegistryService()).thenReturn(registryService);
        Mockito.when(registryService.getConfigSystemRegistry(Mockito.anyInt())).thenReturn(userRegistry);
        Mockito.when(serviceReferenceHolder.getRealmService()).thenReturn(realmService);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        apiProvider.addAPI(api);

        //Mock Updating API
        Resource apiSourceArtifact = Mockito.mock(Resource.class);
        Mockito.when(apiSourceArtifact.getUUID()).thenReturn("12640983654");
        String apiSourcePath = "path";
        PowerMockito.when(APIUtil.getAPIPath(api.getId())).thenReturn(apiSourcePath);
        PowerMockito.when(apiProvider.registry.get(apiSourcePath)).thenReturn(apiSourceArtifact);
        Mockito.when(artifact.getAttribute(APIConstants.API_OVERVIEW_STATUS)).thenReturn("CREATED");
        Mockito.when(artifactManager.getGenericArtifact(apiSourceArtifact.getUUID())).thenReturn(artifact);
        Mockito.when(gatewayManager.isAPIPublished(api, "carbon.super")).thenReturn(false);
        PowerMockito.when(APIUtil.createAPIArtifactContent(artifact, api)).thenReturn(artifact);
        Mockito.when(artifact.getId()).thenReturn("12640983654");
        PowerMockito.when(GovernanceUtils.getArtifactPath(apiProvider.registry, "12640983654")).
                thenReturn(apiSourcePath);
        Mockito.doNothing().when(artifactManager).updateGenericArtifact(artifact);
        WorkflowExecutorFactory wfe = PowerMockito.mock(WorkflowExecutorFactory.class);
        Mockito.when(WorkflowExecutorFactory.getInstance()).thenReturn(wfe);
        boolean status = apiProvider.updateAPIforStateChange(apiId, APIConstants.RETIRED, failedGWEnv);

        Assert.assertEquals(2, api.getEnvironments().size());
        Assert.assertEquals(true, status);
    }

    @Test
    public void testUpdateAPIforStateChange_ToPublishedWithFaultyGWs() throws RegistryException, UserStoreException,
            APIManagementException, FaultGatewaysException {
        APIIdentifier apiId = new APIIdentifier("admin", "API1", "1.0.0");
        Set<String> environments = new HashSet<String>();
        environments.add("Production");
        environments.add("Sandbox");

        API api = new API(apiId);
        api.setContext("/test");
        api.setStatus(APIConstants.CREATED);
        api.setAsDefaultVersion(true);
        api.setEnvironments(environments);

        Mockito.when(apimgtDAO.getPublishedDefaultVersion(apiId)).thenReturn("1.0.0");

        Map<String, String> failedGWEnv = new HashMap<String, String>();
        failedGWEnv.put("Production", "Failed to publish");
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        Mockito.when(artifactManager.newGovernanceArtifact(any(QName.class))).thenReturn(artifact);
        Mockito.when(APIUtil.createAPIArtifactContent(artifact, api)).thenReturn(artifact);
        RegistryService registryService = Mockito.mock(RegistryService.class);
        UserRegistry userRegistry = Mockito.mock(UserRegistry.class);
        ServiceReferenceHolder serviceReferenceHolder = TestUtils.getServiceReferenceHolder();
        RealmService realmService = Mockito.mock(RealmService.class);
        TenantManager tenantManager = Mockito.mock(TenantManager.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getRegistryService()).thenReturn(registryService);
        Mockito.when(registryService.getConfigSystemRegistry(Mockito.anyInt())).thenReturn(userRegistry);
        Mockito.when(serviceReferenceHolder.getRealmService()).thenReturn(realmService);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        apiProvider.addAPI(api);

        //Mock Updating API
        Resource apiSourceArtifact = Mockito.mock(Resource.class);
        Mockito.when(apiSourceArtifact.getUUID()).thenReturn("12640983654");
        String apiSourcePath = "path";
        PowerMockito.when(APIUtil.getAPIPath(api.getId())).thenReturn(apiSourcePath);
        PowerMockito.when(apiProvider.registry.get(apiSourcePath)).thenReturn(apiSourceArtifact);
        Mockito.when(artifact.getAttribute(APIConstants.API_OVERVIEW_STATUS)).thenReturn("CREATED");
        Mockito.when(artifactManager.getGenericArtifact(apiSourceArtifact.getUUID())).thenReturn(artifact);
        Mockito.when(gatewayManager.isAPIPublished(api, "carbon.super")).thenReturn(false);
        PowerMockito.when(APIUtil.createAPIArtifactContent(artifact, api)).thenReturn(artifact);
        Mockito.when(artifact.getId()).thenReturn("12640983654");
        PowerMockito.when(GovernanceUtils.getArtifactPath(apiProvider.registry, "12640983654")).
                thenReturn(apiSourcePath);
        Mockito.doNothing().when(artifactManager).updateGenericArtifact(artifact);
        try {
            apiProvider.updateAPIforStateChange(apiId, APIConstants.PUBLISHED, failedGWEnv);
        } catch(FaultGatewaysException e) {
            Assert.assertTrue(e.getFaultMap().contains("Failed to publish"));
        }
        //From the 2 environments, only 1 was successful
        Assert.assertEquals(1, api.getEnvironments().size());
    }

    @Test
    public void testUpdateAPIforStateChange_ToRetiredWithFaultyGWs() throws RegistryException, UserStoreException,
            APIManagementException, FaultGatewaysException {
        APIIdentifier apiId = new APIIdentifier("admin", "API1", "1.0.0");
        Set<String> environments = new HashSet<String>();
        environments.add("Production");

        API api = new API(apiId);
        api.setContext("/test");
        api.setStatus(APIConstants.CREATED);
        api.setAsDefaultVersion(true);
        api.setEnvironments(environments);

        Mockito.when(apimgtDAO.getPublishedDefaultVersion(apiId)).thenReturn("1.0.0");

        Map<String, String> failedGWEnv = new HashMap<String, String>();
        failedGWEnv.put("Sandbox", "Failed to un-publish");

        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        Mockito.when(artifactManager.newGovernanceArtifact(any(QName.class))).thenReturn(artifact);
        Mockito.when(APIUtil.createAPIArtifactContent(artifact, api)).thenReturn(artifact);
        RegistryService registryService = Mockito.mock(RegistryService.class);
        UserRegistry userRegistry = Mockito.mock(UserRegistry.class);
        ServiceReferenceHolder serviceReferenceHolder = TestUtils.getServiceReferenceHolder();
        RealmService realmService = Mockito.mock(RealmService.class);
        TenantManager tenantManager = Mockito.mock(TenantManager.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getRegistryService()).thenReturn(registryService);
        Mockito.when(registryService.getConfigSystemRegistry(Mockito.anyInt())).thenReturn(userRegistry);
        Mockito.when(serviceReferenceHolder.getRealmService()).thenReturn(realmService);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        apiProvider.addAPI(api);

        //Mock Updating API
        Resource apiSourceArtifact = Mockito.mock(Resource.class);
        Mockito.when(apiSourceArtifact.getUUID()).thenReturn("12640983654");
        String apiSourcePath = "path";
        PowerMockito.when(APIUtil.getAPIPath(api.getId())).thenReturn(apiSourcePath);
        PowerMockito.when(apiProvider.registry.get(apiSourcePath)).thenReturn(apiSourceArtifact);
        Mockito.when(artifact.getAttribute(APIConstants.API_OVERVIEW_STATUS)).thenReturn("CREATED");
        Mockito.when(artifactManager.getGenericArtifact(apiSourceArtifact.getUUID())).thenReturn(artifact);
        Mockito.when(gatewayManager.isAPIPublished(api, "carbon.super")).thenReturn(false);
        Mockito.when(artifact.getId()).thenReturn("12640983654");
        PowerMockito.when(GovernanceUtils.getArtifactPath(apiProvider.registry, "12640983654")).
                thenReturn(apiSourcePath);
        Mockito.doNothing().when(artifactManager).updateGenericArtifact(artifact);
        WorkflowExecutorFactory wfe = PowerMockito.mock(WorkflowExecutorFactory.class);
        Mockito.when(WorkflowExecutorFactory.getInstance()).thenReturn(wfe);
        try {
            apiProvider.updateAPIforStateChange(apiId, APIConstants.RETIRED, failedGWEnv);
        } catch(FaultGatewaysException e) {
            Assert.assertTrue(e.getFaultMap().contains("Failed to un-publish"));
        }

        //API was going to be removed from 1 gateway, but removal has failed. Therefore API is available
        // in both environments
        Assert.assertEquals(2, api.getEnvironments().size());
    }

    @Test
    public void testUpdateAPIforStateChange_InvalidAPI() throws RegistryException, UserStoreException,
            APIManagementException, FaultGatewaysException {
        APIIdentifier apiId = new APIIdentifier("admin", "API1", "1.0.0");
        Set<String> environments = new HashSet<String>();
        environments.add("Production");
        environments.add("Sandbox");

        API api = new API(apiId);
        api.setContext("/test");
        api.setStatus(APIConstants.CREATED);
        api.setAsDefaultVersion(true);
        api.setEnvironments(environments);

        Mockito.when(apimgtDAO.getPublishedDefaultVersion(apiId)).thenReturn("1.0.0");
        Map<String, String> failedGWEnv = new HashMap<String, String>();
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        try {
            apiProvider.updateAPIforStateChange(apiId, APIConstants.PUBLISHED, failedGWEnv);
        } catch(APIManagementException e) {
            Assert.assertEquals("Couldn't find an API with the name-" + apiId.getApiName() + "version-"
                    + apiId.getVersion(), e.getMessage());
        }
    }

    @Test
    public void testGetAllPaginatedAPIs() throws RegistryException, UserStoreException, APIManagementException {
        APIIdentifier apiId1 = new APIIdentifier("admin", "API1", "1.0.0");
        API api1 = new API(apiId1);
        api1.setContext("/test");

        APIIdentifier apiId2 = new APIIdentifier("admin", "API2", "1.0.0");
        API api2 = new API(apiId2);
        api2.setContext("/test1");

        PaginationContext paginationCtx = Mockito.mock(PaginationContext.class);
        PowerMockito.when(PaginationContext.getInstance()).thenReturn(paginationCtx);
        Mockito.when(paginationCtx.getLength()).thenReturn(2);

        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        ServiceReferenceHolder sh = TestUtils.getServiceReferenceHolder();
        RegistryService registryService = Mockito.mock(RegistryService.class);
        PowerMockito.when(sh.getRegistryService()).thenReturn(registryService);
        UserRegistry userReg = Mockito.mock(UserRegistry.class);
        PowerMockito.when(registryService.getGovernanceUserRegistry(CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME,
                -1234)).thenReturn(userReg);
        PowerMockito.when(APIUtil.getArtifactManager(userReg, APIConstants.API_KEY)).thenReturn(artifactManager);

        APIManagerConfigurationService amConfigService = Mockito.mock(APIManagerConfigurationService.class);
        APIManagerConfiguration amConfig = Mockito.mock(APIManagerConfiguration.class);
        PowerMockito.when(sh.getAPIManagerConfigurationService()).thenReturn(amConfigService);
        PowerMockito.when(amConfigService.getAPIManagerConfiguration()).thenReturn(amConfig);
        PowerMockito.when(amConfig.getFirstProperty(APIConstants.API_PUBLISHER_APIS_PER_PAGE)).thenReturn("2");
        RealmService realmService = Mockito.mock(RealmService.class);
        TenantManager tm = Mockito.mock(TenantManager.class);
        PowerMockito.when(sh.getRealmService()).thenReturn(realmService);
        PowerMockito.when(realmService.getTenantManager()).thenReturn(tm);
        PowerMockito.when(tm.getTenantId("carbon.super")).thenReturn(-1234);

        GenericArtifact genericArtifact1 = Mockito.mock(GenericArtifact.class);
        GenericArtifact genericArtifact2 = Mockito.mock(GenericArtifact.class);
        Mockito.when(APIUtil.getAPI(genericArtifact1)).thenReturn(api1);
        Mockito.when(APIUtil.getAPI(genericArtifact2)).thenReturn(api2);
        List<GovernanceArtifact> governanceArtifacts = new ArrayList<GovernanceArtifact>();
        governanceArtifacts.add(genericArtifact1);
        governanceArtifacts.add(genericArtifact2);
        List<GovernanceArtifact> governanceArtifacts1 = new ArrayList<GovernanceArtifact>();
        PowerMockito.when(GovernanceUtils
                .findGovernanceArtifacts(Mockito.anyMap(), any(Registry.class), Mockito.anyString()))
                .thenReturn(governanceArtifacts, governanceArtifacts1);
        Map<String, Object> result = apiProvider.getAllPaginatedAPIs("carbon.super", 0, 10);
        List<API> apiList = (List<API>) result.get("apis");
        Assert.assertEquals(2, apiList.size());
        Assert.assertEquals("API1", apiList.get(0).getId().getApiName());
        Assert.assertEquals("API2", apiList.get(1).getId().getApiName());
        Assert.assertEquals(2, result.get("totalLength"));

        //No APIs available
        Map<String, Object> result1 = apiProvider.getAllPaginatedAPIs("carbon.super", 0, 10);
        List<API> apiList1 = (List<API>) result1.get("apis");
        Assert.assertEquals(0, apiList1.size());
        //Registry Exception while retrieving artifacts
        Mockito.when(artifactManager.findGenericArtifacts(Matchers.anyMap())).thenThrow(RegistryException.class);
        try {
            apiProvider.getAllPaginatedAPIs("carbon.super", 0, 10);
        } catch (APIManagementException e) {
            Assert.assertEquals("Failed to get all APIs", e.getMessage());
        }
    }

    @Test
    public void testAddDocumentationContent() throws Exception {
        APIIdentifier apiId = new APIIdentifier("admin", "API1", "1.0.0");
        API api = new API(apiId);

        String docName = "HowTo";
        Documentation doc = new Documentation(DocumentationType.HOWTO, docName);
        doc.setVisibility(DocumentVisibility.API_LEVEL);
        String docPath = "/apimgt/applicationdata/provider/admin/API1/1.0.0/documentation/contents";
        String documentationPath = docPath + docName;
        String contentPath = docPath + APIConstants.INLINE_DOCUMENT_CONTENT_DIR +
                RegistryConstants.PATH_SEPARATOR + docName;

        Mockito.when(APIUtil.getAPIDocPath(apiId)).thenReturn(docPath);
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        Resource docResource = Mockito.mock(Resource.class);
        Mockito.when(docResource.getUUID()).thenReturn("678ghk");
        Mockito.when(apiProvider.registry.get(documentationPath)).thenReturn(docResource);

        GenericArtifact docArtifact = Mockito.mock(GenericArtifact.class);
        PowerMockito.whenNew(GenericArtifactManager.class).withAnyArguments().thenReturn(artifactManager);
        Mockito.when(artifactManager.getGenericArtifact("678ghk")).thenReturn(docArtifact);
        Mockito.when(APIUtil.getDocumentation(docArtifact)).thenReturn(doc);

        Resource docContent = Mockito.mock(Resource.class);
        Mockito.when(apiProvider.registry.resourceExists(contentPath)).thenReturn(true, false);
        Mockito.when(apiProvider.registry.get(contentPath)).thenReturn(docContent);
        Mockito.when(apiProvider.registry.newResource()).thenReturn(docContent);
        apiProvider.addDocumentationContent(api, docName, "content");

        RegistryAuthorizationManager registryAuthorizationManager = Mockito.mock(RegistryAuthorizationManager.class);
        PowerMockito.whenNew(RegistryAuthorizationManager.class).withAnyArguments()
                .thenReturn(registryAuthorizationManager);
        apiProvider.tenantDomain = "carbon.super";
        doc.setVisibility(DocumentVisibility.OWNER_ONLY);
        apiProvider.addDocumentationContent(api, docName, "content");
        doc.setVisibility(DocumentVisibility.PRIVATE);
        apiProvider.addDocumentationContent(api, docName, "content");
        Mockito.doThrow(RegistryException.class).when(apiProvider.registry).put(Matchers.anyString(),
                any(Resource.class));
        try {
            apiProvider.addDocumentationContent(api, docName, "content");
        } catch (APIManagementException e) {
            String msg = "Failed to add the documentation content of : "
                    + docName + " of API :" + apiId.getApiName();
            Assert.assertEquals(msg, e.getMessage());
        }
    }

    @Test
    public void testSearchAPIs() throws APIManagementException, RegistryException {
        //APIs of the provider
        API api1 = new API(new APIIdentifier("admin", "API1", "1.0.1"));
        api1.setContext("api1context");
        api1.setStatus(APIConstants.PUBLISHED);
        api1.setDescription("API 1 Desciption");

        Set<URITemplate> uriTemplates = new HashSet<URITemplate>();

        URITemplate uriTemplate1 = new URITemplate();
        uriTemplate1.setHTTPVerb("POST");
        uriTemplate1.setAuthType("Application");
        uriTemplate1.setUriTemplate("/add");
        uriTemplate1.setThrottlingTier("Gold");
        uriTemplates.add(uriTemplate1);

        api1.setUriTemplates(uriTemplates);

        API api2 = new API(new APIIdentifier("admin", "API2", "1.0.0"));
        api2.setContext("api2context");
        api2.setStatus(APIConstants.CREATED);
        api2.setDescription("API 2 Desciption");

        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        prepareForGetAPIsByProvider(artifactManager, apiProvider, "admin", api1, api2);

        //Search by Name matching both APIs
        List<API> foundApiList0 = apiProvider.searchAPIs("API", "Name", "admin");
        Assert.assertNotNull(foundApiList0);
        Assert.assertEquals(2, foundApiList0.size());

        //Search by exact name
        List<API> foundApiList1 = apiProvider.searchAPIs("API1", "Name", "admin");
        Assert.assertNotNull(foundApiList1);
        Assert.assertEquals(1, foundApiList1.size());

        //Search by exact provider
        List<API> foundApiList2 = apiProvider.searchAPIs("admin", "Provider", "admin");
        Assert.assertNotNull(foundApiList2);
        Assert.assertEquals(2, foundApiList2.size());

        //Search by exact version
        List<API> foundApiList3 = apiProvider.searchAPIs("1.0.0", "Version", "admin");
        Assert.assertNotNull(foundApiList3);
        Assert.assertEquals(1, foundApiList3.size());

        //Search by exact context
        List<API> foundApiList4 = apiProvider.searchAPIs("api1context", "Context", "admin");
        Assert.assertNotNull(foundApiList4);
        Assert.assertEquals(1, foundApiList4.size());

        //Search by exact context
        List<API> foundApiList5 = apiProvider.searchAPIs("api2context", "Context", "admin");
        Assert.assertNotNull(foundApiList5);
        Assert.assertEquals(1, foundApiList5.size());

        //Search by wrong context
        List<API> foundApiList6 = apiProvider.searchAPIs("test", "Context", "admin");
        Assert.assertNotNull(foundApiList6);
        Assert.assertEquals(0, foundApiList6.size());

        //Search by status
        List<API> foundApiList7 = apiProvider.searchAPIs("Published", "Status", "admin");
        Assert.assertNotNull(foundApiList7);
        Assert.assertEquals(1, foundApiList7.size());

        //Search by Description
        List<API> foundApiList8 = apiProvider.searchAPIs("API 1 Desciption", "Description", "admin");
        Assert.assertNotNull(foundApiList8);
        Assert.assertEquals(1, foundApiList8.size());

        //Search by Description
        List<API> foundApiList9 = apiProvider.searchAPIs("API", "Description", "admin");
        Assert.assertNotNull(foundApiList9);
        Assert.assertEquals(2, foundApiList9.size());

        //Search by Subcontext
        List<API> foundApiList10 = apiProvider.searchAPIs("add", "Subcontext", "admin");
        Assert.assertNotNull(foundApiList10);
        Assert.assertEquals(1, foundApiList10.size());
    }

    @Test
    public void testSearchAPIs_NoProviderId() throws APIManagementException, RegistryException {
        API api1 = new API(new APIIdentifier("admin", "API1", "1.0.1"));
        api1.setContext("api1context");
        api1.setStatus(APIConstants.PUBLISHED);
        api1.setDescription("API 1 Desciption");

        Set<URITemplate> uriTemplates = new HashSet<URITemplate>();

        URITemplate uriTemplate1 = new URITemplate();
        uriTemplate1.setHTTPVerb("POST");
        uriTemplate1.setAuthType("Application");
        uriTemplate1.setUriTemplate("/add");
        uriTemplate1.setThrottlingTier("Gold");
        uriTemplates.add(uriTemplate1);

        api1.setUriTemplates(uriTemplates);

        API api2 = new API(new APIIdentifier("admin", "API2", "1.0.0"));
        api2.setContext("api2context");
        api2.setStatus(APIConstants.CREATED);
        api2.setDescription("API 2 Desciption");

        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        GenericArtifact genericArtifact1 = Mockito.mock(GenericArtifact.class);
        GenericArtifact genericArtifact2 = Mockito.mock(GenericArtifact.class);
        Mockito.when(genericArtifact1.getAttribute(APIConstants.API_OVERVIEW_NAME)).thenReturn("API1");
        Mockito.when(genericArtifact1.getAttribute(APIConstants.API_OVERVIEW_VERSION)).thenReturn("1.0.1");
        Mockito.when(genericArtifact1.getAttribute(APIConstants.API_OVERVIEW_CONTEXT)).thenReturn("api1context");
        Mockito.when(genericArtifact1.getAttribute(APIConstants.API_OVERVIEW_DESCRIPTION)).thenReturn(
                "API 1 Desciption");
        Mockito.when(genericArtifact1.getAttribute(APIConstants.API_OVERVIEW_PROVIDER)).thenReturn("admin");
        Mockito.when(genericArtifact1.getAttribute(APIConstants.API_OVERVIEW_STATUS)).thenReturn("Published");
        Mockito.when(genericArtifact2.getAttribute(APIConstants.API_OVERVIEW_NAME)).thenReturn("API2");
        Mockito.when(genericArtifact2.getAttribute(APIConstants.API_OVERVIEW_VERSION)).thenReturn("1.0.0");
        Mockito.when(genericArtifact2.getAttribute(APIConstants.API_OVERVIEW_CONTEXT)).thenReturn("api2context");
        Mockito.when(genericArtifact2.getAttribute(APIConstants.API_OVERVIEW_DESCRIPTION)).thenReturn(
                "API 2 Desciption");
        Mockito.when(genericArtifact2.getAttribute(APIConstants.API_OVERVIEW_PROVIDER)).thenReturn("admin");
        Mockito.when(genericArtifact2.getAttribute(APIConstants.API_OVERVIEW_STATUS)).thenReturn("Created");
        Mockito.when(APIUtil.getAPI(genericArtifact1, apiProvider.registry)).thenReturn(api1);
        Mockito.when(APIUtil.getAPI(genericArtifact2, apiProvider.registry)).thenReturn(api2);
        Mockito.when(APIUtil.getAPI(genericArtifact1)).thenReturn(api1);
        Mockito.when(APIUtil.getAPI(genericArtifact2)).thenReturn(api2);
        GenericArtifact[] genericArtifacts = {genericArtifact1, genericArtifact2};
        Mockito.when(artifactManager.getAllGenericArtifacts()).thenReturn(genericArtifacts);

        //Search by Name matching both APIs
        List<API> foundApiList0 = apiProvider.searchAPIs("API", "Name", null);
        Assert.assertNotNull(foundApiList0);
        Assert.assertEquals(2, foundApiList0.size());

        //Search by exact name
        List<API> foundApiList1 = apiProvider.searchAPIs("API1", "Name", null);
        Assert.assertNotNull(foundApiList1);
        Assert.assertEquals(1, foundApiList1.size());

        //Search by exact provider
        List<API> foundApiList2 = apiProvider.searchAPIs("admin", "Provider", null);
        Assert.assertNotNull(foundApiList2);
        Assert.assertEquals(2, foundApiList2.size());

        //Search by exact version
        List<API> foundApiList3 = apiProvider.searchAPIs("1.0.0", "Version", null);
        Assert.assertNotNull(foundApiList3);
        Assert.assertEquals(1, foundApiList3.size());

        //Search by exact context
        List<API> foundApiList4 = apiProvider.searchAPIs("api1context", "Context", null);
        Assert.assertNotNull(foundApiList4);
        Assert.assertEquals(1, foundApiList4.size());

        //Search by exact context
        List<API> foundApiList5 = apiProvider.searchAPIs("api2context", "Context", null);
        Assert.assertNotNull(foundApiList5);
        Assert.assertEquals(1, foundApiList5.size());

        //Search by wrong context
        List<API> foundApiList6 = apiProvider.searchAPIs("test", "Context", null);
        Assert.assertNotNull(foundApiList6);
        Assert.assertEquals(0, foundApiList6.size());

        //Search by status
        List<API> foundApiList7 = apiProvider.searchAPIs("Published", "Status", null);
        Assert.assertNotNull(foundApiList7);
        Assert.assertEquals(1, foundApiList7.size());

        //Search by Description
        List<API> foundApiList8 = apiProvider.searchAPIs("API 1 Desciption", "Description", null);
        Assert.assertNotNull(foundApiList8);
        Assert.assertEquals(1, foundApiList8.size());

        //Search by Description
        List<API> foundApiList9 = apiProvider.searchAPIs("API", "Description", null);
        Assert.assertNotNull(foundApiList9);
        Assert.assertEquals(2, foundApiList9.size());

        //Search by Subcontext
        List<API> foundApiList10 = apiProvider.searchAPIs("add", "Subcontext", null);
        Assert.assertNotNull(foundApiList10);
        Assert.assertEquals(1, foundApiList10.size());
    }

    @Test
    public void testSearchAPIs_WhenNoAPIs() throws APIManagementException, RegistryException {
        String providerId = "admin";
        String providerPath = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR + providerId;

        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        //Mock to return 0 APIs by provider
        Association[] associactions = {};
        Mockito.when(apiProvider.registry.getAssociations(providerPath, APIConstants.PROVIDER_ASSOCIATION)).
                thenReturn(associactions);

        List<API> foundApiList0 = apiProvider.searchAPIs("API", "Name", "admin");
        Assert.assertNotNull(foundApiList0);
        Assert.assertEquals(0, foundApiList0.size());
    }

    @Test
    public void testSearchAPIs_ForException() throws APIManagementException, RegistryException {
        String providerId = "admin";
        String providerPath = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR + providerId;

        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);

        //Mock to throw exception when retrieving APIs by provider
        Mockito.when(apiProvider.registry.getAssociations(providerPath, APIConstants.PROVIDER_ASSOCIATION)).
                thenThrow(RegistryException.class);
        try {
            apiProvider.searchAPIs("API", "Name", "admin");
        } catch (APIManagementException e) {
            Assert.assertEquals("Failed to search APIs with type", e.getMessage());
        }
    }

    @Test
    public void testSearchAPIs_NoProviderId_WhenNoAPIs() throws APIManagementException, RegistryException {
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        //Mock to return 0 APIs
        GenericArtifact[] genericArtifacts = {};
        Mockito.when(artifactManager.getAllGenericArtifacts()).thenReturn(genericArtifacts);
        List<API> foundApiList0 = apiProvider.searchAPIs("API", "Name", null);
        Assert.assertNotNull(foundApiList0);
        Assert.assertEquals(0, foundApiList0.size());
    }

    @Test
    public void testSearchAPIs_NoProviderId_ForException() throws APIManagementException, RegistryException {
        String providerId = "admin";
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        //Mock to throw exception when retrieving APIs
        Mockito.when(artifactManager.getAllGenericArtifacts()).thenThrow(RegistryException.class);
        try {
            apiProvider.searchAPIs("API", "Name", null);
        } catch (APIManagementException e) {
            Assert.assertEquals("Failed to search APIs with type", e.getMessage());
        }
    }

    @Test
    public void testGetCustomFaultSequences() throws Exception {
        APIIdentifier apiId = new APIIdentifier("admin", "API1", "1.0.1");
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        mockSequences(APIConstants.API_CUSTOM_FAULTSEQUENCE_LOCATION, APIConstants.API_CUSTOM_SEQUENCE_TYPE_FAULT,
                apiId);
        List<String> sequenceList = apiProvider.getCustomFaultSequences(apiId);
        Assert.assertNotNull(sequenceList);
        Assert.assertEquals(2, sequenceList.size());
        Assert.assertTrue(sequenceList.contains("fault-seq"));
        Assert.assertTrue(sequenceList.contains("custom-fault-seq"));

        // OMException when building OMElement
        PowerMockito.when(APIUtil.buildOMElement(any(InputStream.class))).thenThrow(new OMException());
        apiProvider.getCustomFaultSequences(apiId);

        //org.wso2.carbon.registry.api.RegistryException
        ServiceReferenceHolder sh = PowerMockito.mock(ServiceReferenceHolder.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(sh);
        RegistryService registryService = Mockito.mock(RegistryService.class);
        PowerMockito.when(sh.getRegistryService()).thenReturn(registryService);
        UserRegistry registry = Mockito.mock(UserRegistry.class);
        PowerMockito.when(registryService.getGovernanceSystemRegistry(Matchers.anyInt())).thenReturn(registry);
        Mockito.when(registry.resourceExists(APIConstants.API_CUSTOM_FAULTSEQUENCE_LOCATION)).thenThrow(
                org.wso2.carbon.registry.api.RegistryException.class);
        String msg = "Error while processing the " + APIConstants.API_CUSTOM_SEQUENCE_TYPE_FAULT
                + " sequences of " + apiId + " in the registry";
        try {
            apiProvider.getCustomFaultSequences(apiId);
        } catch (APIManagementException e) {
            Assert.assertEquals(msg, e.getMessage());
        }

        //Registry Exception
        PowerMockito.when(registryService.getGovernanceSystemRegistry(Matchers.anyInt())).thenThrow(
                RegistryException.class);
        try {
            apiProvider.getCustomFaultSequences(apiId);
        } catch (APIManagementException e) {
            Assert.assertEquals("Error while retrieving registry for tenant -1", e.getMessage());
        }
    }

    @Test
    public void testGetCustomInSequences() throws Exception {
        APIIdentifier apiId = new APIIdentifier("admin", "API1", "1.0.1");
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        mockSequences(APIConstants.API_CUSTOM_INSEQUENCE_LOCATION, APIConstants.API_CUSTOM_SEQUENCE_TYPE_IN,
                apiId);
        List<String> sequenceList = apiProvider.getCustomInSequences(apiId);
        Assert.assertNotNull(sequenceList);
        Assert.assertEquals(2, sequenceList.size());
        Assert.assertTrue(sequenceList.contains("fault-seq"));
        Assert.assertTrue(sequenceList.contains("custom-fault-seq"));

        // OMException when building OMElement
        PowerMockito.when(APIUtil.buildOMElement(any(InputStream.class))).thenThrow(new OMException());
        apiProvider.getCustomInSequences(apiId);

        //org.wso2.carbon.registry.api.RegistryException
        ServiceReferenceHolder sh = PowerMockito.mock(ServiceReferenceHolder.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(sh);
        RegistryService registryService = Mockito.mock(RegistryService.class);
        PowerMockito.when(sh.getRegistryService()).thenReturn(registryService);
        UserRegistry registry = Mockito.mock(UserRegistry.class);
        PowerMockito.when(registryService.getGovernanceSystemRegistry(Matchers.anyInt())).thenReturn(registry);
        Mockito.when(registry.resourceExists(APIConstants.API_CUSTOM_INSEQUENCE_LOCATION)).thenThrow(
                org.wso2.carbon.registry.api.RegistryException.class);
        String msg = "Issue is in getting custom InSequences from the Registry";
        try {
            apiProvider.getCustomInSequences(apiId);
        } catch (APIManagementException e) {
            Assert.assertEquals(msg, e.getMessage());
        }
        //Registry Exception
        PowerMockito.when(registryService.getGovernanceSystemRegistry(Matchers.anyInt())).thenThrow(
                RegistryException.class);
        try {
            apiProvider.getCustomInSequences(apiId);
        } catch (APIManagementException e) {
            Assert.assertEquals(msg, e.getMessage());
        }
    }

    @Test
    public void testGetCustomOutSequences() throws Exception {
        APIIdentifier apiId = new APIIdentifier("admin", "API1", "1.0.1");
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        mockSequences(APIConstants.API_CUSTOM_OUTSEQUENCE_LOCATION, APIConstants.API_CUSTOM_SEQUENCE_TYPE_OUT,
                apiId);
        List<String> sequenceList = apiProvider.getCustomOutSequences(apiId);

        Assert.assertNotNull(sequenceList);
        Assert.assertEquals(2, sequenceList.size());
        Assert.assertTrue(sequenceList.contains("fault-seq"));
        Assert.assertTrue(sequenceList.contains("custom-fault-seq"));

        // OMException when building OMElement
        PowerMockito.when(APIUtil.buildOMElement(any(InputStream.class))).thenThrow(new OMException());
        apiProvider.getCustomOutSequences(apiId);

        //org.wso2.carbon.registry.api.RegistryException
        ServiceReferenceHolder sh = PowerMockito.mock(ServiceReferenceHolder.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(sh);
        RegistryService registryService = Mockito.mock(RegistryService.class);
        PowerMockito.when(sh.getRegistryService()).thenReturn(registryService);
        UserRegistry registry = Mockito.mock(UserRegistry.class);
        PowerMockito.when(registryService.getGovernanceSystemRegistry(Matchers.anyInt())).thenReturn(registry);
        Mockito.when(registry.resourceExists(APIConstants.API_CUSTOM_OUTSEQUENCE_LOCATION)).thenThrow(
                org.wso2.carbon.registry.api.RegistryException.class);
        String msg = "Issue is in getting custom OutSequences from the Registry";
        try {
            apiProvider.getCustomOutSequences(apiId);
        } catch (APIManagementException e) {
            Assert.assertEquals(msg, e.getMessage());
        }

        //Registry Exception
        PowerMockito.when(registryService.getGovernanceSystemRegistry(Matchers.anyInt())).thenThrow(
                RegistryException.class);
        try {
            apiProvider.getCustomOutSequences(apiId);
        } catch (APIManagementException e) {
            Assert.assertEquals(msg, e.getMessage());
        }
    }

    @Test
    public void testGetSequenceFile() throws Exception {
        APIIdentifier apiIdentifier = new APIIdentifier("admin", "API1", "1.0");
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null,null);
        mockSequences(APIConstants.API_CUSTOM_FAULTSEQUENCE_LOCATION, APIConstants.API_CUSTOM_SEQUENCE_TYPE_FAULT,
                apiIdentifier);

        String sequenceContent = apiProvider.getSequenceFileContent(apiIdentifier, "fault", "custom-fault-seq");
        Assert.assertNotNull(sequenceContent);

        ServiceReferenceHolder sh = PowerMockito.mock(ServiceReferenceHolder.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(sh);
        RegistryService registryService = Mockito.mock(RegistryService.class);
        PowerMockito.when(sh.getRegistryService()).thenReturn(registryService);
        UserRegistry registry = Mockito.mock(UserRegistry.class);
        PowerMockito.when(registryService.getGovernanceSystemRegistry(Matchers.anyInt())).thenReturn(registry);
        Mockito.when(registry.resourceExists(APIUtil.getSequencePath(apiIdentifier,
                APIConstants.API_CUSTOM_SEQUENCE_TYPE_FAULT))).thenThrow(
                org.wso2.carbon.registry.api.RegistryException.class);
        String msg = "Error while processing the " + APIConstants.API_CUSTOM_SEQUENCE_TYPE_FAULT +" sequences of " +
                apiIdentifier + " in the registry";
        try {
            apiProvider.getSequenceFileContent(apiIdentifier, "fault", "custom-fault-seq");
        } catch(APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains(msg));
        }
        //Registry Exception
        PowerMockito.when(registryService.getGovernanceSystemRegistry(Matchers.anyInt())).thenThrow(
                RegistryException.class);
        String msg1 =  "Error while retrieving registry for tenant -1";
        try {
            apiProvider.getSequenceFileContent(apiIdentifier, "fault", "custom-fault-seq");
        } catch(APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains(msg1));
        }
    }

    private void mockSequences(String seqLoc, String apiSeqLoc, APIIdentifier apiId) throws Exception {
        ServiceReferenceHolder sh = PowerMockito.mock(ServiceReferenceHolder.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(sh);
        RegistryService registryService = Mockito.mock(RegistryService.class);
        PowerMockito.when(sh.getRegistryService()).thenReturn(registryService);
        UserRegistry registry = Mockito.mock(UserRegistry.class);
        PowerMockito.when(registryService.getGovernanceSystemRegistry(Matchers.anyInt())).thenReturn(registry);
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

    private void mockSequencesMultiple(String seqLoc, String apiSeqLoc, APIIdentifier apiId) throws Exception {
        ServiceReferenceHolder sh = PowerMockito.mock(ServiceReferenceHolder.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(sh);
        RegistryService registryService = Mockito.mock(RegistryService.class);
        PowerMockito.when(sh.getRegistryService()).thenReturn(registryService);
        UserRegistry registry = Mockito.mock(UserRegistry.class);
        PowerMockito.when(registryService.getGovernanceSystemRegistry(Matchers.anyInt())).thenReturn(registry);
        Mockito.when(registry.resourceExists(seqLoc)).thenReturn(true);
        Collection seqCollection = Mockito.mock(Collection.class);
        Mockito.when(registry.get(seqLoc)).thenReturn(
                seqCollection);
        String[] seqChildPaths = {"path1","path2"};
        Mockito.when(seqCollection.getChildren()).thenReturn(seqChildPaths);
        Resource sequence = Mockito.mock(Resource.class);
        Mockito.when(registry.get(seqChildPaths[0])).thenReturn(sequence);
        InputStream responseStream = IOUtils.toInputStream("<sequence name=\"pqr\"></sequence>", "UTF-8");

        Resource sequence2 = Mockito.mock(Resource.class);
        Mockito.when(registry.get(seqChildPaths[1])).thenReturn(sequence2);
        InputStream responseStream2 = IOUtils.toInputStream("<sequence name=\"abc\"></sequence>", "UTF-8");

        OMElement seqElment = buildOMElement(responseStream);
        OMElement seqElment2 = buildOMElement(responseStream2);
        PowerMockito.when(APIUtil.buildOMElement(responseStream)).thenReturn(seqElment);
        PowerMockito.when(APIUtil.buildOMElement(responseStream2)).thenReturn(seqElment2);

        Mockito.when(sequence.getContentStream()).thenReturn(responseStream);
        Mockito.when(sequence2.getContentStream()).thenReturn(responseStream2);
    }

    /**
     * This method can be used when invoking getAPIsByProvider()
     */
    private void prepareForGetAPIsByProvider(GenericArtifactManager artifactManager, APIProviderImplWrapper apiProvider,
                                             String providerId, API api1, API api2) throws APIManagementException, RegistryException {
        String providerPath = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR + providerId;
        //Mocking API1 association
        Association association1 = Mockito.mock(Association.class);
        String apiPath1 = "/API1/1.0.0";
        Resource resource1 = Mockito.mock(Resource.class);
        String apiArtifactId1 = "76897689";
        Mockito.when(apiProvider.registry.resourceExists(apiPath1)).thenReturn(true);
        Mockito.when(association1.getDestinationPath()).thenReturn(apiPath1);
        Mockito.when(apiProvider.registry.get(apiPath1)).thenReturn(resource1);
        Mockito.when(resource1.getUUID()).thenReturn(apiArtifactId1);
        GenericArtifact apiArtifact1 = Mockito.mock(GenericArtifact.class);
        Mockito.when(artifactManager.getGenericArtifact(apiArtifactId1)).thenReturn(apiArtifact1);
        Mockito.when(APIUtil.getAPI(apiArtifact1, apiProvider.registry)).thenReturn(api1);

        //Mocking API2 association
        Association association2 = Mockito.mock(Association.class);
        String apiPath2 = "/API2/1.0.0";
        Resource resource2 = Mockito.mock(Resource.class);
        String apiArtifactId2 = "76897622";
        Mockito.when(apiProvider.registry.resourceExists(apiPath2)).thenReturn(true);
        Mockito.when(association2.getDestinationPath()).thenReturn(apiPath2);
        Mockito.when(apiProvider.registry.get(apiPath2)).thenReturn(resource2);
        Mockito.when(resource2.getUUID()).thenReturn(apiArtifactId2);
        GenericArtifact apiArtifact2 = Mockito.mock(GenericArtifact.class);
        Mockito.when(artifactManager.getGenericArtifact(apiArtifactId2)).thenReturn(apiArtifact2);
        Mockito.when(APIUtil.getAPI(apiArtifact2, apiProvider.registry)).thenReturn(api2);

        Association[] associactions = {association1, association2};
        Mockito.when(apiProvider.registry.getAssociations(providerPath, APIConstants.PROVIDER_ASSOCIATION)).
                thenReturn(associactions);
    }

    /**
     * This method can be used when invoking changeLifeCycleStatus()
     *
     */
    private void prepareForChangeLifeCycleStatus(APIProviderImplWrapper apiProvider, ApiMgtDAO apimgtDAO,
                                                 APIIdentifier apiId, GenericArtifact apiArtifact) throws GovernanceException, APIManagementException,
            FaultGatewaysException, WorkflowException {

        Mockito.when(APIUtil.getAPIArtifact(apiId, apiProvider.registry)).thenReturn(apiArtifact);
        Mockito.when(apiArtifact.getAttribute(APIConstants.API_OVERVIEW_PROVIDER)).thenReturn("admin");
        Mockito.when(apiArtifact.getAttribute(APIConstants.API_OVERVIEW_NAME)).thenReturn("API1");
        Mockito.when(apiArtifact.getAttribute(APIConstants.API_OVERVIEW_VERSION)).thenReturn("1.0.0");
        Mockito.when(apiArtifact.getLifecycleState()).thenReturn("CREATED");
        Mockito.when(apimgtDAO.getAPIID(apiId, null)).thenReturn(1);

        //Workflow has not started, this will trigger the executor
        WorkflowDTO wfDTO1 = Mockito.mock(WorkflowDTO.class);
        Mockito.when(wfDTO1.getStatus()).thenReturn(null);
        WorkflowDTO wfDTO2 = Mockito.mock(WorkflowDTO.class);
        Mockito.when(wfDTO2.getStatus()).thenReturn(WorkflowStatus.APPROVED);
        Mockito.when(apimgtDAO.retrieveWorkflowFromInternalReference("1",
                WorkflowConstants.WF_TYPE_AM_API_STATE)).thenReturn(wfDTO1, wfDTO2);
        ServiceReferenceHolder sh = TestUtils.getServiceReferenceHolder();
        APIManagerConfigurationService amConfigService = Mockito.mock(APIManagerConfigurationService.class);
        APIManagerConfiguration amConfig = Mockito.mock(APIManagerConfiguration.class);
        PowerMockito.when(sh.getAPIManagerConfigurationService()).thenReturn(amConfigService);
        PowerMockito.when(amConfigService.getAPIManagerConfiguration()).thenReturn(amConfig);
        WorkflowProperties workflowProperties = Mockito.mock(WorkflowProperties.class);
        Mockito.when(workflowProperties.getWorkflowCallbackAPI()).thenReturn("");
        Mockito.when(amConfig.getWorkflowProperties()).thenReturn(workflowProperties);

        WorkflowExecutorFactory wfe = PowerMockito.mock(WorkflowExecutorFactory.class);
        Mockito.when(WorkflowExecutorFactory.getInstance()).thenReturn(wfe);
        WorkflowExecutor apiStateWFExecutor = new APIStateChangeSimpleWorkflowExecutor();
        Mockito.when(wfe.getWorkflowExecutor(WorkflowConstants.WF_TYPE_AM_API_STATE)).thenReturn(apiStateWFExecutor);
        Mockito.when(APIUtil.isAnalyticsEnabled()).thenReturn(false);
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

    private LifecycleBean getLCBean() {
        LifecycleBean bean = new LifecycleBean();
        List<Property> lifecycleProps = new ArrayList<Property>();

        Property property1 = new Property();
        property1.setKey("registry.custom_lifecycle.checklist.option.APILifeCycle.1.item");
        String[] values1 = {"status:Created", "name:Requires re-subscription when publishing the API", "value:false",
                "order:1"};
        property1.setValues(values1);

        Property property2 = new Property();
        property2.setKey("registry.lifecycle.APILifeCycle.state");
        String[] values2 = {"Created"};
        property2.setValues(values2);

        Property property3 = new Property();
        property3.setKey("registry.custom_lifecycle.checklist.option.APILifeCycle.0.item.permission");
        String[] values3 = {"registry.custom_lifecycle.checklist.option.APILifeCycle.0.item.permission"};
        property3.setValues(values3);

        Property property4 = new Property();
        property4.setKey("registry.lifecycle.APILifeCycle.lastStateUpdatedTime");
        String[] values4 = {"2017-08-31 13:36:54.501"};
        property4.setValues(values4);

        Property property5 = new Property();
        property5.setKey("registry.custom_lifecycle.checklist.option.APILifeCycle.1.item.permission");
        String[] values5 = {"registry.custom_lifecycle.checklist.option.APILifeCycle.1.item.permission"};
        property5.setValues(values5);

        Property property6 = new Property();
        property6.setKey("registry.custom_lifecycle.checklist.option.APILifeCycle.0.item");
        String[] values6 = {"status:Created", "name:Deprecate old versions after publishing the API", "value:false",
                "order:0"};
        property6.setValues(values6);

        Property property7 = new Property();
        property7.setKey("registry.LC.name");
        String[] values7 = {"APILifeCycle"};
        property7.setValues(values7);

        lifecycleProps.add(property1);
        lifecycleProps.add(property2);
        lifecycleProps.add(property3);
        lifecycleProps.add(property4);
        lifecycleProps.add(property5);
        lifecycleProps.add(property6);

        Property[] propertyArr = new Property[lifecycleProps.size()];
        bean.setLifecycleProperties(lifecycleProps.toArray(propertyArr));
        String[] userRoles = {"publisher"};
        bean.setRolesOfUser(userRoles);
        return bean;
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
        doc1.setFilePath("/registry/resource/_system/governance/apimgt/applicationdata/provider/"
                + "files/provider/fileName");

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
     * This method tests adding file to documentation method.
     *
     * @throws GovernanceException    Governance Exception.
     * @throws APIManagementException API Management Exception.
     */
    @Test
    public void testAddFileToDocumentation() throws APIManagementException, RegistryException {

        APIIdentifier identifier = new APIIdentifier("admin-AT-carbon.super", "API1", "1.0.0");
        Set<String> environments = new HashSet<String>();

        Set<URITemplate> uriTemplates = new HashSet<URITemplate>();

        Tier tier = new Tier("Gold");
        Map<String, Tier> tiers = new TreeMap<>();
        tiers.put("Gold", tier);

        URITemplate uriTemplate1 = new URITemplate();
        uriTemplate1.setHTTPVerb("POST");
        uriTemplate1.setAuthType("Application");
        uriTemplate1.setUriTemplate("/add");
        uriTemplate1.setThrottlingTier("Gold");
        uriTemplates.add(uriTemplate1);

        final API api = new API(identifier);
        api.setStatus(APIConstants.CREATED);
        api.setVisibility("public");
        api.setAccessControl("all");
        api.setTransports("http,https");
        api.setContext("/test");
        api.setEnvironments(environments);
        api.setUriTemplates(uriTemplates);


        List<Documentation> documentationList = getDocumentationList();

        final APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, documentationList, null);
        RegistryService registryService = Mockito.mock(RegistryService.class);
        UserRegistry userRegistry = Mockito.mock(UserRegistry.class);
        ServiceReferenceHolder serviceReferenceHolder = TestUtils.getServiceReferenceHolder();
        RealmService realmService = Mockito.mock(RealmService.class);
        TenantManager tenantManager = Mockito.mock(TenantManager.class);

        Mockito.when(APIUtil.getTiers(APIConstants.TIER_RESOURCE_TYPE, "carbon.super")).thenReturn(tiers);
        Mockito.when(artifactManager.newGovernanceArtifact(any(QName.class))).thenReturn(artifact);
        Mockito.when(APIUtil.createAPIArtifactContent(artifact, api)).thenReturn(artifact);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getRegistryService()).thenReturn(registryService);
        Mockito.when(registryService.getConfigSystemRegistry(Mockito.anyInt())).thenReturn(userRegistry);
        Mockito.when(serviceReferenceHolder.getRealmService()).thenReturn(realmService);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        apiProvider.addAPI(api);

        String fileName = "test.txt";
        String contentType = "application/force-download";
        Documentation doc = new Documentation(DocumentationType.HOWTO, fileName);
        doc.setSourceType(DocumentSourceType.FILE);
        PowerMockito.when(APIUtil.getDocumentationFilePath(api.getId(), fileName)).thenReturn("filePath");
        InputStream inputStream = Mockito.mock(InputStream.class);

        apiProvider.addFileToDocumentation(api.getId(), doc, fileName, inputStream, contentType);
    }

    /**
     * This method tests adding file to documentation method when document source type is not file.
     *
     * @throws GovernanceException    Governance Exception.
     * @throws APIManagementException API Management Exception.
     */
    @Test(expected = APIManagementException.class)
    public void testAddFileToDocumentationWhenDocSourceTypeIsNotFile() throws APIManagementException,
            GovernanceException {

        APIIdentifier identifier = new APIIdentifier("admin-AT-carbon.super", "API1", "1.0.0");
        final APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);

        String fileName = "test.txt";
        String contentType = "application/force-download";
        Documentation doc = new Documentation(DocumentationType.HOWTO, fileName);
        doc.setSourceType(DocumentSourceType.INLINE);
        InputStream inputStream = Mockito.mock(InputStream.class);

        apiProvider.addFileToDocumentation(identifier, doc, fileName, inputStream, contentType);
    }

    /**
     * This method tests adding file to documentation method for exception.
     *
     * @throws GovernanceException    Governance Exception.
     * @throws APIManagementException API Management Exception.
     */
    @Test(expected = APIManagementException.class)
    public void testAddFileToDocumentationForException() throws Exception {

        APIIdentifier identifier = new APIIdentifier("admin-AT-carbon.super", "API1", "1.0.0");
        Set<String> environments = new HashSet<String>();

        Set<URITemplate> uriTemplates = new HashSet<URITemplate>();

        URITemplate uriTemplate1 = new URITemplate();
        uriTemplate1.setHTTPVerb("POST");
        uriTemplate1.setAuthType("Application");
        uriTemplate1.setUriTemplate("/add");
        uriTemplate1.setThrottlingTier("Gold");
        uriTemplates.add(uriTemplate1);

        final API api = new API(identifier);
        api.setStatus(APIConstants.CREATED);
        api.setVisibility("public");
        api.setAccessControl("all");
        api.setTransports("http,https");
        api.setContext("/test");
        api.setEnvironments(environments);
        api.setUriTemplates(uriTemplates);
        api.setVisibleRoles("role1 role2");

        List<Documentation> documentationList = getDocumentationList();
        final APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, documentationList, null);

        Mockito.when(artifactManager.newGovernanceArtifact(any(QName.class))).thenReturn(artifact);
        Mockito.when(APIUtil.createAPIArtifactContent(artifact, api)).thenReturn(artifact);
        apiProvider.addAPI(api);

        String fileName = "test.txt";
        String contentType = "application/force-download";
        Documentation doc = new Documentation(DocumentationType.HOWTO, fileName);
        doc.setSourceType(DocumentSourceType.FILE);
        PowerMockito.when(APIUtil.getDocumentationFilePath(api.getId(), fileName)).thenReturn("filePath");
        PowerMockito.doThrow(new APIManagementException("MultiTenantUserAdmin")).when(APIUtil.class,
                "setFilePermission", "filePath");
        InputStream inputStream = Mockito.mock(InputStream.class);
        apiProvider.addFileToDocumentation(api.getId(), doc, fileName, inputStream, contentType);
    }

    @Test
    public void testSaveGraphqlSchemaDefinition() throws Exception {
        Resource resource = new ResourceImpl();
        String resourcePath = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR + "admin" + RegistryConstants.PATH_SEPARATOR +
                "API1" + RegistryConstants.PATH_SEPARATOR + "1.0.0" + RegistryConstants.PATH_SEPARATOR;
        String schemaContent = "sample schema";
        APIIdentifier apiId = new APIIdentifier("admin", "API1", "1.0.0");
        API api = new API(apiId);
        Mockito.when(APIUtil.getGraphqlDefinitionFilePath("API1", "1.0.0", "admin")).thenReturn(resourcePath);

        Resource resourceMock = Mockito.mock(Resource.class);
        resourceMock.setContent(schemaContent);
        resourceMock.setMediaType(String.valueOf(ContentType.TEXT_PLAIN));

        ServiceReferenceHolder sh = PowerMockito.mock(ServiceReferenceHolder.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(sh);
        RegistryService registryService = Mockito.mock(RegistryService.class);
        PowerMockito.when(sh.getRegistryService()).thenReturn(registryService);
        UserRegistry userRegistry = Mockito.mock(UserRegistry.class);
        Mockito.when(userRegistry.newResource()).thenReturn(resource);

        PowerMockito.doNothing().when(APIUtil.class, "clearResourcePermissions", Mockito.any(), Mockito.any(),
                Mockito.anyInt());
        PowerMockito.doNothing().when(APIUtil.class, "setResourcePermissions", Mockito.any(), Mockito.any(),
                Mockito.any(),Mockito.any());

        GraphQLSchemaDefinition graphQLSchemaDefinition = Mockito.mock(GraphQLSchemaDefinition.class);
        PowerMockito.doCallRealMethod().when(graphQLSchemaDefinition).saveGraphQLSchemaDefinition(api, schemaContent, userRegistry);

        //org.wso2.carbon.registry.api.RegistryException
        Mockito.doThrow(RegistryException.class).when(registry).put(Matchers.anyString(), any(Resource.class));
        try {
            graphQLSchemaDefinition.saveGraphQLSchemaDefinition(api, schemaContent, registry);
        } catch (APIManagementException e) {
            String msg = "Error while adding Graphql Definition for API1-1.0.0";
            Assert.assertEquals(msg, e.getMessage());
        }
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
        // Create a new APIProviderImplWrapper object
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);

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

        // Create a new APIProviderImplWrapper object and make mock calls to the tenant config
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        RegistryService registryService = Mockito.mock(RegistryService.class);
        UserRegistry userRegistry = Mockito.mock(UserRegistry.class);
        Resource resource = Mockito.mock(Resource.class);
        Mockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getRegistryService()).thenReturn(registryService);
        Mockito.when(registryService.getConfigSystemRegistry(tenantId)).thenReturn(userRegistry);
        Mockito.when(userRegistry.resourceExists(APIConstants.API_TENANT_CONF_LOCATION)).thenReturn(true);
        Mockito.when(userRegistry.get(APIConstants.API_TENANT_CONF_LOCATION)).thenReturn(resource);
        Mockito.when(resource.getContent()).thenReturn(jsonObject);
        PowerMockito.mockStatic(APIUtil.class);
        Mockito.when(APIUtil.getSecurityAuditAttributesFromRegistry(tenantId)).thenReturn(jsonObject);

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
        // Create a new APIProviderImplWrapper object
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
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
        // Create a new APIProviderImplWrapper object
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);

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
        Mockito.when(registryService.getConfigSystemRegistry(tenantId)).thenReturn(userRegistry);
        Mockito.when(userRegistry.resourceExists(APIConstants.API_TENANT_CONF_LOCATION)).thenReturn(true);
        Mockito.when(userRegistry.get(APIConstants.API_TENANT_CONF_LOCATION)).thenReturn(resource);
        Mockito.when(resource.getContent()).thenReturn(jsonObject);
        PowerMockito.mockStatic(APIUtil.class);
        Mockito.when(APIUtil.getSecurityAuditAttributesFromRegistry(tenantId)).thenReturn(jsonObject);

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
}
