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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Background service that hydrates (indexes) APIs that were skipped while
 * deduplication was disabled. When dedup is re-enabled, this service drains
 * the SkippedApiTracker and indexes all pending APIs in batches using a 
 * thread pool.
 */
public class BackgroundHydrationService {

    private static final Log log = LogFactory.getLog(BackgroundHydrationService.class);
    private static volatile BackgroundHydrationService instance;

    private static final int THREAD_POOL_SIZE = 4;
    private static final int BATCH_SIZE = 50;

    private final ExecutorService executorService;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    private BackgroundHydrationService() {
        this.executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE, r -> {
            Thread t = new Thread(r, "GatekeeperHydration-" + System.currentTimeMillis());
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * Get the singleton instance.
     *
     * @return BackgroundHydrationService instance
     */
    public static BackgroundHydrationService getInstance() {
        if (instance == null) {
            synchronized (BackgroundHydrationService.class) {
                if (instance == null) {
                    instance = new BackgroundHydrationService();
                }
            }
        }
        return instance;
    }

    /**
     * Trigger hydration for all skipped APIs in the given organization.
     * This method is non-blocking - hydration happens in background threads.
     *
     * @param organization the organization/tenant domain
     */
    public void triggerHydration(String organization) {
        if (isRunning.compareAndSet(false, true)) {
            executorService.submit(() -> {
                try {
                    hydrateOrganization(organization);
                } catch (Exception e) {
                    log.error("Error during background hydration for org: " + organization, e);
                } finally {
                    isRunning.set(false);
                }
            });
        } else {
            log.info("Hydration already in progress, skipping trigger for org: " + organization);
        }
    }

    /**
     * Perform hydration for an organization by draining skipped APIs and indexing them in batches.
     *
     * @param organization the organization/tenant domain
     */
    private void hydrateOrganization(String organization) {
        SkippedApiTracker tracker = SkippedApiTracker.getInstance();
        Set<String> skippedApiIds = tracker.drainSkipped(organization);

        if (skippedApiIds.isEmpty()) {
            log.info("No skipped APIs to hydrate for org: " + organization);
            return;
        }

        log.info("Starting background hydration of " + skippedApiIds.size()
                + " skipped APIs for org: " + organization);

        List<String> apiIdList = new ArrayList<>(skippedApiIds);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // Process in batches
        for (int i = 0; i < apiIdList.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, apiIdList.size());
            List<String> batch = apiIdList.subList(i, end);

            log.info("Hydrating batch " + ((i / BATCH_SIZE) + 1) + " ("
                    + batch.size() + " APIs) for org: " + organization);

            for (String apiId : batch) {
                try {
                    indexSingleApi(apiId, organization);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    log.warn("Failed to hydrate API " + apiId + ": " + e.getMessage());
                }
            }
        }

        log.info("Background hydration completed for org: " + organization
                + ". Success: " + successCount.get() + ", Failed: " + failCount.get());
    }

    /**
     * Index a single API by fetching its definition and calling GatekeeperService.indexAPI.
     *
     * @param apiId        the API UUID
     * @param organization the organization/tenant domain
     * @throws Exception if indexing fails
     */
    private void indexSingleApi(String apiId, String organization) throws Exception {
        // Fetch the API definition
        String adminUsername = getAdminUsername(organization);
        org.wso2.carbon.apimgt.api.APIProvider apiProvider =
                org.wso2.carbon.apimgt.impl.APIManagerFactory.getInstance()
                        .getAPIProvider(adminUsername);

        if (apiProvider == null) {
            throw new RuntimeException("Could not get APIProvider for org: " + organization);
        }

        org.wso2.carbon.apimgt.api.model.API api = apiProvider.getAPIbyUUID(apiId, organization);
        if (api == null) {
            log.debug("API " + apiId + " no longer exists, skipping hydration.");
            return;
        }

        String apiDefinition = apiProvider.getOpenAPIDefinition(api.getUuid(), organization);
        if (apiDefinition == null || apiDefinition.isEmpty()) {
            log.debug("No API definition available for " + apiId + ", skipping hydration.");
            return;
        }

        // Index using GatekeeperService
        GatekeeperService gatekeeperService = GatekeeperService.getInstance();
        gatekeeperService.indexAPI(apiId, apiDefinition, organization);

        if (log.isDebugEnabled()) {
            log.debug("Successfully hydrated API: " + apiId);
        }
    }

    /**
     * Get the admin username for the organization.
     *
     * @param organization the organization/tenant domain
     * @return admin username
     */
    private String getAdminUsername(String organization) {
        if (organization == null || "carbon.super".equals(organization)) {
            return "admin";
        }
        return "admin@" + organization;
    }

    /**
     * Check if hydration is currently running.
     *
     * @return true if hydration is in progress
     */
    public boolean isHydrationRunning() {
        return isRunning.get();
    }

    /**
     * Shutdown the thread pool gracefully.
     */
    public void shutdown() {
        executorService.shutdown();
    }
}
