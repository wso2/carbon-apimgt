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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.dto.DistributedThrottleConfig;
import org.wso2.carbon.apimgt.impl.dto.RedisConfig;
import org.wso2.carbon.apimgt.throttling.siddhi.extension.util.ThrottlingConstants;
import org.wso2.carbon.apimgt.throttling.siddhi.extension.internal.ServiceReferenceHolder;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisException;

/**
 * Implementation for key-value store client that combines
 * connection management and client operations.
 */
public class JedisKeyValueStoreClient implements KeyValueStoreClient {

    private static final Log log = LogFactory.getLog(JedisKeyValueStoreClient.class);

    // Connection pool
    private static volatile JedisPool jedisPool;

    //key-value store configs
    private static volatile String host = ThrottlingConstants.DEFAULT_HOST;
    private static volatile int port = ThrottlingConstants.DEFAULT_PORT;
    private static volatile String user;
    private static volatile char[] password;
    private static volatile int connectionTimeout;
    private static volatile boolean sslEnabled;
    private static volatile JedisPoolConfig poolConfig = new JedisPoolConfig();


    private static void populateKeyValueStoreConfigs() {
        try {
            DistributedThrottleConfig distributedConfig = ServiceReferenceHolder.getInstance()
                    .getAPIManagerConfigurationService()
                    .getAPIManagerConfiguration()
                    .getDistributedThrottleConfig();
            if (distributedConfig != null && distributedConfig.getHost() != null && distributedConfig.getPort() > 0) {
                host = distributedConfig.getHost();
                port = distributedConfig.getPort();
                user = distributedConfig.getUser();
                password = distributedConfig.getPassword();
                connectionTimeout = distributedConfig.getConnectionTimeout();
                sslEnabled = distributedConfig.isSslEnabled();
                poolConfig.setMaxTotal(distributedConfig.getMaxTotal());
                poolConfig.setMaxIdle(distributedConfig.getMaxIdle());
                poolConfig.setMinIdle(distributedConfig.getMinIdle());
                poolConfig.setBlockWhenExhausted(distributedConfig.isBlockWhenExhausted());
                poolConfig.setTestOnBorrow(distributedConfig.isTestOnBorrow());
                poolConfig.setTestOnReturn(distributedConfig.isTestOnReturn());
                poolConfig.setTestWhileIdle(distributedConfig.isTestWhileIdle());
                
                if (log.isDebugEnabled()) {
                    log.debug("Using DistributedThrottleConfig for connection: " + host + ":" + port);
                }

            } else {
                RedisConfig redisConfig = ServiceReferenceHolder.getInstance()
                        .getAPIManagerConfigurationService()
                        .getAPIManagerConfiguration()
                        .getRedisConfig();
                if (redisConfig != null) {
                    host = redisConfig.getHost();
                    port = redisConfig.getPort();
                    user = redisConfig.getUser();
                    password = redisConfig.getPassword();
                    connectionTimeout = redisConfig.getConnectionTimeout();
                    sslEnabled = redisConfig.isSslEnabled();
                    poolConfig.setMaxTotal(redisConfig.getMaxTotal());
                    poolConfig.setMaxIdle(redisConfig.getMaxIdle());
                    poolConfig.setMinIdle(redisConfig.getMinIdle());
                    poolConfig.setBlockWhenExhausted(redisConfig.isBlockWhenExhausted());
                    poolConfig.setTestOnBorrow(redisConfig.isTestOnBorrow());
                    poolConfig.setTestOnReturn(redisConfig.isTestOnReturn());
                    poolConfig.setTestWhileIdle(redisConfig.isTestWhileIdle());
                    
                    if (log.isDebugEnabled()) {
                        log.debug("Using RedisConfig for connection: " + host + ":" + port);
                    }
                }
                else {
                    log.warn("Unknown config type provided to createPoolConfig. Using default JedisPoolConfig values.");
                }

            }
        } catch (Exception e) {
            log.warn("Failed to load distributed throttle configuration from API Manager config. Using JedisPool defaults.", e);
        }
    }

