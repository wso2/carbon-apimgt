/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.impl.utils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A simple, thread-safe LRU cache implementation. This cache allows concurrent reads.
 * Concurrent write attempts are synchronized using an exclusive lock.
 */
public class LRUCache<K,V> extends LinkedHashMap<K,V> {

    private int maxEntries;
    private ReadWriteLock lock;

    public LRUCache(int maxEntries) {
        super(maxEntries + 1, 1, false);
        this.maxEntries = maxEntries;
        this.lock = new ReentrantReadWriteLock();
    }

    @Override
    public V get(Object key) {
        lock.readLock().lock();
        try {
            return super.get(key);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public V put(K key, V value) {
        lock.writeLock().lock();
        try {
            return super.put(key, value);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public V remove(Object key) {
        lock.writeLock().lock();
        try {
            return super.remove(key);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void clear() {
        lock.writeLock().lock();
        try {
            super.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry eldest) {
        boolean remove = size() > maxEntries;
        if (remove) {
            handleRemovableEntry(eldest);
        }
        return remove;
    }
    
    protected void handleRemovableEntry(Map.Entry<K,V> entry) {
        
    }
    
    public void exclusiveLock() {
        lock.writeLock().lock();
    }

    public void release() {
        lock.writeLock().unlock();
    }
}
