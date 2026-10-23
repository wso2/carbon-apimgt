/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.gateway.internal;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.wso2.carbon.apimgt.api.gateway.GatewayAPIDTO;
import org.wso2.carbon.apimgt.keymgt.model.entity.API;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Tests for tracking the deployed state of APIs in {@link DataHolder}, including APIs that get deployed before
 * their metadata is loaded (e.g. APIs of eager loaded tenants) and APIs whose metadata is reloaded.
 */
public class DataHolderTest {

    private static final String TENANT_A = "tenant1.example.com";
    private static final String TENANT_B = "tenant2.example.com";
    private static final String API_NAME = "PetStore";
    private static final String API_VERSION = "1.0.0";
    private static final String CONTEXT_A = "/t/tenant1.example.com/petstore/1.0.0";
    private static final String DEFAULT_CONTEXT_A = "/t/tenant1.example.com/petstore";
    private static final String CONTEXT_B = "/t/tenant2.example.com/petstore/1.0.0";

    private DataHolder dataHolder;

    @Before
    public void init() throws Exception {
        // Use a fresh instance per test since DataHolder is a singleton
        Constructor<DataHolder> constructor = DataHolder.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        dataHolder = constructor.newInstance();
    }

    @Test
    public void testMarkAPIAsDeployedWhenMetadataIsAvailable() {
        dataHolder.addAPIMetaData(createAPI(TENANT_A, CONTEXT_A, false));
        Assert.assertFalse(dataHolder.isDeployed(createGatewayAPIDTO(TENANT_A, CONTEXT_A)));

        dataHolder.markAPIAsDeployed(createGatewayAPIDTO(TENANT_A, CONTEXT_A, "revision-1"));

        API api = getAPI(TENANT_A, CONTEXT_A);
        Assert.assertTrue(api.isDeployed());
        Assert.assertEquals("revision-1", api.getRevisionId());
        Assert.assertNull(getDeploymentsBeforeMetadata(TENANT_A));
    }

    @Test
    public void testAPIWithoutDeploymentIsNotMarkedAsDeployed() {
        dataHolder.addAPIMetaData(createAPI(TENANT_A, CONTEXT_A, false));

        Assert.assertFalse(dataHolder.isDeployed(createGatewayAPIDTO(TENANT_A, CONTEXT_A)));
        Assert.assertFalse(getAPI(TENANT_A, CONTEXT_A).isDeployed());
    }

    @Test
    public void testAPIDeployedBeforeMetadataIsMarkedAsDeployedOnceMetadataIsLoaded() {
        // API gets deployed (e.g. eager tenant loading at startup) before the API metadata is loaded
        dataHolder.markAPIAsDeployed(createGatewayAPIDTO(TENANT_A, CONTEXT_A, "revision-1"));
        Assert.assertNotNull(getDeploymentsBeforeMetadata(TENANT_A));

        dataHolder.addAPIMetaData(createAPI(TENANT_A, CONTEXT_A, false));

        API api = getAPI(TENANT_A, CONTEXT_A);
        Assert.assertTrue(api.isDeployed());
        Assert.assertEquals("revision-1", api.getRevisionId());
        // The tracked deployment is consumed and the empty per tenant map is dropped
        Assert.assertNull(getDeploymentsBeforeMetadata(TENANT_A));
    }

    @Test
    public void testAPIDeployedBeforeMetadataIsReportedAsDeployed() {
        // Used by the tenant loading (and retry) flow to skip APIs that are already deployed
        dataHolder.markAPIAsDeployed(createGatewayAPIDTO(TENANT_A, CONTEXT_A));

        Assert.assertTrue(dataHolder.isDeployed(createGatewayAPIDTO(TENANT_A, CONTEXT_A)));
        Assert.assertFalse(dataHolder.isDeployed(createGatewayAPIDTO(TENANT_B, CONTEXT_A)));
    }

