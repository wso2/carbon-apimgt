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

package org.wso2.carbon.apimgt.rest.api.common.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.wso2.carbon.apimgt.core.models.AccessTokenInfo;
import org.wso2.carbon.apimgt.rest.api.common.util.InterceptorUtil;

import java.util.concurrent.TimeUnit;

/**
 * Singleton instance holding the token cache data.
 */

public class TokenCache {

    private LoadingCache<String, AccessTokenInfo> tokenCache;
    // TODO : Need to get these values from a config (ex: deployment.yaml).
    private int cacheTimeout = 1;
    private int cacheSize = 100;

    private TokenCache() {
        tokenCache = CacheBuilder.newBuilder().maximumSize(cacheSize).expireAfterAccess(cacheTimeout, TimeUnit.MINUTES)
                .build(new TokenCacheLoader());
    }

    /**
     * Method to get the instance of the TokenCache.
     *
     * @return {@link TokenCache} instance
     */
    public static TokenCache getInstance() {
        return TokenCacheHolder.INSTANCE;
    }

    public LoadingCache<String, AccessTokenInfo> getTokenCache() {
        return tokenCache;
    }

    /**
     * This is an inner class to hold the instance of the TokenCache.
     * ref: Initialization-on-demand holder idiom
     */
    private static class TokenCacheHolder {

        private static final TokenCache INSTANCE = new TokenCache();
    }

    static class TokenCacheLoader extends CacheLoader<String, AccessTokenInfo> {

        @Override public AccessTokenInfo load(String accessToken) throws Exception {
            return InterceptorUtil.getValidatedTokenResponse(accessToken);
        }
    }
}
