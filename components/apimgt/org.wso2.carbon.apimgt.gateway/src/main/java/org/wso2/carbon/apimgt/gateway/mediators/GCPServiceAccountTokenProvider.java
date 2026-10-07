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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

/**
 * Mints a Google Cloud OAuth2 access token from an uploaded service-account key using the
 * JWT-bearer assertion grant - without any third-party client library.
 * <p>
 * This is the minimal equivalent of what {@code google-auth-library-oauth2-http}'s
 * {@code ServiceAccountCredentials} does for a scoped service account: it builds an RS256-signed JWT
 * assertion ({@code iss=client_email}, {@code scope}, {@code aud=token_uri}, {@code iat}/{@code exp})
 * and exchanges it at the Google token endpoint
 * ({@code grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer}) for a short-lived access token.
 * Caching and refresh are inherited from {@link GCPAccessTokenProvider}.
 * <p>
 * Implemented with the JDK ({@code java.security} for RS256 signing) plus a JSON parser and the shared API
 * Manager HTTP client for the token exchange, so the gateway does not depend on the Google auth / opencensus /
 * grpc-context library chain. Using the shared client means the exchange honours the configured outbound proxy.
 */
public class GCPServiceAccountTokenProvider extends GCPAccessTokenProvider {

    private static final Log log = LogFactory.getLog(GCPServiceAccountTokenProvider.class);

    private static final String JWT_BEARER_GRANT_TYPE = "urn:ietf:params:oauth:grant-type:jwt-bearer";
    // Fallback token endpoint, used only when the service-account key JSON omits token_uri - matching the
    // google-auth SDK, whose generated keys always carry "token_uri": "https://oauth2.googleapis.com/token".
    private static final String DEFAULT_TOKEN_URI = "https://oauth2.googleapis.com/token";
    // GCP caps the assertion (and hence the minted token) lifetime at 1 hour.
    private static final long ASSERTION_LIFETIME_SECONDS = 3600L;

    private final String clientEmail;
    private final String privateKeyId;
    private final PrivateKey privateKey;
    private final String tokenUri;
    private final String scope;

    /**
     * @param serviceAccountKeyJson the raw service-account key JSON (the {@code key.json} contents).
     * @param scope                 the OAuth2 scope to request (space-separated for multiple scopes).
     * @throws IllegalArgumentException if the JSON is malformed, is missing required fields, or the private
     *                                  key is invalid.
     */
    public GCPServiceAccountTokenProvider(String serviceAccountKeyJson, String scope) {

        this(serviceAccountKeyJson, scope, DEFAULT_TOKEN_URI);
    }

    /**
     * Streaming variant: parses the key JSON directly off the stream, so the caller never has to materialise
     * the (sensitive) key as a {@code String}. The caller retains ownership of - and should wipe/close - the
     * stream's backing buffer.
     *
     * @param serviceAccountKeyJson the service-account key JSON as a stream.
     * @param scope                 the OAuth2 scope to request (space-separated for multiple scopes).
     * @throws IllegalArgumentException if the JSON is malformed, is missing required fields, or the private
     *                                  key is invalid.
     */
    public GCPServiceAccountTokenProvider(InputStream serviceAccountKeyJson, String scope) {

        this(serviceAccountKeyJson, scope, DEFAULT_TOKEN_URI);
    }

    /**
     * Package-private constructor whose third argument is the <em>fallback</em> token endpoint - used only when
     * the key JSON omits {@code token_uri}. Tests pass a loopback URL here (with a key that carries no
     * {@code token_uri}) to point the exchange at a local stub. The public constructors pass
     * {@link #DEFAULT_TOKEN_URI}.
     * <p>
     * The effective {@code token_uri} may come from the (user-supplied) key JSON, so callers must gate it with
     * the network access-control policy ({@link org.wso2.carbon.apimgt.impl.utils.APIUtil#validateRemoteURL}) via
     * {@link #getTokenUri()} before the exchange, so a hostile or mistyped {@code token_uri} cannot make the
     * gateway POST the signed assertion to an internal or attacker-chosen host.
     */
    GCPServiceAccountTokenProvider(String serviceAccountKeyJson, String scope, String fallbackTokenUri) {

        this(parseKey(serviceAccountKeyJson), scope, fallbackTokenUri);
    }

