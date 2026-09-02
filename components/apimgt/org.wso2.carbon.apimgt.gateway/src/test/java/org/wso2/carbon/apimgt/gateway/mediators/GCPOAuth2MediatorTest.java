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

import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Unit tests for {@link GCPOAuth2Mediator}: the deploy-time / first-request provider selection, the
 * reassembly of the chunked service-account key, and the request-time {@code Authorization: Bearer <token>}
 * injection.
 * <p>
 * The service-account key is delivered as base64 chunk properties (as it is by the endpoint sequence), the
 * token provider is asserted/injected via reflection, and the Synapse message context is mocked, so no
 * gateway runtime or network is required.
 */
public class GCPOAuth2MediatorTest {

    private static final String SCOPE = "https://www.googleapis.com/auth/cloud-platform";
    private static final String CHUNK_PREFIX = "_gcp_sak_chunk_";
    private static final int CHUNK_LENGTH = 180;

    private GCPOAuth2Mediator injector;
    private Axis2MessageContext synapseCtx;
    private org.apache.axis2.context.MessageContext axis2Ctx;

    @Before
    public void setUp() {

        injector = new GCPOAuth2Mediator();
        synapseCtx = Mockito.mock(Axis2MessageContext.class);
        axis2Ctx = Mockito.mock(org.apache.axis2.context.MessageContext.class);
        Mockito.when(synapseCtx.getAxis2MessageContext()).thenReturn(axis2Ctx);
    }

    // -------------------------------------------------------------------------
    // Provider selection
    // -------------------------------------------------------------------------

    @Test
    public void testInitSelectsMetadataProviderWhenNoKey() {

        // No key chunks configured (count stays 0) -> keyless -> metadata (attached-identity) provider, eager.
        injector.setScope(SCOPE);
        injector.init(null);

        Assert.assertTrue("No key must fall back to the metadata (attached-identity) provider",
                getProvider() instanceof GCPMetadataTokenProvider);
    }

    @Test
    public void testInitDefersServiceAccountProviderWhenChunksPresent() throws Exception {

        // With key chunks the chunks are per-request properties, so the provider is NOT built at init time.
        setChunkProperties(validKeyJson());
        injector.setScope(SCOPE);
        injector.init(null);

        Assert.assertNull("The service-account provider must be built lazily, not at init", getProvider());
    }

    @Test
    public void testBuildsServiceAccountProviderFromChunks() throws Exception {

        setChunkProperties(validKeyJson());
        injector.setScope(SCOPE);

        GCPAccessTokenProvider provider = invokeBuildFromChunks();

        Assert.assertTrue("Configured key chunks must build the service-account provider",
                provider instanceof GCPServiceAccountTokenProvider);
    }

    @Test
    public void testBuildFromChunksWithInvalidKeyThrowsSynapseException() throws Exception {

        setChunkProperties("{\"type\":\"service_account\"}");   // missing client_email / private_key
        try {
            invokeBuildFromChunks();
            Assert.fail("Expected a SynapseException for an invalid service-account key");
        } catch (InvocationTargetException e) {
            Assert.assertTrue("Cause must be a SynapseException", e.getCause() instanceof SynapseException);
        }
    }

    @Test
    public void testReassembleServiceAccountKeyRoundTrips() throws Exception {

        String keyJson = validKeyJson();
        setChunkProperties(keyJson);

        Method method = GCPOAuth2Mediator.class
                .getDeclaredMethod("reassembleServiceAccountKey", MessageContext.class);
        method.setAccessible(true);
        byte[] reassembled = (byte[]) method.invoke(injector, synapseCtx);

        Assert.assertEquals("Reassembled key must equal the original",
                keyJson, new String(reassembled, StandardCharsets.UTF_8));
    }

    // -------------------------------------------------------------------------
    // mediate() — token injection
    // -------------------------------------------------------------------------

    @Test
    public void testMediateInjectsBearerToken() throws Exception {

        setProvider(stubProvider("tok-123"));
        Map<String, Object> headers = new HashMap<>();
        Mockito.when(axis2Ctx.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS))
                .thenReturn(headers);

        boolean result = injector.mediate(synapseCtx);

        Assert.assertTrue(result);
        Assert.assertEquals("Bearer tok-123", headers.get("Authorization"));
    }

    @Test
    public void testMediateThrowsWhenTokenIsEmpty() throws Exception {

        setProvider(stubProvider(""));
        try {
            injector.mediate(synapseCtx);
            Assert.fail("Expected a SynapseException when no token could be obtained");
        } catch (SynapseException expected) {
            // expected
        }
    }

    @Test
    public void testMediateWrapsProviderFailureAsSynapseException() throws Exception {

        GCPAccessTokenProvider failing = Mockito.mock(GCPAccessTokenProvider.class);
        Mockito.when(failing.getAccessToken()).thenThrow(new IOException("token endpoint unreachable"));
        setProvider(failing);

        try {
            injector.mediate(synapseCtx);
            Assert.fail("Expected a SynapseException wrapping the provider failure");
        } catch (SynapseException expected) {
            // expected
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static GCPAccessTokenProvider stubProvider(String token) throws Exception {

        GCPAccessTokenProvider provider = Mockito.mock(GCPAccessTokenProvider.class);
        Mockito.when(provider.getAccessToken()).thenReturn(token);
        return provider;
    }

    /**
     * base64-encodes the key, splits it into chunks the way the endpoint sequence does, stubs each chunk as a
     * message-context property, and wires the injector's prefix + count.
     */
    private void setChunkProperties(String keyJson) {

        String base64 = Base64.getEncoder().encodeToString(keyJson.getBytes(StandardCharsets.UTF_8));
        int count = 0;
        for (int offset = 0; offset < base64.length(); offset += CHUNK_LENGTH, count++) {
            String chunk = base64.substring(offset, Math.min(base64.length(), offset + CHUNK_LENGTH));
            Mockito.when(synapseCtx.getProperty(CHUNK_PREFIX + count)).thenReturn(chunk);
        }
        injector.setServiceAccountKeyChunkPrefix(CHUNK_PREFIX);
        injector.setServiceAccountKeyChunkCount(String.valueOf(count));
    }

    private GCPAccessTokenProvider invokeBuildFromChunks() throws Exception {

        Method method = GCPOAuth2Mediator.class
                .getDeclaredMethod("buildProviderFromChunks", MessageContext.class);
        method.setAccessible(true);
        return (GCPAccessTokenProvider) method.invoke(injector, synapseCtx);
    }

    private GCPAccessTokenProvider getProvider() {

        try {
            Field field = GCPOAuth2Mediator.class.getDeclaredField("tokenProvider");
            field.setAccessible(true);
            return (GCPAccessTokenProvider) field.get(injector);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    private void setProvider(GCPAccessTokenProvider provider) {

        try {
            Field field = GCPOAuth2Mediator.class.getDeclaredField("tokenProvider");
            field.setAccessible(true);
            field.set(injector, provider);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String validKeyJson() throws Exception {

        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair keyPair = generator.generateKeyPair();
        String pem = "-----BEGIN PRIVATE KEY-----\n"
                + Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded())
                + "\n-----END PRIVATE KEY-----\n";
        JSONObject key = new JSONObject();
        key.put("type", "service_account");
        key.put("project_id", "test-project");
        key.put("private_key_id", "key-123");
        key.put("client_email", "svc@test-project.iam.gserviceaccount.com");
        key.put("private_key", pem);
        key.put("token_uri", "https://oauth2.googleapis.com/token");
        return key.toString();
    }
}
