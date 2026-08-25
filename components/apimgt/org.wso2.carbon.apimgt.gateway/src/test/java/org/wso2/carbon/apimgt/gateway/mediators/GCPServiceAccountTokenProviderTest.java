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
package org.wso2.carbon.apimgt.gateway.mediators;

import com.sun.net.httpserver.HttpServer;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Unit tests for {@link GCPServiceAccountTokenProvider} - the JDK-only replacement for the google-auth
 * service-account OAuth2 flow.
 * <p>
 * The token endpoint is stubbed with a local {@link HttpServer}; the tests construct the provider through
 * its package-private constructor to point the exchange at the loopback server. Production always uses the
 * fixed Google endpoint and ignores any {@code token_uri} in the key. This exercises the real RS256
 * JWT-bearer assertion building, signing and token exchange - the signature is verified against the
 * generated public key.
 */
public class GCPServiceAccountTokenProviderTest {

    private static final String SCOPE = "https://www.googleapis.com/auth/cloud-platform";
    private static final String CLIENT_EMAIL = "svc@test-project.iam.gserviceaccount.com";
    private static final String JWT_BEARER_GRANT_TYPE = "urn:ietf:params:oauth:grant-type:jwt-bearer";

    private HttpServer server;
    private String tokenUri;
    private KeyPair keyPair;

    private final AtomicReference<String> capturedBody = new AtomicReference<>();
    private final AtomicInteger requestCount = new AtomicInteger(0);
    private volatile int responseStatus;
    private volatile String responseBody;

