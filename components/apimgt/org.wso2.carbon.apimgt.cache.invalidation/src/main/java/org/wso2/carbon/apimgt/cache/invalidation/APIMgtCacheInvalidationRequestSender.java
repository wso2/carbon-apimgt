/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.cache.invalidation;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.wso2.carbon.apimgt.cache.invalidation.internal.DataHolder;
import org.wso2.carbon.apimgt.impl.CacheInvalidationConfiguration;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.caching.impl.Util;
import org.wso2.carbon.databridge.commons.Event;

import java.util.Collections;

import javax.cache.CacheEntryInfo;
import javax.cache.CacheInvalidationRequestSender;
import javax.cache.event.CacheEntryCreatedListener;
import javax.cache.event.CacheEntryEvent;
import javax.cache.event.CacheEntryListenerException;
import javax.cache.event.CacheEntryRemovedListener;
import javax.cache.event.CacheEntryUpdatedListener;

public class APIMgtCacheInvalidationRequestSender implements CacheEntryRemovedListener, CacheEntryUpdatedListener,
        CacheEntryCreatedListener, CacheInvalidationRequestSender {

    CacheInvalidationConfiguration cacheInvalidationConfiguration;

    public APIMgtCacheInvalidationRequestSender(CacheInvalidationConfiguration cacheInvalidationConfiguration) {

        this.cacheInvalidationConfiguration = cacheInvalidationConfiguration;

    }

    public void send(CacheEntryInfo cacheInfo) {

        if (cacheInvalidationConfiguration.isEnabled() && DataHolder.getInstance().isStarted()) {
            boolean excludedCachePresent = false;
            for (String excludedCache : cacheInvalidationConfiguration.getExcludedCaches()) {
                if (cacheInfo.getCacheName().contains(excludedCache)) {
                    excludedCachePresent = true;
                    break;
                }
            }
            if (!excludedCachePresent) {
                Object[] objects = new Object[]{cacheInfo.getCacheManagerName(), cacheInfo.getCacheName(),
                        constructCacheKeyString(cacheInfo.getCacheKey()), cacheInfo.getTenantDomain(),
                        cacheInfo.getTenantId(),
                        cacheInvalidationConfiguration.getDomain(), DataHolder.getNodeId()};
                Event cacheInvalidationMessage =
                        new Event(cacheInvalidationConfiguration.getStream(), System.currentTimeMillis(), null, null,
                                objects);
                APIUtil.publishEvent(CachingConstants.CACHING_EVENT_PUBLISHER, Collections.emptyMap(),
                        cacheInvalidationMessage);
            }
        }
    }

    public void entryCreated(CacheEntryEvent cacheEntryEvent) throws CacheEntryListenerException {

        send(Util.createCacheInfo(cacheEntryEvent));
    }

    public void entryRemoved(CacheEntryEvent cacheEntryEvent) throws CacheEntryListenerException {

        send(Util.createCacheInfo(cacheEntryEvent));
    }

    public void entryUpdated(CacheEntryEvent cacheEntryEvent) throws CacheEntryListenerException {

        send(Util.createCacheInfo(cacheEntryEvent));
    }

    private String constructCacheKeyString(Object cacheKey) {

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(CachingConstants.TYPE, cacheKey.getClass().getName());
        String jsonString = new Gson().toJson(cacheKey);
        jsonObject.add(CachingConstants.VALUE, new JsonParser().parse(jsonString));
        return jsonObject.toString();
    }
}
