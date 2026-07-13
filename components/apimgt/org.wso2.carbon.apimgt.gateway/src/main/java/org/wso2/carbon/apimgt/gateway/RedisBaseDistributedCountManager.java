/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.gateway;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.commons.throttle.core.DistributedCounterException;
import org.apache.synapse.commons.throttle.core.DistributedCounterManager;
import org.wso2.carbon.apimgt.impl.dto.RedisConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisException;
import redis.clients.jedis.exceptions.JedisExhaustedPoolException;
import redis.clients.jedis.params.SetParams;

import java.util.List;

/**
 * Redis Base Distributed Counter Manager for Throttler.
 */
public class RedisBaseDistributedCountManager implements DistributedCounterManager {

    private static final Log log = LogFactory.getLog(RedisBaseDistributedCountManager.class);
    JedisPool redisPool;
    long keyLockRetrievalTimeout;

    public RedisBaseDistributedCountManager(JedisPool redisPool) {
        this.redisPool = redisPool;
        RedisConfig redisConfig = org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder.
                getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration().getRedisConfig();
        keyLockRetrievalTimeout = redisConfig.getKeyLockRetrievalTimeout();
    }

    @Override
    public long getCounter(String key) {

        long startTime = 0;
        try {
            String count = null;
            startTime = System.currentTimeMillis();
            try (Jedis jedis = redisPool.getResource()) {
                Transaction transaction = jedis.multi();
                Response<String> response = transaction.get(key);
                transaction.exec();

                if (response != null && response.get() != null) {
                    count = response.get();
                }
                if (count != null) {
                    long l = Long.parseLong(count);
                    if (log.isTraceEnabled()) {
                        log.trace(String.format("%s Key exist in redis with value %s", key, l));
                    }
                    return l;
                } else {
                    log.trace(String.format("Key %s does not exist !", key));
                }
                log.trace("shared counter key didn't exist. But returning:" + 0);
                return 0;
            }
        } finally {
            if (log.isTraceEnabled()) {
                log.trace("Time Taken to getDistributedCounter :" + (System.currentTimeMillis() - startTime));
            }
        }
    }

    @Override
    public void setCounter(String key, long value) {
        long startTime = 0;
        try {
            startTime = System.currentTimeMillis();
            asyncGetAndAlterCounter(key, value); // this should remove the expiry time as new key is created by this
        } finally {
            if (log.isTraceEnabled()) {
                log.trace("Time Taken to setDistributedCounter :" + (System.currentTimeMillis() - startTime));
            }
        }
    }
    @Override
    public void setCounterWithExpiry(String key, long value, long expiryTime) {
        long startTime = 0;
        try {
            startTime = System.currentTimeMillis();
            // this removes the expiry time as new key is created by this
            asyncGetAlterAndSetExpiryOfCounter(key, value, expiryTime);
        } finally {
            if (log.isTraceEnabled()) {
                log.trace("Set DistributedCounter : key:" + key + ", value:" + value + ", expiryTime:" + expiryTime);
                log.trace("Time Taken to set DistributedCounter :" + (System.currentTimeMillis() - startTime));
            }
        }
    }

    @Override
    public long addAndGetCounter(String key, long value) {

        long startTime = 0;
        try {
            startTime = System.currentTimeMillis();
            try (Jedis jedis = redisPool.getResource()) {

                Transaction transaction = jedis.multi();
                Response<String> previousResponse = transaction.get(key);
                Response<Long> incrementedValueResponse = transaction.incrBy(key, value);
                transaction.exec();
                Long incrementedValue = incrementedValueResponse.get();
                if (log.isTraceEnabled()) {
                    log.trace(String.format("Key %s is increased from %s to %s", key, previousResponse.get(),
                            incrementedValue));
                }
                return incrementedValue;
            }
        } finally {
            if (log.isTraceEnabled()) {
                log.trace("Time Taken to addAndGetDistributedCounter :" + (System.currentTimeMillis() - startTime));
            }
        }
    }

    @Override
    public void removeCounter(String key) {

        long startTime = 0;
        try {
            startTime = System.currentTimeMillis();

            try (Jedis jedis = redisPool.getResource()) {

                Transaction transaction = jedis.multi();
                transaction.del(key);
                transaction.exec();
            }
        } catch (JedisException e) {
            // Non-critical cleanup; key expires via TTL if skipped.
            log.warn("Redis error in removeCounter for key: " + key + ". Key will expire via TTL.", e);
        } finally {
            if (log.isTraceEnabled()) {
                log.trace("Time Taken to removeCounter key" + key + ":" +(System.currentTimeMillis() - startTime));
            }
        }
    }

