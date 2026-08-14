/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.persistence.utils;

import static org.mockito.ArgumentMatchers.anyString;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;


import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APICategory;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.api.model.APIProductIdentifier;
import org.wso2.carbon.apimgt.api.model.Identifier;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.persistence.APIConstants;
import org.wso2.carbon.apimgt.persistence.dto.DevPortalAPI;
import org.wso2.carbon.apimgt.persistence.dto.PublisherAPI;
import org.wso2.carbon.apimgt.persistence.exceptions.APIPersistenceException;
import org.wso2.carbon.apimgt.persistence.internal.ServiceReferenceHolder;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifactImpl;
import org.wso2.carbon.governance.api.util.GovernanceArtifactConfiguration;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.ActionConstants;
import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.Tag;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.realm.RegistryAuthorizationManager;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.AuthorizationManager;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import junit.framework.Assert;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ MultitenantUtils.class, ServiceReferenceHolder.class, GenericArtifact.class,
        PrivilegedCarbonContext.class, GovernanceUtils.class, ServerConfiguration.class,
        RegistryPersistenceUtil.class, RegistryContext.class, RegistryUtils.class })
public class RegistryPersistenceUtilTestCase {

    private final int SUPER_TENANT_ID = -1234;
    private final String SUPER_TENANT_DOMAIN = "carbon.super";
    private final int TENANT_ID = 1;
    private final String TENANT_DOMAIN = "wso2.com";
    private Registry registry;
    private RealmService realmService;

    @Before
    public void setupClass() throws Exception {
        System.setProperty("carbon.home", "");
        ServiceReferenceHolder serviceRefHolder = Mockito.mock(ServiceReferenceHolder.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceRefHolder);
        realmService = Mockito.mock(RealmService.class);
        PowerMockito.when(serviceRefHolder.getRealmService()).thenReturn(realmService);

        TenantManager tenantManager = Mockito.mock(TenantManager.class);
        PowerMockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        PowerMockito.when(tenantManager.getTenantId(SUPER_TENANT_DOMAIN)).thenReturn(SUPER_TENANT_ID);
        PowerMockito.when(tenantManager.getTenantId(TENANT_DOMAIN)).thenReturn(TENANT_ID);
        
        registry = Mockito.mock(Registry.class);

        PowerMockito.mockStatic(MultitenantUtils.class);
        Resource resource = new ResourceImpl();
        Mockito.when(registry.get(anyString())).thenReturn(resource);
        Tag[] tags = new Tag[1];
        Tag tag = new Tag();
        tag.setTagName("testTag");
        tags[0] = tag;
        Mockito.when(registry.getTags(anyString())).thenReturn(tags);
        PowerMockito.mockStatic(GovernanceUtils.class);
        
        PowerMockito.mockStatic(PrivilegedCarbonContext.class);
        PrivilegedCarbonContext privilegedContext = Mockito.mock(PrivilegedCarbonContext.class);
        PowerMockito.when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(privilegedContext);
    }

