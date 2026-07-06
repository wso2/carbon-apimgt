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
package org.wso2.carbon.apimgt.throttling.siddhi.extension;

import java.util.AbstractMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.dto.DistributedThrottleConfig;

import org.wso2.carbon.apimgt.throttling.siddhi.extension.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.throttling.siddhi.extension.util.kvstore.KeyValueStoreClient;
import org.wso2.carbon.apimgt.throttling.siddhi.extension.util.kvstore.KeyValueStoreException;
import org.wso2.carbon.apimgt.throttling.siddhi.extension.util.kvstore.KeyValueStoreManager;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.query.selector.attribute.aggregator.AttributeAggregator;
import org.wso2.siddhi.core.query.selector.QuerySelector;
import org.wso2.siddhi.query.api.definition.Attribute;

public class DistributedCountAttributeAggregator extends AttributeAggregator {

    private static final Log log = LogFactory.getLog(DistributedCountAttributeAggregator.class);
    private static final Attribute.Type type = Attribute.Type.LONG;
    private KeyValueStoreClient kvStoreClient;
    private String key;
    private final AtomicLong localCounter = new AtomicLong(0L);
    // Store the net change of local counter since last sync task
    private final AtomicLong unsyncedCounter = new AtomicLong(0L);
    private static final ConcurrentHashMap<String, DistributedCountAttributeAggregator> ACTIVE_AGGREGATORS =
            new ConcurrentHashMap<>();
    private final Object kvStoreLock = new Object();

    // Track the error logging status of syncing with key-value store
    private final AtomicLong lastErrorLogTimestamp = new AtomicLong(0L);
    private static final long ERROR_LOG_INTERVAL_MS = 30000L; // 30 seconds

    // Distributed throttling configs
    private static volatile DistributedThrottleConfig DISTRIBUTED_THROTTLE_CONFIG = null;
    private static boolean distributedThrottlingEnabled = false;
    private static int corePoolSize = 10;
    private static int kvStoreSyncIntervalMilliseconds = 10;

    // Scheduler initialization control
    private static volatile boolean schedulerStarted = false;
    private static final Object schedulerLock = new Object();

    // Static shared scheduler for all aggregators
    private static ScheduledExecutorService kvStoreSyncScheduler = null;
    private static ScheduledFuture<?> masterSyncTask = null;
    private static ScheduledExecutorService masterScheduler = null;
    private volatile boolean pendingReset = false;
    private volatile long storedWindowExpiry = 0L;
    private volatile boolean keyHasTTL = false;


