/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.apimgt.persistence;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.wso2.carbon.CarbonConstants.REGISTRY_SYSTEM_USERNAME;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.persistence.utils.PersistenceHelper;
import org.wso2.carbon.apimgt.persistence.utils.RegistryPersistenceUtil;
import org.wso2.carbon.apimgt.persistence.dto.DevPortalAPI;
import org.wso2.carbon.apimgt.persistence.dto.Organization;
import org.wso2.carbon.apimgt.persistence.dto.PublisherAPI;
import org.wso2.carbon.apimgt.persistence.dto.PublisherAPIProduct;
import org.wso2.carbon.apimgt.persistence.dto.UserContext;
import org.wso2.carbon.apimgt.persistence.exceptions.APIPersistenceException;
import org.wso2.carbon.apimgt.persistence.exceptions.AsyncSpecPersistenceException;
import org.wso2.carbon.apimgt.persistence.exceptions.GraphQLPersistenceException;
import org.wso2.carbon.apimgt.persistence.exceptions.OASPersistenceException;
import org.wso2.carbon.apimgt.persistence.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.persistence.mapper.APIMapper;
import org.wso2.carbon.apimgt.persistence.mapper.APIProductMapper;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.Tag;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.indexing.indexer.IndexerException;
import org.wso2.carbon.registry.indexing.solr.SolrClient;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;

import junit.framework.Assert;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ CarbonContext.class, RegistryPersistenceUtil.class, ServiceReferenceHolder.class,
        PrivilegedCarbonContext.class, GovernanceUtils.class, SolrClient.class, RegistryPersistenceImpl.class })
public class RegistryPersistenceImplTestCase {
    private final int SUPER_TENANT_ID = -1234;
    private final String SUPER_TENANT_DOMAIN = "carbon.super";
    private final int TENANT_ID = 1;
    private final String TENANT_DOMAIN = "wso2.com";

    @Before
    public void setupClass() throws UserStoreException, IndexerException, APIManagementException {
        System.setProperty("carbon.home", "");
        ServiceReferenceHolder serviceRefHolder = Mockito.mock(ServiceReferenceHolder.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceRefHolder);
        RealmService realmService = Mockito.mock(RealmService.class);
        PowerMockito.when(serviceRefHolder.getRealmService()).thenReturn(realmService);

        TenantManager tenantManager = Mockito.mock(TenantManager.class);
        PowerMockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        PowerMockito.when(tenantManager.getTenantId(SUPER_TENANT_DOMAIN)).thenReturn(SUPER_TENANT_ID);

        PowerMockito.mockStatic(CarbonContext.class);
        CarbonContext context = Mockito.mock(CarbonContext.class);
        PowerMockito.when(CarbonContext.getThreadLocalCarbonContext()).thenReturn(context);

        SolrClient solrClient = Mockito.mock(SolrClient.class);
        PowerMockito.mockStatic(SolrClient.class);
        PowerMockito.when(SolrClient.getInstance()).thenReturn(solrClient);

        PowerMockito.mockStatic(GovernanceUtils.class);
        GovernanceArtifact artifact = Mockito.mock(GovernanceArtifact.class);
        List<GovernanceArtifact> artifacts = new ArrayList<GovernanceArtifact>();
        artifacts.add(artifact);

        PowerMockito.mockStatic(RegistryPersistenceImpl.class);
        PowerMockito.when(RegistryPersistenceImpl.searchDevportalAPIs(anyString(), anyInt(),
                        any(), anyInt(), anyInt()))
                .thenReturn(artifacts);
    }

    @Test
    public void testRegistrySelectionForSuperTenantUser() throws Exception {

        RegistryService registryService = Mockito.mock(RegistryService.class);
        TenantManager tenantManager = Mockito.mock(TenantManager.class);
        Mockito.when(tenantManager.getTenantId(SUPER_TENANT_DOMAIN)).thenReturn(SUPER_TENANT_ID);

        PowerMockito.mockStatic(CarbonContext.class);
        CarbonContext context = Mockito.mock(CarbonContext.class);
        PowerMockito.when(CarbonContext.getThreadLocalCarbonContext()).thenReturn(context);

        PowerMockito.mockStatic(PrivilegedCarbonContext.class);
        PrivilegedCarbonContext privilegedContext = Mockito.mock(PrivilegedCarbonContext.class);
        PowerMockito.when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(privilegedContext);

        PowerMockito.mockStatic(RegistryPersistenceUtil.class);

        ServiceReferenceHolder serviceRefHolder = Mockito.mock(ServiceReferenceHolder.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceRefHolder);
        RealmService realmService = Mockito.mock(RealmService.class);
        PowerMockito.when(serviceRefHolder.getRealmService()).thenReturn(realmService);
        PowerMockito.when(serviceRefHolder.getRegistryService()).thenReturn(registryService);
        UserRegistry userRegistry = Mockito.mock(UserRegistry.class);
        PowerMockito.when(registryService.getRegistry(REGISTRY_SYSTEM_USERNAME, SUPER_TENANT_ID)).thenReturn(userRegistry);

        UserRealm realm = Mockito.mock(UserRealm.class);
        PowerMockito.when(realmService.getTenantUserRealm(SUPER_TENANT_ID)).thenReturn(realm);
        PowerMockito.doNothing().when(RegistryPersistenceUtil.class, "loadloadTenantAPIRXT", Mockito.any(String.class),
                Mockito.any(Integer.class));

        Mockito.when(context.getTenantDomain()).thenReturn(SUPER_TENANT_DOMAIN);
        Mockito.when(context.getTenantId()).thenReturn(SUPER_TENANT_ID);

        APIPersistence apiPersistenceInstance = new RegistryPersistenceImplWrapper(tenantManager,
                registryService);
        // return null artifact because we are not testing artifact related params. this is only to get the registry obj
        GenericArtifactManager artifactManager = Mockito.mock(GenericArtifactManager.class);
        PowerMockito.when(
                RegistryPersistenceUtil.getArtifactManager(Mockito.any(Registry.class), Mockito.any(String.class)))
                .thenReturn(artifactManager);
        Mockito.when(artifactManager.getGenericArtifact(Mockito.any(String.class))).thenReturn(null);

        // trigger registry object creation
        UserContext ctx = new UserContext("user", new Organization(SUPER_TENANT_DOMAIN), null, null);
        apiPersistenceInstance.searchAPIsForDevPortal(new Organization(SUPER_TENANT_DOMAIN), "", 0, 10, ctx );
        Mockito.verify(registryService, times(1)).getGovernanceUserRegistry("user", SUPER_TENANT_ID);

        apiPersistenceInstance = new RegistryPersistenceImplWrapper(tenantManager,
                registryService);
        // trigger registry object creation
        ctx = new UserContext("wso2.anonymous.user", new Organization(SUPER_TENANT_DOMAIN), null, null);
        apiPersistenceInstance.searchAPIsForDevPortal(new Organization(SUPER_TENANT_DOMAIN), "", 0, 10, ctx );
        Mockito.verify(registryService, times(1)).getGovernanceUserRegistry("wso2.anonymous.user", SUPER_TENANT_ID);

    }
    @Test
    public void testRegistrySelectionForTenantUser() throws Exception {

        RegistryService registryService = Mockito.mock(RegistryService.class);
        TenantManager tenantManager = Mockito.mock(TenantManager.class);
        Mockito.when(tenantManager.getTenantId(TENANT_DOMAIN)).thenReturn(TENANT_ID);

        PowerMockito.mockStatic(CarbonContext.class);
        CarbonContext context = Mockito.mock(CarbonContext.class);
        PowerMockito.when(CarbonContext.getThreadLocalCarbonContext()).thenReturn(context);

        PowerMockito.mockStatic(PrivilegedCarbonContext.class);
        PrivilegedCarbonContext privilegedContext = Mockito.mock(PrivilegedCarbonContext.class);
        PowerMockito.when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(privilegedContext);

        PowerMockito.mockStatic(RegistryPersistenceUtil.class);

        ServiceReferenceHolder serviceRefHolder = Mockito.mock(ServiceReferenceHolder.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceRefHolder);
        RealmService realmService = Mockito.mock(RealmService.class);
        PowerMockito.when(serviceRefHolder.getRealmService()).thenReturn(realmService);
        PowerMockito.when(serviceRefHolder.getRegistryService()).thenReturn(registryService);
        UserRegistry userRegistry = Mockito.mock(UserRegistry.class);
        PowerMockito.when(registryService.getRegistry(REGISTRY_SYSTEM_USERNAME, TENANT_ID)).thenReturn(userRegistry);

        UserRealm realm = Mockito.mock(UserRealm.class);
        PowerMockito.when(realmService.getTenantUserRealm(TENANT_ID)).thenReturn(realm);
        PowerMockito.doNothing().when(RegistryPersistenceUtil.class, "loadloadTenantAPIRXT", Mockito.any(String.class),
                Mockito.any(Integer.class));

        Mockito.when(context.getTenantDomain()).thenReturn(TENANT_DOMAIN);
        Mockito.when(context.getTenantId()).thenReturn(TENANT_ID);

        // return null artifact because we are not testing artifact related params. this is only to get the registry obj
        GenericArtifactManager artifactManager = Mockito.mock(GenericArtifactManager.class);
        PowerMockito.when(
                RegistryPersistenceUtil.getArtifactManager(Mockito.any(Registry.class), Mockito.any(String.class)))
                .thenReturn(artifactManager);
        Mockito.when(artifactManager.getGenericArtifact(Mockito.any(String.class))).thenReturn(null);

        APIPersistence apiPersistenceInstance = new RegistryPersistenceImplWrapper(tenantManager, registryService);


        // trigger registry object creation
        UserContext ctx = new UserContext("user", new Organization(TENANT_DOMAIN), null, null);
        apiPersistenceInstance.searchAPIsForDevPortal(new Organization(TENANT_DOMAIN), "", 0, 10, ctx );

        Mockito.verify(registryService, times(1)).getGovernanceUserRegistry("user", TENANT_ID);

        ctx = new UserContext("wso2.anonymous.user", new Organization(TENANT_DOMAIN), null, null);
        apiPersistenceInstance.searchAPIsForDevPortal(new Organization(TENANT_DOMAIN), "", 0, 10, ctx );
        Mockito.verify(registryService, times(1)).getGovernanceUserRegistry("wso2.anonymous.user", TENANT_ID);


    }

