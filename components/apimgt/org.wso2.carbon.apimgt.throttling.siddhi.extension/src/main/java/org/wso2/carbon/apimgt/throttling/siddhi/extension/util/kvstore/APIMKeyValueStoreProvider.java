//package org.wso2.carbon.apimgt.throttling.siddhi.extension.util.kvstore;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.wso2.siddhi.core.util.kvstore.KeyValueStoreClient;
//import org.wso2.siddhi.core.util.kvstore.KeyValueStoreException;
//
//
///**
// * Manages the creation of Key-Value store client instances based on system configuration.
// */
//public class APIMKeyValueStoreProvider implements KeyValueStoreProvider{
//
//    private static final Logger log = LoggerFactory.getLogger(APIMKeyValueStoreProvider.class);
//
//    public static final String KEYVALUE_STORE_TYPE_PROPERTY = "siddhi.kvstore.type";
//    public static final String REDIS_TYPE = "redis";
//    public static final String VALKEY_TYPE = "valkey";
//    public static final String DEFAULT_KV_STORE_TYPE = REDIS_TYPE; // Default to Redis
//
//
//    private static String lastInitializedClientType = null;
//
////    private APIMKeyValueStoreProvider() {
////        // To prevent instantiation
////    }
//
//    /**
//     * Gets an instance of a {@link KeyValueStoreClient} based on the system property
//     * {@value #KEYVALUE_STORE_TYPE_PROPERTY}.
//     *
//     * @return A configured and connected {@link KeyValueStoreClient} instance.
//     * @throws KeyValueStoreException if the specified store type is unknown or if the
//     *                                client adapter fails to connect.
//     */
//    public KeyValueStoreClient getClient() {
//        String storeType = System.getProperty(KEYVALUE_STORE_TYPE_PROPERTY, DEFAULT_KV_STORE_TYPE).toLowerCase();
//        KeyValueStoreClient client;
//
//        log.info("Attempting to initialize KeyValueStoreClient for type: {}", storeType);
//
//        switch (storeType) {
//        case REDIS_TYPE:
//            client = new RedisClientAdapter();
//            break;
//        //        case VALKEY_TYPE:
//        //            client = new ValkeyClientAdapter();
//        //            break;
//        default:
//            log.error("Unknown key-value store type specified: '{}'. Supported types are '{}' or '{}'.",
//                    storeType, REDIS_TYPE, VALKEY_TYPE);
//            throw new KeyValueStoreException("Unknown key-value store type: " + storeType);
//        }
//
//        try {
//            client.connect(); // Initialize connection parameters and connect
//            log.info("Successfully created and connected KeyValueStoreClient for type: {}", storeType);
//            lastInitializedClientType = storeType; // Track the type for shutdown purposes
//        } catch (Exception e) {
//            log.error("Failed to connect KeyValueStoreClient for type: {}. Reason: {}", storeType, e.getMessage(), e);
//            if (e instanceof KeyValueStoreException) {
//                throw (KeyValueStoreException) e;
//            }
//            throw new KeyValueStoreException("Failed to connect KeyValueStoreClient for type: " + storeType, e);
//        }
//        return client;
//    }
//
//    /**
//     * Shuts down resources associated with the configured key-value store clients.
//     */
//    public void shutdown() {
//        log.info("Shutting down KeyValueStoreManager managed resources.");
//
//        try {
//            log.info("Attempting to shutdown RedisConnectionManager pool (if used).");
//            RedisConnectionManager.shutdownPool();
//        } catch (Exception e) {
//            log.error("Error during shutdown of RedisConnectionManager pool.", e);
//        }
//
//        if (lastInitializedClientType != null) {
//            log.info("Last initialized client type was: {}. Ensure its resources are managed appropriately by the consumer.", lastInitializedClientType);
//            lastInitializedClientType = null; // Reset after logging
//        }
//        log.info("KeyValueStoreManager shutdown process completed.");
//    }
//}