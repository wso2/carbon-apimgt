/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.carbon.apimgt.throttling.siddhi.extension.util.kvstore.KeyValueStoreOperation;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Unit tests for the batched Redis sync of {@link DistributedCountAttributeAggregator}: one MGET for the keys
 * without new requests and one pipeline for the writes, per tick.
 *
 * Test strategy (same as {@link DistributedCountAggregatorTest}): no Redis and no mocking framework. A fake
 * in-memory {@link KeyValueStoreClient} that supports batch operations is injected, and one sync tick is run by
 * calling the private static {@code runSyncTick()} through reflection. The fake can hold a batch "in flight"
 * (blocked on a latch) so that a window reset can be made to happen between Collect and Apply deterministically.
 */
public class DistributedCountAggregatorBatchSyncTest {

    private static final long TEST_TIMEOUT_MS = 20000L;

    // -----------------------------------------------------------------------
    // Fake key-value store client with batch support
    // -----------------------------------------------------------------------

    /**
     * In-memory client that supports MGET and pipelines. Records every batch it receives, can block a batch until
     * released (to simulate a call in flight) and can fail the next read or write batch.
     */
    private static class FakeBatchClient implements KeyValueStoreClient {

        final ConcurrentHashMap<String, String> store = new ConcurrentHashMap<>();
        final ConcurrentHashMap<String, Long> ttls = new ConcurrentHashMap<>();
        final List<List<String>> mgetCalls = Collections.synchronizedList(new ArrayList<List<String>>());
        final List<List<KeyValueStoreOperation>> pipelineCalls =
                Collections.synchronizedList(new ArrayList<List<KeyValueStoreOperation>>());
        final AtomicInteger singleKeyCalls = new AtomicInteger();
        volatile boolean blockReads;
        volatile boolean blockWrites;
        final CountDownLatch entered = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);
        volatile RuntimeException failNextRead;
        volatile RuntimeException failNextWrite;
        volatile boolean dropLastReply;

        @Override
        public boolean supportsBatchOperations() {
            return true;
        }

