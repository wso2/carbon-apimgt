/*
 *  Copyright (c) 2026, WSO2 LLC. (https://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.gateway.notifiers;

import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.gateway.internal.DataHolder;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIConstants.GatewayNotification.GatewayRegistrationResponse;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.EventHubConfigurationDto;
import org.wso2.carbon.apimgt.impl.dto.GatewayArtifactSynchronizerProperties;
import org.wso2.carbon.apimgt.impl.dto.GatewayNotificationConfiguration;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Checks that the GatewayNotifier heartbeat and registration close their per-call HTTP client and response (#21030).
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({APIUtil.class, ServiceReferenceHolder.class})
public class GatewayNotifierTest {

    private CloseableHttpClient httpClient;
    private CloseableHttpResponse httpResponse;
    private StatusLine statusLine;
    private GatewayNotifier gatewayNotifier;

    @Before
    public void setUp() throws Exception {
        EventHubConfigurationDto eventHubConfig = new EventHubConfigurationDto();
        eventHubConfig.setUsername("admin");
        eventHubConfig.setPassword("admin".toCharArray());
        eventHubConfig.setServiceUrl("https://localhost:9443/internal/data/v1");

        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        Mockito.when(apiManagerConfiguration.getGatewayNotificationConfiguration())
                .thenReturn(new GatewayNotificationConfiguration());
        Mockito.when(apiManagerConfiguration.getGatewayArtifactSynchronizerProperties())
                .thenReturn(Mockito.mock(GatewayArtifactSynchronizerProperties.class));
        Mockito.when(apiManagerConfiguration.getEventHubConfigurationDto()).thenReturn(eventHubConfig);

        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfiguration()).thenReturn(apiManagerConfiguration);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        Mockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);

        httpClient = Mockito.mock(CloseableHttpClient.class);
        httpResponse = Mockito.mock(CloseableHttpResponse.class);
        statusLine = Mockito.mock(StatusLine.class);
        Mockito.when(httpResponse.getStatusLine()).thenReturn(statusLine);
        PowerMockito.mockStatic(APIUtil.class);
        Mockito.when(APIUtil.getHttpClient(Mockito.anyInt(), Mockito.anyString())).thenReturn(httpClient);

        Constructor<GatewayNotifier> constructor = GatewayNotifier.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        gatewayNotifier = constructor.newInstance();
    }

    @After
    public void tearDown() {
        gatewayNotifier.stopHeartbeat();
        DataHolder.setGatewayRegistrationResponse(GatewayRegistrationResponse.NOT_RESPONDED);
    }

    @Test
    public void testHeartbeatClosesClientAndResponse() throws Exception {
        mockResponse(200, "{}");
        Mockito.when(httpClient.execute(Mockito.any(HttpPost.class))).thenReturn(httpResponse);

        runWorker("Heartbeat");

        Mockito.verify(httpClient).execute(Mockito.any(HttpPost.class));
        Mockito.verify(httpResponse).close();
        Mockito.verify(httpClient).close();
    }

    @Test
    public void testHeartbeatClosesClientAndResponseOnErrorStatus() throws Exception {
        mockResponse(500, "error");
        Mockito.when(httpClient.execute(Mockito.any(HttpPost.class))).thenReturn(httpResponse);

        runWorker("Heartbeat");

        Mockito.verify(httpResponse).close();
        Mockito.verify(httpClient).close();
    }

    @Test
    public void testHeartbeatClosesClientWhenRequestFails() throws Exception {
        Mockito.when(httpClient.execute(Mockito.any(HttpPost.class))).thenThrow(new IOException("connection refused"));

        runWorker("Heartbeat");

        Mockito.verify(httpClient).close();
    }

    @Test
    public void testRegistrationClosesClientAfterRetriesAndResponse() throws Exception {
        mockResponse(200, "{\"status\":\"" + APIConstants.GatewayNotification.STATUS_REGISTERED + "\"}");
        AtomicBoolean clientClosed = new AtomicBoolean(false);
        AtomicBoolean closedDuringRetries = new AtomicBoolean(false);
        Mockito.doAnswer(invocation -> {
            clientClosed.set(true);
            return null;
        }).when(httpClient).close();
        Mockito.when(APIUtil.executeHTTPRequestWithRetries(Mockito.any(HttpRequestBase.class),
                Mockito.any(HttpClient.class), Mockito.anyLong(), Mockito.anyInt(), Mockito.anyDouble()))
                .thenAnswer(invocation -> {
                    closedDuringRetries.set(clientClosed.get());
                    return httpResponse;
                });

        runWorker("GatewayRegistrationWorker");

        Assert.assertFalse("Client must stay open while retries run", closedDuringRetries.get());
        Assert.assertTrue(DataHolder.getInstance().isGatewayRegistered());
        Mockito.verify(httpResponse).close();
        Mockito.verify(httpClient).close();
    }

    @Test
    public void testRegistrationClosesClientWhenRetriesFail() throws Exception {
        Mockito.when(APIUtil.executeHTTPRequestWithRetries(Mockito.any(HttpRequestBase.class),
                Mockito.any(HttpClient.class), Mockito.anyLong(), Mockito.anyInt(), Mockito.anyDouble()))
                .thenThrow(new APIManagementException("retries exhausted"));

        runWorker("GatewayRegistrationWorker");

        Assert.assertFalse(DataHolder.getInstance().isGatewayRegistered());
        Mockito.verify(httpClient).close();
    }

    private void mockResponse(int statusCode, String body) {
        Mockito.when(statusLine.getStatusCode()).thenReturn(statusCode);
        Mockito.when(httpResponse.getEntity()).thenReturn(new StringEntity(body, ContentType.APPLICATION_JSON));
    }

    private void runWorker(String innerClassName) throws Exception {
        Class<?> workerClass = Class.forName(GatewayNotifier.class.getName() + "$" + innerClassName);
        Constructor<?> constructor = workerClass.getDeclaredConstructor(GatewayNotifier.class);
        constructor.setAccessible(true);
        ((Runnable) constructor.newInstance(gatewayNotifier)).run();
    }
}
