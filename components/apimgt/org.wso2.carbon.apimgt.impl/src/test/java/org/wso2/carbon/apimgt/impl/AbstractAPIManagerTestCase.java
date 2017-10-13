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

import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.io.IOUtils;
import org.apache.derby.iapi.services.io.ArrayInputStream;
import org.apache.http.entity.ContentType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIMgtResourceAlreadyExistsException;
import org.wso2.carbon.apimgt.api.APIMgtResourceNotFoundException;
import org.wso2.carbon.apimgt.api.BlockConditionNotFoundException;
import org.wso2.carbon.apimgt.api.PolicyNotFoundException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIKey;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.Documentation;
import org.wso2.carbon.apimgt.api.model.DocumentationType;
import org.wso2.carbon.apimgt.api.model.Mediation;
import org.wso2.carbon.apimgt.api.model.ResourceFile;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.api.model.Wsdl;
import org.wso2.carbon.apimgt.api.model.policy.APIPolicy;
import org.wso2.carbon.apimgt.api.model.policy.ApplicationPolicy;
import org.wso2.carbon.apimgt.api.model.policy.GlobalPolicy;
import org.wso2.carbon.apimgt.api.model.policy.PolicyConstants;
import org.wso2.carbon.apimgt.api.model.policy.SubscriptionPolicy;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.definitions.APIDefinitionFromSwagger20;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifactImpl;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.CollectionImpl;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.dataobjects.ResourceDO;
import org.wso2.carbon.registry.core.pagination.PaginationContext;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.UUID;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import static org.wso2.carbon.utils.ServerConstants.CARBON_HOME;

@RunWith (PowerMockRunner.class)
@PrepareForTest ({APIUtil.class, MultitenantUtils.class, PrivilegedCarbonContext.class, ServiceReferenceHolder.class,
        GovernanceUtils.class, PaginationContext.class, IOUtils.class, AXIOMUtil.class })
public class AbstractAPIManagerTestCase {

    public static final String SAMPLE_API_NAME = "test";
    public static final String API_PROVIDER = "admin";
    public static final String SAMPLE_API_VERSION = "1.0.0";
    public static final String SAMPLE_TENANT_DOMAIN = "carbon.super";
    public static final String SAMPLE_RESOURCE_ID = "xyz";
    public static final String SAMPLE_TENANT_DOMAIN_1 = "abc.com";
    private PrivilegedCarbonContext privilegedCarbonContext;
    private PaginationContext paginationContext;
    private ApiMgtDAO apiMgtDAO;
    private Registry registry;
    private GenericArtifactManager genericArtifactManager;
    private RegistryService registryService;
    private TenantManager tenantManager;
    private APIDefinitionFromSwagger20 apiDefinitionFromSwagger20;

    @Before
    public void init() {
        System.setProperty(CARBON_HOME, "");
        privilegedCarbonContext = Mockito.mock(PrivilegedCarbonContext.class);
        PowerMockito.mockStatic(PrivilegedCarbonContext.class);
        PowerMockito.when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(privilegedCarbonContext);
        PowerMockito.mockStatic(GovernanceUtils.class);
        paginationContext = Mockito.mock(PaginationContext.class);
        PowerMockito.mockStatic(PaginationContext.class);
        PowerMockito.when(PaginationContext.getInstance()).thenReturn(paginationContext);
        apiMgtDAO = Mockito.mock(ApiMgtDAO.class);
        registry = Mockito.mock(Registry.class);
        genericArtifactManager = Mockito.mock(GenericArtifactManager.class);
        registryService = Mockito.mock(RegistryService.class);
        tenantManager = Mockito.mock(TenantManager.class);
        apiDefinitionFromSwagger20 = Mockito.mock(APIDefinitionFromSwagger20.class);

    }

    @Test
    public void testGetAllApis() throws GovernanceException, APIManagementException {
        PowerMockito.mockStatic(APIUtil.class);
        APIIdentifier identifier = getAPIIdentifier(SAMPLE_API_NAME, API_PROVIDER, SAMPLE_API_VERSION);
        API api = new API(identifier);

        GenericArtifact[] genericArtifacts = new GenericArtifact[1];
        GenericArtifact genericArtifact = getGenericArtifact(SAMPLE_API_NAME, API_PROVIDER, SAMPLE_API_VERSION,
                "sample");
        genericArtifacts[0] = genericArtifact;
        Mockito.when(genericArtifactManager.getAllGenericArtifacts()).thenThrow(RegistryException.class)
                .thenReturn(genericArtifacts);
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(genericArtifactManager);
        abstractAPIManager.tenantDomain = SAMPLE_TENANT_DOMAIN;
        try {
            abstractAPIManager.getAllAPIs(); //error scenario
            Assert.fail("Registry exception not thrown for error scenario");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Failed to get APIs from the registry"));
        }
        BDDMockito.when(APIUtil.getAPI((GenericArtifact)Mockito.any())).thenThrow(APIManagementException.class)
                .thenReturn(api);
        Assert.assertEquals(abstractAPIManager.getAllAPIs().size(),0);
        abstractAPIManager.tenantDomain = SAMPLE_TENANT_DOMAIN_1;
        List<API> apis = abstractAPIManager.getAllAPIs();
        Assert.assertNotNull(apis);
        Assert.assertEquals(apis.size(), 1);

    }