    @Test
    public void testRegistrySelectionForTenantUserCrossTenatAccess() throws Exception {

        RegistryService registryService = Mockito.mock(RegistryService.class);
        TenantManager tenantManager = Mockito.mock(TenantManager.class);
        Mockito.when(tenantManager.getTenantId(TENANT_DOMAIN)).thenReturn(TENANT_ID);
        Mockito.when(tenantManager.getTenantId(SUPER_TENANT_DOMAIN)).thenReturn(SUPER_TENANT_ID);

        PowerMockito.mockStatic(CarbonContext.class);
        CarbonContext context = Mockito.mock(CarbonContext.class);
        PowerMockito.when(CarbonContext.getThreadLocalCarbonContext()).thenReturn(context);

        PowerMockito.mockStatic(PrivilegedCarbonContext.class);
        PrivilegedCarbonContext privilegedContext = Mockito.mock(PrivilegedCarbonContext.class);
        PowerMockito.when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(privilegedContext);

        PowerMockito.mockStatic(RegistryPersistenceUtil.class);

        ServiceReferenceHolder serviceRefHolder = Mockito.mock(ServiceReferenceHolder.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceRefHolder);
        RealmService realmService = Mockito.mock(RealmService.class);
        PowerMockito.when(serviceRefHolder.getRealmService()).thenReturn(realmService);

        PowerMockito.when(serviceRefHolder.getRegistryService()).thenReturn(registryService);
        UserRegistry userRegistry = Mockito.mock(UserRegistry.class);
        PowerMockito.when(registryService.getRegistry(REGISTRY_SYSTEM_USERNAME, SUPER_TENANT_ID)).thenReturn(userRegistry);

        UserRealm realm = Mockito.mock(UserRealm.class);
        PowerMockito.when(realmService.getTenantUserRealm(TENANT_ID)).thenReturn(realm);
        PowerMockito.doNothing().when(RegistryPersistenceUtil.class, "loadloadTenantAPIRXT", Mockito.any(String.class),
                Mockito.any(Integer.class));

        Mockito.when(context.getTenantDomain()).thenReturn(TENANT_DOMAIN);
        Mockito.when(context.getTenantId()).thenReturn(TENANT_ID);

        APIPersistence apiPersistenceInstance = new RegistryPersistenceImplWrapper(tenantManager, registryService);

        // return null artifact because we are not testing artifact related params. this is only to get the registry obj
        GenericArtifactManager artifactManager = Mockito.mock(GenericArtifactManager.class);
        PowerMockito.when(
                RegistryPersistenceUtil.getArtifactManager(Mockito.any(Registry.class), Mockito.any(String.class)))
                .thenReturn(artifactManager);
        Mockito.when(artifactManager.getGenericArtifact(Mockito.any(String.class))).thenReturn(null);

        // trigger registry object creation. access super tenant api
        UserContext ctx = new UserContext("user", new Organization(TENANT_DOMAIN), null, null);
        apiPersistenceInstance.searchAPIsForDevPortal(new Organization(SUPER_TENANT_DOMAIN), "", 0, 10, ctx );

        // check whether super tenant's system registy is accessed
        Mockito.verify(registryService, times(1)).getGovernanceSystemRegistry((SUPER_TENANT_ID));

    }

    @Test
    public void testGetPublisherAPI() throws Exception {

        Registry registry = Mockito.mock(UserRegistry.class);
        Resource resource = new ResourceImpl();
        Mockito.when(registry.get(anyString())).thenReturn(resource);
        Tag[] tags = new Tag[1];
        Tag tag = new Tag();
        tag.setTagName("testTag");
        tags[0] = tag;
        Mockito.when(registry.getTags(anyString())).thenReturn(tags);
        GenericArtifact artifact = PersistenceHelper.getSampleAPIArtifact();
        String apiUUID = artifact.getId();
        String apiDescription = artifact.getAttribute(APIConstants.API_OVERVIEW_DESCRIPTION);
        Mockito.when(GovernanceUtils.getArtifactPath(registry, apiUUID)).thenReturn(generateArtifactPath(artifact));

        APIPersistence apiPersistenceInstance = new RegistryPersistenceImplWrapper(registry, artifact);

        Organization org = new Organization(SUPER_TENANT_DOMAIN);
        PublisherAPI publisherAPI = apiPersistenceInstance.getPublisherAPI(org, apiUUID);
        Assert.assertEquals("API UUID does not match", apiUUID, publisherAPI.getId());
        Assert.assertEquals("API Description does not match", apiDescription, publisherAPI.getDescription());
        Assert.assertEquals("API audience does not match", artifact.getAttribute(APIConstants.API_OVERVIEW_AUDIENCE),
                publisherAPI.getAudience());
    }

    @Test
    public void testGetDevPortalAPI() throws Exception {

        Registry registry = Mockito.mock(UserRegistry.class);
        Resource resource = new ResourceImpl();
        Mockito.when(registry.get(anyString())).thenReturn(resource);
        Tag[] tags = new Tag[1];
        Tag tag = new Tag();
        tag.setTagName("testTag");
        tags[0] = tag;
        Mockito.when(registry.getTags(anyString())).thenReturn(tags);

        GenericArtifact artifact = PersistenceHelper.getSampleAPIArtifact();
        String apiUUID = artifact.getId();
        String apiDescription = artifact.getAttribute(APIConstants.API_OVERVIEW_DESCRIPTION);
        Mockito.when(GovernanceUtils.getArtifactPath(registry, apiUUID)).thenReturn(generateArtifactPath(artifact));

        APIPersistence apiPersistenceInstance = new RegistryPersistenceImplWrapper(registry, artifact);
        Organization org = new Organization(SUPER_TENANT_DOMAIN);
        DevPortalAPI devAPI = apiPersistenceInstance.getDevPortalAPI(org, apiUUID);
        Assert.assertEquals("API UUID does not match", apiUUID, devAPI.getId());
        Assert.assertEquals("API Description does not match", apiDescription, devAPI.getDescription());
    }

