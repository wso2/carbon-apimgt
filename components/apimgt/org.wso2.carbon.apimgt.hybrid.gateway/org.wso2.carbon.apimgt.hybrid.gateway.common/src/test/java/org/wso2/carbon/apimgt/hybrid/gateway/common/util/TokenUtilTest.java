/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.apimgt.hybrid.gateway.common.util;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.hybrid.gateway.common.config.ConfigManager;
import org.wso2.carbon.apimgt.hybrid.gateway.common.dto.OAuthApplicationInfoDTO;
import org.wso2.carbon.apimgt.hybrid.gateway.common.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.hybrid.gateway.common.TestBase;
import org.wso2.carbon.apimgt.hybrid.gateway.common.dto.AccessTokenDTO;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.any;

@RunWith(PowerMockRunner.class)
@PrepareForTest({APIManagerConfiguration.class, ServiceReferenceHolder.class, APIManagerConfigurationService.class,
        TokenUtil.class, HttpClients.class, HttpRequestUtil.class, LogFactory.class, APIUtil.class, ConfigManager.class})
public class TokenUtilTest extends TestBase {

    private static final String KEY = "Key";
    private static final String VALUE = "Value";

    @Before
    public void init() {
        Log log = Mockito.mock(Log.class);
        PowerMockito.mockStatic(LogFactory.class);
        PowerMockito.when(LogFactory.getLog(Mockito.any(Class.class))).thenReturn(log);
    }

    @Test
    public void getBasicAuthHeaderValue() throws Exception {
        String headerValue = TokenUtil.getBasicAuthHeaderValue(KEY, VALUE.toCharArray());
        Assert.assertNotNull(headerValue);
        String encodedString = headerValue.split("Basic ")[1];
        byte[] decodedBytes = Base64.decodeBase64(encodedString);
        String decodedString = new String(decodedBytes);
        Assert.assertEquals(KEY + ":" + VALUE, decodedString);
    }

    @Test
    public void registerClient() throws Exception {
        Map<String, String> configMap = new HashMap<>();
        configMap.put(APIConstants.API_KEY_VALIDATOR_USERNAME, "Username");
        configMap.put(APIConstants.API_KEY_VALIDATOR_PASSWORD, "Password");
        mockAPIMConfiguration(configMap);
        mockAppCreationCall();
        PowerMockito.mockStatic(ConfigManager.class);
        ConfigManager configManager = Mockito.mock(ConfigManager.class);
        PowerMockito.when(ConfigManager.getConfigManager()).thenReturn(configManager);
        PowerMockito.mockStatic(APIUtil.class);
        HttpClient httpClient = Mockito.mock(HttpClient.class);
        PowerMockito.when(APIUtil.getHttpClient(Mockito.anyInt(), Mockito.anyString())).thenReturn(httpClient);
        Mockito.when(configManager.getProperty(any(String.class))).thenReturn("https://localhost:9443");
        OAuthApplicationInfoDTO infoDTO = TokenUtil.registerClient();
        Assert.assertNotNull(infoDTO);
    }

    @Test
    public void generateAccessToken() throws Exception {
        Map<String, String> configMap = new HashMap<>();
        configMap.put(APIConstants.API_KEY_VALIDATOR_USERNAME, "Username");
        configMap.put(APIConstants.API_KEY_VALIDATOR_PASSWORD, "Password");
        mockAPIMConfiguration(configMap);
        mockTokenGenCall();
        PowerMockito.mockStatic(ConfigManager.class);
        ConfigManager configManager = Mockito.mock(ConfigManager.class);
        PowerMockito.when(ConfigManager.getConfigManager()).thenReturn(configManager);
        PowerMockito.mockStatic(APIUtil.class);
        HttpClient httpClient = Mockito.mock(HttpClient.class);
        PowerMockito.when(APIUtil.getHttpClient(Mockito.anyInt(), Mockito.anyString())).thenReturn(httpClient);
        Mockito.when(configManager.getProperty(any(String.class))).thenReturn("https://localhost:8243");
        AccessTokenDTO tokenDTO = TokenUtil.generateAccessToken("ClientId", "ClientSecret".toCharArray(), "Scope");
        Assert.assertNotNull(tokenDTO);
    }

    public void mockAppCreationCall() throws Exception {
        PowerMockito.mockStatic(HttpClients.class);
        CloseableHttpClient httpClient = Mockito.mock(CloseableHttpClient.class);
        PowerMockito.when(HttpClients.createDefault()).thenReturn(httpClient);
        PowerMockito.mockStatic(HttpRequestUtil.class);
        PowerMockito.when(HttpRequestUtil.executeHTTPMethodWithRetry(any(HttpClient.class), any(HttpGet.class),
                any(Integer.class))).thenReturn("{\n" +
                "    \"callBackURL\": \"www.google.lk\",\n" +
                "    \"jsonString\":\n \"{}\",\n" +
                "    \"clientName\": null,\n" +
                "    \"clientId\": \"HfEl1jJPdg5tbtrxhAwybN05QGoa\",\n" +
                "    \"clientSecret\": \"l6c0aoLcWR3fwezHhc7XoGOht5Aa\"\n" +
                "}");
    }

    public void mockTokenGenCall() throws Exception {
        PowerMockito.mockStatic(HttpClients.class);
        CloseableHttpClient httpClient = Mockito.mock(CloseableHttpClient.class);
        PowerMockito.when(HttpClients.createDefault()).thenReturn(httpClient);
        PowerMockito.mockStatic(HttpRequestUtil.class);
        PowerMockito.when(HttpRequestUtil.executeHTTPMethodWithRetry(any(HttpClient.class), any(HttpGet.class),
                any(Integer.class))).thenReturn("{\n" +
                "    \"scope\":\"apim:api_view\",\n" +
                "    \"token_type\":\"Bearer\",\n" +
                "    \"expires_in\":3600,\n" +
                "    \"refresh_token\":\"33c3be152ebf0030b3fb76f2c1f80bf8\",\n" +
                "    \"access_token\":\"292ff0fd256814536baca0926f483c8d\"\n" +
                "}");
    }

    private void mockAPIMConfiguration(Map<String, String> configMap) {

        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);

        APIManagerConfigurationService apiManagerConfigurationService = Mockito
                .mock(APIManagerConfigurationService.class);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfigurationService())
                .thenReturn(apiManagerConfigurationService);
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        Mockito.when(apiManagerConfigurationService.getAPIManagerConfiguration()).thenReturn(apiManagerConfiguration);

        for (String key : configMap.keySet()) {
            Mockito.when(apiManagerConfiguration.getFirstProperty(key))
                    .thenReturn(configMap.get(key));
        }
    }

}