    @Test
    public void testRevisionRuleIsSameWithAndWithoutMetadata() {
        // A deployment without a revision clears the revision, whether or not the metadata was loaded first
        API loadedFirst = createAPI(TENANT_A, CONTEXT_A, false);
        loadedFirst.setRevisionId("stale-revision");
        dataHolder.addAPIMetaData(loadedFirst);
        dataHolder.markAPIAsDeployed(createGatewayAPIDTO(TENANT_A, CONTEXT_A));

        dataHolder.markAPIAsDeployed(createGatewayAPIDTO(TENANT_B, CONTEXT_B));
        API loadedLater = createAPI(TENANT_B, CONTEXT_B, false);
        loadedLater.setRevisionId("stale-revision");
        dataHolder.addAPIMetaData(loadedLater);

        Assert.assertTrue(getAPI(TENANT_A, CONTEXT_A).isDeployed());
        Assert.assertNull(getAPI(TENANT_A, CONTEXT_A).getRevisionId());
        Assert.assertTrue(getAPI(TENANT_B, CONTEXT_B).isDeployed());
        Assert.assertNull(getAPI(TENANT_B, CONTEXT_B).getRevisionId());
    }

    @Test
    public void testDefaultVersionAPIDeployedBeforeMetadataIsMarkedAsDeployedForBothContexts() {
        dataHolder.markAPIAsDeployed(createGatewayAPIDTO(TENANT_A, CONTEXT_A));

        dataHolder.addAPIMetaData(createAPI(TENANT_A, CONTEXT_A, true));

        Assert.assertTrue(getAPI(TENANT_A, CONTEXT_A).isDeployed());
        Assert.assertTrue(getAPI(TENANT_A, DEFAULT_CONTEXT_A).isDeployed());
    }

    @Test
    public void testReloadedMetadataKeepsDeployedStateAndRevision() {
        // e.g. an API_UPDATE event replaces the metadata of an API that is already deployed
        dataHolder.addAPIMetaData(createAPI(TENANT_A, CONTEXT_A, false));
        dataHolder.markAPIAsDeployed(createGatewayAPIDTO(TENANT_A, CONTEXT_A, "revision-1"));

        API reloaded = createAPI(TENANT_A, CONTEXT_A, false);
        dataHolder.addAPIMetaData(reloaded);

        Assert.assertSame(reloaded, getAPI(TENANT_A, CONTEXT_A));
        Assert.assertTrue(reloaded.isDeployed());
        Assert.assertEquals("revision-1", reloaded.getRevisionId());
    }

    @Test
    public void testReloadedMetadataOfUndeployedAPIIsNotMarkedAsDeployed() {
        dataHolder.addAPIMetaData(createAPI(TENANT_A, CONTEXT_A, false));

        dataHolder.addAPIMetaData(createAPI(TENANT_A, CONTEXT_A, false));

        Assert.assertFalse(dataHolder.isDeployed(createGatewayAPIDTO(TENANT_A, CONTEXT_A)));
    }

    @Test
    public void testDeploymentBeforeMetadataIsTrackedWhenOnDemandLoadingIsDisabled() {
        // Tracking does not depend on the on demand loading configuration. The deployed flag is accurate either way.
        dataHolder.markAPIAsDeployed(createGatewayAPIDTO(TENANT_A, CONTEXT_A));

        dataHolder.addAPIMetaData(createAPI(TENANT_A, CONTEXT_A, false));

        Assert.assertTrue(dataHolder.isDeployed(createGatewayAPIDTO(TENANT_A, CONTEXT_A)));
    }

    @Test
    public void testAPIUndeployedBeforeMetadataIsNotMarkedAsDeployed() {
        dataHolder.markAPIAsDeployed(createGatewayAPIDTO(TENANT_A, CONTEXT_A));
        dataHolder.removeAPIFromAllTenantMap(CONTEXT_A, TENANT_A);
        Assert.assertNull(getDeploymentsBeforeMetadata(TENANT_A));

        dataHolder.addAPIMetaData(createAPI(TENANT_A, CONTEXT_A, false));

        Assert.assertFalse(dataHolder.isDeployed(createGatewayAPIDTO(TENANT_A, CONTEXT_A)));
    }

