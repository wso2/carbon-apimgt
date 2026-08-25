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
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
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
 * Implemented with only the JDK ({@code java.security} for RS256 signing, {@code HttpURLConnection}
 * for the token exchange) plus a JSON parser, so the gateway does not depend on the Google auth /
 * http-client / opencensus / grpc-context library chain.
 */
public class GCPServiceAccountTokenProvider extends GCPAccessTokenProvider {

    private static final String JWT_BEARER_GRANT_TYPE = "urn:ietf:params:oauth:grant-type:jwt-bearer";
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
     * Package-private constructor that lets tests point the token exchange at a loopback stub. The token
     * endpoint is deliberately NOT taken from the (untrusted) service-account key JSON: a malicious or
     * mistyped {@code token_uri} must not be able to make the gateway POST the signed assertion to an
     * arbitrary or internal host (SSRF / credential leak). Production always uses {@link #DEFAULT_TOKEN_URI}
     * via the public constructor.
     */
    GCPServiceAccountTokenProvider(String serviceAccountKeyJson, String scope, String tokenUri) {

        JSONObject key;
        try {
            key = new JSONObject(serviceAccountKeyJson);
        } catch (JSONException e) {
            // Honour the declared contract: every invalid input surfaces as IllegalArgumentException so
            // GCPOAuth2TokenInjector.init() can wrap it in the intended SynapseException guidance.
            throw new IllegalArgumentException("Service-account key is not valid JSON.", e);
        }
        this.clientEmail = key.optString("client_email", null);
        this.privateKeyId = key.optString("private_key_id", null);
        String privateKeyPem = key.optString("private_key", null);
        this.tokenUri = tokenUri;
        this.scope = scope;
        if (StringUtils.isEmpty(clientEmail) || StringUtils.isEmpty(privateKeyPem)) {
            throw new IllegalArgumentException(
                    "Service-account key JSON is missing required fields (client_email / private_key).");
        }
        this.privateKey = parsePrivateKey(privateKeyPem);
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
     */
    private JSONObject exchangeAssertionForToken(String assertion) throws IOException {

        String body = "grant_type=" + URLEncoder.encode(JWT_BEARER_GRANT_TYPE, StandardCharsets.UTF_8)
                + "&assertion=" + URLEncoder.encode(assertion, StandardCharsets.UTF_8);
        HttpURLConnection connection = (HttpURLConnection) new URL(tokenUri).openConnection();
        try {
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(READ_TIMEOUT_MS);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Accept", "application/json");
            try (OutputStream out = connection.getOutputStream()) {
                out.write(body.getBytes(StandardCharsets.UTF_8));
            }
            int status = connection.getResponseCode();
            boolean success = status >= HttpURLConnection.HTTP_OK && status < HttpURLConnection.HTTP_MULT_CHOICE;
            InputStream stream = success ? connection.getInputStream() : connection.getErrorStream();
            String response = (stream == null) ? "" : readAll(stream);
            if (!success) {
                throw new IOException("The GCP token endpoint returned HTTP " + status + ": " + response);
            }
            return new JSONObject(response);
        } finally {
            connection.disconnect();
        }
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