    @Override
    public long asyncGetAndAddCounter(String key, long value) {

        long startTime = 0;
        try {
            startTime = System.currentTimeMillis();

            try (Jedis jedis = redisPool.getResource()) {
                long current = 0;
                Transaction transaction = jedis.multi();
                Response<String> currentValue = transaction.get(key);

                Response<Long> incrementedValue = transaction.incrBy(key, value);
                transaction.exec();
                if (currentValue != null && currentValue.get() != null) {
                    current = Long.parseLong(currentValue.get());
                }
                if (log.isTraceEnabled()) {
                    log.trace(String.format("Key %s increased from %s to %s", key, current, incrementedValue.get()));
                }
                return current;
            }
        } finally {
            if (log.isTraceEnabled()) {
                log.trace("Time Taken to asyncGetAndAddDistributedCounter :" + (System.currentTimeMillis() - startTime));
            }
        }
    }

    @Override
    public long asyncAddCounter(String key, long value) {

        long startTime = 0;
        try {
            startTime = System.currentTimeMillis();

            try (Jedis jedis = redisPool.getResource()) {
                long incrementedValue = 0;
                Transaction transaction = jedis.multi();

                Response<Long> responseValue = transaction.incrBy(key, value);
                transaction.exec();
                if (responseValue != null && responseValue.get() != null) {
                    incrementedValue = responseValue.get();
                }
                if (log.isTraceEnabled()) {
                    log.trace(String.format("Key %s increased from %s to %s", key, incrementedValue - value,
                            incrementedValue));
                }
                return incrementedValue;
            }
        } finally {
            if (log.isTraceEnabled()) {
                log.trace("Time Taken to asyncAddCounter :" + (System.currentTimeMillis() - startTime));
            }
        }

    }

    @Override
    public long asyncGetAndAlterCounter(String key, long value) {

        long startTime = 0;
        try {
            startTime = System.currentTimeMillis();

            try (Jedis jedis = redisPool.getResource()) {

                long current = 0;
                Transaction transaction = jedis.multi();
                Response<String> currentValue = transaction.get(key);
                transaction.del(key);
                Response<Long> incrementedValue = transaction.incrBy(key, value);
                transaction.exec();

                if (currentValue != null && currentValue.get() != null) {
                    current = Long.parseLong(currentValue.get());
                }
                if (log.isTraceEnabled()) {
                    log.trace(String.format("Key %s increased from %s to %s", key, current, incrementedValue.get()));
                }
                return current;
            }
        } finally {
            if (log.isTraceEnabled()) {
                log.trace("Time Taken to asyncGetAndAlterDistributedCounter :" + (System.currentTimeMillis() - startTime));
            }
        }
    }

    @Override
    public long asyncGetAlterAndSetExpiryOfCounter(String key, long value, long expiryTimeStamp) {

        long startTime = 0;
        try {
            startTime = System.currentTimeMillis();

            try (Jedis jedis = redisPool.getResource()) {

                long current = 0;
                Transaction transaction = jedis.multi();
                Response<String> currentValue = transaction.get(key);
                transaction.del(key);
                Response<Long> incrementedValue = transaction.incrBy(key, value);
                Response<Long> expireSetResponse = transaction.pexpireAt(key, expiryTimeStamp);
                transaction.exec();
                if (expireSetResponse.get() == 1) {
                    log.trace("Expire timeout was set of key:" + key +  " status:" + expireSetResponse.get());
                } else if (expireSetResponse.get() == 0) {
                    log.trace("Expire timeout was not set of key:" + key + " status:" +  expireSetResponse.get() +
                            " e.g. key doesn't exist, or operation skipped due to the provided arguments.");
                } else {
                    log.trace("Expire timeout was not set");
                }

                if (currentValue != null && currentValue.get() != null) {
                    current = Long.parseLong(currentValue.get());
                }
                if (log.isTraceEnabled()) {
                    log.trace(String.format("Key %s increased from %s to %s", key, current, incrementedValue.get()));
                }
                return current;
            }
        } finally {
            if (log.isTraceEnabled()) {
                log.trace("Time Taken to asyncGetAndAlterDistributedCounter :" + (System.currentTimeMillis() - startTime));
            }
        }
    }

