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

import org.apache.synapse.SynapseException;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.wso2.carbon.apimgt.api.APIManagementException;

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
 * Unit tests for {@link GCPOAuth2Mediator}: the first-request provider selection, the reassembly of the
 * single (pipe-joined base64) service-account key property, and the request-time
 * {@code Authorization: Bearer <token>} injection.
 * <p>
 * The key is delivered exactly as the endpoint sequence sets it - one {@code serviceAccountKey} value: the
 * whole base64 key in normal mode, or a pipe-joined concatenation of vault-lookups in secure-vault mode. The
 * token provider is asserted/injected via reflection and the Synapse message context is mocked, so no gateway
 * runtime or network is required.
 */
public class GCPOAuth2MediatorTest {

    private static final String SCOPE = "https://www.googleapis.com/auth/cloud-platform";
    private static final int CHUNK_LENGTH = 180;

    private GCPOAuth2Mediator mediator;
    private Axis2MessageContext synapseCtx;
    private org.apache.axis2.context.MessageContext axis2Ctx;

    @Before
    public void setUp() {

        // The policy check is stubbed to a no-op so buildProvider can run without the tenant-config / caching
        // runtime that APIUtil.validateRemoteURL needs; the reject path is covered by its own test below.
        mediator = new TestGCPOAuth2Mediator();
        synapseCtx = Mockito.mock(Axis2MessageContext.class);
        axis2Ctx = Mockito.mock(org.apache.axis2.context.MessageContext.class);
        Mockito.when(synapseCtx.getAxis2MessageContext()).thenReturn(axis2Ctx);
    }

    // -------------------------------------------------------------------------
    // Provider selection
    // -------------------------------------------------------------------------

    @Test
    public void testBuildProviderSelectsMetadataWhenNoKey() throws Exception {

        // No serviceAccountKey property -> keyless -> metadata (attached-identity) provider.
        mediator.setScope(SCOPE);

        Assert.assertTrue("No key must fall back to the metadata (attached-identity) provider",
                invokeBuildProvider() instanceof GCPMetadataTokenProvider);
    }

    @Test
    public void testInitDoesNotBuildProviderEagerly() throws Exception {

        // In secure-vault mode serviceAccountKey is resolved per-request, so init must build nothing.
        mediator.setServiceAccountKey(pipeJoined(validKeyJson()));
        mediator.setScope(SCOPE);
        mediator.init(null);

        Assert.assertNull("The provider must be built lazily on the first request, not at init", getProvider());
    }

    @Test
    public void testBuildsServiceAccountProviderFromKey() throws Exception {

        mediator.setServiceAccountKey(pipeJoined(validKeyJson()));
        mediator.setScope(SCOPE);

        Assert.assertTrue("A configured key must build the service-account provider",
                invokeBuildProvider() instanceof GCPServiceAccountTokenProvider);
    }

    @Test
    public void testBuildsServiceAccountProviderFromWholeBase64Key() throws Exception {

        // Normal mode delivers the whole base64 key as one value (no pipe delimiters).
        mediator.setServiceAccountKey(wholeBase64(validKeyJson()));
        mediator.setScope(SCOPE);

        Assert.assertTrue("A whole-base64 key must also build the service-account provider",
                invokeBuildProvider() instanceof GCPServiceAccountTokenProvider);
    }

    @Test
    public void testBuildFromInvalidKeyThrowsSynapseException() throws Exception {

        mediator.setServiceAccountKey(pipeJoined("{\"type\":\"service_account\"}")); // no client_email / private_key
        try {
            invokeBuildProvider();
            Assert.fail("Expected a SynapseException for an invalid service-account key");
        } catch (InvocationTargetException e) {
            Assert.assertTrue("Cause must be a SynapseException", e.getCause() instanceof SynapseException);
        }
    }

    @Test
    public void testBuildRejectsTokenEndpointBlockedByPolicy() throws Exception {

        // When the network access-control policy rejects the resolved token endpoint, the provider must not be
        // built or cached - the mediation fails instead of contacting the disallowed host.
        mediator = new GCPOAuth2Mediator() {
            @Override
            protected void validateRemoteUrl(String url) throws APIManagementException {
                throw new APIManagementException("blocked by policy");
            }
        };
        mediator.setServiceAccountKey(pipeJoined(validKeyJson()));
        mediator.setScope(SCOPE);

        try {
            invokeBuildProvider();
            Assert.fail("Expected a SynapseException when the token endpoint is blocked by policy");
        } catch (InvocationTargetException e) {
            Assert.assertTrue("Cause must be a SynapseException", e.getCause() instanceof SynapseException);
        }
        Assert.assertNull("A rejected endpoint must not cache a provider", getProvider());
    }

