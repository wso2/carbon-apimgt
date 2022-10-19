/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
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
package org.wso2.apk.apimgt.impl.caching;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.StringUtils;
import org.wso2.apk.apimgt.impl.APIConstants;
import org.wso2.apk.apimgt.impl.APIManagerConfiguration;
import org.wso2.apk.apimgt.impl.config.APIMConfigService;
import org.wso2.apk.apimgt.impl.config.APIMConfigServiceImpl;
import org.wso2.apk.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.base.ServerConfiguration;

import java.util.concurrent.TimeUnit;

public class CacheProvider {

    static APIMConfigService apimConfigService = new APIMConfigServiceImpl();

    public static void createTenantConfigCache() {

        Cache<?, ?> cache = CacheManager.getCache(APIConstants.TENANT_CONFIG_CACHE_NAME);
        if (cache != null) {
            return;
        }
        initiateTenantConfigCache();
    }

    public static Cache<String, JsonObject> getTenantConfigCache() {

        Cache<?, ?> cache = CacheManager.getCache(APIConstants.TENANT_CONFIG_CACHE_NAME);
        if (cache != null) {
            return (Cache<String, JsonObject>) cache;
        }

        initiateTenantConfigCache();
        return (Cache<String, JsonObject>) CacheManager.getCache(APIConstants.TENANT_CONFIG_CACHE_NAME);
    }

    private static void initiateTenantConfigCache() {

        String apimGWCacheExpiry = getApiManagerConfiguration().getFirstProperty(APIConstants.TOKEN_CACHE_EXPIRY);
        long expiryDuration = Long.parseLong(apimGWCacheExpiry);

        if (apimGWCacheExpiry.isEmpty()) {
            expiryDuration = getDefaultCacheTimeout();
        }

        LoadingCache<String, JsonObject> tenantConfigCache =
                CacheBuilder.newBuilder()
                        .expireAfterAccess(expiryDuration, TimeUnit.SECONDS)
                        .concurrencyLevel(Runtime.getRuntime().availableProcessors())
                        .build(
                                new CacheLoader<String, JsonObject>() {
                                    @Override
                                    public JsonObject load(String key) throws Exception {

                                        String[] parts = key.split("_");
                                        if (parts.length > 0) {
                                            String organization = parts[0];
                                            String tenantConfig = apimConfigService.getTenantConfig(organization);
                                            if (StringUtils.isNotEmpty(tenantConfig)) {
                                                return new JsonParser().parse(tenantConfig).getAsJsonObject();
                                            }
                                        }
                                        return new JsonObject();
                                    }
                                }
                        );

        CacheManager.addCache(APIConstants.TENANT_CONFIG_CACHE_NAME, tenantConfigCache);
    }

    public static long getDefaultCacheTimeout() {
        if (ServerConfiguration.getInstance().getFirstProperty(APIConstants.DEFAULT_CACHE_TIMEOUT) != null) {
            return Long.parseLong(ServerConfiguration.getInstance().
                    getFirstProperty(APIConstants.DEFAULT_CACHE_TIMEOUT)) * 60;
        }
        return APIConstants.DEFAULT_TIMEOUT;
    }

    private static APIManagerConfiguration getApiManagerConfiguration() {
        return ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration();
    }
}
