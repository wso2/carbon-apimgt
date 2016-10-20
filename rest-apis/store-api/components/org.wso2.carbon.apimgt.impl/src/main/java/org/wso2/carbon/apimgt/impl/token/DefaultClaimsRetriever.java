/*
*Copyright (c) 2005-2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/
package org.wso2.carbon.apimgt.impl.token;


import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.utils.ClaimCacheKey;
import org.wso2.carbon.apimgt.impl.utils.UserClaims;
import org.wso2.carbon.user.api.ClaimManager;
import org.wso2.carbon.user.api.ClaimMapping;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.cache.Cache;
import javax.cache.CacheConfiguration;
import javax.cache.Caching;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

/**
 * This class is the default implementation of ClaimsRetriever.
 * It reads user claim values from the default carbon user store.
 * The user claims are encoded to the JWT in the natural order of the claimURIs.
 * To engage this class its fully qualified class name should be mentioned under
 * api-manager.xml -> JWTConfiguration -> ClaimsRetrieverImplClass
 */
public class DefaultClaimsRetriever implements ClaimsRetriever {
    //TODO refactor caching implementation

    private String dialectURI = DEFAULT_DIALECT_URI;

    private  boolean isClaimsCacheInitialized = false;
    /**
     * Reads the DialectURI of the ClaimURIs to be retrieved from api-manager.xml ->
     * JWTConfiguration -> ConsumerDialectURI.
     * If not configured it uses http://wso2.org/claims as default
     */
    public void init() {
        dialectURI = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().
                getAPIManagerConfiguration().getFirstProperty(APIConstants.CONSUMER_DIALECT_URI);
        if (dialectURI == null) {
            dialectURI = DEFAULT_DIALECT_URI;
        }
    }

    protected Cache getClaimsLocalCache() {
        String apimClaimsCacheExpiry = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().
                getAPIManagerConfiguration().getFirstProperty(APIConstants.JWT_CLAIM_CACHE_EXPIRY);
        if(!isClaimsCacheInitialized && apimClaimsCacheExpiry != null) {init();
            isClaimsCacheInitialized = true;
           return Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER).
                    createCacheBuilder(APIConstants.CLAIMS_APIM_CACHE)
                   .setExpiry(CacheConfiguration.ExpiryType.MODIFIED, new CacheConfiguration.Duration(TimeUnit.SECONDS,
                           Long.parseLong(apimClaimsCacheExpiry)))
                   .setExpiry(CacheConfiguration.ExpiryType.ACCESSED, new CacheConfiguration.Duration(TimeUnit.SECONDS,
                           Long.parseLong(apimClaimsCacheExpiry))).setStoreByValue(false).build();
        }else {
           return Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER).getCache(APIConstants.CLAIMS_APIM_CACHE);
        }
    }

    public SortedMap<String, String> getClaims(String endUserName) throws APIManagementException {
        SortedMap<String, String> claimValues;
        try {
            if (endUserName != null) {
                int tenantId = APIUtil.getTenantId(endUserName);
                String tenantAwareUserName = MultitenantUtils.getTenantAwareUsername(endUserName);
                //check in local cache
                String key = endUserName + ':' + tenantId;
                ClaimCacheKey cacheKey = new ClaimCacheKey(key);
                Object result = getClaimsLocalCache().get(cacheKey);
                if (result != null) {
                    return ((UserClaims) result).getClaimValues();
                } else {
                    ClaimManager claimManager = ServiceReferenceHolder.getInstance().getRealmService().
                            getTenantUserRealm(tenantId).getClaimManager();
                    //Claim[] claims = claimManager.getAllClaims(dialectURI);
                    ClaimMapping[] claims = claimManager.getAllClaimMappings(dialectURI);
                    String[] claimURIs = claimMappingtoClaimURIString(claims);
                    UserStoreManager userStoreManager = ServiceReferenceHolder.getInstance().getRealmService().
                            getTenantUserRealm(tenantId).getUserStoreManager();

                    claimValues = new TreeMap(userStoreManager.getUserClaimValues(tenantAwareUserName, claimURIs,null));
                    UserClaims userClaims = new UserClaims(claimValues);
                    //add to cache
                    getClaimsLocalCache().put(cacheKey, userClaims);
                    return claimValues;
                }
            }
        } catch (UserStoreException e) {
            throw new APIManagementException("Error while retrieving user claim values from " + "user store", e);
        }
        return null;
    }

    /**
     * Always returns the ConsumerDialectURI configured in api-manager.xml
     */
    public String getDialectURI(String endUserName) {
        return dialectURI;
    }

    /**
     * Helper method to convert array of <code>Claim</code> object to
     * array of <code>String</code> objects corresponding to the ClaimURI values.
     */
    private String[] claimMappingtoClaimURIString(ClaimMapping[] claims) {
        String[] temp = new String[claims.length];
        for (int i = 0; i < claims.length; i++) {
            temp[i] = claims[i].getClaim().getClaimUri();
       
        }
        return temp;
    }
}
