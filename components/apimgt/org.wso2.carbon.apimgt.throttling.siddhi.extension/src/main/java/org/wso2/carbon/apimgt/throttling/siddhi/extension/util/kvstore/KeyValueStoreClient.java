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

/**
 * Interface for a generic Key-Value store client.
 */
public interface KeyValueStoreClient {

    /**
     * Disconnects from the key-value store and releases any associated resources.
     * This involves closing active connections, shutting down a connection pool,
     * or other cleanup tasks.
     */
    void disconnect();

    /**
     * Retrieves the string value associated with the given key.
     *
     * @param key The key whose associated value is to be returned.
     */
    String get(String key);

    /**
     * Sets the string value for the given key.
     * If the store previously contained a mapping for the key, the old value is replaced by
     * the specified value.
     *
     * @param key   The key with which the specified value is to be associated.
     * @param value The value to be associated with the specified key.
     */
    void set(String key, String value);

    /**
     * Increments the numeric value of a key by one.
     *
     * @param key The key whose numeric value is to be incremented.
     * @return The value of the key after the increment operation.
     */
    long incrementBy(String key, long increment);

    /**
     * Decrements the numeric value of a key by one.
     *
     * @param key The key whose numeric value is to be decremented.
     * @return The value of the key after the decrement operation.
     */
    long decrementBy(String key, long decrement);

    /**
     * Deletes the mapping for a key from this store if it is present.
     *
     * @param key The key whose mapping is to be removed from the store.
     */
    void delete(String key);
}