    @Test
    public void testGetPublisherAPIProduct() throws Exception {

        Registry registry = Mockito.mock(UserRegistry.class);
        Resource resource = new ResourceImpl();
        Mockito.when(registry.get(anyString())).thenReturn(resource);
        Tag[] tags = new Tag[1];
        Tag tag = new Tag();
        tag.setTagName("testTag");
        tags[0] = tag;
        Mockito.when(registry.getTags(anyString())).thenReturn(tags);

        GenericArtifact artifact = PersistenceHelper.getSampleAPIProductArtifact();
        String apiProductId = artifact.getId();
        String apiPath = generateArtifactPath(artifact);
        Mockito.when(GovernanceUtils.getArtifactPath(registry, apiProductId)).thenReturn(apiPath);

        APIPersistence apiPersistenceInstance = new RegistryPersistenceImplWrapper(registry, artifact);
        Organization org = new Organization(SUPER_TENANT_DOMAIN);
        PublisherAPIProduct productAPI = apiPersistenceInstance.getPublisherAPIProduct(org, apiProductId);
        Assert.assertEquals("API Product UUID does not match", apiProductId, productAPI.getId());
    }

    @Test
    public void testThumbnailTasks() throws Exception {

        Registry registry = Mockito.mock(Registry.class);
        GenericArtifact artifact = PersistenceHelper.getSampleAPIArtifact();
        String apiUUID = artifact.getId();

        GenericArtifactManager manager = Mockito.mock(GenericArtifactManager.class);
        PowerMockito.mockStatic(RegistryPersistenceUtil.class);
        PowerMockito.when(RegistryPersistenceUtil.getArtifactManager(registry, APIConstants.API_KEY))
                .thenReturn(manager);
        PowerMockito.when(RegistryPersistenceUtil.extractApiSourcePath(anyString())).thenCallRealMethod();
        PowerMockito.when(RegistryPersistenceUtil.extractProviderFromPath(anyString(), anyString(), anyString()))
                .thenCallRealMethod();
        PowerMockito.when(RegistryPersistenceUtil.getProviderFromArtifact(any(GenericArtifact.class)))
                .thenCallRealMethod();
        PowerMockito.when(RegistryPersistenceUtil.replaceEmailDomain(anyString())).thenCallRealMethod();
        Mockito.when(manager.getGenericArtifact(apiUUID)).thenReturn(artifact);
        Mockito.doNothing().when(manager).updateGenericArtifact(artifact);

        Organization org = new Organization(SUPER_TENANT_DOMAIN);
        APIPersistence apiPersistenceInstance = new RegistryPersistenceImplWrapper(registry, artifact);

        String apiPath = generateArtifactPath(artifact);
        String apiSourcePath = apiPath.substring(0, apiPath.lastIndexOf("/api"));
        String thumbPath = apiSourcePath + RegistryConstants.PATH_SEPARATOR + APIConstants.API_ICON_IMAGE;
        Mockito.when(GovernanceUtils.getArtifactPath(registry, apiUUID)).thenReturn(apiPath);

        Mockito.when(registry.resourceExists(thumbPath)).thenReturn(true);
        Resource imageResource = Mockito.mock(Resource.class);
        Mockito.when(registry.get(thumbPath)).thenReturn(imageResource);

        apiPersistenceInstance.getThumbnail(org, apiUUID);
        Mockito.verify(registry, times(1)).get(thumbPath);

        apiPersistenceInstance.deleteThumbnail(org, apiUUID);
        Mockito.verify(registry, times(1)).delete(thumbPath);
        Mockito.verify(manager, times(1)).updateGenericArtifact(artifact);
    }

    @Test
    public void testGetWSDL() throws Exception {

        Registry registry = Mockito.mock(Registry.class);
        GenericArtifact artifact = PersistenceHelper.getSampleAPIArtifact();
        String apiUUID = artifact.getId();

        Organization org = new Organization(SUPER_TENANT_DOMAIN);
        APIPersistence apiPersistenceInstance = new RegistryPersistenceImplWrapper(registry, artifact);

        String apiProviderName = artifact.getAttribute(APIConstants.API_OVERVIEW_PROVIDER);
        apiProviderName = RegistryPersistenceUtil.replaceEmailDomain(apiProviderName);
        String apiName = artifact.getAttribute(APIConstants.API_OVERVIEW_NAME);
        String apiVersion = artifact.getAttribute(APIConstants.API_OVERVIEW_VERSION);
        String artifactPath = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR + apiProviderName
                + RegistryConstants.PATH_SEPARATOR + apiName + RegistryConstants.PATH_SEPARATOR + apiVersion;
        String apiPath = generateArtifactPath(artifact);
        Mockito.when(GovernanceUtils.getArtifactPath(registry, apiUUID)).thenReturn(apiPath);

        String wsdlResourcePath = artifactPath + RegistryConstants.PATH_SEPARATOR
                + RegistryPersistenceUtil.createWsdlFileName(apiProviderName, apiName, apiVersion);

        Mockito.when(registry.resourceExists(wsdlResourcePath)).thenReturn(true);
        Resource wsdlResource = Mockito.mock(Resource.class);
        Mockito.when(registry.get(wsdlResourcePath)).thenReturn(wsdlResource);

        apiPersistenceInstance.getWSDL(org, apiUUID);
        Mockito.verify(registry, times(1)).get(wsdlResourcePath);

        // WSDL zip test
        String wsdlResourcePathOld = APIConstants.API_WSDL_RESOURCE_LOCATION
                + RegistryPersistenceUtil.createWsdlFileName(apiProviderName, apiName, apiVersion);
        Mockito.when(registry.resourceExists(wsdlResourcePath)).thenReturn(false);
        Mockito.when(registry.resourceExists(wsdlResourcePathOld)).thenReturn(false);
        //zip location
        wsdlResourcePath = artifactPath + RegistryConstants.PATH_SEPARATOR
                + APIConstants.API_WSDL_ARCHIVE_LOCATION + apiProviderName
                + APIConstants.WSDL_PROVIDER_SEPERATOR + apiName + apiVersion + APIConstants.ZIP_FILE_EXTENSION;
        Mockito.when(registry.resourceExists(wsdlResourcePath)).thenReturn(true);
        Mockito.when(registry.get(wsdlResourcePath)).thenReturn(wsdlResource);
        apiPersistenceInstance.getWSDL(org, apiUUID);
        Mockito.verify(registry, times(1)).get(wsdlResourcePath);

    }

    @Test
    public void testGetGraphQLSchema() throws GraphQLPersistenceException, RegistryException {
        Registry registry = Mockito.mock(Registry.class);
        GenericArtifact artifact = PersistenceHelper.getSampleAPIArtifact();
        String apiUUID = artifact.getId();

        String apiProviderName = artifact.getAttribute(APIConstants.API_OVERVIEW_PROVIDER);
        apiProviderName = RegistryPersistenceUtil.replaceEmailDomain(apiProviderName);
        String apiName = artifact.getAttribute(APIConstants.API_OVERVIEW_NAME);
        String apiVersion = artifact.getAttribute(APIConstants.API_OVERVIEW_VERSION);

        String path = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR + apiProviderName
                + RegistryConstants.PATH_SEPARATOR + apiName + RegistryConstants.PATH_SEPARATOR + apiVersion
                + RegistryConstants.PATH_SEPARATOR;
        String schemaName = apiProviderName + APIConstants.GRAPHQL_SCHEMA_PROVIDER_SEPERATOR + apiName
                + apiVersion + APIConstants.GRAPHQL_SCHEMA_FILE_EXTENSION;
        String schemaResourePath = path + schemaName;
        String apiPath = generateArtifactPath(artifact);
        Mockito.when(GovernanceUtils.getArtifactPath(registry, apiUUID)).thenReturn(apiPath);

        String schema = "{\n" +
                        "  hero {\n" +
                        "    name\n" +
                        "    friends {\n" +
                        "      name\n" +
                        "    }\n" +
                        "  }\n" +
                        "}";
        Organization org = new Organization(SUPER_TENANT_DOMAIN);
        APIPersistence apiPersistenceInstance = new RegistryPersistenceImplWrapper(registry, artifact);
        Mockito.when(registry.resourceExists(schemaResourePath)).thenReturn(true);
        Resource oasResource = new ResourceImpl();
        oasResource.setContent(schema.getBytes());
        Mockito.when(registry.get(schemaResourePath)).thenReturn(oasResource);

        String def = apiPersistenceInstance.getGraphQLSchema(org, apiUUID);
        Assert.assertEquals("API graphql schema does not match", schema, def);
    }

