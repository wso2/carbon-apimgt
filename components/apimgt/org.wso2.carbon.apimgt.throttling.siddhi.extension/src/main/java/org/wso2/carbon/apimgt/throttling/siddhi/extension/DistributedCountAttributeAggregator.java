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
package org.wso2.carbon.apimgt.throttling.siddhi.extension;

import java.util.AbstractMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.dto.DistributedThrottleConfig;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.throttling.siddhi.extension.util.kvstore.KeyValueStoreClient;
import org.wso2.carbon.apimgt.throttling.siddhi.extension.util.kvstore.KeyValueStoreException;
import org.wso2.carbon.apimgt.throttling.siddhi.extension.util.kvstore.KeyValueStoreManager;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.query.selector.attribute.aggregator.AttributeAggregator;
import org.wso2.siddhi.core.query.selector.QuerySelector;
import org.wso2.siddhi.query.api.definition.Attribute;

public class DistributedCountAttributeAggregator extends AttributeAggregator {

    private static final Log log = LogFactory.getLog(
            DistributedCountAttributeAggregator.class);
    private static Attribute.Type type = Attribute.Type.LONG;
    private KeyValueStoreClient kvStoreClient;
    private String key;
    private final AtomicLong localCounter = new AtomicLong(0L);
    // Store the net change of local counter since last sync task
    private final AtomicLong unsyncedCounter = new AtomicLong(0L);
    private static final ConcurrentHashMap<String, DistributedCountAttributeAggregator> ACTIVE_AGGREGATORS =
            new ConcurrentHashMap<>();

    // Distributed throttling configs
    private static DistributedThrottleConfig DISTRIBUTED_THROTTLE_CONFIG = null;
    private static boolean distributedThrottlingEnabled = false;
    private static int corePoolSize = 10;
    private static int kvStoreSyncIntervalMilliseconds = 10;

    // Scheduler initialization control
    private static volatile boolean schedulerStarted = false;
    private static final Object schedulerLock = new Object();

    // Static shared scheduler for all aggregators
    private static ScheduledExecutorService kvStoreSyncScheduler = null;


