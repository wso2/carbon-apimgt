package org.wso2.carbon.apimgt.common.gateway;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.ssl.SSLContexts;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockserver.configuration.ConfigurationProperties;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Header;
import org.wso2.carbon.apimgt.common.gateway.configdto.HttpClientConfigurationDTO;
import org.wso2.carbon.apimgt.common.gateway.util.CommonAPIUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.net.ssl.SSLContext;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

/**
 * Test cases for {@link CommonAPIUtil}
 */
public class CommonAPIUtilTestCase {
    private static ClientAndServer mockServer;
    private static ClientAndServer proxyServer;
    int connectionLimit = 100;
    int maximumConnectionsPerRoute = 10;
    int connectionTimeout = -1;
    static String trustStorePath = Objects.requireNonNull(CommonAPIUtilTestCase.class.getClassLoader()
            .getResource("client-truststore.jks")).getPath();
    static String trustStorePassword = "wso2carbon";

    static SSLContext sslContext;

    @BeforeClass
    public static void startProxy() {
        proxyServer = ClientAndServer.startClientAndServer();
        mockServer = ClientAndServer.startClientAndServer();
        String username = "user";
        String password = "pass";
        ConfigurationProperties.proxyAuthenticationUsername(username);
        ConfigurationProperties.proxyAuthenticationPassword(password);
        mockServer.withSecure(true).when(
                request().withPath("/hello")
        ).respond(
                response().withHeaders(
                        new Header(CONTENT_TYPE.toString(), "application/json")
                ).withBody(
                        "{\"hello\":\"world\"}"
                )
        );
        try (InputStream trustStoreContent = Files.newInputStream(Paths.get(trustStorePath))) {
            KeyStore trustStore = KeyStore.getInstance("JKS");
            trustStore.load(trustStoreContent, trustStorePassword.toCharArray());
            sslContext = SSLContexts.custom().loadTrustMaterial(trustStore, null).build();
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @AfterClass
    public static void clean() {
        mockServer.stop();
        proxyServer.stop();
    }

    @Test
    public void testGetHttpClientWithProxy() {
        String proxyHost = "127.0.0.1";
        String proxyUsername = "user";
        String proxyPassword = "pass";
        String[] nonProxyHosts = new String[]{};
        String proxyProtocol = "http";

        HttpGet httpGet = new HttpGet("https://localhost:" + mockServer.getPort() + "/hello");
        HttpClientConfigurationDTO.Builder builder = new HttpClientConfigurationDTO.Builder();

        // Check if the proxyConfiguration is provided but the host is under nonProxyHosts, the outbound call is sent
        // directly to the backend.
        HttpGet httpGetWithTLS = new HttpGet("http://localhost:" + mockServer.getPort() + "/hello");
        HttpClientConfigurationDTO nonProxyHostBasedProxyConfig = builder
                .withConnectionParams(connectionLimit, maximumConnectionsPerRoute, connectionTimeout)
                .withSSLContext(sslContext)
                // proxyProtocol here is https (due to existing limitation)
                .withProxy(proxyHost, proxyServer.getPort(), proxyUsername, "random", proxyProtocol,
                        new String[]{"localhost"})
                .build();
        HttpClient clientForNonProxyHost = null;
        clientForNonProxyHost = CommonAPIUtil.getHttpClient("https", nonProxyHostBasedProxyConfig);

        Assert.assertNotNull(clientForNonProxyHost);
        HttpResponse nonProxyHostResponse = getHttpResponseFromClient(clientForNonProxyHost, httpGetWithTLS);
        Assert.assertNotNull(nonProxyHostResponse);
        Assert.assertEquals(200, nonProxyHostResponse.getStatusLine().getStatusCode());
        // Specifically tests if the proxyServer did not respond at all.
        proxyServer.verifyZeroInteractions();

        // Given the proxy configuration, checks if the call is successfully routed via the proxy server.
        HttpClientConfigurationDTO configuration = builder
                .withConnectionParams(connectionLimit, maximumConnectionsPerRoute, connectionTimeout)
                .withSSLContext(sslContext)
                .withProxy(proxyHost, proxyServer.getPort(), proxyUsername, proxyPassword, proxyProtocol, nonProxyHosts)
                .build();

        HttpClient client = null;
        client = CommonAPIUtil.getHttpClient("https", configuration);
        Assert.assertNotNull(client);
        HttpResponse httpResponse = getHttpResponseFromClient(client, httpGet);
        Assert.assertNotNull(httpResponse);
        Assert.assertEquals(200, httpResponse.getStatusLine().getStatusCode());
        try {
            Assert.assertEquals("{\"hello\":\"world\"}",
                    readInputStream(httpResponse.getEntity().getContent()));
        } catch (IOException e) {
            Assert.fail("Exception occurred while reading the content from response.");
        }

        // Given the proxy configuration with wrong credentials, checks if the call fails at the proxy server.
        HttpClientConfigurationDTO configWithWrongProxyCredentials = builder
                .withConnectionParams(connectionLimit, maximumConnectionsPerRoute, connectionTimeout)
                .withSSLContext(sslContext)
                .withProxy(proxyHost, proxyServer.getPort(), proxyUsername, "random", proxyProtocol, nonProxyHosts)
                .build();
        HttpClient clientWithWrongProxyCreds = null;
        clientWithWrongProxyCreds = CommonAPIUtil.getHttpClient("https", configWithWrongProxyCredentials);
        Assert.assertNotNull(clientWithWrongProxyCreds);

        HttpResponse failedResponse = getHttpResponseFromClient(clientWithWrongProxyCreds, httpGet);
        Assert.assertNotNull(failedResponse);
        Assert.assertEquals(407, failedResponse.getStatusLine().getStatusCode());
    }

    @Test
    public void testGetHttpClientWithoutProxy() {
        // Checks https request without a proxy server
        HttpGet httpsGet = new HttpGet("https://localhost:" + mockServer.getPort() + "/hello");
        HttpClientConfigurationDTO.Builder builder = new HttpClientConfigurationDTO.Builder();
        HttpClientConfigurationDTO configuration = builder
                .withConnectionParams(connectionLimit, maximumConnectionsPerRoute, connectionTimeout)
                .withSSLContext(sslContext)
                .build();

        HttpClient securedClient = null;
        securedClient = CommonAPIUtil.getHttpClient("https", configuration);
        Assert.assertNotNull(securedClient);
        HttpResponse httpsResponse = getHttpResponseFromClient(securedClient, httpsGet);
        Assert.assertNotNull(httpsResponse);
        Assert.assertEquals(200, httpsResponse.getStatusLine().getStatusCode());
        try {
            Assert.assertEquals("{\"hello\":\"world\"}",
                    readInputStream(httpsResponse.getEntity().getContent()));
        } catch (IOException e) {
            Assert.fail("Exception occurred while reading the content from response.");
        }

        // Checks http request without a proxy server
        HttpGet httpGet = new HttpGet("http://localhost:" + mockServer.getPort() + "/hello");
        HttpClient client = null;
        client = CommonAPIUtil.getHttpClient("http", configuration);
        Assert.assertNotNull(client);
        HttpResponse httpResponse = getHttpResponseFromClient(client, httpGet);
        Assert.assertNotNull(httpResponse);
        Assert.assertEquals(200, httpResponse.getStatusLine().getStatusCode());
        try {
            Assert.assertEquals("{\"hello\":\"world\"}",
                    readInputStream(httpResponse.getEntity().getContent()));
        } catch (IOException e) {
            Assert.fail("Exception occurred while reading the content from response.");
        }
    }

    private HttpResponse getHttpResponseFromClient(HttpClient httpClient, HttpGet httpGet) {
        HttpResponse httpResponse = null;
        int retryCount = 0;
        while (retryCount < 3) {
            try {
                retryCount++;
                httpResponse = httpClient.execute(httpGet);
            } catch (Exception ex) {
                try {
                    Thread.sleep(2 * 1000L);
                } catch (InterruptedException e) {
                    // Ignore
                }
            }
        }
        return httpResponse;
    }

    private String readInputStream(InputStream inputStream) {
        return new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));
    }
}
