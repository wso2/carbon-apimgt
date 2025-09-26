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


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.utils.ClaimCacheKey;
import org.wso2.carbon.apimgt.impl.utils.UserClaims;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.cache.Cache;
import javax.cache.CacheConfiguration;
import javax.cache.Caching;
import java.util.SortedMap;
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
    
    private static final Log log = LogFactory.getLog(DefaultClaimsRetriever.class);

    private String dialectURI = DEFAULT_DIALECT_URI;

    private  boolean isClaimsCacheInitialized = false;
    /**
     * Reads the DialectURI of the ClaimURIs to be retrieved from api-manager.xml ->
     * JWTConfiguration -> ConsumerDialectURI.
     * If not configured it uses http://wso2.org/claims as default
     */
    public void init() {
        dialectURI = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().
                getAPIManagerConfiguration().getJwtConfigurationDto().getConsumerDialectUri();
        if (dialectURI == null) {
            dialectURI = DEFAULT_DIALECT_URI;
            if (log.isDebugEnabled()) {
                log.debug("Consumer dialect URI not configured, using default: " + DEFAULT_DIALECT_URI);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Consumer dialect URI configured as: " + dialectURI);
            }
        }
        log.info("DefaultClaimsRetriever initialized with dialect URI: " + dialectURI);
    }

    protected Cache getClaimsLocalCache() {
        String apimClaimsCacheExpiry = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().
                getAPIManagerConfiguration().getFirstProperty(APIConstants.JWT_CLAIM_CACHE_EXPIRY);
        if(!isClaimsCacheInitialized && apimClaimsCacheExpiry != null) {
            init();
            isClaimsCacheInitialized = true;
            if (log.isDebugEnabled()) {
                log.debug("Initializing claims cache with expiry: " + apimClaimsCacheExpiry + " seconds");
            }
           return Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER).
                    createCacheBuilder(APIConstants.CLAIMS_APIM_CACHE)
                   .setExpiry(CacheConfiguration.ExpiryType.MODIFIED, new CacheConfiguration.Duration(TimeUnit.SECONDS,
                           Long.parseLong(apimClaimsCacheExpiry)))
                   .setExpiry(CacheConfiguration.ExpiryType.ACCESSED, new CacheConfiguration.Duration(TimeUnit.SECONDS,
                           Long.parseLong(apimClaimsCacheExpiry))).setStoreByValue(false).build();
        }else {
           if (log.isDebugEnabled()) {
               log.debug("Retrieving existing claims cache instance");
           }
           return Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER).getCache(APIConstants.CLAIMS_APIM_CACHE);
        }
    }

    public SortedMap<String, String> getClaims(String endUserName) throws APIManagementException {
        if (endUserName == null) {
            log.warn("End user name is null while retrieving claims");
            return null;
        }
        
        if (log.isDebugEnabled()) {
            log.debug("Retrieving claims for user: " + endUserName);
        }
        
        String strEnabledJWTClaimCache = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                .getAPIManagerConfiguration().getFirstProperty(APIConstants.ENABLED_JWT_CLAIM_CACHE);
        boolean enabledJWTClaimCache = true;
        if (strEnabledJWTClaimCache != null) {
            enabledJWTClaimCache = Boolean.valueOf(strEnabledJWTClaimCache);
        }
        
        SortedMap<String, String> claimValues;
        int tenantId = APIUtil.getTenantId(endUserName);
        String tenantAwareUserName = MultitenantUtils.getTenantAwareUsername(endUserName);
        
        if (log.isDebugEnabled()) {
            log.debug("Processing claims for tenant aware user: " + tenantAwareUserName + " in tenant: " + tenantId);
        }
        
        //check in local cache
        String key = endUserName + ':' + tenantId;
        ClaimCacheKey cacheKey = new ClaimCacheKey(key);
        Object result = null;
        if (enabledJWTClaimCache) {
            result = getClaimsLocalCache().get(cacheKey);
        }
        if (result != null) {
            if (log.isDebugEnabled()) {
                log.debug("Claims retrieved from cache for user: " + endUserName);
            }
            return ((UserClaims) result).getClaimValues();
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Claims not found in cache, retrieving from user store for user: " + endUserName);
            }
            claimValues = APIUtil.getClaims(endUserName, tenantId, dialectURI);
            UserClaims userClaims = new UserClaims(claimValues);
            //add to cache
            if (enabledJWTClaimCache) {
                getClaimsLocalCache().put(cacheKey, userClaims);
                if (log.isDebugEnabled()) {
                    log.debug("Claims cached for user: " + endUserName);
                }
            }
            
            if (log.isDebugEnabled() && claimValues != null) {
                log.debug("Successfully retrieved " + claimValues.size() + " claims for user: " + endUserName);
            }
            return claimValues;
        }
    }

    /**
     * Always returns the ConsumerDialectURI configured in api-manager.xml
     */
    public String getDialectURI(String endUserName) {
        return dialectURI;
    }
}
