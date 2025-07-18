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

package org.wso2.carbon.apimgt.gateway.scheduler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.gateway.internal.DataHolder;
import org.wso2.carbon.apimgt.impl.dto.GatewayArtifactSynchronizerProperties;
import org.wso2.carbon.apimgt.impl.dto.GatewayNotificationConfiguration;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.wso2.carbon.apimgt.impl.utils.APIUtil;

/**
 * This class is responsible for scheduling notify API calls every 300 seconds
 */
public class HealthCheckAPIScheduler {
    private static final Log log = LogFactory.getLog(HealthCheckAPIScheduler.class);
    private static int NOTIFY_INTERVAL = 30; // Default, will be overridden by config
    private static boolean heartbeatEnabled = true;
    private static String configuredGWID = ""; // Start with empty, will be set at runtime or config
    private final HealthCheckAPIClient healthCheckAPIClient;
    GatewayArtifactSynchronizerProperties gatewayArtifactSynchronizerProperties;
    GatewayNotificationConfiguration config;
    private ScheduledExecutorService scheduler;

    public HealthCheckAPIScheduler() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        healthCheckAPIClient = new HealthCheckAPIClient();
        config = org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder.getInstance()
                .getAPIManagerConfigurationService().getAPIManagerConfiguration().getGatewayNotificationConfiguration();
        gatewayArtifactSynchronizerProperties =
                org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder.getInstance()
                        .getAPIManagerConfiguration().getGatewayArtifactSynchronizerProperties();
        if (config != null) {
            heartbeatEnabled = config.isEnabled();
            NOTIFY_INTERVAL = config.getNotifyIntervalSeconds();
            if (config.getConfiguredGWID() != null && !config.getConfiguredGWID().isEmpty()) {
                configuredGWID = config.getConfiguredGWID();
            }
        }
    }

    public void start() {
        if (!heartbeatEnabled) {
            log.info("Gateway HealthCheck/Heartbeat scheduler is disabled by configuration.");
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("Executing Gateway registration logic .......");
        }
        try {
            String registrationPayload = buildRegistrationPayload(configuredGWID);
            String response = healthCheckAPIClient.notifyGateway(registrationPayload);
            String gwidFromResponse = healthCheckAPIClient.extractGatewayIdFromResponse(response);
            String status = healthCheckAPIClient.extractStatusFromResponse(response);
            if (gwidFromResponse != null && !gwidFromResponse.isEmpty() && "REGISTERED".equals(status)) {
                configuredGWID = gwidFromResponse;
                DataHolder.getInstance().setConfiguredGWID(gwidFromResponse);
                log.info("Gateway registered. GWID: " + configuredGWID);
            } else {
                log.error("Initial Gateway registration failed. Will retry on next run.");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            scheduler.scheduleAtFixedRate(new HealthCheckAPITask(), 30, NOTIFY_INTERVAL, TimeUnit.SECONDS);
        }
    }

    public void stop() {
        if (log.isDebugEnabled()) {
            log.debug("Stopping Notify API Scheduler");
        }

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
    }

    private String buildRegistrationPayload(String gwid) {
        long millis = java.time.Instant.now().toEpochMilli();
        // if gwid is null or empty we generate a new one using random UUID
        if (gwid == null || gwid.isEmpty()) {
            gwid = java.util.UUID.randomUUID().toString();
        }
        String gatewayLabelsStr = "\""+String.join("\",\"", gatewayArtifactSynchronizerProperties.getGatewayLabels())+"\"";
        String ipAddress = APIUtil.getHostAddress();

        return "{" + "\"payloadType\": \"register\"," + "\"gatewayProperties\": {\"ipAddress\": \"" + ipAddress + "\"},"
                + "\"environmentLabels\": [" + gatewayLabelsStr + "]," + "\"gatewayId\": \"" + gwid + "\","
                + "\"timeStamp\": " + millis + "}";
    }

    private String buildHeartbeatPayload(String gwid) {
        long millis = java.time.Instant.now().toEpochMilli();
        // Example: { "payloadType": "heartbeat", "gatewayId": "...", "timeStamp": ..., "gatewayProperties": {...}, "environmentLabels": [...] }
        return "{" + "\"payloadType\": \"heartbeat\"," + "\"gatewayId\": \"" + gwid + "\"," + "\"timeStamp\": " + millis
                + "}";
    }

    private class HealthCheckAPITask implements Runnable {
        @Override
        public void run() {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("Executing Gateway heartbeat logic .......");
                }
                String heartbeatPayload = buildHeartbeatPayload(configuredGWID);
                String response = healthCheckAPIClient.notifyGateway(heartbeatPayload);
                String status = healthCheckAPIClient.extractStatusFromResponse(response);
                if ("RE_REGISTER".equals(status)) {
                    // GWID not found or not acknowledged, re-register
                    log.warn("Gateway heartbeat not acknowledged. Re-registering gateway.");
                    String registrationPayload = buildRegistrationPayload(configuredGWID);
                    String regResponse = healthCheckAPIClient.notifyGateway(registrationPayload);
                    String gwid = healthCheckAPIClient.extractGatewayIdFromResponse(regResponse);
                    String regStatus = healthCheckAPIClient.extractStatusFromResponse(regResponse);
                    if (gwid != null && !gwid.isEmpty() && "RE-REGISTERED".equals(regStatus)) {
                        configuredGWID = gwid;
                        // Store in DataHolder
                        DataHolder.getInstance().setConfiguredGWID(gwid);
                        log.info("Gateway re-registered. New GWID: " + configuredGWID);
                    } else {
                        log.error("Gateway re-registration failed. Will retry on next run.");
                    }
                } else if ("ACKNOWLEDGED".equals(status)) {
                    if (log.isDebugEnabled()) {
                        log.debug("Heartbeat sent successfully for GWID: " + configuredGWID);
                    }
                } else {
                    log.error("Unexpected status received: " + status + ". Response: " + response);
                }
            } catch (Exception e) {
                log.error("Error occurred while executing Gateway HealthCheck/Registration logic", e);
            }
        }
    }
}