    @Test
    public void testGetGraphQLSchema_RevisionArtifact() throws GraphQLPersistenceException, RegistryException {
        Registry registry = Mockito.mock(Registry.class);
        GenericArtifact artifact = PersistenceHelper.getSampleAPIArtifact();
        String apiUUID = artifact.getId();

        String apiProviderName = artifact.getAttribute(APIConstants.API_OVERVIEW_PROVIDER);
        apiProviderName = RegistryPersistenceUtil.replaceEmailDomain(apiProviderName);
        String apiName = artifact.getAttribute(APIConstants.API_OVERVIEW_NAME);
        String apiVersion = artifact.getAttribute(APIConstants.API_OVERVIEW_VERSION);

        // The current (non-revision) artifact path
        String currentApiPath = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR + apiProviderName
                + RegistryConstants.PATH_SEPARATOR + apiName + RegistryConstants.PATH_SEPARATOR + apiVersion
                + RegistryConstants.PATH_SEPARATOR + "api";

        // Set the artifact path to a revision path — simulates loading a revision artifact
        String revisionPath = APIConstants.API_REVISION_LOCATION + RegistryConstants.PATH_SEPARATOR
                + apiUUID + RegistryConstants.PATH_SEPARATOR + "1" + RegistryConstants.PATH_SEPARATOR + "api";
        ((GenericArtifactWrapper) artifact).setArtifactPath(revisionPath);

        // When resolving revision UUID to current path, return the normal provider path
        Mockito.when(GovernanceUtils.getArtifactPath(registry, apiUUID)).thenReturn(currentApiPath);

        // GraphQL schema resource sits under the revision source path
        String revisionSourcePath = revisionPath.substring(0, revisionPath.lastIndexOf("/api"));
        String schemaName = apiProviderName + APIConstants.GRAPHQL_SCHEMA_PROVIDER_SEPERATOR + apiName
                + apiVersion + APIConstants.GRAPHQL_SCHEMA_FILE_EXTENSION;
        String schemaResourcePath = revisionSourcePath + RegistryConstants.PATH_SEPARATOR + schemaName;

        String schema = "{\n"
                + "  hero {\n"
                + "    name\n"
                + "  }\n"
                + "}";
        Organization org = new Organization(SUPER_TENANT_DOMAIN);
        APIPersistence apiPersistenceInstance = new RegistryPersistenceImplWrapper(registry, artifact);
        Mockito.when(registry.resourceExists(schemaResourcePath)).thenReturn(true);
        Resource schemaResource = new ResourceImpl();
        schemaResource.setContent(schema.getBytes());
        Mockito.when(registry.get(schemaResourcePath)).thenReturn(schemaResource);

        String def = apiPersistenceInstance.getGraphQLSchema(org, apiUUID);
        Assert.assertEquals("GraphQL schema should be retrievable from revision artifact", schema, def);
    }

    @Test
    public void testGetOASDefinition() throws OASPersistenceException, RegistryException {
        Registry registry = Mockito.mock(Registry.class);
        GenericArtifact artifact = PersistenceHelper.getSampleAPIArtifact();
        String apiUUID = artifact.getId();

        String apiProviderName = artifact.getAttribute(APIConstants.API_OVERVIEW_PROVIDER);
        apiProviderName = RegistryPersistenceUtil.replaceEmailDomain(apiProviderName);
        String apiName = artifact.getAttribute(APIConstants.API_OVERVIEW_NAME);
        String apiVersion = artifact.getAttribute(APIConstants.API_OVERVIEW_VERSION);
        String definitionPath = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR
                + RegistryPersistenceUtil.replaceEmailDomain(apiProviderName) + RegistryConstants.PATH_SEPARATOR
                + apiName + RegistryConstants.PATH_SEPARATOR + apiVersion + RegistryConstants.PATH_SEPARATOR
                + APIConstants.API_OAS_DEFINITION_RESOURCE_NAME;
        String apiPath = generateArtifactPath(artifact);
        Mockito.when(GovernanceUtils.getArtifactPath(registry, apiUUID)).thenReturn(apiPath);
        String definition = "{\"swagger\":\"2.0\",\"info\":{\"description\":\"This is a sample server Petstore server\"}}";
        Organization org = new Organization(SUPER_TENANT_DOMAIN);
        APIPersistence apiPersistenceInstance = new RegistryPersistenceImplWrapper(registry, artifact);
        Mockito.when(registry.resourceExists(definitionPath)).thenReturn(true);
        Resource oasResource = new ResourceImpl();
        oasResource.setContent(definition.getBytes());
        Mockito.when(registry.get(definitionPath)).thenReturn(oasResource);

        String def = apiPersistenceInstance.getOASDefinition(org, apiUUID);
        Assert.assertEquals("API oas definition does not match", definition, def);

    }

    @Test
    public void testGetAsyncDefinition()
            throws AsyncSpecPersistenceException, RegistryException, APIPersistenceException, APIManagementException {
        Registry registry = Mockito.mock(UserRegistry.class);
        GenericArtifact artifact = PersistenceHelper.getSampleAPIArtifact();
        String apiUUID = artifact.getId();

        GenericArtifactManager manager = Mockito.mock(GenericArtifactManager.class);
        PowerMockito.mockStatic(RegistryPersistenceUtil.class);
        PowerMockito.when(RegistryPersistenceUtil.getArtifactManager(registry, APIConstants.API_KEY))
                .thenReturn(manager);
        PowerMockito.when(RegistryPersistenceUtil.extractApiSourcePath(anyString())).thenCallRealMethod();
        Mockito.when(manager.getGenericArtifact(apiUUID)).thenReturn(artifact);
        Mockito.doNothing().when(manager).updateGenericArtifact(artifact);

        // Derive expected path from artifact.getPath() — same source as production code
        String apiPath = artifact.getPath();
        String apiSourcePath = apiPath.substring(0, apiPath.lastIndexOf("/api"));
        String definitionPath = apiSourcePath + RegistryConstants.PATH_SEPARATOR
                + APIConstants.API_ASYNC_API_DEFINITION_RESOURCE_NAME;

        String definition = "{\"asyncapi\":\"2.0.0\",\"info\":{\"description\":\"This is a sample Async API\"}}";
        Organization org = new Organization(SUPER_TENANT_DOMAIN);
        APIPersistence apiPersistenceInstance = new RegistryPersistenceImplWrapper(registry, artifact);
        Mockito.when(registry.resourceExists(definitionPath)).thenReturn(true);
        Resource asyncResource = new ResourceImpl();
        asyncResource.setContent(definition.getBytes());
        Mockito.when(registry.get(definitionPath)).thenReturn(asyncResource);

        String def = apiPersistenceInstance.getAsyncDefinition(org, apiUUID);
        Assert.assertEquals("API Async definition does not match", definition, def);

    }