    // -------------------------------------------------------------------------
    // Key reassembly
    // -------------------------------------------------------------------------

    @Test
    public void testReassembleRoundTripsPipeJoinedKey() throws Exception {

        String keyJson = validKeyJson();
        byte[] reassembled = invokeReassemble(pipeJoined(keyJson));

        Assert.assertEquals("Pipe-joined chunks must reassemble to the original key",
                keyJson, new String(reassembled, StandardCharsets.UTF_8));
    }

    @Test
    public void testReassembleRoundTripsWholeBase64Key() throws Exception {

        String keyJson = validKeyJson();
        byte[] reassembled = invokeReassemble(wholeBase64(keyJson));

        Assert.assertEquals("A single (unsplit) base64 value must reassemble to the original key",
                keyJson, new String(reassembled, StandardCharsets.UTF_8));
    }

    @Test
    public void testReassembleRejectsEmptyChunk() throws Exception {

        // A missing (empty) vault chunk must fail loudly rather than silently truncating the key.
        try {
            invokeReassemble("QUFB||QkJC");
            Assert.fail("Expected a SynapseException for an empty chunk");
        } catch (InvocationTargetException e) {
            Assert.assertTrue("Cause must be a SynapseException", e.getCause() instanceof SynapseException);
        }
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

        boolean result = mediator.mediate(synapseCtx);

        Assert.assertTrue(result);
        Assert.assertEquals("Bearer tok-123", headers.get("Authorization"));
    }

    @Test
    public void testMediateThrowsWhenTokenIsEmpty() throws Exception {

        setProvider(stubProvider(""));
        try {
            mediator.mediate(synapseCtx);
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
            mediator.mediate(synapseCtx);
            Assert.fail("Expected a SynapseException wrapping the provider failure");
        } catch (SynapseException expected) {
            // expected
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /** Mediator with the network-policy check stubbed to a no-op, so buildProvider runs without the runtime. */
    private static class TestGCPOAuth2Mediator extends GCPOAuth2Mediator {

        @Override
        protected void validateRemoteUrl(String url) {
            // No-op: the reject path is covered by testBuildRejectsTokenEndpointBlockedByPolicy.
        }
    }

    private static GCPAccessTokenProvider stubProvider(String token) throws Exception {

        GCPAccessTokenProvider provider = Mockito.mock(GCPAccessTokenProvider.class);
        Mockito.when(provider.getAccessToken()).thenReturn(token);
        return provider;
    }

    /** Whole base64 of the key - the value the sequence emits in normal mode. */
    private static String wholeBase64(String keyJson) {

        return Base64.getEncoder().encodeToString(keyJson.getBytes(StandardCharsets.UTF_8));
    }

    /** base64 key split into CHUNK_LENGTH pieces joined with '|' - what the vault-mode concat resolves to. */
    private static String pipeJoined(String keyJson) {

        String base64 = wholeBase64(keyJson);
        StringBuilder joined = new StringBuilder();
        for (int offset = 0; offset < base64.length(); offset += CHUNK_LENGTH) {
            if (offset > 0) {
                joined.append('|');
            }
            joined.append(base64, offset, Math.min(base64.length(), offset + CHUNK_LENGTH));
        }
        return joined.toString();
    }

    private GCPAccessTokenProvider invokeBuildProvider() throws Exception {

        Method method = GCPOAuth2Mediator.class.getDeclaredMethod("buildProvider");
        method.setAccessible(true);
        return (GCPAccessTokenProvider) method.invoke(mediator);
    }

    private byte[] invokeReassemble(String pipeJoinedBase64) throws Exception {

        Method method = GCPOAuth2Mediator.class
                .getDeclaredMethod("reassembleServiceAccountKey", String.class);
        method.setAccessible(true);
        return (byte[]) method.invoke(mediator, pipeJoinedBase64);
    }

    private GCPAccessTokenProvider getProvider() {

        try {
            Field field = GCPOAuth2Mediator.class.getDeclaredField("tokenProvider");
            field.setAccessible(true);
            return (GCPAccessTokenProvider) field.get(mediator);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    private void setProvider(GCPAccessTokenProvider provider) {

        try {
            Field field = GCPOAuth2Mediator.class.getDeclaredField("tokenProvider");
            field.setAccessible(true);
            field.set(mediator, provider);
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
