/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.gateway.handlers.security.service;

import org.wso2.carbon.apimgt.impl.caching.CacheInvalidationService;
import org.wso2.carbon.apimgt.gateway.service.CacheInvalidationServiceImpl;
import org.wso2.carbon.mediation.initializer.AbstractServiceBusAdmin;

public class APIAuthenticationService extends AbstractServiceBusAdmin {

    CacheInvalidationService cacheInvalidationService;

    public APIAuthenticationService() {
        cacheInvalidationService = new CacheInvalidationServiceImpl();
    }

    public void invalidateResourceCache(String apiContext, String apiVersion, String resourceURLContext,
			String httpVerb) {
        cacheInvalidationService.invalidateResourceCache(apiContext, apiVersion, resourceURLContext, httpVerb);
    }


    /**
     * This method is to invalidate an access token which is already in gateway cache.
     *
     * @param accessToken The access token to be remove from the cache
     */
    public void invalidateKey(String accessToken) {
        cacheInvalidationService.invalidateKey(accessToken);
    }

    /**
     * This method is to invalidate a username which is already in gateway cache.
     *
     * @param username The username to be remove from the cache
     */
    public void invalidateCachedUsername(String username) {
        cacheInvalidationService.invalidateCachedUsername(username);
    }

    /**
     * This method is to invalidate usernames which are already in gateway cache.
     *
     * @param username_list The usernames to be remove from the cache
     */
    public void invalidateCachedUsernames(String[] username_list) {
        cacheInvalidationService.invalidateCachedUsernames(username_list);
    }


    /**
     * Invalidate the cached access tokens from the API Gateway. This method is supposed to be used when you have to
     * remove a bulk of access tokens from the cache. For example, a removal of a subscription would require the
     * application's active access tokens to be removed from the cache. These access tokens can reside in different
     * tenant caches since the subscription owning tenant and API owning tenant can be different.
     * @param accessTokens String[] - The access tokens to be cleared from the cache.
     */
    public void invalidateCachedTokens(String[] accessTokens){
        cacheInvalidationService.invalidateCachedTokens(accessTokens);
    }

}