    /**
     * The initialization method for FunctionExecutor
     *
     * @param attributeExpressionExecutors are the executors of each attributes in the function
     * @param executionPlanContext         Execution plan runtime context
     */
    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext executionPlanContext) {
        if (DISTRIBUTED_THROTTLE_CONFIG == null) {
            DISTRIBUTED_THROTTLE_CONFIG = getDistributedThrottleConfig();
            if (DISTRIBUTED_THROTTLE_CONFIG != null) {
                distributedThrottlingEnabled = DISTRIBUTED_THROTTLE_CONFIG.isEnabled();
                corePoolSize = DISTRIBUTED_THROTTLE_CONFIG.getCorePoolSize();
                kvStoreSyncIntervalMilliseconds = DISTRIBUTED_THROTTLE_CONFIG.getSyncInterval();
            }
        }
        if (distributedThrottlingEnabled && !schedulerStarted) {
            startScheduler();
        }
        String throttleKey = QuerySelector.getThreadLocalGroupByKey();
        if (distributedThrottlingEnabled && throttleKey != null) {
            this.key = "distributed_count:" + throttleKey;
            try {
                this.kvStoreClient = KeyValueStoreManager.getClient();
                if (this.kvStoreClient != null) {
                    initializeFromKVStore();
                    ACTIVE_AGGREGATORS.put(key, this);
                }
            } catch (KeyValueStoreException e) {
                log.error("Failed to initialize KeyValueStoreClient for aggregator with key " + key, e);
                this.kvStoreClient = null;
            } catch (Exception e) {
                log.error("Unexpected error initializing KeyValueStoreClient for aggregator with key " + key, e);
                this.kvStoreClient = null;
            }
        }
    }

    private void initializeFromKVStore() {
        try {
            String kvStoreValue = kvStoreClient.get(key);
            if (kvStoreValue != null) {
                long initialValue = Long.parseLong(kvStoreValue);
                localCounter.set(initialValue);
            } else {
                kvStoreClient.set(key, "0");
            }
        } catch (Exception e) {
            log.error("Error initializing from key-value store for key " + key, e);
            localCounter.set(0L);
        }
    }

    private void syncWithKVStore() {
        if (kvStoreClient == null || key == null) {
            return;
        }
        long delta = unsyncedCounter.getAndSet(0L);
        if (delta == 0) {
            localCounter.set(Long.parseLong(kvStoreClient.get(key)));
            return;
        }
        try {
            if (delta > 0) {
                localCounter.set(kvStoreClient.incrementBy(key, delta));
            } else {
                localCounter.set(kvStoreClient.decrementBy(key, Math.abs(delta)));
            }
        } catch (KeyValueStoreException e) {
            log.error("Error syncing with key-value store for the key " + key, e);
            unsyncedCounter.addAndGet(delta);
        }
    }

    public Attribute.Type getReturnType() {
        return type;
    }

    @Override
    public Object processAdd(Object data) {
        try {
            localCounter.incrementAndGet();
            if (distributedThrottlingEnabled && kvStoreClient != null && key != null) {
                unsyncedCounter.incrementAndGet();
            }
            return localCounter.get();
        } catch (Exception e) {
            log.error("Error in processAdd for key " + key, e);
            return localCounter.get();
        }
    }

    @Override
    public Object processAdd(Object[] data) {
        return processAdd((Object) data);
    }

    @Override
    public Object processRemove(Object data) {
        try {
            localCounter.decrementAndGet();
            if (distributedThrottlingEnabled && kvStoreClient != null && key != null) {
                unsyncedCounter.decrementAndGet();
            }
            return localCounter.get();

        } catch (Exception e) {
            log.error("Error in processRemove for key " + key, e);
            return localCounter.get();
        }
    }

    @Override
    public Object processRemove(Object[] data) {
        return processRemove((Object) data);
    }

    @Override
    public Object reset() {
        try {
            localCounter.set(0L);
            if (distributedThrottlingEnabled && kvStoreClient != null && key != null) {
                kvStoreClient.set(key, "0");
                unsyncedCounter.set(0L); // Clear pending changes
            }
            return 0L;

        } catch (KeyValueStoreException e) {
            log.error("Error resetting counter for key " + key, e);
            return 0L;
        }
    }

    @Override
    public void start() {
        //Nothing to start
    }

    @Override
    public void stop() {
        try {
            // Only remove if key is not null and distributed throttling is enabled
            if (distributedThrottlingEnabled && key != null) {
                ACTIVE_AGGREGATORS.remove(key);
                if (kvStoreClient != null) {
                    syncWithKVStore();
                }
            }
            // Shutdown scheduler if no active aggregators
            if (ACTIVE_AGGREGATORS.isEmpty()) {
                shutdownScheduler();
                schedulerStarted = false; // Reset for potential restart
            }
        } catch (Exception e) {
            log.error("Error during stop for key " + key, e);
        }
    }

    @Override
    public Object[] currentState() {
        if (distributedThrottlingEnabled && kvStoreClient != null && key != null) {
            try {
                syncWithKVStore();
            } catch (Exception e) {
                log.warn("Could not sync with key-value store before returning state for key " + key, e);
            }
        }
        return new Object[]{new AbstractMap.SimpleEntry<String, Object>("Value", localCounter.get())};
    }

    @Override
    public void restoreState(Object[] state) {
        Map.Entry<String, Object> stateEntry = (Map.Entry<String, Object>) state[0];
        long restoredValue = (Long) stateEntry.getValue();

        localCounter.set(restoredValue);
        unsyncedCounter.set(0L);

        if (distributedThrottlingEnabled && kvStoreClient != null && key != null) {
            try {
                kvStoreClient.set(key, String.valueOf(restoredValue));
            } catch (KeyValueStoreException e) {
                log.error("Error restoring state to key-value store for key "+ key, e);
            }
        }
    }

    private static DistributedThrottleConfig getDistributedThrottleConfig() {
        //check apimconfig is null or not
        try {
            return ServiceReferenceHolder.getInstance()
                    .getAPIManagerConfigurationService()
                    .getAPIManagerConfiguration()
                    .getDistributedThrottleConfig();
        } catch (Exception e) {
            log.warn("Failed to load distributed throttle configuration from API Manager config. Using defaults.", e);
            return null;
        }
    }

    private static void startScheduler() {
        if (!distributedThrottlingEnabled || schedulerStarted) {
            return;
        }
        synchronized (schedulerLock) {
            if (!distributedThrottlingEnabled || schedulerStarted) {
                return;
            }
            if (kvStoreSyncScheduler == null) {
                kvStoreSyncScheduler = Executors.newScheduledThreadPool(corePoolSize, r -> {
                    Thread t = new Thread(r, "key-value store-Sync-Thread");
                    t.setDaemon(true);
                    return t;
                });
            }

            log.info("Starting key-value store sync scheduler with interval: "
                    + kvStoreSyncIntervalMilliseconds + " ms, pool size: " + corePoolSize);

            kvStoreSyncScheduler.scheduleAtFixedRate(() -> {
                try {
                    for (DistributedCountAttributeAggregator aggregator : ACTIVE_AGGREGATORS.values()) {
                        kvStoreSyncScheduler.submit(() -> {
                            try {
                                aggregator.syncWithKVStore();
                            } catch (Throwable t) {
                                log.error("Error syncing with key-value store for key " + aggregator.key, t);
                            }
                        });
                    }
                } catch (Throwable t) {
                    log.error("Error in key-value store sync scheduler task", t);
                }
            }, kvStoreSyncIntervalMilliseconds, kvStoreSyncIntervalMilliseconds, TimeUnit.MILLISECONDS);

            schedulerStarted = true;
        }
    }

    public static void shutdownScheduler() {
        synchronized (schedulerLock) {
            if (kvStoreSyncScheduler != null && !kvStoreSyncScheduler.isShutdown()) {
                log.info("Shutting down key-value store sync scheduler...");
                kvStoreSyncScheduler.shutdown();
                try {
                    if (!kvStoreSyncScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                        log.warn("The key-value store sync scheduler did not terminate in time. Forcing shutdown...");
                        kvStoreSyncScheduler.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    log.warn("Interrupted while shutting down key-value store sync scheduler. Forcing shutdown...");
                    kvStoreSyncScheduler.shutdownNow();
                    Thread.currentThread().interrupt();
                }
            }
            kvStoreSyncScheduler = null;
            schedulerStarted = false;

            // Shutdown the KeyValueStoreManager to close JedisPool
            try {
                KeyValueStoreManager.shutdown();
            } catch (Exception e) {
                log.error("Error shutting down KeyValueStoreManager", e);
            }
        }
    }

}