    @Test
    public void testTenantUnloadedBeforeMetadataIsNotMarkedAsDeployed() {
        dataHolder.markAPIAsDeployed(createGatewayAPIDTO(TENANT_A, CONTEXT_A));
        dataHolder.markApisAsUnDeployedInTenant(TENANT_A);
        Assert.assertNull(getDeploymentsBeforeMetadata(TENANT_A));

        dataHolder.addAPIMetaData(createAPI(TENANT_A, CONTEXT_A, false));

        Assert.assertFalse(dataHolder.isDeployed(createGatewayAPIDTO(TENANT_A, CONTEXT_A)));
    }

    @Test
    public void testAPIUndeployedBeforeMetadataDoesNotAffectOtherAPIsOfTenant() {
        String otherContext = "/t/tenant1.example.com/orders/1.0.0";
        dataHolder.markAPIAsDeployed(createGatewayAPIDTO(TENANT_A, CONTEXT_A));
        dataHolder.markAPIAsDeployed(createGatewayAPIDTO(TENANT_A, otherContext));
        dataHolder.removeAPIFromAllTenantMap(CONTEXT_A, TENANT_A);

        dataHolder.addAPIMetaData(createAPI(TENANT_A, CONTEXT_A, false));
        dataHolder.addAPIMetaData(createAPI(TENANT_A, otherContext, false));

        Assert.assertFalse(dataHolder.isDeployed(createGatewayAPIDTO(TENANT_A, CONTEXT_A)));
        Assert.assertTrue(dataHolder.isDeployed(createGatewayAPIDTO(TENANT_A, otherContext)));
        Assert.assertNull(getDeploymentsBeforeMetadata(TENANT_A));
    }

    @Test
    public void testTenantUnloadedBeforeMetadataDoesNotAffectOtherTenants() {
        dataHolder.markAPIAsDeployed(createGatewayAPIDTO(TENANT_A, CONTEXT_A));
        dataHolder.markAPIAsDeployed(createGatewayAPIDTO(TENANT_B, CONTEXT_B));
        dataHolder.markApisAsUnDeployedInTenant(TENANT_A);

        dataHolder.addAPIMetaData(createAPI(TENANT_A, CONTEXT_A, false));
        dataHolder.addAPIMetaData(createAPI(TENANT_B, CONTEXT_B, false));

        Assert.assertFalse(dataHolder.isDeployed(createGatewayAPIDTO(TENANT_A, CONTEXT_A)));
        Assert.assertTrue(dataHolder.isDeployed(createGatewayAPIDTO(TENANT_B, CONTEXT_B)));
    }

    @Test
    public void testDeploymentBeforeMetadataIsAppliedOnlyOnce() {
        dataHolder.markAPIAsDeployed(createGatewayAPIDTO(TENANT_A, CONTEXT_A));
        dataHolder.addAPIMetaData(createAPI(TENANT_A, CONTEXT_A, false));
        Assert.assertTrue(dataHolder.isDeployed(createGatewayAPIDTO(TENANT_A, CONTEXT_A)));

        // Tenant gets unloaded and the metadata of the API is reloaded later
        dataHolder.markApisAsUnDeployedInTenant(TENANT_A);
        dataHolder.addAPIMetaData(createAPI(TENANT_A, CONTEXT_A, false));

        Assert.assertFalse(dataHolder.isDeployed(createGatewayAPIDTO(TENANT_A, CONTEXT_A)));
    }

    @Test
    public void testDeploymentBeforeMetadataIsTenantScoped() {
        dataHolder.markAPIAsDeployed(createGatewayAPIDTO(TENANT_A, CONTEXT_A));

        dataHolder.addAPIMetaData(createAPI(TENANT_B, CONTEXT_A, false));

        Assert.assertFalse(dataHolder.isDeployed(createGatewayAPIDTO(TENANT_B, CONTEXT_A)));
        Assert.assertNotNull(getDeploymentsBeforeMetadata(TENANT_A));
    }

