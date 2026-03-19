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

package org.wso2.carbon.apimgt.internal.service.websocket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.PlatformGatewayDeploymentEventService;
import org.wso2.carbon.apimgt.api.model.PlatformGatewayDeploymentEventRecord;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Registers the WebSocket-based platform gateway deployment dispatcher when the Internal Data Service
 * webapp starts, so deploy/undeploy events are pushed to connected gateways.
 * Schedules periodic cleanup of delivered events and periodic push of pending events to already-connected
 * gateways (so CP2-updated events reach gateways connected to CP1).
 */
public class PlatformGatewayDeploymentDispatcherListener implements ServletContextListener {

    private static final Log log = LogFactory.getLog(PlatformGatewayDeploymentDispatcherListener.class);

    /** Retain delivered events for 24 hours before cleanup. */
    private static final long CLEANUP_RETENTION_MS = 24 * 60 * 60 * 1000L;
    /** Run cleanup every 6 hours. */
    private static final long CLEANUP_INTERVAL_HOURS = 6;
    /** Push pending events to connected gateways every 2 minutes. */
    private static final long PUSH_PENDING_INTERVAL_MINUTES = 2;
    /** Max wait for scheduler shutdown on context destroy (seconds). */
    private static final long SHUTDOWN_AWAIT_SECONDS = 10L;

    private ScheduledExecutorService cleanupScheduler;
    private ScheduledExecutorService pushScheduler;
    private volatile ScheduledFuture<?> cleanupFuture;
    private volatile ScheduledFuture<?> pushPendingFuture;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            WebSocketPlatformGatewayDeploymentDispatcher dispatcher = new WebSocketPlatformGatewayDeploymentDispatcher();
            ServiceReferenceHolder.getInstance().setPlatformGatewayDeploymentDispatcher(dispatcher);
            ServiceReferenceHolder.getInstance().setPlatformGatewayAPIKeyEventService(new PlatformGatewayAPIKeyEventServiceImpl());
            log.info("Platform gateway deployment dispatcher and API key event service registered (WebSocket push enabled)");
            cleanupScheduler = Executors.newScheduledThreadPool(1, r -> {
                Thread t = new Thread(r, "platform-gw-event-cleanup");
                t.setDaemon(true);
                return t;
            });
            pushScheduler = Executors.newScheduledThreadPool(1, r -> {
                Thread t = new Thread(r, "platform-gw-push-pending");
                t.setDaemon(true);
                return t;
            });
            scheduleEventTableCleanup();
            schedulePushPendingToConnectedGateways();
        } catch (Exception e) {
            log.warn("Could not register platform gateway deployment dispatcher: " + e.getMessage(), e);
        }
    }

    private void scheduleEventTableCleanup() {
        PlatformGatewayDeploymentEventService eventService =
                ServiceReferenceHolder.getInstance().getPlatformGatewayDeploymentEventService();
        if (eventService == null) {
            return;
        }
        cleanupFuture = cleanupScheduler.scheduleAtFixedRate(
                () -> {
                    try {
                        int deleted = eventService.cleanupDeliveredEventsOlderThan(CLEANUP_RETENTION_MS);
                        if (deleted > 0) {
                            log.info("Platform gateway deployment event cleanup: deleted " + deleted + " delivered row(s)");
                        }
                    } catch (Exception e) {
                        log.warn("Platform gateway deployment event cleanup failed: " + e.getMessage());
                    }
                },
                CLEANUP_INTERVAL_HOURS,
                CLEANUP_INTERVAL_HOURS,
                TimeUnit.HOURS);
        if (log.isDebugEnabled()) {
            log.debug("Scheduled AM_GW_PLATFORM_DEPLOYMENT_EVENT cleanup every " + CLEANUP_INTERVAL_HOURS + " hours");
        }
    }

    /**
     * Periodically push pending deployment events to gateways that are already connected to this CP.
     * Covers the case where CP2 triggered the deploy but the gateway is connected to CP1.
     */
    private void schedulePushPendingToConnectedGateways() {
        PlatformGatewayDeploymentEventService eventService =
                ServiceReferenceHolder.getInstance().getPlatformGatewayDeploymentEventService();
        if (eventService == null) {
            return;
        }
        pushPendingFuture = pushScheduler.scheduleAtFixedRate(
                () -> {
                    try {
                        pushPendingEventsToConnectedGateways(eventService);
                    } catch (Exception e) {
                        log.warn("Push pending deployment events failed: " + e.getMessage());
                    }
                },
                PUSH_PENDING_INTERVAL_MINUTES,
                PUSH_PENDING_INTERVAL_MINUTES,
                TimeUnit.MINUTES);
        if (log.isDebugEnabled()) {
            log.debug("Scheduled push pending events to connected gateways every " + PUSH_PENDING_INTERVAL_MINUTES + " minutes");
        }
    }

    private void pushPendingEventsToConnectedGateways(PlatformGatewayDeploymentEventService eventService) {
        PlatformGatewaySessionRegistry registry = PlatformGatewaySessionRegistry.getInstance();
        Set<String> connectedIds = registry.getConnectedGatewayIds();
        if (connectedIds.isEmpty()) {
            return;
        }
        for (String gatewayId : connectedIds) {
            try {
                List<PlatformGatewayDeploymentEventRecord> pending = eventService.getPendingEventsForGateway(gatewayId);
                if (pending.isEmpty()) {
                    continue;
                }
                List<String> idsToMark = new ArrayList<>(pending.size());
                for (PlatformGatewayDeploymentEventRecord record : pending) {
                    registry.sendToGateways(Collections.singleton(gatewayId), record.getPayload());
                    idsToMark.add(record.getId());
                }
                if (!idsToMark.isEmpty()) {
                    eventService.markDelivered(idsToMark);
                    if (log.isDebugEnabled()) {
                        log.debug("Pushed " + idsToMark.size() + " pending event(s) to connected gateway " + gatewayId);
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to push pending events for gateway " + gatewayId + ": " + e.getMessage());
            }
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (cleanupFuture != null) {
            cleanupFuture.cancel(false);
            cleanupFuture = null;
        }
        if (pushPendingFuture != null) {
            pushPendingFuture.cancel(false);
            pushPendingFuture = null;
        }
        shutdownScheduler(cleanupScheduler, "cleanup");
        cleanupScheduler = null;
        shutdownScheduler(pushScheduler, "push-pending");
        pushScheduler = null;
        try {
            ServiceReferenceHolder.getInstance().setPlatformGatewayDeploymentDispatcher(null);
            ServiceReferenceHolder.getInstance().setPlatformGatewayAPIKeyEventService(null);
            if (log.isDebugEnabled()) {
                log.debug("Platform gateway deployment dispatcher and API key event service unregistered");
            }
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Error unregistering platform gateway services", e);
            }
        }
    }

    private void shutdownScheduler(ScheduledExecutorService scheduler, String name) {
        if (scheduler == null) {
            return;
        }
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(SHUTDOWN_AWAIT_SECONDS, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
                if (log.isDebugEnabled()) {
                    log.debug("Platform gateway " + name + " scheduler did not terminate in time, forced shutdown");
                }
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
            if (log.isDebugEnabled()) {
                log.debug("Platform gateway " + name + " scheduler shutdown interrupted", e);
            }
        }
    }
}
