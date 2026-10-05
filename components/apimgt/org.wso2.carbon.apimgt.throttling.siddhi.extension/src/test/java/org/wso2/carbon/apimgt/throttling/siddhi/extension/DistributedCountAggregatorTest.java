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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.wso2.carbon.apimgt.impl.dto.DistributedThrottleConfig;
import org.wso2.carbon.apimgt.throttling.siddhi.extension.util.kvstore.KeyValueStoreClient;
import org.wso2.carbon.apimgt.throttling.siddhi.extension.util.kvstore.KeyValueStoreException;
import org.wso2.carbon.apimgt.throttling.siddhi.extension.util.kvstore.KeyValueStoreManager;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;

import java.lang.reflect.Field;
import java.util.AbstractMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Unit tests for DistributedCountAttributeAggregator's Redis interaction logic.
 *
 * Dependencies: JUnit 4, siddhi-core, apimgt.impl (already in test scope).
 * No Mockito or other mocking frameworks are used.
 *
 * Test strategy: reflection is used to inject a fake KeyValueStoreClient and to read
 * private state fields after operations, bypassing the OSGi ServiceReferenceHolder so
 * tests run outside a Carbon container.
 */
public class DistributedCountAggregatorTest {

    // -----------------------------------------------------------------------
    // Fake in-memory KeyValueStoreClient — records calls for assertion
    // -----------------------------------------------------------------------

    private static class InMemoryKeyValueStoreClient implements KeyValueStoreClient {

        final ConcurrentHashMap<String, String> store = new ConcurrentHashMap<>();
        final AtomicInteger setWithExpiryCount = new AtomicInteger();
        final AtomicInteger expireMillisCount = new AtomicInteger();
        final AtomicInteger incrementByCount = new AtomicInteger();
        volatile String lastSetWithExpiryValue;
        volatile long lastSetWithExpiryTTL = -1L;
        volatile long lastExpireMillisTTL = -1L;

        @Override
        public void setWithExpiry(String key, String value, long ttlMillis) {
            store.put(key, value);
            lastSetWithExpiryValue = value;
            lastSetWithExpiryTTL = ttlMillis;
            setWithExpiryCount.incrementAndGet();
        }

        @Override
        public void expireMillis(String key, long ttlMillis) {
            lastExpireMillisTTL = ttlMillis;
            expireMillisCount.incrementAndGet();
        }

        @Override
        public long incrementBy(String key, long increment) {
            String result = store.merge(key, String.valueOf(increment),
                    (existing, delta) -> String.valueOf(Long.parseLong(existing) + Long.parseLong(delta)));
            incrementByCount.incrementAndGet();
            return Long.parseLong(result);
        }

        @Override
        public long decrementBy(String key, long decrement) {
            String result = store.merge(key, String.valueOf(-decrement),
                    (existing, delta) -> String.valueOf(Long.parseLong(existing) + Long.parseLong(delta)));
            return Long.parseLong(result);
        }

        @Override
        public String get(String key) {
            return store.get(key);
        }

        @Override
        public void set(String key, String value) {
            store.put(key, value);
        }

        @Override
        public void delete(String key) {
            store.remove(key);
        }

        @Override
        public void disconnect() {
            store.clear();
        }
    }

    /** Throws on every setWithExpiry call — simulates a Redis PSETEX failure. */
    private static class FailingOnWriteKeyValueStoreClient extends InMemoryKeyValueStoreClient {
        @Override
        public void setWithExpiry(String key, String value, long ttlMillis) {
            throw new KeyValueStoreException("Simulated PSETEX failure");
        }
    }

    /** Throws on every incrementBy call — simulates a Redis INCRBY failure. */
    private static class FailingOnIncrByKeyValueStoreClient extends InMemoryKeyValueStoreClient {
        @Override
        public long incrementBy(String key, long increment) {
            throw new KeyValueStoreException("Simulated INCRBY failure");
        }
    }

    /** Fails setWithExpiry only for keys containing the given substring — simulates one
     *  node/shard of a Redis-backed store being unavailable while others are healthy. */
    private static class PartiallyFailingKeyValueStoreClient extends InMemoryKeyValueStoreClient {
        private final String failingKeySubstring;
        volatile int failureCount = 0;

        PartiallyFailingKeyValueStoreClient(String failingKeySubstring) {
            this.failingKeySubstring = failingKeySubstring;
        }

        @Override
        public void setWithExpiry(String key, String value, long ttlMillis) {
            if (key.contains(failingKeySubstring)) {
                failureCount++;
                throw new KeyValueStoreException("Simulated PSETEX failure for " + key);
            }
            super.setWithExpiry(key, value, ttlMillis);
        }
    }

    /** Throws on every get call — simulates a Redis GET failure (as opposed to a
     *  successful GET that simply returns null for an absent key). */
    private static class FailingOnGetKeyValueStoreClient extends InMemoryKeyValueStoreClient {
        @Override
        public String get(String key) {
            throw new KeyValueStoreException("Simulated GET failure");
        }
    }

    /** Blocks inside incrementBy until released — used to deterministically land a
     *  reset() call in the middle of an in-flight syncWithKVStore() INCRBY, without relying
     *  on timing. */
    private static class BlockingOnIncrByKeyValueStoreClient extends InMemoryKeyValueStoreClient {
        final CountDownLatch enteredIncrBy = new CountDownLatch(1);
        final CountDownLatch releaseIncrBy = new CountDownLatch(1);

