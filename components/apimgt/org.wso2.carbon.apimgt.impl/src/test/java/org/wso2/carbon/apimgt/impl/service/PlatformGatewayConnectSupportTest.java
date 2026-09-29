/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.apimgt.impl.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.api.model.VHost;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;

import java.util.HashMap;
import java.util.Map;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ApiMgtDAO.class})
public class PlatformGatewayConnectSupportTest {

    @Test
    public void testResolveConnectGatewayIdIsDeterministic() {
        String first = PlatformGatewayServiceImpl.resolveConnectGatewayId("token-id-123");
        String second = PlatformGatewayServiceImpl.resolveConnectGatewayId("token-id-123");
        Assert.assertNotNull(first);
        Assert.assertEquals(first, second);
    }

    @Test
    public void testResolveStorageOrganizationIdUsesRequestOrg() throws Exception {
        PowerMockito.mockStatic(ApiMgtDAO.class);
        ApiMgtDAO apiMgtDAO = Mockito.mock(ApiMgtDAO.class);
        PowerMockito.when(ApiMgtDAO.getInstance()).thenReturn(apiMgtDAO);

        Environment env = new Environment();
        env.setGatewayType(APIConstants.WSO2_API_PLATFORM_GATEWAY);
        Mockito.when(apiMgtDAO.getEnvironment("carbon.super", "gw-1")).thenReturn(env);

        String storageOrg = PlatformGatewayServiceImpl.resolveStorageOrganizationId("carbon.super", "gw-1");
        Assert.assertEquals("carbon.super", storageOrg);
    }

    @Test
    public void testResolveStorageOrganizationIdReturnsNullWhenNotInRequestOrg() throws Exception {
        PowerMockito.mockStatic(ApiMgtDAO.class);
        ApiMgtDAO apiMgtDAO = Mockito.mock(ApiMgtDAO.class);
        PowerMockito.when(ApiMgtDAO.getInstance()).thenReturn(apiMgtDAO);

        Mockito.when(apiMgtDAO.getEnvironment("carbon.super", "gw-1")).thenReturn(null);

        Assert.assertNull(PlatformGatewayServiceImpl.resolveStorageOrganizationId("carbon.super", "gw-1"));
    }

    @Test
    public void testBuildGatewayBaseUrlFromHttpVHostWithPath() {
        VHost vhost = new VHost();
        vhost.setHost("gw");
        vhost.setHttpPort(8280);
        vhost.setHttpsPort(VHost.DEFAULT_HTTPS_PORT);
        vhost.setHttpContext("/base");

        Assert.assertEquals("http://gw:8280/base",
                PlatformGatewayServiceImpl.buildGatewayBaseUrlFromVHost(vhost));
    }

    @Test
    public void testBuildGatewayBaseUrlFromHttpsVHostWithPath() {
        VHost vhost = new VHost();
        vhost.setHost("gw");
        vhost.setHttpsPort(8443);
        vhost.setHttpPort(VHost.DEFAULT_HTTP_PORT);
        vhost.setHttpContext("/base");

        Assert.assertEquals("https://gw:8443/base",
                PlatformGatewayServiceImpl.buildGatewayBaseUrlFromVHost(vhost));
    }

    @Test
    public void testResolveGatewayBaseUrlPrefersStoredValue() {
        Environment env = new Environment();
        Map<String, String> additional = new HashMap<>();
        additional.put(APIConstants.GatewayNotification.GATEWAY_BASE_URL, "http://gw:8280/base");
        env.setAdditionalProperties(additional);

        Assert.assertEquals("http://gw:8280/base", PlatformGatewayServiceImpl.resolveGatewayBaseUrl(env));
    }

    @Test
    public void testResolveInvocationUrlsForHttpConfiguredGateway() {
        Environment env = new Environment();
        env.setGatewayType(APIConstants.WSO2_API_PLATFORM_GATEWAY);
        Map<String, String> additional = new HashMap<>();
        additional.put(APIConstants.GatewayNotification.GATEWAY_BASE_URL, "http://localhost:8443");
        env.setAdditionalProperties(additional);

        Map<String, String> urls = PlatformGatewayServiceImpl.resolveInvocationUrlsForTransports(env, "http,https");

        Assert.assertEquals("http://localhost:8443", urls.get(APIConstants.HTTP_PROTOCOL));
        Assert.assertFalse(urls.containsKey(APIConstants.HTTPS_PROTOCOL));
    }

    @Test
    public void testResolveInvocationUrlsForHttpConfiguredGatewayHttpsTransportOnly() {
        Environment env = new Environment();
        env.setGatewayType(APIConstants.WSO2_API_PLATFORM_GATEWAY);
        Map<String, String> additional = new HashMap<>();
        additional.put(APIConstants.GatewayNotification.GATEWAY_BASE_URL, "http://localhost:8443");
        env.setAdditionalProperties(additional);

        Map<String, String> urls = PlatformGatewayServiceImpl.resolveInvocationUrlsForTransports(env, "https");

        Assert.assertEquals("http://localhost:8443", urls.get(APIConstants.HTTP_PROTOCOL));
        Assert.assertFalse(urls.containsKey(APIConstants.HTTPS_PROTOCOL));
    }

    @Test
    public void testResolveInvocationUrlsForHttpsConfiguredGateway() {
        Environment env = new Environment();
        env.setGatewayType(APIConstants.WSO2_API_PLATFORM_GATEWAY);
        Map<String, String> additional = new HashMap<>();
        additional.put(APIConstants.GatewayNotification.GATEWAY_BASE_URL, "https://localhost:8443");
        env.setAdditionalProperties(additional);

        Map<String, String> urls = PlatformGatewayServiceImpl.resolveInvocationUrlsForTransports(env, "https");

        Assert.assertEquals("https://localhost:8443", urls.get(APIConstants.HTTPS_PROTOCOL));
        Assert.assertFalse(urls.containsKey(APIConstants.HTTP_PROTOCOL));
    }

    @Test
    public void testResolveStorageOrganizationIdReturnsNullForBlankGatewayId() throws APIManagementException {
        Assert.assertNull(PlatformGatewayServiceImpl.resolveStorageOrganizationId("carbon.super", " "));
    }
}