    @Test
    public void testAPIGet() throws APIManagementException, RegistryException, UserStoreException {
       
        GenericArtifact artifact = PersistenceHelper.getSampleAPIArtifact();
        String apiPath = generateArtifactPath(artifact);
        Mockito.when(GovernanceUtils.getArtifactPath(registry, artifact.getId())).thenReturn(apiPath);
        API api = RegistryPersistenceUtil.getAPI(artifact, registry);
        Assert.assertEquals("Attibute overview_type does not match", artifact.getAttribute("overview_type"),
                api.getType());
        Assert.assertEquals("API id does not match", artifact.getId(),
                api.getUuid());
        Assert.assertEquals("API tag does not match", "testTag",
                api.getTags().iterator().next());
    }
    @Test
    public void testcreateAPIArtifactContent() throws APIPersistenceException, APIManagementException, RegistryException {
        API api = new API(new APIIdentifier("pubuser", "TestAPI", "1.0"));
        Set<Tier> availableTiers = new HashSet<Tier>();
        availableTiers.add(new Tier("Unlimited"));
        availableTiers.add(new Tier("Gold"));
        api.setAvailableTiers(availableTiers);
        Set<URITemplate> uriTemplates = new HashSet<URITemplate>();
        URITemplate template = new URITemplate();
        template.setHTTPVerb("GET");
        template.setUriTemplate("/test");
        template.setAuthType("None");
        uriTemplates.add(template);
        api.setUriTemplates(uriTemplates);
        List<APICategory> categories = new ArrayList<APICategory>();
        APICategory category = new APICategory();
        category.setName("testcategory");
        categories.add(category);
        api.setApiCategories(categories);

        GenericArtifact genericArtifact = new GenericArtifactImpl(new QName("", "TestAPI", ""),
                "application/vnd.wso2-api+xml");
        genericArtifact.setAttribute("URITemplate", "/test");

        GenericArtifact retArtifact = RegistryPersistenceUtil.createAPIArtifactContent(genericArtifact, api);
        
        Assert.assertEquals("API name does not match", api.getId().getApiName(),
                retArtifact.getAttribute("overview_name"));
        Assert.assertEquals("API version does not match", api.getId().getVersion(),
                retArtifact.getAttribute("overview_version"));
        Assert.assertEquals("API provider does not match", api.getId().getProviderName(),
                retArtifact.getAttribute("overview_provider"));
    }
    @Test
    public void testAPIProductGet() throws GovernanceException, APIManagementException {

        GenericArtifact artifact = PersistenceHelper.getSampleAPIProductArtifact();
        String apiPath = generateArtifactPath(artifact);
        Mockito.when(GovernanceUtils.getArtifactPath(registry, artifact.getId())).thenReturn(apiPath);
        APIProduct apiProduct = RegistryPersistenceUtil.getAPIProduct(artifact, registry);
        Assert.assertEquals("Attibute overview_type does not match", artifact.getAttribute("overview_type"),
                apiProduct.getType());
        Assert.assertEquals("API product id does not match", artifact.getId(),
                apiProduct.getUuid());
    }
    
    @Test
    public void testcreateAPIProductArtifactContent()
            throws APIPersistenceException, APIManagementException, RegistryException {
        APIProduct product = new APIProduct(new APIProductIdentifier("pubuser", "TestAPIProd", "1.0.0"));
        GenericArtifact genericArtifact = new GenericArtifactImpl(new QName("", "TestAPIProd", ""),
                "application/vnd.wso2-api+xml");
        List<APICategory> categories = new ArrayList<APICategory>();
        APICategory category = new APICategory();
        category.setName("testcategory");
        categories.add(category);
        product.setApiCategories(categories);
        Set<Tier> availableTiers = new HashSet<Tier>();
        availableTiers.add(new Tier("Unlimited"));
        availableTiers.add(new Tier("Gold"));
        product.setAvailableTiers(availableTiers);
        GenericArtifact retArtifact = RegistryPersistenceUtil.createAPIProductArtifactContent(genericArtifact, product);
        
        Assert.assertEquals("API name does not match", product.getId().getName(),
                retArtifact.getAttribute("overview_name"));
        Assert.assertEquals("API version does not match", product.getId().getVersion(),
                retArtifact.getAttribute("overview_version"));
        Assert.assertEquals("API provider does not match", product.getId().getProviderName(),
                retArtifact.getAttribute("overview_provider"));
    }
    
    @Test
    public void testGetAPIForSearch() throws APIPersistenceException, GovernanceException {
        GenericArtifact genericArtifact = PersistenceHelper.getSampleAPIArtifact();
        PublisherAPI api = RegistryPersistenceUtil.getAPIForSearch(genericArtifact);
        Assert.assertEquals("API name does not match", genericArtifact.getAttribute("overview_name"),
                api.getApiName());
        Assert.assertEquals("API version does not match", genericArtifact.getAttribute("overview_version"),
                api.getVersion());
        Assert.assertEquals("API provider does not match", genericArtifact.getAttribute("overview_provider"),
                api.getProviderName());
    }
    
    @Test
    public void testGetDevPortalAPIForSearch() throws APIPersistenceException, GovernanceException {
        GenericArtifact genericArtifact = PersistenceHelper.getSampleAPIArtifact();
        DevPortalAPI api = RegistryPersistenceUtil.getDevPortalAPIForSearch(genericArtifact);
        Assert.assertEquals("API name does not match", genericArtifact.getAttribute("overview_name"),
                api.getApiName());
        Assert.assertEquals("API version does not match", genericArtifact.getAttribute("overview_version"),
                api.getVersion());
        Assert.assertEquals("API provider does not match", genericArtifact.getAttribute("overview_provider"),
                api.getProviderName());
    }
    
