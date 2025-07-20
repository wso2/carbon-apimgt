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

package org.wso2.carbon.apimgt.gateway.health;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.wso2.carbon.apimgt.gateway.internal.DataHolder;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.EventHubConfigurationDto;
import org.wso2.carbon.apimgt.impl.dto.GatewayArtifactSynchronizerProperties;
import org.wso2.carbon.apimgt.impl.dto.GatewayNotificationConfiguration;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * This class is responsible for gateway health monitoring, including scheduling
 * heartbeat notifications and handling gateway registration/re-registration.
 */
public class GatewayHealthMonitor {
    private static final Log log = LogFactory.getLog(GatewayHealthMonitor.class);
    private static int NOTIFY_INTERVAL = 30; // Default, will be overridden by config
    private static boolean heartbeatEnabled = true;
    private static String configuredGWID = ""; // Start with empty, will be set at runtime or config

    private final GatewayArtifactSynchronizerProperties gatewayArtifactSynchronizerProperties;
    private final GatewayNotificationConfiguration config;
    private ScheduledExecutorService scheduler;

    public GatewayHealthMonitor() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
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
            String response = notifyGateway(registrationPayload);
            String gwidFromResponse = extractGatewayIdFromResponse(response);
            String status = extractStatusFromResponse(response);
            if (gwidFromResponse != null && !gwidFromResponse.isEmpty() && APIConstants.GatewayNotification.STATUS_REGISTERED.equals(status)) {
                configuredGWID = gwidFromResponse;
                DataHolder.getInstance().setConfiguredGWID(gwidFromResponse);
                log.info("Gateway registered. GWID: " + configuredGWID);
            } else {
                log.error("Initial Gateway registration failed. Will retry on next run.");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            scheduler.scheduleAtFixedRate(new HealthCheckTask(), 30, NOTIFY_INTERVAL, TimeUnit.SECONDS);
        }
    }

    public void stop() {
        if (log.isDebugEnabled()) {
            log.debug("Stopping Gateway Health Monitor");
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
        String ipAddress = APIUtil.getHostAddress();

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();
        node.put(APIConstants.GatewayNotification.PAYLOAD_TYPE, APIConstants.GatewayNotification.PAYLOAD_TYPE_REGISTER);
        node.put(APIConstants.GatewayNotification.GATEWAY_ID, gwid);
        node.put(APIConstants.GatewayNotification.TIMESTAMP, millis);
        
        ObjectNode gatewayProperties = mapper.createObjectNode();
        gatewayProperties.put("ipAddress", ipAddress);
        node.set(APIConstants.GatewayNotification.GATEWAY_PROPERTIES, gatewayProperties);
        
        ArrayNode environmentLabels = mapper.createArrayNode();
        for (String label : gatewayArtifactSynchronizerProperties.getGatewayLabels()) {
            environmentLabels.add(label);
        }
        node.set(APIConstants.GatewayNotification.ENVIRONMENT_LABELS, environmentLabels);
        
        return node.toString();
    }

    private String buildHeartbeatPayload(String gwid) {
        long millis = java.time.Instant.now().toEpochMilli();
        
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();
        node.put(APIConstants.GatewayNotification.PAYLOAD_TYPE, APIConstants.GatewayNotification.PAYLOAD_TYPE_HEARTBEAT);
        node.put(APIConstants.GatewayNotification.GATEWAY_ID, gwid);
        node.put(APIConstants.GatewayNotification.TIMESTAMP, millis);
        
        return node.toString();
    }

    /**
     * Sends a notification (register or heartbeat) to /notify-gateway.
     * @param payload JSON payload as per NotifyGatewayPayload
     * @return response body as String
     */
    private String notifyGateway(String payload) {
        String endpoint = getServiceURL() + "/notify-gateway";
        try {
            HttpResponse response = executePost(endpoint, payload);
            int statusCode = response.getStatusLine().getStatusCode();
            String responseBody = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
            log.debug("/notify-gateway called. Status: " + statusCode + ", Response: " + responseBody);
            return responseBody;
        } catch (IOException e) {
            log.error("Error occurred while calling /notify-gateway", e);
            return null;
        }
    }

    private String extractGatewayIdFromResponse(String responseBody) {
        int idx = responseBody.indexOf("\"" + APIConstants.GatewayNotification.GATEWAY_ID + "\"");
        if (idx != -1) {
            int start = responseBody.indexOf(':', idx) + 1;
            int quote1 = responseBody.indexOf('"', start);
            int quote2 = responseBody.indexOf('"', quote1 + 1);
            if (quote1 != -1 && quote2 != -1) {
                return responseBody.substring(quote1 + 1, quote2);
            }
        }
        return null;
    }

    private String extractStatusFromResponse(String responseBody) {
        int idx = responseBody.indexOf("\"status\"");
        if (idx != -1) {
            int start = responseBody.indexOf(':', idx) + 1;
            int quote1 = responseBody.indexOf('"', start);
            int quote2 = responseBody.indexOf('"', quote1 + 1);
            if (quote1 != -1 && quote2 != -1) {
                return responseBody.substring(quote1 + 1, quote2);
            }
        }
        return null;
    }

    private HttpResponse executePost(String endpoint, String payload) throws IOException {
        URL url = new URL(endpoint);
        EventHubConfigurationDto config = getEventHubConfiguration();
        HttpClient httpClient = APIUtil.getHttpClient(url.getPort(), url.getProtocol());

        HttpPost request = new HttpPost(endpoint);
        request.setHeader(APIConstants.AUTHORIZATION_HEADER_DEFAULT, APIConstants.AUTHORIZATION_BASIC
                + new String(getServiceCredentials(config), StandardCharsets.UTF_8));
        request.setHeader(APIConstants.HEADER_CONTENT_TYPE, APIConstants.APPLICATION_JSON_MEDIA_TYPE);
        request.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));

        return httpClient.execute(request);
    }

    private String getServiceURL() {
        return getEventHubConfiguration().getServiceUrl().concat(APIConstants.INTERNAL_WEB_APP_EP);
    }

    private EventHubConfigurationDto getEventHubConfiguration() {
        return ServiceReferenceHolder.getInstance().getApiManagerConfigurationService()
                .getAPIManagerConfiguration().getEventHubConfigurationDto();
    }

    private byte[] getServiceCredentials(EventHubConfigurationDto config) {
        String credentials = config.getUsername() + APIConstants.DELEM_COLON + config.getPassword();
        return Base64.encodeBase64(credentials.getBytes(StandardCharsets.UTF_8));
    }

    private class HealthCheckTask implements Runnable {
        @Override
        public void run() {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("Executing Gateway heartbeat logic .......");
                }
                String heartbeatPayload = buildHeartbeatPayload(configuredGWID);
                String response = notifyGateway(heartbeatPayload);
                String status = extractStatusFromResponse(response);
                if (APIConstants.GatewayNotification.STATUS_RE_REGISTER.equals(status)) {
                    // GWID not found or not acknowledged, re-register
                    log.warn("Gateway heartbeat not acknowledged. Re-registering gateway.");
                    String registrationPayload = buildRegistrationPayload(configuredGWID);
                    String regResponse = notifyGateway(registrationPayload);
                    String gwid = extractGatewayIdFromResponse(regResponse);
                    String regStatus = extractStatusFromResponse(regResponse);
                    if (gwid != null && !gwid.isEmpty() && APIConstants.GatewayNotification.STATUS_REGISTERED.equals(regStatus)) {
                        configuredGWID = gwid;
                        // Store in DataHolder
                        DataHolder.getInstance().setConfiguredGWID(gwid);
                        log.info("Gateway re-registered with GWID: " + configuredGWID);
                    } else {
                        log.error("Gateway re-registration failed. Will retry on next run.");
                    }
                } else if (APIConstants.GatewayNotification.STATUS_ACKNOWLEDGED.equals(status)) {
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
