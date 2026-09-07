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
package org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIEndpointDTO;
import org.wso2.carbon.core.util.CryptoUtil;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the GCP service-account key handling in
 * {@link PublisherCommonUtils#encryptEndpointSecurityGCPServiceAccountKey}. The focus is the keyless path
 * (no service-account key configured), which must be accepted - the gateway then uses its attached GCP
 * identity - rather than rejected, while a supplied key is still encrypted and an existing key preserved.
 */
public class PublisherCommonUtilsGCPTest {

    private static final String PRODUCTION = APIConstants.OAuthConstants.ENDPOINT_SECURITY_PRODUCTION;
    private static final String SANDBOX = APIConstants.OAuthConstants.ENDPOINT_SECURITY_SANDBOX;
    private static final String GCP_KEY = APIConstants.ENDPOINT_SECURITY_GCP_SERVICE_ACCOUNT_KEY;

    private CryptoUtil cryptoUtil;

    @Before
    public void setUp() throws Exception {

        cryptoUtil = mock(CryptoUtil.class);
        when(cryptoUtil.encryptAndBase64EncodeAnySize(any(byte[].class))).thenReturn("ENCRYPTED");
    }

    // -------------------------------------------------------------------------
    // Map-based overload (create-API flow)
    // -------------------------------------------------------------------------

    @Test
    public void testProductionKeyIsEncrypted() throws Exception {

        Map<String, Object> endpointConfig = gcpEndpointConfig(PRODUCTION, "{\"type\":\"service_account\"}");

        PublisherCommonUtils.encryptEndpointSecurityGCPServiceAccountKey(
                endpointConfig, cryptoUtil, null, null, new APIDTO());

        Assert.assertEquals("ENCRYPTED", securityBlock(endpointConfig, PRODUCTION).get(GCP_KEY));
        verify(cryptoUtil).encryptAndBase64EncodeAnySize(any(byte[].class));
    }

    @Test
    public void testKeylessProductionStoresNoKeyAndDoesNotThrow() throws Exception {

        Map<String, Object> endpointConfig = gcpEndpointConfig(PRODUCTION, null);

        PublisherCommonUtils.encryptEndpointSecurityGCPServiceAccountKey(
                endpointConfig, cryptoUtil, null, null, new APIDTO());

        Assert.assertFalse("Keyless GCP auth must not persist a service-account key",
                securityBlock(endpointConfig, PRODUCTION).containsKey(GCP_KEY));
        verify(cryptoUtil, never()).encryptAndBase64EncodeAnySize(any(byte[].class));
    }

    @Test
    public void testKeylessSandboxStoresNoKeyAndDoesNotThrow() throws Exception {

        Map<String, Object> endpointConfig = gcpEndpointConfig(SANDBOX, null);

        PublisherCommonUtils.encryptEndpointSecurityGCPServiceAccountKey(
                endpointConfig, cryptoUtil, null, null, new APIDTO());

        Assert.assertFalse(securityBlock(endpointConfig, SANDBOX).containsKey(GCP_KEY));
        verify(cryptoUtil, never()).encryptAndBase64EncodeAnySize(any(byte[].class));
    }

    @Test
    public void testExistingProductionKeyIsPreservedWhenNoNewKeySupplied() throws Exception {

        Map<String, Object> endpointConfig = gcpEndpointConfig(PRODUCTION, null);

        PublisherCommonUtils.encryptEndpointSecurityGCPServiceAccountKey(
                endpointConfig, cryptoUtil, "OLD_ENCRYPTED", null, new APIDTO());

        Assert.assertEquals("OLD_ENCRYPTED", securityBlock(endpointConfig, PRODUCTION).get(GCP_KEY));
        verify(cryptoUtil, never()).encryptAndBase64EncodeAnySize(any(byte[].class));
    }

    @Test(expected = APIManagementException.class)
    public void testInvalidJsonKeyIsRejectedAtSave() throws Exception {

        // A newly-supplied key that is not valid JSON must be rejected at save time (before it is encrypted),
        // rather than deploying successfully and failing when the gateway mediator later parses it.
        Map<String, Object> endpointConfig = gcpEndpointConfig(PRODUCTION, "{ invalid json");

        PublisherCommonUtils.encryptEndpointSecurityGCPServiceAccountKey(
                endpointConfig, cryptoUtil, null, null, new APIDTO());
    }

    // -------------------------------------------------------------------------
    // APIEndpointDTO-based overload (add/update-endpoint flow)
    // -------------------------------------------------------------------------

    @Test
    public void testEndpointDtoKeylessDoesNotThrow() throws Exception {

        Map<String, Object> endpointConfig = gcpEndpointConfig(PRODUCTION, null);
        APIEndpointDTO apiEndpointDTO =
                new APIEndpointDTO().deploymentStage(APIConstants.APIEndpoint.PRODUCTION);

        PublisherCommonUtils.encryptEndpointSecurityGCPServiceAccountKey(
                apiEndpointDTO, cryptoUtil, "", endpointConfig);

        Assert.assertFalse(securityBlock(endpointConfig, PRODUCTION).containsKey(GCP_KEY));
        verify(cryptoUtil, never()).encryptAndBase64EncodeAnySize(any(byte[].class));
    }

    // -------------------------------------------------------------------------
    // Clear-flag (switch an existing key-based endpoint to keyless)
    // -------------------------------------------------------------------------

    @Test
    public void testProductionKeyClearedWhenClearFlagSet() throws Exception {

        Map<String, Object> endpointConfig = gcpEndpointConfig(PRODUCTION, null);
        securityBlock(endpointConfig, PRODUCTION)
                .put(APIConstants.ENDPOINT_SECURITY_GCP_SERVICE_ACCOUNT_KEY_CLEAR, true);

        PublisherCommonUtils.encryptEndpointSecurityGCPServiceAccountKey(
                endpointConfig, cryptoUtil, "OLD_ENCRYPTED", null, new APIDTO());

        Assert.assertFalse("Clear flag must drop the stored production key so the gateway falls back to keyless",
                securityBlock(endpointConfig, PRODUCTION).containsKey(GCP_KEY));
        Assert.assertFalse("The transient clear flag must never be persisted",
                securityBlock(endpointConfig, PRODUCTION)
                        .containsKey(APIConstants.ENDPOINT_SECURITY_GCP_SERVICE_ACCOUNT_KEY_CLEAR));
        verify(cryptoUtil, never()).encryptAndBase64EncodeAnySize(any(byte[].class));
    }

    @Test
    public void testSandboxKeyClearedWhenClearFlagSet() throws Exception {

        Map<String, Object> endpointConfig = gcpEndpointConfig(SANDBOX, null);
        securityBlock(endpointConfig, SANDBOX)
                .put(APIConstants.ENDPOINT_SECURITY_GCP_SERVICE_ACCOUNT_KEY_CLEAR, true);

        PublisherCommonUtils.encryptEndpointSecurityGCPServiceAccountKey(
                endpointConfig, cryptoUtil, null, "OLD_ENCRYPTED", new APIDTO());

        Assert.assertFalse("Clear flag must drop the stored sandbox key so the gateway falls back to keyless",
                securityBlock(endpointConfig, SANDBOX).containsKey(GCP_KEY));
        Assert.assertFalse("The transient clear flag must never be persisted",
                securityBlock(endpointConfig, SANDBOX)
                        .containsKey(APIConstants.ENDPOINT_SECURITY_GCP_SERVICE_ACCOUNT_KEY_CLEAR));
        verify(cryptoUtil, never()).encryptAndBase64EncodeAnySize(any(byte[].class));
    }

    @Test
    public void testExistingProductionKeyPreservedWhenClearFlagFalse() throws Exception {

        // Regression guard: a false/absent clear flag must keep the existing "unchanged => preserve" behaviour.
        Map<String, Object> endpointConfig = gcpEndpointConfig(PRODUCTION, null);
        securityBlock(endpointConfig, PRODUCTION)
                .put(APIConstants.ENDPOINT_SECURITY_GCP_SERVICE_ACCOUNT_KEY_CLEAR, false);

        PublisherCommonUtils.encryptEndpointSecurityGCPServiceAccountKey(
                endpointConfig, cryptoUtil, "OLD_ENCRYPTED", null, new APIDTO());

        Assert.assertEquals("Without a clear flag an existing key must be preserved",
                "OLD_ENCRYPTED", securityBlock(endpointConfig, PRODUCTION).get(GCP_KEY));
        Assert.assertFalse("The transient clear flag must never be persisted",
                securityBlock(endpointConfig, PRODUCTION)
                        .containsKey(APIConstants.ENDPOINT_SECURITY_GCP_SERVICE_ACCOUNT_KEY_CLEAR));
        verify(cryptoUtil, never()).encryptAndBase64EncodeAnySize(any(byte[].class));
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static Map<String, Object> gcpEndpointConfig(String envKey, String serviceAccountKey) {

        Map<String, Object> security = new HashMap<>();
        security.put(APIConstants.OAuthConstants.ENDPOINT_SECURITY_TYPE, APIConstants.ENDPOINT_SECURITY_TYPE_GCP);
        if (serviceAccountKey != null) {
            security.put(GCP_KEY, serviceAccountKey);
        }
        Map<String, Object> endpointSecurity = new HashMap<>();
        endpointSecurity.put(envKey, security);
        Map<String, Object> endpointConfig = new HashMap<>();
        endpointConfig.put(APIConstants.ENDPOINT_SECURITY, endpointSecurity);
        return endpointConfig;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> securityBlock(Map<String, Object> endpointConfig, String envKey) {

        Map<String, Object> endpointSecurity =
                (Map<String, Object>) endpointConfig.get(APIConstants.ENDPOINT_SECURITY);
        return (Map<String, Object>) endpointSecurity.get(envKey);
    }
}
