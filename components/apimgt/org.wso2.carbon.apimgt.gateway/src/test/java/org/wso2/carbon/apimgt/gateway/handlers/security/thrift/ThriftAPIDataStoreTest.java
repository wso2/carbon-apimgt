/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.gateway.handlers.security.thrift;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;

import java.util.ArrayList;

import static org.powermock.api.mockito.PowerMockito.doThrow;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * ThriftAPIDataStore test class
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest(ThriftKeyValidatorClientPool.class)
public class ThriftAPIDataStoreTest {

    private String apiContext = "testAPI";
    private String apiVersion = "v1";
    private String apiKey = "12345";
    private String requiredAuthenticationLevel = "OAuth";
    private String clientDomain = "carbon.super";
    private String matchingResource = "/foo";
    private String httpVerb = "GET";
    private APIKeyValidationInfoDTO apiKeyValidationInfoDTO;
    private ThriftKeyValidatorClientPool thriftKeyValidatorClientPool;
    private ThriftKeyValidatorClient client;
    private ThriftAPIDataStore thriftAPIDataStore;

    @Before
    public void init() throws Exception {
        thriftKeyValidatorClientPool = Mockito.mock(ThriftKeyValidatorClientPool.class);
        client = Mockito.mock(ThriftKeyValidatorClient.class);
        PowerMockito.mockStatic(ThriftKeyValidatorClientPool.class);
        thriftAPIDataStore = new ThriftAPIDataStore();
        apiKeyValidationInfoDTO = new APIKeyValidationInfoDTO();
        Whitebox.setInternalState(ThriftAPIDataStore.class, "clientPool", thriftKeyValidatorClientPool);
    }

    @Test
    public void testGetAPIKeyValidationInfo() throws Exception {

        PowerMockito.when(ThriftKeyValidatorClientPool.getInstance()).thenReturn(thriftKeyValidatorClientPool);
        PowerMockito.when(thriftKeyValidatorClientPool.get()).thenReturn(client);
        apiKeyValidationInfoDTO.setAuthorized(true);
        PowerMockito.when(client.getAPIKeyData(apiContext, apiVersion, apiKey, requiredAuthenticationLevel,
                clientDomain, matchingResource, httpVerb)).thenReturn(apiKeyValidationInfoDTO);
        Assert.assertNotNull(thriftAPIDataStore.getAPIKeyData(apiContext, apiVersion,
                apiKey, requiredAuthenticationLevel, clientDomain, matchingResource, httpVerb));

    }

    @Test(expected = APISecurityException.class)

    public void testGetAPIKeyValidationInfoWhenClientIsNull() throws Exception {

        PowerMockito.when(ThriftKeyValidatorClientPool.getInstance()).thenReturn(thriftKeyValidatorClientPool);
        PowerMockito.when(thriftKeyValidatorClientPool.get()).thenReturn(null);
        apiKeyValidationInfoDTO.setAuthorized(true);
        PowerMockito.when(client.getAPIKeyData(apiContext, apiVersion, apiKey, requiredAuthenticationLevel,
                clientDomain, matchingResource, httpVerb)).thenReturn(apiKeyValidationInfoDTO);
        thriftAPIDataStore.getAPIKeyData(apiContext, apiVersion,
                apiKey, requiredAuthenticationLevel, clientDomain, matchingResource, httpVerb);
    }

    @Test(expected = APISecurityException.class)
    public void testGetAPIKeyValidationInfoWhenFailedToRetrieveThriftClientFromPool() throws Exception {
        ThriftKeyValidatorClientPool thriftKeyValidatorClientPool = Mockito.mock(ThriftKeyValidatorClientPool.class);
        PowerMockito.mockStatic(ThriftKeyValidatorClientPool.class);
        when(ThriftKeyValidatorClientPool.getInstance()).thenReturn(thriftKeyValidatorClientPool);
        doThrow(new Exception()).when(thriftKeyValidatorClientPool).get();
        ThriftAPIDataStore thriftAPIDataStore = new ThriftAPIDataStore();
        thriftAPIDataStore.getAPIKeyData(apiContext, apiVersion, apiKey, requiredAuthenticationLevel, clientDomain,
                matchingResource, httpVerb);
    }

    @Test(expected = APISecurityException.class)
    public void testGetAPIKeyValidationInfoWhenFailedToRetrieveAPIKeyDataFromKM() throws Exception {
        when(thriftKeyValidatorClientPool.get()).thenReturn(client);
        Mockito.doThrow(APISecurityException.class).when(client).getAPIKeyData(apiContext, apiVersion,
                apiKey, requiredAuthenticationLevel, clientDomain, matchingResource, httpVerb);
        thriftAPIDataStore.getAPIKeyData(apiContext, apiVersion, apiKey, requiredAuthenticationLevel, clientDomain,
                matchingResource, httpVerb);
    }