    /**
     * The initialization method for FunctionExecutor
     *
     * @param attributeExpressionExecutors are the executors of each attributes in the function
     * @param executionPlanContext         Execution plan runtime context
     */
    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext executionPlanContext) {
        if (DISTRIBUTED_THROTTLE_CONFIG == null) {
            synchronized (DistributedCountAttributeAggregator.class) {
                if (DISTRIBUTED_THROTTLE_CONFIG == null) {
                    DistributedThrottleConfig config = getDistributedThrottleConfig();
                    if (config != null) {
                        // Plain writes BEFORE the volatile store — any thread that later reads
                        // DISTRIBUTED_THROTTLE_CONFIG as non-null sees these via JMM happens-before.
                        distributedThrottlingEnabled = config.isEnabled();
                        corePoolSize = config.getCorePoolSize();
                        kvStoreSyncIntervalMilliseconds = config.getSyncInterval();
                        DISTRIBUTED_THROTTLE_CONFIG = config; // volatile write last
                    }
                }
            }
        }
        String throttleKey = QuerySelector.getThreadLocalGroupByKey();
        if (distributedThrottlingEnabled && !schedulerStarted && throttleKey != null) {
            startScheduler();
        }
        if (distributedThrottlingEnabled && throttleKey != null) {
            this.key = "wso2_throttler:" + throttleKey;
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

    /**
     * The method to initialize the local counter from the key-value store.
     * Initialize the value in key value store if it is not set.
     */
    private void initializeFromKVStore() {
        try {
            String kvStoreValue = kvStoreClient.get(key);
            if (kvStoreValue != null) {
                long initialValue = Long.parseLong(kvStoreValue);
                localCounter.set(initialValue);
            } else {
                writeCounterValue("0");
            }
        } catch (Exception e) {
            log.error("Error initializing from key-value store for key " + key, e);
            localCounter.set(0L);
        }
    }

    /**
     * Writes the given value to the key-value store with the remaining TTL of the current window.
     * Does nothing when {@code storedWindowExpiry} is not yet known or has already passed.
     *
     * @param value the value to store for this aggregator's key.
     */
    private void writeCounterValue(String value) {
        long windowExpiry = storedWindowExpiry;
        if (windowExpiry > 0) {
            long remainingMillis = windowExpiry - System.currentTimeMillis();
            if (remainingMillis > 0) {
                kvStoreClient.setWithExpiry(key, value, remainingMillis);
            }
        }
    }

    /**
     * Synchronize the local counter with the key-value store.
     * Pending resets (PSETEX "0") are flushed first and retried on failure; then any
     * accumulated delta is pushed via INCRBY/DECRBY, or the current Redis value is pulled
     * when there is no local change.
     */
    private void syncWithKVStore() {
        synchronized (kvStoreLock) {
            if (kvStoreClient == null || key == null) {
                return;
            }
            if (pendingReset) {
                try {
                    writeCounterValue("0");
                    // Do NOT reset localCounter here — reset() already zeroed it at window boundary.
                    // Any increments to localCounter since then belong to the new window and are valid.
                    pendingReset = false; // cleared only on success — failure retried next tick
                    keyHasTTL = true;
                } catch (KeyValueStoreException e) {
                    long currentTimeMillis = System.currentTimeMillis();
                    if (currentTimeMillis - lastErrorLogTimestamp.get() > ERROR_LOG_INTERVAL_MS) {
                        log.error("Error resetting counter in key-value store for key " + key, e);
                        lastErrorLogTimestamp.set(currentTimeMillis);
                    }
                }
                return;
            }
            long currentUnsyncedCount = unsyncedCounter.getAndSet(0L);
            if (pendingReset) {
                // reset() fired between our initial pendingReset check and getAndSet —
                // discard the captured old-window delta; the next tick will PSETEX 0.
                return;
            }
            try {
                if (currentUnsyncedCount == 0) {
                    String kvStoreValue = kvStoreClient.get(key);
                    if (kvStoreValue == null) {
                        writeCounterValue("0");
                        localCounter.set(0L);
                    } else if (!pendingReset) {
                        // skip if reset() fired while waiting for the GET response —
                        // the returned value is the old-window total and must not overwrite localCounter=0.
                        localCounter.set(Long.parseLong(kvStoreValue));
                    }
                } else {
                    long kvStoreValue;
                    if (currentUnsyncedCount > 0) {
                        kvStoreValue = kvStoreClient.incrementBy(key, currentUnsyncedCount);
                    } else {
                        kvStoreValue = kvStoreClient.decrementBy(key, Math.abs(currentUnsyncedCount));
                    }
                    // INCRBY/DECRBY never sets a TTL. Stamp one the first time after a gap
                    // (empty windows) where the key was recreated without a TTL.
                    if (!keyHasTTL) {
                        long expiry = storedWindowExpiry;
                        long remainingMillis = expiry - System.currentTimeMillis();
                        if (remainingMillis > 0) {
                            try {
                                kvStoreClient.expireMillis(key, remainingMillis);
                                keyHasTTL = true;
                            } catch (KeyValueStoreException e) {
                                // Non-fatal: retried on the next sync tick while keyHasTTL is false.
                                long now = System.currentTimeMillis();
                                if (now - lastErrorLogTimestamp.get() > ERROR_LOG_INTERVAL_MS) {
                                    log.warn("Could not set TTL via PEXPIRE for key " + key, e);
                                    lastErrorLogTimestamp.set(now);
                                }
                            }
                        }
                    }
                    if (!pendingReset) {
                        // guard: reset() can fire while the INCRBY/DECRBY is in flight;
                        // discard the result to avoid reinstating old-window counts in localCounter.
                        localCounter.set(kvStoreValue);
                    }
                }
            } catch (KeyValueStoreException | NumberFormatException e) {
                long currentTimeMillis = System.currentTimeMillis();
                if (currentTimeMillis - lastErrorLogTimestamp.get() > ERROR_LOG_INTERVAL_MS) {
                    log.error("Error syncing with key-value store for the key " + key, e);
                    lastErrorLogTimestamp.set(currentTimeMillis);
                }
                // skip restore if reset() fired after the snapshot — old-window delta must be discarded
                if (!pendingReset) {
                    unsyncedCounter.addAndGet(currentUnsyncedCount);
                }
            }
        }
    }

    public Attribute.Type getReturnType() {
        return type;
    }

    /**
     * Process an add event by incrementing the local counter.
     * If distributed throttling is enabled, also increments the unsynced counter
     * which will be synchronized with the distributed key-value store.
     *
     * @param data The event data to be added.
     * @return The updated value of the local counter after increment.
     */
    @Override
    public Object processAdd(Object data) {
        try {
            long newCount = localCounter.incrementAndGet();
            if (distributedThrottlingEnabled && kvStoreClient != null && key != null) {
                unsyncedCounter.incrementAndGet();
                // Refresh storedWindowExpiry when stale — this happens after one or more empty
                // windows where reset() was never called (no RESET event → no reset()).
                if (storedWindowExpiry <= System.currentTimeMillis()) {
                    Long expiry = ThrottleStreamProcessor.getThreadLocalWindowExpiry();
                    if (expiry != null && expiry > storedWindowExpiry) {
                        storedWindowExpiry = expiry;
                        keyHasTTL = false; // key was created by INCRBY after gap — needs PEXPIRE
                    }
                }
            }
            return newCount;
        } catch (Exception e) {
            log.error("Error in processAdd for key " + key, e);
            return localCounter.get();
        }
    }

    @Override
    public Object processAdd(Object[] data) {
        if (log.isDebugEnabled()) {
            log.debug("DistributedCountAggregator: processAdd called with data: "
                    + (data != null && data.length > 0 ? data[0] : null));
        }
        if (isResetRequested(data)) {
            return reset();
        }
        return processAdd(data != null && data.length > 0 ? data[0] : null);
    }


    /**
     * Process a remove event by decrementing the local counter.
     * If distributed throttling is enabled, also decrements the unsynced counter
     * which will be synchronized with the distributed key-value store.
     *
     * @param data The event data to be removed.
     * @return The updated value of the local counter after decrement.
     */
    @Override
    public Object processRemove(Object data) {
        if (log.isDebugEnabled()) {
            log.debug("DistributedCountAggregator: processRemove called with data: " + data);
        }
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
        return processRemove(data != null && data.length > 0 ? data[0] : null);
    }


    /**
     * Resets the local counter to zero and signals the background sync thread to PSETEX "0"
     * on its next tick. Never blocks on a Redis network call. Transient failures are retried
     * automatically.
     *
     * @return 0L after reset.
     */
    @Override
    public Object reset() {
        if (log.isDebugEnabled()) {
            log.debug("DistributedCountAggregator: reset called");
        }
        localCounter.set(0L);
        if (distributedThrottlingEnabled && kvStoreClient != null && key != null) {
            Long expiry = ThrottleStreamProcessor.getThreadLocalWindowExpiry();
            if (expiry != null) {
                storedWindowExpiry = expiry;
            }
            unsyncedCounter.set(0L);
            pendingReset = true;
        }
        return 0L;
    }

    @Override
    public void start() {
        //Nothing to start
    }

    /**
     * Stops the aggregator instance and performs cleanup.
     * Removes the aggregator from the active aggregator map, synchronizes any unsynced changes
     * with the key-value store, and shuts down the scheduler if there are no more active aggregators.
     * This method should be called when the aggregator is no longer needed.
     */
    @Override
    public void stop() {
        if (log.isDebugEnabled()) {
            log.debug("DistributedCountAggregator: stop called");
        }
        try {
            // Only remove if key is not null and distributed throttling is enabled
            if (distributedThrottlingEnabled && key != null) {
                ACTIVE_AGGREGATORS.remove(key);
                if (kvStoreClient != null) {
                    syncWithKVStore(); // best-effort: flush pendingReset (PSETEX 0) if pending
                    if (!pendingReset) {
                        syncWithKVStore(); // best-effort: push new-window delta; both may be no-ops if Redis is down
                    }
                }
                // Shutdown scheduler if no active aggregators exist
                if (ACTIVE_AGGREGATORS.isEmpty()) {
                    shutdownScheduler();
                }
            }
        } catch (Exception e) {
            log.error("Error during stop for key " + key, e);
        }
    }

    @Override
    public Object[] currentState() {
        if (log.isDebugEnabled()) {
            log.debug("DistributedCountAggregator: currentState called");
        }
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
        if (log.isDebugEnabled()) {
            log.debug("DistributedCountAggregator: restoreState called with state: " + state);
        }
        Map.Entry<String, Object> stateEntry = (Map.Entry<String, Object>) state[0];
        long restoredValue = (Long) stateEntry.getValue();

        // In distributed mode, Redis is authoritative — ignore the stale single-node Siddhi snapshot.
        // Fall back to 0 on key-absent or Redis error; the sync scheduler recovers the real value once Redis is up.
        // In non-distributed mode, the Siddhi snapshot is the only state.
        // Redis GET runs outside the lock to avoid blocking the sync thread during network I/O.
        long counterToRestore = distributedThrottlingEnabled ? 0L : restoredValue;
        boolean seedRedis = false;
        if (distributedThrottlingEnabled && kvStoreClient != null && key != null) {
            try {
                String kvStoreValue = kvStoreClient.get(key);
                if (kvStoreValue != null) {
                    counterToRestore = Long.parseLong(kvStoreValue);
                } else {
                    seedRedis = true;
                }
            } catch (KeyValueStoreException | NumberFormatException e) {
                log.error("Error reading from key-value store during restoreState for key "
                        + key + ". Starting fresh at 0.", e);
            }
        }

        // Lock only for the fast in-memory writes — no Redis call inside.
        synchronized (kvStoreLock) {
            unsyncedCounter.set(0L);
            pendingReset = false;
            localCounter.set(counterToRestore);
        }

        // Key absent in Redis — attempt to seed with 0; no-op if storedWindowExpiry is not yet known.
        if (seedRedis) {
            try {
                writeCounterValue("0");
            } catch (KeyValueStoreException e) {
                log.error("Error seeding key-value store with 0 during restoreState for key " + key, e);
            }
        }
    }

    /**
     * Retrieves the distributed throttle configuration for the API Manager.
     * Attempts to fetch the configuration from the service reference holder.
     * If fetching the configuration fails, logs a warning message and returns null.
     *
     * @return the {@link DistributedThrottleConfig} instance if successfully loaded, or null if loading fails.
     */
    private static DistributedThrottleConfig getDistributedThrottleConfig() {
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

    /**
     * Starts the scheduler responsible for periodically synchronizing all active aggregators
     * with the distributed key-value store. The scheduler runs at a fixed interval and submits
     * sync tasks for each active aggregator instance. This ensures that local counter changes
     * are propagated to the distributed store in a timely manner.
     * The scheduler is shared among all aggregator instances and is only started once.
     */
    private static void startScheduler() {
        if (!distributedThrottlingEnabled || schedulerStarted) {
            return;
        }
        synchronized (schedulerLock) {
            if (!distributedThrottlingEnabled || schedulerStarted) {
                return;
            }
            if (kvStoreSyncScheduler == null) {
                kvStoreSyncScheduler = Executors.newScheduledThreadPool(corePoolSize, r ->
                        new Thread(r, "apim-distributed-throttle-sync"));
            }
            if (masterScheduler == null) {
                masterScheduler = Executors.newSingleThreadScheduledExecutor(r ->
                        new Thread(r, "apim-distributed-throttle-master-sync"));
            }

            log.debug("Starting key-value store sync scheduler with interval: "
                    + kvStoreSyncIntervalMilliseconds + " ms, pool size: " + corePoolSize);

            masterSyncTask = masterScheduler.scheduleAtFixedRate(() -> {
                try {
                    CompletableFuture<?>[] futures = ACTIVE_AGGREGATORS.values().stream()
                            .map(aggregator -> CompletableFuture.runAsync(() -> {
                                try {
                                    aggregator.syncWithKVStore();
                                } catch (Throwable t) {
                                    log.error("Error syncing with key-value store for key " + aggregator.key, t);
                                }
                            }, kvStoreSyncScheduler))
                            .toArray(CompletableFuture[]::new);
                    if (futures.length == 0) {
                        return;
                    }
                    CompletableFuture.allOf(futures).join();

                } catch (Throwable t) {
                    log.error("Error in key-value store sync scheduler task", t);
                }
            }, kvStoreSyncIntervalMilliseconds, kvStoreSyncIntervalMilliseconds, TimeUnit.MILLISECONDS);

            schedulerStarted = true;
        }
    }

    /**
     * Shuts down the key-value store sync scheduler and the KeyValueStoreManager.
     * This method is called when there are no active aggregators remaining.
     * It ensures that all scheduled sync tasks are stopped and resources such as thread pools
     * and key-value store connections are properly released.
     */
    public static void shutdownScheduler() {
        synchronized (schedulerLock) {
            if (masterSyncTask != null) {
                masterSyncTask.cancel(false);
                masterSyncTask = null;
            }
            shutdownExecutor(kvStoreSyncScheduler, "key-value store sync scheduler");
            kvStoreSyncScheduler = null;
            shutdownExecutor(masterScheduler, "master sync scheduler");
            masterScheduler = null;
            schedulerStarted = false;

            // Shutdown the KeyValueStoreManager to close JedisPool
            try {
                KeyValueStoreManager.shutdown();
            } catch (Exception e) {
                log.error("Error shutting down KeyValueStoreManager", e);
            }
        }
    }

    /**
     * Shuts down the given executor service gracefully, forcing shutdown if it does not
     * terminate within the timeout.
     *
     * @param executor The executor to shut down.
     * @param name     Name of the executor for logging.
     */
    private static void shutdownExecutor(ScheduledExecutorService executor, String name) {
        if (executor != null && !executor.isShutdown()) {
            log.debug("Shutting down " + name + "...");
            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    log.warn("The " + name + " did not terminate in time. Forcing shutdown...");
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                log.warn("Interrupted while shutting down " + name + ". Forcing shutdown...");
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    private boolean isResetRequested(Object[] data) {
        return data != null && data.length > 1 && Boolean.TRUE.equals(data[1]);
    }

}

