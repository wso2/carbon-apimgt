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
import com.azure.core.http.HttpClient;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.identity.CredentialUnavailableException;
import com.azure.identity.WorkloadIdentityCredential;
import com.azure.identity.WorkloadIdentityCredentialBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ManagedIdentityTokenProvider;
import org.wso2.carbon.apimgt.impl.APIConstants;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;

/**
 * Azure SDK-based implementation of ManagedIdentityTokenProvider using WorkloadIdentityCredential.
 * This provider delegates token acquisition, caching, and refresh entirely to the Azure Identity
 * SDK. The AKS mutating webhook must inject AZURE_TENANT_ID, AZURE_CLIENT_ID, and
 * AZURE_FEDERATED_TOKEN_FILE environment variables into the pod for the credential to function.
 */
public class AzureUmiTokenProvider implements ManagedIdentityTokenProvider {

    private static final Log log = LogFactory.getLog(AzureUmiTokenProvider.class);

    private static final int TOKEN_ACQUISITION_TIMEOUT_SECONDS = 30;

    private WorkloadIdentityCredential credential;
    private TokenRequestContext tokenRequestContext;
    private HttpClient httpClient;

    @Override
    public void init(Map<String, String> properties) throws APIManagementException {
        String scope = (properties != null) ? properties.get(APIConstants.AI.AZURE_UMI_SCOPE_KEY) : null;
        if (StringUtils.isBlank(scope)) {
            throw new APIManagementException(
                    "Azure UMI: missing required property '" + APIConstants.AI.AZURE_UMI_SCOPE_KEY
                            + "'. Provide a valid OAuth2 scope (e.g. https://ai.azure.com/.default).");
        }
        validateEnvVar(APIConstants.AI.AZURE_UMI_ENV_TENANT_ID);
        validateEnvVar(APIConstants.AI.AZURE_UMI_ENV_CLIENT_ID);
        validateEnvVar(APIConstants.AI.AZURE_UMI_ENV_FEDERATED_TOKEN_FILE);

        // Close any previously held client before recreating, to avoid leaking Netty event-loop
        // resources if init() is called more than once.
        if (httpClient instanceof java.io.Closeable) {
            try {
                ((java.io.Closeable) httpClient).close();
            } catch (IOException e) {
                log.warn("AzureUmiTokenProvider: error closing previous HTTP client during re-init", e);
            }
        }
        httpClient = new NettyAsyncHttpClientBuilder().build();
        credential = new WorkloadIdentityCredentialBuilder()
                .httpClient(httpClient)
                .build();
        tokenRequestContext = new TokenRequestContext().addScopes(scope);

        log.info("AzureUmiTokenProvider initialised [scope=" + scope + "]");
    }

    /**
     * Returns the type identifier for this token provider, which is used in configuration to select
     * this implementation.
     *
     * @return a string constant representing the Azure UMI token provider type.
     */
    @Override
    public String getType() {
        return APIConstants.AI.AZURE_UMI_TOKEN_PROVIDER_TYPE;
    }

    /**
     * Returns a valid bearer access token. Token caching and refresh are managed internally
     * by the Azure Identity SDK.
     *
     * @return an access token string without the "Bearer " prefix.
     * @throws APIManagementException if the token cannot be acquired.
     */
    @Override
    public String getAccessToken() throws APIManagementException {
        try {
            AccessToken token = credential.getToken(tokenRequestContext)
                    .block(Duration.ofSeconds(TOKEN_ACQUISITION_TIMEOUT_SECONDS));
            if (token == null) {
                throw new APIManagementException(
                        "Azure UMI: token acquisition returned null.");
            }
            return token.getToken();
        } catch (CredentialUnavailableException e) {
            throw new APIManagementException(
                    "Azure UMI: workload identity credential is unavailable. "
                            + "Ensure the AKS Workload Identity webhook is configured for this pod.", e);
        } catch (ClientAuthenticationException e) {
            throw new APIManagementException(
                    "Azure UMI: authentication failed while acquiring token. "
                            + "Check the UMI client ID, tenant ID, and RBAC role assignment.", e);
        } catch (RuntimeException e) {
            throw new APIManagementException(
                    "Azure UMI: token acquisition failed — Azure endpoint may be unreachable or the "
                            + "request timed out after " + TOKEN_ACQUISITION_TIMEOUT_SECONDS + "s.", e);
        }
    }

    /**
     * Releases the Netty event-loop resources held by the HTTP client and closes the credential.
     * Must be called when this provider is no longer needed (e.g. on mediator destroy).
     */
    @Override
    public void close() throws IOException {
        credential = null;
        tokenRequestContext = null;
        if (httpClient instanceof java.io.Closeable) {
            ((java.io.Closeable) httpClient).close();
            httpClient = null;
        }
    }

    /**
     * Validates that the given environment variable is set and non-empty.
     *
     * @param envVarName the environment variable name to check.
     * @throws APIManagementException if the variable is missing or empty.
     */
    private void validateEnvVar(String envVarName) throws APIManagementException {
        String value = System.getenv(envVarName);
        if (value == null || value.isEmpty()) {
            throw new APIManagementException(
                    "Azure UMI: " + envVarName + " environment variable is not set. "
                            + "Ensure the AKS Workload Identity webhook is configured for this pod.");
        }
    }
}