    @Test
    public void testDeploymentWithoutContextIsIgnored() {
        // Internal APIs such as the JWKS API are deployed without an API context and have no metadata
        GatewayAPIDTO jwksAPIDTO = createGatewayAPIDTO(TENANT_A, null);
        jwksAPIDTO.setName("_JwksEndpoint_");

        dataHolder.markAPIAsDeployed(jwksAPIDTO);

        Assert.assertFalse(dataHolder.isDeployed(jwksAPIDTO));
        Assert.assertNull(getDeploymentsBeforeMetadata(TENANT_A));
    }

    @Test
    public void testDeploymentWithoutTenantDomainIsIgnored() {
        GatewayAPIDTO gatewayAPIDTO = createGatewayAPIDTO(null, CONTEXT_A);

        dataHolder.markAPIAsDeployed(gatewayAPIDTO);
        dataHolder.addAPIMetaData(createAPI(TENANT_A, CONTEXT_A, false));

        Assert.assertFalse(dataHolder.isDeployed(gatewayAPIDTO));
        Assert.assertFalse(dataHolder.isDeployed(createGatewayAPIDTO(TENANT_A, CONTEXT_A)));
    }

    @Test
    public void testAPIsDeployedAfterInternalAPIAreMarkedAsDeployedOnceMetadataIsLoaded() {
        // Startup deployment of a tenant deploys the JWKS API first and then the APIs of the tenant
        GatewayAPIDTO jwksAPIDTO = createGatewayAPIDTO(TENANT_A, null);
        jwksAPIDTO.setName("_JwksEndpoint_");
        dataHolder.markAPIAsDeployed(jwksAPIDTO);
        dataHolder.markAPIAsDeployed(createGatewayAPIDTO(TENANT_A, CONTEXT_A));

        dataHolder.addAPIMetaData(createAPI(TENANT_A, CONTEXT_A, false));

        Assert.assertTrue(dataHolder.isDeployed(createGatewayAPIDTO(TENANT_A, CONTEXT_A)));
    }

    @Test
    public void testRemoveDefaultVersionAPIWithContextWithoutVersion() {
        // e.g. versionless contexts, for which addAPIMetaData does not derive a separate default context
        String context = "/t/tenant1.example.com/petstore";
        dataHolder.addAPIMetaData(createAPI(TENANT_A, context, true));

        dataHolder.removeAPIFromAllTenantMap(context, TENANT_A);

        Assert.assertNull(getAPI(TENANT_A, context));
    }

    @Test
    public void testConcurrentDeploymentsBeforeMetadataAreMarkedAsDeployed() throws Exception {
        int apiCount = 200;
        List<Callable<Void>> tasks = new ArrayList<>();
        for (int i = 0; i < apiCount; i++) {
            String tenant = (i % 2 == 0) ? TENANT_A : TENANT_B;
            String context = "/t/" + tenant + "/api" + i + "/" + API_VERSION;
            tasks.add(() -> {
                dataHolder.markAPIAsDeployed(createGatewayAPIDTO(tenant, context));
                return null;
            });
        }
        runConcurrently(tasks);

        for (int i = 0; i < apiCount; i++) {
            String tenant = (i % 2 == 0) ? TENANT_A : TENANT_B;
            String context = "/t/" + tenant + "/api" + i + "/" + API_VERSION;
            dataHolder.addAPIMetaData(createAPI(tenant, context, false));
            Assert.assertTrue("API with context " + context + " should be marked as deployed",
                    dataHolder.isDeployed(createGatewayAPIDTO(tenant, context)));
        }
        Assert.assertNull(getDeploymentsBeforeMetadata(TENANT_A));
        Assert.assertNull(getDeploymentsBeforeMetadata(TENANT_B));
    }

