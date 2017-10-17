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

package org.wso2.carbon.apimgt.impl.publishers;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIStore;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;

@RunWith(PowerMockRunner.class)
@PrepareForTest({EntityUtils.class, CarbonUtils.class, ServiceReferenceHolder.class, URL.class, HttpURLConnection.class,
        FileUtils.class, WSO2APIPublisher.class})
public class WSO2APIPublisherTestCase {
    @Test
    public void testPublishToStoreWithNullStoreArguments() throws Exception {
        //Error path - When username or password or endpoint is not defined
        WSO2APIPublisher wso2APIPublisher = new WSO2APIPublisher();
        API api = Mockito.mock(API.class);
        APIStore store = new APIStore();
        store.setDisplayName("Sample");
        try {
            wso2APIPublisher.publishToStore(api, store);
            Assert.fail("APIManagement exception not thrown for error scenario");
        } catch (APIManagementException e) {
            String msg = "External APIStore endpoint URL or credentials are not defined. " +
                    "Cannot proceed with publishing API to the APIStore - " + store.getDisplayName();
            Assert.assertEquals(msg, e.getMessage());
        }
    }

    @Test
    public void testPublishToStoreWithAccessFailures() throws Exception {
        //Error path - When accessing the external store got failed
        HttpClient defaultHttpClient = Mockito.mock(HttpClient.class);
        HttpResponse httpResponse = Mockito.mock(HttpResponse.class);
        HttpEntity entity = Mockito.mock(HttpEntity.class);
        WSO2APIPublisher wso2APIPublisher = new WSO2APIPublisherWrapper(defaultHttpClient);
        APIIdentifier identifier = new APIIdentifier("P1_API1_v1.0.0");
        API api = new API(identifier);
        APIStore store = new APIStore();
        store.setDisplayName("Sample");
        store.setUsername("admin");
        store.setPassword("admin");
        store.setEndpoint("https://localhost:9292/sample");
        Mockito.doReturn(httpResponse).when(defaultHttpClient).execute(Mockito.any(HttpPost.class), Mockito.any(HttpContext.class));
        Mockito.doReturn(entity).when(httpResponse).getEntity();
        PowerMockito.mockStatic(EntityUtils.class);
        PowerMockito.when(EntityUtils.toString((HttpEntity)Mockito.anyObject(), Mockito.anyString())).thenReturn("{\"error\" : true, \"message\" : \"Login failed. Please recheck the username and password and try again..\"}");
        try {
            wso2APIPublisher.publishToStore(api, store);
            Assert.fail("APIManagement exception not thrown for error scenario");
        } catch (APIManagementException e) {
            String errorMsg = "\"Login failed. Please recheck the username and password and try again..\"";
            String msg = " Authentication with external APIStore - " + store.getDisplayName()
                    + "  failed due to " + errorMsg + ".API publishing to APIStore- " +
                    store.getDisplayName() + " failed.";
            Assert.assertEquals(msg, e.getMessage());
        }
    }

