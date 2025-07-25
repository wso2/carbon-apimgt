/*
 *  Copyright (c) 2025, WSO2 LLC. (https://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl.scheduler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.dao.GatewayManagementDAO;
import org.wso2.carbon.apimgt.impl.dto.GatewayNotificationConfiguration;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;

import java.sql.Timestamp;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Scheduler for cleaning up gateway heartbeat data from the database.
 * This scheduler runs periodically to:
 * 1. Update gateway status to EXPIRED for gateways that haven't sent heartbeat within the expire time
 * 2. Delete gateway records that are older than the data retention period
 * 3. Clean up orphaned deployment records
 */
public class GatewayCleanupScheduler {

    private static final Log log = LogFactory.getLog(GatewayCleanupScheduler.class);
    // define sheduled task interval in seconds
    private static final int DEFAULT_CLEANUP_STARTUP_DELAY = 60;
    private static GatewayCleanupScheduler instance;
    private ScheduledExecutorService scheduler;
    private GatewayManagementDAO gatewayManagementDAO;
    private GatewayNotificationConfiguration configuration;
    private boolean isRunning = false;

    private GatewayCleanupScheduler() {
        this.gatewayManagementDAO = GatewayManagementDAO.getInstance();
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    /**
     * Returns the singleton instance of GatewayCleanupScheduler.
     * Creates a new instance if one doesn't exist.
     *
     * @return the singleton GatewayCleanupScheduler instance
     */
    public static synchronized GatewayCleanupScheduler getInstance() {
        if (instance == null) {
            instance = new GatewayCleanupScheduler();
        }
        return instance;
    }

    /**
     * Starts the gateway cleanup scheduler with configured interval and startup delay.
     * The scheduler performs periodic cleanup operations including:
     * - Updating gateway status to EXPIRED for inactive gateways
     * - Deleting old gateway records based on retention period
     *
     * @throws Exception if there's an error starting the scheduler
     */
    public void start() {
        if (isRunning) {
            log.warn("Gateway cleanup scheduler is already running");
            return;
        }

        try {
            configuration = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                    .getAPIManagerConfiguration().getGatewayNotificationConfiguration();

            if (!configuration.isEnabled()) {
                log.info("Gateway cleanup scheduler is disabled in configuration");
                return;
            }

            log.info("Starting Gateway cleanup scheduler with interval: "
                             + configuration.getGatewayCleanupConfiguration().getCleanupIntervalSeconds() + " Seconds");

            scheduler.scheduleAtFixedRate(new GatewayCleanupTask(), DEFAULT_CLEANUP_STARTUP_DELAY,
                                          configuration.getGatewayCleanupConfiguration().getCleanupIntervalSeconds(),
                                          java.util.concurrent.TimeUnit.SECONDS);

            isRunning = true;
            log.info("Gateway cleanup scheduler started successfully");

        } catch (Exception e) {
            log.error("Failed to start Gateway cleanup scheduler", e);
        }
    }

    /**
     * Stops the gateway cleanup scheduler and shuts down the executor service.
     * Waits for ongoing tasks to complete gracefully before forcing shutdown.
     * Sets the running status to false and logs the completion.
     */
    public void stop() {
        if (!isRunning) {
            log.warn("Gateway cleanup scheduler is not running");
            return;
        }

        log.info("Stopping Gateway cleanup scheduler");

        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        isRunning = false;
        log.info("Gateway cleanup scheduler stopped successfully");
    }

    /**
     * Checks if the gateway cleanup scheduler is currently running.
     *
     * @return true if the scheduler is running, false otherwise
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Runnable task that performs the actual cleanup operations
     */
    private class GatewayCleanupTask implements Runnable {

        @Override
        public void run() {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("Starting Gateway cleanup task");
                }

                long currentTime = System.currentTimeMillis();

                long expireTimeThreshold =
                        currentTime - (configuration.getGatewayCleanupConfiguration().getExpireTimeSeconds() * 1000L);
                long retentionThreshold =
                        currentTime - (configuration.getGatewayCleanupConfiguration().getDataRetentionPeriodSeconds()
                                * 1000L);

                Timestamp expireTimestamp = new Timestamp(expireTimeThreshold);
                Timestamp retentionTimestamp = new Timestamp(retentionThreshold);

                int expiredCount = gatewayManagementDAO.updateExpiredGateways(expireTimestamp);

                int deletedCount = gatewayManagementDAO.deleteOldGatewayRecords(retentionTimestamp);

                if (log.isInfoEnabled()) {
                    log.info("Gateway cleanup completed - Expired: " + expiredCount + ", Deleted: " + deletedCount);
                }

            } catch (APIManagementException e) {
                log.error("Error during Gateway cleanup task", e);
            } catch (Exception e) {
                log.error("Unexpected error during Gateway cleanup task", e);
            }
        }
    }
} 