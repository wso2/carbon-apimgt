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

import java.lang.reflect.Field;
import java.util.AbstractMap;
import java.util.concurrent.ConcurrentHashMap;
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

        // Second sync: keyHasTTL is now true — no additional PEXPIRE.
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
     * syncWithKVStore() must clear pendingReset without issuing a PSETEX.
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
        Assert.assertFalse("pendingReset must still be cleared even when writeCounterValue is a no-op",
                (boolean) getInstanceField(aggregator, "pendingReset"));
    }

    /**
     * writeCounterValue() must be a no-op when the window has already expired
     * (storedWindowExpiry is in the past). No PSETEX must be sent in this case.
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
        Assert.assertFalse("pendingReset must be cleared regardless of whether PSETEX was sent",
                (boolean) getInstanceField(aggregator, "pendingReset"));
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
     * restoreState() must clear pendingReset inside the lock so that the sync thread does not
     * issue a stale PSETEX "0" after state has been freshly restored from Redis.
     */
    @Test
    public void restoreStateClearsPendingReset() throws Exception {
        DistributedCountAttributeAggregator aggregator = createAggregator("testKey15");
        setInstanceField(aggregator, "pendingReset", true);

        Object[] state = new Object[]{new AbstractMap.SimpleEntry<String, Object>("Value", 3L)};
        aggregator.restoreState(state);

        Assert.assertFalse("restoreState() must clear pendingReset to prevent stale PSETEX retries",
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
}
