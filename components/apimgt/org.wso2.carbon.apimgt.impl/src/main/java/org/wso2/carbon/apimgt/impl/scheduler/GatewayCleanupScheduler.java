/*
 * Copyright (c) 2024, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.apimgt.impl.scheduler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.dao.GatewayManagementDAO;
import org.wso2.carbon.apimgt.impl.dto.GatewayCleanupConfiguration;
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
    private static GatewayCleanupScheduler instance;
    private ScheduledExecutorService scheduler;
    private GatewayManagementDAO gatewayManagementDAO;
    private GatewayCleanupConfiguration configuration;
    private boolean isRunning = false;
    // define sheduled task interval in seconds
    private static final int DEFAULT_CLEANUP_STARTUP_DELAY = 60;
    
    private GatewayCleanupScheduler() {
        this.gatewayManagementDAO = GatewayManagementDAO.getInstance();
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }
    
    public static synchronized GatewayCleanupScheduler getInstance() {
        if (instance == null) {
            instance = new GatewayCleanupScheduler();
        }
        return instance;
    }
    
    /**
     * Start the gateway cleanup scheduler
     */
    public void start() {
        if (isRunning) {
            log.warn("Gateway cleanup scheduler is already running");
            return;
        }
        
        try {
            // Get configuration from APIManagerConfiguration
            configuration = ServiceReferenceHolder.getInstance()
                .getAPIManagerConfigurationService()
                .getAPIManagerConfiguration()
                .getGatewayCleanupConfiguration();
            
            if (!configuration.isEnabled()) {
                log.info("Gateway cleanup scheduler is disabled in configuration");
                return;
            }
            
            log.info("Starting Gateway cleanup scheduler with interval: " + 
                    configuration.getCleanupIntervalSeconds() + " Seconds");
            
            // Schedule the cleanup task to run periodically
            scheduler.scheduleAtFixedRate(
                new GatewayCleanupTask(),
                DEFAULT_CLEANUP_STARTUP_DELAY,
                configuration.getCleanupIntervalSeconds(),
                java.util.concurrent.TimeUnit.SECONDS
            );
            
            isRunning = true;
            log.info("Gateway cleanup scheduler started successfully");
            
        } catch (Exception e) {
            log.error("Failed to start Gateway cleanup scheduler", e);
        }
    }
    
    /**
     * Stop the gateway cleanup scheduler
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
                
                // Calculate thresholds
                long expireTimeThreshold = currentTime - (configuration.getExpireTimeSeconds() * 1000L);
                long retentionThreshold = currentTime - (configuration.getDataRetentionPeriodSeconds() * 1000L);
                
                Timestamp expireTimestamp = new Timestamp(expireTimeThreshold);
                Timestamp retentionTimestamp = new Timestamp(retentionThreshold);

                int expiredCount = gatewayManagementDAO.updateExpiredGateways(expireTimestamp);

                int deletedCount = gatewayManagementDAO.deleteOldGatewayRecords(retentionTimestamp);

                
                if (log.isInfoEnabled()) {
                    log.info("Gateway cleanup completed - Expired: " + expiredCount + 
                            ", Deleted: " + deletedCount);
                }
                
            } catch (APIManagementException e) {
                log.error("Error during Gateway cleanup task", e);
            } catch (Exception e) {
                log.error("Unexpected error during Gateway cleanup task", e);
            }
        }
    }
    
    /**
     * Check if the scheduler is currently running
     */
    public boolean isRunning() {
        return isRunning;
    }
} 