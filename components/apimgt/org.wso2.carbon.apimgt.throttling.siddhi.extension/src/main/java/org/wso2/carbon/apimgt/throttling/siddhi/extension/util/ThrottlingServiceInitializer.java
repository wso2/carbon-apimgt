////package org.wso2.carbon.apimgt.throttling.siddhi.extension.util;
////
////import org.wso2.siddhi.core.util.kvstore.KeyValueStoreManager;
////import org.wso2.carbon.apimgt.throttling.siddhi.extension.util.kvstore.APIMKeyValueStoreProvider;
////
////public class ThrottlingServiceInitializer {
////
////    public void start() {
////        // Create an instance of our provider
////        APIMKeyValueStoreProvider provider = new APIMKeyValueStoreProvider();
////
////        // Register it with the siddhi-core manager
////        KeyValueStoreManager.registerProvider(provider);
////
////        System.out.println("Throttling KeyValueStoreProvider registered successfully.");
////    }
////
////    public void stop() {
////        // Shutdown the manager, which calls our provider's shutdown method
////        KeyValueStoreManager.shutdown();
////        System.out.println("Throttling KeyValueStoreProvider shut down successfully.");
////    }
////}
//
//
//
//COUNTATTRIBUTEAGGREGATOR
//
//        /*
//         * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//         *
//         * WSO2 Inc. licenses this file to you under the Apache License,
//         * Version 2.0 (the "License"); you may not use this file except
//         * in compliance with the License.
//         * You may obtain a copy of the License at
//         *
//         *     http://www.apache.org/licenses/LICENSE-2.0
//         *
//         * Unless required by applicable law or agreed to in writing,
//         * software distributed under the License is distributed on an
//         * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
//         * KIND, either express or implied. See the License for the
//         * specific language governing permissions and limitations
//         * under the License.
//         */
//        package org.wso2.siddhi.core.query.selector.attribute.aggregator;
//
//import org.wso2.siddhi.core.config.ExecutionPlanContext;
//import org.wso2.siddhi.core.executor.ExpressionExecutor;
//import org.wso2.siddhi.core.query.selector.QuerySelector;
//import org.wso2.siddhi.core.query.selector.attribute.aggregator.AttributeAggregator;
//import org.wso2.siddhi.core.util.kvstore.KeyValueStoreClient;
//import org.wso2.siddhi.core.util.kvstore.KeyValueStoreManager;
//import org.wso2.siddhi.core.util.kvstore.KeyValueStoreException;
//import org.wso2.siddhi.query.api.definition.Attribute;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//
//import java.util.AbstractMap;
//import java.util.Map;
//import java.util.concurrent.*;
//import java.util.concurrent.atomic.AtomicLong;
//
//public class CountAttributeAggregator extends AttributeAggregator {
//
//    private static final Logger log = LoggerFactory.getLogger(
//            org.wso2.siddhi.core.query.selector.attribute.aggregator.CountAttributeAggregator.class);
//    private static Attribute.Type type = Attribute.Type.LONG;
//    private KeyValueStoreClient kvStoreClient;
//    private String kvStoreType;
//    private String key;
//    private final AtomicLong localCounter = new AtomicLong(0L);
//    private final AtomicLong pendingDelta = new AtomicLong(0L);
//    //    String className = System.getProperty("distributed.counter.impl","org.wso2.carbon.apimgt.throttling.impl.DefaultInMemoryConnectionManager");
//
//
//    // Static shared scheduler for all aggregators
//    private static final ScheduledExecutorService redisSyncScheduler =
//            Executors.newScheduledThreadPool(10, r -> {
//                Thread t = new Thread(r, "Redis-Sync-Thread");
//                t.setDaemon(true);
//                return t;
//            });
//
//    // Track all active aggregators for periodic sync
//    private static final ConcurrentHashMap<String, org.wso2.siddhi.core.query.selector.attribute.aggregator.CountAttributeAggregator> activeAggregators =
//            new ConcurrentHashMap<>();
//
//    // Sync interval in seconds
//    private static final int REDIS_SYNC_INTERVAL_SECONDS = 1;
//
//    static {
//        // Start periodic Redis sync task
//        redisSyncScheduler.scheduleAtFixedRate(() -> {
//            log.error("Rediadsfs sync scheduler started with interval: {} seconds", REDIS_SYNC_INTERVAL_SECONDS);
//            for (org.wso2.siddhi.core.query.selector.attribute.aggregator.CountAttributeAggregator aggregator : activeAggregators.values()) {
//                aggregator.syncWithKeyValueStore();
//                log.error("Redis sync schedulasdfasdfer started with interval: {} seconds", REDIS_SYNC_INTERVAL_SECONDS);
//            }
//        }, REDIS_SYNC_INTERVAL_SECONDS, REDIS_SYNC_INTERVAL_SECONDS, TimeUnit.SECONDS);
//
//        log.error("Redis sync scheduler started with interval: {} seconds", REDIS_SYNC_INTERVAL_SECONDS);
//    }
//
//    /**
//     * The initialization method for FunctionExecutor
//     *
//     * @param attributeExpressionExecutors are the executors of each attributes in the function
//     * @param executionPlanContext         Execution plan runtime context
//     */
//    @Override
//    protected void init(ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext executionPlanContext) {
//        //        boolean isDistributed = Boolean.parseBoolean(System.getProperty("siddhi.throttling.distributed", "false"));
//        boolean isDistributed = true;
//        if (isDistributed) {
//            try {
//                log.info("Distributed counter enabled. Attempting to get a KeyValueStoreClient...");
//                this.kvStoreClient = KeyValueStoreManager.getClient();
//            } catch (KeyValueStoreException e) {
//                // Handle errors and fallback gracefully
//                log.error("Failed to get KeyValueStoreClient. Operating in local fallback mode. Reason: {}", e.getMessage());
//                this.kvStoreClient = null;
//            }
//        }
//        else {
//            // If not distributed, set client to null and operate in local-only mode.
//            this.kvStoreClient = null;
//            log.info("Distributed counter is disabled. Aggregator will use local in-memory counter.");
//            return; // Exit the initialization for distributed features.
//        }
//    }
//
//    // Add a lazy initialization method
//    private void ensureInitialized() {
//        if (this.key == null) {
//            this.key = QuerySelector.getThreadLocalGroupByKey();
//
//            if (this.key != null) {
//                log.info("Lazy initialization for key '{}'", this.key);
//
//                if (kvStoreClient != null) {
//                    initializeFromKeyValueStore();
//                    activeAggregators.put(key, this);
//                }
//            } else {
//                log.warn("Key is still null during lazy initialization. Query context may not be properly set.");
//            }
//        }
//    }
//
//    private void initializeFromKeyValueStore() {
//        if (kvStoreClient != null) {
//            try {
//                String redisValue = kvStoreClient.get(key);
//                if (redisValue != null) {
//                    long initialValue = Long.parseLong(redisValue);
//                    localCounter.set(initialValue);
//                    log.info("Initialized local counter for key '{}' from Redis value: {}", key, initialValue);
//                } else {
//                    // Key doesn't exist in Redis, start with 0
//                    localCounter.set(0L);
//                    kvStoreClient.set(key, "0");
//                    log.info("Key '{}' not found in Redis. Initialized with value: 0", key);
//                }
//            } catch (Exception e) {
//                log.error("Error initializing from Redis for key '{}'. Starting with local value 0. Error: {}",
//                        key, e.getMessage());
//                localCounter.set(0L);
//            }
//        }
//    }
//
//    private void syncWithKeyValueStore() {
//        if (kvStoreClient == null || key == null) {
//            return;
//        }
//        // Get and reset pending delta atomically
//        long delta = pendingDelta.getAndSet(0L);
//        if (delta == 0) {
//            //todo -> check if key exists in redis
//            localCounter.set(Long.parseLong(kvStoreClient.get(key)));
//            return; // No changes to sync
//        }
//        try {
//            if (delta > 0) {
//                // Positive delta - increment
//                long newRedisValue = kvStoreClient.incrementBy(key, delta);
//                localCounter.set(newRedisValue);
//                log.debug("Synced +{} to Redis for key '{}'", delta, key);
//            } else {
//                // Negative delta - decrement
//                long newRedisValue = kvStoreClient.decrementBy(key, Math.abs(delta));
//                localCounter.set(newRedisValue);
//                log.debug("Synced {} to Redis for key '{}'", delta, key);
//            }
//        } catch (KeyValueStoreException e) {
//            log.error("Error syncing delta {} to Redis for key '{}'. Will retry on next sync. Error: {}",
//                    delta, key, e.getMessage());
//            // Add the delta back to pending changes for retry
//            pendingDelta.addAndGet(delta);
//        }
//    }
//
//    public Attribute.Type getReturnType() {
//        return type;
//    }
//
//    @Override
//    public Object processAdd(Object data) {
//        ensureInitialized();
//        try {
//            // Fast local increment
//            localCounter.incrementAndGet();
//
//            // Track pending change for Redis sync
//            if (kvStoreClient != null && key != null) {
//                pendingDelta.incrementAndGet();
//            }
//            //todo -> return localCounter instead
//            log.info("processAdd for both local and Redis counters for key '{}'", key);
//            return localCounter.get();
//        } catch (Exception e) {
//            log.error("Error in processAdd for key '{}'. Error: {}", key, e.getMessage());
//            return localCounter.get();
//        }
//    }
//
//    @Override
//    public Object processAdd(Object[] data) {
//        return processAdd((Object) data);
//    }
//
//    @Override
//    public Object processRemove(Object data) {
//        ensureInitialized();
//        try {
//            // Fast local decrement
//            localCounter.decrementAndGet();
//            // Track pending change for Redis sync
//            if (kvStoreClient != null && key != null) {
//                pendingDelta.decrementAndGet();
//            }
//            log.info("processRemove for both local and Redis counters for key '{}'", key);
//            return localCounter.get();
//
//        } catch (Exception e) {
//            log.error("Error in processRemove for key '{}'. Error: {}", key, e.getMessage());
//            return localCounter.get();
//        }
//    }
//
//    @Override
//    public Object processRemove(Object[] data) {
//        // Similar to processAdd(Object[] data)
//        return processRemove((Object) data);
//    }
//
//    @Override
//    public Object reset() {
//        // Ensure we're initialized before resetting
//        ensureInitialized();
//
//        try {
//            localCounter.set(0L);
//
//            if (kvStoreClient != null && key != null) {
//                // Reset Redis immediately for reset operations
//                kvStoreClient.set(key, "0");
//                pendingDelta.set(0L); // Clear pending changes
//                log.info("Reset both local and Redis counters for key '{}'", key);
//            } else {
//                log.info("Reset local counter for key '{}' (Redis not available or key not set)", key);
//            }
//
//            return 0L;
//
//        } catch (KeyValueStoreException e) {
//            log.error("Error resetting Redis counter for key '{}'. Local counter reset successfully. Error: {}",
//                    key, e.getMessage());
//            return 0L;
//        }
//    }
//
//    @Override
//    public void start() {
//    }
//
//    @Override
//    public void stop() {
//        try {
//            // Only remove if key is not null
//            if (key != null) {
//                activeAggregators.remove(key);
//            }
//            // Perform final sync with Redis
//            if (kvStoreClient != null && key != null) {
//                syncWithKeyValueStore();
//                log.info("Final sync completed for key '{}' before stop", key);
//            }
//        } catch (Exception e) {
//            log.error("Error during stop for key '{}'. Error: {}", key, e.getMessage());
//        }
//    }
//
//    @Override
//    public Object[] currentState() {
//        ensureInitialized();
//        long currentValue = localCounter.get();
//        if (kvStoreClient != null && key != null) {
//            try {
//                // Sync any pending changes before returning state
//                syncWithKeyValueStore();
//                log.debug("Synced pending changes before returning state for key '{}'", key);
//            } catch (Exception e) {
//                log.warn("Could not sync with Redis before returning state for key '{}'. Using local value: {}. Error: {}",
//                        key, currentValue, e.getMessage());
//            }
//        }
//
//        return new Object[]{new AbstractMap.SimpleEntry<>("Value", currentValue)};
//    }
//
//    @Override
//    public void restoreState(Object[] state) {
//        ensureInitialized();
//        Map.Entry<String, Object> stateEntry = (Map.Entry<String, Object>) state[0];
//        long restoredValue = (Long) stateEntry.getValue();
//
//        localCounter.set(restoredValue);
//        pendingDelta.set(0L); // Clear pending changes
//
//        if (kvStoreClient != null && key != null) {
//            try {
//                kvStoreClient.set(key, String.valueOf(restoredValue));
//                log.info("Successfully restored state for key '{}' to {} in both local and Redis counters.", key, restoredValue);
//            } catch (KeyValueStoreException e) {
//                log.error("Error restoring state to Redis for key '{}'. State restored to local counter only. Error: {}",
//                        key, e.getMessage());
//            }
//        } else {
//            log.warn("restoreState: Redis not available or key not set for key '{}'. State restored to local counter only.", key);
//        }
//    }
//}
//
//---------
//
////KeyValueStoreClient
////
////package org.wso2.siddhi.core.util.kvstore;
////
/////**
//// * Interface for a generic Key-Value store client.
//// */
////public interface KeyValueStoreClient {
////
////    /**
////     * Establishes a connection to the key-value store.
////     */
////    void connect();
////
////    /**
////     * Disconnects from the key-value store and releases any associated resources.
////     * This can involve closing active connections, shutting down a connection pool,
////     * or other cleanup tasks.
////     */
////    void disconnect();
////
////    /**
////     * Checks if the client is currently connected or able to establish a connection
////     * to the key-value store.
////
////     */
////    boolean isConnected();
////
////    /**
////     * Retrieves the string value associated with the given key.
////     *
////     * @param key The key whose associated value is to be returned.
////     */
////    String get(String key);
////
////    /**
////     * Sets the string value for the given key.
////     * If the store previously contained a mapping for the key, the old value is replaced by
////     * the specified value.
////     *
////     * @param key   The key with which the specified value is to be associated.
////     * @param value The value to be associated with the specified key.
////     */
////    void set(String key, String value);
////
////    /**
////     * Increments the numeric value of a key by one.
////     * If the key does not exist, it is set to 0 before performing the operation.
////     *
////     * @param key The key whose numeric value is to be incremented.
////     * @return The value of the key after the increment operation.
////     */
////    long incrementBy(String key, long increment);
////
////    /**
////     * Decrements the numeric value of a key by one.
////     * If the key does not exist, it is set to 0 before performing the operation.
////     *
////     * @param key The key whose numeric value is to be decremented.
////     * @return The value of the key after the decrement operation.
////     */
////    long decrementBy(String key, long decrement);
////
////    /**
////     * Deletes the mapping for a key from this store if it is present.
////     *
////     * @param key The key whose mapping is to be removed from the store.
////     */
////    void delete(String key);
////}
////
////
////KeyValueStoreManager
////
////package org.wso2.siddhi.core.util.kvstore;
////
////import org.slf4j.Logger;
////import org.slf4j.LoggerFactory;
////
/////**
//// * A static manager that dynamically loads KeyValueStoreClient implementations
//// * based on system properties.
//// */
////public final class KeyValueStoreManager {
////
////    private static final Logger log = LoggerFactory.getLogger(org.wso2.siddhi.core.util.kvstore.KeyValueStoreManager.class);
////
////    // System property keys
////    public static final String KEYVALUE_STORE_TYPE_PROPERTY = "keyvalue.store.type";
////    public static final String KEYVALUE_STORE_CLIENT_CLASS_PROPERTY = "keyvalue.store.client.class";
////    public static final String DEFAULT_KV_STORE_TYPE = "redis";
////
////    private static volatile org.wso2.siddhi.core.util.kvstore.KeyValueStoreClient cachedClient;
////    private static final Object lock = new Object();
////
////    /**
////     * Private constructor to prevent instantiation of this utility class.
////     */
////    private KeyValueStoreManager() {
////    }
////
////    /**
////     * Gets an instance of a KeyValueStoreClient based on system properties.
////     * Uses lazy initialization and caches the client instance.
////     *
////     * @return A configured KeyValueStoreClient instance.
////     * @throws KeyValueStoreException if the client cannot be created.
////     */
////    public static org.wso2.siddhi.core.util.kvstore.KeyValueStoreClient getClient() throws KeyValueStoreException {
////        if (cachedClient == null) {
////            synchronized (lock) {
////                if (cachedClient == null) {
////                    cachedClient = createClient();
////                }
////            }
////        }
////        return cachedClient;
////    }
////
////    /**
////     * Creates a new KeyValueStoreClient instance based on system properties.
////     */
////    private static org.wso2.siddhi.core.util.kvstore.KeyValueStoreClient createClient() throws KeyValueStoreException {
////        try {
////            String clientClassName = resolveClientClassName();
////            log.info("Loading KeyValueStoreClient implementation: {}", clientClassName);
////
////            // Load the class using classloader
////            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
////            if (classLoader == null) {
////                classLoader = org.wso2.siddhi.core.util.kvstore.KeyValueStoreManager.class.getClassLoader();
////            }
////
////            Class<?> clientClass = classLoader.loadClass(clientClassName);
////
////            // Verify it implements KeyValueStoreClient interface
////            if (!org.wso2.siddhi.core.util.kvstore.KeyValueStoreClient.class.isAssignableFrom(clientClass)) {
////                throw new KeyValueStoreException("Class " + clientClassName +
////                        " does not implement KeyValueStoreClient interface");
////            }
////
////            // Create instance using default constructor
////            Object clientInstance = clientClass.getDeclaredConstructor().newInstance();
////            org.wso2.siddhi.core.util.kvstore.KeyValueStoreClient client = (org.wso2.siddhi.core.util.kvstore.KeyValueStoreClient) clientInstance;
////
////            // Connect the client
////            client.connect();
////
////            log.info("Successfully created and connected KeyValueStoreClient: {}", clientClassName);
////            return client;
////
////        } catch (ClassNotFoundException e) {
////            throw new KeyValueStoreException("KeyValueStoreClient implementation class not found", e);
////        } catch (InstantiationException | IllegalAccessException e) {
////            throw new KeyValueStoreException("Failed to instantiate KeyValueStoreClient", e);
////        } catch (NoSuchMethodException e) {
////            throw new KeyValueStoreException("No default constructor found for KeyValueStoreClient", e);
////        } catch (Exception e) {
////            throw new KeyValueStoreException("Error creating KeyValueStoreClient", e);
////        }
////    }
////
////    /**
////     * Resolves the client class name based on system properties.
////     */
////    private static String resolveClientClassName() {
////        // First check if a custom client class is directly specified
////        //        String customClassName = System.getProperty(KEYVALUE_STORE_CLIENT_CLASS_PROPERTY);
////        String customClassName = System.getProperty(KEYVALUE_STORE_CLIENT_CLASS_PROPERTY,
////                "org.wso2.carbon.apimgt.throttling.siddhi.extension.util.kvstore.RedisClientAdapter");
////
////        if (customClassName != null && !customClassName.trim().isEmpty()) {
////            log.info("Using custom KeyValueStoreClient class: {}", customClassName);
////            return customClassName.trim();
////        }
////
////        // Otherwise, resolve based on store type
////        String storeType = System.getProperty(KEYVALUE_STORE_TYPE_PROPERTY, DEFAULT_KV_STORE_TYPE).toLowerCase();
////        log.info("Resolving KeyValueStoreClient for store type: {}", storeType);
////
////        return getDefaultClientClassName(storeType);
////    }
////
////    /**
////     * Maps store type to default client implementation class name.
////     */
////    private static String getDefaultClientClassName(String storeType) {
////        switch (storeType) {
////        case "redis":
////            return System.getProperty("keyvalue.store.redis.class",
////                    "org.wso2.siddhi.core.util.kvstore.RedisClientAdapter");
////        case "valkey":
////            return System.getProperty("keyvalue.store.valkey.class",
////                    "org.wso2.siddhi.core.util.kvstore.ValkeyClientAdapter");
////        case "inmemory":
////            return System.getProperty("keyvalue.store.inmemory.class",
////                    "org.wso2.siddhi.core.util.kvstore.InMemoryClientAdapter");
////        case "hazelcast":
////            return System.getProperty("keyvalue.store.hazelcast.class",
////                    "org.wso2.siddhi.core.util.kvstore.HazelcastClientAdapter");
////        default:
////            throw new KeyValueStoreException("Unsupported store type: " + storeType +
////                    ". Supported types: redis, valkey, inmemory, hazelcast. " +
////                    "Or specify a custom class using: " + KEYVALUE_STORE_CLIENT_CLASS_PROPERTY);
////        }
////    }
////
////    /**
////     * Shuts down the cached client and clears the cache.
////     */
////    public static void shutdown() {
////        synchronized (lock) {
////            if (cachedClient != null) {
////                try {
////                    log.info("Shutting down KeyValueStoreClient: {}", cachedClient.getClass().getName());
////                    cachedClient.disconnect();
////                } catch (Exception e) {
////                    log.error("Error during KeyValueStoreClient shutdown", e);
////                } finally {
////                    cachedClient = null;
////                }
////            }
////        }
////    }
////
////    /**
////     * Clears the cached client instance. Next call to getClient() will create a new instance.
////     * Useful for testing or when configuration changes at runtime.
////     */
////    public static void clearCache() {
////        synchronized (lock) {
////            if (cachedClient != null) {
////                try {
////                    cachedClient.disconnect();
////                } catch (Exception e) {
////                    log.warn("Error disconnecting cached client during cache clear", e);
////                }
////                cachedClient = null;
////            }
////        }
////    }
////
////    /**
////     * Checks if a client is currently cached and connected.
////     */
////    public static boolean isClientAvailable() {
////        org.wso2.siddhi.core.util.kvstore.KeyValueStoreClient client = cachedClient;
////        return client != null && client.isConnected();
////    }
////}
////
////
////KeyValueStoreException
////
////package org.wso2.siddhi.core.util.kvstore;
////
////public class KeyValueStoreException extends RuntimeException {
////
////    public KeyValueStoreException(String message) {
////        super(message);
////    }
////
////    public KeyValueStoreException(String message, Throwable cause) {
////        super(message, cause);
////    }
////}
