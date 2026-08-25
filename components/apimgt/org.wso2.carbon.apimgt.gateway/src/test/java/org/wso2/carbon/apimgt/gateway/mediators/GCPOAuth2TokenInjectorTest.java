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

import java.io.IOException;
import java.lang.reflect.Field;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Unit tests for {@link GCPOAuth2TokenInjector}: the deploy-time provider selection and the request-time
 * {@code Authorization: Bearer <token>} injection.
 * <p>
 * The token provider is injected directly via reflection (or asserted after {@code init()}), and the Synapse
 * message context is mocked, so no gateway runtime or network is required.
 */
public class GCPOAuth2TokenInjectorTest {

    private static final String SCOPE = "https://www.googleapis.com/auth/cloud-platform";

    private GCPOAuth2TokenInjector injector;
    private Axis2MessageContext synapseCtx;
    private org.apache.axis2.context.MessageContext axis2Ctx;

    @Before
    public void setUp() {

        injector = new GCPOAuth2TokenInjector();
        synapseCtx = Mockito.mock(Axis2MessageContext.class);
        axis2Ctx = Mockito.mock(org.apache.axis2.context.MessageContext.class);
        Mockito.when(synapseCtx.getAxis2MessageContext()).thenReturn(axis2Ctx);
    }

    // -------------------------------------------------------------------------
    // init() — provider selection
    // -------------------------------------------------------------------------

    @Test
    public void testInitSelectsServiceAccountProviderWhenKeyPresent() throws Exception {

        injector.setServiceAccountKey(validKeyJson());
        injector.setScope(SCOPE);
        injector.init(null);

        Assert.assertTrue("A configured key must select the service-account provider",
                getProvider() instanceof GCPServiceAccountTokenProvider);
    }

    @Test
    public void testInitSelectsMetadataProviderWhenNoKey() {

        injector.setScope(SCOPE);
        injector.init(null);

        Assert.assertTrue("No key must fall back to the metadata (attached-identity) provider",
                getProvider() instanceof GCPMetadataTokenProvider);
    }

    @Test
    public void testInitWithInvalidKeyThrowsSynapseException() {

        injector.setServiceAccountKey("{\"type\":\"service_account\"}");
        try {
            injector.init(null);
            Assert.fail("Expected a SynapseException for an invalid service-account key");
        } catch (SynapseException expected) {
            // expected
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

    private GCPAccessTokenProvider getProvider() {

        try {
            Field field = GCPOAuth2TokenInjector.class.getDeclaredField("tokenProvider");
            field.setAccessible(true);
            return (GCPAccessTokenProvider) field.get(injector);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    private void setProvider(GCPAccessTokenProvider provider) {

        try {
            Field field = GCPOAuth2TokenInjector.class.getDeclaredField("tokenProvider");
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