    @Test
    public void testAddAPI() throws RegistryException, APIPersistenceException, APIManagementException {
        GenericArtifact artifact = PersistenceHelper.getSampleAPIArtifact();
        PublisherAPI publisherAPI = new PublisherAPI();
        publisherAPI.setApiName(artifact.getAttribute(APIConstants.API_OVERVIEW_NAME));
        publisherAPI.setProviderName(artifact.getAttribute(APIConstants.API_OVERVIEW_PROVIDER));
        publisherAPI.setVersion(artifact.getAttribute(APIConstants.API_OVERVIEW_VERSION));

        API api = APIMapper.INSTANCE.toApi(publisherAPI);

        Registry registry = Mockito.mock(UserRegistry.class);
        Resource resource = new ResourceImpl();
        Mockito.when(registry.get(anyString())).thenReturn(resource);
        Tag[] tags = new Tag[0];
        Mockito.when(registry.getTags(anyString())).thenReturn(tags);

        PowerMockito.mockStatic(RegistryPersistenceUtil.class);
        GenericArtifactManager manager = Mockito.mock(GenericArtifactManager.class);
        PowerMockito.when(RegistryPersistenceUtil.getArtifactManager(registry, APIConstants.API_KEY))
                .thenReturn(manager);
        //add 1 year old timestamp since it is not the latest
        LocalDateTime now = LocalDateTime.now().minusDays(365);
        Timestamp timestamp = Timestamp.valueOf(now);
        PowerMockito.when(RegistryPersistenceUtil.createAPIArtifactContent(any(GenericArtifact.class), any(API.class)))
                .thenReturn(artifact);

        GenericArtifact newArtifact = Mockito.mock(GenericArtifact.class);
        publisherAPI.setVersionTimestamp(timestamp.getTime() + "");
        Mockito.when(manager.newGovernanceArtifact(new QName(api.getId().getApiName()))).thenReturn(newArtifact );

        Organization org = new Organization(SUPER_TENANT_DOMAIN);
        APIPersistence apiPersistenceInstance = new RegistryPersistenceImplWrapper(registry, artifact);
        PublisherAPI returnAPI = apiPersistenceInstance.addAPI(org, publisherAPI);
        Assert.assertEquals(returnAPI.getVersionTimestamp(), String.valueOf(timestamp.getTime()));

    }
    @Test
    public void testUpdateAPI() throws APIPersistenceException, RegistryException, APIManagementException {

        PublisherAPI publisherAPI = new PublisherAPI();
        publisherAPI.setDescription("Modified description");
        API api = APIMapper.INSTANCE.toApi(publisherAPI);

        Registry registry = Mockito.mock(UserRegistry.class);
        Resource resource = new ResourceImpl();
        Mockito.when(registry.get(anyString())).thenReturn(resource);
        Tag[] tags = new Tag[0];
        Mockito.when(registry.getTags(anyString())).thenReturn(tags);
        GenericArtifact existArtifact = PersistenceHelper.getSampleAPIArtifact();
        String apiUUID = existArtifact.getId();

        PowerMockito.mockStatic(RegistryPersistenceUtil.class);
        GenericArtifactManager manager = Mockito.mock(GenericArtifactManager.class);
        PowerMockito.when(RegistryPersistenceUtil.getArtifactManager(registry, APIConstants.API_KEY))
                .thenReturn(manager);
        Mockito.when(manager.getGenericArtifact(apiUUID)).thenReturn(existArtifact);
        Mockito.doNothing().when(manager).updateGenericArtifact(existArtifact);

        PowerMockito.when(RegistryPersistenceUtil.getArtifactManager(registry, APIConstants.API_KEY))
                .thenReturn(manager);

        GenericArtifact updatedArtifact = PersistenceHelper.getSampleAPIArtifact();
        updatedArtifact.setAttribute(APIConstants.API_OVERVIEW_DESCRIPTION, api.getDescription());
        PowerMockito.when(RegistryPersistenceUtil.createAPIArtifactContent(any(GenericArtifact.class), any(API.class)))
                .thenReturn(updatedArtifact);
        String apiPath = generateArtifactPath(updatedArtifact);
        Mockito.when(RegistryPersistenceUtil.getAPIPath(any(APIIdentifier.class))).thenReturn(apiPath);

        Organization org = new Organization(SUPER_TENANT_DOMAIN);
        APIPersistence apiPersistenceInstance = new RegistryPersistenceImplWrapper(registry, existArtifact);

        PublisherAPI updatedAPI = apiPersistenceInstance.updateAPI(org, publisherAPI);
        Assert.assertEquals("Updated API description does not match", "Modified description",
                updatedAPI.getDescription());
    }

    @Test
    public void testAddAPIProduct() throws RegistryException, APIPersistenceException, APIManagementException {
        GenericArtifact artifact = PersistenceHelper.getSampleAPIProductArtifact();
        PublisherAPIProduct publisherAPI = new PublisherAPIProduct();
        publisherAPI.setApiProductName(artifact.getAttribute(APIConstants.API_OVERVIEW_NAME));
        publisherAPI.setProviderName(artifact.getAttribute(APIConstants.API_OVERVIEW_PROVIDER));
        publisherAPI.setVersion(artifact.getAttribute(APIConstants.API_OVERVIEW_VERSION));

        APIProduct api = APIProductMapper.INSTANCE.toApiProduct(publisherAPI);

        Registry registry = Mockito.mock(UserRegistry.class);
        Resource resource = new ResourceImpl();
        Mockito.when(registry.get(anyString())).thenReturn(resource);
        Tag[] tags = new Tag[0];
        Mockito.when(registry.getTags(anyString())).thenReturn(tags);

        PowerMockito.mockStatic(RegistryPersistenceUtil.class);
        GenericArtifactManager manager = Mockito.mock(GenericArtifactManager.class);
        PowerMockito.when(RegistryPersistenceUtil.getArtifactManager(registry, APIConstants.API_KEY))
                .thenReturn(manager);

        PowerMockito.when(RegistryPersistenceUtil.createAPIProductArtifactContent(any(GenericArtifact.class),
                any(APIProduct.class))).thenReturn(artifact);

        GenericArtifact newArtifact = Mockito.mock(GenericArtifact.class);
        Mockito.when(manager.newGovernanceArtifact(new QName(publisherAPI.getApiProductName())))
                .thenReturn(newArtifact);

        Mockito.when(manager.getGenericArtifact(any(String.class))).thenReturn(newArtifact);
        Mockito.doNothing().when(newArtifact).invokeAction("Publish", APIConstants.API_LIFE_CYCLE);

        Organization org = new Organization(SUPER_TENANT_DOMAIN);
        APIPersistence apiPersistenceInstance = new RegistryPersistenceImplWrapper(registry, artifact);
        apiPersistenceInstance.addAPIProduct(org, publisherAPI);
    }

    @Test
    public void testUpdateAPIProduct() throws APIPersistenceException, RegistryException, APIManagementException {

        Registry registry = Mockito.mock(UserRegistry.class);
        Resource resource = new ResourceImpl();
        Mockito.when(registry.get(anyString())).thenReturn(resource);
        Tag[] tags = new Tag[0];
        Mockito.when(registry.getTags(anyString())).thenReturn(tags);
        GenericArtifact existArtifact = PersistenceHelper.getSampleAPIProductArtifact();
        String apiUUID = existArtifact.getId();

        PowerMockito.mockStatic(RegistryPersistenceUtil.class);
        GenericArtifactManager manager = Mockito.mock(GenericArtifactManager.class);
        PowerMockito.when(RegistryPersistenceUtil.getArtifactManager(registry, APIConstants.API_KEY))
                .thenReturn(manager);
        Mockito.when(manager.getGenericArtifact(apiUUID)).thenReturn(existArtifact);
        Mockito.doNothing().when(manager).updateGenericArtifact(existArtifact);

        PowerMockito.when(RegistryPersistenceUtil.getArtifactManager(registry, APIConstants.API_KEY))
                .thenReturn(manager);

        PublisherAPIProduct publisherAPI = new PublisherAPIProduct();
        publisherAPI.setDescription("Modified description");
        publisherAPI.setId(existArtifact.getId());
        APIProduct api = APIProductMapper.INSTANCE.toApiProduct(publisherAPI);

        GenericArtifact updatedArtifact = PersistenceHelper.getSampleAPIProductArtifact();
        updatedArtifact.setAttribute(APIConstants.API_OVERVIEW_DESCRIPTION, api.getDescription());
        PowerMockito.when(RegistryPersistenceUtil.createAPIProductArtifactContent(any(GenericArtifact.class),
                any(APIProduct.class))).thenReturn(updatedArtifact);

        Organization org = new Organization(SUPER_TENANT_DOMAIN);
        APIPersistence apiPersistenceInstance = new RegistryPersistenceImplWrapper(registry, existArtifact);

        PublisherAPIProduct updatedAPI = apiPersistenceInstance.updateAPIProduct(org, publisherAPI);
        Assert.assertEquals("Updated API description does not match", "Modified description",
                updatedAPI.getDescription());
    }

