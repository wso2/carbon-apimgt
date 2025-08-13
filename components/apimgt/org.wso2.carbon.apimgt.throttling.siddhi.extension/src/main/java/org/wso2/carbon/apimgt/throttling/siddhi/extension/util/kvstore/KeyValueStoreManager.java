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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;

/**
 * Manages the creation of Key-Value store client instances based on system configuration.
 */
public class KeyValueStoreManager {

    private static final Logger log = LoggerFactory.getLogger(KeyValueStoreManager.class);
    public static final String kvStoreType = System.getProperty("distributed.throttle.type");
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
        String clientClassName = resolveClientClassName();//directly read

        try {
            Class<?> clazz = Class.forName(clientClassName);

            if (!KeyValueStoreClient.class.isAssignableFrom(clazz)) {
                throw new KeyValueStoreException("Class " + clientClassName +
                        " does not implement KeyValueStoreClient interface");
            }

            Constructor<?> constructor = clazz.getDeclaredConstructor();

            return (KeyValueStoreClient) constructor.newInstance();

        } catch (Exception e) {
            throw new KeyValueStoreException("Error creating KeyValueStoreClient: " + clientClassName, e);
        }
    }

    /**
     * Resolves the client class name based on system properties.
     */
    private static String resolveClientClassName() {
        if (kvStoreType == null) {
            throw new KeyValueStoreException("The key value store type is null");
        }

        if (REDIS_TYPE.equals(kvStoreType)) {
            return "org.wso2.carbon.apimgt.throttling.siddhi.extension.util.kvstore.JedisKeyValueStoreClient";
            //
        }

        return kvStoreType; // Allow custom class names
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
}

