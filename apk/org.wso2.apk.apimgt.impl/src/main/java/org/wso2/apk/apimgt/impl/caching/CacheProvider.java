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

import org.wso2.apk.apimgt.impl.APIConstants;
import org.wso2.apk.apimgt.impl.APIManagerConfiguration;
import org.wso2.apk.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.base.ServerConfiguration;

import java.util.concurrent.TimeUnit;

public class CacheProvider<T> {

    public static CacheStore createTenantConfigCache() {

        String apimGWCacheExpiry = getApiManagerConfiguration().getFirstProperty(APIConstants.TOKEN_CACHE_EXPIRY);
        long expiryDuration = Long.parseLong(apimGWCacheExpiry);

        if (apimGWCacheExpiry.isEmpty()) {
            expiryDuration = getDefaultCacheTimeout();
        }
        return new CacheStore<String>(Long.parseLong(apimGWCacheExpiry), TimeUnit.SECONDS);
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