    @Test(expected = APISecurityException.class)
    public void testGetAPIKeyValidationInfoWhenFailedToGetAndReleaseThriftClientToPool() throws Exception {
        when(thriftKeyValidatorClientPool.get()).thenReturn(client);
        Mockito.doThrow(APISecurityException.class).when(client).getAPIKeyData(apiContext, apiVersion,
                apiKey, requiredAuthenticationLevel, clientDomain, matchingResource, httpVerb);
        doThrow(new Exception()).when(thriftKeyValidatorClientPool).release(client);
        //Should throw exception and discontinue the flow
        thriftAPIDataStore.getAPIKeyData(apiContext, apiVersion,
                apiKey, requiredAuthenticationLevel, clientDomain, matchingResource, httpVerb);
    }

    @Test
    public void testGetAPIKeyValidationInfoWhenFailedToReleaseThriftClientToPool() throws Exception {
        when(thriftKeyValidatorClientPool.get()).thenReturn(client);
        when(client.getAPIKeyData(apiContext, apiVersion, apiKey, requiredAuthenticationLevel,
                clientDomain, matchingResource, httpVerb)).thenReturn(apiKeyValidationInfoDTO);
        doThrow(new Exception()).when(thriftKeyValidatorClientPool).release(client);
        //Should not throw exception and discontinue the flow
        Assert.assertNotNull(thriftAPIDataStore.getAPIKeyData(apiContext, apiVersion,
                apiKey, requiredAuthenticationLevel, clientDomain, matchingResource, httpVerb));
    }

    @Test
    public void testGetAllURITemplates() throws Exception {
        when(thriftKeyValidatorClientPool.get()).thenReturn(client);
        when(client.getAllURITemplates(apiContext, apiVersion)).thenReturn(new
                ArrayList<URITemplate>());
        Assert.assertNotNull(thriftAPIDataStore.getAllURITemplates(apiContext, apiVersion));
    }

    @Test(expected = APISecurityException.class)
    public void testGetAllURITemplatesWhenThriftClientIsNull() throws Exception {
        when(thriftKeyValidatorClientPool.get()).thenReturn(null);
        when(client.getAllURITemplates(apiContext, apiVersion)).thenReturn(new
                ArrayList<URITemplate>());
        thriftAPIDataStore.getAllURITemplates(apiContext, apiVersion);
    }

    @Test(expected = APISecurityException.class)
    public void testGetAllURITemplatesWhenFailedToRetrieveThriftClientFromPool() throws Exception {
        doThrow(new Exception()).when(thriftKeyValidatorClientPool).get();
        thriftAPIDataStore.getAllURITemplates(apiContext, apiVersion);
    }

    @Test(expected = APISecurityException.class)
    public void testGetAllURITemplatesWhenFailedToRetrieveAPIKeyDataFromKM() throws Exception {
        when(thriftKeyValidatorClientPool.get()).thenReturn(client);
        Mockito.doThrow(APISecurityException.class).when(client).getAllURITemplates(apiContext, apiVersion);
        thriftAPIDataStore.getAllURITemplates(apiContext, apiVersion);
    }

    @Test
    public void testGetAllURITemplatesoWhenFailedToReleaseThriftClientToPool() throws Exception {
        when(thriftKeyValidatorClientPool.get()).thenReturn(client);
        when(client.getAllURITemplates(apiContext, apiVersion)).thenReturn(new ArrayList<URITemplate>());
        doThrow(new Exception()).when(thriftKeyValidatorClientPool).release(client);
        //Should not throw exception and discontinue the flow
        Assert.assertNotNull(thriftAPIDataStore.getAllURITemplates(apiContext, apiVersion));
    }

    @Test(expected = APISecurityException.class)
    public void testGetAllURITemplatesoWhenFailedToGetAndReleaseThriftClientToPool() throws Exception {
        when(thriftKeyValidatorClientPool.get()).thenReturn(client);
        Mockito.doThrow(APISecurityException.class).when(client).getAllURITemplates(apiContext, apiVersion);
        doThrow(new Exception()).when(thriftKeyValidatorClientPool).release(client);
        //Should throw exception and discontinue the flow
        thriftAPIDataStore.getAllURITemplates(apiContext, apiVersion);
    }

    @Test
    public void testCleanUp() throws Exception {
        when(thriftKeyValidatorClientPool.get()).thenReturn(client);
        thriftAPIDataStore.cleanup();
    }

}
