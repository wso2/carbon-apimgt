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

package org.wso2.carbon.apimgt.healthcheck.probe;

/**
 * How full one of the throttle event receiver's worker thread pools was at one moment. Each open Gateway
 * connection holds one thread from its pool for as long as it stays open, so {@link #getFree()} is how many more
 * connections the receiver can still accept; at {@code 0}, a new connection is accepted by the port but never read.
 */
public final class ThreadPoolUsage {

    /**
     * Value of every count when the pool size could not be read.
     */
    static final int UNKNOWN_COUNT = -1;

    private final String pool;
    private final int size;
    private final int busy;

    ThreadPoolUsage(String pool, int size, int busy) {

        this.pool = pool;
        this.size = size;
        this.busy = busy;
    }

    /**
     * @return {@code tcp} or {@code ssl}
     */
    public String getPool() {

        return pool;
    }

    /**
     * @return whether the configured pool size could be read; when not, every count is {@link #UNKNOWN_COUNT}
     */
    public boolean isKnown() {

        return size != UNKNOWN_COUNT;
    }

    /**
     * @return the configured maximum number of threads, or {@link #UNKNOWN_COUNT}
     */
    public int getSize() {

        return size;
    }

    /**
     * @return threads currently serving a connection, or {@link #UNKNOWN_COUNT}
     */
    public int getBusy() {

        return isKnown() ? busy : UNKNOWN_COUNT;
    }

    /**
     * @return threads still available for new connections, or {@link #UNKNOWN_COUNT}
     */
    public int getFree() {

        return isKnown() ? Math.max(0, size - busy) : UNKNOWN_COUNT;
    }
}
