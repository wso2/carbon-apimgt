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
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.GatewayManagementDAO;
import org.wso2.carbon.apimgt.impl.dto.GatewayNotificationConfiguration;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;

import java.sql.Timestamp;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Scheduler for validating liveliness of gateway and expired them
 */
public class GatewayValidationScheduler {

    private static final Log log = LogFactory.getLog(GatewayValidationScheduler.class);
    private static volatile GatewayValidationScheduler instance;
    private ScheduledExecutorService scheduler;
    private GatewayManagementDAO gatewayManagementDAO;
    private GatewayNotificationConfiguration configuration;
    private boolean isRunning = false;

    private GatewayValidationScheduler() {
        this.gatewayManagementDAO = GatewayManagementDAO.getInstance();
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    /**
     * Returns the singleton instance of GatewayCleanupScheduler.
     */
    public static synchronized GatewayValidationScheduler getInstance() {
        if (instance == null) {
            instance = new GatewayValidationScheduler();
        }
        return instance;
    }

    /**
     * Starts the gateway cleanup scheduler with configured interval and startup delay.
     */
    public void start() {
        if (isRunning) {
            log.warn("Gateway cleanup scheduler is already running");
            return;
        }

        configuration =
                ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration()
                        .getGatewayNotificationConfiguration();

        if (!configuration.isEnabled()) {
            log.info("Gateway cleanup scheduler is disabled in configuration");
            return;
        }

        log.info("Starting Gateway cleanup scheduler with interval: " + configuration.getGatewayCleanupConfiguration()
                .getCleanupIntervalSeconds() + " Seconds");

        scheduler.scheduleAtFixedRate(new GatewayValidationTask(),
                                      APIConstants.GatewayNotification.DEFAULT_CLEANUP_STARTUP_DELAY,
                                      configuration.getGatewayCleanupConfiguration().getCleanupIntervalSeconds(),
                                      TimeUnit.SECONDS);

        isRunning = true;
        log.info("Gateway cleanup scheduler started successfully");
    }

    /**
     * Stops the gateway cleanup scheduler and shuts down the executor service.
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
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Runnable task that performs the actual cleanup operations
     */
    private class GatewayValidationTask implements Runnable {

        @Override
        public void run() {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("Starting Gateway cleanup task");
                }

                long currentTime = System.currentTimeMillis();

                long expireTimeThreshold =
                        currentTime - (configuration.getGatewayCleanupConfiguration().getExpireTimeSeconds() * 1000L);

                Timestamp expireTimestamp = new Timestamp(expireTimeThreshold);

                int expiredCount = gatewayManagementDAO.updateExpiredGateways(expireTimestamp);

                if (log.isDebugEnabled() && (expiredCount > 0)) {
                    log.debug("Gateway cleanup completed - Expired: " + expiredCount);
                }

            } catch (APIManagementException e) {
                log.error("Error during Gateway cleanup task", e);
            }
        }
    }
}