        @Override
        public long incrementBy(String key, long increment) {
            enteredIncrBy.countDown();
            try {
                releaseIncrBy.await(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return super.incrementBy(key, increment);
        }
    }

    /** Blocks inside get() until released — used to deterministically land a reset() call in
     *  the middle of an in-flight syncWithKVStore() GET (the zero-delta path), without relying
     *  on timing. */
    private static class BlockingOnGetKeyValueStoreClient extends InMemoryKeyValueStoreClient {
        final CountDownLatch enteredGet = new CountDownLatch(1);
        final CountDownLatch releaseGet = new CountDownLatch(1);

        @Override
        public String get(String key) {
            enteredGet.countDown();
            try {
                releaseGet.await(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return super.get(key);
        }
    }

    /** Blocks inside incrementBy until released, then throws — used to deterministically land
     *  a reset() call in the middle of an INCRBY that goes on to FAIL, so the two can be
     *  combined without relying on timing. */
    private static class BlockingThenFailingOnIncrByKeyValueStoreClient extends InMemoryKeyValueStoreClient {
        final CountDownLatch enteredIncrBy = new CountDownLatch(1);
        final CountDownLatch releaseIncrBy = new CountDownLatch(1);

        @Override
        public long incrementBy(String key, long increment) {
            enteredIncrBy.countDown();
            try {
                releaseIncrBy.await(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            throw new KeyValueStoreException("Simulated INCRBY failure after reset raced in");
        }
    }

    // -----------------------------------------------------------------------
    // Reflection helpers
    // -----------------------------------------------------------------------

    private static void setStaticField(Class<?> clazz, String name, Object value) throws Exception {
        Field f = clazz.getDeclaredField(name);
        f.setAccessible(true);
        f.set(null, value);
    }

    private static Object getStaticField(Class<?> clazz, String name) throws Exception {
        Field f = clazz.getDeclaredField(name);
        f.setAccessible(true);
        return f.get(null);
    }

    private static void setInstanceField(Object obj, String name, Object value) throws Exception {
        Field f = obj.getClass().getDeclaredField(name);
        f.setAccessible(true);
        f.set(obj, value);
    }

    private static Object getInstanceField(Object obj, String name) throws Exception {
        Field f = obj.getClass().getDeclaredField(name);
        f.setAccessible(true);
        return f.get(obj);
    }

    @SuppressWarnings("unchecked")
    private static ThreadLocal<Long> getWindowExpiryThreadLocal() throws Exception {
        Field f = ThrottleStreamProcessor.class.getDeclaredField("windowExpiryThreadLocal");
        f.setAccessible(true);
        return (ThreadLocal<Long>) f.get(null);
    }

    // -----------------------------------------------------------------------
    // Setup / teardown
    // -----------------------------------------------------------------------

    private InMemoryKeyValueStoreClient fakeClient;

    @Before
    public void setUp() throws Exception {
        fakeClient = new InMemoryKeyValueStoreClient();

        // Build a minimal config so init() does not attempt ServiceReferenceHolder lookup.
        DistributedThrottleConfig config = new DistributedThrottleConfig();
        config.setEnabled(true);
        config.setSyncInterval(10);
        config.setCorePoolSize(2);

        // Inject static state on DCAA: config + flags.
        setStaticField(DistributedCountAttributeAggregator.class, "DISTRIBUTED_THROTTLE_CONFIG", config);
        setStaticField(DistributedCountAttributeAggregator.class, "distributedThrottlingEnabled", true);
        setStaticField(DistributedCountAttributeAggregator.class, "kvStoreSyncIntervalMilliseconds", 10);
        setStaticField(DistributedCountAttributeAggregator.class, "corePoolSize", 2);
        // Keep schedulerStarted=true so the background scheduler is never launched during tests.
        setStaticField(DistributedCountAttributeAggregator.class, "schedulerStarted", true);

        // Inject the fake client into KeyValueStoreManager so any getClient() call returns it.
        setStaticField(KeyValueStoreManager.class, "clientInstance", fakeClient);
    }

    @After
    public void tearDown() throws Exception {
        // Stop any scheduler started by the Siddhi-based tests so its periodic task cannot
        // sync aggregators belonging to a later test.
        DistributedCountAttributeAggregator.shutdownScheduler();

        // Reset all static state between tests.
        setStaticField(DistributedCountAttributeAggregator.class, "DISTRIBUTED_THROTTLE_CONFIG", null);
        setStaticField(DistributedCountAttributeAggregator.class, "distributedThrottlingEnabled", false);
        setStaticField(DistributedCountAttributeAggregator.class, "schedulerStarted", false);

        @SuppressWarnings("unchecked")
        ConcurrentHashMap<String, DistributedCountAttributeAggregator> active =
                (ConcurrentHashMap<String, DistributedCountAttributeAggregator>)
                        getStaticField(DistributedCountAttributeAggregator.class, "ACTIVE_AGGREGATORS");
        active.clear();

        setStaticField(KeyValueStoreManager.class, "clientInstance", null);

        // Remove any stale ThreadLocal value left by a test.
        getWindowExpiryThreadLocal().remove();
    }

    /**
     * Creates a DCAA instance with all necessary fields injected via reflection.
     * init() is bypassed — the background scheduler is not started.
     */
    private DistributedCountAttributeAggregator createAggregator(String throttleKey) throws Exception {
        DistributedCountAttributeAggregator aggregator = new DistributedCountAttributeAggregator();
        setInstanceField(aggregator, "key", "wso2_throttler:" + throttleKey);
        setInstanceField(aggregator, "kvStoreClient", fakeClient);
        return aggregator;
    }

    /**
     * Sets the windowExpiryThreadLocal on the calling thread.
     * Must be paired with a removeWindowExpiryThreadLocal() call to avoid leaking state.
     */
    private void setWindowExpiryThreadLocal(long expiryMillis) throws Exception {
        getWindowExpiryThreadLocal().set(expiryMillis);
    }

    private void removeWindowExpiryThreadLocal() throws Exception {
        getWindowExpiryThreadLocal().remove();
    }

    // -----------------------------------------------------------------------
    // Tests
    // -----------------------------------------------------------------------

    /**
     * reset() must set pendingReset=true non-blockingly; the subsequent syncWithKVStore()
     * (triggered via currentState()) must issue a PSETEX "0" call and clear the flag.
     */
    @Test
    public void pendingResetIsSetByResetAndClearedAfterSync() throws Exception {
        DistributedCountAttributeAggregator aggregator = createAggregator("testKey1");
        long futureExpiry = System.currentTimeMillis() + 60_000L;
        setWindowExpiryThreadLocal(futureExpiry);
        try {
            aggregator.reset();
        } finally {
            removeWindowExpiryThreadLocal();
        }

        boolean pendingResetAfterReset = (boolean) getInstanceField(aggregator, "pendingReset");
        Assert.assertTrue("pendingReset must be true immediately after reset()", pendingResetAfterReset);

        aggregator.currentState(); // triggers syncWithKVStore()

        boolean pendingResetAfterSync = (boolean) getInstanceField(aggregator, "pendingReset");
        Assert.assertFalse("pendingReset must be cleared after syncWithKVStore() succeeds", pendingResetAfterSync);

        Assert.assertEquals("setWithExpiry must be called exactly once for the PSETEX 0",
                1, fakeClient.setWithExpiryCount.get());
        Assert.assertEquals("PSETEX value must be 0 to reset the window count",
                "0", fakeClient.lastSetWithExpiryValue);
        Assert.assertTrue("PSETEX TTL must be positive (remaining window time)",
                fakeClient.lastSetWithExpiryTTL > 0);
    }

    /**
     * After processAdd() detects a stale storedWindowExpiry (empty-window gap scenario),
     * keyHasTTL is set to false. The subsequent syncWithKVStore() must call PEXPIRE exactly
     * once. A second syncWithKVStore() must not call PEXPIRE again because keyHasTTL=true.
     */
    @Test
    public void expireMillisCalledOncePerWindowAfterGap() throws Exception {
        DistributedCountAttributeAggregator aggregator = createAggregator("testKey2");

        // storedWindowExpiry starts at 0L (class default) — simulates a post-gap state.
        // Set ThreadLocal to a future expiry so processAdd() can refresh storedWindowExpiry.
        long futureExpiry = System.currentTimeMillis() + 60_000L;
        setWindowExpiryThreadLocal(futureExpiry);
        try {
            aggregator.processAdd((Object) null);
        } finally {
            removeWindowExpiryThreadLocal();
        }

        // First sync: INCRBY fires; keyHasTTL=false so PEXPIRE is also called.
        aggregator.currentState();
        Assert.assertEquals("PEXPIRE must be called once after detecting a gap",
                1, fakeClient.expireMillisCount.get());
        Assert.assertTrue("PEXPIRE TTL must be positive", fakeClient.lastExpireMillisTTL > 0);

        // Second sync: another delta forces the INCRBY branch again, but keyHasTTL is now
        // true — no additional PEXPIRE.
        aggregator.processAdd((Object) null);
        aggregator.currentState();
        Assert.assertEquals("PEXPIRE must not be called again when keyHasTTL is already true",
                1, fakeClient.expireMillisCount.get());
    }

    /**
     * When storedWindowExpiry is in the past (stale — as happens after empty windows),
     * processAdd() must refresh it from the ThreadLocal and mark keyHasTTL=false.
     */
    @Test
    public void storedWindowExpiryRefreshedFromThreadLocalWhenStale() throws Exception {
        DistributedCountAttributeAggregator aggregator = createAggregator("testKey3");

        // Simulate a stale storedWindowExpiry (default 0L is already in the past).
        long futureExpiry = System.currentTimeMillis() + 30_000L;
        setWindowExpiryThreadLocal(futureExpiry);
        try {
            aggregator.processAdd((Object) null);
        } finally {
            removeWindowExpiryThreadLocal();
        }

        long stored = (long) getInstanceField(aggregator, "storedWindowExpiry");
        Assert.assertEquals("storedWindowExpiry must be refreshed to the ThreadLocal value",
                futureExpiry, stored);

        boolean keyHasTTL = (boolean) getInstanceField(aggregator, "keyHasTTL");
        Assert.assertFalse("keyHasTTL must be false after storedWindowExpiry is refreshed (key needs PEXPIRE)",
                keyHasTTL);
    }

    /**
     * Multiple processAdd() calls must accumulate in unsyncedCounter. syncWithKVStore()
     * (via currentState()) must flush the full delta in a single INCRBY and zero
     * unsyncedCounter afterwards.
     */
    @Test
    public void accumulatedDeltaFlushedViaIncrementBy() throws Exception {
        DistributedCountAttributeAggregator aggregator = createAggregator("testKey4");

        // Give a valid future expiry so the TTL path doesn't interfere.
        long futureExpiry = System.currentTimeMillis() + 60_000L;
        setInstanceField(aggregator, "storedWindowExpiry", futureExpiry);
        setInstanceField(aggregator, "keyHasTTL", true);

        aggregator.processAdd((Object) null);
        aggregator.processAdd((Object) null);
        aggregator.processAdd((Object) null);

        AtomicLong unsynced = (AtomicLong) getInstanceField(aggregator, "unsyncedCounter");
        Assert.assertEquals("unsyncedCounter must hold all 3 increments before sync", 3L, unsynced.get());

        aggregator.currentState(); // triggers syncWithKVStore()

        Assert.assertEquals("incrementBy must be called exactly once to flush the batch delta",
                1, fakeClient.incrementByCount.get());
        Assert.assertEquals("Redis value after INCRBY must equal 3",
                "3", fakeClient.store.get("wso2_throttler:testKey4"));
        Assert.assertEquals("unsyncedCounter must be zero after a successful sync", 0L, unsynced.get());
    }

    /**
     * reset() must zero both localCounter and unsyncedCounter immediately (non-blocking)
     * and set pendingReset=true. No Redis call should occur during reset() itself.
     */
    @Test
    public void resetZerosCountersImmediatelyWithoutBlockingOnRedis() throws Exception {
        DistributedCountAttributeAggregator aggregator = createAggregator("testKey5");

        long futureExpiry = System.currentTimeMillis() + 60_000L;
        setInstanceField(aggregator, "storedWindowExpiry", futureExpiry);
        setInstanceField(aggregator, "keyHasTTL", true);

        aggregator.processAdd((Object) null);
        aggregator.processAdd((Object) null);
        aggregator.processAdd((Object) null);
        aggregator.processAdd((Object) null);
        aggregator.processAdd((Object) null);

        AtomicLong local = (AtomicLong) getInstanceField(aggregator, "localCounter");
        AtomicLong unsynced = (AtomicLong) getInstanceField(aggregator, "unsyncedCounter");
        Assert.assertEquals("localCounter must be 5 before reset", 5L, local.get());
        Assert.assertEquals("unsyncedCounter must be 5 before reset", 5L, unsynced.get());

        setWindowExpiryThreadLocal(futureExpiry);
        try {
            Object result = aggregator.reset();
            Assert.assertEquals("reset() must return 0L", 0L, result);
        } finally {
            removeWindowExpiryThreadLocal();
        }

        Assert.assertEquals("localCounter must be 0 immediately after reset()", 0L, local.get());
        Assert.assertEquals("unsyncedCounter must be 0 immediately after reset()", 0L, unsynced.get());
        Assert.assertTrue("pendingReset must be true after reset()",
                (boolean) getInstanceField(aggregator, "pendingReset"));

        // No Redis calls must have been made by reset() itself.
        Assert.assertEquals("reset() must not call setWithExpiry — that is the sync thread's job",
                0, fakeClient.setWithExpiryCount.get());
        Assert.assertEquals("reset() must not call expireMillis",
                0, fakeClient.expireMillisCount.get());
        Assert.assertEquals("reset() must not call incrementBy",
                0, fakeClient.incrementByCount.get());
    }

    /**
     * storedWindowExpiry must not be updated when the ThreadLocal value is not newer than
     * the current storedWindowExpiry (prevents regression to an older window's boundary).
     */
    @Test
    public void storedWindowExpiryNotOverwrittenWithOlderValue() throws Exception {
        DistributedCountAttributeAggregator aggregator = createAggregator("testKey6");

        long futureExpiry = System.currentTimeMillis() + 60_000L;
        setInstanceField(aggregator, "storedWindowExpiry", futureExpiry);
        setInstanceField(aggregator, "keyHasTTL", true);

        // storedWindowExpiry is in the future, so processAdd() should NOT refresh it.
        long olderExpiry = System.currentTimeMillis() + 1_000L; // older than current
        setWindowExpiryThreadLocal(olderExpiry);
        try {
            aggregator.processAdd((Object) null);
        } finally {
            removeWindowExpiryThreadLocal();
        }

        long stored = (long) getInstanceField(aggregator, "storedWindowExpiry");
        Assert.assertEquals("storedWindowExpiry must not be overwritten with a value from the past",
                futureExpiry, stored);
        Assert.assertTrue("keyHasTTL must remain true when storedWindowExpiry was not refreshed",
                (boolean) getInstanceField(aggregator, "keyHasTTL"));
    }

    /**
     * writeCounterValue() must be a no-op when storedWindowExpiry is 0 (not yet known).
     * Concretely: reset() called before any window expiry is known — the subsequent
     * syncWithKVStore() must NOT issue a PSETEX, and since nothing was actually written,
     * pendingReset must stay true so a later tick (once the expiry is known) retries the flush
     * instead of silently losing the reset.
     */
    @Test
    public void writeCounterValueIsNoOpWhenStoredWindowExpiryIsZero() throws Exception {
        DistributedCountAttributeAggregator aggregator = createAggregator("testKey7");
        // storedWindowExpiry stays 0 (default) — no ThreadLocal set before reset()
        aggregator.reset();

        boolean pendingResetAfterReset = (boolean) getInstanceField(aggregator, "pendingReset");
        Assert.assertTrue("pendingReset must be true after reset()", pendingResetAfterReset);

        aggregator.currentState(); // triggers syncWithKVStore() → pendingReset path → writeCounterValue no-op

        Assert.assertEquals("PSETEX must not be called when storedWindowExpiry is 0",
                0, fakeClient.setWithExpiryCount.get());
        Assert.assertTrue("pendingReset must remain true since nothing was actually written to Redis",
                (boolean) getInstanceField(aggregator, "pendingReset"));
    }

    /**
     * writeCounterValue() must be a no-op when the window has already expired
     * (storedWindowExpiry is in the past). No PSETEX must be sent in this case, and since the
     * reset was never actually flushed, pendingReset must stay true so the next tick retries it.
     */
    @Test
    public void writeCounterValueIsNoOpWhenWindowHasExpired() throws Exception {
        DistributedCountAttributeAggregator aggregator = createAggregator("testKey8");
        // Simulate a storedWindowExpiry that is already in the past.
        setInstanceField(aggregator, "storedWindowExpiry", System.currentTimeMillis() - 5_000L);
        setInstanceField(aggregator, "pendingReset", true);

        aggregator.currentState(); // triggers syncWithKVStore() → pendingReset path → remainingMillis <= 0 → no-op

        Assert.assertEquals("PSETEX must not be called when the window has already expired",
                0, fakeClient.setWithExpiryCount.get());
        Assert.assertTrue("pendingReset must remain true since the write never actually happened",
                (boolean) getInstanceField(aggregator, "pendingReset"));
    }

    /**
     * The remainingMillis > 0 boundary check in writeCounterValue() must not be some
     * hard-coded/rounded threshold that only works for the large (tens-of-seconds) windows
     * used elsewhere in this file — a SHORT but still genuinely positive remaining window
     * must still fire the PSETEX. Uses a 2-second margin (comfortably clear of test-execution
     * jitter) rather than a millisecond-scale value, which would risk flipping negative before
     * the assertion runs and turning a real bug into an indistinguishable false failure.
     */
    @Test
    public void writeCounterValueFiresWhenRemainingTimeIsShortButPositive() throws Exception {
        DistributedCountAttributeAggregator aggregator = createAggregator("testKey27");
        setInstanceField(aggregator, "pendingReset", true);
        long shortRemainingMs = 2000L;
        setInstanceField(aggregator, "storedWindowExpiry", System.currentTimeMillis() + shortRemainingMs);

        aggregator.currentState();

        Assert.assertEquals("PSETEX must still fire when the remaining window time is short "
                + "but positive", 1, fakeClient.setWithExpiryCount.get());
        Assert.assertTrue("The TTL sent must be positive and no larger than the short window "
                + "itself — got: " + fakeClient.lastSetWithExpiryTTL,
                fakeClient.lastSetWithExpiryTTL > 0 && fakeClient.lastSetWithExpiryTTL <= shortRemainingMs);
    }

    /**
     * When the Redis PSETEX call for a pendingReset throws, pendingReset must remain true
     * so the next sync tick retries. The flag must never be cleared on failure.
     */
    @Test
    public void pendingResetNotClearedWhenSetWithExpiryThrows() throws Exception {
        FailingOnWriteKeyValueStoreClient failingClient = new FailingOnWriteKeyValueStoreClient();
        DistributedCountAttributeAggregator aggregator = createAggregator("testKey9");
        setInstanceField(aggregator, "kvStoreClient", failingClient);

        long futureExpiry = System.currentTimeMillis() + 60_000L;
        setWindowExpiryThreadLocal(futureExpiry);
        try {
            aggregator.reset();
        } finally {
            removeWindowExpiryThreadLocal();
        }

        // First sync attempt: PSETEX throws → pendingReset stays true.
        aggregator.currentState();
        Assert.assertTrue("pendingReset must remain true when PSETEX fails",
                (boolean) getInstanceField(aggregator, "pendingReset"));

        // Second sync attempt: still failing → still true.
        aggregator.currentState();
        Assert.assertTrue("pendingReset must remain true on repeated PSETEX failures",
                (boolean) getInstanceField(aggregator, "pendingReset"));
    }

    /**
     * If reset() fires on another thread WHILE a syncWithKVStore() INCRBY for the OLD window's
     * delta is still in flight, the stale INCRBY result must not be allowed to overwrite the
     * fresh post-reset state. Uses a blocking fake client + latches to land the race
     * deterministically instead of relying on sleep-based timing.
     */
    @Test
    public void resetDuringInFlightIncrByDiscardsTheStaleResult() throws Exception {
        BlockingOnIncrByKeyValueStoreClient blockingClient = new BlockingOnIncrByKeyValueStoreClient();
        DistributedCountAttributeAggregator aggregator = createAggregator("testKey21");
        setInstanceField(aggregator, "kvStoreClient", blockingClient);

        long futureExpiry = System.currentTimeMillis() + 60_000L;
        setInstanceField(aggregator, "storedWindowExpiry", futureExpiry);
        setInstanceField(aggregator, "keyHasTTL", true);

        aggregator.processAdd((Object) null);
        aggregator.processAdd((Object) null); // unsyncedCounter=2 — this is the OLD window's delta.

        Thread syncThread = new Thread(aggregator::currentState);
        syncThread.start();
        Assert.assertTrue("Sync thread must have entered incrementBy within the timeout",
                blockingClient.enteredIncrBy.await(5, TimeUnit.SECONDS));

        // reset() fires from the "main" thread WHILE the old-window INCRBY is still blocked —
        // mirrors the window-boundary RESET broadcast racing an in-progress periodic sync tick.
        setWindowExpiryThreadLocal(futureExpiry + 60_000L);
        try {
            aggregator.reset();
        } finally {
            removeWindowExpiryThreadLocal();
        }

        blockingClient.releaseIncrBy.countDown();
        syncThread.join(5000);
        Assert.assertFalse("Sync thread must have completed", syncThread.isAlive());

        AtomicLong local = (AtomicLong) getInstanceField(aggregator, "localCounter");
        AtomicLong unsynced = (AtomicLong) getInstanceField(aggregator, "unsyncedCounter");
        Assert.assertEquals("localCounter must be 0 from reset(), not overwritten by the "
                + "stale in-flight INCRBY result", 0L, local.get());
        Assert.assertEquals("unsyncedCounter must be 0 (reset()'s own zeroing), not reinstated "
                + "with the old-window delta", 0L, unsynced.get());
        Assert.assertTrue("pendingReset must be true — reset() fired after this sync had "
                        + "already captured its (now-stale) delta snapshot, so a later sync "
                        + "tick is still needed to flush the PSETEX 0",
                (boolean) getInstanceField(aggregator, "pendingReset"));
    }

    /**
     * The same race as above, but for the zero-delta GET path: if reset() fires WHILE a
     * syncWithKVStore() GET is in flight (another TM's value for this key), the stale GET
     * result must not be allowed to overwrite the fresh post-reset localCounter=0.
     */
    @Test
    public void resetDuringInFlightGetDiscardsTheStaleResult() throws Exception {
        BlockingOnGetKeyValueStoreClient blockingClient = new BlockingOnGetKeyValueStoreClient();
        DistributedCountAttributeAggregator aggregator = createAggregator("testKey24");
        setInstanceField(aggregator, "kvStoreClient", blockingClient);

        long futureExpiry = System.currentTimeMillis() + 60_000L;
        setInstanceField(aggregator, "storedWindowExpiry", futureExpiry);
        setInstanceField(aggregator, "keyHasTTL", true);
        blockingClient.store.put("wso2_throttler:testKey24", "77"); // stale value from another TM

        // No local delta (unsyncedCounter=0) — currentState() takes the GET branch.
        Thread syncThread = new Thread(aggregator::currentState);
        syncThread.start();
        Assert.assertTrue("Sync thread must have entered get() within the timeout",
                blockingClient.enteredGet.await(5, TimeUnit.SECONDS));

        // reset() fires from the "main" thread WHILE the GET is still blocked.
        setWindowExpiryThreadLocal(futureExpiry + 60_000L);
        try {
            aggregator.reset();
        } finally {
            removeWindowExpiryThreadLocal();
        }

        blockingClient.releaseGet.countDown();
        syncThread.join(5000);
        Assert.assertFalse("Sync thread must have completed", syncThread.isAlive());

        AtomicLong local = (AtomicLong) getInstanceField(aggregator, "localCounter");
        Assert.assertEquals("localCounter must be 0 from reset(), not overwritten by the "
                + "stale in-flight GET result (77)", 0L, local.get());
    }

    /**
     * If reset() fires WHILE an old-window INCRBY is in flight AND that INCRBY goes on to
     * FAIL, the failed call's delta must NOT be restored into unsyncedCounter — reset() has
     * already superseded it, so restoring it would reinstate a stale amount on top of a
     * counter that is supposed to start the new window at 0.
     */
    @Test
    public void resetDuringInFlightFailingIncrByDoesNotRestoreStaleDelta() throws Exception {
        BlockingThenFailingOnIncrByKeyValueStoreClient client = new BlockingThenFailingOnIncrByKeyValueStoreClient();
        DistributedCountAttributeAggregator aggregator = createAggregator("testKey25");
        setInstanceField(aggregator, "kvStoreClient", client);

        long futureExpiry = System.currentTimeMillis() + 60_000L;
        setInstanceField(aggregator, "storedWindowExpiry", futureExpiry);
        setInstanceField(aggregator, "keyHasTTL", true);

        aggregator.processAdd((Object) null);
        aggregator.processAdd((Object) null); // unsyncedCounter=2 — the OLD window's delta.

        Thread syncThread = new Thread(aggregator::currentState);
        syncThread.start();
        Assert.assertTrue("Sync thread must have entered incrementBy within the timeout",
                client.enteredIncrBy.await(5, TimeUnit.SECONDS));

        setWindowExpiryThreadLocal(futureExpiry + 60_000L);
        try {
            aggregator.reset(); // unsyncedCounter -> 0, pendingReset=true
        } finally {
            removeWindowExpiryThreadLocal();
        }

        client.releaseIncrBy.countDown(); // incrementBy now throws
        syncThread.join(5000);
        Assert.assertFalse("Sync thread must have completed", syncThread.isAlive());

        AtomicLong unsynced = (AtomicLong) getInstanceField(aggregator, "unsyncedCounter");
        Assert.assertEquals("unsyncedCounter must stay 0 from reset() — the failed INCRBY's "
                + "old-window delta (2) must not be restored on top of it, since reset() "
                + "already superseded it", 0L, unsynced.get());
    }

    /**
     * When the Redis INCRBY call throws and no pendingReset is set, the captured delta
     * must be restored into unsyncedCounter so it is not silently lost.
     */
    @Test
    public void deltaRestoredToUnsyncedCounterOnIncrementByFailure() throws Exception {
        FailingOnIncrByKeyValueStoreClient failingClient = new FailingOnIncrByKeyValueStoreClient();
        DistributedCountAttributeAggregator aggregator = createAggregator("testKey10");
        setInstanceField(aggregator, "kvStoreClient", failingClient);

        long futureExpiry = System.currentTimeMillis() + 60_000L;
        setInstanceField(aggregator, "storedWindowExpiry", futureExpiry);
        setInstanceField(aggregator, "keyHasTTL", true);

        aggregator.processAdd((Object) null);
        aggregator.processAdd((Object) null);
        aggregator.processAdd((Object) null);

        AtomicLong unsynced = (AtomicLong) getInstanceField(aggregator, "unsyncedCounter");
        Assert.assertEquals("unsyncedCounter must be 3 before sync", 3L, unsynced.get());

        aggregator.currentState(); // INCRBY throws → delta restored

        Assert.assertEquals("unsyncedCounter must be restored to 3 after a failed INCRBY",
                3L, unsynced.get());
    }

    /**
     * When unsyncedCounter is 0 and the Redis key already exists (written by another TM),
     * syncWithKVStore() must pull the current value via GET and update localCounter.
     */
    @Test
    public void localCounterSyncedFromRedisGetWhenNoLocalDelta() throws Exception {
        DistributedCountAttributeAggregator aggregator = createAggregator("testKey11");
        setInstanceField(aggregator, "storedWindowExpiry", System.currentTimeMillis() + 60_000L);
        setInstanceField(aggregator, "keyHasTTL", true);
        fakeClient.store.put("wso2_throttler:testKey11", "50");

        // No processAdd() calls — unsyncedCounter=0 → GET path.
        aggregator.currentState();

        AtomicLong local = (AtomicLong) getInstanceField(aggregator, "localCounter");
        Assert.assertEquals("localCounter must be set to the Redis GET value from another TM",
                50L, local.get());
    }

    /**
     * When unsyncedCounter is 0 and the Redis key is absent, syncWithKVStore() must call
     * writeCounterValue("0") (PSETEX "0") to initialize the key and set localCounter to 0.
     */
    @Test
    public void localCounterSetToZeroAndRedisSeededWhenKeyAbsent() throws Exception {
        DistributedCountAttributeAggregator aggregator = createAggregator("testKey12");
        long futureExpiry = System.currentTimeMillis() + 60_000L;
        setInstanceField(aggregator, "storedWindowExpiry", futureExpiry);
        setInstanceField(aggregator, "keyHasTTL", true);
        // Store is empty — no key exists.

        aggregator.currentState(); // GET returns null → writeCounterValue("0") + localCounter=0

        AtomicLong local = (AtomicLong) getInstanceField(aggregator, "localCounter");
        Assert.assertEquals("localCounter must be 0 when Redis key is absent", 0L, local.get());
        Assert.assertEquals("PSETEX must be called once to seed Redis with 0 when key is absent",
                1, fakeClient.setWithExpiryCount.get());
        Assert.assertEquals("seeded Redis value must be 0", "0", fakeClient.lastSetWithExpiryValue);
    }

    /**
     * stop() must execute two syncWithKVStore() calls: first flushes pendingReset via PSETEX "0",
     * second pushes the accumulated new-window delta via INCRBY.
     */
    @Test
    public void stopFlushesResetThenDeltaInTwoSyncs() throws Exception {
        DistributedCountAttributeAggregator aggregator = createAggregator("testKey13");
        long futureExpiry = System.currentTimeMillis() + 60_000L;
        setWindowExpiryThreadLocal(futureExpiry);
        try {
            aggregator.reset(); // pendingReset=true, storedWindowExpiry=futureExpiry, unsyncedCounter=0
        } finally {
            removeWindowExpiryThreadLocal();
        }
        // Simulate two new-window increments arriving after the reset.
        setInstanceField(aggregator, "keyHasTTL", true);
        aggregator.processAdd((Object) null);
        aggregator.processAdd((Object) null); // unsyncedCounter=2

        aggregator.stop();

        Assert.assertEquals("stop() must issue exactly one PSETEX for the pendingReset",
                1, fakeClient.setWithExpiryCount.get());
        Assert.assertEquals("PSETEX value must be 0 to reset the window counter",
                "0", fakeClient.lastSetWithExpiryValue);
        Assert.assertEquals("stop() must issue exactly one INCRBY for the new-window delta",
                1, fakeClient.incrementByCount.get());
    }

    /**
     * stop()'s second sync (the new-window delta push) is only attempted "if (!pendingReset)"
     * after the first sync — i.e. only if the PSETEX reset-flush actually succeeded. When that
     * first flush FAILS, stop() must skip the delta sync entirely rather than push a delta on
     * top of a reset that never actually completed in Redis.
     */
    @Test
    public void stopSkipsTheDeltaSyncWhenTheResetFlushFails() throws Exception {
        FailingOnWriteKeyValueStoreClient failingClient = new FailingOnWriteKeyValueStoreClient();
        DistributedCountAttributeAggregator aggregator = createAggregator("testKey26");
        setInstanceField(aggregator, "kvStoreClient", failingClient);

        long futureExpiry = System.currentTimeMillis() + 60_000L;
        setWindowExpiryThreadLocal(futureExpiry);
        try {
            aggregator.reset(); // pendingReset=true; the PSETEX will fail via failingClient
        } finally {
            removeWindowExpiryThreadLocal();
        }
        setInstanceField(aggregator, "keyHasTTL", true);
        aggregator.processAdd((Object) null);
        aggregator.processAdd((Object) null); // unsyncedCounter=2, new-window delta

        aggregator.stop();

        Assert.assertTrue("pendingReset must remain true since the PSETEX flush failed",
                (boolean) getInstanceField(aggregator, "pendingReset"));
        Assert.assertEquals("stop() must skip the delta sync entirely when the reset flush "
                + "failed — incrementBy must never be called",
                0, failingClient.incrementByCount.get());
    }

    /**
     * restoreState() must load localCounter from Redis in distributed mode, ignoring the
     * stale single-node Siddhi snapshot value. Redis is the authoritative source.
     */
    @Test
    public void restoreStateUsesRedisValueInDistributedMode() throws Exception {
        DistributedCountAttributeAggregator aggregator = createAggregator("testKey14");
        fakeClient.store.put("wso2_throttler:testKey14", "100");

        Object[] state = new Object[]{new AbstractMap.SimpleEntry<String, Object>("Value", 5L)};
        aggregator.restoreState(state);

        AtomicLong local = (AtomicLong) getInstanceField(aggregator, "localCounter");
        Assert.assertEquals("localCounter must reflect the Redis value, not the Siddhi snapshot",
                100L, local.get());
    }

    /**
     * restoreState() must NOT clear an unflushed pendingReset. Dropping it would leave the
     * previous window's stale total sitting in Redis forever, since nothing would ever issue
     * the pending PSETEX "0" afterwards. The flag must survive the restore so the next sync
     * tick still flushes it.
     */
    @Test
    public void restoreStateDoesNotClearPendingReset() throws Exception {
        DistributedCountAttributeAggregator aggregator = createAggregator("testKey15");
        setInstanceField(aggregator, "pendingReset", true);

        Object[] state = new Object[]{new AbstractMap.SimpleEntry<String, Object>("Value", 3L)};
        aggregator.restoreState(state);

        Assert.assertTrue("restoreState() must preserve an unflushed pendingReset, not drop it",
                (boolean) getInstanceField(aggregator, "pendingReset"));
    }

    /**
     * In non-distributed mode restoreState() must use the Siddhi snapshot value because
     * there is no Redis to consult.
     */
    @Test
    public void restoreStateInNonDistributedModeUsesSiddhiSnapshot() throws Exception {
        // Temporarily disable distributed throttling for this test.
        setStaticField(DistributedCountAttributeAggregator.class, "distributedThrottlingEnabled", false);

        DistributedCountAttributeAggregator aggregator = createAggregator("testKey16");
        Object[] state = new Object[]{new AbstractMap.SimpleEntry<String, Object>("Value", 7L)};
        aggregator.restoreState(state);

        AtomicLong local = (AtomicLong) getInstanceField(aggregator, "localCounter");
        Assert.assertEquals("In non-distributed mode localCounter must come from the Siddhi snapshot",
                7L, local.get());
        Assert.assertEquals("No Redis calls must be made in non-distributed mode",
                0, fakeClient.setWithExpiryCount.get());
    }

    /**
     * When the Redis GET during restoreState() throws (as opposed to succeeding and simply
     * returning null for an absent key), counterToRestore must fall back to 0 rather than the
     * stale single-node Siddhi snapshot, and no PSETEX seed must be attempted — a thrown GET
     * is not the same as a confirmed-absent key.
     *
     * storedWindowExpiry must be set to a future value here, exactly like the sibling
     * "key is absent" test below — otherwise writeCounterValue()'s own no-op guard
     * (storedWindowExpiry == 0) would silently swallow a PSETEX call regardless of whether
     * this code incorrectly treated a thrown GET the same as a confirmed-absent key,
     * making the "no PSETEX" assertion pass even if that exact regression were present.
     */
    @Test
    public void restoreStateFallsBackToZeroWhenRedisGetThrows() throws Exception {
        FailingOnGetKeyValueStoreClient failingClient = new FailingOnGetKeyValueStoreClient();
        DistributedCountAttributeAggregator aggregator = createAggregator("testKey22");
        setInstanceField(aggregator, "kvStoreClient", failingClient);
        long futureExpiry = System.currentTimeMillis() + 60_000L;
        setInstanceField(aggregator, "storedWindowExpiry", futureExpiry);

        Object[] state = new Object[]{new AbstractMap.SimpleEntry<String, Object>("Value", 42L)};
        aggregator.restoreState(state);

        AtomicLong local = (AtomicLong) getInstanceField(aggregator, "localCounter");
        Assert.assertEquals("localCounter must fall back to 0 (not the stale Siddhi snapshot "
                + "of 42) when the Redis GET itself throws", 0L, local.get());
        Assert.assertEquals("A thrown GET must not be treated as a confirmed-absent key, so no "
                + "PSETEX seed should be attempted", 0, failingClient.setWithExpiryCount.get());
    }

    /**
     * When the Redis key is genuinely absent (GET returns null, no exception) and the window
     * boundary is already known, restoreState() must actually issue a PSETEX "0" to seed Redis
     * — not just skip touching localCounter. writeCounterValue()'s own no-op guard (tested
     * elsewhere via storedWindowExpiry=0) must not mask whether this call site is reached.
     */
    @Test
    public void restoreStateSeedsRedisWithZeroWhenKeyIsAbsent() throws Exception {
        DistributedCountAttributeAggregator aggregator = createAggregator("testKey23");
        long futureExpiry = System.currentTimeMillis() + 60_000L;
        setInstanceField(aggregator, "storedWindowExpiry", futureExpiry);
        // fakeClient's store has no entry for "wso2_throttler:testKey23" — key is absent.

        Object[] state = new Object[]{new AbstractMap.SimpleEntry<String, Object>("Value", 9L)};
        aggregator.restoreState(state);

        AtomicLong local = (AtomicLong) getInstanceField(aggregator, "localCounter");
        Assert.assertEquals("localCounter must be 0 (not the stale Siddhi snapshot of 9) when "
                + "the Redis key is absent", 0L, local.get());
        Assert.assertEquals("restoreState() must seed Redis with a real PSETEX 0 when the key "
                + "is absent and the window boundary is already known",
                1, fakeClient.setWithExpiryCount.get());
        Assert.assertEquals("0", fakeClient.lastSetWithExpiryValue);
    }

    /**
     * After a successful PSETEX in the pendingReset path, keyHasTTL must be set to true
     * so that subsequent INCRBY calls do not redundantly issue another PEXPIRE.
     */
    @Test
    public void keyHasTTLSetTrueAfterSuccessfulPSetexInResetPath() throws Exception {
        DistributedCountAttributeAggregator aggregator = createAggregator("testKey17");
        long futureExpiry = System.currentTimeMillis() + 60_000L;
        setWindowExpiryThreadLocal(futureExpiry);
        try {
            aggregator.reset();
        } finally {
            removeWindowExpiryThreadLocal();
        }

        Assert.assertFalse("keyHasTTL must be false before the first sync after reset()",
                (boolean) getInstanceField(aggregator, "keyHasTTL"));

        aggregator.currentState(); // pendingReset → PSETEX → keyHasTTL = true

        Assert.assertTrue("keyHasTTL must be true after a successful PSETEX in the pendingReset path",
                (boolean) getInstanceField(aggregator, "keyHasTTL"));
    }

    /**
     * processRemove() must decrement both localCounter and unsyncedCounter when distributed
     * throttling is enabled. The net delta must reflect the subtraction correctly.
     */
    @Test
    public void processRemoveDecrementsUnsyncedCounter() throws Exception {
        DistributedCountAttributeAggregator aggregator = createAggregator("testKey18");
        long futureExpiry = System.currentTimeMillis() + 60_000L;
        setInstanceField(aggregator, "storedWindowExpiry", futureExpiry);
        setInstanceField(aggregator, "keyHasTTL", true);

        aggregator.processAdd((Object) null);
        aggregator.processAdd((Object) null);
        aggregator.processAdd((Object) null); // localCounter=3, unsyncedCounter=3

        aggregator.processRemove((Object) null); // localCounter=2, unsyncedCounter=2

        AtomicLong local = (AtomicLong) getInstanceField(aggregator, "localCounter");
        AtomicLong unsynced = (AtomicLong) getInstanceField(aggregator, "unsyncedCounter");
        Assert.assertEquals("localCounter must be 2 after three adds and one remove", 2L, local.get());
        Assert.assertEquals("unsyncedCounter must be 2 so only the net delta is pushed to Redis",
                2L, unsynced.get());
    }

    /**
     * When reset() is called without a ThreadLocal value (no current window boundary known),
     * storedWindowExpiry must remain unchanged so a stale or zero value is not introduced.
     */
    @Test
    public void resetWithNullThreadLocalDoesNotChangeStoredWindowExpiry() throws Exception {
        DistributedCountAttributeAggregator aggregator = createAggregator("testKey19");
        long knownExpiry = System.currentTimeMillis() + 60_000L;
        setInstanceField(aggregator, "storedWindowExpiry", knownExpiry);

        // ThreadLocal is null (not set) — reset() must leave storedWindowExpiry alone.
        aggregator.reset();

        long stored = (long) getInstanceField(aggregator, "storedWindowExpiry");
        Assert.assertEquals("storedWindowExpiry must not change when ThreadLocal is null during reset()",
                knownExpiry, stored);
        Assert.assertTrue("pendingReset must still be set even when ThreadLocal is absent",
                (boolean) getInstanceField(aggregator, "pendingReset"));
    }

    /**
     * The TTL passed to PEXPIRE after INCRBY must closely match the remaining window time
     * (storedWindowExpiry - currentTimeMillis). Verifies the TTL is wired correctly, not
     * hard-coded or calculated from a stale snapshot.
     */
    @Test
    public void pexpireTtlMatchesRemainingWindowTime() throws Exception {
        DistributedCountAttributeAggregator aggregator = createAggregator("testKey20");
        long windowDurationMs = 30_000L;
        long futureExpiry = System.currentTimeMillis() + windowDurationMs;
        setInstanceField(aggregator, "storedWindowExpiry", futureExpiry);
        setInstanceField(aggregator, "keyHasTTL", false); // forces PEXPIRE after INCRBY

        aggregator.processAdd((Object) null);
        aggregator.currentState(); // INCRBY + PEXPIRE

        Assert.assertEquals("PEXPIRE must be called exactly once after the first INCRBY in a new window",
                1, fakeClient.expireMillisCount.get());
        long ttlApplied = fakeClient.lastExpireMillisTTL;
        Assert.assertTrue("PEXPIRE TTL must be positive (remaining window time)",
                ttlApplied > 0);
        Assert.assertTrue("PEXPIRE TTL must not exceed the original window duration",
                ttlApplied <= windowDurationMs);
        Assert.assertTrue("PEXPIRE TTL must be within 2 seconds of the remaining window time",
                ttlApplied >= windowDurationMs - 2_000L);
    }

    // -----------------------------------------------------------------------
    // Multi-key, full-Siddhi-query tests.
    //
    // Everything above drives a single, manually-constructed aggregator instance directly
    // via reflection — the right harness for pinning down syncWithKVStore/reset/restoreState
    // mechanics precisely. The tests below instead run a real #throttler:timeBatch(...)
    // group-by query through SiddhiManager, because the thing under test here is how
    // MULTIPLE per-key aggregator clones (one per distinct application/API resource
    // sharing a policy, the normal production shape) behave together — something a single
    // manually-built instance can't exercise. setUp()/tearDown() above already wire
    // distributedThrottlingEnabled=true and a shared fakeClient; each test below only
    // overrides schedulerStarted=false so the real periodic sync scheduler actually runs
    // (setUp() keeps it artificially off for the single-instance tests, which drive syncs
    // manually instead).
    // -----------------------------------------------------------------------

    /**
     * The RESET broadcast (GroupByAggregationAttributeExecutor's special handling of
     * StreamEvent.Type.RESET, which resets every key in its aggregatorMap regardless of
     * which one the boundary placeholder represents) must zero EVERY key's Redis-backed
     * value on a window rollover, not just one — confirmed here for three keys spanning
     * two different applications and two resources under the same application.
     */
    @Test
    public void resetBroadcastZeroesEveryKeyInRedisBackedStore_multipleAppsAndResources() throws Exception {
        setStaticField(DistributedCountAttributeAggregator.class, "schedulerStarted", false);

        SiddhiManager siddhiManager = new SiddhiManager();
        String query = "" +
                "define stream cseEventStream (throttleKey string, messageID string);" +
                "@info(name = 'q1') " +
                "from cseEventStream#throttler:timeBatch(3000) " +
                "select throttleKey, throttler:distributedCount(messageID) as cnt, expiryTimeStamp " +
                "group by throttleKey " +
                "insert all events into outputStream;";

        ExecutionPlanRuntime runtime = siddhiManager.createExecutionPlanRuntime(query);
        runtime.start();
        InputHandler in = runtime.getInputHandler("cseEventStream");

        String[] keys = {"App1_ResourceA", "App2_ResourceA", "App2_ResourceB"};
        for (String key : keys) {
            for (int i = 1; i <= 5; i++) {
                in.send(new Object[]{key, key + "-msg" + i});
            }
        }
        Thread.sleep(3200); // cross the 3s window boundary

        // The Redis-backed flush is asynchronous (a background scheduler tick, independent
        // of the calling thread), so poll for the expected end-state rather than relying on
        // a single fixed sleep. Siddhi's own group-by key generator also appends its own
        // delimiter (observed: "::") to the constructed key, so match by prefix.
        long deadline = System.currentTimeMillis() + 8000;
        boolean allKeysReset = false;
        while (System.currentTimeMillis() < deadline) {
            allKeysReset = true;
            for (String key : keys) {
                String expectedPrefix = "wso2_throttler:" + key;
                boolean found = fakeClient.store.entrySet().stream()
                        .anyMatch(e -> e.getKey().startsWith(expectedPrefix) && "0".equals(e.getValue()));
                if (!found) {
                    allKeysReset = false;
                    break;
                }
            }
            if (allKeysReset) {
                break;
            }
            Thread.sleep(100);
        }
        runtime.shutdown();

        Assert.assertTrue("Expected every key to be reset to 0 in the Redis-backed store "
                + "within the poll window — final store contents: " + fakeClient.store, allKeysReset);
    }

    /**
     * A Redis write failure for ONE key during the broadcast reset must not prevent a
     * DIFFERENT, healthy key's reset from succeeding — each per-key aggregator clone's
     * pendingReset/retry state is independent.
     */
    @Test
    public void redisFailureOnOneKeyDoesNotPreventAHealthyKeyFromResetting() throws Exception {
        setStaticField(DistributedCountAttributeAggregator.class, "schedulerStarted", false);
        PartiallyFailingKeyValueStoreClient failingClient = new PartiallyFailingKeyValueStoreClient("Failing");
        setStaticField(KeyValueStoreManager.class, "clientInstance", failingClient);

        SiddhiManager siddhiManager = new SiddhiManager();
        String query = "" +
                "define stream cseEventStream (throttleKey string, messageID string);" +
                "@info(name = 'q1') " +
                "from cseEventStream#throttler:timeBatch(3000) " +
                "select throttleKey, throttler:distributedCount(messageID) as cnt, expiryTimeStamp " +
                "group by throttleKey " +
                "insert all events into outputStream;";

        ExecutionPlanRuntime runtime = siddhiManager.createExecutionPlanRuntime(query);
        runtime.start();
        InputHandler in = runtime.getInputHandler("cseEventStream");

        for (int i = 0; i < 5; i++) {
            in.send(new Object[]{"Failing_App", "f-" + i});
        }
        for (int i = 0; i < 5; i++) {
            in.send(new Object[]{"Healthy_App", "h-" + i});
        }
        Thread.sleep(3200);

        long deadline = System.currentTimeMillis() + 8000;
        boolean healthyReset = false;
        while (System.currentTimeMillis() < deadline) {
            healthyReset = failingClient.store.entrySet().stream()
                    .anyMatch(e -> e.getKey().contains("Healthy_App") && "0".equals(e.getValue()));
            if (healthyReset) {
                break;
            }
            Thread.sleep(100);
        }
        runtime.shutdown();

        Assert.assertTrue("Healthy_App should have reset to 0 despite Failing_App's writes "
                + "throwing — final store: " + failingClient.store, healthyReset);
        Assert.assertTrue("The failing key's writes should have been attempted (and failed) "
                + "at least once, confirming the failure path was actually exercised",
                failingClient.failureCount > 0);
        boolean failingKeyFalselyShowsReset = failingClient.store.entrySet().stream()
                .anyMatch(e -> e.getKey().contains("Failing_App") && "0".equals(e.getValue()));
        Assert.assertFalse("Failing_App's key must NOT show 0, since every write for it "
                + "was made to fail", failingKeyFalselyShowsReset);
    }

    /**
     * The admin-triggered reset flag for distributedCount (app.xml's actual form:
     * throttler:distributedCount(messageID, resetFlag)) — confirms resetting one key leaves
     * other apps/resources untouched, in BOTH the query output and the Redis-backed store.
     */
    @Test
    public void adminFlagResetForDistributedCountOnlyAffectsTargetedKey() throws Exception {
        setStaticField(DistributedCountAttributeAggregator.class, "schedulerStarted", false);

        SiddhiManager siddhiManager = new SiddhiManager();
        String query = "" +
                "define stream cseEventStream (throttleKey string, messageID string, reset bool);" +
                "@info(name = 'q1') " +
                "from cseEventStream#throttler:timeBatch(10000) " +
                "select throttleKey, throttler:distributedCount(messageID, reset) as cnt, expiryTimeStamp " +
                "group by throttleKey " +
                "insert all events into outputStream;";

        ExecutionPlanRuntime runtime = siddhiManager.createExecutionPlanRuntime(query);
        Map<String, Long> lastCountPerKey = new ConcurrentHashMap<>();
        runtime.addCallback("q1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                if (inEvents != null) {
                    for (Event e : inEvents) {
                        lastCountPerKey.put((String) e.getData(0), (Long) e.getData(1));
                    }
                }
            }
        });
        try {
            runtime.start();
            InputHandler in = runtime.getInputHandler("cseEventStream");

            String[] keys = {"App1_ResourceA", "App1_ResourceB", "App2_ResourceA"};
            for (String key : keys) {
                for (int i = 0; i < 3; i++) {
                    in.send(new Object[]{key, key + "-" + i, false});
                }
            }
            Assert.assertEquals(Long.valueOf(3L), lastCountPerKey.get("App1_ResourceA"));
            Assert.assertEquals(Long.valueOf(3L), lastCountPerKey.get("App1_ResourceB"));
            Assert.assertEquals(Long.valueOf(3L), lastCountPerKey.get("App2_ResourceA"));

            // Admin resets ONLY App1_ResourceA via the 2-arg distributedCount reset flag.
            in.send(new Object[]{"App1_ResourceA", "admin-reset", true});
            in.send(new Object[]{"App1_ResourceA", "post-reset", false});

            // Poll for the expected steady-state — checking untouched keys for merely "not 0"
            // is unreliable, since a key's very first write is itself a "0" seed
            // (initializeFromKVStore) before its own accumulated delta syncs up.
            long deadline = System.currentTimeMillis() + 5000;
            String targetValue = null;
            String otherAValue = null;
            String otherBValue = null;
            while (System.currentTimeMillis() < deadline) {
                targetValue = redisValueContaining("App1_ResourceA");
                otherAValue = redisValueContaining("App1_ResourceB");
                otherBValue = redisValueContaining("App2_ResourceA");
                if ("1".equals(targetValue) && "3".equals(otherAValue) && "3".equals(otherBValue)) {
                    break;
                }
                Thread.sleep(50);
            }

            Assert.assertEquals("Targeted key should read 1 (reset then one new increment) in query output",
                    Long.valueOf(1L), lastCountPerKey.get("App1_ResourceA"));
            Assert.assertEquals("A different resource under the same app must be untouched",
                    Long.valueOf(3L), lastCountPerKey.get("App1_ResourceB"));
            Assert.assertEquals("A different app entirely must be untouched",
                    Long.valueOf(3L), lastCountPerKey.get("App2_ResourceA"));
            Assert.assertEquals("Redis-backed value for the targeted key should reflect the "
                    + "reset (0) plus one new increment (1)", "1", targetValue);
            Assert.assertEquals("Redis-backed value for the untouched sibling resource should "
                    + "reach its true accumulated value, proving it was never reset", "3", otherAValue);
            Assert.assertEquals("Redis-backed value for the untouched other app should reach "
                    + "its true accumulated value, proving it was never reset", "3", otherBValue);
        } finally {
            runtime.shutdown();
        }
    }

    private String redisValueContaining(String keySubstring) {
        return fakeClient.store.entrySet().stream()
                .filter(e -> e.getKey().contains(keySubstring))
                .map(Map.Entry::getValue)
                .findFirst().orElse(null);
    }

    /**
     * distributedCount() with distributed throttling explicitly disabled (the default when
     * Redis isn't configured at all) must behave exactly like plain count() — a correct
     * pure-local counter — across multiple keys and multiple window rollovers.
     */
    @Test
    public void distributedCountWithDistributionDisabledBehavesAsLocalCounter_multipleKeys() throws Exception {
        setStaticField(DistributedCountAttributeAggregator.class, "distributedThrottlingEnabled", false);

        SiddhiManager siddhiManager = new SiddhiManager();
        String query = "" +
                "define stream cseEventStream (throttleKey string, messageID string);" +
                "@info(name = 'q1') " +
                "from cseEventStream#throttler:timeBatch(3000) " +
                "select throttleKey, throttler:distributedCount(messageID) as cnt, expiryTimeStamp " +
                "group by throttleKey " +
                "insert all events into outputStream;";

        ExecutionPlanRuntime runtime = siddhiManager.createExecutionPlanRuntime(query);
        Map<String, Long> lastCountPerKey = new ConcurrentHashMap<>();
        runtime.addCallback("q1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                if (inEvents != null) {
                    for (Event e : inEvents) {
                        lastCountPerKey.put((String) e.getData(0), (Long) e.getData(1));
                    }
                }
            }
        });
        runtime.start();
        InputHandler in = runtime.getInputHandler("cseEventStream");