    @Override
    public long getTimestamp(String key) {

        long startTime = 0;
        try {
            startTime = System.currentTimeMillis();

            try (Jedis jedis = redisPool.getResource()) {
                Transaction transaction = jedis.multi();
                Response<String> response = transaction.get(key);
                transaction.exec();

                if (response != null && response.get() != null) {
                    log.trace("Getting timestamp of key:" + key + ". Timestamp not null. Value:" + response.get());
                    return Long.parseLong(response.get());
                } else {
                    log.trace("Timestamp key doesn't exist !!!. key: " + key + "  So returning 0");
                }
                return 0;
            }
        } finally {
            if (log.isTraceEnabled()) {
                log.trace("Time Taken to getSharedTimestamp :" + (System.currentTimeMillis() - startTime));
            }
        }
    }

    @Override
    public void setTimestamp(String key, long timeStamp) {
        long startTime = 0;
        try {
            startTime = System.currentTimeMillis();

            try (Jedis jedis = redisPool.getResource()) {

                Transaction transaction = jedis.multi();
                transaction.set(key, String.valueOf(timeStamp));
                transaction.exec();
            }
        } finally {
            if (log.isTraceEnabled()) {
                log.trace(
                        "Time Taken to setTimestamp " + +timeStamp + " : " + (System.currentTimeMillis() - startTime));
            }
        }
    }

    public void setTimestampWithExpiry(String key, long timeStamp, long expiryTime) {
        long startTime = 0;
        try {
            startTime = System.currentTimeMillis();

            try (Jedis jedis = redisPool.getResource()) {
                Transaction transaction = jedis.multi();
                transaction.set(key, String.valueOf(timeStamp));
                Response<Long> expireSetResponse = transaction.pexpireAt(key, expiryTime);
                transaction.exec();

                if (expireSetResponse.get() == 1) {
                    log.trace("Expire timeout was set of key:" + key +  " status:" + expireSetResponse.get());
                } else if (expireSetResponse.get() == 0) {
                    log.trace("Expire timeout was not set of key:" + key + " status:" +  expireSetResponse.get() +
                            " e.g. key doesn't exist, or operation skipped due to the provided arguments.");
                } else {
                    log.trace("Expire timeout was not set");
                }
                log.trace("setTimestamp :" + timeStamp);
            }
        } finally {
            if (log.isTraceEnabled()) {
                log.trace("Time Taken to setTimestamp :" + (System.currentTimeMillis() - startTime));
            }
        }
    }

    @Override
    public void removeTimestamp(String key) {

        long startTime = 0;
        try {
            startTime = System.currentTimeMillis();

            try (Jedis jedis = redisPool.getResource()) {

                Transaction transaction = jedis.multi();
                transaction.del(key);
                transaction.exec();
            }
        } catch (JedisException e) {
            // Non-critical cleanup; key expires via TTL if skipped.
            log.warn("Redis error in removeTimestamp for key: " + key + ". Key will expire via TTL.", e);
        } finally {
            if (log.isTraceEnabled()) {
                log.trace("Time Taken to removeTimestamp key " + key + (System.currentTimeMillis() - startTime));
            }
        }
    }

    @Override
    public void setExpiry(String key, long expiryTimeStamp) {
        long currentTime = System.currentTimeMillis();
        if (log.isTraceEnabled()) {
            log.trace("Setting expiry of key " + key + " to " + expiryTimeStamp + " current timestamp:" + currentTime);
        }
        long startTime = 0;
        try {
            startTime = System.currentTimeMillis();
            try (Jedis jedis = redisPool.getResource()) {
                Transaction transaction = jedis.multi();
                Response<Long> expireSetResponse = transaction.pexpireAt(key, expiryTimeStamp);
                transaction.exec();
                if (expireSetResponse.get() == 1) {
                    log.trace("Expire timeout was set of key:" + key +  " status:" + expireSetResponse.get());
                } else if (expireSetResponse.get() == 0) {
                    log.trace("Expire timeout was not set of key:" + key + " status:" +  expireSetResponse.get() +
                            " e.g. key doesn't exist, or operation skipped due to the provided arguments.");
                } else {
                    log.trace("Expire timeout was not set");
                }
            }
        } finally {
            if (log.isTraceEnabled()) {
                log.trace("Time Taken to perform Redis setExpiry operation:" + (System.currentTimeMillis() - startTime));
            }
        }
    }

