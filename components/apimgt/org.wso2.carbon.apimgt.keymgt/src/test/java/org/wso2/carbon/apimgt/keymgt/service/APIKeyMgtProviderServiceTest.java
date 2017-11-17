/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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
package org.wso2.carbon.apimgt.keymgt.service;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.APIInfoDTO;
import org.wso2.carbon.apimgt.impl.dto.APIKeyInfoDTO;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Caching.class, ApiMgtDAO.class })
public class APIKeyMgtProviderServiceTest {
    private final String USER_NAME = "admin";
    private final String CONSUMER_KEY = "Har2MjbxeMg3ysWEudjOKnXb3pAa";
    private ApiMgtDAO apiMgtDAO;

    @Before
    public void Init() throws Exception {
        apiMgtDAO = Mockito.mock(ApiMgtDAO.class);
        PowerMockito.mockStatic(ApiMgtDAO.class);
        PowerMockito.when(ApiMgtDAO.getInstance()).thenReturn(apiMgtDAO);
    }

    @Test
    public void testGetIssuedKeyInfo() throws Exception {
        APIKeyMgtProviderService apiKeyMgtProviderService = new APIKeyMgtProviderService();
        APIKeyInfoDTO[] apiKeyInfoDTOS = new APIKeyInfoDTO[1];
        Mockito.when(apiMgtDAO.getSubscribedUsersForAPI(Mockito.any(APIInfoDTO.class))).thenReturn(apiKeyInfoDTOS);
        Assert.assertEquals(apiKeyInfoDTOS, apiKeyMgtProviderService.getIssuedKeyInfo(new APIInfoDTO()));
    }

    @Test
    public void testGetAPIsOfUser() throws Exception {
        APIKeyMgtProviderService apiKeyMgtProviderService = new APIKeyMgtProviderService();
        APIInfoDTO[] apiInfoDTOS = new APIInfoDTO[2];
        apiInfoDTOS[0] = new APIInfoDTO();
        apiInfoDTOS[1] = new APIInfoDTO();
        apiInfoDTOS[0].setProviderId(USER_NAME);
        apiInfoDTOS[1].setProviderId("otherUser");

        Mockito.when(apiMgtDAO.getSubscribedAPIsOfUser(USER_NAME)).thenReturn(apiInfoDTOS);
        APIInfoDTO[] apiInfoResult = apiKeyMgtProviderService.getAPIsOfUser(USER_NAME, USER_NAME);
        Assert.assertEquals(1, apiInfoResult.length);
        Assert.assertEquals(USER_NAME, apiInfoResult[0].getProviderId());
    }

    @Test
    public void testActivateAccessTokens() throws Exception {
        APIKeyMgtProviderService apiKeyMgtProviderService = new APIKeyMgtProviderService();
        apiKeyMgtProviderService.activateAccessTokens(new String[] { USER_NAME }, new APIInfoDTO());
        Mockito.verify(apiMgtDAO, Mockito.atLeastOnce())
                .changeAccessTokenStatus(Mockito.anyString(), Mockito.any(APIInfoDTO.class), Mockito.anyString());
    }

    @Test
    public void testBlockAccessTokens() throws Exception {
        APIKeyMgtProviderService apiKeyMgtProviderService = new APIKeyMgtProviderService();
        apiKeyMgtProviderService.blockAccessTokens(new String[] { USER_NAME }, new APIInfoDTO());
        Mockito.verify(apiMgtDAO, Mockito.atLeastOnce())
                .changeAccessTokenStatus(Mockito.anyString(), Mockito.any(APIInfoDTO.class), Mockito.anyString());
    }

    @Test
    public void testRevokeAccessTokens() throws Exception {
        APIKeyMgtProviderService apiKeyMgtProviderService = new APIKeyMgtProviderService();
        apiKeyMgtProviderService.revokeAccessTokens(new String[] { USER_NAME }, new APIInfoDTO());
        Mockito.verify(apiMgtDAO, Mockito.atLeastOnce())
                .changeAccessTokenStatus(Mockito.anyString(), Mockito.any(APIInfoDTO.class), Mockito.anyString());

    }

    @Test
    public void testRemoveScopeCache() throws Exception {
        PowerMockito.mockStatic(Caching.class);
        CacheManager cacheManager = Mockito.mock(CacheManager.class);
        PowerMockito.when(Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER)).thenReturn(cacheManager);
        Cache<Object, Object> cache = Mockito.mock(Cache.class);
        Mockito.when(cacheManager.getCache(APIConstants.APP_SCOPE_CACHE)).thenReturn(cache);
        Mockito.doNothing().when(cache).removeAll();

        APIKeyMgtProviderService apiKeyMgtProviderService = new APIKeyMgtProviderService();

        apiKeyMgtProviderService.removeScopeCache(new String[] { CONSUMER_KEY });
        Mockito.verify(cacheManager, Mockito.atLeastOnce()).getCache(APIConstants.APP_SCOPE_CACHE);

        PowerMockito.when(Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER))
                .thenThrow(RuntimeException.class);
        try {
            apiKeyMgtProviderService.removeScopeCache(null);
        } catch (Exception e) {
            Assert.fail("Exception cannot be throw");
        }

        try {
            apiKeyMgtProviderService.removeScopeCache(new String[] {});
        } catch (Exception e) {
            Assert.fail("Exception cannot be throw");
        }

    }
}
