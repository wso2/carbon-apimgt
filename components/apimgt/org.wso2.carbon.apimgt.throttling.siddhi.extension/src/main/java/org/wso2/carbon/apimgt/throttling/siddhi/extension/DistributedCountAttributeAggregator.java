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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
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
import org.wso2.carbon.apimgt.throttling.siddhi.extension.util.kvstore.KeyValueStoreOperation;
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
    // Batch sync configs ([apim.distributed_throttling] enable_batch_sync and batch_size in deployment.toml)
    private static boolean batchSyncEnabled = true;
    private static int batchSize = DistributedThrottleConfig.DEFAULT_BATCH_SIZE;

    // Rate-limits the error log of a failed batch (one log covers every key in it)
    private static final AtomicLong lastBatchErrorLogTimestamp = new AtomicLong(0L);
    // Sync statistics, logged at DEBUG level every SYNC_STATS_LOG_INTERVAL_MS
    private static final long SYNC_STATS_LOG_INTERVAL_MS = 30000L; // 30 seconds
    private static final SyncStats SYNC_STATS = new SyncStats();

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

    // Incremented by every reset(). A batched reply is applied only if no reset happened after its
    // operation was collected; otherwise the reply belongs to the closed window and is dropped.
    private final AtomicLong resetEpoch = new AtomicLong(0L);
    // Set when a batched read finds the key missing; the next tick creates it with SET NX PX "0".
    // Guarded by kvStoreLock.
    private boolean seedNeeded = false;
    // True from the tick that collects a batched write (PSETEX, INCRBY/DECRBY, SET NX) for this key until its
    // reply or failure is applied. syncWithKVStore() writes nothing meanwhile, so its writes cannot interleave
    // with the batch. Guarded by kvStoreLock.
    private boolean writeInFlight = false;
    // Set by stop(). A batched write still in flight for this key then flushes the key itself when it completes.
    private volatile boolean stopped = false;


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
                        batchSyncEnabled = config.isBatchSyncEnabled();
                        batchSize = config.getBatchSize() > 0
                                ? config.getBatchSize() : DistributedThrottleConfig.DEFAULT_BATCH_SIZE;
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
            Long windowExpiry = ThrottleStreamProcessor.getThreadLocalWindowExpiry();
            if (windowExpiry != null) {
                this.storedWindowExpiry = windowExpiry;
            }
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
     * Does nothing when {@code storedWindowExpiry} is not yet known or has already passed —
     * callers must check the return value rather than assume the write happened.
     *
     * @param value the value to store for this aggregator's key.
     * @return true if the value was actually written to the key-value store, false if the write
     *         was skipped because the window expiry was unknown or already in the past.
     */
    private boolean writeCounterValue(String value) {
        long windowExpiry = storedWindowExpiry;
        if (windowExpiry <= 0) {
            return false;
        }
        long remainingMillis = windowExpiry - System.currentTimeMillis();
        if (remainingMillis <= 0) {
            return false;
        }
        kvStoreClient.setWithExpiry(key, value, remainingMillis);
        return true;
    }

    /**
     * Synchronize the local counter with the key-value store.
     * Pending resets (PSETEX "0") are flushed first and retried on failure; then any
     * accumulated delta is pushed via INCRBY/DECRBY, or the current Redis value is pulled
     * when there is no local change.
     * Does nothing while a batched write for this key is in flight.
     */
    private void syncWithKVStore() {
        synchronized (kvStoreLock) {
            if (kvStoreClient == null || key == null) {
                return;
            }
            if (writeInFlight) {
                // Writing now could interleave with the batched write (e.g. its late PSETEX "0" would wipe an
                // INCRBY sent here). The pending reset and delta stay for the next tick, or for the batch's
                // completion if the key is stopped.
                return;
            }
            if (pendingReset) {
                try {
                    // Do NOT reset localCounter here — reset() already zeroed it at window boundary.
                    // Any increments to localCounter since then belong to the new window and are valid.
                    if (writeCounterValue("0")) {
                        pendingReset = false; // cleared only on a confirmed write — retried next tick otherwise
                        keyHasTTL = true;
                    } else {
                        keyHasTTL = false;
                    }
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

    /**
     * Batch sync, step 1 (collect): decides this key's operation for the current tick and adds it to the
     * collector. Uses the same rules as {@link #syncWithKVStore()}, but does no I/O, so the lock is held
     * only briefly.
     * <ul>
     *     <li>pending reset: PSETEX "0" with the remaining window TTL</li>
     *     <li>unsynced delta: INCRBY/DECRBY, plus PEXPIRE if the key has no known TTL</li>
     *     <li>key found missing on the last read: SET NX PX "0"</li>
     *     <li>otherwise: read the key (MGET)</li>
     * </ul>
     * A write marks the key {@code writeInFlight} until its reply or failure is applied.
     */
    private void collectSyncUnit(BatchCollector collector) {
        synchronized (kvStoreLock) {
            if (kvStoreClient == null || key == null) {
                return;
            }
            long epoch = resetEpoch.get();
            long windowExpiry = storedWindowExpiry;
            long remainingMillis = windowExpiry > 0 ? windowExpiry - System.currentTimeMillis() : 0L;
            if (pendingReset) {
                seedNeeded = false; // the reset writes the key
                if (remainingMillis > 0) {
                    collector.addWrite(new SyncUnit(this, SyncUnit.Kind.RESET, epoch, windowExpiry, 0L,
                            KeyValueStoreOperation.setWithExpiry(key, "0", remainingMillis)));
                    writeInFlight = true;
                } else {
                    // Same as syncWithKVStore(): nothing is written while the window expiry is unknown or past.
                    keyHasTTL = false;
                }
                return;
            }
            long delta = unsyncedCounter.getAndSet(0L);
            if (delta != 0) {
                KeyValueStoreOperation count = delta > 0
                        ? KeyValueStoreOperation.incrementBy(key, delta)
                        : KeyValueStoreOperation.decrementBy(key, Math.abs(delta));
                // INCRBY/DECRBY never sets a TTL. Stamp one the first time after a gap
                // (empty windows) where the key was recreated without a TTL.
                if (!keyHasTTL && remainingMillis > 0) {
                    collector.addWrite(new SyncUnit(this, SyncUnit.Kind.COUNT, epoch, windowExpiry, delta, count,
                            KeyValueStoreOperation.expireMillis(key, remainingMillis)));
                } else {
                    collector.addWrite(new SyncUnit(this, SyncUnit.Kind.COUNT, epoch, windowExpiry, delta, count));
                }
                writeInFlight = true;
                return;
            }
            if (seedNeeded) {
                seedNeeded = false;
                if (remainingMillis > 0) {
                    collector.addWrite(new SyncUnit(this, SyncUnit.Kind.SEED, epoch, windowExpiry, 0L,
                            KeyValueStoreOperation.setIfAbsentWithExpiry(key, "0", remainingMillis)));
                    writeInFlight = true;
                    return;
                }
            }
            collector.addRead(new SyncUnit(this, SyncUnit.Kind.READ, epoch, windowExpiry, 0L));
        }
    }

    /**
     * Batch sync, step 3 (apply) for a read: takes the cluster-wide count from the MGET reply.
     *
     * @param value the key's value in the key-value store, or null if the key does not exist.
     */
    private void applyRead(SyncUnit unit, String value) {
        synchronized (kvStoreLock) {
            if (resetEpoch.get() != unit.resetEpoch) {
                return; // reset() ran after the read was collected: the value is the closed window's total
            }
            if (value == null) {
                // The key is missing, so the stored count is 0. It is created on the next tick.
                setCounterFromStore(unit, 0L);
                seedNeeded = true;
                return;
            }
            try {
                setCounterFromStore(unit, Long.parseLong(value));
            } catch (NumberFormatException e) {
                logRateLimited("Error syncing with key-value store for the key " + key, e, false);
            }
        }
    }

    /**
     * Batch sync, step 3 (apply) for a write: handles the pipeline replies of this key's operations.
     *
     * @param replies the replies of {@code unit.operations}, in the same order.
     */
    private void applyWriteReplies(SyncUnit unit, List<Object> replies) {
        Object reply = replies.get(0);
        if (reply instanceof Exception) {
            applyWriteFailure(unit, (Exception) reply, true);
            return;
        }
        if (unit.kind == SyncUnit.Kind.COUNT && !(reply instanceof Number)) {
            applyWriteFailure(unit, new KeyValueStoreException("Unexpected reply for " + unit.operations.get(0)
                    + ": " + reply), true);
            return;
        }
        synchronized (kvStoreLock) {
            writeInFlight = false;
            boolean noResetSinceCollect = resetEpoch.get() == unit.resetEpoch;
            if (unit.kind == SyncUnit.Kind.RESET) {
                // If another reset() ran meanwhile, its PSETEX is still pending and is written on a later tick.
                if (noResetSinceCollect) {
                    pendingReset = false;
                    keyHasTTL = storedWindowExpiry == unit.windowExpiry;
                }
            } else if (unit.kind == SyncUnit.Kind.COUNT) {
                if (replies.size() > 1) {
                    Object ttlReply = replies.get(1);
                    if (ttlReply instanceof Exception) {
                        // Non-fatal: retried with the next INCRBY/DECRBY while keyHasTTL is false.
                        logRateLimited("Could not set TTL via PEXPIRE for key " + key, (Exception) ttlReply, true);
                    } else if (storedWindowExpiry == unit.windowExpiry) {
                        keyHasTTL = true;
                    }
                }
                if (noResetSinceCollect) {
                    setCounterFromStore(unit, ((Number) reply).longValue());
                }
            }
            // SEED: nothing to update. If the key exists now, the next read returns its value.
        }
        if (stopped) {
            // stop() skipped its flush while this write was in flight: push what is left now.
            flushToKVStore();
        }
    }

    /**
     * Batch sync, step 3 (apply) for a failed write. A failed reset stays pending and is retried on the
     * next tick. A failed INCRBY/DECRBY delta is added back to the unsynced counter, unless a reset made
     * it part of the closed window.
     *
     * @param logError false when the whole batch failed and was already logged once for all its keys.
     */
    private void applyWriteFailure(SyncUnit unit, Exception cause, boolean logError) {
        synchronized (kvStoreLock) {
            writeInFlight = false;
            if (logError) {
                String message;
                if (unit.kind == SyncUnit.Kind.RESET) {
                    message = "Error resetting counter in key-value store for key " + key;
                } else if (unit.kind == SyncUnit.Kind.SEED) {
                    message = "Error seeding key-value store with 0 for key " + key;
                } else {
                    message = "Error syncing with key-value store for the key " + key;
                }
                logRateLimited(message, cause, false);
            }
            if (unit.kind == SyncUnit.Kind.COUNT && resetEpoch.get() == unit.resetEpoch) {
                unsyncedCounter.addAndGet(unit.delta);
            }
        }
        if (stopped) {
            // stop() skipped its flush while this write was in flight: push the restored delta and any
            // pending reset now.
            flushToKVStore();
        }
    }

    /**
     * Sets the local counter to a value read from the key-value store, plus the requests counted since the
     * operation was collected (still in unsyncedCounter, not yet in the store). The caller holds kvStoreLock
     * and has checked that no reset happened since the operation was collected.
     */
    private void setCounterFromStore(SyncUnit unit, long value) {
        localCounter.set(value + unsyncedCounter.get());
        if (resetEpoch.get() != unit.resetEpoch) {
            // reset() does not take kvStoreLock, so it can run between the caller's check and the set above.
            // Drop the closed window's value and keep only the requests counted since the reset.
            localCounter.set(unsyncedCounter.get());
        }
    }

    /**
     * Logs a sync error for this key at most once per {@link #ERROR_LOG_INTERVAL_MS}.
     */
    private void logRateLimited(String message, Throwable t, boolean warnOnly) {
        long currentTimeMillis = System.currentTimeMillis();
        if (currentTimeMillis - lastErrorLogTimestamp.get() > ERROR_LOG_INTERVAL_MS) {
            if (warnOnly) {
                log.warn(message, t);
            } else {
                log.error(message, t);
            }
            lastErrorLogTimestamp.set(currentTimeMillis);
        }
    }

    /**
     * Best-effort flush of a pending reset and then of the unsynced delta. Both may be no-ops if the
     * key-value store is down.
     */
    private void flushToKVStore() {
        syncWithKVStore(); // flush pendingReset (PSETEX 0) if pending
        if (!pendingReset) {
            syncWithKVStore(); // push the new-window delta
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
     * Note: not invoked in practice for throttle plans, since {@link ThrottleStreamProcessor}
     * never emits EXPIRED events to this selector.
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
        // Before zeroing the counter: a batched reply collected before this reset must see the new epoch.
        resetEpoch.incrementAndGet();
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
     * If a batched write for this key is in flight, the flush runs when that write completes.
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
                stopped = true;
                ACTIVE_AGGREGATORS.remove(key);
                if (kvStoreClient != null) {
                    flushToKVStore();
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
            log.debug("DistributedCountAggregator: restoreState called with state: " + Arrays.toString(state));
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
            // Do NOT clear pendingReset here. If a reset() flagged an unflushed PSETEX 0 and this
            // restore landed before the sync thread flushed it, dropping the flag would leave the
            // previous window's total in Redis. Keep it pending (the next sync tick still flushes
            // it) and seed localCounter to 0 to match what reset() already intended.
            if (pendingReset) {
                localCounter.set(0L);
            } else {
                localCounter.set(counterToRestore);
            }
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
                    + kvStoreSyncIntervalMilliseconds + " ms, pool size: " + corePoolSize
                    + ", batch sync: " + (batchSyncEnabled ? "enabled, batch size: " + batchSize : "disabled"));

            masterSyncTask = masterScheduler.scheduleAtFixedRate(() -> {
                try {
                    runSyncTick();
                } catch (Throwable t) {
                    log.error("Error in key-value store sync scheduler task", t);
                }
            }, kvStoreSyncIntervalMilliseconds, kvStoreSyncIntervalMilliseconds, TimeUnit.MILLISECONDS);

            schedulerStarted = true;
        }
    }

    /**
     * One sync tick over all active aggregators.
     * <p>
     * Keys whose client supports batch operations (and when batch sync is enabled) are synced in three steps:
     * <ol>
     *     <li>collect: each key's operation is added to a read list or a write list (brief lock, no I/O)</li>
     *     <li>send: the lists are split into chunks of at most {@code batchSize} keys or commands, and the
     *     chunks are sent in parallel on the sync pool, one MGET or one pipeline each</li>
     *     <li>apply: each reply is given back to its key (brief lock, reset-epoch check)</li>
     * </ol>
     * The other keys are synced one by one with {@link #syncWithKVStore()}, as before.
     */
    private static void runSyncTick() {
        long startNanos = System.nanoTime();
        ScheduledExecutorService executor = kvStoreSyncScheduler;
        if (executor == null) {
            return;
        }
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        Map<KeyValueStoreClient, BatchCollector> collectors = new IdentityHashMap<>();
        int keyCount = 0;
        int perKeySyncs = 0;
        for (DistributedCountAttributeAggregator aggregator : ACTIVE_AGGREGATORS.values()) {
            keyCount++;
            KeyValueStoreClient client = aggregator.kvStoreClient;
            if (batchSyncEnabled && client != null && client.supportsBatchOperations()) {
                BatchCollector collector = collectors.get(client);
                if (collector == null) {
                    collector = new BatchCollector(client);
                    collectors.put(client, collector);
                }
                try {
                    aggregator.collectSyncUnit(collector);
                } catch (Throwable t) {
                    log.error("Error preparing key-value store sync for key " + aggregator.key, t);
                }
            } else {
                perKeySyncs++;
                try {
                    futures.add(CompletableFuture.runAsync(() -> {
                        try {
                            aggregator.syncWithKVStore();
                        } catch (Throwable t) {
                            log.error("Error syncing with key-value store for key " + aggregator.key, t);
                        }
                    }, executor));
                } catch (RejectedExecutionException e) {
                    // The scheduler is shutting down. Nothing was taken from this key, so nothing is lost.
                    log.debug("Key-value store sync skipped for key " + aggregator.key + ": scheduler is shutting down");
                }
            }
        }

        int readBatches = 0;
        int writeBatches = 0;
        int writeCommands = 0;
        for (BatchCollector collector : collectors.values()) {
            for (BatchChunk chunk : collector.toChunks(batchSize)) {
                if (chunk.write) {
                    writeBatches++;
                    writeCommands += chunk.operationCount;
                } else {
                    readBatches++;
                }
                try {
                    futures.add(CompletableFuture.runAsync(chunk, executor));
                } catch (RejectedExecutionException e) {
                    chunk.fail(e); // the scheduler is shutting down
                }
            }
        }
        if (!futures.isEmpty()) {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0])).join();
        }
        SYNC_STATS.record(keyCount, perKeySyncs, readBatches, writeBatches, writeCommands,
                System.nanoTime() - startNanos);
    }

    /**
     * Logs the error of a failed batch at most once per {@link #ERROR_LOG_INTERVAL_MS}.
     */
    private static void logBatchError(String message, Throwable t) {
        long currentTimeMillis = System.currentTimeMillis();
        long previous = lastBatchErrorLogTimestamp.get();
        if (currentTimeMillis - previous > ERROR_LOG_INTERVAL_MS
                && lastBatchErrorLogTimestamp.compareAndSet(previous, currentTimeMillis)) {
            log.error(message, t);
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

    private static boolean isResetRequested(Object[] data) {
        return data != null && data.length > 1 && Boolean.TRUE.equals(data[1]);
    }

    /**
     * One key's work in a batched sync tick: a read, or one or two write operations.
     */
    private static final class SyncUnit {

        enum Kind {
            /** MGET the key. */
            READ,
            /** PSETEX "0" for a pending reset. */
            RESET,
            /** INCRBY/DECRBY the unsynced delta, plus PEXPIRE when the key has no known TTL. */
            COUNT,
            /** SET NX PX "0" for a key found missing. */
            SEED
        }

        final DistributedCountAttributeAggregator owner;
        final Kind kind;
        // The owner's reset epoch and window expiry when the unit was collected
        final long resetEpoch;
        final long windowExpiry;
        // The delta taken from the unsynced counter (COUNT only)
        final long delta;
        final List<KeyValueStoreOperation> operations;

        SyncUnit(DistributedCountAttributeAggregator owner, Kind kind, long resetEpoch, long windowExpiry,
                 long delta, KeyValueStoreOperation... operations) {
            this.owner = owner;
            this.kind = kind;
            this.resetEpoch = resetEpoch;
            this.windowExpiry = windowExpiry;
            this.delta = delta;
            this.operations = operations.length == 0
                    ? Collections.<KeyValueStoreOperation>emptyList() : Arrays.asList(operations);
        }
    }

    /**
     * The reads and writes collected in one tick for one key-value store client.
     */
    private static final class BatchCollector {

        private final KeyValueStoreClient client;
        private final List<SyncUnit> reads = new ArrayList<>();
        private final List<SyncUnit> writes = new ArrayList<>();

        BatchCollector(KeyValueStoreClient client) {
            this.client = client;
        }

        void addRead(SyncUnit unit) {
            reads.add(unit);
        }

        void addWrite(SyncUnit unit) {
            writes.add(unit);
        }

        /**
         * Splits the reads into chunks of at most {@code maxSize} keys and the writes into chunks of at most
         * {@code maxSize} commands. A key's write operations (INCRBY and its PEXPIRE) stay in the same chunk.
         */
        List<BatchChunk> toChunks(int maxSize) {
            List<BatchChunk> chunks = new ArrayList<>();
            for (int from = 0; from < reads.size(); from += maxSize) {
                List<SyncUnit> units = reads.subList(from, Math.min(from + maxSize, reads.size()));
                chunks.add(new BatchChunk(client, units, false, 0));
            }
            List<SyncUnit> units = new ArrayList<>();
            int operationCount = 0;
            for (SyncUnit unit : writes) {
                int unitOperations = unit.operations.size();
                if (operationCount > 0 && operationCount + unitOperations > maxSize) {
                    chunks.add(new BatchChunk(client, units, true, operationCount));
                    units = new ArrayList<>();
                    operationCount = 0;
                }
                units.add(unit);
                operationCount += unitOperations;
            }
            if (!units.isEmpty()) {
                chunks.add(new BatchChunk(client, units, true, operationCount));
            }
            return chunks;
        }
    }

    /**
     * One round trip of a batched tick: an MGET of up to batchSize keys, or a pipeline of up to batchSize
     * write commands. Runs on the sync pool, then applies each reply to its key.
     */
    private static final class BatchChunk implements Runnable {

        private final KeyValueStoreClient client;
        private final List<SyncUnit> units;
        final boolean write;
        final int operationCount;

        BatchChunk(KeyValueStoreClient client, List<SyncUnit> units, boolean write, int operationCount) {
            this.client = client;
            this.units = units;
            this.write = write;
            this.operationCount = operationCount;
        }

        @Override
        public void run() {
            try {
                if (write) {
                    sendWrites();
                } else {
                    sendReads();
                }
            } catch (Throwable t) {
                log.error("Error in batched key-value store sync", t);
            }
        }

        private void sendReads() {
            List<String> keys = new ArrayList<>(units.size());
            for (SyncUnit unit : units) {
                keys.add(unit.owner.key);
            }
            List<String> values;
            try {
                values = client.getMultiple(keys);
                if (values == null || values.size() != keys.size()) {
                    throw new KeyValueStoreException("Expected " + keys.size() + " values from the key-value store"
                            + " but got " + (values == null ? "none" : values.size()));
                }
            } catch (RuntimeException e) {
                fail(e);
                return;
            }
            for (int i = 0; i < units.size(); i++) {
                SyncUnit unit = units.get(i);
                try {
                    unit.owner.applyRead(unit, values.get(i));
                } catch (RuntimeException e) {
                    log.error("Error applying key-value store value for key " + unit.owner.key, e);
                }
            }
        }

        private void sendWrites() {
            List<KeyValueStoreOperation> operations = new ArrayList<>(operationCount);
            for (SyncUnit unit : units) {
                operations.addAll(unit.operations);
            }
            List<Object> replies;
            try {
                replies = client.executeBatch(operations);
                if (replies == null || replies.size() != operations.size()) {
                    throw new KeyValueStoreException("Expected " + operations.size() + " replies from the key-value"
                            + " store but got " + (replies == null ? "none" : replies.size()));
                }
            } catch (RuntimeException e) {
                fail(e);
                return;
            }
            int index = 0;
            for (SyncUnit unit : units) {
                int unitOperations = unit.operations.size();
                try {
                    unit.owner.applyWriteReplies(unit, replies.subList(index, index + unitOperations));
                } catch (RuntimeException e) {
                    log.error("Error applying key-value store reply for key " + unit.owner.key, e);
                }
                index += unitOperations;
            }
        }

        /**
         * Handles a chunk that could not be sent or whose replies could not be read: logs once for the whole
         * chunk and applies the failure to every key in it (deltas are restored and resets stay pending).
         */
        void fail(Exception cause) {
            SYNC_STATS.failedBatches.incrementAndGet();
            logBatchError(write
                    ? "Error writing " + operationCount + " operations of " + units.size() + " keys to the key-value store"
                    : "Error reading " + units.size() + " keys from the key-value store", cause);
            if (!write) {
                return; // a failed read changes nothing; the keys are read again on the next tick
            }
            for (SyncUnit unit : units) {
                try {
                    unit.owner.applyWriteFailure(unit, cause, false);
                } catch (RuntimeException e) {
                    log.error("Error restoring unsynced count for key " + unit.owner.key, e);
                }
            }
        }
    }

    /**
     * Sync statistics for the DEBUG summary logged every {@link #SYNC_STATS_LOG_INTERVAL_MS}.
     * Updated by the master sync thread only, except {@code failedBatches}.
     */
    private static final class SyncStats {

        private final AtomicLong failedBatches = new AtomicLong(0L);
        private long windowStartMillis = System.currentTimeMillis();
        private long ticks;
        private long keys;
        private long perKeySyncs;
        private long readBatches;
        private long writeBatches;
        private long writeCommands;
        private long totalTickNanos;
        private long maxTickNanos;

        void record(int keyCount, int perKeySyncCount, int readBatchCount, int writeBatchCount,
                    int writeCommandCount, long tickNanos) {
            ticks++;
            keys += keyCount;
            perKeySyncs += perKeySyncCount;
            readBatches += readBatchCount;
            writeBatches += writeBatchCount;
            writeCommands += writeCommandCount;
            totalTickNanos += tickNanos;
            maxTickNanos = Math.max(maxTickNanos, tickNanos);

            long now = System.currentTimeMillis();
            if (now - windowStartMillis < SYNC_STATS_LOG_INTERVAL_MS) {
                return;
            }
            if (log.isDebugEnabled()) {
                log.debug(String.format("Distributed throttling sync in the last %d s: %d ticks, %.1f keys per tick,"
                                + " %.1f MGET and %.1f pipeline round trips per tick, %.1f pipelined commands per"
                                + " tick, %.1f per-key syncs per tick, %d failed batches, tick duration avg %.2f ms,"
                                + " max %.2f ms",
                        (now - windowStartMillis) / 1000, ticks, (double) keys / ticks,
                        (double) readBatches / ticks, (double) writeBatches / ticks,
                        (double) writeCommands / ticks, (double) perKeySyncs / ticks, failedBatches.getAndSet(0L),
                        totalTickNanos / 1e6 / ticks, maxTickNanos / 1e6));
            } else {
                failedBatches.set(0L);
            }
            windowStartMillis = now;
            ticks = 0;
            keys = 0;
            perKeySyncs = 0;
            readBatches = 0;
            writeBatches = 0;
            writeCommands = 0;
            totalTickNanos = 0;
            maxTickNanos = 0;
        }
    }
}