    GCPServiceAccountTokenProvider(InputStream serviceAccountKeyJson, String scope, String fallbackTokenUri) {

        this(parseKey(serviceAccountKeyJson), scope, fallbackTokenUri);
    }

    private GCPServiceAccountTokenProvider(JSONObject key, String scope, String fallbackTokenUri) {

        this.clientEmail = key.optString("client_email", null);
        this.privateKeyId = key.optString("private_key_id", null);
        String privateKeyPem = key.optString("private_key", null);
        // Match the google-auth SDK (ServiceAccountCredentials): use the token_uri embedded in the key JSON,
        // falling back to the standard Google endpoint only when the key omits it. Because this value can come
        // from a user-supplied key, the mediator validates it against the network access-control policy (see
        // getTokenUri()) before the exchange.
        String tokenUriFromKey = key.optString("token_uri", null);
        this.tokenUri = StringUtils.isNotEmpty(tokenUriFromKey) ? tokenUriFromKey : fallbackTokenUri;
        this.scope = scope;
        if (StringUtils.isEmpty(clientEmail) || StringUtils.isEmpty(privateKeyPem)) {
            throw new IllegalArgumentException(
                    "Service-account key JSON is missing required fields (client_email / private_key).");
        }
        this.privateKey = parsePrivateKey(privateKeyPem);
    }

    /**
     * The resolved token endpoint the exchange will POST the signed assertion to - the key JSON's
     * {@code token_uri} when present, else the fallback. Exposed so the mediator can gate it with the
     * network access-control policy before any network round-trip.
     *
     * @return the effective token endpoint URI.
     */
    String getTokenUri() {

        return tokenUri;
    }

    private static JSONObject parseKey(String serviceAccountKeyJson) {

        try {
            return new JSONObject(serviceAccountKeyJson);
        } catch (JSONException e) {
            // Honour the declared contract: every invalid input surfaces as IllegalArgumentException so
            // GCPOAuth2Mediator can wrap it in the intended SynapseException guidance.
            throw new IllegalArgumentException("Service-account key is not valid JSON.", e);
        }
    }

    private static JSONObject parseKey(InputStream serviceAccountKeyJson) {

        try {
            return new JSONObject(new JSONTokener(
                    new InputStreamReader(serviceAccountKeyJson, StandardCharsets.UTF_8)));
        } catch (JSONException e) {
            throw new IllegalArgumentException("Service-account key is not valid JSON.", e);
        }
    }

    @Override
    protected JSONObject fetchToken() throws IOException {

        long now = System.currentTimeMillis() / 1000L;
        return exchangeAssertionForToken(buildSignedAssertion(now));
    }