    @Test(expected = APIManagementException.class)
    public void testPublishToStoreWithErrorInAddingAPI() throws Exception {
        //Error path - When an adding API failed
        HttpClient defaultHttpClient = Mockito.mock(HttpClient.class);
        HttpResponse httpResponse = Mockito.mock(HttpResponse.class);
        HttpEntity entity = Mockito.mock(HttpEntity.class);
        WSO2APIPublisher wso2APIPublisher = new WSO2APIPublisherWrapper(defaultHttpClient);

        APIIdentifier identifier = new APIIdentifier("P1_API1_v1.0.0");
        API api = new API(identifier);
        api.setThumbnailUrl("/thumbnail");
        APIStore store = new APIStore();
        store.setDisplayName("Sample");
        store.setUsername("admin");
        store.setPassword("admin");
        store.setEndpoint("https://localhost:9292/store");

        Mockito.doReturn(httpResponse).when(defaultHttpClient).execute(Mockito.any(HttpPost.class), Mockito.any(HttpContext.class));
        Mockito.doReturn(entity).when(httpResponse).getEntity();
        PowerMockito.mockStatic(EntityUtils.class);
        PowerMockito.when(EntityUtils.toString((HttpEntity)Mockito.anyObject(), Mockito.anyString())).thenReturn("{\"error\" : false}");
        ServerConfiguration serverConfig = Mockito.mock(ServerConfiguration.class);
        PowerMockito.mockStatic(CarbonUtils.class);
        PowerMockito.when(CarbonUtils.getServerConfiguration()).thenReturn(serverConfig);
        PowerMockito.when(CarbonUtils.getTransportProxyPort(Mockito.any(ConfigurationContext.class), Mockito.anyString())).thenReturn(9292);
        Mockito.doReturn("").when(serverConfig).getFirstProperty("MgtProxyContextPath");
        Mockito.doReturn("localhost").when(serverConfig).getFirstProperty("HostName");
        ConfigurationContextService configurationContextService = Mockito.mock(ConfigurationContextService.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(ServiceReferenceHolder.getContextService()).thenReturn(configurationContextService);
        ConfigurationContext configurationContext = Mockito.mock(ConfigurationContext.class);
        Mockito.doReturn(configurationContext).when(configurationContextService).getServerConfigContext();
        wso2APIPublisher.publishToStore(api, store);
    }

    @Test
    public void testPublishToStore() throws Exception {
        //Happy path
        HttpClient defaultHttpClient = Mockito.mock(HttpClient.class);
        HttpResponse httpResponse = Mockito.mock(HttpResponse.class);
        HttpEntity entity = Mockito.mock(HttpEntity.class);
        WSO2APIPublisher wso2APIPublisher = new WSO2APIPublisherWrapper(defaultHttpClient);

        APIIdentifier identifier = new APIIdentifier("P1_API1_v1.0.0");
        API api = new API(identifier);
        api.setThumbnailUrl("/thumbnail");
        api.setContext("/store");
        api.setBusinessOwner("user");
        api.setBusinessOwnerEmail("user@gmail.com");
        api.setTechnicalOwner("admin");
        api.setTechnicalOwnerEmail("admin@gmail.com");
        api.setVisibility("Public");
        api.setVisibleRoles("admin");
        api.setEndpointSecured(true);
        api.setEndpointAuthDigest(true);
        api.setEndpointUTUsername("admin");
        api.setEndpointUTPassword("admin");
        APIStore store = new APIStore();
        store.setDisplayName("Sample");
        store.setUsername("admin");
        store.setPassword("admin");
        store.setEndpoint("https://localhost:9292/store");

        Mockito.doReturn(httpResponse).when(defaultHttpClient).execute(Mockito.any(HttpPost.class), Mockito.any(HttpContext.class));
        Mockito.doReturn(entity).when(httpResponse).getEntity();
        PowerMockito.mockStatic(EntityUtils.class);
        PowerMockito.when(EntityUtils.toString((HttpEntity)Mockito.anyObject(), Mockito.anyString())).thenReturn("{\"error\" : false}");
        ServerConfiguration serverConfig = Mockito.mock(ServerConfiguration.class);
        PowerMockito.mockStatic(CarbonUtils.class);
        PowerMockito.when(CarbonUtils.getServerConfiguration()).thenReturn(serverConfig);
        PowerMockito.when(CarbonUtils.getTransportProxyPort(Mockito.any(ConfigurationContext.class), Mockito.anyString())).thenReturn(9292);
        Mockito.doReturn("").when(serverConfig).getFirstProperty("MgtProxyContextPath");
        Mockito.doReturn("localhost").when(serverConfig).getFirstProperty("HostName");
        ConfigurationContextService configurationContextService = Mockito.mock(ConfigurationContextService.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(ServiceReferenceHolder.getContextService()).thenReturn(configurationContextService);
        ConfigurationContext configurationContext = Mockito.mock(ConfigurationContext.class);
        Mockito.doReturn(configurationContext).when(configurationContextService).getServerConfigContext();
        URL url = PowerMockito.mock(URL.class);
        PowerMockito.whenNew(URL.class).withAnyArguments().thenReturn(url);
        HttpURLConnection urlConnection = PowerMockito.mock(HttpURLConnection.class);
        PowerMockito.when(url.openConnection()).thenReturn(urlConnection);
        PowerMockito.when(urlConnection.getResponseCode()).thenReturn(200);
        File file = PowerMockito.mock(File.class);
        PowerMockito.whenNew(File.class).withAnyArguments().thenReturn(file);
        PowerMockito.when(file.exists()).thenReturn(true);
        PowerMockito.mockStatic(FileUtils.class);
        PowerMockito.doNothing().when(FileUtils.class);
        FileUtils.copyURLToFile((URL)Mockito.anyObject(),(File) Mockito.anyObject());
        RealmService realmService = Mockito.mock(RealmService.class);
        Mockito.when(serviceReferenceHolder.getRealmService()).thenReturn(realmService);
        TenantManager tenantManager = Mockito.mock(TenantManager.class);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        Mockito.when(tenantManager.getTenantId(Mockito.anyString())).thenReturn(1234);
        APIManagerConfigurationService apiManagerConfigurationService = Mockito.mock(APIManagerConfigurationService.class);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfigurationService()).thenReturn(apiManagerConfigurationService);
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        Mockito.when(apiManagerConfigurationService.getAPIManagerConfiguration()).thenReturn(apiManagerConfiguration);
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.EXTERNAL_API_STORES + "." + APIConstants.EXTERNAL_API_STORES_STORE_URL)).thenReturn("http://localhost:9292/redirect");
        boolean published = wso2APIPublisher.publishToStore(api, store);
        Assert.assertTrue(published);
    }

    @Test
    public void testDeleteFromStoreWithNullStoreArguments() throws Exception {
        //Error path - When username or password or endpoint is not defined
        WSO2APIPublisher wso2APIPublisher = new WSO2APIPublisher();
        APIIdentifier apiIdentifier = Mockito.mock(APIIdentifier.class);
        APIStore store = new APIStore();
        store.setDisplayName("Sample");
        try {
            wso2APIPublisher.deleteFromStore(apiIdentifier, store);
            Assert.fail("APIManagement exception not thrown for error scenario");
        } catch (APIManagementException e) {
            String msg = "External APIStore endpoint URL or credentials are not defined. " +
                    "Cannot proceed with deleting API from the APIStore - " + store.getDisplayName();
            Assert.assertEquals(msg, e.getMessage());
        }
    }

    @Test
    public void testDeleteFromStore() throws Exception {
        //Happy path
        HttpClient defaultHttpClient = Mockito.mock(HttpClient.class);
        HttpResponse httpResponse = Mockito.mock(HttpResponse.class);
        HttpEntity entity = Mockito.mock(HttpEntity.class);
        WSO2APIPublisher wso2APIPublisher = new WSO2APIPublisherWrapper(defaultHttpClient);
        APIIdentifier identifier = new APIIdentifier("P1_API1_v1.0.0");
        APIStore store = new APIStore();
        store.setDisplayName("Sample");
        store.setUsername("admin");
        store.setPassword("admin");
        store.setEndpoint("https://localhost:9292/store");
        Mockito.doReturn(httpResponse).when(defaultHttpClient).execute(Mockito.any(HttpPost.class), Mockito.any(HttpContext.class));
        Mockito.doReturn(entity).when(httpResponse).getEntity();
        PowerMockito.mockStatic(EntityUtils.class);
        PowerMockito.when(EntityUtils.toString((HttpEntity)Mockito.anyObject(), Mockito.anyString())).thenReturn("{\"error\" : false}");
        boolean deleted = wso2APIPublisher.deleteFromStore(identifier, store);
        Assert.assertTrue(deleted);
    }

    @Test
    public void testUpdateToStoreWithNullStoreArguments() throws Exception {
        //Error path - When username or password or endpoint is not defined
        WSO2APIPublisher wso2APIPublisher = new WSO2APIPublisher();
        API api = Mockito.mock(API.class);
        APIStore store = new APIStore();
        store.setDisplayName("Sample");
        try {
            wso2APIPublisher.updateToStore(api, store);
            Assert.fail("APIManagement exception not thrown for error scenario");
        } catch (APIManagementException e) {
            String msg = "External APIStore endpoint URL or credentials are not defined.Cannot proceed with " +
                    "publishing API to the APIStore - " + store.getDisplayName();
            Assert.assertEquals(msg, e.getMessage());
        }
    }

    @Test
    public void testUpdateToStore() throws Exception {
        //Happy path
        HttpClient defaultHttpClient = Mockito.mock(HttpClient.class);
        HttpResponse httpResponse = Mockito.mock(HttpResponse.class);
        HttpEntity entity = Mockito.mock(HttpEntity.class);
        WSO2APIPublisher wso2APIPublisher = new WSO2APIPublisherWrapper(defaultHttpClient);
        APIIdentifier identifier = new APIIdentifier("P1_API1_v1.0.0");
        API api = new API(identifier);
        APIStore store = new APIStore();
        store.setDisplayName("Sample");
        store.setUsername("admin");
        store.setPassword("admin");
        store.setEndpoint("https://localhost:9292/store");
        Mockito.doReturn(httpResponse).when(defaultHttpClient).execute(Mockito.any(HttpPost.class), Mockito.any(HttpContext.class));
        Mockito.doReturn(entity).when(httpResponse).getEntity();
        PowerMockito.mockStatic(EntityUtils.class);
        PowerMockito.when(EntityUtils.toString((HttpEntity)Mockito.anyObject(), Mockito.anyString())).thenReturn("{\"error\" : false}");
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        RealmService realmService = Mockito.mock(RealmService.class);
        Mockito.when(serviceReferenceHolder.getRealmService()).thenReturn(realmService);
        TenantManager tenantManager = Mockito.mock(TenantManager.class);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        Mockito.when(tenantManager.getTenantId(Mockito.anyString())).thenReturn(1234);
        APIManagerConfigurationService apiManagerConfigurationService = Mockito.mock(APIManagerConfigurationService.class);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfigurationService()).thenReturn(apiManagerConfigurationService);
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        Mockito.when(apiManagerConfigurationService.getAPIManagerConfiguration()).thenReturn(apiManagerConfiguration);
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.EXTERNAL_API_STORES + "." + APIConstants.EXTERNAL_API_STORES_STORE_URL)).thenReturn("http://localhost:9292/redirect");
        boolean updated = wso2APIPublisher.updateToStore(api, store);
        Assert.assertTrue(updated);
    }

    @Test
    public void testIsAPIAvailableWithNullStoreArguments() throws Exception {
        //Error path - When username or password or endpoint is not defined
        WSO2APIPublisher wso2APIPublisher = new WSO2APIPublisher();
        API api = Mockito.mock(API.class);
        APIStore store = new APIStore();
        store.setDisplayName("Sample");
        try {
            wso2APIPublisher.isAPIAvailable(api, store);
            Assert.fail("APIManagement exception not thrown for error scenario");
        } catch (APIManagementException e) {
            String msg = "External APIStore endpoint URL or credentials are not defined. " +
                    "Cannot proceed with checking API availability from the APIStore - "
                    + store.getDisplayName();
            Assert.assertEquals(msg, e.getMessage());
        }
    }

    @Test
    public void testIsAPIAvailable() throws Exception {
        //Happy path
        HttpClient defaultHttpClient = Mockito.mock(HttpClient.class);
        HttpResponse httpResponse = Mockito.mock(HttpResponse.class);
        HttpEntity entity = Mockito.mock(HttpEntity.class);
        WSO2APIPublisher wso2APIPublisher = new WSO2APIPublisherWrapper(defaultHttpClient);
        APIIdentifier identifier = new APIIdentifier("P1_API1_v1.0.0");
        API api = new API(identifier);
        APIStore store = new APIStore();
        store.setDisplayName("Sample");
        store.setUsername("admin");
        store.setPassword("admin");
        store.setEndpoint("https://localhost:9292/store");
        Mockito.doReturn(httpResponse).when(defaultHttpClient).execute(Mockito.any(HttpPost.class), Mockito.any(HttpContext.class));
        Mockito.doReturn(entity).when(httpResponse).getEntity();
        PowerMockito.mockStatic(EntityUtils.class);
        PowerMockito.when(EntityUtils.toString((HttpEntity)Mockito.anyObject(), Mockito.anyString())).thenReturn("{\"error\" : false}");
        boolean available = wso2APIPublisher.isAPIAvailable(api, store);
        Assert.assertTrue(available);
    }

    @Test
    public void testCreateVersionedAPIToStoreWithNullStoreArguments() throws Exception {
        //Error path - When username or password or endpoint is not defined
        WSO2APIPublisher wso2APIPublisher = new WSO2APIPublisher();
        API api = Mockito.mock(API.class);
        APIStore store = new APIStore();
        store.setDisplayName("Sample");
        String version = "v1.0.0";
        try {
            wso2APIPublisher.createVersionedAPIToStore(api, store, version);
            Assert.fail("APIManagement exception not thrown for error scenario");
        } catch (APIManagementException e) {
            String msg = "External APIStore endpoint URL or credentials are not defined. Cannot proceed with " +
                    "publishing API to the APIStore - " + store.getDisplayName();
            Assert.assertEquals(msg, e.getMessage());
        }
    }

    @Test
    public void testCreateVersionedAPIToStore() throws Exception {
        //Happy path
        HttpClient defaultHttpClient = Mockito.mock(HttpClient.class);
        HttpResponse httpResponse = Mockito.mock(HttpResponse.class);
        HttpEntity entity = Mockito.mock(HttpEntity.class);
        WSO2APIPublisher wso2APIPublisher = new WSO2APIPublisherWrapper(defaultHttpClient);
        APIIdentifier identifier = new APIIdentifier("P1_API1_v1.0.0");
        API api = new API(identifier);
        APIStore store = new APIStore();
        store.setDisplayName("Sample");
        store.setUsername("admin");
        store.setPassword("admin");
        store.setEndpoint("https://localhost:9292/store");
        String version = "v1.0.0";
        Mockito.doReturn(httpResponse).when(defaultHttpClient).execute(Mockito.any(HttpPost.class), Mockito.any(HttpContext.class));
        Mockito.doReturn(entity).when(httpResponse).getEntity();
        PowerMockito.mockStatic(EntityUtils.class);
        PowerMockito.when(EntityUtils.toString((HttpEntity)Mockito.anyObject(), Mockito.anyString())).thenReturn("{\"error\" : false}");
        boolean published = wso2APIPublisher.createVersionedAPIToStore(api, store, version);
        Assert.assertTrue(published);
    }

}
