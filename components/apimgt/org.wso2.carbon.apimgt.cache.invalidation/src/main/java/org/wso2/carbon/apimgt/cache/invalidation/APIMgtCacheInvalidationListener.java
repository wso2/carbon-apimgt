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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.cache.invalidation.internal.DataHolder;
import org.wso2.carbon.apimgt.impl.CacheInvalidationConfiguration;
import org.wso2.carbon.caching.impl.CacheImpl;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Topic;

import static org.wso2.carbon.caching.impl.CachingConstants.CLEAR_ALL_PREFIX;

/**
 * This class Used to Listen the JMS events.
 */
public class APIMgtCacheInvalidationListener implements MessageListener {

    private static final Log log = LogFactory.getLog(APIMgtCacheInvalidationListener.class);
    private final CacheInvalidationConfiguration cacheInvalidationConfiguration;

    public APIMgtCacheInvalidationListener(CacheInvalidationConfiguration cacheInvalidationConfiguration) {

        this.cacheInvalidationConfiguration = cacheInvalidationConfiguration;

    }

    public void onMessage(Message message) {

        try {
            if (message != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Event received in JMS Event Receiver - " + message);
                }
                Topic jmsDestination = (Topic) message.getJMSDestination();
                if (cacheInvalidationConfiguration.getCacheInValidationTopic()
                        .equalsIgnoreCase(jmsDestination.getTopicName())) {
                    if (message instanceof MapMessage) {
                        MapMessage mapMessage = (MapMessage) message;
                        Map<String, Object> map = new HashMap<String, Object>();
                        Enumeration enumeration = mapMessage.getMapNames();
                        while (enumeration.hasMoreElements()) {
                            String key = (String) enumeration.nextElement();
                            map.put(key, mapMessage.getObject(key));
                        }
                        handleCacheInvalidationMessage(map);
                    } else {
                        log.warn("Event dropped due to unsupported message type " + message.getClass());
                    }
                }
            } else {
                log.warn("Dropping the empty/null event received through jms receiver");
            }
        } catch (JMSException e) {
            log.error("JMSException occurred when processing the received message ", e);
        }
    }

    private void handleCacheInvalidationMessage(Map map) {

        String cacheManagerName = (String) map.get("cacheManagerName");
        String cacheName = (String) map.get("cacheName");
        String cacheKey = (String) map.get("cacheKey");
        String tenantDomain = (String) map.get("tenantDomain");
        int tenantId = (Integer) map.get("tenantId");
        String clusterDomain = (String) map.get("clusterDomain");
        String nodeId = (String) map.get("nodeId");
        if (!DataHolder.getNodeId().equals(nodeId) &&
                cacheInvalidationConfiguration.getDomain().equals(clusterDomain)) {
            if (log.isDebugEnabled()) {
                log.debug("Processing cache invalidation for cache: " + cacheName + ", tenant: " + tenantDomain +
                          ", from node: " + nodeId);
            }
            try {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
                carbonContext.setTenantId(tenantId);
                carbonContext.setTenantDomain(tenantDomain);
                CacheManager cacheManager = Caching.getCacheManagerFactory().getCacheManager(cacheManagerName);
                Cache<Object, Object> cache = cacheManager.getCache(cacheName);
                if (cache == null) {
                    log.warn("Cache not found for invalidation: " + cacheName);
                    return;
                }
                Object cacheKeyObject = constructCacheKeyObject(cacheKey);
                if (cache instanceof CacheImpl) {
                    if (CLEAR_ALL_PREFIX.equals(cacheKeyObject)) {
                        ((CacheImpl) cache).removeAllLocal();
                        if (log.isDebugEnabled()) {
                            log.debug("All entries removed from cache: " + cacheName);
                        }
                    } else {
                        ((CacheImpl) cache).removeLocal(cacheKeyObject);
                        if (log.isDebugEnabled()) {
                            log.debug("Cache entry removed from cache: " + cacheName);
                        }
                    }
                } else {
                    log.warn("Cache implementation not supported for local removal: " + cacheName);
                }
            } catch (ClassNotFoundException e) {
                log.error("Error while constructing cache key object for cache: " + cacheName, e);
            } catch (Exception e) {
                log.error("Error while processing cache invalidation for cache: " + cacheName, e);
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Cache invalidation ignored - same node: " + DataHolder.getNodeId().equals(nodeId) +
                          ", domain match: " + cacheInvalidationConfiguration.getDomain().equals(clusterDomain));
            }
        }
    }

    private Object constructCacheKeyObject(String cacheKey) throws ClassNotFoundException {

        JsonElement parsedValue = new JsonParser().parse(cacheKey);
        if (parsedValue instanceof JsonObject) {
            JsonObject jsonObject = (JsonObject) parsedValue;
            String type = jsonObject.get(CachingConstants.TYPE).getAsString();
            JsonElement value = jsonObject.get(CachingConstants.VALUE);
            return new Gson().fromJson(value, Class.forName(type));
        }
        return null;
    }
}