    @Before
    public void setUp() throws Exception {

        keyPair = generateRsaKeyPair();
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/token", exchange -> {
            requestCount.incrementAndGet();
            capturedBody.set(readAll(exchange.getRequestBody()));
            byte[] out = responseBody.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(responseStatus, out.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(out);
            }
        });
        server.start();
        tokenUri = "http://localhost:" + server.getAddress().getPort() + "/token";
    }

    @After
    public void tearDown() {

        if (server != null) {
            server.stop(0);
        }
    }

    // -------------------------------------------------------------------------
    // Constructor validation
    // -------------------------------------------------------------------------

    @Test
    public void testConstructorRejectsMissingClientEmail() {

        assertConstructorRejects(keyJson(null, validPrivateKeyPem()));
    }

    @Test
    public void testConstructorRejectsMissingPrivateKey() {

        assertConstructorRejects(keyJson(CLIENT_EMAIL, null));
    }

    @Test
    public void testConstructorRejectsMalformedPrivateKey() {

        String badPem = "-----BEGIN PRIVATE KEY-----\naGVsbG8=\n-----END PRIVATE KEY-----";
        assertConstructorRejects(keyJson(CLIENT_EMAIL, badPem));
    }

    @Test
    public void testConstructorRejectsMalformedJson() {

        // A truncated or wrongly decrypted key is not valid JSON; it must still surface as
        // IllegalArgumentException so GCPOAuth2TokenInjector can turn it into the intended guidance.
        assertConstructorRejects("this-is-not-json");
    }

    // -------------------------------------------------------------------------
    // Token exchange
    // -------------------------------------------------------------------------

    @Test
    public void testGetAccessTokenPerformsJwtBearerExchange() throws Exception {

        responseStatus = 200;
        responseBody = new JSONObject().put("access_token", "srv-token").put("expires_in", 3600).toString();

        GCPServiceAccountTokenProvider provider =
                new GCPServiceAccountTokenProvider(keyJson(CLIENT_EMAIL, validPrivateKeyPem()), SCOPE, tokenUri);
        String token = provider.getAccessToken();

        Assert.assertEquals("srv-token", token);

        // The request must be a jwt-bearer grant carrying a signed assertion.
        Map<String, String> form = parseForm(capturedBody.get());
        Assert.assertEquals(JWT_BEARER_GRANT_TYPE, form.get("grant_type"));
        String assertion = form.get("assertion");
        Assert.assertNotNull("assertion parameter must be present", assertion);

        // The assertion is a well-formed, RS256-signed JWT with the expected claims.
        String[] parts = assertion.split("\\.");
        Assert.assertEquals("A JWT has three dot-separated segments", 3, parts.length);

        JSONObject header = new JSONObject(new String(base64UrlDecode(parts[0]), StandardCharsets.UTF_8));
        Assert.assertEquals("RS256", header.getString("alg"));
        Assert.assertEquals("JWT", header.getString("typ"));

        JSONObject claims = new JSONObject(new String(base64UrlDecode(parts[1]), StandardCharsets.UTF_8));
        Assert.assertEquals(CLIENT_EMAIL, claims.getString("iss"));
        Assert.assertEquals(SCOPE, claims.getString("scope"));
        Assert.assertEquals(tokenUri, claims.getString("aud"));
        Assert.assertTrue("exp must be after iat", claims.getLong("exp") > claims.getLong("iat"));

        // The signature must verify against the service account's public key.
        Signature verifier = Signature.getInstance("SHA256withRSA");
        verifier.initVerify(keyPair.getPublic());
        verifier.update((parts[0] + "." + parts[1]).getBytes(StandardCharsets.UTF_8));
        Assert.assertTrue("The JWT signature must verify with the service-account public key",
                verifier.verify(base64UrlDecode(parts[2])));
    }

    @Test
    public void testTokenExchangeErrorResponseThrows() {

        responseStatus = 400;
        responseBody = "{\"error\":\"invalid_grant\"}";

        try {
            new GCPServiceAccountTokenProvider(keyJson(CLIENT_EMAIL, validPrivateKeyPem()), SCOPE, tokenUri)
                    .getAccessToken();
            Assert.fail("Expected an IOException for a non-2xx token endpoint response");
        } catch (IOException e) {
            Assert.assertTrue("Message should carry the HTTP status: " + e.getMessage(),
                    e.getMessage().contains("400"));
        }
    }

    @Test
    public void testTokenIsCachedAcrossCalls() throws Exception {

        responseStatus = 200;
        responseBody = new JSONObject().put("access_token", "srv-token").put("expires_in", 3600).toString();

        GCPServiceAccountTokenProvider provider =
                new GCPServiceAccountTokenProvider(keyJson(CLIENT_EMAIL, validPrivateKeyPem()), SCOPE, tokenUri);
        provider.getAccessToken();
        provider.getAccessToken();

        Assert.assertEquals("The token endpoint must be hit only once for a still-valid token",
                1, requestCount.get());
    }

    @Test
    public void testTokenUriInKeyIsIgnored() throws Exception {

        responseStatus = 200;
        responseBody = new JSONObject().put("access_token", "srv-token").put("expires_in", 3600).toString();

        // The key carries a hostile token_uri; the provider must ignore it and post the signed assertion
        // only to the endpoint it was constructed with - never to an attacker-chosen or internal host.
        JSONObject key = new JSONObject(keyJson(CLIENT_EMAIL, validPrivateKeyPem()));
        key.put("token_uri", "http://169.254.169.254/latest/meta-data/");
        GCPServiceAccountTokenProvider provider =
                new GCPServiceAccountTokenProvider(key.toString(), SCOPE, tokenUri);

        Assert.assertEquals("srv-token", provider.getAccessToken());
        Assert.assertEquals("The key's token_uri must be ignored; only the fixed endpoint is contacted",
                1, requestCount.get());
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void assertConstructorRejects(String keyJson) {

        try {
            new GCPServiceAccountTokenProvider(keyJson, SCOPE);
            Assert.fail("Expected an IllegalArgumentException for an invalid service-account key");
        } catch (IllegalArgumentException expected) {
            // expected
        }
    }

    private String keyJson(String clientEmail, String privateKeyPem) {

        JSONObject key = new JSONObject();
        key.put("type", "service_account");
        key.put("project_id", "test-project");
        key.put("private_key_id", "key-123");
        if (clientEmail != null) {
            key.put("client_email", clientEmail);
        }
        if (privateKeyPem != null) {
            key.put("private_key", privateKeyPem);
        }
        return key.toString();
    }

    private String validPrivateKeyPem() {

        String base64 = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
        return "-----BEGIN PRIVATE KEY-----\n" + base64 + "\n-----END PRIVATE KEY-----\n";
    }

    private static KeyPair generateRsaKeyPair() throws Exception {

        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        return generator.generateKeyPair();
    }

    private static Map<String, String> parseForm(String body) throws IOException {

        Map<String, String> form = new HashMap<>();
        for (String pair : body.split("&")) {
            int eq = pair.indexOf('=');
            if (eq > 0) {
                String name = URLDecoder.decode(pair.substring(0, eq), StandardCharsets.UTF_8.name());
                String value = URLDecoder.decode(pair.substring(eq + 1), StandardCharsets.UTF_8.name());
                form.put(name, value);
            }
        }
        return form;
    }

    private static byte[] base64UrlDecode(String value) {

        return Base64.getUrlDecoder().decode(value);
    }

    private static String readAll(InputStream stream) throws IOException {

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] chunk = new byte[1024];
        int read;
        while ((read = stream.read(chunk)) != -1) {
            buffer.write(chunk, 0, read);
        }
        return new String(buffer.toByteArray(), StandardCharsets.UTF_8);
    }
}
