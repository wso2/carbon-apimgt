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

package org.wso2.carbon.apimgt.governance.generic.observer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.governance.generic.service.DeprecationGuideScheduler;
import org.wso2.carbon.apimgt.governance.generic.service.GenericService;
import org.wso2.carbon.core.ServerStartupObserver;

/**
 * Server startup observer for the Generic module.
 * Performs hydration of the LSH index from the database during server startup.
 */
public class GenericStartupObserver implements ServerStartupObserver {

    private static final Log log = LogFactory.getLog(GenericStartupObserver.class);

    @Override
    public void completingServerStartup() {
        // Not used
    }

    private static final int MAX_RETRIES = 5;
    private static final long RETRY_DELAY_MS = 10000; // 10 seconds

    @Override
    public void completedServerStartup() {
        log.info("Generic engine startup observer triggered - scheduling LSH index hydration");

        // Run initialization in a background thread with retry logic to handle
        // datasource timing issues (JNDI may not be ready when this observer fires)
        Thread initThread = new Thread(() -> {
            int attempt = 0;
            while (attempt < MAX_RETRIES) {
                attempt++;
                try {
                    log.info("Generic engine initialization attempt " + attempt + "/" + MAX_RETRIES);

                    // Initialize the GenericService which performs hydration
                    GenericService genericService = GenericService.getInstance();
                    genericService.initialize();

                    log.info("Generic engine LSH index hydration completed successfully. " +
                            "Index contains " + genericService.getIndexSize() + " API signatures.");

                    // Index any existing APIs that are not yet in AM_API_MINHASH table
                    log.info("Checking for existing APIs that need to be indexed for deduplication...");
                    int newlyIndexedCount = genericService.indexExistingAPIs();

                    if (newlyIndexedCount > 0) {
                        log.info("Indexed " + newlyIndexedCount + " pre-existing APIs for deduplication. " +
                                "Total index size: " + genericService.getIndexSize());
                    } else {
                        log.info("All existing APIs are already indexed. No new APIs to index.");
                    }

                    // Initialize the Deprecation Guide scheduler
                    log.info("Initializing Deprecation Guide Scheduler...");
                    DeprecationGuideScheduler.initialize();

                    log.info("Generic engine initialization completed successfully on attempt " + attempt);
                    return; // Success — exit the retry loop

                } catch (Throwable e) {
                    if (attempt < MAX_RETRIES) {
                        log.warn("Generic engine initialization attempt " + attempt + " failed "
                                + "(datasource may not be ready). Retrying in "
                                + (RETRY_DELAY_MS / 1000) + "s: " + e.getMessage());
                        try {
                            Thread.sleep(RETRY_DELAY_MS);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            log.error("Generic engine initialization interrupted", ie);
                            return;
                        }
                    } else {
                        log.error("Failed to initialize Generic service after " + MAX_RETRIES
                                + " attempts. Deduplication checks may not function correctly.", e);
                    }
                }
            }
        }, "GenericInitThread");
        initThread.setDaemon(true);
        initThread.start();
    }
}
