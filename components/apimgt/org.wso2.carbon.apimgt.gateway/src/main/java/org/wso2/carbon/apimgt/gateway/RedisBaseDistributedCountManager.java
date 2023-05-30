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

/**
 * Redis Base Distributed Counter Manager for Throttler.
 */
public class RedisBaseDistributedCountManager implements DistributedCounterManager {

    private static final Log log = LogFactory.getLog(RedisBaseDistributedCountManager.class);
    JedisPool redisPool;

    public RedisBaseDistributedCountManager(JedisPool redisPool) {

        this.redisPool = redisPool;
    }

    @Override
    public long getCounter(String key) {

        long startTime = 0;
        try {
            String count;
            startTime = System.currentTimeMillis();
            try (Jedis jedis = redisPool.getResource()) {
                count = jedis.get(key);
                if (count != null) {
                    long l = Long.parseLong(count);
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("%s Key already exist in redis with value %s", key, l));
                    }
                    return l;
                }
                return 0;
            }
        } finally {
            if (log.isDebugEnabled()) {
                log.debug("Time Taken to getDistributedCounter :" + (System.currentTimeMillis() - startTime));
            }
        }

    }

    @Override
    public void setCounter(String key, long value) {

        long startTime = 0;
        try {
            startTime = System.currentTimeMillis();

            asyncGetAndAlterCounter(key, value);
        } finally {
            if (log.isDebugEnabled()){
                log.debug("Time Taken to setDistributedCounter :" + (System.currentTimeMillis() - startTime));
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
                if (log.isDebugEnabled()) {
                    log.debug(String.format("%s Key increased from %s to %s", key, previousResponse.get(),
                            incrementedValue));
                }
                return incrementedValue;
            }

        } finally {
            if (log.isDebugEnabled()) {
                log.debug("Time Taken to addAndGetDistributedCounter :" + (System.currentTimeMillis() - startTime));
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
                if (log.isDebugEnabled()) {
                    log.debug(String.format("%s Key Removed", key));
                }
            }
        } finally {
            if (log.isDebugEnabled()) {
                log.debug("Time Taken to removeCounter :" + (System.currentTimeMillis() - startTime));
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
                if (log.isDebugEnabled()) {
                    log.info(String.format("%s Key increased from %s to %s", key, current, incrementedValue.get()));
                }
                return current;
            }
        } finally {
            if (log.isDebugEnabled()) {
                log.debug("Time Taken to asyncGetAndAddDistributedCounter :" + (System.currentTimeMillis() - startTime));
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
                if (log.isDebugEnabled()) {
                    log.info(String.format("%s Key increased from %s to %s", key, current, incrementedValue.get()));
                }
                return current;
            }
        } finally {
            if (log.isDebugEnabled()) {
                log.debug("Time Taken to asyncGetAndAlterDistributedCounter :" + (System.currentTimeMillis() - startTime));
            }
        }
    }

    @Override
    public long getTimestamp(String key) {

        long startTime = 0;
        try {
            startTime = System.currentTimeMillis();

            try (Jedis jedis = redisPool.getResource()) {

                String timeStamp = jedis.get(key);
                if (timeStamp != null) {
                    return Long.parseLong(timeStamp);
                }
                return 0;
            }
        } finally {
            if (log.isDebugEnabled()) {
                log.debug("Time Taken to getSharedTimestamp :" + (System.currentTimeMillis() - startTime));
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
            if (log.isDebugEnabled()) {
                log.debug("Time Taken to setTimestamp :" + (System.currentTimeMillis() - startTime));
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
            if (log.isDebugEnabled()) {
                log.debug("Time Taken to removeTimestamp :" + (System.currentTimeMillis() - startTime));
            }
        }
    }

    @Override
    public void setExpiry(String key, long expiryTimeStamp) {

        long startTime = 0;
        try {
            startTime = System.currentTimeMillis();

            try (Jedis jedis = redisPool.getResource()) {
                Transaction transaction = jedis.multi();
                transaction.pexpireAt(key, expiryTimeStamp);
                transaction.exec();
            }
        } finally {
            if (log.isDebugEnabled()) {
                log.debug("Time Taken to setExpiry :" + (System.currentTimeMillis() - startTime));
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
}