    private JedisPool getJedisPool() {
        if (jedisPool == null) {
            synchronized (JedisKeyValueStoreClient.class) {
                if (jedisPool == null) {
                    populateKeyValueStoreConfigs();
                    try {
                        if (StringUtils.isNotEmpty(user) && password != null) {
                            jedisPool = new JedisPool(poolConfig, host, port, connectionTimeout, user,
                                    String.valueOf(password), sslEnabled);
                        } else if (password != null) {
                            jedisPool = new JedisPool(poolConfig, host, port, connectionTimeout,
                                    String.valueOf(password), sslEnabled);
                        } else {
                            jedisPool = new JedisPool(poolConfig, host, port, connectionTimeout, sslEnabled);
                        }
                        if (log.isDebugEnabled()) {
                            log.debug("Initialized JedisPool for server: " + host + ":" + port);
                        }
                        return jedisPool;
                    } catch (Exception e) {
                        log.error("Failed to initialize KeyValue JedisPool for server at " + host + ":" + port, e);
                    }
                }
            }
        }
        return jedisPool;
    }

    private Jedis getJedis() {
        JedisPool pool = getJedisPool();
        if (pool != null) {
            try {
                return pool.getResource();
            } catch (JedisException e) {
                log.error("Failed to get Jedis resource from pool. Check whether the server is running and accessible.", e);
                return null;
            }
        }
        log.error("JedisPool is not initialized. Cannot get Jedis resource.");
        return null;
    }

    private void closeJedis(Jedis jedis) {
        if (jedis != null) {
            try {
                jedis.close();
            } catch (JedisException e) {
                log.error("Error while returning Jedis resource to pool", e);
            }
        }
    }

    @Override
    public void disconnect() {
        try {
            shutdownPool();
        } catch (Exception e) {
            throw new KeyValueStoreException("Error shutting down KeyValue connection pool", e);
        }
    }

    private void shutdownPool() {
        synchronized (JedisKeyValueStoreClient.class) {
            if (jedisPool != null && !jedisPool.isClosed()) {
                try {
                    jedisPool.close();
                } catch (Exception e) {
                    log.error("Error while closing JedisPool", e);
                } finally {
                    jedisPool = null;
                }
            }
        }
    }

    @Override
    public String get(String key) {
        if (key == null) {
            return null;
        }
        Jedis jedis = null;
        try {
            jedis = getJedis();
            if (jedis == null) {
                throw new KeyValueStoreException("Failed to get connection from KeyValue pool for GET operation.");
            }
            return jedis.get(key);
        } catch (JedisException e) {
            throw new KeyValueStoreException("Error during KeyValue GET for key: " + key, e);
        } finally {
            closeJedis(jedis);
        }
    }

    @Override
    public void set(String key, String value) {
        if (key == null) {
            throw new KeyValueStoreException("Key cannot be null for SET operation.");
        }
        Jedis jedis = null;
        try {
            jedis = getJedis();
            if (jedis == null) {
                throw new KeyValueStoreException("Failed to get connection from KeyValue pool for SET operation.");
            }
            jedis.set(key, value);
        } catch (JedisException e) {
            throw new KeyValueStoreException("Error during KeyValue SET for key: " + key, e);
        } finally {
            closeJedis(jedis);
        }
    }

    @Override
    public long incrementBy(String key, long increment) {
        if (key == null) {
            throw new KeyValueStoreException("Key cannot be null for INCREMENT operation.");
        }
        Jedis jedis = null;
        try {
            jedis = getJedis();
            if (jedis == null) {
                throw new KeyValueStoreException("Failed to get connection from KeyValue pool for INCREMENT operation.");
            }
            return jedis.incrBy(key, increment);
        } catch (JedisException e) {
            throw new KeyValueStoreException("Error during KeyValue INCREMENT for key: " + key, e);
        } finally {
            closeJedis(jedis);
        }
    }

    @Override
    public long decrementBy(String key, long decrement) {
        if (key == null) {
            throw new KeyValueStoreException("Key cannot be null for DECREMENT operation.");
        }
        Jedis jedis = null;
        try {
            jedis = getJedis();
            if (jedis == null) {
                throw new KeyValueStoreException("Failed to get connection from KeyValue pool for DECREMENT operation.");
            }
            return jedis.decrBy(key, decrement);
        } catch (JedisException e) {
            throw new KeyValueStoreException("Error during KeyValue DECREMENT for key: " + key, e);
        } finally {
            closeJedis(jedis);
        }
    }

    @Override
    public void delete(String key) {
        if (key == null) {
            throw new KeyValueStoreException("Key cannot be null for DELETE operation.");
        }
        Jedis jedis = null;
        try {
            jedis = getJedis();
            if (jedis == null) {
                throw new KeyValueStoreException("Failed to get connection from KeyValue pool for DELETE operation.");
            }
            jedis.del(key);
        } catch (JedisException e) {
            throw new KeyValueStoreException("Error during KeyValue DELETE for key: " + key, e);
        } finally {
            closeJedis(jedis);
        }
    }
}
