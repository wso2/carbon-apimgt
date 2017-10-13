/*
 *
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.apimgt.impl.utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.impl.ServiceReferenceHolderMockCreator;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@RunWith(PowerMockRunner.class)
@PrepareForTest({LogFactory.class, ServiceReferenceHolder.class, SSLSocketFactory.class, CarbonUtils.class})
@PowerMockIgnore("javax.net.ssl.*")
public class APIUtilTest {

    @Test
    public void testGetAPINamefromRESTAPI() throws Exception {
        String restAPI = "admin--map";
        String apiName = APIUtil.getAPINamefromRESTAPI(restAPI);

        Assert.assertEquals(apiName, "map");
    }

    @Test
    public void testGetAPIProviderFromRESTAPI() throws Exception {
        String restAPI = "admin--map";
        String providerName = APIUtil.getAPIProviderFromRESTAPI(restAPI, null);

        Assert.assertEquals(providerName, "admin@carbon.super");

        restAPI = "user@test.com--map";
        providerName = APIUtil.getAPIProviderFromRESTAPI(restAPI, "test.com");
        Assert.assertEquals(providerName, "user@test.com");

        restAPI = "user-AT-test.com--map";
        providerName = APIUtil.getAPIProviderFromRESTAPI(restAPI, "test.com");
        Assert.assertEquals(providerName, "user@test.com");

    }

    @Test
    public void testGetHttpClient() throws Exception {
        Log log = Mockito.mock(Log.class);
        PowerMockito.mockStatic(LogFactory.class);
        Mockito.when(LogFactory.getLog(Mockito.any(Class.class))).thenReturn(log);

        SSLSocketFactory socketFactory = Mockito.mock(SSLSocketFactory.class);
        PowerMockito.mockStatic(SSLSocketFactory.class);
        Mockito.when(SSLSocketFactory.getSocketFactory()).thenReturn(socketFactory);

        ServiceReferenceHolderMockCreator holderMockCreator = new ServiceReferenceHolderMockCreator(1);
        ServiceReferenceHolderMockCreator.initContextService();

        HttpClient client = APIUtil.getHttpClient(3244, "http");

        Assert.assertNotNull(client);
        Scheme scheme = client.getConnectionManager().getSchemeRegistry().get("http");
        Assert.assertEquals(3244, scheme.getDefaultPort());

        client = APIUtil.getHttpClient(3244, "https");
        Assert.assertNotNull(client);
        scheme = client.getConnectionManager().getSchemeRegistry().get("https");
        Assert.assertEquals(3244, scheme.getDefaultPort());

        client = APIUtil.getHttpClient(-1, "http");
        Assert.assertNotNull(client);
        scheme = client.getConnectionManager().getSchemeRegistry().get("http");
        Assert.assertEquals(80, scheme.getDefaultPort());

        client = APIUtil.getHttpClient(-1, "https");
        Assert.assertNotNull(client);
        scheme = client.getConnectionManager().getSchemeRegistry().get("https");
        Assert.assertEquals(443, scheme.getDefaultPort());
    }

    @Test
    public void testGetHttpClientIgnoreHostNameVerify() throws Exception {
        Log log = Mockito.mock(Log.class);
        PowerMockito.mockStatic(LogFactory.class);
        Mockito.when(LogFactory.getLog(Mockito.any(Class.class))).thenReturn(log);

        SSLSocketFactory socketFactory = Mockito.mock(SSLSocketFactory.class);
        PowerMockito.mockStatic(SSLSocketFactory.class);
        Mockito.when(SSLSocketFactory.getSocketFactory()).thenReturn(socketFactory);

        ServiceReferenceHolderMockCreator holderMockCreator = new ServiceReferenceHolderMockCreator(1);
        ServiceReferenceHolderMockCreator.initContextService();
        
        System.setProperty("org.wso2.ignoreHostnameVerification", "true");
        HttpClient client = APIUtil.getHttpClient(3244, "https");

        Assert.assertNotNull(client);
    }

    /*
    @Test
    public void testGetHttpClientSSLVerifyClient() throws Exception {
        System.setProperty("carbon.home", "");

        Log log = Mockito.mock(Log.class);
        PowerMockito.mockStatic(LogFactory.class);
        Mockito.when(LogFactory.getLog(Mockito.any(Class.class))).thenReturn(log);

        SSLSocketFactory socketFactory = Mockito.mock(SSLSocketFactory.class);
        PowerMockito.mockStatic(SSLSocketFactory.class);
        Mockito.when(SSLSocketFactory.getSocketFactory()).thenReturn(socketFactory);

        ServiceReferenceHolderMockCreator holderMockCreator = new ServiceReferenceHolderMockCreator(1);
        ServiceReferenceHolderMockCreator.initContextService();

        TransportInDescription transportInDescription = holderMockCreator.getConfigurationContextServiceMockCreator().
                getContextMockCreator().getConfigurationMockCreator().getTransportInDescription();

        Parameter sslVerifyClient = Mockito.mock(Parameter.class);
        Mockito.when(transportInDescription.getParameter(APIConstants.SSL_VERIFY_CLIENT)).thenReturn(sslVerifyClient);
        Mockito.when(sslVerifyClient.getValue()).thenReturn(APIConstants.SSL_VERIFY_CLIENT_STATUS_REQUIRE);

        System.setProperty("org.wso2.ignoreHostnameVerification", "true");

        File keyStore = new File(Thread.currentThread().getContextClassLoader().
                getResource("wso2carbon.jks").getFile());

        ServerConfiguration serverConfiguration = Mockito.mock(ServerConfiguration.class);
        PowerMockito.mockStatic(CarbonUtils.class);
        Mockito.when(CarbonUtils.getServerConfiguration()).thenReturn(serverConfiguration);

        Mockito.when(serverConfiguration.getFirstProperty("Security.KeyStore.Location")).
                thenReturn(keyStore.getAbsolutePath());
        Mockito.when(serverConfiguration.getFirstProperty("Security.KeyStore.Password")).
                thenReturn("wso2carbon");

        InputStream inputStream = new FileInputStream(keyStore.getAbsolutePath());
        KeyStore keystore = KeyStore.getInstance("JKS");
        char[] pwd = "wso2carbon".toCharArray();
        keystore.load(inputStream, pwd);
        SSLContext sslcontext = SSLContexts.custom().loadTrustMaterial(keystore).useSSL().build();
        SSLContext.setDefault(sslcontext);

        HttpClient client = APIUtil.getHttpClient(3244, "https");

        Assert.assertNotNull(client);
    }
    */

    @Test
    public void testIsValidURL() throws Exception {
        String validURL = "http://fsdfsfd.sda";

        Assert.assertTrue(APIUtil.isValidURL(validURL));

        String invalidURL = "sadafvsdfwef";

        Assert.assertFalse(APIUtil.isValidURL(invalidURL));
        Assert.assertFalse(APIUtil.isValidURL(null));
    }

    @Test
    public void testGetRESTAPIScopesFromConfig() throws Exception {
        File siteConfFile = new File(Thread.currentThread().getContextClassLoader().
                getResource("tenant-conf.json").getFile());

        String tenantConfValue = FileUtils.readFileToString(siteConfFile);

        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(tenantConfValue);
        JSONObject restapiScopes = (JSONObject) json.get("RESTAPIScopes");

        Map<String, String> expectedScopes = new HashMap<String, String>();
        JSONArray scopes = (JSONArray) restapiScopes.get("Scope");

        for (Object scopeObj : scopes) {
            JSONObject scope = (JSONObject) scopeObj;
            String name = (String) scope.get("Name");
            String roles = (String) scope.get("Roles");
            expectedScopes.put(name, roles);
        }

        Map<String, String> restapiScopesFromConfig = APIUtil.getRESTAPIScopesFromConfig(restapiScopes);

        Assert.assertEquals(expectedScopes, restapiScopesFromConfig);
    }
}
