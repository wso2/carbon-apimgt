/*
* Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.wso2.carbon.apimgt.impl.token;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.utils.ClaimCacheKey;
import org.wso2.carbon.apimgt.impl.utils.UserClaims;
import org.wso2.carbon.user.api.Claim;
import org.wso2.carbon.user.api.ClaimManager;
import org.wso2.carbon.user.api.ClaimMapping;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.SortedMap;
import java.util.TreeMap;
import javax.cache.Cache;
import javax.cache.CacheBuilder;
import javax.cache.CacheConfiguration;
import javax.cache.CacheManager;
import javax.cache.Caching;

import static org.wso2.carbon.apimgt.impl.token.ClaimsRetriever.DEFAULT_DIALECT_URI;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ServiceReferenceHolder.class, APIUtil.class, Caching.class})
public class DefaultClaimsRetrieverTestCase {
    private final int TENANT_ID = 6543;
    private final String USER_NAME = "john";

    private APIManagerConfigurationService apiManagerConfigurationService = Mockito
            .mock(APIManagerConfigurationService.class);
    private APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
    private ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
    private CacheManager cacheManager = Mockito.mock(CacheManager.class);
    private RealmService realmService = Mockito.mock(RealmService.class);
    private UserRealm userRealm = Mockito.mock(UserRealm.class);
    private ClaimManager claimManager = Mockito.mock(ClaimManager.class);
    private UserStoreManager userStoreManager = Mockito.mock(UserStoreManager.class);

    @Before
    public void setup() throws UserStoreException {
        PowerMockito.mockStatic(Caching.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        PowerMockito.when(Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER)).thenReturn(cacheManager);
        Mockito.when(apiManagerConfigurationService.getAPIManagerConfiguration()).thenReturn(apiManagerConfiguration);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfigurationService())
                .thenReturn(apiManagerConfigurationService);
        Mockito.when(serviceReferenceHolder.getRealmService()).thenReturn(realmService);
        Mockito.when(realmService.getTenantUserRealm(TENANT_ID)).thenReturn(userRealm);
        Mockito.when(userRealm.getClaimManager()).thenReturn(claimManager);
        Mockito.when(userRealm.getUserStoreManager()).thenReturn(userStoreManager);
    }

    @Test
    public void testInitWhenDialectUrlNotNull() {

        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.CONSUMER_DIALECT_URI))
                .thenReturn("http://wso2.org/claims_new");
        DefaultClaimsRetriever defaultClaimsRetriever = new DefaultClaimsRetriever();
        defaultClaimsRetriever.init();
        String dialectUri = defaultClaimsRetriever.getDialectURI("");
        Assert.assertNotNull(dialectUri);
        Assert.assertEquals("http://wso2.org/claims_new", dialectUri);
    }

    @Test
    public void testInitWhenDialectUrlIsNull() {

        DefaultClaimsRetriever defaultClaimsRetriever = new DefaultClaimsRetriever();
        defaultClaimsRetriever.init();
        String dialectUri = defaultClaimsRetriever.getDialectURI("");
        Assert.assertNotNull(dialectUri);
        Assert.assertEquals(DEFAULT_DIALECT_URI, dialectUri);
    }

    @Test
    public void testGetClaimsWhenCacheEmpty() throws Exception {

        DefaultClaimsRetriever defaultClaimsRetriever = new DefaultClaimsRetriever();
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.JWT_CLAIM_CACHE_EXPIRY)).thenReturn(null);
        Cache cache = Mockito.mock(Cache.class);
        Mockito.when(cacheManager.getCache(APIConstants.CLAIMS_APIM_CACHE)).thenReturn(cache);
        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.when(APIUtil.getTenantId(USER_NAME)).thenReturn(TENANT_ID);

        Claim claim1 = new Claim();
        claim1.setClaimUri("http://wso2.org/claim1");
        Claim claim2 = new Claim();
        claim2.setClaimUri("http://wso2.com/claim2");

        ClaimMapping[] cliams = new ClaimMapping[] { new ClaimMapping(claim1, "claim1"),
                new ClaimMapping(claim2, "claim2") };
        Mockito.when(claimManager.getAllClaimMappings(DEFAULT_DIALECT_URI)).thenReturn(cliams);

        SortedMap<String, String> claimValues = new TreeMap<String, String>();
        claimValues.put("claim1", "http://wso2.org/claim1");
        claimValues.put("claim2", "http://wso2.org/claim2");
        Mockito.when(userStoreManager.getUserClaimValues(Matchers.any(String.class), Matchers.any(String[].class),
                Matchers.any(String.class))).thenReturn(claimValues);
        SortedMap<String, String> claims = defaultClaimsRetriever.getClaims(USER_NAME);

        Assert.assertNotNull(claims);
        Assert.assertEquals(claimValues, claims);
    }

    @Test
    public void testGetClaimsWhenCacheNonEmpty() throws Exception{
        DefaultClaimsRetriever defaultClaimsRetriever = new DefaultClaimsRetriever();
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.JWT_CLAIM_CACHE_EXPIRY)).thenReturn("3600");
        CacheBuilder cacheBuilder = Mockito.mock(CacheBuilder.class);
        Mockito.when(cacheManager.createCacheBuilder(APIConstants.CLAIMS_APIM_CACHE)).thenReturn(cacheBuilder);
        Cache cache = Mockito.mock(Cache.class);

        Mockito.when(cacheBuilder.setStoreByValue(false)).thenReturn(cacheBuilder);
        Mockito.when(cacheBuilder.setExpiry(Matchers.any(CacheConfiguration.ExpiryType.class),Matchers.any(
                CacheConfiguration.Duration.class))).thenReturn(cacheBuilder);
        Mockito.when(cacheBuilder.build()).thenReturn(cache);
        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.when(APIUtil.getTenantId(USER_NAME)).thenReturn(TENANT_ID);

        SortedMap<String, String> claimValues = new TreeMap<String, String>();
        claimValues.put("claim1", "http://wso2.org/claim1");
        claimValues.put("claim2", "http://wso2.org/claim2");
        UserClaims userClaims = new UserClaims(claimValues);
        Mockito.when(cache.get(Matchers.any(ClaimCacheKey.class))).thenReturn(userClaims);
        SortedMap<String, String> claims = defaultClaimsRetriever.getClaims(USER_NAME);

        Assert.assertNotNull(claims);
        Assert.assertEquals(claimValues, claims);
    }
}