    @Override
    public long getTtl(String key) {
        long startTime = 0;
        long ttl;
        try {
            try (Jedis jedis = redisPool.getResource()) {
                Transaction transaction = jedis.multi();
                Response<Long> pttl = transaction.pttl(key);
                transaction.exec();
                ttl = pttl.get();
                if (ttl == -2) {
                    log.trace("TTL of key :" + key + " : " + ttl + " (Key does not exists)");
                } else if (ttl == -1) {
                    log.trace("TTL of key :" + key + " : " + ttl + " (Key does not have an associated expire)");
                }
                return ttl;
            }
        } catch (JedisException e) {
            // Return -2 (key-not-found sentinel) as a safe fallback.
            log.warn("Redis error in getTtl for key: " + key + ". Returning -2.", e);
            return -2;
        } finally {
            if (log.isTraceEnabled()) {
                log.trace("Time Taken to perform Redis getTtl operation:" + (System.currentTimeMillis() - startTime));
            }
        }
    }

    @Override
    public long setLock(String key, String value) {
        long startTime = 0;
        try {
            startTime = System.currentTimeMillis();
            try (Jedis jedis = redisPool.getResource()) {
                Transaction transaction = jedis.multi();
                Response<Long> response = transaction.setnx(key, value);
                transaction.exec();
                long responseCode = response.get();
                if (responseCode == 1) {
                    log.trace("Key was set");
                } else if (responseCode == 0) {
                    log.trace("Key was not set. It is already available");

                }
                log.trace("setLock with key" + key + "with value " + value);
                return responseCode;
            }
        } catch (JedisException e) {
            // Return 0 (lock not acquired) as a safe fallback.
            log.warn("Redis error in setLock for key: " + key + ". Returning 0.", e);
            return 0;
        } finally {
            if (log.isTraceEnabled()) {
                log.trace("Time Taken to setLock :" + (System.currentTimeMillis() - startTime));
            }
        }
    }

    @Override
    public boolean setLockWithExpiry(String key, String value, long expiryTimeStamp) {
        long startTime = 0;
        try {
            startTime = System.currentTimeMillis();
            long ttlMillis = expiryTimeStamp - System.currentTimeMillis();
            if (ttlMillis <= 0) {
                // Lock lease already expired — refuse to borrow a pool connection for a useless lock.
                return false;
            }
            try (Jedis jedis = redisPool.getResource()) {
                // Atomic NX + expiry: only the client that creates the key gets "OK".
                String result = jedis.set(key, value, SetParams.setParams().nx().px(ttlMillis));
                boolean acquired = "OK".equals(result);
                if (log.isTraceEnabled()) {
                    if (acquired) {
                        log.trace("Lock acquired for key:" + key);
                    } else {
                        log.trace("Lock not acquired for key:" + key + " (already held by another node)");
                    }
                }
                return acquired;
            }
        } catch (JedisException e) {
            throw new DistributedCounterException(
                    "Redis error acquiring lock for key: " + key, e, toKind(e));
        } finally {
            if (log.isTraceEnabled()) {
                log.trace("Time Taken to setLockWithExpiry :" + (System.currentTimeMillis() - startTime));
            }
        }
    }

    @Override
    public boolean isEnable() {

        return true;
    }

    @Override
    public String getType() {

        return "redis";
    }

    @Override
    public long getKeyLockRetrievalTimeout() {
        return keyLockRetrievalTimeout;
    }

    @Override
    public void removeLock(String key) {
        long startTime = 0;
        try {
            startTime = System.currentTimeMillis();

            try (Jedis jedis = redisPool.getResource()) {

                Transaction transaction = jedis.multi();
                transaction.del(key);
                transaction.exec();
            }
        } catch (JedisException e) {
            // Non-critical; lock expires naturally.
            log.warn("Redis error in removeLock for key: " + key + ". Lock will expire naturally.", e);
        } finally {
            if (log.isTraceEnabled()) {
                log.trace("Time Taken to remove lock :" + (System.currentTimeMillis() - startTime));
            }
        }
    }

