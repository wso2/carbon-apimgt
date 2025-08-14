/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.throttling.siddhi.extension.util.kvstore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.dto.DistributedThrottleConfig;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;

import java.lang.reflect.Constructor;

/**
 * Manages the creation of Key-Value store client instances based on system configuration.
 */
public class KeyValueStoreManager {

    private static final Log log = LogFactory.getLog(KeyValueStoreManager.class);
    public static String kvStoreType;
    public static final String REDIS_TYPE = "redis";//move to constants
    private static volatile KeyValueStoreClient clientInstance;

    private KeyValueStoreManager() {
        // To prevent instantiation
    }

    /**
     * Gets the singleton KeyValueStoreClient instance.
     */
    public static KeyValueStoreClient getClient() throws KeyValueStoreException {
        if (clientInstance == null) {
            synchronized (KeyValueStoreManager.class) {
                if (clientInstance == null) {
                    clientInstance = createClient();
                }
            }
        }
        return clientInstance;
    }

    private static KeyValueStoreClient createClient() throws KeyValueStoreException {
        kvStoreType = getKeyValueStore();
        if (REDIS_TYPE.equals(kvStoreType)) {
            return new JedisKeyValueStoreClient();
        }
        else {
            try {
                Class<?> clazz = Class.forName(kvStoreType);

                if (!KeyValueStoreClient.class.isAssignableFrom(clazz)) {
                    throw new KeyValueStoreException("Class " + kvStoreType +
                            " does not implement KeyValueStoreClient interface");
                }

                Constructor<?> constructor = clazz.getDeclaredConstructor();

                return (KeyValueStoreClient) constructor.newInstance();

            } catch (Exception e) {
                throw new KeyValueStoreException("Error creating KeyValueStoreClient: " + kvStoreType, e);
            }
        }
    }

    /**
     * Shuts down resources associated with the configured key-value store clients.
     */
    public static void shutdown() {
        synchronized (KeyValueStoreManager.class) {
            if (clientInstance != null) {
                try {
                    log.info("Disconnecting KeyValueStoreClient");
                    clientInstance.disconnect();
                } catch (Exception e) {
                    log.error("Error disconnecting KeyValueStoreClient", e);
                } finally {
                    clientInstance = null;
                }
            }
        }
        log.info("KeyValueStoreManager shutdown completed.");
    }

    private static String getKeyValueStore() {
        try {
            DistributedThrottleConfig dtConfig =  ServiceReferenceHolder.getInstance()
                    .getAPIManagerConfigurationService()
                    .getAPIManagerConfiguration()
                    .getDistributedThrottleConfig();
            return dtConfig.getType();
        } catch (Exception e) {
            log.warn("Failed to load distributed throttle configuration from API Manager config. Using defaults.", e);
            return REDIS_TYPE;
        }
    }
}

