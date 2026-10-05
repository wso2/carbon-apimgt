/*
 *  Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com)
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.persistence.dto.Organization;
import org.wso2.carbon.apimgt.persistence.dto.PublisherAPI;
import org.wso2.carbon.apimgt.persistence.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.persistence.utils.PersistenceHelper;
import org.wso2.carbon.apimgt.persistence.utils.RegistryPersistenceUtil;
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
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.indexing.indexer.IndexerException;
import org.wso2.carbon.registry.indexing.solr.SolrClient;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;

import junit.framework.Assert;

/**
 * Tests that verify correct behavior after an API provider change.
 *
 * After a provider change, the artifact's API_OVERVIEW_PROVIDER attribute reflects the new provider,
 * but the physical registry path still uses the original provider. These tests ensure all methods
 * derive paths from the actual artifact path rather than from the provider attribute.
 *
 * Each provider change combination is tested for:
 * - WSDL retrieval (filename embeds provider)
 * - GraphQL schema retrieval (filename embeds provider)
 * - Thumbnail retrieval (path-based)
 * - OAS definition retrieval (path-based)
 * - Async definition retrieval (path-based)
 * - Publisher API loading
 *
 * Both pre-fix (raw @ in attribute) and post-fix (correct -AT- in attribute) variants are covered.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ CarbonContext.class, RegistryPersistenceUtil.class, ServiceReferenceHolder.class,
        PrivilegedCarbonContext.class, GovernanceUtils.class, SolrClient.class, RegistryPersistenceImpl.class })
public class RegistryPersistenceImplProviderChangeReadTestCase {

    private static final String SUPER_TENANT_DOMAIN = "carbon.super";
    private static final int SUPER_TENANT_ID = -1234;

    @Before
    public void setupClass() throws UserStoreException, IndexerException {
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
        GovernanceArtifact govArtifact = Mockito.mock(GovernanceArtifact.class);
        List<GovernanceArtifact> artifacts = new ArrayList<GovernanceArtifact>();
        artifacts.add(govArtifact);

        PowerMockito.mockStatic(RegistryPersistenceImpl.class);
    }

    /**
     * Creates a sample artifact that simulates a provider change scenario.
     *
     * @param newProvider        the new provider value in the artifact attribute
     * @param oldProviderEncoded the original provider in the registry path (already -AT- encoded)
     */
    private GenericArtifact createProviderChangedArtifact(String newProvider, String oldProviderEncoded)
            throws GovernanceException {
        GenericArtifact artifact = PersistenceHelper.getSampleAPIArtifact();
        artifact.setAttribute(APIConstants.API_OVERVIEW_PROVIDER, newProvider);
        String apiName = artifact.getAttribute(APIConstants.API_OVERVIEW_NAME);
        String apiVersion = artifact.getAttribute(APIConstants.API_OVERVIEW_VERSION);
        String oldPath = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR
                + oldProviderEncoded + RegistryConstants.PATH_SEPARATOR + apiName
                + RegistryConstants.PATH_SEPARATOR + apiVersion + RegistryConstants.PATH_SEPARATOR + "api";
        ((GenericArtifactWrapper) artifact).setArtifactPath(oldPath);
        return artifact;
    }

    private String getSourcePathFromArtifact(GenericArtifact artifact) throws GovernanceException {
        String path = artifact.getPath();
        return path.substring(0, path.lastIndexOf("/api"));
    }

    // =====================================================================
    // Composite verification helpers — each tests all affected methods
    // =====================================================================

    /**
     * Verifies that all provider-sensitive methods use the original provider path
     * after a provider change. Tests WSDL, GraphQL, thumbnail, OAS, async definition,
     * and publisher API loading.
     */
    /**
     * Verifies that all provider-sensitive methods use the original provider path.
     * Tests WSDL (filename embeds provider), GraphQL (filename embeds provider),
     * OAS definition (path-based), and Async definition (path-based).
     *
     * Note: Thumbnail and PublisherAPI require PowerMockito.mockStatic for
     * RegistryPersistenceUtil which conflicts when called in sequence with other
     * verify methods. These are tested separately with representative scenarios.
     */
    private void verifyAllMethodsUseOriginalProviderPath(String newProvider, String oldProviderEncoded)
            throws Exception {
        verifyWsdlUsesOriginalProviderPath(newProvider, oldProviderEncoded);
        verifyGraphQLSchemaUsesOriginalProviderPath(newProvider, oldProviderEncoded);
        verifyOASDefinitionUsesOriginalProviderPath(newProvider, oldProviderEncoded);
        verifyAsyncDefinitionUsesOriginalProviderPath(newProvider, oldProviderEncoded);
    }

    private void verifyWsdlUsesOriginalProviderPath(String newProvider, String oldProviderEncoded)
            throws Exception {
        Registry registry = Mockito.mock(Registry.class);
        GenericArtifact artifact = createProviderChangedArtifact(newProvider, oldProviderEncoded);
        String apiUUID = artifact.getId();
        String apiName = artifact.getAttribute(APIConstants.API_OVERVIEW_NAME);
        String apiVersion = artifact.getAttribute(APIConstants.API_OVERVIEW_VERSION);

        APIPersistence apiPersistenceInstance = new RegistryPersistenceImplWrapper(registry, artifact);

        String oldSourcePath = getSourcePathFromArtifact(artifact);
        String expectedWsdlPath = oldSourcePath + RegistryConstants.PATH_SEPARATOR
                + RegistryPersistenceUtil.createWsdlFileName(oldProviderEncoded, apiName, apiVersion);

        Mockito.when(registry.resourceExists(expectedWsdlPath)).thenReturn(true);
        Resource wsdlResource = Mockito.mock(Resource.class);
        Mockito.when(registry.get(expectedWsdlPath)).thenReturn(wsdlResource);

        apiPersistenceInstance.getWSDL(new Organization(SUPER_TENANT_DOMAIN), apiUUID);
        Mockito.verify(registry, times(1)).get(expectedWsdlPath);
    }

    private void verifyGraphQLSchemaUsesOriginalProviderPath(String newProvider, String oldProviderEncoded)
            throws Exception {
        Registry registry = Mockito.mock(Registry.class);
        GenericArtifact artifact = createProviderChangedArtifact(newProvider, oldProviderEncoded);
        String apiUUID = artifact.getId();
        String apiName = artifact.getAttribute(APIConstants.API_OVERVIEW_NAME);
        String apiVersion = artifact.getAttribute(APIConstants.API_OVERVIEW_VERSION);

        APIPersistence apiPersistenceInstance = new RegistryPersistenceImplWrapper(registry, artifact);

        String oldSourcePath = getSourcePathFromArtifact(artifact);
        String schemaName = oldProviderEncoded + APIConstants.GRAPHQL_SCHEMA_PROVIDER_SEPERATOR
                + apiName + apiVersion + APIConstants.GRAPHQL_SCHEMA_FILE_EXTENSION;
        String expectedSchemaPath = oldSourcePath + RegistryConstants.PATH_SEPARATOR + schemaName;

        String schema = "{ hero { name } }";
        Mockito.when(registry.resourceExists(expectedSchemaPath)).thenReturn(true);
        Resource schemaResource = new ResourceImpl();
        schemaResource.setContent(schema.getBytes());
        Mockito.when(registry.get(expectedSchemaPath)).thenReturn(schemaResource);

        String def = apiPersistenceInstance.getGraphQLSchema(new Organization(SUPER_TENANT_DOMAIN), apiUUID);
        Assert.assertEquals("GraphQL schema should use original provider path", schema, def);
    }

    private void verifyThumbnailUsesOriginalProviderPath(String newProvider, String oldProviderEncoded)
            throws Exception {
        Registry registry = Mockito.mock(Registry.class);
        GenericArtifact artifact = createProviderChangedArtifact(newProvider, oldProviderEncoded);
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

        APIPersistence apiPersistenceInstance = new RegistryPersistenceImplWrapper(registry, artifact);

        String oldSourcePath = getSourcePathFromArtifact(artifact);
        String expectedThumbPath = oldSourcePath + RegistryConstants.PATH_SEPARATOR + APIConstants.API_ICON_IMAGE;

        Mockito.when(registry.resourceExists(expectedThumbPath)).thenReturn(true);
        Resource imageResource = Mockito.mock(Resource.class);
        Mockito.when(registry.get(expectedThumbPath)).thenReturn(imageResource);

        apiPersistenceInstance.getThumbnail(new Organization(SUPER_TENANT_DOMAIN), apiUUID);
        Mockito.verify(registry, times(1)).get(expectedThumbPath);
    }

    private void verifyOASDefinitionUsesOriginalProviderPath(String newProvider, String oldProviderEncoded)
            throws Exception {
        Registry registry = Mockito.mock(UserRegistry.class);
        GenericArtifact artifact = createProviderChangedArtifact(newProvider, oldProviderEncoded);
        String apiUUID = artifact.getId();

        String oldSourcePath = getSourcePathFromArtifact(artifact);
        String expectedDefinitionPath = oldSourcePath + RegistryConstants.PATH_SEPARATOR
                + APIConstants.API_OAS_DEFINITION_RESOURCE_NAME;

        Mockito.when(GovernanceUtils.getArtifactPath(registry, apiUUID)).thenReturn(artifact.getPath());

        String definition = "{\"swagger\":\"2.0\"}";
        APIPersistence apiPersistenceInstance = new RegistryPersistenceImplWrapper(registry, artifact);
        Mockito.when(registry.resourceExists(expectedDefinitionPath)).thenReturn(true);
        Resource oasResource = new ResourceImpl();
        oasResource.setContent(definition.getBytes());
        Mockito.when(registry.get(expectedDefinitionPath)).thenReturn(oasResource);

        String def = apiPersistenceInstance.getOASDefinition(new Organization(SUPER_TENANT_DOMAIN), apiUUID);
        Assert.assertEquals("OAS definition should use original provider path", definition, def);
    }

    private void verifyAsyncDefinitionUsesOriginalProviderPath(String newProvider, String oldProviderEncoded)
            throws Exception {
        Registry registry = Mockito.mock(UserRegistry.class);
        GenericArtifact artifact = createProviderChangedArtifact(newProvider, oldProviderEncoded);
        String apiUUID = artifact.getId();

        GenericArtifactManager manager = Mockito.mock(GenericArtifactManager.class);
        PowerMockito.mockStatic(RegistryPersistenceUtil.class);
        PowerMockito.when(RegistryPersistenceUtil.getArtifactManager(registry, APIConstants.API_KEY))
                .thenReturn(manager);
        PowerMockito.when(RegistryPersistenceUtil.extractApiSourcePath(anyString())).thenCallRealMethod();
        Mockito.when(manager.getGenericArtifact(apiUUID)).thenReturn(artifact);

        String oldSourcePath = getSourcePathFromArtifact(artifact);
        String expectedDefinitionPath = oldSourcePath + RegistryConstants.PATH_SEPARATOR
                + APIConstants.API_ASYNC_API_DEFINITION_RESOURCE_NAME;

        Mockito.when(GovernanceUtils.getArtifactPath(registry, apiUUID)).thenReturn(artifact.getPath());

        String definition = "{\"asyncapi\":\"2.0.0\"}";
        APIPersistence apiPersistenceInstance = new RegistryPersistenceImplWrapper(registry, artifact);
        Mockito.when(registry.resourceExists(expectedDefinitionPath)).thenReturn(true);
        Resource asyncResource = new ResourceImpl();
        asyncResource.setContent(definition.getBytes());
        Mockito.when(registry.get(expectedDefinitionPath)).thenReturn(asyncResource);

        String def = apiPersistenceInstance.getAsyncDefinition(new Organization(SUPER_TENANT_DOMAIN), apiUUID);
        Assert.assertEquals("Async definition should use original provider path", definition, def);
    }

    private void verifyPublisherAPILoadsAfterProviderChange(String newProvider, String oldProviderEncoded)
            throws Exception {
        Registry registry = Mockito.mock(UserRegistry.class);
        Resource resource = new ResourceImpl();
        Tag[] tags = new Tag[1];
        Tag tag = new Tag();
        tag.setTagName("testTag");
        tags[0] = tag;

        GenericArtifact artifact = createProviderChangedArtifact(newProvider, oldProviderEncoded);
        String apiUUID = artifact.getId();
        String expectedArtifactPath = artifact.getPath();

        // Mock registry to respond to the old provider path specifically
        Mockito.when(registry.get(expectedArtifactPath)).thenReturn(resource);
        Mockito.when(registry.getTags(expectedArtifactPath)).thenReturn(tags);
        Mockito.when(GovernanceUtils.getArtifactPath(registry, apiUUID)).thenReturn(expectedArtifactPath);

        APIPersistence apiPersistenceInstance = new RegistryPersistenceImplWrapper(registry, artifact);
        PublisherAPI publisherAPI = apiPersistenceInstance.getPublisherAPI(
                new Organization(SUPER_TENANT_DOMAIN), apiUUID);

        Assert.assertNotNull("Publisher API should not be null after provider change", publisherAPI);
        Assert.assertEquals("API UUID should match", apiUUID, publisherAPI.getId());
        // Verify the old provider path was used, not the new provider path
        Mockito.verify(registry, times(1)).get(expectedArtifactPath);
    }

    // =====================================================================
    // Super tenant — no email domain
    // Pre-fix: new provider has raw @ (only applies when tenant suffix present)
    // Post-fix: new provider has correct -AT- encoding
    // For super tenant without email, no @ in provider so pre/post-fix are same
    // =====================================================================

    @Test
    public void testSuperTenant_PrimaryToPrimary() throws Exception {
        verifyAllMethodsUseOriginalProviderPath("publisher", "admin");
    }

    @Test
    public void testSuperTenant_PrimaryToSecondary() throws Exception {
        verifyAllMethodsUseOriginalProviderPath("WSO2.COM/publisher", "admin");
    }

    @Test
    public void testSuperTenant_SecondaryToPrimary() throws Exception {
        verifyAllMethodsUseOriginalProviderPath("publisher", "WSO2.COM/admin");
    }

    @Test
    public void testSuperTenant_SecondaryToSecondary() throws Exception {
        verifyAllMethodsUseOriginalProviderPath("WSO2.COM/temp", "WSO2.COM/admin");
    }

    // =====================================================================
    // Tenant — no email domain
    // Pre-fix: attribute has raw @ (e.g., publisher@abc.com)
    // Post-fix: attribute has -AT- (e.g., publisher-AT-abc.com)
    // =====================================================================

    @Test
    public void testTenant_PrimaryToPrimary_RawAt() throws Exception {
        verifyAllMethodsUseOriginalProviderPath("publisher@abc.com", "admin-AT-abc.com");
    }

    @Test
    public void testTenant_PrimaryToPrimary_Encoded() throws Exception {
        verifyAllMethodsUseOriginalProviderPath("publisher-AT-abc.com", "admin-AT-abc.com");
    }

    @Test
    public void testTenant_PrimaryToSecondary_RawAt() throws Exception {
        verifyAllMethodsUseOriginalProviderPath("WSO2.COM/publisher@abc.com", "admin-AT-abc.com");
    }

    @Test
    public void testTenant_PrimaryToSecondary_Encoded() throws Exception {
        verifyAllMethodsUseOriginalProviderPath("WSO2.COM/publisher-AT-abc.com", "admin-AT-abc.com");
    }

    @Test
    public void testTenant_SecondaryToPrimary_RawAt() throws Exception {
        verifyAllMethodsUseOriginalProviderPath("publisher@abc.com", "WSO2.COM/admin-AT-abc.com");
    }

    @Test
    public void testTenant_SecondaryToPrimary_Encoded() throws Exception {
        verifyAllMethodsUseOriginalProviderPath("publisher-AT-abc.com", "WSO2.COM/admin-AT-abc.com");
    }

    @Test
    public void testTenant_SecondaryToSecondary_RawAt() throws Exception {
        verifyAllMethodsUseOriginalProviderPath("WSO2.COM/temp@abc.com", "WSO2.COM/admin-AT-abc.com");
    }

    @Test
    public void testTenant_SecondaryToSecondary_Encoded() throws Exception {
        verifyAllMethodsUseOriginalProviderPath("WSO2.COM/temp-AT-abc.com", "WSO2.COM/admin-AT-abc.com");
    }

    // =====================================================================
    // Super tenant — email domain configured
    // Pre-fix: attribute has raw @ (e.g., publisher@gmail.com)
    // Post-fix: attribute has -AT- (e.g., publisher-AT-gmail.com)
    // =====================================================================

    @Test
    public void testSuperTenantEmail_PrimaryToPrimary_RawAt() throws Exception {
        verifyAllMethodsUseOriginalProviderPath("publisher@gmail.com", "user-AT-gmail.com");
    }

    @Test
    public void testSuperTenantEmail_PrimaryToPrimary_Encoded() throws Exception {
        verifyAllMethodsUseOriginalProviderPath("publisher-AT-gmail.com", "user-AT-gmail.com");
    }

    @Test
    public void testSuperTenantEmail_PrimaryToSecondary_RawAt() throws Exception {
        verifyAllMethodsUseOriginalProviderPath("WSO2.COM/publisher@gmail.com", "user-AT-gmail.com");
    }

    @Test
    public void testSuperTenantEmail_PrimaryToSecondary_Encoded() throws Exception {
        verifyAllMethodsUseOriginalProviderPath("WSO2.COM/publisher-AT-gmail.com", "user-AT-gmail.com");
    }

    @Test
    public void testSuperTenantEmail_SecondaryToPrimary_RawAt() throws Exception {
        verifyAllMethodsUseOriginalProviderPath("publisher@gmail.com", "WSO2.COM/user-AT-gmail.com");
    }

    @Test
    public void testSuperTenantEmail_SecondaryToPrimary_Encoded() throws Exception {
        verifyAllMethodsUseOriginalProviderPath("publisher-AT-gmail.com", "WSO2.COM/user-AT-gmail.com");
    }

    @Test
    public void testSuperTenantEmail_SecondaryToSecondary_RawAt() throws Exception {
        verifyAllMethodsUseOriginalProviderPath("WSO2.COM/temp@gmail.com", "WSO2.COM/user-AT-gmail.com");
    }

    @Test
    public void testSuperTenantEmail_SecondaryToSecondary_Encoded() throws Exception {
        verifyAllMethodsUseOriginalProviderPath("WSO2.COM/temp-AT-gmail.com", "WSO2.COM/user-AT-gmail.com");
    }

    // =====================================================================
    // Tenant — email domain configured
    // Pre-fix: attribute has raw @ (e.g., publisher@gmail.com@abc.com)
    // Post-fix: attribute has -AT- (e.g., publisher-AT-gmail.com-AT-abc.com)
    // =====================================================================

    @Test
    public void testTenantEmail_PrimaryToPrimary_RawAt() throws Exception {
        verifyAllMethodsUseOriginalProviderPath("publisher@gmail.com@abc.com",
                "user-AT-gmail.com-AT-abc.com");
    }

    @Test
    public void testTenantEmail_PrimaryToPrimary_Encoded() throws Exception {
        verifyAllMethodsUseOriginalProviderPath("publisher-AT-gmail.com-AT-abc.com",
                "user-AT-gmail.com-AT-abc.com");
    }

    @Test
    public void testTenantEmail_PrimaryToSecondary_RawAt() throws Exception {
        verifyAllMethodsUseOriginalProviderPath("WSO2.COM/publisher@gmail.com@abc.com",
                "user-AT-gmail.com-AT-abc.com");
    }

    @Test
    public void testTenantEmail_PrimaryToSecondary_Encoded() throws Exception {
        verifyAllMethodsUseOriginalProviderPath("WSO2.COM/publisher-AT-gmail.com-AT-abc.com",
                "user-AT-gmail.com-AT-abc.com");
    }

    @Test
    public void testTenantEmail_SecondaryToPrimary_RawAt() throws Exception {
        verifyAllMethodsUseOriginalProviderPath("publisher@gmail.com@abc.com",
                "WSO2.COM/user-AT-gmail.com-AT-abc.com");
    }

    @Test
    public void testTenantEmail_SecondaryToPrimary_Encoded() throws Exception {
        verifyAllMethodsUseOriginalProviderPath("publisher-AT-gmail.com-AT-abc.com",
                "WSO2.COM/user-AT-gmail.com-AT-abc.com");
    }

    @Test
    public void testTenantEmail_SecondaryToSecondary_RawAt() throws Exception {
        verifyAllMethodsUseOriginalProviderPath("WSO2.COM/temp@gmail.com@abc.com",
                "WSO2.COM/user-AT-gmail.com-AT-abc.com");
    }

    @Test
    public void testTenantEmail_SecondaryToSecondary_Encoded() throws Exception {
        verifyAllMethodsUseOriginalProviderPath("WSO2.COM/temp-AT-gmail.com-AT-abc.com",
                "WSO2.COM/user-AT-gmail.com-AT-abc.com");
    }

    // =====================================================================
    // Thumbnail — standalone tests per domain
    // (Requires PowerMockito.mockStatic for RegistryPersistenceUtil,
    // tested separately from composite helper to avoid mock conflicts)
    // =====================================================================

    // Super tenant — no email (no @ in provider, pre/post-fix are same)
    @Test
    public void testThumbnail_SuperTenant_PrimaryToPrimary() throws Exception {
        verifyThumbnailUsesOriginalProviderPath("publisher", "admin");
    }

    @Test
    public void testThumbnail_SuperTenant_PrimaryToSecondary() throws Exception {
        verifyThumbnailUsesOriginalProviderPath("WSO2.COM/publisher", "admin");
    }

    @Test
    public void testThumbnail_SuperTenant_SecondaryToPrimary() throws Exception {
        verifyThumbnailUsesOriginalProviderPath("publisher", "WSO2.COM/admin");
    }

    @Test
    public void testThumbnail_SuperTenant_SecondaryToSecondary() throws Exception {
        verifyThumbnailUsesOriginalProviderPath("WSO2.COM/temp", "WSO2.COM/admin");
    }

    // Tenant — no email — pre-fix and post-fix
    @Test
    public void testThumbnail_Tenant_PrimaryToPrimary_RawAt() throws Exception {
        verifyThumbnailUsesOriginalProviderPath("publisher@abc.com", "admin-AT-abc.com");
    }

    @Test
    public void testThumbnail_Tenant_PrimaryToPrimary_Encoded() throws Exception {
        verifyThumbnailUsesOriginalProviderPath("publisher-AT-abc.com", "admin-AT-abc.com");
    }

    @Test
    public void testThumbnail_Tenant_SecondaryToSecondary_RawAt() throws Exception {
        verifyThumbnailUsesOriginalProviderPath("WSO2.COM/temp@abc.com", "WSO2.COM/admin-AT-abc.com");
    }

    @Test
    public void testThumbnail_Tenant_SecondaryToSecondary_Encoded() throws Exception {
        verifyThumbnailUsesOriginalProviderPath("WSO2.COM/temp-AT-abc.com", "WSO2.COM/admin-AT-abc.com");
    }

    // Super tenant — email — pre-fix and post-fix
    @Test
    public void testThumbnail_SuperTenantEmail_PrimaryToPrimary_RawAt() throws Exception {
        verifyThumbnailUsesOriginalProviderPath("publisher@gmail.com", "user-AT-gmail.com");
    }

    @Test
    public void testThumbnail_SuperTenantEmail_PrimaryToPrimary_Encoded() throws Exception {
        verifyThumbnailUsesOriginalProviderPath("publisher-AT-gmail.com", "user-AT-gmail.com");
    }

    @Test
    public void testThumbnail_SuperTenantEmail_SecondaryToSecondary_RawAt() throws Exception {
        verifyThumbnailUsesOriginalProviderPath("WSO2.COM/temp@gmail.com", "WSO2.COM/user-AT-gmail.com");
    }

    @Test
    public void testThumbnail_SuperTenantEmail_SecondaryToSecondary_Encoded() throws Exception {
        verifyThumbnailUsesOriginalProviderPath("WSO2.COM/temp-AT-gmail.com", "WSO2.COM/user-AT-gmail.com");
    }

    // Tenant — email — pre-fix and post-fix
    @Test
    public void testThumbnail_TenantEmail_PrimaryToPrimary_RawAt() throws Exception {
        verifyThumbnailUsesOriginalProviderPath("publisher@gmail.com@abc.com",
                "user-AT-gmail.com-AT-abc.com");
    }

    @Test
    public void testThumbnail_TenantEmail_PrimaryToPrimary_Encoded() throws Exception {
        verifyThumbnailUsesOriginalProviderPath("publisher-AT-gmail.com-AT-abc.com",
                "user-AT-gmail.com-AT-abc.com");
    }

    @Test
    public void testThumbnail_TenantEmail_SecondaryToSecondary_RawAt() throws Exception {
        verifyThumbnailUsesOriginalProviderPath("WSO2.COM/temp@gmail.com@abc.com",
                "WSO2.COM/user-AT-gmail.com-AT-abc.com");
    }

    @Test
    public void testThumbnail_TenantEmail_SecondaryToSecondary_Encoded() throws Exception {
        verifyThumbnailUsesOriginalProviderPath("WSO2.COM/temp-AT-gmail.com-AT-abc.com",
                "WSO2.COM/user-AT-gmail.com-AT-abc.com");
    }

    // =====================================================================
    // Publisher API — standalone tests per domain
    // (Requires specific registry mock setup, tested separately)
    // =====================================================================

    // Super tenant — no email
    @Test
    public void testPublisherAPI_SuperTenant_PrimaryToPrimary() throws Exception {
        verifyPublisherAPILoadsAfterProviderChange("publisher", "admin");
    }

    @Test
    public void testPublisherAPI_SuperTenant_SecondaryToSecondary() throws Exception {
        verifyPublisherAPILoadsAfterProviderChange("WSO2.COM/temp", "WSO2.COM/admin");
    }

    // Tenant — no email — pre-fix and post-fix
    @Test
    public void testPublisherAPI_Tenant_PrimaryToPrimary_RawAt() throws Exception {
        verifyPublisherAPILoadsAfterProviderChange("publisher@abc.com", "admin-AT-abc.com");
    }

    @Test
    public void testPublisherAPI_Tenant_PrimaryToPrimary_Encoded() throws Exception {
        verifyPublisherAPILoadsAfterProviderChange("publisher-AT-abc.com", "admin-AT-abc.com");
    }

    // Super tenant — email — pre-fix and post-fix
    @Test
    public void testPublisherAPI_SuperTenantEmail_PrimaryToPrimary_RawAt() throws Exception {
        verifyPublisherAPILoadsAfterProviderChange("publisher@gmail.com", "user-AT-gmail.com");
    }

    @Test
    public void testPublisherAPI_SuperTenantEmail_PrimaryToPrimary_Encoded() throws Exception {
        verifyPublisherAPILoadsAfterProviderChange("publisher-AT-gmail.com", "user-AT-gmail.com");
    }

    // Tenant — email — pre-fix and post-fix
    @Test
    public void testPublisherAPI_TenantEmail_PrimaryToPrimary_RawAt() throws Exception {
        verifyPublisherAPILoadsAfterProviderChange("publisher@gmail.com@abc.com",
                "user-AT-gmail.com-AT-abc.com");
    }

    @Test
    public void testPublisherAPI_TenantEmail_PrimaryToPrimary_Encoded() throws Exception {
        verifyPublisherAPILoadsAfterProviderChange("publisher-AT-gmail.com-AT-abc.com",
                "user-AT-gmail.com-AT-abc.com");
    }

    // =====================================================================
    // changeApiProvider — verify @ is encoded to -AT- when stored
    // =====================================================================

    /**
     * Verifies that changeApiProvider() encodes @ to -AT- in the artifact attribute.
     */
    private void verifyChangeApiProviderEncodesAtSign(String rawNewProvider, String expectedEncodedProvider)
            throws Exception {
        Registry registry = Mockito.mock(UserRegistry.class);
        GenericArtifact artifact = PersistenceHelper.getSampleAPIArtifact();
        String apiUUID = artifact.getId();

        PowerMockito.mockStatic(RegistryPersistenceUtil.class);
        GenericArtifactManager manager = Mockito.mock(GenericArtifactManager.class);
        PowerMockito.when(RegistryPersistenceUtil.getArtifactManager(registry, APIConstants.API_KEY))
                .thenReturn(manager);
        PowerMockito.when(RegistryPersistenceUtil.replaceEmailDomain(anyString())).thenCallRealMethod();
        Mockito.when(manager.getGenericArtifact(apiUUID)).thenReturn(artifact);

        Mockito.when(GovernanceUtils.getArtifactPath(registry, apiUUID)).thenReturn(artifact.getPath());
        Resource apiResource = new ResourceImpl();
        Mockito.when(registry.get(anyString())).thenReturn(apiResource);

        APIPersistence apiPersistenceInstance = new RegistryPersistenceImplWrapper(registry, artifact);
        apiPersistenceInstance.changeApiProvider(rawNewProvider, apiUUID, SUPER_TENANT_DOMAIN);

        // Verify the artifact's provider attribute is encoded
        String storedProvider = artifact.getAttribute(APIConstants.API_OVERVIEW_PROVIDER);
        Assert.assertEquals("Provider should be encoded with -AT- when stored in artifact",
                expectedEncodedProvider, storedProvider);
    }

    @Test
    public void testChangeApiProvider_EncodesAtSign_SimpleUser() throws Exception {
        // No @ in provider — should remain unchanged
        verifyChangeApiProviderEncodesAtSign("publisher", "publisher");
    }

    @Test
    public void testChangeApiProvider_EncodesAtSign_TenantUser() throws Exception {
        // publisher@abc.com → publisher-AT-abc.com
        verifyChangeApiProviderEncodesAtSign("publisher@abc.com", "publisher-AT-abc.com");
    }

    @Test
    public void testChangeApiProvider_EncodesAtSign_EmailDomainUser() throws Exception {
        // user@gmail.com → user-AT-gmail.com
        verifyChangeApiProviderEncodesAtSign("user@gmail.com", "user-AT-gmail.com");
    }

    @Test
    public void testChangeApiProvider_EncodesAtSign_EmailTenantUser() throws Exception {
        // user@gmail.com@abc.com → user-AT-gmail.com-AT-abc.com
        verifyChangeApiProviderEncodesAtSign("user@gmail.com@abc.com",
                "user-AT-gmail.com-AT-abc.com");
    }

    @Test
    public void testChangeApiProvider_EncodesAtSign_SecondaryUserstoreUser() throws Exception {
        // WSO2.COM/publisher@abc.com → WSO2.COM/publisher-AT-abc.com
        verifyChangeApiProviderEncodesAtSign("WSO2.COM/publisher@abc.com",
                "WSO2.COM/publisher-AT-abc.com");
    }

    @Test
    public void testChangeApiProvider_EncodesAtSign_AlreadyEncoded() throws Exception {
        // Already encoded — should remain unchanged (idempotent)
        verifyChangeApiProviderEncodesAtSign("publisher-AT-abc.com", "publisher-AT-abc.com");
    }
}