        private void maybeBlock(boolean block) {
            if (block) {
                entered.countDown();
                try {
                    release.await(10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        @Override
        public List<String> getMultiple(List<String> keys) {
            mgetCalls.add(new ArrayList<>(keys));
            maybeBlock(blockReads);
            RuntimeException failure = failNextRead;
            if (failure != null) {
                failNextRead = null;
                throw failure;
            }
            List<String> values = new ArrayList<>();
            for (String key : keys) {
                values.add(store.get(key));
            }
            return values;
        }

        @Override
        public List<Object> executeBatch(List<KeyValueStoreOperation> operations) {
            pipelineCalls.add(new ArrayList<>(operations));
            maybeBlock(blockWrites);
            RuntimeException failure = failNextWrite;
            if (failure != null) {
                failNextWrite = null;
                throw failure;
            }
            List<Object> replies = new ArrayList<>();
            for (KeyValueStoreOperation op : operations) {
                try {
                    switch (op.getType()) {
                        case INCRBY:
                            replies.add(add(op.getKey(), op.getAmount()));
                            break;
                        case DECRBY:
                            replies.add(add(op.getKey(), -op.getAmount()));
                            break;
                        case PEXPIRE:
                            ttls.put(op.getKey(), op.getTtlMillis());
                            replies.add(1L);
                            break;
                        case PSETEX:
                            store.put(op.getKey(), op.getValue());
                            ttls.put(op.getKey(), op.getTtlMillis());
                            replies.add("OK");
                            break;
                        case SET_NX_PX:
                            if (store.putIfAbsent(op.getKey(), op.getValue()) == null) {
                                ttls.put(op.getKey(), op.getTtlMillis());
                                replies.add("OK");
                            } else {
                                replies.add(null);
                            }
                            break;
                        default:
                            throw new IllegalStateException("unexpected " + op);
                    }
                } catch (NumberFormatException e) {
                    replies.add(new KeyValueStoreException("ERR value is not an integer", e));
                }
            }
            if (dropLastReply && !replies.isEmpty()) {
                replies.remove(replies.size() - 1);
            }
            return replies;
        }

        long add(String key, long delta) {
            return Long.parseLong(store.compute(key,
                    (k, v) -> String.valueOf((v == null ? 0L : Long.parseLong(v)) + delta)));
        }

        // Single-key methods: used by stop() / currentState() and by the per-key sync.
        @Override
        public void disconnect() {
        }

        @Override
        public String get(String key) {
            singleKeyCalls.incrementAndGet();
            return store.get(key);
        }

        @Override
        public void set(String key, String value) {
            singleKeyCalls.incrementAndGet();
            store.put(key, value);
        }

        @Override
        public long incrementBy(String key, long increment) {
            singleKeyCalls.incrementAndGet();
            return add(key, increment);
        }

        @Override
        public long decrementBy(String key, long decrement) {
            singleKeyCalls.incrementAndGet();
            return add(key, -decrement);
        }

        @Override
        public void setWithExpiry(String key, String value, long ttlMillis) {
            singleKeyCalls.incrementAndGet();
            store.put(key, value);
            ttls.put(key, ttlMillis);
        }

        @Override
        public void expireMillis(String key, long ttlMillis) {
            singleKeyCalls.incrementAndGet();
            ttls.put(key, ttlMillis);
        }

        @Override
        public void delete(String key) {
            singleKeyCalls.incrementAndGet();
            store.remove(key);
        }
    }

    /** A client written against the interface before batching: only the original single-key methods. */
    private static class SingleKeyOnlyClient implements KeyValueStoreClient {
        final ConcurrentHashMap<String, String> store = new ConcurrentHashMap<>();
        final AtomicInteger calls = new AtomicInteger();

        @Override
        public void disconnect() {
        }

        @Override
        public String get(String key) {
            calls.incrementAndGet();
            return store.get(key);
        }

        @Override
        public void set(String key, String value) {
            calls.incrementAndGet();
            store.put(key, value);
        }

        @Override
        public long incrementBy(String key, long increment) {
            calls.incrementAndGet();
            return Long.parseLong(store.merge(key, String.valueOf(increment),
                    (a, b) -> String.valueOf(Long.parseLong(a) + Long.parseLong(b))));
        }

        @Override
        public long decrementBy(String key, long decrement) {
            return incrementBy(key, -decrement);
        }

        @Override
        public void setWithExpiry(String key, String value, long ttlMillis) {
            calls.incrementAndGet();
            store.put(key, value);
        }

        @Override
        public void expireMillis(String key, long ttlMillis) {
            calls.incrementAndGet();
        }

        @Override
        public void delete(String key) {
            calls.incrementAndGet();
            store.remove(key);
        }
    }

    // -----------------------------------------------------------------------
    // Reflection helpers
    // -----------------------------------------------------------------------

    private static final Class<DistributedCountAttributeAggregator> DCAA = DistributedCountAttributeAggregator.class;

    private static Field field(Class<?> clazz, String name) throws Exception {
        Field f = clazz.getDeclaredField(name);
        f.setAccessible(true);
        return f;
    }

    private static void setStatic(String name, Object value) throws Exception {
        field(DCAA, name).set(null, value);
    }

    private static void set(Object aggregator, String name, Object value) throws Exception {
        field(DCAA, name).set(aggregator, value);
    }

    private static Object get(Object aggregator, String name) throws Exception {
        return field(DCAA, name).get(aggregator);
    }

    private static long counter(Object aggregator, String name) throws Exception {
        return ((AtomicLong) get(aggregator, name)).get();
    }

    @SuppressWarnings("unchecked")
    private static ConcurrentHashMap<String, DistributedCountAttributeAggregator> activeAggregators() throws Exception {
        return (ConcurrentHashMap<String, DistributedCountAttributeAggregator>) field(DCAA, "ACTIVE_AGGREGATORS")
                .get(null);
    }

    /** reset() and processAdd() read the window expiry from this thread-local; tests start without one. */
    private static void clearWindowExpiryThreadLocal() throws Exception {
        ((ThreadLocal<?>) field(ThrottleStreamProcessor.class, "windowExpiryThreadLocal").get(null)).remove();
    }

    /** Runs one sync tick (Collect, Send, Apply) on the calling thread. */
    private static void tick() throws Exception {
        Method m = DCAA.getDeclaredMethod("runSyncTick");
        m.setAccessible(true);
        try {
            m.invoke(null);
        } catch (InvocationTargetException e) {
            throw (Exception) e.getCause();
        }
    }

    // -----------------------------------------------------------------------
    // Setup / teardown
    // -----------------------------------------------------------------------

    private ScheduledExecutorService syncPool;
    private ExecutorService tickThread;
    private final long windowExpiry = System.currentTimeMillis() + 60000L;

    /** Creates an aggregator for the key, with the client injected and the window expiry known, and registers it. */
    private DistributedCountAttributeAggregator aggregator(String key, KeyValueStoreClient client) throws Exception {
        DistributedCountAttributeAggregator aggregator = new DistributedCountAttributeAggregator();
        set(aggregator, "key", key);
        set(aggregator, "kvStoreClient", client);
        set(aggregator, "storedWindowExpiry", windowExpiry);
        activeAggregators().put(key, aggregator);
        return aggregator;
    }

    private static void requests(DistributedCountAttributeAggregator aggregator, int count) {
        for (int i = 0; i < count; i++) {
            aggregator.processAdd((Object) null);
        }
    }

    /** Starts a tick on another thread and waits until its batch is blocked in the fake client. */
    private Future<?> tickInFlight(FakeBatchClient client) throws Exception {
        Future<?> running = tickThread.submit(() -> {
            tick();
            return null;
        });
        Assert.assertTrue("batch should be in flight", client.entered.await(5, TimeUnit.SECONDS));
        return running;
    }

    @Before
    public void setUp() throws Exception {
        clearWindowExpiryThreadLocal();
        DistributedThrottleConfig config = new DistributedThrottleConfig();
        config.setEnabled(true);
        setStatic("DISTRIBUTED_THROTTLE_CONFIG", config);
        setStatic("distributedThrottlingEnabled", true);
        setStatic("schedulerStarted", true); // never start the real scheduler; ticks are run by the tests
        setStatic("batchSyncEnabled", true);
        setStatic("batchSize", 500);
        syncPool = Executors.newScheduledThreadPool(4);
        setStatic("kvStoreSyncScheduler", syncPool);
        tickThread = Executors.newSingleThreadExecutor();
    }

    @After
    public void tearDown() throws Exception {
        activeAggregators().clear();
        setStatic("kvStoreSyncScheduler", null);
        syncPool.shutdownNow();
        tickThread.shutdownNow();
        setStatic("DISTRIBUTED_THROTTLE_CONFIG", null);
        setStatic("distributedThrottlingEnabled", false);
        setStatic("schedulerStarted", false);
        setStatic("batchSyncEnabled", true);
        setStatic("batchSize", DistributedThrottleConfig.DEFAULT_BATCH_SIZE);
        field(KeyValueStoreManager.class, "clientInstance").set(null, null);
        clearWindowExpiryThreadLocal();
    }

    // -----------------------------------------------------------------------
    // Collect: the same choice of operation as the per-key sync
    // -----------------------------------------------------------------------

    /**
     * One tick: a key with new requests is pushed with INCRBY (+ PEXPIRE while its TTL is unknown), keys without
     * new requests are read in one MGET, and a pending reset is written with PSETEX "0". A key that MGET finds
     * missing is created on the next tick with SET NX PX "0".
     */
    @Test(timeout = TEST_TIMEOUT_MS)
    public void testCollectChoosesTheSameOperationsAsThePerKeySync() throws Exception {
        FakeBatchClient client = new FakeBatchClient();
        DistributedCountAttributeAggregator active = aggregator("A", client);
        DistributedCountAttributeAggregator idle = aggregator("B", client);
        DistributedCountAttributeAggregator resetKey = aggregator("R", client);
        DistributedCountAttributeAggregator missing = aggregator("D", client);
        client.store.put("B", "7");
        client.store.put("R", "99");
        requests(active, 3);
        resetKey.reset();

        tick();

        Assert.assertEquals("one MGET for both keys without new requests", 1, client.mgetCalls.size());
        Assert.assertEquals(new HashSet<>(Arrays.asList("B", "D")), new HashSet<>(client.mgetCalls.get(0)));
        Assert.assertEquals("one pipeline for all writes", 1, client.pipelineCalls.size());
        Assert.assertEquals("INCRBY + PEXPIRE for A, PSETEX for R", 3, client.pipelineCalls.get(0).size());
        Assert.assertEquals("no single-key calls on the batched path", 0, client.singleKeyCalls.get());
        Assert.assertEquals("3", client.store.get("A"));
        Assert.assertEquals(3L, counter(active, "localCounter"));
        Assert.assertEquals(0L, counter(active, "unsyncedCounter"));
        Assert.assertEquals(true, get(active, "keyHasTTL"));
        Assert.assertEquals(7L, counter(idle, "localCounter"));
        Assert.assertEquals("0", client.store.get("R"));
        Assert.assertEquals(false, get(resetKey, "pendingReset"));
        Assert.assertEquals(true, get(resetKey, "keyHasTTL"));
        Assert.assertEquals(0L, counter(missing, "localCounter"));
        Assert.assertEquals(true, get(missing, "seedNeeded"));

        client.mgetCalls.clear();
        client.pipelineCalls.clear();
        tick();
        Assert.assertEquals("tick 2: only the seed of the missing key is written", 1, client.pipelineCalls.size());
        KeyValueStoreOperation seed = client.pipelineCalls.get(0).get(0);
        Assert.assertEquals(KeyValueStoreOperation.Type.SET_NX_PX, seed.getType());
        Assert.assertEquals("D", seed.getKey());
        Assert.assertTrue("seed TTL is the rest of the window", seed.getTtlMillis() > 0 && seed.getTtlMillis() <= 60000L);
        Assert.assertEquals("0", client.store.get("D"));
        Assert.assertEquals("tick 2: A, B and R are read", 3, client.mgetCalls.get(0).size());

        requests(active, 1);
        client.pipelineCalls.clear();
        tick();
        Assert.assertEquals("TTL known now: INCRBY only", 1, client.pipelineCalls.get(0).size());
        Assert.assertEquals(KeyValueStoreOperation.Type.INCRBY, client.pipelineCalls.get(0).get(0).getType());
        Assert.assertEquals(4L, counter(active, "localCounter"));
    }

    @Test(timeout = TEST_TIMEOUT_MS)
    public void testNegativeDeltaIsSentAsDecrby() throws Exception {
        FakeBatchClient client = new FakeBatchClient();
        DistributedCountAttributeAggregator aggregator = aggregator("K", client);
        client.store.put("K", "10");
        set(aggregator, "keyHasTTL", true);
        aggregator.processRemove((Object) null);

        tick();

        KeyValueStoreOperation op = client.pipelineCalls.get(0).get(0);
        Assert.assertEquals(KeyValueStoreOperation.Type.DECRBY, op.getType());
        Assert.assertEquals(1L, op.getAmount());
        Assert.assertEquals("9", client.store.get("K"));
        Assert.assertEquals(9L, counter(aggregator, "localCounter"));
    }

    @Test(timeout = TEST_TIMEOUT_MS)
    public void testPendingResetIsNotWrittenWhileTheWindowExpiryIsUnknown() throws Exception {
        FakeBatchClient client = new FakeBatchClient();
        DistributedCountAttributeAggregator aggregator = aggregator("K", client);
        set(aggregator, "storedWindowExpiry", 0L);
        aggregator.reset();

        tick();

        Assert.assertEquals("nothing to write without a TTL", 0, client.pipelineCalls.size());
        Assert.assertEquals(true, get(aggregator, "pendingReset"));
        Assert.assertEquals(false, get(aggregator, "keyHasTTL"));
    }

    @Test(timeout = TEST_TIMEOUT_MS)
    public void testMissingKeyIsNotSeededAfterItsWindowEnded() throws Exception {
        FakeBatchClient client = new FakeBatchClient();
        DistributedCountAttributeAggregator aggregator = aggregator("K", client);
        set(aggregator, "storedWindowExpiry", System.currentTimeMillis() - 1000L);

        tick();
        Assert.assertEquals(true, get(aggregator, "seedNeeded"));
        tick();

        Assert.assertEquals("no SET NX for a window that already ended", 0, client.pipelineCalls.size());
        Assert.assertEquals("the key is read again instead", 2, client.mgetCalls.size());
        Assert.assertNull(client.store.get("K"));
    }

    // -----------------------------------------------------------------------
    // Send: chunking
    // -----------------------------------------------------------------------

    @Test(timeout = TEST_TIMEOUT_MS)
    public void testChunksRespectBatchSizeAndKeepEachKeysOperationsTogether() throws Exception {
        setStatic("batchSize", 4);
        FakeBatchClient client = new FakeBatchClient();
        for (int i = 0; i < 9; i++) {
            aggregator("idle" + i, client);
        }
        for (int i = 0; i < 5; i++) {
            requests(aggregator("busy" + i, client), 1); // INCRBY + PEXPIRE each
        }

        tick();

        List<Integer> mgetSizes = new ArrayList<>();
        for (List<String> call : client.mgetCalls) {
            mgetSizes.add(call.size());
        }
        Collections.sort(mgetSizes);
        Assert.assertEquals("9 reads in MGETs of at most 4 keys", Arrays.asList(1, 4, 4), mgetSizes);

        List<Integer> pipelineSizes = new ArrayList<>();
        for (List<KeyValueStoreOperation> call : client.pipelineCalls) {
            pipelineSizes.add(call.size());
            for (int i = 0; i < call.size(); i += 2) {
                Assert.assertEquals(KeyValueStoreOperation.Type.INCRBY, call.get(i).getType());
                Assert.assertEquals("a key's PEXPIRE is in the same pipeline as its INCRBY",
                        KeyValueStoreOperation.Type.PEXPIRE, call.get(i + 1).getType());
                Assert.assertEquals(call.get(i).getKey(), call.get(i + 1).getKey());
            }
        }
        Collections.sort(pipelineSizes);
        Assert.assertEquals("10 commands in pipelines of at most 4", Arrays.asList(2, 4, 4), pipelineSizes);
    }

    @Test(timeout = TEST_TIMEOUT_MS)
    public void testEachClientInstanceGetsItsOwnBatch() throws Exception {
        FakeBatchClient first = new FakeBatchClient();
        FakeBatchClient second = new FakeBatchClient();
        aggregator("K1", first);
        aggregator("K2", second);

        tick();

        Assert.assertEquals(Collections.singletonList(Collections.singletonList("K1")), first.mgetCalls);
        Assert.assertEquals(Collections.singletonList(Collections.singletonList("K2")), second.mgetCalls);
    }

    // -----------------------------------------------------------------------
    // Apply: requests and resets that happen while a batch is in flight
    // -----------------------------------------------------------------------

    @Test(timeout = TEST_TIMEOUT_MS)
    public void testRequestsArrivingWhileTheBatchIsInFlightAreSyncedOnTheNextTick() throws Exception {
        FakeBatchClient client = new FakeBatchClient();
        DistributedCountAttributeAggregator aggregator = aggregator("K", client);
        client.store.put("K", "112"); // other TMs' count
        set(aggregator, "keyHasTTL", true);
        requests(aggregator, 3);
        client.blockWrites = true;

        Future<?> running = tickInFlight(client);
        requests(aggregator, 2); // while INCRBY 3 is in flight
        Assert.assertEquals(2L, counter(aggregator, "unsyncedCounter"));
        client.release.countDown();
        running.get(5, TimeUnit.SECONDS);

        Assert.assertEquals("reply (115) plus the 2 in-flight requests", 117L, counter(aggregator, "localCounter"));
        Assert.assertEquals("the 2 in-flight requests are still queued", 2L, counter(aggregator, "unsyncedCounter"));
        client.blockWrites = false;
        tick();
        Assert.assertEquals("117", client.store.get("K"));
        Assert.assertEquals(117L, counter(aggregator, "localCounter"));
        Assert.assertEquals(0L, counter(aggregator, "unsyncedCounter"));
    }

    /**
     * Requests counted while an MGET is in flight are not in its reply: the applied value keeps them, for a key
     * found in the store and for a missing one, and they stay queued for the next tick.
     */
    @Test(timeout = TEST_TIMEOUT_MS)
    public void testRequestsArrivingWhileAReadIsInFlightAreKeptInTheLocalCount() throws Exception {
        FakeBatchClient client = new FakeBatchClient();
        DistributedCountAttributeAggregator present = aggregator("P", client);
        DistributedCountAttributeAggregator missing = aggregator("M", client);
        client.store.put("P", "50");
        client.blockReads = true;

        Future<?> running = tickInFlight(client); // MGET P, M in flight
        requests(present, 2);
        requests(missing, 3);
        client.release.countDown();
        running.get(5, TimeUnit.SECONDS);

        Assert.assertEquals("stored 50 plus the 2 in-flight requests", 52L, counter(present, "localCounter"));
        Assert.assertEquals(2L, counter(present, "unsyncedCounter"));
        Assert.assertEquals("missing key (0) plus the 3 in-flight requests", 3L, counter(missing, "localCounter"));
        Assert.assertEquals(3L, counter(missing, "unsyncedCounter"));
        client.blockReads = false;
        tick();
        Assert.assertEquals("52", client.store.get("P"));
        Assert.assertEquals(52L, counter(present, "localCounter"));
        Assert.assertEquals("3", client.store.get("M"));
        Assert.assertEquals(3L, counter(missing, "localCounter"));
    }

    /**
     * The window resets while the INCRBY of the closed window is in flight: its reply (the closed window's total)
     * must not become the new window's count; the reset stays pending and is written on the next tick.
     */
    @Test(timeout = TEST_TIMEOUT_MS)
    public void testResetWhileAWriteIsInFlightDropsTheClosedWindowsReply() throws Exception {
        FakeBatchClient client = new FakeBatchClient();
        DistributedCountAttributeAggregator aggregator = aggregator("K", client);
        client.store.put("K", "100");
        set(aggregator, "keyHasTTL", true);
        requests(aggregator, 5);
        client.blockWrites = true;

        Future<?> running = tickInFlight(client);
        aggregator.reset();          // the window ends while INCRBY 5 is in flight
        requests(aggregator, 2);     // the new window's first requests
        client.release.countDown();
        running.get(5, TimeUnit.SECONDS);

        Assert.assertEquals("the closed window's total (105) is not applied", 2L, counter(aggregator, "localCounter"));
        Assert.assertEquals(true, get(aggregator, "pendingReset"));
        client.blockWrites = false;
        tick();
        Assert.assertEquals("PSETEX 0 on the next tick", "0", client.store.get("K"));
        Assert.assertEquals(false, get(aggregator, "pendingReset"));
        tick();
        Assert.assertEquals("then the new window's requests", "2", client.store.get("K"));
        Assert.assertEquals(2L, counter(aggregator, "localCounter"));
    }

    @Test(timeout = TEST_TIMEOUT_MS)
    public void testResetWhileAReadIsInFlightDropsTheClosedWindowsValue() throws Exception {
        FakeBatchClient client = new FakeBatchClient();
        DistributedCountAttributeAggregator aggregator = aggregator("K", client);
        client.store.put("K", "50");
        client.blockReads = true;

        Future<?> running = tickInFlight(client);
        aggregator.reset();
        client.release.countDown();
        running.get(5, TimeUnit.SECONDS);

        Assert.assertEquals("the closed window's value (50) is not applied", 0L, counter(aggregator, "localCounter"));
        Assert.assertEquals(true, get(aggregator, "pendingReset"));
    }

    @Test(timeout = TEST_TIMEOUT_MS)
    public void testResetWhileAResetIsInFlightKeepsTheNewResetPending() throws Exception {
        FakeBatchClient client = new FakeBatchClient();
        DistributedCountAttributeAggregator aggregator = aggregator("K", client);
        aggregator.reset();
        client.blockWrites = true;

        Future<?> running = tickInFlight(client);
        aggregator.reset(); // another reset while the first PSETEX is in flight
        client.release.countDown();
        running.get(5, TimeUnit.SECONDS);

        Assert.assertEquals("the newer reset is still owed", true, get(aggregator, "pendingReset"));
    }

    // -----------------------------------------------------------------------
    // Apply: failures
    // -----------------------------------------------------------------------

    @Test(timeout = TEST_TIMEOUT_MS)
    public void testFailedPipelineRestoresTheDeltaAndKeepsTheResetPending() throws Exception {
        FakeBatchClient client = new FakeBatchClient();
        DistributedCountAttributeAggregator counted = aggregator("A", client);
        DistributedCountAttributeAggregator resetKey = aggregator("R", client);
        set(counted, "keyHasTTL", true);
        requests(counted, 4);
        resetKey.reset();
        client.failNextWrite = new KeyValueStoreException("simulated connection reset");

        tick();

        Assert.assertEquals("delta restored", 4L, counter(counted, "unsyncedCounter"));
        Assert.assertEquals("reset still pending", true, get(resetKey, "pendingReset"));
        tick();
        Assert.assertEquals("4", client.store.get("A"));
        Assert.assertEquals(0L, counter(counted, "unsyncedCounter"));
        Assert.assertEquals("0", client.store.get("R"));
        Assert.assertEquals(false, get(resetKey, "pendingReset"));
    }

    @Test(timeout = TEST_TIMEOUT_MS)
    public void testFailedPipelineAfterAResetDiscardsTheClosedWindowsDelta() throws Exception {
        FakeBatchClient client = new FakeBatchClient();
        DistributedCountAttributeAggregator aggregator = aggregator("A", client);
        set(aggregator, "keyHasTTL", true);
        requests(aggregator, 4);
        client.blockWrites = true;
        client.failNextWrite = new KeyValueStoreException("simulated timeout");

        Future<?> running = tickInFlight(client);
        aggregator.reset();
        requests(aggregator, 1);
        client.release.countDown();
        running.get(5, TimeUnit.SECONDS);

        Assert.assertEquals("the closed window's 4 are dropped, the new window's 1 is kept", 1L,
                counter(aggregator, "unsyncedCounter"));
    }

    @Test(timeout = TEST_TIMEOUT_MS)
    public void testFailedCommandOnlyAffectsItsOwnKey() throws Exception {
        FakeBatchClient client = new FakeBatchClient();
        DistributedCountAttributeAggregator bad = aggregator("BAD", client);
        DistributedCountAttributeAggregator good = aggregator("GOOD", client);
        client.store.put("BAD", "not-a-number");
        set(bad, "keyHasTTL", true);
        set(good, "keyHasTTL", true);
        requests(bad, 2);
        requests(good, 1);

        tick();

        Assert.assertEquals("both in one pipeline", 1, client.pipelineCalls.size());
        Assert.assertEquals("BAD's delta restored", 2L, counter(bad, "unsyncedCounter"));
        Assert.assertEquals("1", client.store.get("GOOD"));
        Assert.assertEquals(1L, counter(good, "localCounter"));
        Assert.assertEquals(0L, counter(good, "unsyncedCounter"));
    }

    @Test(timeout = TEST_TIMEOUT_MS)
    public void testWrongNumberOfRepliesIsTreatedAsAFailedBatch() throws Exception {
        FakeBatchClient client = new FakeBatchClient();
        DistributedCountAttributeAggregator aggregator = aggregator("K", client);
        set(aggregator, "keyHasTTL", true);
        requests(aggregator, 3);
        client.dropLastReply = true;

        tick();

        Assert.assertEquals("delta restored", 3L, counter(aggregator, "unsyncedCounter"));
    }

    @Test(timeout = TEST_TIMEOUT_MS)
    public void testFailedReadChangesNothing() throws Exception {
        FakeBatchClient client = new FakeBatchClient();
        DistributedCountAttributeAggregator aggregator = aggregator("K", client);
        client.store.put("K", "8");
        tick();
        Assert.assertEquals(8L, counter(aggregator, "localCounter"));
        client.store.put("K", "9");
        client.failNextRead = new KeyValueStoreException("simulated read failure");

        tick();
        Assert.assertEquals("unchanged after the failed read", 8L, counter(aggregator, "localCounter"));
        tick();
        Assert.assertEquals("read again on the next tick", 9L, counter(aggregator, "localCounter"));
    }

    @Test(timeout = TEST_TIMEOUT_MS)
    public void testStopWhileABatchIsInFlightStillFlushesARestoredDelta() throws Exception {
        FakeBatchClient client = new FakeBatchClient();
        DistributedCountAttributeAggregator aggregator = aggregator("A", client);
        aggregator("OTHER", client); // keeps the map non-empty, so stop() does not shut the scheduler down
        set(aggregator, "keyHasTTL", true);
        requests(aggregator, 6);
        client.blockWrites = true;
        client.failNextWrite = new KeyValueStoreException("simulated failure");

        Future<?> running = tickInFlight(client);
        aggregator.stop(); // the 6 are in flight, so there is nothing to flush yet
        Assert.assertNotEquals("6", client.store.get("A"));
        client.release.countDown();
        running.get(5, TimeUnit.SECONDS);

        Assert.assertEquals("the restored delta is flushed although the key was stopped", "6", client.store.get("A"));
    }

    /**
     * stop() while the tick's PSETEX "0" is in flight: if stop() wrote the key itself (PSETEX "0", then INCRBY of
     * the new window's requests), the late PSETEX "0" of the batch would wipe that INCRBY. The flush must wait for
     * the batch to complete.
     */
    @Test(timeout = TEST_TIMEOUT_MS)
    public void testStopWhileAResetIsInFlightKeepsTheNewWindowsCount() throws Exception {
        FakeBatchClient client = new FakeBatchClient();
        DistributedCountAttributeAggregator aggregator = aggregator("A", client);
        aggregator("OTHER", client); // keeps the map non-empty, so stop() does not shut the scheduler down
        client.store.put("A", "100"); // the closed window's total
        aggregator.reset();
        client.blockWrites = true;

        Future<?> running = tickInFlight(client); // PSETEX "0" in flight
        requests(aggregator, 3);                  // the new window's first requests
        aggregator.stop();
        Assert.assertEquals("stop() does not write the key while a batched write is in flight",
                0, client.singleKeyCalls.get());
        client.release.countDown();
        running.get(5, TimeUnit.SECONDS);

        Assert.assertEquals("the new window's 3 requests are flushed after the PSETEX", "3", client.store.get("A"));
        Assert.assertEquals(false, get(aggregator, "pendingReset"));
        Assert.assertEquals(0L, counter(aggregator, "unsyncedCounter"));
    }

    /**
     * stop() while an INCRBY is in flight and it succeeds: the requests counted after the tick collected the key are
     * flushed when the batch completes, since stop() left them for the batch.
     */
    @Test(timeout = TEST_TIMEOUT_MS)
    public void testStopWhileAWriteIsInFlightFlushesTheRestAfterItSucceeds() throws Exception {
        FakeBatchClient client = new FakeBatchClient();
        DistributedCountAttributeAggregator aggregator = aggregator("A", client);
        aggregator("OTHER", client); // keeps the map non-empty, so stop() does not shut the scheduler down
        set(aggregator, "keyHasTTL", true);
        requests(aggregator, 3);
        client.blockWrites = true;

        Future<?> running = tickInFlight(client); // INCRBY 3 in flight
        requests(aggregator, 2);
        aggregator.stop();
        client.release.countDown();
        running.get(5, TimeUnit.SECONDS);

        Assert.assertEquals("both the in-flight 3 and the later 2 are in the store", "5", client.store.get("A"));
        Assert.assertEquals(0L, counter(aggregator, "unsyncedCounter"));
    }

    /**
     * currentState() while a batched write for the key is in flight must not write the key: for a key missing in
     * the store, its GET then PSETEX "0" could otherwise wipe the in-flight INCRBY if it lands in between.
     */
    @Test(timeout = TEST_TIMEOUT_MS)
    public void testCurrentStateDoesNotWriteTheKeyWhileABatchedWriteIsInFlight() throws Exception {
        FakeBatchClient client = new FakeBatchClient();
        DistributedCountAttributeAggregator aggregator = aggregator("A", client); // missing in the store
        requests(aggregator, 3);
        client.blockWrites = true;

        Future<?> running = tickInFlight(client); // INCRBY 3 + PEXPIRE in flight
        aggregator.currentState();
        Assert.assertEquals("no single-key GET / PSETEX while the write is in flight", 0, client.singleKeyCalls.get());
        client.release.countDown();
        running.get(5, TimeUnit.SECONDS);

        Assert.assertEquals("3", client.store.get("A"));
        aggregator.currentState();
        Assert.assertTrue("currentState() syncs again once the write is applied", client.singleKeyCalls.get() > 0);
        Assert.assertEquals(3L, counter(aggregator, "localCounter"));
    }

    // -----------------------------------------------------------------------
    // Fallback to the per-key sync
    // -----------------------------------------------------------------------

    @Test(timeout = TEST_TIMEOUT_MS)
    public void testBatchSyncDisabledUsesThePerKeySync() throws Exception {
        setStatic("batchSyncEnabled", false);
        FakeBatchClient client = new FakeBatchClient();
        DistributedCountAttributeAggregator aggregator = aggregator("K", client);
        requests(aggregator, 1);

        tick();

        Assert.assertEquals(0, client.mgetCalls.size());
        Assert.assertEquals(0, client.pipelineCalls.size());
        Assert.assertTrue(client.singleKeyCalls.get() > 0);
        Assert.assertEquals("1", client.store.get("K"));
        Assert.assertEquals(1L, counter(aggregator, "localCounter"));
    }

    @Test(timeout = TEST_TIMEOUT_MS)
    public void testClientWithoutBatchSupportUsesThePerKeySync() throws Exception {
        SingleKeyOnlyClient client = new SingleKeyOnlyClient();
        DistributedCountAttributeAggregator aggregator = aggregator("K", client);
        requests(aggregator, 2);

        tick();

        Assert.assertTrue(client.calls.get() > 0);
        Assert.assertEquals("2", client.store.get("K"));
        Assert.assertEquals(2L, counter(aggregator, "localCounter"));
        Assert.assertEquals(0L, counter(aggregator, "unsyncedCounter"));
    }
}
