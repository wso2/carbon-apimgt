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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.apimgt.api.*;
import org.wso2.carbon.apimgt.api.model.Documentation;
import org.wso2.carbon.apimgt.api.model.DocumentationType;
import org.wso2.carbon.apimgt.api.model.ResourceFile;
import org.wso2.carbon.apimgt.api.model.*;
import org.wso2.carbon.apimgt.api.model.policy.*;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dao.ScopesDAO;
import org.wso2.carbon.apimgt.impl.definitions.GraphQLSchemaDefinition;
import org.wso2.carbon.apimgt.impl.definitions.OASParserUtil;
import org.wso2.carbon.apimgt.impl.dto.KeyManagerDto;
import org.wso2.carbon.apimgt.impl.dto.ThrottleProperties;
import org.wso2.carbon.apimgt.impl.factory.KeyManagerHolder;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.persistence.APIPersistence;
import org.wso2.carbon.apimgt.persistence.dto.*;
import org.wso2.carbon.apimgt.persistence.exceptions.APIPersistenceException;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifactImpl;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.dataobjects.ResourceDO;
import org.wso2.carbon.registry.core.jdbc.realm.RegistryAuthorizationManager;
import org.wso2.carbon.registry.core.pagination.PaginationContext;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.mockito.Matchers.any;
import static org.wso2.carbon.apimgt.impl.TestUtils.mockRegistryAndUserRealm;
import static org.wso2.carbon.apimgt.impl.token.ClaimsRetriever.DEFAULT_DIALECT_URI;
import static org.wso2.carbon.utils.ServerConstants.CARBON_HOME;

@RunWith (PowerMockRunner.class)
@PrepareForTest({ APIUtil.class, MultitenantUtils.class, PrivilegedCarbonContext.class, ServiceReferenceHolder.class,
        GovernanceUtils.class, PaginationContext.class, IOUtils.class, AXIOMUtil.class, RegistryUtils.class,
        AbstractAPIManager.class, OASParserUtil.class, KeyManagerHolder.class })
public class AbstractAPIManagerTestCase {

