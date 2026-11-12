/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

import java.util.ArrayList;
import java.util.List;

/**
 * Interface for a generic Key-Value store client.
 * <p>
 * The batch methods ({@link #supportsBatchOperations()}, {@link #getMultiple(List)} and
 * {@link #executeBatch(List)}) are default methods, so existing implementations keep working
 * without any change. Such clients are synced one key at a time, as before.
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
     * Sets the string value for the given key with a TTL (time-to-live).
     * The key is automatically removed from the store after {@code ttlMillis} milliseconds.
     * This operation is atomic — value and expiry are set in a single command.
     *
     * @param key       The key with which the specified value is to be associated.
     * @param value     The value to be associated with the specified key.
     * @param ttlMillis The time-to-live in milliseconds after which the key is auto-deleted.
     */
    void setWithExpiry(String key, String value, long ttlMillis);

    /**
     * Sets a TTL on an existing key without changing its value (Redis PEXPIRE).
     * If the key does not exist this is a no-op. Use this to stamp a TTL on a key
     * that was created by INCRBY, which never sets an expiry on its own.
     *
     * @param key       The key whose expiry is to be set.
     * @param ttlMillis The time-to-live in milliseconds.
     */
    void expireMillis(String key, long ttlMillis);

    /**
     * Deletes the mapping for a key from this store if it is present.
     *
     * @param key The key whose mapping is to be removed from the store.
     */
    void delete(String key);

    /**
     * Tells whether this client sends {@link #getMultiple(List)} and {@link #executeBatch(List)}
     * as real batches (for example Redis MGET and pipelining). Only such clients are synced in
     * batches; the others are synced one key at a time.
     *
     * @return true if batch operations are supported natively, false otherwise (the default).
     */
    default boolean supportsBatchOperations() {
        return false;
    }

    /**
     * Retrieves the values of several keys (Redis MGET).
     * The default implementation calls {@link #get(String)} once per key.
     *
     * @param keys The keys whose values are to be returned.
     * @return the values in the same order as the keys, with null for a key that does not exist.
     * @throws KeyValueStoreException if the values could not be read.
     */
    default List<String> getMultiple(List<String> keys) {
        List<String> values = new ArrayList<>(keys.size());
        for (String key : keys) {
            values.add(get(key));
        }
        return values;
    }

    /**
     * Executes several write operations in one batch (Redis pipeline).
     * The default implementation runs each operation through the single-key methods.
     *
     * @param operations The operations to execute, in order.
     * @return one reply per operation, in the same order. INCRBY and DECRBY reply with the new value
     *         as a {@link Long}. For the other types any reply that is not an exception means success.
     *         An operation that failed has a {@link KeyValueStoreException} as its reply.
     * @throws KeyValueStoreUnavailableException if the batch was not sent (for example no connection could be
     *         obtained): none of the operations was applied.
     * @throws KeyValueStoreException if the batch failed after it may have been sent (for example the connection
     *         dropped while the replies were read): each operation may or may not have been applied.
     */
    default List<Object> executeBatch(List<KeyValueStoreOperation> operations) {
        List<Object> replies = new ArrayList<>(operations.size());
        for (KeyValueStoreOperation operation : operations) {
            String key = operation.getKey();
            try {
                switch (operation.getType()) {
                    case INCRBY:
                        replies.add(incrementBy(key, operation.getAmount()));
                        break;
                    case DECRBY:
                        replies.add(decrementBy(key, operation.getAmount()));
                        break;
                    case PEXPIRE:
                        expireMillis(key, operation.getTtlMillis());
                        replies.add(Boolean.TRUE);
                        break;
                    case PSETEX:
                        setWithExpiry(key, operation.getValue(), operation.getTtlMillis());
                        replies.add(Boolean.TRUE);
                        break;
                    case SET_NX_PX:
                        // Not atomic here; clients that support batching do it in one command.
                        if (get(key) == null) {
                            setWithExpiry(key, operation.getValue(), operation.getTtlMillis());
                        }
                        replies.add(Boolean.TRUE);
                        break;
                    default:
                        replies.add(new KeyValueStoreException("Unsupported operation: " + operation));
                }
            } catch (KeyValueStoreException e) {
                replies.add(e);
            } catch (RuntimeException e) {
                replies.add(new KeyValueStoreException("Error during " + operation, e));
            }
        }
        return replies;
    }
}