    private String generateArtifactPath(GenericArtifact artifact) throws GovernanceException {
        String apiName = artifact.getAttribute(APIConstants.API_OVERVIEW_NAME);
        String apiVersion = artifact.getAttribute(APIConstants.API_OVERVIEW_VERSION);
        String apiProviderName = artifact.getAttribute(APIConstants.API_OVERVIEW_PROVIDER);
        apiProviderName = RegistryPersistenceUtil.replaceEmailDomain(apiProviderName);
        return APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR + apiProviderName
                + RegistryConstants.PATH_SEPARATOR + apiName + RegistryConstants.PATH_SEPARATOR + apiVersion
                + RegistryConstants.PATH_SEPARATOR + "api";
    }

    private String getSourcePathFromArtifact(GenericArtifact artifact) throws GovernanceException {
        String path = artifact.getPath();
        return path.substring(0, path.lastIndexOf("/api"));
    }

    // =====================================================================
    // Non-provider-change tests for tenant users (provider with -AT-)
    // These ensure existing functionality isn't broken.
    // =====================================================================

    @Test
    public void testGetWSDL_TenantUser() throws Exception {
        Registry registry = Mockito.mock(Registry.class);
        GenericArtifact artifact = PersistenceHelper.getSampleAPIArtifactForTenant();
        String apiUUID = artifact.getId();
        String apiName = artifact.getAttribute(APIConstants.API_OVERVIEW_NAME);
        String apiVersion = artifact.getAttribute(APIConstants.API_OVERVIEW_VERSION);

        Organization org = new Organization(TENANT_DOMAIN);
        APIPersistence apiPersistenceInstance = new RegistryPersistenceImplWrapper(registry, artifact);

        String sourcePath = getSourcePathFromArtifact(artifact);
        String expectedWsdlPath = sourcePath + RegistryConstants.PATH_SEPARATOR
                + RegistryPersistenceUtil.createWsdlFileName("admin-AT-wso2.com", apiName, apiVersion);

        Mockito.when(registry.resourceExists(expectedWsdlPath)).thenReturn(true);
        Resource wsdlResource = Mockito.mock(Resource.class);
        Mockito.when(registry.get(expectedWsdlPath)).thenReturn(wsdlResource);

        apiPersistenceInstance.getWSDL(org, apiUUID);
        Mockito.verify(registry, times(1)).get(expectedWsdlPath);
    }

    @Test
    public void testGetGraphQLSchema_TenantUser() throws Exception {
        Registry registry = Mockito.mock(Registry.class);
        GenericArtifact artifact = PersistenceHelper.getSampleAPIArtifactForTenant();
        String apiUUID = artifact.getId();
        String apiName = artifact.getAttribute(APIConstants.API_OVERVIEW_NAME);
        String apiVersion = artifact.getAttribute(APIConstants.API_OVERVIEW_VERSION);

        Organization org = new Organization(TENANT_DOMAIN);
        APIPersistence apiPersistenceInstance = new RegistryPersistenceImplWrapper(registry, artifact);

        String sourcePath = getSourcePathFromArtifact(artifact);
        String schemaName = "admin-AT-wso2.com" + APIConstants.GRAPHQL_SCHEMA_PROVIDER_SEPERATOR
                + apiName + apiVersion + APIConstants.GRAPHQL_SCHEMA_FILE_EXTENSION;
        String expectedSchemaPath = sourcePath + RegistryConstants.PATH_SEPARATOR + schemaName;

        String schema = "{ hero { name } }";
        Mockito.when(registry.resourceExists(expectedSchemaPath)).thenReturn(true);
        Resource schemaResource = new ResourceImpl();
        schemaResource.setContent(schema.getBytes());
        Mockito.when(registry.get(expectedSchemaPath)).thenReturn(schemaResource);

        String def = apiPersistenceInstance.getGraphQLSchema(org, apiUUID);
        Assert.assertEquals("GraphQL schema should match for tenant user", schema, def);
    }

    @Test
    public void testGetThumbnail_TenantUser() throws Exception {
        Registry registry = Mockito.mock(Registry.class);
        GenericArtifact artifact = PersistenceHelper.getSampleAPIArtifactForTenant();
        String apiUUID = artifact.getId();

        PowerMockito.mockStatic(RegistryPersistenceUtil.class);
        GenericArtifactManager manager = Mockito.mock(GenericArtifactManager.class);
        PowerMockito.when(RegistryPersistenceUtil.getArtifactManager(registry, APIConstants.API_KEY))
                .thenReturn(manager);
        PowerMockito.when(RegistryPersistenceUtil.extractApiSourcePath(anyString())).thenCallRealMethod();
        PowerMockito.when(RegistryPersistenceUtil.extractProviderFromPath(anyString(), anyString(), anyString()))
                .thenCallRealMethod();
        PowerMockito.when(RegistryPersistenceUtil.getProviderFromArtifact(any(GenericArtifact.class)))
                .thenCallRealMethod();
        PowerMockito.when(RegistryPersistenceUtil.replaceEmailDomain(anyString())).thenCallRealMethod();
        Mockito.when(manager.getGenericArtifact(apiUUID)).thenReturn(artifact);

        Organization org = new Organization(TENANT_DOMAIN);
        APIPersistence apiPersistenceInstance = new RegistryPersistenceImplWrapper(registry, artifact);

        String sourcePath = getSourcePathFromArtifact(artifact);
        String expectedThumbPath = sourcePath + RegistryConstants.PATH_SEPARATOR + APIConstants.API_ICON_IMAGE;

        Mockito.when(registry.resourceExists(expectedThumbPath)).thenReturn(true);
        Resource imageResource = Mockito.mock(Resource.class);
        Mockito.when(registry.get(expectedThumbPath)).thenReturn(imageResource);

        apiPersistenceInstance.getThumbnail(org, apiUUID);
        Mockito.verify(registry, times(1)).get(expectedThumbPath);
    }

    @Test
    public void testGetOASDefinition_TenantUser() throws Exception {
        Registry registry = Mockito.mock(UserRegistry.class);
        GenericArtifact artifact = PersistenceHelper.getSampleAPIArtifactForTenant();
        String apiUUID = artifact.getId();

        String sourcePath = getSourcePathFromArtifact(artifact);
        String expectedDefinitionPath = sourcePath + RegistryConstants.PATH_SEPARATOR
                + APIConstants.API_OAS_DEFINITION_RESOURCE_NAME;

        String apiPath = artifact.getPath();
        Mockito.when(GovernanceUtils.getArtifactPath(registry, apiUUID)).thenReturn(apiPath);

        String definition = "{\"swagger\":\"2.0\"}";
        Organization org = new Organization(TENANT_DOMAIN);
        APIPersistence apiPersistenceInstance = new RegistryPersistenceImplWrapper(registry, artifact);
        Mockito.when(registry.resourceExists(expectedDefinitionPath)).thenReturn(true);
        Resource oasResource = new ResourceImpl();
        oasResource.setContent(definition.getBytes());
        Mockito.when(registry.get(expectedDefinitionPath)).thenReturn(oasResource);

        String def = apiPersistenceInstance.getOASDefinition(org, apiUUID);
        Assert.assertEquals("OAS definition should match for tenant user", definition, def);
    }

