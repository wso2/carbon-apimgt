/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.gateway.utils;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit tests for {@link APILockManager}.
 *
 * <p>Covers:
 * <ul>
 *   <li>Scenario 1 — Tenant-loading thread wins (tryLockNow first), passthrough thread blocks on
 *       lock() and re-checks isDeployed() after the startup thread releases.</li>
 *   <li>Scenario 2 — Passthrough thread wins (lock() first), tryLockNow in startup thread returns
 *       false → startup thread skips deployment.</li>
 *   <li>Scenario 3 — isLocked() reflects real state before and after unlock.</li>
 *   <li>Scenario 4 — Lock entry is cleaned up from the internal map after unlock, preventing
 *       memory leaks over time.</li>
 *   <li>Scenario 5 — tryLockNow is idempotent for the same key (second call returns false while
 *       first hold is still active).</li>
 *   <li>Scenario 6 — Concurrent mutual exclusion: N threads all try to deploy; exactly one
 *       increments the counter per wave.</li>
 * </ul>
 */
public class APILockManagerTest {

    private final APILockManager lockManager = APILockManager.getInstance();
    private static final String TEST_KEY = "LoadAPI_OnDemandTest_/t/test.com/pizzashack/1.0.0";

    /**
     * Release the lock only if it is still held at the end of a test (e.g., a mid-test failure),
     * so passing tests do not exercise the error-log path inside unlock().
     */
    @After
    public void tearDown() {
        if (lockManager.isLocked(TEST_KEY)) {
            lockManager.unlock(TEST_KEY);
        }
    }

    /**
     * Scenario 1: Tenant-loading thread (tryLockNow) acquires the lock first.
     * Passthrough thread on lock() must block, then re-check after the lock is released.
     */
    @Test
    public void testStartupThreadWinsPassthroughBlocks() throws InterruptedException {
        // Simulate startup thread acquiring the lock non-blocking
        boolean startupAcquired = lockManager.tryLockNow(TEST_KEY);
        Assert.assertTrue("Startup thread should acquire an uncontested lock", startupAcquired);
        Assert.assertTrue("Lock should be reported as locked", lockManager.isLocked(TEST_KEY));

        AtomicBoolean passthroughLockAcquired = new AtomicBoolean(false);
        CountDownLatch passthroughStarted = new CountDownLatch(1);
        CountDownLatch passthroughDone = new CountDownLatch(1);

        // Simulate passthrough thread attempting to acquire (should block)
        Thread passthroughThread = new Thread(() -> {
            passthroughStarted.countDown(); // signal that it's about to block
            lockManager.lock(TEST_KEY);
            try {
                passthroughLockAcquired.set(true);
            } finally {
                lockManager.unlock(TEST_KEY);
                passthroughDone.countDown();
            }
        });
        passthroughThread.start();

        // Wait until the passthrough thread has started
        Assert.assertTrue(passthroughStarted.await(2, TimeUnit.SECONDS));

        // Wait deterministically until the passthrough thread is parked inside lock()
        // (Thread.State.WAITING == LockSupport.park, which ReentrantLock.lock() uses).
        // This replaces a fixed Thread.sleep() that is unreliable on slow/loaded CI runners.
        long deadline = System.currentTimeMillis() + 2000;
        while (passthroughThread.getState() != Thread.State.WAITING
                && System.currentTimeMillis() < deadline) {
            Thread.yield();
        }
        Assert.assertEquals("Passthrough thread should be parked inside lock()",
                Thread.State.WAITING, passthroughThread.getState());

        // Passthrough thread is definitively blocked — it must NOT have acquired the lock yet
        Assert.assertFalse("Passthrough should still be blocked while startup holds lock",
                passthroughLockAcquired.get());

        // Startup thread finishes — release the lock
        lockManager.unlock(TEST_KEY);

        // Now passthrough thread should unblock and acquire
        Assert.assertTrue("Passthrough thread should unblock after startup releases",
                passthroughDone.await(3, TimeUnit.SECONDS));
        Assert.assertTrue("Passthrough thread should have acquired the lock after startup released",
                passthroughLockAcquired.get());
    }