        String[] keys = {"App1", "App2", "App3"};
        for (int k = 0; k < keys.length; k++) {
            for (int i = 0; i <= k + 3; i++) {
                in.send(new Object[]{keys[k], keys[k] + "-w1-" + i});
            }
        }
        Assert.assertEquals(Long.valueOf(4L), lastCountPerKey.get("App1"));
        Assert.assertEquals(Long.valueOf(5L), lastCountPerKey.get("App2"));
        Assert.assertEquals(Long.valueOf(6L), lastCountPerKey.get("App3"));

        Thread.sleep(3500); // cross the boundary

        for (String key : keys) {
            in.send(new Object[]{key, key + "-w2"});
        }
        runtime.shutdown();

        Assert.assertEquals("App1 should reset correctly in local-only mode", Long.valueOf(1L),
                lastCountPerKey.get("App1"));
        Assert.assertEquals("App2 should reset correctly in local-only mode", Long.valueOf(1L),
                lastCountPerKey.get("App2"));
        Assert.assertEquals("App3 should reset correctly in local-only mode", Long.valueOf(1L),
                lastCountPerKey.get("App3"));
    }

    /**
     * shutdownScheduler()'s masterSyncTask.cancel() must actually cancel the scheduled
     * periodic task (not just null out the field) — confirmed via the returned
     * ScheduledFuture's own cancellation state, and that both executor fields are nulled.
     */
    @Test
    public void shutdownSchedulerCancelsTheMasterSyncTask() throws Exception {
        setStaticField(DistributedCountAttributeAggregator.class, "schedulerStarted", false);

        SiddhiManager siddhiManager = new SiddhiManager();
        String query = "" +
                "define stream cseEventStream (throttleKey string, messageID string);" +
                "@info(name = 'q1') " +
                "from cseEventStream#throttler:timeBatch(3000) " +
                "select throttleKey, throttler:distributedCount(messageID) as cnt, expiryTimeStamp " +
                "group by throttleKey " +
                "insert all events into outputStream;";

        ExecutionPlanRuntime runtime = siddhiManager.createExecutionPlanRuntime(query);
        try {
            runtime.start();
            InputHandler in = runtime.getInputHandler("cseEventStream");
            in.send(new Object[]{"AppX", "msg1"});

            long deadline = System.currentTimeMillis() + 2000;
            boolean started = false;
            while (System.currentTimeMillis() < deadline) {
                started = (boolean) getStaticField(DistributedCountAttributeAggregator.class, "schedulerStarted");
                if (started) {
                    break;
                }
                Thread.sleep(20);
            }
            Assert.assertTrue("Scheduler should have started after the first distributed-mode event", started);

            ScheduledFuture<?> taskBeforeShutdown =
                    (ScheduledFuture<?>) getStaticField(DistributedCountAttributeAggregator.class, "masterSyncTask");
            Assert.assertTrue("masterSyncTask should be a live, non-cancelled task before shutdown",
                    taskBeforeShutdown != null && !taskBeforeShutdown.isCancelled());

            DistributedCountAttributeAggregator.shutdownScheduler();

            Assert.assertTrue("masterSyncTask should be cancelled after shutdownScheduler()",
                    taskBeforeShutdown.isCancelled());
            Assert.assertNull("masterScheduler field should be nulled out after shutdown",
                    getStaticField(DistributedCountAttributeAggregator.class, "masterScheduler"));
            Assert.assertNull("kvStoreSyncScheduler field should be nulled out after shutdown",
                    (ScheduledExecutorService) getStaticField(DistributedCountAttributeAggregator.class,
                            "kvStoreSyncScheduler"));
        } finally {
            runtime.shutdown();
        }
    }

