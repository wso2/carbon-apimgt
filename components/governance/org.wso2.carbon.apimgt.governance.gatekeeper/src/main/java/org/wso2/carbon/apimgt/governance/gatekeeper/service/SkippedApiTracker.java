/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.apimgt.governance.gatekeeper.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks APIs that were skipped during indexing because deduplication was disabled.
 * When deduplication is re-enabled, these APIs can be hydrated (indexed) in the background.
 * Thread-safe using ConcurrentHashMap-backed sets.
 */
public class SkippedApiTracker {

    private static final Log log = LogFactory.getLog(SkippedApiTracker.class);
    private static volatile SkippedApiTracker instance;

    // organization -> Set<apiUuid>
    private final ConcurrentHashMap<String, Set<String>> skippedApis = new ConcurrentHashMap<>();

    private SkippedApiTracker() {
    }

    /**
     * Get the singleton instance.
     *
     * @return SkippedApiTracker instance
     */
    public static SkippedApiTracker getInstance() {
        if (instance == null) {
            synchronized (SkippedApiTracker.class) {
                if (instance == null) {
                    instance = new SkippedApiTracker();
                }
            }
        }
        return instance;
    }

    /**
     * Record that an API was skipped during indexing.
     *
     * @param apiUuid      the API UUID
     * @param organization the organization/tenant domain
     */
    public void trackSkipped(String apiUuid, String organization) {
        skippedApis.computeIfAbsent(organization,
                k -> ConcurrentHashMap.newKeySet()).add(apiUuid);
        if (log.isDebugEnabled()) {
            log.debug("Tracked skipped API: " + apiUuid + " for org: " + organization
                    + " (total skipped: " + getSkippedCount(organization) + ")");
        }
    }

    /**
     * Drain all skipped API UUIDs for an organization (returns and clears them atomically).
     *
     * @param organization the organization/tenant domain
     * @return set of API UUIDs that were skipped, or empty set
     */
    public Set<String> drainSkipped(String organization) {
        Set<String> removed = skippedApis.remove(organization);
        if (removed != null && !removed.isEmpty()) {
            log.info("Drained " + removed.size() + " skipped APIs for org: " + organization);
            return removed;
        }
        return Collections.emptySet();
    }

    /**
     * Check if there are any skipped APIs for the given organization.
     *
     * @param organization the organization/tenant domain
     * @return true if there are skipped APIs
     */
    public boolean hasSkippedApis(String organization) {
        Set<String> apis = skippedApis.get(organization);
        return apis != null && !apis.isEmpty();
    }

    /**
     * Get the count of skipped APIs for an organization.
     *
     * @param organization the organization/tenant domain
     * @return count of skipped APIs
     */
    public int getSkippedCount(String organization) {
        Set<String> apis = skippedApis.get(organization);
        return apis != null ? apis.size() : 0;
    }

    /**
     * Remove a specific API from the skipped tracker (e.g., if it was deleted).
     *
     * @param apiUuid      the API UUID
     * @param organization the organization/tenant domain
     */
    public void removeFromSkipped(String apiUuid, String organization) {
        Set<String> apis = skippedApis.get(organization);
        if (apis != null) {
            apis.remove(apiUuid);
        }
    }

    /**
     * Clear all tracked data (for testing or reset).
     */
    public void clearAll() {
        skippedApis.clear();
    }
}
