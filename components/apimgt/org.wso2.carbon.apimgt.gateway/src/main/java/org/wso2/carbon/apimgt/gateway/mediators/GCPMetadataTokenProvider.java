/*
 * Copyright (c) 2026 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.gateway.mediators;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Fetches a Google Cloud OAuth2 access token for the gateway's <em>attached</em> GCP identity from the
 * GCP metadata server - the keyless equivalent of Application Default Credentials / Workload Identity.
 * <p>
 * When the gateway runs on GCP compute (GCE, GKE with Workload Identity, Cloud Run, ...) with a service
 * account attached to the workload, the metadata server mints access tokens for that identity directly -
 * so no service-account key needs to be stored, mounted or rotated. Unlike
 * {@link GCPServiceAccountTokenProvider}, there is no private key and no JWT signing: the token is a
 * single authenticated GET against the metadata endpoint.
 * <p>
 * Caching and refresh are inherited from {@link GCPAccessTokenProvider}. This only works when the gateway
 * actually runs on GCP; off-GCP the metadata host does not resolve and {@link #fetchToken()} fails.
 */
public class GCPMetadataTokenProvider extends GCPAccessTokenProvider {

    private static final String METADATA_TOKEN_URL =
            "http://metadata.google.internal/computeMetadata/v1/instance/service-accounts/default/token";
    // The metadata server requires this header on every request as an anti-SSRF guard.
    private static final String METADATA_FLAVOR_HEADER = "Metadata-Flavor";
    private static final String METADATA_FLAVOR_VALUE = "Google";

    private final String scope;

    /**
     * @param scope the OAuth2 scope to request (space-separated for multiple scopes); may be empty to use
     *              the scopes already configured for the attached service account.
     */
    public GCPMetadataTokenProvider(String scope) {

        this.scope = scope;
    }

    @Override
    protected JSONObject fetchToken() throws IOException {

        HttpURLConnection connection = (HttpURLConnection) new URL(buildTokenUrl(scope)).openConnection();
        try {
            connection.setRequestMethod("GET");
            connection.setRequestProperty(METADATA_FLAVOR_HEADER, METADATA_FLAVOR_VALUE);
            connection.setRequestProperty("Accept", "application/json");
            connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(READ_TIMEOUT_MS);
            int status = connection.getResponseCode();
            boolean success = status >= HttpURLConnection.HTTP_OK && status < HttpURLConnection.HTTP_MULT_CHOICE;
            InputStream stream = success ? connection.getInputStream() : connection.getErrorStream();
            String response = (stream == null) ? "" : readAll(stream);
            if (!success) {
                throw new IOException("The GCP metadata server returned HTTP " + status + ": " + response
                        + ". Ensure the gateway runs on GCP with a service account attached to the workload.");
            }
            // Verify the response carries the "Metadata-Flavor: Google" header, confirming the token came from
            // the real GCP metadata server and not another service answering on metadata.google.internal /
            // 169.254.169.254 (a spoofed local proxy, a poisoned hosts file, or an off-GCP host). This is the
            // check google-auth relies on before trusting the metadata endpoint.
            if (!METADATA_FLAVOR_VALUE.equalsIgnoreCase(connection.getHeaderField(METADATA_FLAVOR_HEADER))) {
                throw new IOException("The response from the GCP metadata server is missing the expected '"
                        + METADATA_FLAVOR_HEADER + ": " + METADATA_FLAVOR_VALUE + "' header; refusing to trust the "
                        + "token (the metadata endpoint may be impersonated).");
            }
            return new JSONObject(response);
        } finally {
            connection.disconnect();
        }
    }

    /**
     * Builds the metadata token URL, appending the {@code scopes} query parameter when a scope is configured.
     * The metadata server expects the scopes as a comma-separated list, whereas the JWT-bearer path (and hence
     * the stored scope value) uses a space-separated list; any run of whitespace is normalised to a single comma
     * so multiple scopes are delivered correctly (matching the google-auth-library {@code ComputeEngineCredentials}
     * behaviour). A single scope is unaffected.
     *
     * @param scope the configured OAuth2 scope(s); may be empty.
     * @return the metadata token URL.
     */
    static String buildTokenUrl(String scope) {

        if (StringUtils.isEmpty(scope)) {
            return METADATA_TOKEN_URL;
        }
        String metadataScopes = scope.trim().replaceAll("\\s+", ",");
        return METADATA_TOKEN_URL + "?scopes=" + URLEncoder.encode(metadataScopes, StandardCharsets.UTF_8);
    }
}