    @Test
    public void testGetAsyncDefinition_TenantUser() throws Exception {
        Registry registry = Mockito.mock(UserRegistry.class);
        GenericArtifact artifact = PersistenceHelper.getSampleAPIArtifactForTenant();
        String apiUUID = artifact.getId();

        GenericArtifactManager manager = Mockito.mock(GenericArtifactManager.class);
        PowerMockito.mockStatic(RegistryPersistenceUtil.class);
        PowerMockito.when(RegistryPersistenceUtil.getArtifactManager(registry, APIConstants.API_KEY))
                .thenReturn(manager);
        PowerMockito.when(RegistryPersistenceUtil.extractApiSourcePath(anyString())).thenCallRealMethod();
        Mockito.when(manager.getGenericArtifact(apiUUID)).thenReturn(artifact);

        String sourcePath = getSourcePathFromArtifact(artifact);
        String expectedDefinitionPath = sourcePath + RegistryConstants.PATH_SEPARATOR
                + APIConstants.API_ASYNC_API_DEFINITION_RESOURCE_NAME;

        String apiPath = artifact.getPath();
        Mockito.when(GovernanceUtils.getArtifactPath(registry, apiUUID)).thenReturn(apiPath);

        String definition = "{\"asyncapi\":\"2.0.0\"}";
        Organization org = new Organization(TENANT_DOMAIN);
        APIPersistence apiPersistenceInstance = new RegistryPersistenceImplWrapper(registry, artifact);
        Mockito.when(registry.resourceExists(expectedDefinitionPath)).thenReturn(true);
        Resource asyncResource = new ResourceImpl();
        asyncResource.setContent(definition.getBytes());
        Mockito.when(registry.get(expectedDefinitionPath)).thenReturn(asyncResource);

        String def = apiPersistenceInstance.getAsyncDefinition(org, apiUUID);
        Assert.assertEquals("Async definition should match for tenant user", definition, def);
    }

    @Test
    public void testGetPublisherAPI_TenantUser() throws Exception {
        Registry registry = Mockito.mock(UserRegistry.class);
        Resource resource = new ResourceImpl();
        Mockito.when(registry.get(anyString())).thenReturn(resource);
        Tag[] tags = new Tag[1];
        Tag tag = new Tag();
        tag.setTagName("testTag");
        tags[0] = tag;
        Mockito.when(registry.getTags(anyString())).thenReturn(tags);

        GenericArtifact artifact = PersistenceHelper.getSampleAPIArtifactForTenant();
        String apiUUID = artifact.getId();
        String apiPath = artifact.getPath();
        Mockito.when(GovernanceUtils.getArtifactPath(registry, apiUUID)).thenReturn(apiPath);

        APIPersistence apiPersistenceInstance = new RegistryPersistenceImplWrapper(registry, artifact);
        Organization org = new Organization(TENANT_DOMAIN);
        PublisherAPI publisherAPI = apiPersistenceInstance.getPublisherAPI(org, apiUUID);

        Assert.assertNotNull("Publisher API should not be null for tenant user", publisherAPI);
        Assert.assertEquals("API UUID should match", apiUUID, publisherAPI.getId());
    }

    @Test
    public void testGetPublisherAPIProduct_SuperTenantUser() throws Exception {
        Registry registry = Mockito.mock(UserRegistry.class);
        Resource resource = new ResourceImpl();
        Mockito.when(registry.get(anyString())).thenReturn(resource);
        Tag[] tags = new Tag[1];
        Tag tag = new Tag();
        tag.setTagName("testTag");
        tags[0] = tag;
        Mockito.when(registry.getTags(anyString())).thenReturn(tags);

        GenericArtifact artifact = PersistenceHelper.getSampleAPIProductArtifact();
        String apiProductId = artifact.getId();
        String apiPath = artifact.getPath();
        Mockito.when(GovernanceUtils.getArtifactPath(registry, apiProductId)).thenReturn(apiPath);

        APIPersistence apiPersistenceInstance = new RegistryPersistenceImplWrapper(registry, artifact);
        Organization org = new Organization(SUPER_TENANT_DOMAIN);
        apiPersistenceInstance.getPublisherAPIProduct(org, apiProductId);
        // Verify OAS definition path is accessed at the correct location
        String sourcePath = getSourcePathFromArtifact(artifact);
        String expectedOasPath = sourcePath + RegistryConstants.PATH_SEPARATOR
                + APIConstants.API_OAS_DEFINITION_RESOURCE_NAME;
        Mockito.verify(registry, times(1)).resourceExists(expectedOasPath);
    }

    // =====================================================================
    // Non-provider-change tests for super tenant email domain users
    // (e.g., user@gmail.com in carbon.super — provider stored as user-AT-gmail.com)
    // =====================================================================

    @Test
    public void testGetWSDL_SuperTenantEmailDomainUser() throws Exception {
        Registry registry = Mockito.mock(Registry.class);
        GenericArtifact artifact = PersistenceHelper.getSampleAPIArtifactWithProvider("user@gmail.com");
        String apiUUID = artifact.getId();
        String apiName = artifact.getAttribute(APIConstants.API_OVERVIEW_NAME);
        String apiVersion = artifact.getAttribute(APIConstants.API_OVERVIEW_VERSION);

        Organization org = new Organization(SUPER_TENANT_DOMAIN);
        APIPersistence apiPersistenceInstance = new RegistryPersistenceImplWrapper(registry, artifact);

        String sourcePath = getSourcePathFromArtifact(artifact);
        String expectedWsdlPath = sourcePath + RegistryConstants.PATH_SEPARATOR
                + RegistryPersistenceUtil.createWsdlFileName("user-AT-gmail.com", apiName, apiVersion);

        Mockito.when(registry.resourceExists(expectedWsdlPath)).thenReturn(true);
        Resource wsdlResource = Mockito.mock(Resource.class);
        Mockito.when(registry.get(expectedWsdlPath)).thenReturn(wsdlResource);

        apiPersistenceInstance.getWSDL(org, apiUUID);
        Mockito.verify(registry, times(1)).get(expectedWsdlPath);
    }

    @Test
    public void testGetGraphQLSchema_SuperTenantEmailDomainUser() throws Exception {
        Registry registry = Mockito.mock(Registry.class);
        GenericArtifact artifact = PersistenceHelper.getSampleAPIArtifactWithProvider("user@gmail.com");
        String apiUUID = artifact.getId();
        String apiName = artifact.getAttribute(APIConstants.API_OVERVIEW_NAME);
        String apiVersion = artifact.getAttribute(APIConstants.API_OVERVIEW_VERSION);

        Organization org = new Organization(SUPER_TENANT_DOMAIN);
        APIPersistence apiPersistenceInstance = new RegistryPersistenceImplWrapper(registry, artifact);

        String sourcePath = getSourcePathFromArtifact(artifact);
        String schemaName = "user-AT-gmail.com" + APIConstants.GRAPHQL_SCHEMA_PROVIDER_SEPERATOR
                + apiName + apiVersion + APIConstants.GRAPHQL_SCHEMA_FILE_EXTENSION;
        String expectedSchemaPath = sourcePath + RegistryConstants.PATH_SEPARATOR + schemaName;

        String schema = "{ hero { name } }";
        Mockito.when(registry.resourceExists(expectedSchemaPath)).thenReturn(true);
        Resource schemaResource = new ResourceImpl();
        schemaResource.setContent(schema.getBytes());
        Mockito.when(registry.get(expectedSchemaPath)).thenReturn(schemaResource);

        String def = apiPersistenceInstance.getGraphQLSchema(org, apiUUID);
        Assert.assertEquals("GraphQL schema should match for super tenant email domain user", schema, def);
    }

    @Test
    public void testGetThumbnail_SuperTenantEmailDomainUser() throws Exception {
        Registry registry = Mockito.mock(Registry.class);
        GenericArtifact artifact = PersistenceHelper.getSampleAPIArtifactWithProvider("user@gmail.com");
        String apiUUID = artifact.getId();

        PowerMockito.mockStatic(RegistryPersistenceUtil.class);
        GenericArtifactManager manager = Mockito.mock(GenericArtifactManager.class);
        PowerMockito.when(RegistryPersistenceUtil.getArtifactManager(registry, APIConstants.API_KEY))
                .thenReturn(manager);
        PowerMockito.when(RegistryPersistenceUtil.extractApiSourcePath(anyString())).thenCallRealMethod();
        PowerMockito.when(RegistryPersistenceUtil.extractProviderFromPath(anyString(), anyString(), anyString()))
                .thenCallRealMethod();
        PowerMockito.when(RegistryPersistenceUtil.getProviderFromArtifact(any(GenericArtifact.class)))
                .thenCallRealMethod();
        PowerMockito.when(RegistryPersistenceUtil.replaceEmailDomain(anyString())).thenCallRealMethod();
        Mockito.when(manager.getGenericArtifact(apiUUID)).thenReturn(artifact);

        Organization org = new Organization(SUPER_TENANT_DOMAIN);
        APIPersistence apiPersistenceInstance = new RegistryPersistenceImplWrapper(registry, artifact);

        String sourcePath = getSourcePathFromArtifact(artifact);
        String expectedThumbPath = sourcePath + RegistryConstants.PATH_SEPARATOR + APIConstants.API_ICON_IMAGE;

        Mockito.when(registry.resourceExists(expectedThumbPath)).thenReturn(true);
        Resource imageResource = Mockito.mock(Resource.class);
        Mockito.when(registry.get(expectedThumbPath)).thenReturn(imageResource);

        apiPersistenceInstance.getThumbnail(org, apiUUID);
        Mockito.verify(registry, times(1)).get(expectedThumbPath);
    }

