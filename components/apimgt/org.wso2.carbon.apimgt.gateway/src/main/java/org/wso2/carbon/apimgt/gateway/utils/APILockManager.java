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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Manages per-API-context ReentrantLocks to coordinate concurrent API artifact deployments.
 *
 * <p>Used to prevent the race condition between two threads that can concurrently attempt to
 * deploy the same Synapse API artifact when {@code enable_on_demand_loading=true} is configured:
 * <ul>
 *   <li><b>Tenant-loading thread</b> (GatewayStartupListener / JMS reconnect) — uses
 *       {@link #tryLockNow(String)} (non-blocking): if the passthrough thread already holds
 *       the lock, the startup thread skips deployment because the API will be deployed by
 *       the passthrough thread anyway.</li>
 *   <li><b>Passthrough thread</b> (DefaultAPIHandler on-demand path) — uses {@link #lock(String)}
 *       (blocking): waits until the tenant-loading thread releases the lock before proceeding,
 *       then re-checks {@code isDeployed()} before actually invoking deploy.</li>
 * </ul>
 *
 * <p>Locks are reference-counted so that map entries are cleaned up automatically when no
 * thread references a given key.
 */
public class APILockManager {

    private static final Log log = LogFactory.getLog(APILockManager.class);
    private static final APILockManager INSTANCE = new APILockManager();

    /**
     * Internal lock entry: a fair ReentrantLock paired with a reference counter.
     */
    private static class LockEntry {
        final ReentrantLock lock;
        final AtomicInteger refCount = new AtomicInteger(0);

        LockEntry() {
            this.lock = new ReentrantLock(true /* fair */);
        }
    }

    private final Map<String, LockEntry> lockMap = new ConcurrentHashMap<>();

    private APILockManager() {
    }

    public static APILockManager getInstance() {
        return INSTANCE;
    }

    /**
     * Acquires the lock for the given key, blocking until it is available.
     * Intended for the passthrough (on-demand loading) thread.
     *
     * @param key unique lock key (typically {@code API_LOADING_ON_DEMAND + apiContext})
     */
    public void lock(String key) {
        LockEntry entry = lockMap.compute(key, (k, v) -> {
            LockEntry e = (v == null) ? new LockEntry() : v;
            e.refCount.incrementAndGet();
            return e;
        });
        entry.lock.lock();
    }

    /**
     * Attempts to acquire the lock for the given key immediately without waiting.
     * Intended for the tenant-loading / JMS-redeploy thread.
     *
     * @param key unique lock key
     * @return {@code true} if the lock was acquired; {@code false} if another thread holds it
     */
    public boolean tryLockNow(String key) {
        LockEntry entry = lockMap.compute(key, (k, v) -> {
            LockEntry e = (v == null) ? new LockEntry() : v;
            e.refCount.incrementAndGet();
            return e;
        });
        if (!entry.lock.tryLock()) {
            lockMap.compute(key, (k, v) -> {
                if (v == null) {
                    return null;
                }
                return v.refCount.decrementAndGet() == 0 ? null : v;
            });
            return false;
        }
        return true;
    }

    /**
     * Returns {@code true} if the lock for the given key is currently held by any thread, or
     * if there are threads waiting/referencing it ({@code refCount > 0}).
     *
     * @param key unique lock key
     * @return {@code true} if locked or in use
     */
    public boolean isLocked(String key) {
        LockEntry entry = lockMap.get(key);
        if (entry == null) {
            return false;
        }
        return entry.lock.isLocked() || entry.refCount.get() > 0;
    }

    /**
     * Releases the lock for the given key. Must be called by the thread that holds the lock,
     * typically in a {@code finally} block.
     *
     * @param key unique lock key
     */
    public void unlock(String key) {
        lockMap.compute(key, (k, v) -> {
            if (v == null) {
                log.error("APILockManager: attempted to unlock a key with no entry: " + key);
                return null;
            }
            if (!v.lock.isHeldByCurrentThread()) {
                log.error("APILockManager: current thread does not hold lock for key: " + key);
                return v;
            }
            v.lock.unlock();
            return v.refCount.decrementAndGet() == 0 ? null : v;
        });
    }
}

