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
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.wso2.carbon.apimgt.api.gateway.GatewayAPIDTO;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.apimgt.keymgt.model.entity.API;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Tests for tracking the deployed state of APIs in {@link DataHolder}, including APIs that get deployed before
 * their metadata is loaded (e.g. APIs of eager loaded tenants when on demand loading is enabled).
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({GatewayUtils.class})
public class DataHolderTest {

    private static final String TENANT_A = "tenant1.example.com";
    private static final String TENANT_B = "tenant2.example.com";
    private static final String API_NAME = "PetStore";
    private static final String API_VERSION = "1.0.0";
    private static final String CONTEXT_A = "/t/tenant1.example.com/petstore/1.0.0";
    private static final String DEFAULT_CONTEXT_A = "/t/tenant1.example.com/petstore";

    private DataHolder dataHolder;

    @Before
    public void init() throws Exception {
        PowerMockito.mockStatic(GatewayUtils.class);
        PowerMockito.when(GatewayUtils.isOnDemandLoading()).thenReturn(true);
        // Use a fresh instance per test since DataHolder is a singleton
        dataHolder = Whitebox.invokeConstructor(DataHolder.class);
    }

    @Test
    public void testMarkAPIAsDeployedWhenMetadataIsAvailable() {
        dataHolder.addAPIMetaData(createAPI(TENANT_A, CONTEXT_A, false));
        Assert.assertFalse(dataHolder.isDeployed(createGatewayAPIDTO(TENANT_A, CONTEXT_A)));

        dataHolder.markAPIAsDeployed(createGatewayAPIDTO(TENANT_A, CONTEXT_A));

        Assert.assertTrue(dataHolder.isDeployed(createGatewayAPIDTO(TENANT_A, CONTEXT_A)));
    }

    @Test
    public void testAPIWithoutDeploymentIsNotMarkedAsDeployed() {
        dataHolder.addAPIMetaData(createAPI(TENANT_A, CONTEXT_A, false));

        Assert.assertFalse(dataHolder.isDeployed(createGatewayAPIDTO(TENANT_A, CONTEXT_A)));
        Assert.assertFalse(dataHolder.getTenantAPIMap().get(TENANT_A).get(CONTEXT_A).isDeployed());
    }

    @Test
    public void testAPIDeployedBeforeMetadataIsMarkedAsDeployedOnceMetadataIsLoaded() {
        // API gets deployed (e.g. eager tenant loading at startup) before the API metadata is loaded
        dataHolder.markAPIAsDeployed(createGatewayAPIDTO(TENANT_A, CONTEXT_A));
        Assert.assertFalse(dataHolder.isDeployed(createGatewayAPIDTO(TENANT_A, CONTEXT_A)));

        dataHolder.addAPIMetaData(createAPI(TENANT_A, CONTEXT_A, false));

        Assert.assertTrue(dataHolder.isDeployed(createGatewayAPIDTO(TENANT_A, CONTEXT_A)));
        Assert.assertTrue(dataHolder.getTenantAPIMap().get(TENANT_A).get(CONTEXT_A).isDeployed());
    }

    @Test
    public void testDefaultVersionAPIDeployedBeforeMetadataIsMarkedAsDeployedForBothContexts() {
        dataHolder.markAPIAsDeployed(createGatewayAPIDTO(TENANT_A, CONTEXT_A));

        dataHolder.addAPIMetaData(createAPI(TENANT_A, CONTEXT_A, true));

        Assert.assertTrue(dataHolder.getTenantAPIMap().get(TENANT_A).get(CONTEXT_A).isDeployed());
        Assert.assertTrue(dataHolder.getTenantAPIMap().get(TENANT_A).get(DEFAULT_CONTEXT_A).isDeployed());
    }

    @Test
    public void testAPIDeployedBeforeMetadataIsNotTrackedWhenOnDemandLoadingIsDisabled() {
        PowerMockito.when(GatewayUtils.isOnDemandLoading()).thenReturn(false);
        dataHolder.markAPIAsDeployed(createGatewayAPIDTO(TENANT_A, CONTEXT_A));

        dataHolder.addAPIMetaData(createAPI(TENANT_A, CONTEXT_A, false));

        Assert.assertFalse(dataHolder.isDeployed(createGatewayAPIDTO(TENANT_A, CONTEXT_A)));
    }

    @Test
    public void testAPIUndeployedBeforeMetadataIsNotMarkedAsDeployed() {
        dataHolder.markAPIAsDeployed(createGatewayAPIDTO(TENANT_A, CONTEXT_A));
        dataHolder.removeAPIFromAllTenantMap(CONTEXT_A, TENANT_A);

        dataHolder.addAPIMetaData(createAPI(TENANT_A, CONTEXT_A, false));

        Assert.assertFalse(dataHolder.isDeployed(createGatewayAPIDTO(TENANT_A, CONTEXT_A)));
    }

    @Test
    public void testTenantUnloadedBeforeMetadataIsNotMarkedAsDeployed() {
        dataHolder.markAPIAsDeployed(createGatewayAPIDTO(TENANT_A, CONTEXT_A));
        dataHolder.markApisAsUnDeployedInTenant(TENANT_A);

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
    }

    @Test
    public void testTenantUnloadedBeforeMetadataDoesNotAffectOtherTenants() {
        String contextB = "/t/tenant2.example.com/petstore/1.0.0";
        dataHolder.markAPIAsDeployed(createGatewayAPIDTO(TENANT_A, CONTEXT_A));
        dataHolder.markAPIAsDeployed(createGatewayAPIDTO(TENANT_B, contextB));
        dataHolder.markApisAsUnDeployedInTenant(TENANT_A);

        dataHolder.addAPIMetaData(createAPI(TENANT_A, CONTEXT_A, false));
        dataHolder.addAPIMetaData(createAPI(TENANT_B, contextB, false));

        Assert.assertFalse(dataHolder.isDeployed(createGatewayAPIDTO(TENANT_A, CONTEXT_A)));
        Assert.assertTrue(dataHolder.isDeployed(createGatewayAPIDTO(TENANT_B, contextB)));
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
    }