    @Test
    public void testConcurrentDeploymentsAndMetadataLoadingMarkAllAPIsAsDeployed() throws Exception {
        // Deployments and metadata loading run in parallel, as with eager tenant loading at startup. Every API must
        // end up marked as deployed regardless of the order in which the two operations run for it.
        int apiCount = 500;
        List<Callable<Void>> tasks = new ArrayList<>();
        for (int i = 0; i < apiCount; i++) {
            String context = "/t/" + TENANT_A + "/api" + i + "/" + API_VERSION;
            tasks.add(() -> {
                dataHolder.markAPIAsDeployed(createGatewayAPIDTO(TENANT_A, context));
                return null;
            });
            tasks.add(() -> {
                dataHolder.addAPIMetaData(createAPI(TENANT_A, context, false));
                return null;
            });
        }
        runConcurrently(tasks);

        for (int i = 0; i < apiCount; i++) {
            String context = "/t/" + TENANT_A + "/api" + i + "/" + API_VERSION;
            Assert.assertTrue("API with context " + context + " should be marked as deployed",
                    getAPI(TENANT_A, context).isDeployed());
        }
        Assert.assertNull(getDeploymentsBeforeMetadata(TENANT_A));
    }

    @Test
    public void testConcurrentDeploymentsAndMetadataReloadsKeepAPIsDeployed() throws Exception {
        // Metadata of deployed APIs is reloaded (e.g. API_UPDATE) while the APIs are being marked as deployed
        int apiCount = 500;
        for (int i = 0; i < apiCount; i++) {
            dataHolder.addAPIMetaData(createAPI(TENANT_A, "/t/" + TENANT_A + "/api" + i + "/" + API_VERSION, false));
        }
        List<Callable<Void>> tasks = new ArrayList<>();
        for (int i = 0; i < apiCount; i++) {
            String context = "/t/" + TENANT_A + "/api" + i + "/" + API_VERSION;
            tasks.add(() -> {
                dataHolder.markAPIAsDeployed(createGatewayAPIDTO(TENANT_A, context));
                return null;
            });
            tasks.add(() -> {
                dataHolder.addAPIMetaData(createAPI(TENANT_A, context, false));
                return null;
            });
        }
        runConcurrently(tasks);

        for (int i = 0; i < apiCount; i++) {
            String context = "/t/" + TENANT_A + "/api" + i + "/" + API_VERSION;
            Assert.assertTrue("API with context " + context + " should be marked as deployed",
                    getAPI(TENANT_A, context).isDeployed());
        }
    }

    private void runConcurrently(List<Callable<Void>> tasks) throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(8);
        try {
            for (Future<Void> future : executorService.invokeAll(tasks)) {
                future.get();
            }
        } finally {
            executorService.shutdown();
        }
    }

    private API getAPI(String tenantDomain, String context) {
        Map<String, API> apiMap = dataHolder.getTenantAPIMap().get(tenantDomain);
        return apiMap != null ? apiMap.get(context) : null;
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> getDeploymentsBeforeMetadata(String tenantDomain) {
        try {
            Field field = DataHolder.class.getDeclaredField("deployedBeforeMetadataMap");
            field.setAccessible(true);
            return ((Map<String, Map<String, String>>) field.get(dataHolder)).get(tenantDomain);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalStateException("Unable to read the deployments tracked before metadata", e);
        }
    }

    private API createAPI(String tenantDomain, String context, boolean isDefaultVersion) {
        API api = new API();
        api.setApiName(API_NAME);
        api.setApiVersion(API_VERSION);
        api.setApiType("HTTP");
        api.setContext(context);
        api.setOrganization(tenantDomain);
        api.setDefaultVersion(isDefaultVersion);
        return api;
    }

    private GatewayAPIDTO createGatewayAPIDTO(String tenantDomain, String context) {
        return createGatewayAPIDTO(tenantDomain, context, null);
    }

    private GatewayAPIDTO createGatewayAPIDTO(String tenantDomain, String context, String revision) {
        GatewayAPIDTO gatewayAPIDTO = new GatewayAPIDTO();
        gatewayAPIDTO.setName(API_NAME);
        gatewayAPIDTO.setTenantDomain(tenantDomain);
        gatewayAPIDTO.setApiContext(context);
        gatewayAPIDTO.setRevision(revision);
        return gatewayAPIDTO;
    }
}
