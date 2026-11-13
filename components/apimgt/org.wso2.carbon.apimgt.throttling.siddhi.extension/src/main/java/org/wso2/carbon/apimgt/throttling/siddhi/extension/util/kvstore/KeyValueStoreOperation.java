/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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

/**
 * A single write command sent to the key-value store as part of a batch
 * (see {@link KeyValueStoreClient#executeBatch(java.util.List)}).
 */
public final class KeyValueStoreOperation {

    /**
     * The write commands that can be batched.
     */
    public enum Type {
        /** Adds {@link #getAmount()} to the counter (Redis INCRBY). Reply: the new value as a {@link Long}. */
        INCRBY,
        /** Subtracts {@link #getAmount()} from the counter (Redis DECRBY). Reply: the new value as a {@link Long}. */
        DECRBY,
        /** Sets a TTL on an existing key (Redis PEXPIRE). */
        PEXPIRE,
        /** Sets the value with a TTL (Redis PSETEX). */
        PSETEX,
        /** Sets the value with a TTL only if the key does not exist (Redis SET key value NX PX ttl). */
        SET_NX_PX
    }

    private final Type type;
    private final String key;
    private final String value;
    private final long amount;
    private final long ttlMillis;

    private KeyValueStoreOperation(Type type, String key, String value, long amount, long ttlMillis) {
        this.type = type;
        this.key = key;
        this.value = value;
        this.amount = amount;
        this.ttlMillis = ttlMillis;
    }

    public static KeyValueStoreOperation incrementBy(String key, long increment) {
        return new KeyValueStoreOperation(Type.INCRBY, key, null, increment, 0L);
    }

    public static KeyValueStoreOperation decrementBy(String key, long decrement) {
        return new KeyValueStoreOperation(Type.DECRBY, key, null, decrement, 0L);
    }

    public static KeyValueStoreOperation expireMillis(String key, long ttlMillis) {
        return new KeyValueStoreOperation(Type.PEXPIRE, key, null, 0L, ttlMillis);
    }

    public static KeyValueStoreOperation setWithExpiry(String key, String value, long ttlMillis) {
        return new KeyValueStoreOperation(Type.PSETEX, key, value, 0L, ttlMillis);
    }

    public static KeyValueStoreOperation setIfAbsentWithExpiry(String key, String value, long ttlMillis) {
        return new KeyValueStoreOperation(Type.SET_NX_PX, key, value, 0L, ttlMillis);
    }

    public Type getType() {
        return type;
    }

    public String getKey() {
        return key;
    }

    /**
     * @return the value to write, for {@link Type#PSETEX} and {@link Type#SET_NX_PX}.
     */
    public String getValue() {
        return value;
    }

    /**
     * @return the increment or decrement, for {@link Type#INCRBY} and {@link Type#DECRBY}.
     */
    public long getAmount() {
        return amount;
    }

    /**
     * @return the TTL in milliseconds, for {@link Type#PEXPIRE}, {@link Type#PSETEX} and {@link Type#SET_NX_PX}.
     */
    public long getTtlMillis() {
        return ttlMillis;
    }

    @Override
    public String toString() {
        return type + " " + key;
    }
}
