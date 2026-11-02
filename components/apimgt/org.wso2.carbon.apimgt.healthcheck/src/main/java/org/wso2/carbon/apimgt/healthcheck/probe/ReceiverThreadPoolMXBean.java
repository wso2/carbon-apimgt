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
 * JMX view of the throttle receiver's worker thread pools, registered as
 * {@value ReceiverThreadPoolMonitor#OBJECT_NAME}. Counts are read live and are {@code -1} when the pool size is
 * unknown.
 */
public interface ReceiverThreadPoolMXBean {

    /**
     * @return {@code UP}, {@code DOWN} if a pool has no free threads, or {@code UNKNOWN}
     */
    String getStatus();

    int getTcpPoolSize();

    int getTcpBusyThreads();

    int getTcpFreeThreads();

    int getSslPoolSize();

    int getSslBusyThreads();

    int getSslFreeThreads();
}