    public static final String SAMPLE_API_NAME = "test";
    public static final String SAMPLE_API_NAME1 = "test1";
    public static final String API_PROVIDER = "admin";
    public static final String SAMPLE_API_VERSION = "1.0.0";
    public static final String SAMPLE_TENANT_DOMAIN = "carbon.super";
    public static final String SAMPLE_RESOURCE_ID = "xyz";
    public static final String SAMPLE_API_RESOURCE_ID = "xyz";
    public static final String SAMPLE_TENANT_DOMAIN_1 = "abc.com";
    private PrivilegedCarbonContext privilegedCarbonContext;
    private PaginationContext paginationContext;
    private ApiMgtDAO apiMgtDAO;
    private ScopesDAO scopesDAO;
    private Registry registry;
    private GenericArtifactManager genericArtifactManager;
    private RegistryService registryService;
    private TenantManager tenantManager;
    private GraphQLSchemaDefinition graphQLSchemaDefinition;
    private KeyManager keyManager;
    private APIPersistence apiPersistenceInstance;

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
        scopesDAO = Mockito.mock(ScopesDAO.class);
        registry = Mockito.mock(Registry.class);
        genericArtifactManager = Mockito.mock(GenericArtifactManager.class);
        registryService = Mockito.mock(RegistryService.class);
        tenantManager = Mockito.mock(TenantManager.class);
        graphQLSchemaDefinition = Mockito.mock(GraphQLSchemaDefinition.class);
        keyManager = Mockito.mock(KeyManager.class);
        apiPersistenceInstance = Mockito.mock(APIPersistence.class);
        PowerMockito.mockStatic(KeyManagerHolder.class);
        KeyManagerDto keyManagerDto = new KeyManagerDto();
        keyManagerDto.setName("default");
        keyManagerDto.setKeyManager(keyManager);
        keyManagerDto.setIssuer("https://localhost");
        Map<String, KeyManagerDto> tenantKeyManagerDtoMap = new HashMap<>();
        tenantKeyManagerDtoMap.put("default", keyManagerDto);
        PowerMockito.when(KeyManagerHolder.getTenantKeyManagers("carbon.super")).thenReturn(tenantKeyManagerDtoMap);
    }

    @Test
    public void testConstructor() throws Exception {

        ServiceReferenceHolderMockCreator holderMockCreator = new ServiceReferenceHolderMockCreator(1);
        ServiceReferenceHolderMockCreator.initContextService();
        holderMockCreator.initRegistryServiceMockCreator(false, new Object());
        RegistryAuthorizationManager registryAuthorizationManager = Mockito.mock(RegistryAuthorizationManager.class);
        Mockito.doThrow(UserStoreException.class).doNothing().when(registryAuthorizationManager)
                .authorizeRole(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        PowerMockito.whenNew(RegistryAuthorizationManager.class).withAnyArguments()
                .thenReturn(registryAuthorizationManager);
        PowerMockito.mockStatic(RegistryUtils.class);
        PowerMockito.when(RegistryUtils.getAbsolutePath((RegistryContext) Mockito.any(), Mockito.anyString()))
                .thenReturn("/test");
        try {
            new AbstractAPIManager(null) {
                @Override
                public ApiTypeWrapper getAPIorAPIProductByUUID(String uuid, String organization) throws APIManagementException {
                    return null;
                }

                @Override
                public API getLightweightAPIByUUID(String uuid, String organization)
                        throws APIManagementException {
                    return null;
                }

                @Override
                public Map<String, Object> searchPaginatedAPIs(String searchQuery, String organization, int start,
                        int end, String sortBy, String sortOrder) throws APIManagementException {
                    return null;
                }

                @Override
                public Map<String, Object> searchPaginatedContent(String searchQuery, String tenantDomain, int start,
                        int end) throws APIManagementException {
                    return null;
                }
            };
            Assert.fail("User store exception not thrown for error scenario");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Error while setting the permissions"));
        }

        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.doNothing().when(APIUtil.class, "loadTenantRegistry", Mockito.anyInt());
        PowerMockito.mockStatic(MultitenantUtils.class);
        PowerMockito.when(MultitenantUtils.getTenantDomain(Mockito.anyString())).thenReturn(SAMPLE_TENANT_DOMAIN_1);
        String userName = "admin";

        Mockito.verify(
                holderMockCreator.getRegistryServiceMockCreator().getMock().getConfigSystemRegistry(Mockito.anyInt()),
                Mockito.atLeastOnce());
    }

    @Test
    public void testGetAllApis() throws GovernanceException, APIManagementException, APIPersistenceException {
        PowerMockito.mockStatic(APIUtil.class);
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(apiPersistenceInstance);
        PublisherAPISearchResult value = new PublisherAPISearchResult();
        List<PublisherAPIInfo> publisherAPIInfoList = new ArrayList<PublisherAPIInfo>();
        PublisherAPIInfo pubInfo = new PublisherAPI();
        pubInfo.setApiName("TestAPI");
        pubInfo.setContext("/test");
        pubInfo.setId("xxxxxx");
        pubInfo.setProviderName("test");
        pubInfo.setType("API");
        pubInfo.setVersion("1");
        publisherAPIInfoList.add(pubInfo);
        value.setPublisherAPIInfoList(publisherAPIInfoList);
        
        PowerMockito.when(apiPersistenceInstance.searchAPIsForPublisher(any(Organization.class), any(String.class),
                any(Integer.class), any(Integer.class), any(UserContext.class), any(String.class), any(String.class))).thenReturn(value);
        List<API> apis = abstractAPIManager.getAllAPIs();
        Assert.assertNotNull(apis);
        Assert.assertEquals(apis.size(), 1);

    }


    @Test
    public void testGetAPIVersions() throws APIManagementException,
            RegistryException {
        String providerName = API_PROVIDER;
        String apiName = "sampleApi";
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(apiMgtDAO);
      
        Set<String> set = new HashSet<String>();
        Mockito.when(apiMgtDAO.getAPIVersions(apiName, providerName, "org1")).thenReturn(set);
        try {
            abstractAPIManager.getAPIVersions(providerName, apiName, "org1");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("API version must be a collection"));
        }

        Assert.assertEquals(abstractAPIManager.getAPIVersions(providerName, apiName, "org1").size(), 0);
        
    }

    @Test
    public void testIsAPIAvailable() throws APIManagementException {
        APIIdentifier apiIdentifier = getAPIIdentifier(SAMPLE_API_NAME, API_PROVIDER, SAMPLE_API_VERSION);
        String organization = "org1";
        String path =
                APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR + apiIdentifier.getProviderName()
                        + RegistryConstants.PATH_SEPARATOR + apiIdentifier.getApiName()
                        + RegistryConstants.PATH_SEPARATOR + apiIdentifier.getVersion();
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(apiMgtDAO);
        Mockito.when(apiMgtDAO.getUUIDFromIdentifier(apiIdentifier, organization)).thenReturn("xxxxx");
        Assert.assertTrue(abstractAPIManager.isAPIAvailable(apiIdentifier, organization));
    }

    @Test
    public void testIsAPIProductAvailable() throws APIManagementException {
        APIProductIdentifier apiProductIdentifier = getAPIProductIdentifier(SAMPLE_API_NAME, API_PROVIDER, SAMPLE_API_VERSION);
        String organization = "carbon.super";
        String path =
                APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR + apiProductIdentifier.getProviderName()
                        + RegistryConstants.PATH_SEPARATOR + apiProductIdentifier.getName()
                        + RegistryConstants.PATH_SEPARATOR + apiProductIdentifier.getVersion();
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(apiMgtDAO);
        Mockito.when(apiMgtDAO.getUUIDFromIdentifier(apiProductIdentifier, organization)).thenReturn("xxxxx");
        Assert.assertTrue(abstractAPIManager.isAPIProductAvailable(apiProductIdentifier, organization));
    }