    /**
     * Scenario 2: Passthrough thread holds the blocking lock.
     * Startup thread's tryLockNow must return false (non-blocking skip behaviour).
     */
    @Test
    public void testPassthroughThreadWinsStartupSkips() throws InterruptedException {
        CountDownLatch passthroughHoldsLock = new CountDownLatch(1);
        CountDownLatch startupChecked = new CountDownLatch(1);

        Thread passthroughThread = new Thread(() -> {
            lockManager.lock(TEST_KEY);
            try {
                passthroughHoldsLock.countDown(); // signal: lock is held
                // Wait until startup thread has done its tryLockNow check
                try {
                    startupChecked.await(3, TimeUnit.SECONDS);
                } catch (InterruptedException ignored) {
                }
            } finally {
                lockManager.unlock(TEST_KEY);
            }
        });
        passthroughThread.start();

        // Wait until passthrough holds the lock
        Assert.assertTrue(passthroughHoldsLock.await(2, TimeUnit.SECONDS));

        // Startup thread's tryLockNow should return false — API is being deployed by passthrough
        boolean startupAcquired = lockManager.tryLockNow(TEST_KEY);
        startupChecked.countDown(); // signal done

        Assert.assertFalse("Startup thread should NOT acquire lock when passthrough holds it",
                startupAcquired);
        passthroughThread.join(3000);
    }

    /**
     * Scenario 3: isLocked() correctly reflects lock state transitions.
     */
    @Test
    public void testIsLockedReflectsState() {
        Assert.assertFalse("Key should not be locked before any lock call",
                lockManager.isLocked(TEST_KEY));

        lockManager.lock(TEST_KEY);
        Assert.assertTrue("Key should be reported locked after lock()", lockManager.isLocked(TEST_KEY));

        lockManager.unlock(TEST_KEY);
        Assert.assertFalse("Key should not be locked after unlock()", lockManager.isLocked(TEST_KEY));
    }

    /**
     * Scenario 4: Lock entry is removed from internal map after all references are released,
     * preventing memory leaks over time.
     */
    @Test
    public void testLockEntryCleanedUpAfterUnlock() {
        lockManager.lock(TEST_KEY);
        lockManager.unlock(TEST_KEY);

        // After unlock with no other references, isLocked should return false (entry removed)
        Assert.assertFalse("Lock entry should be cleaned up after unlock", lockManager.isLocked(TEST_KEY));
    }

    /**
     * Scenario 5: Second tryLockNow on a key already held returns false (not re-entrant across
     * different "threads" — a different thread cannot steal the lock).
     */
    @Test
    public void testTryLockNowIsNotReentrantAcrossThreads() throws InterruptedException {
        // First caller acquires successfully
        boolean first = lockManager.tryLockNow(TEST_KEY);
        Assert.assertTrue("First tryLockNow should succeed", first);

        // A different thread tries the same key — must fail
        AtomicBoolean secondResult = new AtomicBoolean(true);
        Thread competitor = new Thread(() -> secondResult.set(lockManager.tryLockNow(TEST_KEY)));
        competitor.start();
        competitor.join(2000);

        Assert.assertFalse("Second tryLockNow from a different thread should fail",
                secondResult.get());

        lockManager.unlock(TEST_KEY);
    }

    /**
     * Scenario 6 — Concurrent stress: N threads all try to deploy the same API (all calling
     * lock() then checking a shared counter). Exactly one should increment the counter on each
     * "wave" without duplication — verifying mutual exclusion.
     */
    @Test
    public void testMutualExclusionUnderConcurrency() throws InterruptedException {
        final int threadCount = 20;
        final AtomicInteger deployCount = new AtomicInteger(0);
        final CountDownLatch startGate = new CountDownLatch(1);
        final CountDownLatch done = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                try {
                    startGate.await();
                } catch (InterruptedException ignored) {
                }
                lockManager.lock(TEST_KEY);
                try {
                    // Simulate check-then-deploy: only deploy if not yet deployed
                    if (deployCount.get() == 0) {
                        deployCount.incrementAndGet();
                    }
                } finally {
                    lockManager.unlock(TEST_KEY);
                    done.countDown();
                }
            }).start();
        }

        startGate.countDown(); // release all threads simultaneously
        Assert.assertTrue("All threads should finish", done.await(10, TimeUnit.SECONDS));
        Assert.assertEquals("Exactly one deployment should have occurred", 1, deployCount.get());
    }
}

