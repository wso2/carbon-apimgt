/*
 *
 * Copyright (c) 2025 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.carbon.apimgt.api.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;



/**
 * This abstract class defines the contract for federated API discovery.
 * Subclasses should implement logic to discover APIs from a federated environment
 * based on the provided environment details.
 */
public abstract class FederatedAPIDiscovery {

    private static Log log = LogFactory.getLog(FederatedAPIDiscovery.class);
    private static final Map<String, ScheduledFuture<?>> scheduledDiscoveryTasks = new ConcurrentHashMap<>();


    private final ScheduledExecutorService scheduledExecutorService =
            Executors.newScheduledThreadPool(10, r -> new Thread(r,
                    "FederatedDiscoveryThread - " + getClass().getName()));
    /**
     * Initializes the federated API discovery with the given environment.
     *
     * @param environment The environment in which the discovery will take place.
     * @throws APIManagementException If an error occurs during initialization.
     */
    public abstract void init(Environment environment, List<String> apisDeployedInGatewayEnv, String organization)
            throws APIManagementException;

    /**
     * Discovers and invokes createAPI method for each API in the federated environment.
     *
     */
    public abstract void discoverAPI();

    /**
     * Schedules periodic discovery at a fixed interval.
     *
     * @param interval Interval between executions.
     */
    public void scheduleDiscovery(String environmentName, long interval) {
        log.debug("Scheduling federated API discovery every " + interval + " minutes. Class "
                + getClass().getName());
        if (scheduledDiscoveryTasks.containsKey(environmentName)) {
            log.debug("Cancel already scheduled federated API discovery for " + environmentName);
            scheduledDiscoveryTasks.get(environmentName).cancel(false);
        }
        log.debug("Scheduling federated API discovery for " + environmentName);
        ScheduledFuture<?> newTask = scheduledExecutorService.scheduleAtFixedRate(this::discoverAPI,
                0, interval, TimeUnit.MINUTES);
        scheduledDiscoveryTasks.put(environmentName, newTask);
    }

    /**
     * Shuts down the scheduler service.
     */
    public void shutdown() {
        log.debug("Shutting down federated API discovery");
        for (ScheduledFuture<?> task : scheduledDiscoveryTasks.values()) {
            if (!task.isCancelled()) {
                task.cancel(false);
            }
        }
        scheduledExecutorService.shutdown();
        try {
            if (!scheduledExecutorService.awaitTermination(60, TimeUnit.SECONDS)) {
                scheduledExecutorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.error("Error while shutting down federated API discovery scheduler", e);
            scheduledExecutorService.shutdownNow();
        }
    }

    public ScheduledFuture<?> getScheduledDiscoveryTask(String environmentName) {
        return scheduledDiscoveryTasks.get(environmentName);
    }
}
