/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.authenticator;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.msf4j.MicroservicesRunner;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import javax.ws.rs.HttpMethod;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

public class LogInServiceTest {

    private final AuthenticatorAPI testMicroservice = new AuthenticatorAPI();
    private MicroservicesRunner microservicesRunner;
    public static final String HOSTNAME = "localhost";
    public static final int PORT = 8094;
    public static final int HTTP_PORT = 9763;
    public static final int HTTPS_PORT = 9443;
    public static final String HEADER_KEY_CONNECTION = "CONNECTION";
    public static final String HEADER_VAL_CLOSE = "CLOSE";
    protected static URI baseURI;
    @Rule
    public WireMockRule wireMockRule;

    static {
        //for localhost testing only
        javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(new javax.net.ssl.HostnameVerifier() {

            public boolean verify(String hostname, javax.net.ssl.SSLSession sslSession) {
                return "localhost".equals(hostname);
            }
        });
    }

    @BeforeClass
    public void setup() throws Exception {

        wireMockRule = new WireMockRule(wireMockConfig().port(HTTP_PORT).httpsPort(HTTPS_PORT));
        wireMockRule.start();

        // Mock service for key manager DCR endpoint
        wireMockRule.stubFor(post(urlEqualTo("/identity/connect/register/"))
                .withBasicAuth("admin", "admin")
                .withRequestBody(equalToJson("{\"client_name\":\"login_Application\"," +
                        "\"redirect_uris\":[\"http://temporary.callback/url\"],\"grant_types\":[\"password\"," +
                        "\"refresh_token\"],\"userinfo_signed_response_alg\":\"SHA256withRSA\"}", true, true))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"client_name\":\"publisher_application\","
                                + "\"client_id\":\"09261007-fd34-45c7-b950-c08f194773e7\","
                                + "\"client_secret\":\"9e38b1a4-08a1-44db-a52d-fc7c751bd742\","
                                + "\"redirect_uris\":[\"\"],\"grant_types\":[\"password\","
                                + "\"refresh_token\"]}")));

        // Mock service for key manager token endpoint
        wireMockRule.stubFor(post(urlEqualTo("/oauth2/token/"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/x-www-form-urlencoded")
                        .withBody("{\"access_token\":\"8d4f62ea-edc5-419d-a898-a517f9d3d6f9\","
                                + "\"refresh_token\":\"ec3cc1f4-6432-4bbb-9f9e-03c6dbd0da47\","
                                + "\"expires_in\":3600,\"scopes\":[\"apim:api_view\",\"apim:api_create\","
                                + "\"apim:api_publish\",\"apim:tier_view\",\"apim:tier_manage\","
                                + "\"apim:subscription_view\",\"apim:subscription_block\",\"apim:subscribe\","
                                + "\"apim:workflow_approve\"],\"expiresTimestamp\":1490615736702}")));

        // Mock service for key manager revoke endpoint
        wireMockRule.stubFor(post(urlEqualTo("/oauth2/revoke/")).willReturn(aResponse().withStatus(200)));

        baseURI = URI.create(String.format("http://%s:%d", HOSTNAME, PORT));
        microservicesRunner = new MicroservicesRunner(PORT);
        microservicesRunner
                .deploy(testMicroservice)
                .start();
    }

    @AfterClass
    public void teardown() throws Exception {
        microservicesRunner.stop();
        wireMockRule.resetAll();
        wireMockRule.stop();
    }

    protected HttpURLConnection request(String path, String method, boolean keepAlive) throws IOException {
        URL url = baseURI.resolve(path).toURL();
        HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
        if (method.equals(HttpMethod.POST) || method.equals(HttpMethod.PUT)) {
            urlConn.setDoOutput(true);
        }
        urlConn.setRequestMethod(method);
        if (!keepAlive) {
            urlConn.setRequestProperty(HEADER_KEY_CONNECTION, HEADER_VAL_CLOSE);
        }

        return urlConn;
    }


    @Test
    public void testLogin() throws IOException {
        HttpURLConnection urlConn = request("/login/token", HttpMethod.POST, true);
        String postParams = "username=admin&password=admin&grant_type=password"
                + "&validity_period=3600&scopes=apim:api_view";
        urlConn.getOutputStream().write((postParams).getBytes(Charsets.UTF_8));
        assertEquals(200, urlConn.getResponseCode());

        String content = new String(IOUtils.toByteArray(urlConn.getInputStream()), Charsets.UTF_8);
        JsonParser parser = new JsonParser();
        JsonObject obj = parser.parse(content).getAsJsonObject();
        assertTrue(obj.has("isTokenValid"));
        assertTrue(obj.get("isTokenValid").getAsBoolean());
        urlConn.disconnect();
    }

    @Test
    public void testLogOut() throws IOException {
        HttpURLConnection urlConn = request("/login/logout", HttpMethod.POST, true);
        urlConn.setRequestProperty("Authorization", "Bearer 1234");
        urlConn.setRequestProperty("Cookie", "WSO2_AM_TOKEN_2=2345");
        assertEquals(200, urlConn.getResponseCode());

        urlConn.disconnect();
    }

    @Test
    public void testRefresh() throws IOException {
        HttpURLConnection urlConn = request("/login/token", HttpMethod.POST, true);
        String postParams = "grant_type=refresh_token&validity_period=3600&scopes=apim:api_view";
        urlConn.setRequestProperty("Authorization", "Bearer 1234");
        urlConn.setRequestProperty("Cookie", "WSO2_AM_REFRESH_TOKEN_2=2345");
        urlConn.getOutputStream().write((postParams).getBytes(Charsets.UTF_8));
        assertEquals(200, urlConn.getResponseCode());

        String content = new String(IOUtils.toByteArray(urlConn.getInputStream()), Charsets.UTF_8);
        JsonParser parser = new JsonParser();
        JsonObject obj = parser.parse(content).getAsJsonObject();
        assertTrue(obj.has("isTokenValid"));
        assertTrue(obj.get("isTokenValid").getAsBoolean());
        urlConn.disconnect();
    }

}
