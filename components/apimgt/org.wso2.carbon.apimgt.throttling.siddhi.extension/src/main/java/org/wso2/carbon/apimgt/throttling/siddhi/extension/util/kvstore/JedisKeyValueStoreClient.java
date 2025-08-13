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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.dto.RedisConfig;
//get this from siddhi extension
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
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
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 6379;

    private static RedisConfig getKeyValueConfig() {
        //check apimconfig is null or not
        try {
            RedisConfig kvStoreConfig = ServiceReferenceHolder.getInstance()
                    .getAPIManagerConfigurationService()
                    .getAPIManagerConfiguration()
                    .getKVStoreConfig();
            if (kvStoreConfig == null) {
                kvStoreConfig = ServiceReferenceHolder.getInstance()
                        .getAPIManagerConfigurationService()
                        .getAPIManagerConfiguration()
                        .getRedisConfig();
            }
            return kvStoreConfig;
        } catch (Exception e) {
            log.warn("Failed to load key-value configuration from API Manager config. Using defaults.", e);
            return null;
        }
    }

    private static JedisPoolConfig createPoolConfig(RedisConfig config) {
        JedisPoolConfig poolConfig = new JedisPoolConfig();

        poolConfig.setMaxTotal(config.getMaxTotal());
        poolConfig.setMaxIdle(config.getMaxIdle());
        poolConfig.setMinIdle(config.getMinIdle());
        poolConfig.setBlockWhenExhausted(config.isBlockWhenExhausted());
        poolConfig.setTestOnBorrow(config.isTestOnBorrow());
        poolConfig.setTestOnReturn(config.isTestOnReturn());
        poolConfig.setTestWhileIdle(config.isTestWhileIdle());

        return poolConfig;
    }

    private JedisPool getJedisPool() {
        if (jedisPool == null) {
            synchronized (JedisKeyValueStoreClient.class) {
                if (jedisPool == null) {
                    RedisConfig config = getKeyValueConfig();
                    String host = config != null && config.getHost() != null ?
                            config.getHost() : DEFAULT_HOST;

                    int port = config != null && config.getPort() > 0 ?
                            config.getPort() : DEFAULT_PORT;

                    if (config != null) {
                        JedisPoolConfig jedisPoolConfig = createPoolConfig(config);
                        try {
                            if (StringUtils.isNotEmpty(config.getUser()) && config.getPassword() != null) {
                                jedisPool = new JedisPool(jedisPoolConfig, config.getHost(), config.getPort(),
                                        config.getConnectionTimeout(), config.getUser(),
                                        String.valueOf(config.getPassword()), config.isSslEnabled());
                            } else if (config.getPassword() != null) {
                                jedisPool = new JedisPool(jedisPoolConfig, config.getHost(), config.getPort(),
                                        config.getConnectionTimeout(), String.valueOf(config.getPassword()), config.isSslEnabled());
                            } else {
                                jedisPool = new JedisPool(jedisPoolConfig, config.getHost(), config.getPort(),
                                        config.getConnectionTimeout(), config.isSslEnabled());
                            }
                            return jedisPool;
                        } catch (Exception e) {
                            log.error("Failed to initialize KeyValue JedisPool for server at " + host + ":" + port, e);
                        }
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
                log.error("Failed to get Jedis resource from pool. Is server running and accessible?", e);
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