    @Test
    public void testTenantDomain() {
        PowerMockito.mockStatic(ServerConfiguration.class);
        ServerConfiguration config = Mockito.mock(ServerConfiguration.class);
        PowerMockito.when(ServerConfiguration.getInstance()).thenReturn(config);
        PowerMockito.when(config.getFirstProperty(anyString())).thenReturn(null);
      
        String domain = RegistryPersistenceUtil
                .getTenantDomain(new APIIdentifier("test@" + TENANT_DOMAIN, "test", "1.0"));
        
        domain = RegistryPersistenceUtil
                .getTenantDomain(new APIIdentifier("test", "test", "1.0"));
        
    }
    
    @Test
    public void testGetArtifactManager() throws RegistryException, APIPersistenceException {
        Registry registry = Mockito.mock(UserRegistry.class);
        GovernanceArtifactConfiguration conf = Mockito.mock(GovernanceArtifactConfiguration.class);
        PowerMockito.when(GovernanceUtils.findGovernanceArtifactConfiguration(APIConstants.API_KEY, registry))
                .thenReturn(conf);
        Association[] assosiations = new Association[0];
        Mockito.when(conf.getRelationshipDefinitions()).thenReturn(assosiations );
        GenericArtifactManager manager = RegistryPersistenceUtil.getArtifactManager(registry, APIConstants.API_KEY);
        Assert.assertNotNull("Manager is null", manager);
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

    // =====================================================================
    // Tests for extractApiSourcePath
    // =====================================================================

    @Test
    public void testExtractApiSourcePath_SimpleUser() throws APIPersistenceException {
        String path = "/apimgt/applicationdata/provider/admin/MyAPI/1.0/api";
        String result = RegistryPersistenceUtil.extractApiSourcePath(path);
        Assert.assertEquals("/apimgt/applicationdata/provider/admin/MyAPI/1.0", result);
    }

    @Test
    public void testExtractApiSourcePath_EmailDomainUser() throws APIPersistenceException {
        String path = "/apimgt/applicationdata/provider/user-AT-gmail.com/MyAPI/1.0/api";
        String result = RegistryPersistenceUtil.extractApiSourcePath(path);
        Assert.assertEquals("/apimgt/applicationdata/provider/user-AT-gmail.com/MyAPI/1.0", result);
    }

    @Test
    public void testExtractApiSourcePath_TenantEmailUser() throws APIPersistenceException {
        String path = "/apimgt/applicationdata/provider/user-AT-gmail.com-AT-abc.com/MyAPI/1.0/api";
        String result = RegistryPersistenceUtil.extractApiSourcePath(path);
        Assert.assertEquals("/apimgt/applicationdata/provider/user-AT-gmail.com-AT-abc.com/MyAPI/1.0", result);
    }

    @Test
    public void testExtractApiSourcePath_SecondaryUserstore() throws APIPersistenceException {
        String path = "/apimgt/applicationdata/provider/WSO2.COM/user/MyAPI/1.0/api";
        String result = RegistryPersistenceUtil.extractApiSourcePath(path);
        Assert.assertEquals("/apimgt/applicationdata/provider/WSO2.COM/user/MyAPI/1.0", result);
    }

    @Test
    public void testExtractApiSourcePath_SecondaryUserstoreEmailTenant() throws APIPersistenceException {
        String path = "/apimgt/applicationdata/provider/WSO2.COM/user-AT-gmail.com-AT-abc.com/MyAPI/1.0/api";
        String result = RegistryPersistenceUtil.extractApiSourcePath(path);
        Assert.assertEquals("/apimgt/applicationdata/provider/WSO2.COM/user-AT-gmail.com-AT-abc.com/MyAPI/1.0",
                result);
    }

    @Test
    public void testExtractApiSourcePath_ApiNameContainsApi() throws APIPersistenceException {
        String path = "/apimgt/applicationdata/provider/admin/api-test/1.0/api";
        String result = RegistryPersistenceUtil.extractApiSourcePath(path);
        Assert.assertEquals("/apimgt/applicationdata/provider/admin/api-test/1.0", result);
    }

    @Test(expected = APIPersistenceException.class)
    public void testExtractApiSourcePath_NullPath() throws APIPersistenceException {
        RegistryPersistenceUtil.extractApiSourcePath(null);
    }

    @Test(expected = APIPersistenceException.class)
    public void testExtractApiSourcePath_NoApiSuffix() throws APIPersistenceException {
        RegistryPersistenceUtil.extractApiSourcePath("/registry/resource/provider/admin/MyAPI/1.0");
    }

    // =====================================================================
    // Tests for extractProvider (2-arg)
    // =====================================================================

    @Test
    public void testExtractProvider_SimpleUser() throws Exception {
        String path = "/apimgt/applicationdata/provider/admin/MyAPI/1.0/api";
        String result = RegistryPersistenceUtil.extractProviderFromPath(path, "MyAPI", "1.0");
        Assert.assertEquals("admin", result);
    }

    @Test
    public void testExtractProvider_EmailDomainUser() throws Exception {
        String path = "/apimgt/applicationdata/provider/user-AT-gmail.com/MyAPI/1.0/api";
        String result = RegistryPersistenceUtil.extractProviderFromPath(path, "MyAPI", "1.0");
        Assert.assertEquals("user-AT-gmail.com", result);
    }

    @Test
    public void testExtractProvider_TenantEmailUser() throws Exception {
        String path = "/apimgt/applicationdata/provider/user-AT-gmail.com-AT-abc.com/MyAPI/1.0/api";
        String result = RegistryPersistenceUtil.extractProviderFromPath(path, "MyAPI", "1.0");
        Assert.assertEquals("user-AT-gmail.com-AT-abc.com", result);
    }

    @Test
    public void testExtractProvider_SecondaryUserstore() throws Exception {
        String path = "/apimgt/applicationdata/provider/WSO2.COM/user/MyAPI/1.0/api";
        String result = RegistryPersistenceUtil.extractProviderFromPath(path, "MyAPI", "1.0");
        Assert.assertEquals("WSO2.COM/user", result);
    }

    @Test
    public void testExtractProvider_SecondaryUserstoreEmailTenant() throws Exception {
        String path = "/apimgt/applicationdata/provider/WSO2.COM/user-AT-gmail.com-AT-abc.com/MyAPI/1.0/api";
        String result = RegistryPersistenceUtil.extractProviderFromPath(path, "MyAPI", "1.0");
        Assert.assertEquals("WSO2.COM/user-AT-gmail.com-AT-abc.com", result);
    }

    // =====================================================================
    // Edge cases: API name matches provider name or version
    // =====================================================================

    @Test
    public void testExtractProvider_ApiNameSameAsSimpleProvider() throws Exception {
        // Provider is "admin", API name is also "admin"
        String path = "/apimgt/applicationdata/provider/admin/admin/1.0/api";
        String result = RegistryPersistenceUtil.extractProviderFromPath(path, "admin", "1.0");
        Assert.assertEquals("admin", result);
    }

    @Test
    public void testExtractProvider_ApiNameSameAsSecondaryUserstoreUsername() throws Exception {
        // Provider is "WSO2.COM/admin", API name is "admin"
        String path = "/apimgt/applicationdata/provider/WSO2.COM/admin/admin/1.0/api";
        String result = RegistryPersistenceUtil.extractProviderFromPath(path, "admin", "1.0");
        Assert.assertEquals("WSO2.COM/admin", result);
    }

    @Test
    public void testExtractProvider_ApiNameAndVersionSameAsProvider() throws Exception {
        // Pathological case: provider "admin", API name "admin", version "admin"
        String path = "/apimgt/applicationdata/provider/admin/admin/admin/api";
        String result = RegistryPersistenceUtil.extractProviderFromPath(path, "admin", "admin");
        Assert.assertEquals("admin", result);
    }

    @Test
    public void testExtractProvider_ApiNameSameAsTenantProvider() throws Exception {
        // Provider is "admin-AT-abc.com", API name is "admin-AT-abc.com" (unlikely but valid)
        String path = "/apimgt/applicationdata/provider/admin-AT-abc.com/admin-AT-abc.com/1.0/api";
        String result = RegistryPersistenceUtil.extractProviderFromPath(path, "admin-AT-abc.com", "1.0");
        Assert.assertEquals("admin-AT-abc.com", result);
    }

    // =====================================================================
    // Tests for extractProviderFromPath (4-arg, with revision path support)
    // =====================================================================

    @Test
    public void testExtractProvider3Arg_CurrentPath() throws APIPersistenceException {
        String path = "/apimgt/applicationdata/provider/admin/MyAPI/1.0/api";
        String result = RegistryPersistenceUtil.extractProviderFromPath(path, "MyAPI", "1.0", registry);
        Assert.assertEquals("admin", result);
    }

    @Test
    public void testExtractProvider3Arg_RevisionPath() throws Exception {
        String revisionPath = "/apimgt/applicationdata/apis/88e758b7-6924-4e9f-8882-431070b6492b/1/api";
        String currentApiPath = "/apimgt/applicationdata/provider/admin/MyAPI/1.0/api";
        Mockito.when(GovernanceUtils.getArtifactPath(registry,
                "88e758b7-6924-4e9f-8882-431070b6492b")).thenReturn(currentApiPath);
        String result = RegistryPersistenceUtil.extractProviderFromPath(revisionPath, "MyAPI", "1.0", registry);
        Assert.assertEquals("admin", result);
    }

    @Test
    public void testExtractProvider3Arg_RevisionPathWithEmailUser() throws Exception {
        String revisionPath = "/apimgt/applicationdata/apis/88e758b7-6924-4e9f-8882-431070b6492b/1/api";
        String currentApiPath = "/apimgt/applicationdata/provider/user-AT-gmail.com-AT-abc.com/MyAPI/1.0/api";
        Mockito.when(GovernanceUtils.getArtifactPath(registry,
                "88e758b7-6924-4e9f-8882-431070b6492b")).thenReturn(currentApiPath);
        String result = RegistryPersistenceUtil.extractProviderFromPath(revisionPath, "MyAPI", "1.0", registry);
        Assert.assertEquals("user-AT-gmail.com-AT-abc.com", result);
    }

    @Test
    public void testExtractProvider4Arg_RevisionPathWithSecondaryUserstore() throws Exception {
        String revisionPath = "/apimgt/applicationdata/apis/88e758b7-6924-4e9f-8882-431070b6492b/1/api";
        String currentApiPath = "/apimgt/applicationdata/provider/WSO2.COM/admin/MyAPI/1.0/api";
        Mockito.when(GovernanceUtils.getArtifactPath(registry,
                 "88e758b7-6924-4e9f-8882-431070b6492b")).thenReturn(currentApiPath);
        String result = RegistryPersistenceUtil.extractProviderFromPath(revisionPath, "MyAPI", "1.0", registry);
        Assert.assertEquals("WSO2.COM/admin", result);
    }

    @Test
    public void testExtractProvider4Arg_RevisionPathWithSecondaryUserstoreEmailTenant() throws Exception {
        String revisionPath = "/apimgt/applicationdata/apis/88e758b7-6924-4e9f-8882-431070b6492b/2/api";
        String currentApiPath = "/apimgt/applicationdata/provider/WSO2.COM/user-AT-gmail.com-AT-abc.com/MyAPI/1.0/api";
        Mockito.when(GovernanceUtils.getArtifactPath(registry,
                "88e758b7-6924-4e9f-8882-431070b6492b")).thenReturn(currentApiPath);
        String result = RegistryPersistenceUtil.extractProviderFromPath(revisionPath, "MyAPI", "1.0", registry);
        Assert.assertEquals("WSO2.COM/user-AT-gmail.com-AT-abc.com", result);
    }

    // =====================================================================
    // Tests for getProviderFromArtifact — handles state B (@) and state A/C (-AT-)
    // =====================================================================

    @Test
    public void testGetProviderFromArtifact_NormalProvider() throws GovernanceException {
        GenericArtifact artifact = PersistenceHelper.getSampleAPIArtifact();
        // "admin" has no @ so should remain "admin"
        String result = RegistryPersistenceUtil.getProviderFromArtifact(artifact);
        Assert.assertEquals("admin", result);
    }

    @Test
    public void testGetProviderFromArtifact_StateBRawAt() throws GovernanceException {
        GenericArtifact artifact = PersistenceHelper.getSampleAPIArtifact();
        // Simulate state B: raw @ stored after pre-fix provider change
        artifact.setAttribute(APIConstants.API_OVERVIEW_PROVIDER, "publisher@abc.com");
        String result = RegistryPersistenceUtil.getProviderFromArtifact(artifact);
        Assert.assertEquals("publisher-AT-abc.com", result);
    }

    @Test
    public void testGetProviderFromArtifact_StateC_AlreadyEncoded() throws GovernanceException {
        GenericArtifact artifact = PersistenceHelper.getSampleAPIArtifact();
        // Simulate state C: -AT- stored after post-fix provider change
        artifact.setAttribute(APIConstants.API_OVERVIEW_PROVIDER, "publisher-AT-abc.com");
        String result = RegistryPersistenceUtil.getProviderFromArtifact(artifact);
        Assert.assertEquals("publisher-AT-abc.com", result);
    }

    @Test
    public void testGetProviderFromArtifact_MultipleAtSigns() throws GovernanceException {
        GenericArtifact artifact = PersistenceHelper.getSampleAPIArtifact();
        // Simulate tenant email user: user@gmail.com@abc.com
        artifact.setAttribute(APIConstants.API_OVERVIEW_PROVIDER, "user@gmail.com@abc.com");
        String result = RegistryPersistenceUtil.getProviderFromArtifact(artifact);
        Assert.assertEquals("user-AT-gmail.com-AT-abc.com", result);
    }

    // =====================================================================
    // Tests for path/name case-mismatch tolerance in extractProviderFromPath
    // The registry resource path segment and the artifact <overview><name>
    // attribute may differ in case in environments migrated from pre-3.x
    // product versions. The lenient match must accept the case mismatch
    // without throwing, and must preserve the provider's original case in
    // the returned substring.
    // =====================================================================

    @Test
    public void testExtractProviderFromPath_PathCamelCaseNameLowercase() throws Exception {
        // Registry path has CamelCase name segment; the artifact's <name>
        // attribute was normalised to lowercase by pre-3.x migration tooling.
        // Pre-fix this threw APIPersistenceException.
        String path = "/apimgt/applicationdata/provider/wam_mhxu8g/CSContractRepositoryStaging/v1/api";
        String result = RegistryPersistenceUtil.extractProviderFromPath(path, "cscontractrepositorystaging", "v1");
        Assert.assertEquals("wam_mhxu8g", result);
    }

    @Test
    public void testExtractProviderFromPath_PathLowercaseNameUppercase() throws Exception {
        // Reverse case-mismatch: path segment lowercase, artifact <name> uppercase.
        String path = "/apimgt/applicationdata/provider/admin/myapi/1.0/api";
        String result = RegistryPersistenceUtil.extractProviderFromPath(path, "MyAPI", "1.0");
        Assert.assertEquals("admin", result);
    }

    @Test
    public void testExtractProviderFromPath_MixedCaseMismatch() throws Exception {
        // Different mixed-case shapes between path and input name.
        String path = "/apimgt/applicationdata/provider/admin/API_GetPO/v1/api";
        String result = RegistryPersistenceUtil.extractProviderFromPath(path, "API_getPO", "v1");
        Assert.assertEquals("admin", result);
    }

    @Test
    public void testExtractProviderFromPath_ProviderCasePreservedOnCaseMismatch() throws Exception {
        // Provider segment has mixed case and API name is case-mismatched.
        // Fix must return the provider substring in its original stored case
        // (not the lowercased search key).
        String path = "/apimgt/applicationdata/provider/Azure_Janhavi.Patil/ABC/v1/api";
        String result = RegistryPersistenceUtil.extractProviderFromPath(path, "abc", "v1");
        Assert.assertEquals("Azure_Janhavi.Patil", result);
    }

    @Test
    public void testExtractProviderFromPath_SecondaryUserstoreCasePreservedOnCaseMismatch() throws Exception {
        // Secondary userstore prefix "WSO2.COM/" with case-mismatched name.
        String path = "/apimgt/applicationdata/provider/WSO2.COM/user/TestNoSeip/v1/api";
        String result = RegistryPersistenceUtil.extractProviderFromPath(path, "testnoseip", "v1");
        Assert.assertEquals("WSO2.COM/user", result);
    }

    @Test
    public void testExtractProviderFromPath_VersionCaseMismatch() throws Exception {
        // Version segment case differs between path and input. extractProviderFromPath
        // is a path-verification operation on an already-resolved artifact; identity
        // is decided by other layers (gateway routing, /newversion, DB constraint),
        // so version case-mismatch tolerance in this specific function is safe.
        String path = "/apimgt/applicationdata/provider/admin/MyAPI/V1/api";
        String result = RegistryPersistenceUtil.extractProviderFromPath(path, "MyAPI", "v1");
        Assert.assertEquals("admin", result);
    }

    @Test(expected = APIPersistenceException.class)
    public void testExtractProviderFromPath_NameFundamentallyDifferentStillThrows() throws Exception {
        // Legitimate not-found case: input name has no case-insensitive match in
        // the path. Fix must still throw APIPersistenceException here — the
        // lenient match only tolerates CASE differences, not entirely different names.
        String path = "/apimgt/applicationdata/provider/admin/MyAPI/1.0/api";
        RegistryPersistenceUtil.extractProviderFromPath(path, "SomethingElse", "1.0");
    }

    @Test(expected = APIPersistenceException.class)
    public void testExtractProviderFromPath_VersionFundamentallyDifferentStillThrows() throws Exception {
        // Legitimate not-found: name matches (any case), but version is different.
        // Should still throw.
        String path = "/apimgt/applicationdata/provider/admin/MyAPI/1.0/api";
        RegistryPersistenceUtil.extractProviderFromPath(path, "myapi", "9.9");
    }

    // =====================================================================
    // Tests for the deprecated 2-arg extractProvider(path, name). Method
    // returns null on failure via caught exceptions rather than throwing.
    // Coverage for the same case-mismatch behaviour — the deprecated helper
    // delegates to the fixed extractProviderFromPath and must also work on
    // legacy case-mismatched data.
    // =====================================================================

    @Test
    public void testExtractProviderDeprecated_CaseMismatchNameLowercase() throws Exception {
        String path = "/apimgt/applicationdata/provider/wam_mhxu8g/CSContractRepositoryStaging/v1/api";
        @SuppressWarnings("deprecation")
        String result = RegistryPersistenceUtil.extractProvider(path, "cscontractrepositorystaging");
        Assert.assertEquals("wam_mhxu8g", result);
    }

    @Test
    public void testExtractProviderDeprecated_CaseMismatchNameUppercase() throws Exception {
        String path = "/apimgt/applicationdata/provider/admin/myapi/1.0/api";
        @SuppressWarnings("deprecation")
        String result = RegistryPersistenceUtil.extractProvider(path, "MyAPI");
        Assert.assertEquals("admin", result);
    }

    @Test
    public void testExtractProviderDeprecated_NameFundamentallyDifferentReturnsNull() {
        // Deprecated method catches exceptions and returns null on failure.
        String path = "/apimgt/applicationdata/provider/admin/MyAPI/1.0/api";
        @SuppressWarnings("deprecation")
        String result = RegistryPersistenceUtil.extractProvider(path, "CompletelyDifferentName");
        Assert.assertNull(result);
    }

    // =====================================================================
    // Tests for setResourcePermissions — RESTRICTED visibility with
    // empty/blank visibleRoles entries
    // =====================================================================

    private static final String ARTIFACT_PATH = "/apimgt/applicationdata/provider/admin/MyAPI/1.0/api";
    private static final String SUPER_TENANT_USER = "admin";
    private static final String TENANT_USER = "user@wso2.com";

    private void mockRegistryPathStatics() throws Exception {
        PowerMockito.mockStatic(RegistryContext.class);
        PowerMockito.when(RegistryContext.getBaseInstance()).thenReturn(null);
        PowerMockito.mockStatic(RegistryUtils.class);
        PowerMockito.when(RegistryUtils.getAbsolutePath(Mockito.any(), Mockito.anyString()))
                .thenAnswer(invocation -> invocation.getArgument(1));
    }

    private RegistryAuthorizationManager mockSuperTenantAuthManager() throws Exception {
        mockRegistryPathStatics();
        PowerMockito.when(MultitenantUtils.getTenantDomain(SUPER_TENANT_USER)).thenReturn(SUPER_TENANT_DOMAIN);
        UserRealm userRealm = Mockito.mock(UserRealm.class);
        Mockito.when(realmService.getTenantUserRealm(SUPER_TENANT_ID)).thenReturn(userRealm);
        RegistryAuthorizationManager authorizationManager = Mockito.mock(RegistryAuthorizationManager.class);
        PowerMockito.whenNew(RegistryAuthorizationManager.class).withAnyArguments()
                .thenReturn(authorizationManager);
        return authorizationManager;
    }

    private AuthorizationManager mockTenantAuthManager() throws Exception {
        mockRegistryPathStatics();
        PowerMockito.when(MultitenantUtils.getTenantDomain(TENANT_USER)).thenReturn(TENANT_DOMAIN);
        UserRealm userRealm = Mockito.mock(UserRealm.class);
        Mockito.when(realmService.getTenantUserRealm(TENANT_ID)).thenReturn(userRealm);
        RegistryAuthorizationManager registryAuthManager = Mockito.mock(RegistryAuthorizationManager.class);
        PowerMockito.whenNew(RegistryAuthorizationManager.class).withAnyArguments()
                .thenReturn(registryAuthManager);
        Mockito.when(registryAuthManager.computePathOnMount(anyString())).thenReturn(ARTIFACT_PATH);
        AuthorizationManager authManager = Mockito.mock(AuthorizationManager.class);
        Mockito.when(userRealm.getAuthorizationManager()).thenReturn(authManager);
        return authManager;
    }

    @Test
    public void testSetResourcePermissionsSuperTenantLoneEmptyRole() throws Exception {
        RegistryAuthorizationManager authorizationManager = mockSuperTenantAuthManager();

        RegistryPersistenceUtil.setResourcePermissions(SUPER_TENANT_USER, APIConstants.API_RESTRICTED_VISIBILITY,
                new String[] { "" }, ARTIFACT_PATH, null);

        Mockito.verify(authorizationManager).authorizeRole(Mockito.eq(APIConstants.EVERYONE_ROLE), anyString(),
                Mockito.eq(ActionConstants.GET));
        Mockito.verify(authorizationManager, Mockito.never()).authorizeRole(Mockito.eq(""), anyString(),
                anyString());
        Mockito.verify(authorizationManager, Mockito.never()).denyRole(Mockito.eq(APIConstants.EVERYONE_ROLE),
                anyString(), anyString());
        Mockito.verify(authorizationManager).denyRole(Mockito.eq(APIConstants.ANONYMOUS_ROLE), anyString(),
                Mockito.eq(ActionConstants.GET));
    }

    @Test
    public void testSetResourcePermissionsSuperTenantBlankRolesSkipped() throws Exception {
        RegistryAuthorizationManager authorizationManager = mockSuperTenantAuthManager();

        RegistryPersistenceUtil.setResourcePermissions(SUPER_TENANT_USER, APIConstants.API_RESTRICTED_VISIBILITY,
                new String[] { "", "  ", "internal/subscriber" }, ARTIFACT_PATH, null);

        Mockito.verify(authorizationManager).authorizeRole(Mockito.eq("internal/subscriber"), anyString(),
                Mockito.eq(ActionConstants.GET));
        Mockito.verify(authorizationManager, Mockito.never()).authorizeRole(Mockito.eq(""), anyString(),
                anyString());
        // no everyone role in the list -> everyone must be denied
        Mockito.verify(authorizationManager).denyRole(Mockito.eq(APIConstants.EVERYONE_ROLE), anyString(),
                Mockito.eq(ActionConstants.GET));
        Mockito.verify(authorizationManager).denyRole(Mockito.eq(APIConstants.ANONYMOUS_ROLE), anyString(),
                Mockito.eq(ActionConstants.GET));
    }

    @Test
    public void testSetResourcePermissionsTenantLoneEmptyRole() throws Exception {
        AuthorizationManager authManager = mockTenantAuthManager();

        RegistryPersistenceUtil.setResourcePermissions(TENANT_USER, APIConstants.API_RESTRICTED_VISIBILITY,
                new String[] { "" }, ARTIFACT_PATH, null);

        Mockito.verify(authManager).authorizeRole(Mockito.eq(APIConstants.EVERYONE_ROLE), anyString(),
                Mockito.eq(ActionConstants.GET));
        Mockito.verify(authManager, Mockito.never()).authorizeRole(Mockito.eq(""), anyString(), anyString());
        Mockito.verify(authManager, Mockito.never()).denyRole(Mockito.eq(APIConstants.EVERYONE_ROLE), anyString(),
                anyString());
        Mockito.verify(authManager).denyRole(Mockito.eq(APIConstants.ANONYMOUS_ROLE), anyString(),
                Mockito.eq(ActionConstants.GET));
    }

    @Test
    public void testSetResourcePermissionsTenantBlankRolesSkipped() throws Exception {
        AuthorizationManager authManager = mockTenantAuthManager();

        RegistryPersistenceUtil.setResourcePermissions(TENANT_USER, APIConstants.API_RESTRICTED_VISIBILITY,
                new String[] { "", "  ", "internal/subscriber" }, ARTIFACT_PATH, null);

        Mockito.verify(authManager).authorizeRole(Mockito.eq("internal/subscriber"), anyString(),
                Mockito.eq(ActionConstants.GET));
        Mockito.verify(authManager, Mockito.never()).authorizeRole(Mockito.eq(""), anyString(), anyString());
        Mockito.verify(authManager).denyRole(Mockito.eq(APIConstants.EVERYONE_ROLE), anyString(),
                Mockito.eq(ActionConstants.GET));
        Mockito.verify(authManager).denyRole(Mockito.eq(APIConstants.ANONYMOUS_ROLE), anyString(),
                Mockito.eq(ActionConstants.GET));
    }
}
