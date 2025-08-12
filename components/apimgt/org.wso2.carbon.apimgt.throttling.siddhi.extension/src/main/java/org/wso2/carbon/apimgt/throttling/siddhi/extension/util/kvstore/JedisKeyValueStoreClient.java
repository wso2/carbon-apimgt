package org.wso2.carbon.apimgt.throttling.siddhi.extension.util.kvstore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.siddhi.core.util.kvstore.KeyValueStoreException;
import org.wso2.siddhi.core.util.kvstore.KeyValueStoreClient;
import org.wso2.carbon.apimgt.impl.dto.RedisConfig;
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

    private static final Logger log = LoggerFactory.getLogger(JedisKeyValueStoreClient.class);

    // Connection pool
    private static volatile JedisPool jedisPool;
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 6379;

    private static RedisConfig getKeyValueConfig() {
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

        if (config != null) {
            poolConfig.setMaxTotal(config.getMaxTotal());
            poolConfig.setMaxIdle(config.getMaxIdle());
            poolConfig.setMinIdle(config.getMinIdle());
            poolConfig.setBlockWhenExhausted(config.isBlockWhenExhausted());
            poolConfig.setTestOnBorrow(config.isTestOnBorrow());
            poolConfig.setTestOnReturn(config.isTestOnReturn());
            poolConfig.setTestWhileIdle(config.isTestWhileIdle());

            log.debug("Pool configuration from config: MaxTotal={}, MaxIdle={}, MinIdle={}",
                    config.getMaxTotal(), config.getMaxIdle(), config.getMinIdle());
        } else {
            poolConfig.setMaxTotal(8);
            poolConfig.setMaxIdle(8);
            poolConfig.setMinIdle(0);
            poolConfig.setBlockWhenExhausted(true);
            poolConfig.setTestOnBorrow(false);
            poolConfig.setTestOnReturn(false);
            poolConfig.setTestWhileIdle(true);

            log.warn("Using default pool configuration values");
        }

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

                    JedisPoolConfig poolConfig = createPoolConfig(config);

                    try {
                        jedisPool = new JedisPool(poolConfig, host, port);
                        log.info("KeyValue JedisPool initialized for server at {}:{} with MaxTotal={}, MaxIdle={}, MinIdle={}",
                                host, port, poolConfig.getMaxTotal(), poolConfig.getMaxIdle(), poolConfig.getMinIdle());
                    } catch (Exception e) {
                        log.error("Failed to initialize KeyValue JedisPool for server at {}:{}", host, port, e);
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

    // KeyValueStoreClient Interface Implementation

    @Override
    public void connect() {
        log.info("Attempting to ensure KeyValue connection pool is initialized.");
        Jedis jedis = null;
        try {
            jedis = getJedis();
            if (jedis != null) {
                log.info("Successfully obtained a connection from KeyValue pool. Connection appears to be available.");
            } else {
                log.warn("Failed to obtain a connection from KeyValue pool during connect(). Pool might be uninitialized or KeyValue unavailable.");
            }
        } catch (Exception e) {
            log.error("Error while trying to establish initial connection to KeyValue via pool.", e);
        } finally {
            closeJedis(jedis);
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