//
//    @Test
//    public void testGetSwagger20Definition() throws Exception {
//        int tenantId = -1234;
//        Organization org = Mockito.mock(Organization.class);
//        PowerMockito.whenNew(Organization.class).withArguments(SAMPLE_TENANT_DOMAIN, null).thenReturn(org);
//
//        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(apiPersistenceInstance);
//        APIIdentifier identifier = getAPIIdentifier(SAMPLE_API_NAME, API_PROVIDER, SAMPLE_API_VERSION);
//        identifier.setUuid(SAMPLE_RESOURCE_ID);
//        PowerMockito.mockStatic(OASParserUtil.class);
//        String swaggerContent = "sample swagger";
//        PowerMockito.when(apiPersistenceInstance.getOASDefinition(org ,
//                SAMPLE_RESOURCE_ID)).thenReturn(swaggerContent);
//
//
//        Assert.assertEquals(abstractAPIManager.getOpenAPIDefinition(SAMPLE_RESOURCE_ID, SAMPLE_TENANT_DOMAIN), swaggerContent);
//        abstractAPIManager.tenantDomain = SAMPLE_TENANT_DOMAIN;
//        Assert.assertEquals(abstractAPIManager.getOpenAPIDefinition(identifier), swaggerContent);
//    }


    @Test
    public void testGetAllDocumentation() throws APIManagementException, RegistryException {
        Registry registry = Mockito.mock(UserRegistry.class);

        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(genericArtifactManager,null, registry , null, apiMgtDAO);
        abstractAPIManager.registry = registry;

        GenericArtifact genericArtifact = getGenericArtifact(SAMPLE_API_NAME, API_PROVIDER, SAMPLE_API_VERSION,
                "sample");
        genericArtifact.setAttribute(APIConstants.DOC_TYPE, "Other");
        genericArtifact.setAttribute(APIConstants.DOC_SOURCE_TYPE, "URL");
        APIIdentifier identifier = getAPIIdentifier(SAMPLE_API_NAME, API_PROVIDER, SAMPLE_API_VERSION);
        String apiDocPath =
                APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR + identifier.getProviderName()
                        + RegistryConstants.PATH_SEPARATOR + identifier.getApiName() + RegistryConstants.PATH_SEPARATOR
                        + identifier.getVersion() + RegistryConstants.PATH_SEPARATOR +
                        APIConstants.DOC_DIR + RegistryConstants.PATH_SEPARATOR;
        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.when(APIUtil.getAPIOrAPIProductDocPath(identifier)).thenReturn(apiDocPath);
        Resource resource1 = new ResourceImpl();
        resource1.setUUID(SAMPLE_RESOURCE_ID);

        Mockito.when(genericArtifact.getPath()).thenReturn("test");
        String docName = "sample";
        Documentation documentation = new Documentation(DocumentationType.HOWTO, docName);
        PowerMockito.when(APIUtil.getDocumentation(genericArtifact)).thenReturn(documentation);
        Mockito.when(registry.resourceExists(apiDocPath)).thenThrow(RegistryException.class).thenReturn(true);
        Mockito.when(apiMgtDAO.checkAPIUUIDIsARevisionUUID(Mockito.anyString())).thenReturn(null);
        try {
            abstractAPIManager.getAllDocumentation(identifier);
            Assert.fail("Registry exception not thrown for error scenario");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Failed to get documentations for api"));
        }

        Resource resource2 = new ResourceImpl();
        resource2.setUUID(SAMPLE_RESOURCE_ID);
        Mockito.when(genericArtifactManager.getGenericArtifact(SAMPLE_RESOURCE_ID)).thenReturn(genericArtifact);

        String documentationName = "doc1";
        Collection documentCollection = new CollectionImpl();
        documentCollection.setChildren(new String[] {
                apiDocPath + documentationName, apiDocPath + APIConstants.INLINE_DOCUMENT_CONTENT_DIR
        });
        Mockito.when(registry.get(apiDocPath)).thenReturn(documentCollection);
        Mockito.when(registry.get(apiDocPath + documentationName)).thenReturn(resource2);
        PowerMockito.when(APIUtil.getDocumentation(genericArtifact)).thenReturn(documentation);
        List<Documentation> documentationList = abstractAPIManager.getAllDocumentation(identifier);
        Assert.assertNotNull(documentationList);
        Assert.assertEquals(documentationList.size(), 1);
        String contentPath = APIConstants.API_LOCATION + RegistryConstants.PATH_SEPARATOR + identifier.getProviderName()
                + RegistryConstants.PATH_SEPARATOR + identifier.getApiName() + RegistryConstants.PATH_SEPARATOR
                + identifier.getVersion() + RegistryConstants.PATH_SEPARATOR + APIConstants.DOC_DIR
                + RegistryConstants.PATH_SEPARATOR + RegistryConstants.PATH_SEPARATOR + documentationName;
        genericArtifact.setAttribute(APIConstants.DOC_SOURCE_TYPE, "In line");
        genericArtifact.setAttribute(APIConstants.DOC_NAME, documentationName);
        ResourceDO resourceDO = new ResourceDO();
        resourceDO.setLastUpdatedOn(12344567);
        Resource resource3 = new ResourceImpl(contentPath, resourceDO);
        Mockito.when(registry.get(contentPath)).thenReturn(resource3);
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

        abstractAPIManager.registry = registry;

        GenericArtifact genericArtifact = getGenericArtifact(SAMPLE_API_NAME, API_PROVIDER, SAMPLE_API_VERSION,
                "sample");
        genericArtifact.setAttribute(APIConstants.DOC_TYPE, "Other");
        genericArtifact.setAttribute(APIConstants.DOC_SOURCE_TYPE, "URL");
        String apiDocPath =
                APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR + identifier.getProviderName()
                        + RegistryConstants.PATH_SEPARATOR + identifier.getApiName() + RegistryConstants.PATH_SEPARATOR
                        + identifier.getVersion() + RegistryConstants.PATH_SEPARATOR +
                        APIConstants.DOC_DIR + RegistryConstants.PATH_SEPARATOR;
        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.when(APIUtil.getAPIOrAPIProductDocPath(identifier)).thenReturn(apiDocPath);
        Resource resource1 = new ResourceImpl();
        resource1.setUUID(SAMPLE_RESOURCE_ID);

        Mockito.when(genericArtifact.getPath()).thenReturn("test");
        String docName = "sample";
        Documentation documentation = new Documentation(DocumentationType.HOWTO, docName);
        PowerMockito.when(APIUtil.getDocumentation(genericArtifact)).thenReturn(documentation);
        Mockito.when(registry.resourceExists(apiDocPath)).thenReturn(true);
        try {
            abstractAPIManager.getAllDocumentation(identifier, loggedInUser);
            Assert.fail("Registry exception not thrown for error scenario");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Failed to get documentations for api"));
        }

        Resource resource2 = new ResourceImpl();
        resource2.setUUID(SAMPLE_RESOURCE_ID);
        Mockito.when(genericArtifactManager.getGenericArtifact(SAMPLE_RESOURCE_ID)).thenReturn(genericArtifact);

        String documentationName = "doc1";
        Collection documentCollection = new CollectionImpl();
        documentCollection.setChildren(new String[] {
                apiDocPath + documentationName, apiDocPath + APIConstants.INLINE_DOCUMENT_CONTENT_DIR
        });
        Mockito.when(registry.get(apiDocPath)).thenReturn(documentCollection);
        Mockito.when(registry.get(apiDocPath + documentationName)).thenReturn(resource2);
        PowerMockito.when(APIUtil.getDocumentation(genericArtifact,loggedInUser)).thenReturn(documentation);
        List<Documentation> documentationList = abstractAPIManager.getAllDocumentation(identifier,loggedInUser);
        Assert.assertNotNull(documentationList);
        Assert.assertEquals(documentationList.size(), 1);
        String contentPath = APIConstants.API_LOCATION + RegistryConstants.PATH_SEPARATOR + identifier.getProviderName()
                + RegistryConstants.PATH_SEPARATOR + identifier.getApiName() + RegistryConstants.PATH_SEPARATOR
                + identifier.getVersion() + RegistryConstants.PATH_SEPARATOR + APIConstants.DOC_DIR
                + RegistryConstants.PATH_SEPARATOR + RegistryConstants.PATH_SEPARATOR + documentationName;
        genericArtifact.setAttribute(APIConstants.DOC_SOURCE_TYPE, "In line");
        genericArtifact.setAttribute(APIConstants.DOC_NAME, documentationName);
        ResourceDO resourceDO = new ResourceDO();
        resourceDO.setLastUpdatedOn(12344567);
        Resource resource3 = new ResourceImpl(contentPath, resourceDO);
        Mockito.when(registry.get(contentPath)).thenReturn(resource3);
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
    public void testGetDocumentationFromId() throws Exception {

        String  docName = "TestDoc";
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(apiPersistenceInstance);

        org.wso2.carbon.apimgt.persistence.dto.Documentation document = Mockito
                .mock(org.wso2.carbon.apimgt.persistence.dto.Documentation.class);
        Mockito.when(document.getName()).thenReturn(docName);
        Organization org = Mockito.mock(Organization.class);
        PowerMockito.whenNew(Organization.class).withArguments(SAMPLE_TENANT_DOMAIN, null).thenReturn(org);
        
        Mockito.when(apiPersistenceInstance.getDocumentation(org, SAMPLE_API_RESOURCE_ID, SAMPLE_RESOURCE_ID))
                .thenReturn(document);

        Assert.assertNotNull(
                abstractAPIManager.getDocumentation(SAMPLE_API_RESOURCE_ID, SAMPLE_RESOURCE_ID, SAMPLE_TENANT_DOMAIN));
        abstractAPIManager.tenantDomain = SAMPLE_TENANT_DOMAIN;
        Documentation doc = abstractAPIManager.getDocumentation(SAMPLE_API_RESOURCE_ID, SAMPLE_RESOURCE_ID,
                SAMPLE_TENANT_DOMAIN);
        Assert.assertEquals(doc.getName(), docName);

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
    public void testIsContextExist() throws APIManagementException {
        String context = "/t/sample";
        Mockito.when(apiMgtDAO.isContextExist(Mockito.anyString())).thenReturn( true);
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(apiMgtDAO);
        abstractAPIManager.tenantDomain = SAMPLE_TENANT_DOMAIN_1;
        Assert.assertTrue(abstractAPIManager.isContextExist(context));
    }

    @Test
    public void testIsScopeKeyExist() throws APIManagementException {

        Mockito.when(scopesDAO.isScopeExist(Mockito.anyString(), Mockito.anyInt()))
                .thenReturn(false, true, false);
        Mockito.when(keyManager.isScopeExists(Mockito.anyString())).thenReturn(false, false);
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(scopesDAO);
        Assert.assertFalse(abstractAPIManager.isScopeKeyExist("sample", -1234));
        Assert.assertTrue(abstractAPIManager.isScopeKeyExist("sample1", -1234));
        Assert.assertFalse(abstractAPIManager.isScopeKeyExist("sample2", -1234));
    }

    @Test
    public void testIsScopeKeyAssigned() throws APIManagementException {
        String organization = "carbon.super";
        String uuid = UUID.randomUUID().toString();
        Mockito.when(apiMgtDAO.isScopeKeyAssignedLocally(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(),
                Mockito.anyString())).thenReturn(false, true);
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(apiMgtDAO);
        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.when(APIUtil.getInternalOrganizationId(organization)).thenReturn(-1234);
        Assert.assertFalse(abstractAPIManager.isScopeKeyAssignedLocally(SAMPLE_API_NAME, "sample", "carbon.super"));
        Assert.assertTrue(abstractAPIManager.isScopeKeyAssignedLocally(SAMPLE_API_NAME, "sample1", "carbon.super"));
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
        PowerMockito.mockStatic(APIUtil.class);
        SortedMap<String, String> claimValues = new TreeMap<String, String>();
        claimValues.put("admin@wso2.om", APIConstants.EMAIL_CLAIM);
        PowerMockito.when(APIUtil.getClaims(API_PROVIDER, tenantId, DEFAULT_DIALECT_URI)).thenReturn(claimValues);
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
        PowerMockito.when(APIUtil.isEnabledUnlimitedTier()).thenReturn(true, false);
        Mockito.doNothing().when(apiMgtDAO).addSubscriber((Subscriber) Mockito.any(), Mockito.anyString());
        abstractAPIManager.addSubscriber(API_PROVIDER, SAMPLE_RESOURCE_ID);
        List<Tier> tierValues = new ArrayList<Tier>();
        tierValues.add(new Tier("Gold"));
        tierValues.add(new Tier("Silver"));
        Map<String, Tier> tierMap = new HashMap<String, Tier>();
        tierMap.put("Gold", new Tier("Gold"));
        tierMap.put("Silver", new Tier("Silver"));
        PowerMockito.when(APIUtil.getTiers(Mockito.anyInt(), Mockito.anyString())).thenReturn(tierMap);
        PowerMockito.when(APIUtil.sortTiers(Mockito.anySet())).thenReturn(tierValues);
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
    public void testGetAPIByAccessToken() throws APIManagementException {
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(apiMgtDAO);
        Assert.assertEquals(abstractAPIManager.getAPIByAccessToken(SAMPLE_RESOURCE_ID).size(), 0);
    }

    @Test
    public void testGetAllTiers() throws APIManagementException {
        Map<String, Tier> tierMap = new HashMap<String, Tier>();
        Tier tier1 = new Tier("tier1");
        Tier tier2 = new Tier("tier2");
        tierMap.put("Gold", tier1);
        tierMap.put("Silver", tier2);
        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.when(APIUtil.getAllTiers()).thenReturn(tierMap);
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(null, null, null, null);
        abstractAPIManager.tenantId = -1;
        Assert.assertEquals(abstractAPIManager.getAllTiers().size(), 2);
        abstractAPIManager.tenantId = -1234;
        abstractAPIManager.tenantDomain = SAMPLE_TENANT_DOMAIN_1;
        PowerMockito.when(APIUtil.getAllTiers(Mockito.anyInt())).thenReturn(tierMap);
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
        PowerMockito.when(APIUtil.getAllTiers()).thenReturn(tierMap);
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(null, null, null, null);
        Assert.assertEquals(abstractAPIManager.getAllTiers(SAMPLE_TENANT_DOMAIN_1).size(), 2);
        PowerMockito.when(APIUtil.getAllTiers(Mockito.anyInt())).thenReturn(tierMap);
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
        PowerMockito.when(APIUtil.getTiers()).thenReturn(tierMap1);
        PowerMockito.when(APIUtil.getTiers(Mockito.anyInt())).thenReturn(tierMap2);
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(null, null, null, null);
        abstractAPIManager.tenantId = -1;
        PowerMockito.when(APIUtil.getTiersFromPolicies(Mockito.anyString(), Mockito.anyInt())).thenReturn(tierMap1);

        Assert.assertEquals(abstractAPIManager.getTiers().size(), 1);
        abstractAPIManager.tenantId = -1234;
        PowerMockito.when(APIUtil.getTiersFromPolicies(Mockito.anyString(), Mockito.anyInt())).thenReturn(tierMap2);

        Assert.assertEquals(abstractAPIManager.getTiers().size(), 2);
        PowerMockito.when(APIUtil.getTiersFromPolicies(Mockito.anyString(), Mockito.anyInt())).thenReturn(tierMap3);
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
        PowerMockito.when(APIUtil.getTiers()).thenReturn(tierMap1);
        PowerMockito.when(APIUtil.getTiers(Mockito.anyInt())).thenReturn(tierMap2);
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(null, null, null, null);
        PowerMockito.when(APIUtil.getTiersFromPolicies(Mockito.anyString(), Mockito.anyInt())).thenReturn(tierMap1);
        Assert.assertEquals(abstractAPIManager.getTiers(SAMPLE_TENANT_DOMAIN_1).size(), 1);
        Assert.assertEquals(abstractAPIManager.getTiers(SAMPLE_TENANT_DOMAIN_1).size(), 1); //verify next branch of if
        // condition
        PowerMockito.when(APIUtil.getTiersFromPolicies(Mockito.anyString(), Mockito.anyInt())).thenReturn(tierMap2);
        Assert.assertEquals(abstractAPIManager.getTiers(SAMPLE_TENANT_DOMAIN_1).size(), 2);
        PowerMockito.when(APIUtil.getTiersFromPolicies(Mockito.anyString(), Mockito.anyInt())).thenReturn(tierMap3);
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
        PowerMockito.when(APIUtil.getTiers(Mockito.anyInt(), Mockito.anyString())).thenReturn(tierMap1);
        PowerMockito.when(APIUtil.getTenantId(Mockito.anyString())).thenReturn(-1234);
        PowerMockito.when(APIUtil.getTiersFromPolicies(Mockito.anyString(), Mockito.anyInt()))
                .thenReturn(tierMap1, tierMap2, tierMap3);
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(null, null, null, null);
        PowerMockito.when(APIUtil.getTiersFromPolicies(Mockito.anyString(), Mockito.anyInt())).thenReturn(tierMap1);
        Assert.assertEquals(abstractAPIManager.getTiers(0, API_PROVIDER).size(), 1);
        Assert.assertEquals(abstractAPIManager.getTiers(0, API_PROVIDER).size(), 1); //verify next branch of if
        // condition
        PowerMockito.when(APIUtil.getTiersFromPolicies(Mockito.anyString(), Mockito.anyInt())).thenReturn(tierMap2);
        Assert.assertEquals(abstractAPIManager.getTiers(1, API_PROVIDER).size(), 2);
        PowerMockito.when(APIUtil.getTiersFromPolicies(Mockito.anyString(), Mockito.anyInt())).thenReturn(tierMap3);
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
        PowerMockito.when(APIUtil.getDomainMappings(Mockito.anyString(), Mockito.anyString())).thenReturn(domainMappings);
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(null, null, null, null);
        Assert.assertEquals(abstractAPIManager.getTenantDomainMappings(SAMPLE_TENANT_DOMAIN, "api").size(), 2);
    }

    @Test
    public void testIsDuplicateContextTemplateMatchingOrganization() throws APIManagementException {
        Mockito.when(apiMgtDAO.isDuplicateContextTemplateMatchesOrganization(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(true, false, true, false);
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(apiMgtDAO);
        abstractAPIManager.tenantDomain = SAMPLE_TENANT_DOMAIN_1;
        Assert.assertTrue(abstractAPIManager
                .isDuplicateContextTemplateMatchingOrganization("/t/context", SAMPLE_TENANT_DOMAIN_1));
        Assert.assertFalse(
                abstractAPIManager.isDuplicateContextTemplateMatchingOrganization("context", SAMPLE_TENANT_DOMAIN_1));
        abstractAPIManager.tenantDomain = SAMPLE_TENANT_DOMAIN;
        Assert.assertTrue(
                abstractAPIManager.isDuplicateContextTemplateMatchingOrganization("/t/context", SAMPLE_TENANT_DOMAIN));
        Assert.assertFalse(
                abstractAPIManager.isDuplicateContextTemplateMatchingOrganization("context", SAMPLE_TENANT_DOMAIN));
        abstractAPIManager.tenantDomain = null;
        Assert.assertFalse(abstractAPIManager.isDuplicateContextTemplateMatchingOrganization(null, null));
        Assert.assertFalse(abstractAPIManager.isDuplicateContextTemplateMatchingOrganization("context", null));
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
    public void testGetPolicies() throws APIManagementException, org.wso2.carbon.user.api.UserStoreException,
            RegistryException, XMLStreamException {
        APIPolicy[] policies1 = { new APIPolicy("policy1") };
        ApplicationPolicy[] policies2 = { new ApplicationPolicy("policy2"), new ApplicationPolicy("policy3") };
        SubscriptionPolicy[] policies3 = { new SubscriptionPolicy("policy4"), new SubscriptionPolicy("policy5"),
                new SubscriptionPolicy("policy6") };
        GlobalPolicy[] policies4 = { new GlobalPolicy("policy7"), new GlobalPolicy("policy8"),
                new GlobalPolicy("policy9"), new GlobalPolicy("policy0") };
        PowerMockito.mockStatic(APIUtil.class);
        BDDMockito.when(APIUtil.getTenantId(Mockito.anyString())).thenReturn(-1234);
        PowerMockito.when(APIUtil.replaceSystemProperty(Mockito.anyString())).thenAnswer((Answer<String>) invocation -> {
            Object[] args = invocation.getArguments();
            return (String) args[0];
        });
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(apiMgtDAO);
        Mockito.when(apiMgtDAO.getAPIPolicies(Mockito.anyInt())).thenReturn(policies1);
        Mockito.when(apiMgtDAO.getApplicationPolicies(Mockito.anyInt())).thenReturn(policies2);
        Mockito.when(apiMgtDAO.getSubscriptionPolicies(Mockito.anyInt())).thenReturn(policies3);
        Mockito.when(apiMgtDAO.getGlobalPolicies(Mockito.anyInt())).thenReturn(policies4);

        ServiceReferenceHolder sh = mockRegistryAndUserRealm(-1234);
        APIManagerConfigurationService amConfigService = Mockito.mock(APIManagerConfigurationService.class);
        APIManagerConfiguration amConfig = Mockito.mock(APIManagerConfiguration.class);
        ThrottleProperties throttleProperties = Mockito.mock(ThrottleProperties.class, Mockito.RETURNS_MOCKS);

        PowerMockito.when(sh.getAPIManagerConfigurationService()).thenReturn(amConfigService);
        PowerMockito.when(amConfigService.getAPIManagerConfiguration()).thenReturn(amConfig);
        PowerMockito.when(amConfig.getThrottleProperties()).thenReturn(throttleProperties);

        Assert.assertEquals(abstractAPIManager.getPolicies(API_PROVIDER, PolicyConstants.POLICY_LEVEL_API).length, 1);
        Assert.assertEquals(abstractAPIManager.getPolicies(API_PROVIDER, PolicyConstants.POLICY_LEVEL_APP).length, 2);

        PowerMockito.when(throttleProperties.isEnableUnlimitedTier()).thenReturn(false);

        Assert.assertEquals(3, abstractAPIManager.getPolicies(API_PROVIDER, PolicyConstants.POLICY_LEVEL_SUB).length);
        Assert.assertEquals(4, abstractAPIManager.getPolicies(API_PROVIDER,
                PolicyConstants.POLICY_LEVEL_GLOBAL).length);
        Assert.assertEquals(0, abstractAPIManager.getPolicies(API_PROVIDER, "Test").length);
    }
    @Test
    public void testGetPoliciesIncludeUnlimitedThrottletier() throws APIManagementException, org.wso2.carbon.user.api.UserStoreException,
            RegistryException, XMLStreamException {
        SubscriptionPolicy[] policies3 = { new SubscriptionPolicy("policy4"), new SubscriptionPolicy("policy5"),
                new SubscriptionPolicy("policy6") };
        PowerMockito.mockStatic(APIUtil.class);
        BDDMockito.when(APIUtil.getTenantId(Mockito.anyString())).thenReturn(-1234);
        PowerMockito.when(APIUtil.replaceSystemProperty(Mockito.anyString())).thenAnswer((Answer<String>) invocation -> {
            Object[] args = invocation.getArguments();
            return (String) args[0];
        });
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(apiMgtDAO);
        Mockito.when(apiMgtDAO.getSubscriptionPolicies(Mockito.anyInt())).thenReturn(policies3);

        ServiceReferenceHolder sh = mockRegistryAndUserRealm(-1234);
        APIManagerConfigurationService amConfigService = Mockito.mock(APIManagerConfigurationService.class);
        APIManagerConfiguration amConfig = Mockito.mock(APIManagerConfiguration.class);
        ThrottleProperties throttleProperties = Mockito.mock(ThrottleProperties.class, Mockito.RETURNS_MOCKS);

        PowerMockito.when(sh.getAPIManagerConfigurationService()).thenReturn(amConfigService);
        PowerMockito.when(amConfigService.getAPIManagerConfiguration()).thenReturn(amConfig);
        PowerMockito.when(amConfig.getThrottleProperties()).thenReturn(throttleProperties);
        PowerMockito.when(throttleProperties.isEnableUnlimitedTier()).thenReturn(true);
        Assert.assertEquals(3, abstractAPIManager.getPolicies(API_PROVIDER, PolicyConstants.POLICY_LEVEL_SUB).length);
    }

    @Test
    public void testGetPoliciesExcludingUnAuthenticatedTier() throws APIManagementException, org.wso2.carbon.user.api.UserStoreException,
            RegistryException, XMLStreamException {
        SubscriptionPolicy[] policies3 = { new SubscriptionPolicy("policy4"), new SubscriptionPolicy("policy5"),
                new SubscriptionPolicy("policy6"), new SubscriptionPolicy(APIConstants.UNAUTHENTICATED_TIER),
                new SubscriptionPolicy(APIConstants.UNLIMITED_TIER)};
        PowerMockito.mockStatic(APIUtil.class);
        BDDMockito.when(APIUtil.getTenantId(Mockito.anyString())).thenReturn(-1234);
        PowerMockito.when(APIUtil.replaceSystemProperty(Mockito.anyString())).thenAnswer((Answer<String>) invocation -> {
            Object[] args = invocation.getArguments();
            return (String) args[0];
        });
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(apiMgtDAO);
        Mockito.when(apiMgtDAO.getSubscriptionPolicies(Mockito.anyInt())).thenReturn(policies3);

        ServiceReferenceHolder sh = mockRegistryAndUserRealm(-1234);
        APIManagerConfigurationService amConfigService = Mockito.mock(APIManagerConfigurationService.class);
        APIManagerConfiguration amConfig = Mockito.mock(APIManagerConfiguration.class);
        ThrottleProperties throttleProperties = Mockito.mock(ThrottleProperties.class);

        PowerMockito.when(sh.getAPIManagerConfigurationService()).thenReturn(amConfigService);
        PowerMockito.when(amConfigService.getAPIManagerConfiguration()).thenReturn(amConfig);
        PowerMockito.when(amConfig.getThrottleProperties()).thenReturn(throttleProperties);

        Mockito.when(throttleProperties.isEnableUnlimitedTier()).thenReturn(false);

        Assert.assertEquals(3, abstractAPIManager.getPolicies(API_PROVIDER, PolicyConstants.POLICY_LEVEL_SUB).length);
    }

    @Test
    public void testGetPoliciesIncludingUnlimitedTier() throws APIManagementException,
            org.wso2.carbon.user.api.UserStoreException, RegistryException, XMLStreamException {
        SubscriptionPolicy[] policies3 = { new SubscriptionPolicy("policy4"), new SubscriptionPolicy("policy5"),
                new SubscriptionPolicy("policy6"), new SubscriptionPolicy(APIConstants.UNLIMITED_TIER)};
        PowerMockito.mockStatic(APIUtil.class);
        BDDMockito.when(APIUtil.getTenantId(Mockito.anyString())).thenReturn(-1234);
        PowerMockito.when(APIUtil.replaceSystemProperty(Mockito.anyString())).thenAnswer((Answer<String>) invocation -> {
            Object[] args = invocation.getArguments();
            return (String) args[0];
        });
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(apiMgtDAO);
        Mockito.when(apiMgtDAO.getSubscriptionPolicies(Mockito.anyInt())).thenReturn(policies3);

        ServiceReferenceHolder sh = mockRegistryAndUserRealm(-1234);
        APIManagerConfigurationService amConfigService = Mockito.mock(APIManagerConfigurationService.class);
        APIManagerConfiguration amConfig = Mockito.mock(APIManagerConfiguration.class);
        ThrottleProperties throttleProperties = Mockito.mock(ThrottleProperties.class);

        PowerMockito.when(sh.getAPIManagerConfigurationService()).thenReturn(amConfigService);
        PowerMockito.when(amConfigService.getAPIManagerConfiguration()).thenReturn(amConfig);
        PowerMockito.when(amConfig.getThrottleProperties()).thenReturn(throttleProperties);

        Mockito.when(throttleProperties.isEnableUnlimitedTier()).thenReturn(true);

        Assert.assertEquals(4, abstractAPIManager.getPolicies(API_PROVIDER, PolicyConstants.POLICY_LEVEL_SUB).length);
    }
    @Test
    public void testSearchPaginatedAPIs()
            throws APIManagementException, org.wso2.carbon.user.api.UserStoreException, RegistryException,
            XMLStreamException {
        Map<String, Object> subContextResult = new HashMap<String, Object>();
        subContextResult.put("1", new Object());
        UserRegistry registry = Mockito.mock(UserRegistry.class);
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapperExtended(null, registryService, registry,
                tenantManager);
        Mockito.when(tenantManager.getTenantId(Mockito.anyString())).thenReturn(-1234);
        Mockito.when(registryService.getGovernanceUserRegistry(Mockito.anyString(), Mockito.anyInt()))
                .thenThrow(RegistryException.class).thenReturn(registry);

        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.when(APIUtil.replaceSystemProperty(Mockito.anyString())).thenAnswer((Answer<String>) invocation -> {
            Object[] args = invocation.getArguments();
            return (String) args[0];
        });

        try {
            abstractAPIManager.searchPaginatedAPIs("search", API_PROVIDER, 0, 5, false);
            Assert.fail("Exception not thrown for error scenario");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Failed to Search APIs"));
        }
        API api = new API(getAPIIdentifier(SAMPLE_API_NAME, API_PROVIDER, SAMPLE_API_VERSION));
        Documentation documentation = new Documentation(DocumentationType.HOWTO, "DOC1");
        Map<Documentation, API> documentationAPIMap = new HashMap<>();
        BDDMockito.when(APIUtil
                .searchAPIsByDoc(Mockito.any(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyString(),
                        Mockito.anyString())).thenReturn(documentationAPIMap);
        Assert.assertEquals(
                abstractAPIManager.searchPaginatedAPIs("doc=search", SAMPLE_TENANT_DOMAIN_1, 0, 5, false).get("length"),
                0);
        documentationAPIMap.put(documentation, api);
        Assert.assertEquals(abstractAPIManager.searchPaginatedAPIs("doc=search", null, 0, 5, false).get("length"), 5);

        // Test related with searches with custom properties
        Map<String, Object> actualAPIs = abstractAPIManager
                .searchPaginatedAPIs("secured=*true*", SAMPLE_TENANT_DOMAIN_1, 0, 5, false);
        List<API> retrievedAPIs = (List<API>) actualAPIs.get("apis");
        Assert.assertEquals("Searching with additional property failed", 1, actualAPIs.get("length"));
        Assert.assertNotNull("Search with additional property failed", retrievedAPIs);
        Assert.assertEquals("Search with additional property failed", 1, retrievedAPIs.size());
        Assert.assertEquals("Search with additional property failed", "sxy", retrievedAPIs.get(0).getId().getApiName());

        actualAPIs = abstractAPIManager
                .searchPaginatedAPIs("name=*test*&secured=*true*", SAMPLE_TENANT_DOMAIN_1, 0, 5, false);
        retrievedAPIs = (List<API>) actualAPIs.get("apis");
        Assert.assertEquals("Searching with additional property failed", 1, actualAPIs.get("length"));
        Assert.assertNotNull("Search with additional property failed", retrievedAPIs);
        Assert.assertEquals("Search with additional property failed", 1, retrievedAPIs.size());
        Assert.assertEquals("Search with additional property failed", "sxy12",
                retrievedAPIs.get(0).getId().getApiName());

        TestUtils.mockAPIMConfiguration(APIConstants.API_STORE_APIS_PER_PAGE, null, -1234);
        Assert.assertEquals(abstractAPIManager.searchPaginatedAPIs("search", null, 0, 5, false).get("length"), 0);
        TestUtils.mockAPIMConfiguration(APIConstants.API_STORE_APIS_PER_PAGE, "5", -1234);
        GovernanceArtifact governanceArtifact = getGenericArtifact(SAMPLE_API_NAME, API_PROVIDER, SAMPLE_API_VERSION,
                "qname");
        List<GovernanceArtifact> governanceArtifactList = new ArrayList<GovernanceArtifact>();
        governanceArtifactList.add(governanceArtifact);
        Assert.assertEquals(abstractAPIManager.searchPaginatedAPIs("search", null, 0, 5, false).get("length"), 0);
        Assert.assertEquals(
                abstractAPIManager.searchPaginatedAPIs(APIConstants.API_OVERVIEW_PROVIDER, null, 0, 5, false)
                        .get("length"), 0);
        BDDMockito.when(GovernanceUtils
                .findGovernanceArtifacts(Mockito.anyString(), Mockito.any(Registry.class), Mockito.anyString(),
                        Mockito.anyBoolean())).thenThrow(RegistryException.class).thenReturn(governanceArtifactList);
        try {
            abstractAPIManager.searchPaginatedAPIs(APIConstants.API_OVERVIEW_PROVIDER, null, 0, 5, false);
            Assert.fail("APIM exception not thrown for error scenario");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Failed to Search APIs"));
        }
        API api1 = new API(getAPIIdentifier("api1", API_PROVIDER, "v1"));
        BDDMockito.when(APIUtil.getAPI((GovernanceArtifact) Mockito.any(), (Registry) Mockito.any())).thenReturn(api1);
        BDDMockito.when(APIUtil.getAPIIdentifierFromUUID((String) Mockito.any())).thenReturn(getAPIIdentifier("api1", API_PROVIDER, "v1"));
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
    public void testGetCreatedResourceUuid() throws RegistryException, APIManagementException {
        Resource resource = new ResourceImpl();
        resource.setUUID(SAMPLE_RESOURCE_ID);
        Mockito.when(registry.get(Mockito.anyString())).thenThrow(RegistryException.class).thenReturn(resource);
        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(registry);
        Assert.assertNull(abstractAPIManager.getCreatedResourceUuid("/test/path"));
        Assert.assertEquals(abstractAPIManager.getCreatedResourceUuid("/test/path"), SAMPLE_RESOURCE_ID);
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

    private APIProductIdentifier getAPIProductIdentifier(String apiProductName, String provider, String version) {
        return new APIProductIdentifier(provider, apiProductName, version);
    }


    @Test
    public void testGetAsyncApiDefinition() throws Exception {
        Organization org = Mockito.mock(Organization.class);
        PowerMockito.whenNew(Organization.class).withArguments(SAMPLE_TENANT_DOMAIN, null).thenReturn(org);

        AbstractAPIManager abstractAPIManager = new AbstractAPIManagerWrapper(apiPersistenceInstance);
        APIIdentifier identifier = getAPIIdentifier(SAMPLE_API_NAME, API_PROVIDER, SAMPLE_API_VERSION);
        identifier.setUuid(SAMPLE_RESOURCE_ID);
        PowerMockito.mockStatic(OASParserUtil.class);
        String asyncDefinition = "Sample Async Definition";
        PowerMockito.when(apiPersistenceInstance.getAsyncDefinition(org ,
                SAMPLE_RESOURCE_ID)).thenReturn(asyncDefinition);

        Assert.assertEquals(abstractAPIManager.getAsyncAPIDefinition(SAMPLE_RESOURCE_ID, SAMPLE_TENANT_DOMAIN), asyncDefinition);
    }
}
