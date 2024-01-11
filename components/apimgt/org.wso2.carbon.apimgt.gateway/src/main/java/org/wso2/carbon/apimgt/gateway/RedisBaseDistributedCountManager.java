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
import org.apache.synapse.commons.throttle.core.DistributedCounterManager;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;
import org.wso2.carbon.apimgt.gateway.throttling.util.ThrottleUtils;
import org.wso2.carbon.apimgt.impl.dto.RedisConfig;

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
            try (Jedis jedis = redisPool.getResource()) {
                Transaction transaction = jedis.multi();
                transaction.setnx(key, value);
                Response<Long> pexpireAtResponse = transaction.pexpireAt(key, expiryTimeStamp);
                transaction.exec();
                long pexpireAtResponseCode = pexpireAtResponse.get();
                if (pexpireAtResponseCode == 1) {
                    if (log.isTraceEnabled()) {
                        log.trace("expiry time of key:" + key + " was set successfully.");
                    }
                    return true;
                } else if (pexpireAtResponseCode == 0) {
                    if (log.isTraceEnabled()) {
                        log.trace("expiry time was not set of key:" + key
                                + " e.g. key doesn't exist, or operation skipped due to the provided arguments.");
                    }
                    return false;
                } else {
                    if (log.isTraceEnabled()) {
                        log.trace("expiry time was not set of key:" + key);
                    }
                    return false;
                }
            }
        } finally {
            if (log.isTraceEnabled()) {
                log.trace("Time Taken to setLock :" + (System.currentTimeMillis() - startTime));
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
        } finally {
            if (log.isTraceEnabled()) {
                log.trace("Time Taken to remove lock :" + (System.currentTimeMillis() - startTime));
            }
        }
    }
}

