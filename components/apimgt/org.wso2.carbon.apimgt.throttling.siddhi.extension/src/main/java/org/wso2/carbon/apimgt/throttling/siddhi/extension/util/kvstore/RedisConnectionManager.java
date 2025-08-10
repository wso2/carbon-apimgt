package org.wso2.carbon.apimgt.throttling.siddhi.extension.util.kvstore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisException;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.wso2.carbon.apimgt.impl.dto.RedisConfig;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;

public class RedisConnectionManager {

    private static final Logger log = LoggerFactory.getLogger(RedisConnectionManager.class);

    private static volatile JedisPool jedisPool;

    private static final String DEFAULT_REDIS_HOST = "localhost";
    private static final int DEFAULT_REDIS_PORT = 6379;

    // Pool monitoring scheduler
    private static final ScheduledExecutorService poolStatsLogger = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "Redis-Pool-Stats-Logger");
        t.setDaemon(true);
        return t;
    });

    static {
        poolStatsLogger.scheduleAtFixedRate(() -> {
            logPoolStats();
        }, 5, 5, TimeUnit.SECONDS); // Log every 5 seconds
    }

    // Default JedisPool configurations
    // private static final int DEFAULT_MAX_TOTAL_CONNECTIONS = 3000;
    // private static final int DEFAULT_MAX_IDLE_CONNECTIONS = 3000;
    // private static final int DEFAULT_MIN_IDLE_CONNECTIONS = 300;
    // private static final boolean DEFAULT_BLOCK_WHEN_EXHAUSTED = true;


    private RedisConnectionManager() {
        //to prevent instantiation
    }

    public static void logPoolStats() {
        if (jedisPool != null && !jedisPool.isClosed()) {
            try {
                log.info("Redis Pool Stats - Active: {}/{}, Idle: {}, Waiters: {}, " +
                                "Created: {}, Borrowed: {}, Returned: {}, Destroyed: {}",
                        jedisPool.getNumActive(), jedisPool.getMaxTotal(),
                        jedisPool.getNumIdle(), jedisPool.getNumWaiters(),
                        jedisPool.getCreatedCount(), jedisPool.getBorrowedCount(),
                        jedisPool.getReturnedCount(), jedisPool.getDestroyedCount());

                // Warning if pool utilization is high
                double utilization = (double) jedisPool.getNumActive() / jedisPool.getMaxTotal() * 100;
                if (utilization > 80) {
                    log.warn("High Redis pool utilization: {:.1f}% ({}/{})",
                            utilization, jedisPool.getNumActive(), jedisPool.getMaxTotal());
                }

                // Warning if there are waiting threads
                if (jedisPool.getNumWaiters() > 0) {
                    log.warn("Redis pool contention detected: {} threads waiting for connections",
                            jedisPool.getNumWaiters());
                }
            } catch (Exception e) {
                log.debug("Error retrieving Redis pool stats: {}", e.getMessage());
            }
        } else {
            log.debug("Redis pool is not initialized or is closed. Stats unavailable.");
        }
    }

    public static JedisPool getJedisPool() {
        if (jedisPool == null) {
            synchronized (RedisConnectionManager.class) {
                if (jedisPool == null) {
                    // Get Redis configuration from API Manager configuration
                    RedisConfig redisConfig = getRedisConfig();
                    
                    String redisHost = redisConfig != null && redisConfig.getHost() != null ? 
                        redisConfig.getHost() : System.getProperty("redis.host", DEFAULT_REDIS_HOST);
                    
                    int redisPort = redisConfig != null && redisConfig.getPort() > 0 ? 
                        redisConfig.getPort() : getPortFromSystemProperty();

                    JedisPoolConfig poolConfig = createPoolConfig(redisConfig);

                    try {
                        jedisPool = new JedisPool(poolConfig, redisHost, redisPort);
                        log.info("JedisPool initialized for Redis server at {}:{} with MaxTotal={}, MaxIdle={}, MinIdle={}", 
                                redisHost, redisPort, poolConfig.getMaxTotal(), poolConfig.getMaxIdle(), poolConfig.getMinIdle());
                    } catch (Exception e) {
                        log.error("Failed to initialize JedisPool for Redis server at {}:{}", redisHost, redisPort, e);
                    }
                }
            }
        }
        return jedisPool;
    }

 private static RedisConfig getRedisConfig() {
        try {
            return ServiceReferenceHolder.getInstance()
                    .getAPIManagerConfigurationService()
                    .getAPIManagerConfiguration()
                    .getRedisConfig();
        } catch (Exception e) {
            log.warn("Failed to load Redis configuration from API Manager config. Using defaults.", e);
            return null;
        }
    }
    
    private static int getPortFromSystemProperty() {
        try {
            return Integer.parseInt(System.getProperty("redis.port", String.valueOf(DEFAULT_REDIS_PORT)));
        } catch (NumberFormatException e) {
            log.warn("Invalid Redis port specified in system property 'redis.port'. Using default port: {}", DEFAULT_REDIS_PORT, e);
            return DEFAULT_REDIS_PORT;
        }
    }
    
    private static JedisPoolConfig createPoolConfig(RedisConfig redisConfig) {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        
        if (redisConfig != null) {
            // Use values from RedisConfig if available
            poolConfig.setMaxTotal(redisConfig.getMaxTotal());
            poolConfig.setMaxIdle(redisConfig.getMaxIdle());
            poolConfig.setMinIdle(redisConfig.getMinIdle());
            poolConfig.setBlockWhenExhausted(redisConfig.isBlockWhenExhausted());
            poolConfig.setTestOnBorrow(redisConfig.isTestOnBorrow());
            poolConfig.setTestOnReturn(redisConfig.isTestOnReturn());
            poolConfig.setTestWhileIdle(redisConfig.isTestWhileIdle());
            // poolConfig.setMinEvictableIdleTimeMillis(redisConfig.getMinEvictableIdleTimeMillis());
            // poolConfig.setTimeBetweenEvictionRunsMillis(redisConfig.getTimeBetweenEvictionRunsMillis());
            
            log.debug("Redis pool configuration from RedisConfig: MaxTotal={}, MaxIdle={}, MinIdle={}", 
                    redisConfig.getMaxTotal(), redisConfig.getMaxIdle(), redisConfig.getMinIdle());
        } else {
            // Fallback to default values (these are already set in RedisConfig class as defaults)
            poolConfig.setMaxTotal(8);
            poolConfig.setMaxIdle(8);
            poolConfig.setMinIdle(0);
            poolConfig.setBlockWhenExhausted(true);
            poolConfig.setTestOnBorrow(false);
            poolConfig.setTestOnReturn(false);
            poolConfig.setTestWhileIdle(true);
            // poolConfig.setMinEvictableIdleTimeMillis(60000L);
            // poolConfig.setTimeBetweenEvictionRunsMillis(30000L);
            
            log.warn("Using default Redis pool configuration values");
        }
        
        return poolConfig;
    }

    public static Jedis getJedis() {
        JedisPool pool = getJedisPool();
        if (pool != null) {
            try {
                return pool.getResource();
            } catch (JedisException e) {
                log.error("Failed to get Jedis resource from pool. Is Redis server running and accessible?", e);
                return null;
            }
        }
        log.error("JedisPool is not initialized. Cannot get Jedis resource.");
        return null;
    }

    public static void closeJedis(Jedis jedis) {
        if (jedis != null) {
            try {
                jedis.close(); // Returns the resource to the pool
            } catch (JedisException e) {
                log.error("Error while returning Jedis resource to pool", e);
            }
        }
    }

    public static void shutdownPool() {
        synchronized (RedisConnectionManager.class) {
            if (jedisPool != null && !jedisPool.isClosed()) {
                try {
                    jedisPool.close();
                    log.info("JedisPool has been closed.");
                } catch (Exception e) {
                    log.error("Error while closing JedisPool", e);
                } finally {
                    jedisPool = null;
                }
            } else {
                log.info("JedisPool was not initialized or already closed. No action taken for shutdown.");
            }
        }
    }
}