    @Test
    public void testDeploymentWithoutContextIsIgnored() {
        // Internal APIs such as the JWKS API are deployed without an API context and have no metadata
        GatewayAPIDTO jwksAPIDTO = createGatewayAPIDTO(TENANT_A, null);
        jwksAPIDTO.setName("_JwksEndpoint_");

        dataHolder.markAPIAsDeployed(jwksAPIDTO);

        Assert.assertFalse(dataHolder.isDeployed(jwksAPIDTO));
    }

    @Test
    public void testDeploymentWithoutTenantDomainIsIgnored() {
        GatewayAPIDTO gatewayAPIDTO = createGatewayAPIDTO(null, CONTEXT_A);

        dataHolder.markAPIAsDeployed(gatewayAPIDTO);
        dataHolder.addAPIMetaData(createAPI(TENANT_A, CONTEXT_A, false));

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
    public void testConcurrentDeploymentsBeforeMetadataAreMarkedAsDeployed() throws Exception {
        int apiCount = 200;
        ExecutorService executorService = Executors.newFixedThreadPool(8);
        try {
            List<Callable<Void>> tasks = new ArrayList<>();
            for (int i = 0; i < apiCount; i++) {
                String tenant = (i % 2 == 0) ? TENANT_A : TENANT_B;
                String context = "/t/" + tenant + "/api" + i + "/" + API_VERSION;
                tasks.add(() -> {
                    dataHolder.markAPIAsDeployed(createGatewayAPIDTO(tenant, context));
                    return null;
                });
            }
            for (Future<Void> future : executorService.invokeAll(tasks)) {
                future.get();
            }
        } finally {
            executorService.shutdown();
        }

        for (int i = 0; i < apiCount; i++) {
            String tenant = (i % 2 == 0) ? TENANT_A : TENANT_B;
            String context = "/t/" + tenant + "/api" + i + "/" + API_VERSION;
            dataHolder.addAPIMetaData(createAPI(tenant, context, false));
            Assert.assertTrue("API with context " + context + " should be marked as deployed",
                    dataHolder.isDeployed(createGatewayAPIDTO(tenant, context)));
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

    @Test
    public void testRevisionOfDeploymentBeforeMetadataIsAppliedOnceMetadataIsLoaded() {
        GatewayAPIDTO gatewayAPIDTO = createGatewayAPIDTO(TENANT_A, CONTEXT_A);
        gatewayAPIDTO.setRevision("revision-1");
        dataHolder.markAPIAsDeployed(gatewayAPIDTO);

        dataHolder.addAPIMetaData(createAPI(TENANT_A, CONTEXT_A, false));

        API api = dataHolder.getTenantAPIMap().get(TENANT_A).get(CONTEXT_A);
        Assert.assertTrue(api.isDeployed());
        Assert.assertEquals("revision-1", api.getRevisionId());
    }

    @Test
    public void testDeploymentBeforeMetadataWithoutRevisionKeepsLoadedRevision() {
        dataHolder.markAPIAsDeployed(createGatewayAPIDTO(TENANT_A, CONTEXT_A));
        API api = createAPI(TENANT_A, CONTEXT_A, false);
        api.setRevisionId("revision-2");

        dataHolder.addAPIMetaData(api);

        Assert.assertTrue(dataHolder.isDeployed(createGatewayAPIDTO(TENANT_A, CONTEXT_A)));
        Assert.assertEquals("revision-2", dataHolder.getTenantAPIMap().get(TENANT_A).get(CONTEXT_A).getRevisionId());
    }

    @Test
    public void testRevisionIsUpdatedWhenMetadataIsAvailable() {
        dataHolder.addAPIMetaData(createAPI(TENANT_A, CONTEXT_A, false));
        GatewayAPIDTO gatewayAPIDTO = createGatewayAPIDTO(TENANT_A, CONTEXT_A);
        gatewayAPIDTO.setRevision("revision-3");

        dataHolder.markAPIAsDeployed(gatewayAPIDTO);

        Assert.assertEquals("revision-3", dataHolder.getTenantAPIMap().get(TENANT_A).get(CONTEXT_A).getRevisionId());
    }

    @Test
    public void testConcurrentDeploymentsAndMetadataLoadingMarkAllAPIsAsDeployed() throws Exception {
        // Deployments and metadata loading run in parallel, as with eager tenant loading at startup. Every API must
        // end up marked as deployed regardless of the order in which the two operations run for it.
        int apiCount = 500;
        ExecutorService executorService = Executors.newFixedThreadPool(8);
        try {
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
            for (Future<Void> future : executorService.invokeAll(tasks)) {
                future.get();
            }
        } finally {
            executorService.shutdown();
        }

        for (int i = 0; i < apiCount; i++) {
            String context = "/t/" + TENANT_A + "/api" + i + "/" + API_VERSION;
            Assert.assertTrue("API with context " + context + " should be marked as deployed",
                    dataHolder.isDeployed(createGatewayAPIDTO(TENANT_A, context)));
        }
    }

    private GatewayAPIDTO createGatewayAPIDTO(String tenantDomain, String context) {
        GatewayAPIDTO gatewayAPIDTO = new GatewayAPIDTO();
        gatewayAPIDTO.setName(API_NAME);
        gatewayAPIDTO.setTenantDomain(tenantDomain);
        gatewayAPIDTO.setApiContext(context);
        return gatewayAPIDTO;
    }
}
