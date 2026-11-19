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
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * Unit tests for {@link GCPMetadataTokenProvider}: the metadata token-URL construction - the comma-separated
 * {@code scopes} query parameter (as the GCE metadata server expects, unlike the space-separated JWT-bearer
 * form) and the {@code GCE_METADATA_HOST} host override - and the metadata fetch itself over the shared HTTP
 * client, including the {@code Metadata-Flavor: Google} anti-spoof response check.
 * <p>
 * The scope tests use the host-explicit {@code buildTokenUrl(scope, host)} overload so they are independent of
 * any {@code GCE_METADATA_HOST} set in the build environment. The fetch tests stub the metadata server with a
 * local {@link HttpServer} and override the URL/HTTP-client seams to reach it.
 */
public class GCPMetadataTokenProviderTest {

    private static final String DEFAULT_HOST = "metadata.google.internal";
    private static final String BASE_URL =
            "http://metadata.google.internal/computeMetadata/v1/instance/service-accounts/default/token";
    private static final String SCOPE = "https://www.googleapis.com/auth/cloud-platform";

    private HttpServer server;
    private String tokenUrl;
    private volatile int responseStatus;
    private volatile String responseBody;
    private volatile boolean sendMetadataFlavor;

    @Before
    public void startStubServer() throws Exception {

        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/token", exchange -> {
            if (sendMetadataFlavor) {
                exchange.getResponseHeaders().add("Metadata-Flavor", "Google");
            }
            byte[] out = responseBody.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(responseStatus, out.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(out);
            }
        });
        server.start();
        tokenUrl = "http://localhost:" + server.getAddress().getPort() + "/token";
    }

    @After
    public void stopStubServer() {

        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    public void testNoScopeOmitsScopesParameter() {

        Assert.assertEquals(BASE_URL, GCPMetadataTokenProvider.buildTokenUrl("", DEFAULT_HOST));
        Assert.assertEquals(BASE_URL, GCPMetadataTokenProvider.buildTokenUrl(null, DEFAULT_HOST));
    }

    @Test
    public void testSingleScopeIsAppendedUnchanged() {

        Assert.assertEquals(
                BASE_URL + "?scopes=https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fcloud-platform",
                GCPMetadataTokenProvider.buildTokenUrl(
                        "https://www.googleapis.com/auth/cloud-platform", DEFAULT_HOST));
    }

    @Test
    public void testSpaceSeparatedScopesAreJoinedWithCommas() {

        // The stored/JWT form is space-separated; the metadata server requires commas.
        String url = GCPMetadataTokenProvider.buildTokenUrl(
                "https://www.googleapis.com/auth/cloud-platform https://www.googleapis.com/auth/userinfo.email",
                DEFAULT_HOST);
        Assert.assertEquals(
                BASE_URL + "?scopes=https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fcloud-platform"
                        + "%2Chttps%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.email",
                url);
        // No raw space (encoded as '+' or %20) survives into the scopes value.
        Assert.assertFalse(url.contains("+"));
        Assert.assertFalse(url.contains("%20"));
    }

    @Test
    public void testCollapsesRunsOfWhitespaceAndTrims() {

        // Leading/trailing whitespace and multi-space/tab runs between scopes collapse to single commas.
        Assert.assertEquals(
                BASE_URL + "?scopes=a%2Cb%2Cc",
                GCPMetadataTokenProvider.buildTokenUrl("  a   b\tc  ", DEFAULT_HOST));
    }

    @Test
    public void testCustomHostIsUsedInTheUrl() {

        // GCE_METADATA_HOST override: only the host changes, the path stays fixed.
        Assert.assertEquals(
                "http://metadata.internal.example/computeMetadata/v1/instance/service-accounts/default/token",
                GCPMetadataTokenProvider.buildTokenUrl("", "metadata.internal.example"));
        Assert.assertEquals(
                "http://metadata.internal.example/computeMetadata/v1/instance/service-accounts/default/token"
                        + "?scopes=a%2Cb",
                GCPMetadataTokenProvider.buildTokenUrl("a b", "metadata.internal.example"));
    }

    @Test
    public void testResolveMetadataHostDefaultsWhenEnvUnset() {

        // In the build environment GCE_METADATA_HOST is not set, so the default host is used. When it is set
        // on the VM, resolveMetadataHost() returns that value (exercised on the workload, not portably in a
        // unit test since the JVM cannot set its own environment).
        if (System.getenv(GCPMetadataTokenProvider.GCE_METADATA_HOST_ENV_VAR) == null) {
            Assert.assertEquals(DEFAULT_HOST, GCPMetadataTokenProvider.resolveMetadataHost());
        }
    }

    // -------------------------------------------------------------------------
    // Metadata fetch (over the shared HTTP client)
    // -------------------------------------------------------------------------

    @Test
    public void testFetchReturnsTokenWhenMetadataFlavorPresent() throws Exception {

        responseStatus = 200;
        responseBody = new JSONObject().put("access_token", "meta-token").put("expires_in", 3600).toString();
        sendMetadataFlavor = true;

        Assert.assertEquals("meta-token", new TestMetadataProvider(SCOPE, tokenUrl).getAccessToken());
    }

    @Test
    public void testFetchRejectsResponseMissingMetadataFlavor() {

        // A response without "Metadata-Flavor: Google" may come from an impersonated endpoint; it must be refused
        // even when it carries a plausible token body.
        responseStatus = 200;
        responseBody = new JSONObject().put("access_token", "meta-token").put("expires_in", 3600).toString();
        sendMetadataFlavor = false;

        try {
            new TestMetadataProvider(SCOPE, tokenUrl).getAccessToken();
            Assert.fail("Expected an IOException when the Metadata-Flavor header is absent");
        } catch (IOException e) {
            Assert.assertTrue("Message should mention the missing header: " + e.getMessage(),
                    e.getMessage().contains("Metadata-Flavor"));
        }
    }

    @Test
    public void testFetchThrowsOnErrorStatus() {

        responseStatus = 500;
        responseBody = "metadata server error";
        sendMetadataFlavor = true;

        try {
            new TestMetadataProvider(SCOPE, tokenUrl).getAccessToken();
            Assert.fail("Expected an IOException for a non-2xx metadata response");
        } catch (IOException e) {
            Assert.assertTrue("Message should carry the HTTP status: " + e.getMessage(),
                    e.getMessage().contains("500"));
        }
    }

    /**
     * Provider variant pointed at the loopback stub: the URL seam returns the stub URL (instead of the real
     * metadata host, which is not set via {@code GCE_METADATA_HOST} in a unit test) and the HTTP client is a
     * plain {@link CloseableHttpClient} rather than the gateway's shared client.
     */
    private static final class TestMetadataProvider extends GCPMetadataTokenProvider {

        private final String stubUrl;

        TestMetadataProvider(String scope, String stubUrl) {
            super(scope);
            this.stubUrl = stubUrl;
        }

        @Override
        String resolveTokenUrl() {
            return stubUrl;
        }

        @Override
        protected CloseableHttpClient getHttpClient(int port, String protocol) {
            return HttpClients.createDefault();
        }
    }
}