    /**
     * Builds the RS256-signed JWT bearer assertion: {@code base64url(header).base64url(claims).base64url(sig)}.
     */
    private String buildSignedAssertion(long nowSeconds) throws IOException {

        JSONObject header = new JSONObject();
        header.put("alg", "RS256");
        header.put("typ", "JWT");
        if (StringUtils.isNotEmpty(privateKeyId)) {
            header.put("kid", privateKeyId);
        }
        JSONObject claims = new JSONObject();
        claims.put("iss", clientEmail);
        claims.put("scope", scope);
        claims.put("aud", tokenUri);
        claims.put("iat", nowSeconds);
        claims.put("exp", nowSeconds + ASSERTION_LIFETIME_SECONDS);

        Base64.Encoder urlEncoder = Base64.getUrlEncoder().withoutPadding();
        String encodedHeader = urlEncoder.encodeToString(header.toString().getBytes(StandardCharsets.UTF_8));
        String encodedClaims = urlEncoder.encodeToString(claims.toString().getBytes(StandardCharsets.UTF_8));
        String signingInput = encodedHeader + "." + encodedClaims;
        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(signingInput.getBytes(StandardCharsets.UTF_8));
            return signingInput + "." + urlEncoder.encodeToString(signature.sign());
        } catch (GeneralSecurityException e) {
            throw new IOException("Failed to sign the GCP JWT assertion with the service-account key.", e);
        }
    }

    /**
     * POSTs the assertion to the token endpoint and returns the parsed JSON response.
     * <p>
     * The exchange goes over the shared API Manager HTTP client ({@link APIUtil#getHttpClient(int, String)}), the
     * same client the REST/OAuth backend token calls use, so it honours the configured outbound proxy
     * ({@code [apim.proxy_config]}) - including authenticated proxies in front of the HTTPS token endpoint.
     */
    private JSONObject exchangeAssertionForToken(String assertion) throws IOException {

        String body = "grant_type=" + URLEncoder.encode(JWT_BEARER_GRANT_TYPE, StandardCharsets.UTF_8)
                + "&assertion=" + URLEncoder.encode(assertion, StandardCharsets.UTF_8);
        URL url = new URL(tokenUri);
        if (log.isDebugEnabled()) {
            // Safe to log: only the token endpoint URL. The signed assertion, request body and token response
            // are credentials and are never logged.
            log.debug("Exchanging the JWT-bearer assertion for a GCP access token at the token endpoint: "
                    + tokenUri);
        }
        try (CloseableHttpClient httpClient = getHttpClient(url.getPort(), url.getProtocol())) {
            HttpPost httpPost = new HttpPost(tokenUri);
            httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
            httpPost.setHeader("Accept", "application/json");
            httpPost.setEntity(new StringEntity(body, StandardCharsets.UTF_8));
            // The shared client sets only connect/connection-request timeouts; add a read (socket) timeout so a
            // token endpoint that accepts the connection then stalls cannot hang the exchange (and, since the
            // refresh is synchronized, block every request waiting on a token) indefinitely.
            httpPost.setConfig(RequestConfig.custom()
                    .setConnectTimeout(CONNECT_TIMEOUT_MS)
                    .setConnectionRequestTimeout(CONNECT_TIMEOUT_MS)
                    .setSocketTimeout(READ_TIMEOUT_MS)
                    .build());
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                int status = response.getStatusLine().getStatusCode();
                String payload = response.getEntity() == null ? ""
                        : EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                if (status < HttpStatus.SC_OK || status >= HttpStatus.SC_MULTIPLE_CHOICES) {
                    // Report the HTTP status only. The response body can contain reflected assertions or token
                    // material, and this exception is logged by the mediator, so it must not carry the payload.
                    throw new IOException("The GCP token endpoint returned HTTP " + status);
                }
                return new JSONObject(payload);
            }
        }
    }

    /**
     * Supplies the HTTP client for the token exchange. Uses the shared API Manager client (which applies the
     * configured proxy); overridable in tests to target a loopback endpoint without the gateway runtime.
     */
    protected CloseableHttpClient getHttpClient(int port, String protocol) {

        return (CloseableHttpClient) APIUtil.getHttpClient(port, protocol);
    }

    /**
     * Parses the service-account private key: an unencrypted PKCS#8 PEM ({@code -----BEGIN PRIVATE KEY-----}).
     */
    private static PrivateKey parsePrivateKey(String privateKeyPem) {

        try {
            String base64 = privateKeyPem
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] der = Base64.getDecoder().decode(base64);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(der));
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw new IllegalArgumentException("Failed to parse the service-account private key "
                    + "(expected an unencrypted PKCS#8 RSA key).", e);
        }
    }
}