    // -----------------------------------------------------------------------
    // windowExpiryThreadLocal correctness.
    //
    // Every test above that touches storedWindowExpiry sets windowExpiryThreadLocal manually
    // via the test's own reflection helper, simulating what ThrottleStreamProcessor does. The
    // tests below instead run real queries and inspect the ACTUAL live aggregator instance(s)
    // (via ACTIVE_AGGREGATORS) to confirm ThrottleStreamProcessor itself sets the ThreadLocal
    // to the correct value during real processing, and that the aggregator side genuinely
    // consumes it correctly — not just that the simulated wiring in earlier tests behaves.
    // -----------------------------------------------------------------------

    /**
     * ThrottleStreamProcessor propagates the window boundary to attribute aggregators through
     * TWO independent paths: the "expiryTimeStamp" column (via complexEventPopulater, visible
     * to the SELECT clause) and the windowExpiryThreadLocal (visible only to aggregators like
     * this one). Both are set from the same expireEventTime in the same process() call, so they
     * must always agree. Cross-checking them against each other — rather than each in
     * isolation — is what actually proves the ThreadLocal carries the real window boundary and
     * not some unrelated or stale value.
     */
    @Test
    public void storedWindowExpiryMatchesQueryOutputExpiryTimeStampColumn() throws Exception {
        setStaticField(DistributedCountAttributeAggregator.class, "schedulerStarted", false);

        SiddhiManager siddhiManager = new SiddhiManager();
        String query = "" +
                "define stream cseEventStream (throttleKey string, messageID string);" +
                "@info(name = 'q1') " +
                "from cseEventStream#throttler:timeBatch(3000) " +
                "select throttleKey, throttler:distributedCount(messageID) as cnt, expiryTimeStamp " +
                "group by throttleKey " +
                "insert all events into outputStream;";

        ExecutionPlanRuntime runtime = siddhiManager.createExecutionPlanRuntime(query);
        AtomicLong lastExpiryTimeStampColumn = new AtomicLong(-1L);
        runtime.addCallback("q1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                if (inEvents != null) {
                    for (Event e : inEvents) {
                        lastExpiryTimeStampColumn.set((Long) e.getData(2));
                    }
                }
            }
        });
        try {
            runtime.start();
            InputHandler in = runtime.getInputHandler("cseEventStream");
            in.send(new Object[]{"App1", "m1"});

            Assert.assertTrue("expiryTimeStamp column must have been populated to a real future "
                    + "timestamp", lastExpiryTimeStampColumn.get() > System.currentTimeMillis());

            @SuppressWarnings("unchecked")
            ConcurrentHashMap<String, DistributedCountAttributeAggregator> active =
                    (ConcurrentHashMap<String, DistributedCountAttributeAggregator>)
                            getStaticField(DistributedCountAttributeAggregator.class, "ACTIVE_AGGREGATORS");
            DistributedCountAttributeAggregator liveAggregator = active.values().stream().findFirst()
                    .orElseThrow(() -> new AssertionError("Expected exactly one live aggregator for App1"));
            long storedWindowExpiry = (long) getInstanceField(liveAggregator, "storedWindowExpiry");

            Assert.assertEquals("The window expiry the aggregator captured internally via the "
                    + "ThreadLocal must exactly match the expiryTimeStamp column the SAME window "
                    + "populates for the query's own output", lastExpiryTimeStampColumn.get(), storedWindowExpiry);
        } finally {
            runtime.shutdown();
        }
    }

    /**
     * windowExpiryThreadLocal is set ONCE per process() invocation and the whole streamEventChunk
     * (every key's event in this batch) is processed while it holds that single value. Confirms
     * multiple DIFFERENT group-by keys sharing the same window all capture the IDENTICAL expiry
     * — not per-key-different by some indexing or ordering mistake.
     */
    @Test
    public void allKeysInSameWindowSeeIdenticalWindowExpiryThroughThreadLocal() throws Exception {
        setStaticField(DistributedCountAttributeAggregator.class, "schedulerStarted", false);

        SiddhiManager siddhiManager = new SiddhiManager();
        String query = "" +
                "define stream cseEventStream (throttleKey string, messageID string);" +
                "@info(name = 'q1') " +
                "from cseEventStream#throttler:timeBatch(3000) " +
                "select throttleKey, throttler:distributedCount(messageID) as cnt, expiryTimeStamp " +
                "group by throttleKey " +
                "insert all events into outputStream;";

        ExecutionPlanRuntime runtime = siddhiManager.createExecutionPlanRuntime(query);
        try {
            runtime.start();
            InputHandler in = runtime.getInputHandler("cseEventStream");

            String[] keys = {"App1_ResourceA", "App2_ResourceA", "App2_ResourceB"};
            for (String key : keys) {
                in.send(new Object[]{key, key + "-m1"});
            }

            @SuppressWarnings("unchecked")
            ConcurrentHashMap<String, DistributedCountAttributeAggregator> active =
                    (ConcurrentHashMap<String, DistributedCountAttributeAggregator>)
                            getStaticField(DistributedCountAttributeAggregator.class, "ACTIVE_AGGREGATORS");
            Assert.assertEquals("Expected exactly 3 live aggregator clones, one per key", 3, active.size());

            long[] expiries = new long[3];
            int i = 0;
            for (DistributedCountAttributeAggregator agg : active.values()) {
                expiries[i++] = (long) getInstanceField(agg, "storedWindowExpiry");
            }

            Assert.assertTrue("The shared window expiry must be a real future timestamp, not the "
                    + "0L default", expiries[0] > System.currentTimeMillis());
            Assert.assertEquals("All keys sharing the same window must have captured the identical "
                    + "expiry via the ThreadLocal", expiries[0], expiries[1]);
            Assert.assertEquals("All keys sharing the same window must have captured the identical "
                    + "expiry via the ThreadLocal", expiries[0], expiries[2]);
        } finally {
            runtime.shutdown();
        }
    }

    /**
     * Confirms windowExpiryThreadLocal is refreshed to the NEW window's boundary on every
     * rollover — not stuck on the first window's value forever, and not merely incrementing by
     * accident. Checked directly against the live aggregator's storedWindowExpiry across three
     * consecutive windows.
     */
    @Test
    public void storedWindowExpiryAdvancesToTheNewWindowOnEachRollover() throws Exception {
        setStaticField(DistributedCountAttributeAggregator.class, "schedulerStarted", false);

        SiddhiManager siddhiManager = new SiddhiManager();
        String query = "" +
                "define stream cseEventStream (throttleKey string, messageID string);" +
                "@info(name = 'q1') " +
                "from cseEventStream#throttler:timeBatch(3000) " +
                "select throttleKey, throttler:distributedCount(messageID) as cnt, expiryTimeStamp " +
                "group by throttleKey " +
                "insert all events into outputStream;";

        ExecutionPlanRuntime runtime = siddhiManager.createExecutionPlanRuntime(query);
        runtime.start();
        InputHandler in = runtime.getInputHandler("cseEventStream");

        in.send(new Object[]{"App1", "w1-m1"});

        @SuppressWarnings("unchecked")
        ConcurrentHashMap<String, DistributedCountAttributeAggregator> active =
                (ConcurrentHashMap<String, DistributedCountAttributeAggregator>)
                        getStaticField(DistributedCountAttributeAggregator.class, "ACTIVE_AGGREGATORS");
        DistributedCountAttributeAggregator liveAggregator = active.values().stream().findFirst()
                .orElseThrow(() -> new AssertionError("Expected a live aggregator for App1"));
        long window1Expiry = (long) getInstanceField(liveAggregator, "storedWindowExpiry");

        Thread.sleep(3500); // cross boundary 1 -> 2

        in.send(new Object[]{"App1", "w2-m1"});
        long window2Expiry = (long) getInstanceField(liveAggregator, "storedWindowExpiry");

        Thread.sleep(3500); // cross boundary 2 -> 3

        in.send(new Object[]{"App1", "w3-m1"});
        long window3Expiry = (long) getInstanceField(liveAggregator, "storedWindowExpiry");

        runtime.shutdown();

        Assert.assertTrue("Window 2's expiry must be strictly later than window 1's — "
                        + "storedWindowExpiry must not be stuck on the first window's value",
                window2Expiry > window1Expiry);
        Assert.assertTrue("Window 3's expiry must be strictly later than window 2's",
                window3Expiry > window2Expiry);
        Assert.assertTrue("Consecutive window expiries should be spaced ~3000ms apart — got: "
                        + (window2Expiry - window1Expiry),
                Math.abs((window2Expiry - window1Expiry) - 3000L) <= 1000L);
        Assert.assertTrue("Consecutive window expiries should be spaced ~3000ms apart — got: "
                        + (window3Expiry - window2Expiry),
                Math.abs((window3Expiry - window2Expiry) - 3000L) <= 1000L);
    }

    /**
     * Confirms each consecutive window-boundary reset stamps a FRESH PSETEX TTL matching ITS
     * OWN window's remaining time — not a stale value carried over from an earlier rollover.
     * Runs through a real multi-rollover query rather than the single-instance reflection
     * harness used by the other writeCounterValue/TTL tests above.
     */
    @Test
    public void consecutiveResetsEachGetFreshTTLMatchingTheirOwnWindow() throws Exception {
        setStaticField(DistributedCountAttributeAggregator.class, "schedulerStarted", false);

        SiddhiManager siddhiManager = new SiddhiManager();
        String query = "" +
                "define stream cseEventStream (throttleKey string, messageID string);" +
                "@info(name = 'q1') " +
                "from cseEventStream#throttler:timeBatch(3000) " +
                "select throttleKey, throttler:distributedCount(messageID) as cnt, expiryTimeStamp " +
                "group by throttleKey " +
                "insert all events into outputStream;";

        ExecutionPlanRuntime runtime = siddhiManager.createExecutionPlanRuntime(query);
        try {
            runtime.start();
            InputHandler in = runtime.getInputHandler("cseEventStream");

            in.send(new Object[]{"App1", "w1-m1"});
            Thread.sleep(3200); // cross boundary 1 -> RESET broadcast fires

            long deadline1 = System.currentTimeMillis() + 5000;
            long ttlAfterFirstReset = -1;
            while (System.currentTimeMillis() < deadline1) {
                if (fakeClient.setWithExpiryCount.get() >= 1) {
                    ttlAfterFirstReset = fakeClient.lastSetWithExpiryTTL;
                    break;
                }
                Thread.sleep(50);
            }
            Assert.assertTrue("Expected the first PSETEX reset to have fired", ttlAfterFirstReset > 0);
            Assert.assertTrue("First reset's TTL should be close to the 3000ms window duration — got: "
                    + ttlAfterFirstReset, ttlAfterFirstReset > 0 && ttlAfterFirstReset <= 3000L);

            int countBeforeSecondReset = fakeClient.setWithExpiryCount.get();
            in.send(new Object[]{"App1", "w2-m1"});
            Thread.sleep(3200); // cross boundary 2 -> a SECOND, independent RESET broadcast

            long deadline2 = System.currentTimeMillis() + 5000;
            long ttlAfterSecondReset = -1;
            while (System.currentTimeMillis() < deadline2) {
                if (fakeClient.setWithExpiryCount.get() > countBeforeSecondReset) {
                    ttlAfterSecondReset = fakeClient.lastSetWithExpiryTTL;
                    break;
                }
                Thread.sleep(50);
            }

            Assert.assertTrue("Expected the SECOND PSETEX reset to have fired independently",
                    ttlAfterSecondReset > 0);
            Assert.assertTrue("Second reset's TTL should ALSO be close to the fresh 3000ms window, "
                    + "not a stale carried-over value — got: " + ttlAfterSecondReset,
                    ttlAfterSecondReset > 0 && ttlAfterSecondReset <= 3000L);
        } finally {
            runtime.shutdown();
        }
    }
}
