/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.hybrid.gateway.status.checker;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.hybrid.gateway.common.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.hybrid.gateway.common.util.HttpRequestUtil;
import org.wso2.carbon.apimgt.hybrid.gateway.common.util.OnPremiseGatewayConstants;
import org.wso2.carbon.apimgt.hybrid.gateway.common.util.TokenUtil;
import org.wso2.carbon.apimgt.hybrid.gateway.status.checker.util.StatusCheckerConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.any;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ StatusChecker.class, HttpClients.class, HttpRequestUtil.class, ServiceReferenceHolder.class })
public class StatusCheckerTest {

    private static final String TENANT_DOMAIN = "tenant";
    private static final String TOKEN = "token";
    private static final String USERNAME = "username@email.com";
    private static final String PASSWORD = "Password";
    private static final String PAYLOAD_STR =
            "{\"tenantDomain\":\"" + TENANT_DOMAIN + "\",\"token\":\"" + TOKEN.toString() + "\"}";
    private static final String PING_URL = "https://test.com/ping";

    @Test
    public void createPayload() throws Exception {
        StatusChecker statusChecker = new StatusChecker(TOKEN, PING_URL);
        String payload = statusChecker.getPingingPayload(TENANT_DOMAIN, TOKEN);
        Assert.assertNotNull(payload);
        Assert.assertEquals(PAYLOAD_STR, payload);
    }

    @Test
    public void callPingAPI() throws Exception {
        String successMessage = "Pinged successfully.";
        PowerMockito.mockStatic(HttpClients.class);
        CloseableHttpClient httpClient = Mockito.mock(CloseableHttpClient.class);
        PowerMockito.when(HttpClients.createDefault()).thenReturn(httpClient);
        PowerMockito.mockStatic(HttpRequestUtil.class);
        PowerMockito.when(HttpRequestUtil.executeHTTPMethodWithRetry(any(HttpClient.class), any(HttpGet.class),
                                                                     any(Integer.class))).thenReturn(successMessage);
        StatusChecker statusChecker = new StatusChecker(TOKEN, PING_URL);
        statusChecker.callPingAPIEndpoint(USERNAME, PASSWORD.toCharArray(), PAYLOAD_STR, PING_URL);
    }

    @Test
    public void run() throws Exception {
        Map<String, String> configMap = new HashMap<>();
        String username = USERNAME + OnPremiseGatewayConstants.USERNAME_SEPARATOR + TENANT_DOMAIN;
        configMap.put(APIConstants.API_KEY_VALIDATOR_USERNAME, username);
        configMap.put(APIConstants.API_KEY_VALIDATOR_PASSWORD, PASSWORD);
        mockAPIMConfiguration(configMap);
        String[] usernameParts = username.split(OnPremiseGatewayConstants.USERNAME_SEPARATOR);
        String tenantDomain = usernameParts[2];
        Assert.assertEquals("tenant", tenantDomain);
        List<String> lines = new ArrayList<>();
        lines.add(StatusCheckerConstants.PING_API_URL + StatusCheckerConstants.VALUE_SEPARATOR +
                          PING_URL);
        callPingAPI();
    }

    @Test
    public void createPostRequest() throws Exception {
        StatusChecker statusChecker = new StatusChecker(TOKEN, PING_URL);
        String authHeaderValue = TokenUtil.getBasicAuthHeaderValue(USERNAME, PASSWORD.toCharArray());
        HttpPost httpPost = statusChecker.createPostRequest(PING_URL, PAYLOAD_STR, authHeaderValue);
        Assert.assertEquals("POST", httpPost.getMethod());
        Assert.assertEquals(OnPremiseGatewayConstants.CONTENT_TYPE_APPLICATION_JSON,
                httpPost.getFirstHeader(OnPremiseGatewayConstants.CONTENT_TYPE_HEADER).getValue());
    }

    private void mockAPIMConfiguration(Map<String, String> configMap) {
        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);

        APIManagerConfigurationService apiManagerConfigurationService = Mockito
                                                                                .mock(APIManagerConfigurationService
                                                                                              .class);
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
