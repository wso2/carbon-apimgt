/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*
*/

package org.wso2.carbon.apimgt.gateway.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.impl.APIConstants;

import javax.cache.Cache;
import javax.cache.Caching;

/**
 * This Class is to Cache the schemas.
 */
public class SchemaCacheUtils {
    private static Logger log = LoggerFactory.getLogger(SchemaCacheUtils.class);

    /**
     * Get the Schemas from the Cache.
     *
     * @return schema cache object.
     */
    protected static Cache getSchemaCache() {
        Cache cache = null;
        try {
            javax.cache.CacheManager manager =
                    Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER);
            cache = manager.getCache(APIMgtGatewayConstants.API_SWAGGER_SCHEMA);
        } catch (NullPointerException e) {
            log.error(
                    "Did not found valid API Validation Information cache configuration. " +
                            e.getMessage(), e);
        }
        return cache;
    }

    /**
     * Put the Schemas into Cache.
     *
     * @param key   Schema name.
     * @param value Schema content.
     */
    public static void putCache(String key, String value) {
        getSchemaCache().put(key, value);

    }

    /**
     * Get Schema for given name.
     *
     * @param key Schema name.
     * @return Schema content.
     */
    public static String getCacheSchema(String key) {
        return (String) getSchemaCache().get(key);

    }
}