    @Test
    public void testGetApi()
            throws APIManagementException, RegistryException, org.wso2.carbon.user.api.UserStoreException {
        PowerMockito.mockStatic(APIUtil.class);
        Resource resource = new ResourceImpl();
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(genericArtifactManager, registryService,
                registry, tenantManager);
        abstractAPIManager.tenantDomain = SAMPLE_TENANT_DOMAIN;
        APIIdentifier identifier = getAPIIdentifier(SAMPLE_API_NAME, API_PROVIDER, SAMPLE_API_VERSION);
        String apiPath =
                APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR + identifier.getProviderName()
                        + RegistryConstants.PATH_SEPARATOR + identifier.getApiName() + RegistryConstants.PATH_SEPARATOR
                        + identifier.getVersion() + APIConstants.API_RESOURCE_NAME;
        Mockito.when(tenantManager.getTenantId(Mockito.anyString())).thenThrow(UserStoreException.class)
                .thenReturn(-1234);
        API sampleAPI = new API(identifier);
        BDDMockito.when(APIUtil.getAPIForPublishing((GovernanceArtifact) Mockito.any(), (Registry) Mockito.any()))
                .thenReturn(sampleAPI);
        BDDMockito.when(APIUtil.getAPIPath(identifier)).thenReturn(apiPath);
        try {
            abstractAPIManager.getAPI(identifier);
            Assert.fail("Exception not thrown for error scenario");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Failed to get API from "));
        }
        Mockito.when(registry.get(apiPath)).thenThrow(RegistryException.class).thenReturn(resource);
        GenericArtifact genericArtifact = getGenericArtifact(SAMPLE_API_NAME, API_PROVIDER, SAMPLE_API_VERSION,
                "sample");
        Mockito.when(genericArtifactManager.getGenericArtifact(SAMPLE_RESOURCE_ID)).thenReturn(genericArtifact);

        try {
            abstractAPIManager.getAPI(identifier);
            Assert.fail("Exception not thrown for error scenario");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Failed to get API from"));
        }
        try {
            abstractAPIManager.getAPI(identifier);
            Assert.fail("Exception not thrown for error scenario");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("artifact id is null"));
        }
        resource.setUUID(SAMPLE_RESOURCE_ID);
        API api = abstractAPIManager.getAPI(identifier);
        Assert.assertNotNull(api);
        Assert.assertEquals(api.getId().getApiName(), SAMPLE_API_NAME);
        sampleAPI.setVisibility(APIConstants.API_GLOBAL_VISIBILITY);
        Assert.assertEquals(abstractAPIManager.getAPI(identifier).getId().getApiName(), SAMPLE_API_NAME);
        abstractAPIManager.tenantDomain = null;
        UserRegistry registry = Mockito.mock(UserRegistry.class);
        Mockito.when(registryService.getGovernanceUserRegistry(Mockito.anyString(), Mockito.anyInt()))
                .thenReturn(registry);
        Mockito.when(registry.get(apiPath)).thenReturn(resource);
        BDDMockito.when(APIUtil.replaceEmailDomainBack(Mockito.anyString())).thenReturn(API_PROVIDER);
        sampleAPI.setVisibility(null);
        try {
            abstractAPIManager.getAPI(identifier);
            Assert.fail("Exception not thrown for error scenario");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("does not have permission to view API"));
        }
        AbstractAPIManager abstractAPIManager1 = new AbstractAPIManagerWrapperExtended(genericArtifactManager,
                registryService, registry, tenantManager);
        abstractAPIManager1.tenantDomain =SAMPLE_TENANT_DOMAIN_1;
        Assert.assertEquals(abstractAPIManager1.getAPI(identifier).getId().getApiName(), SAMPLE_API_NAME);
    }

    @Test
    public void testGetAPIbyUUID()
            throws APIManagementException, GovernanceException, org.wso2.carbon.user.api.UserStoreException {
        Mockito.when(tenantManager.getTenantId("test")).thenThrow(UserStoreException.class).thenReturn(-1234);
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(genericArtifactManager, registryService,
                tenantManager);
        try {
            abstractAPIManager.getAPIbyUUID("1", "test");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Failed to get API"));
        }

        GenericArtifact genericArtifact = getGenericArtifact(SAMPLE_API_NAME, API_PROVIDER, SAMPLE_API_VERSION,
                "sample");
        Mockito.when(genericArtifactManager.getGenericArtifact("1")).thenThrow(RegistryException.class)
                .thenReturn(null, genericArtifact);

        try {
            abstractAPIManager.getAPIbyUUID("1", SAMPLE_TENANT_DOMAIN);
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Failed to get API"));
        }
        try {
            abstractAPIManager.getAPIbyUUID("1", SAMPLE_TENANT_DOMAIN);
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("API artifact corresponding to artifactId"));
        }
        API api = abstractAPIManager.getAPIbyUUID("1", SAMPLE_TENANT_DOMAIN);
        Assert.assertNotNull(api);
        API api1 = abstractAPIManager.getAPIbyUUID("1", SAMPLE_TENANT_DOMAIN_1);
        Assert.assertNotNull(api1);
        Assert.assertEquals(api1.getId().getApiName(),SAMPLE_API_NAME);
        abstractAPIManager.tenantDomain = SAMPLE_TENANT_DOMAIN_1;
        Assert.assertEquals(abstractAPIManager.getAPIbyUUID("1", null).getId().getApiName(),SAMPLE_API_NAME);
    }

    @Test
    public void testGetLightweightAPIByUUID()
            throws APIManagementException, GovernanceException, org.wso2.carbon.user.api.UserStoreException {
        Mockito.when(tenantManager.getTenantId("test")).thenThrow(UserStoreException.class).thenReturn(-1234);
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(genericArtifactManager, registryService,
                tenantManager);
        try {
            abstractAPIManager.getLightweightAPIByUUID("1", "test");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Failed to get tenant Id"));
        }
        GenericArtifact genericArtifact = getGenericArtifact(SAMPLE_API_NAME, API_PROVIDER, SAMPLE_API_VERSION,
                "sample");
        Mockito.when(genericArtifactManager.getGenericArtifact("1")).thenThrow(RegistryException.class)
                .thenReturn(null, genericArtifact);

        try {
            abstractAPIManager.getLightweightAPIByUUID("1", SAMPLE_TENANT_DOMAIN);
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Failed to get API"));
        }
        try {
            abstractAPIManager.getLightweightAPIByUUID("1", SAMPLE_TENANT_DOMAIN);
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("API artifact corresponding to artifactId"));
        }
        API api = abstractAPIManager.getLightweightAPIByUUID("1", "1");
        Assert.assertNotNull(api);
        abstractAPIManager.tenantDomain = SAMPLE_TENANT_DOMAIN_1;
        Assert.assertEquals(abstractAPIManager.getLightweightAPIByUUID("1", null).getId().getApiName(),SAMPLE_API_NAME);
    }

    @Test
    public void testGetLightweightAPI()
            throws APIManagementException, RegistryException, org.wso2.carbon.user.api.UserStoreException {
        Resource resource = new ResourceImpl();
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(genericArtifactManager, registryService,
                registry, tenantManager);
        APIIdentifier apiIdentifier = getAPIIdentifier(SAMPLE_API_NAME, API_PROVIDER, SAMPLE_API_VERSION);
        abstractAPIManager.registry = null;
        try {
            abstractAPIManager.getLightweightAPI(apiIdentifier);
            Assert.fail("Exception not thrown for error scenario");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Failed to get registry from api identifier"));
        }
        abstractAPIManager.registry = registry;
        abstractAPIManager.tenantDomain = SAMPLE_TENANT_DOMAIN;
        APIIdentifier identifier = getAPIIdentifier(SAMPLE_API_NAME, API_PROVIDER, SAMPLE_API_VERSION);
        String apiPath =
                APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR + identifier.getProviderName()
                        + RegistryConstants.PATH_SEPARATOR + identifier.getApiName() + RegistryConstants.PATH_SEPARATOR
                        + identifier.getVersion() + APIConstants.API_RESOURCE_NAME;
        Mockito.when(registry.get(apiPath)).thenThrow(RegistryException.class).thenReturn(resource);
        GenericArtifact genericArtifact = getGenericArtifact(SAMPLE_API_NAME, API_PROVIDER, SAMPLE_API_VERSION,
                "sample");
        Mockito.when(genericArtifactManager.getGenericArtifact(SAMPLE_RESOURCE_ID)).thenReturn(genericArtifact);
        try {
            abstractAPIManager.getLightweightAPI(apiIdentifier);
            Assert.fail("Exception not thrown for error scenario");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Failed to get API from"));
        }
        try {
            abstractAPIManager.getLightweightAPI(apiIdentifier);
            Assert.fail("Exception not thrown for error scenario");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("artifact id is null"));
        }
        resource.setUUID(SAMPLE_RESOURCE_ID);
        API api = abstractAPIManager.getLightweightAPI(apiIdentifier);
        Assert.assertNotNull(api);
        Assert.assertEquals(api.getId().getApiName(), SAMPLE_API_NAME);
        UserRegistry registry = Mockito.mock(UserRegistry.class);
        Mockito.when(registryService.getGovernanceUserRegistry(Mockito.anyString(), Mockito.anyInt())).thenThrow
                (RegistryException.class).thenReturn(registry);
        abstractAPIManager.tenantDomain = SAMPLE_TENANT_DOMAIN_1;
        PowerMockito.mockStatic(APIUtil.class);
        try {
            abstractAPIManager.getLightweightAPI(apiIdentifier);
            Assert.fail("Exception not thrown for error scenario");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Failed to get API from registry on path of"));
        }
        Mockito.when(registry.get(Mockito.anyString())).thenReturn(resource);
        Assert.assertEquals(abstractAPIManager.getLightweightAPI(apiIdentifier).getId().getApiName(), SAMPLE_API_NAME);
        Mockito.when(registryService.getGovernanceSystemRegistry(Mockito.anyInt())).thenReturn(registry);
        Mockito.when(tenantManager.getTenantId(Mockito.anyString())).thenThrow(UserStoreException.class).thenReturn
                (-1234);
        AbstractAPIManager abstractAPIManager1 = new AbstractAPIManagerWrapperExtended(genericArtifactManager,
                registryService, registry, tenantManager);
        try {
            abstractAPIManager1.getLightweightAPI(apiIdentifier);
            Assert.fail("User store exception not thrown for error scenario");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Failed to get API from registry on path of"));
        }
        Assert.assertEquals(abstractAPIManager1.getLightweightAPI(apiIdentifier).getId().getApiName(), SAMPLE_API_NAME);
    }

    @Test
    public void testGetAPIVersions() throws APIManagementException,
            RegistryException {
        String providerName = API_PROVIDER;
        String apiName = "sampleApi";
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(registry);
        try {
            abstractAPIManager.getAPIVersions(providerName, apiName);
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("API version must be a collection"));
        }
        String apiPath = APIConstants.API_LOCATION + RegistryConstants.PATH_SEPARATOR + providerName
                + RegistryConstants.PATH_SEPARATOR + apiName;
        Collection collection = new CollectionImpl();
        Mockito.when(registry.get(apiPath)).thenThrow(RegistryException.class).thenReturn(collection);
        try {
            abstractAPIManager.getAPIVersions(providerName, apiName);
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Failed to get versions for API"));
        }
        collection.setChildren(new String[0]);
        Assert.assertEquals(abstractAPIManager.getAPIVersions(providerName, apiName).size(), 0);
        collection.setChildren(new String[] { apiPath + "test/abc", apiPath + "test/asd" });
        Assert.assertEquals(abstractAPIManager.getAPIVersions(providerName, apiName).size(), 2);
    }

    @Test
    public void testGetApiByPath() throws APIManagementException, RegistryException {
        Resource resource = new ResourceImpl();
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(genericArtifactManager, registry, null);
        String apiPath = "/test/sample/1.0.0";
        Mockito.when(registry.get(apiPath)).thenThrow(RegistryException.class).thenReturn(resource);
        GenericArtifact genericArtifact = getGenericArtifact(SAMPLE_API_NAME, API_PROVIDER, SAMPLE_API_VERSION,
                "sample");

        try {
            abstractAPIManager.getAPI(apiPath);
            Assert.fail("Exception not thrown for error scenario");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Failed to get API from :"));
        }
        try {
            abstractAPIManager.getAPI(apiPath);
            Assert.fail("Exception not thrown for error scenario");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("artifact id is null"));
        }
        resource.setUUID(SAMPLE_RESOURCE_ID);
        Mockito.when(genericArtifactManager.getGenericArtifact(SAMPLE_RESOURCE_ID)).thenReturn(genericArtifact);
        API api = abstractAPIManager.getAPI(apiPath);
        Assert.assertNotNull(api);
        Assert.assertEquals(api.getId().getApiName(), SAMPLE_API_NAME);
    }

    @Test
    public void testIsAPIAvailable() throws RegistryException, APIManagementException {
        APIIdentifier apiIdentifier = getAPIIdentifier(SAMPLE_API_NAME, API_PROVIDER, SAMPLE_API_VERSION);
        String path =
                APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR + apiIdentifier.getProviderName()
                        + RegistryConstants.PATH_SEPARATOR + apiIdentifier.getApiName()
                        + RegistryConstants.PATH_SEPARATOR + apiIdentifier.getVersion();
        Mockito.when(registry.resourceExists(path)).thenThrow(RegistryException.class).thenReturn(true);
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(registry);
        try {
            abstractAPIManager.isAPIAvailable(apiIdentifier);
            Assert.fail("Exception not thrown for error scenario");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Failed to check availability of api"));
        }
        Assert.assertTrue(abstractAPIManager.isAPIAvailable(apiIdentifier));
    }

    @Test
    public void testGetAllGlobalMediationPolicies()
            throws RegistryException, APIManagementException, IOException, XMLStreamException {
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(registry);
        Collection parentCollection = new CollectionImpl();
        String mediationResourcePath = APIConstants.API_CUSTOM_SEQUENCE_LOCATION;
        String childCollectionPath = mediationResourcePath + "/testMediation";
        parentCollection.setChildren(new String[] { childCollectionPath });
        Mockito.when(registry.get(mediationResourcePath)).thenReturn(parentCollection);
        Collection childCollection = new CollectionImpl();
        String resourcePath = childCollectionPath + "/policy1";
        childCollection.setChildren(new String[] { resourcePath });
        Mockito.when(registry.get(childCollectionPath)).thenReturn(childCollection);
        Resource resource = new ResourceImpl();
        resource.setUUID(SAMPLE_RESOURCE_ID);

        Mockito.when(registry.get(resourcePath)).thenReturn(resource);
        try {
            abstractAPIManager.getAllGlobalMediationPolicies();
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Failed to get global mediation policies"));
        }
        String mediationPolicyContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<sequence xmlns=\"http://ws.apache.org/ns/synapse\" name=\"default-endpoint\">\n</sequence>";
        resource.setContent(mediationPolicyContent);

        List<Mediation> policies = abstractAPIManager.getAllGlobalMediationPolicies();
        Assert.assertNotNull(policies);
        Assert.assertEquals(policies.size(), 1);
        PowerMockito.mockStatic(IOUtils.class);
        PowerMockito.mockStatic(AXIOMUtil.class);
        PowerMockito.when(IOUtils.toString((InputStream) Mockito.any(), Mockito.anyString()))
                .thenThrow(IOException.class).thenReturn(mediationPolicyContent);
        PowerMockito.when(AXIOMUtil.stringToOM(Mockito.anyString())).thenThrow(XMLStreamException.class);
        abstractAPIManager.getAllGlobalMediationPolicies(); // cover the logged only exceptions
        abstractAPIManager.getAllGlobalMediationPolicies(); // cover the logged only exceptions

    }

    @Test
    public void testGetGlobalMediationPolicy()
            throws RegistryException, APIManagementException, XMLStreamException, IOException {
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(registry);
        String resourceUUID = SAMPLE_RESOURCE_ID;
        Collection parentCollection = new CollectionImpl();
        String mediationResourcePath = APIConstants.API_CUSTOM_SEQUENCE_LOCATION;
        String childCollectionPath = mediationResourcePath + "/testMediation";
        parentCollection.setChildren(new String[] { childCollectionPath });
        Mockito.when(registry.get(mediationResourcePath)).thenThrow(RegistryException.class).thenReturn(parentCollection);
        Collection childCollection = new CollectionImpl();
        String resourcePath = childCollectionPath + "/policy1";
        childCollection.setChildren(new String[] { resourcePath });
        Mockito.when(registry.get(childCollectionPath)).thenReturn(childCollection);
        Resource resource = new ResourceImpl(resourcePath, new ResourceDO());
        resource.setUUID(resourceUUID);

        Mockito.when(registry.get(resourcePath)).thenReturn(resource);
        try {
            abstractAPIManager.getGlobalMediationPolicy(resourceUUID);
            Assert.fail("Registry Exception not thrown for error scenario");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Error while accessing registry objects"));
        }
        abstractAPIManager.getGlobalMediationPolicy(resourceUUID); // test for registry exception
        String mediationPolicyContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<sequence xmlns=\"http://ws.apache.org/ns/synapse\" name=\"default-endpoint\">\n</sequence>";
        resource.setContent(mediationPolicyContent);
        Mediation policy = abstractAPIManager.getGlobalMediationPolicy(resourceUUID);
        Assert.assertNotNull(policy);
        PowerMockito.mockStatic(IOUtils.class);
        PowerMockito.mockStatic(AXIOMUtil.class);
        PowerMockito.when(IOUtils.toString((InputStream) Mockito.any(), Mockito.anyString()))
                .thenThrow(IOException.class).thenReturn(mediationPolicyContent);
        PowerMockito.when(AXIOMUtil.stringToOM(Mockito.anyString())).thenThrow(XMLStreamException.class);
        abstractAPIManager.getGlobalMediationPolicy(resourceUUID); // cover the logged only exceptions
        abstractAPIManager.getGlobalMediationPolicy(resourceUUID); // cover the logged only exceptions

    }

    @Test
    public void testGetAllWsdls() throws RegistryException, APIManagementException {
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(registry);
        Collection parentCollection = new CollectionImpl();
        String wsdlResourcepath = APIConstants.API_WSDL_RESOURCE;
        String resourcePath = wsdlResourcepath + "/wsdl1";
        parentCollection.setChildren(new String[] { resourcePath });
        Mockito.when(registry.get(wsdlResourcepath)).thenReturn(parentCollection);
        Resource resource = new ResourceImpl();
        Mockito.when(registry.get(resourcePath)).thenThrow(RegistryException.class).thenReturn(resource);
        Mockito.when(registry.resourceExists(wsdlResourcepath)).thenReturn(true);
        try {
            abstractAPIManager.getAllWsdls();
            Assert.fail("Exception not thrown for error scenario");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Failed to get wsdl list"));
        }
        resource.setUUID(SAMPLE_RESOURCE_ID);

        List<Wsdl> wsdls = abstractAPIManager.getAllWsdls();
        Assert.assertNotNull(wsdls);
        Assert.assertEquals(wsdls.size(), 1);

    }

    @Test
    public void testGetWsdlById() throws RegistryException, APIManagementException, IOException {
        String resourceId = SAMPLE_RESOURCE_ID;
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(registry);
        Collection parentCollection = new CollectionImpl();
        String wsdlResourcepath = APIConstants.API_WSDL_RESOURCE;
        String resourcePath = wsdlResourcepath + "/wsdl1";
        parentCollection.setChildren(new String[] { resourcePath });
        Mockito.when(registry.get(wsdlResourcepath)).thenReturn(parentCollection);
        Resource resource = new ResourceImpl(resourcePath, new ResourceDO());
        Mockito.when(registry.get(resourcePath)).thenThrow(RegistryException.class).thenReturn(resource);
        Mockito.when(registry.resourceExists(wsdlResourcepath)).thenReturn(false, true);
        Assert.assertNull(abstractAPIManager.getWsdlById(resourceId));
        resource.setUUID(resourceId);
        try {
            abstractAPIManager.getWsdlById(resourceId);
            Assert.fail("Exception not thrown for error scenario");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Error while accessing registry objects"));
        }
        String wsdlContent = "sample wsdl";
        resource.setContent(wsdlContent);
        Wsdl wsdl = abstractAPIManager.getWsdlById(resourceId);
        Assert.assertNotNull(wsdl);
        PowerMockito.mockStatic(IOUtils.class);
        PowerMockito.when(IOUtils.toString((InputStream) Mockito.any(), Mockito.anyString()))
                .thenThrow(IOException.class);
        abstractAPIManager.getWsdlById(resourceId);// covers logged only exception;

    }

    @Test
    public void testDeleteWsdl() throws APIManagementException, RegistryException {
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(registry);
        Collection parentCollection = new CollectionImpl();
        String wsdlResourcePath = APIConstants.API_WSDL_RESOURCE;
        String resourcePath = wsdlResourcePath + "/wsdl1";
        parentCollection.setChildren(new String[] { resourcePath });
        Mockito.when(registry.get(wsdlResourcePath)).thenReturn(parentCollection);
        Resource resource = new ResourceImpl(resourcePath, new ResourceDO());
        Mockito.when(registry.get(resourcePath)).thenThrow(RegistryException.class).thenReturn(resource);
        Mockito.when(registry.resourceExists(wsdlResourcePath)).thenReturn(true);
        try {
            abstractAPIManager.deleteWsdl(SAMPLE_RESOURCE_ID);
            Assert.fail("Exception not thrown for error scenario");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Error while accessing registry objects"));
        }

        Mockito.when(registry.resourceExists(resourcePath)).thenReturn(true, true, true, true, false);
        resource.setUUID(SAMPLE_RESOURCE_ID);
        Mockito.doThrow(RegistryException.class).doNothing().when(registry).delete(resourcePath);
        try {
            abstractAPIManager.deleteWsdl(SAMPLE_RESOURCE_ID);
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Failed to delete wsdl"));
        }
        Assert.assertFalse(abstractAPIManager.deleteWsdl(SAMPLE_RESOURCE_ID));
        Assert.assertTrue(abstractAPIManager.deleteWsdl(SAMPLE_RESOURCE_ID));
    }

    @Test
    public void testGetWsdl() throws APIManagementException, RegistryException, IOException {
        Resource resourceMock = Mockito.mock(Resource.class);
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(registry);
        APIIdentifier identifier = getAPIIdentifier(SAMPLE_API_NAME, API_PROVIDER, SAMPLE_API_VERSION);
        String wsdlName =
                identifier.getProviderName() + "--" + identifier.getApiName() + identifier.getVersion() + ".wsdl";
        String wsdlResourcePath = APIConstants.API_WSDL_RESOURCE_LOCATION + wsdlName;
        Resource resource = new ResourceImpl(wsdlResourcePath, new ResourceDO());
        Mockito.when(registry.get(wsdlResourcePath)).thenThrow(RegistryException.class).thenReturn(resource);
        Mockito.when(registry.resourceExists(wsdlResourcePath)).thenReturn(true);
        try {
            abstractAPIManager.getWsdl(identifier);
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Error while getting wsdl file from the registry"));
        }
        String wsdlContent = "sample wsdl";
        resource.setContent(wsdlContent);
        InputStream inputStream = new ArrayInputStream();
        Mockito.when(resourceMock.getContentStream()).thenReturn(inputStream);
        Assert.assertTrue(abstractAPIManager.getWsdl(identifier).contains(wsdlContent));
        PowerMockito.mockStatic(IOUtils.class);
        PowerMockito.when(IOUtils.toString((InputStream) Mockito.any(), Mockito.anyString()))
                .thenThrow(IOException.class);
        try {
            abstractAPIManager.getWsdl(identifier).contains(wsdlContent);
            Assert.fail("Exception not thrown for error scenario");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Error occurred while getting the content of wsdl"));
        }
    }

    @Test
    public void testUploadWsdl() throws RegistryException, APIManagementException {
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(registry);
        Resource resource = new ResourceImpl();
        String resourcePath = "/test/wsdl";
        String wsdlContent = "sample wsdl";
        Resource resourceMock = Mockito.mock(Resource.class);
        resourceMock.setContent(wsdlContent);
        resourceMock.setMediaType(String.valueOf(ContentType.APPLICATION_XML));
        Mockito.when(registry.newResource()).thenReturn(resource);
        Mockito.doThrow(RegistryException.class).doReturn(resourcePath).when(registry).put(resourcePath, resource);
        try {
            abstractAPIManager.uploadWsdl(resourcePath, wsdlContent);
            Assert.fail("Exception not thrown for error scenario");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Error while uploading wsdl to from the registry"));
        }
        abstractAPIManager.uploadWsdl(resourcePath, wsdlContent);
        Mockito.verify(registry, Mockito.atLeastOnce()).put(resourcePath, resource);
    }

    @Test
    public void testUpdateWsdl() throws APIManagementException, RegistryException {
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(registry);
        Resource resource = new ResourceImpl();
        String resourcePath = "/test/wsdl";
        String wsdlContent = "sample wsdl";
        Mockito.when(registry.get(resourcePath)).thenThrow(RegistryException.class).thenReturn(resource);
        try {
            abstractAPIManager.updateWsdl(resourcePath, wsdlContent);
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Error while updating the existing wsdl"));
        }
        try {
            abstractAPIManager.updateWsdl(resourcePath, wsdlContent);
        } catch (APIManagementException e) {
            Assert.fail("Error while updating wsdl");
        }
    }

    @Test
    public void testGetSwagger20Definition() throws Exception {
        int tenantId = -1234;
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(null, registryService, tenantManager);
        Mockito.when(tenantManager.getTenantId(SAMPLE_TENANT_DOMAIN)).thenThrow(UserStoreException.class)
                .thenReturn(tenantId);

        APIIdentifier identifier = getAPIIdentifier(SAMPLE_API_NAME, API_PROVIDER, SAMPLE_API_VERSION);
        try {
            abstractAPIManager.getSwagger20Definition(identifier);
            Assert.fail("Use store exception not thrown for error scenario");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Failed to get swagger documentation of API"));
        }
        setFinalStatic(AbstractAPIManager.class.getDeclaredField("definitionFromSwagger20"),
                apiDefinitionFromSwagger20);
        String swaggerContent = "sample swagger";
        Mockito.when(apiDefinitionFromSwagger20.getAPIDefinition(identifier, null)).thenReturn(swaggerContent);
        Assert.assertEquals(abstractAPIManager.getSwagger20Definition(identifier), swaggerContent);
        abstractAPIManager.tenantDomain = SAMPLE_TENANT_DOMAIN;
        Assert.assertEquals(abstractAPIManager.getSwagger20Definition(identifier), swaggerContent);
        Mockito.when(registryService.getGovernanceUserRegistry(CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME, tenantId))
                .thenThrow(RegistryException.class);
        abstractAPIManager.tenantDomain = null;
        try {
            abstractAPIManager.getSwagger20Definition(identifier);
            Assert.fail("Registry exception not thrown for error scenario");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Failed to get swagger documentation of API"));
        }
    }

    @Test
    public void testAddResourceFile() throws APIManagementException, RegistryException, IOException {
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(registry);
        Resource resource = new ResourceImpl();
        Mockito.when(registry.newResource()).thenReturn(resource);
        String resourcePath = "/test";
        String contentType = "sampleType";
        ResourceFile resourceFile = new ResourceFile(null, contentType);
        try {
            abstractAPIManager.addResourceFile(resourcePath, resourceFile);
            Assert.fail("Registry exception not thrown for error scenario");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Error while adding the resource to the registry"));
        }
        InputStream in = IOUtils.toInputStream("sample content", "UTF-8");
        resourceFile = new ResourceFile(in, contentType);
        String returnedPath = abstractAPIManager.addResourceFile(resourcePath, resourceFile);
        Assert.assertTrue(returnedPath.contains(resourcePath) && returnedPath.contains("/t/"));
        abstractAPIManager.tenantDomain = SAMPLE_TENANT_DOMAIN;
        returnedPath = abstractAPIManager.addResourceFile(resourcePath, resourceFile);
        Assert.assertTrue(returnedPath.contains(resourcePath) && !returnedPath.contains("/t/"));

    }

    @Test
    public void testIsDocumentationExist() throws APIManagementException, RegistryException {
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(registry);
        APIIdentifier identifier = getAPIIdentifier(SAMPLE_API_NAME, API_PROVIDER, SAMPLE_API_VERSION);
        String docName = "sampleDoc";
        String docPath =
                APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR + identifier.getProviderName()
                        + RegistryConstants.PATH_SEPARATOR + identifier.getApiName() + RegistryConstants.PATH_SEPARATOR
                        + identifier.getVersion() + RegistryConstants.PATH_SEPARATOR + APIConstants.DOC_DIR
                        + RegistryConstants.PATH_SEPARATOR + docName;
        Mockito.when(registry.resourceExists(docPath)).thenThrow(RegistryException.class).thenReturn(true);
        try {
            abstractAPIManager.isDocumentationExist(identifier, docName);
            Assert.fail("Registry exception not thrown for error scenario");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Failed to check existence of the document"));
        }
        Assert.assertTrue(abstractAPIManager.isDocumentationExist(identifier, docName));
    }

    @Test
    public void testGetAllDocumentation() throws APIManagementException, RegistryException {
        Registry registry = Mockito.mock(UserRegistry.class);

        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(genericArtifactManager, registry, null);
        abstractAPIManager.registry = registry;

        GenericArtifact genericArtifact = getGenericArtifact(SAMPLE_API_NAME, API_PROVIDER, SAMPLE_API_VERSION,
                "sample");
        genericArtifact.setAttribute(APIConstants.DOC_TYPE, "Other");
        genericArtifact.setAttribute(APIConstants.DOC_SOURCE_TYPE, "URL");
        Association association = new Association();
        String associationDestinationPath = "doc/destination";
        association.setDestinationPath(associationDestinationPath);
        Association[] associations = new Association[] { association };
        APIIdentifier identifier = getAPIIdentifier(SAMPLE_API_NAME, API_PROVIDER, SAMPLE_API_VERSION);
        String apiResourcePath =
                APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR + identifier.getProviderName()
                        + RegistryConstants.PATH_SEPARATOR + identifier.getApiName() + RegistryConstants.PATH_SEPARATOR
                        + identifier.getVersion() + APIConstants.API_RESOURCE_NAME;
        Mockito.when(registry.getAssociations(apiResourcePath, APIConstants.DOCUMENTATION_ASSOCIATION))
                .thenThrow(RegistryException.class).thenReturn(associations);
        try {
            abstractAPIManager.getAllDocumentation(identifier);
            Assert.fail("Registry exception not thrown for error scenario");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Failed to get documentations for api"));
        }

        Resource resource = new ResourceImpl();
        resource.setUUID(SAMPLE_RESOURCE_ID);
        Mockito.when(registry.get(associationDestinationPath)).thenReturn(resource);
        Mockito.when(genericArtifactManager.getGenericArtifact(SAMPLE_RESOURCE_ID)).thenReturn(genericArtifact);
        List<Documentation> documentationList = abstractAPIManager.getAllDocumentation(identifier);
        Assert.assertNotNull(documentationList);
        Assert.assertEquals(documentationList.size(), 1);
        String documentationName = "doc1";
        String contentPath = APIConstants.API_LOCATION + RegistryConstants.PATH_SEPARATOR + identifier.getProviderName()
                + RegistryConstants.PATH_SEPARATOR + identifier.getApiName() + RegistryConstants.PATH_SEPARATOR
                + identifier.getVersion() + RegistryConstants.PATH_SEPARATOR + APIConstants.DOC_DIR
                + RegistryConstants.PATH_SEPARATOR + RegistryConstants.PATH_SEPARATOR + documentationName;
        genericArtifact.setAttribute(APIConstants.DOC_SOURCE_TYPE, "In line");
        genericArtifact.setAttribute(APIConstants.DOC_NAME, documentationName);
        ResourceDO resourceDO = new ResourceDO();
        resourceDO.setLastUpdatedOn(12344567);
        Resource resource1 = new ResourceImpl(contentPath, resourceDO);
        Mockito.when(registry.get(contentPath)).thenReturn(resource1);
        documentationList = abstractAPIManager.getAllDocumentation(identifier);
        Assert.assertNotNull(documentationList);
        Assert.assertEquals(documentationList.size(), 1);

    }

    @Test
    public void testGetAllDocumentationWithLoggedUser()
            throws APIManagementException, org.wso2.carbon.user.api.UserStoreException, RegistryException {
        int tenantId = -1234;
        UserRegistry registry = Mockito.mock(UserRegistry.class);
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(genericArtifactManager, registryService,
                registry, tenantManager);
        Mockito.when(tenantManager.getTenantId(SAMPLE_TENANT_DOMAIN)).thenThrow(UserStoreException.class)
                .thenReturn(tenantId);
        APIIdentifier identifier = getAPIIdentifier(SAMPLE_API_NAME, API_PROVIDER, SAMPLE_API_VERSION);
        String loggedInUser = "admin";
        try {
            abstractAPIManager.getAllDocumentation(identifier, loggedInUser);
            Assert.fail("User store exception not thrown for erroe scenario");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Failed to get documentations for api"));
        }
        String apiResourcePath =
                APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR + identifier.getProviderName()
                        + RegistryConstants.PATH_SEPARATOR + identifier.getApiName() + RegistryConstants.PATH_SEPARATOR
                        + identifier.getVersion() + APIConstants.API_RESOURCE_NAME;
        GenericArtifact genericArtifact = getGenericArtifact(SAMPLE_API_NAME, API_PROVIDER, SAMPLE_API_VERSION,
                "sample");
        genericArtifact.setAttribute(APIConstants.DOC_TYPE, "Other");
        genericArtifact.setAttribute(APIConstants.DOC_SOURCE_TYPE, "URL");
        Association association = new Association();
        String associationDestinationPath = "doc/destination";
        association.setDestinationPath(associationDestinationPath);
        Association[] associations = new Association[] { association };
        abstractAPIManager.registry = registry;
        Mockito.when(registryService.getGovernanceUserRegistry(CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME, tenantId))
                .thenThrow(RegistryException.class).thenReturn(registry);
        Mockito.when(registry.getAssociations(apiResourcePath, APIConstants.DOCUMENTATION_ASSOCIATION))
                .thenReturn(associations);
        try {
            abstractAPIManager.getAllDocumentation(identifier, loggedInUser);
            Assert.fail("Registry exception not thrown for error scenario");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Failed to get documentations for api"));
        }

        Resource resource = new ResourceImpl();
        resource.setUUID(SAMPLE_RESOURCE_ID);
        Mockito.when(registry.get(associationDestinationPath)).thenThrow(RegistryException.class).thenReturn(resource);
        try {
            abstractAPIManager.getAllDocumentation(identifier, loggedInUser);
            Assert.fail("Registry exception not thrown for error scenario");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Failed to get documentations for api"));
        }
        abstractAPIManager.tenantDomain = SAMPLE_TENANT_DOMAIN;
        Mockito.when(genericArtifactManager.getGenericArtifact(SAMPLE_RESOURCE_ID)).thenReturn(genericArtifact);
        List<Documentation> documentationList = abstractAPIManager.getAllDocumentation(identifier, loggedInUser);
        Assert.assertNotNull(documentationList);
        Assert.assertEquals(documentationList.size(), 1);
        String documentationName = "doc1";
        String contentPath = APIConstants.API_LOCATION + RegistryConstants.PATH_SEPARATOR + identifier.getProviderName()
                + RegistryConstants.PATH_SEPARATOR + identifier.getApiName() + RegistryConstants.PATH_SEPARATOR
                + identifier.getVersion() + RegistryConstants.PATH_SEPARATOR + APIConstants.DOC_DIR
                + RegistryConstants.PATH_SEPARATOR + RegistryConstants.PATH_SEPARATOR + documentationName;
        genericArtifact.setAttribute(APIConstants.DOC_SOURCE_TYPE, "In line");
        genericArtifact.setAttribute(APIConstants.DOC_NAME, documentationName);
        ResourceDO resourceDO = new ResourceDO();
        resourceDO.setLastUpdatedOn(12344567);
        Resource resource1 = new ResourceImpl(contentPath, resourceDO);
        Mockito.when(registry.get(contentPath)).thenReturn(resource1);
        documentationList = abstractAPIManager.getAllDocumentation(identifier, loggedInUser);
        Assert.assertNotNull(documentationList);
        Assert.assertEquals(documentationList.size(), 1);

    }

    @Test
    public void testGetDocumentation() throws APIManagementException, RegistryException {
        Registry registry = Mockito.mock(UserRegistry.class);
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(genericArtifactManager, registry, null);
        APIIdentifier identifier = getAPIIdentifier(SAMPLE_API_NAME, API_PROVIDER, SAMPLE_API_VERSION);
        String documentationName = "doc1";
        String contentPath = APIConstants.API_LOCATION + RegistryConstants.PATH_SEPARATOR + identifier.getProviderName()
                + RegistryConstants.PATH_SEPARATOR + identifier.getApiName() + RegistryConstants.PATH_SEPARATOR
                + identifier.getVersion() + RegistryConstants.PATH_SEPARATOR + APIConstants.DOC_DIR
                + RegistryConstants.PATH_SEPARATOR + documentationName;
        GenericArtifact genericArtifact = getGenericArtifact(SAMPLE_API_NAME, API_PROVIDER, SAMPLE_API_VERSION,
                "sample");
        String docType = "other";
        genericArtifact.setAttribute(APIConstants.DOC_TYPE, docType);
        genericArtifact.setAttribute(APIConstants.DOC_SOURCE_TYPE, "URL");
        Resource resource = new ResourceImpl();
        resource.setUUID(SAMPLE_RESOURCE_ID);
        Mockito.when(registry.get(contentPath)).thenThrow(RegistryException.class).thenReturn(resource);
        Mockito.when(genericArtifactManager.getGenericArtifact(SAMPLE_RESOURCE_ID)).thenReturn(genericArtifact);
        try {
            abstractAPIManager.getDocumentation(identifier, DocumentationType.OTHER, documentationName);
            Assert.fail("Registry exception not thrown for error scenario");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Failed to get documentation details"));
        }
        Assert.assertTrue(
                abstractAPIManager.getDocumentation(identifier, DocumentationType.OTHER, documentationName).getId()
                        .equals(genericArtifact.getId()));
    }

    @Test
    public void testGetDocumentationFromId()
            throws APIManagementException, org.wso2.carbon.user.api.UserStoreException, RegistryException {
        int tenantId = -1234;
        UserRegistry registry = Mockito.mock(UserRegistry.class);
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(genericArtifactManager, registryService,
                registry, tenantManager);
        Mockito.when(tenantManager.getTenantId(SAMPLE_TENANT_DOMAIN)).thenThrow(UserStoreException.class)
                .thenReturn(tenantId);
        try {
            abstractAPIManager.getDocumentation(SAMPLE_RESOURCE_ID, SAMPLE_TENANT_DOMAIN);
            Assert.fail("User store exception not thrown for error scenario");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Failed to get documentation details"));
        }
        Mockito.when(registryService.getGovernanceUserRegistry(CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME, tenantId))
                .thenThrow(RegistryException.class).thenReturn(registry);
        try {
            abstractAPIManager.getDocumentation(SAMPLE_RESOURCE_ID, SAMPLE_TENANT_DOMAIN);
            Assert.fail("Registry exception not thrown for error scenario");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Failed to get documentation details"));
        }

        GenericArtifact genericArtifact = getGenericArtifact(SAMPLE_API_NAME, API_PROVIDER, SAMPLE_API_VERSION,
                "sample");
        genericArtifact.setAttribute(APIConstants.DOC_TYPE, "Other");
        genericArtifact.setAttribute(APIConstants.DOC_SOURCE_TYPE, "URL");
        GenericArtifact artifact = Mockito.mock(GenericArtifact.class);
        Mockito.when(genericArtifactManager.getGenericArtifact(SAMPLE_RESOURCE_ID)).thenReturn(artifact);

        Mockito.when(artifact.getPath()).thenReturn("test");
        String docName = "sample";
        Documentation documentation = new Documentation(DocumentationType.HOWTO, docName);
        PowerMockito.mockStatic(APIUtil.class);
        BDDMockito.given(APIUtil.getDocumentation(artifact)).willReturn(documentation);

        ResourceDO resourceDO = new ResourceDO();
        resourceDO.setLastUpdatedOn(234567);
        Resource resource = new ResourceImpl("", resourceDO);
        Mockito.when(registry.get(Mockito.anyString())).thenReturn(resource);
        Assert.assertNotNull(abstractAPIManager.getDocumentation(SAMPLE_RESOURCE_ID, SAMPLE_TENANT_DOMAIN));
        abstractAPIManager.tenantDomain = SAMPLE_TENANT_DOMAIN;
        Documentation doc = abstractAPIManager.getDocumentation(SAMPLE_RESOURCE_ID, SAMPLE_TENANT_DOMAIN);
        Assert.assertEquals(doc.getName(),docName);

    }

    @Test
    public void testGetDocumentationContent()
            throws APIManagementException, org.wso2.carbon.user.api.UserStoreException, RegistryException {
        int tenantId = -1234;
        UserRegistry registry = Mockito.mock(UserRegistry.class);
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapperExtended(genericArtifactManager,
                registryService, registry, tenantManager);
        Mockito.when(tenantManager.getTenantId(Mockito.anyString())).thenThrow(UserStoreException.class)
                .thenReturn(tenantId);
        APIIdentifier identifier = getAPIIdentifier(SAMPLE_API_NAME, API_PROVIDER, SAMPLE_API_VERSION);
        String docName  = "doc1";
        try {
            abstractAPIManager.getDocumentationContent(identifier, docName);
            Assert.fail("User store exception not thrown for error scenario");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Failed to get document content found for documentation:"));
        }
        Mockito.when(registryService.getGovernanceSystemRegistry(Mockito.anyInt()))
                .thenThrow(RegistryException.class).thenReturn(registry);
        try {
            abstractAPIManager.getDocumentationContent(identifier, docName);
            Assert.fail("Registry exception not thrown for error scenario");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("No document content found for documentation:"));
        }
        Mockito.when(registry.resourceExists(Mockito.anyString())).thenReturn(false,true,true);
        String docContent = abstractAPIManager.getDocumentationContent(identifier, docName);
        Assert.assertNull(docContent);
        String docObject = "samlple doc content";
        Resource resource = new ResourceImpl();
        resource.setContent(docObject.getBytes(StandardCharsets.UTF_8));
        Mockito.when(registry.get(Mockito.anyString())).thenReturn(resource);
        docContent = abstractAPIManager.getDocumentationContent(identifier, docName);
        Assert.assertEquals(docContent,docObject);
        abstractAPIManager = new AbstractAPIManagerWrapper(genericArtifactManager, registryService,
                registry, tenantManager);
        docContent = abstractAPIManager.getDocumentationContent(identifier, docName);
        Assert.assertEquals(docContent,docObject);
        Mockito.when(registryService.getGovernanceUserRegistry(Mockito.anyString(), Mockito.anyInt()))
                .thenReturn(registry);
        abstractAPIManager.tenantDomain = SAMPLE_TENANT_DOMAIN_1;
        docContent = abstractAPIManager.getDocumentationContent(identifier, docName);
        Assert.assertEquals(docContent,docObject);
    }

    @Test
    public void testGetSubscriberById() throws APIManagementException {
        Subscriber subscriber = new Subscriber("subscriber");
        Mockito.when(apiMgtDAO.getSubscriberById(Mockito.anyString())).thenReturn(subscriber);
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(apiMgtDAO);
        Assert.assertEquals(abstractAPIManager.getSubscriberById(SAMPLE_RESOURCE_ID).getName(),"subscriber");
    }

    @Test
    public void testIsContextExist() throws APIManagementException {
        String context = "/t/sample";
        Mockito.when(apiMgtDAO.isContextExist(Mockito.anyString())).thenReturn( true);
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(apiMgtDAO);
        abstractAPIManager.tenantDomain = SAMPLE_TENANT_DOMAIN_1;
        Assert.assertTrue(abstractAPIManager.isContextExist(context));
    }

    @Test
    public void testIsScopeKeyExist() throws APIManagementException {
        Mockito.when(apiMgtDAO.isScopeKeyExist(Mockito.anyString(), Mockito.anyInt())).thenReturn(false, true);
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(apiMgtDAO);
        Assert.assertFalse(abstractAPIManager.isScopeKeyExist("sample", -1234));
        Assert.assertTrue(abstractAPIManager.isScopeKeyExist("sample1", -1234));
    }

    @Test
    public void testIsScopeKeyAssigned() throws APIManagementException {
        APIIdentifier identifier = getAPIIdentifier(SAMPLE_API_NAME, API_PROVIDER, SAMPLE_API_VERSION);
        Mockito.when(apiMgtDAO.isScopeKeyAssigned((APIIdentifier) Mockito.any(), Mockito.anyString(), Mockito.anyInt()))
                .thenReturn(false, true);
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(apiMgtDAO);
        Assert.assertFalse(abstractAPIManager.isScopeKeyAssigned(identifier, "sample", -1234));
        Assert.assertTrue(abstractAPIManager.isScopeKeyAssigned(identifier, "sample1", -1234));
    }

    @Test
    public void testIsApiNameExist() throws APIManagementException {
        Mockito.when(apiMgtDAO.isApiNameExist(Mockito.anyString(), Mockito.anyString())).thenReturn(false, true);
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(apiMgtDAO);
        abstractAPIManager.tenantDomain = SAMPLE_TENANT_DOMAIN_1;
        Assert.assertFalse(abstractAPIManager.isApiNameExist(SAMPLE_API_NAME));
        Assert.assertTrue(abstractAPIManager.isApiNameExist(SAMPLE_API_NAME));

    }

    @Test
    public void testAddSubscriber() throws APIManagementException, org.wso2.carbon.user.api.UserStoreException {
        int tenantId = -1234;
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(null, null, null, tenantManager,
                apiMgtDAO);
        Mockito.when(tenantManager.getTenantId(Mockito.anyString())).thenThrow(UserStoreException.class)
                .thenReturn(tenantId);
        try {
            abstractAPIManager.addSubscriber(API_PROVIDER, SAMPLE_RESOURCE_ID);
            Assert.fail("User store exception not thrown for error scenario");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Error while adding the subscriber"));
        }

        Mockito.doThrow(APIManagementException.class).doNothing().when(apiMgtDAO)
                .addSubscriber((Subscriber) Mockito.any(), Mockito.anyString());
        try {
            abstractAPIManager.addSubscriber(API_PROVIDER, SAMPLE_RESOURCE_ID);
            Assert.fail("APIM exception not thrown for error scenario");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Error while adding the subscriber"));
        }
        PowerMockito.mockStatic(APIUtil.class);
        BDDMockito.when(APIUtil.isEnabledUnlimitedTier()).thenReturn(true, false);
        Mockito.doNothing().when(apiMgtDAO).addSubscriber((Subscriber) Mockito.any(), Mockito.anyString());
        abstractAPIManager.addSubscriber(API_PROVIDER, SAMPLE_RESOURCE_ID);
        List<Tier> tierValues = new ArrayList<Tier>();
        tierValues.add(new Tier("Gold"));
        tierValues.add(new Tier("Silver"));
        Map<String, Tier> tierMap = new HashMap<String, Tier>();
        tierMap.put("Gold", new Tier("Gold"));
        tierMap.put("Silver", new Tier("Silver"));
        BDDMockito.when(APIUtil.getTiers(Mockito.anyInt(), Mockito.anyString())).thenReturn(tierMap);
        BDDMockito.when(APIUtil.sortTiers(Mockito.anySet())).thenReturn(tierValues);
        abstractAPIManager.addSubscriber(API_PROVIDER, SAMPLE_RESOURCE_ID);
        Mockito.verify(apiMgtDAO, Mockito.times(3)).addSubscriber((Subscriber) Mockito.any(), Mockito.anyString());

    }

    @Test
    public void testUpdateSubscriber() throws APIManagementException {
        Subscriber subscriber = new Subscriber("sub1");
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(apiMgtDAO);
        Mockito.doNothing().when(apiMgtDAO).updateSubscriber((Subscriber) Mockito.any());
        abstractAPIManager.updateSubscriber(subscriber);
        Mockito.verify(apiMgtDAO, Mockito.times(1)).updateSubscriber(subscriber);
    }

    @Test
    public void testGetSubscriber() throws APIManagementException {
        Subscriber subscriber = new Subscriber("sub1");
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(apiMgtDAO);
        Mockito.when(apiMgtDAO.getSubscriber(Mockito.anyInt())).thenReturn(subscriber);
        Assert.assertTrue(abstractAPIManager.getSubscriber(1).getName().equals("sub1"));
    }

    @Test
    public void testGetIcon()
            throws APIManagementException, org.wso2.carbon.user.api.UserStoreException, RegistryException {
        APIIdentifier identifier = new APIIdentifier(SAMPLE_API_NAME, API_PROVIDER, SAMPLE_API_VERSION);
        int tenantId = -1234;
        UserRegistry registry = Mockito.mock(UserRegistry.class);
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapperExtended(genericArtifactManager,
                registryService, registry, tenantManager);
        Mockito.when(tenantManager.getTenantId(Mockito.anyString())).thenThrow(UserStoreException.class)
                .thenReturn(tenantId);
        try {
            abstractAPIManager.getIcon(identifier);
            Assert.fail("User store exception not thrown for error scenario");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Error while loading API icon of API"));
        }
        Mockito.when(registryService.getGovernanceSystemRegistry(Mockito.anyInt())).thenThrow(RegistryException.class)
                .thenReturn(registry);
        try {
            abstractAPIManager.getIcon(identifier);
            Assert.fail("User store exception not thrown for error scenario");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Error while loading API icon of API"));
        }
        Assert.assertNull(abstractAPIManager.getIcon(identifier));
        AbstractAPIManager abstractAPIManager1 = new AbstractAPIManagerWrapper(genericArtifactManager, registryService,
                registry, tenantManager);
        Mockito.when(registryService.getGovernanceUserRegistry(Mockito.anyString(), Mockito.anyInt()))
                .thenReturn(registry);
        Assert.assertNull(abstractAPIManager1.getIcon(identifier));
        abstractAPIManager1.tenantDomain = SAMPLE_TENANT_DOMAIN_1;
        Mockito.when(registry.resourceExists(Mockito.anyString())).thenReturn(true);
        Resource resource = new ResourceImpl();
        resource.setContent("sample conetent");
        resource.setMediaType("api");
        Mockito.when(registry.get(Mockito.anyString())).thenReturn(resource);
        Assert.assertTrue(abstractAPIManager1.getIcon(identifier).getContentType().equals("api"));
    }

    @Test
    public void testGetSubscriberAPIs() throws APIManagementException, RegistryException {
        Subscriber subscriber = new Subscriber("sub1");
        Application application = new Application("app1", subscriber);
        application.setId(1);
        SubscribedAPI subscribedAPI1 = new SubscribedAPI(subscriber,
                getAPIIdentifier(SAMPLE_API_NAME, API_PROVIDER, SAMPLE_API_VERSION));
        subscribedAPI1.setUUID(SAMPLE_RESOURCE_ID);
        SubscribedAPI subscribedAPI2 = new SubscribedAPI(subscriber, getAPIIdentifier("sample1", API_PROVIDER, "2.0.0"));
        Set<SubscribedAPI> subscribedAPIs = new HashSet<SubscribedAPI>();
        subscribedAPI1.setApplication(application);
        subscribedAPI2.setApplication(application);
        subscribedAPIs.add(subscribedAPI1);
        subscribedAPIs.add(subscribedAPI2);
        Mockito.when(apiMgtDAO.getSubscribedAPIs((Subscriber) Mockito.any(), Mockito.anyString()))
                .thenReturn(subscribedAPIs);
        UserRegistry registry = Mockito.mock(UserRegistry.class);
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(genericArtifactManager, null, registry,
                null, apiMgtDAO);
        Resource resource = new ResourceImpl();
        resource.setUUID(SAMPLE_RESOURCE_ID);
        Mockito.when(registry.get(Mockito.anyString())).thenThrow(RegistryException.class).thenReturn(resource);
        try {
            abstractAPIManager.getSubscriberAPIs(subscriber);
            Assert.fail("Registry exception not thrown for error scenario");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Failed to get APIs for subscriber: "));
        }
        GenericArtifact artifact = getGenericArtifact(SAMPLE_API_NAME,API_PROVIDER,SAMPLE_API_VERSION,"sample_qname");
        Mockito.when(genericArtifactManager.getGenericArtifact(Mockito.anyString())).thenReturn(artifact);
        PowerMockito.mockStatic(APIUtil.class);
        BDDMockito.when(APIUtil.getAPI((GovernanceArtifact)Mockito.any(),(Registry)Mockito.any())).thenReturn(new API
                (getAPIIdentifier(SAMPLE_API_NAME, API_PROVIDER, SAMPLE_API_VERSION)));
        abstractAPIManager.tenantDomain = SAMPLE_TENANT_DOMAIN_1;
        Assert.assertEquals(abstractAPIManager.getSubscriberAPIs(subscriber).size(),1);

    }

    @Test
    public void testGetApplicationByUUID() throws APIManagementException {
        Application application = new Application("app1");
        Mockito.when(apiMgtDAO.getApplicationByUUID(Mockito.anyString())).thenReturn(application);
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(apiMgtDAO);
        Assert.assertEquals(abstractAPIManager.getApplicationByUUID(SAMPLE_RESOURCE_ID).getUUID(), "app1");
    }

    @Test
    public void testGetSubscriptionByUUID() throws APIManagementException {
        Subscriber subscriber = new Subscriber("sub1");
        SubscribedAPI subscribedAPI = new SubscribedAPI(subscriber,
                getAPIIdentifier(SAMPLE_API_NAME, API_PROVIDER, SAMPLE_API_VERSION));
        Mockito.when(apiMgtDAO.getSubscriptionByUUID(Mockito.anyString())).thenReturn(subscribedAPI);
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(apiMgtDAO);
        Assert.assertEquals(abstractAPIManager.getSubscriptionByUUID(SAMPLE_RESOURCE_ID).getApiId().getApiName(),
                SAMPLE_API_NAME);
    }

    @Test
    public void testHandleException1() throws APIManagementException {
        String msg = "Sample error message";
        Exception e = new Exception();
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(null, null, null, null);
        try {
            abstractAPIManager.handleException(msg, e);
            Assert.fail("Exception not thrown for error scenarios");
        } catch (APIManagementException e1) {
            Assert.assertTrue(e1.getMessage().contains(msg));
        }
    }

    @Test
    public void testHandleException2() throws APIManagementException {
        String msg = "Sample error message";
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(null, null, null, null);
        try {
            abstractAPIManager.handleException(msg);
            Assert.fail("Exception not thrown for error scenarios");
        } catch (APIManagementException e1) {
            Assert.assertTrue(e1.getMessage().contains(msg));
        }
    }

    @Test
    public void testHandleResourceAlreadyExistsException() throws APIManagementException {
        String msg = "Sample error message";
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(null, null, null, null);
        try {
            abstractAPIManager.handleResourceAlreadyExistsException(msg);
            Assert.fail("Exception not thrown for error scenarios");
        } catch (APIMgtResourceAlreadyExistsException e1) {
            Assert.assertTrue(e1.getMessage().contains(msg));
        }
    }

    @Test
    public void testHandleResourceNotFoundException() throws APIManagementException {
        String msg = "Sample error message";
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(null, null, null, null);
        try {
            abstractAPIManager.handleResourceNotFoundException(msg);
            Assert.fail("Exception not thrown for error scenarios");
        } catch (APIMgtResourceNotFoundException e1) {
            Assert.assertTrue(e1.getMessage().contains(msg));
        }
    }

    @Test
    public void testHandlePolicyNotFoundException() throws APIManagementException {
        String msg = "Sample error message";
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(null, null, null, null);
        try {
            abstractAPIManager.handlePolicyNotFoundException(msg);
            Assert.fail("Exception not thrown for error scenarios");
        } catch (PolicyNotFoundException e1) {
            Assert.assertTrue(e1.getMessage().contains(msg));
        }
    }

    @Test
    public void testHandleBlockConditionNotFoundException() throws APIManagementException {
        String msg = "Sample error message";
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(null, null, null, null);
        try {
            abstractAPIManager.handleBlockConditionNotFoundException(msg);
            Assert.fail("Exception not thrown for error scenarios");
        } catch (BlockConditionNotFoundException e1) {
            Assert.assertTrue(e1.getMessage().contains(msg));
        }
    }

    @Test
    public void testIsApplicationTokenExists() throws APIManagementException {
        Mockito.when(apiMgtDAO.isAccessTokenExists(Mockito.anyString())).thenReturn(true);
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(apiMgtDAO);
        Assert.assertEquals(abstractAPIManager.isApplicationTokenExists(SAMPLE_RESOURCE_ID), true);
    }

    @Test
    public void testIsApplicationTokenRevoked() throws APIManagementException {
        Mockito.when(apiMgtDAO.isAccessTokenRevoked(Mockito.anyString())).thenReturn(true);
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(apiMgtDAO);
        Assert.assertEquals(abstractAPIManager.isApplicationTokenRevoked(SAMPLE_RESOURCE_ID), true);
    }

    @Test
    public void testGetAccessTokenData() throws APIManagementException {
        APIKey apiKey = new APIKey();
        apiKey.setAccessToken(SAMPLE_RESOURCE_ID);
        Mockito.when(apiMgtDAO.getAccessTokenData(Mockito.anyString())).thenReturn(apiKey);
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(apiMgtDAO);
        Assert.assertEquals(abstractAPIManager.getAccessTokenData(SAMPLE_RESOURCE_ID).getAccessToken(),
                SAMPLE_RESOURCE_ID);
    }

    @Test
    public void testSearchAccessToken() throws APIManagementException {
        APIKey apiKey1 = new APIKey();
        apiKey1.setAccessToken("token1");
        Map<Integer, APIKey> accessTokenMap = new HashMap<Integer, APIKey>();
        accessTokenMap.put(1, apiKey1);

        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(apiMgtDAO);
        Mockito.when(apiMgtDAO.getAccessTokens(Mockito.anyString())).thenReturn(accessTokenMap);
        Assert.assertEquals(abstractAPIManager.searchAccessToken(null, "query1", API_PROVIDER).size(), 1);
        Mockito.when(apiMgtDAO.getAccessTokensByUser(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(accessTokenMap);
        Assert.assertEquals(abstractAPIManager.searchAccessToken("User", "query1", API_PROVIDER).size(), 1);
        APIKey apiKey2 = new APIKey();
        apiKey1.setAccessToken("token2");
        accessTokenMap.put(2, apiKey2);
        Mockito.when(apiMgtDAO.getAccessTokensByDate(Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyString()))
                .thenReturn(accessTokenMap);
        Assert.assertEquals(abstractAPIManager.searchAccessToken("Before", "query1", API_PROVIDER).size(), 2);
        Assert.assertEquals(abstractAPIManager.searchAccessToken("After", "query1", API_PROVIDER).size(), 2);
        Assert.assertEquals(abstractAPIManager.searchAccessToken("default", "query1", API_PROVIDER).size(), 2);

    }

    @Test
    public void testGetAPIByAccessToken() throws APIManagementException {
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(apiMgtDAO);
        Set<APIIdentifier> apis = new HashSet<APIIdentifier>();
        apis.add(getAPIIdentifier(SAMPLE_API_NAME, API_PROVIDER, SAMPLE_API_VERSION));
        Mockito.when(apiMgtDAO.getAPIByAccessToken(Mockito.anyString())).thenReturn(apis);
        Assert.assertEquals(abstractAPIManager.getAPIByAccessToken(SAMPLE_RESOURCE_ID).size(), 1);
    }

    @Test
    public void testGetAPI() throws APIManagementException, RegistryException {
        APIIdentifier identifier = getAPIIdentifier(SAMPLE_API_NAME, API_PROVIDER, SAMPLE_API_VERSION);
        GenericArtifact genericArtifact = getGenericArtifact(SAMPLE_API_NAME, API_PROVIDER, SAMPLE_API_VERSION,
                "sample");
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(genericArtifactManager, registry, null);
        Mockito.when(genericArtifactManager.getGenericArtifact(Mockito.anyString())).thenReturn(genericArtifact);
        Resource resource = new ResourceImpl();
        Mockito.when(registry.get(Mockito.anyString())).thenThrow(RegistryException.class).thenReturn(resource);
        try {
            abstractAPIManager.getAPI(identifier, null, "/test");
            Assert.fail("Registry exception not thrown for error scenario");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Failed to get API from : "));
        }
        try {
            abstractAPIManager.getAPI(identifier, null, "/test");
            Assert.fail("APIM exception not thrown for error scenario");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("artifact id is null for : "));
        }
        resource.setUUID(SAMPLE_RESOURCE_ID);
        PowerMockito.mockStatic(APIUtil.class);
        BDDMockito.when(APIUtil.getAPI(genericArtifact, registry, null, "/test")).thenReturn(new API(identifier));
        Assert.assertEquals(abstractAPIManager.getAPI(identifier, null, "/test").getId().getProviderName(),
                API_PROVIDER);
    }

    @Test
    public void testGetAllTiers() throws APIManagementException {
        Map<String, Tier> tierMap = new HashMap<String, Tier>();
        Tier tier1 = new Tier("tier1");
        Tier tier2 = new Tier("tier2");
        tierMap.put("Gold", tier1);
        tierMap.put("Silver", tier2);
        PowerMockito.mockStatic(APIUtil.class);
        BDDMockito.when(APIUtil.getAllTiers()).thenReturn(tierMap);
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(null, null, null, null);
        abstractAPIManager.tenantId = -1;
        Assert.assertEquals(abstractAPIManager.getAllTiers().size(), 2);
        abstractAPIManager.tenantId = -1234;
        abstractAPIManager.tenantDomain = SAMPLE_TENANT_DOMAIN_1;
        BDDMockito.when(APIUtil.getAllTiers(Mockito.anyInt())).thenReturn(tierMap);
        Assert.assertEquals(abstractAPIManager.getAllTiers().size(), 2);
    }

    @Test
    public void testGetAllTiersForTenant() throws APIManagementException {

        Mockito.when(privilegedCarbonContext.getTenantId()).thenReturn(-1234, -1, 1);
        Map<String, Tier> tierMap = new HashMap<String, Tier>();
        Tier tier1 = new Tier("tier1");
        Tier tier2 = new Tier("tier2");
        tierMap.put("Gold", tier1);
        tierMap.put("Silver", tier2);
        PowerMockito.mockStatic(APIUtil.class);
        BDDMockito.when(APIUtil.getAllTiers()).thenReturn(tierMap);
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(null, null, null, null);
        Assert.assertEquals(abstractAPIManager.getAllTiers(SAMPLE_TENANT_DOMAIN_1).size(), 2);
        BDDMockito.when(APIUtil.getAllTiers(Mockito.anyInt())).thenReturn(tierMap);
        Assert.assertEquals(abstractAPIManager.getAllTiers(SAMPLE_TENANT_DOMAIN_1).size(), 2);
        Assert.assertEquals(abstractAPIManager.getAllTiers(SAMPLE_TENANT_DOMAIN_1).size(), 2); // verify the next branch
    }

    @Test
    public void testGetTiers() throws APIManagementException {
        Map<String, Tier> tierMap1 = new HashMap<String, Tier>();
        Map<String, Tier> tierMap2 = new HashMap<String, Tier>();
        Map<String, Tier> tierMap3 = new HashMap<String, Tier>();
        Tier tier1 = new Tier("tier1");
        Tier tier2 = new Tier("tier2");
        Tier tier3 = new Tier("tier3");
        tierMap1.put("Gold", tier1);
        tierMap2.put("Gold", tier1);
        tierMap2.put("Silver", tier2);
        tierMap3.put("Gold", tier1);
        tierMap3.put("Silver", tier2);
        tierMap3.put("Platinum", tier3);
        PowerMockito.mockStatic(APIUtil.class);
        BDDMockito.when(APIUtil.getTiers()).thenReturn(tierMap1);
        BDDMockito.when(APIUtil.getTiers(Mockito.anyInt())).thenReturn(tierMap2);
        BDDMockito.when(APIUtil.isAdvanceThrottlingEnabled()).thenReturn(false, false, true);
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(null, null, null, null);
        abstractAPIManager.tenantId = -1;
        Assert.assertEquals(abstractAPIManager.getTiers().size(), 1);
        abstractAPIManager.tenantId = -1234;
        Assert.assertEquals(abstractAPIManager.getTiers().size(), 2);
        BDDMockito.when(APIUtil.getTiersFromPolicies(Mockito.anyString(), Mockito.anyInt())).thenReturn(tierMap3);
        Assert.assertEquals(abstractAPIManager.getTiers().size(), 3);
    }

    @Test
    public void testGetTiersForTenant() throws APIManagementException {
        Mockito.when(privilegedCarbonContext.getTenantId()).thenReturn(-1234, -1, 1);
        Map<String, Tier> tierMap1 = new HashMap<String, Tier>();
        Map<String, Tier> tierMap2 = new HashMap<String, Tier>();
        Map<String, Tier> tierMap3 = new HashMap<String, Tier>();
        Tier tier1 = new Tier("tier1");
        Tier tier2 = new Tier("tier2");
        Tier tier3 = new Tier("tier3");
        tierMap1.put("Gold", tier1);
        tierMap2.put("Gold", tier1);
        tierMap2.put("Silver", tier2);
        tierMap3.put("Gold", tier1);
        tierMap3.put("Silver", tier2);
        tierMap3.put("Platinum", tier3);
        PowerMockito.mockStatic(APIUtil.class);
        BDDMockito.when(APIUtil.getTiers()).thenReturn(tierMap1);
        BDDMockito.when(APIUtil.getTiers(Mockito.anyInt())).thenReturn(tierMap2);
        BDDMockito.when(APIUtil.isAdvanceThrottlingEnabled()).thenReturn(false, false, false, true);
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(null, null, null, null);
        Assert.assertEquals(abstractAPIManager.getTiers(SAMPLE_TENANT_DOMAIN_1).size(), 1);
        Assert.assertEquals(abstractAPIManager.getTiers(SAMPLE_TENANT_DOMAIN_1).size(), 1); //verify next branch of if
        // condition
        Assert.assertEquals(abstractAPIManager.getTiers(SAMPLE_TENANT_DOMAIN_1).size(), 2);
        BDDMockito.when(APIUtil.getTiersFromPolicies(Mockito.anyString(), Mockito.anyInt())).thenReturn(tierMap3);
        Assert.assertEquals(abstractAPIManager.getTiers(SAMPLE_TENANT_DOMAIN_1).size(), 3);
    }

    @Test
    public void testGetTiersForTierType() throws APIManagementException {
        Map<String, Tier> tierMap1 = new HashMap<String, Tier>();
        Map<String, Tier> tierMap2 = new HashMap<String, Tier>();
        Map<String, Tier> tierMap3 = new HashMap<String, Tier>();
        Tier tier1 = new Tier("tier1");
        Tier tier2 = new Tier("tier2");
        Tier tier3 = new Tier("tier3");
        tierMap1.put("Gold", tier1);
        tierMap2.put("Gold", tier1);
        tierMap2.put("Silver", tier2);
        tierMap3.put("Gold", tier1);
        tierMap3.put("Silver", tier2);
        tierMap3.put("Platinum", tier3);
        PowerMockito.mockStatic(APIUtil.class);
        BDDMockito.when(APIUtil.getTiers(Mockito.anyInt(), Mockito.anyString())).thenReturn(tierMap1);
        BDDMockito.when(APIUtil.getTenantId(Mockito.anyString())).thenReturn(-1234);
        BDDMockito.when(APIUtil.getTiersFromPolicies(Mockito.anyString(), Mockito.anyInt()))
                .thenReturn(tierMap1, tierMap2, tierMap3);
        BDDMockito.when(APIUtil.isAdvanceThrottlingEnabled()).thenReturn(false, true, true, true, true);
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(null, null, null, null);
        Assert.assertEquals(abstractAPIManager.getTiers(0, API_PROVIDER).size(), 1);
        Assert.assertEquals(abstractAPIManager.getTiers(0, API_PROVIDER).size(), 1); //verify next branch of if
        // condition
        Assert.assertEquals(abstractAPIManager.getTiers(1, API_PROVIDER).size(), 2);
        BDDMockito.when(APIUtil.getTiersFromPolicies(Mockito.anyString(), Mockito.anyInt())).thenReturn(tierMap3);
        Assert.assertEquals(abstractAPIManager.getTiers(2, API_PROVIDER).size(), 3);
        try {
            abstractAPIManager.getTiers(3, API_PROVIDER);
            Assert.fail("Exception not thrown undefined tier type");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("No such a tier type : "));
        }
    }

    @Test
    public void testGetTenantDomainMappings() throws APIManagementException {
        Map<String, String> domainMappings = new HashMap<String, String>();
        domainMappings.put("domain1", SAMPLE_TENANT_DOMAIN);
        domainMappings.put("domain2", SAMPLE_TENANT_DOMAIN);
        PowerMockito.mockStatic(APIUtil.class);
        BDDMockito.when(APIUtil.getDomainMappings(Mockito.anyString(), Mockito.anyString())).thenReturn(domainMappings);
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(null, null, null, null);
        Assert.assertEquals(abstractAPIManager.getTenantDomainMappings(SAMPLE_TENANT_DOMAIN, "api").size(), 2);
    }

    @Test
    public void testIsDuplicateContextTemplate() throws APIManagementException {
        Mockito.when(apiMgtDAO.isDuplicateContextTemplate(Mockito.anyString())).thenReturn(true, false, true, false);
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(apiMgtDAO);
        abstractAPIManager.tenantDomain = SAMPLE_TENANT_DOMAIN_1;
        Assert.assertTrue(abstractAPIManager.isDuplicateContextTemplate("/t/context"));
        Assert.assertFalse(abstractAPIManager.isDuplicateContextTemplate("context"));
        abstractAPIManager.tenantDomain = SAMPLE_TENANT_DOMAIN;
        Assert.assertTrue(abstractAPIManager.isDuplicateContextTemplate("/t/context"));
        Assert.assertFalse(abstractAPIManager.isDuplicateContextTemplate("context"));
        abstractAPIManager.tenantDomain = null;
        Assert.assertFalse(abstractAPIManager.isDuplicateContextTemplate(null));
        Assert.assertFalse(abstractAPIManager.isDuplicateContextTemplate("context"));
    }

    @Test
    public void testGetApiNamesMatchingContext() throws APIManagementException {
        List<String> apiList = new ArrayList<String>();
        apiList.add("api1");
        apiList.add("api2");
        apiList.add("api3");
        Mockito.when(apiMgtDAO.getAPINamesMatchingContext(Mockito.anyString())).thenReturn(apiList);
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(apiMgtDAO);
        Assert.assertEquals(abstractAPIManager.getApiNamesMatchingContext("context").size(), 3);
    }

    @Test
    public void testGetPolicies() throws APIManagementException {
        APIPolicy[] policies1 = { new APIPolicy("policy1") };
        ApplicationPolicy[] policies2 = { new ApplicationPolicy("policy2"), new ApplicationPolicy("policy3") };
        SubscriptionPolicy[] policies3 = { new SubscriptionPolicy("policy4"), new SubscriptionPolicy("policy5"),
                new SubscriptionPolicy("policy6") };
        GlobalPolicy[] policies4 = { new GlobalPolicy("policy7"), new GlobalPolicy("policy8"),
                new GlobalPolicy("policy9"), new GlobalPolicy("policy0") };
        PowerMockito.mockStatic(APIUtil.class);
        BDDMockito.when(APIUtil.getTenantId(Mockito.anyString())).thenReturn(-1234);
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(apiMgtDAO);
        Mockito.when(apiMgtDAO.getAPIPolicies(Mockito.anyInt())).thenReturn(policies1);
        Mockito.when(apiMgtDAO.getApplicationPolicies(Mockito.anyInt())).thenReturn(policies2);
        Mockito.when(apiMgtDAO.getSubscriptionPolicies(Mockito.anyInt())).thenReturn(policies3);
        Mockito.when(apiMgtDAO.getGlobalPolicies(Mockito.anyInt())).thenReturn(policies4);
        Assert.assertEquals(abstractAPIManager.getPolicies(API_PROVIDER, PolicyConstants.POLICY_LEVEL_API).length, 1);
        Assert.assertEquals(abstractAPIManager.getPolicies(API_PROVIDER, PolicyConstants.POLICY_LEVEL_APP).length, 2);
        Assert.assertEquals(abstractAPIManager.getPolicies(API_PROVIDER, PolicyConstants.POLICY_LEVEL_SUB).length, 3);
        Assert.assertEquals(abstractAPIManager.getPolicies(API_PROVIDER, PolicyConstants.POLICY_LEVEL_GLOBAL).length,
                4);
        Assert.assertNull(abstractAPIManager.getPolicies(API_PROVIDER, "Test"));
    }

    @Test
    public void testSearchPaginatedAPIs()
            throws APIManagementException, org.wso2.carbon.user.api.UserStoreException, RegistryException {
        Map<String, Object> subContextResult = new HashMap<String, Object>();
        subContextResult.put("1", new Object());
        UserRegistry registry = Mockito.mock(UserRegistry.class);
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapperExtended(null, registryService, registry,
                tenantManager);
        Mockito.when(tenantManager.getTenantId(Mockito.anyString())).thenReturn(-1234);
        Mockito.when(registryService.getGovernanceUserRegistry(Mockito.anyString(), Mockito.anyInt()))
                .thenThrow(RegistryException.class).thenReturn(registry);
        try {
            abstractAPIManager.searchPaginatedAPIs("search", API_PROVIDER, 0, 5, false);
            Assert.fail("Exception not thrown for error scenario");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Failed to Search APIs"));
        }
        API api = new API(getAPIIdentifier(SAMPLE_API_NAME, API_PROVIDER, SAMPLE_API_VERSION));
        Documentation documentation = new Documentation(DocumentationType.HOWTO, "DOC1");
        Map<Documentation, API> documentationAPIMap = new HashMap<Documentation, API>();
        PowerMockito.mockStatic(APIUtil.class);
        BDDMockito.when(APIUtil
                .searchAPIsByDoc((Registry) Mockito.any(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyString(),
                        Mockito.anyString())).thenReturn(documentationAPIMap);
        Assert.assertEquals(
                abstractAPIManager.searchPaginatedAPIs("doc=search", SAMPLE_TENANT_DOMAIN_1, 0, 5, false).get("length"),
                0);
        documentationAPIMap.put(documentation, api);
        Assert.assertEquals(abstractAPIManager.searchPaginatedAPIs("doc=search", null, 0, 5, false).get("length"), 5);
        Map<String, Object> contextApis = new HashMap<String, Object>();
        contextApis.put("api2", new Object());
        BDDMockito.when(APIUtil.searchAPIsByURLPattern((Registry) Mockito.any(), Mockito.anyString(), Mockito.anyInt(),
                Mockito.anyInt())).thenReturn(contextApis);
        Assert.assertTrue(
                abstractAPIManager.searchPaginatedAPIs("subcontext=search", null, 0, 5, false).containsKey("api2"));

        TestUtils.mockAPIMConfiguration(APIConstants.API_STORE_APIS_PER_PAGE, null);
        Assert.assertEquals(abstractAPIManager.searchPaginatedAPIs("search", null, 0, 5, false).get("length"), 0);
        TestUtils.mockAPIMConfiguration(APIConstants.API_STORE_APIS_PER_PAGE, "5");
        GovernanceArtifact governanceArtifact = getGenericArtifact(SAMPLE_API_NAME, API_PROVIDER, SAMPLE_API_VERSION,
                "qname");
        List<GovernanceArtifact> governanceArtifactList = new ArrayList<GovernanceArtifact>();
        governanceArtifactList.add(governanceArtifact);
        Assert.assertEquals(abstractAPIManager.searchPaginatedAPIs("search", null, 0, 5, false).get("length"), 0);
        Assert.assertEquals(
                abstractAPIManager.searchPaginatedAPIs(APIConstants.API_OVERVIEW_PROVIDER, null, 0, 5, false)
                        .get("length"), 0);
        BDDMockito.when(GovernanceUtils
                .findGovernanceArtifacts(APIConstants.API_OVERVIEW_OWNER, registry, APIConstants.API_RXT_MEDIA_TYPE))
                .thenThrow(RegistryException.class).thenReturn(governanceArtifactList);
        try {
            abstractAPIManager.searchPaginatedAPIs(APIConstants.API_OVERVIEW_PROVIDER, null, 0, 5, false);
            Assert.fail("APIM exception not thrown for error scenario");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Failed to Search APIs"));
        }
        API api1 = new API(getAPIIdentifier("api1", API_PROVIDER, "v1"));
        BDDMockito.when(APIUtil.getAPI((GovernanceArtifact) Mockito.any(), (Registry) Mockito.any())).thenReturn(api1);
        SortedSet<API> apiSet = (SortedSet<API>) abstractAPIManager
                .searchPaginatedAPIs(APIConstants.API_OVERVIEW_PROVIDER, null, 0, 5, false).get("apis");
        Assert.assertEquals(apiSet.size(), 1);
        Assert.assertEquals(apiSet.first().getId().getApiName(), "api1");
        Assert.assertEquals(abstractAPIManager.searchPaginatedAPIs(APIConstants.API_OVERVIEW_PROVIDER, null, 0, 5, true)
                .get("length"), 0);
        PowerMockito.when(paginationContext.getLength()).thenReturn(12);
        Assert.assertTrue(
                (Boolean) abstractAPIManager.searchPaginatedAPIs(APIConstants.API_OVERVIEW_PROVIDER, null, 0, 5, true)
                        .get("isMore"));
    }

    @Test
    public void testDeleteGlobalMediationPolicy() throws APIManagementException, RegistryException {
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapperExtended(null, null, registry, null);
        Mockito.when(registry.resourceExists(Mockito.anyString())).thenReturn(true, false, true, false);
        Mockito.doThrow(RegistryException.class).doNothing().when(registry).delete(Mockito.anyString());
        try {
            abstractAPIManager.deleteGlobalMediationPolicy(SAMPLE_RESOURCE_ID);
            Assert.fail("Registry exception not thrown for error scenario");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Failed to delete global mediation policy"));
        }
        Assert.assertFalse(abstractAPIManager.deleteGlobalMediationPolicy(SAMPLE_RESOURCE_ID));
        Assert.assertTrue(abstractAPIManager.deleteGlobalMediationPolicy(SAMPLE_RESOURCE_ID));
    }

    @Test
    public void testGetCreatedResourceUuid() throws RegistryException, APIManagementException {
        Resource resource = new ResourceImpl();
        resource.setUUID(SAMPLE_RESOURCE_ID);
        Mockito.when(registry.get(Mockito.anyString())).thenThrow(RegistryException.class).thenReturn(resource);
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(registry);
        Assert.assertNull(abstractAPIManager.getCreatedResourceUuid("/test/path"));
        Assert.assertEquals(abstractAPIManager.getCreatedResourceUuid("/test/path"), SAMPLE_RESOURCE_ID);
    }

    @Test
    public void testGetAllApiSpecificMediationPolicies()
            throws RegistryException, APIManagementException, IOException, XMLStreamException {
        APIIdentifier identifier = getAPIIdentifier(SAMPLE_API_NAME, API_PROVIDER, SAMPLE_API_VERSION);
        String parentCollectionPath =
                APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR + identifier.getProviderName()
                        + RegistryConstants.PATH_SEPARATOR + identifier.getApiName() + RegistryConstants.PATH_SEPARATOR
                        + identifier.getVersion() + APIConstants.API_RESOURCE_NAME;
        parentCollectionPath = parentCollectionPath.substring(0, parentCollectionPath.lastIndexOf("/"));
        Collection parentCollection = new CollectionImpl();
        parentCollection.setChildren(new String[] {
                parentCollectionPath + RegistryConstants.PATH_SEPARATOR + APIConstants.API_CUSTOM_SEQUENCE_TYPE_IN,
                parentCollectionPath + RegistryConstants.PATH_SEPARATOR + APIConstants.API_CUSTOM_SEQUENCE_TYPE_OUT,
                parentCollectionPath + RegistryConstants.PATH_SEPARATOR
                        + APIConstants.API_CUSTOM_SEQUENCE_TYPE_FAULT });
        Collection childCollection = new CollectionImpl();
        childCollection.setChildren(new String[] { "mediation1" });
        Mockito.when(registry.get(parentCollectionPath)).thenThrow(RegistryException.class)
                .thenReturn(parentCollection);
        Mockito.when(registry.get(
                parentCollectionPath + RegistryConstants.PATH_SEPARATOR + APIConstants.API_CUSTOM_SEQUENCE_TYPE_IN))
                .thenReturn(childCollection);

        Resource resource = new ResourceImpl();
        resource.setUUID(SAMPLE_RESOURCE_ID);

        String mediationPolicyContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<sequence xmlns=\"http://ws.apache.org/ns/synapse\" name=\"default-endpoint\">\n</sequence>";
        resource.setContent(mediationPolicyContent);
        Mockito.when(registry.get("mediation1")).thenReturn(resource);
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(registry);
        try {
            abstractAPIManager.getAllApiSpecificMediationPolicies(identifier);
            Assert.fail("Registry exception not thrown for error scenario");
        } catch (APIManagementException e) {
            Assert.assertTrue(
                    e.getMessage().contains("Error occurred  while getting Api Specific mediation policies "));
        }
        Assert.assertEquals(abstractAPIManager.getAllApiSpecificMediationPolicies(identifier).size(), 1);
        PowerMockito.mockStatic(IOUtils.class);
        PowerMockito.mockStatic(AXIOMUtil.class);
        PowerMockito.when(IOUtils.toString((InputStream) Mockito.any(), Mockito.anyString()))
                .thenThrow(IOException.class).thenReturn(mediationPolicyContent);
        PowerMockito.when(AXIOMUtil.stringToOM(Mockito.anyString())).thenThrow(XMLStreamException.class);
        abstractAPIManager.getAllApiSpecificMediationPolicies(identifier);// covers exception which is only logged
        abstractAPIManager.getAllApiSpecificMediationPolicies(identifier);// covers exception which is only logged
    }

    @Test
    public void testGetMediationNameFromConfig() throws Exception {
        String mediationPolicyContent =
                "<inSequence>\n" + "   <property name=\"ClientApiNonBlocking\"\n" + "           value=\"true\"\n"
                        + "           scope=\"axis2\"\n" + "           action=\"remove\"/>\n" + "   <send>\n"
                        + "      <endpoint name=\"FileEpr\">\n"
                        + "         <address uri=\"vfs:file:////home/test/file-out\"/>\n" + "      </endpoint>\n"
                        + "   </send>\n" + "</inSequen>";
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(null, null, null, null);
        Assert.assertNull(abstractAPIManager.getMediationNameFromConfig(mediationPolicyContent));
        mediationPolicyContent =
                "<sequence>\n" + "\t<name>policy1</name>\n" + "   <property name=\"ClientApiNonBlocking\"\n"
                        + "           value=\"true\"\n" + "           scope=\"axis2\"\n"
                        + "           action=\"remove\"/>\n" + "   <send>\n" + "      <endpoint name=\"FileEpr\">\n"
                        + "         <address uri=\"vfs:file:////home/shammi/file-out\"/>\n" + "      </endpoint>\n"
                        + "   </send>\n" + "</sequence>";
        Assert.assertEquals(abstractAPIManager.getMediationNameFromConfig(mediationPolicyContent), "policy1.xml");

    }

    @Test
    public void testGetApiSpecificMediationPolicy()
            throws RegistryException, APIManagementException, IOException, XMLStreamException {
        String parentCollectionPath = "config/mediation/";

        parentCollectionPath = parentCollectionPath.substring(0, parentCollectionPath.lastIndexOf("/"));
        Collection parentCollection = new CollectionImpl();
        parentCollection.setChildren(new String[] {
                parentCollectionPath + RegistryConstants.PATH_SEPARATOR + APIConstants.API_CUSTOM_SEQUENCE_TYPE_OUT,
                parentCollectionPath + RegistryConstants.PATH_SEPARATOR + APIConstants.API_CUSTOM_SEQUENCE_TYPE_FAULT,
                parentCollectionPath + RegistryConstants.PATH_SEPARATOR + APIConstants.API_CUSTOM_SEQUENCE_TYPE_IN, });
        Collection childCollection = new CollectionImpl();
        childCollection.setChildren(new String[] { "mediation1" });
        Mockito.when(registry.get(parentCollectionPath)).thenThrow(RegistryException.class)
                .thenReturn(null, parentCollection);
        Mockito.when(registry.get(
                parentCollectionPath + RegistryConstants.PATH_SEPARATOR + APIConstants.API_CUSTOM_SEQUENCE_TYPE_IN))
                .thenReturn(childCollection);

        Resource resource = new ResourceImpl("api/mediation/policy1", new ResourceDO());
        resource.setUUID(SAMPLE_RESOURCE_ID);

        String mediationPolicyContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<sequence xmlns=\"http://ws.apache.org/ns/synapse\" name=\"default-endpoint\">\n</sequence>";
        resource.setContent(mediationPolicyContent);
        Mockito.when(registry.get("mediation1")).thenReturn(resource);
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(registry);
        try {
            abstractAPIManager.getApiSpecificMediationPolicy(parentCollectionPath, SAMPLE_RESOURCE_ID);
            Assert.fail("Registry exception not thrown for error scenario");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Error while obtaining registry objects"));
        }
        Assert.assertNull(abstractAPIManager.getApiSpecificMediationPolicy(parentCollectionPath, SAMPLE_RESOURCE_ID));
        Assert.assertEquals(
                abstractAPIManager.getApiSpecificMediationPolicy(parentCollectionPath, SAMPLE_RESOURCE_ID).getName(),
                "default-endpoint");
        PowerMockito.mockStatic(IOUtils.class);
        PowerMockito.mockStatic(AXIOMUtil.class);
        PowerMockito.when(IOUtils.toString((InputStream) Mockito.any(), Mockito.anyString()))
                .thenThrow(IOException.class).thenReturn(mediationPolicyContent);
        PowerMockito.when(AXIOMUtil.stringToOM(Mockito.anyString())).thenThrow(XMLStreamException.class);

        try {
            abstractAPIManager.getApiSpecificMediationPolicy(parentCollectionPath, SAMPLE_RESOURCE_ID);
            Assert.fail("IO exception not thrown for error scenario");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Error occurred while converting content stream into string"));
        }
        try {
            abstractAPIManager.getApiSpecificMediationPolicy(parentCollectionPath, SAMPLE_RESOURCE_ID);
            Assert.fail("XMLStream exception  not thrown for error scenario");
        } catch (APIManagementException e) {
            Assert.assertTrue(
                    e.getMessage().contains("Error occurred while getting omElement out of mediation content"));
        }
        resource.setContent(null);
        try {
            abstractAPIManager.getApiSpecificMediationPolicy(parentCollectionPath, SAMPLE_RESOURCE_ID);
            Assert.fail("Registry exception not thrown for error scenario");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Error occurred while accessing content stream of mediation"));
        }
    }

    @Test
    public void testDeleteApiSpecificMediationPolicy() throws RegistryException, APIManagementException {
        String resourcePath = "config/mediation/";
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapperExtended(null, null, registry, null);
        Mockito.when(registry.resourceExists(Mockito.anyString())).thenReturn(true, false, true, false);
        Mockito.doThrow(RegistryException.class).doNothing().when(registry).delete(Mockito.anyString());
        try {
            abstractAPIManager.deleteApiSpecificMediationPolicy(resourcePath, SAMPLE_RESOURCE_ID);
            Assert.fail("Registry exception not thrown for error scenario");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Failed to delete specific mediation policy"));
        }
        Assert.assertFalse(abstractAPIManager.deleteApiSpecificMediationPolicy(resourcePath, SAMPLE_RESOURCE_ID));
        Assert.assertTrue(abstractAPIManager.deleteApiSpecificMediationPolicy(resourcePath, SAMPLE_RESOURCE_ID));
    }

    @Test
    public void testCheckIfResourceExists() throws APIManagementException, RegistryException {
        String resourcePath = "config/mediation/";
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(registry);
        Mockito.when(registry.resourceExists(Mockito.anyString())).thenThrow(RegistryException.class)
                .thenReturn(false, true);
        try {
            abstractAPIManager.checkIfResourceExists(resourcePath);
            Assert.fail("Registry exception not thrown for error scenario");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Error while obtaining registry objects"));
        }
        Assert.assertFalse(abstractAPIManager.checkIfResourceExists(resourcePath));
        Assert.assertTrue(abstractAPIManager.checkIfResourceExists(resourcePath));
    }

    @Test
    public void testGetThumbnailLastUpdatedTime()
            throws APIManagementException, org.wso2.carbon.user.api.UserStoreException, RegistryException {
        APIIdentifier identifier = new APIIdentifier(SAMPLE_API_NAME, API_PROVIDER, SAMPLE_API_VERSION);
        Mockito.when(registry.resourceExists(Mockito.anyString())).thenReturn(true, false, true);
        ResourceDO resourceDO = new ResourceDO();
        resourceDO.setLastUpdatedOn(34579002);
        Resource resource = new ResourceImpl("test/", resourceDO);

        Mockito.when(registry.get(Mockito.anyString())).thenThrow(RegistryException.class).thenReturn(resource);
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(registry);

        try {
            abstractAPIManager.getThumbnailLastUpdatedTime(identifier);
            Assert.fail("Registry exception not thrown for error scenario");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Error while loading API icon from the registry"));
        }
        Assert.assertNull(abstractAPIManager.getThumbnailLastUpdatedTime(identifier));
        Assert.assertEquals(abstractAPIManager.getThumbnailLastUpdatedTime(identifier), "34579002");
    }

    @Test
    public void testGetSwaggerDefinitionTimeStamps() throws Exception {
        APIIdentifier identifier = getAPIIdentifier(SAMPLE_API_NAME, API_PROVIDER, SAMPLE_API_VERSION);
        UserRegistry registry = Mockito.mock(UserRegistry.class);
        Mockito.when(tenantManager.getTenantId(Mockito.anyString())).thenThrow(UserStoreException.class)
                .thenReturn(-1234);
        APIDefinitionFromSwagger20 apiDefinitionFromSwagger20 = Mockito.mock(APIDefinitionFromSwagger20.class);
        setFinalStatic(AbstractAPIManager.class.getDeclaredField("definitionFromSwagger20"),
                apiDefinitionFromSwagger20);
        Mockito.when(registryService.getGovernanceUserRegistry(Mockito.anyString(), Mockito.anyInt())).thenThrow
                (RegistryException.class).thenReturn(registry);
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(null, registryService,registry,
                tenantManager);
        Assert.assertNull(abstractAPIManager.getSwaggerDefinitionTimeStamps(identifier));
        Assert.assertNull(abstractAPIManager.getSwaggerDefinitionTimeStamps(identifier));
        abstractAPIManager.tenantDomain = SAMPLE_TENANT_DOMAIN_1;
        Map<String, String> result = new HashMap<String, String>();
        result.put("swagger1","scopes:apim_create,resources:{get:/*}");
        result.put("swagger2","scopes:apim_view,resources:{get:/menu}");
        Mockito.when(apiDefinitionFromSwagger20.getAPISwaggerDefinitionTimeStamps((APIIdentifier) Mockito.any(),
                (org.wso2.carbon.registry.api.Registry) Mockito.any())).thenReturn(result);
        Assert.assertEquals(abstractAPIManager.getSwaggerDefinitionTimeStamps(identifier).size(),2);
        abstractAPIManager.tenantDomain = SAMPLE_TENANT_DOMAIN;
        result.put("swagger3","");
        Assert.assertEquals(abstractAPIManager.getSwaggerDefinitionTimeStamps(identifier).size(),3);

    }

    private static void setFinalStatic(Field field, Object newValue) throws Exception {
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(null, newValue);
    }

    private GenericArtifact getGenericArtifact(String apiName, String provider, String version, String qName)
            throws GovernanceException {
        String id = UUID.randomUUID().toString();
        GenericArtifact genericArtifact = new GenericArtifactImpl(id, new QName(qName), "");
        genericArtifact.setAttribute(APIConstants.API_OVERVIEW_PROVIDER, provider);
        genericArtifact.setAttribute(APIConstants.API_OVERVIEW_NAME, apiName);
        genericArtifact.setAttribute(APIConstants.API_OVERVIEW_VERSION, version);
        return genericArtifact;
    }

    private APIIdentifier getAPIIdentifier(String apiName, String provider, String version) {
        return new APIIdentifier(provider, apiName, version);
    }
}