    @Override
    public long[] getWindowState(String key) {
        long startTime = System.currentTimeMillis();
        try {
            try (Jedis jedis = redisPool.getResource()) {
                List<String> vals = jedis.hmget(key, "ts", "counter");
                if (vals == null) {
                    return new long[]{0L, 0L};
                }
                try {
                    String tsValue = vals.get(0);
                    String counterValue = vals.get(1);
                    if (tsValue == null || counterValue == null) {
                        if (tsValue != null || counterValue != null) {
                            log.warn("Incomplete window state in Redis for key: " + key
                                    + " (ts=" + tsValue + ", counter=" + counterValue
                                    + "). Treating as empty window.");
                        }
                        return new long[]{0L, 0L};
                    }
                    long ts = Long.parseLong(tsValue);
                    long counter = Long.parseLong(counterValue);
                    return new long[]{ts, counter};
                } catch (RuntimeException e) {
                    // Catches NumberFormatException (corrupted field value) and
                    // IndexOutOfBoundsException (unexpected short list from Jedis).
                    log.warn("Corrupted or unexpected window state in Redis for key: " + key
                            + " vals=" + vals + ". Treating as empty window.", e);
                    return new long[]{0L, 0L};
                }
            }
        } catch (JedisException e) {
            throw new DistributedCounterException(
                    "Redis error in getWindowState for key: " + key, e, toKind(e));
        } finally {
            if (log.isTraceEnabled()) {
                log.trace("Time Taken to getWindowState :" + (System.currentTimeMillis() - startTime));
            }
        }
    }

    @Override
    public void setWindow(String key, long count, long ts, long expiryTime) {
        long startTime = System.currentTimeMillis();
        try {
            try (Jedis jedis = redisPool.getResource()) {
                Transaction tx = jedis.multi();
                tx.hset(key, "ts", String.valueOf(ts));
                tx.hset(key, "counter", String.valueOf(count));
                tx.pexpireAt(key, expiryTime);
                List<Object> results = tx.exec();
                if (results == null || results.size() < 3) {
                    throw new DistributedCounterException(
                            "Transaction failed for setWindow on key: " + key
                            + " results=" + results, null, DistributedCounterException.Kind.TRANSACTION_ABORT);
                }
            }
        } catch (JedisException e) {
            throw new DistributedCounterException(
                    "Redis error in setWindow for key: " + key, e, toKind(e));
        } finally {
            if (log.isTraceEnabled()) {
                log.trace("Time Taken to setWindow :" + (System.currentTimeMillis() - startTime));
            }
        }
    }

    @Override
    public long incrWindowCounter(String key, long delta, long expiryTime) {
        long startTime = System.currentTimeMillis();
        try {
            try (Jedis jedis = redisPool.getResource()) {
                Transaction tx = jedis.multi();
                Response<Long> counterResp = tx.hincrBy(key, "counter", delta);
                // pexpireAt guards against the narrow race where the hash TTL fires between
                // the caller's pre-check and this call: HINCRBY auto-creates a bare hash
                // with no TTL; pexpireAt ensures it always expires at the window boundary.
                tx.pexpireAt(key, expiryTime);
                List<Object> results = tx.exec();
                if (results == null || results.size() < 2) {
                    throw new DistributedCounterException(
                            "Transaction failed for incrWindowCounter on key: " + key
                            + " results=" + results, null, DistributedCounterException.Kind.TRANSACTION_ABORT);
                }
                Long newCounter = counterResp.get();
                if (newCounter == null) {
                    throw new DistributedCounterException(
                            "Counter increment returned null for key: " + key, null,
                            DistributedCounterException.Kind.SERVER_ERROR);
                }
                return newCounter;
            }
        } catch (JedisException e) {
            throw new DistributedCounterException(
                    "Redis error in incrWindowCounter for key: " + key, e, toKind(e));
        } finally {
            if (log.isTraceEnabled()) {
                log.trace("Time Taken to incrWindowCounter :" + (System.currentTimeMillis() - startTime));
            }
        }
    }

    private static DistributedCounterException.Kind toKind(JedisException e) {
        if (e instanceof JedisExhaustedPoolException) {
            return DistributedCounterException.Kind.POOL_EXHAUSTED;
        }
        if (e instanceof JedisConnectionException) {
            return DistributedCounterException.Kind.CONNECTION_FAILURE;
        }
        return DistributedCounterException.Kind.SERVER_ERROR;
    }
}