    @Test
    public void testGetOASDefinition_SuperTenantEmailDomainUser() throws Exception {
        Registry registry = Mockito.mock(UserRegistry.class);
        GenericArtifact artifact = PersistenceHelper.getSampleAPIArtifactWithProvider("user@gmail.com");
        String apiUUID = artifact.getId();

        String sourcePath = getSourcePathFromArtifact(artifact);
        String expectedDefinitionPath = sourcePath + RegistryConstants.PATH_SEPARATOR
                + APIConstants.API_OAS_DEFINITION_RESOURCE_NAME;

        Mockito.when(GovernanceUtils.getArtifactPath(registry, apiUUID)).thenReturn(artifact.getPath());

        String definition = "{\"swagger\":\"2.0\"}";
        Organization org = new Organization(SUPER_TENANT_DOMAIN);
        APIPersistence apiPersistenceInstance = new RegistryPersistenceImplWrapper(registry, artifact);
        Mockito.when(registry.resourceExists(expectedDefinitionPath)).thenReturn(true);
        Resource oasResource = new ResourceImpl();
        oasResource.setContent(definition.getBytes());
        Mockito.when(registry.get(expectedDefinitionPath)).thenReturn(oasResource);

        String def = apiPersistenceInstance.getOASDefinition(org, apiUUID);
        Assert.assertEquals("OAS definition should match for super tenant email domain user", definition, def);
    }

    @Test
    public void testGetPublisherAPI_SuperTenantEmailDomainUser() throws Exception {
        Registry registry = Mockito.mock(UserRegistry.class);
        Resource resource = new ResourceImpl();
        Mockito.when(registry.get(anyString())).thenReturn(resource);
        Tag[] tags = new Tag[1];
        Tag tag = new Tag();
        tag.setTagName("testTag");
        tags[0] = tag;
        Mockito.when(registry.getTags(anyString())).thenReturn(tags);

        GenericArtifact artifact = PersistenceHelper.getSampleAPIArtifactWithProvider("user@gmail.com");
        String apiUUID = artifact.getId();
        Mockito.when(GovernanceUtils.getArtifactPath(registry, apiUUID)).thenReturn(artifact.getPath());

        APIPersistence apiPersistenceInstance = new RegistryPersistenceImplWrapper(registry, artifact);
        Organization org = new Organization(SUPER_TENANT_DOMAIN);
        PublisherAPI publisherAPI = apiPersistenceInstance.getPublisherAPI(org, apiUUID);

        Assert.assertNotNull("Publisher API should not be null for email domain user", publisherAPI);
        Assert.assertEquals("API UUID should match", apiUUID, publisherAPI.getId());
    }

    // =====================================================================
    // Non-provider-change tests for secondary userstore users
    // (e.g., WSO2.COM/admin — provider path includes the userstore domain)
    // =====================================================================

    @Test
    public void testGetWSDL_SecondaryUserstoreUser() throws Exception {
        Registry registry = Mockito.mock(Registry.class);
        GenericArtifact artifact = PersistenceHelper.getSampleAPIArtifactWithProvider("WSO2.COM/admin");
        String apiUUID = artifact.getId();
        String apiName = artifact.getAttribute(APIConstants.API_OVERVIEW_NAME);
        String apiVersion = artifact.getAttribute(APIConstants.API_OVERVIEW_VERSION);

        Organization org = new Organization(SUPER_TENANT_DOMAIN);
        APIPersistence apiPersistenceInstance = new RegistryPersistenceImplWrapper(registry, artifact);

        String sourcePath = getSourcePathFromArtifact(artifact);
        String expectedWsdlPath = sourcePath + RegistryConstants.PATH_SEPARATOR
                + RegistryPersistenceUtil.createWsdlFileName("WSO2.COM/admin", apiName, apiVersion);

        Mockito.when(registry.resourceExists(expectedWsdlPath)).thenReturn(true);
        Resource wsdlResource = Mockito.mock(Resource.class);
        Mockito.when(registry.get(expectedWsdlPath)).thenReturn(wsdlResource);

        apiPersistenceInstance.getWSDL(org, apiUUID);
        Mockito.verify(registry, times(1)).get(expectedWsdlPath);
    }

    @Test
    public void testGetGraphQLSchema_SecondaryUserstoreUser() throws Exception {
        Registry registry = Mockito.mock(Registry.class);
        GenericArtifact artifact = PersistenceHelper.getSampleAPIArtifactWithProvider("WSO2.COM/admin");
        String apiUUID = artifact.getId();
        String apiName = artifact.getAttribute(APIConstants.API_OVERVIEW_NAME);
        String apiVersion = artifact.getAttribute(APIConstants.API_OVERVIEW_VERSION);

        Organization org = new Organization(SUPER_TENANT_DOMAIN);
        APIPersistence apiPersistenceInstance = new RegistryPersistenceImplWrapper(registry, artifact);

        String sourcePath = getSourcePathFromArtifact(artifact);
        String schemaName = "WSO2.COM/admin" + APIConstants.GRAPHQL_SCHEMA_PROVIDER_SEPERATOR
                + apiName + apiVersion + APIConstants.GRAPHQL_SCHEMA_FILE_EXTENSION;
        String expectedSchemaPath = sourcePath + RegistryConstants.PATH_SEPARATOR + schemaName;

        String schema = "{ hero { name } }";
        Mockito.when(registry.resourceExists(expectedSchemaPath)).thenReturn(true);
        Resource schemaResource = new ResourceImpl();
        schemaResource.setContent(schema.getBytes());
        Mockito.when(registry.get(expectedSchemaPath)).thenReturn(schemaResource);

        String def = apiPersistenceInstance.getGraphQLSchema(org, apiUUID);
        Assert.assertEquals("GraphQL schema should match for secondary userstore user", schema, def);
    }

    @Test
    public void testGetOASDefinition_SecondaryUserstoreUser() throws Exception {
        Registry registry = Mockito.mock(UserRegistry.class);
        GenericArtifact artifact = PersistenceHelper.getSampleAPIArtifactWithProvider("WSO2.COM/admin");
        String apiUUID = artifact.getId();

        String sourcePath = getSourcePathFromArtifact(artifact);
        String expectedDefinitionPath = sourcePath + RegistryConstants.PATH_SEPARATOR
                + APIConstants.API_OAS_DEFINITION_RESOURCE_NAME;

        Mockito.when(GovernanceUtils.getArtifactPath(registry, apiUUID)).thenReturn(artifact.getPath());

        String definition = "{\"swagger\":\"2.0\"}";
        Organization org = new Organization(SUPER_TENANT_DOMAIN);
        APIPersistence apiPersistenceInstance = new RegistryPersistenceImplWrapper(registry, artifact);
        Mockito.when(registry.resourceExists(expectedDefinitionPath)).thenReturn(true);
        Resource oasResource = new ResourceImpl();
        oasResource.setContent(definition.getBytes());
        Mockito.when(registry.get(expectedDefinitionPath)).thenReturn(oasResource);

        String def = apiPersistenceInstance.getOASDefinition(org, apiUUID);
        Assert.assertEquals("OAS definition should match for secondary userstore user", definition, def);
    }
}
