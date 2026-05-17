/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
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

package org.wso2.carbon.apimgt.gateway;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.identity.CredentialUnavailableException;
import com.azure.identity.WorkloadIdentityCredential;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.Collections;

/**
 * Unit tests for {@link AzureUmiTokenProvider}.
 * <p>
 * getAccessToken() tests inject a mock {@link WorkloadIdentityCredential} directly via
 * reflection so that Azure SDK internals and AKS environment variables are not required.
 */
public class AzureUmiTokenProviderTest {

    private AzureUmiTokenProvider provider;
    private WorkloadIdentityCredential mockCredential;

    @SuppressWarnings("unchecked")
    private Mono<AccessToken> mockMono;

    @Before
    public void setUp() throws Exception {
        provider = new AzureUmiTokenProvider();
        mockCredential = Mockito.mock(WorkloadIdentityCredential.class);
        mockMono = Mockito.mock(Mono.class);

        // Inject mock credential and a pre-built TokenRequestContext to bypass init()
        setField("credential", mockCredential);
        setField("tokenRequestContext",
                new TokenRequestContext().addScopes("https://ai.azure.com/.default"));

        Mockito.when(mockCredential.getToken(Mockito.any(TokenRequestContext.class)))
                .thenReturn(mockMono);
    }

    // -------------------------------------------------------------------------
    // getType()
    // -------------------------------------------------------------------------

    @Test
    public void testGetType_returnsAzureUmiConstant() {
        Assert.assertEquals(APIConstants.AI.AZURE_UMI_TOKEN_PROVIDER_TYPE, provider.getType());
    }

    // -------------------------------------------------------------------------
    // getAccessToken() — happy path
    // -------------------------------------------------------------------------

    @Test
    public void testGetAccessToken_success_returnsTokenString() throws APIManagementException {
        AccessToken mockToken = Mockito.mock(AccessToken.class);
        Mockito.when(mockToken.getToken()).thenReturn("mock-access-token-abc123");
        Mockito.when(mockMono.block(Mockito.any(Duration.class))).thenReturn(mockToken);

        String result = provider.getAccessToken();

        Assert.assertEquals("mock-access-token-abc123", result);
    }

    // -------------------------------------------------------------------------
    // getAccessToken() — failure paths
    // -------------------------------------------------------------------------

    @Test
    public void testGetAccessToken_nullTokenFromSDK_throwsAPIManagementException() {
        Mockito.when(mockMono.block(Mockito.any(Duration.class))).thenReturn(null);

        try {
            provider.getAccessToken();
            Assert.fail("Expected APIManagementException when SDK returns null token");
        } catch (APIManagementException e) {
            Assert.assertTrue("Exception message should mention null",
                    e.getMessage().contains("null"));
        }
    }

    @Test
    public void testGetAccessToken_credentialUnavailable_throwsAPIManagementException() {
        Mockito.when(mockMono.block(Mockito.any(Duration.class)))
                .thenThrow(new CredentialUnavailableException(
                        "Workload identity webhook not configured", null));

        try {
            provider.getAccessToken();
            Assert.fail("Expected APIManagementException for CredentialUnavailableException");
        } catch (APIManagementException e) {
            Assert.assertTrue("Exception message should indicate credential unavailable",
                    e.getMessage().contains("workload identity credential is unavailable"));
        }
    }

    @Test
    public void testGetAccessToken_clientAuthenticationFails_throwsAPIManagementException() {
        Mockito.when(mockMono.block(Mockito.any(Duration.class)))
                .thenThrow(new ClientAuthenticationException(
                        "Invalid client credentials", null));

        try {
            provider.getAccessToken();
            Assert.fail("Expected APIManagementException for ClientAuthenticationException");
        } catch (APIManagementException e) {
            Assert.assertTrue("Exception message should indicate authentication failure",
                    e.getMessage().contains("authentication failed"));
        }
    }

    @Test
    public void testGetAccessToken_timeout_throwsAPIManagementException() {
        Mockito.when(mockMono.block(Mockito.any(Duration.class)))
                .thenThrow(new RuntimeException("Timeout acquiring token"));

        try {
            provider.getAccessToken();
            Assert.fail("Expected APIManagementException for RuntimeException");
        } catch (APIManagementException e) {
            Assert.assertTrue("Exception message should indicate token acquisition failure",
                    e.getMessage().contains("token acquisition failed"));
        }
    }

    // -------------------------------------------------------------------------
    // init() — env var validation
    // -------------------------------------------------------------------------

    /**
     * Verifies that init() throws when AZURE_TENANT_ID is absent.
     * AKS Workload Identity env vars are never present in a CI environment;
     * the test is skipped locally if the var happens to be set.
     */
    @Test
    public void testInit_missingTenantId_throwsAPIManagementException() {
        Assume.assumeTrue(
                "Skipped: AZURE_TENANT_ID is set in this environment",
                System.getenv(APIConstants.AI.AZURE_UMI_ENV_TENANT_ID) == null);

        AzureUmiTokenProvider fresh = new AzureUmiTokenProvider();
        try {
            fresh.init(Collections.singletonMap(
                    APIConstants.AI.AZURE_UMI_SCOPE_KEY, "https://ai.azure.com/.default"));
            Assert.fail("Expected APIManagementException for missing AZURE_TENANT_ID");
        } catch (APIManagementException e) {
            Assert.assertTrue("Exception message should name the missing env var",
                    e.getMessage().contains(APIConstants.AI.AZURE_UMI_ENV_TENANT_ID));
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void setField(String name, Object value) throws Exception {
        Field field = AzureUmiTokenProvider.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(provider, value);
    }
}